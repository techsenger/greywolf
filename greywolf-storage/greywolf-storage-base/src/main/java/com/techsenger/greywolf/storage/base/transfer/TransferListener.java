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

import com.techsenger.greywolf.storage.base.operation.ErrorResolution;
import java.util.List;

/**
 * Reports {@link AbstractTransferer#start} progress and asks for decisions when one is needed; every method is
 * invoked directly from the copying virtual thread, never the JavaFX application thread, so implementations
 * must hop onto it themselves (e.g. via {@code UiExecutor.execute}) before touching any view.
 *
 * <p>{@link #onConflict} and {@link #onError} block the calling thread until they return, which is safe here
 * since the caller always runs on a virtual thread.
 *
 * @author Pavel Castornii
 */
public interface TransferListener {

    /**
     * Asks how to resolve a target that already exists.
     *
     * @param conflict the conflicting task.
     * @return the user's decision.
     */
    ConflictDecision onConflict(CopyConflict conflict);

    /**
     * Reports throttled progress of an in-progress copy.
     *
     * @param progress the current progress snapshot.
     */
    void onProgress(TransferProgress progress);

    /**
     * Asks how to proceed after a single task failed.
     *
     * @param failure the failed task.
     * @return the user's decision.
     */
    ErrorResolution onError(FailedOperation failure);

    /**
     * Reports that every task was processed (copied, skipped, or already recorded as a failure).
     *
     * @param filesCopied the number of tasks actually copied.
     * @param filesSkipped the number of tasks skipped (by conflict or error resolution, or unsupported).
     * @param failures every failure recorded during the run, including entries skipped during scanning.
     */
    void onCompleted(int filesCopied, int filesSkipped, List<FailedOperation> failures);

    /**
     * Reports that the run was cancelled mid-way through copying.
     */
    void onCancelled();

    /**
     * Reports that the run was aborted after a fatal, or user-chosen, unrecoverable failure.
     *
     * @param reason a human-readable description of why the run was aborted.
     */
    void onAborted(String reason);
}
