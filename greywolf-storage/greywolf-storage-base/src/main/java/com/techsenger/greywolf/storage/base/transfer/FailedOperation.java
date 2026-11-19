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

/**
 * A single task that could not be completed, kept for the end-of-operation summary instead of aborting the
 * whole run.
 *
 * @param task the task that failed.
 * @param cause the exception raised while attempting it.
 * @param message an already human-readable description of {@code cause}, suitable for display as-is.
 * @author Pavel Castornii
 */
public record FailedOperation(TransferTask task, Exception cause, String message) {
}
