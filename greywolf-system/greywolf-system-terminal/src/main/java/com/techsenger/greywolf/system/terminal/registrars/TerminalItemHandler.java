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

import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfHandler;
import com.techsenger.greywolf.system.terminal.TerminalTabConfig;
import com.techsenger.greywolf.system.terminal.TerminalTabParams;
import com.techsenger.greywolf.system.terminal.TerminalTabView;
import com.techsenger.greywolf.system.terminal.TerminalTabViewModel;
import javafx.event.ActionEvent;

/**
 *
 * @author Pavel Castornii
 */
public class TerminalItemHandler extends AbstractGreyWolfHandler<ActionEvent> {

    public TerminalItemHandler(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void handle(ActionEvent event) {
        var shell = getShell();
        var workspace = shell.getComposer().getWorkspace();
        var config = shell.getContext().getConfigManager()
                .getOrCreateConfig(TerminalTabConfig.class, TerminalTabConfig::new);
        var params = new TerminalTabParams(config, System.getProperty("user.home"));
        var viewModel = new TerminalTabViewModel(params);
        var view = new TerminalTabView(viewModel, shell, workspace);
        view.initialize();
        workspace.getComposer().addTab(view);
        view.requestFocus();
    }
}
