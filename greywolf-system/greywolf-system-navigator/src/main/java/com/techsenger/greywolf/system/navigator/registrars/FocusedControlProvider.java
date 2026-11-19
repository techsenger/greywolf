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
import com.techsenger.greywolf.system.navigator.NavigatorTabView;
import com.techsenger.greywolf.system.navigator.area.NavigatorAreaView;
import com.techsenger.greywolf.system.navigator.utils.ComponentUtils;
import com.techsenger.shellfx.core.action.ActionName;
import java.util.Objects;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.MenuItem;

/**
 * Provides a menu item that works on the navigator tab that currently has focus: the item is visible only while
 * there is such a tab. A subclass binds the other properties of the item to those of the tab it observes with
 * {@link #observeTab()}.
 *
 * @param <C> the type of the menu item
 * @author Pavel Castornii
 */
class FocusedControlProvider<C extends MenuItem> extends ActionControlProvider<GreyWolfShellView, C> {

    private final GreyWolfShellView shell;

    private @Nullable ObservableValue<@Nullable NavigatorTabView> tab;

    /**
     * Creates a provider.
     *
     * @param item the item to provide
     * @param actionName the name of the action of the item, whose hotkey is the accelerator of the item
     * @param shell the shell the navigator tab is looked up in
     */
    FocusedControlProvider(C item, ActionName actionName, GreyWolfShellView shell) {
        super(item, actionName, shell.getContext().getActionRegistry());
        this.shell = shell;
    }

    @Override
    public void initialize(GreyWolfShellView view) {
        super.initialize(view);
        tab = ComponentUtils.observeCurrentTab(shell)
                .map(currentTab -> currentTab instanceof NavigatorTabView managerTab ? managerTab : null);
        getControl().visibleProperty().bind(tab.map(managerTab -> true).orElse(false));
    }

    @Override
    public void deinitialize(GreyWolfShellView view) {
        super.deinitialize(view);
        getControl().visibleProperty().unbind();
        getControl().setOnAction(null);
    }

    /**
     * Returns the navigator tab that currently has focus. Can be used after {@code initialize}.
     */
    protected ObservableValue<@Nullable NavigatorTabView> observeTab() {
        return Objects.requireNonNull(tab);
    }

    /**
     * Returns the area of the navigator tab that currently has focus. Can be used after {@code initialize}.
     */
    protected ObservableValue<@Nullable NavigatorAreaView> observeArea() {
        return observeTab().map(managerTab -> managerTab.getComposer().getArea());
    }
}
