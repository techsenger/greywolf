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
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfControlRegistrar;
import com.techsenger.greywolf.core.slots.WorkspaceSlots;
import com.techsenger.shellfx.core.registries.SimpleControlProvider;
import com.techsenger.shellfx.core.registries.SimpleGroupProvider;
import com.techsenger.shellfx.material.menu.DynamicContextMenu;

/**
 * Registers the controls of the &quot;New&quot; popup of the workspace - permanently, since greywolf-core is
 * never deactivated. Where they sit is set by {@link WorkspaceSlotRegistrar}.
 *
 * @author Pavel Castornii
 */
public class WorkspaceControlRegistrar extends AbstractGreyWolfControlRegistrar {

    public WorkspaceControlRegistrar(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void register() {
        register(WorkspaceSlots.NewMenu.MENU, () -> new SimpleControlProvider<>(new DynamicContextMenu()));
        register(WorkspaceSlots.NewMenu.DEFAULT_GROUP, SimpleGroupProvider::new);
    }
}
