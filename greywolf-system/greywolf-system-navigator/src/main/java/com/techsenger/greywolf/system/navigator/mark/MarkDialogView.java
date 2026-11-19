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

package com.techsenger.greywolf.system.navigator.mark;

import atlantafx.base.theme.Styles;
import com.techsenger.greywolf.storage.base.FileType;
import com.techsenger.greywolf.storage.ui.style.FileLabels;
import com.techsenger.greywolf.storage.ui.style.StorageUiIcons;
import com.techsenger.shellfx.core.dialog.AbstractDialogView;
import com.techsenger.shellfx.core.registries.StylesheetRegistry;
import com.techsenger.shellfx.material.button.ResultButton;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.style.Spacing;
import com.techsenger.shellfx.material.style.StyleClasses;
import com.techsenger.shellfx.storage.base.FileEntryType;
import com.techsenger.toolkit.fx.utils.NodeUtils;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import javafx.collections.MapChangeListener;
import javafx.geometry.VPos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public final class MarkDialogView extends AbstractDialogView<MarkDialogViewModel<?>> {

    private static final int TYPE_COLUMN_COUNT = 2;

    private final Label maskLabel = new Label("Mask");

    private final ComboBox<String> maskComboBox = new ComboBox<>();

    private final ToggleButton caseSensitiveButton =
            new ToggleButton(null, new FontIconView(StorageUiIcons.MATCH_CASE));

    private final ToggleButton regexButton = new ToggleButton(null, new FontIconView(StorageUiIcons.REG_EXP));

    private final HBox maskBox = new HBox(maskComboBox, caseSensitiveButton, regexButton);

    private final Label entryTypeLabel = new Label("Entry Type");

    private final Label fileTypeLabel = new Label("File Type");

    private final GridPane entryTypeGridPane = new GridPane();

    private final GridPane fileTypeGridPane = new GridPane();

    private final Map<FileEntryType, CheckBox> checkBoxesByEntryType = new EnumMap<>(FileEntryType.class);

    private final Map<FileType, CheckBox> checkBoxesByFileType = new EnumMap<>(FileType.class);

    private final GridPane gridPane = new GridPane();

    private final ResultButton cancelButton = new ResultButton(MarkDialogButtons.CANCEL, "Cancel");

    private final ResultButton applyButton = new ResultButton(MarkDialogButtons.APPLY, "Apply");

    private final ResultButton okButton = new ResultButton(MarkDialogButtons.OK, "OK");

    public MarkDialogView(MarkDialogViewModel<?> viewModel, StylesheetRegistry stylesheetRegistry) {
        super(viewModel, stylesheetRegistry);
    }

    @Override
    public void requestFocus() {
        NodeUtils.requestFocus(maskComboBox.getEditor(), () -> maskComboBox.getEditor().selectAll());
    }

    @Override
    protected void build() {
        super.build();
        maskComboBox.setEditable(true);
        caseSensitiveButton.getStyleClass().addAll(StyleClasses.SQUARE, Styles.FLAT, StyleClasses.SIZE_M);
        regexButton.getStyleClass().addAll(StyleClasses.SQUARE, Styles.FLAT, StyleClasses.SIZE_M);
        HBox.setHgrow(maskComboBox, Priority.ALWAYS);
        maskComboBox.setMaxWidth(Double.MAX_VALUE);
        maskComboBox.setItems(getViewModel().getMasks());
        maskBox.setSpacing(Spacing.getHorizontalThird());

        maskLabel.setMinWidth(Label.USE_PREF_SIZE);
        gridPane.add(maskLabel, 0, 0);
        gridPane.add(maskBox, 1, 0);
        GridPane.setHgrow(maskBox, Priority.ALWAYS);

        entryTypeLabel.setMinWidth(Label.USE_PREF_SIZE);
        GridPane.setValignment(entryTypeLabel, VPos.TOP);
        gridPane.add(entryTypeLabel, 0, 1);
        buildEntryTypeGrid();
        gridPane.add(entryTypeGridPane, 1, 1);
        GridPane.setHgrow(entryTypeGridPane, Priority.ALWAYS);

        fileTypeLabel.setMinWidth(Label.USE_PREF_SIZE);
        GridPane.setValignment(fileTypeLabel, VPos.TOP);
        gridPane.add(fileTypeLabel, 0, 2);
        buildFileTypeGrid();
        gridPane.add(fileTypeGridPane, 1, 2);
        GridPane.setHgrow(fileTypeGridPane, Priority.ALWAYS);

        gridPane.setHgap(Spacing.getHorizontal());
        gridPane.setVgap(Spacing.getVertical());
        VBox.setVgrow(gridPane, Priority.ALWAYS);
        getContentBox().getChildren().add(gridPane);
        getContentBox().setSpacing(Spacing.getVertical());

        registerButtons(cancelButton, applyButton, okButton);
        getButtonWidthGroup().add(cancelButton, applyButton, okButton);
        getFocusTrap().activate();
    }

    @Override
    protected void bind() {
        super.bind();
        var viewModel = getViewModel();
        maskComboBox.getEditor().textProperty().bindBidirectional(viewModel.maskProperty());
        caseSensitiveButton.selectedProperty().bindBidirectional(viewModel.caseSensitiveSelectedProperty());
        regexButton.selectedProperty().bindBidirectional(viewModel.regexSelectedProperty());
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        var viewModel = getViewModel();
        applySelections(checkBoxesByEntryType, viewModel.getSelectionsByEntryType());
        viewModel.getSelectionsByEntryType().addListener((MapChangeListener<FileEntryType, Boolean>) change -> {
            if (change.wasAdded()) {
                var checkBox = Objects.requireNonNull(checkBoxesByEntryType.get(change.getKey()));
                checkBox.setSelected(Objects.requireNonNull(change.getValueAdded()));
            }
        });
        applySelections(checkBoxesByFileType, viewModel.getSelectionsByFileType());
        viewModel.getSelectionsByFileType().addListener((MapChangeListener<FileType, Boolean>) change -> {
            if (change.wasAdded()) {
                var checkBox = Objects.requireNonNull(checkBoxesByFileType.get(change.getKey()));
                checkBox.setSelected(Objects.requireNonNull(change.getValueAdded()));
            }
        });
    }

    /**
     * Builds the grid with the entry types: Folder and Link in the first row, File and Other in the second one.
     */
    private void buildEntryTypeGrid() {
        entryTypeGridPane.setVgap(Spacing.getVertical());
        entryTypeGridPane.setHgap(Spacing.getHorizontal());
        addEntryTypeCheckBox(FileEntryType.DIRECTORY, 0, 0);
        addEntryTypeCheckBox(FileEntryType.LINK, 1, 0);
        addEntryTypeCheckBox(FileEntryType.FILE, 0, 1);
        addEntryTypeCheckBox(FileEntryType.OTHER, 1, 1);
        addEqualColumns(entryTypeGridPane);
    }

    private void addEntryTypeCheckBox(FileEntryType entryType, int column, int row) {
        var checkBox = new CheckBox(FileLabels.ofEntryType(entryType));
        checkBox.setOnAction(e -> getViewModel().onEntryTypeChanged(entryType, checkBox.isSelected()));
        checkBoxesByEntryType.put(entryType, checkBox);
        entryTypeGridPane.add(checkBox, column, row);
    }

    /**
     * Builds the grid with every file type laid out row by row.
     */
    private void buildFileTypeGrid() {
        fileTypeGridPane.setVgap(Spacing.getVertical());
        fileTypeGridPane.setHgap(Spacing.getHorizontal());
        var types = FileType.values();
        for (var i = 0; i < types.length; i++) {
            var type = types[i];
            var checkBox = new CheckBox(FileLabels.ofFileType(type));
            checkBox.setOnAction(e -> getViewModel().onFileTypeChanged(type, checkBox.isSelected()));
            checkBoxesByFileType.put(type, checkBox);
            fileTypeGridPane.add(checkBox, i % TYPE_COLUMN_COUNT, i / TYPE_COLUMN_COUNT);
        }
        addEqualColumns(fileTypeGridPane);
    }

    private void addEqualColumns(GridPane grid) {
        for (var i = 0; i < TYPE_COLUMN_COUNT; i++) {
            var column = new ColumnConstraints();
            column.setPercentWidth(100.0 / TYPE_COLUMN_COUNT);
            grid.getColumnConstraints().add(column);
        }
    }

    private <K> void applySelections(Map<K, CheckBox> checkBoxesByKey, Map<K, Boolean> selectionsByKey) {
        for (var entry : checkBoxesByKey.entrySet()) {
            entry.getValue().setSelected(Objects.requireNonNull(selectionsByKey.get(entry.getKey())));
        }
    }
}
