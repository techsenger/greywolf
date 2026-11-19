/*
 * Copyright 2026 Pavel Castornii.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.techsenger.greywolf.storage.base.transfer;

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.base.GreyWolfStorage;
import com.techsenger.greywolf.storage.base.operation.AbstractScanner;
import com.techsenger.greywolf.storage.base.operation.NameMask;
import com.techsenger.greywolf.storage.base.operation.ScanListener;
import com.techsenger.shellfx.storage.base.OperationType;
import com.techsenger.shellfx.storage.base.UriUtils;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Recursively builds a {@link TransferPlan} from a list of source entries and a target directory (where a single
 * source may get a new name), without touching
 * any data: lists directories via {@link GreyWolfStorage#getFiles(URI)}, sums up bytes/counts, and excludes
 * symbolic links (recorded as {@link FailedOperation}s) instead of copying or following them.
 *
 * <p>Conflict detection is deliberately not done here: whether a target already exists is checked lazily as each
 * task is processed, so the user is asked in the same order tasks are executed instead of up front in a separate
 * pass.
 *
 * @author Pavel Castornii
 */
public final class TransferScanner extends AbstractScanner<TransferPlan> {

    /**
     * Scans {@code sources} into a {@link TransferPlan} on a new virtual thread and reports it via
     * {@link ScanListener#onScanCompleted}; returns immediately.
     *
     * @param sources the marked entries to copy, in the order they should be copied.
     * @param targetDirectory the directory to copy into.
     * @param targetStorage the storage {@code targetDirectory} belongs to.
     * @param mask applied to every entry's name, at every level of the tree; a non-matching directory is
     *     excluded entirely, along with everything inside it.
     * @param listener receives scan progress and the resulting plan.
     */
    public void scan(List<GreyWolfFile> sources, GreyWolfFile targetDirectory, GreyWolfStorage targetStorage,
            NameMask mask, ScanListener<TransferPlan> listener) {
        scan(sources, targetDirectory, null, targetStorage, mask, listener);
    }

    /**
     * Same as {@link #scan(List, GreyWolfFile, GreyWolfStorage, NameMask, ScanListener)}, but the single source
     * can get a new name in the target directory.
     *
     * @param targetName the name the source gets in {@code targetDirectory}, or {@code null} to keep its name;
     *     only allowed for exactly one source.
     */
    public void scan(List<GreyWolfFile> sources, GreyWolfFile targetDirectory, @Nullable String targetName,
            GreyWolfStorage targetStorage, NameMask mask, ScanListener<TransferPlan> listener) {
        if (targetName != null && sources.size() != 1) {
            throw new IllegalArgumentException("A new name can be given to a single source only");
        }
        start(listener, () -> buildPlan(sources, targetDirectory, targetName, targetStorage, mask, listener));
    }

    private @Nullable TransferPlan buildPlan(List<GreyWolfFile> sources, GreyWolfFile targetDirectory,
            @Nullable String targetName, GreyWolfStorage targetStorage, NameMask mask,
            ScanListener<TransferPlan> listener) throws IOException {
        for (var source : sources) {
            if (source.isDirectory() && isSelfOrAncestor(source.getUri(), targetDirectory.getUri())) {
                throw new IllegalArgumentException(
                        "Can't transfer '" + source.getName() + "' into itself or one of its own subfolders.");
            }
        }
        var tasks = new ArrayList<TransferTask>();
        var skipped = new ArrayList<FailedOperation>();
        for (var source : sources) {
            if (checkpoint()) {
                return null;
            }
            if (!mask.matches(source.getName())) {
                continue;
            }
            var name = targetName != null ? targetName : source.getName();
            var targetUri = UriUtils.resolvePath(targetDirectory.getUri(), name, source.isDirectory());
            scanEntry(source, targetUri, mask, tasks, skipped, listener);
        }
        if (isCancelled()) {
            return null;
        }
        return new TransferPlan(tasks, targetStorage, getBytesFound(), getFilesFound(), getFoldersFound(), skipped);
    }

    private void scanEntry(GreyWolfFile source, URI targetUri, NameMask mask, List<TransferTask> tasks,
            List<FailedOperation> skipped, ScanListener<TransferPlan> listener) throws IOException {
        if (checkpoint()) {
            return;
        }
        var task = new TransferTask(source, targetUri);
        if (source.isLink()) {
            skipped.add(new FailedOperation(task, new UnsupportedOperationException("Links are not supported"),
                    "Links are not supported: '" + source.getName() + "'."));
            return;
        }
        tasks.add(task);
        if (source.isDirectory()) {
            countFolder(listener);
            var sourceStorage = source.getStorage();
            for (var child : sourceStorage.getFiles(source.getUri(), OperationType.SECONDARY)) {
                if (!mask.matches(child.getName())) {
                    continue;
                }
                var childTarget = UriUtils.resolvePath(targetUri, child.getName(), child.isDirectory());
                scanEntry(child, childTarget, mask, tasks, skipped, listener);
                if (isCancelled()) {
                    return;
                }
            }
        } else {
            countFile(source.getSize(), listener);
        }
    }

    private boolean isSelfOrAncestor(URI candidateAncestor, URI target) {
        var ancestor = candidateAncestor.toString();
        var value = target.toString();
        return value.equals(ancestor) || value.startsWith(ancestor.endsWith("/") ? ancestor : ancestor + "/");
    }
}
