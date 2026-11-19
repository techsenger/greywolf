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
import com.techsenger.greywolf.storage.base.delete.DeleteProgress;
import com.techsenger.greywolf.storage.ui.delete.DeletingDialogConfig;
import com.techsenger.greywolf.storage.ui.delete.DeletingDialogParams;
import com.techsenger.greywolf.storage.ui.delete.DeletingDialogPort;
import com.techsenger.greywolf.storage.ui.delete.DeletingDialogView;
import com.techsenger.greywolf.storage.ui.delete.DeletingDialogViewModel;
import com.techsenger.shellfx.storage.base.UriUtils;
import javafx.event.ActionEvent;

/**
 * Opens {@code DeletingDialog} nested directly in the shell window, outside its normal folder-tab trigger,
 * for styling/layout work; shown fabricated progress since it needs no real deletion in progress to display.
 *
 * @author Pavel Castornii
 */
public class DeletingItemHandler extends AbstractGreyWolfHandler<ActionEvent> {

    public DeletingItemHandler(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void handle(ActionEvent event) {
        var greyWolf = getGreyWolf();
        var config = greyWolf.getConfigManager().getOrCreateConfig(DeletingDialogConfig.class,
                DeletingDialogConfig::new);
        var params = new DeletingDialogParams(config, greyWolf.getSettings());
        var viewModel = new DeletingDialogViewModel<>(params);
        var view = new DeletingDialogView(viewModel, greyWolf.getStylesheetRegistry());
        view.initialize();
        greyWolf.getShell().getComposer().addDialog(view);
        DeletingDialogPort dialog = viewModel;
        var storage = greyWolf.getFileStorageRegistry().getPrimaryStorage().orElseThrow();
        // Long enough to force the path label's leading-ellipsis truncation into view.
        var currentUri = UriUtils.resolvePath(storage.getUri(),
                "very/deeply/nested/example/directory/structure/for/testing/leading/ellipsis/behavior/"
                        + "file-to-delete-with-a-rather-long-name.txt", false);
        // Sized to a done/total pair landing in different units - "1023.4 KB / 1023.9 MB", 21 chars - so the
        // Total Size column can be checked against its worst-case width in progress-dialog.css.
        dialog.setProgress(new DeleteProgress(800, 1000, 1_047_961L, 1_073_600_000L, currentUri, 12_500L, 3_000L,
                64.0));
        viewModel.setVisible(true);
    }
}
