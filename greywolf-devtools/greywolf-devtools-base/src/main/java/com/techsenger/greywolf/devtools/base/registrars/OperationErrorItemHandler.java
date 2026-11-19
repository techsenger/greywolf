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
import com.techsenger.greywolf.storage.ui.operation.OperationErrorDialogConfig;
import com.techsenger.greywolf.storage.ui.operation.OperationErrorDialogParams;
import com.techsenger.greywolf.storage.ui.operation.OperationErrorDialogPort;
import com.techsenger.greywolf.storage.ui.operation.OperationErrorDialogView;
import com.techsenger.greywolf.storage.ui.operation.OperationErrorDialogViewModel;
import com.techsenger.shellfx.storage.base.UriUtils;
import javafx.event.ActionEvent;

/**
 * Opens {@code OperationErrorDialog} nested directly in the shell window, outside its normal folder-tab
 * trigger, for styling/layout work; shown a fabricated error message since it needs no real failure to display.
 *
 * @author Pavel Castornii
 */
public class OperationErrorItemHandler extends AbstractGreyWolfHandler<ActionEvent> {

    public OperationErrorItemHandler(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void handle(ActionEvent event) {
        var greyWolf = getGreyWolf();
        var config = greyWolf.getConfigManager().getOrCreateConfig(OperationErrorDialogConfig.class,
                OperationErrorDialogConfig::new);
        var params = new OperationErrorDialogParams(config, greyWolf.getSettings());
        var viewModel = new OperationErrorDialogViewModel<>(params);
        var view = new OperationErrorDialogView(viewModel, greyWolf.getStylesheetRegistry());
        view.initialize();
        greyWolf.getShell().getComposer().addDialog(view);
        OperationErrorDialogPort dialog = viewModel;
        dialog.setMessage("Access denied while copying the file");
        var storage = greyWolf.getFileStorageRegistry().getPrimaryStorage().orElseThrow();
        // Long enough to force the readonly path field to scroll past its start, verifying it lands on the end.
        var sourceUri = UriUtils.resolvePath(storage.getUri(),
                "very/deeply/nested/example/directory/structure/for/testing/the/path/field/scrolling/to/its/"
                        + "very/end/example-file-with-a-rather-long-name.txt", false);
        dialog.setFilePath(sourceUri.toString());
        viewModel.setVisible(true);
    }
}
