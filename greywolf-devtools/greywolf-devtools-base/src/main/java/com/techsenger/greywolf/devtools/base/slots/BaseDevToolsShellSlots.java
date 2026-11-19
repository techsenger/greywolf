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

package com.techsenger.greywolf.devtools.base.slots;

import com.techsenger.greywolf.core.GreyWolfShellView;
import com.techsenger.shellfx.material.slot.GroupSlot;
import com.techsenger.shellfx.material.slot.MenuSlot;
import javafx.scene.control.MenuItem;

/**
 * The slots - menus and menu groups - this module registers and populates entirely by itself - nothing outside this
 * package ever needs to reference them.
 *
 * @author Pavel Castornii
 */
public final class BaseDevToolsShellSlots {

    public static final class NavigatorMenu {

        public static final MenuSlot<GreyWolfShellView> MENU = new MenuSlot<>(GreyWolfShellView.class, "Navigator");

        public static final GroupSlot<GreyWolfShellView, MenuItem> BASE_GROUP =
                new GroupSlot<>(GreyWolfShellView.class, "Base");

        private NavigatorMenu() {
            // empty
        }
    }

    private BaseDevToolsShellSlots() {
        // empty
    }
}
