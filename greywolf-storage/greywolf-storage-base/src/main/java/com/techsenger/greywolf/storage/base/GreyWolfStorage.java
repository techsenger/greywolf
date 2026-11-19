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
import com.techsenger.shellfx.storage.base.FileStorage;
import com.techsenger.shellfx.storage.base.OperationType;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;

/**
 * A {@link FileStorage} of GreyWolf. Beyond the {@link FileStorage} contract it extends, it has the operations a
 * plain file storage lacks (deleting, moving, streams); like every method of the storage that reaches it, each of
 * them takes an {@link OperationType} and reports its {@link FileOperation} to the listeners of the storage.
 *
 * @author Pavel Castornii
 */
public interface GreyWolfStorage extends FileStorage<GreyWolfFile> {

    /**
     * Returns the category this storage belongs to.
     *
     * @return the category, never {@code null}
     */
    StorageCategory getCategory();

    /**
     * Returns the converter between the URIs of this storage and the text of their locations.
     *
     * @return the converter, never {@code null}
     */
    LocationConverter getLocationConverter();

    /**
     * Reads the first {@code size} bytes of a file at {@code uri}, or all available bytes if the file is
     * smaller, reporting {@link GreyWolfFileOperations#READ}.
     *
     * @param uri the URI of the file to read
     * @param size maximum number of bytes to read
     * @param operationType whether this call is a directly user-initiated action or a step of bulk/internal
     *     machinery.
     * @return the file header bytes, never {@code null}
     * @throws NoSuchFileException if no file exists at {@code uri}
     * @throws AccessDeniedException if the caller lacks read permission
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if {@code size} is negative
     */
    byte[] readFileHeader(URI uri, int size, OperationType operationType) throws NoSuchFileException,
            AccessDeniedException, IOException;

    /**
     * Opens a stream for reading the raw content of the file at {@code uri}, reporting
     * {@link GreyWolfFileOperations#READ}; the caller is responsible for closing it.
     * Intended for chunked, progress-reporting reads of files too large to load via
     * {@link #readFile(URI, OperationType)}.
     *
     * @param uri the URI of the file to read
     * @param operationType whether this call is a directly user-initiated action or a step of bulk/internal
     *     machinery.
     * @return an open input stream, never {@code null}
     * @throws NoSuchFileException if no file exists at {@code uri}
     * @throws AccessDeniedException if the caller lacks read permission
     * @throws IOException if an I/O error occurs
     */
    InputStream openInputStream(URI uri, OperationType operationType) throws NoSuchFileException,
            AccessDeniedException, IOException;

    /**
     * Opens a stream for writing raw content to the file at {@code uri}, creating it if absent or truncating it
     * if present, reporting {@link GreyWolfFileOperations#WRITE}; the caller is responsible
     * for closing it. Intended for chunked, progress-reporting writes of files too large to write via
     * {@link #writeFile(URI, byte[], OperationType)}.
     *
     * @param uri the URI of the file to write
     * @param operationType whether this call is a directly user-initiated action or a step of bulk/internal
     *     machinery.
     * @return an open output stream, never {@code null}
     * @throws AccessDeniedException if the caller lacks write permission
     * @throws IOException if an I/O error occurs
     */
    OutputStream openOutputStream(URI uri, OperationType operationType) throws AccessDeniedException,
            IOException;

    /**
     * Deletes the single file or empty directory at {@code uri}, reporting {@link GreyWolfFileOperations#DELETE};
     * does not recurse into non-empty directories.
     *
     * @param uri the URI of the entry to delete
     * @param operationType whether this call is a directly user-initiated action or a step of bulk/internal
     *     machinery.
     * @throws NoSuchFileException if no entry exists at {@code uri}
     * @throws AccessDeniedException if the caller lacks write permission
     * @throws IOException if an I/O error occurs
     */
    void delete(URI uri, OperationType operationType) throws NoSuchFileException, AccessDeniedException,
            IOException;

    /**
     * Moves the file or directory at {@code sourceUri} to {@code targetUri} within this storage, in place and
     * without copying its content, reporting {@link GreyWolfFileOperations#DELETE} for the source and
     * {@link GreyWolfFileOperations#CREATE} for the target, both classified by {@code operationType}. The entry
     * may get a different name and parent directory; an existing target is never replaced. It is meant for a change
     * of the parent directory: a change of the name alone is a {@link #renameFile(URI, String, OperationType)}.
     *
     * <p>Fails with an {@link IOException} when the entry can't be moved in place (e.g. the target is on another
     * file system), leaving the source untouched - the caller then falls back to copying and deleting.
     *
     * @param sourceUri the URI of the entry to move.
     * @param targetUri the URI the entry should get; its parent directory must exist.
     * @param operationType whether this call is a directly user-initiated action or a step of bulk/internal
     *     machinery.
     * @throws NoSuchFileException if the source or the parent directory of the target doesn't exist.
     * @throws FileAlreadyExistsException if an entry already exists at {@code targetUri}.
     * @throws AccessDeniedException if the caller lacks the permission to move the entry.
     * @throws IOException if the entry can't be moved in place or an I/O error occurs.
     */
    void move(URI sourceUri, URI targetUri, OperationType operationType) throws NoSuchFileException,
            FileAlreadyExistsException, AccessDeniedException, IOException;

    /**
     * Notifies this storage's listeners that {@code operation} was performed on the file at {@code uri} through
     * a channel outside this storage's own API (e.g. the OS's default application opening the file directly).
     *
     * @param uri the URI of the file it was performed on
     * @param operation the operation that was performed
     * @param operationType whether this was a directly user-initiated action or a step of bulk/internal
     *     machinery
     */
    void notifyOperation(URI uri, FileOperation operation, OperationType operationType);
}
