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

package com.techsenger.greywolf.system.navigator;

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.core.GreyWolfShellPort;
import com.techsenger.greywolf.core.panels.Placement;
import com.techsenger.greywolf.system.navigator.area.NavigatorAreaPort;
import com.techsenger.greywolf.system.navigator.location.LocationMode;
import com.techsenger.greywolf.system.navigator.location.LocationTabParams;
import com.techsenger.greywolf.system.navigator.location.LocationTabPort;
import com.techsenger.greywolf.system.navigator.toolbar.ToolBarPort;
import com.techsenger.greywolf.shared.location.LocationBarPort;
import com.techsenger.greywolf.storage.ui.view.FileRecipient;
import com.techsenger.shellfx.core.tab.HostTabPort;
import javafx.beans.property.ReadOnlyObjectProperty;

/**
 * A single navigator tab: hosts a directory-listing area plus this tab's own chrome (toolbar, location bar,
 * location tab) and docking layout.
 *
 * @author Pavel Castornii
 */
public interface NavigatorTabPort extends HostTabPort, FileRecipient {

    /**
     * Scene-graph-side lookups this tab's siblings (chiefly the area) can use to reach the ports of this tab's
     * other composed children, without this tab relaying data between them itself.
     */
    interface ComposerAccess extends HostTabPort.ComposerAccess {

        @Override
        GreyWolfShellPort getShellPort();

        /**
         * Returns the port of this tab's composed toolbar.
         */
        ToolBarPort getToolBarPort();

        /**
         * Returns the port of this tab's composed location bar.
         */
        LocationBarPort getLocationBarPort();

        /**
         * Returns the port of this tab's composed area, which owns the file listing, selection, mode, and
         * column state.
         */
        NavigatorAreaPort getAreaPort();

        /**
         * Shows this tab's location tab, creating it from {@code params} (lazily building the docking layout
         * it lives in, if not already built) if it isn't already open, and returns its port; {@code params} is
         * ignored if it's already open. {@code params} is built by the caller, which is the only party that
         * knows what {@link LocationMode} and other initial values a freshly created tab should start with.
         *
         * @param params the params to construct the location tab from, if not already open.
         */
        LocationTabPort openLocationTab(LocationTabParams params);

        /**
         * Opens a new navigator tab next to this one: in the same panel when this tab is embedded in a panel,
         * among the top-level tabs otherwise.
         *
         * @param params the params of the new tab.
         */
        void openTab(NavigatorTabParams params);

        /**
         * Returns the port of this tab's composed location tab, or {@code null} when it isn't currently
         * shown. The location tab composes a directory tree and/or a flat Personal/Recents places list
         * depending on its own mode - see {@link LocationMode}.
         */
        @Nullable LocationTabPort getLocationTabPort();
    }

    @Override
    ComposerAccess getComposerAccess();

    /**
     * Returns where this tab is currently placed - a top-level workspace tab or a panel manager panel -
     * derived from its actual container, or {@code null} while not currently attached to one.
     */
    @Nullable Placement getPlacement();

    ReadOnlyObjectProperty<@Nullable Placement> placementProperty();
}
