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

package com.techsenger.greywolf.shared.registrars;

import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.core.clipboard.ClipboardFiles;
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfControlRegistrar;
import com.techsenger.greywolf.shared.location.LocationBarView;
import com.techsenger.greywolf.shared.slots.LocationBarSlots.SegmentMenu;
import com.techsenger.greywolf.storage.ui.style.StorageUiIcons;
import com.techsenger.shellfx.core.registries.SimpleControlProvider;
import com.techsenger.shellfx.core.registries.SimpleGroupProvider;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.menu.DynamicContextMenu;
import javafx.scene.control.MenuItem;

/**
 * Registers the context menu of a segment of the location bar and its groups, and the items that need nothing but
 * the location bar: properties and paste into a folder. The other items are registered by the plugins. Where the
 * groups sit is set by {@link LocationBarSlotRegistrar}.
 *
 * @author Pavel Castornii
 */
public class LocationBarControlRegistrar extends AbstractGreyWolfControlRegistrar {

    public LocationBarControlRegistrar(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void register() {
        register(SegmentMenu.MENU, () -> new SimpleControlProvider<>(new DynamicContextMenu()));
        register(SegmentMenu.CLIPBOARD_GROUP, SimpleGroupProvider::new);
        register(SegmentMenu.SEND_GROUP, SimpleGroupProvider::new);
        register(SegmentMenu.PROPERTIES_GROUP, SimpleGroupProvider::new);

        register(SegmentMenu.PROPERTIES_GROUP, 100, () -> new SimpleControlProvider<>(
                new MenuItem("P_roperties")) {

            @Override
            public void initialize(LocationBarView<?> view) {
                super.initialize(view);
                var port = view.getViewModel();
                getControl().setOnAction(new FilePropertiesItemHandler(getGreyWolf(), view, port::getFile));
            }
        });

        register(SegmentMenu.CLIPBOARD_GROUP, 100, () -> new SimpleControlProvider<>(
                new MenuItem("Paste into _Folder", new FontIconView(StorageUiIcons.PASTE))) {

            @Override
            public void initialize(LocationBarView<?> view) {
                super.initialize(view);
                var port = view.getViewModel();
                getControl().disableProperty().bind(ClipboardFiles.presentIn(getGreyWolf().getClipboard()).not());
                getControl().setOnAction(new PasteIntoFolderItemHandler(getGreyWolf(), view, port::getFile,
                        folder -> { }));
            }

            @Override
            public void deinitialize(LocationBarView<?> view) {
                super.deinitialize(view);
                getControl().disableProperty().unbind();
            }
        });
    }
}
