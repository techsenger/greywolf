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
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfSlotRegistrar;
import com.techsenger.greywolf.system.navigator.slots.NavigatorAreaSlots;
import com.techsenger.greywolf.storage.ui.slots.FileViewSlots;

/**
 * Puts the groups of the context menu of the navigator area into place: run file, open file, create,
 * clipboard, modify, other panel, mark and properties.
 *
 * @author Pavel Castornii
 */
public class AreaSlotRegistrar extends AbstractGreyWolfSlotRegistrar {

    public AreaSlotRegistrar(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void register() {
        register(NavigatorAreaSlots.AreaMenu.MENU, 100, FileViewSlots.RUN_GROUP);
        register(NavigatorAreaSlots.AreaMenu.MENU, 200, FileViewSlots.OPEN_GROUP);
        register(NavigatorAreaSlots.AreaMenu.MENU, 300, NavigatorAreaSlots.AreaMenu.CREATE_GROUP);
        register(NavigatorAreaSlots.AreaMenu.MENU, 400, NavigatorAreaSlots.AreaMenu.CLIPBOARD_GROUP);
        register(NavigatorAreaSlots.AreaMenu.MENU, 500, NavigatorAreaSlots.AreaMenu.MODIFY_GROUP);
        register(NavigatorAreaSlots.AreaMenu.MENU, 600, NavigatorAreaSlots.AreaMenu.OTHER_PANEL_GROUP);
        register(NavigatorAreaSlots.AreaMenu.MENU, 700, NavigatorAreaSlots.AreaMenu.MARK_GROUP);
        register(NavigatorAreaSlots.AreaMenu.MENU, 800, NavigatorAreaSlots.AreaMenu.PROPERTIES_GROUP);
    }
}
