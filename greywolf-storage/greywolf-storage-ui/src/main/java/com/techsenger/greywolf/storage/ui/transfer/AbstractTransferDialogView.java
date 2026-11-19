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
import com.techsenger.greywolf.storage.ui.style.StorageUiIcons;
import com.techsenger.shellfx.core.dialog.AbstractDialogView;
import com.techsenger.shellfx.core.registries.StylesheetRegistry;
import com.techsenger.shellfx.core.window.AbstractFrameWindowView;
import com.techsenger.shellfx.dialogs.alert.AlertDialogParams;
import com.techsenger.shellfx.dialogs.alert.AlertDialogPort;
import com.techsenger.shellfx.dialogs.alert.AlertDialogView;
import com.techsenger.shellfx.dialogs.alert.AlertDialogViewModel;
import com.techsenger.shellfx.material.button.ResultButton;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.style.Spacing;
import com.techsenger.shellfx.material.style.StyleClasses;
import com.techsenger.toolkit.fx.utils.NodeUtils;
import com.techsenger.toolkit.fx.value.ValueUtils;
import javafx.geometry.Insets;
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
 * Base view of a dialog that configures a transfer: the mask and the options, with a row between them for the
 * counterpart, the side of the transfer opposite to the one that started it.
 *
 * @param <VM> the view model type.
 * @author Pavel Castornii
 */
