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

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.core.GreyWolfShellView;
import com.techsenger.shellfx.core.action.ActionName;
import java.util.Objects;
import java.util.function.BiConsumer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.MenuItem;

/**
 * Provides a check or radio menu item whose selected state follows a value. The state is not bound, since JavaFX sets
 * it itself when the item is clicked.
 *
 * @param <C> the type of the menu item
 * @author Pavel Castornii
 */
abstract class SelectedControlProvider<C extends MenuItem> extends FocusedControlProvider<C> {

    private final BiConsumer<C, Boolean> selectedSetter;

    private final ChangeListener<Boolean> selectedListener;

    private @Nullable ObservableValue<Boolean> selected;

    /**
     * Creates a provider.
     *
     * @param item the item to provide
     * @param shell the shell the navigator tab is looked up in
     * @param actionName the name of the action of the item, whose hotkey is the accelerator of the item
     * @param selectedSetter sets the selected state of the item, for example {@code CheckMenuItem::setSelected}
     */
    SelectedControlProvider(C item, GreyWolfShellView shell, ActionName actionName,
            BiConsumer<C, Boolean> selectedSetter) {
        super(item, actionName, shell);
        this.selectedSetter = selectedSetter;
        this.selectedListener = (ov, oldV, newV) -> selectedSetter.accept(getControl(), newV);
    }

    @Override
    public void initialize(GreyWolfShellView view) {
        super.initialize(view);
        selected = observeSelected();
        selectedSetter.accept(getControl(), selected.getValue());
        selected.addListener(selectedListener);
    }

    @Override
    public void deinitialize(GreyWolfShellView view) {
        super.deinitialize(view);
        Objects.requireNonNull(selected).removeListener(selectedListener);
    }

    /**
     * Returns the value the selected state of the item follows. It is called once, when the item is initialized.
     */
    protected abstract ObservableValue<Boolean> observeSelected();
}
