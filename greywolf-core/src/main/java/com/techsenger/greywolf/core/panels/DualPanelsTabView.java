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

package com.techsenger.greywolf.core.panels;

import com.google.errorprone.annotations.concurrent.LazyInit;
import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.core.GreyWolfShellPort;
import com.techsenger.greywolf.core.GreyWolfShellView;
import com.techsenger.greywolf.storage.ui.view.FileView;
import com.techsenger.greywolf.storage.ui.view.FileViewProvider;
import com.techsenger.patternfx.mvvm.ChildView;
import com.techsenger.patternfx.mvvm.ParentView;
import com.techsenger.shellfx.core.tab.AbstractHostTabView;
import com.techsenger.shellfx.core.tab.TabPort;
import com.techsenger.shellfx.core.tab.TabView;
import com.techsenger.shellfx.core.window.WindowType;
import com.techsenger.shellfx.core.window.WindowView;
import com.techsenger.shellfx.layout.dockhost.DockHostParams;
import com.techsenger.shellfx.layout.dockhost.DockHostView;
import com.techsenger.shellfx.layout.dockhost.DockHostViewModel;
import com.techsenger.shellfx.layout.dockhost.DockUserPermission;
import com.techsenger.shellfx.layout.dockhost.ModelNodeBuilder;
import com.techsenger.shellfx.layout.dockhost.TabDockView;
import com.techsenger.shellfx.layout.dockhost.UtilityDockContainerView;
import com.techsenger.tabpanepro.core.skin.DragAndDropContext;
import com.techsenger.toolkit.fx.value.ValueUtils;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public final class DualPanelsTabView extends AbstractHostTabView<DualPanelsTabViewModel>
        implements UtilityDockContainerView<DualPanelsTabViewModel> {

    public class Composer extends AbstractHostTabView<DualPanelsTabViewModel>.Composer
            implements DualPanelsTabComposer, FileViewProvider {

        public static final double DEFAULT_TAB_DOCK_SIZE = 250;

        @LazyInit
        private DockHostView<?> dockHost;

        @LazyInit
        private PanelTabDockView firstPanelTabDock;

        @LazyInit
        private PanelTabDockView secondPanelTabDock;

        private final ReadOnlyObjectWrapper<Panel> currentPanel = new ReadOnlyObjectWrapper<>();

        private final ReadOnlyObjectWrapper<@Nullable TabView<?>> currentTab = new ReadOnlyObjectWrapper<>();

        private final ChangeListener<? super ParentView<?>> focusedListener = (ov, oldV, newV) -> {
            if (getNode().isSelected()) {
                var current = newV;
                while (current != null && current != DualPanelsTabView.this) {
                    if (current == firstPanelTabDock) {
                        currentPanel.set(Panel.FIRST);
                        break;
                    } else if (current == secondPanelTabDock) {
                        currentPanel.set(Panel.SECOND);
                        break;
                    }
                    if (current instanceof WindowView<?> window
                            && window.getViewModel().getWindowType() == WindowType.TOP_LEVEL) {
                        // A top-level window (e.g. a dialog opened directly on the shell, outside any panel)
                        // is never part of this tab's own composer tree, and its Composer#getParent() throws
                        // for exactly that reason - stop here instead of calling it.
                        current = null;
                    } else if (current instanceof ChildView<?> child) {
                        current = child.getComposer().getParent();
                    } else {
                        current = null;
                    }
                }
            }
        };

        @Override
        public void compose() {
            super.compose();

            this.dockHost = createDockHost();
            getModifiableChildren().add(this.dockHost);
            VBox.setVgrow(this.dockHost.getNode(), Priority.ALWAYS);
            getContentBox().getChildren().add(this.dockHost.getNode());
            currentPanel.addListener((ov, oldV, newV) -> updateCurrentTab());

            ChangeListener<? super TabView<?>> tabListener = (ov, oldV, newV) -> {
                updateCurrentTab();
                updateAccelerators();
            };
            firstPanelTabDock = createPanelTabDock(Panel.FIRST);
            firstPanelTabDock.getComposer().selectedTabProperty().addListener(tabListener);

            secondPanelTabDock = createPanelTabDock(Panel.SECOND);
            secondPanelTabDock.getComposer().selectedTabProperty().addListener(tabListener);

            this.currentPanel.set(Panel.FIRST);
            ValueUtils.callAndAddListener(currentPanel, (ov, oldV, newV) -> updateActivePanel(newV));
            var modelRoot = ModelNodeBuilder.root(Orientation.HORIZONTAL, s -> s
                    .area(firstPanelTabDock, 0.5)
                    .area(secondPanelTabDock, 0.5));
            this.dockHost.getComposer().applyModel(modelRoot);
        }

        @Override
        public void addUtilityDock(TabDockView<?> tabDock) {
            this.dockHost.getComposer().addTabDock(tabDock, Side.BOTTOM, DEFAULT_TAB_DOCK_SIZE);
        }

        @Override
        public Panel getCurrentPanel() {
            return this.currentPanel.get();
        }

        @Override
        public ReadOnlyObjectProperty<Panel> currentPanelProperty() {
            return this.currentPanel.getReadOnlyProperty();
        }

        public @Nullable TabView<?> getSelectedTab(Panel panel) {
            var panelTabDock = getPanelTabDock(panel);
            var selectedTab = panelTabDock.getComposer().getSelectedTab();
            return selectedTab;
        }

        @Override
        public @Nullable TabPort getSelectedTabPort(Panel panel) {
            var tab = getSelectedTab(panel);
            return tab == null ? null : tab.getViewModel();
        }

        public PanelTabDockView getPanelTabDock(Panel panel) {
            if (panel == Panel.FIRST) {
                return this.firstPanelTabDock;
            } else {
                return this.secondPanelTabDock;
            }
        }

        @Override
        public PanelTabDockPort getPanelTabDockPort(Panel panel) {
            return getPanelTabDock(panel).getViewModel();
        }

        @Override
        public GreyWolfShellView getShell() {
            return (GreyWolfShellView) super.getShell();
        }

        @Override
        public GreyWolfShellPort getShellPort() {
            return (GreyWolfShellPort) super.getShellPort();
        }

        /**
         * The selected tab property in the current panel.
         */
        public ReadOnlyObjectProperty<@Nullable TabView<?>> currentTabProperty() {
            return this.currentTab.getReadOnlyProperty();
        }

        public @Nullable TabView<?> getCurrentTab() {
            return this.currentTab.get();
        }

        @Override
        public ObservableValue<@Nullable FileView<?>> observeFileView() {
            return currentTabProperty().flatMap(tab -> {
                if (tab != null && tab.getComposer() instanceof FileViewProvider provider) {
                    return provider.observeFileView();
                }
                return new SimpleObjectProperty<@Nullable FileView<?>>();
            });
        }

        @Override
        public @Nullable TabPort getCurrentTabPort() {
            var tab = getCurrentTab();
            return tab == null ? null : tab.getViewModel();
        }

        protected DockHostView<?> createDockHost() {
            var params = new DockHostParams(getViewModel().getConfig().getDockHost());
            var viewModel = new DockHostViewModel<>(params);
            var view = new DockHostView<>(viewModel) {
                @Override
                protected DragAndDropContext createDragAndDropContext() {
                    return getShell().getComposer().getWorkspace().getNode().getDragAndDropContext();
                }
            };
            view.initialize();
            // the panels are fixed: the user can only minimize them, new docks are created by the code
            viewModel.getUserPermissions().remove(DockUserPermission.CREATE);
            return view;
        }

        protected PanelTabDockView createPanelTabDock(Panel panel) {
            var panelConfig = panel == Panel.FIRST
                    ? getViewModel().getConfig().getFirstPanel()
                    : getViewModel().getConfig().getSecondPanel();
            var params = new PanelTabDockParams(panelConfig, panel);
            var viewModel = new PanelTabDockViewModel(params);
            var view = new PanelTabDockView(viewModel, DualPanelsTabView.this);
            view.getComposer().setDockHost(dockHost);
            view.initialize();
            viewModel.setDraggable(true);
            viewModel.setMinimizable(true);
            return view;
        }

        private void addListeners() {
            getShell().getComposer().focusedProperty().addListener(focusedListener);
        }

        private void removeListeners() {
            getShell().getComposer().focusedProperty().removeListener(focusedListener);
        }

        private void updateCurrentTab() {
            var panel = getCurrentPanel();
            var tabDock = getPanelTabDock(panel);
            var tab = tabDock.getComposer().getSelectedTab();
            this.currentTab.set(tab);
        }

        private void updateActivePanel(Panel panel) {
            firstPanelTabDock.getViewModel().setActive(panel == Panel.FIRST);
            secondPanelTabDock.getViewModel().setActive(panel == Panel.SECOND);
            updateAccelerators();
        }

        /**
         * Tells the shell that the components in use have changed: the areas of the active panel and of the selected
         * tabs give their accelerators, the others do not.
         */
        private void updateAccelerators() {
            if (getNode().isSelected()) {
                getShell().updateAccelerators();
            }
        }
    }

    public DualPanelsTabView(DualPanelsTabViewModel viewModel, GreyWolfShellView shell) {
        super(viewModel, shell);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new DualPanelsTabView.Composer();
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        getComposer().addListeners();
    }

    @Override
    protected void removeListeners() {
        super.removeListeners();
        getComposer().removeListeners();
    }
}
