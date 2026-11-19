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

package com.techsenger.greywolf.system.navigator.slots;

import com.techsenger.greywolf.system.navigator.area.NavigatorAreaView;
import com.techsenger.shellfx.material.slot.ContextMenuSlot;
import com.techsenger.shellfx.material.slot.GroupSlot;
import javafx.scene.control.MenuItem;

/**
 * The slots - the context menu and its groups - the Navigator module adds to its area; other plugins can register
 * their own groups and controls into them.
 *
 * @author Pavel Castornii
 */
public final class NavigatorAreaSlots {

    public static final class AreaMenu {

        public static final ContextMenuSlot<NavigatorAreaView> MENU =
                new ContextMenuSlot<>(NavigatorAreaView.class, "Area");

        public static final GroupSlot<NavigatorAreaView, MenuItem> CREATE_GROUP =
                new GroupSlot<>(NavigatorAreaView.class, "Create");

        public static final GroupSlot<NavigatorAreaView, MenuItem> CLIPBOARD_GROUP =
                new GroupSlot<>(NavigatorAreaView.class, "Clipboard");

        public static final GroupSlot<NavigatorAreaView, MenuItem> OTHER_PANEL_GROUP =
                new GroupSlot<>(NavigatorAreaView.class, "OtherPanel");

        public static final GroupSlot<NavigatorAreaView, MenuItem> MODIFY_GROUP =
                new GroupSlot<>(NavigatorAreaView.class, "Modify");

        public static final GroupSlot<NavigatorAreaView, MenuItem> MARK_GROUP =
                new GroupSlot<>(NavigatorAreaView.class, "Mark");

        public static final GroupSlot<NavigatorAreaView, MenuItem> PROPERTIES_GROUP =
                new GroupSlot<>(NavigatorAreaView.class, "Properties");

        private AreaMenu() {
            // empty
        }
    }

    private NavigatorAreaSlots() {
        // empty
    }
}
