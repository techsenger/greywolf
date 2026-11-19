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

import com.techsenger.greywolf.storage.base.transfer.TransferProgress;
import com.techsenger.greywolf.storage.ui.operation.AbstractDetailsDialogView;
import com.techsenger.greywolf.storage.ui.operation.ProgressFormats;
import com.techsenger.greywolf.storage.ui.operation.ProgressViewUtils;
import com.techsenger.shellfx.core.registries.StylesheetRegistry;
import com.techsenger.shellfx.material.button.ResultButton;
import com.techsenger.shellfx.material.style.Spacing;
import com.techsenger.toolkit.core.file.FileUtils;
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
public final class TransferringDialogView extends AbstractDetailsDialogView<TransferringDialogViewModel<?>> {

    private final GridPane mainGridPane = new GridPane();

    private final Label fromLabel = new Label("From");

    private final Label fromValueLabel = new Label();

    private final Label toLabel = new Label("To");

    private final Label toValueLabel = new Label();

    /**
     * This box is required only to provide a minimum width equal to the details' minimum width.
     */
    private final HBox toValueBox = new HBox(toValueLabel);

    private final Label currentFileLabel = new Label("Current File");

    private final ProgressBar currentFileBar = new ProgressBar(0);

    private final Label currentFilePercentLabel = new Label();

    private final Label totalSizeLabel = new Label("Total Size");

    private final ProgressBar totalSizeBar = new ProgressBar(0);

    private final Label totalSizePercentLabel = new Label();

    private final Label detailsLabel = new Label("Details");

    private final GridPane detailsGridPane = new GridPane();

    private final Label fileSizeLabel = new Label("File Size");

    private final Label fileSizeValueLabel = new Label();

    private final Label fileEtaLabel = new Label("File ETA");

    private final Label fileEtaValueLabel = new Label();

    private final Label totalFilesAbsoluteLabel = new Label("Total Files");

    private final Label totalFilesAbsoluteValueLabel = new Label();

    private final Label totalSizeAbsoluteLabel = new Label("Total Size");

    private final Label totalSizeAbsoluteValueLabel = new Label();

    private final Label elapsedLabel = new Label("Elapsed");

    private final Label elapsedValueLabel = new Label();

    private final Label totalEtaLabel = new Label("Total ETA");

    private final Label totalEtaValueLabel = new Label();

    private final Label currentSpeedLabel = new Label("Current Speed");

    private final Label currentSpeedValueLabel = new Label();

    private final Label averageSpeedLabel = new Label("Average Speed");

    private final Label averageSpeedValueLabel = new Label();

    private final ResultButton cancelButton = new ResultButton(TransferringDialogButtons.CANCEL, "Cancel");

    public TransferringDialogView(TransferringDialogViewModel<?> viewModel, StylesheetRegistry stylesheetRegistry) {
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

        fromLabel.setMinWidth(Label.USE_PREF_SIZE);
        ProgressViewUtils.setupPathLabel(fromValueLabel);
        mainGridPane.add(fromLabel, 0, 0);
        mainGridPane.add(fromValueLabel, 1, 0);
        GridPane.setHgrow(fromValueLabel, Priority.ALWAYS);

        toLabel.setMinWidth(Label.USE_PREF_SIZE);
        ProgressViewUtils.setupPathLabel(toValueLabel);
        mainGridPane.add(toLabel, 0, 1);
        mainGridPane.add(toValueBox, 1, 1);
        HBox.setHgrow(toValueLabel, Priority.ALWAYS);
        GridPane.setHgrow(toValueBox, Priority.ALWAYS);

        currentFileLabel.setMinWidth(Label.USE_PREF_SIZE);
        ProgressViewUtils.setupBar(currentFileBar);
        ProgressViewUtils.setupPercentLabel(currentFilePercentLabel);
        mainGridPane.add(currentFileLabel, 0, 2);
        mainGridPane.add(currentFileBar, 1, 2);
        mainGridPane.add(currentFilePercentLabel, 2, 2);

        totalSizeLabel.setMinWidth(Label.USE_PREF_SIZE);
        ProgressViewUtils.setupBar(totalSizeBar);
        ProgressViewUtils.setupPercentLabel(totalSizePercentLabel);
        mainGridPane.add(totalSizeLabel, 0, 3);
        mainGridPane.add(totalSizeBar, 1, 3);
        mainGridPane.add(totalSizePercentLabel, 2, 3);

        detailsLabel.setMinWidth(Label.USE_PREF_SIZE);
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
        mainGridPane.add(detailsLabel, 0, 4);
        mainGridPane.add(detailsGridPane, 1, 4);
    }

    @Override
    protected void removeDetails() {
        mainGridPane.getChildren().removeAll(detailsLabel, detailsGridPane);
    }

