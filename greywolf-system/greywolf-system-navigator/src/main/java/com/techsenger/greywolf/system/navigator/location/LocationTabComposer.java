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

package com.techsenger.greywolf.system.navigator.location;

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.system.navigator.area.NavigatorAreaPort;
import com.techsenger.greywolf.shared.directory.DirectoryTreeParams;
import com.techsenger.greywolf.shared.directory.DirectoryTreePort;
import com.techsenger.shellfx.core.tab.TabComposer;

/**
 * Scene-graph-side operations this tab's view model can invoke on its composed child areas.
 *
 * @author Pavel Castornii
 */
public interface LocationTabComposer extends TabComposer {

    /**
     * Returns the port of this tab's composed directory-tree half, or {@code null} when the current mode
     * doesn't include it.
     */
    @Nullable DirectoryTreePort getDirectoryTreePort();

    /**
     * Returns the port of this tab's composed places half, or {@code null} when the current mode doesn't
     * include it.
     */
    @Nullable PlacesAreaPort getPlacesAreaPort();

    /**
     * Returns the port of the area of the navigator tab this tab belongs to.
     */
    NavigatorAreaPort getAreaPort();

    /**
     * Applies {@code mode}, lazily composing whichever half(s) aren't already composed from
     * {@code directoryTreeParams}/{@code placesParams}.
     */
    void updateMode(LocationMode mode, @Nullable DirectoryTreeParams directoryTreeParams,
            @Nullable PlacesAreaParams placesParams);
}
