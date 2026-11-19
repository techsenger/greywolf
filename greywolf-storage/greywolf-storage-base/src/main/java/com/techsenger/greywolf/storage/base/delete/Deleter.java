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
import com.techsenger.greywolf.storage.base.operation.ErrorResolution;
import com.techsenger.greywolf.storage.base.operation.RunControl;
import com.techsenger.shellfx.storage.base.OperationType;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;

/**
 * Executes a {@link DeletePlan} sequentially on a single virtual thread, deleting one entry at a time through its
 * storage and reporting progress and failures through a {@link DeleteListener}. Works the same way for every
 * storage, local or remote, since it only relies on deleting a single entry.
 *
 * @author Pavel Castornii
 */
public final class Deleter {

    private enum EntryOutcome {
        CONTINUE, CANCELLED, ABORTED
    }

    private static final long PROGRESS_INTERVAL_MILLIS = 150;

    private final RunControl runControl = new RunControl();

    private int itemsDone;

    private int deleted;

    private int skipped;

    private long bytesDone;

    private long startTime;

    private long lastProgressEmit;

    private @Nullable ErrorResolution stickyErrorResolution;

    /**
     * Starts executing {@code plan} on a new virtual thread; returns immediately.
     *
     * @param plan the plan to execute.
     * @param listener receives progress and decision requests.
     */
    public void start(DeletePlan plan, DeleteListener listener) {
        Thread.startVirtualThread(() -> run(plan, listener));
    }

    /**
     * Requests cancellation; the run stops before the next entry and reports
     * {@link DeleteListener#onCancelled()}. Entries already deleted stay deleted. Also wakes up a paused run.
     */
    public void cancel() {
        runControl.cancel();
    }

    /**
     * Suspends deleting before the next entry, without losing any progress.
     */
    public void pause() {
        runControl.pause();
    }

    /**
     * Lets a paused run continue.
     */
    public void resume() {
        runControl.resume();
    }

    private void run(DeletePlan plan, DeleteListener listener) {
        var failures = new ArrayList<FailedDeletion>();
        startTime = System.currentTimeMillis();
        lastProgressEmit = 0L;
        for (var entry : plan.entries()) {
            runControl.awaitIfPaused();
            if (runControl.isCancelled()) {
                listener.onCancelled();
                return;
            }
            var outcome = processEntry(entry, plan, listener, failures);
            if (outcome == EntryOutcome.CANCELLED) {
                listener.onCancelled();
                return;
            }
            if (outcome == EntryOutcome.ABORTED) {
                return;
            }
        }
        if (!plan.entries().isEmpty()) {
            emitProgress(plan.entries().get(plan.entries().size() - 1), plan, listener);
        }
        listener.onCompleted(deleted, skipped, failures);
    }

    private EntryOutcome processEntry(GreyWolfFile entry, DeletePlan plan, DeleteListener listener,
            List<FailedDeletion> failures) {
        while (true) {
            runControl.awaitIfPaused();
            if (runControl.isCancelled()) {
                return EntryOutcome.CANCELLED;
            }
            maybeEmitProgress(entry, plan, listener);
            try {
                deleteEntry(entry);
                var size = entry.getSize();
                bytesDone += entry.isDirectory() || size == null ? 0L : size;
                deleted++;
                itemsDone++;
                return EntryOutcome.CONTINUE;
            } catch (Exception ex) {
                var failure = new FailedDeletion(entry, ex, describeError(ex));
                var resolution = stickyErrorResolution == ErrorResolution.SKIP_ALL
                        ? ErrorResolution.SKIP : listener.onError(failure);
                if (resolution == ErrorResolution.SKIP_ALL) {
                    stickyErrorResolution = ErrorResolution.SKIP_ALL;
                    resolution = ErrorResolution.SKIP;
                }
                if (resolution == ErrorResolution.RETRY) {
                    continue;
                }
                if (resolution == ErrorResolution.ABORT) {
                    // Unlike OperationErrorDialog, the abort alert has no separate path field, so the path is
                    // folded back into this one line instead of being dropped.
                    listener.onAborted(failure.message() + " " + entry.getUri());
                    return EntryOutcome.ABORTED;
                }
                failures.add(failure);
                skipped++;
                itemsDone++;
                return EntryOutcome.CONTINUE;
            }
        }
    }

    /**
     * Deletes a single entry; an entry that is already gone counts as deleted, since that is the state the user
     * asked for.
     */
    private void deleteEntry(GreyWolfFile entry) throws IOException {
        var storage = entry.getStorage();
        try {
            storage.delete(entry.getUri(), OperationType.PRIMARY);
        } catch (NoSuchFileException ex) {
            // Already gone - nothing left to do.
        }
    }

    private void maybeEmitProgress(GreyWolfFile entry, DeletePlan plan, DeleteListener listener) {
        var now = System.currentTimeMillis();
        if (now - lastProgressEmit < PROGRESS_INTERVAL_MILLIS) {
            return;
        }
        lastProgressEmit = now;
        emitProgress(entry, plan, listener);
    }

    private void emitProgress(GreyWolfFile entry, DeletePlan plan, DeleteListener listener) {
        var elapsed = System.currentTimeMillis() - startTime;
        var itemsPerSecond = elapsed <= 0 ? 0.0 : itemsDone * 1000.0 / elapsed;
        var totalItems = plan.entries().size();
        var estimatedRemaining = itemsPerSecond <= 0 ? -1L
                : Math.round((totalItems - itemsDone) * 1000.0 / itemsPerSecond);
        listener.onProgress(new DeleteProgress(itemsDone, totalItems, bytesDone, plan.totalBytes(), entry.getUri(),
                elapsed, estimatedRemaining, itemsPerSecond));
    }

    /**
     * Describes {@code ex} without naming the failed entry - callers show this alongside a separate path field,
     * so naming it here would identify the same entry twice.
     */
    private String describeError(Exception ex) {
        if (ex instanceof AccessDeniedException) {
            return "Access denied while deleting";
        }
        if (ex instanceof DirectoryNotEmptyException) {
            return "The folder is not empty";
        }
        return "Couldn't delete: " + ex.getMessage();
    }
}
