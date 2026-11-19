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

package com.techsenger.greywolf.core;

import com.techsenger.greywolf.boot.core.BootService;
import com.techsenger.greywolf.core.internal.plugin.PluginState;
import com.techsenger.shellfx.core.DefaultShellViewModel;
import com.techsenger.shellfx.material.UiExecutor;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import com.techsenger.weaverbird.core.api.component.ComponentException;
import com.techsenger.weaverbird.core.api.component.UnknownComponentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public final class GreyWolfShellViewModel extends DefaultShellViewModel<GreyWolfShellComposer>
        implements GreyWolfShellPort {

    private static final Logger logger = LoggerFactory.getLogger(GreyWolfShellViewModel.class);

    private final ObservableSource<Void> upgradeMenuBarSource = new SimpleObservableSource<>();

    public GreyWolfShellViewModel(GreyWolfShellParams params) {
        super(params);
    }

    @Override
    public GreyWolfShellPort.ComposerAccess getComposerAccess() {
        return (GreyWolfShellPort.ComposerAccess) super.getComposerAccess();
    }

    @Override
    public GreyWolfShellContextAccess getContextAccess() {
        return (GreyWolfShellContextAccess) super.getContextAccess();
    }

    @Override
    public void onPluginsUpdated() {
        upgradeMenuBarSource.next(null);
        getComposer().getWorkspacePort().onPluginsUpdated();
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setBlocked(true);
    }

    /**
     * Called by the view once the stage has actually been shown - waits for {@link BootService} to complete
     * before updating the menu bar and the workspace and unblocking the shell, so the window never briefly displays
     * with a stale menu or before plugin-contributed controls have finished registering. If no plugin is activated
     * after that (most likely the first start), asks the view to open the plugins dialog.
     */
    void onShown() {
        BootService.complete().thenRun(() -> {
            var hasActivatedPlugins = hasActivatedPlugins();
            UiExecutor.execute(() -> {
                onPluginsUpdated();
                setBlocked(false);
                if (!hasActivatedPlugins) {
                    getComposer().openPluginsDialog();
                }
            });
        });
    }

    ObservableSource<Void> upgradeMenuBarSource() {
        return upgradeMenuBarSource;
    }

    private boolean hasActivatedPlugins() {
        try {
            return getContextAccess().getPluginManager().getPlugins().stream()
                    .anyMatch(p -> p.getState() == PluginState.ACTIVATED);
        } catch (ComponentException | UnknownComponentException ex) {
            logger.error("Error reading plugins", ex);
            return true;
        }
    }
}
