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

package com.techsenger.greywolf.system.navigator;

import com.google.errorprone.annotations.concurrent.LazyInit;
import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.core.GreyWolfShellPort;
import com.techsenger.greywolf.core.GreyWolfShellView;
import com.techsenger.greywolf.core.panels.PanelTabDockPort;
import com.techsenger.greywolf.core.panels.PanelTabDockView;
import com.techsenger.greywolf.core.panels.Placement;
import com.techsenger.greywolf.core.panels.PlacementResolver;
import com.techsenger.greywolf.system.navigator.area.NavigatorAreaParams;
import com.techsenger.greywolf.system.navigator.area.NavigatorAreaPort;
import com.techsenger.greywolf.system.navigator.area.NavigatorAreaView;
import com.techsenger.greywolf.system.navigator.area.NavigatorAreaViewModel;
import com.techsenger.greywolf.system.navigator.location.LocationTabParams;
import com.techsenger.greywolf.system.navigator.location.LocationTabPort;
import com.techsenger.greywolf.system.navigator.location.LocationTabView;
import com.techsenger.greywolf.system.navigator.location.LocationTabViewModel;
import com.techsenger.greywolf.system.navigator.toolbar.ToolBarParams;
import com.techsenger.greywolf.system.navigator.toolbar.ToolBarPort;
import com.techsenger.greywolf.system.navigator.toolbar.ToolBarView;
import com.techsenger.greywolf.system.navigator.toolbar.ToolBarViewModel;
import com.techsenger.greywolf.shared.location.LocationBarParams;
import com.techsenger.greywolf.shared.location.LocationBarPort;
import com.techsenger.greywolf.shared.location.LocationBarView;
import com.techsenger.greywolf.shared.location.LocationBarViewModel;
import com.techsenger.greywolf.storage.ui.view.FileView;
import com.techsenger.greywolf.storage.ui.view.FileViewProvider;
import com.techsenger.shellfx.core.area.AreaParams;
import com.techsenger.shellfx.core.dialog.DialogView;
import com.techsenger.shellfx.core.registries.ControlBuilder;
import com.techsenger.shellfx.core.tab.AbstractHostTabView;
import com.techsenger.shellfx.layout.dockhost.DockHostParams;
import com.techsenger.shellfx.layout.dockhost.DockHostView;
import com.techsenger.shellfx.layout.dockhost.DockHostViewModel;
import com.techsenger.shellfx.layout.dockhost.ModelNodeBuilder;
import com.techsenger.shellfx.layout.dockhost.TabDockComposer;
import com.techsenger.shellfx.layout.dockhost.TabDockPort;
import com.techsenger.shellfx.layout.dockhost.TabDockView;
import com.techsenger.shellfx.layout.dockhost.TabDockViewModel;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public final class NavigatorTabView extends AbstractHostTabView<NavigatorTabViewModel> {

    public class Composer extends AbstractHostTabView<NavigatorTabViewModel>.Composer
            implements NavigatorTabComposer, FileViewProvider {

        private static final double ACCESSORY_TAB_DOCK_SIZE = 250;

        private static final double ACCESSORY_SPLIT_POSITION = 0.25;

        @LazyInit
        private ToolBarView toolBar;

        @LazyInit
        private LocationBarView<?> locationBar;

        @LazyInit
        private NavigatorAreaView area;

        private @Nullable DockHostView<?> layout;

        private @Nullable TabDockView<?> accessoryTabDock;

        private @Nullable SplitPane accessorySplitPane;

        private final ReadOnlyObjectWrapper<@Nullable LocationTabView> locationTab =
                new ReadOnlyObjectWrapper<>();

        @Override
        public void compose() {
            super.compose();

            this.locationBar = createLocationBar();
            this.locationBar.getViewModel().setShowStorageIcon(false);
            // the path is shown without waiting for the first directory to be read; it is set before the listener
            // below, as the area, which opens the directory itself, needs no notice about it and doesn't exist yet
            this.locationBar.getViewModel().setDirectory(getViewModel().getInitialDirectory());
            this.locationBar.getViewModel().directoryProperty().addListener((ov, oldV, newV) -> {
                if (newV != null) {
                    area.getViewModel().setDirectory(newV);
                }
            });
            getModifiableChildren().add(locationBar);
            getContentBox().getChildren().add(0, this.locationBar.getNode());

            this.area = createArea();

            this.toolBar = createToolBar(area.getViewModel());
            getModifiableChildren().add(toolBar);
            getContentBox().getChildren().add(0, this.toolBar.getNode());

            // Fires whenever this tab is dragged into/out of a panel's dock (see PanelTabDockView's tab
            // drag-and-drop, and the initial attach once this tab is first added to whichever container it
            // opens in): getModifiableChildren().add(...) elsewhere reparents this tab's view on every such
            // move, which is exactly what drives parentProperty().
            parentProperty().addListener((ov, oldV, newV) -> {
                getViewModel().placementWrapper().set(PlacementResolver.ofContainer(getParent()));
            });
        }

        @Override
        public void layout(@Nullable Placement placement) {
            if (placement == Placement.WORKSPACE) {
                layout = createLayout();
                getModifiableChildren().add(layout);
                // Releases area from this tab's own direct ownership (see the bare panel branch
                // below), a no-op if it was never there - applyModel below gives it a new parent (dockHost),
                // and a ChildView must have exactly one parent at a time.
                getModifiableChildren().remove(area);
                var modelRoot = ModelNodeBuilder.root(Orientation.HORIZONTAL, s -> s.mainArea(area));
                layout.getComposer().applyModel(modelRoot);
                getContentBox().getChildren().remove(area.getNode());
                getContentBox().getChildren().add(layout.getNode());
                promoteEmbeddedAccessory(layout);
            } else if (placement != null && placement.isPanel()) {
                getModifiableChildren().add(area);
                getContentBox().getChildren().add(area.getNode());
            }
            if (accessoryTabDock != null) {
                applyAccessoryChrome(accessoryTabDock);
            }
        }

        @Override
        public @Nullable LocationTabPort getLocationTabPort() {
            var tab = getLocationTab();
            return tab == null ? null : tab.getViewModel();
        }

        public @Nullable LocationTabView getLocationTab() {
            return locationTab.get();
        }

        public ReadOnlyObjectProperty<@Nullable LocationTabView> locationTabProperty() {
            return locationTab.getReadOnlyProperty();
        }

        @Override
        public LocationTabPort openLocationTab(LocationTabParams params) {
            var tab = getLocationTab();
            if (tab == null) {
                tab = createLocationTab(params);
                LocationTabPort port = tab.getViewModel();
                port.directoryProperty().addListener((ov, oldV, newV) -> {
                    if (newV != null) {
                        getAreaPort().setDirectory(newV);
                    }
                });
                port.setOnClosed(() -> this.locationTab.set(null));
                ensureAccessoryTabDock().getComposer().addTab(tab);
                this.locationTab.set(tab);
            }
            return tab.getViewModel();
        }

        public NavigatorAreaView getArea() {
            return area;
        }

        @Override
        public ObservableValue<@Nullable FileView<?>> observeFileView() {
            return new SimpleObjectProperty<@Nullable FileView<?>>(area);
        }

        @Override
        public NavigatorAreaPort getAreaPort() {
            return area.getViewModel();
        }

        @Override
        public @Nullable PanelTabDockPort getPanelTabDockPort() {
            var dock = getPanelTabDock();
            return dock == null ? null : dock.getViewModel();
        }

        @Override
        public ToolBarPort getToolBarPort() {
            return toolBar.getViewModel();
        }

        @Override
        public void openTab(NavigatorTabParams params) {
            var view = createTab(params);
            var dock = getPanelTabDock();
            if (dock != null) {
                dock.getComposer().addTab(view);
            } else {
                getShell().getComposer().getWorkspace().getComposer().addTab(view);
            }
            view.requestFocus();
        }

        @Override
        public GreyWolfShellView getShell() {
            return (GreyWolfShellView) super.getShell();
        }

        @Override
        public GreyWolfShellPort getShellPort() {
            return (GreyWolfShellPort) super.getShellPort();
        }

        public LocationBarView<?> getLocationBar() {
            return locationBar;
        }

        @Override
        public LocationBarPort getLocationBarPort() {
            return this.locationBar.getViewModel();
        }

        @Override
        public void addDialog(DialogView<?> dialog) {
            // Routed to the owning PanelManagerTab's dialog container when embedded, so an embedded file
            // manager's dialogs overlay the whole panel manager tab (both panels) rather than being cramped
            // into this tab's own, narrower panel. Called by this tab's area, which creates its own dialogs
            // but has no window container of its own to add them to.
            var panelTabDock = getPanelTabDock();
            if (panelTabDock != null) {
                panelTabDock.getComposer().getDualPanelsTab().getComposer().addDialog(dialog);
            } else {
                super.addDialog(dialog);
            }
        }

        protected NavigatorTabView createTab(NavigatorTabParams params) {
            var view = new NavigatorTabView(new NavigatorTabViewModel(params), getShell());
            view.initialize();
            return view;
        }

        protected ToolBarView createToolBar(NavigatorAreaPort areaPort) {
            var params = new ToolBarParams();
            var viewModel = new ToolBarViewModel(params);
            var context = getShell().getContext();
            var builder = new ControlBuilder(context.getSlotRegistry(), context.getControlRegistry());
            var view = new ToolBarView(viewModel, builder, areaPort);
            view.initialize();
            return view;
        }

        protected LocationBarView<?> createLocationBar() {
            var context = getShell().getContext();
            var storageRegistry = context.getFileStorageRegistry();
            var params = new LocationBarParams(() -> storageRegistry.getAllStorages(), context.getSettings());
            var viewModel = new LocationBarViewModel<>(params);
            var builder = new ControlBuilder(context.getSlotRegistry(), context.getControlRegistry());
            var view = new LocationBarView<>(viewModel, context.getStylesheetRegistry(), builder, this::getParent);
            view.initialize();
            return view;
        }

        protected DockHostView<?> createLayout() {
            var params = new DockHostParams(getViewModel().getConfig().getDockHost());
            var viewModel = new DockHostViewModel<>(params);
            var view = new DockHostView<>(viewModel);
            view.initialize();
            return view;
        }

        protected NavigatorAreaView createArea() {
            var tab = getViewModel();
            var params = new NavigatorAreaParams(tab.getConfig().getArea(), tab, tab.getInitialDirectory());
            var viewModel = new NavigatorAreaViewModel(params);
            var view = new NavigatorAreaView(viewModel, NavigatorTabView.this);
            view.initialize();
            return view;
        }

        protected LocationTabView createLocationTab(LocationTabParams params) {
            var viewModel = new LocationTabViewModel(params);
            var view = new LocationTabView(viewModel, getShell(), this::getParent, this::getAreaPort);
            view.initialize();
            return view;
        }

        /**
         * Creates the {@code TabDock} {@link #locationTab} is added to. Detached from any {@code DockHost} at
         * first &mdash; see {@link AccessoryTabDockView} &mdash; regardless of whether this tab is EMBEDDED or
         * already STANDALONE when opened; {@link #ensureAccessoryTabDock()} attaches it appropriately either way.
         */
        protected TabDockView<?> createAccessoryTabDock() {
            var viewModel = new TabDockViewModel<TabDockComposer>(new AreaParams(null));
            var dock = new AccessoryTabDockView(viewModel, this::detachEmbeddedAccessory);
            dock.initialize();
            return dock;
        }

        /**
         * Returns {@link #accessoryTabDock}, creating and attaching it first (appropriately for the current
         * placement) if {@link #locationTab} is being opened for the first time.
         */
        private TabDockView<?> ensureAccessoryTabDock() {
            if (accessoryTabDock != null) {
                return accessoryTabDock;
            }
            accessoryTabDock = createAccessoryTabDock();
            TabDockPort port = accessoryTabDock.getViewModel();
            port.setMinimizable(true);
            port.setOnClosed(() -> this.accessoryTabDock = null);
            // layout(Placement) has already run by the time this is reachable, so a non-null layout here
            // means this tab is WORKSPACE with a real DockHost ready to dock into - never something to build
            // here.
            if (layout != null) {
                layout.getComposer().addTabDock(accessoryTabDock, Side.LEFT, ACCESSORY_TAB_DOCK_SIZE);
            } else {
                attachAccessoryEmbedded(accessoryTabDock);
            }
            applyAccessoryChrome(accessoryTabDock);
            return accessoryTabDock;
        }

        /**
         * Shows {@code dock} (still detached from any {@code DockHost}) next to {@link #area} in a plain
         * {@link #accessorySplitPane}, fixed at {@link #ACCESSORY_SPLIT_POSITION} - the panel
         * counterpart of the WORKSPACE branch in {@link #layout(Placement)}, which a full docking layout
         * would otherwise be overkill for.
         */
        private void attachAccessoryEmbedded(TabDockView<?> dock) {
            getModifiableChildren().add(dock);
            getContentBox().getChildren().remove(area.getNode());
            SplitPane.setResizableWithParent(dock.getNode(), false);
            this.accessorySplitPane = new SplitPane(dock.getNode(), area.getNode());
            this.accessorySplitPane.setDividerPositions(ACCESSORY_SPLIT_POSITION);
            VBox.setVgrow(this.accessorySplitPane, Priority.ALWAYS);
            getContentBox().getChildren().add(this.accessorySplitPane);
        }

        /**
         * Moves {@link #accessoryTabDock} out of {@link #accessorySplitPane} and into {@code dockHost}'s own
         * docking model, once this tab becomes STANDALONE; a no-op if the accessory isn't open, or is already
         * properly docked (opened while already STANDALONE, so {@link #attachAccessoryEmbedded} never ran).
         */
        private void promoteEmbeddedAccessory(DockHostView<?> dockHost) {
            if (accessoryTabDock == null || accessorySplitPane == null) {
                return;
            }
            getContentBox().getChildren().remove(accessorySplitPane);
            getModifiableChildren().remove(accessoryTabDock);
            this.accessorySplitPane = null;
            dockHost.getComposer().addTabDock(accessoryTabDock, Side.LEFT, ACCESSORY_TAB_DOCK_SIZE);
        }

        /**
         * Undoes {@link #attachAccessoryEmbedded}: called by {@link AccessoryTabDockView}'s own
         * {@code Composer#close()} in place of the real {@code DockHost}-based close it doesn't have while this
         * tab is EMBEDDED and the accessory was never promoted via {@link #promoteEmbeddedAccessory}.
         */
        private void detachEmbeddedAccessory() {
            if (accessorySplitPane == null) {
                return;
            }
            getContentBox().getChildren().remove(accessorySplitPane);
            getModifiableChildren().remove(accessoryTabDock);
            this.accessorySplitPane = null;
            getContentBox().getChildren().add(area.getNode());
        }

        /**
         * Applies the chrome this tab's accessory panel should have for the current placement: a normal,
         * closable tab header for WORKSPACE (see the root README's &quot;Panel Manager&quot; section on why
         * the panel can also be closed via its own close button there), or none at all for a panel,
         * where the menu checkbox is the only way to close it.
         */
        private void applyAccessoryChrome(TabDockView<?> dock) {
            var workspace = getViewModel().getPlacement() == Placement.WORKSPACE;
            dock.getViewModel().setTabHeaderVisible(workspace);
        }

        private @Nullable PanelTabDockView getPanelTabDock() {
            var parent = parentProperty().get();
            return parent instanceof PanelTabDockView dock ? dock : null;
        }
    }

    public NavigatorTabView(NavigatorTabViewModel viewModel, GreyWolfShellView shell) {
        super(viewModel, shell);
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
        return new NavigatorTabView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        var css = NavigatorTabView.class.getResource("navigator.css").toExternalForm();
        getContentBox().getStylesheets().add(css);
    }
}
