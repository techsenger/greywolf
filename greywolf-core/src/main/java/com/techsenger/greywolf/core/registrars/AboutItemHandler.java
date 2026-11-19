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
import com.techsenger.greywolf.core.internal.about.AboutDialogConfig;
import com.techsenger.greywolf.core.internal.about.AboutDialogParams;
import com.techsenger.greywolf.core.internal.about.AboutDialogView;
import com.techsenger.greywolf.core.internal.about.AboutDialogViewModel;
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfHandler;
import javafx.event.ActionEvent;
import javafx.stage.Modality;

/**
 * Opens the modal dialog with the information about the application.
 *
 * @author Pavel Castornii
 */
public class AboutItemHandler extends AbstractGreyWolfHandler<ActionEvent> {

    public AboutItemHandler(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void handle(ActionEvent event) {
        var greyWolf = getGreyWolf();
        var config = greyWolf.getConfigManager().getOrCreateConfig(AboutDialogConfig.class,
                AboutDialogConfig::new);
        var params = new AboutDialogParams(config, greyWolf.getSettings(), greyWolf.getHostServices());
        var viewModel = new AboutDialogViewModel<>(params);
        var view = new AboutDialogView(viewModel, greyWolf.getStylesheetRegistry());
        view.initialize();
        view.getWindow().initOwner(getShell().getWindow());
        view.getWindow().initModality(Modality.APPLICATION_MODAL);
        viewModel.setVisible(true);
    }
}
