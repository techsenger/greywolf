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

package com.techsenger.greywolf.shared.location;

import com.techsenger.annotations.Nullable;
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.greywolf.core.GreyWolfSettings;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.base.GreyWolfStorage;
import com.techsenger.greywolf.storage.base.GreyWolfStorageProvider;
import com.techsenger.greywolf.storage.ui.view.FileViewModel;
import com.techsenger.shellfx.core.area.AbstractAreaViewModel;
import com.techsenger.shellfx.core.settings.SettingsListener;
import com.techsenger.shellfx.material.UiExecutor;
import com.techsenger.shellfx.material.theme.Theme;
import com.techsenger.shellfx.storage.base.OperationType;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public class LocationBarViewModel<C extends LocationBarComposer> extends AbstractAreaViewModel<C>
        implements LocationBarPort, FileViewModel<C> {

    private static final Logger logger = LoggerFactory.getLogger(LocationBarViewModel.class);

    private final GreyWolfStorageProvider storageProvider;

    private final GreyWolfSettings settings;

    private final SettingsListener settingsListener = this::applySettings;

    private final ReadOnlyObjectWrapper<Theme> theme = new ReadOnlyObjectWrapper<>();

    private final ObservableList<GreyWolfFile> modifiablePathFiles = FXCollections.observableArrayList();

    private final ObservableList<GreyWolfFile> pathFiles =
            FXCollections.unmodifiableObservableList(modifiablePathFiles);

    private final BooleanProperty showStorageIcon = new SimpleBooleanProperty();

    private final ReadOnlyBooleanWrapper editMode = new ReadOnlyBooleanWrapper();

    private final ObjectProperty<@Nullable GreyWolfFile> directory = new SimpleObjectProperty<>();

    private final ReadOnlyObjectWrapper<@Nullable GreyWolfFile> file = new ReadOnlyObjectWrapper<>();

    /**
     * The most recently requested popup anchor - an arrow's parent, or a clicked storage-root segment - used
     * only to detect whether an in-flight {@link #onArrowClicked}/{@link #onSegmentClicked} result has been
     * superseded by a newer click, so a stale result is discarded instead of overwriting a popup already open
     * for the newer one.
     */
    private volatile @Nullable GreyWolfFile requestedPopupAnchor;

    public LocationBarViewModel(LocationBarParams params) {
        super(params);
        this.storageProvider = params.getStorageProvider();
        this.settings = params.getSettings();
    }

    @Override
    public @Unmodifiable ObservableList<GreyWolfFile> getPathFiles() {
        return pathFiles;
    }

    @Override
    public boolean isShowStorageIcon() {
        return showStorageIcon.get();
    }

    @Override
    public void setShowStorageIcon(boolean showStorageIcon) {
        this.showStorageIcon.set(showStorageIcon);
    }

    @Override
    public BooleanProperty showStorageIconProperty() {
        return showStorageIcon;
    }

    @Override
    public boolean isEditMode() {
        return editMode.get();
    }

    @Override
    public ReadOnlyBooleanProperty editModeProperty() {
        return editMode.getReadOnlyProperty();
    }

    @Override
    public @Nullable GreyWolfFile getDirectory() {
        return directory.get();
    }

    @Override
    public void setDirectory(GreyWolfFile directory) {
        if (Objects.equals(this.directory.get(), directory)) {
            logger.debug("{} Skipped setDirectory: already current", getDescriptor().getLogPrefix());
            return;
        }
        var old = this.directory.get();
        this.directory.set(directory);
        logger.debug("{} Processing setDirectory; old directory: {}, new directory: {}",
                getDescriptor().getLogPrefix(), old, directory);
        Thread.startVirtualThread(() -> {
            try {
                var path = directory.getStorage().getHierarchy(directory.getUri(), OperationType.SECONDARY);
                UiExecutor.execute(() -> {
                    if (!Objects.equals(this.directory.get(), directory)) {
                        logger.debug("{} Discarded stale setDirectory result for {}: current directory is now {}",
                                getDescriptor().getLogPrefix(), directory.getUri(), this.directory.get());
                        return;
                    }
                    modifiablePathFiles.setAll(path);
                    var popup = getComposer().getFolderPopupPort();
                    if (popup != null) {
                        popup.closeSafely();
                    }
                    editMode.set(false);
                });
            } catch (IOException ex) {
                logger.error("{} Failed to build ancestor path for {}", getDescriptor().getLogPrefix(),
                        directory.getUri(), ex);
            }
        });
    }

    @Override
    public ObjectProperty<@Nullable GreyWolfFile> directoryProperty() {
        return directory;
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
    protected void preInitialize() {
        super.preInitialize();
        applySettings();
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        settings.addListener(settingsListener);
    }

    @Override
    protected void preDeinitialize() {
        super.preDeinitialize();
        settings.removeListener(settingsListener);
    }

    protected Theme getTheme() {
        return theme.get();
    }

    protected ReadOnlyObjectProperty<Theme> themeProperty() {
        return theme.getReadOnlyProperty();
    }

    /**
     * Called by the view when the context menu of a segment is requested, so the handlers of its items know the
     * folder.
     *
     * @param segment the segment the menu is requested for
     */
    protected void onContextRequested(GreyWolfFile segment) {
        file.set(segment);
    }

    /**
     * Called by the view when the user clicks past the last segment, requesting the switch to edit mode.
     */
    protected void onEditPath() {
        if (pathFiles.isEmpty()) {
            return;
        }
        editMode.set(true);
    }

    /**
     * Called by the view when the user commits a path typed in edit mode (e.g. presses Enter). The view stays
     * in edit mode until this resolves; success closes edit mode, failure leaves it open so the typed text
     * isn't lost.
     *
     * @param path the entered path, relative to the current storage root
     */
    protected void onPathEntered(String path) {
        var dir = this.directory.get();
        if (dir == null) {
            return;
        }
        Thread.startVirtualThread(() -> {
            var storage = dir.getStorage();
            var uri = storage.getUri().resolve(path.startsWith("/") ? path.substring(1) : path);
            try {
                var file = storage.getFile(uri, OperationType.SECONDARY);
                setDirectory(file);
            } catch (IOException ex) {
                logger.error("{} Failed to navigate to entered path {}", getDescriptor().getLogPrefix(), path, ex);
                // Once error-feedback UX is defined, notify the view so it can show an error; edit mode is
                // intentionally left open here so the user's typed text isn't lost.
            }
        });
    }

    /**
     * Called by the view when the user cancels editing — Escape key, or the text field losing focus.
     */
    protected void onEditCancelled() {
        editMode.set(false);
    }

    /**
     * Called by the view when the user clicks a segment button. Storage-root segments open the list of all
     * available storages instead of navigating directly.
     *
     * @param segment the clicked segment
     */
    protected void onSegmentClicked(GreyWolfFile segment) {
        if (segment.isRoot()) {
            requestedPopupAnchor = segment;
            Thread.startVirtualThread(() -> {
                var roots = storageProvider.getStorages().stream().map(GreyWolfStorage::getRoot).toList();
                UiExecutor.execute(() -> {
                    if (!Objects.equals(this.requestedPopupAnchor, segment)) {
                        return;
                    }
                    openFolderPopup(roots, pathFiles.isEmpty() ? null : pathFiles.get(0));
                });
            });
        } else {
            setDirectory(segment);
        }
    }

    /**
     * Called by the view when the user clicks the button that stands for the segments hidden because of the lack of
     * space.
     *
     * @param hiddenSegments the hidden segments
     */
    protected void onHiddenSegmentsClicked(List<GreyWolfFile> hiddenSegments) {
        openFolderPopup(hiddenSegments, null);
    }

    /**
     * Called by the view when the user clicks the chevron following {@code parent}, requesting its child
     * directories for the dropdown.
     *
     * @param parent the segment whose trailing arrow was clicked
     */
    protected void onArrowClicked(GreyWolfFile parent) {
        requestedPopupAnchor = parent;
        Thread.startVirtualThread(() -> {
            try {
                var directories = parent.getStorage().getDirectories(parent.getUri(), OperationType.SECONDARY);
                directories.sort(Comparator.comparing(GreyWolfFile::getName, String.CASE_INSENSITIVE_ORDER));
                UiExecutor.execute(() -> {
                    if (!Objects.equals(this.requestedPopupAnchor, parent)) {
                        return;
                    }
                    var parentIndex = pathFiles.indexOf(parent);
                    openFolderPopup(directories, parentIndex >= 0 && parentIndex + 1 < pathFiles.size()
                            ? pathFiles.get(parentIndex + 1) : null);
                });
            } catch (IOException ex) {
                logger.error("{} Failed to load directories for {}", getDescriptor().getLogPrefix(),
                        parent.getUri(), ex);
            }
        });
    }

    String getEditText() {
        return pathFiles.stream()
                .skip(1)
                .map(GreyWolfFile::getName)
                .collect(Collectors.joining("/", "/", ""));
    }

    GreyWolfSettings getSettings() {
        return settings;
    }

    /**
     * Opens the popup with the files. A file the user picks in it becomes the directory, even if it is a storage root.
     */
    private void openFolderPopup(List<GreyWolfFile> files, @Nullable GreyWolfFile initialFile) {
        var params = new FolderPopupParams(settings, files, initialFile, isShowStorageIcon());
        var port = getComposer().openFolderPopup(params);
        if (port != null) {
            port.submittedProperty().addListener((ov, oldV, newV) -> {
                var file = port.getFile();
                if (newV && file != null) {
                    port.closeSafely();
                    setDirectory(file);
                }
            });
            // the popup hides itself when the user leaves it; a hidden popup is of no use, so it is closed
            port.visibleProperty().addListener((ov, oldV, newV) -> {
                if (oldV && !newV) {
                    port.closeSafely();
                }
            });
            port.setVisible(true);
            port.requestFocus();
        }
    }

    private void applySettings() {
        theme.set(settings.getAppearance().getTheme());
    }
}
