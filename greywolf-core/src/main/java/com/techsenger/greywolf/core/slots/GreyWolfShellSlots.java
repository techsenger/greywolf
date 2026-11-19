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

package com.techsenger.greywolf.core.slots;

import com.techsenger.greywolf.core.GreyWolfShellView;
import com.techsenger.shellfx.material.slot.ContextMenuSlot;
import com.techsenger.shellfx.material.slot.GroupSlot;
import com.techsenger.shellfx.material.slot.MenuBarSlot;
import com.techsenger.shellfx.material.slot.MenuSlot;
import javafx.scene.control.MenuItem;

/**
 * The slots - menus and menu groups - GreyWolf' own shell menu bar offers.
 *
 * @author Pavel Castornii
 */
public final class GreyWolfShellSlots {

    public static final class FileMenu {

        public static final MenuSlot<GreyWolfShellView> MENU = new MenuSlot<>(GreyWolfShellView.class, "File");

        public static final GroupSlot<GreyWolfShellView, MenuItem> NEW_GROUP =
                new GroupSlot<>(GreyWolfShellView.class, "New");

        public static final GroupSlot<GreyWolfShellView, MenuItem> RUN_GROUP =
                new GroupSlot<>(GreyWolfShellView.class, "Run");

        public static final GroupSlot<GreyWolfShellView, MenuItem> OPEN_GROUP =
                new GroupSlot<>(GreyWolfShellView.class, "Open");

        public static final GroupSlot<GreyWolfShellView, MenuItem> LIFECYCLE_GROUP =
                new GroupSlot<>(GreyWolfShellView.class, "Lifecycle");

        private FileMenu() {
            // empty
        }
    }

    public static final class ViewMenu {

        public static final MenuSlot<GreyWolfShellView> MENU = new MenuSlot<>(GreyWolfShellView.class, "View");

        private ViewMenu() {
            // empty
        }
    }

    public static final class ToolsMenu {

        public static final MenuSlot<GreyWolfShellView> MENU = new MenuSlot<>(GreyWolfShellView.class, "Tools");

        public static final GroupSlot<GreyWolfShellView, MenuItem> BASE_GROUP =
                new GroupSlot<>(GreyWolfShellView.class, "Base");

        public static final GroupSlot<GreyWolfShellView, MenuItem> PLUGINS_GROUP =
                new GroupSlot<>(GreyWolfShellView.class, "Plugins");

        public static final GroupSlot<GreyWolfShellView, MenuItem> SETTINGS_GROUP =
                new GroupSlot<>(GreyWolfShellView.class, "Settings");

        private ToolsMenu() {
            // empty
        }
    }

    public static final class DevToolsMenu {

        public static final MenuSlot<GreyWolfShellView> MENU =
                new MenuSlot<>(GreyWolfShellView.class, "DevTools");

        public static final GroupSlot<GreyWolfShellView, MenuItem> BASE_GROUP =
                new GroupSlot<>(GreyWolfShellView.class, "Base");

        private DevToolsMenu() {
            // empty
        }
    }

    public static final class HelpMenu {

        public static final MenuSlot<GreyWolfShellView> MENU = new MenuSlot<>(GreyWolfShellView.class, "Help");

        public static final GroupSlot<GreyWolfShellView, MenuItem> BASE_GROUP =
                new GroupSlot<>(GreyWolfShellView.class, "Base");

        private HelpMenu() {
            // empty
        }
    }

    /**
     * The popup the &quot;+&quot; button of the top-level workspace shows, letting plugins (navigator, terminal,
     * ...) contribute a &quot;New Xxx&quot; item that opens a top-level tab.
     */
    public static final class NewMenu {

        public static final ContextMenuSlot<GreyWolfShellView> MENU =
                new ContextMenuSlot<>(GreyWolfShellView.class, "New");

        public static final GroupSlot<GreyWolfShellView, MenuItem> DEFAULT_GROUP =
                new GroupSlot<>(GreyWolfShellView.class, "Default");

        private NewMenu() {
            // empty
        }
    }

    /**
     * The menu bar of {@link GreyWolfShellView}; the File/View/Tools/Help menus, and those of plugins, are put
     * into it.
     */
    public static final MenuBarSlot<GreyWolfShellView> MAIN_MENU =
            new MenuBarSlot<>(GreyWolfShellView.class, "MainMenu");

    private GreyWolfShellSlots() {
        // empty
    }
}
