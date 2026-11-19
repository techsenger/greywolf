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

import com.techsenger.greywolf.core.GreyWolfSettings;
import com.techsenger.greywolf.storage.base.GreyWolfStorage;
import com.techsenger.greywolf.storage.base.GreyWolfStorageProvider;
import com.techsenger.shellfx.base.registry.ItemRegistry;
import com.techsenger.shellfx.core.area.AreaParams;

/**
 *
 * @author Pavel Castornii
 */
public class DirectoryTreeParams extends AreaParams {

    private final GreyWolfStorageProvider storageProvider;

    private final ItemRegistry<GreyWolfStorage> storageRegistry;

    private final GreyWolfSettings settings;

    /**
     * Creates params.
     *
     * @param storageProvider tells the storages the tree shows, each time they are needed
     * @param storageRegistry tells when the storages change, so the tree asks the provider again
     * @param settings the settings
     */
    public DirectoryTreeParams(GreyWolfStorageProvider storageProvider,
            ItemRegistry<GreyWolfStorage> storageRegistry, GreyWolfSettings settings) {
        super(null);
        this.storageProvider = storageProvider;
        this.storageRegistry = storageRegistry;
        this.settings = settings;
    }

    public GreyWolfStorageProvider getStorageProvider() {
        return storageProvider;
    }

    public ItemRegistry<GreyWolfStorage> getStorageRegistry() {
        return storageRegistry;
    }

    public GreyWolfSettings getSettings() {
        return settings;
    }
}
