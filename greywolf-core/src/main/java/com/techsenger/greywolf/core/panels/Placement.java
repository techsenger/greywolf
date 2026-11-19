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

/**
 * Tells a component (e.g. a navigator or a terminal) which rules it should follow, based on where it is
 * currently placed - a plain top-level workspace tab, or the first or the second panel of a
 * {@code DualPanelsTab}. Not currently attached to any of them is represented by {@code null}, not a constant
 * here, since that isn't really a placement - see {@link PlacementResolver#ofContainer}.
 *
 * @author Pavel Castornii
 */
public enum Placement {

    /**
     * The component is hosted as a top-level tab in the shell's workspace, with no owning panel.
     */
    WORKSPACE,

    /**
     * The component is embedded in the first panel of a {@code DualPanelsTab}.
     */
    FIRST_PANEL,

    /**
     * The component is embedded in the second panel of a {@code DualPanelsTab}.
     */
    SECOND_PANEL;

    /**
     * Tells whether this placement is one of the two panels of a {@code DualPanelsTab}.
     */
    public boolean isPanel() {
        return this != WORKSPACE;
    }
}