    @Override
    protected void fitToDetails() {
        // Measured with the details in the layout, so the .size-value-label em-based min-width has real
        // CSS/font metrics behind it; applied to toValueBox so it keeps this width even once details are
        // removed, instead of the dialog shrinking every time they are hidden.
        detailsGridPane.applyCss();
        toValueBox.setMinWidth(detailsGridPane.prefWidth(-1));
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
     * Lays each pair out over columns 0-1 and 3-4, leaving column 2 as an empty, elastic gap (see
     * {@link #build()}'s {@code spacerColumn}) so the second pair doesn't crowd the first as the dialog widens.
     */
    private void buildDetailsGridPane() {
        VBox.setVgrow(detailsGridPane, Priority.ALWAYS);
        detailsGridPane.setHgap(Spacing.getHorizontal());
        detailsGridPane.setVgap(Spacing.getVertical());
        ProgressViewUtils.pinWidth(fileSizeLabel, fileSizeValueLabel, fileEtaLabel, fileEtaValueLabel,
                totalFilesAbsoluteLabel, totalFilesAbsoluteValueLabel, totalSizeAbsoluteLabel,
                totalSizeAbsoluteValueLabel, elapsedLabel, elapsedValueLabel, totalEtaLabel, totalEtaValueLabel,
                currentSpeedLabel, currentSpeedValueLabel, averageSpeedLabel, averageSpeedValueLabel);
        fileSizeValueLabel.getStyleClass().add("size-value-label");
        totalSizeAbsoluteValueLabel.getStyleClass().add("size-value-label");

        var spacerColumn = new ColumnConstraints();
        spacerColumn.setHgrow(Priority.ALWAYS);
        detailsGridPane.getColumnConstraints().addAll(new ColumnConstraints(), new ColumnConstraints(),
                spacerColumn);

        detailsGridPane.add(fileSizeLabel, 0, 0);
        detailsGridPane.add(fileSizeValueLabel, 1, 0);
        detailsGridPane.add(fileEtaLabel, 3, 0);
        detailsGridPane.add(fileEtaValueLabel, 4, 0);

        detailsGridPane.add(totalFilesAbsoluteLabel, 0, 1);
        detailsGridPane.add(totalFilesAbsoluteValueLabel, 1, 1);
        detailsGridPane.add(totalSizeAbsoluteLabel, 3, 1);
        detailsGridPane.add(totalSizeAbsoluteValueLabel, 4, 1);

        detailsGridPane.add(elapsedLabel, 0, 2);
        detailsGridPane.add(elapsedValueLabel, 1, 2);
        detailsGridPane.add(totalEtaLabel, 3, 2);
        detailsGridPane.add(totalEtaValueLabel, 4, 2);

        detailsGridPane.add(currentSpeedLabel, 0, 3);
        detailsGridPane.add(currentSpeedValueLabel, 1, 3);
        detailsGridPane.add(averageSpeedLabel, 3, 3);
        detailsGridPane.add(averageSpeedValueLabel, 4, 3);
    }

    private void applyProgress(TransferProgress progress) {
        fromValueLabel.setText(progress.currentSource().toString());
        toValueLabel.setText(progress.currentTarget().toString());

        var currentFileFraction = ProgressFormats.fraction(progress.currentFileBytesDone(),
                progress.currentFileBytesTotal());
        currentFileBar.setProgress(currentFileFraction);
        currentFilePercentLabel.setText(ProgressFormats.formatPercent(currentFileFraction));

        var sizeFraction = ProgressFormats.fraction(progress.bytesDone(), progress.totalBytes());
        totalSizeBar.setProgress(sizeFraction);
        totalSizePercentLabel.setText(ProgressFormats.formatPercent(sizeFraction));

        fileSizeValueLabel.setText(ProgressFormats.formatBytesPair(progress.currentFileBytesDone(),
                progress.currentFileBytesTotal()));
        var currentFileRemainingBytes = progress.currentFileBytesTotal() - progress.currentFileBytesDone();
        var fileEtaMillis = progress.currentBytesPerSecond() <= 0 ? -1L
                : currentFileRemainingBytes * 1000L / progress.currentBytesPerSecond();
        fileEtaValueLabel.setText(ProgressFormats.formatDuration(fileEtaMillis));
        totalFilesAbsoluteValueLabel.setText(progress.filesDone() + " / " + progress.totalFiles());
        totalSizeAbsoluteValueLabel.setText(ProgressFormats.formatBytesPair(progress.bytesDone(),
                progress.totalBytes()));
        elapsedValueLabel.setText(ProgressFormats.formatDuration(progress.elapsedMillis()));
        totalEtaValueLabel.setText(ProgressFormats.formatDuration(progress.estimatedRemainingMillis()));
        currentSpeedValueLabel.setText(FileUtils.formatSize(progress.currentBytesPerSecond()) + "/s");
        averageSpeedValueLabel.setText(FileUtils.formatSize(progress.averageBytesPerSecond()) + "/s");
    }
}
