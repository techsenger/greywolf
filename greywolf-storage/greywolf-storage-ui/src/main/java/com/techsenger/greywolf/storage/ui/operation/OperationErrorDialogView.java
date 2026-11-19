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

import atlantafx.base.theme.Styles;
import com.techsenger.shellfx.core.dialog.AbstractDialogView;
import com.techsenger.shellfx.core.registries.StylesheetRegistry;
import com.techsenger.shellfx.material.button.ResultButton;
import com.techsenger.shellfx.material.style.Spacing;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public final class OperationErrorDialogView extends AbstractDialogView<OperationErrorDialogViewModel<?>> {

    private final Label messageLabel = new Label();

    private final TextArea pathTextArea = new TextArea();

    private final VBox mainBox = new VBox(Spacing.getVertical(), messageLabel, pathTextArea);

    private final ResultButton skipButton = new ResultButton(OperationErrorDialogButtons.SKIP, "Skip");

    private final ResultButton skipAllButton = new ResultButton(OperationErrorDialogButtons.SKIP_ALL, "Skip All");

    private final ResultButton retryButton = new ResultButton(OperationErrorDialogButtons.RETRY, "Retry");

    private final ResultButton abortButton = new ResultButton(OperationErrorDialogButtons.ABORT, "Abort");

    public OperationErrorDialogView(OperationErrorDialogViewModel<?> viewModel, StylesheetRegistry stylesheetRegistry) {
        super(viewModel, stylesheetRegistry);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    protected void build() {
        super.build();
        getIconViewBox().getStyleClass().add(Styles.DANGER);
        messageLabel.setWrapText(true);
        messageLabel.setWrapText(true);
        pathTextArea.setEditable(false);
        pathTextArea.setMaxWidth(Double.MAX_VALUE);
        pathTextArea.setWrapText(true);
        pathTextArea.setPrefRowCount(3);
        VBox.setVgrow(pathTextArea, Priority.ALWAYS);
        VBox.setVgrow(mainBox, Priority.ALWAYS);
        getContentBox().getChildren().add(mainBox);

        skipButton.setDefaultButton(true);
        registerButtons(abortButton, retryButton, skipAllButton, skipButton);
        getButtonWidthGroup().add(abortButton, retryButton, skipAllButton, skipButton);
        getFocusTrap().activate();
    }

    @Override
    protected void bind() {
        super.bind();
        messageLabel.textProperty().bind(getViewModel().messageProperty());
        pathTextArea.textProperty().bind(getViewModel().filePathProperty());
    }
}
