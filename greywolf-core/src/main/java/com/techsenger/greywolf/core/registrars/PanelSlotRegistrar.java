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
import com.techsenger.greywolf.core.slots.PanelTabDockSlots;
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfSlotRegistrar;

/**
 * Builds the tree of slots of the &quot;New&quot; popup of the panel tab dock - permanently, since greywolf-core is
 * never deactivated.
 *
 * @author Pavel Castornii
 */
public class PanelSlotRegistrar extends AbstractGreyWolfSlotRegistrar {

    public PanelSlotRegistrar(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void register() {
        register(PanelTabDockSlots.NewMenu.MENU, 100, PanelTabDockSlots.NewMenu.DEFAULT_GROUP);
    }
}
