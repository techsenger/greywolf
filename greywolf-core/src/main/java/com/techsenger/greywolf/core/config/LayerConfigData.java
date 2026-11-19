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

package com.techsenger.greywolf.core.config;

import com.techsenger.shellfx.core.StoreData;
import com.techsenger.shellfx.core.config.AbstractConfig;
import java.io.Serial;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The configs of the layer of one Weaverbird component, the core or a plugin, in the form they are written to its file:
 * the root object of the file. The configs stay in their own data, so they disappear together with the component that
 * owns their classes.
 *
 * <p>The maps are safe for concurrent use. Once the data is added to the config manager, its maps are changed only by
 * the config manager, which keeps an index of the UUIDs of the layers.
 *
 * @author Pavel Castornii
 */
public final class LayerConfigData implements StoreData {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Map<Class<? extends AbstractConfig>, AbstractConfig> configsByClass;

    private final Map<UUID, AbstractConfig> configsByUuid;

    LayerConfigData() {
        this.configsByClass = new ConcurrentHashMap<>();
        this.configsByUuid = new ConcurrentHashMap<>();
    }

    @Override
    public boolean isEmpty() {
        return configsByClass.isEmpty() && configsByUuid.isEmpty();
    }

    @Override
    public String toString() {
        return "LayerConfigData [configsByClass: " + configsByClass + ", configsByUuid: " + configsByUuid + ']';
    }

    /**
     * Returns the live map of the configs identified by their class.
     */
    public Map<Class<? extends AbstractConfig>, AbstractConfig> getConfigsByClass() {
        return configsByClass;
    }

    /**
     * Returns the live map of the configs identified by the UUID of a component instance.
     */
    public Map<UUID, AbstractConfig> getConfigsByUuid() {
        return configsByUuid;
    }
}
