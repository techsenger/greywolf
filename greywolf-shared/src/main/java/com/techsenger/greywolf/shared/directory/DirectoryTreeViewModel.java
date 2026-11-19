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

package com.techsenger.greywolf.shared.directory;

import com.google.errorprone.annotations.concurrent.LazyInit;
import com.techsenger.annotations.Nullable;
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.greywolf.core.GreyWolfSettings;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.base.GreyWolfFileProvider;
import com.techsenger.greywolf.storage.base.GreyWolfFileRefiner;
import com.techsenger.greywolf.storage.base.GreyWolfStorage;
import com.techsenger.greywolf.storage.base.GreyWolfStorageProvider;
import com.techsenger.greywolf.storage.base.StorageCategory;
import com.techsenger.greywolf.storage.ui.view.FileViewModel;
import com.techsenger.patternfx.mvvm.ChildComposer;
import com.techsenger.shellfx.base.registry.ItemRegistry;
import com.techsenger.shellfx.base.registry.ItemRegistryListener;
import com.techsenger.shellfx.core.area.AbstractAreaViewModel;
import com.techsenger.shellfx.core.settings.SettingsListener;
import com.techsenger.shellfx.material.RequestSetter;
import com.techsenger.shellfx.material.UiExecutor;
import com.techsenger.shellfx.material.theme.Theme;
import com.techsenger.shellfx.storage.base.OperationType;
import com.techsenger.shellfx.storage.base.async.AsyncResult;
import com.techsenger.shellfx.storage.base.async.AsyncStatus;
import com.techsenger.shellfx.storage.base.async.FileRefinement;
import com.techsenger.shellfx.storage.base.async.LinkUtils;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public class DirectoryTreeViewModel<C extends ChildComposer> extends AbstractAreaViewModel<C>
        implements DirectoryTreePort, FileViewModel<C> {

    private static final Logger logger = LoggerFactory.getLogger(DirectoryTreeViewModel.class);

    /**
     * Waits in the calling thread, which is a background one, for an asynchronous operation of the provider or the
     * refiner, and gives its failure as it is if it is an I/O one.
     */
    private static <T> T await(CompletableFuture<T> future) throws IOException {
        try {
            return future.get();
        } catch (ExecutionException ex) {
            var cause = ex.getCause();
            throw cause instanceof IOException ioException ? ioException : new IOException(cause);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException(ex);
        }
    }

    /**
     * Makes the end callback of an operation of the provider or the refiner end the future: with its exception if the
     * operation failed, and otherwise with nothing, which changes nothing for a future that has the data already.
     */
    private static Consumer<AsyncResult> finishing(CompletableFuture<?> future) {
        return result -> {
            if (result.getStatus() == AsyncStatus.FAILED) {
                future.completeExceptionally(Objects.requireNonNull(result.getException()));
            } else {
                future.complete(null);
            }
        };
    }

    private @Nullable DirectoryTreeParams params;

    @LazyInit
    private GreyWolfStorageProvider storageProvider;

    @LazyInit
    private GreyWolfFileProvider fileProvider;

    @LazyInit
    private GreyWolfFileRefiner fileRefiner;

    private @Nullable ItemRegistry<GreyWolfStorage> storageRegistry;

    private final ItemRegistryListener<GreyWolfStorage> storageChangesListener =
            (added, removed) -> UiExecutor.execute(this::updateStorages);

    private final GreyWolfSettings settings;

    private final SettingsListener settingsListener = this::applySettings;

    private final ReadOnlyObjectWrapper<Theme> theme = new ReadOnlyObjectWrapper<>();

    /**
     * The most recently requested directory - not necessarily a real, resolvable one (it arrives from
     * outside via {@link #setDirectory}, before we've even tried to locate it in the tree), so it's
     * used only to detect whether an in-flight {@link #expandTo} call has been superseded by a newer
     * request, never as the tree's actual current state. See {@link #directory} for that.
     */
    @LazyInit
    private volatile GreyWolfFile requestedDirectory;

    /**
     * The directory the tree has navigated to right now - set when the user opens a node, and when
     * {@link #expandTo} genuinely resolves a requested one, after which the View selects it. Unlike
     * {@link #requestedDirectory}, which may reference a directory the tree never manages to show.
     */
    private final ReadOnlyObjectWrapper<@Nullable GreyWolfFile> directory = new ReadOnlyObjectWrapper<>();

    private final ObservableSource<GreyWolfFile> directorySource = new SimpleObservableSource<>();

    private final ReadOnlyObjectWrapper<@Nullable GreyWolfFile> file = new ReadOnlyObjectWrapper<>();

    private final ObservableList<GreyWolfStorage> modifiableStorages = FXCollections.observableArrayList();

    private final ObservableList<GreyWolfStorage> storages =
            FXCollections.unmodifiableObservableList(modifiableStorages);

    private final ObservableMap<GreyWolfFile, @Unmodifiable List<GreyWolfFile>> modifiableChildrenByDirectory =
            FXCollections.observableHashMap();

    private final ObservableMap<GreyWolfFile, @Unmodifiable List<GreyWolfFile>> childrenByDirectory =
            FXCollections.unmodifiableObservableMap(modifiableChildrenByDirectory);

    private final ObservableSource<List<GreyWolfFile>> expandPathSource = new SimpleObservableSource<>();

    /**
     * Tracks directories already fetched and pushed to the view, so tabs sharing this tree don't re-fetch
     * loaded nodes or lose expanded subtree state when switching. Concurrent, since expansions can run on
     * different virtual threads simultaneously.
     */
    private final Set<GreyWolfFile> loadedDirectories = ConcurrentHashMap.newKeySet();

    private boolean loadingTabTree = false;

    public DirectoryTreeViewModel(DirectoryTreeParams params) {
        super(params);
        this.params = params;
        this.settings = params.getSettings();
    }

    @Override
    @RequestSetter
    public void setDirectory(GreyWolfFile directory) {
        if (Objects.equals(this.requestedDirectory, directory)) {
            logger.debug("{} Skipped setDirectory: already current", getDescriptor().getLogPrefix());
            return;
        }
        var old = this.requestedDirectory;
        this.requestedDirectory = directory;
        logger.debug("{} Processing setDirectory; old directory: {}, new directory: {}",
                getDescriptor().getLogPrefix(), old, directory);
        expandTo(directory);
    }

    @Override
    public @Nullable GreyWolfFile getDirectory() {
        return directory.get();
    }

    @Override
    public ReadOnlyObjectProperty<@Nullable GreyWolfFile> directoryProperty() {
        return directory.getReadOnlyProperty();
    }

    @Override
    public @Nullable GreyWolfFile getFile() {
        return file.get();
    }

    @Override
    public ReadOnlyObjectProperty<@Nullable GreyWolfFile> fileProperty() {
        return file.getReadOnlyProperty();
    }

    @Override
    public @Unmodifiable ObservableList<GreyWolfStorage> getStorages() {
        return storages;
    }

    @Override
    public @Unmodifiable ObservableMap<GreyWolfFile, @Unmodifiable List<GreyWolfFile>> getChildrenByDirectory() {
        return childrenByDirectory;
    }

    @Override
    public void onDirectoryRefresh(GreyWolfFile dir) {
        var sizeBefore = loadedDirectories.size();
        loadedDirectories.remove(dir);
        removeLoadedDescendants(dir);
        Thread.startVirtualThread(() -> {
            try {
                var children = getDirectories(dir);
                loadedDirectories.add(dir);
                UiExecutor.execute(() -> modifiableChildrenByDirectory.put(dir, children));
                var sizeAfter = loadedDirectories.size();
                logger.debug("{} Refreshed {}: loaded directories count before={}, after={}",
                        getDescriptor().getLogPrefix(), dir.getUri(), sizeBefore, sizeAfter);
            } catch (IOException ex) {
                logger.error("{} Failed to refresh {}", getDescriptor().getLogPrefix(), dir.getUri(), ex);
            }
        });
    }

    @Override
    protected void preInitialize() {
        super.preInitialize();
        applySettings();
        directory.addListener((ov, oldV, newV) -> {
            if (loadingTabTree || newV == null) {
                return;
            }
            this.requestedDirectory = newV;
        });
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        var params = Objects.requireNonNull(this.params);
        this.storageProvider = params.getStorageProvider();
        this.fileProvider = new GreyWolfFileProvider(storageProvider);
        this.fileRefiner = new GreyWolfFileRefiner(storageProvider);
        setStorages(storageProvider.getStorages());
        this.storageRegistry = params.getStorageRegistry();
        this.storageRegistry.addListener(storageChangesListener);
        this.settings.addListener(settingsListener);
        this.params = null;
    }

    @Override
    protected void preDeinitialize() {
        super.preDeinitialize();
        if (storageRegistry != null) {
            storageRegistry.removeListener(storageChangesListener);
        }
        settings.removeListener(settingsListener);
    }

    protected Theme getTheme() {
        return theme.get();
    }

    protected ReadOnlyObjectProperty<Theme> themeProperty() {
        return theme.getReadOnlyProperty();
    }

    protected ObservableList<GreyWolfStorage> getModifiableStorages() {
        return modifiableStorages;
    }

    protected void setStorages(List<? extends GreyWolfStorage> storages) {
        this.modifiableStorages.setAll(storages);
    }

    /**
     * Called by the view when the user opens a node, with a click or the Enter key; the node becomes the directory.
     * Selecting a node without opening it, for example with the right button, does not call it.
     *
     * @param dir the folder of the node
     */
    protected void onNodeOpened(GreyWolfFile dir) {
        directory.set(dir);
    }

    /**
     * Called by the view when the context menu of a node is requested, so the handlers of its items know the folder.
     *
     * @param node the folder of the node the menu is requested for
     */
    protected void onContextRequested(GreyWolfFile node) {
        file.set(node);
    }

    protected void onNodeExpanded(GreyWolfFile dir) {
        if (loadingTabTree || !loadedDirectories.add(dir)) {
            return;
        }
        Thread.startVirtualThread(() -> {
            try {
                var children = getDirectories(dir);
                UiExecutor.execute(() -> modifiableChildrenByDirectory.put(dir, children));
            } catch (IOException ex) {
                logger.error("{} Failed to load directories for {}", getDescriptor().getLogPrefix(), dir.getUri(),
                        ex);
            }
        });
    }

    protected void expandTo(GreyWolfFile dir) {
        Thread.startVirtualThread(() -> {
            List<GreyWolfFile> path;
            try {
                var hierarchy = new CompletableFuture<List<GreyWolfFile>>();
                fileProvider.provideHierarchyAsync(dir.getStorage(), dir.getUri(), OperationType.SECONDARY,
                        hierarchy::complete, finishing(hierarchy));
                path = await(hierarchy);
            } catch (IOException ex) {
                logger.error("{} Failed to build ancestor path for {}", getDescriptor().getLogPrefix(),
                        dir.getUri(), ex);
                return;
            }
            List<@Nullable List<GreyWolfFile>> directories = new ArrayList<>();
            // Ancestors newly marked as loaded by this call, so a discarded (stale) result can roll them back
            // instead of leaving loadedDirectories claiming a directory whose children never reached the view.
            var addedByThisCall = new ArrayList<GreyWolfFile>();
            for (var ancestor : path) {
                if (loadedDirectories.add(ancestor)) {
                    addedByThisCall.add(ancestor);
                    try {
                        var dirs = getDirectories(ancestor);
                        directories.add(dirs);
                    } catch (IOException ex) {
                        loadedDirectories.remove(ancestor);
                        addedByThisCall.remove(ancestor);
                        logger.error("{} Failed to load directories for {}", getDescriptor().getLogPrefix(),
                                ancestor.getUri(), ex);
                        break; // can't reach deeper levels if this one failed
                    }
                } else {
                    directories.add(null);
                }
            }
            UiExecutor.execute(() -> {
                if (!Objects.equals(this.requestedDirectory, dir)) {
                    loadedDirectories.removeAll(addedByThisCall);
                    logger.debug("{} Discarded stale expandTo result for {}: current directory is now {}; rolled "
                                    + "back {} loaded-directory entries", getDescriptor().getLogPrefix(),
                            dir.getUri(), this.requestedDirectory, addedByThisCall.size());
                    return;
                }
                loadingTabTree = true;
                var prefix = new ArrayList<GreyWolfFile>();
                for (var i = 0; i < directories.size(); i++) {
                    var ancestor = path.get(i);
                    prefix.add(ancestor);
                    var dirs = directories.get(i);
                    if (dirs != null) {
                        modifiableChildrenByDirectory.put(ancestor, dirs);
                    }
                    expandPathSource.next(prefix);
                }
                // Selected once, after the whole path is built, so the row index scrollToIfNeeded computes is
                // against the final tree state instead of shifting on every ancestor as the path is built up.
                directory.set(dir);
                directorySource.next(dir);
                loadingTabTree = false;
            });
        });
    }

    ObservableSource<GreyWolfFile> directorySource() {
        return directorySource;
    }

    ObservableSource<List<GreyWolfFile>> expandPathSource() {
        return expandPathSource;
    }

    /**
     * Returns the subdirectories of the directory, with the links to directories among them, sorted by name together.
     * A link is entered through its target. The directory is listed by the file provider and the links that it leaves
     * to the second phase are refined by the file refiner, so the node is shown when its links are known. The recent
     * storage has none, since the files it lists are shown in the branches of their own storages.
     */
    private @Unmodifiable List<GreyWolfFile> getDirectories(GreyWolfFile dir) throws IOException {
        if (dir.getStorage().getCategory() == StorageCategory.RECENT) {
            return List.of();
        }
        var link = dir.getLink();
        var target = link == null ? null : link.getTarget();
        var listed = dir.isDirectoryLink() && target != null ? target : dir;
        var listing = new CompletableFuture<List<GreyWolfFile>>();
        fileProvider.provideFilesAsync(listed.getStorage(), listed.getUri(), OperationType.SECONDARY, listing::complete,
                finishing(listing));
        var files = await(listing);
        var unresolved = files.stream().filter(LinkUtils::isUnresolved).toList();
        if (!unresolved.isEmpty()) {
            var refinements = new ConcurrentLinkedQueue<FileRefinement<GreyWolfFile>>();
            var refining = new CompletableFuture<Void>();
            fileRefiner.refineFilesAsync(unresolved, refinements::add, finishing(refining));
            await(refining);
            // the files are not shown yet, so they are written here, in the background thread
            refinements.forEach(FileRefinement::apply);
        }
        var directories = new ArrayList<>(files);
        directories.removeIf(file -> !file.isDirectory() && !file.isDirectoryLink());
        directories.sort(Comparator.comparing(GreyWolfFile::getName, String.CASE_INSENSITIVE_ORDER));
        return Collections.unmodifiableList(directories);
    }

    /**
     * Brings the shown storages up to date with the provider: a storage that is gone is removed together with what was
     * loaded under it, and a new one is added at its place, so what the user has opened stays as it is.
     */
    private void updateStorages() {
        var current = storageProvider.getStorages();
        var gone = modifiableStorages.stream().filter(storage -> !current.contains(storage)).toList();
        gone.forEach(storage -> removeLoadedDescendants(storage.getRoot()));
        modifiableStorages.removeAll(gone);
        for (var i = 0; i < current.size(); i++) {
            if (!modifiableStorages.contains(current.get(i))) {
                modifiableStorages.add(i, current.get(i));
            }
        }
    }

    /**
     * Removes every entry from the loaded-directories set and the children map whose URI is nested under
     * {@code dir}'s URI, so a refreshed node's descendants re-fetch instead of reusing stale state, and a
     * later reload of unchanged content still fires a change instead of being silently dropped as a no-op
     * equal {@code put}.
     */
    private void removeLoadedDescendants(GreyWolfFile dir) {
        var prefix = dir.getUri().toString();
        loadedDirectories.removeIf(node -> node.getUri().toString().startsWith(prefix));
        modifiableChildrenByDirectory.keySet().removeIf(node -> node.getUri().toString().startsWith(prefix));
    }

    private void applySettings() {
        theme.set(settings.getAppearance().getTheme());
    }
}
