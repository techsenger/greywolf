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

package com.techsenger.greywolf.system.navigator.registrars;

import com.techsenger.greywolf.system.navigator.utils.AcceleratorUtils;
import com.techsenger.patternfx.mvvm.ParentView;
import com.techsenger.shellfx.core.action.ActionName;
import com.techsenger.shellfx.core.registries.ActionRegistry;
import com.techsenger.shellfx.core.registries.SimpleControlProvider;
import javafx.scene.control.MenuItem;

/**
 * Provides a menu item whose accelerator follows the hotkey of its action.
 *
 * @param <V> the view type of the component the item is built for
 * @param <C> the type of the menu item
 * @author Pavel Castornii
 */
class ActionControlProvider<V extends ParentView<?>, C extends MenuItem> extends SimpleControlProvider<V, C> {

    private final ActionName actionName;

    private final ActionRegistry actionRegistry;

    /**
     * Creates a provider.
     *
     * @param item the item to provide
     * @param actionName the name of the action of the item, whose hotkey is the accelerator of the item
     * @param actionRegistry the registry the action is looked up in
     */
    ActionControlProvider(C item, ActionName actionName, ActionRegistry actionRegistry) {
        super(item);
        this.actionName = actionName;
        this.actionRegistry = actionRegistry;
    }

    @Override
    public void initialize(V view) {
        super.initialize(view);
        AcceleratorUtils.bindHotkey(getControl(), actionName, actionRegistry);
    }

    @Override
    public void deinitialize(V view) {
        super.deinitialize(view);
        getControl().acceleratorProperty().unbind();
    }
}
