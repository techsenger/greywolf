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

package com.techsenger.greywolf.core.internal;

import com.techsenger.annotations.Nullable;
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.greywolf.core.registries.FileStorageRegistry;
import com.techsenger.greywolf.storage.base.GreyWolfStorage;
import com.techsenger.greywolf.storage.base.RecentStorage;
import com.techsenger.greywolf.storage.base.StorageCategory;
import com.techsenger.greywolf.storage.base.spi.CustomStorageService;
import com.techsenger.greywolf.storage.base.spi.RecentStorageService;
import com.techsenger.greywolf.storage.base.spi.SystemStorageService;
import com.techsenger.drivekit.core.DriveMonitor;
import com.techsenger.drivekit.core.MonitorMode;
import com.techsenger.shellfx.base.registry.AbstractItemRegistry;
import com.techsenger.shellfx.base.registry.Registration;
import com.techsenger.shellfx.storage.base.FileStorageUtils;
import com.techsenger.shellfx.storage.base.StorageListener;
import com.techsenger.toolkit.core.jpms.ServiceUtils;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers the system, recent and custom storage services via {@link java.util.ServiceLoader} and registers their
 * storages with itself through its own public API - a plugin registering its own storage directly (rather than
 * through a discovered provider) calls the very same methods, at a time of its own choosing. The system storages are
 * kept up to date by following the changes of the drives.
 *
 * @author Pavel Castornii
 */
