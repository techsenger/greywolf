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

package com.techsenger.greywolf.storage.system;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.greywolf.storage.base.GreyWolfStorage;
import com.techsenger.greywolf.storage.base.DefaultGreyWolfFile;
import com.techsenger.greywolf.storage.base.spi.SystemStorageService;
import com.techsenger.drivekit.core.DriveMonitor;
import com.techsenger.shellfx.storage.base.FileStorageType;
import com.techsenger.shellfx.storage.system.DriveUtils;
import com.techsenger.toolkit.core.os.OsUtils;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.filechooser.FileSystemView;

/**
 * Discovers the local disk volumes of the current machine as {@link GreyWolfStorage} instances. A volume found
 * again with nothing changed is given as the very instance given before, which is recognized by the identifier the
 * operating system keeps for the volume.
 *
 * @author Pavel Castornii
 */
public final class SystemStorageProvider implements SystemStorageService {

    private Map<String, GreyWolfStorage> storagesById = Map.of();

    @Override
    public synchronized @Unmodifiable List<GreyWolfStorage> getStorages() {
        var windows = OsUtils.isWindows();
        var fsv = FileSystemView.getFileSystemView();
        var result = new ArrayList<GreyWolfStorage>();
        var newStoragesById = new HashMap<String, GreyWolfStorage>();
        for (var drive : DriveMonitor.getDrives()) {
            if (!DriveUtils.isShown(drive)) {
                continue;
            }
            var rootUri = drive.getRoot().toUri();
            var id = drive.getId();
            var name = drive.getLabel().isEmpty()
                    ? fsv.getSystemDisplayName(drive.getRoot().toFile()) : drive.getLabel();
            var storage = createStorage(windows, DriveUtils.toStorageType(drive.getType()), name, rootUri, id);
            result.add(storage);
            newStoragesById.put(id, storage);
        }
        storagesById = newStoragesById;
        return List.copyOf(result);
    }

    private GreyWolfStorage createStorage(boolean windows, FileStorageType type, String displayName, URI rootUri,
            String id) {
        var newStorage = windows
                ? new WindowGreyWolfStorage(type, displayName, rootUri, id, DefaultGreyWolfFile::new)
                : new UnixGreyWolfStorage(type, displayName, rootUri, id, DefaultGreyWolfFile::new);
        // the same instance is given while nothing of the storage has changed, as it keeps its listeners
        var existing = storagesById.get(id);
        return existing != null && existing.equals(newStorage) ? existing : newStorage;
    }
}
