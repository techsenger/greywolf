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

package com.techsenger.greywolf.boot;

import com.techsenger.greywolf.boot.core.FileConstants;
import com.techsenger.toolkit.core.os.OperatingSystem;
import com.techsenger.toolkit.core.os.OsUtils;
import com.techsenger.weaverbird.core.api.ComponentManager;
import com.techsenger.weaverbird.core.api.Framework;
import com.techsenger.weaverbird.core.api.FrameworkFactory;
import com.techsenger.weaverbird.core.api.FrameworkSettings;
import com.techsenger.weaverbird.core.api.SystemProperties;
import com.techsenger.weaverbird.core.api.component.ComponentConfig;
import com.techsenger.weaverbird.core.api.component.ComponentException;
import com.techsenger.weaverbird.core.api.component.UnknownComponentException;
import com.techsenger.weaverbird.core.api.module.ArtifactEventListener;
import com.techsenger.weaverbird.core.api.module.ModuleArtifact;
import com.techsenger.weaverbird.core.api.registry.ComponentEntry;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Starts the GreyWolf: shows the splash screen, starts the framework and the core, and, once the core has shown
 * the application, starts the plugins.
 *
 * <p>The control passes between the boot and the core three times, because the splash screen and the plugins belong
 * to the boot, while the window and its unblocking belong to the core, and the plugins must be started only after
 * the window is shown:
 * <ol>
 * <li>The boot starts the core component and waits for {@code applicationStarted}.</li>
 * <li>The core shows the window and calls {@link #complete()}, which completes {@code applicationStarted}: it
 *     instructs the boot to finish loading whatever is not loaded yet.</li>
 * <li>The boot starts the plugins, closes the splash screen and completes {@code completed}, the future that the
 *     core received from {@link #complete()}, so the core unblocks the window.</li>
 * </ol>
 *
 * @author Pavel Castornii
 */
public final class Boot {

    private static final Logger logger = LoggerFactory.getLogger(Boot.class);

    private static final String CORE_COMPONENT = "greywolf-core";

    private static volatile Boot instance;

    private SplashScreen splashScreen;

    private Framework framework;

    private ComponentConfig coreConfig;

    private List<ComponentEntry> activatedPlugins;

    private boolean resolveRequired;

    private volatile double progressStep = 0.0;

    private volatile double progress = 0.0;

    private final CompletableFuture<Void> applicationStarted = new CompletableFuture<>();

    private final CompletableFuture<Void> completed = new CompletableFuture<>();

    public static void main(String[] args) {
        instance = new Boot();
        instance.start();
    }

    /**
     * Instructs the boot to finish loading whatever is not loaded yet, that is, to start the plugins. Intended to
     * be called by the core once the application is shown; calls after the first one only return the future.
     *
     * @return a future completed when the boot is finished; already completed if the boot is not running
     */
    public static CompletableFuture<Void> complete() {
        var boot = instance;
        if (boot == null) {
            return CompletableFuture.completedFuture(null);
        }
        boot.applicationStarted.complete(null);
        return boot.completed;
    }

    private void start() {
        try {
            // starting framework
            this.splashScreen = new SplashScreen();
            createFramework();
            startRepo();

            // starting core
            prepareCoreInfo();
            preparePluginInfo();
            calculateProgressSteps();
            resolveCore();
            splashScreen.update("Activating " + CORE_COMPONENT);
            var componentManager = framework.getComponentManager();
            componentManager.startComponent(CORE_COMPONENT, ProjectInfo.getVersion());
            applicationStarted.get();
            splashScreen.update(advanceProgress());

            // starting plugins
            startPlugins(componentManager);
        } catch (Throwable t) {
            logger.error("Failed to start", t);
        } finally {
            splashScreen.close();
            completed.complete(null);
            instance = null;
        }
    }

    private void createFramework() {
        var rootPath = Paths.get(System.getProperty(SystemProperties.ROOT_PATH));
        var settings = FrameworkSettings.builder()
                .repoChecksumEnabled(false)
                .application(app -> app
                    .name("greywolf-boot")
                    .version(ProjectInfo.getVersion()))
                .build();
        framework = FrameworkFactory.create(settings, rootPath);
    }

    private void startRepo() throws ComponentException, UnknownComponentException {
        framework.getComponentManager().startComponent("weaverbird-repo", framework.getVersion());
    }

    private void prepareCoreInfo() throws ComponentException, UnknownComponentException {
        var compManager = framework.getComponentManager();
        this.coreConfig = compManager.readConfig(CORE_COMPONENT, ProjectInfo.getVersion());
        this.resolveRequired = !framework.getRegistry().isComponentResolved(CORE_COMPONENT, ProjectInfo.getVersion());
        var os = OsUtils.getOs();
        if (os != null) {
            if (os == OperatingSystem.WINDOWS) {
                compManager.getConfigInfo().put("javafx.classifier", "win");
            } else {
                compManager.getConfigInfo().put("javafx.classifier", os.toString().toLowerCase(Locale.ROOT));
            }
        }
    }

    private void preparePluginInfo() throws Exception {
        var configDirPath = framework.getPathManager().getPathResolver()
                .resolveDataDirectory(CORE_COMPONENT, ProjectInfo.getVersion());
        var xmlFilePath = configDirPath.resolve(FileConstants.ACTIVATED_PLUGINS);
        if (Files.exists(xmlFilePath)) {
            var reader = new ActivatedPluginsReader();
            activatedPlugins = reader.read(xmlFilePath);
        } else {
            activatedPlugins = Collections.emptyList();
        }
    }

    private void calculateProgressSteps() {
        // Each module download and each component activation are treated as
        // equal steps, as they take approximately the same amount of time
        int moduleCount = resolveRequired ? coreConfig.getModules().size() : 0;
        int componentCount = activatedPlugins.size() + 1; // +1 for core
        int totalSteps = moduleCount + componentCount;
        double step = 1.0 / totalSteps;
        this.progressStep = step;
    }

    private void resolveCore() throws ComponentException, UnknownComponentException {
        if (!resolveRequired) {
            return;
        }
        var listener = new ArtifactEventListener() {

            @Override
            public void onStarted(ModuleArtifact ma) { }

            @Override
            public void onFinished(ModuleArtifact ma) {
                splashScreen.update(advanceProgress());
            }
        };
        splashScreen.update("Installing " + CORE_COMPONENT);
        framework.getComponentManager().resolveComponent(CORE_COMPONENT, ProjectInfo.getVersion(), listener);
    }

    private void startPlugins(ComponentManager componentManager) throws Exception {
        if (activatedPlugins.isEmpty()) {
            return;
        }
        var configs = new ArrayList<ComponentConfig>();
        for (var p : activatedPlugins) {
            configs.add(componentManager.readConfig(p.getName(), p.getVersion()));
        }
        componentManager.executeExclusively(() -> {
            var plan = componentManager.createDeployPlan(configs);
            for (var entry : plan.getEntries()) {
                splashScreen.update("Activating " + entry.getConfig().getName());
                componentManager.startComponent(entry.getConfig(), null, entry.getParentIds(), null, false);
                splashScreen.update(advanceProgress());
            }
        });
    }

    private synchronized double advanceProgress() {
        progress += progressStep;
        return progress;
    }

    private Boot() {
        // empty
    }
}
