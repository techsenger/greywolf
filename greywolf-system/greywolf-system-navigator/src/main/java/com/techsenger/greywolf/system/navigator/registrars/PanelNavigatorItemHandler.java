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

package com.techsenger.greywolf.system.navigator.registrars;

import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.core.panels.PanelTabDockView;
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfHandler;
import com.techsenger.greywolf.core.registries.FileStorageRegistryUtils;
import com.techsenger.greywolf.system.navigator.NavigatorTabConfig;
import com.techsenger.greywolf.system.navigator.NavigatorTabParams;
import com.techsenger.greywolf.system.navigator.NavigatorTabView;
import com.techsenger.greywolf.system.navigator.NavigatorTabViewModel;
import javafx.event.ActionEvent;

/**
 * Opens a {@link NavigatorTabView} embedded in whichever panel's dock the user clicked the &quot;+&quot;
 * button on &mdash; unlike {@link ShellNavigatorItemHandler}, which opens a standalone, top-level one.
 *
 * @author Pavel Castornii
 */
public class PanelNavigatorItemHandler extends AbstractGreyWolfHandler<ActionEvent> {

    private final PanelTabDockView panelTabDock;

    public PanelNavigatorItemHandler(GreyWolf greyWolf, PanelTabDockView panelTabDock) {
        super(greyWolf);
        this.panelTabDock = panelTabDock;
    }

    @Override
    public void handle(ActionEvent event) {
        var shell = getShell();
        var context = shell.getContext();
        var directory = FileStorageRegistryUtils.resolveInitialDirectory(context.getFileStorageRegistry());

        var config = context.getConfigManager().getOrCreateConfig(NavigatorTabConfig.class,
                NavigatorTabConfig::new);
        var params = new NavigatorTabParams(config, directory);
        var viewModel = new NavigatorTabViewModel(params);
        var view = new NavigatorTabView(viewModel, shell);
        view.initialize();
        panelTabDock.getComposer().addTab(view);
        view.requestFocus();
    }
}
