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

package com.techsenger.greywolf.devtools.base.registrars;

import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfHandler;
import com.techsenger.greywolf.storage.ui.name.NameDialogConfig;
import com.techsenger.greywolf.storage.ui.name.NameDialogParams;
import com.techsenger.greywolf.storage.ui.name.NameDialogType;
import com.techsenger.greywolf.storage.ui.name.NameDialogView;
import com.techsenger.greywolf.storage.ui.name.NameDialogViewModel;
import javafx.event.ActionEvent;
import javafx.scene.control.IndexRange;

/**
 * Opens the name dialog as the rename dialog nested directly in the shell window, outside its normal file-tab
 * trigger, for styling/layout work with a sample file name - nobody is expected to press OK.
 *
 * @author Pavel Castornii
 */
public class RenameItemHandler extends AbstractGreyWolfHandler<ActionEvent> {

    public RenameItemHandler(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void handle(ActionEvent event) {
        var greyWolf = getGreyWolf();
        var config = greyWolf.getConfigManager().getOrCreateConfig(NameDialogConfig.class,
                NameDialogConfig::new);
        var params = new NameDialogParams(config, greyWolf.getSettings(), NameDialogType.RENAME);
        var viewModel = new NameDialogViewModel<>(params);
        var view = new NameDialogView(viewModel, greyWolf.getStylesheetRegistry());
        view.initialize();
        greyWolf.getShell().getComposer().addDialog(view);
        viewModel.setText("report.txt");
        viewModel.setSelection(new IndexRange(0, "report".length()));
        viewModel.setVisible(true);
    }
}
