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

package com.techsenger.greywolf.system.terminal.toolbar;

import com.google.errorprone.annotations.concurrent.LazyInit;
import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.core.panels.Placement;
import com.techsenger.greywolf.system.terminal.TerminalTabPort;
import com.techsenger.greywolf.system.terminal.TerminalTabView;
import com.techsenger.greywolf.system.terminal.area.TerminalAreaPort;
import com.techsenger.greywolf.system.terminal.slots.ToolBarSlots;
import com.techsenger.shellfx.core.area.AbstractAreaView;
import com.techsenger.shellfx.core.registries.ControlBuilder;
import com.techsenger.shellfx.core.registries.Controls;
import com.techsenger.shellfx.material.style.StyleClasses;
import com.techsenger.toolkit.fx.value.ValueUtils;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public final class ToolBarView extends AbstractAreaView<ToolBarViewModel> {

    public class Composer extends AbstractAreaView<ToolBarViewModel>.Composer implements ToolBarComposer {

        @LazyInit
        private TerminalAreaPort area;

        @Override
        public @Nullable TerminalTabView getParent() {
            return (TerminalTabView) super.getParent();
        }

        @Override
        public @Nullable TerminalTabPort getParentPort() {
            var parent = getParent();
            return parent == null ? null : parent.getViewModel();
        }

        @Override
        public TerminalAreaPort getAreaPort() {
            return area;
        }

        private void setAreaPort(TerminalAreaPort area) {
            this.area = area;
        }
    }

    private final ControlBuilder controlBuilder;

    private final VBox box = new VBox();

    @LazyInit
    private Controls<ToolBarView, ToolBar> controls;

    @LazyInit
    private ToolBar toolBar;

    public ToolBarView(ToolBarViewModel viewModel, ControlBuilder controlBuilder, TerminalAreaPort areaPort) {
        super(viewModel);
        this.controlBuilder = controlBuilder;
        getComposer().setAreaPort(areaPort);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    public VBox getNode() {
        return box;
    }

    @Override
    protected Composer createComposer() {
        return new ToolBarView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        this.controls = controlBuilder.buildToolBar(this, ToolBarSlots.TOOL_BAR);
        this.toolBar = controls.root();
        this.box.getChildren().setAll(toolBar);
    }

    @Override
    protected void unbuild() {
        controls.deinitializeAll(this);
        super.unbuild();
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        ValueUtils.callAndAddListener(getViewModel().placementProperty(),
                (ov, oldV, newV) -> updatePlacement(oldV, newV));
    }

    /**
     * Rebuilds the tool bar when the tab reaches the workspace and applies the style classes for the new placement.
     * The placement goes from {@code null} to a panel and then to the workspace, or from {@code null} straight to
     * the workspace, so the workspace is reached once. The rebuild is needed because the size of an icon is frozen
     * into the icon when it is first resolved, so the icons of the old tool bar would keep their panel size after
     * the buttons became larger. Ignores {@code null} (not attached), so a tab in transit is not rebuilt twice.
     */
    private void updatePlacement(@Nullable Placement oldPlacement, @Nullable Placement newPlacement) {
        if (newPlacement == null || newPlacement == oldPlacement) {
            return;
        }
        var workspace = newPlacement == Placement.WORKSPACE;
        if (workspace) {
            controls.deinitializeAll(this);
            controls = controlBuilder.buildToolBar(this, ToolBarSlots.TOOL_BAR);
            toolBar = controls.root();
            box.getChildren().setAll(toolBar);
        }
        var sizeClass = workspace ? StyleClasses.SIZE_L : StyleClasses.SIZE_M;
        toolBar.getItems().stream()
                .filter(item -> item instanceof Button)
                .forEach(item -> {
                    if (!item.getStyleClass().contains(sizeClass)) {
                        item.getStyleClass().add(sizeClass);
                    }
                });
        if (workspace && !toolBar.getStyleClass().contains(StyleClasses.BLEND)) {
            toolBar.getStyleClass().add(StyleClasses.BLEND);
        }
    }
}
