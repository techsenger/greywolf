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

import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.core.slots.GreyWolfShellSlots;
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfSlotRegistrar;
import com.techsenger.greywolf.system.navigator.slots.NavigatorShellSlots;

/**
 * Puts the slots the Navigator plugin contributes into the GreyWolf shell - the Other Panel and Properties
 * groups in the File menu, the Mark menu in the main menu bar and the Mode and Columns menus in the View
 * menu, each with its group.
 *
 * @author Pavel Castornii
 */
public class ShellSlotRegistrar extends AbstractGreyWolfSlotRegistrar {

    public ShellSlotRegistrar(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void register() {
        register(GreyWolfShellSlots.FileMenu.MENU, 400, NavigatorShellSlots.FileMenu.OTHER_PANEL_GROUP);
        register(GreyWolfShellSlots.FileMenu.MENU, 500, NavigatorShellSlots.FileMenu.PROPERTIES_GROUP);

        register(GreyWolfShellSlots.MAIN_MENU, 200, NavigatorShellSlots.MarkMenu.MENU);
        register(NavigatorShellSlots.MarkMenu.MENU, 100, NavigatorShellSlots.MarkMenu.DEFAULT_GROUP);

        register(GreyWolfShellSlots.ViewMenu.MENU, 100, NavigatorShellSlots.ViewMenu.NAVIGATOR_GROUP);

        register(NavigatorShellSlots.ViewMenu.NAVIGATOR_GROUP, 100, NavigatorShellSlots.ColumnsMenu.MENU);
        register(NavigatorShellSlots.ColumnsMenu.MENU, 100, NavigatorShellSlots.ColumnsMenu.DEFAULT_GROUP);

        register(NavigatorShellSlots.ViewMenu.NAVIGATOR_GROUP, 200, NavigatorShellSlots.ModeMenu.MENU);
        register(NavigatorShellSlots.ModeMenu.MENU, 100, NavigatorShellSlots.ModeMenu.DEFAULT_GROUP);
    }
}
