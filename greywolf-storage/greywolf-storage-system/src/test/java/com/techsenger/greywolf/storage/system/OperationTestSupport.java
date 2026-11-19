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
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.base.GreyWolfStorage;
import com.techsenger.greywolf.storage.base.DefaultGreyWolfFile;
import com.techsenger.greywolf.storage.base.delete.DeletePlan;
import com.techsenger.greywolf.storage.base.delete.DeleteScanner;
import com.techsenger.greywolf.storage.base.operation.NameMask;
import com.techsenger.greywolf.storage.base.operation.ScanListener;
import com.techsenger.greywolf.storage.base.transfer.TransferPlan;
import com.techsenger.greywolf.storage.base.transfer.TransferScanner;
import com.techsenger.greywolf.storage.system.factory.SystemStorageFactory;
import com.techsenger.shellfx.storage.base.FileStorageType;
import com.techsenger.shellfx.storage.base.OperationType;
import com.techsenger.shellfx.storage.base.UriUtils;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;

/**
 * Fixtures shared by the integration tests of the file operations (scan, copy, delete): in-memory local
 * storages backed by a fresh Jimfs file system per test (one factory per OS storage flavor), plus blocking
 * wrappers around {@link TransferScanner#scan} and {@link DeleteScanner#scan}.
 *
 * @author Pavel Castornii
 */
public final class OperationTestSupport {

    private static final long TIMEOUT_SECONDS = 10;

    /**
     * Keeps every created Jimfs file system strongly reachable for the lifetime of the test JVM - a storage only
     * keeps the root URI, not the {@link FileSystem} object itself, and Jimfs registers a file system by URI only
     * as long as something still references it; without this, an unlucky GC can collect it mid-test and turn
     * every subsequent URI lookup into a {@code FileSystemNotFoundException}.
     */
    private static final List<FileSystem> RETAINED_FILE_SYSTEMS = new CopyOnWriteArrayList<>();

    /**
     * {@code @MethodSource} reference to {@link #storages()}, for tests outside this class.
     */
    public static final String STORAGES = "com.techsenger.greywolf.storage.system.OperationTestSupport#storages";

    /**
     * A mask that matches every name; used by tests that don't care about mask filtering.
     */
    public static final NameMask MATCH_ALL = new NameMask("*", false, false);

    /**
     * Returns one {@link GreyWolfStorage} factory per local storage flavor (unix, macOS, windows), each backed
     * by a fresh, empty Jimfs file system created only when the factory is invoked.
     *
     * @return the storage factories, one per supported local OS flavor.
     */
    public static Stream<Named<Supplier<GreyWolfStorage>>> storages() {
        return Stream.of(
                Named.of("local (unix)", () -> createLocalStorage(Configuration.unix(), false)),
                Named.of("local (macos)", () -> createLocalStorage(Configuration.osX(), false)),
                Named.of("local (windows)", () -> createLocalStorage(Configuration.windows(), true)));
    }

    /**
     * Creates a file at {@code parentUri/name} with {@code content} and returns it.
     *
     * @throws IOException if the file can't be written.
     */
    public static GreyWolfFile createFile(GreyWolfStorage storage, URI parentUri, String name, byte[] content)
            throws IOException {
        var uri = UriUtils.resolvePath(parentUri, name, false);
        storage.writeFile(uri, content, OperationType.PRIMARY);
        return storage.getFile(uri, OperationType.PRIMARY);
    }

