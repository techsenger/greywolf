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

import com.techsenger.shellfx.storage.base.FileOperation;
import com.techsenger.shellfx.storage.base.OperationType;
import java.net.URI;

/**
 * A {@link GreyWolfStorage} that aggregates recently used files from other, real storages, retrievable via
 * {@code FileStorageRegistry#getRecentStorage()}. It lists the recorded files, which the file provider replaces
 * with the real files, and holds no files of its own; this interface adds operations that manage
 * the tracked list itself, without touching any real file.
 *
 * @author Pavel Castornii
 */
public interface RecentStorage extends GreyWolfStorage {

    /**
     * Records an operation performed on a file of another storage, so this list follows the files in use (a visit
     * is recorded, a deleted file is dropped). It is not an operation of this storage, so its listeners are not
     * told about it, except for the drop of a recorded file, see {@link #remove}.
     *
     * @param storage the storage the operation was performed on
     * @param uri the URI of the file the operation was performed on
     * @param operation the operation that was performed
     * @param operationType whether it was a directly user-initiated action or a step of bulk/internal machinery
     */
    void onOperation(GreyWolfStorage storage, URI uri, FileOperation operation, OperationType operationType);

    /**
     * Removes every currently recorded file from this list, without deleting any of them from their real
     * storage. The listeners are told about every removed file as {@code DELETE}, see {@link #remove}.
     *
     * @param operationType whether the user asked for it or it is a step of internal machinery
     */
    void clear(OperationType operationType);

    /**
     * Removes the file at {@code uri} from this list, without deleting it from its real storage - a no-op if
     * {@code uri} isn't currently recorded. The listeners are told that the file is gone as a {@code DELETE} of its
     * URI, with the given type, from the thread that removed it.
     *
     * @param uri the URI of the file to remove
     * @param operationType whether the user removed the file by hand or it is a step of internal machinery
     */
    void remove(URI uri, OperationType operationType);
}
