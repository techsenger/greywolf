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

import com.techsenger.greywolf.core.GreyWolfShellView;
import com.techsenger.shellfx.material.slot.GroupSlot;
import com.techsenger.shellfx.material.slot.MenuSlot;
import javafx.scene.control.MenuItem;

/**
 * The slots - menus and menu groups - the Navigator module adds to GreyWolf' shell; other plugins can register
 * their own groups and controls into them.
 *
 * @author Pavel Castornii
 */
public final class NavigatorShellSlots {

    public static final class ColumnsMenu {

        public static final MenuSlot<GreyWolfShellView> MENU = new MenuSlot<>(GreyWolfShellView.class, "Columns");

        public static final GroupSlot<GreyWolfShellView, MenuItem> DEFAULT_GROUP =
                new GroupSlot<>(GreyWolfShellView.class, "Default");

        private ColumnsMenu() {
            // empty
        }
    }

    public static final class FileMenu {

        public static final GroupSlot<GreyWolfShellView, MenuItem> OTHER_PANEL_GROUP =
                new GroupSlot<>(GreyWolfShellView.class, "OtherPanel");

        public static final GroupSlot<GreyWolfShellView, MenuItem> PROPERTIES_GROUP =
                new GroupSlot<>(GreyWolfShellView.class, "Properties");

        private FileMenu() {
            // empty
        }
    }

    public static final class MarkMenu {

        public static final MenuSlot<GreyWolfShellView> MENU = new MenuSlot<>(GreyWolfShellView.class, "Mark");

        public static final GroupSlot<GreyWolfShellView, MenuItem> DEFAULT_GROUP =
                new GroupSlot<>(GreyWolfShellView.class, "Default");

        private MarkMenu() {
            // empty
        }
    }

    public static final class ModeMenu {

        public static final MenuSlot<GreyWolfShellView> MENU = new MenuSlot<>(GreyWolfShellView.class, "Mode");

        public static final GroupSlot<GreyWolfShellView, MenuItem> DEFAULT_GROUP =
                new GroupSlot<>(GreyWolfShellView.class, "Default");

        private ModeMenu() {
            // empty
        }
    }

    public static final class ViewMenu {

        public static final GroupSlot<GreyWolfShellView, MenuItem> NAVIGATOR_GROUP =
                new GroupSlot<>(GreyWolfShellView.class, "Navigator");

        private ViewMenu() {
            // empty
        }
    }

    private NavigatorShellSlots() {
        // empty
    }
}
