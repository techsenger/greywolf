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

import com.techsenger.greywolf.storage.base.operation.ErrorResolution;
import java.util.List;

/**
 * Reports {@link Deleter#start} progress and asks for a decision when one is needed; every method is invoked
 * directly from the deleting virtual thread, never the JavaFX application thread, so implementations must hop
 * onto it themselves (e.g. via {@code UiExecutor.execute}) before touching any view.
 *
 * <p>{@link #onError} blocks the calling thread until it returns, which is safe here since the caller always runs
 * on a virtual thread.
 *
 * @author Pavel Castornii
 */
public interface DeleteListener {

    /**
     * Reports throttled progress of an in-progress deletion.
     *
     * @param progress the current progress snapshot.
     */
    void onProgress(DeleteProgress progress);

    /**
     * Asks how to proceed after a single entry failed to be deleted.
     *
     * @param failure the failed entry.
     * @return the user's decision.
     */
    ErrorResolution onError(FailedDeletion failure);

    /**
     * Reports that every entry was processed (deleted or skipped).
     *
     * @param deleted the number of entries actually deleted.
     * @param skipped the number of entries skipped after a failure.
     * @param failures every failure recorded during the run.
     */
    void onCompleted(int deleted, int skipped, List<FailedDeletion> failures);

    /**
     * Reports that the run was cancelled by the user; entries deleted so far stay deleted.
     */
    void onCancelled();

    /**
     * Reports that the run was aborted by the user's choice after a failure.
     *
     * @param reason a human-readable description of why the run was aborted.
     */
    void onAborted(String reason);
}
