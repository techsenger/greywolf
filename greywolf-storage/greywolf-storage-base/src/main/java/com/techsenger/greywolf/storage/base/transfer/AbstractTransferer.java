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
import com.techsenger.greywolf.storage.base.GreyWolfStorage;
import com.techsenger.greywolf.storage.base.operation.ErrorResolution;
import com.techsenger.greywolf.storage.base.operation.RunControl;
import com.techsenger.shellfx.storage.base.OperationType;
import com.techsenger.shellfx.storage.base.UriUtils;
import java.io.IOException;
import java.net.URI;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;

/**
 * Executes a {@link TransferPlan} sequentially on a single virtual thread, reporting progress and asking for
 * conflict/error decisions through a {@link TransferListener}. Runs entirely against the {@link GreyWolfStorage}
 * abstraction, so source and target may belong to different storages. What happens to a single task - copying it,
 * moving it - is left to the subclass.
 *
 * @author Pavel Castornii
 */
public abstract class AbstractTransferer {

    private enum TaskOutcome {
        CONTINUE, CANCELLED, ABORTED
    }

    private static final int BUFFER_SIZE = 8192;

    private static final long PROGRESS_INTERVAL_MILLIS = 150;

    private final RunControl runControl = new RunControl();

    private int filesCopied;

    /**
     * File tasks (not directories) completed so far - unlike {@link #filesCopied}, which also counts
     * directories for the end-of-run summary, this is kept strictly in sync with {@link TransferPlan#totalFiles()}
     * (itself file-only) for {@link TransferProgress#filesDone()}, and is incremented right before the progress
     * update that reports a file as finished, never after.
     */
    private int filesDone;

    private int filesSkipped;

    private long bytesDone;

    private long startTime;

    private long lastProgressEmit;

    private long lastProgressEmitBytes;

    private @Nullable ConflictResolution stickyConflictResolution;

    private @Nullable ErrorResolution stickyErrorResolution;

    /**
     * Starts executing {@code plan} on a new virtual thread; returns immediately.
     *
     * @param plan the plan to execute.
     * @param listener receives progress and decision requests.
     */
    public void start(TransferPlan plan, TransferListener listener) {
        Thread.startVirtualThread(() -> run(plan, listener));
    }

    /**
     * Requests cancellation; the in-progress file (if any) is stopped and its partially-written target is
     * deleted, then {@link TransferListener#onCancelled()} is reported. Has no visible effect once the run has
     * already completed. Also wakes up a paused run, so a cancel requested while paused takes effect right away
     * instead of waiting for {@link #resume()} first.
     */
    public void cancel() {
        runControl.cancel();
    }

    /**
     * Suspends copying before the next file/chunk, without losing any progress - the current file's partially
     * written bytes are left as they are, and copying continues from exactly there on {@link #resume()}. Has no
     * visible effect once the run has already completed.
     */
    public void pause() {
        runControl.pause();
    }

    /**
     * Lets a {@link #pause()}d run continue. Has no effect if the run isn't currently paused.
     */
    public void resume() {
        runControl.resume();
    }

    /**
     * Transfers one task - a file or a directory - to {@code effectiveTarget}, which is already free to be written
     * to (a conflict has been resolved).
     *
     * @param task the task to transfer.
     * @param effectiveTarget the URI to transfer the task to.
     * @param targetStorage the storage {@code effectiveTarget} belongs to.
     * @param plan the plan being executed.
     * @param listener receives progress.
     * @throws IOException if the task can't be transferred.
     */
    protected abstract void transfer(TransferTask task, URI effectiveTarget, GreyWolfStorage targetStorage,
            TransferPlan plan, TransferListener listener) throws IOException;

    /**
     * Called once before the first task is processed.
     */
    protected void onRunStarted() {
        // nothing by default
    }

    /**
     * Tells whether {@code task} has already been taken care of by an earlier task, so it is neither checked for
     * conflicts nor transferred. A subclass that skips a task this way is responsible for reporting its progress.
     *
     * @param task the task about to be processed.
     * @param plan the plan being executed.
     * @param listener receives progress.
     * @return {@code true} to skip the task silently.
     */
    protected boolean isHandled(TransferTask task, TransferPlan plan, TransferListener listener) {
        return false;
    }

    /**
     * Called once after every task has been processed, right before the run is reported as completed.
     */
    protected void onRunCompleted() {
        // nothing by default
    }

    /**
     * Creates the directory at {@code targetUri}; an already existing one is not an error, since merging into it
     * is exactly what the tasks of its content do.
     *
     * @param targetStorage the storage to create the directory in.
     * @param targetUri the URI of the directory.
     * @throws IOException if the directory can't be created.
     */
    protected final void createDirectoryIfNeeded(GreyWolfStorage targetStorage, URI targetUri) throws IOException {
        try {
            targetStorage.createDirectory(targetUri, OperationType.SECONDARY);
        } catch (FileAlreadyExistsException ex) {
            // Merging into an existing directory is not an error - its own contents are separate tasks.
        }
    }

