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
import com.techsenger.greywolf.core.config.LayerConfigData;
import com.techsenger.greywolf.core.config.LayerConfigManager;
import com.techsenger.shellfx.core.config.AbstractConfig;
import com.techsenger.toolkit.core.function.Factory;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Pavel Castornii
 */
public final class LayerConfigManagerImpl implements LayerConfigManager {

    private final @Nullable ModuleLayer coreLayer = getClass().getModule().getLayer();

    private final Map<ModuleLayer, LayerConfigData> dataByLayer = new ConcurrentHashMap<>();

    private final Map<UUID, LayerConfigData> dataByUuid = new ConcurrentHashMap<>();

    @Override
    public void addLayerData(ModuleLayer layer, LayerConfigData data) {
        if (dataByLayer.putIfAbsent(layer, data) != null) {
            throw new IllegalArgumentException("Layer already has config data: " + layer);
        }
        index(data);
    }

    @Override
    public void removeLayerData(ModuleLayer layer) {
        var data = dataByLayer.remove(layer);
        if (data != null) {
            dataByUuid.values().removeIf(indexed -> indexed == data);
        }
    }

    @Override
    public <T extends AbstractConfig> @Nullable T getConfig(Class<T> type) {
        return type.cast(resolve(type).getConfigsByClass().get(type));
    }

    @Override
    public <T extends AbstractConfig> T getOrCreateConfig(Class<T> type, Factory<T> factory) {
        return type.cast(resolve(type).getConfigsByClass().computeIfAbsent(type, key -> factory.create()));
    }

    @Override
    public <T extends AbstractConfig> void putConfig(Class<T> type, T config) {
        resolve(type).getConfigsByClass().put(type, config);
    }

    @Override
    public void removeConfig(Class<? extends AbstractConfig> type) {
        resolve(type).getConfigsByClass().remove(type);
    }

    @Override
    @SuppressWarnings({"unchecked", "TypeParameterUnusedInFormals"})
    public <T extends AbstractConfig> @Nullable T getConfig(UUID uuid) {
        var layerData = dataByUuid.get(uuid);
        return layerData == null ? null : (T) layerData.getConfigsByUuid().get(uuid);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends AbstractConfig> T getOrCreateConfig(UUID uuid, Factory<T> factory) {
        @Nullable T config = getConfig(uuid);
        if (config != null) {
            return config;
        }
        var created = factory.create();
        var layerData = resolve(created.getClass());
        var existing = layerData.getConfigsByUuid().putIfAbsent(uuid, created);
        if (existing != null) {
            return (T) existing;
        }
        dataByUuid.put(uuid, layerData);
        return created;
    }

    @Override
    public void putConfig(UUID uuid, AbstractConfig config) {
        removeConfig(uuid);
        var layerData = resolve(config.getClass());
        layerData.getConfigsByUuid().put(uuid, config);
        dataByUuid.put(uuid, layerData);
    }

    @Override
    public void removeConfig(UUID uuid) {
        var layerData = dataByUuid.remove(uuid);
        if (layerData != null) {
            layerData.getConfigsByUuid().remove(uuid);
        }
    }

    private LayerConfigData resolve(Class<?> type) {
        var layer = type.getModule().getLayer();
        var layerData = layer == null ? null : dataByLayer.get(layer);
        if (layerData == null && coreLayer != null) {
            layerData = dataByLayer.get(coreLayer);
        }
        if (layerData == null) {
            throw new IllegalStateException("There is no layer config data for " + type.getName());
        }
        return layerData;
    }

    private void index(LayerConfigData layerData) {
        layerData.getConfigsByUuid().keySet().forEach(uuid -> dataByUuid.put(uuid, layerData));
    }
}
