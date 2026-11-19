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

package com.techsenger.greywolf.devtools.shellfx.registrars;

import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.core.slots.GreyWolfShellSlots;
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfControlRegistrar;
import com.techsenger.shellfx.core.registries.SimpleControlProvider;
import com.techsenger.shellfx.devtools.style.DevToolsIcons;
import com.techsenger.shellfx.material.icon.FontIconView;
import javafx.scene.control.MenuItem;

/**
 *
 * @author Pavel Castornii
 */
public class ShellControlRegistrar extends AbstractGreyWolfControlRegistrar {

    public ShellControlRegistrar(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void register() {
        registerShellFxToolsItem();
    }

    private void registerShellFxToolsItem() {
        register(GreyWolfShellSlots.DevToolsMenu.BASE_GROUP, 200, () -> {
            var item = new MenuItem("ShellFX Tools", new FontIconView(DevToolsIcons.TOOLS));
            item.setOnAction(new ShellFxToolsItemHandler(getGreyWolf()));
            return new SimpleControlProvider<>(item);
        });
    }
}
