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

package com.techsenger.greywolf.core.panels;

import com.techsenger.shellfx.layout.dockhost.TabDockPort;
import com.techsenger.shellfx.layout.tabhost.TabHostPort;
import javafx.beans.property.ReadOnlyBooleanProperty;

/**
 * Dock that hosts the tabs of a single panel (first or second).
 *
 * @author Pavel Castornii
 */
public interface PanelTabDockPort extends TabDockPort {

    interface ComposerAccess extends TabHostPort.ComposerAccess {

        /**
         * Returns the port of the {@code DualPanelsTab} this dock belongs to - every {@code PanelTabDock}
         * lives inside exactly one, so a tab embedded here (e.g. via the &quot;New&quot; menu) can reach its
         * sibling panel or the enclosing shell.
         */
        DualPanelsTabPort getDualPanelsTabPort();
    }

    @Override
    ComposerAccess getComposerAccess();

    Panel getPanel();

    boolean isActive();

    ReadOnlyBooleanProperty activeProperty();
}
