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

import java.net.URI;

/**
 * A throttled snapshot of an in-progress {@link AbstractTransferer} run, pushed to {@link TransferListener#onProgress}
 * at most a few times per second.
 *
 * @param filesDone files (and directories) completed so far.
 * @param totalFiles total files (and directories) planned.
 * @param bytesDone bytes copied so far, across all files.
 * @param totalBytes total bytes planned, across all files.
 * @param currentSource the source URI of the file currently being copied.
 * @param currentTarget the target URI of the file currently being copied.
 * @param currentFileBytesDone bytes copied so far for the current file.
 * @param currentFileBytesTotal total bytes for the current file.
 * @param elapsedMillis time elapsed since the operation started.
 * @param estimatedRemainingMillis estimated time left, or a negative value when it can't be estimated yet.
 * @param currentBytesPerSecond the copy speed over the most recent progress interval.
 * @param averageBytesPerSecond the average copy speed since the operation started.
 * @author Pavel Castornii
 */
public record TransferProgress(int filesDone, int totalFiles, long bytesDone, long totalBytes, URI currentSource,
        URI currentTarget, long currentFileBytesDone, long currentFileBytesTotal, long elapsedMillis,
        long estimatedRemainingMillis, long currentBytesPerSecond, long averageBytesPerSecond) {

}
