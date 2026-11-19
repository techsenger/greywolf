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

import com.google.errorprone.annotations.concurrent.LazyInit;
import com.techsenger.annotations.Nullable;
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.greywolf.core.plugin.Plugin;
import com.techsenger.greywolf.core.registries.FileStorageRegistry;
import com.techsenger.greywolf.core.style.CoreIcons;
import com.techsenger.greywolf.storage.base.GreyWolfFileProvider;
import com.techsenger.greywolf.storage.base.GreyWolfFileRefiner;
import com.techsenger.shellfx.core.close.CloseCheckResult;
import com.techsenger.shellfx.core.close.ClosePreparationResult;
import com.techsenger.shellfx.core.config.ConfigManager;
import com.techsenger.shellfx.core.config.ConfigUtils;
import com.techsenger.shellfx.core.dialog.AbstractDialogViewModel;
import com.techsenger.shellfx.dialogs.alert.AlertDialogButtons;
import com.techsenger.shellfx.dialogs.alert.AlertDialogParams;
import com.techsenger.shellfx.dialogs.alert.AlertDialogType;
import com.techsenger.shellfx.dialogs.progress.ProgressDialogConfig;
import com.techsenger.shellfx.dialogs.progress.ProgressDialogParams;
import com.techsenger.shellfx.dialogs.progress.ProgressDialogPort;
import com.techsenger.shellfx.material.RequestSetter;
import com.techsenger.shellfx.material.UiExecutor;
import com.techsenger.shellfx.storage.base.UriUtils;
import com.techsenger.shellfx.storage.base.async.TwoPhaseSortPolicy;
import com.techsenger.shellfx.storage.ui.chooser.ExtensionFilter;
import com.techsenger.shellfx.storage.ui.chooser.FileChooserDialogButtons;
import com.techsenger.shellfx.storage.ui.chooser.FileChooserDialogConfig;
import com.techsenger.shellfx.storage.ui.chooser.FileChooserDialogParams;
import com.techsenger.shellfx.storage.ui.chooser.FileChooserType;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import com.techsenger.weaverbird.core.api.component.ComponentException;
import com.techsenger.weaverbird.core.api.component.UnknownComponentException;
import com.techsenger.weaverbird.core.api.module.ArtifactEventListener;
import com.techsenger.weaverbird.core.api.module.ModuleArtifact;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public final class PluginDialogViewModel extends AbstractDialogViewModel<PluginDialogComposer> {

    private static final class PluginProgressListener implements PluginListener {

        private final @Nullable ProgressDialogPort progressDialog;

        private volatile int pluginCount;

        private final AtomicInteger processedPluginCount = new AtomicInteger(0);

        @LazyInit
        private volatile Plugin startedPlugin;

        private PluginProgressListener(@Nullable ProgressDialogPort progressDialog) {
            this.progressDialog = progressDialog;
        }

        @Override
        public void onStarted(Plugin plugin) {
            this.startedPlugin = plugin;
        }

        @Override
        public void onFinished(Plugin plugin) {
            if (progressDialog != null) {
                UiExecutor.execute(() -> progressDialog
                            .setProgress((double) processedPluginCount.incrementAndGet() / pluginCount));
            }
        }
    };

    private static final class ModuleProgressListener implements ArtifactEventListener {

        private final ProgressDialogPort progressDialog;

        private final AtomicInteger resolvedModuleCount = new AtomicInteger(0);

        private volatile int moduleCount;

        private ModuleProgressListener(ProgressDialogPort progressDialog) {
            this.progressDialog = progressDialog;
        }

        @LazyInit
        private volatile ModuleArtifact module;

        @Override
        public void onStarted(ModuleArtifact ma) {
            this.module = ma;
        }

        @Override
        public void onFinished(ModuleArtifact ma) {
            double progress = (double) resolvedModuleCount.incrementAndGet() / moduleCount;
            UiExecutor.execute(() -> progressDialog.setProgress(progress));
        }
    };

    private static final Logger logger = LoggerFactory.getLogger(PluginDialogViewModel.class);

    private static final String THIRD_PARTY_WARNING = """
            Third-party plugins run with the same permissions as GreyWolf and can access your files and network. \
            Install plugins only from sources you trust.

            Techsenger assumes no responsibility for any damage, data loss, or security issues caused by \
            third-party plugins. You are responsible for assessing the risks before installing them.

            Continue?""";

    private final PluginManagerImpl pluginManager;

    private final FileStorageRegistry fileStorageRegistry;

    private final ConfigManager configManager;

    private final ObservableList<ObservablePlugin> modifiablePlugins = FXCollections.observableArrayList();

    private final ObservableList<ObservablePlugin> plugins =
            FXCollections.unmodifiableObservableList(modifiablePlugins);

    private final ReadOnlyObjectWrapper<@Nullable ObservablePlugin> plugin = new ReadOnlyObjectWrapper<>();

    private final ObservableSource<@Nullable ObservablePlugin> pluginSource = new SimpleObservableSource<>();

    private final BooleanProperty removeDisabled = new SimpleBooleanProperty();

    private final BooleanProperty installDisabled = new SimpleBooleanProperty();

    private final BooleanProperty uninstallDisabled = new SimpleBooleanProperty();

    private final BooleanProperty activateDisabled = new SimpleBooleanProperty();

    private final BooleanProperty deactivateDisabled = new SimpleBooleanProperty();

    private final ReadOnlyBooleanWrapper selectAll = new ReadOnlyBooleanWrapper();

    private final ReadOnlyDoubleWrapper dividerPosition = new ReadOnlyDoubleWrapper();

    private final ObservableSource<Double> dividerPositionSource = new SimpleObservableSource<>();

    private @Nullable URI archiveDirectory;

    public PluginDialogViewModel(PluginDialogParams params) {
        super(params);
        this.pluginManager = params.getPluginManager();
        this.fileStorageRegistry = params.getFileStorageRegistry();
        this.configManager = params.getConfigManager();
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> cnsmr) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public @Unmodifiable ObservableList<ObservablePlugin> getPlugins() {
        return plugins;
    }

    public @Nullable ObservablePlugin getPlugin() {
        return plugin.get();
    }

    public ReadOnlyObjectProperty<@Nullable ObservablePlugin> pluginProperty() {
        return plugin.getReadOnlyProperty();
    }

    public boolean isRemoveDisabled() {
        return removeDisabled.get();
    }

    public BooleanProperty removeDisabledProperty() {
        return removeDisabled;
    }

    public boolean isInstallDisabled() {
        return installDisabled.get();
    }

    public BooleanProperty installDisabledProperty() {
        return installDisabled;
    }

    public boolean isUninstallDisabled() {
        return uninstallDisabled.get();
    }

    public BooleanProperty uninstallDisabledProperty() {
        return uninstallDisabled;
    }

    public boolean isActivateDisabled() {
        return activateDisabled.get();
    }

    public BooleanProperty activateDisabledProperty() {
        return activateDisabled;
    }

    public boolean isDeactivateDisabled() {
        return deactivateDisabled.get();
    }

    public BooleanProperty deactivateDisabledProperty() {
        return deactivateDisabled;
    }

    public boolean isSelectAll() {
        return selectAll.get();
    }

    public ReadOnlyBooleanProperty selectAllProperty() {
        return selectAll.getReadOnlyProperty();
    }

    public double getDividerPosition() {
        return dividerPosition.get();
    }

    public ReadOnlyDoubleProperty dividerPositionProperty() {
        return dividerPosition.getReadOnlyProperty();
    }

    @RequestSetter
    public void setDividerPosition(double value) {
        dividerPositionSource.next(value);
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setTitle("Plugins");
        setIcon(CoreIcons.PLUGINS);
        setResizable(true);
        setMinHeight(400);
        setMinWidth(600);
        setRightButtons(PluginDialogButtons.CLOSE);
        setOnResult((name) -> {
            if (name == PluginDialogButtons.CLOSE) {
                closeSafely();
            }
        });
        onRefresh();
    }

    @Override
    protected void loadStateFromConfig() {
        super.loadStateFromConfig();
        setDividerPosition(getConfig().getDividerPosition());
    }

    @Override
    protected void observeStateForConfig() {
        super.observeStateForConfig();
        var config = getConfig();
        ConfigUtils.observe(this.dividerPosition, config, PluginDialogConfig::setDividerPosition);
    }

    @Override
    protected PluginDialogConfig getConfig() {
        return (PluginDialogConfig) super.getConfig();
    }

    void onAdd() {
        var alertParams = new AlertDialogParams(getWindowType(), getSettings(), AlertDialogType.CONFIRMATION);
        var confirmDialog = getComposer().openAlertDialog(alertParams);
        confirmDialog.setMessage(THIRD_PARTY_WARNING);
        confirmDialog.setButtonDefault(AlertDialogButtons.NO, true);
        confirmDialog.setOnResult((name) -> {
            confirmDialog.closeSafely();
            if (name == AlertDialogButtons.YES) {
                openArchiveChooser();
            }
        });
        confirmDialog.setVisible(true);
    }

    void onRemove() {
        var progressDialog = createProgressDialog();
        Thread.startVirtualThread(() -> {
            List<ObservablePlugin> pluginsToRemove = new ArrayList<>();
            List<ObservablePlugin> pluginsToUninstall = new ArrayList<>();
            for (var p : modifiablePlugins) {
                if (p.isSelected()) {
                    pluginsToRemove.add(p);
                    if (p.getState() == PluginState.INSTALLED) {
                        pluginsToUninstall.add(p);
                    }
                }
            }
            // The whole non-core layer graph is rebuilt whenever the active set changes - see
            // PluginManagerImpl#startPlugins for why - so every currently activated plugin is deactivated first,
            // then the ones not being removed are redeployed together.
            var currentlyActivePlugins = modifiablePlugins.stream()
                    .filter(p -> p.getState() == PluginState.ACTIVATED).toList();
            var targetActivePlugins = currentlyActivePlugins.stream()
                    .filter(p -> !p.isSelected())
                    .toList();

            int stepCount = (currentlyActivePlugins.isEmpty() ? 0 : 1)
                    + (pluginsToUninstall.isEmpty() ? 0 : 1)
                    + 1
                    + (targetActivePlugins.isEmpty() ? 0 : 1);
            var currentStep = 1;
            try {
                if (!currentlyActivePlugins.isEmpty()) {
                    deactivatePlugins(currentlyActivePlugins, progressDialog, currentStep++, stepCount);
                }
                if (!pluginsToUninstall.isEmpty()) {
                    uninstallPlugins(pluginsToUninstall, progressDialog, currentStep++, stepCount);
                }
                if (!pluginsToRemove.isEmpty()) {
                    removePlugins(pluginsToRemove, progressDialog, currentStep++, stepCount);
                }
                if (!targetActivePlugins.isEmpty()) {
                    activatePlugins(targetActivePlugins, progressDialog, currentStep, stepCount);
                }
            } catch (Exception ex) {
                // empty
            } finally {
                completePluginOperation(progressDialog, true);
            }
        });
    }

    void onInstall() {
        var progressDialog = createProgressDialog();
        Thread.startVirtualThread(() -> {
            List<ObservablePlugin> pluginsToInstall = new ArrayList<>();
            for (var p : modifiablePlugins) {
                if (p.isSelected() && p.getState() == PluginState.AVAILABLE) {
                    pluginsToInstall.add(p);
                }
            }
            try {
                installPlugins(pluginsToInstall, progressDialog, 1, 1);
            } catch (Exception ex) {
                // empty
            } finally {
                completePluginOperation(progressDialog, false);
            }
        });
    }

    void onUninstall() {
        var progressDialog = createProgressDialog();
        Thread.startVirtualThread(() -> {
            List<ObservablePlugin> pluginsToUninstall = new ArrayList<>();
            for (var p : modifiablePlugins) {
                if (p.isSelected()
                        && (p.getState() == PluginState.INSTALLED || p.getState() == PluginState.ACTIVATED)) {
                    pluginsToUninstall.add(p);
                }
            }
            // The whole non-core layer graph is rebuilt whenever the active set changes - see
            // PluginManagerImpl#startPlugins for why - so every currently activated plugin is deactivated first,
            // then the ones not being uninstalled are redeployed together.
            var currentlyActivePlugins = modifiablePlugins.stream()
                    .filter(p -> p.getState() == PluginState.ACTIVATED).toList();
            var targetActivePlugins = currentlyActivePlugins.stream()
                    .filter(p -> !p.isSelected())
                    .toList();

            int stepCount = (currentlyActivePlugins.isEmpty() ? 0 : 1)
                    + (pluginsToUninstall.isEmpty() ? 0 : 1)
                    + (targetActivePlugins.isEmpty() ? 0 : 1);
            var currentStep = 1;
            try {
                if (!currentlyActivePlugins.isEmpty()) {
                    deactivatePlugins(currentlyActivePlugins, progressDialog, currentStep++, stepCount);
                }
                if (!pluginsToUninstall.isEmpty()) {
                    uninstallPlugins(pluginsToUninstall, progressDialog, currentStep++, stepCount);
                }
                if (!targetActivePlugins.isEmpty()) {
                    activatePlugins(targetActivePlugins, progressDialog, currentStep, stepCount);
                }
            } catch (Exception ex) {
                // empty
            } finally {
                completePluginOperation(progressDialog, true);
            }
        });
    }

    void onActivate() {
        var progressDialog = createProgressDialog();
        Thread.startVirtualThread(() -> {
            List<ObservablePlugin> pluginsToInstall = new ArrayList<>();
            List<ObservablePlugin> newlyRequestedPlugins = new ArrayList<>();
            for (var p : modifiablePlugins) {
                if (p.isSelected()) {
                    if (p.getState() == PluginState.AVAILABLE) {
                        pluginsToInstall.add(p);
                        newlyRequestedPlugins.add(p);
                    } else if (p.getState() == PluginState.INSTALLED) {
                        newlyRequestedPlugins.add(p);
                    }
                }
            }
            // The whole non-core layer graph is rebuilt on every activation change - see PluginManagerImpl#startPlugins
            // for why - so every currently activated plugin is deactivated first, then redeployed together with the
            // newly requested ones in one pass.
            var currentlyActivePlugins = modifiablePlugins.stream()
                    .filter(p -> p.getState() == PluginState.ACTIVATED).toList();
            var targetActivePlugins = new ArrayList<>(currentlyActivePlugins);
            targetActivePlugins.addAll(newlyRequestedPlugins);

            int stepCount = (pluginsToInstall.isEmpty() ? 0 : 1)
                    + (currentlyActivePlugins.isEmpty() ? 0 : 1)
                    + (targetActivePlugins.isEmpty() ? 0 : 1);
            var currentStep = 1;
            try {
                if (!pluginsToInstall.isEmpty()) {
                    installPlugins(pluginsToInstall, progressDialog, currentStep++, stepCount);
                }
                if (!currentlyActivePlugins.isEmpty()) {
                    deactivatePlugins(currentlyActivePlugins, progressDialog, currentStep++, stepCount);
                }
                if (!targetActivePlugins.isEmpty()) {
                    activatePlugins(targetActivePlugins, progressDialog, currentStep, stepCount);
                }
            } catch (Exception ex) {
                // empty
            } finally {
                completePluginOperation(progressDialog, true);
            }
        });
    }

    void onDeactivate() {
        var progressDialog = createProgressDialog();
        Thread.startVirtualThread(() -> {
            // The whole non-core layer graph is rebuilt on every activation change - see PluginManagerImpl#startPlugins
            // for why - so every currently activated plugin is deactivated first, then the ones not being removed
            // are redeployed together.
            var currentlyActivePlugins = modifiablePlugins.stream()
                    .filter(p -> p.getState() == PluginState.ACTIVATED).toList();
            var targetActivePlugins = currentlyActivePlugins.stream()
                    .filter(p -> !p.isSelected())
                    .toList();

            int stepCount = (currentlyActivePlugins.isEmpty() ? 0 : 1) + (targetActivePlugins.isEmpty() ? 0 : 1);
            var currentStep = 1;
            try {
                if (!currentlyActivePlugins.isEmpty()) {
                    deactivatePlugins(currentlyActivePlugins, progressDialog, currentStep++, stepCount);
                }
                if (!targetActivePlugins.isEmpty()) {
                    activatePlugins(targetActivePlugins, progressDialog, currentStep, stepCount);
                }
            } catch (Exception ex) {
                // empty
            } finally {
                completePluginOperation(progressDialog, true);
            }
        });
    }

    void onRefresh() {
        try {
            var sortedPlugins = this.pluginManager.getPlugins().stream()
                    .sorted(Comparator.comparing(Plugin::getName, String.CASE_INSENSITIVE_ORDER))
                    .map(p -> (ObservablePlugin) p)
                    .toList();
            modifiablePlugins.setAll(sortedPlugins);
            if (!this.modifiablePlugins.isEmpty()) {
                setPlugin(this.modifiablePlugins.get(0));
            } else {
                setPlugin(null);
            }
        } catch (ComponentException | UnknownComponentException ex) {
            var message = "Error reading plugins";
            logger.error(message, ex);
            var alertParams = new AlertDialogParams(getWindowType(), getSettings(), AlertDialogType.ERROR);
            var dialog = getComposer().openAlertDialog(alertParams);
            dialog.setMessage(message);
            dialog.setVisible(true);
        }
    }

    void onPluginChecked(int index) {
        updateActionButtonsDisabled();
        updateSelectAll();
    }

    void onSelectAll(boolean selected) {
        for (var p : modifiablePlugins) {
            p.setSelected(selected);
        }
        updateActionButtonsDisabled();
        updateSelectAll();
    }

    ReadOnlyObjectWrapper<@Nullable ObservablePlugin> pluginWrapper() {
        return plugin;
    }

    ObservableSource<@Nullable ObservablePlugin> pluginSource() {
        return pluginSource;
    }

    ReadOnlyDoubleWrapper dividerPositionWrapper() {
        return dividerPosition;
    }

    ObservableSource<Double> dividerPositionSource() {
        return dividerPositionSource;
    }

    private void openArchiveChooser() {
        var chooserConfig = configManager.getOrCreateConfig(FileChooserDialogConfig.class,
                FileChooserDialogConfig::new);
        var params = new FileChooserDialogParams<>(chooserConfig, getWindowType(), getSettings(),
                FileChooserType.OPEN, this.fileStorageRegistry::getAllStorages);
        params.setFileProvider(new GreyWolfFileProvider(this.fileStorageRegistry::getAllStorages));
        params.setFileRefiner(new GreyWolfFileRefiner(this.fileStorageRegistry::getAllStorages));
        params.setTwoPhaseSortPolicy(TwoPhaseSortPolicy.AFTER_EACH_PHASE);
        params.setInitialDirectory(archiveDirectory);
        var fileChooser = getComposer().openFileChooser(params);
        var archiveFilter = new ExtensionFilter("Plugin archives", false, "*.zip");
        fileChooser.setExtensionFilters(List.of(archiveFilter, new ExtensionFilter("All files", false, "*.*")));
        fileChooser.setExtensionFilter(archiveFilter);
        fileChooser.setOnResult((name) -> {
            if (name.equals(FileChooserDialogButtons.OK)) {
                var f = fileChooser.getResult();
                fileChooser.closeSafely();
                Thread.startVirtualThread(() -> {
                    try {
                        this.pluginManager.addPlugin(f);
                        UiExecutor.execute(() -> {
                            this.archiveDirectory = UriUtils.getParentUri(f.getStorage().getUri(), f.getUri());
                            onSelectAll(false);
                            onRefresh();
                        });
                    } catch (Exception ex) {
                        var filePath = f.getUri().normalize();
                        logger.error("Error adding {}", filePath, ex);
                        UiExecutor.execute(() -> {
                            var alertParams = new AlertDialogParams(getWindowType(), getSettings(),
                                    AlertDialogType.ERROR);
                            var dialog = getComposer().openAlertDialog(alertParams);
                            dialog.setMessage("Error adding " + filePath + " : " + ex.getMessage());
                            dialog.setVisible(true);
                            onSelectAll(false);
                            onRefresh();
                        });
                    }
                });
            } else if (name.equals(FileChooserDialogButtons.CANCEL)) {
                fileChooser.closeSafely();
            }
        });
        fileChooser.setVisible(true);
    }

    private ProgressDialogPort createProgressDialog() {
        var config = configManager.getOrCreateConfig(ProgressDialogConfig.class, ProgressDialogConfig::new);
        var progressParams = new ProgressDialogParams(config, getWindowType(), getSettings());
        var progressDialog = getComposer().openProgressDialog(progressParams);
        progressDialog.setStepsVisible(true);
        progressDialog.setOnCloseRequest(() -> {
            progressDialog.closeSafely();
            setPlugin(this.plugin.get());
        });
        progressDialog.setVisible(true);
        return progressDialog;
    }

    // No equals-check: ObservablePluginImpl.setState mutates the same instance in place, and this is also used
    // to re-push the current plugin after such a mutation, where the field reference does not change.
    @RequestSetter
    private void setPlugin(@Nullable ObservablePlugin plugin) {
        pluginSource.next(plugin);
    }

    private void updateActionButtonsDisabled() {
        var selectedPlugins = modifiablePlugins.stream().filter(ObservablePlugin::isSelected).toList();
        removeDisabled.set(selectedPlugins.isEmpty());
        installDisabled.set(selectedPlugins.stream().noneMatch(p -> p.getState() == PluginState.AVAILABLE));
        uninstallDisabled.set(selectedPlugins.stream()
                .noneMatch(p -> p.getState() == PluginState.INSTALLED || p.getState() == PluginState.ACTIVATED));
        activateDisabled.set(selectedPlugins.stream()
                .noneMatch(p -> p.getState() == PluginState.AVAILABLE || p.getState() == PluginState.INSTALLED));
        deactivateDisabled.set(selectedPlugins.stream().noneMatch(p -> p.getState() == PluginState.ACTIVATED));
    }

    private void updateSelectAll() {
        selectAll.set(!modifiablePlugins.isEmpty()
                && modifiablePlugins.stream().allMatch(ObservablePlugin::isSelected));
    }

    // This method must be called from a non-JavaFX thread.
    private void removePlugins(List<? extends Plugin> pluginsToRemove, ProgressDialogPort progressDialog,
            int currentStep, int stepCount) throws Exception {
        UiExecutor.execute(() -> {
            progressDialog.setMessage("Removing plugins...");
            progressDialog.setCurrentStep(currentStep);
            progressDialog.setStepCount(stepCount);
            progressDialog.setProgress(0);
        });

        var listener = new PluginProgressListener(progressDialog);
        listener.pluginCount = pluginsToRemove.size();

        try {
            this.pluginManager.removePlugins(pluginsToRemove, listener);
        } catch (ComponentException ex) {
            handleException(ex, "Error removing plugin ", listener);
        }
    }

    // This method must be called from a non-JavaFX thread.
    private void installPlugins(List<? extends Plugin> pluginsToInstall, ProgressDialogPort progressDialog,
            int currentStep, int stepCount) throws Exception {
        UiExecutor.execute(() -> {
            progressDialog.setMessage("Installing plugins...");
            progressDialog.setCurrentStep(currentStep);
            progressDialog.setStepCount(stepCount);
            progressDialog.setProgress(0);
        });

        var pluginListener = new PluginProgressListener(null);
        pluginListener.pluginCount = pluginsToInstall.size();

        var moduleListener = new ModuleProgressListener(progressDialog);
        moduleListener.moduleCount = pluginsToInstall
                .stream().mapToInt(p -> p.getComponentConfig().getModules().size()).sum();

        try {
            this.pluginManager.resolvePlugins(pluginsToInstall, pluginListener, moduleListener);
        } catch (ComponentException ex) {
            handleException(ex, "Error installing plugin ", pluginListener);
        }
    }

    // This method must be called from a non-JavaFX thread.
    private void uninstallPlugins(List<? extends Plugin> pluginsToUninstall, ProgressDialogPort progressDialog,
            int currentStep, int stepCount) throws Exception {
        UiExecutor.execute(() -> {
            progressDialog.setMessage("Uninstalling plugins...");
            progressDialog.setCurrentStep(currentStep);
            progressDialog.setStepCount(stepCount);
            progressDialog.setProgress(0);
        });

        var pluginListener = new PluginProgressListener(null);
        pluginListener.pluginCount = pluginsToUninstall.size();

        var moduleListener = new ModuleProgressListener(progressDialog);
        moduleListener.moduleCount = pluginsToUninstall
                .stream().mapToInt(p -> p.getComponentConfig().getModules().size()).sum();

        try {
            this.pluginManager.unresolvePlugins(pluginsToUninstall, pluginListener, moduleListener);
        } catch (ComponentException ex) {
            handleException(ex, "Error uninstalling plugin ", pluginListener);
        }
    }

    private void activatePlugins(List<? extends Plugin> pluginsToActivate, ProgressDialogPort progressDialog,
            int currentStep, int stepCount) throws Exception {
        UiExecutor.execute(() -> {
            progressDialog.setMessage("Activating plugins...");
            progressDialog.setCurrentStep(currentStep);
            progressDialog.setStepCount(stepCount);
            progressDialog.setProgress(0);
        });

        var pluginListener = new PluginProgressListener(progressDialog);
        pluginListener.pluginCount = pluginsToActivate.size();

        try {
            this.pluginManager.startPlugins(pluginsToActivate, pluginListener);
        } catch (ComponentException | UnknownComponentException  ex) {
            handleException(ex, "Error activating plugin ", pluginListener);
        }
    }

    private void deactivatePlugins(List<? extends Plugin> pluginsToDeactivate, ProgressDialogPort progressDialog,
            int currentStep, int stepCount) throws Exception {
        UiExecutor.execute(() -> {
            progressDialog.setMessage("Deactivating plugins...");
            progressDialog.setCurrentStep(currentStep);
            progressDialog.setStepCount(stepCount);
            progressDialog.setProgress(0);
        });

        var pluginListener = new PluginProgressListener(progressDialog);
        pluginListener.pluginCount = pluginsToDeactivate.size();

        try {
            this.pluginManager.stopPlugins(pluginsToDeactivate, pluginListener);
        } catch (ComponentException | UnknownComponentException  ex) {
            handleException(ex, "Error deactivating plugin ", pluginListener);
        }
    }

    private void handleException(Exception ex, String msg, PluginProgressListener listener) throws Exception {
        var finalMsg = msg + listener.startedPlugin.getComponentConfig().getFullName();
        logger.error(finalMsg, ex);
        UiExecutor.execute(() -> {
            var alertParams = new AlertDialogParams(getWindowType(), getSettings(), AlertDialogType.ERROR);
            var dialog = getComposer().openAlertDialog(alertParams);
            dialog.setMessage(finalMsg + " : " + ex.getMessage());
            dialog.setVisible(true);
        });
        throw new Exception();
    }

    private void completePluginOperation(ProgressDialogPort progressDialog, boolean savePluginRegistry) {
        UiExecutor.execute(() -> {
            progressDialog.closeSafely();
            onSelectAll(false);
            onRefresh();
            // Plugin state is changed on the JavaFX thread, so activated plugins
            // must be written after those changes have been applied.
            if (savePluginRegistry) {
                this.pluginManager.writeActivatedPlugins(modifiablePlugins);
            }
        });
    }
}
