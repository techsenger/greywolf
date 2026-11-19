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

package com.techsenger.greywolf.system.terminal.utils;

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.base.StorageCategory;
import java.nio.file.Path;

/**
 * Utilities for the terminal to work with the files of the storages.
 *
 * @author Pavel Castornii
 */
public final class TerminalUtils {

    /**
     * Returns the path of the directory in the file system of the machine, which is what the command interpreter of a
     * terminal can work in.
     *
     * @param file the file to resolve
     * @return the path, or {@code null} if the file is not a directory or its storage is not a system one
     */
    public static @Nullable String getDirectoryPath(GreyWolfFile file) {
        if (!file.isDirectory() || file.getStorage().getCategory() != StorageCategory.SYSTEM) {
            return null;
        }
        return Path.of(file.getUri()).toString();
    }

    private TerminalUtils() {
        // empty
    }
}
