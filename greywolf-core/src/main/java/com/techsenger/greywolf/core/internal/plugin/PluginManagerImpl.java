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

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.greywolf.boot.core.FileConstants;
import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.core.plugin.Plugin;
import com.techsenger.greywolf.core.plugin.PluginManager;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.shellfx.storage.base.OperationType;
import com.techsenger.weaverbird.core.api.component.ComponentConfig;
import com.techsenger.weaverbird.core.api.component.ComponentDescriptor;
import com.techsenger.weaverbird.core.api.component.ComponentException;
import com.techsenger.weaverbird.core.api.component.UnknownComponentException;
import com.techsenger.weaverbird.core.api.module.ArtifactEventListener;
import com.techsenger.weaverbird.core.api.registry.ComponentEntry;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public class PluginManagerImpl implements PluginManager {

    private static final String PLUGIN_TYPE = "plugin";

    private static final Logger logger = LoggerFactory.getLogger(PluginManagerImpl.class);

    private final GreyWolf greyWolf;

    private final ComponentDescriptor componentDescriptor;

    public PluginManagerImpl(GreyWolf greyWolf, ComponentDescriptor descriptor) {
        this.greyWolf = greyWolf;
        this.componentDescriptor = descriptor;
    }

    @Override
    public @Unmodifiable List<Plugin> getPlugins() throws ComponentException, UnknownComponentException {
        var framework = greyWolf.getWeaverbird();
        List<ComponentConfig> configs = new ArrayList<>();
        for (var entry : framework.getRegistry().getAddedComponents()) {
            var config = framework.getComponentManager().readConfig(entry.getName(), entry.getVersion());
            configs.add(config);
        }

        Set<ComponentEntry> activatedComponents = framework.getComponentManager().getComponents().stream()
                .map(c -> new ComponentEntry(c.getDescriptor().getConfig()))
                .collect(Collectors.toSet());
        return configs.stream()
                .filter(c -> PLUGIN_TYPE.equals(c.getType() == null ? null : c.getType().toLowerCase(Locale.ROOT)))
                .map(c -> (Plugin) new ObservablePluginImpl(c, resolvePluginState(activatedComponents, c)))
                .toList();
    }

    /**
     * Deploys and activates every plugin in {@code pluginsToActivate}, in an order resolved from their declared
     * parents. {@code pluginsToActivate} must be the full set of plugins that should end up activated, not just
     * the newly requested ones - see {@link #stopPlugins} for why.
     */
    public void startPlugins(List<? extends Plugin> pluginsToActivate, PluginListener pluginListener)
            throws ComponentException, UnknownComponentException {
        try {
            var componentManager = greyWolf.getWeaverbird().getComponentManager();
            if (!pluginsToActivate.isEmpty()) {
                var pluginsByConfig = pluginsToActivate.stream()
                        .collect(Collectors.toMap(Plugin::getComponentConfig, p -> p));
                componentManager.executeExclusively(() -> {
                    var configs = pluginsToActivate.stream().map(Plugin::getComponentConfig).toList();
                    var plan = componentManager.createDeployPlan(configs);
                    for (var entry : plan.getEntries()) {
                        var plugin = Objects.requireNonNull(pluginsByConfig.get(entry.getConfig()));
                        pluginListener.onStarted(plugin);
                        componentManager.startComponent(entry.getConfig(), null, entry.getParentIds(), null, false);
                        Platform.runLater(() -> ((ObservablePlugin) plugin).setState(PluginState.ACTIVATED));
                        pluginListener.onFinished(plugin);
                    }
                });
            }
        } finally {
            Platform.runLater(() -> greyWolf.getShell().getViewModel().onPluginsUpdated());
        }
    }

    /**
     * Adds a plugin from its archive file. The file is read from its storage, so it is copied into a temporary file,
     * as the framework needs a file to read the archive from; the temporary file is deleted afterwards.
     *
     * @param archive the file of the plugin archive, located in any storage
     * @throws ComponentException if the archive is not a valid plugin archive
     * @throws IOException if the archive cannot be read or the temporary file cannot be written
     */
    void addPlugin(GreyWolfFile archive) throws ComponentException, IOException {
        var tempDirectory = greyWolf.getWeaverbird().getPathManager().getTempDirectory();
        Files.createDirectories(tempDirectory);
        var archivePath = Files.createTempFile(tempDirectory, "plugin-", ".zip");
        try {
            try (var in = archive.getStorage().openInputStream(archive.getUri(), OperationType.PRIMARY)) {
                Files.copy(in, archivePath, StandardCopyOption.REPLACE_EXISTING);
            }
            greyWolf.getWeaverbird().getComponentManager().addComponent(archivePath);
        } finally {
            Files.deleteIfExists(archivePath);
        }
    }

    void resolvePlugins(List<? extends Plugin> pluginsToResolve, PluginListener pluginListener, ArtifactEventListener
            moduleListener) throws ComponentException {
        for (var p : pluginsToResolve) {
            pluginListener.onStarted(p);
            greyWolf.getWeaverbird().getComponentManager().resolveComponent(p.getComponentConfig(), moduleListener);
            Platform.runLater(() -> ((ObservablePlugin) p).setState(PluginState.INSTALLED));
            pluginListener.onFinished(p);
        }
    }

    /**
     * Deactivates every plugin in {@code pluginsToDeactivate}. Since {@link #startPlugins} always rebuilds the
     * whole non-core layer graph from scratch (an already-deployed component's {@code ModuleLayer} cannot be
     * given a new parent after the fact), callers that go on to redeploy a different active set must pass every
     * currently activated plugin here first, not just the ones actually being removed.
     */
    void stopPlugins(List<? extends Plugin> pluginsToDeactivate, PluginListener pluginListener)
            throws ComponentException, UnknownComponentException {
        try {
            var fw = greyWolf.getWeaverbird();
            if (!pluginsToDeactivate.isEmpty()) {
                var componentsByNameVer = fw.getComponentManager().getComponents()
                        .stream()
                        .collect(Collectors.toMap(c -> new ComponentEntry(c.getDescriptor().getConfig()), c -> c));
                for (var p : pluginsToDeactivate) {
                    pluginListener.onStarted(p);
                    var component = componentsByNameVer.get(new ComponentEntry(p.getComponentConfig()));
                    if (component != null) {
                        fw.getComponentManager().stopComponent(component.getDescriptor());
                    }
                    Platform.runLater(() -> ((ObservablePlugin) p).setState(PluginState.INSTALLED));
                    pluginListener.onFinished(p);
                }
            }
        } finally {
            Platform.runLater(() -> greyWolf.getShell().getViewModel().onPluginsUpdated());
        }
    }

    void unresolvePlugins(List<? extends Plugin> pluginsToUnresolve, PluginListener pluginListener,
            ArtifactEventListener moduleListener)
            throws ComponentException {
        for (var p : pluginsToUnresolve) {
            pluginListener.onStarted(p);
            greyWolf.getWeaverbird().getComponentManager().unresolveComponent(p.getComponentConfig(), moduleListener);
            Platform.runLater(() -> ((ObservablePlugin) p).setState(PluginState.ACTIVATED));
            pluginListener.onFinished(p);
        }
    }

    void removePlugins(List<? extends Plugin> pluginsToRemove, PluginListener pluginListener)
            throws ComponentException {
        for (var p : pluginsToRemove) {
            pluginListener.onStarted(p);
            greyWolf.getWeaverbird().getComponentManager().removeComponent(p.getComponentConfig());
            pluginListener.onFinished(p);
        }
    }

    void writeActivatedPlugins(List<? extends Plugin> plugins) {
        var writer = new ActivatedPluginsWriter();
        var xmlFilePath = getActivatedPluginsPath();
        try {
            Files.createDirectories(xmlFilePath.getParent());
            writer.write(plugins, xmlFilePath);
            logger.debug("Wrote activated plugins to {}", xmlFilePath);
        } catch (Exception ex) {
            logger.error("Error writing activated plugins to {}", xmlFilePath, ex);
        }
    }

    private PluginState resolvePluginState(Set<ComponentEntry> activatedComponents, ComponentConfig config) {
        var framework = greyWolf.getWeaverbird();
        if (framework.getRegistry().isComponentResolved(config.getName(), config.getVersion())) {
            if (activatedComponents.contains(new ComponentEntry(config))) {
                return PluginState.ACTIVATED;
            } else {
                return PluginState.INSTALLED;
            }
        } else {
            return PluginState.AVAILABLE;
        }
    }

    private Path getActivatedPluginsPath() {
        var configDirPath = greyWolf.getWeaverbird().getPathManager().getPathResolver()
                .resolveDataDirectory(componentDescriptor.getConfig());
        var xmlFilePath = configDirPath.resolve(FileConstants.ACTIVATED_PLUGINS);
        return xmlFilePath;
    }
}
