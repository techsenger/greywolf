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

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.techsenger.greywolf.storage.base.GreyWolfFileOperations;
import com.techsenger.greywolf.storage.base.GreyWolfStorage;
import com.techsenger.greywolf.storage.base.DefaultGreyWolfFile;
import com.techsenger.greywolf.storage.base.FormatException;
import com.techsenger.greywolf.storage.base.StorageCategory;
import com.techsenger.shellfx.storage.base.FileOperation;
import com.techsenger.shellfx.storage.base.FileStorageType;
import com.techsenger.shellfx.storage.base.OperationType;
import com.techsenger.shellfx.storage.base.StorageListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Integration tests for what {@link GreyWolfStorage} adds to the file storage contract of ShellFX, run against the
 * system storages ({@code UnixGreyWolfStorage}, {@code WindowGreyWolfStorage}) over a fresh in-memory Jimfs file
 * system of each flavor for every test, the root of which is the root of the storage. The methods inherited from
 * ShellFX and only extended with the {@link OperationType} flag are not tested here, except for {@code renameFile},
 * whose notifications are part of the contract. Every method is checked for its result, for the exception of each
 * failure it documents, and for the notifications of the listeners (a failed operation must notify nobody).
 * {@link java.nio.file.AccessDeniedException} is not tested: Jimfs does not enforce permissions, so there is no way
 * to make an operation fail with it.
 *
 * @author Pavel Castornii
 */
public class SystemGreyWolfStorageIT {

    /**
     * What a listener of the storage was told about an operation.
     */
    private record Notification(FileOperation operation, OperationType operationType, URI uri) {

    }

    /**
     * Keeps every created Jimfs file system strongly reachable for the lifetime of the test JVM - a storage only
     * keeps the root URI, not the {@link FileSystem} object itself, and Jimfs registers a file system by URI only
     * as long as something still references it; without this, an unlucky GC can collect it mid-test.
     */
    private static final List<FileSystem> RETAINED_FILE_SYSTEMS = new CopyOnWriteArrayList<>();

    private static final String CONTENT = "caf\u00e9";

    /**
     * Returns one storage factory per flavor of system: Linux, macOS and Windows. The root of each storage is the
     * root of its own fresh Jimfs file system, which is empty: the working directory Jimfs creates by default is
     * moved into the root itself, so that nothing but the test's own entries is there.
     */
    static Stream<Named<Supplier<GreyWolfStorage>>> flavors() {
        return Stream.of(
                Named.of("linux", () -> createStorage(
                        Configuration.unix().toBuilder().setWorkingDirectory("/").build(), false)),
                Named.of("macos", () -> createStorage(
                        Configuration.osX().toBuilder().setWorkingDirectory("/").build(), false)),
                Named.of("windows", () -> createStorage(
                        Configuration.windows().toBuilder().setWorkingDirectory("C:\\").build(), true)));
    }

    private static GreyWolfStorage createStorage(Configuration configuration, boolean windows) {
        var fileSystem = Jimfs.newFileSystem(configuration);
        RETAINED_FILE_SYSTEMS.add(fileSystem);
        var rootUri = fileSystem.getRootDirectories().iterator().next().toUri();
        var storage = windows
                ? new WindowGreyWolfStorage(FileStorageType.BASE, "test", rootUri, "test", DefaultGreyWolfFile::new)
                : new UnixGreyWolfStorage(FileStorageType.BASE, "test", rootUri, "test", DefaultGreyWolfFile::new);
        return storage;
    }

    private static Path rootOf(GreyWolfStorage storage) {
        return Paths.get(storage.getUri());
    }

