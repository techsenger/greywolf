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

package com.techsenger.greywolf.storage.base.delete;

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.base.operation.AbstractScanner;
import com.techsenger.greywolf.storage.base.operation.ScanListener;
import com.techsenger.shellfx.storage.base.OperationType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Recursively builds a {@link DeletePlan} from a list of entries without touching any data: lists folders through
 * their storage and orders every folder after its contents. A link (a symbolic link or a Windows junction) is
 * planned as a single entry and never followed, so deleting it can't reach anything outside the selection. One
 * instance scans exactly once.
 *
 * @author Pavel Castornii
 */
public final class DeleteScanner extends AbstractScanner<DeletePlan> {

    /**
     * Scans {@code sources} into a {@link DeletePlan} on a new virtual thread and reports the outcome to
     * {@code listener}; returns immediately.
     *
     * @param sources the entries to delete, in the order they should be deleted.
     * @param listener receives scan progress and the resulting plan.
     */
    public void scan(List<? extends GreyWolfFile> sources, ScanListener<DeletePlan> listener) {
        start(listener, () -> buildPlan(sources, listener));
    }

    private @Nullable DeletePlan buildPlan(List<? extends GreyWolfFile> sources, ScanListener<DeletePlan> listener)
            throws IOException {
        var entries = new ArrayList<GreyWolfFile>();
        for (var source : sources) {
            scanEntry(source, entries, listener);
            if (isCancelled()) {
                return null;
            }
        }
        return new DeletePlan(entries, getBytesFound(), getFilesFound(), getFoldersFound());
    }

    private void scanEntry(GreyWolfFile entry, List<GreyWolfFile> entries, ScanListener<DeletePlan> listener)
            throws IOException {
        if (checkpoint()) {
            return;
        }
        // a link is never entered, even if it points to a directory: it is not a directory itself
        if (entry.isDirectory()) {
            countFolder(listener);
            var storage = entry.getStorage();
            for (var child : storage.getFiles(entry.getUri(), OperationType.SECONDARY)) {
                scanEntry(child, entries, listener);
                if (isCancelled()) {
                    return;
                }
            }
            entries.add(entry);
        } else {
            entries.add(entry);
            countFile(entry.getSize(), listener);
        }
    }
}
