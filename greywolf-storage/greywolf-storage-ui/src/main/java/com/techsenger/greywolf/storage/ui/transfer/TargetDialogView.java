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

import com.techsenger.shellfx.core.registries.StylesheetRegistry;
import com.techsenger.shellfx.material.textfield.CopyableTextField;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * Shows the directory of the sources; the target is not editable.
 *
 * @author Pavel Castornii
 */
public final class TargetDialogView extends AbstractTransferDialogView<TargetDialogViewModel<?>> {

    private final Label sourceLabel = new Label("Source");

    private final CopyableTextField sourceField = new CopyableTextField();

    public TargetDialogView(TargetDialogViewModel<?> viewModel, StylesheetRegistry stylesheetRegistry) {
        super(viewModel, stylesheetRegistry);
    }

    @Override
    protected void buildCounterpartRow(GridPane gridPane, int row) {
        sourceField.setEditable(false);
        sourceField.setMaxWidth(Double.MAX_VALUE);
        sourceLabel.setMinWidth(Label.USE_PREF_SIZE);
        gridPane.add(sourceLabel, 0, row);
        gridPane.add(sourceField, 1, row);
        GridPane.setHgrow(sourceField, Priority.ALWAYS);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        var sourceDirectory = getViewModel().getSourceDirectory();
        var storage = sourceDirectory.getStorage();
        sourceField.setText(storage.getLocationConverter().toLocation(sourceDirectory.getUri()));
    }
}