    /**
     * Copies the content of a file task stream to stream, honouring pause and cancel and reporting progress.
     *
     * @param task the file task.
     * @param effectiveTarget the URI to write to.
     * @param targetStorage the storage {@code effectiveTarget} belongs to.
     * @param plan the plan being executed.
     * @param listener receives progress.
     * @throws IOException if reading or writing fails.
     */
    protected final void copyFile(TransferTask task, URI effectiveTarget, GreyWolfStorage targetStorage,
            TransferPlan plan, TransferListener listener) throws IOException {
        var sourceStorage = task.source().getStorage();
        var totalSize = task.source().getSize() == null ? 0L : task.source().getSize();
        var fileBytesDone = 0L;
        try (var in = sourceStorage.openInputStream(task.source().getUri(), OperationType.SECONDARY);
                var out = targetStorage.openOutputStream(effectiveTarget, OperationType.SECONDARY)) {
            var buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = in.read(buffer)) >= 0) {
                runControl.awaitIfPaused();
                if (runControl.isCancelled()) {
                    throw new CancellationException("Transfer cancelled");
                }
                out.write(buffer, 0, read);
                fileBytesDone += read;
                bytesDone += read;
                maybeEmitProgress(task, effectiveTarget, fileBytesDone, totalSize, plan, listener, false);
            }
        }
        filesDone++;
        maybeEmitProgress(task, effectiveTarget, fileBytesDone, totalSize, plan, listener, true);
    }

    /**
     * Accounts for a file that was transferred without reading its bytes (e.g. moved in place) and reports it.
     *
     * @param task the file task.
     * @param effectiveTarget the URI the file ended up at.
     * @param plan the plan being executed.
     * @param listener receives progress.
     */
    protected final void completeFileWithoutCopy(TransferTask task, URI effectiveTarget, TransferPlan plan,
            TransferListener listener) {
        var size = task.source().getSize() == null ? 0L : task.source().getSize();
        bytesDone += size;
        filesDone++;
        maybeEmitProgress(task, effectiveTarget, size, size, plan, listener, true);
    }

    /**
     * Tells whether something exists at {@code targetUri}; a failed probe counts as "doesn't exist".
     *
     * @param targetStorage the storage to look in.
     * @param targetUri the URI to look at.
     * @return {@code true} if an entry exists there.
     */
    protected final boolean targetExists(GreyWolfStorage targetStorage, URI targetUri) {
        try {
            targetStorage.getFile(targetUri, OperationType.SECONDARY);
            return true;
        } catch (NoSuchFileException ex) {
            return false;
        } catch (Exception ex) {
            // Best-effort probe; a genuine permission problem will resurface when the copy itself is attempted.
            return false;
        }
    }

    private void run(TransferPlan plan, TransferListener listener) {
        var failures = new ArrayList<>(plan.skippedEntries());
        filesSkipped = failures.size();
        filesCopied = 0;
        filesDone = 0;
        bytesDone = 0L;
        startTime = System.currentTimeMillis();
        lastProgressEmit = startTime;
        lastProgressEmitBytes = 0L;
        var targetStorage = plan.targetStorage();
        onRunStarted();

        for (var task : plan.tasks()) {
            runControl.awaitIfPaused();
            if (runControl.isCancelled()) {
                listener.onCancelled();
                return;
            }
            var outcome = processTask(task, targetStorage, plan, listener, failures);
            if (outcome == TaskOutcome.CANCELLED) {
                listener.onCancelled();
                return;
            }
            if (outcome == TaskOutcome.ABORTED) {
                return;
            }
        }
        onRunCompleted();
        listener.onCompleted(filesCopied, filesSkipped, failures);
    }

    private TaskOutcome processTask(TransferTask task, GreyWolfStorage targetStorage, TransferPlan plan,
            TransferListener listener, List<FailedOperation> failures) {
        if (isHandled(task, plan, listener)) {
            return TaskOutcome.CONTINUE;
        }
        if (task.targetUri().equals(task.source().getUri())) {
            // Transferring an entry onto itself would destroy it (overwrite truncates it, move then deletes it),
            // and asking about the "existing" target would be pointless.
            filesSkipped++;
            return TaskOutcome.CONTINUE;
        }
        var effectiveTarget = resolveConflict(task, targetStorage, listener);
        if (effectiveTarget == null) {
            filesSkipped++;
            return TaskOutcome.CONTINUE;
        }
        while (true) {
            runControl.awaitIfPaused();
            if (runControl.isCancelled()) {
                return TaskOutcome.CANCELLED;
            }
            try {
                transfer(task, effectiveTarget, targetStorage, plan, listener);
                filesCopied++;
                return TaskOutcome.CONTINUE;
            } catch (Exception ex) {
                if (runControl.isCancelled()) {
                    cleanupPartialFile(targetStorage, effectiveTarget, task);
                    return TaskOutcome.CANCELLED;
                }
                var failure = new FailedOperation(task, ex, describeError(ex));
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
                    // Unlike OperationErrorDialog, this alert has no separate path field, so the path is
                    // folded back into this one line instead of being dropped.
                    listener.onAborted(failure.message() + " " + task.source().getUri());
                    return TaskOutcome.ABORTED;
                }
                failures.add(failure);
                filesSkipped++;
                return TaskOutcome.CONTINUE;
            }
        }
    }

    private @Nullable URI resolveConflict(TransferTask task, GreyWolfStorage targetStorage,
            TransferListener listener) {
        if (!targetExists(targetStorage, task.targetUri())) {
            return task.targetUri();
        }
        if (stickyConflictResolution != null) {
            return applyResolution(stickyConflictResolution, task, targetStorage);
        }
        var decision = listener.onConflict(new CopyConflict(task));
        if (decision.applyToAll()) {
            stickyConflictResolution = decision.resolution();
        }
        return applyResolution(decision.resolution(), task, targetStorage);
    }

    private @Nullable URI applyResolution(ConflictResolution resolution, TransferTask task,
            GreyWolfStorage targetStorage) {
        return switch (resolution) {
            case OVERWRITE -> task.targetUri();
            case SKIP -> null;
            case RENAME -> findAvailableTargetUri(task, targetStorage);
        };
    }

    /**
     * Finds the first available name for {@code task}'s target by appending {@code " (1)"}, {@code " (2)"}, etc.
     * before the extension (or at the end, for a directory) until one doesn't already exist under the target's
     * parent directory - e.g. {@code boom.txt} becomes {@code boom (1).txt}, then {@code boom (2).txt}, and so on.
     */
    private URI findAvailableTargetUri(TransferTask task, GreyWolfStorage targetStorage) {
        var isDirectory = task.source().isDirectory();
        // A task's target is always nested inside the target directory, so it can never be the storage root
        // itself - getParentUri() only returns null for the root.
        var parentUri = Objects.requireNonNull(UriUtils.getParentUri(targetStorage.getUri(), task.targetUri()));
        var segments = UriUtils.getPathSegments(targetStorage.getUri(), task.targetUri());
        var name = segments.get(segments.size() - 1);
        var dotIndex = isDirectory ? -1 : name.lastIndexOf('.');
        var baseName = dotIndex > 0 ? name.substring(0, dotIndex) : name;
        var extension = dotIndex > 0 ? name.substring(dotIndex) : "";
        var attempt = 1;
        URI candidateUri;
        do {
            candidateUri = UriUtils.resolvePath(parentUri, baseName + " (" + attempt + ")" + extension, isDirectory);
            attempt++;
        } while (targetExists(targetStorage, candidateUri));
        return candidateUri;
    }

    private void maybeEmitProgress(TransferTask task, URI effectiveTarget, long fileBytesDone, long fileBytesTotal,
            TransferPlan plan, TransferListener listener, boolean force) {
        var now = System.currentTimeMillis();
        if (!force && now - lastProgressEmit < PROGRESS_INTERVAL_MILLIS) {
            return;
        }
        var elapsed = now - startTime;
        var averageSpeed = elapsed <= 0 ? 0L : bytesDone * 1000L / elapsed;
        var intervalMillis = now - lastProgressEmit;
        var intervalBytes = bytesDone - lastProgressEmitBytes;
        var currentSpeed = intervalMillis <= 0 ? averageSpeed : intervalBytes * 1000L / intervalMillis;
        lastProgressEmit = now;
        lastProgressEmitBytes = bytesDone;
        var remainingBytes = plan.totalBytes() - bytesDone;
        var estimatedRemaining = averageSpeed <= 0 ? -1L : remainingBytes * 1000L / averageSpeed;
        listener.onProgress(new TransferProgress(filesDone, plan.totalFiles(), bytesDone, plan.totalBytes(),
                task.source().getUri(), effectiveTarget, fileBytesDone, fileBytesTotal, elapsed, estimatedRemaining,
                currentSpeed, averageSpeed));
    }

    private void cleanupPartialFile(GreyWolfStorage targetStorage, URI targetUri, TransferTask task) {
        if (task.source().isDirectory()) {
            return;
        }
        try {
            targetStorage.delete(targetUri, OperationType.PRIMARY);
        } catch (IOException ex) {
            // Best-effort cleanup; nothing more to do if it fails.
        }
    }

    /**
     * Describes {@code ex} without naming the failed file - callers that show this alongside a separate path
     * field (e.g. {@code OperationErrorDialog}) would otherwise show the same file identified twice.
     */
    private String describeError(Exception ex) {
        if (ex instanceof AccessDeniedException) {
            return "Access denied while transferring the file";
        }
        if (ex instanceof NoSuchFileException) {
            return "The file no longer exists";
        }
        return "Couldn't transfer the file: " + ex.getMessage();
    }
}
