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

package com.techsenger.greywolf.devtools.weaverbird.registrars;

import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.core.slots.GreyWolfShellSlots;
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfControlRegistrar;
import com.techsenger.shellfx.core.registries.SimpleControlProvider;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.weaverbird.gui.settings.DiagramSettings;
import com.techsenger.weaverbird.gui.style.WeaverbirdIcons;
import javafx.scene.control.MenuItem;

/**
 *
 * @author Pavel Castornii
 */
public class ShellControlRegistrar extends AbstractGreyWolfControlRegistrar {

    private final DiagramSettings diagramSettings;

    public ShellControlRegistrar(GreyWolf greyWolf, DiagramSettings settings) {
        super(greyWolf);
        this.diagramSettings = settings;
    }

    @Override
    public void register() {
        registerConsoleItem();
        registerDiagramsItem();
    }

    private void registerConsoleItem() {
        register(GreyWolfShellSlots.DevToolsMenu.BASE_GROUP, 300, () -> {
            var item = new MenuItem("Weaverbird Console", new FontIconView(WeaverbirdIcons.CONSOLE));
            item.setOnAction(new ConsoleItemHandler(getGreyWolf()));
            return new SimpleControlProvider<>(item);
        });
    }

    private void registerDiagramsItem() {
        register(GreyWolfShellSlots.DevToolsMenu.BASE_GROUP, 400, () -> {
            var item = new MenuItem("Weaverbird Diagrams", new FontIconView(WeaverbirdIcons.DIAGRAMS));
            item.setOnAction(new DiagramItemHandler(getGreyWolf(), diagramSettings));
            return new SimpleControlProvider<>(item);
        });
    }
}
