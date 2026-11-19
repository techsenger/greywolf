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

import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.base.GreyWolfStorage;
import com.techsenger.greywolf.storage.base.delete.DeletePlan;
import com.techsenger.greywolf.storage.base.delete.DeleteScanner;
import com.techsenger.greywolf.storage.system.OperationTestSupport;
import com.techsenger.shellfx.storage.base.UriUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DeleteScanner}: verifies the {@link DeletePlan} it builds - which entries are in it
 * and in which order - without deleting anything (that's {@code DeleterIT}'s responsibility). Run against a fresh,
 * in-memory storage created from scratch for every test.
 *
 * @author Pavel Castornii
 */
public class DeleteScannerIT {

    private static List<String> uris(DeletePlan plan) {
        return plan.entries().stream().map(entry -> entry.getUri().toString()).toList();
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void scan_singleFile_planContainsOnlyThatFile(Supplier<GreyWolfStorage> storageFactory)
            throws Exception {
        var storage = storageFactory.get();
        var source = OperationTestSupport.createFile(storage, storage.getUri(), "a.txt", "abc");

        var plan = OperationTestSupport.scanForDelete(List.of(source));

        assertThat(plan.entries()).extracting(GreyWolfFile::getName).containsExactly("a.txt");
        assertThat(plan.totalFiles()).isEqualTo(1);
        assertThat(plan.totalFolders()).isZero();
        assertThat(plan.totalBytes()).isEqualTo(3L);
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void scan_nestedDirectoryFourLevels_planContainsEveryEntryWithTotals(
            Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var level1 = OperationTestSupport.createDirectory(storage, storage.getUri(), "level1");
        OperationTestSupport.createFile(storage, level1.getUri(), "one.txt", "1");
        var level2 = OperationTestSupport.createDirectory(storage, level1.getUri(), "level2");
        OperationTestSupport.createFile(storage, level2.getUri(), "two.txt", "22");
        var level3 = OperationTestSupport.createDirectory(storage, level2.getUri(), "level3");
        OperationTestSupport.createFile(storage, level3.getUri(), "three.txt", "333");
        var level4 = OperationTestSupport.createDirectory(storage, level3.getUri(), "level4");
        OperationTestSupport.createFile(storage, level4.getUri(), "four.txt", "4444");

        var plan = OperationTestSupport.scanForDelete(List.of(level1));

        assertThat(plan.entries()).extracting(GreyWolfFile::getName).containsExactlyInAnyOrder("level1", "level2",
                "level3", "level4", "one.txt", "two.txt", "three.txt", "four.txt");
        assertThat(plan.totalFiles()).isEqualTo(4);
        assertThat(plan.totalFolders()).isEqualTo(4);
        assertThat(plan.totalBytes()).isEqualTo(10L);
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void scan_nestedDirectory_everyFolderComesAfterItsWholeContent(Supplier<GreyWolfStorage> storageFactory)
            throws Exception {
        var storage = storageFactory.get();
        var level1 = OperationTestSupport.createDirectory(storage, storage.getUri(), "level1");
        OperationTestSupport.createFile(storage, level1.getUri(), "one.txt", "1");
        var level2 = OperationTestSupport.createDirectory(storage, level1.getUri(), "level2");
        OperationTestSupport.createFile(storage, level2.getUri(), "two.txt", "2");
        var level3 = OperationTestSupport.createDirectory(storage, level2.getUri(), "level3");
        OperationTestSupport.createFile(storage, level3.getUri(), "three.txt", "3");

        var plan = OperationTestSupport.scanForDelete(List.of(level1));

        var uris = uris(plan);
        for (var entry : plan.entries()) {
            if (!entry.isDirectory()) {
                continue;
            }
            var folderIndex = uris.indexOf(entry.getUri().toString());
            for (var i = 0; i < uris.size(); i++) {
                var isInside = i != folderIndex && uris.get(i).startsWith(entry.getUri().toString());
                if (isInside) {
                    assertThat(i).as("%s must come before its folder %s", uris.get(i), entry.getName())
                            .isLessThan(folderIndex);
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void scan_oneOfThreeSiblingFolders_planHasNothingFromTheOthers(Supplier<GreyWolfStorage> storageFactory)
            throws Exception {
        var storage = storageFactory.get();
        var folders = List.of(
                OperationTestSupport.createDirectory(storage, storage.getUri(), "a"),
                OperationTestSupport.createDirectory(storage, storage.getUri(), "b"),
                OperationTestSupport.createDirectory(storage, storage.getUri(), "c"));
        for (var folder : folders) {
            var inner = OperationTestSupport.createDirectory(storage, folder.getUri(), "inner");
            OperationTestSupport.createFile(storage, inner.getUri(), folder.getName() + ".txt", "x");
        }

        var plan = OperationTestSupport.scanForDelete(List.of(folders.get(1)));

        assertThat(plan.entries()).extracting(GreyWolfFile::getName)
                .containsExactlyInAnyOrder("b", "inner", "b.txt");
        var bUri = folders.get(1).getUri().toString();
        assertThat(uris(plan)).allMatch(uri -> uri.startsWith(bUri));
    }

    @ParameterizedTest
    @MethodSource(OperationTestSupport.STORAGES)
    public void scan_symbolicLinkToFolder_planContainsOnlyTheLinkNotTheTargetContent(
            Supplier<GreyWolfStorage> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var victim = OperationTestSupport.createDirectory(storage, storage.getUri(), "victim");
        OperationTestSupport.createFile(storage, victim.getUri(), "precious.txt", "precious");
        var doomed = OperationTestSupport.createDirectory(storage, storage.getUri(), "doomed");
        Files.createSymbolicLink(Path.of(UriUtils.resolvePath(doomed.getUri(), "link", true)),
                Path.of(victim.getUri()));

        var plan = OperationTestSupport.scanForDelete(List.of(doomed));

        assertThat(plan.entries()).extracting(GreyWolfFile::getName).containsExactlyInAnyOrder("doomed", "link");
    }
}
