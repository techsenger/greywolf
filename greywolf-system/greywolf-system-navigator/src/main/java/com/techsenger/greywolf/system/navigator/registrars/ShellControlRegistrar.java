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

package com.techsenger.greywolf.system.navigator.registrars;

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.core.GreyWolfShellView;
import com.techsenger.greywolf.core.panels.PanelTabDockView;
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfControlRegistrar;
import com.techsenger.greywolf.core.slots.GreyWolfShellSlots;
import com.techsenger.greywolf.core.slots.PanelTabDockSlots;
import com.techsenger.greywolf.system.navigator.NavigatorFileColumns;
import com.techsenger.greywolf.system.navigator.actions.NavigatorActions.Name;
import com.techsenger.greywolf.system.navigator.area.NavigatorAreaPort;
import com.techsenger.greywolf.system.navigator.area.NavigatorAreaView;
import com.techsenger.greywolf.system.navigator.area.ViewModeType;
import com.techsenger.greywolf.system.navigator.mark.MarkOperationType;
import com.techsenger.greywolf.system.navigator.location.LocationMode;
import com.techsenger.greywolf.system.navigator.location.LocationTabPort;
import com.techsenger.greywolf.system.navigator.location.LocationTabView;
import com.techsenger.greywolf.system.navigator.slots.NavigatorShellSlots;
import com.techsenger.greywolf.system.navigator.style.NavigatorIcons;
import com.techsenger.greywolf.system.navigator.utils.AcceleratorUtils;
import com.techsenger.greywolf.system.navigator.utils.ComponentUtils;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.shellfx.core.registries.SimpleControlProvider;
import com.techsenger.shellfx.core.registries.SimpleGroupProvider;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.menu.DynamicMenu;
import com.techsenger.shellfx.material.table.TableColumnName;
import com.techsenger.shellfx.storage.ui.FileColumns;
import java.util.Objects;
import java.util.function.Predicate;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;

/**
 * Registers the controls the Navigator plugin contributes to the GreyWolf shell - its own menus (Mark, Mode,
 * Columns), items in the shared View and File menus, and the &quot;New Navigator&quot; item in every panel
 * dock's popup. Where they sit is set by {@link ShellSlotRegistrar}.
 *
 * @author Pavel Castornii
 */
public class ShellControlRegistrar extends AbstractGreyWolfControlRegistrar {

    private static boolean isFolder(GreyWolfFile file) {
        return file.isDirectory() || file.isDirectoryLink();
    }

    /**
     * Returns {@code true} while the area of the focused tab has a selected entry the item can act on.
     */
    private static ObservableValue<Boolean> fileMatches(
            FocusedControlProvider<?> provider, Predicate<GreyWolfFile> applicable) {
        return provider.observeArea()
                .flatMap(area -> area.getViewModel().fileProperty())
                .map(applicable::test)
                .orElse(false);
    }

    /**
     * Returns {@code true} while the item must be disabled: the selected entry is not one the item can act on.
     */
    private static ObservableValue<Boolean> disabledUnless(
            FocusedControlProvider<?> provider, Predicate<GreyWolfFile> applicable) {
        return fileMatches(provider, applicable).map(matches -> !matches);
    }

