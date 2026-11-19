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
import com.techsenger.greywolf.core.slots.GreyWolfShellSlots;
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfSlotRegistrar;
import com.techsenger.greywolf.devtools.base.slots.BaseDevToolsShellSlots;
import com.techsenger.greywolf.devtools.base.OptionalModules;
import com.techsenger.greywolf.system.navigator.NavigatorTabView;

/**
 * Puts the "Navigator" submenu (Tools -> DevTools -> Navigator) into the slot tree - only when the optional
 * {@code greywolf-system-navigator} module is actually present in this module's layer.
 *
 * @author Pavel Castornii
 */
public class ShellSlotRegistrar extends AbstractGreyWolfSlotRegistrar {

    public ShellSlotRegistrar(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void register() {
        if (OptionalModules.isPresent(() -> NavigatorTabView.class)) {
            register(GreyWolfShellSlots.DevToolsMenu.BASE_GROUP, 100, BaseDevToolsShellSlots.NavigatorMenu.MENU);
            register(BaseDevToolsShellSlots.NavigatorMenu.MENU, 100,
                    BaseDevToolsShellSlots.NavigatorMenu.BASE_GROUP);
        }
    }
}
