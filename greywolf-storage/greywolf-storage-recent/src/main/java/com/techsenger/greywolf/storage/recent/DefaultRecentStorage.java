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

package com.techsenger.greywolf.storage.recent;

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.base.GreyWolfFileOperations;
import com.techsenger.greywolf.storage.base.GreyWolfStorage;
import com.techsenger.greywolf.storage.base.DefaultGreyWolfFile;
import com.techsenger.greywolf.storage.base.FormatException;
import com.techsenger.greywolf.storage.base.LocationConverter;
import com.techsenger.greywolf.storage.base.PersonalDirectories;
import com.techsenger.greywolf.storage.base.RecentStorage;
import com.techsenger.greywolf.storage.base.StorageCategory;
import com.techsenger.greywolf.storage.recent.style.RecentStorageIcons;
import com.techsenger.shellfx.base.icon.FontIcon;
import com.techsenger.shellfx.storage.base.FileEntryType;
import com.techsenger.shellfx.storage.base.FileOperation;
import com.techsenger.shellfx.storage.base.FileStorageType;
import com.techsenger.shellfx.storage.base.OperationType;
import com.techsenger.shellfx.storage.base.StorageIconProvider;
import com.techsenger.shellfx.storage.base.StorageListener;
import com.techsenger.shellfx.storage.base.StorageListenerSupport;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.SequencedMap;
import java.util.stream.Collectors;

/**
 * A {@link RecentStorage} that aggregates recently used files from other, real storages into one flat,
 * navigable listing - a facade, not a real backend. It knows no other storage, so it lists the recorded entries as
 * files of its own; the file provider checks them against the storages that hold the files and lists the real files
 * instead, so {@link GreyWolfFile#getStorage()} reports the real storage, and operations on the files go to it. The
 * own file operations of this storage are not supported.
 *
 * <p><b>Listeners.</b> As on any storage, a {@link StorageListener} of this storage is told about the operations
 * performed through this storage - that is, by calling its own methods - once per operation, after it has
 * succeeded. The reported URI is the URI of the file the operation was performed on (the one of its real
 * storage); for {@link GreyWolfFileOperations#LIST} it is the URI that was listed or looked up. Nothing else
 * reaches the listeners:
 * <ul>
 * <li>an operation performed directly on a real storage, which is what happens with the files listed here, since
 * they belong to their real storage; its listeners are told about it, this storage's are not;</li>
 * <li>an operation that failed.</li>
 * </ul>
 * The only exception is dropping a recorded file from the list ({@link #remove}, {@link #clear}, a deleted file
 * dropped by {@link #onOperation}): it is reported once per file as {@link GreyWolfFileOperations#DELETE} of its
 * URI, with the type its caller gives, so a view of the list can forget the file.
 *
 * <p><b>Following.</b> This storage knows no other storage: the registry reports the operations performed on the
 * real ones ({@link #onOperation}). This only
 * keeps the recorded entries up to date (a visit is recorded, a deleted file is dropped); the listeners are not
 * told about these operations, because they were not performed through this storage, but only about the drop of a
 * recorded file. So an operation performed through this storage reaches its listeners from the method itself, and so
 * it is reported exactly once.
 * {@link #notifyOperation}, which reports an operation as this storage's own, only tells the listeners.
 *
 * @author Pavel Castornii
 */
final class DefaultRecentStorage implements RecentStorage {

    private static final URI ROOT_URI = URI.create("recent:///");

    private static final String READ_ONLY_MESSAGE = "The recent storage holds no files of its own, operate on the "
            + "storage of the file";

    /**
     * How many directory entries {@link #entriesByUri} keeps before evicting its lowest-{@link #score} one;
     * tracked separately from {@link #MAX_FILE_ENTRIES} since directories and files are visited at very different
     * rates.
     */
    private static final int MAX_DIRECTORY_ENTRIES = 50;

    private static final int MAX_FILE_ENTRIES = 100;

    /**
     * The time scale {@link #score} decays an entry's usage over: an entry visited once falls to roughly a
     * third of its peak score after this long unused, and to under 5% after three times as long - old enough
     * that a single stale visit isn't punished immediately, recent enough that "recent" stays meaningful.
     */
    private static final long SCORE_DECAY_SECONDS = Duration.ofDays(30).toSeconds();

    /**
     * Shows the URIs of the recorded entries as they are, since this storage knows no real storage to ask; the root
     * is shown by the name of this storage and can't be reached by typed-in text.
     */
    private final class RecentLocationConverter implements LocationConverter {

