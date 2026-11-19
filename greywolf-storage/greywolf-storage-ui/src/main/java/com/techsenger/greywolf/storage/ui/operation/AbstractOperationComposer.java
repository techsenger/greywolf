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

package com.techsenger.greywolf.storage.ui.operation;

import com.techsenger.shellfx.core.registries.StylesheetRegistry;
import com.techsenger.shellfx.core.window.WindowContainerView;
import com.techsenger.shellfx.dialogs.alert.AlertDialogParams;
import com.techsenger.shellfx.dialogs.alert.AlertDialogPort;
import com.techsenger.shellfx.dialogs.alert.AlertDialogView;
import com.techsenger.shellfx.dialogs.alert.AlertDialogViewModel;
import com.techsenger.shellfx.dialogs.progress.ProgressDialogParams;
import com.techsenger.shellfx.dialogs.progress.ProgressDialogPort;
import com.techsenger.shellfx.dialogs.progress.ProgressDialogView;
import com.techsenger.shellfx.dialogs.progress.ProgressDialogViewModel;

/**
 * Opens every dialog directly into the given container, never nested inside another dialog, so the dialogs
 * keep working regardless of which of them is currently open or already closed.
 *
 * @author Pavel Castornii
 */
public abstract class AbstractOperationComposer implements OperationComposer {

    private final WindowContainerView.Composer container;

    private final StylesheetRegistry stylesheetRegistry;

    protected AbstractOperationComposer(WindowContainerView.Composer container, StylesheetRegistry stylesheetRegistry) {
        this.container = container;
        this.stylesheetRegistry = stylesheetRegistry;
    }

    @Override
    public DeferredDialog<ProgressDialogPort> createProgressDialog(ProgressDialogParams params) {
        var viewModel = new ProgressDialogViewModel<>(params);
        var view = new ProgressDialogView<>(viewModel, stylesheetRegistry);
        view.initialize();
        return new DeferredDialog<>(viewModel, () -> {
            getContainer().addDialog(view);
            viewModel.setVisible(true);
        });
    }

    @Override
    public OperationErrorDialogPort openOperationErrorDialog(OperationErrorDialogParams params) {
        var viewModel = new OperationErrorDialogViewModel<>(params);
        var view = new OperationErrorDialogView(viewModel, stylesheetRegistry);
        view.initialize();
        getContainer().addDialog(view);
        return viewModel;
    }

    @Override
    public AlertDialogPort openAlertDialog(AlertDialogParams params) {
        var viewModel = new AlertDialogViewModel<>(params);
        var view = new AlertDialogView<>(viewModel, stylesheetRegistry);
        view.initialize();
        getContainer().addDialog(view);
        return viewModel;
    }

    protected WindowContainerView.Composer getContainer() {
        return container;
    }

    protected StylesheetRegistry getStylesheetRegistry() {
        return stylesheetRegistry;
    }
}
