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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * Lets the user type the target or pick one of the earlier ones.
 *
 * @author Pavel Castornii
 */
public final class SourceDialogView extends AbstractTransferDialogView<SourceDialogViewModel<?>> {

    private final Label targetLabel = new Label("Target");

    private final ComboBox<String> targetComboBox = new ComboBox<>();

    public SourceDialogView(SourceDialogViewModel<?> viewModel, StylesheetRegistry stylesheetRegistry) {
        super(viewModel, stylesheetRegistry);
    }

    @Override
    protected void buildCounterpartRow(GridPane gridPane, int row) {
        targetComboBox.setEditable(true);
        targetComboBox.setMaxWidth(Double.MAX_VALUE);
        targetLabel.setMinWidth(Label.USE_PREF_SIZE);
        gridPane.add(targetLabel, 0, row);
        gridPane.add(targetComboBox, 1, row);
        GridPane.setHgrow(targetComboBox, Priority.ALWAYS);
    }

    @Override
    protected void bind() {
        super.bind();
        var viewModel = getViewModel();
        targetComboBox.setItems(viewModel.getTargets());
        targetComboBox.getEditor().textProperty().bindBidirectional(viewModel.targetProperty());
    }
}
