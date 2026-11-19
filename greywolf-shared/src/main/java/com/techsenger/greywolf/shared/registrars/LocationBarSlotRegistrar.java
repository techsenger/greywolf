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
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfSlotRegistrar;
import com.techsenger.greywolf.shared.slots.LocationBarSlots.SegmentMenu;
import com.techsenger.greywolf.storage.ui.slots.FileViewSlots;

/**
 * Puts the groups of the context menu of a segment of the location bar into place: open, send, properties and
 * clipboard.
 *
 * @author Pavel Castornii
 */
public class LocationBarSlotRegistrar extends AbstractGreyWolfSlotRegistrar {

    public LocationBarSlotRegistrar(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void register() {
        register(SegmentMenu.MENU, 100, FileViewSlots.OPEN_GROUP);
        register(SegmentMenu.MENU, 200, SegmentMenu.CLIPBOARD_GROUP);
        register(SegmentMenu.MENU, 300, SegmentMenu.SEND_GROUP);
        register(SegmentMenu.MENU, 400, SegmentMenu.PROPERTIES_GROUP);
    }
}
