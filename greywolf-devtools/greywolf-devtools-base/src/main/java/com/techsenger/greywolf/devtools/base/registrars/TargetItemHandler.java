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
import com.techsenger.greywolf.storage.ui.transfer.TargetDialogConfig;
import com.techsenger.greywolf.storage.ui.transfer.TargetDialogParams;
import com.techsenger.greywolf.storage.ui.transfer.TargetDialogView;
import com.techsenger.greywolf.storage.ui.transfer.TargetDialogViewModel;
import com.techsenger.greywolf.storage.ui.transfer.TransferOperationType;
import com.techsenger.shellfx.storage.base.FileEntryType;
import com.techsenger.shellfx.storage.base.UriUtils;
import java.util.List;
import javafx.event.ActionEvent;

/**
 * Opens {@code TargetDialog} nested directly in the shell window, outside its normal folder-tab trigger,
 * for styling/layout work - the read-only-source variant used by a clipboard paste; shown a fabricated source since
 * it needs no real clipboard content to display.
 *
 * @author Pavel Castornii
 */
public class TargetItemHandler extends AbstractGreyWolfHandler<ActionEvent> {

    public TargetItemHandler(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void handle(ActionEvent event) {
        var greyWolf = getGreyWolf();
        var config = greyWolf.getConfigManager().getOrCreateConfig(TargetDialogConfig.class,
                TargetDialogConfig::new);
        var storage = greyWolf.getFileStorageRegistry().getPrimaryStorage().orElseThrow();
        var sourceFile = storage.createVirtual(
                UriUtils.resolvePath(storage.getUri(), "dummy-source-folder/dummy-file.txt", false),
                FileEntryType.FILE, "dummy-file.txt");
        var params = new TargetDialogParams(config, greyWolf.getSettings(), greyWolf.getConfigManager(),
                TransferOperationType.COPY, List.of(sourceFile), storage.getRoot());
        var viewModel = new TargetDialogViewModel<>(params);
        var view = new TargetDialogView(viewModel, greyWolf.getStylesheetRegistry());
        view.initialize();
        greyWolf.getShell().getComposer().addDialog(view);
        viewModel.setVisible(true);
    }
}
