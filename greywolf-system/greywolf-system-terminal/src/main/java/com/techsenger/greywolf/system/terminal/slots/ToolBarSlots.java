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

package com.techsenger.greywolf.system.terminal.slots;

import com.techsenger.greywolf.system.terminal.toolbar.ToolBarView;
import com.techsenger.shellfx.material.slot.GroupSlot;
import com.techsenger.shellfx.material.slot.ToolBarSlot;
import javafx.scene.control.Control;

/**
 * The slots of the Terminal tool bar and of the groups of controls it consists of.
 *
 * @author Pavel Castornii
 */
public final class ToolBarSlots {

    public static final ToolBarSlot<ToolBarView> TOOL_BAR = new ToolBarSlot<>(ToolBarView.class, "ToolBar");

    public static final GroupSlot<ToolBarView, Control> DEFAULT_GROUP = new GroupSlot<>(ToolBarView.class, "Default");

    private ToolBarSlots() {
        // empty
    }
}
