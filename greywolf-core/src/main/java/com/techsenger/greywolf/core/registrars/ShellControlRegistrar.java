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

package com.techsenger.greywolf.core.registrars;

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.core.GreyWolfShellView;
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfControlRegistrar;
import com.techsenger.greywolf.core.slots.GreyWolfShellSlots;
import com.techsenger.greywolf.core.style.CoreIcons;
import com.techsenger.shellfx.core.registries.SimpleControlProvider;
import com.techsenger.shellfx.core.registries.SimpleGroupProvider;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.menu.DynamicMenu;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 * Registers the controls of GreyWolf' own shell menus - permanently, since greywolf-core is never deactivated.
 * Where they sit is set by {@link ShellSlotRegistrar}.
 *
 * @author Pavel Castornii
 */
public class ShellControlRegistrar extends AbstractGreyWolfControlRegistrar {

    /**
     * Provides a menu item with an optional accelerator and an action.
     */
    private static final class ItemProvider extends SimpleControlProvider<GreyWolfShellView, MenuItem> {

        ItemProvider(String text, @Nullable KeyCombination accelerator, EventHandler<ActionEvent> handler) {
            super(new MenuItem(text));
            getControl().setAccelerator(accelerator);
            getControl().setOnAction(handler);
        }
    }

    public ShellControlRegistrar(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void register() {
        registerMainMenu();

        registerFileMenu();
        registerDualPanelsItem();
        registerNextThemeItem();
        registerExitItem();

        registerViewMenu();

        registerToolsMenu();
        registerPluginsItem();
        registerSettingsItem();

        registerDevToolsMenu();

        registerHelpMenu();
        registerAboutItem();
    }

    private void registerMainMenu() {
        register(GreyWolfShellSlots.MAIN_MENU, () -> new SimpleControlProvider<>(new MenuBar()));
    }

    private void registerFileMenu() {
        register(GreyWolfShellSlots.FileMenu.MENU, () -> new SimpleControlProvider<>(new DynamicMenu("_File")));
        register(GreyWolfShellSlots.FileMenu.NEW_GROUP, SimpleGroupProvider::new);
        register(GreyWolfShellSlots.FileMenu.RUN_GROUP, SimpleGroupProvider::new);
        register(GreyWolfShellSlots.FileMenu.OPEN_GROUP, SimpleGroupProvider::new);
        register(GreyWolfShellSlots.FileMenu.LIFECYCLE_GROUP, SimpleGroupProvider::new);
    }

    private void registerDualPanelsItem() {
        register(GreyWolfShellSlots.FileMenu.NEW_GROUP, 300, () -> {
            var item = new MenuItem("Dual _Panels", new FontIconView(CoreIcons.DUAL_PANELS));
            item.setAccelerator(
                    new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN, KeyCodeCombination.SHIFT_DOWN));
            item.setOnAction(new DualPanelsItemHandler(getGreyWolf()));
            return new SimpleControlProvider<>(item);
        });
    }

    private void registerNextThemeItem() {
        register(GreyWolfShellSlots.ToolsMenu.SETTINGS_GROUP, 200, () -> new ItemProvider("Next Theme", null,
                new NextThemeItemHandler(getGreyWolf())));
    }

    private void registerExitItem() {
        register(GreyWolfShellSlots.FileMenu.LIFECYCLE_GROUP, 1000, () -> new ItemProvider("E_xit",
                new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN),
                new ExitItemHandler(getGreyWolf())));
    }

    private void registerViewMenu() {
        register(GreyWolfShellSlots.ViewMenu.MENU, () -> new SimpleControlProvider<>(new DynamicMenu("_View")));
    }

    private void registerToolsMenu() {
        register(GreyWolfShellSlots.ToolsMenu.MENU, () -> new SimpleControlProvider<>(new DynamicMenu("_Tools")));
        register(GreyWolfShellSlots.ToolsMenu.BASE_GROUP, SimpleGroupProvider::new);
        register(GreyWolfShellSlots.ToolsMenu.PLUGINS_GROUP, SimpleGroupProvider::new);
        register(GreyWolfShellSlots.ToolsMenu.SETTINGS_GROUP, SimpleGroupProvider::new);
    }

    private void registerPluginsItem() {
        register(GreyWolfShellSlots.ToolsMenu.PLUGINS_GROUP, 100, () -> new ItemProvider("_Plugins...", null,
                new PluginsItemHandler(getGreyWolf())));
    }

    private void registerSettingsItem() {
        register(GreyWolfShellSlots.ToolsMenu.SETTINGS_GROUP, 100, () -> new ItemProvider("_Settings", null,
                new ExitItemHandler(getGreyWolf())));
    }

    private void registerDevToolsMenu() {
        register(GreyWolfShellSlots.DevToolsMenu.MENU,
                () -> new SimpleControlProvider<>(new DynamicMenu("_DevTools")));
        register(GreyWolfShellSlots.DevToolsMenu.BASE_GROUP, SimpleGroupProvider::new);
    }

    private void registerHelpMenu() {
        register(GreyWolfShellSlots.HelpMenu.MENU, () -> new SimpleControlProvider<>(new DynamicMenu("_Help")));
        register(GreyWolfShellSlots.HelpMenu.BASE_GROUP, SimpleGroupProvider::new);
    }

    private void registerAboutItem() {
        register(GreyWolfShellSlots.HelpMenu.BASE_GROUP, 10000, () -> new ItemProvider("_About GreyWolf", null,
                new AboutItemHandler(getGreyWolf())));
    }
}
