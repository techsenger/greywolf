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

package com.techsenger.greywolf.devtools.base.registrars;

import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfControlRegistrar;
import com.techsenger.greywolf.devtools.base.slots.BaseDevToolsShellSlots;
import com.techsenger.greywolf.devtools.base.OptionalModules;
import com.techsenger.greywolf.system.navigator.NavigatorTabView;
import com.techsenger.shellfx.core.registries.SimpleControlProvider;
import com.techsenger.shellfx.core.registries.SimpleGroupProvider;
import com.techsenger.shellfx.material.menu.DynamicMenu;
import java.util.function.Function;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;

/**
 * Contributes the controls of the "Navigator" submenu (Tools -> DevTools -> Navigator) with items that open
 * this module's dialogs directly, outside their normal triggers, for styling/layout work &mdash; only when the
 * optional {@code greywolf-system-navigator} module is actually present in this module's layer.
 *
 * @author Pavel Castornii
 */
public class ShellControlRegistrar extends AbstractGreyWolfControlRegistrar {

    public ShellControlRegistrar(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void register() {
        if (OptionalModules.isPresent(() -> NavigatorTabView.class)) {
            registerNavigatorMenu();
            registerItem(100, "Create _File...", CreateFileItemHandler::new);
            registerItem(200, "Create _Folder...", CreateFolderItemHandler::new);
            registerItem(300, "_Mark...", MarkItemHandler::new);
            registerItem(400, "_Properties...", FilePropertiesItemHandler::new);
            registerItem(500, "_Source...", SourceItemHandler::new);
            registerItem(600, "_Target...", TargetItemHandler::new);
            registerItem(700, "Con_flict", ConflictItemHandler::new);
            registerItem(800, "_Operation Error", OperationErrorItemHandler::new);
            registerItem(900, "Tra_nsferring", TransferringItemHandler::new);
            registerItem(1000, "_Deleting", DeletingItemHandler::new);
            registerItem(1100, "_Rename...", RenameItemHandler::new);
        }
    }

    private void registerNavigatorMenu() {
        register(BaseDevToolsShellSlots.NavigatorMenu.MENU,
                () -> new SimpleControlProvider<>(new DynamicMenu("_Navigator")));
        register(BaseDevToolsShellSlots.NavigatorMenu.BASE_GROUP, SimpleGroupProvider::new);
    }

    private void registerItem(int position, String text,
            Function<GreyWolf, EventHandler<ActionEvent>> handlerFactory) {
        register(BaseDevToolsShellSlots.NavigatorMenu.BASE_GROUP, position, () -> {
            var item = new MenuItem(text);
            item.setOnAction(handlerFactory.apply(getGreyWolf()));
            return new SimpleControlProvider<>(item);
        });
    }
}
