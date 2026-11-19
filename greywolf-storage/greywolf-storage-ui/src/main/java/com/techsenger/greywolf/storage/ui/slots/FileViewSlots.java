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

package com.techsenger.greywolf.storage.ui.slots;

import com.techsenger.greywolf.storage.ui.view.FileView;
import com.techsenger.shellfx.material.slot.GroupSlot;
import javafx.scene.control.MenuItem;

/**
 * The slots of the components that show the content of a directory. A component puts them into its menus, and
 * plugins register their controls for them without depending on the component.
 *
 * @author Pavel Castornii
 */
public final class FileViewSlots {

    /**
     * The group of menu items that run the executable file the component acts on, for example detached or in a
     * terminal.
     */
    public static final GroupSlot<FileView<?>, MenuItem> RUN_GROUP = new GroupSlot<>(FileView.class, "Run");

    /**
     * The group of menu items that open the file the component acts on, for example in a new tab or in a terminal.
     */
    public static final GroupSlot<FileView<?>, MenuItem> OPEN_GROUP = new GroupSlot<>(FileView.class, "Open");

    private FileViewSlots() {
        // empty
    }
}