    public ShellControlRegistrar(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void register() {
        registerShellControls();
    }

    private void registerShellControls() {
        registerShellNavigatorItem();

        registerFileMenuGroups();
        registerRunItem();
        registerOpenItems();
        registerSendToItem();
        registerSendCurrentItem();
        registerPropertiesItem();

        registerMarkMenu();
        registerMarkAllItem();
        registerUnmarkAllItem();
        registerMarkItem();
        registerUnmarkItem();
        registerInvertItem();

        registerViewMenuGroup();
        registerModeMenu();
        registerModeItem(ViewModeType.LIST, Name.MODE_LIST, "_List", 100);
        registerModeItem(ViewModeType.TILES, Name.MODE_TILES, "_Tiles", 200);
        registerModeItem(ViewModeType.DETAILS, Name.MODE_DETAILS, "_Details", 300);
        registerColumnsMenu();
        registerColumnItem(FileColumns.SIZE, Name.COLUMN_SIZE, 100);
        registerColumnItem(NavigatorFileColumns.TYPE, Name.COLUMN_TYPE, 200);
        registerColumnItem(FileColumns.LAST_MODIFIED, Name.COLUMN_LAST_MODIFIED, 300);
        registerLocationModeItem(LocationMode.TREE, Name.LOCATION_TREE, "_Tree", 300);
        registerLocationModeItem(LocationMode.PLACES, Name.LOCATION_PLACES, "_Places", 400);
        registerLocationModeItem(LocationMode.TREE_AND_PLACES, Name.LOCATION_TREE_AND_PLACES,
                "Tree & Pl_aces", 500);

        registerPanelNavigatorItem();
    }

    private void registerShellNavigatorItem() {
        register(GreyWolfShellSlots.FileMenu.NEW_GROUP, 100, () -> {
            var item = new MenuItem("_Navigator", new FontIconView(NavigatorIcons.NAVIGATOR));
            AcceleratorUtils.bindHotkey(item, Name.NEW_NAVIGATOR, getGreyWolf().getActionRegistry());
            item.setOnAction(new ShellNavigatorItemHandler(getGreyWolf()));
            return new SimpleControlProvider<>(item);
        });
    }

    private void registerFileMenuGroups() {
        register(NavigatorShellSlots.FileMenu.OTHER_PANEL_GROUP, SimpleGroupProvider::new);
        register(NavigatorShellSlots.FileMenu.PROPERTIES_GROUP, SimpleGroupProvider::new);
    }

    private void registerRunItem() {
        register(GreyWolfShellSlots.FileMenu.RUN_GROUP, 100, () -> new FocusedControlProvider<>(
                new MenuItem("_Run"), Name.RUN, getShell()) {

            @Override
            public void initialize(GreyWolfShellView v) {
                super.initialize(v);
                getControl().visibleProperty().bind(fileMatches(this, GreyWolfFile::isRunnable));
                getControl().setOnAction(new RunItemHandler(getGreyWolf(), ShellControlRegistrar.this::currentArea));
            }
        });
    }

    private void registerOpenItems() {
        register(GreyWolfShellSlots.FileMenu.OPEN_GROUP, 100, () -> new FocusedControlProvider<>(
                new MenuItem("_Open"), Name.OPEN, getShell()) {

            @Override
            public void initialize(GreyWolfShellView v) {
                super.initialize(v);
                getControl().disableProperty().bind(disabledUnless(this, ShellControlRegistrar::isFolder));
                getControl().setOnAction(new OpenItemHandler(getGreyWolf(), ShellControlRegistrar.this::currentArea));
            }

            @Override
            public void deinitialize(GreyWolfShellView v) {
                super.deinitialize(v);
                getControl().disableProperty().unbind();
            }
        });

        register(GreyWolfShellSlots.FileMenu.OPEN_GROUP, 200, () -> new FocusedControlProvider<>(
                new MenuItem("Open in _New Tab"), Name.OPEN_IN_NEW_TAB, getShell()) {

            @Override
            public void initialize(GreyWolfShellView v) {
                super.initialize(v);
                getControl().disableProperty().bind(disabledUnless(this, ShellControlRegistrar::isFolder));
                getControl().setOnAction(new OpenInNewTabItemHandler(
                        getGreyWolf(), ShellControlRegistrar.this::currentArea, NavigatorAreaPort::getFile));
            }

            @Override
            public void deinitialize(GreyWolfShellView v) {
                super.deinitialize(v);
                getControl().disableProperty().unbind();
            }
        });

        register(GreyWolfShellSlots.FileMenu.OPEN_GROUP, 300, () -> new FocusedControlProvider<>(
                new MenuItem("Open in _System"), Name.OPEN_IN_SYSTEM, getShell()) {

            @Override
            public void initialize(GreyWolfShellView v) {
                super.initialize(v);
                getControl().disableProperty().bind(disabledUnless(this, file -> !file.isParentEntry()));
                getControl().setOnAction(new OpenInSystemItemHandler(
                        getGreyWolf(), ShellControlRegistrar.this::currentArea));
            }

            @Override
            public void deinitialize(GreyWolfShellView v) {
                super.deinitialize(v);
                getControl().disableProperty().unbind();
            }
        });
    }

    private void registerPropertiesItem() {
        register(NavigatorShellSlots.FileMenu.PROPERTIES_GROUP, 100, () -> new FocusedControlProvider<>(
                new MenuItem("Propert_ies"), Name.PROPERTIES, getShell()) {

            @Override
            public void initialize(GreyWolfShellView v) {
                super.initialize(v);
                getControl().disableProperty().bind(disabledUnless(this, file -> !file.isParentEntry()));
                getControl().setOnAction(new PropertiesItemHandler(
                        getGreyWolf(), ShellControlRegistrar.this::currentArea));
            }

            @Override
            public void deinitialize(GreyWolfShellView v) {
                super.deinitialize(v);
                getControl().disableProperty().unbind();
            }
        });
    }

    private void registerSendToItem() {
        register(NavigatorShellSlots.FileMenu.OTHER_PANEL_GROUP, 100, () -> new FocusedControlProvider<>(
                new MenuItem("Send _to Other Panel"), Name.SEND_TO, getShell()) {

            @Override
            public void initialize(GreyWolfShellView v) {
                super.initialize(v);
                getControl().setOnAction(new SendItemHandler(
                        getGreyWolf(), ShellControlRegistrar.this::currentArea, NavigatorAreaPort::getFile));
            }
        });
    }

    private void registerSendCurrentItem() {
        register(NavigatorShellSlots.FileMenu.OTHER_PANEL_GROUP, 200, () -> new FocusedControlProvider<>(
                new MenuItem("Send Current to _Other Panel"), Name.SEND_CURRENT, getShell()) {

            @Override
            public void initialize(GreyWolfShellView v) {
                super.initialize(v);
                // it is here only to carry the hotkey of the action, so it is never shown
                getControl().visibleProperty().unbind();
                getControl().setVisible(false);
                getControl().setOnAction(new SendItemHandler(
                        getGreyWolf(), ShellControlRegistrar.this::currentArea, NavigatorAreaPort::getDirectory));
            }
        });
    }

    private void registerMarkMenu() {
        register(NavigatorShellSlots.MarkMenu.MENU, () -> new SimpleControlProvider<>(new DynamicMenu("_Mark")));
        register(NavigatorShellSlots.MarkMenu.DEFAULT_GROUP, SimpleGroupProvider::new);
    }

    private void registerMarkItem() {
        register(NavigatorShellSlots.MarkMenu.DEFAULT_GROUP, 100, () -> new FocusedControlProvider<>(
                new MenuItem("_Mark..."), Name.MARK, getShell()) {

            @Override
            public void initialize(GreyWolfShellView v) {
                super.initialize(v);
                getControl().setOnAction(new MarkDialogItemHandler(
                        getGreyWolf(), ShellControlRegistrar.this::currentArea, MarkOperationType.MARK));
            }
        });
    }

    private void registerUnmarkItem() {
        register(NavigatorShellSlots.MarkMenu.DEFAULT_GROUP, 200, () -> new FocusedControlProvider<>(
                new MenuItem("_Unmark..."), Name.UNMARK, getShell()) {

            @Override
            public void initialize(GreyWolfShellView v) {
                super.initialize(v);
                getControl().setOnAction(new MarkDialogItemHandler(
                        getGreyWolf(), ShellControlRegistrar.this::currentArea, MarkOperationType.UNMARK));
            }
        });
    }

    private void registerInvertItem() {
        register(NavigatorShellSlots.MarkMenu.DEFAULT_GROUP, 300, () -> new FocusedControlProvider<>(
                new MenuItem("_Invert Marking"), Name.INVERT_MARKING, getShell()) {

            @Override
            public void initialize(GreyWolfShellView v) {
                super.initialize(v);
                getControl().setOnAction(new InvertMarkItemHandler(
                        getGreyWolf(), ShellControlRegistrar.this::currentArea));
            }
        });
    }

    private void registerMarkAllItem() {
        register(NavigatorShellSlots.MarkMenu.DEFAULT_GROUP, 400, () -> new FocusedControlProvider<>(
                new MenuItem("Mark _All"), Name.MARK_ALL, getShell()) {

            @Override
            public void initialize(GreyWolfShellView v) {
                super.initialize(v);
                getControl().setOnAction(new MarkItemHandler(
                        getGreyWolf(), ShellControlRegistrar.this::currentArea, true));
            }
        });
    }

    private void registerUnmarkAllItem() {
        register(NavigatorShellSlots.MarkMenu.DEFAULT_GROUP, 500, () -> new FocusedControlProvider<>(
                new MenuItem("U_nmark All"), Name.UNMARK_ALL, getShell()) {

            @Override
            public void initialize(GreyWolfShellView v) {
                super.initialize(v);
                getControl().setOnAction(new MarkItemHandler(
                        getGreyWolf(), ShellControlRegistrar.this::currentArea, false));
            }
        });
    }

    private void registerViewMenuGroup() {
        register(NavigatorShellSlots.ViewMenu.NAVIGATOR_GROUP, SimpleGroupProvider::new);
    }

    private void registerColumnsMenu() {
        register(NavigatorShellSlots.ColumnsMenu.MENU,
                () -> new SimpleControlProvider<>(new DynamicMenu("_Columns")));
        register(NavigatorShellSlots.ColumnsMenu.DEFAULT_GROUP, SimpleGroupProvider::new);
    }

    private void registerColumnItem(TableColumnName columnName, Name actionName, int position) {
        register(NavigatorShellSlots.ColumnsMenu.DEFAULT_GROUP, position, () -> new SelectedControlProvider<>(
                new CheckMenuItem(columnName.getText()), getShell(), actionName, CheckMenuItem::setSelected) {

            @Override
            protected ObservableValue<Boolean> observeSelected() {
                return observeArea()
                        .map(NavigatorAreaView::getViewModel)
                        .flatMap(port -> Objects.requireNonNull(port.getColumnInfos().get(columnName))
                                .visibleProperty())
                        .orElse(false);
            }

            @Override
            public void initialize(GreyWolfShellView v) {
                super.initialize(v);
                getControl().disableProperty().bind(observeArea()
                        .map(NavigatorAreaView::getViewModel)
                        .flatMap(NavigatorAreaPort::viewModeTypeProperty)
                        .map(type -> type != ViewModeType.DETAILS)
                        .orElse(true));
                getControl().setOnAction(new ColumnItemHandler(
                        getGreyWolf(), ShellControlRegistrar.this::currentArea, getControl(), columnName));
            }

            @Override
            public void deinitialize(GreyWolfShellView v) {
                super.deinitialize(v);
                getControl().disableProperty().unbind();
            }
        });
    }

    private void registerModeMenu() {
        register(NavigatorShellSlots.ModeMenu.MENU, () -> new SimpleControlProvider<>(new DynamicMenu("_Mode")));
        register(NavigatorShellSlots.ModeMenu.DEFAULT_GROUP, SimpleGroupProvider::new);
    }

    private void registerModeItem(ViewModeType modeType, Name actionName, String text, int position) {
        register(NavigatorShellSlots.ModeMenu.DEFAULT_GROUP, position, () -> new SelectedControlProvider<>(
                new RadioMenuItem(text), getShell(), actionName, RadioMenuItem::setSelected) {

            @Override
            protected ObservableValue<Boolean> observeSelected() {
                return observeArea()
                        .map(NavigatorAreaView::getViewModel)
                        .flatMap(NavigatorAreaPort::viewModeTypeProperty)
                        .map(type -> type == modeType)
                        .orElse(false);
            }

            @Override
            public void initialize(GreyWolfShellView v) {
                super.initialize(v);
                getControl().setOnAction(new ModeItemHandler(
                        getGreyWolf(), ShellControlRegistrar.this::currentArea, modeType));
            }
        });
    }

    private void registerLocationModeItem(LocationMode mode, Name actionName, String text, int position) {
        register(NavigatorShellSlots.ViewMenu.NAVIGATOR_GROUP, position, () -> new SelectedControlProvider<>(
                new CheckMenuItem(text), getShell(), actionName, CheckMenuItem::setSelected) {

            @Override
            protected ObservableValue<Boolean> observeSelected() {
                return observeTab()
                        .flatMap(managerTab -> managerTab.getComposer().locationTabProperty())
                        .map(LocationTabView::getViewModel)
                        .flatMap(LocationTabPort::modeProperty)
                        .map(locationMode -> locationMode == mode)
                        .orElse(false);
            }

            @Override
            public void initialize(GreyWolfShellView v) {
                super.initialize(v);
                getControl().setOnAction(new LocationModeItemHandler(getGreyWolf(), getControl(), mode));
            }
        });
    }

    private void registerPanelNavigatorItem() {
        register(PanelTabDockSlots.NewMenu.DEFAULT_GROUP, 100, () -> new SimpleControlProvider<PanelTabDockView,
                MenuItem>(new MenuItem("New _Navigator", new FontIconView(NavigatorIcons.NAVIGATOR))) {

            @Override
            public void initialize(PanelTabDockView view) {
                super.initialize(view);
                getControl().setOnAction(new PanelNavigatorItemHandler(getGreyWolf(), view));
            }
        });
    }

    private @Nullable NavigatorAreaPort currentArea() {
        var area = ComponentUtils.getCurrentArea(getShell());
        return area == null ? null : area.getViewModel();
    }
}
