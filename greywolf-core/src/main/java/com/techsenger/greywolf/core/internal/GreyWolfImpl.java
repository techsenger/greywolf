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

import com.google.errorprone.annotations.concurrent.LazyInit;
import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.core.GreyWolfSettings;
import com.techsenger.greywolf.core.GreyWolfShellView;
import com.techsenger.greywolf.core.clipboard.GreyWolfClipboard;
import com.techsenger.greywolf.core.config.LayerConfigManager;
import com.techsenger.greywolf.core.internal.plugin.PluginManagerImpl;
import com.techsenger.greywolf.core.project.ProjectInfo;
import com.techsenger.greywolf.core.registries.FileStorageRegistry;
import com.techsenger.shellfx.core.registries.ActionRegistry;
import com.techsenger.shellfx.core.registries.BundleRegistry;
import com.techsenger.shellfx.core.registries.ControlRegistry;
import com.techsenger.shellfx.core.registries.SlotRegistry;
import com.techsenger.shellfx.core.registries.StylesheetRegistry;
import com.techsenger.shellfx.material.style.Density;
import com.techsenger.shellfx.material.theme.AtlantaFxTheme;
import com.techsenger.toolkit.core.version.Version;
import com.techsenger.weaverbird.core.api.Framework;
import com.techsenger.weaverbird.core.api.FrameworkContext;
import com.techsenger.weaverbird.core.api.component.ComponentDescriptor;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.scene.text.Font;

/**
 *
 * @author Pavel Castornii
 */
public class GreyWolfImpl implements GreyWolf {

    private static final Object INSTANCE_KEY = new Object();

    @LazyInit
    private static GreyWolfImpl instance;

    @SuppressWarnings("NullAway")
    public static GreyWolfImpl getInstance(FrameworkContext context) {
        return (GreyWolfImpl) context.getProperties().get(INSTANCE_KEY);
    }

    @SuppressWarnings("NullAway")
    public static GreyWolfImpl getInstance() {
        return instance;
    }

    static void create(Framework framework, ComponentDescriptor componentDescriptor) {
        instance = new GreyWolfImpl(framework, componentDescriptor);
        framework.getContext().getProperties().put(INSTANCE_KEY, instance);
    }

    static void launch() {
        Application.launch(GreyWolfApplication.class);
    }

    private static GreyWolfSettings createSettings() {
        var settings = new GreyWolfSettings();
        var appearance = settings.getAppearance();
        appearance.setRegularFont(Font.font("Inter", 14));
        appearance.setMonospaceFont(Font.font("JetBrains Mono", 14));
        appearance.setTheme(AtlantaFxTheme.CUPERTINO_DARK);
        appearance.setDensity(Density.XS);
        return settings;
    }

    private final PluginManagerImpl pluginManager;

    private final Framework weaverbird;

    private final GreyWolfSettings settings = createSettings();

    private final SlotRegistry slotRegistry = new SlotRegistry();

    private final ControlRegistry controlRegistry = new ControlRegistry();

    private final StylesheetRegistry stylesheetRegistry = new StylesheetRegistry();

    private final ActionRegistry actionRegistry = new ActionRegistry();

    private final BundleRegistry bundleRegistry = new BundleRegistry();

    private final FileStorageRegistry storageRegistry = new FileStorageRegistryImpl();

    private final GreyWolfClipboard clipboard = new GreyWolfClipboardImpl();

    private final LayerConfigManager configManager;

    @LazyInit
    private HostServices hostServices;

    public GreyWolfImpl(Framework weaverbird, ComponentDescriptor componentDescriptor) {
        this.pluginManager = new PluginManagerImpl(this, componentDescriptor);
        this.weaverbird = weaverbird;
        this.configManager = new LayerConfigManagerImpl();
    }

    @Override
    public Version getVersion() {
        return ProjectInfo.getVersion();
    }

    @Override
    public GreyWolfShellView getShell() {
        return GreyWolfApplication.getShell();
    }

    @Override
    public PluginManagerImpl getPluginManager() {
        return pluginManager;
    }

    @Override
    public Framework getWeaverbird() {
        return this.weaverbird;
    }

    @Override
    public GreyWolfSettings getSettings() {
        return this.settings;
    }

    @Override
    public SlotRegistry getSlotRegistry() {
        return this.slotRegistry;
    }

    @Override
    public ControlRegistry getControlRegistry() {
        return this.controlRegistry;
    }

    @Override
    public StylesheetRegistry getStylesheetRegistry() {
        return this.stylesheetRegistry;
    }

    @Override
    public ActionRegistry getActionRegistry() {
        return this.actionRegistry;
    }

    @Override
    public BundleRegistry getBundleRegistry() {
        return this.bundleRegistry;
    }

    @SuppressWarnings("unchecked")
    @Override
    public FileStorageRegistry getFileStorageRegistry() {
        return storageRegistry;
    }

    @Override
    public GreyWolfClipboard getClipboard() {
        return clipboard;
    }

    @Override
    public LayerConfigManager getConfigManager() {
        return this.configManager;
    }

    @Override
    public HostServices getHostServices() {
        return this.hostServices;
    }

    void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }
}
