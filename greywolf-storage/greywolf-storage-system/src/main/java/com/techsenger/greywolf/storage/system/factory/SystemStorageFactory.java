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

package com.techsenger.greywolf.storage.system.factory;

import com.techsenger.greywolf.storage.base.GreyWolfStorage;
import com.techsenger.greywolf.storage.base.DefaultGreyWolfFile;
import com.techsenger.greywolf.storage.system.UnixGreyWolfStorage;
import com.techsenger.greywolf.storage.system.WindowGreyWolfStorage;
import com.techsenger.shellfx.storage.base.FileStorageType;
import com.techsenger.toolkit.core.function.Factory;
import java.net.URI;

/**
 * Creates {@link GreyWolfStorage} instances backed by a Unix-like or Windows-like file system rooted at any URI; the
 * concrete implementations are confined to this module, so this factory is the only way to obtain them.
 *
 * @author Pavel Castornii
 */
public final class SystemStorageFactory {

    /**
     * Creates a storage backed by a Unix-like file system rooted at {@code rootUri}.
     *
     * @param type the storage type
     * @param displayName the human-readable name shown in the UI
     * @param rootUri the root URI of the storage
     * @param id the identifier of the volume or the device the storage stands for
     * @param fileFactory the factory used to create file entries returned by the storage
     * @return a new storage instance, never {@code null}
     */
    public static GreyWolfStorage createUnixStorage(FileStorageType type, String displayName, URI rootUri,
            String id, Factory<DefaultGreyWolfFile> fileFactory) {
        return new UnixGreyWolfStorage(type, displayName, rootUri, id, fileFactory);
    }

    /**
     * Creates a storage backed by a Windows file system rooted at {@code rootUri}.
     *
     * @param type the storage type
     * @param displayName the human-readable name shown in the UI
     * @param rootUri the root URI of the storage
     * @param id the identifier of the volume or the device the storage stands for
     * @param fileFactory the factory used to create file entries returned by the storage
     * @return a new storage instance, never {@code null}
     */
    public static GreyWolfStorage createWindowsStorage(FileStorageType type, String displayName, URI rootUri,
            String id, Factory<DefaultGreyWolfFile> fileFactory) {
        return new WindowGreyWolfStorage(type, displayName, rootUri, id, fileFactory);
    }

    private SystemStorageFactory() {
        // empty
    }
}
