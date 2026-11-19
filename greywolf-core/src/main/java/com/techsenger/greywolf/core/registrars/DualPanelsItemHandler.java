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
import com.techsenger.greywolf.core.panels.DualPanelsTabConfig;
import com.techsenger.greywolf.core.panels.DualPanelsTabParams;
import com.techsenger.greywolf.core.panels.DualPanelsTabView;
import com.techsenger.greywolf.core.panels.DualPanelsTabViewModel;
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfHandler;
import javafx.event.ActionEvent;

/**
 * Opens a {@link DualPanelsTabView} as a top-level workspace tab, with both panels left empty for the
 * user to fill with whichever registered panel-tab plugin they choose.
 *
 * @author Pavel Castornii
 */
public class DualPanelsItemHandler extends AbstractGreyWolfHandler<ActionEvent> {

    public DualPanelsItemHandler(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void handle(ActionEvent event) {
        var shell = getShell();
        var configManager = shell.getContext().getConfigManager();
        var config = configManager.getOrCreateConfig(DualPanelsTabConfig.class, DualPanelsTabConfig::new);
        var params = new DualPanelsTabParams(config);
        var viewModel = new DualPanelsTabViewModel(params);
        var view = new DualPanelsTabView(viewModel, shell);
        view.initialize();
        shell.getComposer().getWorkspace().getComposer().addTab(view);
    }
}
