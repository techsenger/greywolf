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

package com.techsenger.greywolf.storage.base;

import com.techsenger.shellfx.storage.base.DefaultFileOperation;
import com.techsenger.shellfx.storage.base.FileEntryType;
import com.techsenger.shellfx.storage.base.FileOperation;
import com.techsenger.shellfx.storage.base.FileOperations;

/**
 * The {@link FileOperation}s a {@link GreyWolfStorage} performs beyond those of a plain file storage.
 *
 * @author Pavel Castornii
 */
public final class GreyWolfFileOperations extends FileOperations {

    /**
     * A file or empty directory was deleted. Either {@link FileEntryType#FILE} or {@link FileEntryType#DIRECTORY} -
     * the entry is already gone by the time this is reported, so its own type can't be re-resolved from it.
     */
    public static final FileOperation DELETE = new DefaultFileOperation("DELETE");
}
