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
import com.techsenger.greywolf.storage.ui.properties.FilePropertiesDialogConfig;
import com.techsenger.greywolf.storage.ui.properties.FilePropertiesDialogParams;
import com.techsenger.greywolf.storage.ui.properties.FilePropertiesDialogView;
import com.techsenger.greywolf.storage.ui.properties.FilePropertiesDialogViewModel;
import com.techsenger.shellfx.storage.base.FileEntryType;
import javafx.event.ActionEvent;

/**
 * Opens {@code FilePropertiesDialog} nested directly in the shell window, outside its normal folder-tab trigger, for
 * styling/layout work; shown a fabricated file since it needs no real one to display.
 *
 * @author Pavel Castornii
 */
public class FilePropertiesItemHandler extends AbstractGreyWolfHandler<ActionEvent> {

    public FilePropertiesItemHandler(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void handle(ActionEvent event) {
        var greyWolf = getGreyWolf();
        var config = greyWolf.getConfigManager().getOrCreateConfig(FilePropertiesDialogConfig.class,
                FilePropertiesDialogConfig::new);
        var storage = greyWolf.getFileStorageRegistry().getPrimaryStorage().orElseThrow();
        var file = storage.createVirtual(storage.getUri(), FileEntryType.FILE, "dummy-file.txt");
        var params = new FilePropertiesDialogParams(config, greyWolf.getSettings(), file);
        var viewModel = new FilePropertiesDialogViewModel<>(params);
        var view = new FilePropertiesDialogView(viewModel, greyWolf.getStylesheetRegistry());
        view.initialize();
        greyWolf.getShell().getComposer().addDialog(view);
        viewModel.setVisible(true);
    }
}
