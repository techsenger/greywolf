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

import com.techsenger.greywolf.storage.base.delete.DeleteProgress;
import com.techsenger.greywolf.storage.ui.operation.AbstractDetailsDialogView;
import com.techsenger.greywolf.storage.ui.operation.ProgressFormats;
import com.techsenger.greywolf.storage.ui.operation.ProgressViewUtils;
import com.techsenger.shellfx.core.registries.StylesheetRegistry;
import com.techsenger.shellfx.material.button.ResultButton;
import com.techsenger.shellfx.material.style.Spacing;
import com.techsenger.toolkit.fx.utils.NodeUtils;
import com.techsenger.toolkit.fx.value.ValueUtils;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public final class DeletingDialogView extends AbstractDetailsDialogView<DeletingDialogViewModel<?>> {

    private final GridPane mainGridPane = new GridPane();

    private final Label currentLabel = new Label("Deleting");

    private final Label currentValueLabel = new Label();

    /**
     * This box is required only to provide a minimum width equal to the details' minimum width.
     */
    private final HBox currentValueBox = new HBox(currentValueLabel);

    private final Label totalItemsLabel = new Label("Total Items");

    private final ProgressBar totalItemsBar = new ProgressBar(0);

    private final Label totalItemsPercentLabel = new Label();

    private final Label detailsLabel = new Label("Details");

    private final GridPane detailsGridPane = new GridPane();

    private final Label totalItemsAbsoluteLabel = new Label("Total Items");

    private final Label totalItemsAbsoluteValueLabel = new Label();

    private final Label totalSizeAbsoluteLabel = new Label("Total Size");

    private final Label totalSizeAbsoluteValueLabel = new Label();

    private final Label elapsedLabel = new Label("Elapsed");

    private final Label elapsedValueLabel = new Label();

    private final Label etaLabel = new Label("ETA");

    private final Label etaValueLabel = new Label();

    private final Label speedLabel = new Label("Speed");

    private final Label speedValueLabel = new Label();

    private final ResultButton cancelButton = new ResultButton(DeletingDialogButtons.CANCEL, "Cancel");

    public DeletingDialogView(DeletingDialogViewModel<?> viewModel, StylesheetRegistry stylesheetRegistry) {
        super(viewModel, stylesheetRegistry);
    }

    @Override
    public void requestFocus() {
        NodeUtils.requestFocus(cancelButton);
    }

    @Override
    protected void build() {
        super.build();
        var css = ProgressFormats.class.getResource("progress-dialog.css").toExternalForm();
        getContentBox().getStylesheets().add(css);

        ProgressViewUtils.pinWidth(currentLabel, totalItemsLabel, detailsLabel);
        ProgressViewUtils.setupPathLabel(currentValueLabel);
        mainGridPane.add(currentLabel, 0, 0);
        mainGridPane.add(currentValueBox, 1, 0);
        HBox.setHgrow(currentValueLabel, Priority.ALWAYS);
        GridPane.setHgrow(currentValueBox, Priority.ALWAYS);

        ProgressViewUtils.setupBar(totalItemsBar);
        ProgressViewUtils.setupPercentLabel(totalItemsPercentLabel);
        mainGridPane.add(totalItemsLabel, 0, 1);
        mainGridPane.add(totalItemsBar, 1, 1);
        mainGridPane.add(totalItemsPercentLabel, 2, 1);

        GridPane.setValignment(detailsLabel, VPos.TOP);
        buildDetailsGridPane();
        GridPane.setHgrow(detailsGridPane, Priority.ALWAYS);

        mainGridPane.setHgap(Spacing.getHorizontal());
        mainGridPane.setVgap(Spacing.getVertical());
        mainGridPane.setPadding(new Insets(0, 0, Spacing.getVertical(), 0));
        getContentBox().getChildren().add(mainGridPane);

        registerButtons(cancelButton);
        getButtonWidthGroup().add(cancelButton);
        getFocusTrap().activate();
    }

    @Override
    protected void addDetails() {
        mainGridPane.add(detailsLabel, 0, 2);
        mainGridPane.add(detailsGridPane, 1, 2);
    }

    @Override
    protected void removeDetails() {
        mainGridPane.getChildren().removeAll(detailsLabel, detailsGridPane);
    }

    @Override
    protected void fitToDetails() {
        // Measured with the details in the layout, so the .size-value-label em-based min-width has real
        // CSS/font metrics behind it; applied to currentValueBox so it keeps this width even once details are
        // removed, instead of the dialog shrinking every time they are hidden.
        detailsGridPane.applyCss();
        currentValueBox.setMinWidth(detailsGridPane.prefWidth(-1));
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        ValueUtils.callAndAddListener(getViewModel().progressProperty(), (ov, oldV, newV) -> {
            if (newV != null) {
                applyProgress(newV);
            }
        });
    }

    /**
     * Lays each pair out over columns 0-1 and 3-4, leaving column 2 as an empty, elastic gap, so the second pair
     * doesn't crowd the first as the dialog widens.
     */
    private void buildDetailsGridPane() {
        VBox.setVgrow(detailsGridPane, Priority.ALWAYS);
        detailsGridPane.setHgap(Spacing.getHorizontal());
        detailsGridPane.setVgap(Spacing.getVertical());
        ProgressViewUtils.pinWidth(totalItemsAbsoluteLabel, totalItemsAbsoluteValueLabel, totalSizeAbsoluteLabel,
                totalSizeAbsoluteValueLabel, elapsedLabel, elapsedValueLabel, etaLabel, etaValueLabel, speedLabel,
                speedValueLabel);
        totalSizeAbsoluteValueLabel.getStyleClass().add("size-value-label");

        var spacerColumn = new ColumnConstraints();
        spacerColumn.setHgrow(Priority.ALWAYS);
        detailsGridPane.getColumnConstraints().addAll(new ColumnConstraints(), new ColumnConstraints(),
                spacerColumn);

        detailsGridPane.add(totalItemsAbsoluteLabel, 0, 0);
        detailsGridPane.add(totalItemsAbsoluteValueLabel, 1, 0);
        detailsGridPane.add(totalSizeAbsoluteLabel, 3, 0);
        detailsGridPane.add(totalSizeAbsoluteValueLabel, 4, 0);

        detailsGridPane.add(elapsedLabel, 0, 1);
        detailsGridPane.add(elapsedValueLabel, 1, 1);
        detailsGridPane.add(etaLabel, 3, 1);
        detailsGridPane.add(etaValueLabel, 4, 1);

        detailsGridPane.add(speedLabel, 0, 2);
        detailsGridPane.add(speedValueLabel, 1, 2);
    }

    private void applyProgress(DeleteProgress progress) {
        currentValueLabel.setText(progress.currentUri().toString());

        var itemsFraction = ProgressFormats.fraction(progress.itemsDone(), progress.totalItems());
        totalItemsBar.setProgress(itemsFraction);
        totalItemsPercentLabel.setText(ProgressFormats.formatPercent(itemsFraction));

        totalItemsAbsoluteValueLabel.setText(progress.itemsDone() + " / " + progress.totalItems());
        totalSizeAbsoluteValueLabel.setText(ProgressFormats.formatBytesPair(progress.bytesDone(),
                progress.totalBytes()));
        elapsedValueLabel.setText(ProgressFormats.formatDuration(progress.elapsedMillis()));
        etaValueLabel.setText(ProgressFormats.formatDuration(progress.estimatedRemainingMillis()));
        speedValueLabel.setText(String.format("%.1f items/s", progress.itemsPerSecond()));
    }
}
