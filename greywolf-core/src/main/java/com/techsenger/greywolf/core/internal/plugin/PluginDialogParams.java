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

package com.techsenger.greywolf.core.internal.plugin;

import com.techsenger.greywolf.core.GreyWolfSettings;
import com.techsenger.greywolf.core.registries.FileStorageRegistry;
import com.techsenger.shellfx.core.config.ConfigManager;
import com.techsenger.shellfx.core.dialog.DialogParams;
import com.techsenger.shellfx.core.window.WindowType;
import java.util.Objects;
import javafx.application.HostServices;

/**
 *
 * @author Pavel Castornii
 */
public class PluginDialogParams extends DialogParams {

    private final PluginManagerImpl pluginManager;

    private final FileStorageRegistry fileStorageRegistry;

    private final GreyWolfSettings settings;

    private final ConfigManager configManager;

    private final HostServices hostServices;

    public PluginDialogParams(PluginDialogConfig config, PluginManagerImpl pluginManager,
            FileStorageRegistry fileStorageRegistry, GreyWolfSettings settings, ConfigManager configManager,
            HostServices hostServices) {
        super(config, WindowType.TOP_LEVEL, settings);
        this.pluginManager = pluginManager;
        this.fileStorageRegistry = fileStorageRegistry;
        this.settings = settings;
        this.configManager = configManager;
        this.hostServices = hostServices;
    }

    public PluginManagerImpl getPluginManager() {
        return pluginManager;
    }

    public FileStorageRegistry getFileStorageRegistry() {
        return fileStorageRegistry;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public HostServices getHostServices() {
        return hostServices;
    }

    @Override
    public PluginDialogConfig getConfig() {
        return (PluginDialogConfig) super.getConfig();
    }

    @Override
    public void validate() {
        super.validate();
        Objects.requireNonNull(getConfig());
        Objects.requireNonNull(pluginManager);
        Objects.requireNonNull(fileStorageRegistry);
        Objects.requireNonNull(configManager);
        Objects.requireNonNull(hostServices);
    }
}
