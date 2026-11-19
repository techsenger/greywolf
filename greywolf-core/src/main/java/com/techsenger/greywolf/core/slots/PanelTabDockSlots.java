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

import com.techsenger.greywolf.core.panels.PanelTabDockView;
import com.techsenger.shellfx.material.slot.ContextMenuSlot;
import com.techsenger.shellfx.material.slot.GroupSlot;
import javafx.scene.control.MenuItem;

/**
 * The slots {@code PanelTabDock} offers to the registries.
 *
 * @author Pavel Castornii
 */
public final class PanelTabDockSlots {

    /**
     * The popup a {@code PanelTabDock}'s &quot;+&quot; button shows, letting plugins (navigator, terminal, ...)
     * contribute a &quot;New Xxx&quot; item that opens an embedded tab in that dock.
     */
    public static final class NewMenu {

        public static final ContextMenuSlot<PanelTabDockView> MENU =
                new ContextMenuSlot<>(PanelTabDockView.class, "New");

        public static final GroupSlot<PanelTabDockView, MenuItem> DEFAULT_GROUP =
                new GroupSlot<>(PanelTabDockView.class, "Default");

        private NewMenu() {
            // empty
        }
    }

    private PanelTabDockSlots() {
        // empty
    }
}
