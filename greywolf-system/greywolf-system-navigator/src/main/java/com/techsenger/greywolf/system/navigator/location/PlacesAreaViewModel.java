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

import com.google.errorprone.annotations.concurrent.LazyInit;
import com.techsenger.annotations.Nullable;
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.greywolf.core.GreyWolfSettings;
import com.techsenger.greywolf.core.registries.FileStorageRegistry;
import com.techsenger.greywolf.system.navigator.style.NavigatorIcons;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.base.GreyWolfStorage;
import com.techsenger.greywolf.storage.base.PersonalDirectories;
import com.techsenger.greywolf.storage.base.PersonalDirectory;
import com.techsenger.greywolf.storage.base.PersonalDirectoryType;
import com.techsenger.patternfx.mvvm.ChildComposer;
import com.techsenger.shellfx.base.icon.FontIcon;
import com.techsenger.shellfx.base.registry.ItemRegistryListener;
import com.techsenger.shellfx.core.area.AbstractAreaViewModel;
import com.techsenger.shellfx.core.settings.SettingsListener;
import com.techsenger.shellfx.material.UiExecutor;
import com.techsenger.shellfx.material.theme.Theme;
import com.techsenger.shellfx.storage.base.OperationType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public final class PlacesAreaViewModel extends AbstractAreaViewModel<ChildComposer> implements PlacesAreaPort {

    private static final Logger logger = LoggerFactory.getLogger(PlacesAreaViewModel.class);

    private @Nullable PlacesAreaParams params;

    private final GreyWolfSettings settings;

    @LazyInit
    private FileStorageRegistry storageRegistry;

    private final ObjectProperty<@Nullable GreyWolfFile> directory = new SimpleObjectProperty<>();

    private final ObservableList<Place> modifiablePersonalPlaces = FXCollections.observableArrayList();

    private final ObservableList<Place> personalPlaces =
            FXCollections.unmodifiableObservableList(modifiablePersonalPlaces);

    private final ObservableList<Place> modifiableStoragePlaces = FXCollections.observableArrayList();

    private final ObservableList<Place> storagePlaces =
            FXCollections.unmodifiableObservableList(modifiableStoragePlaces);

    /**
     * Indexes the rows of {@link #personalPlaces} and {@link #storagePlaces} by directory, which also tells the group
     * and the position of the row. Indexed again whenever either list is replaced, which is rare.
     */
    private final Map<GreyWolfFile, Place> placesByDirectory = new HashMap<>();

    /**
     * The entry in {@link #personalPlaces}/{@link #storagePlaces} that {@link #directory} currently matches, or
     * {@code null} if it matches none - resolved by {@link #updatePlace} (checking {@link #personalPlaces} first,
     * then {@link #storagePlaces}).
     */
    private final ReadOnlyObjectWrapper<@Nullable Place> place = new ReadOnlyObjectWrapper<>();

    /**
     * Brings the Storages rows up to date whenever the registry reports that its storages have changed.
     */
    private final ItemRegistryListener<GreyWolfStorage> storageListener =
            (added, removed) -> UiExecutor.execute(this::refreshStoragePlaces);

    private final ReadOnlyObjectWrapper<Theme> theme = new ReadOnlyObjectWrapper<>();

    private final SettingsListener settingsListener = this::applySettings;

    public PlacesAreaViewModel(PlacesAreaParams params) {
        super(params);
        this.params = params;
        this.settings = params.getSettings();
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
    public @Nullable Place getPlace() {
        return place.get();
    }

    @Override
    public ReadOnlyObjectProperty<@Nullable Place> placeProperty() {
        return place.getReadOnlyProperty();
    }

    @Override
    public @Unmodifiable ObservableList<Place> getPersonalPlaces() {
        return personalPlaces;
    }

    @Override
    public @Unmodifiable ObservableList<Place> getStoragePlaces() {
        return storagePlaces;
    }

    @Override
    protected void preInitialize() {
        super.preInitialize();
        applySettings();
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        var params = Objects.requireNonNull(this.params);
        this.storageRegistry = params.getStorageRegistry();
        this.params = null;
        storageRegistry.addListener(storageListener);
        settings.addListener(settingsListener);
        refreshStoragePlaces();
        directory.addListener((ov, oldV, newV) -> updatePlace());
        Thread.startVirtualThread(() -> {
            var places = buildPersonalPlaces();
            UiExecutor.execute(() -> {
                setPersonalPlaces(places);
                updatePlace();
            });
        });
    }

    @Override
    protected void preDeinitialize() {
        super.preDeinitialize();
        storageRegistry.removeListener(storageListener);
        settings.removeListener(settingsListener);
    }

    Theme getTheme() {
        return theme.get();
    }

    ReadOnlyObjectProperty<Theme> themeProperty() {
        return theme.getReadOnlyProperty();
    }

    private void refreshStoragePlaces() {
        var places = new ArrayList<Place>();
        var storages = new ArrayList<>(storageRegistry.getSystemStorages());
        storages.addAll(storageRegistry.getCustomStorages());
        storageRegistry.getRecentStorage().ifPresent(storages::add);
        for (var storage : storages) {
            places.add(new Place(storage.getRoot(), null, storage.getName(), PlaceGroup.STORAGE, places.size()));
        }
        modifiableStoragePlaces.setAll(places);
        indexPlaces();
        updatePlace();
    }

    private void setPersonalPlaces(List<Place> personalPlaces) {
        this.modifiablePersonalPlaces.setAll(personalPlaces);
        indexPlaces();
    }

    /**
     * Indexes the rows of both lists again in {@link #placesByDirectory}; a directory in both lists belongs to the
     * Personal row.
     */
    private void indexPlaces() {
        placesByDirectory.clear();
        Stream.concat(modifiablePersonalPlaces.stream(), modifiableStoragePlaces.stream()).forEach(place -> {
            var dir = place.directory();
            if (dir != null) {
                placesByDirectory.putIfAbsent(dir, place);
            }
        });
    }

    private List<Place> buildPersonalPlaces() {
        var places = new ArrayList<Place>();
        for (var personalDirectory : PersonalDirectories.getAll()) {
            addPersonalPlace(places, personalDirectory);
        }
        return places;
    }

    private void addPersonalPlace(List<Place> places, PersonalDirectory personalDirectory) {
        var uri = personalDirectory.uri();
        var storage = storageRegistry.getStorage(uri).orElse(null);
        if (storage == null) {
            logger.debug("{} No storage claims personal directory '{}' ({})", getDescriptor().getLogPrefix(),
                    personalDirectory.type(), uri);
            return;
        }
        try {
            var file = storage.getFile(uri, OperationType.SECONDARY);
            places.add(new Place(file, iconFor(personalDirectory.type()),
                    labelFor(personalDirectory.type()), PlaceGroup.PERSONAL, places.size()));
        } catch (IOException ex) {
            logger.debug("{} Couldn't resolve personal directory '{}' ({})", getDescriptor().getLogPrefix(),
                    personalDirectory.type(), uri, ex);
        }
    }

    private String labelFor(PersonalDirectoryType type) {
        return switch (type) {
            case HOME -> "Home";
            case DESKTOP -> "Desktop";
            case DOCUMENTS -> "Documents";
            case DOWNLOADS -> "Downloads";
            case MUSIC -> "Music";
            case PICTURES -> "Pictures";
            case VIDEOS -> "Videos";
        };
    }

    /**
     * Returns the icon of a personal directory in this list: a plain symbol of the place rather than a folder, since
     * every row here is a place, unlike in a listing of folders and files, where the entry's own icon is used.
     */
    private FontIcon<?> iconFor(PersonalDirectoryType type) {
        return switch (type) {
            case HOME -> NavigatorIcons.HOME;
            case DESKTOP -> NavigatorIcons.DESKTOP;
            case DOCUMENTS -> NavigatorIcons.DOCUMENTS;
            case DOWNLOADS -> NavigatorIcons.DOWNLOADS;
            case MUSIC -> NavigatorIcons.MUSIC;
            case PICTURES -> NavigatorIcons.PICTURES;
            case VIDEOS -> NavigatorIcons.VIDEOS;
        };
    }

    /**
     * Re-resolves {@link #place} for the current directory against the rows shown now. Works on the UI thread over
     * an in-memory map only, so it does no I/O.
     */
    private void updatePlace() {
        var dir = directory.get();
        place.set(dir == null ? null : placesByDirectory.get(dir));
    }

    private void applySettings() {
        theme.set(settings.getAppearance().getTheme());
    }
}
