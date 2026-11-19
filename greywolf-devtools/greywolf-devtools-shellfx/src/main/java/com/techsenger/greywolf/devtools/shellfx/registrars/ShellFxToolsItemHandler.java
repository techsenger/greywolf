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

package com.techsenger.greywolf.devtools.shellfx.registrars;

import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.core.GreyWolfShellView;
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfHandler;
import com.techsenger.connectorfx.LocalConnector;
import com.techsenger.shellfx.core.tab.TabView;
import com.techsenger.shellfx.core.window.WindowType;
import com.techsenger.shellfx.devtools.DevToolsHostType;
import com.techsenger.shellfx.devtools.DevToolsTabDockConfig;
import com.techsenger.shellfx.devtools.DevToolsTabDockParams;
import com.techsenger.shellfx.devtools.DevToolsTabDockView;
import com.techsenger.shellfx.devtools.DevToolsTabDockViewModel;
import com.techsenger.shellfx.devtools.DevToolsWindowConfig;
import com.techsenger.shellfx.devtools.DevToolsWindowParams;
import com.techsenger.shellfx.devtools.DevToolsWindowView;
import com.techsenger.shellfx.devtools.DevToolsWindowViewModel;
import com.techsenger.shellfx.dialogs.alert.AlertDialogParams;
import com.techsenger.shellfx.dialogs.alert.AlertDialogType;
import com.techsenger.shellfx.dialogs.alert.AlertDialogView;
import com.techsenger.shellfx.dialogs.alert.AlertDialogViewModel;
import com.techsenger.shellfx.layout.dockhost.UtilityDockContainerView;
import javafx.event.ActionEvent;
import javafx.stage.Modality;

/**
 *
 * @author Pavel Castornii
 */
public class ShellFxToolsItemHandler extends AbstractGreyWolfHandler<ActionEvent> {

    public ShellFxToolsItemHandler(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void handle(ActionEvent event) {
        var shell = getShell();
        var workspace = shell.getComposer().getWorkspace();
        var tab = workspace.getComposer().getSelectedTab();
        if (tab != null && tab instanceof UtilityDockContainerView<?> c) {
            if (containsDevTools(tab)) {
                showAlreadyOpenAlert(shell);
                return;
            }
            var devTools = createDevToolsDock(shell);
            devTools.getViewModel().setDraggable(true);
            c.getComposer().addUtilityDock(devTools);
        } else {
            var devTools = createDevToolsWindow(shell);
            devTools.getViewModel().setMaximizable(false);
            devTools.getComposer().addTabDock();
            devTools.getViewModel().setVisible(true);
        }
    }

    private boolean containsDevTools(TabView<?> tab) {
        var iterator = tab.getComposer().depthFirstIterator();
        while (iterator.hasNext()) {
            if (iterator.next() instanceof DevToolsTabDockView<?>) {
                return true;
            }
        }
        return false;
    }

    private void showAlreadyOpenAlert(GreyWolfShellView shell) {
        var greyWolf = getGreyWolf();
        var params = new AlertDialogParams(WindowType.TOP_LEVEL, greyWolf.getSettings(), AlertDialogType.INFO);
        var viewModel = new AlertDialogViewModel<>(params);
        var view = new AlertDialogView<>(viewModel, greyWolf.getStylesheetRegistry());
        view.initialize();
        viewModel.setMessage("DevTools are already open in this tab.");
        view.getWindow().initOwner(shell.getWindow());
        view.getWindow().initModality(Modality.APPLICATION_MODAL);
        viewModel.setVisible(true);
    }

    private DevToolsTabDockView<?> createDevToolsDock(GreyWolfShellView shell) {
        var context = shell.getContext();
        var connector = new LocalConnector(shell.getWindow(), null);
        var configManager = context.getConfigManager();
        var config = configManager.getOrCreateConfig(DevToolsTabDockConfig.class, DevToolsTabDockConfig::new);
        var params = new DevToolsTabDockParams(config, DevToolsHostType.SPLIT_SPACE, context.getSettings(),
                connector, shell.getWindow().hashCode());
        var viewModel = new DevToolsTabDockViewModel<>(params);
        var view = new DevToolsTabDockView<>(viewModel, shell, shell);
        view.initialize();
        return view;
    }

    private DevToolsWindowView<?> createDevToolsWindow(GreyWolfShellView shell) {
        var context = shell.getContext();
        var settings = context.getSettings();
        var configManager = context.getConfigManager();
        var config = configManager.getOrCreateConfig(DevToolsWindowConfig.class, DevToolsWindowConfig::new);
        var params = new DevToolsWindowParams(config, settings, configManager);
        var viewModel = new DevToolsWindowViewModel<>(params);
        var view = new DevToolsWindowView<>(viewModel, shell);
        view.initialize();
        return view;
    }
}
