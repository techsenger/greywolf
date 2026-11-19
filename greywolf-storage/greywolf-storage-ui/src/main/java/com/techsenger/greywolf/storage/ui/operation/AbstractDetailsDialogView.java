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
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.style.StyleClasses;
import com.techsenger.shellfx.shared.style.SharedIcons;
import com.techsenger.toolkit.fx.value.ValueUtils;
import javafx.scene.control.Button;

/**
 * Base view of a dialog with a collapsible details section: adds the toggle button and shows or hides the section
 * through the subclass's layout.
 *
 * @param <V> the view model type.
 * @author Pavel Castornii
 */
public abstract class AbstractDetailsDialogView<V extends AbstractDetailsDialogViewModel<?>>
        extends AbstractDialogView<V> {

    private final Button detailsButton = new Button("Details", new FontIconView(SharedIcons.CHEVRON_DOWN));

    private boolean fittedToDetails;

    protected AbstractDetailsDialogView(V viewModel, StylesheetRegistry stylesheetRegistry) {
        super(viewModel, stylesheetRegistry);
    }

    /**
     * Puts the details section into the dialog's layout; called whenever it becomes visible.
     */
    protected abstract void addDetails();

    /**
     * Takes the details section out of the dialog's layout; called whenever it becomes hidden.
     */
    protected abstract void removeDetails();

    /**
     * Lets the subclass size the dialog to the details section, so collapsing it later doesn't shrink the
     * dialog. Called once, when the dialog gets a scene, with the section temporarily in the layout.
     */
    protected abstract void fitToDetails();

    @Override
    protected void build() {
        super.build();
        detailsButton.getStyleClass().addAll(StyleClasses.SIZE_L, Styles.FLAT);
        getLeftBottomBox().getChildren().add(detailsButton);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        ValueUtils.callAndAddListener(getViewModel().detailsVisibleProperty(), (ov, oldV, newV) ->
                updateDetailsVisible(newV));
        getNode().sceneProperty().addListener((ov, oldV, newV) -> {
            if (newV != null && !fittedToDetails) {
                fittedToDetails = true;
                var hidden = !getViewModel().isDetailsVisible();
                if (hidden) {
                    addDetails();
                }
                fitToDetails();
                if (hidden) {
                    removeDetails();
                }
            }
        });
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        detailsButton.setOnAction(e -> getViewModel().onDetails());
    }

    private void updateDetailsVisible(boolean visible) {
        detailsButton.setGraphic(new FontIconView(visible ? SharedIcons.CHEVRON_UP : SharedIcons.CHEVRON_DOWN));
        if (visible) {
            addDetails();
        } else {
            removeDetails();
        }
    }
}
