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

import atlantafx.base.theme.Styles;
import com.techsenger.shellfx.core.dialog.AbstractDialogView;
import com.techsenger.shellfx.core.registries.StylesheetRegistry;
import com.techsenger.shellfx.material.button.ResultButton;
import com.techsenger.shellfx.material.style.Spacing;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public final class ConflictDialogView extends AbstractDialogView<ConflictDialogViewModel<?>> {

    private final Label messageLabel = new Label("A file or folder with this name already exists");

    private final TextArea pathTextArea = new TextArea();

    private final VBox mainBox = new VBox(Spacing.getVertical(), messageLabel, pathTextArea);

    private final CheckBox applyToAllCheckBox = new CheckBox("Apply to all conflicts");

    private final ResultButton skipButton = new ResultButton(ConflictDialogButtons.SKIP, "Skip");

    private final ResultButton renameButton = new ResultButton(ConflictDialogButtons.RENAME, "Rename");

    private final ResultButton overwriteButton = new ResultButton(ConflictDialogButtons.OVERWRITE, "Overwrite");

    public ConflictDialogView(ConflictDialogViewModel<?> viewModel, StylesheetRegistry stylesheetRegistry) {
        super(viewModel, stylesheetRegistry);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    protected void build() {
        super.build();
        getIconViewBox().getStyleClass().add(Styles.WARNING);
        messageLabel.setWrapText(true);
        pathTextArea.setEditable(false);
        pathTextArea.setMaxWidth(Double.MAX_VALUE);
        pathTextArea.setWrapText(true);
        pathTextArea.setPrefRowCount(3);
        VBox.setVgrow(pathTextArea, Priority.ALWAYS);
        VBox.setVgrow(mainBox, Priority.ALWAYS);
        getContentBox().getChildren().add(mainBox);

        getLeftBottomBox().getChildren().add(applyToAllCheckBox);

        skipButton.setMaxWidth(Region.USE_COMPUTED_SIZE);
        renameButton.setMaxWidth(Region.USE_COMPUTED_SIZE);
        overwriteButton.setMaxWidth(Region.USE_COMPUTED_SIZE);
        registerButtons(skipButton, renameButton, overwriteButton);
        getButtonWidthGroup().add(skipButton, renameButton, overwriteButton);
        getFocusTrap().activate();
    }

    @Override
    protected void bind() {
        super.bind();
        applyToAllCheckBox.selectedProperty().bindBidirectional(getViewModel().applyToAllSelectedProperty());
        pathTextArea.textProperty().bind(getViewModel().filePathProperty());
    }
}
