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
 * The three {@link ViewMode} instances (list/tiles/details) of a single placement - the first panel, the second
 * panel or the workspace - plus which of them is the current one. Kept as three live instances rather than
 * created on demand, so switching away from and back to a mode (e.g. List &rarr; Tiles &rarr; List) restores
 * whatever that mode had before, instead of resetting to a default.
 *
 * @author Pavel Castornii
 */
public class ViewModes implements Serializable {

    private final ListViewMode listMode;

    private final TilesViewMode tilesMode;

    private final DetailsViewMode detailsMode = new DetailsViewMode();

    private ViewModeType currentType = ViewModeType.LIST;

    /**
     * Creates the view modes with the given initial column count of both the list and the tiles modes.
     *
     * @param columnCount the initial column count.
     */
    public ViewModes(int columnCount) {
        this.listMode = new ListViewMode(columnCount);
        this.tilesMode = new TilesViewMode(columnCount);
    }

    public ViewModeType getCurrentType() {
        return currentType;
    }

    public void setCurrentType(ViewModeType type) {
        this.currentType = type;
    }

    public DetailsViewMode getDetailsMode() {
        return detailsMode;
    }

    /**
     * Returns the instance of this placement for {@code type} - the same one every time, never a fresh one.
     *
     * @param type the mode type to look up.
     */
    public ViewMode getMode(ViewModeType type) {
        return switch (type) {
            case LIST -> listMode;
            case TILES -> tilesMode;
            case DETAILS -> detailsMode;
        };
    }
}
