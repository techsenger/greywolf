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
import com.techsenger.greywolf.storage.ui.transfer.ConflictDialogConfig;
import com.techsenger.greywolf.storage.ui.transfer.ConflictDialogParams;
import com.techsenger.greywolf.storage.ui.transfer.ConflictDialogPort;
import com.techsenger.greywolf.storage.ui.transfer.ConflictDialogView;
import com.techsenger.greywolf.storage.ui.transfer.ConflictDialogViewModel;
import com.techsenger.shellfx.storage.base.UriUtils;
import javafx.event.ActionEvent;

/**
 * Opens {@code ConflictDialog} nested directly in the shell window, outside its normal folder-tab trigger, for
 * styling/layout work; shown a fabricated conflict since it needs no real one to display.
 *
 * @author Pavel Castornii
 */
public class ConflictItemHandler extends AbstractGreyWolfHandler<ActionEvent> {

    public ConflictItemHandler(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void handle(ActionEvent event) {
        var greyWolf = getGreyWolf();
        var config = greyWolf.getConfigManager().getOrCreateConfig(ConflictDialogConfig.class,
                ConflictDialogConfig::new);
        var params = new ConflictDialogParams(config, greyWolf.getSettings());
        var viewModel = new ConflictDialogViewModel<>(params);
        var view = new ConflictDialogView(viewModel, greyWolf.getStylesheetRegistry());
        view.initialize();
        greyWolf.getShell().getComposer().addDialog(view);
        ConflictDialogPort dialog = viewModel;
        var storage = greyWolf.getFileStorageRegistry().getPrimaryStorage().orElseThrow();
        // Long enough to force the readonly path field to scroll past its start, verifying it lands on the end.
        var targetUri = UriUtils.resolvePath(storage.getUri(),
                "very/deeply/nested/example/directory/structure/for/testing/the/path/field/scrolling/to/its/"
                        + "very/end/example-file-with-a-rather-long-name.txt", false);
        dialog.setFilePath(targetUri.toString());
        viewModel.setVisible(true);
    }
}
