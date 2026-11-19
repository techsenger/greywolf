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

package com.techsenger.greywolf.storage.ui.delete;

import com.techsenger.greywolf.storage.ui.operation.AbstractOperationComposer;
import com.techsenger.shellfx.core.registries.StylesheetRegistry;
import com.techsenger.shellfx.core.window.WindowContainerView;

/**
 * Opens the dialogs of a delete operation directly into the given container, never nested inside another dialog.
 *
 * @author Pavel Castornii
 */
public final class DefaultDeleteComposer extends AbstractOperationComposer implements DeleteComposer {

    public DefaultDeleteComposer(WindowContainerView.Composer container, StylesheetRegistry stylesheetRegistry) {
        super(container, stylesheetRegistry);
    }

    @Override
    public DeletingDialogPort openDeletingDialog(DeletingDialogParams params) {
        var viewModel = new DeletingDialogViewModel<>(params);
        var view = new DeletingDialogView(viewModel, getStylesheetRegistry());
        view.initialize();
        getContainer().addDialog(view);
        return viewModel;
    }
}
