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

package com.techsenger.greywolf.system.terminal.toolbar;

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.core.panels.Placement;
import com.techsenger.greywolf.system.terminal.TerminalTabPort;
import com.techsenger.greywolf.system.terminal.area.TerminalAreaPort;
import com.techsenger.patternfx.core.ChildPort;
import com.techsenger.shellfx.core.area.AreaPort;
import javafx.beans.property.ObjectProperty;

/**
 * Provides full access to the component's client API.
 *
 * @author Pavel Castornii
 */
public interface ToolBarPort extends AreaPort {

    interface ComposerAccess extends ChildPort.ComposerAccess {

        @Override
        @Nullable TerminalTabPort getParentPort();

        /**
         * Returns the area the tool bar works on. The controls of the tool bar derive their own state from it.
         */
        TerminalAreaPort getAreaPort();
    }

    @Nullable Placement getPlacement();

    /**
     * Mirrors the owning tab's placement for styling purposes only &mdash; the tab, not this toolbar, is the
     * single source of truth for it.
     *
     * @param placement the tab's current placement, or {@code null} while unattached.
     */
    void setPlacement(@Nullable Placement placement);

    ObjectProperty<@Nullable Placement> placementProperty();

    @Override
    ComposerAccess getComposerAccess();
}
