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
import com.techsenger.greywolf.storage.base.transfer.TransferProgress;
import com.techsenger.greywolf.storage.ui.transfer.TransferOperationType;
import com.techsenger.greywolf.storage.ui.transfer.TransferringDialogConfig;
import com.techsenger.greywolf.storage.ui.transfer.TransferringDialogParams;
import com.techsenger.greywolf.storage.ui.transfer.TransferringDialogPort;
import com.techsenger.greywolf.storage.ui.transfer.TransferringDialogView;
import com.techsenger.greywolf.storage.ui.transfer.TransferringDialogViewModel;
import com.techsenger.shellfx.storage.base.UriUtils;
import javafx.event.ActionEvent;

/**
 * Opens {@code TransferringDialog} nested directly in the shell window, outside its normal folder-tab trigger,
 * for styling/layout work; shown fabricated progress since it needs no real copy in progress to display.
 *
 * @author Pavel Castornii
 */
public class TransferringItemHandler extends AbstractGreyWolfHandler<ActionEvent> {

    public TransferringItemHandler(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void handle(ActionEvent event) {
        var greyWolf = getGreyWolf();
        var config = greyWolf.getConfigManager().getOrCreateConfig(TransferringDialogConfig.class,
                TransferringDialogConfig::new);
        var params = new TransferringDialogParams(config, greyWolf.getSettings(), TransferOperationType.COPY);
        var viewModel = new TransferringDialogViewModel<>(params);
        var view = new TransferringDialogView(viewModel, greyWolf.getStylesheetRegistry());
        view.initialize();
        greyWolf.getShell().getComposer().addDialog(view);
        TransferringDialogPort dialog = viewModel;
        var storage = greyWolf.getFileStorageRegistry().getPrimaryStorage().orElseThrow();
        var dummyUri = storage.getUri();
        // Long enough to force the From/To labels' leading-ellipsis truncation into view.
        var sourceUri = UriUtils.resolvePath(dummyUri,
                "very/deeply/nested/example/directory/structure/for/testing/leading/ellipsis/behavior/"
                        + "source-file-with-a-rather-long-name.txt", false);
        var targetUri = UriUtils.resolvePath(dummyUri,
                "another/very/deeply/nested/example/directory/structure/used/for/testing/leading/ellipsis/"
                        + "target-file-with-a-rather-long-name.txt", false);
        // Sized to a done/total pair landing in different units - "1023.4 KB / 1023.9 MB", 21 chars - so the
        // File Size/Total Size columns can be checked against their worst-case width in transfer-dialog.css.
        dialog.setProgress(new TransferProgress(8, 10, 500_047_961L, 1_073_600_000L, sourceUri, targetUri,
                100_047_961L, 1_073_600_000L, 12_500L, 30_000L, 1_048_576L, 917_504L));
        viewModel.setVisible(true);
    }
}
