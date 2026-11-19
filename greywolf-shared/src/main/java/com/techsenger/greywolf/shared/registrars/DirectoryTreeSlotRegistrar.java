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

package com.techsenger.greywolf.shared.registrars;

import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfSlotRegistrar;
import com.techsenger.greywolf.shared.slots.DirectoryTreeSlots.NodeMenu;
import com.techsenger.greywolf.storage.ui.slots.FileViewSlots;

/**
 * Puts the groups of the context menu of a node of the directory tree into place: open, send, properties,
 * clipboard and the group of the tree itself.
 *
 * @author Pavel Castornii
 */
public class DirectoryTreeSlotRegistrar extends AbstractGreyWolfSlotRegistrar {

    public DirectoryTreeSlotRegistrar(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void register() {
        register(NodeMenu.MENU, 100, FileViewSlots.OPEN_GROUP);
        register(NodeMenu.MENU, 200, NodeMenu.CLIPBOARD_GROUP);
        register(NodeMenu.MENU, 300, NodeMenu.SEND_GROUP);
        register(NodeMenu.MENU, 400, NodeMenu.TREE_GROUP);
        register(NodeMenu.MENU, 500, NodeMenu.PROPERTIES_GROUP);
    }
}