        @Override
        public String toLocation(URI uri) {
            if (uri.equals(ROOT_URI)) {
                return getName();
            }
            if (findEntry(uri.toString()) == null) {
                throw new NoSuchElementException(uri.toString());
            }
            return uri.toString();
        }

        @Override
        public URI fromLocation(String text) throws FormatException {
            throw new FormatException("Recent can't be navigated to by typed-in text");
        }
    }

    private final GreyWolfFile root;

    /**
     * The recorded entries by their URI, the most recently visited first. Guarded by its own intrinsic lock, not a
     * concurrent map - {@link #onOperation} can run concurrently on any thread performing a file operation
     * on any real storage, and {@link #trim} reads the entries and then removes one of them.
     */
    private final SequencedMap<String, RecentEntry> entriesByUri = new LinkedHashMap<>();

    private final StorageListenerSupport listenerSupport = new StorageListenerSupport();

    private final StorageIconProvider iconProvider = new StorageIconProvider() {
        @Override
        public FontIcon<?> getBaseIcon() {
            return RecentStorageIcons.STORAGE;
        }
    };

    private final LocationConverter locationConverter = new RecentLocationConverter();

    /**
     * Creates an empty storage; {@link #restore} fills it from the persisted file.
     */
    DefaultRecentStorage() {
        var rootFile = buildVirtual(FileEntryType.DIRECTORY, "Recent", ROOT_URI);
        rootFile.setRoot(true);
        this.root = rootFile;
    }

    @Override
    public FileStorageType getType() {
        return FileStorageType.VIRTUAL;
    }

    @Override
    public StorageCategory getCategory() {
        return StorageCategory.RECENT;
    }

    @Override
    public URI getUri() {
        return ROOT_URI;
    }

    @Override
    public String getName() {
        return "Recent";
    }

    @Override
    public StorageIconProvider getIconProvider() {
        return iconProvider;
    }

    @Override
    public GreyWolfFile getRoot() {
        return root;
    }

    @Override
    public List<GreyWolfFile> getDirectories(URI uri, OperationType operationType) throws NoSuchFileException {
        requireRoot(uri);
        listenerSupport.notifyListeners(uri, GreyWolfFileOperations.LIST, operationType);
        return new ArrayList<>();
    }

    @Override
    public List<GreyWolfFile> getFiles(URI uri, OperationType operationType) throws NoSuchFileException {
        requireRoot(uri);
        var result = snapshot().stream().map(this::buildEntryFile).collect(Collectors.toCollection(ArrayList::new));
        listenerSupport.notifyListeners(uri, GreyWolfFileOperations.LIST, operationType);
        return result;
    }

    @Override
    public List<GreyWolfFile> getFilesRecursively(URI uri, OperationType operationType)
            throws NoSuchFileException {
        // Recorded entries (files and directories alike) are terminal rows in this flat facade - none of them
        // expand into further children within the Recent listing itself, so a recursive listing is always
        // identical to the flat one; getDirectories() above returning empty is the other half of that
        // invariant.
        return getFiles(uri, operationType);
    }

    @Override
    public GreyWolfFile getFile(URI uri, OperationType operationType) throws NoSuchFileException, IOException {
        if (uri.equals(ROOT_URI)) {
            listenerSupport.notifyListeners(uri, GreyWolfFileOperations.LIST, operationType);
            return root;
        }
        var entry = findEntry(uri.toString());
        if (entry == null) {
            throw new NoSuchFileException(uri.toString());
        }
        listenerSupport.notifyListeners(uri, GreyWolfFileOperations.LIST, operationType);
        return buildEntryFile(entry);
    }

    @Override
    public @Nullable GreyWolfFile getParent(GreyWolfFile file, OperationType operationType) {
        return getParent(file.getUri(), operationType);
    }

    @Override
    public @Nullable GreyWolfFile getParent(URI uri, OperationType operationType) {
        listenerSupport.notifyListeners(uri, GreyWolfFileOperations.LIST, operationType);
        return uri.equals(ROOT_URI) ? null : root;
    }

    @Override
    public List<GreyWolfFile> getHierarchy(URI uri, OperationType operationType) throws NoSuchFileException,
            AccessDeniedException, IOException {
        return uri.equals(ROOT_URI) ? List.of(getFile(uri, operationType))
                : List.of(root, getFile(uri, operationType));
    }

    @Override
    public void createDirectory(URI uri, OperationType operationType) throws NoSuchFileException,
            FileAlreadyExistsException, AccessDeniedException, IOException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    @Override
    public GreyWolfFile createFile(URI uri, String name, OperationType operationType) throws NoSuchFileException,
            FileAlreadyExistsException, AccessDeniedException, IOException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    @Override
    public GreyWolfFile createVirtual(@Nullable URI uri, FileEntryType entryType, String name) {
        return buildVirtual(entryType, name, uri != null ? uri : getUri());
    }

