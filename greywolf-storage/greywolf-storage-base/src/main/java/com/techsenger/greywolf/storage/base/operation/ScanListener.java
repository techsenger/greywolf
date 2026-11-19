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

/**
 * Reports the progress of a scan - the pass over the selected entries that builds the plan of an operation; every
 * method is invoked directly from the scanning virtual thread, never the JavaFX application thread, so
 * implementations must hop onto it themselves (e.g. via {@code UiExecutor.execute}) before touching any view.
 *
 * @param <P> the type of the plan the scan produces.
 * @author Pavel Castornii
 */
public interface ScanListener<P> {

    /**
     * Reports scan counters found so far; throttled by the scanner, so implementations don't need to debounce it
     * themselves.
     *
     * @param foldersFound folders discovered so far.
     * @param filesFound files discovered so far.
     * @param bytesFound total bytes of {@code filesFound} discovered so far.
     */
    void onScanProgress(int foldersFound, int filesFound, long bytesFound);

    /**
     * Reports that scanning finished successfully and produced {@code plan}.
     *
     * @param plan the completed scan plan.
     */
    void onScanCompleted(P plan);

    /**
     * Reports that scanning failed outright (e.g. a storage error occurred) before the operation could start.
     *
     * @param ex the failure.
     */
    void onScanFailed(Exception ex);

    /**
     * Reports that scanning was cancelled before it could produce a plan.
     */
    void onScanCancelled();
}
