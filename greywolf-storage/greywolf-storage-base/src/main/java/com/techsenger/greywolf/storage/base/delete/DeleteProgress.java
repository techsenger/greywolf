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

import java.net.URI;

/**
 * A throttled snapshot of an in-progress {@link Deleter} run, pushed to {@link DeleteListener#onProgress} at most
 * a few times per second.
 *
 * @param itemsDone entries processed so far (deleted or skipped).
 * @param totalItems total entries planned.
 * @param bytesDone bytes freed so far, across all deleted files.
 * @param totalBytes total bytes planned, across all files.
 * @param currentUri the URI of the entry being deleted.
 * @param elapsedMillis time elapsed since the operation started.
 * @param estimatedRemainingMillis estimated time left, or a negative value when it can't be estimated yet.
 * @param itemsPerSecond the average number of entries processed per second since the operation started.
 * @author Pavel Castornii
 */
public record DeleteProgress(int itemsDone, int totalItems, long bytesDone, long totalBytes, URI currentUri,
        long elapsedMillis, long estimatedRemainingMillis, double itemsPerSecond) {

}
