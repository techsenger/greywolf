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

package com.techsenger.greywolf.storage.system.delete;

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.base.GreyWolfStorage;
import com.techsenger.greywolf.storage.base.delete.DeleteListener;
import com.techsenger.greywolf.storage.base.delete.DeletePlan;
import com.techsenger.greywolf.storage.base.delete.DeleteProgress;
import com.techsenger.greywolf.storage.base.delete.Deleter;
import com.techsenger.greywolf.storage.base.delete.FailedDeletion;
import com.techsenger.greywolf.storage.base.operation.ErrorResolution;
import com.techsenger.greywolf.storage.system.OperationTestSupport;
import com.techsenger.shellfx.storage.base.OperationType;
import com.techsenger.shellfx.storage.base.UriUtils;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link Deleter}: verifies what is actually deleted - and, just as important, what is left
 * untouched - given a {@link DeletePlan}; how that plan was built is {@code DeleteScannerIT}'s concern, so
 * {@link OperationTestSupport#scanForDelete} is used here purely as fixture setup. Run against a fresh, in-memory
 * storage created from scratch for every test.
 *
 * @author Pavel Castornii
 */
public class DeleterIT {

    private record DeleteResult(int deleted, int skipped, List<FailedDeletion> failures,
            List<DeleteProgress> progressUpdates, int errorRequests, boolean cancelled,
            @Nullable String abortedReason) {
    }

    private static final long TIMEOUT_SECONDS = 10;

    /**
     * Entries in a tree made by {@link #createTree}: four folders and one file in each of them.
     */
    private static final int TREE_ENTRIES = 8;

    private static byte[] readBytes(GreyWolfStorage storage, URI uri) throws IOException {
        try (var in = storage.openInputStream(uri, OperationType.PRIMARY)) {
            return in.readAllBytes();
        }
    }

    private static String readContent(GreyWolfStorage storage, URI dirUri, String name) throws IOException {
        return new String(readBytes(storage, UriUtils.resolvePath(dirUri, name, false)), StandardCharsets.UTF_8);
    }

    private static List<String> readEntryNames(GreyWolfStorage storage, URI dirUri) throws IOException {
        return storage.getFiles(dirUri, OperationType.PRIMARY).stream().map(GreyWolfFile::getName).toList();
    }

    /**
     * Creates {@code parentUri/name} with four nested levels - {@code name/l2/l3/l4} - and one file in each of
     * them ({@code root.txt}, {@code l2.txt}, {@code l3.txt}, {@code l4.txt}) whose content is {@code name} plus
     * the file's own name, so {@link #assertTreeIntact} can tell trees of different names apart.
     */
    private static GreyWolfFile createTree(GreyWolfStorage storage, URI parentUri, String name)
            throws IOException {
        var top = OperationTestSupport.createDirectory(storage, parentUri, name);
        OperationTestSupport.createFile(storage, top.getUri(), "root.txt", name + "-root");
        var level2 = OperationTestSupport.createDirectory(storage, top.getUri(), "l2");
        OperationTestSupport.createFile(storage, level2.getUri(), "l2.txt", name + "-l2");
        var level3 = OperationTestSupport.createDirectory(storage, level2.getUri(), "l3");
        OperationTestSupport.createFile(storage, level3.getUri(), "l3.txt", name + "-l3");
        var level4 = OperationTestSupport.createDirectory(storage, level3.getUri(), "l4");
        OperationTestSupport.createFile(storage, level4.getUri(), "l4.txt", name + "-l4");
        return top;
    }

    /**
     * Verifies every folder and file of a tree made by {@link #createTree} is still there with its content.
     */
    private static void assertTreeIntact(GreyWolfStorage storage, URI parentUri, String name) throws IOException {
        var topUri = UriUtils.resolvePath(parentUri, name, true);
        var level2Uri = UriUtils.resolvePath(topUri, "l2", true);
        var level3Uri = UriUtils.resolvePath(level2Uri, "l3", true);
        var level4Uri = UriUtils.resolvePath(level3Uri, "l4", true);
        assertThat(readEntryNames(storage, topUri)).containsExactlyInAnyOrder("root.txt", "l2");
        assertThat(readEntryNames(storage, level2Uri)).containsExactlyInAnyOrder("l2.txt", "l3");
        assertThat(readEntryNames(storage, level3Uri)).containsExactlyInAnyOrder("l3.txt", "l4");
        assertThat(readEntryNames(storage, level4Uri)).containsExactly("l4.txt");
        assertThat(readContent(storage, topUri, "root.txt")).isEqualTo(name + "-root");
        assertThat(readContent(storage, level2Uri, "l2.txt")).isEqualTo(name + "-l2");
        assertThat(readContent(storage, level3Uri, "l3.txt")).isEqualTo(name + "-l3");
        assertThat(readContent(storage, level4Uri, "l4.txt")).isEqualTo(name + "-l4");
    }

    private static DeleteResult delete(DeletePlan plan) throws Exception {
        return delete(plan, ErrorResolution.ABORT);
    }

    private static DeleteResult delete(DeletePlan plan, ErrorResolution errorResolution) throws Exception {
        var future = new CompletableFuture<DeleteResult>();
        var progressUpdates = new CopyOnWriteArrayList<DeleteProgress>();
        var errorRequests = new AtomicInteger();
        new Deleter().start(plan, new DeleteListener() {
            @Override
            public void onProgress(DeleteProgress progress) {
                progressUpdates.add(progress);
            }

            @Override
            public ErrorResolution onError(FailedDeletion failure) {
                errorRequests.incrementAndGet();
                return errorResolution;
            }

            @Override
            public void onCompleted(int deleted, int skipped, List<FailedDeletion> failures) {
                future.complete(new DeleteResult(deleted, skipped, failures, progressUpdates,
                        errorRequests.get(), false, null));
            }

            @Override
            public void onCancelled() {
                future.complete(new DeleteResult(0, 0, List.of(), progressUpdates, errorRequests.get(), true,
                        null));
            }

            @Override
            public void onAborted(String reason) {
                future.complete(new DeleteResult(0, 0, List.of(), progressUpdates, errorRequests.get(), false,
                        reason));
            }
        });
        return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void start_singleFile_onlyThatFileDeleted(Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var root = storage.getUri();
        var doomed = OperationTestSupport.createFile(storage, root, "doomed.txt", "bye");
        OperationTestSupport.createFile(storage, root, "kept.txt", "stay");

        var result = delete(OperationTestSupport.scanForDelete(List.of(doomed)));

        assertThat(result.deleted()).isEqualTo(1);
        assertThat(result.failures()).isEmpty();
        assertThat(readEntryNames(storage, root)).containsExactly("kept.txt");
        assertThat(readContent(storage, root, "kept.txt")).isEqualTo("stay");
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void start_emptyDirectory_directoryDeleted(Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var root = storage.getUri();
        var emptyDir = OperationTestSupport.createDirectory(storage, root, "empty");

        var result = delete(OperationTestSupport.scanForDelete(List.of(emptyDir)));

        assertThat(result.deleted()).isEqualTo(1);
        assertThat(readEntryNames(storage, root)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void start_nestedDirectoryFourLevels_wholeTreeDeleted(Supplier<GreyWolfStorage> storageFactory)
            throws Exception {
        var storage = storageFactory.get();
        var root = storage.getUri();
        var doomed = createTree(storage, root, "doomed");

        var result = delete(OperationTestSupport.scanForDelete(List.of(doomed)));

        assertThat(result.failures()).isEmpty();
        assertThat(result.deleted()).isEqualTo(TREE_ENTRIES);
        assertThat(readEntryNames(storage, root)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void start_oneOfThreeSiblingFolders_otherFoldersIntact(Supplier<GreyWolfStorage> storageFactory)
            throws Exception {
        var storage = storageFactory.get();
        var root = storage.getUri();
        createTree(storage, root, "a");
        var doomed = createTree(storage, root, "b");
        createTree(storage, root, "c");

        var result = delete(OperationTestSupport.scanForDelete(List.of(doomed)));

        assertThat(result.failures()).isEmpty();
        assertThat(result.deleted()).isEqualTo(TREE_ENTRIES);
        assertThat(readEntryNames(storage, root)).containsExactlyInAnyOrder("a", "c");
        assertTreeIntact(storage, root, "a");
        assertTreeIntact(storage, root, "c");
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void start_severalMarkedFolders_onlyMarkedDeletedOthersIntact(Supplier<GreyWolfStorage> storageFactory)
            throws Exception {
        var storage = storageFactory.get();
        var root = storage.getUri();
        var markedA = createTree(storage, root, "a");
        createTree(storage, root, "b");
        var markedC = createTree(storage, root, "c");
        createTree(storage, root, "d");

        var result = delete(OperationTestSupport.scanForDelete(List.of(markedA, markedC)));

        assertThat(result.failures()).isEmpty();
        assertThat(result.deleted()).isEqualTo(2 * TREE_ENTRIES);
        assertThat(readEntryNames(storage, root)).containsExactlyInAnyOrder("b", "d");
        assertTreeIntact(storage, root, "b");
        assertTreeIntact(storage, root, "d");
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void start_folderInsideParent_parentAndItsOtherContentsIntact(Supplier<GreyWolfStorage> storageFactory)
            throws Exception {
        var storage = storageFactory.get();
        var root = storage.getUri();
        var parent = OperationTestSupport.createDirectory(storage, root, "parent");
        OperationTestSupport.createFile(storage, parent.getUri(), "keep.txt", "keep me");
        createTree(storage, parent.getUri(), "keepDir");
        var doomed = createTree(storage, parent.getUri(), "doomedDir");
        createTree(storage, root, "neighbour");

        var result = delete(OperationTestSupport.scanForDelete(List.of(doomed)));

        assertThat(result.failures()).isEmpty();
        assertThat(readEntryNames(storage, root)).containsExactlyInAnyOrder("parent", "neighbour");
        assertThat(readEntryNames(storage, parent.getUri())).containsExactlyInAnyOrder("keep.txt", "keepDir");
        assertThat(readContent(storage, parent.getUri(), "keep.txt")).isEqualTo("keep me");
        assertTreeIntact(storage, parent.getUri(), "keepDir");
        assertTreeIntact(storage, root, "neighbour");
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void start_sameNamedFolderInAnotherParent_otherFolderIntact(Supplier<GreyWolfStorage> storageFactory)
            throws Exception {
        var storage = storageFactory.get();
        var root = storage.getUri();
        var parentX = OperationTestSupport.createDirectory(storage, root, "x");
        var parentY = OperationTestSupport.createDirectory(storage, root, "y");
        var doomed = createTree(storage, parentX.getUri(), "data");
        createTree(storage, parentY.getUri(), "data");

        var result = delete(OperationTestSupport.scanForDelete(List.of(doomed)));

        assertThat(result.failures()).isEmpty();
        assertThat(readEntryNames(storage, parentX.getUri())).isEmpty();
        assertTreeIntact(storage, parentY.getUri(), "data");
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void start_fileAmongSiblings_siblingFilesAndFoldersIntact(Supplier<GreyWolfStorage> storageFactory)
            throws Exception {
        var storage = storageFactory.get();
        var root = storage.getUri();
        var doomed = OperationTestSupport.createFile(storage, root, "doomed.txt", "bye");
        OperationTestSupport.createFile(storage, root, "doomed.txt.bak", "backup");
        OperationTestSupport.createFile(storage, root, "other.txt", "other");
        createTree(storage, root, "folder");

        var result = delete(OperationTestSupport.scanForDelete(List.of(doomed)));

        assertThat(result.failures()).isEmpty();
        assertThat(readEntryNames(storage, root))
                .containsExactlyInAnyOrder("doomed.txt.bak", "other.txt", "folder");
        assertThat(readContent(storage, root, "doomed.txt.bak")).isEqualTo("backup");
        assertTreeIntact(storage, root, "folder");
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void start_symbolicLinkToFolder_linkDeletedAndTargetFolderIntact(
            Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var root = storage.getUri();
        createTree(storage, root, "victim");
        var doomed = OperationTestSupport.createDirectory(storage, root, "doomed");
        Files.createSymbolicLink(Path.of(UriUtils.resolvePath(doomed.getUri(), "link", true)),
                Path.of(UriUtils.resolvePath(root, "victim", true)));

        var result = delete(OperationTestSupport.scanForDelete(List.of(doomed)));

        assertThat(result.failures()).isEmpty();
        assertThat(readEntryNames(storage, root)).containsExactly("victim");
        assertTreeIntact(storage, root, "victim");
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void start_entryAlreadyMissing_countedAsDeletedWithoutFailure(Supplier<GreyWolfStorage> storageFactory)
            throws Exception {
        var storage = storageFactory.get();
        var root = storage.getUri();
        var vanishing = OperationTestSupport.createFile(storage, root, "vanishing.txt", "gone soon");
        var plan = OperationTestSupport.scanForDelete(List.of(vanishing));
        storage.delete(vanishing.getUri(), OperationType.PRIMARY);

        var result = delete(plan);

        assertThat(result.deleted()).isEqualTo(1);
        assertThat(result.failures()).isEmpty();
        assertThat(result.errorRequests()).isZero();
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void start_multipleFiles_finalProgressReflectsTotals(Supplier<GreyWolfStorage> storageFactory)
            throws Exception {
        var storage = storageFactory.get();
        var root = storage.getUri();
        var sources = List.of(
                OperationTestSupport.createFile(storage, root, "a.txt", "aaaa"),
                OperationTestSupport.createFile(storage, root, "b.txt", "bbbbbbbb"),
                OperationTestSupport.createFile(storage, root, "c.txt", "cc"));

        var result = delete(OperationTestSupport.scanForDelete(sources));

        assertThat(result.progressUpdates()).isNotEmpty();
        var last = result.progressUpdates().get(result.progressUpdates().size() - 1);
        assertThat(last.itemsDone()).isEqualTo(3);
        assertThat(last.totalItems()).isEqualTo(3);
        assertThat(last.bytesDone()).isEqualTo(14L);
        assertThat(last.totalBytes()).isEqualTo(14L);
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void start_cancelledDuringDelete_stopsAndKeepsRemainingEntries(Supplier<GreyWolfStorage> storageFactory)
            throws Exception {
        var storage = storageFactory.get();
        var root = storage.getUri();
        var sources = List.of(
                OperationTestSupport.createFile(storage, root, "a.txt", "a"),
                OperationTestSupport.createFile(storage, root, "b.txt", "b"),
                OperationTestSupport.createFile(storage, root, "c.txt", "c"));
        var plan = OperationTestSupport.scanForDelete(sources);

        var deleter = new Deleter();
        var future = new CompletableFuture<Boolean>();
        deleter.start(plan, new DeleteListener() {
            @Override
            public void onProgress(DeleteProgress progress) {
                deleter.cancel();
            }

            @Override
            public ErrorResolution onError(FailedDeletion failure) {
                return ErrorResolution.ABORT;
            }

            @Override
            public void onCompleted(int deleted, int skipped, List<FailedDeletion> failures) {
                future.complete(false);
            }

            @Override
            public void onCancelled() {
                future.complete(true);
            }

            @Override
            public void onAborted(String reason) {
                future.complete(false);
            }
        });

        assertThat(future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue();
        assertThat(readEntryNames(storage, root)).containsExactlyInAnyOrder("b.txt", "c.txt");
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void start_nonEmptyFolderFails_skipResolution_failureRecordedAndFolderKept(
            Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var root = storage.getUri();
        var notEmpty = createTree(storage, root, "notEmpty");
        var plan = new DeletePlan(List.of(notEmpty), 0L, 0, 1);

        var result = delete(plan, ErrorResolution.SKIP);

        assertThat(result.deleted()).isZero();
        assertThat(result.skipped()).isEqualTo(1);
        assertThat(result.failures()).hasSize(1);
        assertThat(result.failures().get(0).file().getName()).isEqualTo("notEmpty");
        assertTreeIntact(storage, root, "notEmpty");
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void start_nonEmptyFolderFails_abortResolution_reportsAbortedWithPath(
            Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var root = storage.getUri();
        var notEmpty = createTree(storage, root, "notEmpty");
        var plan = new DeletePlan(List.of(notEmpty), 0L, 0, 1);

        var result = delete(plan, ErrorResolution.ABORT);

        assertThat(result.abortedReason()).isNotNull().contains(notEmpty.getUri().toString());
        assertThat(result.errorRequests()).isEqualTo(1);
        assertTreeIntact(storage, root, "notEmpty");
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void start_twoFailures_skipAllResolution_askedOnlyOnce(Supplier<GreyWolfStorage> storageFactory)
            throws Exception {
        var storage = storageFactory.get();
        var root = storage.getUri();
        var first = createTree(storage, root, "first");
        var second = createTree(storage, root, "second");
        var plan = new DeletePlan(List.of(first, second), 0L, 0, 2);

        var result = delete(plan, ErrorResolution.SKIP_ALL);

        assertThat(result.errorRequests()).isEqualTo(1);
        assertThat(result.failures()).hasSize(2);
        assertThat(result.skipped()).isEqualTo(2);
        assertTreeIntact(storage, root, "first");
        assertTreeIntact(storage, root, "second");
    }
}