final class FileStorageRegistryImpl extends AbstractItemRegistry<GreyWolfStorage>
        implements FileStorageRegistry {

    private record Change(List<GreyWolfStorage> added, List<GreyWolfStorage> removed) {

    }

    private static final Logger logger = LoggerFactory.getLogger(FileStorageRegistryImpl.class);

    private final List<SystemStorageService> systemProviders;

    private final List<GreyWolfStorage> customStorages = new CopyOnWriteArrayList<>();

    private volatile @Nullable RecentStorage recentStorage;

    private volatile List<GreyWolfStorage> systemStorages = List.of();

    private volatile Map<String, GreyWolfStorage> storagesByUri = Map.of();

    private final Map<GreyWolfStorage, StorageListener> listenersByStorage = new ConcurrentHashMap<>();

    FileStorageRegistryImpl() {
        var layer = FileStorageRegistryImpl.class.getModule().getLayer();
        this.systemProviders = ServiceUtils.loadProviders(layer, false, SystemStorageService.class);
        var recentProviders = ServiceUtils.loadProviders(layer, false, RecentStorageService.class);
        if (recentProviders.size() > 1) {
            throw new IllegalStateException("Only one recent storage is allowed");
        }
        // the recent storage must be set before the system storages are applied, so that they get observed
        this.recentStorage = recentProviders.isEmpty() ? null : recentProviders.get(0).getStorage();
        applySystemStorages(collectSystemStorages());
        // the monitor is followed only after the first application, which it doesn't report, has been made
        DriveMonitor.addListener(change -> {
            Change storageChange;
            synchronized (this) {
                storageChange = applySystemStorages(collectSystemStorages());
            }
            notifyChanged(storageChange.added(), storageChange.removed());
        });
        DriveMonitor.setMode(MonitorMode.SMART_REFRESH);
        // registered last, so that their roots are checked against all the other storages
        for (var service : ServiceUtils.loadProviders(layer, false, CustomStorageService.class)) {
            service.getStorages().forEach(this::registerCustomStorage);
        }
    }

    @Override
    public void refreshSystemStorages() {
        // the registry is not locked here: the monitor holds its own lock while it notifies the listener of the
        // registry, which takes the lock of the registry
        DriveMonitor.refresh();
    }

    @Override
    public @Unmodifiable List<GreyWolfStorage> getSystemStorages() {
        return systemStorages;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<GreyWolfStorage> getPrimaryStorage() {
        var storages = getSystemStorages();
        if (FileStorageUtils.findLocal(storages).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of((GreyWolfStorage) FileStorageUtils.getMain(storages));
    }

    @Override
    public @Unmodifiable List<GreyWolfStorage> getCustomStorages() {
        return Collections.unmodifiableList(customStorages);
    }

    @Override
    public Registration registerCustomStorage(GreyWolfStorage storage) {
        if (storage.getCategory() != StorageCategory.CUSTOM) {
            throw new IllegalArgumentException(storage.getCategory() + " is not supported");
        }
        for (var other : getAllStorages()) {
            if (other.getUri().equals(storage.getUri())) {
                throw new IllegalArgumentException("The root " + storage.getUri() + " of the storage '"
                        + storage.getName() + "' is the root of the storage '" + other.getName() + "' too");
            }
        }
        customStorages.add(storage);
        observeStorage(storage);
        notifyChanged(List.of(storage), List.of());
        return () -> {
            if (customStorages.remove(storage)) {
                unobserveStorage(storage);
                notifyChanged(List.of(), List.of(storage));
            }
        };
    }

    @Override
    public Optional<RecentStorage> getRecentStorage() {
        return Optional.ofNullable(recentStorage);
    }

    @Override
    public @Unmodifiable List<GreyWolfStorage> getAllStorages() {
        var recent = recentStorage;
        var systemAndRecent = recent != null
                ? Stream.concat(getSystemStorages().stream(), Stream.of(recent))
                : getSystemStorages().stream();
        return Stream.concat(systemAndRecent, customStorages.stream())
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Optional<GreyWolfStorage> getStorage(URI uri) {
        var bySystem = storagesByUri.get(uri.toString());
        if (bySystem != null) {
            return Optional.of(bySystem);
        }
        return FileStorageUtils.findByUri(getAllStorages(), uri).map(s -> (GreyWolfStorage) s);
    }

    /**
     * Starts reporting the operations of {@code storage} to the recent storage, which knows no storage itself; a
     * no-op if there is no recent storage or the storage is already followed.
     */
    private void observeStorage(GreyWolfStorage storage) {
        var recent = recentStorage;
        if (recent == null) {
            return;
        }
        listenersByStorage.computeIfAbsent(storage, key -> {
            StorageListener listener = (uri, operation, operationType) ->
                    recent.onOperation(key, uri, operation, operationType);
            key.addListener(listener);
            return listener;
        });
    }

    private SequencedSet<GreyWolfStorage> collectSystemStorages() {
        var result = new LinkedHashSet<GreyWolfStorage>();
        for (var service : systemProviders) {
            result.addAll(service.getStorages());
        }
        return result;
    }

    /**
     * Makes {@code storages} the current system storages: stops following the ones that are gone and follows all the
     * rest. The state is not renewed here, as the caller may not be able to do it in the FX thread yet.
     *
     * @return whether the set of the storages has changed
     */
    private Change applySystemStorages(SequencedSet<GreyWolfStorage> storages) {
        // what stays in the map after the pass is what is gone
        var currentByStorage = new HashMap<GreyWolfStorage, GreyWolfStorage>();
        systemStorages.forEach(storage -> currentByStorage.put(storage, storage));
        var result = new ArrayList<GreyWolfStorage>(storages.size());
        var added = new ArrayList<GreyWolfStorage>();
        for (var storage : storages) {
            var current = currentByStorage.remove(storage);
            if (current == null) {
                added.add(storage);
                result.add(storage);
                logger.debug("System storage is found: {}", storage);
            } else {
                if (current != storage) {
                    logger.warn("A provider gave a new instance of the storage {} that is equal to the current one, "
                            + "which is kept: a provider must give the same instance while nothing has changed",
                            storage);
                }
                // the current instance is the one that is followed and that the clients may hold
                result.add(current);
            }
        }
        currentByStorage.values().forEach(storage -> {
            unobserveStorage(storage);
            logger.debug("System storage is gone: {}", storage);
        });
        result.forEach(this::observeStorage);
        var immutable = List.copyOf(result);
        systemStorages = immutable;
        updateStoragesByUri(immutable);
        return new Change(Collections.unmodifiableList(added), List.copyOf(currentByStorage.values()));
    }

    private void unobserveStorage(GreyWolfStorage storage) {
        var listener = listenersByStorage.remove(storage);
        if (listener != null) {
            storage.removeListener(listener);
        }
    }

    private void updateStoragesByUri(List<GreyWolfStorage> storages) {
        Map<String, GreyWolfStorage> newMap = new HashMap<>(storages.size() * 2);
        for (var s : storages) {
            newMap.put(s.getUri().toString(), s);
        }
        storagesByUri = Collections.unmodifiableMap(newMap);
    }
}

