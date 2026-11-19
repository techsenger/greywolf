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

package com.techsenger.greywolf.storage.system.transfer;

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.base.GreyWolfStorage;
import com.techsenger.greywolf.storage.base.operation.ErrorResolution;
import com.techsenger.greywolf.storage.base.transfer.ConflictDecision;
import com.techsenger.greywolf.storage.base.transfer.ConflictResolution;
import com.techsenger.greywolf.storage.base.transfer.Copier;
import com.techsenger.greywolf.storage.base.transfer.CopyConflict;
import com.techsenger.greywolf.storage.base.transfer.FailedOperation;
import com.techsenger.greywolf.storage.base.transfer.TransferListener;
import com.techsenger.greywolf.storage.base.transfer.TransferPlan;
import com.techsenger.greywolf.storage.base.transfer.TransferProgress;
import com.techsenger.greywolf.storage.system.OperationTestSupport;
import com.techsenger.shellfx.storage.base.OperationType;
import com.techsenger.shellfx.storage.base.UriUtils;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link Copier}: verifies actual copying behavior (content, conflicts, errors, progress,
 * cancellation) given a {@link TransferPlan} - how that plan was built is {@code TransferScannerIT}'s concern, so
 * {@link OperationTestSupport#scan} is used here purely as fixture setup. Run against a fresh, in-memory storage
 * created from scratch for every test.
 *
 * @author Pavel Castornii
 */
public class CopierIT {

    private record CopyResult(int filesCopied, int filesSkipped, List<FailedOperation> failures,
            List<TransferProgress> progressUpdates, boolean cancelled, @Nullable String abortedReason) {
    }

    private static final long TIMEOUT_SECONDS = 10;

    private static byte[] readBytes(GreyWolfStorage storage, URI uri) throws IOException {
        try (var in = storage.openInputStream(uri, OperationType.PRIMARY)) {
            return in.readAllBytes();
        }
    }

    private static String readContent(GreyWolfStorage storage, GreyWolfFile dir, String name) throws IOException {
        return new String(readContentBytes(storage, dir, name), StandardCharsets.UTF_8);
    }

    private static byte[] readContentBytes(GreyWolfStorage storage, GreyWolfFile dir, String name)
            throws IOException {
        return readBytes(storage, UriUtils.resolvePath(dir.getUri(), name, false));
    }

    private static List<String> readEntryNames(GreyWolfStorage storage, GreyWolfFile dir) throws IOException {
        return storage.getFiles(dir.getUri(), OperationType.PRIMARY).stream().map(GreyWolfFile::getName).toList();
    }

    private static byte[] largeContent(int size) {
        var content = new byte[size];
        for (var i = 0; i < size; i++) {
            content[i] = (byte) i;
        }
        return content;
    }

    private static CopyResult copy(TransferPlan plan) throws Exception {
        return copy(plan, ConflictResolution.SKIP, ErrorResolution.ABORT);
    }

    private static CopyResult copy(TransferPlan plan, ConflictResolution conflictResolution,
            ErrorResolution errorResolution) throws Exception {
        var future = new CompletableFuture<CopyResult>();
        var progressUpdates = new CopyOnWriteArrayList<TransferProgress>();
        new Copier().start(plan, new TransferListener() {
            @Override
            public ConflictDecision onConflict(CopyConflict conflict) {
                return new ConflictDecision(conflictResolution, false);
            }

            @Override
            public void onProgress(TransferProgress progress) {
                progressUpdates.add(progress);
            }

            @Override
            public ErrorResolution onError(FailedOperation failure) {
                return errorResolution;
            }

            @Override
            public void onCompleted(int filesCopied, int filesSkipped, List<FailedOperation> failures) {
                future.complete(new CopyResult(filesCopied, filesSkipped, failures, progressUpdates, false, null));
            }

            @Override
            public void onCancelled() {
                future.complete(new CopyResult(0, 0, List.of(), progressUpdates, true, null));
            }

            @Override
            public void onAborted(String reason) {
                future.complete(new CopyResult(0, 0, List.of(), progressUpdates, false, reason));
            }
        });
        return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void copySingleFile_fileWithContent_copiedWithSameContent(Supplier<GreyWolfStorage> storageFactory)
            throws Exception {
        var storage = storageFactory.get();
        var source = OperationTestSupport.createFile(storage, storage.getUri(), "source.txt", "hello world");
        var target = OperationTestSupport.createDirectory(storage, storage.getUri(), "target");

        var plan = OperationTestSupport.scan(storage, List.of(source), target);
        var result = copy(plan);

        assertThat(result.filesCopied()).isEqualTo(1);
        assertThat(result.failures()).isEmpty();
        assertThat(readContent(storage, target, "source.txt")).isEqualTo("hello world");
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void copyDirectory_withFiles_allFilesCopied(Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var sourceDir = OperationTestSupport.createDirectory(storage, storage.getUri(), "srcDir");
        OperationTestSupport.createFile(storage, sourceDir.getUri(), "one.txt", "one");
        OperationTestSupport.createFile(storage, sourceDir.getUri(), "two.txt", "two");
        var target = OperationTestSupport.createDirectory(storage, storage.getUri(), "target");

        var plan = OperationTestSupport.scan(storage, List.of(sourceDir), target);
        var result = copy(plan);

        assertThat(result.failures()).isEmpty();
        var copiedDir = storage.getFile(UriUtils.resolvePath(target.getUri(), "srcDir", true), OperationType.PRIMARY);
        assertThat(readEntryNames(storage, copiedDir)).containsExactlyInAnyOrder("one.txt", "two.txt");
        assertThat(readContent(storage, copiedDir, "one.txt")).isEqualTo("one");
        assertThat(readContent(storage, copiedDir, "two.txt")).isEqualTo("two");
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void copyEmptyDirectory_noChildren_directoryCreatedAtTarget(Supplier<GreyWolfStorage> storageFactory)
            throws Exception {
        var storage = storageFactory.get();
        var sourceDir = OperationTestSupport.createDirectory(storage, storage.getUri(), "emptyDir");
        var target = OperationTestSupport.createDirectory(storage, storage.getUri(), "target");

        var plan = OperationTestSupport.scan(storage, List.of(sourceDir), target);
        var result = copy(plan);

        assertThat(result.failures()).isEmpty();
        var copiedDir = storage.getFile(UriUtils.resolvePath(target.getUri(), "emptyDir", true), OperationType.PRIMARY);
        assertThat(copiedDir.isDirectory()).isTrue();
        assertThat(readEntryNames(storage, copiedDir)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void copyNestedDirectory_multipleLevels_structurePreservedAtEveryLevel(
            Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var level1 = OperationTestSupport.createDirectory(storage, storage.getUri(), "level1");
        var level2 = OperationTestSupport.createDirectory(storage, level1.getUri(), "level2");
        var level3 = OperationTestSupport.createDirectory(storage, level2.getUri(), "level3");
        OperationTestSupport.createFile(storage, level3.getUri(), "deep.txt", "deep content");
        OperationTestSupport.createFile(storage, level1.getUri(), "shallow.txt", "shallow content");
        var target = OperationTestSupport.createDirectory(storage, storage.getUri(), "target");

        var plan = OperationTestSupport.scan(storage, List.of(level1), target);
        var result = copy(plan);

        assertThat(result.failures()).isEmpty();
        var copiedLevel1Uri = UriUtils.resolvePath(target.getUri(), "level1", true);
        var copiedLevel2Uri = UriUtils.resolvePath(copiedLevel1Uri, "level2", true);
        var copiedLevel3Uri = UriUtils.resolvePath(copiedLevel2Uri, "level3", true);
        assertThat(readContent(storage, storage.getFile(copiedLevel3Uri, OperationType.PRIMARY), "deep.txt"))
                .isEqualTo("deep content");
        assertThat(readContent(storage, storage.getFile(copiedLevel1Uri, OperationType.PRIMARY), "shallow.txt"))
                .isEqualTo("shallow content");
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void copyMultipleFiles_severalFiles_allCopiedWithContent(Supplier<GreyWolfStorage> storageFactory)
            throws Exception {
        var storage = storageFactory.get();
        var sources = List.of(
                OperationTestSupport.createFile(storage, storage.getUri(), "one.txt", "one"),
                OperationTestSupport.createFile(storage, storage.getUri(), "two.txt", "two"),
                OperationTestSupport.createFile(storage, storage.getUri(), "three.txt", "three"));
        var target = OperationTestSupport.createDirectory(storage, storage.getUri(), "target");

        var plan = OperationTestSupport.scan(storage, sources, target);
        var result = copy(plan);

        assertThat(result.filesCopied()).isEqualTo(3);
        assertThat(readContent(storage, target, "one.txt")).isEqualTo("one");
        assertThat(readContent(storage, target, "two.txt")).isEqualTo("two");
        assertThat(readContent(storage, target, "three.txt")).isEqualTo("three");
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void copyFileWithLargeSize_contentLargerThanBufferSize_copiedFully(
            Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var content = largeContent(50_000);
        var source = OperationTestSupport.createFile(storage, storage.getUri(), "large.bin", content);
        var target = OperationTestSupport.createDirectory(storage, storage.getUri(), "target");

        var plan = OperationTestSupport.scan(storage, List.of(source), target);
        var result = copy(plan);

        assertThat(result.filesCopied()).isEqualTo(1);
        assertThat(readContentBytes(storage, target, "large.bin")).isEqualTo(content);
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void copyToExistingDirectory_targetSubdirectoryAlreadyExists_mergesContents(
            Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var target = OperationTestSupport.createDirectory(storage, storage.getUri(), "target");
        var existingSubDir = OperationTestSupport.createDirectory(storage, target.getUri(), "shared");
        OperationTestSupport.createFile(storage, existingSubDir.getUri(), "old.txt", "old");
        var sourceDir = OperationTestSupport.createDirectory(storage, storage.getUri(), "shared");
        OperationTestSupport.createFile(storage, sourceDir.getUri(), "new.txt", "new");

        var plan = OperationTestSupport.scan(storage, List.of(sourceDir), target);
        var result = copy(plan);

        assertThat(result.failures()).isEmpty();
        var mergedDir = storage.getFile(existingSubDir.getUri(), OperationType.PRIMARY);
        assertThat(readEntryNames(storage, mergedDir)).containsExactlyInAnyOrder("old.txt", "new.txt");
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void overwriteExistingFile_conflictResolutionOverwrite_targetContentReplaced(
            Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var target = OperationTestSupport.createDirectory(storage, storage.getUri(), "target");
        OperationTestSupport.createFile(storage, target.getUri(), "file.txt", "old content");
        var source = OperationTestSupport.createFile(storage, storage.getUri(), "file.txt", "new content");

        var plan = OperationTestSupport.scan(storage, List.of(source), target);
        var result = copy(plan, ConflictResolution.OVERWRITE, ErrorResolution.ABORT);

        assertThat(result.filesCopied()).isEqualTo(1);
        assertThat(readContent(storage, target, "file.txt")).isEqualTo("new content");
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void skipExistingFile_conflictResolutionSkip_targetContentUnchanged(
            Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var target = OperationTestSupport.createDirectory(storage, storage.getUri(), "target");
        OperationTestSupport.createFile(storage, target.getUri(), "file.txt", "old content");
        var source = OperationTestSupport.createFile(storage, storage.getUri(), "file.txt", "new content");

        var plan = OperationTestSupport.scan(storage, List.of(source), target);
        var result = copy(plan, ConflictResolution.SKIP, ErrorResolution.ABORT);

        assertThat(result.filesCopied()).isEqualTo(0);
        assertThat(result.filesSkipped()).isEqualTo(1);
        assertThat(readContent(storage, target, "file.txt")).isEqualTo("old content");
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void calculateProgress_multipleFiles_finalProgressReflectsTotals(Supplier<GreyWolfStorage> storageFactory)
            throws Exception {
        var storage = storageFactory.get();
        var sources = List.of(
                OperationTestSupport.createFile(storage, storage.getUri(), "a.txt", "aaaa"),
                OperationTestSupport.createFile(storage, storage.getUri(), "b.txt", "bbbbbbbb"),
                OperationTestSupport.createFile(storage, storage.getUri(), "c.txt", "cc"));
        var target = OperationTestSupport.createDirectory(storage, storage.getUri(), "target");

        var plan = OperationTestSupport.scan(storage, sources, target);
        var result = copy(plan);

        assertThat(result.progressUpdates()).isNotEmpty();
        var last = result.progressUpdates().get(result.progressUpdates().size() - 1);
        assertThat(last.filesDone()).isEqualTo(3);
        assertThat(last.totalFiles()).isEqualTo(3);
        assertThat(last.bytesDone()).isEqualTo(14L);
        assertThat(last.totalBytes()).isEqualTo(14L);
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void cancellation_cancelledDuringCopy_stopsAndReportsCancelled(Supplier<GreyWolfStorage> storageFactory)
            throws Exception {
        var storage = storageFactory.get();
        var sources = List.of(
                OperationTestSupport.createFile(storage, storage.getUri(), "a.bin", largeContent(20_000)),
                OperationTestSupport.createFile(storage, storage.getUri(), "b.bin", largeContent(20_000)),
                OperationTestSupport.createFile(storage, storage.getUri(), "c.bin", largeContent(20_000)));
        var target = OperationTestSupport.createDirectory(storage, storage.getUri(), "target");
        var plan = OperationTestSupport.scan(storage, sources, target);

        var copier = new Copier();
        var future = new CompletableFuture<Boolean>();
        copier.start(plan, new TransferListener() {
            @Override
            public ConflictDecision onConflict(CopyConflict conflict) {
                return new ConflictDecision(ConflictResolution.SKIP, false);
            }

            @Override
            public void onProgress(TransferProgress progress) {
                copier.cancel();
            }

            @Override
            public ErrorResolution onError(FailedOperation failure) {
                return ErrorResolution.ABORT;
            }

            @Override
            public void onCompleted(int filesCopied, int filesSkipped, List<FailedOperation> failures) {
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
        assertThat(readEntryNames(storage, target)).containsExactly("a.bin");
    }
}
