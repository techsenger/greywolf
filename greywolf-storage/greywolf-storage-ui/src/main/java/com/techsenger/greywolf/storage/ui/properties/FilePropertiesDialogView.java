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

package com.techsenger.greywolf.storage.ui.properties;

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.ui.style.FileLabels;
import com.techsenger.shellfx.core.dialog.AbstractDialogView;
import com.techsenger.shellfx.core.registries.StylesheetRegistry;
import com.techsenger.shellfx.material.button.ResultButton;
import com.techsenger.shellfx.material.style.Spacing;
import com.techsenger.shellfx.material.textfield.CopyableTextField;
import com.techsenger.shellfx.storage.base.FileStorageType;
import com.techsenger.shellfx.storage.ui.DateTimeUtils;
import com.techsenger.toolkit.core.file.FileUtils;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public final class FilePropertiesDialogView extends AbstractDialogView<FilePropertiesDialogViewModel<?>> {

    private static final String NO_DATA = "–";

    private final Label nameLabel = new Label("Name");

    private final CopyableTextField nameTextField = new CopyableTextField();

    private final Label uriLabel = new Label("URI");

    private final CopyableTextField uriTextField = new CopyableTextField();

    private final Label pathLabel = new Label("Path");

    private final CopyableTextField pathTextField = new CopyableTextField();

    private final Label typeLabel = new Label("Type");

    private final CopyableTextField typeTextField = new CopyableTextField();

    private final Label sizeLabel = new Label("Size");

    private final CopyableTextField sizeTextField = new CopyableTextField();

    private final Label createdTimeLabel = new Label("Created");

    private final CopyableTextField createdTimeTextField = new CopyableTextField();

    private final Label modifiedTimeLabel = new Label("Modified");

    private final CopyableTextField modifiedTimeTextField = new CopyableTextField();

    private final GridPane gridPane = new GridPane();

    private final ResultButton closeButton = new ResultButton(FilePropertiesDialogButtons.CLOSE, "Close");

    public FilePropertiesDialogView(FilePropertiesDialogViewModel<?> viewModel, StylesheetRegistry stylesheetRegistry) {
        super(viewModel, stylesheetRegistry);
    }

    @Override
    public void requestFocus() {
        nameTextField.requestFocus();
    }

    @Override
    protected void build() {
        super.build();
        addRow(nameLabel, nameTextField);
        addRow(uriLabel, uriTextField);
        addRow(pathLabel, pathTextField);
        addRow(typeLabel, typeTextField, sizeLabel, sizeTextField);
        addRow(createdTimeLabel, createdTimeTextField, modifiedTimeLabel, modifiedTimeTextField);
        gridPane.setHgap(Spacing.getHorizontal());
        gridPane.setVgap(Spacing.getVertical());
        VBox.setVgrow(gridPane, Priority.ALWAYS);
        getContentBox().getChildren().add(gridPane);
        getContentBox().setSpacing(Spacing.getVertical());

        closeButton.setDefaultButton(true);
        registerButtons(closeButton);
        getFocusTrap().activate();
    }

    @Override
    protected void bind() {
        super.bind();
        applyFile(getViewModel().getFile());
    }

    private void addRow(Label label, CopyableTextField field) {
        addRow(label, field, null, null);
    }

    private void addRow(Label label0, CopyableTextField field0, @Nullable Label label1,
            @Nullable CopyableTextField field1) {
        var rowIndex = gridPane.getRowCount();
        setupRowLabel(label0);
        gridPane.add(label0, 0, rowIndex);
        setupRowTextField(field0);
        gridPane.add(field0, 1, rowIndex);
        if (label1 != null && field1 != null) {
            setupRowLabel(label1);
            gridPane.add(label1, 2, rowIndex);
            setupRowTextField(field1);
            gridPane.add(field1, 3, rowIndex);
        } else {
            GridPane.setColumnSpan(field0, 3);
        }
    }

    private void setupRowLabel(Label label) {
        label.setMinWidth(Label.USE_PREF_SIZE);
    }

    private void setupRowTextField(TextField field) {
        field.setEditable(false);
        field.setFocusTraversable(true);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }

    private void applyFile(GreyWolfFile file) {
        nameTextField.setText(file.isParentEntry() ? ".." : file.getName());
        uriTextField.setText(file.getUri().toString());
        if (file.getStorage().getType() == FileStorageType.NETWORK) {
            pathTextField.setText(NO_DATA);
        } else {
            pathTextField.setText(file.getUri().getPath());
        }
        typeTextField.setText(FileLabels.ofEntryOrFileType(file));
        var size = file.getSize();
        sizeTextField.setText(size != null ? FileUtils.formatSize(size) : NO_DATA);
        var createdTime = file.getCreatedTime();
        createdTimeTextField.setText(createdTime != null ? DateTimeUtils.formatFull(createdTime) : NO_DATA);
        var modifiedTime = file.getModifiedTime();
        modifiedTimeTextField.setText(modifiedTime != null ? DateTimeUtils.formatFull(modifiedTime) : NO_DATA);
    }
}
