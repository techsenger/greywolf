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

import java.io.Serializable;

/**
 * The way a navigator area's files are rendered. Owned by the area and tracked per placement (a panel or
 * the workspace), not by the tab that hosts the area.
 *
 * <p>{@code extends Serializable} because it is persisted as a field of the area's config: config classes
 * round-trip through plain Java serialization, so every implementation must be serializable too.
 *
 * @author Pavel Castornii
 */
public interface ViewMode extends Serializable {

    ViewModeType getType();

    /**
     * Tells whether this mode lays files out in a configurable number of columns.
     */
    boolean hasColumnCount();
}
