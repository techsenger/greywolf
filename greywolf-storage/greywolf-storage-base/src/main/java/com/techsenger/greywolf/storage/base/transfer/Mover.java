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

import com.techsenger.greywolf.storage.base.GreyWolfStorage;
import com.techsenger.shellfx.storage.base.OperationType;
import com.techsenger.shellfx.storage.base.UriUtils;
import java.io.IOException;
import java.net.URI;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Moves every task of a {@link TransferPlan}. An entry that stays within one storage and has no existing target
 * is moved in place by the storage itself - renamed if its parent directory stays the same, moved otherwise; a
 * directory moved this way takes its whole content along, so the tasks of that content are skipped. Everything
 * else - another storage, an existing target, or an entry the storage can't move in place - is copied and then
 * deleted from the source: files right away, directories at the end of the run, deepest first and only if they
 * ended up empty.
 *
 * @author Pavel Castornii
 */
public final class Mover extends AbstractTransferer {

    private final List<URI> movedDirectories = new ArrayList<>();

    private final List<TransferTask> copiedDirectories = new ArrayList<>();

    @Override
    protected void onRunStarted() {
        movedDirectories.clear();
        copiedDirectories.clear();
    }

    @Override
    protected boolean isHandled(TransferTask task, TransferPlan plan, TransferListener listener) {
        var sourceUri = task.source().getUri().toString();
        for (var moved : movedDirectories) {
            var prefix = moved.toString().endsWith("/") ? moved.toString() : moved + "/";
            if (sourceUri.startsWith(prefix)) {
                if (!task.source().isDirectory()) {
                    completeFileWithoutCopy(task, task.targetUri(), plan, listener);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    protected void transfer(TransferTask task, URI effectiveTarget, GreyWolfStorage targetStorage,
            TransferPlan plan, TransferListener listener) throws IOException {
        var source = task.source();
        var sourceStorage = source.getStorage();
        if (sourceStorage == targetStorage && moveInPlace(targetStorage, source.getUri(), effectiveTarget)) {
            if (source.isDirectory()) {
                movedDirectories.add(source.getUri());
            } else {
                completeFileWithoutCopy(task, effectiveTarget, plan, listener);
            }
            return;
        }
        if (source.isDirectory()) {
            createDirectoryIfNeeded(targetStorage, effectiveTarget);
            copiedDirectories.add(task);
        } else {
            copyFile(task, effectiveTarget, targetStorage, plan, listener);
            sourceStorage.delete(source.getUri(), OperationType.PRIMARY);
        }
    }

    @Override
    protected void onRunCompleted() {
        for (var i = copiedDirectories.size() - 1; i >= 0; i--) {
            var source = copiedDirectories.get(i).source();
            try {
                source.getStorage().delete(source.getUri(), OperationType.PRIMARY);
            } catch (IOException ex) {
                // Not empty (skipped, failed or masked-out entries) or not deletable - the directory stays.
            }
        }
    }

    /**
     * Tries to move the entry in place: renames it if the target is in the same directory, moves it otherwise.
     * Returns {@code false} when the storage can't do it that way (an existing target, another file system), so
     * the caller falls back to copying. A missing or inaccessible entry is not hidden.
     */
    private boolean moveInPlace(GreyWolfStorage storage, URI sourceUri, URI targetUri) throws IOException {
        try {
            var storageUri = storage.getUri();
            if (Objects.equals(UriUtils.getParentUri(storageUri, sourceUri),
                    UriUtils.getParentUri(storageUri, targetUri))) {
                var segments = UriUtils.getPathSegments(storageUri, targetUri);
                storage.renameFile(sourceUri, segments.get(segments.size() - 1), OperationType.SECONDARY);
            } else {
                storage.move(sourceUri, targetUri, OperationType.SECONDARY);
            }
            return true;
        } catch (NoSuchFileException | AccessDeniedException ex) {
            throw ex;
        } catch (IOException ex) {
            // An existing target (overwrite or merge) and an entry that can't be moved in place both end up here.
            return false;
        }
    }
}
