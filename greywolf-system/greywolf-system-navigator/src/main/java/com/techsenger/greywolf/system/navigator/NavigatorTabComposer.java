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
import com.techsenger.greywolf.core.panels.PanelTabDockPort;
import com.techsenger.greywolf.core.panels.Placement;
import com.techsenger.shellfx.core.tab.HostTabComposer;

/**
 * Scene-graph-side operations this tab's view model can invoke on its composed child components and host.
 *
 * @author Pavel Castornii
 */
public interface NavigatorTabComposer extends HostTabComposer, NavigatorTabPort.ComposerAccess {

    /**
     * Arranges this tab's content for {@code placement}: a full docking layout (built lazily) with the
     * area as its main area for WORKSPACE, or just the area's own node for a panel (see the root
     * README's &quot;Panel Manager&quot; section). Called by the view model once the placement is
     * actually known, unlike composition, which runs before this tab is attached to any container.
     *
     * @param placement this tab's current placement, or {@code null} while unattached.
     */
    void layout(@Nullable Placement placement);

    /**
     * Returns the port of the panel tab dock this tab is currently embedded in, derived live from this
     * tab's actual parent, or {@code null} when it is standalone (top-level). Never cached &mdash; this
     * tab can be dragged between panels or out to a top-level tab at any time, so every caller re-derives
     * it from the current parent rather than trusting a value that may already be stale.
     */
    @Nullable PanelTabDockPort getPanelTabDockPort();
}
