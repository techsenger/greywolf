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

package com.techsenger.greywolf.core.workspace;

import atlantafx.base.theme.Styles;
import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.core.slots.WorkspaceSlots;
import com.techsenger.greywolf.core.style.CoreIcons;
import com.techsenger.shellfx.core.ShellContext;
import com.techsenger.shellfx.core.registries.ControlBuilder;
import com.techsenger.shellfx.core.registries.Controls;
import com.techsenger.shellfx.layout.tabhost.TabHostView;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.style.Spacing;
import com.techsenger.shellfx.material.style.StyleClasses;
import com.techsenger.shellfx.material.tabpane.SlantedTabPanePro;
import com.techsenger.tabpanepro.core.skin.DragAndDropContext;
import com.techsenger.tabpanepro.core.skin.TabHeaderAreaPolicy;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

/**
 * The shell's top-level workspace, with tab drag-and-drop enabled so a tab can be pulled out of a {@code
 * DualPanelsTab}'s panel and dropped here as a top-level tab, and vice versa (see {@code PanelTabDockView}
 * for the panel side of this pairing) - unlike a plain {@code TabHostView(viewModel, true)}, which has
 * drag-and-drop disabled by default.
 *
 * @author Pavel Castornii
 */
public class WorkspaceView extends TabHostView<WorkspaceViewModel> {

    private final ShellContext context;

    private final Button newButton = new Button(null, new FontIconView(CoreIcons.ADD));

    @Nullable
    private Controls<WorkspaceView, ContextMenu> newMenuControls;

    public WorkspaceView(WorkspaceViewModel viewModel, ShellContext context) {
        super(viewModel, SlantedTabPanePro::new);
        this.context = context;
    }

    @Override
    protected void build() {
        super.build();
        // Owns the context every DualPanelsTab's dock host reads, so a tab can be dragged out of a panel and
        // dropped here as a top-level tab, and vice versa.
        getNode().setDragAndDropContext(new DragAndDropContext());
        getNode().setTabDragEnabled(true);
        getNode().setTabDropEnabled(true);
        var tabHeaderArea = getTabHeaderArea();
        var css = WorkspaceView.class.getResource("workspace.css").toExternalForm();
        getNode().getStylesheets().add(css);
        getNode().getStyleClass().add("workspace-tab-pane");
        newButton.getStyleClass().addAll(StyleClasses.SQUARE, Styles.FLAT, StyleClasses.SIZE_M, "workspace-new-button");
        newButton.setTooltip(new Tooltip("New"));
        var buttonBox = new HBox(newButton);
        buttonBox.setPadding(new Insets(1, 0, 0, Spacing.getHorizontalHalf()));
        buttonBox.setAlignment(Pos.CENTER);
        tabHeaderArea.getStickyArea().getChildren().add(buttonBox);
        // hidden until upgradeTabHeader() finds something in the popup
        newButton.setVisible(false);
        newButton.managedProperty().bind(newButton.visibleProperty());
    }

    @Override
    protected void unbuild() {
        deinitializeNewMenu();
        super.unbuild();
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        getViewModel().upgradeTabHeaderSource().addListener((v) -> upgradeTabHeader());
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        newButton.setOnAction(e -> showNewMenu());
    }

    /**
     * Rebuilds the &quot;New&quot; popup from the registries and updates the tab header to match: the &quot;+&quot;
     * button is shown only if the popup has items, and the header stays visible without tabs only then.
     */
    private void upgradeTabHeader() {
        deinitializeNewMenu();
        var controlBuilder = new ControlBuilder(context.getSlotRegistry(), context.getControlRegistry());
        var controls = controlBuilder.buildContextMenu(this, WorkspaceSlots.NewMenu.MENU);
        newMenuControls = controls;
        var hasItems = !controls.root().getItems().isEmpty();
        newButton.setVisible(hasItems);
        getTabHeaderArea().setPolicy(hasItems
                ? TabHeaderAreaPolicy.ALWAYS_VISIBLE : TabHeaderAreaPolicy.VISIBLE_WHEN_TABS_PRESENT);
    }

    private void deinitializeNewMenu() {
        if (newMenuControls != null) {
            newMenuControls.root().hide();
            newMenuControls.deinitializeAll(this);
            newMenuControls = null;
        }
    }

    private void showNewMenu() {
        if (newMenuControls != null) {
            newMenuControls.root().show(newButton, Side.BOTTOM, 0, 0);
        }
    }
}
