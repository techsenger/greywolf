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

import com.techsenger.greywolf.shared.directory.DirectoryTreeView;
import com.techsenger.shellfx.material.slot.ContextMenuSlot;
import com.techsenger.shellfx.material.slot.GroupSlot;
import javafx.scene.control.MenuItem;

/**
 * The slots - the context menu of a node of the directory tree and its groups; plugins register their own groups and
 * controls into them. The menu acts on the folder the node stands for.
 *
 * @author Pavel Castornii
 */
public final class DirectoryTreeSlots {

    public static final class NodeMenu {

        public static final ContextMenuSlot<DirectoryTreeView<?>> MENU =
                new ContextMenuSlot<>(DirectoryTreeView.class, "Node");

        public static final GroupSlot<DirectoryTreeView<?>, MenuItem> SEND_GROUP =
                new GroupSlot<>(DirectoryTreeView.class, "Send");

        public static final GroupSlot<DirectoryTreeView<?>, MenuItem> PROPERTIES_GROUP =
                new GroupSlot<>(DirectoryTreeView.class, "Properties");

        public static final GroupSlot<DirectoryTreeView<?>, MenuItem> CLIPBOARD_GROUP =
                new GroupSlot<>(DirectoryTreeView.class, "Clipboard");

        public static final GroupSlot<DirectoryTreeView<?>, MenuItem> TREE_GROUP =
                new GroupSlot<>(DirectoryTreeView.class, "Tree");

        private NodeMenu() {
            // empty
        }
    }

    private DirectoryTreeSlots() {
        // empty
    }
}
