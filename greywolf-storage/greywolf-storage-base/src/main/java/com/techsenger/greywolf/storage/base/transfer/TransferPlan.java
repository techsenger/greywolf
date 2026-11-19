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

import com.techsenger.greywolf.storage.base.GreyWolfStorage;
import java.util.List;

/**
 * The immutable result of a {@link TransferScanner} pass: every planned {@link TransferTask} in copy order, the storage
 * all of their targets belong to, aggregated totals for progress reporting, and entries that were excluded from
 * the plan up front (e.g. unsupported symbolic links).
 *
 * @param tasks the planned copy steps, in the order they should be executed.
 * @param targetStorage the storage every task's target belongs to.
 * @param totalBytes the sum of every file task's size.
 * @param totalFiles the number of file tasks.
 * @param totalFolders the number of directory tasks.
 * @param skippedEntries entries excluded from {@code tasks} during scanning, already reported as failures.
 * @author Pavel Castornii
 */
public record TransferPlan(List<TransferTask> tasks, GreyWolfStorage targetStorage, long totalBytes, int totalFiles,
        int totalFolders, List<FailedOperation> skippedEntries) {

}
