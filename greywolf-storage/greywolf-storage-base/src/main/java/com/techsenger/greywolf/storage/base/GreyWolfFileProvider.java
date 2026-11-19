/*
 * Copyright 2024-2026 Pavel Castornii.
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

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.storage.base.FileStorage;
import com.techsenger.shellfx.storage.base.FileStorageUtils;
import com.techsenger.shellfx.storage.base.OperationType;
import com.techsenger.shellfx.storage.base.async.AsyncResult;
import com.techsenger.shellfx.storage.base.async.AsyncTask;
import com.techsenger.shellfx.storage.base.async.DefaultFileProvider;
import java.io.IOException;
import java.net.URI;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The file provider of GreyWolf. Besides what the default provider completes, it gives the regular files their type
 * by the name; a file whose name is not enough is left to {@link GreyWolfFileRefiner}, which reads the start of its
 * content.
 *
 * <p>The recent storage lists only what was recorded, so this provider checks every record against the storage that
 * holds the file and lists the result in the order of the records, in one phase. There are three cases:
 * <ul>
 * <li>the file exists: the real file of its own storage is listed, and the user goes to that storage from it;</li>
 * <li>the storage is not found or does not answer in time, e.g. an unplugged drive: the recorded file of the recent
 * storage is listed, which is shown as an error, and the record is kept, as the storage may be back;</li>
 * <li>the storage is found but the file does not exist: the file is not listed and its record is removed.</li>
 * </ul>
 *
 * @author Pavel Castornii
 */
public class GreyWolfFileProvider extends DefaultFileProvider<GreyWolfFile> {

    private static final Logger logger = LoggerFactory.getLogger(GreyWolfFileProvider.class);

    private static final long RECENT_TIMEOUT_SECONDS = 10;

    /**
     * Creates a provider over the storages that are there at the moment of each operation.
     *
     * @param storageProvider tells the storages the targets of links and the recorded files may lie in
     */
    public GreyWolfFileProvider(GreyWolfStorageProvider storageProvider) {
        super(storageProvider);
    }

    @Override
    public AsyncTask provideFilesAsync(FileStorage<GreyWolfFile> storage, URI uri, OperationType operationType,
            Consumer<List<GreyWolfFile>> onProvided, Consumer<AsyncResult> onFinished) {
        if (!(storage instanceof RecentStorage recent)) {
            return super.provideFilesAsync(storage, uri, operationType, onProvided, onFinished);
        }
        return provideAsync("files at " + uri, () -> {
            var files = checkEntries(recent, storage.getFiles(uri, operationType));
            files.forEach(file -> assemble(storage, file));
            return files;
        }, onProvided, onFinished);
    }

    /**
     * Checks one recorded entry of the recent storage against the storage that holds its file; the three cases are
     * described in the description of the class.
     *
     * @param recent the recent storage, which forgets the entry if the file no longer exists
     * @param entry the recorded file of the recent storage
     * @return the real file, the entry itself if its storage is not available, or {@code null} if the file does not
     *     exist
     */
    public @Nullable GreyWolfFile checkEntry(RecentStorage recent, GreyWolfFile entry) {
        var uri = entry.getUri();
        var storage = FileStorageUtils.findByUri(getStorageProvider().getStorages(), uri);
        if (storage.isEmpty()) {
            return entry;
        }
        try {
            return storage.get().getFile(uri, OperationType.SECONDARY);
        } catch (NoSuchFileException ex) {
            recent.remove(uri, OperationType.SECONDARY);
            return null;
        } catch (IOException ex) {
            logger.warn("Couldn't read {}", uri, ex);
            return entry;
        }
    }

    @Override
    protected void prepare(FileStorage<GreyWolfFile> storage, GreyWolfFile file) throws IOException {
        // the files of the recent storage come from other storages, and it is the storage of the file that decides
        super.prepare(file.getStorage(), file);
        if (file.isFile()) {
            FileTypeResolver.apply(file, FileTypeResolver.resolveByName(file.getName()));
        }
    }

    /**
     * Checks all the entries at once, so a slow storage delays the others no longer than the timeout.
     */
    private List<GreyWolfFile> checkEntries(RecentStorage recent, List<GreyWolfFile> entries) {
        var executor = Executors.newVirtualThreadPerTaskExecutor();
        try {
            var futures = new ArrayList<Future<GreyWolfFile>>(entries.size());
            for (var entry : entries) {
                futures.add(executor.submit(() -> checkEntry(recent, entry)));
            }
            var deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(RECENT_TIMEOUT_SECONDS);
            var result = new ArrayList<GreyWolfFile>(entries.size());
            for (var i = 0; i < entries.size(); i++) {
                var file = awaitEntry(futures.get(i), entries.get(i), deadline);
                if (file != null) {
                    result.add(file);
                }
            }
            return result;
        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * Returns the file to list for the entry, or {@code null} if the file no longer exists. An entry that does not
     * answer by the deadline is treated as one whose storage is not available.
     */
    private @Nullable GreyWolfFile awaitEntry(Future<GreyWolfFile> future, GreyWolfFile entry, long deadline) {
        try {
            return future.get(Math.max(0, deadline - System.nanoTime()), TimeUnit.NANOSECONDS);
        } catch (TimeoutException ex) {
            logger.warn("No answer for {} in {} s", entry.getUri(), RECENT_TIMEOUT_SECONDS);
            return entry;
        } catch (ExecutionException ex) {
            logger.error("Error checking {}", entry.getUri(), ex);
            return entry;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return entry;
        }
    }
}