    /**
     * Registers a listener that records everything it is told by the storage.
     */
    private static List<Notification> listen(GreyWolfStorage storage) {
        var notifications = new CopyOnWriteArrayList<Notification>();
        storage.addListener((uri, operation, operationType) ->
                notifications.add(new Notification(operation, operationType, uri)));
        return notifications;
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getCategory_systemStorage_system(Supplier<GreyWolfStorage> storageFactory) {
        var storage = storageFactory.get();

        assertThat(storage.getCategory()).isEqualTo(StorageCategory.SYSTEM);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void readFileHeader_fileLongerThanSize_firstBytesOnly(
            Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var path = Files.write(rootOf(storage).resolve("data.bin"), new byte[] {1, 2, 3, 4, 5});

        assertThat(storage.readFileHeader(path.toUri(), 3, OperationType.PRIMARY)).containsExactly(1, 2, 3);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void readFileHeader_fileShorterThanSize_wholeContent(
            Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var path = Files.write(rootOf(storage).resolve("data.bin"), new byte[] {1, 2, 3});

        assertThat(storage.readFileHeader(path.toUri(), 100, OperationType.PRIMARY)).containsExactly(1, 2, 3);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void readFileHeader_zeroSize_emptyArray(Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var path = Files.write(rootOf(storage).resolve("data.bin"), new byte[] {1, 2, 3});

        assertThat(storage.readFileHeader(path.toUri(), 0, OperationType.PRIMARY)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void readFileHeader_emptyFile_emptyArray(Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var path = Files.createFile(rootOf(storage).resolve("empty.bin"));

        assertThat(storage.readFileHeader(path.toUri(), 10, OperationType.PRIMARY)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void readFileHeader_missingFile_noSuchFileException(Supplier<GreyWolfStorage> storageFactory) {
        var storage = storageFactory.get();
        var missing = rootOf(storage).resolve("missing.bin").toUri();

        assertThatThrownBy(() -> storage.readFileHeader(missing, 10, OperationType.PRIMARY))
                .isInstanceOf(NoSuchFileException.class);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void readFileHeader_folder_ioException(Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var folder = Files.createDirectory(rootOf(storage).resolve("folder"));

        assertThatThrownBy(() -> storage.readFileHeader(folder.toUri(), 10, OperationType.PRIMARY))
                .isInstanceOf(IOException.class);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void readFileHeader_negativeSize_illegalArgumentException(
            Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var path = Files.write(rootOf(storage).resolve("data.bin"), new byte[] {1, 2, 3});

        assertThatThrownBy(() -> storage.readFileHeader(path.toUri(), -1, OperationType.PRIMARY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void openInputStream_existingFile_wholeContentAndReadReported(
            Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var path = Files.write(rootOf(storage).resolve("data.bin"), new byte[] {1, 2, 3});
        var uri = path.toUri();
        var notifications = listen(storage);

        try (var in = storage.openInputStream(uri, OperationType.PRIMARY)) {
            assertThat(in.readAllBytes()).containsExactly(1, 2, 3);
        }

        assertThat(notifications)
                .containsExactly(new Notification(GreyWolfFileOperations.READ, OperationType.PRIMARY, uri));
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void openInputStream_secondaryOperation_readReportedAsSecondary(
            Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var uri = Files.write(rootOf(storage).resolve("data.bin"), new byte[] {1}).toUri();
        var notifications = listen(storage);

        storage.openInputStream(uri, OperationType.SECONDARY).close();

        assertThat(notifications)
                .containsExactly(new Notification(GreyWolfFileOperations.READ, OperationType.SECONDARY, uri));
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void openInputStream_missingFile_noSuchFileExceptionAndNobodyNotified(
            Supplier<GreyWolfStorage> storageFactory) {
        var storage = storageFactory.get();
        var missing = rootOf(storage).resolve("missing.bin").toUri();
        var notifications = listen(storage);

        assertThatThrownBy(() -> storage.openInputStream(missing, OperationType.PRIMARY))
                .isInstanceOf(NoSuchFileException.class);
        assertThat(notifications).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void openInputStream_folder_ioExceptionAndNobodyNotified(
            Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var folder = Files.createDirectory(rootOf(storage).resolve("folder"));
        var notifications = listen(storage);

        assertThatThrownBy(() -> storage.openInputStream(folder.toUri(), OperationType.PRIMARY))
                .isInstanceOf(IOException.class);
        assertThat(notifications).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void openOutputStream_missingFile_fileCreatedWithTheContentAndWriteReported(
            Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var path = rootOf(storage).resolve("data.bin");
        var uri = path.toUri();
        var notifications = listen(storage);

        try (var out = storage.openOutputStream(uri, OperationType.PRIMARY)) {
            out.write(new byte[] {1, 2, 3});
        }

        assertThat(Files.readAllBytes(path)).containsExactly(1, 2, 3);
        assertThat(notifications)
                .containsExactly(new Notification(GreyWolfFileOperations.WRITE, OperationType.PRIMARY, uri));
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void openOutputStream_existingFile_oldContentReplaced(
            Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var path = Files.write(rootOf(storage).resolve("data.bin"), new byte[] {9, 9, 9, 9, 9});

        try (var out = storage.openOutputStream(path.toUri(), OperationType.PRIMARY)) {
            out.write(new byte[] {1, 2});
        }

        assertThat(Files.readAllBytes(path)).containsExactly(1, 2);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void openOutputStream_secondaryOperation_writeReportedAsSecondary(
            Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var uri = rootOf(storage).resolve("data.bin").toUri();
        var notifications = listen(storage);

        storage.openOutputStream(uri, OperationType.SECONDARY).close();

        assertThat(notifications)
                .containsExactly(new Notification(GreyWolfFileOperations.WRITE, OperationType.SECONDARY, uri));
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void openOutputStream_missingParent_noSuchFileExceptionAndNobodyNotified(
            Supplier<GreyWolfStorage> storageFactory) {
        var storage = storageFactory.get();
        var parent = rootOf(storage).resolve("missing");
        var uri = parent.resolve("data.bin").toUri();
        var notifications = listen(storage);

        assertThatThrownBy(() -> storage.openOutputStream(uri, OperationType.PRIMARY))
                .isInstanceOf(NoSuchFileException.class);
        assertThat(Files.exists(parent)).isFalse();
        assertThat(notifications).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void openOutputStream_folder_ioExceptionAndFolderKept(
            Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var folder = Files.createDirectory(rootOf(storage).resolve("folder"));
        var notifications = listen(storage);

        assertThatThrownBy(() -> storage.openOutputStream(folder.toUri(), OperationType.PRIMARY))
                .isInstanceOf(IOException.class);
        assertThat(Files.isDirectory(folder)).isTrue();
        assertThat(notifications).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void delete_file_fileRemovedAndDeleteReported(
            Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var path = Files.writeString(rootOf(storage).resolve("file.txt"), CONTENT);
        var uri = path.toUri();
        var notifications = listen(storage);

        storage.delete(uri, OperationType.PRIMARY);

        assertThat(Files.exists(path)).isFalse();
        assertThat(notifications)
                .containsExactly(new Notification(GreyWolfFileOperations.DELETE, OperationType.PRIMARY, uri));
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void delete_emptyFolder_folderRemoved(Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var folder = Files.createDirectory(rootOf(storage).resolve("folder"));

        storage.delete(folder.toUri(), OperationType.PRIMARY);

        assertThat(Files.exists(folder)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void delete_missingEntry_noSuchFileExceptionAndNobodyNotified(
            Supplier<GreyWolfStorage> storageFactory) {
        var storage = storageFactory.get();
        var missing = rootOf(storage).resolve("missing.txt").toUri();
        var notifications = listen(storage);

        assertThatThrownBy(() -> storage.delete(missing, OperationType.PRIMARY))
                .isInstanceOf(NoSuchFileException.class);
        assertThat(notifications).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void delete_nonEmptyFolder_directoryNotEmptyExceptionAndContentKept(
            Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var folder = Files.createDirectory(rootOf(storage).resolve("folder"));
        var inside = Files.writeString(folder.resolve("inside.txt"), CONTENT);
        var notifications = listen(storage);

        assertThatThrownBy(() -> storage.delete(folder.toUri(), OperationType.PRIMARY))
                .isInstanceOf(DirectoryNotEmptyException.class);
        assertThat(Files.readString(inside)).isEqualTo(CONTENT);
        assertThat(notifications).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void renameFile_file_renamedAndRenameReported(
            Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var root = rootOf(storage);
        var path = Files.writeString(root.resolve("old.txt"), CONTENT);
        var newUri = root.resolve("new.txt").toUri();
        var notifications = listen(storage);

        storage.renameFile(path.toUri(), "new.txt", OperationType.SECONDARY);

        assertThat(Files.exists(path)).isFalse();
        assertThat(Files.readString(root.resolve("new.txt"))).isEqualTo(CONTENT);
        assertThat(notifications)
                .containsExactly(new Notification(GreyWolfFileOperations.RENAME, OperationType.SECONDARY, newUri));
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void renameFile_missingEntry_noSuchFileExceptionAndNobodyNotified(
            Supplier<GreyWolfStorage> storageFactory) {
        var storage = storageFactory.get();
        var missing = rootOf(storage).resolve("missing.txt").toUri();
        var notifications = listen(storage);

        assertThatThrownBy(() -> storage.renameFile(missing, "new.txt", OperationType.PRIMARY))
                .isInstanceOf(NoSuchFileException.class);
        assertThat(notifications).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void renameFile_nameTakenByAnotherEntry_fileAlreadyExistsExceptionAndNobodyNotified(
            Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var root = rootOf(storage);
        var source = Files.writeString(root.resolve("source.txt"), "source");
        Files.writeString(root.resolve("taken.txt"), "taken");
        var notifications = listen(storage);

        assertThatThrownBy(() -> storage.renameFile(source.toUri(), "taken.txt", OperationType.PRIMARY))
                .isInstanceOf(FileAlreadyExistsException.class);
        assertThat(Files.readString(source)).isEqualTo("source");
        assertThat(notifications).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void move_fileToOtherFolderUnderOtherName_movedAndDeleteOfSourceAndCreateOfTargetReported(
            Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var root = rootOf(storage);
        var source = Files.writeString(root.resolve("source.txt"), CONTENT);
        var folder = Files.createDirectory(root.resolve("folder"));
        var target = folder.resolve("target.txt");
        var sourceUri = source.toUri();
        var targetUri = target.toUri();
        var notifications = listen(storage);

        storage.move(sourceUri, targetUri, OperationType.SECONDARY);

        assertThat(Files.exists(source)).isFalse();
        assertThat(Files.readString(target)).isEqualTo(CONTENT);
        assertThat(notifications).containsExactly(
                new Notification(GreyWolfFileOperations.DELETE, OperationType.SECONDARY, sourceUri),
                new Notification(GreyWolfFileOperations.CREATE, OperationType.SECONDARY, targetUri));
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void move_folderWithContent_movedTogetherWithItsContent(
            Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var root = rootOf(storage);
        var source = Files.createDirectory(root.resolve("source"));
        Files.writeString(source.resolve("inside.txt"), CONTENT);
        var parent = Files.createDirectory(root.resolve("parent"));
        var target = parent.resolve("target");

        storage.move(source.toUri(), target.toUri(), OperationType.PRIMARY);

        assertThat(Files.exists(source)).isFalse();
        assertThat(Files.readString(target.resolve("inside.txt"))).isEqualTo(CONTENT);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void move_missingSource_noSuchFileExceptionAndNothingCreatedAndNobodyNotified(
            Supplier<GreyWolfStorage> storageFactory) {
        var storage = storageFactory.get();
        var root = rootOf(storage);
        var source = root.resolve("missing.txt");
        var target = root.resolve("target.txt");
        var notifications = listen(storage);

        assertThatThrownBy(() -> storage.move(source.toUri(), target.toUri(), OperationType.PRIMARY))
                .isInstanceOf(NoSuchFileException.class);
        assertThat(Files.exists(target)).isFalse();
        assertThat(notifications).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void move_missingParentOfTarget_noSuchFileExceptionAndSourceKept(
            Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var root = rootOf(storage);
        var source = Files.writeString(root.resolve("source.txt"), CONTENT);
        var parent = root.resolve("missing");
        var target = parent.resolve("target.txt");
        var notifications = listen(storage);

        assertThatThrownBy(() -> storage.move(source.toUri(), target.toUri(), OperationType.PRIMARY))
                .isInstanceOf(NoSuchFileException.class);
        assertThat(Files.readString(source)).isEqualTo(CONTENT);
        assertThat(Files.exists(parent)).isFalse();
        assertThat(notifications).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void move_targetIsExistingFile_fileAlreadyExistsExceptionAndBothKept(
            Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var root = rootOf(storage);
        var source = Files.writeString(root.resolve("source.txt"), "source");
        var target = Files.writeString(root.resolve("target.txt"), "target");
        var notifications = listen(storage);

        assertThatThrownBy(() -> storage.move(source.toUri(), target.toUri(), OperationType.PRIMARY))
                .isInstanceOf(FileAlreadyExistsException.class);
        assertThat(Files.readString(source)).isEqualTo("source");
        assertThat(Files.readString(target)).isEqualTo("target");
        assertThat(notifications).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void move_targetIsExistingFolder_fileAlreadyExistsExceptionAndBothKept(
            Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var root = rootOf(storage);
        var source = Files.writeString(root.resolve("source.txt"), CONTENT);
        var target = Files.createDirectory(root.resolve("target"));

        assertThatThrownBy(() -> storage.move(source.toUri(), target.toUri(), OperationType.PRIMARY))
                .isInstanceOf(FileAlreadyExistsException.class);
        assertThat(Files.readString(source)).isEqualTo(CONTENT);
        assertThat(Files.isDirectory(target)).isTrue();
    }

    @Test
    public void move_targetOnAnotherFileSystem_atomicMoveNotSupportedExceptionAndSourceKept() throws Exception {
        var configuration = Configuration.unix().toBuilder().setWorkingDirectory("/").build();
        var storage = createStorage(configuration, false);
        var otherStorage = createStorage(configuration, false);
        var source = Files.writeString(rootOf(storage).resolve("source.txt"), CONTENT);
        var target = rootOf(otherStorage).resolve("target.txt");
        var notifications = listen(storage);

        assertThatThrownBy(() -> storage.move(source.toUri(), target.toUri(), OperationType.PRIMARY))
                .isInstanceOf(AtomicMoveNotSupportedException.class);
        assertThat(Files.readString(source)).isEqualTo(CONTENT);
        assertThat(Files.exists(target)).isFalse();
        assertThat(notifications).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void toLocation_nestedEntry_pathOfTheSystem(Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var path = rootOf(storage).resolve("folder").resolve("file.txt");

        var text = storage.getLocationConverter().toLocation(path.toUri());

        assertThat(text).isEqualTo(path.toString());
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void toLocation_rootUri_pathOfTheRoot(Supplier<GreyWolfStorage> storageFactory) {
        var storage = storageFactory.get();

        var text = storage.getLocationConverter().toLocation(storage.getUri());

        assertThat(text).isEqualTo(rootOf(storage).toString());
    }

    /**
     * Not run on Jimfs: the text is parsed by the file system of the platform, whatever the storage is.
     */
    @Test
    public void fromLocation_validPath_uriOfThePath() throws Exception {
        var storage = createStorage(Configuration.unix(), false);
        var path = Paths.get(System.getProperty("java.io.tmpdir")).resolve("a.txt");

        var uri = storage.getLocationConverter().fromLocation(path.toString());

        assertThat(uri).isEqualTo(path.toUri());
    }

    @Test
    public void fromLocation_textWithTrailingSeparator_uriWithTrailingSlash() throws Exception {
        var storage = createStorage(Configuration.unix(), false);
        var path = Paths.get(System.getProperty("java.io.tmpdir")).resolve("missing");

        var uri = storage.getLocationConverter().fromLocation(path + File.separator);

        assertThat(uri.getPath()).endsWith("/");
    }

    @Test
    public void fromLocation_textWithNulCharacter_formatExceptionCausedByInvalidPath() {
        var storage = createStorage(Configuration.unix(), false);

        assertThatThrownBy(() -> storage.getLocationConverter().fromLocation("\0"))
                .isInstanceOf(FormatException.class)
                .hasCauseInstanceOf(InvalidPathException.class);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void notifyOperation_registeredListeners_everyListenerToldAboutTheOperation(
            Supplier<GreyWolfStorage> storageFactory) {
        var storage = storageFactory.get();
        var uri = rootOf(storage).resolve("file.txt").toUri();
        var first = listen(storage);
        var second = listen(storage);

        storage.notifyOperation(uri, GreyWolfFileOperations.READ, OperationType.SECONDARY);

        var expected = new Notification(GreyWolfFileOperations.READ, OperationType.SECONDARY, uri);
        assertThat(first).containsExactly(expected);
        assertThat(second).containsExactly(expected);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void removeListener_removedListener_notToldAnymore(Supplier<GreyWolfStorage> storageFactory) {
        var storage = storageFactory.get();
        var uri = rootOf(storage).resolve("file.txt").toUri();
        var kept = listen(storage);
        var removed = new CopyOnWriteArrayList<Notification>();
        StorageListener listener = (notifiedUri, operation, operationType) ->
                removed.add(new Notification(operation, operationType, notifiedUri));
        storage.addListener(listener);
        storage.removeListener(listener);

        storage.notifyOperation(uri, GreyWolfFileOperations.WRITE, OperationType.PRIMARY);

        assertThat(kept).hasSize(1);
        assertThat(removed).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void removeListener_notRegisteredListener_otherListenersStillToldAboutOperations(
            Supplier<GreyWolfStorage> storageFactory) {
        var storage = storageFactory.get();
        var uri = rootOf(storage).resolve("file.txt").toUri();
        var kept = listen(storage);
        var other = new CopyOnWriteArrayList<Notification>();
        StorageListener notRegistered = (notifiedUri, operation, operationType) ->
                other.add(new Notification(operation, operationType, notifiedUri));

        storage.removeListener(notRegistered);
        storage.notifyOperation(uri, GreyWolfFileOperations.WRITE, OperationType.PRIMARY);

        assertThat(kept).hasSize(1);
        assertThat(other).isEmpty();
    }
}
