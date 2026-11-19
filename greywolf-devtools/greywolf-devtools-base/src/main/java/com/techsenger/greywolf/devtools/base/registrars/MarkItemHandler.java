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
import com.techsenger.greywolf.system.navigator.mark.MarkDialogConfig;
import com.techsenger.greywolf.system.navigator.mark.MarkDialogParams;
import com.techsenger.greywolf.system.navigator.mark.MarkDialogView;
import com.techsenger.greywolf.system.navigator.mark.MarkDialogViewModel;
import com.techsenger.greywolf.system.navigator.mark.MarkOperationType;
import javafx.event.ActionEvent;

/**
 * Opens {@code MarkDialog} nested directly in the shell window, outside its normal folder-tab trigger, for
 * styling/layout work - nobody is expected to press OK.
 *
 * @author Pavel Castornii
 */
public class MarkItemHandler extends AbstractGreyWolfHandler<ActionEvent> {

    public MarkItemHandler(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void handle(ActionEvent event) {
        var greyWolf = getGreyWolf();
        var config = greyWolf.getConfigManager().getOrCreateConfig(MarkDialogConfig.class, MarkDialogConfig::new);
        var params = new MarkDialogParams(config, greyWolf.getSettings(), MarkOperationType.MARK);
        var viewModel = new MarkDialogViewModel<>(params);
        var view = new MarkDialogView(viewModel, greyWolf.getStylesheetRegistry());
        view.initialize();
        greyWolf.getShell().getComposer().addDialog(view);
        viewModel.setVisible(true);
    }
}
