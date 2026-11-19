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

package com.techsenger.greywolf.devtools.weaverbird.registrars;

import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfHandler;
import com.techsenger.weaverbird.gui.console.ConsoleTabConfig;
import com.techsenger.weaverbird.gui.console.ConsoleTabParams;
import com.techsenger.weaverbird.gui.console.ConsoleTabView;
import com.techsenger.weaverbird.gui.console.ConsoleTabViewModel;
import javafx.event.ActionEvent;

/**
 *
 * @author Pavel Castornii
 */
public class ConsoleItemHandler extends AbstractGreyWolfHandler<ActionEvent> {

    public ConsoleItemHandler(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void handle(ActionEvent event) {
        var greyWolf = getGreyWolf();
        var shell = getShell();
        var workspace = shell.getComposer().getWorkspace();

        var config = greyWolf.getConfigManager().getOrCreateConfig(ConsoleTabConfig.class, ConsoleTabConfig::new);
        var params = new ConsoleTabParams(config, greyWolf.getWeaverbird(), null, null);
        var viewModel = new ConsoleTabViewModel<>(params);
        var view = new ConsoleTabView<>(viewModel, shell);
        view.initialize();
        workspace.getComposer().addTab(view);
    }
}
