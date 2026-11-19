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

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.core.tab.TabContainerView;

/**
 * Resolves a {@link Placement} from a tab's actual container. Kept out of {@link Placement} itself, since
 * that enum is also used from view-model/port code that must not depend on View types.
 *
 * @author Pavel Castornii
 */
public final class PlacementResolver {

    /**
     * Derives the placement implied by {@code container}, the current value of a tab's own
     * {@code parentProperty()}: {@link Placement#FIRST_PANEL} or {@link Placement#SECOND_PANEL} if it is a
     * {@code PanelTabDockView}, {@link Placement#WORKSPACE} otherwise, or {@code null} if {@code container}
     * itself is {@code null} - not currently attached to any container (e.g. the brief window before the
     * initial attach, or mid-drag between one container and the next).
     *
     * @param container the tab's current container, or {@code null} if not currently attached.
     * @return the implied placement, or {@code null} while unattached.
     */
    public static @Nullable Placement ofContainer(@Nullable TabContainerView<?> container) {
        if (container instanceof PanelTabDockView dock) {
            return dock.getViewModel().getPanel() == Panel.FIRST ? Placement.FIRST_PANEL : Placement.SECOND_PANEL;
        }
        return container == null ? null : Placement.WORKSPACE;
    }

    private PlacementResolver() {
        // empty
    }
}
