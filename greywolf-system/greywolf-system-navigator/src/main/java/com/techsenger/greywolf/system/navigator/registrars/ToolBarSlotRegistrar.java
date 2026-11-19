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
import com.techsenger.greywolf.system.navigator.slots.ToolBarSlots;

/**
 * Puts the groups of the Navigator tool bar into the tool bar slot.
 *
 * @author Pavel Castornii
 */
public class ToolBarSlotRegistrar extends AbstractGreyWolfSlotRegistrar {

    public ToolBarSlotRegistrar(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void register() {
        register(ToolBarSlots.TOOL_BAR, 100, ToolBarSlots.NAVIGATION_GROUP);
        register(ToolBarSlots.TOOL_BAR, 200, ToolBarSlots.CLIPBOARD_GROUP);
        register(ToolBarSlots.TOOL_BAR, 300, ToolBarSlots.BASE_GROUP);
        register(ToolBarSlots.TOOL_BAR, 400, ToolBarSlots.PANELS_GROUP);
        register(ToolBarSlots.TOOL_BAR, 500, ToolBarSlots.OTHER_GROUP);
    }
}
