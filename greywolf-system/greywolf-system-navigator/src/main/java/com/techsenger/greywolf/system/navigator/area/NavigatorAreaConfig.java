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

package com.techsenger.greywolf.system.navigator.area;

import com.techsenger.shellfx.core.area.AreaConfig;
import java.io.Serial;

/**
 * The view-mode settings (list/tiles/details, column counts, table columns) a navigator area can live in. Nested
 * in {@code NavigatorTabConfig} - which is shared across every navigator tab in the application - rather
 * than registered on its own, so it persists across restarts.
 *
 * <p>{@link #getFirstPanelViewModes()}/{@link #getSecondPanelViewModes()} are shared by every area currently
 * embedded in the given panel: an area that changes its state stores it there and the others of that panel follow.
 * {@link #getWorkspaceViewModes()} only remembers the state an area in the workspace used last: a workspace area
 * is independent of the others, so a new one just starts from it.
 *
 * @author Pavel Castornii
 */
public class NavigatorAreaConfig extends AreaConfig {

    @Serial
    private static final long serialVersionUID = 1L;

    private ViewModes firstPanelViewModes;

    private ViewModes secondPanelViewModes;

    private ViewModes workspaceViewModes;

    public NavigatorAreaConfig() {
        this.firstPanelViewModes = new ViewModes(DefaultColumnCounts.PANEL);
        this.secondPanelViewModes = new ViewModes(DefaultColumnCounts.PANEL);
        this.workspaceViewModes = new ViewModes(DefaultColumnCounts.WORKSPACE);
    }

    public ViewModes getFirstPanelViewModes() {
        return firstPanelViewModes;
    }

    public ViewModes getSecondPanelViewModes() {
        return secondPanelViewModes;
    }

    public ViewModes getWorkspaceViewModes() {
        return workspaceViewModes;
    }
}
