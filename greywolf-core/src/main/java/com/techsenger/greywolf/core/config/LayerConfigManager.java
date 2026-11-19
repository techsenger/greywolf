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

import com.techsenger.shellfx.core.config.ConfigManager;

/**
 * The only {@link ConfigManager} of the application. It keeps the configs in layer data, one for the layer of each
 * Weaverbird component (the core or a plugin), and finds the data of a config by the layer its class was loaded
 * from, so a config is always kept together with the component that owns its class. A class of a layer that has
 * no data belongs to the data of the core layer, which is added like the data of plugins.
 *
 * <p>A config identified by UUID is found by an index of the UUIDs of all layers, and a new one is kept in the
 * data of its own class, the same as a config identified by class.
 *
 * <p>The manager can be used from any thread, for example the activators of components add and remove their
 * layer data from their own thread.
 *
 * @author Pavel Castornii
 */
public interface LayerConfigManager extends ConfigManager {

    /**
     * Makes the configs of a layer, the core or a plugin, available.
     *
     * @param layer the layer of the component, whose classes the configs belong to.
     * @param data the data of the layer, already read.
     * @throws IllegalArgumentException if {@code layer} already has data.
     */
    void addLayerData(ModuleLayer layer, LayerConfigData data);

    /**
     * Makes the configs of a layer, the core or a plugin, unavailable. All UI components created from the classes
     * of the layer must be closed by then, otherwise a config they request is kept in the data of the core
     * layer, where it could not be read back once the layer is gone.
     *
     * @param layer the layer whose data is removed; the data is written by the caller afterwards.
     */
    void removeLayerData(ModuleLayer layer);
}
