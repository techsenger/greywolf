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

import com.techsenger.greywolf.storage.base.GreyWolfStorage;
import com.techsenger.greywolf.storage.base.operation.NameMask;
import com.techsenger.greywolf.storage.base.transfer.TransferPlan;
import com.techsenger.greywolf.storage.base.transfer.TransferScanner;
import com.techsenger.greywolf.storage.system.OperationTestSupport;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.function.Supplier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link TransferScanner}: verifies the {@link TransferPlan} it builds, without touching
 * any actual copying (that's {@code CopierIT}'s responsibility). Run against a fresh, in-memory storage created
 * from scratch for every test.
 *
 * @author Pavel Castornii
 */
public class TransferScannerIT {

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void scan_singleFile_planContainsOneFileTask(Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var source = OperationTestSupport.createFile(storage, storage.getUri(), "a.txt", "abc");
        var target = OperationTestSupport.createDirectory(storage, storage.getUri(), "target");

        var plan = OperationTestSupport.scan(storage, List.of(source), target);

        assertThat(plan.tasks()).hasSize(1);
        assertThat(plan.tasks().get(0).source().getName()).isEqualTo("a.txt");
        assertThat(plan.totalFiles()).isEqualTo(1);
        assertThat(plan.totalFolders()).isEqualTo(0);
        assertThat(plan.totalBytes()).isEqualTo(3L);
        assertThat(plan.skippedEntries()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void scan_directoryWithFiles_planContainsDirectoryAndFileTasks(Supplier<GreyWolfStorage> storageFactory)
            throws Exception {
        var storage = storageFactory.get();
        var sourceDir = OperationTestSupport.createDirectory(storage, storage.getUri(), "srcDir");
        OperationTestSupport.createFile(storage, sourceDir.getUri(), "one.txt", "one");
        OperationTestSupport.createFile(storage, sourceDir.getUri(), "two.txt", "two");
        var target = OperationTestSupport.createDirectory(storage, storage.getUri(), "target");

        var plan = OperationTestSupport.scan(storage, List.of(sourceDir), target);

        assertThat(plan.tasks()).hasSize(3);
        assertThat(plan.tasks().get(0).source().getName()).isEqualTo("srcDir");
        assertThat(plan.totalFolders()).isEqualTo(1);
        assertThat(plan.totalFiles()).isEqualTo(2);
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void scan_emptyDirectory_planContainsOnlyDirectoryTask(Supplier<GreyWolfStorage> storageFactory)
            throws Exception {
        var storage = storageFactory.get();
        var sourceDir = OperationTestSupport.createDirectory(storage, storage.getUri(), "emptyDir");
        var target = OperationTestSupport.createDirectory(storage, storage.getUri(), "target");

        var plan = OperationTestSupport.scan(storage, List.of(sourceDir), target);

        assertThat(plan.tasks()).hasSize(1);
        assertThat(plan.totalFolders()).isEqualTo(1);
        assertThat(plan.totalFiles()).isEqualTo(0);
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void scan_nestedDirectories_planContainsEveryLevel(Supplier<GreyWolfStorage> storageFactory)
            throws Exception {
        var storage = storageFactory.get();
        var level1 = OperationTestSupport.createDirectory(storage, storage.getUri(), "level1");
        var level2 = OperationTestSupport.createDirectory(storage, level1.getUri(), "level2");
        var level3 = OperationTestSupport.createDirectory(storage, level2.getUri(), "level3");
        OperationTestSupport.createFile(storage, level3.getUri(), "deep.txt", "deep content");
        OperationTestSupport.createFile(storage, level1.getUri(), "shallow.txt", "shallow content");
        var target = OperationTestSupport.createDirectory(storage, storage.getUri(), "target");

        var plan = OperationTestSupport.scan(storage, List.of(level1), target);

        assertThat(plan.tasks()).hasSize(5);
        assertThat(plan.totalFolders()).isEqualTo(3);
        assertThat(plan.totalFiles()).isEqualTo(2);
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void scan_multipleFiles_totalsReflectAllSources(Supplier<GreyWolfStorage> storageFactory)
            throws Exception {
        var storage = storageFactory.get();
        var sources = List.of(
                OperationTestSupport.createFile(storage, storage.getUri(), "a.txt", "aaaa"),
                OperationTestSupport.createFile(storage, storage.getUri(), "b.txt", "bbbbbbbb"),
                OperationTestSupport.createFile(storage, storage.getUri(), "c.txt", "cc"));
        var target = OperationTestSupport.createDirectory(storage, storage.getUri(), "target");

        var plan = OperationTestSupport.scan(storage, sources, target);

        assertThat(plan.totalFiles()).isEqualTo(3);
        assertThat(plan.totalBytes()).isEqualTo(14L);
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void scan_maskFiltersNonMatchingEntries_excludedFromPlan(Supplier<GreyWolfStorage> storageFactory)
            throws Exception {
        var storage = storageFactory.get();
        var sources = List.of(
                OperationTestSupport.createFile(storage, storage.getUri(), "keep.txt", "keep"),
                OperationTestSupport.createFile(storage, storage.getUri(), "skip.log", "skip"));
        var target = OperationTestSupport.createDirectory(storage, storage.getUri(), "target");
        var mask = new NameMask("*.txt", false, false);

        var plan = OperationTestSupport.scan(storage, sources, target, mask);

        assertThat(plan.tasks()).hasSize(1);
        assertThat(plan.tasks().get(0).source().getName()).isEqualTo("keep.txt");
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void scan_maskExcludesDirectory_wholeSubtreeExcludedFromPlan(Supplier<GreyWolfStorage> storageFactory)
            throws Exception {
        var storage = storageFactory.get();
        var excludedDir = OperationTestSupport.createDirectory(storage, storage.getUri(), "excludedDir");
        OperationTestSupport.createFile(storage, excludedDir.getUri(), "keep.txt", "content");
        var target = OperationTestSupport.createDirectory(storage, storage.getUri(), "target");
        var mask = new NameMask("keep*", false, false);

        var plan = OperationTestSupport.scan(storage, List.of(excludedDir), target, mask);

        assertThat(plan.tasks()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void scan_sourceIsAncestorOfTarget_reportsScanFailed(Supplier<GreyWolfStorage> storageFactory)
            throws Exception {
        var storage = storageFactory.get();
        var parent = OperationTestSupport.createDirectory(storage, storage.getUri(), "parent");
        var target = OperationTestSupport.createDirectory(storage, parent.getUri(), "child");

        assertThatThrownBy(() -> OperationTestSupport.scan(storage, List.of(parent), target))
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void scan_cancelledBeforeStart_reportsCancelled(Supplier<GreyWolfStorage> storageFactory)
            throws Exception {
        var storage = storageFactory.get();
        var source = OperationTestSupport.createFile(storage, storage.getUri(), "a.txt", "abc");
        var target = OperationTestSupport.createDirectory(storage, storage.getUri(), "target");
        var scanner = new TransferScanner();
        scanner.cancel();

        assertThatThrownBy(() -> OperationTestSupport.scan(scanner, storage, List.of(source), target,
                OperationTestSupport.MATCH_ALL))
                .hasCauseInstanceOf(CancellationException.class);
    }
}
