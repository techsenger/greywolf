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
import com.techsenger.greywolf.core.GreyWolfShellView;
import com.techsenger.greywolf.core.panels.DualPanelsTabView;
import com.techsenger.greywolf.core.panels.Panel;
import com.techsenger.greywolf.core.panels.Placement;
import com.techsenger.greywolf.system.navigator.NavigatorTabView;
import com.techsenger.greywolf.system.navigator.area.NavigatorAreaPort;
import com.techsenger.greywolf.system.navigator.area.NavigatorAreaView;
import com.techsenger.greywolf.system.navigator.location.LocationTabView;
import com.techsenger.greywolf.storage.ui.view.FileRecipient;
import com.techsenger.greywolf.storage.ui.view.FileView;
import com.techsenger.patternfx.mvvm.ChildView;
import com.techsenger.shellfx.core.tab.TabView;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;

/**
 *
 * @author Pavel Castornii
 */
public final class ComponentUtils {

    /**
     * Returns the navigator tab that currently has focus within {@code shell}'s current panel manager
     * tab's panel, or {@code null} if no such tab is focused.
     */
    public static @Nullable NavigatorTabView getCurrentManagerTab(GreyWolfShellView shell) {
        var workspace = shell.getComposer().getWorkspace();
        var currentTab = workspace.getComposer().getSelectedTab();
        if (currentTab instanceof NavigatorTabView navigatorTab) {
            return navigatorTab;
        }
        if (!(currentTab instanceof DualPanelsTabView panelManagerTab)) {
            return null;
        }
        var selectedTab = panelManagerTab.getComposer().getCurrentTab();
        return selectedTab instanceof NavigatorTabView managerTab ? managerTab : null;
    }

    /**
     * Returns the area of the navigator tab that currently has focus within {@code shell}, or {@code null} if no
     * such tab is focused.
     */
    public static @Nullable NavigatorAreaView getCurrentArea(GreyWolfShellView shell) {
        var managerTab = getCurrentManagerTab(shell);
        return managerTab == null ? null : managerTab.getComposer().getArea();
    }

    /**
     * Returns the port of the area of the navigator tab that {@code view}, the location bar or the directory tree
     * of the tab, belongs to, or {@code null} if it is neither.
     */
    public static @Nullable NavigatorAreaPort getAreaPort(FileView<?> view) {
        if (!(view instanceof ChildView<?> child)) {
            return null;
        }
        var parent = child.getComposer().getParent();
        if (parent instanceof NavigatorTabView tab) {
            return tab.getComposer().getAreaPort();
        }
        return parent instanceof LocationTabView locationTab ? locationTab.getComposer().getAreaPort() : null;
    }

    /**
     * Returns the recipient that is the selected tab of the panel other than the one {@code placement} names, or
     * {@code null} if the placement is not a panel, the shell shows no dual panels tab, or that tab cannot receive.
     */
    public static @Nullable FileRecipient getOtherPanelRecipient(GreyWolfShellView shell,
            @Nullable Placement placement) {
        if (placement == null || !placement.isPanel()) {
            return null;
        }
        var workspace = shell.getComposer().getWorkspace();
        if (!(workspace.getComposer().getSelectedTab() instanceof DualPanelsTabView dualPanels)) {
            return null;
        }
        var panel = placement == Placement.FIRST_PANEL ? Panel.FIRST : Panel.SECOND;
        return dualPanels.getComposer().getSelectedTabPort(panel.getOther()) instanceof FileRecipient recipient
                ? recipient : null;
    }

    /**
     * Returns a lazy value of the tab that currently has focus within {@code shell}: the selected workspace tab or,
     * for a dual panels tab, the current tab of its panels. The caller keeps it to remove its own listener.
     */
    public static ObservableValue<@Nullable TabView<?>> observeCurrentTab(GreyWolfShellView shell) {
        return shell.getComposer().getWorkspace().getComposer().selectedTabProperty().flatMap(tab ->
                tab instanceof DualPanelsTabView dualPanels
                        ? dualPanels.getComposer().currentTabProperty()
                        : new SimpleObjectProperty<@Nullable TabView<?>>(tab));
    }

    private ComponentUtils() {
        // empty
    }
}
