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

package com.techsenger.greywolf.core.internal.about;

import com.techsenger.greywolf.core.style.CoreIcons;
import com.techsenger.shellfx.core.dialog.AbstractDialogView;
import com.techsenger.shellfx.core.registries.StylesheetRegistry;
import com.techsenger.shellfx.material.button.ResultButton;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.style.Spacing;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 *
 * @author Pavel Castornii
 */
public final class AboutDialogView extends AbstractDialogView<AboutDialogViewModel<?>> {

    private static final double HEADER_SPACING = 3;

    private static final double SIDE_PADDING = 50;

    private static final String SITE_URL = "https://github.com/techsenger/greywolf";

    private static final String COPYRIGHT = "Copyright 2026 Pavel Castornii. For more information, visit the ";

    private static final String LICENSE = """
            GreyWolf is licensed under the Apache License, Version 2.0. It includes third-party libraries and \
            components that are distributed under their own licenses, so review those licenses as well before \
            using or redistributing this software.""";

    private static final String SUPPORT = "If you find GreyWolf useful, consider contributing to its development "
            + "or supporting the project financially.";

    private final Label titleLabel = new Label("GreyWolf");

    private final Label versionLabel = new Label();

    private final Text copyrightText = new Text(COPYRIGHT);

    private final Hyperlink siteLink = new Hyperlink("Project Page");

    private final Text periodText = new Text(".");

    private final TextFlow copyrightFlow = new TextFlow(copyrightText, siteLink, periodText);

    private final Label licenseLabel = new Label(LICENSE);

    private final Label supportLabel = new Label(SUPPORT);

    private final ResultButton contributeButton = new ResultButton(AboutDialogButtons.CONTRIBUTE, "Contribute",
            new FontIconView(CoreIcons.CONTRIBUTE));

    private final ResultButton donateButton = new ResultButton(AboutDialogButtons.DONATE, "Donate",
            new FontIconView(CoreIcons.DONATE));

    public AboutDialogView(AboutDialogViewModel<?> viewModel, StylesheetRegistry stylesheetRegistry) {
        super(viewModel, stylesheetRegistry);
    }

    @Override
    public void requestFocus() {
        contributeButton.requestFocus();
    }

    @Override
    protected void build() {
        super.build();
        var css = AboutDialogView.class.getResource("about-dialog.css").toExternalForm();
        getContentBox().getStylesheets().add(css);
        titleLabel.getStyleClass().add("about-dialog-title");
        versionLabel.getStyleClass().add("about-dialog-version");

        copyrightText.getStyleClass().add("about-dialog-text");
        periodText.getStyleClass().add("about-dialog-text");
        siteLink.getStyleClass().add("about-dialog-link");
        siteLink.setOnAction(e -> getViewModel().onOpenUrl(SITE_URL));
        // Without the preferred minimum height wrapped text is cut off with an ellipsis.
        copyrightFlow.setMinHeight(Region.USE_PREF_SIZE);
        licenseLabel.setWrapText(true);
        licenseLabel.setMinHeight(Region.USE_PREF_SIZE);
        licenseLabel.setMaxWidth(Double.MAX_VALUE);
        supportLabel.setWrapText(true);
        supportLabel.setMinHeight(Region.USE_PREF_SIZE);
        supportLabel.setMaxWidth(Double.MAX_VALUE);
        var textBox = new VBox(copyrightFlow, licenseLabel, supportLabel);
        textBox.setSpacing(Spacing.getVertical() * 2);

        // The distance between the lines of the header is the same as on the splash screen.
        var header = new VBox(titleLabel, versionLabel);
        header.setAlignment(Pos.CENTER);
        header.setSpacing(HEADER_SPACING);

        var content = getContentBox();
        content.setAlignment(Pos.CENTER);
        content.setSpacing(Spacing.getVertical() * 2);
        content.setPadding(new Insets(Spacing.getVertical(), Spacing.getHorizontal(), Spacing.getVertical(),
                Spacing.getHorizontal()));
        getBottomBox().setPadding(new Insets(Spacing.getVertical(), SIDE_PADDING, Spacing.getVertical(),
                SIDE_PADDING));
        content.getChildren().addAll(header, textBox);

        registerButtons(contributeButton, donateButton);
        getButtonWidthGroup().add(contributeButton, donateButton);
        getFocusTrap().activate();
    }

    @Override
    protected void bind() {
        super.bind();
        versionLabel.setText(getViewModel().getVersion());
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        getViewModel().visibleProperty().addListener((ov, oldV, newV) -> fitStageHeight());
    }

    /**
     * A top-level stage is sized inside show() without regard to the wrapped text, so once the stage is showing its
     * height is fitted to the content for the current width.
     */
    private void fitStageHeight() {
        var stage = getWindow();
        if (!stage.isShowing()) {
            return;
        }
        var root = getNode();
        root.applyCss();
        var height = root.prefHeight(stage.getScene().getWidth());
        stage.setHeight(height + stage.getHeight() - stage.getScene().getHeight());
    }
}
