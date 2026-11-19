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

package com.techsenger.greywolf.samples.plugin.registrars;

import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfControlRegistrar;
import com.techsenger.greywolf.system.navigator.area.NavigatorAreaView;
import com.techsenger.greywolf.samples.plugin.style.PluginIcons;
import com.techsenger.greywolf.storage.ui.slots.FileViewSlots;
import com.techsenger.shellfx.core.registries.SimpleControlProvider;
import com.techsenger.shellfx.material.icon.FontIconView;
import javafx.scene.control.MenuItem;

/**
 * Adds the sample plugin item to the context menu of the Navigator area. The item does the same as the tool bar
 * button: it shows the name of the file that is currently selected in the area. The item goes into the open file group
 * of the menu, which the Navigator already declares.
 *
 * @author Pavel Castornii
 */
public class AreaControlRegistrar extends AbstractGreyWolfControlRegistrar {

    public AreaControlRegistrar(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void register() {
        register(FileViewSlots.OPEN_GROUP, 1000, NavigatorAreaView.class,
                () -> new SimpleControlProvider<NavigatorAreaView, MenuItem>() {

            @Override
            public void initialize(NavigatorAreaView view) {
                super.initialize(view);
                var item = new MenuItem("Sample plugin item", new FontIconView(PluginIcons.ROCKET));
                item.setOnAction(new ShowFileItemHandler(getGreyWolf(), view));
                setControl(item);
            }
        });
    }
}
