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

package com.techsenger.greywolf.storage.base.operation;

import com.techsenger.annotations.Nullable;
import java.util.concurrent.Callable;

/**
 * Base of the scanners that walk the selected entries to build the plan of an operation: runs the walk on a
 * virtual thread, reports its outcome, supports pausing and cancelling it, and counts what it finds with throttled
 * progress reports. One instance scans exactly once - {@link #cancel()} only ever needs to affect that single run.
 *
 * @param <P> the type of the plan the scan produces.
 * @author Pavel Castornii
 */
public abstract class AbstractScanner<P> {

    private static final long PROGRESS_INTERVAL_MILLIS = 150;

    private final RunControl runControl = new RunControl();

    private int foldersFound;

    private int filesFound;

    private long bytesFound;

    private long lastProgressEmit;

    /**
     * Requests cancellation; scanning stops early, at the next entry it checks, and reports
     * {@link ScanListener#onScanCancelled()}. Has no visible effect once scanning has already completed. Also wakes
     * up a paused scan, so a cancel requested while paused takes effect right away instead of waiting for
     * {@link #resume()} first.
     */
    public void cancel() {
        runControl.cancel();
    }

    /**
     * Suspends scanning before the next entry, without losing any progress found so far. Has no visible effect
     * once scanning has already completed.
     */
    public void pause() {
        runControl.pause();
    }

    /**
     * Lets a {@link #pause()}d scan continue. Has no effect if the scan isn't currently paused.
     */
    public void resume() {
        runControl.resume();
    }

    /**
     * Runs {@code planBuilder} on a new virtual thread and reports its outcome to {@code listener}: the plan it
     * returns, its failure, or - when it returns {@code null} - a cancellation. Returns immediately.
     *
     * @param listener receives the outcome of the scan.
     * @param planBuilder walks the entries and returns the plan, or {@code null} if the scan was cancelled.
     */
    protected final void start(ScanListener<P> listener, Callable<@Nullable P> planBuilder) {
        Thread.startVirtualThread(() -> {
            @Nullable P plan;
            try {
                plan = planBuilder.call();
            } catch (Exception ex) {
                listener.onScanFailed(ex);
                return;
            }
            if (plan == null) {
                listener.onScanCancelled();
                return;
            }
            listener.onScanCompleted(plan);
        });
    }

    /**
     * Waits while the scan is paused. Meant to be called before every entry the walk looks at.
     *
     * @return {@code true} if the scan was cancelled and the walk must stop.
     */
    protected final boolean checkpoint() {
        runControl.awaitIfPaused();
        return runControl.isCancelled();
    }

    protected final boolean isCancelled() {
        return runControl.isCancelled();
    }

    /**
     * Counts a folder the walk has found and reports progress if enough time has passed since the last report.
     *
     * @param listener receives the progress.
     */
    protected final void countFolder(ScanListener<P> listener) {
        foldersFound++;
        maybeEmitProgress(listener);
    }

    /**
     * Counts a file the walk has found and reports progress if enough time has passed since the last report.
     *
     * @param size the size of the file in bytes, or {@code null} if unknown.
     * @param listener receives the progress.
     */
    protected final void countFile(@Nullable Long size, ScanListener<P> listener) {
        filesFound++;
        bytesFound += size == null ? 0L : size;
        maybeEmitProgress(listener);
    }

    protected final int getFoldersFound() {
        return foldersFound;
    }

    protected final int getFilesFound() {
        return filesFound;
    }

    protected final long getBytesFound() {
        return bytesFound;
    }

    private void maybeEmitProgress(ScanListener<P> listener) {
        var now = System.currentTimeMillis();
        if (now - lastProgressEmit < PROGRESS_INTERVAL_MILLIS) {
            return;
        }
        lastProgressEmit = now;
        listener.onScanProgress(foldersFound, filesFound, bytesFound);
    }
}