public abstract class AbstractTransferDialogView<VM extends AbstractTransferDialogViewModel<?>>
        extends AbstractDialogView<VM> {

    public class Composer extends AbstractFrameWindowView<VM>.Composer implements TransferDialogComposer {

        @Override
        public AlertDialogPort openAlertDialog(AlertDialogParams params) {
            var view = createAlertDialog(params);
            getParent().getComposer().addDialog(view);
            return view.getViewModel();
        }

        protected AlertDialogView<?> createAlertDialog(AlertDialogParams params) {
            var viewModel = new AlertDialogViewModel<>(params);
            var view = new AlertDialogView<>(viewModel, getStylesheetRegistry());
            view.initialize();
            return view;
        }
    }

    private final GridPane gridPane = new GridPane();

    private final Label infoLabel = new Label();

    private final Label maskLabel = new Label("Mask");

    private final ComboBox<String> maskComboBox = new ComboBox<>();

    private final ToggleButton caseSensitiveButton =
            new ToggleButton(null, new FontIconView(StorageUiIcons.MATCH_CASE));

    private final ToggleButton regexButton = new ToggleButton(null, new FontIconView(StorageUiIcons.REG_EXP));

    private final HBox maskButtonsBox = new HBox(caseSensitiveButton, regexButton);

    private final Label optionsLabel = new Label("Options");

    private final CheckBox followLinksCheckBox = new CheckBox("Follow links");

    private final CheckBox stableSymlinksCheckBox = new CheckBox("Stable symlinks");

    private final CheckBox preserveAttributesCheckBox = new CheckBox("Preserve attributes");

    private final CheckBox checkDataIntegrityCheckBox = new CheckBox("Check data integrity");

    private final GridPane optionsGridPane = new GridPane();

    private final ResultButton cancelButton = new ResultButton(TransferDialogButtons.CANCEL, "Cancel");

    private final ResultButton okButton = new ResultButton(TransferDialogButtons.OK, "OK");

    protected AbstractTransferDialogView(VM viewModel, StylesheetRegistry stylesheetRegistry) {
        super(viewModel, stylesheetRegistry);
    }

    @Override
    public void requestFocus() {
        NodeUtils.requestFocus(maskComboBox.getEditor(), () -> maskComboBox.getEditor().selectAll());
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    /**
     * Adds the controls of the counterpart, the side of the transfer opposite to the one that started it.
     *
     * @param gridPane the grid of the fields: the label goes to column 0 and the control to column 1.
     * @param row the row reserved for them.
     */
    protected abstract void buildCounterpartRow(GridPane gridPane, int row);

    @Override
    protected Composer createComposer() {
        return new Composer();
    }

    @Override
    protected void build() {
        super.build();
        maskComboBox.setEditable(true);

        maskComboBox.setMaxWidth(Double.MAX_VALUE);
        caseSensitiveButton.getStyleClass().addAll(StyleClasses.SQUARE, Styles.FLAT, StyleClasses.SIZE_M);
        regexButton.getStyleClass().addAll(StyleClasses.SQUARE, Styles.FLAT, StyleClasses.SIZE_M);
        maskButtonsBox.setSpacing(Spacing.getHorizontalThird());
        // negative left padding
        maskButtonsBox.setPadding(new Insets(0, 0, 0, Spacing.getHorizontalThird() - Spacing.getHorizontal()));
        maskLabel.setMinWidth(Label.USE_PREF_SIZE);
        gridPane.add(maskLabel, 0, 0);
        gridPane.add(maskComboBox, 1, 0);
        gridPane.add(maskButtonsBox, 2, 0);
        GridPane.setHgrow(maskComboBox, Priority.ALWAYS);

        buildCounterpartRow(gridPane, 1);

        optionsLabel.setMinWidth(Label.USE_PREF_SIZE);
        var optionsColumn1 = new ColumnConstraints();
        optionsColumn1.setPercentWidth(50);
        var optionsColumn2 = new ColumnConstraints();
        optionsColumn2.setPercentWidth(50);
        optionsGridPane.getColumnConstraints().addAll(optionsColumn1, optionsColumn2);
        optionsGridPane.setHgap(Spacing.getHorizontal());
        optionsGridPane.setVgap(Spacing.getVertical());
        optionsGridPane.add(followLinksCheckBox, 0, 0);
        optionsGridPane.add(stableSymlinksCheckBox, 1, 0);
        optionsGridPane.add(preserveAttributesCheckBox, 0, 1);
        optionsGridPane.add(checkDataIntegrityCheckBox, 1, 1);

        // todo:
        followLinksCheckBox.setDisable(true);
        stableSymlinksCheckBox.setDisable(true);
        preserveAttributesCheckBox.setDisable(true);
        checkDataIntegrityCheckBox.setDisable(true);

        gridPane.add(optionsLabel, 0, 2);
        gridPane.add(optionsGridPane, 1, 2);
        GridPane.setColumnSpan(optionsGridPane, 2);
        GridPane.setValignment(optionsLabel, VPos.TOP);

        gridPane.setHgap(Spacing.getHorizontal());
        gridPane.setVgap(Spacing.getVertical());

        var box = new VBox(infoLabel, gridPane);
        box.setSpacing(Spacing.getVertical());
        VBox.setVgrow(box, Priority.NEVER);
        getContentBox().getChildren().add(box);
        getContentBox().setSpacing(Spacing.getVertical());

        okButton.setDefaultButton(true);
        registerButtons(cancelButton, okButton);
        getButtonWidthGroup().add(cancelButton, okButton);
        getFocusTrap().activate();
    }

    @Override
    protected void bind() {
        super.bind();
        var viewModel = getViewModel();
        maskComboBox.setItems(viewModel.getMasks());
        caseSensitiveButton.selectedProperty().bindBidirectional(viewModel.caseSensitiveSelectedProperty());
        regexButton.selectedProperty().bindBidirectional(viewModel.regexSelectedProperty());
        followLinksCheckBox.selectedProperty().bindBidirectional(viewModel.followLinksSelectedProperty());
        stableSymlinksCheckBox.selectedProperty().bindBidirectional(viewModel.stableSymlinksSelectedProperty());
        preserveAttributesCheckBox.selectedProperty()
                .bindBidirectional(viewModel.preserveAttributesSelectedProperty());
        checkDataIntegrityCheckBox.selectedProperty()
                .bindBidirectional(viewModel.checkDataIntegritySelectedProperty());
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        var viewModel = getViewModel();
        ValueUtils.callAndAddListener(viewModel.titleProperty(),
                (ov, oldV, newV) -> updateInfo(newV, viewModel.getSourceFiles().size()));
        ValueUtils.callAndAddListener(viewModel.maskProperty(), (ov, oldV, newV) -> maskComboBox.setValue(newV));
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        var viewModel = getViewModel();
        maskComboBox.getEditor().textProperty().addListener((ov, oldV, newV) -> viewModel.setMask(newV));
    }

    private void updateInfo(String verb, int count) {
        infoLabel.setText(verb + " " + count + (count == 1 ? " object" : " objects") + ":");
    }
}
