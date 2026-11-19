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

package com.techsenger.greywolf.system.navigator.utils;

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.core.action.ActionName;
import com.techsenger.shellfx.core.registries.ActionRegistry;
import javafx.scene.control.MenuItem;

/**
 * Utilities for binding the items of menus to the hotkeys of actions.
 *
 * @author Pavel Castornii
 */
public final class AcceleratorUtils {

    /**
     * Makes the accelerator of {@code item} follow the hotkey of an action, so a change of the hotkey is seen at once.
     * Does nothing if there is no name or no such action in the registry. The caller unbinds the accelerator when the
     * item is no longer used.
     *
     * @param item the item to bindHotkey
     * @param name the name of the action of the item, or {@code null} for an item that has no action
     * @param registry the registry the action is looked up in
     */
    public static void bindHotkey(MenuItem item, @Nullable ActionName name, ActionRegistry registry) {
        if (name == null) {
            return;
        }
        var action = registry.getActionsByName().get(name);
        if (action != null) {
            item.acceleratorProperty().bind(action.hotkeyProperty());
        }
    }

    private AcceleratorUtils() {
        // empty
    }
}
