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

import com.techsenger.shellfx.core.FileStore;
import com.techsenger.shellfx.core.config.AbstractConfig;
import com.techsenger.shellfx.core.config.ConfigUtils;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Saves and restores the configs of the layer of a Weaverbird component, the core or a plugin, in its own file:
 * {@link #restore()} reads the file and makes the configs available in the config manager of the GreyWolf,
 * {@link #save()} withdraws them and writes them back.
 *
 * @author Pavel Castornii
 */
final class LayerConfigPersistence {

    private static final Logger logger = LoggerFactory.getLogger(LayerConfigPersistence.class);

    private final ModuleLayer layer;

    private final FileStore<LayerConfigData> file;

    private final LayerConfigManager layerConfigManager;

    /**
     * Creates the persistence of a component.
     *
     * @param layer the layer of the component, whose classes the configs belong to
     * @param file the file of the component, read with the class loader of the component so the classes of its configs
     *     are found
     * @param layerConfigManager the config manager the configs are added to
     */
    LayerConfigPersistence(ModuleLayer layer, FileStore<LayerConfigData> file, LayerConfigManager layerConfigManager) {
        this.layer = layer;
        this.file = file;
        this.layerConfigManager = layerConfigManager;
    }

    /**
     * Reads the configs of the component and adds them to the config manager.
     *
     * @throws IOException if the file cannot be read or holds data of an unexpected type, then nothing
     *     is added
     */
    public void restore() throws IOException {
        file.restore();
        try {
            layerConfigManager.addLayerData(layer, file.getData());
        } catch (ClassCastException ex) {
            throw new IOException("Config file " + file.getPath() + " holds data of an unexpected type", ex);
        }
    }

    /**
     * Withdraws the configs of the component from the config manager and writes them to the file. First warns about
     * every listener that is still registered on a config: by then the components are gone, so such a listener is a
     * leak.
     *
     * @throws IOException if the file cannot be written
     */
    public void save() throws IOException {
        layerConfigManager.removeLayerData(layer);
        var layerData = file.getData();
        if (logger.isWarnEnabled()) {
            warnAboutListeners(layerData.getConfigsByClass());
            warnAboutListeners(layerData.getConfigsByUuid());
        }
        file.save();
    }

    private <K> void warnAboutListeners(Map<K, AbstractConfig> configs) {
        configs.forEach((key, config) -> {
            var listenerClasses = ConfigUtils.getListenerClasses(config);
            if (!listenerClasses.isEmpty() && logger.isWarnEnabled()) {
                logger.warn("Config {} ({}) is saved while listeners are still registered on it, probably a leak: {}",
                        key, config.getClass().getName(), listenerClasses.stream().map(Class::getName).toList());
            }
        });
    }
}
