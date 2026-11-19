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

package com.techsenger.greywolf.system.terminal.registrars;

import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.core.slots.PanelTabDockSlots;
import com.techsenger.greywolf.core.panels.PanelTabDockView;
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfControlRegistrar;
import com.techsenger.greywolf.system.terminal.style.TerminalIcons;
import com.techsenger.shellfx.core.registries.SimpleControlProvider;
import com.techsenger.shellfx.material.icon.FontIconView;
import javafx.scene.control.MenuItem;

/**
 * Registers the controls the Terminal plugin contributes to the panel tab dock - the &quot;New Terminal&quot; item
 * in the popup of every panel dock.
 *
 * @author Pavel Castornii
 */
public class PanelControlRegistrar extends AbstractGreyWolfControlRegistrar {

    public PanelControlRegistrar(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void register() {
        register(PanelTabDockSlots.NewMenu.DEFAULT_GROUP, 200, () -> new SimpleControlProvider<>(
                new MenuItem("New _Terminal", new FontIconView(TerminalIcons.TERMINAL))) {

            @Override
            public void initialize(PanelTabDockView view) {
                super.initialize(view);
                getControl().setOnAction(new NewTerminalItemHandler(getGreyWolf(), view));
            }
        });
    }
}
