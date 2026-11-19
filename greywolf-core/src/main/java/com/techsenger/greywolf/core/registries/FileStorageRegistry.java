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

package com.techsenger.greywolf.core.registries;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.greywolf.storage.base.GreyWolfStorage;
import com.techsenger.greywolf.storage.base.RecentStorage;
import com.techsenger.greywolf.storage.base.StorageCategory;
import com.techsenger.shellfx.base.registry.ItemRegistry;
import com.techsenger.shellfx.base.registry.Registration;
import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * A registry of storages available: system storages discovered from the operating system and custom ones registered
 * at runtime. The roots of the storages never overlap, so a URI belongs to one storage.
 *
 * @author Pavel Castornii
 */
public interface FileStorageRegistry extends ItemRegistry<GreyWolfStorage> {

    /**
     * Searches the drives of the current machine now and brings the system storages up to date. The registry follows
     * the drives on its own, but a change that the system does not show, such as a new label of a drive, is noticed
     * only here. It must not be called by a thread that holds a lock the registry may take.
     */
    void refreshSystemStorages();

    /**
     * Returns all system storages discovered from the operating system, reflecting the last completed
     * discovery; the registry follows the drives, and {@code refreshSystemStorages()} makes sure the result is current.
     *
     * @return an unmodifiable list, never {@code null}, may be empty
     */
    @Unmodifiable List<GreyWolfStorage> getSystemStorages();

    /**
     * Returns the primary system storage, the one that holds the root of the operating system.
     *
     * @return the primary storage, or empty if none exists
     */
    Optional<GreyWolfStorage> getPrimaryStorage();

    /**
     * Returns all custom storages currently registered in this registry, i.e. storages registered
     * programmatically rather than discovered from the operating system.
     *
     * @return an unmodifiable list, never {@code null}, may be empty
     */
    @Unmodifiable List<GreyWolfStorage> getCustomStorages();

    /**
     * Registers a custom storage with this registry.
     *
     * @param storage the storage to register, must not be {@code null}
     * @return a registration handle used to remove the storage from the registry, never {@code null}
     * @throws IllegalArgumentException if {@code storage.getCategory()} isn't {@link StorageCategory#CUSTOM} -
     *     a system storage registers itself through {@link #refreshSystemStorages()} instead, and the
     *     recent-files storage is never registered as custom at all, only retrievable via
     *     {@link #getRecentStorage()}; or if the root of the storage is the same as the root of a registered
     *     storage - then a URI couldn't be told to belong to one of them. Nested roots are allowed.
     */
    Registration registerCustomStorage(GreyWolfStorage storage);

    /**
     * Returns the recent-files storage discovered at construction time.
     *
     * @return the recent-files storage, or empty if none was discovered (e.g. the module providing it isn't
     *     installed).
     */
    Optional<RecentStorage> getRecentStorage();

    /**
     * Returns all available storages: system storages, then the recent-files storage (if any), then custom
     * storages; call {@code refreshSystemStorages()} first if up-to-date information about system storages is
     * required.
     *
     * @return an unmodifiable list, never {@code null}, may be empty
     */
    @Unmodifiable List<GreyWolfStorage> getAllStorages();

    /**
     * Returns the storage that owns the given URI among all storages currently registered. When roots are nested,
     * as a volume is mounted into a directory of another one, the storage with the longest root owns the URI.
     *
     * @param uri the URI to resolve, must not be {@code null}
     * @return the matching storage, or empty if no storage matches
     */
    Optional<GreyWolfStorage> getStorage(URI uri);
}
