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

package com.techsenger.greywolf.storage.ui.transfer;

import com.techsenger.greywolf.storage.ui.operation.AbstractOperationComposer;
import com.techsenger.shellfx.core.registries.StylesheetRegistry;
import com.techsenger.shellfx.core.window.WindowContainerView;

/**
 * Opens the dialogs of a transfer operation directly into the given container, never nested inside another dialog.
 *
 * @author Pavel Castornii
 */
public final class DefaultTransferComposer extends AbstractOperationComposer implements TransferComposer {

    public DefaultTransferComposer(WindowContainerView.Composer container, StylesheetRegistry stylesheetRegistry) {
        super(container, stylesheetRegistry);
    }

    @Override
    public SourceDialogPort openSourceDialog(SourceDialogParams params) {
        var viewModel = new SourceDialogViewModel<>(params);
        var view = new SourceDialogView(viewModel, getStylesheetRegistry());
        view.initialize();
        getContainer().addDialog(view);
        return viewModel;
    }

    @Override
    public TargetDialogPort openTargetDialog(TargetDialogParams params) {
        var viewModel = new TargetDialogViewModel<>(params);
        var view = new TargetDialogView(viewModel, getStylesheetRegistry());
        view.initialize();
        getContainer().addDialog(view);
        return viewModel;
    }

    @Override
    public TransferringDialogPort openTransferringDialog(TransferringDialogParams params) {
        var viewModel = new TransferringDialogViewModel<>(params);
        var view = new TransferringDialogView(viewModel, getStylesheetRegistry());
        view.initialize();
        getContainer().addDialog(view);
        return viewModel;
    }

    @Override
    public ConflictDialogPort openConflictDialog(ConflictDialogParams params) {
        var viewModel = new ConflictDialogViewModel<>(params);
        var view = new ConflictDialogView(viewModel, getStylesheetRegistry());
        view.initialize();
        getContainer().addDialog(view);
        return viewModel;
    }
}