    /**
     * Text-content overload of {@link #createFile(GreyWolfStorage, URI, String, byte[])}, encoded as UTF-8.
     *
     * @throws IOException if the file can't be written.
     */
    public static GreyWolfFile createFile(GreyWolfStorage storage, URI parentUri, String name, String content)
            throws IOException {
        return createFile(storage, parentUri, name, content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates a directory at {@code parentUri/name} and returns it.
     *
     * @throws IOException if the directory can't be created.
     */
    public static GreyWolfFile createDirectory(GreyWolfStorage storage, URI parentUri, String name)
            throws IOException {
        var uri = UriUtils.resolvePath(parentUri, name, true);
        storage.createDirectory(uri, OperationType.PRIMARY);
        return storage.getFile(uri, OperationType.PRIMARY);
    }

    /**
     * Runs a scan matching every name on a new {@link TransferScanner} and blocks until it completes.
     *
     * @throws Exception if the scan fails or is cancelled; the original failure is the returned future's cause.
     */
    public static TransferPlan scan(GreyWolfStorage storage, List<GreyWolfFile> sources, GreyWolfFile target)
            throws Exception {
        return scan(new TransferScanner(), storage, sources, target, MATCH_ALL);
    }

    /**
     * Mask-aware overload of {@link #scan(GreyWolfStorage, List, GreyWolfFile)}, on a new {@link TransferScanner}.
     *
     * @throws Exception if the scan fails or is cancelled; the original failure is the returned future's cause.
     */
    public static TransferPlan scan(GreyWolfStorage storage, List<GreyWolfFile> sources, GreyWolfFile target,
            NameMask mask) throws Exception {
        return scan(new TransferScanner(), storage, sources, target, mask);
    }

    /**
     * Full overload of {@link #scan(GreyWolfStorage, List, GreyWolfFile, NameMask)} that runs on a
     * caller-supplied {@link TransferScanner}, letting a test call {@link TransferScanner#cancel()} on it before
     * or during the scan.
     *
     * @throws Exception if the scan fails or is cancelled; the original failure is the returned future's cause.
     */
    public static TransferPlan scan(TransferScanner scanner, GreyWolfStorage storage, List<GreyWolfFile> sources,
            GreyWolfFile target, NameMask mask) throws Exception {
        return awaitScan(listener -> scanner.scan(sources, target, storage, mask, listener));
    }

    /**
     * Runs a {@link DeleteScanner} over {@code sources} and blocks until it completes.
     *
     * @throws Exception if the scan fails or is cancelled; the original failure is the returned future's cause.
     */
    public static DeletePlan scanForDelete(List<? extends GreyWolfFile> sources) throws Exception {
        return awaitScan(listener -> new DeleteScanner().scan(sources, listener));
    }

    /**
     * Starts a scan with a listener that completes a future, and blocks until the scan produces its plan.
     *
     * @param starter starts the scan with the given listener.
     * @throws Exception if the scan fails or is cancelled; the original failure is the returned future's cause.
     */
    private static <P> P awaitScan(Consumer<ScanListener<P>> starter) throws Exception {
        var future = new CompletableFuture<P>();
        starter.accept(new ScanListener<P>() {
            @Override
            public void onScanProgress(int foldersFound, int filesFound, long bytesFound) {
                // not needed by tests
            }

            @Override
            public void onScanCompleted(P plan) {
                future.complete(plan);
            }

            @Override
            public void onScanFailed(Exception ex) {
                future.completeExceptionally(ex);
            }

            @Override
            public void onScanCancelled() {
                future.completeExceptionally(new CancellationException("Scan cancelled"));
            }
        });
        return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private static GreyWolfStorage createLocalStorage(Configuration configuration, boolean windows) {
        // the working directory Jimfs creates by default is moved into the root itself, so the root of the file
        // system - which is the root of the storage - holds nothing but what a test puts there
        var fileSystem = Jimfs.newFileSystem(
                configuration.toBuilder().setWorkingDirectory(windows ? "C:\\" : "/").build());
        RETAINED_FILE_SYSTEMS.add(fileSystem);
        var root = fileSystem.getRootDirectories().iterator().next();
        var storage = windows
                ? SystemStorageFactory.createWindowsStorage(FileStorageType.BASE, "test", root.toUri(), "test",
                        DefaultGreyWolfFile::new)
                : SystemStorageFactory.createUnixStorage(FileStorageType.BASE, "test", root.toUri(), "test",
                        DefaultGreyWolfFile::new);
        return storage;
    }

    private OperationTestSupport() {
        // empty
    }
}
