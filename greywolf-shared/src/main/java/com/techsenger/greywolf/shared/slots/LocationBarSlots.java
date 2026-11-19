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

package com.techsenger.greywolf.shared.slots;

import com.techsenger.greywolf.shared.location.LocationBarView;
import com.techsenger.shellfx.material.slot.ContextMenuSlot;
import com.techsenger.shellfx.material.slot.GroupSlot;
import javafx.scene.control.MenuItem;

/**
 * The slots - the context menu of a segment of the location bar and its groups; plugins register their own groups and
 * controls into them. The menu acts on the folder the segment stands for.
 *
 * @author Pavel Castornii
 */
public final class LocationBarSlots {

    public static final class SegmentMenu {

        public static final ContextMenuSlot<LocationBarView<?>> MENU =
                new ContextMenuSlot<>(LocationBarView.class, "Segment");

        public static final GroupSlot<LocationBarView<?>, MenuItem> SEND_GROUP =
                new GroupSlot<>(LocationBarView.class, "Send");

        public static final GroupSlot<LocationBarView<?>, MenuItem> PROPERTIES_GROUP =
                new GroupSlot<>(LocationBarView.class, "Properties");

        public static final GroupSlot<LocationBarView<?>, MenuItem> CLIPBOARD_GROUP =
                new GroupSlot<>(LocationBarView.class, "Clipboard");

        private SegmentMenu() {
            // empty
        }
    }

    private LocationBarSlots() {
        // empty
    }
}
