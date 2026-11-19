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

package com.techsenger.greywolf.system.navigator.location;

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.core.GreyWolfSettings;
import com.techsenger.greywolf.core.registries.FileStorageRegistry;
import com.techsenger.greywolf.shared.directory.DirectoryTreeParams;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.ui.view.FolderContentListener;
import com.techsenger.shellfx.core.close.CloseCheckResult;
import com.techsenger.shellfx.core.close.ClosePreparationResult;
import com.techsenger.shellfx.core.tab.AbstractTabViewModel;
import java.util.Objects;
import java.util.function.Consumer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author Pavel Castornii
 */
public final class LocationTabViewModel extends AbstractTabViewModel<LocationTabComposer>
        implements LocationTabPort, FolderContentListener {

    private @Nullable LocationTabParams params;

    private final FileStorageRegistry storageRegistry;

    private final GreyWolfSettings settings;

    private final ObjectProperty<LocationMode> mode = new SimpleObjectProperty<>();

    private final ObjectProperty<@Nullable GreyWolfFile> directory = new SimpleObjectProperty<>();

    public LocationTabViewModel(LocationTabParams params) {
        super(params);
        this.params = params;
        this.storageRegistry = params.getStorageRegistry();
        this.settings = params.getSettings();
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public LocationMode getMode() {
        return mode.get();
    }

    @Override
    public void setMode(LocationMode mode) {
        this.mode.set(mode);
    }

    @Override
    public ObjectProperty<LocationMode> modeProperty() {
        return mode;
    }

    @Override
    public @Nullable GreyWolfFile getDirectory() {
        return directory.get();
    }

    @Override
    public void setDirectory(GreyWolfFile directory) {
        this.directory.set(directory);
    }

    @Override
    public ObjectProperty<@Nullable GreyWolfFile> directoryProperty() {
        return directory;
    }

    @Override
    public void onContentChanged(GreyWolfFile folder) {
        getComposer().getAreaPort().onContentChanged(folder);
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        var params = Objects.requireNonNull(this.params);
        directory.addListener((ov, oldV, newV) -> pushDirectoryToChildren());
        mode.addListener((ov, oldV, newV) -> {
            setTitle(titleFor(newV));
            getComposer().updateMode(newV, createDirectoryTreeParams(newV), createPlacesAreaParams(newV));
            pushDirectoryToChildren();
        });
        this.params = null;
        setMode(params.getMode());
    }

    private @Nullable DirectoryTreeParams createDirectoryTreeParams(LocationMode mode) {
        if (mode == LocationMode.PLACES) {
            return null;
        }
        return new DirectoryTreeParams(storageRegistry::getAllStorages, storageRegistry, settings);
    }

    private @Nullable PlacesAreaParams createPlacesAreaParams(LocationMode mode) {
        if (mode == LocationMode.TREE) {
            return null;
        }
        return new PlacesAreaParams(storageRegistry, settings);
    }

    private void pushDirectoryToChildren() {
        var dir = directory.get();
        if (dir == null) {
            return;
        }
        var directoryTree = getComposer().getDirectoryTreePort();
        if (directoryTree != null) {
            directoryTree.setDirectory(dir);
        }
        var placesArea = getComposer().getPlacesAreaPort();
        if (placesArea != null) {
            placesArea.setDirectory(dir);
        }
    }

    private String titleFor(LocationMode mode) {
        return switch (mode) {
            case TREE -> "Tree";
            case PLACES -> "Places";
            case TREE_AND_PLACES -> "Tree & Places";
        };
    }
}
