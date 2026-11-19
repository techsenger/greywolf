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

import com.techsenger.greywolf.system.navigator.toolbar.ToolBarView;
import com.techsenger.shellfx.material.slot.GroupSlot;
import com.techsenger.shellfx.material.slot.ToolBarSlot;
import javafx.scene.control.Control;

/**
 * The slots of the Navigator tool bar and of the groups of controls it consists of.
 *
 * @author Pavel Castornii
 */
public final class ToolBarSlots {

    public static final ToolBarSlot<ToolBarView> TOOL_BAR = new ToolBarSlot<>(ToolBarView.class, "ToolBar");

    public static final GroupSlot<ToolBarView, Control> NAVIGATION_GROUP =
            new GroupSlot<>(ToolBarView.class, "Navigation");

    public static final GroupSlot<ToolBarView, Control> CLIPBOARD_GROUP =
            new GroupSlot<>(ToolBarView.class, "Clipboard");

    public static final GroupSlot<ToolBarView, Control> BASE_GROUP = new GroupSlot<>(ToolBarView.class, "Base");

    public static final GroupSlot<ToolBarView, Control> PANELS_GROUP = new GroupSlot<>(ToolBarView.class, "Panels");

    public static final GroupSlot<ToolBarView, Control> OTHER_GROUP = new GroupSlot<>(ToolBarView.class, "Other");

    private ToolBarSlots() {
        // empty
    }
}
