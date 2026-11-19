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

import com.techsenger.greywolf.storage.base.GreyWolfFile;
import java.util.List;

/**
 * The immutable result of a {@link DeleteScanner} pass: every entry to delete, in the order it must be deleted
 * (the contents of a folder before the folder itself), plus totals for progress reporting.
 *
 * @param entries the entries to delete, in deletion order.
 * @param totalBytes the sum of every file entry's size.
 * @param totalFiles the number of non-folder entries (symbolic links included).
 * @param totalFolders the number of folder entries.
 * @author Pavel Castornii
 */
public record DeletePlan(List<GreyWolfFile> entries, long totalBytes, int totalFiles, int totalFolders) {

}
