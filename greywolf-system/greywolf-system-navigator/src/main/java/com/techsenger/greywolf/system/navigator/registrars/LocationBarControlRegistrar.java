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
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfControlRegistrar;
import com.techsenger.greywolf.system.navigator.utils.ComponentUtils;
import com.techsenger.greywolf.shared.location.LocationBarView;
import com.techsenger.greywolf.shared.slots.LocationBarSlots.SegmentMenu;
import com.techsenger.greywolf.storage.ui.slots.FileViewSlots;
import com.techsenger.shellfx.core.registries.SimpleControlProvider;
import javafx.scene.control.MenuItem;

/**
 * Registers the items of the context menu of a segment of the location bar: open, open in a new tab,
 * and send to the other panel, all acting on the folder the menu was requested for. The menu
 * and its groups are registered by the shared module.
 *
 * @author Pavel Castornii
 */
public class LocationBarControlRegistrar extends AbstractGreyWolfControlRegistrar {

    /**
     * The class the items of the open group are registered for; the group is declared for any view that shows files,
     * so the items are told apart by the class of the view.
     */
    @SuppressWarnings("unchecked")
    private static final Class<LocationBarView<?>> VIEW_CLASS =
            (Class<LocationBarView<?>>) (Class<?>) LocationBarView.class;

    public LocationBarControlRegistrar(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void register() {
        register(FileViewSlots.OPEN_GROUP, 100, VIEW_CLASS, () -> new SimpleControlProvider<>(
                new MenuItem("_Open")) {

            @Override
            public void initialize(LocationBarView<?> view) {
                super.initialize(view);
                var port = view.getViewModel();
                getControl().setOnAction(new OpenInItemHandler(getGreyWolf(),
                        () -> ComponentUtils.getAreaPort(view), area -> port.getFile()));
            }
        });

        register(FileViewSlots.OPEN_GROUP, 200, VIEW_CLASS, () -> new SimpleControlProvider<>(
                new MenuItem("Open in New _Tab")) {

            @Override
            public void initialize(LocationBarView<?> view) {
                super.initialize(view);
                var port = view.getViewModel();
                getControl().setOnAction(new OpenInNewTabItemHandler(getGreyWolf(),
                        () -> ComponentUtils.getAreaPort(view), area -> port.getFile()));
            }
        });

        register(SegmentMenu.SEND_GROUP, 100, () -> new SimpleControlProvider<>(
                new MenuItem("Send _to Other Panel")) {

            @Override
            public void initialize(LocationBarView<?> view) {
                super.initialize(view);
                var port = view.getViewModel();
                getControl().setOnAction(new SendItemHandler(getGreyWolf(),
                        () -> ComponentUtils.getAreaPort(view), area -> port.getFile()));
            }
        });
    }
}
