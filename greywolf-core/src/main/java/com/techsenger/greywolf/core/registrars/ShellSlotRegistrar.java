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

import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.core.slots.GreyWolfShellSlots;
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfSlotRegistrar;

/**
 * Builds the tree of slots of GreyWolf' own shell menus - permanently, since greywolf-core is never
 * deactivated.
 *
 * @author Pavel Castornii
 */
public class ShellSlotRegistrar extends AbstractGreyWolfSlotRegistrar {

    public ShellSlotRegistrar(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void register() {
        register(GreyWolfShellSlots.MAIN_MENU, 100, GreyWolfShellSlots.FileMenu.MENU);
        register(GreyWolfShellSlots.MAIN_MENU, 300, GreyWolfShellSlots.ViewMenu.MENU);
        register(GreyWolfShellSlots.MAIN_MENU, 400, GreyWolfShellSlots.ToolsMenu.MENU);
        register(GreyWolfShellSlots.MAIN_MENU, 600, GreyWolfShellSlots.HelpMenu.MENU);

        register(GreyWolfShellSlots.FileMenu.MENU, 100, GreyWolfShellSlots.FileMenu.NEW_GROUP);
        register(GreyWolfShellSlots.FileMenu.MENU, 200, GreyWolfShellSlots.FileMenu.RUN_GROUP);
        register(GreyWolfShellSlots.FileMenu.MENU, 300, GreyWolfShellSlots.FileMenu.OPEN_GROUP);
        register(GreyWolfShellSlots.FileMenu.MENU, 10000, GreyWolfShellSlots.FileMenu.LIFECYCLE_GROUP);

        register(GreyWolfShellSlots.ToolsMenu.MENU, 100, GreyWolfShellSlots.ToolsMenu.BASE_GROUP);
        register(GreyWolfShellSlots.ToolsMenu.MENU, 10000, GreyWolfShellSlots.ToolsMenu.PLUGINS_GROUP);
        register(GreyWolfShellSlots.ToolsMenu.MENU, 10100, GreyWolfShellSlots.ToolsMenu.SETTINGS_GROUP);
        register(GreyWolfShellSlots.ToolsMenu.BASE_GROUP, 10000, GreyWolfShellSlots.DevToolsMenu.MENU);

        register(GreyWolfShellSlots.DevToolsMenu.MENU, 100, GreyWolfShellSlots.DevToolsMenu.BASE_GROUP);

        register(GreyWolfShellSlots.HelpMenu.MENU, 100, GreyWolfShellSlots.HelpMenu.BASE_GROUP);
    }
}