    @Override
    public void renameFile(URI uri, String newName, OperationType operationType) throws NoSuchFileException,
            FileAlreadyExistsException, AccessDeniedException, IOException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    @Override
    public void move(URI sourceUri, URI targetUri, OperationType operationType) throws NoSuchFileException,
            FileAlreadyExistsException, AccessDeniedException, IOException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    @Override
    public boolean refersToStorage(URI uri) {
        return uri.equals(ROOT_URI);
    }

    @Override
    public void writeFile(URI uri, String content, Charset charset, OperationType operationType)
            throws AccessDeniedException, IOException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    @Override
    public String readFile(URI uri, Charset charset, OperationType operationType) throws NoSuchFileException,
            AccessDeniedException, IOException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    @Override
    public void writeFile(URI uri, byte[] content, OperationType operationType) throws AccessDeniedException,
            IOException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    @Override
    public byte[] readFile(URI uri, OperationType operationType) throws NoSuchFileException,
            AccessDeniedException, IOException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    @Override
    public byte[] readFileHeader(URI uri, int size, OperationType operationType) throws NoSuchFileException,
            AccessDeniedException, IOException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    @Override
    public InputStream openInputStream(URI uri, OperationType operationType) throws NoSuchFileException,
            AccessDeniedException, IOException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    @Override
    public OutputStream openOutputStream(URI uri, OperationType operationType) throws AccessDeniedException,
            IOException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    @Override
    public void delete(URI uri, OperationType operationType) throws NoSuchFileException, AccessDeniedException,
            IOException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    @Override
    public LocationConverter getLocationConverter() {
        return locationConverter;
    }

    @Override
    public void notifyOperation(URI uri, FileOperation operation, OperationType operationType) {
        listenerSupport.notifyListeners(uri, operation, operationType);
    }

    @Override
    public void onOperation(GreyWolfStorage storage, URI uri, FileOperation operation,
            OperationType operationType) {
        if (operation == GreyWolfFileOperations.DELETE) {
            remove(uri, operationType);
        } else if (operationType == OperationType.PRIMARY && !PersonalDirectories.isPersonalDirectory(uri)) {
            recordVisit(storage, uri);
        }
    }

    @Override
    public void clear(OperationType operationType) {
        List<String> removed;
        synchronized (entriesByUri) {
            removed = List.copyOf(entriesByUri.keySet());
            entriesByUri.clear();
        }
        removed.forEach(key -> listenerSupport.notifyListeners(URI.create(key), GreyWolfFileOperations.DELETE,
                operationType));
    }

    @Override
    public void remove(URI uri, OperationType operationType) {
        RecentEntry removed;
        synchronized (entriesByUri) {
            removed = entriesByUri.remove(uri.toString());
        }
        if (removed != null) {
            listenerSupport.notifyListeners(uri, GreyWolfFileOperations.DELETE, operationType);
        }
    }

    @Override
    public void addListener(StorageListener listener) {
        listenerSupport.addListener(listener);
    }

    @Override
    public void removeListener(StorageListener listener) {
        listenerSupport.removeListener(listener);
    }

    /**
     * Replaces the recorded entries with the ones read from the data file.
     *
     * @param file the existing data file to read
     * @throws Exception if the file cannot be read
     */
    void restore(Path file) throws Exception {
        var entries = new RecentEntriesReader().read(file);
        synchronized (entriesByUri) {
            entriesByUri.clear();
            for (var entry : entries) {
                entriesByUri.put(entry.uri(), entry);
            }
        }
    }

    /**
     * Writes the recorded entries, in listing order, to the data file.
     *
     * @param file the data file to write, whose directory exists
     * @throws Exception if the file cannot be written
     */
    void save(Path file) throws Exception {
        new RecentEntriesWriter().write(snapshot(), file);
    }

    /**
     * Returns a snapshot of every currently recorded entry.
     *
     * @return an immutable copy of the current entries, in listing order
     */
    private List<RecentEntry> snapshot() {
        synchronized (entriesByUri) {
            return List.copyOf(entriesByUri.values());
        }
    }

