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

package com.techsenger.greywolf.system.terminal;

import com.google.errorprone.annotations.concurrent.LazyInit;
import com.techsenger.greywolf.core.GreyWolfShellView;
import com.techsenger.greywolf.core.panels.PlacementResolver;
import com.techsenger.greywolf.system.terminal.area.TerminalAreaParams;
import com.techsenger.greywolf.system.terminal.area.TerminalAreaPort;
import com.techsenger.greywolf.system.terminal.area.TerminalAreaView;
import com.techsenger.greywolf.system.terminal.area.TerminalAreaViewModel;
import com.techsenger.greywolf.system.terminal.toolbar.ToolBarParams;
import com.techsenger.greywolf.system.terminal.toolbar.ToolBarPort;
import com.techsenger.greywolf.system.terminal.toolbar.ToolBarView;
import com.techsenger.greywolf.system.terminal.toolbar.ToolBarViewModel;
import com.techsenger.shellfx.core.ShellView;
import com.techsenger.shellfx.core.registries.ControlBuilder;
import com.techsenger.shellfx.core.tab.AbstractTabView;
import com.techsenger.shellfx.core.tab.TabContainerView;
import com.techsenger.shellfx.layout.dockhost.DockHostParams;
import com.techsenger.shellfx.layout.dockhost.DockHostView;
import com.techsenger.shellfx.layout.dockhost.DockHostViewModel;
import com.techsenger.shellfx.layout.dockhost.ModelNodeBuilder;
import com.techsenger.shellfx.layout.dockhost.TabDockView;
import com.techsenger.shellfx.layout.dockhost.UtilityDockContainerView;
import javafx.geometry.Orientation;
import javafx.geometry.Side;

/**
 *
 * @author Pavel Castornii
 */
public final class TerminalTabView extends AbstractTabView<TerminalTabViewModel>
        implements UtilityDockContainerView<TerminalTabViewModel> {

    public class Composer extends AbstractTabView<TerminalTabViewModel>.Composer implements TerminalTabComposer {

        @LazyInit
        private TabContainerView<?> tabContainer;

        @LazyInit
        private ToolBarView toolBar;

        @LazyInit
        private DockHostView<?> layout;

        @LazyInit
        private TerminalAreaView area;

        @Override
        public void compose() {
            area = createArea();
            toolBar = createToolBar(area.getViewModel());
            getModifiableChildren().add(toolBar);
            getContentBox().getChildren().add(toolBar.getNode());

            layout = createLayout();
            getModifiableChildren().add(layout);
            getContentBox().getChildren().add(layout.getNode());

            var modelRoot = ModelNodeBuilder.root(Orientation.HORIZONTAL, s -> s.mainArea(area));
            layout.getComposer().applyModel(modelRoot);

            // Fires whenever this tab is dragged into/out of a panel's dock (see PanelTabDockView's tab
            // drag-and-drop): getModifiableChildren().add(...) elsewhere reparents this tab's view on every
            // such move, which is exactly what drives parentProperty().
            parentProperty().addListener((ov, oldV, newV) ->
                    getViewModel().placementWrapper().set(PlacementResolver.ofContainer(getParent())));
        }

        public ToolBarView getToolBar() {
            return toolBar;
        }

        public DockHostView<?> getLayout() {
            return layout;
        }

        public TerminalAreaView getArea() {
            return area;
        }

        @Override
        public ToolBarPort getToolBarPort() {
            return toolBar.getViewModel();
        }

        @Override
        public TerminalAreaPort getAreaPort() {
            return area.getViewModel();
        }

        @Override
        public void openNewTerminal(String path) {
            var terminal = createTerminal(path);
            tabContainer.getComposer().addTab(terminal);
        }

        @Override
        public void addUtilityDock(TabDockView<?> dock) {
            layout.getComposer().addTabDock(dock, Side.BOTTOM, 250);
        }

        @Override
        public GreyWolfShellView getShell() {
            return (GreyWolfShellView) super.getShell();
        }

        private ToolBarView createToolBar(TerminalAreaPort areaPort) {
            var params = new ToolBarParams();
            var viewModel = new ToolBarViewModel(params);
            var context = getShell().getContext();
            var builder = new ControlBuilder(context.getSlotRegistry(), context.getControlRegistry());
            var toolBarView = new ToolBarView(viewModel, builder, areaPort);
            toolBarView.initialize();
            return toolBarView;
        }

        private DockHostView<?> createLayout() {
            var params = new DockHostParams(getViewModel().getConfig().getDockHost());
            var viewModel = new DockHostViewModel<>(params);
            var dockHostView = new DockHostView<>(viewModel);
            dockHostView.initialize();
            return dockHostView;
        }

        private TerminalTabView createTerminal(String path) {
            var params = new TerminalTabParams(getViewModel().getConfig(), path);
            var viewModel = new TerminalTabViewModel(params);
            var terminalView = new TerminalTabView(viewModel, getShell(), tabContainer);
            terminalView.initialize();
            return terminalView;
        }

        private TerminalAreaView createArea() {
            var params = new TerminalAreaParams(getViewModel().getConfig().getArea(),
                    getShell().getContext().getSettings(), getViewModel(), getViewModel().getPath());
            var viewModel = new TerminalAreaViewModel(params);
            var areaView = new TerminalAreaView(viewModel);
            areaView.initialize();
            return areaView;
        }
    }

    public TerminalTabView(TerminalTabViewModel viewModel, ShellView<?> shell, TabContainerView<?> tabContainer) {
        super(viewModel, shell);
        getComposer().tabContainer = tabContainer;
    }

    @Override
    public void requestFocus() {
        getComposer().area.requestFocus();
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new TerminalTabView.Composer();
    }
}
