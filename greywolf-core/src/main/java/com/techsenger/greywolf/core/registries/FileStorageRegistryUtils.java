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

package com.techsenger.greywolf.core.registries;

import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.shellfx.storage.base.FileStorageUtils;

/**
 * Helpers that answer common questions about the storages of a {@link FileStorageRegistry}.
 *
 * @author Pavel Castornii
 */
public final class FileStorageRegistryUtils {

    /**
     * Refreshes {@code registry}'s system storages and resolves the directory a freshly opened tool should start in:
     * the primary storage's home directory, falling back to its root if it has no home.
     *
     * @param registry the registry to take the storages from
     */
    public static GreyWolfFile resolveInitialDirectory(FileStorageRegistry registry) {
        registry.refreshSystemStorages();
        var homeOpt = FileStorageUtils.getHome(registry.getSystemStorages());
        return homeOpt.isPresent() ? homeOpt.get()
                : FileStorageUtils.getMain(registry.getSystemStorages()).getRoot();
    }

    private FileStorageRegistryUtils() {
        // empty
    }
}
