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

package com.techsenger.greywolf.system.navigator.area;

import com.techsenger.annotations.Nullable;
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.greywolf.core.GreyWolfSettings;
import com.techsenger.greywolf.core.clipboard.ClipboardFiles;
import com.techsenger.greywolf.core.clipboard.ClipboardOperation;
import com.techsenger.greywolf.core.clipboard.GreyWolfClipboard;
import com.techsenger.greywolf.core.panels.PanelTabDockPort;
import com.techsenger.greywolf.core.panels.Placement;
import com.techsenger.greywolf.core.registries.FileStorageRegistry;
import com.techsenger.greywolf.system.navigator.NavigatorTabConfig;
import com.techsenger.greywolf.system.navigator.NavigatorTabParams;
import com.techsenger.greywolf.system.navigator.NavigatorTabPort;
import com.techsenger.greywolf.system.navigator.mark.MarkCriteria;
import com.techsenger.greywolf.system.navigator.mark.MarkDialogButtons;
import com.techsenger.greywolf.system.navigator.mark.MarkDialogConfig;
import com.techsenger.greywolf.system.navigator.mark.MarkDialogParams;
import com.techsenger.greywolf.system.navigator.mark.MarkOperationType;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.base.GreyWolfFileOperations;
import com.techsenger.greywolf.storage.base.GreyWolfFileProvider;
import com.techsenger.greywolf.storage.base.GreyWolfFileRefiner;
import com.techsenger.greywolf.storage.base.GreyWolfStorage;
import com.techsenger.greywolf.storage.base.DefaultGreyWolfFile;
import com.techsenger.greywolf.storage.base.StorageCategory;
import com.techsenger.greywolf.storage.ui.name.NameDialogConfig;
import com.techsenger.greywolf.storage.ui.name.NameDialogParams;
import com.techsenger.greywolf.storage.ui.name.NameDialogType;
import com.techsenger.greywolf.storage.ui.properties.FilePropertiesDialogButtons;
import com.techsenger.greywolf.storage.ui.properties.FilePropertiesDialogConfig;
import com.techsenger.greywolf.storage.ui.properties.FilePropertiesDialogParams;
import com.techsenger.greywolf.storage.ui.transfer.SourceDialogConfig;
import com.techsenger.greywolf.storage.ui.transfer.SourceDialogParams;
import com.techsenger.greywolf.storage.ui.transfer.TargetDialogConfig;
import com.techsenger.greywolf.storage.ui.transfer.TargetDialogParams;
import com.techsenger.greywolf.storage.ui.transfer.TransferOperationType;
import com.techsenger.greywolf.storage.ui.view.FileViewModel;
import com.techsenger.shellfx.core.area.AbstractAreaViewModel;
import com.techsenger.shellfx.core.config.ConfigListener;
import com.techsenger.shellfx.core.config.ConfigManager;
import com.techsenger.shellfx.core.settings.SettingsListener;
import com.techsenger.shellfx.core.window.WindowType;
import com.techsenger.shellfx.dialogs.alert.AlertDialogParams;
import com.techsenger.shellfx.dialogs.alert.AlertDialogType;
import com.techsenger.shellfx.dialogs.text.TextDialogButtons;
import com.techsenger.shellfx.dialogs.text.TextsDialogPort;
import com.techsenger.shellfx.material.RequestSetter;
import com.techsenger.shellfx.material.UiExecutor;
import com.techsenger.shellfx.material.table.TableColumnInfo;
import com.techsenger.shellfx.material.table.TableColumnInfoAdapter;
import com.techsenger.shellfx.material.table.TableColumnName;
import com.techsenger.shellfx.material.table.TableConfig;
import com.techsenger.shellfx.material.theme.Theme;
import com.techsenger.shellfx.storage.base.OperationType;
import com.techsenger.shellfx.storage.base.UriUtils;
import com.techsenger.shellfx.storage.base.async.AsyncStatus;
import com.techsenger.shellfx.storage.base.async.AsyncTask;
import com.techsenger.shellfx.storage.base.async.FileProvider;
import com.techsenger.shellfx.storage.base.async.FileRefinement;
import com.techsenger.shellfx.storage.base.async.FileRefiner;
import com.techsenger.shellfx.storage.ui.StorageErrors;
import com.techsenger.shellfx.storage.ui.style.FileDecorationType;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import java.io.IOException;
import java.net.URI;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public final class NavigatorAreaViewModel extends AbstractAreaViewModel<NavigatorAreaComposer>
        implements NavigatorAreaPort, FileViewModel<NavigatorAreaComposer> {

    @FunctionalInterface
    private interface EntryCreator {

        GreyWolfFile create(GreyWolfStorage storage, URI uri, String name) throws IOException;
    }

    private static final Logger logger = LoggerFactory.getLogger(NavigatorAreaViewModel.class);

    /**
     * Caps how many directories {@link #historyUris} keeps: the oldest entry is dropped once a new one would
     * exceed it, the same way a manager's history eventually forgets its oldest pages.
     */
    private static final int MAX_HISTORY_SIZE = 100;

    /**
     * Passed to the config listeners when the state of a panel changed, so the other areas
     * of that panel follow it.
     */
    private static final Object PANEL_STATE_CHANGED_HINT = new Object();

    private static Map<TableColumnName, TableColumnInfoAdapter> createColumnInfos(TableConfig config) {
        var columnInfos = new HashMap<TableColumnName, TableColumnInfoAdapter>();
        for (var info : config.getColumns()) {
            var adapter = new TableColumnInfoAdapter(new TableColumnInfo(info));
            columnInfos.put(adapter.getName(), adapter);
        }
        return columnInfos;
    }

    /**
     * Returns {@code mode}'s column count, or {@code -1} if {@code mode} is details, which has none.
     */
    private static int getColumnCount(ViewMode mode) {
        if (mode instanceof ListViewMode listMode) {
            return listMode.getColumnCount();
        } else if (mode instanceof TilesViewMode tilesMode) {
            return tilesMode.getColumnCount();
        }
        return -1;
    }

    /**
     * Determines which entry in the newly listed {@code files} should be selected, so it can be scrolled into
     * view: {@code previousDirectory} itself if it is still present (matched by equality, not reference), or
     * the synthetic parent (&quot;..&quot;) entry as a fallback. The fallback also applies whenever
     * {@code previousDirectory} isn't found in {@code files} at all, e.g. jumping to an unrelated directory via
     * the location bar/directory tree, or landing on a directory that turns out to be empty.
     *
     * @return the entry to select, or {@code null} if neither {@code previousDirectory} nor a parent entry is
     *     available.
     */
    private static @Nullable GreyWolfFile findFileToSelect(List<GreyWolfFile> files,
            @Nullable GreyWolfFile previousDirectory) {
        var found = findFile(files, previousDirectory);
        if (found != null) {
            return found;
        }
        return files.stream()
                .filter(GreyWolfFile::isParentEntry)
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns the entry of {@code files} that is equal to {@code file}, or {@code null} if there is none.
     */
    private static @Nullable GreyWolfFile findFile(List<GreyWolfFile> files,
            @Nullable GreyWolfFile file) {
        if (file == null) {
            return null;
        }
        for (var candidate : files) {
            if (candidate.equals(file)) {
                return candidate;
            }
        }
        return null;
    }

    private final NavigatorTabPort tab;

    private final FileStorageRegistry storageRegistry;

    private final GreyWolfSettings settings;

    private final SettingsListener settingsListener = this::applySettings;

    private @Nullable Theme styledTheme;

    private final ConfigManager configManager;

    private final GreyWolfClipboard clipboard;

    //VIEW MODE //////////////////////////////////////////////

    private final Map<TableColumnName, TableColumnInfoAdapter> columnInfos;

    private final ObjectProperty<ViewModeType> viewModeType = new SimpleObjectProperty<>(ViewModeType.DETAILS);

    /**
     * Holds a placeholder until the view modes of the config are taken, which happens after the view is built.
     */
    private final ReadOnlyObjectWrapper<ViewMode> viewMode = new ReadOnlyObjectWrapper<>(new DetailsViewMode());

    private final ReadOnlyIntegerWrapper columnCount = new ReadOnlyIntegerWrapper();

    /**
     * Set while the state is being taken from the config, so that taking it is not mistaken for a change made
     * by the user and written back.
     */
    private boolean loadingState;

    private final ConfigListener configListener = this::onConfigChanged;

    private boolean notifyingConfigListeners;

    private final ReadOnlyObjectWrapper<Comparator<GreyWolfFile>> fileComparator =
            new ReadOnlyObjectWrapper<>(Comparators.directoryFirst(null));

    private final ObservableSource<Void> updateViewModeSource = new SimpleObservableSource<>();

    // DIRECTORY/FILES ////////////////////////////////////////

    private final ReadOnlyObjectWrapper<GreyWolfFile> directory = new ReadOnlyObjectWrapper<>();

    private final ObservableList<GreyWolfFile> modifiableFiles = FXCollections.observableArrayList();

    private final ObservableList<GreyWolfFile> files =
            FXCollections.unmodifiableObservableList(modifiableFiles);

    private final ReadOnlyObjectWrapper<@Nullable GreyWolfFile> file = new ReadOnlyObjectWrapper<>();

    private final ObservableSource<@Nullable GreyWolfFile> fileSource = new SimpleObservableSource<>();

    private final ReadOnlyObjectWrapper<ContextTarget> contextTarget = new ReadOnlyObjectWrapper<>(ContextTarget.NONE);

    private final ReadOnlyIntegerWrapper fileIndex = new ReadOnlyIntegerWrapper(-1);

    private final ObservableSource<Integer> fileIndexSource = new SimpleObservableSource<>();

    private final ReadOnlyBooleanWrapper upAvailable = new ReadOnlyBooleanWrapper();

    // FILE PROVISION, REFINEMENT, DECORATION ///////////////////

    private final ReadOnlyBooleanWrapper waiting = new ReadOnlyBooleanWrapper();

    private final FileProvider<GreyWolfFile> fileProvider;

    private final FileRefiner<GreyWolfFile> fileRefiner;

    /**
     * The operations of the provider that are running for the directory this area is opening or has opened, which a
     * new navigation and closing the area cancel.
     */
    private final List<AsyncTask> tasks = new CopyOnWriteArrayList<>();

    /**
     * The files that are shown provisionally, as the second phase is still to refine them. The files are compared by
     * identity, as the refinement of a file can change what it is equal to.
     */
    private final Set<GreyWolfFile> pending =
            Collections.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));

    private final Queue<FileRefinement<GreyWolfFile>> refinements = new ConcurrentLinkedQueue<>();

    private final AtomicBoolean refinementsScheduled = new AtomicBoolean();

    /**
     * Tells the view that the icon and the style of the files are outdated, which happens when the second loading
     * phase refines them. The view decorates the files again from scratch.
     */
    private final ObservableSource<List<GreyWolfFile>> decorateSource = new SimpleObservableSource<>();

    /**
     * Tells the view that only the style of one file is outdated, which marking a single file changes. The view
     * resolves the style of the file at once, without walking the files it shows.
     */
    private final ObservableSource<GreyWolfFile> restyleFileSource = new SimpleObservableSource<>();

    /**
     * Tells the view that only the style of the files is outdated, as a theme or a mark changes nothing else. The
     * view keeps their icons and resolves just the styles again.
     */
    private final ObservableSource<List<GreyWolfFile>> restyleFilesSource = new SimpleObservableSource<>();

    // INFO & STATISTICS ////////////////////////////////////////

    private final ReadOnlyStringWrapper fileInfo = new ReadOnlyStringWrapper();

    private final ReadOnlyIntegerWrapper folderCount = new ReadOnlyIntegerWrapper(-1);

    private final ReadOnlyIntegerWrapper fileCount = new ReadOnlyIntegerWrapper(-1);

    private final ReadOnlyIntegerWrapper markedCount = new ReadOnlyIntegerWrapper(-1);

    // HISTORY //////////////////////////////////////////////////

    /**
     * URIs of the directories visited by this area, oldest first; resolved back into a directory entry on
     * demand via {@link #storageRegistry} rather than kept as live file references, since the storage a URI
     * belongs to may be unregistered (e.g. a custom/remote storage disconnecting) between the visit and a
     * later Back/Forward.
     */
    private final ObservableList<URI> modifiableHistoryUris = FXCollections.observableArrayList();

    private final ObservableList<URI> historyUris = FXCollections.unmodifiableObservableList(modifiableHistoryUris);

    /**
     * Position of the currently shown directory within {@link #historyUris}, or {@code -1} before this area
     * has navigated anywhere.
     */
    private final IntegerProperty historyIndex = new SimpleIntegerProperty(-1);

    private final ReadOnlyBooleanWrapper backAvailable = new ReadOnlyBooleanWrapper();

    private final ReadOnlyBooleanWrapper forwardAvailable = new ReadOnlyBooleanWrapper();

    /**
     * Set while this area itself moves {@link #historyIndex}, so that the move is not mistaken for a request of a
     * client to navigate to that entry.
     */
    private boolean updatingHistoryIndex;

    // OTHERS ///////////////////////////////////////////////////

    private final ObjectProperty<@Nullable Placement> placement = new SimpleObjectProperty<>();

    private @Nullable NavigatorAreaParams params;

    private int markRangeAnchorIndex = -1;

    /**
     * Bumped by every {@link #navigateAsync} call and captured by its background read; a commit whose captured
     * value no longer matches this field was superseded by a newer navigation started before it finished, and
     * is discarded instead of committing over a directory this area has since moved on from.
     */
    private int navigationRequestId;

    public NavigatorAreaViewModel(NavigatorAreaParams params) {
        super(params);
        this.tab = params.getTab();
        var context = tab.getComposerAccess().getShellPort().getContextAccess();
        storageRegistry = context.getFileStorageRegistry();
        settings = context.getSettings();
        fileProvider = new GreyWolfFileProvider(storageRegistry::getAllStorages);
        fileRefiner = new GreyWolfFileRefiner(storageRegistry::getAllStorages);
        this.configManager = context.getConfigManager();
        clipboard = context.getClipboard();
        this.params = params;
        this.columnInfos = createColumnInfos(TableConfigUtils.createDefault());
    }

    @Override
    public GreyWolfFile getDirectory() {
        return directory.get();
    }

    @Override
    @RequestSetter
    public void setDirectory(GreyWolfFile dir) {
        Objects.requireNonNull(dir, "Directory can't be null");
        navigateTo(dir);
    }

    @Override
    public ReadOnlyObjectProperty<GreyWolfFile> directoryProperty() {
        return directory.getReadOnlyProperty();
    }

    @Override
    public @Unmodifiable ObservableList<GreyWolfFile> getFiles() {
        return files;
    }

    @Override
    public @Nullable GreyWolfFile getFile() {
        return file.get();
    }

    @Override
    @RequestSetter
    public void setFile(@Nullable GreyWolfFile file) {
        fileSource.next(file);
    }

    @Override
    public ReadOnlyObjectProperty<@Nullable GreyWolfFile> fileProperty() {
        return file.getReadOnlyProperty();
    }

    @Override
    public ContextTarget getContextTarget() {
        return contextTarget.get();
    }

    @Override
    public ReadOnlyObjectProperty<ContextTarget> contextTargetProperty() {
        return contextTarget.getReadOnlyProperty();
    }

    @Override
    public int getFileIndex() {
        return fileIndex.get();
    }

    @Override
    @RequestSetter
    public void setFileIndex(int index) {
        fileIndexSource.next(index);
    }

    @Override
    public ReadOnlyIntegerProperty fileIndexProperty() {
        return fileIndex.getReadOnlyProperty();
    }

    @Override
    public @Nullable Placement getPlacement() {
        return placement.get();
    }

    @Override
    public void setPlacement(@Nullable Placement placement) {
        this.placement.set(placement);
    }

    @Override
    public ObjectProperty<@Nullable Placement> placementProperty() {
        return placement;
    }

    @Override
    public ViewModeType getViewModeType() {
        return viewModeType.get();
    }

    @Override
    public void setViewModeType(ViewModeType type) {
        viewModeType.set(type);
    }

    @Override
    public ObjectProperty<ViewModeType> viewModeTypeProperty() {
        return viewModeType;
    }

    @Override
    public ViewMode getViewMode() {
        return viewMode.get();
    }

    @Override
    public ReadOnlyObjectProperty<ViewMode> viewModeProperty() {
        return viewMode.getReadOnlyProperty();
    }

    @Override
    public void onColumnCountChanged(int count) {
        var mode = viewMode.get();
        if (getColumnCount(mode) < 0 || getColumnCount(mode) == count) {
            return;
        }
        if (mode instanceof ListViewMode listMode) {
            listMode.setColumnCount(count);
        } else if (mode instanceof TilesViewMode tilesMode) {
            tilesMode.setColumnCount(count);
        }
        updateViewModeSource.next(null);
        applyViewModeEffects();
        publishChange();
    }

    @Override
    public @Unmodifiable Map<TableColumnName, TableColumnInfoAdapter> getColumnInfos() {
        return Collections.unmodifiableMap(columnInfos);
    }

    @Override
    public void onAddColumn(TableColumnName name) {
        var info = Objects.requireNonNull(columnInfos.get(name), "No info for column " + name);
        if (info.isVisible()) {
            return;
        }
        info.setIndex(visibleColumnCount());
        updateViewModeSource.next(null);
        saveColumns();
    }

    @Override
    public void onRemoveColumn(TableColumnName name) {
        var info = Objects.requireNonNull(columnInfos.get(name), "No info for column " + name);
        if (!info.isVisible()) {
            return;
        }
        info.setIndex(-1);
        updateViewModeSource.next(null);
        saveColumns();
    }

    @Override
    public int getFolderCount() {
        return folderCount.get();
    }

    @Override
    public ReadOnlyIntegerProperty folderCountProperty() {
        return folderCount.getReadOnlyProperty();
    }

    @Override
    public int getFileCount() {
        return fileCount.get();
    }

    @Override
    public ReadOnlyIntegerProperty fileCountProperty() {
        return fileCount.getReadOnlyProperty();
    }

    @Override
    public int getMarkedCount() {
        return markedCount.get();
    }

    @Override
    public ReadOnlyIntegerProperty markedCountProperty() {
        return markedCount.getReadOnlyProperty();
    }

    @Override
    public @Nullable String getFileInfo() {
        return fileInfo.get();
    }

    @Override
    public ReadOnlyStringProperty fileInfoProperty() {
        return fileInfo.getReadOnlyProperty();
    }

    @Override
    public Comparator<GreyWolfFile> getFileComparator() {
        return fileComparator.get();
    }

    @Override
    public ReadOnlyObjectProperty<Comparator<GreyWolfFile>> fileComparatorProperty() {
        return fileComparator.getReadOnlyProperty();
    }

    @Override
    public @Unmodifiable ObservableList<URI> getHistoryUris() {
        return historyUris;
    }

    @Override
    public int getHistoryIndex() {
        return historyIndex.get();
    }

    @Override
    @RequestSetter
    public void setHistoryIndex(int index) {
        historyIndex.set(index);
    }

    @Override
    public IntegerProperty historyIndexProperty() {
        return historyIndex;
    }

    @Override
    public boolean isBackAvailable() {
        return backAvailable.get();
    }

    @Override
    public ReadOnlyBooleanProperty backAvailableProperty() {
        return backAvailable.getReadOnlyProperty();
    }

    @Override
    public boolean isForwardAvailable() {
        return forwardAvailable.get();
    }

    @Override
    public ReadOnlyBooleanProperty forwardAvailableProperty() {
        return forwardAvailable.getReadOnlyProperty();
    }

    @Override
    public boolean isUpAvailable() {
        return upAvailable.get();
    }

    @Override
    public ReadOnlyBooleanProperty upAvailableProperty() {
        return upAvailable.getReadOnlyProperty();
    }

    @Override
    public boolean isWaiting() {
        return waiting.get();
    }

    @Override
    public ReadOnlyBooleanProperty waitingProperty() {
        return waiting.getReadOnlyProperty();
    }

    @Override
    public int getColumnCount() {
        return columnCount.get();
    }

    @Override
    public ReadOnlyIntegerProperty columnCountProperty() {
        return columnCount.getReadOnlyProperty();
    }

    @Override
    public void onMarkAll() {
        markFiles(this.files, file -> true);
    }

    @Override
    public void onUnmarkAll() {
        markFiles(this.files, file -> false);
    }

    @Override
    public void onInvertMarks() {
        markFiles(this.files, file -> !file.isMarked());
    }

    @Override
    public void onMark(MarkOperationType operationType) {
        var config = configManager.getOrCreateConfig(MarkDialogConfig.class, MarkDialogConfig::new);
        var params = new MarkDialogParams(config, settings, operationType);
        var dialog = getComposer().openMarkDialog(params);
        dialog.setOnResult((name) -> {
            if (name == MarkDialogButtons.CANCEL) {
                dialog.closeSafely();
                requestFocus();
                return;
            }
            applyMarkOperation(operationType, new MarkCriteria(dialog.getMask(), dialog.isCaseSensitiveSelected(),
                    dialog.isRegexSelected(), Map.copyOf(dialog.getSelectionsByEntryType()),
                    Map.copyOf(dialog.getSelectionsByFileType())));
            if (name == MarkDialogButtons.OK) {
                dialog.closeSafely();
                requestFocus();
            }
        });
        dialog.setVisible(true);
    }

    @Override
    public void onCreateFile() {
        var nameDialog = openNameDialog(NameDialogType.CREATE_FILE, dialog -> createEntry(dialog, "File", false,
                (storage, uri, name) -> storage.createFile(uri, name, OperationType.PRIMARY)));
        nameDialog.setVisible(true);
    }

    @Override
    public void onCreateFolder() {
        var nameDialog = openNameDialog(NameDialogType.CREATE_FOLDER, dialog -> createEntry(dialog, "Folder", true,
                (storage, uri, name) -> {
                    storage.createDirectory(uri, OperationType.PRIMARY);
                    return storage.getFile(uri, OperationType.SECONDARY);
                }));
        nameDialog.setVisible(true);
    }

    @Override
    public void onCopyToPanel() {
        transferToOtherPanel(TransferOperationType.COPY);
    }

    @Override
    public void onMoveToPanel() {
        transferToOtherPanel(TransferOperationType.MOVE);
    }

    @Override
    public void onCutToClipboard() {
        placeInClipboard(ClipboardOperation.CUT);
    }

    @Override
    public void onCopyToClipboard() {
        placeInClipboard(ClipboardOperation.COPY);
    }

    @Override
    public void onPasteFromClipboard() {
        onPasteIntoFolder(getDirectory());
    }

    @Override
    public void onPasteIntoFolder(GreyWolfFile folder) {
        if (!(clipboard.getContent() instanceof ClipboardFiles content)) {
            showWarning("Clipboard is empty.");
            return;
        }
        var files = content.getFiles();
        var sourceDirectory = content.getDirectory();
        var cut = content.getOperation() == ClipboardOperation.CUT;
        var config = configManager.getOrCreateConfig(TargetDialogConfig.class, TargetDialogConfig::new);
        var params = new TargetDialogParams(config, settings, configManager,
                cut ? TransferOperationType.MOVE : TransferOperationType.COPY, files, sourceDirectory);
        var operation = cut ? getComposer().createMoveOperation(params, folder)
                : getComposer().createCopyOperation(params, folder);
        operation.setOnFinished((target) -> {
            requestFocus();
            if (cut) {
                clipboard.setContent(null);
            }
            refresh(getFile());
            if (!refreshOtherPanelIfShowing(target, List.of()) && cut) {
                refreshOtherPanelIfShowing(sourceDirectory, files);
            }
        });
        operation.start();
    }

    @Override
    public void onRename() {
        var file = getFile();
        if (file == null || file.isParentEntry()) {
            return;
        }
        var fileName = file.getName();
        var dialog = openNameDialog(NameDialogType.RENAME, (d) -> renameFile(file, d));
        dialog.setText(fileName);
        var extensionIndex = fileName.lastIndexOf('.');
        dialog.setSelection(new IndexRange(0,
                file.isDirectory() || extensionIndex <= 0 ? fileName.length() : extensionIndex));
        dialog.setVisible(true);
    }

    @Override
    public void onDelete() {
        var sources = getMarkedFilesInOrder();
        if (sources.isEmpty()) {
            showWarning("Nothing is marked or selected to delete.");
            return;
        }
        var deleteOperation = getComposer().createDeleteOperation(configManager, settings);
        deleteOperation.setSources(sources);
        deleteOperation.setOnFinished(() -> {
            requestFocus();
            refreshAfterRemoval(sources);
            refreshOtherPanelIfShowing(getDirectory(), sources);
        });
        deleteOperation.start();
    }

    @Override
    public void onFileProperties() {
        var file = getFile();
        if (file != null) {
            onFileProperties(file);
        }
    }

    @Override
    public void onFileProperties(GreyWolfFile file) {
        var config = configManager.getOrCreateConfig(FilePropertiesDialogConfig.class, FilePropertiesDialogConfig::new);
        var params = new FilePropertiesDialogParams(config, settings, file);
        var dialog = getComposer().openFilePropertiesDialog(params);
        dialog.setOnResult((name) -> {
            if (name == FilePropertiesDialogButtons.CLOSE) {
                dialog.closeSafely();
                requestFocus();
            }
        });
        dialog.setVisible(true);
    }

    @Override
    public void refresh(@Nullable GreyWolfFile fileToSelect) {
        navigateAsync(getDirectory().getUri(), this::getDirectory, fileToSelect, null, null);
    }

    @Override
    public void refreshAfterRemoval(List<? extends GreyWolfFile> removed) {
        var removedUris = removed.stream().map(GreyWolfFile::getUri).collect(Collectors.toSet());
        var selected = getFile();
        GreyWolfFile fileToSelect = selected;
        if (selected != null && removedUris.contains(selected.getUri())) {
            fileToSelect = null;
            var index = files.indexOf(selected);
            for (var i = index + 1; i < files.size() && fileToSelect == null; i++) {
                if (!removedUris.contains(files.get(i).getUri())) {
                    fileToSelect = files.get(i);
                }
            }
            for (var i = index - 1; i >= 0 && fileToSelect == null; i--) {
                if (!removedUris.contains(files.get(i).getUri())) {
                    fileToSelect = files.get(i);
                }
            }
        }
        refresh(fileToSelect);
    }

    @Override
    public void onContentChanged(GreyWolfFile folder) {
        if (folder.getUri().equals(getDirectory().getUri())) {
            refresh(getFile());
        }
    }

    /**
     * Moves this area one step back in {@link #historyUris}; does nothing if already at the oldest entry.
     * Requests focus back onto the active file listing control regardless, since this is also reachable from
     * the Back toolbar button, which would otherwise keep it after the click.
     */
    @Override
    public void onBack() {
        requestFocus();
        if (historyIndex.get() <= 0) {
            return;
        }
        setHistoryIndex(historyIndex.get() - 1);
    }

    /**
     * Moves this area one step forward in {@link #historyUris}; does nothing if already at the newest entry.
     * Requests focus back onto the active file listing control regardless, since this is also reachable from
     * the Forward toolbar button, which would otherwise keep it after the click.
     */
    @Override
    public void onForward() {
        requestFocus();
        if (historyIndex.get() < 0 || historyIndex.get() >= historyUris.size() - 1) {
            return;
        }
        setHistoryIndex(historyIndex.get() + 1);
    }

    /**
     * Navigates to the synthetic parent (&quot;..&quot;) entry of {@link #files}, if one is present; does
     * nothing at a storage's root, where no parent entry exists. Requests focus back onto the active file
     * listing control regardless, since this is also reachable from the Up toolbar button, which would
     * otherwise keep it after the click.
     */
    @Override
    public void onUp() {
        requestFocus();
        if (files.isEmpty() || !files.get(0).isParentEntry()) {
            return;
        }
        navigateTo(files.get(0));
    }

    /**
     * Reloads this area's current directory listing, preserving the current selection. Requests focus back
     * onto the active file listing control regardless, since this is also reachable from the Refresh toolbar
     * button, which would otherwise keep it after the click.
     */
    @Override
    public void onRefresh() {
        requestFocus();
        refresh(getFile());
    }

    /**
     * Returns the marked entries in {@link #files}, in their current display order, or a single-element list
     * containing the currently selected file if nothing is marked - excluding the synthetic parent
     * (&quot;..&quot;) entry either way. Empty if there is nothing to copy.
     */
    @Override
    public List<GreyWolfFile> getMarkedFilesInOrder() {
        var marked = new ArrayList<GreyWolfFile>();
        for (var file : this.files) {
            if (file.isMarked()) {
                marked.add(file);
            }
        }
        if (!marked.isEmpty()) {
            return marked;
        }
        var selected = getFile();
        if (selected != null && !selected.isParentEntry()) {
            return List.of(selected);
        }
        return List.of();
    }

    @Override
    public void onOpen() {
        var file = getFile();
        if (file == null) {
            return;
        }
        if (file.isDirectory() || file.isDirectoryLink()) {
            onOpenIn(file);
        } else if (file.isRunnable()) {
            onRunFile(file);
        } else {
            onOpenFile(file);
        }
    }

    @Override
    public void onOpenIn(GreyWolfFile folder) {
        Objects.requireNonNull(folder, "Folder can't be null");
        var link = folder.getLink();
        if (link == null) {
            navigateTo(folder);
            return;
        }
        // a link is entered by going to the directory it finally points to, whose storage is found from its URI
        var targetFile = link.getTarget();
        var targetUri = targetFile != null ? targetFile.getUri() : link.getTargetUri();
        navigateAsync(targetUri, () -> storageRegistry.getStorage(targetUri)
                .orElseThrow(() -> new NoSuchFileException(targetUri.toString()))
                .getFile(targetUri, OperationType.SECONDARY), getDirectory(), () -> {
            var dir = getDirectory();
            pushHistory(dir);
            pushToLocationBar(dir);
            pushToLocationTab(dir);
        }, null);
    }

    @Override
    public void onOpenInNewTab(GreyWolfFile folder) {
        Objects.requireNonNull(folder, "Folder can't be null");
        var link = folder.getLink();
        var target = link == null ? null : link.getTarget();
        var config = configManager.getOrCreateConfig(NavigatorTabConfig.class, NavigatorTabConfig::new);
        tab.getComposerAccess().openTab(new NavigatorTabParams(config, target != null ? target : folder));
    }

    @Override
    public void onOpenFile(GreyWolfFile file) {
        Objects.requireNonNull(file, "File can't be null");
        var context = tab.getComposerAccess().getShellPort().getContextAccess();
        context.getHostServices().showDocument(file.getUri().toString());
        file.getStorage().notifyOperation(file.getUri(), GreyWolfFileOperations.READ, OperationType.PRIMARY);
    }

    @Override
    public void onRunFile(GreyWolfFile file) {
        Objects.requireNonNull(file, "File can't be null");
        var path = Path.of(file.getUri());
        var directory = path.getParent();
        if (directory == null) {
            return;
        }
        var command = new ArrayList<String>();
        if (System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("windows")) {
            command.addAll(List.of("cmd", "/c", "start", "", "/d", directory.toString(), path.toString()));
        } else {
            command.add(path.toString());
        }
        var builder = new ProcessBuilder(command).directory(directory.toFile())
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD);
        try {
            builder.start().getOutputStream().close();
            file.getStorage().notifyOperation(file.getUri(), GreyWolfFileOperations.READ, OperationType.PRIMARY);
        } catch (IOException ex) {
            showError("Couldn't run '" + file.getName() + "': " + StorageErrors.describe(ex) + ".");
        }
    }

    @Override
    protected void loadStateFromConfig() {
        super.loadStateFromConfig();
        useViewModes(getConfig().getWorkspaceViewModes(), false);
    }

    @Override
    protected void observeStateForConfig() {
        super.observeStateForConfig();
        getConfig().addListener(configListener);
    }

    @Override
    protected void preDeinitialize() {
        super.preDeinitialize();
        getConfig().removeListener(configListener);
        settings.removeListener(settingsListener);
        cancelTasks();
    }

    @Override
    protected NavigatorAreaConfig getConfig() {
        return (NavigatorAreaConfig) super.getConfig();
    }

    @Override
    protected void preInitialize() {
        super.preInitialize();
        file.addListener((ov, oldV, newV) -> updateFileInfo());
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        styledTheme = settings.getAppearance().getTheme();
        settings.addListener(settingsListener);
        viewModeType.addListener((ov, oldV, newV) -> {
            var current = getCurrentViewModes();
            current.setCurrentType(newV);
            viewMode.set(current.getMode(newV));
            if (!loadingState) {
                applyViewModeEffects();
                publishChange();
            }
        });
        updateColumnCount();
        historyIndex.addListener((ov, oldV, newV) -> {
            updateHistoryAvailability();
            if (updatingHistoryIndex) {
                return;
            }
            if (newV.intValue() < 0 || newV.intValue() >= historyUris.size()) {
                logger.warn("{} Ignored history index {} out of range 0..{}", getDescriptor().getLogPrefix(), newV,
                        historyUris.size() - 1);
                setHistoryIndexSilently(oldV.intValue());
                return;
            }
            goToHistory(oldV.intValue(), newV.intValue());
        });
        modifiableHistoryUris.addListener((ListChangeListener<URI>) change -> updateHistoryAvailability());
        placement.addListener((ov, oldV, newV) -> applyPlacement(newV));
        var dir = Objects.requireNonNull(this.params).getDirectory();
        this.params = null;
        navigateTo(dir);
    }

    GreyWolfSettings getSettings() {
        return this.settings;
    }

    void onChangePanel() {
        var otherPanelDock = resolveOtherPanelDock();
        if (otherPanelDock != null) {
            otherPanelDock.requestFocus();
        }
    }

    void onColumnWidthChanged(TableColumnName name, double width) {
        Objects.requireNonNull(columnInfos.get(name), "No info for column " + name).setWidth(width);
        saveColumns();
    }

    void onColumnSortTypeChanged(TableColumnName name, TableColumn.SortType sortType) {
        Objects.requireNonNull(columnInfos.get(name), "No info for column " + name).setSortType(sortType);
        saveColumns();
    }

    void onColumnIndexChanged(TableColumnName name, int index) {
        Objects.requireNonNull(columnInfos.get(name), "No info for column " + name).setIndex(index);
        saveColumns();
    }

    void onColumnSortIndexChanged(TableColumnName name, Integer index) {
        Objects.requireNonNull(columnInfos.get(name), "No info for column " + name).setSortIndex(index);
        saveColumns();
    }

    /**
     * Toggles the marked state of the entry at {@code index}, called by the view for Insert (on
     * {@link #getFileIndex()}) or a Ctrl+Click on an arbitrary entry (which need not be
     * {@link #getFileIndex()}).
     *
     * @param index the position of the entry to toggle.
     */
    void onMark(int index) {
        if (index < 0 || index >= this.files.size()) {
            return;
        }
        var file = this.files.get(index);
        var delta = applyMark(file, !file.isMarked());
        if (delta != 0) {
            markedCount.set(markedCount.get() + delta);
            restyleFileSource.next(file);
        }
    }

    /**
     * Extends the Shift+arrow mark range from its anchor to {@code newIndex}: toggles the mark of every
     * entry the range newly covers (so an already-marked entry swept into the range gets unmarked, not
     * forced on) and toggles back every entry it no longer covers, which undoes that same toggle if the
     * gesture later retreats past it - restoring whatever the entry's mark was before this gesture touched
     * it, with no separate history needed.
     *
     * @param newIndex the new end of the mark range.
     */
    void onMarkRange(int newIndex) {
        var freshAnchor = this.markRangeAnchorIndex == -1;
        if (freshAnchor) {
            this.markRangeAnchorIndex = getFileIndex();
        }
        var anchor = this.markRangeAnchorIndex;
        var oldFrom = freshAnchor ? 0 : Math.min(anchor, getFileIndex());
        var oldTo = freshAnchor ? -1 : Math.max(anchor, getFileIndex());
        var newFrom = Math.min(anchor, newIndex);
        var newTo = Math.max(anchor, newIndex);
        var toggled = new ArrayList<GreyWolfFile>();
        for (var i = newFrom; i <= newTo; i++) {
            if (i < oldFrom || i > oldTo) {
                toggled.add(this.files.get(i));
            }
        }
        for (var i = oldFrom; i <= oldTo; i++) {
            if (i < newFrom || i > newTo) {
                toggled.add(this.files.get(i));
            }
        }
        markFiles(toggled, file -> !file.isMarked());
    }

    /**
     * Ends the current Shift+arrow mark range: the next Shift+arrow press then starts a brand-new range. Called
     * by the view when the user does anything else, and by this area itself when what the range refers to goes
     * away.
     */
    void onMarkRangeFinished() {
        this.markRangeAnchorIndex = -1;
    }

    /**
     * Framework contract: written directly by the view to report what the context menu is open for.
     * Direct invocation by user code results in undefined behavior.
     */
    ReadOnlyObjectWrapper<ContextTarget> contextTargetWrapper() {
        return contextTarget;
    }

    /**
     * Framework contract: written directly by the view to report the actually-selected entry. Direct
     * invocation by user code results in undefined behavior.
     */
    ReadOnlyObjectWrapper<@Nullable GreyWolfFile> fileWrapper() {
        return file;
    }

    /**
     * Framework contract: emits a requested selection for the view to apply to whichever of the
     * table/list/tiles controls is currently active. Direct invocation by user code results in undefined
     * behavior.
     */
    ObservableSource<@Nullable GreyWolfFile> fileSource() {
        return fileSource;
    }

    /**
     * Framework contract: written directly by the view to report the actually-selected index. Direct
     * invocation by user code results in undefined behavior.
     */
    ReadOnlyIntegerWrapper fileIndexWrapper() {
        return fileIndex;
    }

    /**
     * Framework contract: emits a requested index for the view to select in whichever of the
     * table/list/tiles controls is currently active. Direct invocation by user code results in undefined
     * behavior.
     */
    ObservableSource<Integer> fileIndexSource() {
        return fileIndexSource;
    }

    ObservableSource<Void> updateViewModeSource() {
        return updateViewModeSource;
    }

    ObservableSource<List<GreyWolfFile>> decorateSource() {
        return decorateSource;
    }

    ObservableSource<GreyWolfFile> restyleFileSource() {
        return restyleFileSource;
    }

    ObservableSource<List<GreyWolfFile>> restyleFilesSource() {
        return restyleFilesSource;
    }

    /**
     * Tells how the view has to decorate the file at the moment it is about to be shown.
     */
    FileDecorationType getDecorationType(GreyWolfFile file) {
        return pending.contains(file) ? FileDecorationType.TEMP : FileDecorationType.FINAL;
    }

    /**
     * Framework contract: written directly by the view to report the comparator of the files that its table
     * currently sorts by. Direct invocation by user code results in undefined behavior.
     */
    ReadOnlyObjectWrapper<Comparator<GreyWolfFile>> fileComparatorWrapper() {
        return fileComparator;
    }

    /**
     * Navigates this area to {@code dir}: records it in {@link #historyUris} and pushes it to the sibling
     * LocationBar/location tab, once {@link #navigateAsync} confirms {@code dir} can actually be read.
     * Used for every "fresh" navigation (opening a folder, going up, the initial directory) &mdash; unlike
     * {@link #goToHistory}, which replays an already-recorded entry and must not record it again.
     */
    private void navigateTo(GreyWolfFile dir) {
        navigateAsync(dir, () -> {
            pushHistory(dir);
            pushToLocationBar(dir);
            pushToLocationTab(dir);
        });
    }

    /**
     * Replays history entry at {@code index} through
     * {@link #navigateAsync(URI, Callable, GreyWolfFile, Runnable, Runnable)}: unlike the common case, the target
     * directory isn't known yet, so its resolver looks it up from the stored URI via {@link #storageRegistry}
     * instead of just returning an already-known directory entry. Never records a new entry via
     * {@link #pushHistory}, since this replays one that is already recorded; if the entry can't be read,
     * {@link #historyIndex} goes back to {@code previousIndex}.
     */
    private void goToHistory(int previousIndex, int index) {
        var uri = historyUris.get(index);
        navigateAsync(uri, () -> storageRegistry.getStorage(uri)
                .orElseThrow(() -> new NoSuchFileException(uri.toString()))
                .getFile(uri, OperationType.SECONDARY), getDirectory(), () -> {
            pushToLocationBar(getDirectory());
            pushToLocationTab(getDirectory());
        }, () -> setHistoryIndexSilently(previousIndex));
    }

    /**
     * Attempts to navigate this area to the already-known {@code dir}; a thin entry point over
     * {@link #navigateAsync(URI, Callable, GreyWolfFile, Runnable, Runnable)} for the common case where the target
     * doesn't need resolving first (opening a listed folder, going up, a location bar/tree selection, the
     * initial directory, ...) &mdash; unlike {@link #goToHistory}, which only has a URI to start from. Passes
     * this area's current directory as the entry to try to re-select afterwards, e.g. the synthetic parent
     * (&quot;..&quot;) entry when moving into one of its children.
     */
    private void navigateAsync(GreyWolfFile dir, @Nullable Runnable onCommitted) {
        if (Objects.equals(getDirectory(), dir)) {
            return;
        }
        navigateAsync(dir.getUri(), () -> dir, getDirectory(), onCommitted, null);
    }

    /**
     * The single place that performs the background read behind every navigation or refresh attempt, committing
     * the result through {@link #commitNavigation} only if both resolving and reading {@code dirResolver}
     * succeed; on failure, nothing about this area changes and an error is shown instead. {@code onCommitted},
     * when given, lets each caller layer its own bookkeeping on top of a successful navigation, and
     * {@code onFailed} on a failed one.
     */
    private void navigateAsync(URI uri, Callable<GreyWolfFile> dirResolver,
            @Nullable GreyWolfFile fileToSelectHint, @Nullable Runnable onCommitted,
            @Nullable Runnable onFailed) {
        var comparator = this.fileComparator.get();
        var requestId = ++navigationRequestId;
        cancelTasks();
        waiting.set(true);
        Thread.startVirtualThread(() -> {
            GreyWolfFile dir;
            GreyWolfFile parent;
            try {
                dir = dirResolver.call();
                parent = dir.getStorage().getParent(dir, OperationType.SECONDARY);
            } catch (Exception ex) {
                logger.warn("{} Couldn't get files from {}", getDescriptor().getLogPrefix(), uri, ex);
                showNavigationError(uri, requestId, ex, onFailed);
                return;
            }
            var task = fileProvider.provideFilesAsync(dir.getStorage(), dir.getUri(), OperationType.PRIMARY, loaded -> {
                var areaFiles = new ArrayList<GreyWolfFile>(loaded.size() + 1);
                if (parent != null) {
                    DefaultGreyWolfFile.cast(parent).setParentEntry(true);
                    areaFiles.add(parent);
                }
                areaFiles.addAll(loaded);
                areaFiles.sort(comparator);
                areaFiles.stream().filter(fileRefiner::isRefinable).forEach(pending::add);
                commitNavigation(dir, areaFiles, fileToSelectHint, requestId, onCommitted);
            }, result -> {
                if (result.getStatus() == AsyncStatus.FAILED) {
                    var cause = result.getException();
                    // a directory that is gone from a storage that is there is not a place to come back to
                    if (cause instanceof NoSuchFileException
                            && dir.getStorage().getCategory() != StorageCategory.RECENT
                            && storageRegistry.getStorage(dir.getUri()).isPresent()) {
                        storageRegistry.getRecentStorage().ifPresent(recent ->
                                recent.remove(dir.getUri(), OperationType.SECONDARY));
                    }
                    showNavigationError(uri, requestId, cause, onFailed);
                }
            });
            tasks.add(task);
        });
    }

    private void showNavigationError(URI uri, int requestId, @Nullable Exception cause, @Nullable Runnable onFailed) {
        UiExecutor.execute(() -> {
            if (requestId == navigationRequestId) {
                waiting.set(false);
            }
            showError("Couldn't open '" + uri + "': " + StorageErrors.describe(cause) + ".");
            if (onFailed != null) {
                onFailed.run();
            }
        });
    }

    private void cancelTasks() {
        tasks.forEach(AsyncTask::cancel);
        tasks.clear();
    }

    /**
     * Pushes an already-read {@code finalFiles} listing to this area as {@code dir}, provided no newer
     * navigation (tracked via {@code requestId} against {@link #navigationRequestId}) has started since the
     * caller began reading &mdash; called from a background thread once the read behind
     * {@link #navigateAsync(URI, Callable, GreyWolfFile, Runnable, Runnable)} succeeds, so it hops onto the UI thread
     * itself.
     */
    private void commitNavigation(GreyWolfFile dir, List<GreyWolfFile> finalFiles,
            @Nullable GreyWolfFile fileToSelectHint, int requestId, @Nullable Runnable onCommitted) {
        var fileToSelect = findFileToSelect(finalFiles, fileToSelectHint);
        UiExecutor.execute(() -> {
            if (requestId != navigationRequestId) {
                logger.debug("{} Discarded stale navigation to {}: superseded by a newer request",
                        getDescriptor().getLogPrefix(), dir.getUri());
                return;
            }
            waiting.set(false);
            this.directory.set(dir);
            tab.setTitle(dir.getName());
            tab.setTooltip(dir.getUri().toString());
            modifiableFiles.forEach(pending::remove);
            setFiles(finalFiles);
            setFile(fileToSelect);
            upAvailable.set(!finalFiles.isEmpty() && finalFiles.get(0).isParentEntry());
            if (onCommitted != null) {
                onCommitted.run();
            }
            // the refinements are queued as they come, and one UI task applies all that are queued by
            // then, so a directory of many files doesn't schedule a task for each file
            var refinable = finalFiles.stream().filter(pending::contains).toList();
            tasks.add(fileRefiner.refineFilesAsync(refinable, refinement -> {
                refinements.add(refinement);
                if (refinementsScheduled.compareAndSet(false, true)) {
                    UiExecutor.execute(this::applyRefinements);
                }
            }, result -> { }));
        });
    }

    private void applyRefinements() {
        refinementsScheduled.set(false);
        var refinedFiles = new ArrayList<GreyWolfFile>();
        var refinement = refinements.poll();
        while (refinement != null) {
            var file = refinement.getFile();
            refinement.apply();
            pending.remove(file);
            refinedFiles.add(file);
            refinement = refinements.poll();
        }
        decorateSource.next(refinedFiles);
    }

    /**
     * Records {@code dir} as the newest history entry: drops whatever entries were ahead of
     * {@link #historyIndex} (a fresh navigation from a Back'd-to entry replaces the old "future", the same way
     * a manager's history does), then appends {@code dir}'s URI and advances {@link #historyIndex} to it,
     * trimming the oldest entry once {@link #MAX_HISTORY_SIZE} would otherwise be exceeded.
     */
    private void pushHistory(GreyWolfFile dir) {
        var uri = dir.getUri();
        var index = historyIndex.get();
        if (index >= 0 && index < historyUris.size() && historyUris.get(index).equals(uri)) {
            return;
        }
        modifiableHistoryUris.remove(index + 1, modifiableHistoryUris.size());
        modifiableHistoryUris.add(uri);
        var newIndex = index + 1;
        if (historyUris.size() > MAX_HISTORY_SIZE) {
            modifiableHistoryUris.remove(0);
            newIndex--;
        }
        setHistoryIndexSilently(newIndex);
    }

    private void setHistoryIndexSilently(int index) {
        updatingHistoryIndex = true;
        try {
            historyIndex.set(index);
        } finally {
            updatingHistoryIndex = false;
        }
    }

    private void updateHistoryAvailability() {
        backAvailable.set(historyIndex.get() > 0);
        forwardAvailable.set(historyIndex.get() < historyUris.size() - 1);
    }

    private void applyPlacement(@Nullable Placement placement) {
        var resolved = resolveViewModes(placement);
        if (resolved == null) {
            return;
        }
        if (placement == Placement.WORKSPACE) {
            // The mode type and the columns stay as they are so nothing jumps around on the user when the area is
            // extracted from a panel, but the column counts are the ones of the workspace.
            viewMode.set(resolved.getMode(viewModeType.get()));
            updateViewModeSource.next(null);
        } else {
            // The state of a panel is shared by all its areas. A new area takes everything from the panel,
            // sorting included; an area that comes from a panel keeps its own sorting.
            useViewModes(resolved, isInPanel());
        }
        applyViewModeEffects();
    }

    /**
     * Tells whether the current view mode of this area is one of a panel, i.e. whether the area comes from a panel
     * rather than being new.
     */
    private boolean isInPanel() {
        var config = getConfig();
        var mode = viewMode.get();
        return mode == config.getFirstPanelViewModes().getMode(mode.getType())
                || mode == config.getSecondPanelViewModes().getMode(mode.getType());
    }

    /**
     * Returns the view modes of the config for {@code placement}, or {@code null} while unattached.
     */
    private @Nullable ViewModes resolveViewModes(@Nullable Placement placement) {
        if (placement == null) {
            return null;
        }
        var config = getConfig();
        return switch (placement) {
            case WORKSPACE -> config.getWorkspaceViewModes();
            case FIRST_PANEL -> config.getFirstPanelViewModes();
            case SECOND_PANEL -> config.getSecondPanelViewModes();
        };
    }

    /**
     * Returns the view modes this area works with: the ones for its placement or, while it is unattached, the ones
     * of the workspace.
     */
    private ViewModes getCurrentViewModes() {
        var resolved = resolveViewModes(placement.get());
        return resolved != null ? resolved : getConfig().getWorkspaceViewModes();
    }

    /**
     * Takes the current mode type, the current mode and the columns from {@code target}. With {@code keepSort}
     * the sorting of this area stays as it is.
     */
    private void useViewModes(ViewModes target, boolean keepSort) {
        loadingState = true;
        try {
            viewModeType.set(target.getCurrentType());
        } finally {
            loadingState = false;
        }
        viewMode.set(target.getMode(viewModeType.get()));
        applyTableConfig(target.getDetailsMode().getTableConfig(), keepSort);
        updateViewModeSource.next(null);
    }

    /**
     * Tells the config listeners that the state of this area changed. The other areas of a panel hear it through
     * the hint and follow; areas in the workspace are independent of each other, so they take nothing from it and
     * the config only remembers what was used last.
     */
    private void publishChange() {
        var current = resolveViewModes(placement.get());
        if (current == null) {
            return;
        }
        var config = getConfig();
        if (placement.get() == Placement.WORKSPACE) {
            config.notifyListeners();
            return;
        }
        notifyingConfigListeners = true;
        try {
            config.notifyListeners(PANEL_STATE_CHANGED_HINT);
        } finally {
            notifyingConfigListeners = false;
        }
    }

    /**
     * Writes the columns of this area into its details mode and publishes the change, unless nothing changed.
     * Skipped before the area is attached (the view reports some columns changes already while it is being
     * built) and while the indexes are mid-update (the table reports them one by one).
     */
    private void saveColumns() {
        var current = resolveViewModes(placement.get());
        if (current == null || !hasConsistentColumnIndexes()) {
            return;
        }
        var details = current.getDetailsMode();
        var snapshot = TableConfigUtils.snapshot(columnInfos.values());
        if (TableConfigUtils.isSame(snapshot.getColumns(), details.getTableConfig().getColumns())) {
            return;
        }
        details.setTableConfig(snapshot);
        publishChange();
    }

    /**
     * Makes this area, embedded in a panel, follow the state of that panel after another area of it changed
     * the state. Does nothing for what is already applied.
     */
    private void onConfigChanged(@Nullable Object hint) {
        var current = resolveViewModes(placement.get());
        if (notifyingConfigListeners || !PANEL_STATE_CHANGED_HINT.equals(hint) || current == null
                || placement.get() == Placement.WORKSPACE) {
            return;
        }
        useViewModes(current, true);
        applyViewModeEffects();
    }

    /**
     * Replaces this area's columns with the ones of {@code config} and asks the view to update its table. Does
     * nothing if they are already the same. With {@code keepSort} the sorting of this area stays as it is.
     */
    private void applyTableConfig(TableConfig config, boolean keepSort) {
        var newInfos = config.getColumns().stream().map(TableColumnInfo::new).toList();
        if (keepSort) {
            for (var info : newInfos) {
                var own = columnInfos.get(info.getName());
                if (own != null) {
                    info.setSortIndex(own.getSortIndex());
                    info.setSortType(own.getSortType());
                }
            }
        }
        if (TableConfigUtils.isSame(newInfos, TableConfigUtils.snapshot(columnInfos.values()).getColumns())) {
            return;
        }
        for (var info : newInfos) {
            var own = Objects.requireNonNull(columnInfos.get(info.getName()), "No info for column " + info.getName());
            own.setIndex(info.getIndex());
            own.setWidth(info.getWidth());
            own.setSortIndex(info.getSortIndex());
            own.setSortType(info.getSortType());
        }
    }

    /**
     * Tells whether the indexes of the visible columns are exactly 0..n-1, each used once.
     */
    private boolean hasConsistentColumnIndexes() {
        var indexes = columnInfos.values().stream().filter(TableColumnInfoAdapter::isVisible)
                .map(TableColumnInfoAdapter::getIndex).sorted().toList();
        for (var i = 0; i < indexes.size(); i++) {
            if (indexes.get(i) != i) {
                return false;
            }
        }
        return true;
    }

    /**
     * Brings what depends on the current view mode in line with it: the selection and the column count control
     * of the toolbar.
     */
    private void applyViewModeEffects() {
        updateColumnCount();
        onMarkRangeFinished();
        fileSource.next(getFile());
    }

    private void updateColumnCount() {
        var count = getColumnCount(viewMode.get());
        if (count >= 0) {
            columnCount.set(count);
        }
    }

    private void pushToLocationBar(GreyWolfFile dir) {
        tab.getComposerAccess().getLocationBarPort().setDirectory(dir);
    }

    private void pushToLocationTab(GreyWolfFile dir) {
        var port = tab.getComposerAccess().getLocationTabPort();
        if (port != null) {
            port.setDirectory(dir);
        }
    }

    private void setFiles(List<GreyWolfFile> newFiles) {
        onMarkRangeFinished();
        this.modifiableFiles.setAll(newFiles);
        updateCounts();
    }


    private void setFileInfo(@Nullable String text) {
        fileInfo.set(text);
    }

    /**
     * Applies {@code operationType} to every entry that {@code criteria} matches: {@code MARK}/{@code UNMARK}
     * force it marked/unmarked.
     */
    private void applyMarkOperation(MarkOperationType operationType, MarkCriteria criteria) {
        markFiles(this.files.stream().filter(criteria::matches).toList(), file -> switch (operationType) {
            case MARK -> true;
            case UNMARK -> false;
        });
    }

    /**
     * Gives the files the marked state that {@code target} tells and restyles the ones that changed, all at once, so
     * marking one file or all of them is a single event for the view. The parent ("..") entry can never be marked and
     * is left untouched.
     */
    private void markFiles(List<GreyWolfFile> files, Predicate<GreyWolfFile> target) {
        var changed = new ArrayList<GreyWolfFile>();
        var delta = 0;
        for (var file : files) {
            var fileDelta = applyMark(file, target.test(file));
            if (fileDelta != 0) {
                changed.add(file);
                delta += fileDelta;
            }
        }
        if (!changed.isEmpty()) {
            markedCount.set(markedCount.get() + delta);
            restyleFilesSource.next(changed);
        }
    }

    /**
     * Gives the file the marked state; the parent entry is never marked.
     *
     * @return {@code 1} if the file became marked, {@code -1} if unmarked, {@code 0} if nothing changed.
     */
    private int applyMark(GreyWolfFile file, boolean marked) {
        if (file.isParentEntry() || file.isMarked() == marked) {
            return 0;
        }
        file.setMarked(marked);
        return marked ? 1 : -1;
    }

    private int visibleColumnCount() {
        return (int) columnInfos.values().stream().filter(TableColumnInfoAdapter::isVisible).count();
    }

    private void updateFileInfo() {
        var file = getFile();
        if (file == null) {
            setFileInfo(null);
            return;
        }
        var link = file.getLink();
        if (link == null) {
            setFileInfo(file.isParentEntry() ? ".." : file.getName());
            return;
        }
        // a link is described by where it points to, not by its own name; the target may be in another storage
        var targetUri = link.getTargetUri();
        setFileInfo(storageRegistry.getStorage(targetUri)
                .map(storage -> storage.getLocationConverter().toLocation(targetUri))
                .orElse(targetUri.toString()));
    }

    private void updateCounts() {
        var folders = 0;
        var regularFiles = 0;
        var marked = 0;
        for (var file : this.files) {
            if (file.isMarked()) {
                marked++;
            }
            if (file.isParentEntry()) {
                continue;
            }
            if (file.isDirectory() || file.isDirectoryLink()) {
                folders++;
            } else {
                regularFiles++;
            }
        }
        folderCount.set(folders);
        fileCount.set(regularFiles);
        markedCount.set(marked);
    }

    /**
     * Adds the name dialog filled with the most recent name and fully selected, the caller makes it visible;
     * {@code okAction} is called when the user confirms the name.
     */
    private TextsDialogPort openNameDialog(NameDialogType type, Consumer<TextsDialogPort> okAction) {
        var config = configManager.getOrCreateConfig(NameDialogConfig.class, NameDialogConfig::new);
        var params = new NameDialogParams(config, settings, type);
        var dialog = getComposer().openNameDialog(params);
        dialog.setSelection(new IndexRange(0, dialog.getText().length()));
        dialog.setOnResult((name) -> {
            if (name == TextDialogButtons.OK) {
                okAction.accept(dialog);
            } else if (name == TextDialogButtons.CANCEL) {
                dialog.closeSafely();
                requestFocus();
            }
        });
        return dialog;
    }

    private void createEntry(TextsDialogPort dialog, String kind, boolean directory, EntryCreator creator) {
        var lowerKind = kind.toLowerCase(Locale.ROOT);
        var name = dialog.getText().trim();
        if (name.isEmpty()) {
            showWarning(kind + " name is not specified.");
            return;
        }
        if (name.contains("/") || name.contains("\\")) {
            showWarning(kind + " name can't contain '/' or '\\'.");
            return;
        }
        var storage = getDirectory().getStorage();
        var uri = UriUtils.resolvePath(getDirectory().getUri(), name, directory);
        Thread.startVirtualThread(() -> {
            GreyWolfFile createdFile = null;
            Exception failure = null;
            try {
                createdFile = creator.create(storage, uri, name);
            } catch (Exception ex) {
                failure = ex;
            }
            var finalFile = createdFile;
            var finalFailure = failure;
            UiExecutor.execute(() -> {
                if (finalFailure != null) {
                    logger.warn("{} Error creating {} at {}", getDescriptor().getLogPrefix(), lowerKind, uri,
                            finalFailure);
                    showError(describe(finalFailure, name, lowerKind));
                    return;
                }
                refresh(finalFile);
                dialog.closeSafely();
                requestFocus();
            });
        });
    }

    private void renameFile(GreyWolfFile file, TextsDialogPort dialog) {
        var newName = dialog.getText().trim();
        if (newName.isEmpty()) {
            showWarning("Name is not specified.");
            return;
        }
        if (newName.contains("/") || newName.contains("\\")) {
            showWarning("Name can't contain '/' or '\\'.");
            return;
        }
        if (newName.equals(file.getName())) {
            dialog.closeSafely();
            requestFocus();
            return;
        }
        var storage = getDirectory().getStorage();
        var oldUri = file.getUri();
        var newUri = UriUtils.resolvePath(getDirectory().getUri(), newName, file.isDirectory());
        Thread.startVirtualThread(() -> {
            GreyWolfFile renamedFile = null;
            Exception failure = null;
            try {
                storage.renameFile(oldUri, newName, OperationType.PRIMARY);
                renamedFile = storage.getFile(newUri, OperationType.SECONDARY);
            } catch (Exception ex) {
                failure = ex;
            }
            var finalFile = renamedFile;
            var finalFailure = failure;
            UiExecutor.execute(() -> {
                if (finalFailure != null) {
                    logger.warn("{} Error renaming {} to {}", getDescriptor().getLogPrefix(), oldUri, newName,
                            finalFailure);
                    showError(describeRenameFailure(finalFailure, file.getName(), newName));
                    return;
                }
                refresh(finalFile);
                dialog.closeSafely();
                requestFocus();
            });
        });
    }

    private String describeRenameFailure(Exception ex, String oldName, String newName) {
        if (ex instanceof FileAlreadyExistsException) {
            return "An entry named '" + newName + "' already exists.";
        }
        if (ex instanceof AccessDeniedException) {
            return "Access denied while renaming '" + oldName + "'.";
        }
        if (ex instanceof NoSuchFileException) {
            return "'" + oldName + "' no longer exists.";
        }
        return "Couldn't rename '" + oldName + "' to '" + newName + "': " + ex.getMessage();
    }

    private String describe(Exception ex, String name, String kind) {
        if (ex instanceof FileAlreadyExistsException) {
            return "A " + kind + " named '" + name + "' already exists.";
        }
        if (ex instanceof AccessDeniedException) {
            return "Access denied while creating " + kind + " '" + name + "'.";
        }
        return "Couldn't create " + kind + " '" + name + "': " + ex.getMessage();
    }

    private void showWarning(String text) {
        var params = new AlertDialogParams(WindowType.NESTED, settings, AlertDialogType.WARNING);
        var dialog = getComposer().openAlertDialog(params);
        dialog.setMessage(text);
        dialog.setVisible(true);
    }

    private void showError(String text) {
        var params = new AlertDialogParams(WindowType.NESTED, settings, AlertDialogType.ERROR);
        var dialog = getComposer().openAlertDialog(params);
        dialog.setMessage(text);
        dialog.setVisible(true);
    }

    /**
     * The other panel's currently shown directory, falling back to this area's own directory if this tab is
     * standalone (no sibling panel at all) or the other panel has no navigator tab open.
     */
    private GreyWolfFile resolveOtherPanelDirectory() {
        var other = resolveOtherPanelAreaPort();
        return other == null ? getDirectory() : other.getDirectory();
    }

    /**
     * Refreshes the other panel's active navigator area, but only if it happens to already be showing
     * {@code directory} - a best-effort way to reflect files just copied into it without disturbing an
     * unrelated directory the other panel might be showing instead; {@code removed} are the entries that
     * have left {@code directory} meanwhile. Does nothing if this tab is standalone (no sibling panel at all).
     *
     * @return {@code true} if the other panel was refreshed.
     */
    private boolean refreshOtherPanelIfShowing(GreyWolfFile directory, List<? extends GreyWolfFile> removed) {
        var other = resolveOtherPanelAreaPort();
        if (other != null && Objects.equals(other.getDirectory().getUri(), directory.getUri())) {
            other.refreshAfterRemoval(removed);
            return true;
        }
        return false;
    }

    /**
     * Places the marked entries in this area (or the selected entry, if none are marked) into the shared
     * clipboard under {@code operation}; does nothing if there is nothing marked or selected.
     */
    private void placeInClipboard(ClipboardOperation operation) {
        var sources = getMarkedFilesInOrder();
        if (sources.isEmpty()) {
            showWarning("Nothing is marked or selected.");
            return;
        }
        @SuppressWarnings("unchecked")
        var files = (List<GreyWolfFile>) (List<?>) sources;
        clipboard.setContent(new ClipboardFiles(operation, getDirectory(), files));
        onUnmarkAll();
    }

    /**
     * Copies or moves the marked entries onto the other panel's current directory, the dialog letting the
     * user change the target.
     */
    private void transferToOtherPanel(TransferOperationType type) {
        var sources = getMarkedFilesInOrder();
        if (sources.isEmpty()) {
            showWarning("Nothing is marked or selected to " + (type == TransferOperationType.MOVE ? "move" : "copy")
                    + ".");
            return;
        }
        @SuppressWarnings("unchecked")
        var files = (List<GreyWolfFile>) (List<?>) sources;
        var move = type == TransferOperationType.MOVE;
        var config = configManager.getOrCreateConfig(SourceDialogConfig.class,
                SourceDialogConfig::new);
        // a single moved object may be renamed on the way, which needs the selected file to know the entry type
        var selected = getFile();
        var currentFile = move ? (selected != null ? selected : files.get(0)) : null;
        var params = new SourceDialogParams(config, settings, configManager, type, files, resolveOtherPanelDirectory(),
                storageRegistry::getAllStorages, currentFile);
        var operation = move ? getComposer().createMoveOperation(params, null)
                : getComposer().createCopyOperation(params, null);
        operation.setOnFinished((target) -> {
            requestFocus();
            if (move) {
                refreshAfterRemoval(files);
            } else {
                refresh(getFile());
            }
            refreshOtherPanelIfShowing(target, List.of());
        });
        operation.start();
    }

    /**
     * Returns the tab dock of the panel other than the one this tab is embedded in, or {@code null} when this tab is
     * standalone (no sibling panel at all). Walks the panel-tab-dock chain through generic, plugin-agnostic
     * ports only, so nothing in {@code core.panels} needs to know this is a navigator.
     */
    private @Nullable PanelTabDockPort resolveOtherPanelDock() {
        var parent = tab.getComposerAccess().getParentPort();
        if (!(parent instanceof PanelTabDockPort dock)) {
            return null;
        }
        var dualPanels = dock.getComposerAccess().getDualPanelsTabPort();
        return dualPanels.getComposerAccess().getPanelTabDockPort(dock.getPanel().getOther());
    }

    /**
     * Returns the area of the navigator tab in the other panel, or {@code null} when this tab is
     * standalone (no sibling panel at all) or the other panel has no navigator tab open.
     */
    private @Nullable NavigatorAreaPort resolveOtherPanelAreaPort() {
        var otherPanelDock = resolveOtherPanelDock();
        if (otherPanelDock == null) {
            return null;
        }
        var selectedTab = otherPanelDock.getComposerAccess().getSelectedTabPort();
        return selectedTab instanceof NavigatorTabPort managerTab ? managerTab.getComposerAccess().getAreaPort()
                : null;
    }

    private void applySettings() {
        var theme = settings.getAppearance().getTheme();
        if (theme != styledTheme) {
            styledTheme = theme;
            restyleFilesSource.next(new ArrayList<>(modifiableFiles));
        }
    }
}
