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
 * Package-private so that only {@code DualPanelsTab} - the sole legitimate caller - can set a
 * {@code PanelTabDock}'s active state; it is never part of {@link PanelTabDockPort}, which is public and can
 * end up in code far outside this package.
 *
 * @author Pavel Castornii
 */
interface ActivatedPanelTabDockPort {

    /**
     * Sets the active state of this panel.
     *
     * <p>Intended for internal use only &mdash; the active state is normally derived automatically from
     * which component currently owns focus. Calling this directly may break that focus-based activation
     * logic.
     *
     * @param active the new active state
     */
    void setActive(boolean active);
}
