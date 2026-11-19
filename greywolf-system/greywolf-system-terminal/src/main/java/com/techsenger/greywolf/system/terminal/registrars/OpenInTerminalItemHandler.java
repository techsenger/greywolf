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

package com.techsenger.greywolf.system.terminal.registrars;

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfHandler;
import com.techsenger.greywolf.storage.ui.view.FileView;
import com.techsenger.greywolf.system.terminal.TerminalTabConfig;
import com.techsenger.greywolf.system.terminal.TerminalTabParams;
import com.techsenger.greywolf.system.terminal.TerminalTabView;
import com.techsenger.greywolf.system.terminal.TerminalTabViewModel;
import com.techsenger.greywolf.system.terminal.utils.TerminalUtils;
import java.util.function.Supplier;
import javafx.event.ActionEvent;

/**
 * Opens a {@link TerminalTabView} in the directory the file view acts on, in the same tab container as the tab of
 * the file view: the dock of its panel, or the workspace.
 *
 * @author Pavel Castornii
 */
public class OpenInTerminalItemHandler extends AbstractGreyWolfHandler<ActionEvent> {

    private final Supplier<@Nullable FileView<?>> fileViewSupplier;

    public OpenInTerminalItemHandler(GreyWolf greyWolf, Supplier<@Nullable FileView<?>> fileViewSupplier) {
        super(greyWolf);
        this.fileViewSupplier = fileViewSupplier;
    }

    @Override
    public void handle(ActionEvent event) {
        var fileView = fileViewSupplier.get();
        if (fileView == null) {
            return;
        }
        var file = fileView.getViewModel().getFile();
        var container = fileView.getComposer().getTabContainer();
        var path = file == null ? null : TerminalUtils.getDirectoryPath(file);
        if (path == null || container == null) {
            return;
        }
        var shell = getShell();
        var config = shell.getContext().getConfigManager()
                .getOrCreateConfig(TerminalTabConfig.class, TerminalTabConfig::new);
        var viewModel = new TerminalTabViewModel(new TerminalTabParams(config, path));
        var view = new TerminalTabView(viewModel, shell, container);
        view.initialize();
        container.getComposer().addTab(view);
        view.requestFocus();
    }
}
