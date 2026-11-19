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

package com.techsenger.greywolf.core.registrars;

import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.core.internal.plugin.PluginDialogConfig;
import com.techsenger.greywolf.core.internal.plugin.PluginDialogParams;
import com.techsenger.greywolf.core.internal.plugin.PluginDialogView;
import com.techsenger.greywolf.core.internal.plugin.PluginDialogViewModel;
import com.techsenger.greywolf.core.internal.plugin.PluginManagerImpl;
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfHandler;
import com.techsenger.shellfx.core.window.WindowType;
import com.techsenger.shellfx.dialogs.alert.AlertDialogParams;
import com.techsenger.shellfx.dialogs.alert.AlertDialogType;
import com.techsenger.shellfx.dialogs.alert.AlertDialogView;
import com.techsenger.shellfx.dialogs.alert.AlertDialogViewModel;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 *
 * @author Pavel Castornii
 */
public class PluginsItemHandler extends AbstractGreyWolfHandler<ActionEvent> {

    public PluginsItemHandler(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void handle(ActionEvent event) {
        openDialog();
    }

    /**
     * Opens the modal plugins dialog over the shell. Plugins can be added and removed only while the workspace has
     * no tabs and the shell is the only open window, so otherwise a warning is shown instead.
     */
    public void openDialog() {
        var greyWolf = getGreyWolf();
        var shellStage = getShell().getWindow();
        var workspacePort = getShell().getComposer().getWorkspacePort();
        var hasTabs = workspacePort.getComposerAccess().getTabCount() > 0;
        var otherWindowTitles = Window.getWindows().stream()
                .filter(w -> w instanceof Stage && w.isShowing() && w != shellStage)
                .map(w -> ((Stage) w).getTitle())
                .map(t -> t == null || t.isBlank() ? "Untitled" : t)
                .toList();
        if (hasTabs || !otherWindowTitles.isEmpty()) {
            showBlockedAlert(hasTabs, otherWindowTitles);
            return;
        }
        var config = greyWolf.getConfigManager().getOrCreateConfig(PluginDialogConfig.class,
                PluginDialogConfig::new);
        var params = new PluginDialogParams(config, (PluginManagerImpl) greyWolf.getPluginManager(),
                greyWolf.getFileStorageRegistry(), greyWolf.getSettings(), greyWolf.getConfigManager());
        var viewModel = new PluginDialogViewModel(params);
        var view = new PluginDialogView(viewModel, greyWolf.getStylesheetRegistry());
        view.initialize();
        view.getWindow().initOwner(greyWolf.getShell().getWindow());
        view.getWindow().initModality(Modality.APPLICATION_MODAL);
        viewModel.setVisible(true);
    }

    private void showBlockedAlert(boolean hasTabs, List<String> windowTitles) {
        var parts = new ArrayList<String>();
        if (hasTabs) {
            parts.add("all tabs");
        }
        if (!windowTitles.isEmpty()) {
            parts.add("secondary windows (" + String.join(", ", windowTitles) + ")");
        }
        var greyWolf = getGreyWolf();
        var params = new AlertDialogParams(WindowType.TOP_LEVEL, greyWolf.getSettings(), AlertDialogType.WARNING);
        var viewModel = new AlertDialogViewModel<>(params);
        var view = new AlertDialogView<>(viewModel, greyWolf.getStylesheetRegistry());
        view.initialize();
        viewModel.setMessage("Please close " + String.join(" and ", parts) + " before opening the Plugins dialog.");
        view.getWindow().initOwner(getShell().getWindow());
        view.getWindow().initModality(Modality.APPLICATION_MODAL);
        viewModel.setVisible(true);
    }
}