    /**
     * Records a visit to {@code uri}: refreshes the existing entry's usage stats and moves it to the front, or
     * inserts a brand-new one, then evicts the weakest entry of the same kind (directory vs. file) if that kind's
     * limit is now exceeded. A no-op if resolving the file fails or it is the root of its storage.
     */
    private void recordVisit(GreyWolfStorage storage, URI uri) {
        try {
            var file = storage.getFile(uri, OperationType.SECONDARY);
            if (file.isRoot()) {
                return;
            }
            var key = uri.toString();
            synchronized (entriesByUri) {
                var previous = entriesByUri.get(key);
                var previousCount = previous == null ? 0 : previous.visitCount();
                entriesByUri.putFirst(key, RecentEntry.builder()
                        .uri(key)
                        .name(file.getName())
                        .entryType(file.getEntryType())
                        .type(file.getType())
                        .storageCategory(storage.getCategory())
                        .storageType(storage.getType())
                        .storageUri(storage.getUri().toString())
                        .visitCount(previousCount + 1)
                        .lastUsedAt(Instant.now())
                        .build());
                trim(file.isDirectory());
            }
        } catch (IOException ex) {
            // The operation that triggered this notification already succeeded on the real file, so a failure
            // here is transient - skip recording rather than tracking an entry with no known name/type.
        }
    }

    /**
     * Evicts the lowest-{@link #score} directory or file entry, one at a time, while its kind's count still
     * exceeds {@link #MAX_DIRECTORY_ENTRIES}/{@link #MAX_FILE_ENTRIES} - called while holding
     * {@link #entriesByUri}' lock, right after inserting into it, so at most one entry is ever evicted per
     * call in practice.
     */
    private void trim(boolean directory) {
        var limit = directory ? MAX_DIRECTORY_ENTRIES : MAX_FILE_ENTRIES;
        while (countOfKind(directory) > limit) {
            entriesByUri.remove(weakestEntry(directory).uri());
        }
    }

    private int countOfKind(boolean directory) {
        var count = 0;
        for (var entry : entriesByUri.values()) {
            if ((entry.entryType() == FileEntryType.DIRECTORY) == directory) {
                count++;
            }
        }
        return count;
    }

    private RecentEntry weakestEntry(boolean directory) {
        RecentEntry weakest = null;
        var weakestScore = Double.MAX_VALUE;
        for (var entry : entriesByUri.values()) {
            if ((entry.entryType() == FileEntryType.DIRECTORY) != directory) {
                continue;
            }
            var score = score(entry);
            if (score < weakestScore) {
                weakestScore = score;
                weakest = entry;
            }
        }
        return Objects.requireNonNull(weakest);
    }

    /**
     * Scores {@code entry} for eviction purposes only - display order is always by {@link RecentEntry#lastUsedAt()}
     * (the list's own order already reflects it) - combining how often it's been visited with how long ago it
     * was last visited, so a frequently used entry resists eviction for a while even if not the most recent,
     * but nothing stays protected forever once it decays past {@link #SCORE_DECAY_SECONDS}.
     */
    private double score(RecentEntry entry) {
        var ageSeconds = Duration.between(entry.lastUsedAt(), Instant.now()).toSeconds();
        return Math.log(1 + entry.visitCount()) * Math.exp(-ageSeconds / (double) SCORE_DECAY_SECONDS);
    }

    /**
     * Finds the recorded entry for {@code uri}, or {@code null} if this facade isn't tracking it.
     */
    private @Nullable RecentEntry findEntry(String uri) {
        synchronized (entriesByUri) {
            return entriesByUri.get(uri);
        }
    }

    /**
     * Builds a virtual (no real backing) entry owned by this facade, for its own root directory and
     * {@link #createVirtual} - since a virtual entry has no content to resolve a more specific type from, its
     * type (and, from it, its icon) is picked directly from {@code entryType}.
     */
    private DefaultGreyWolfFile buildVirtual(FileEntryType entryType, String name, URI uri) {
        var file = new DefaultGreyWolfFile();
        file.setStorage(this);
        file.setEntryType(entryType);
        file.setName(name);
        file.setUri(uri);
        file.setVirtual(true);
        return file;
    }

    /**
     * Builds a file for {@code entry} from what was recorded about it: its name, type and URI. This storage knows no
     * other storage, so the file provider finds the real file and lists it instead, or lists this one if the storage
     * of the file can't be reached.
     */
    private GreyWolfFile buildEntryFile(RecentEntry entry) {
        var file = new DefaultGreyWolfFile();
        file.setStorage(this);
        file.setEntryType(entry.entryType());
        file.setName(entry.name());
        file.setUri(URI.create(entry.uri()));
        file.setType(entry.type());
        file.setVirtual(true);
        return file;
    }

    private void requireRoot(URI uri) throws NoSuchFileException {
        if (!uri.equals(ROOT_URI)) {
            throw new NoSuchFileException(uri.toString());
        }
    }
}
