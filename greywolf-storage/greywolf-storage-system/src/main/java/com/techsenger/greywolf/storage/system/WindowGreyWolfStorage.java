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

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.base.GreyWolfFileOperations;
import com.techsenger.greywolf.storage.base.GreyWolfStorage;
import com.techsenger.greywolf.storage.base.DefaultGreyWolfFile;
import com.techsenger.greywolf.storage.base.LocationConverter;
import com.techsenger.greywolf.storage.base.StorageCategory;
import com.techsenger.shellfx.storage.base.DefaultStorageFile;
import com.techsenger.shellfx.storage.base.EntryLink;
import com.techsenger.shellfx.storage.base.FileOperation;
import com.techsenger.shellfx.storage.base.FileStorageType;
import com.techsenger.shellfx.storage.base.OperationType;
import com.techsenger.shellfx.storage.system.DefaultStorageIconProvider;
import com.techsenger.shellfx.storage.system.WindowsFileStorage;
import com.techsenger.toolkit.core.file.FileUtils;
import com.techsenger.toolkit.core.function.Factory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Pavel Castornii
 */
public final class WindowGreyWolfStorage extends WindowsFileStorage<GreyWolfFile>
        implements GreyWolfStorage {

    private static final Set<String> DEFAULT_EXECUTABLE_EXTENSIONS = Set.of("exe", "bat", "cmd", "com", "msi", "ps1");

    private static final Set<String> EXECUTABLE_EXTENSIONS = readExecutableExtensions();

    /**
     * Reads the extensions of executable files from the {@code PATHEXT} environment variable; if it is not set or has
     * no extensions, the default list is used.
     */
    private static Set<String> readExecutableExtensions() {
        var pathExt = System.getenv("PATHEXT");
        if (pathExt == null) {
            return DEFAULT_EXECUTABLE_EXTENSIONS;
        }
        var extensions = Arrays.stream(pathExt.split(";"))
                .map(String::trim)
                .map(extension -> extension.startsWith(".") ? extension.substring(1) : extension)
                .filter(extension -> !extension.isEmpty())
                .map(extension -> extension.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
        return extensions.isEmpty() ? DEFAULT_EXECUTABLE_EXTENSIONS : extensions;
    }

    private final LocationConverter locationConverter = new SystemLocationConverter();

    public WindowGreyWolfStorage(FileStorageType type, String displayName, URI rootUri, String id,
            Factory<DefaultGreyWolfFile> fileFactory) {
        super(type, displayName, rootUri, id, fileFactory, new DefaultStorageIconProvider(type));
    }

    @Override
    public StorageCategory getCategory() {
        return StorageCategory.SYSTEM;
    }

    @Override
    public byte[] readFileHeader(URI uri, int size, OperationType operationType) throws IOException {
        var path = toPath(uri);
        byte[] result;
        try {
            result = SystemFileUtils.readFileHeader(uri, size);
        } catch (IOException ex) {
            throw diagnoseReadFailure(path, ex);
        }
        notifyListeners(uri, GreyWolfFileOperations.READ, operationType);
        return result;
    }

    @Override
    public InputStream openInputStream(URI uri, OperationType operationType) throws NoSuchFileException,
            AccessDeniedException, IOException {
        var path = toPath(uri);
        InputStream result;
        try {
            result = SystemFileUtils.openInputStream(uri);
        } catch (IOException ex) {
            throw diagnoseReadFailure(path, ex);
        }
        notifyListeners(uri, GreyWolfFileOperations.READ, operationType);
        return result;
    }

    @Override
    public OutputStream openOutputStream(URI uri, OperationType operationType) throws AccessDeniedException,
            IOException {
        var path = toPath(uri);
        OutputStream result;
        try {
            result = SystemFileUtils.openOutputStream(uri);
        } catch (IOException ex) {
            throw diagnoseWriteFailure(path, ex);
        }
        notifyListeners(uri, GreyWolfFileOperations.WRITE, operationType);
        return result;
    }

    @Override
    public void delete(URI uri, OperationType operationType) throws NoSuchFileException, AccessDeniedException,
            IOException {
        var path = toPath(uri);
        try {
            SystemFileUtils.delete(uri);
        } catch (IOException ex) {
            throw diagnoseDeleteFailure(path, ex);
        }
        notifyListeners(uri, GreyWolfFileOperations.DELETE, operationType);
    }

    @Override
    public void move(URI sourceUri, URI targetUri, OperationType operationType) throws NoSuchFileException,
            FileAlreadyExistsException, AccessDeniedException, IOException {
        SystemFileUtils.move(sourceUri, targetUri);
        notifyListeners(sourceUri, GreyWolfFileOperations.DELETE, operationType);
        notifyListeners(targetUri, GreyWolfFileOperations.CREATE, operationType);
    }

    @Override
    public LocationConverter getLocationConverter() {
        return locationConverter;
    }

    @Override
    public void notifyOperation(URI uri, FileOperation operation, OperationType operationType) {
        notifyListeners(uri, operation, operationType);
    }

    @Override
    protected EntryLink createLink(URI targetUri, @Nullable URI resolvedUri, @Nullable GreyWolfFile target) {
        return SystemFileUtils.createLink(targetUri, resolvedUri, target);
    }

    @Override
    protected void populateFile(DefaultStorageFile file, Path entryPath, BasicFileAttributes attrs) {
        super.populateFile(file, entryPath, attrs);
        var executable = false;
        if (attrs.isRegularFile()) {
            var extension = FileUtils.getExtension(file.getName());
            executable = extension != null && EXECUTABLE_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT));
        }
        DefaultGreyWolfFile.cast(file).setExecutable(executable);
    }
}
