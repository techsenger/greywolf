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

import com.techsenger.greywolf.core.GreyWolfSettings;
import com.techsenger.greywolf.storage.base.GreyWolfStorageProvider;
import com.techsenger.shellfx.core.area.AreaParams;

/**
 *
 * @author Pavel Castornii
 */
public class LocationBarParams extends AreaParams {

    private final GreyWolfStorageProvider storageProvider;

    private final GreyWolfSettings settings;

    public LocationBarParams(GreyWolfStorageProvider storageProvider, GreyWolfSettings settings) {
        super(null);
        this.storageProvider = storageProvider;
        this.settings = settings;
    }

    public GreyWolfStorageProvider getStorageProvider() {
        return storageProvider;
    }

    public GreyWolfSettings getSettings() {
        return settings;
    }
}
