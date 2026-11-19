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
import com.techsenger.greywolf.storage.ui.transfer.SourceDialogConfig;
import com.techsenger.greywolf.storage.ui.transfer.SourceDialogParams;
import com.techsenger.greywolf.storage.ui.transfer.SourceDialogView;
import com.techsenger.greywolf.storage.ui.transfer.SourceDialogViewModel;
import com.techsenger.greywolf.storage.ui.transfer.TransferOperationType;
import com.techsenger.shellfx.storage.base.FileEntryType;
import com.techsenger.shellfx.storage.base.UriUtils;
import java.util.stream.Stream;
import javafx.event.ActionEvent;

/**
 * Opens {@code SourceDialog} nested directly in the shell window, outside its normal folder-tab trigger,
 * for styling/layout work - the editable-target variant used by Dual Panels' Copy/Move-to-other-panel; shown
 * fabricated data since it needs no real marked files or target to display.
 *
 * @author Pavel Castornii
 */
public class SourceItemHandler extends AbstractGreyWolfHandler<ActionEvent> {

    public SourceItemHandler(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void handle(ActionEvent event) {
        var greyWolf = getGreyWolf();
        var config = greyWolf.getConfigManager().getOrCreateConfig(SourceDialogConfig.class,
                SourceDialogConfig::new);
        var storage = greyWolf.getFileStorageRegistry().getPrimaryStorage().orElseThrow();
        var sourceFiles = Stream.of("dummy-file-1.txt", "dummy-file-2.txt", "dummy-file-3.txt")
                .map(name -> storage.createVirtual(
                        UriUtils.resolvePath(storage.getUri(), name, false), FileEntryType.FILE, name))
                .toList();
        var params = new SourceDialogParams(config, greyWolf.getSettings(), greyWolf.getConfigManager(),
                TransferOperationType.COPY, sourceFiles, storage.getRoot(),
                () -> greyWolf.getFileStorageRegistry().getAllStorages(), null);
        var viewModel = new SourceDialogViewModel<>(params);
        var view = new SourceDialogView(viewModel, greyWolf.getStylesheetRegistry());
        view.initialize();
        greyWolf.getShell().getComposer().addDialog(view);
        viewModel.setVisible(true);
    }
}
