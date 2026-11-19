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

import com.techsenger.greywolf.core.GreyWolfSettings;
import com.techsenger.greywolf.core.registries.FileStorageRegistry;
import com.techsenger.shellfx.core.area.AreaParams;

/**
 *
 * @author Pavel Castornii
 */
public class PlacesAreaParams extends AreaParams {

    private final FileStorageRegistry storageRegistry;

    private final GreyWolfSettings settings;

    public PlacesAreaParams(FileStorageRegistry storageRegistry, GreyWolfSettings settings) {
        super(null);
        this.storageRegistry = storageRegistry;
        this.settings = settings;
    }

    public FileStorageRegistry getStorageRegistry() {
        return storageRegistry;
    }

    public GreyWolfSettings getSettings() {
        return settings;
    }
}
