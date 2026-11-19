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

import java.util.Objects;

/**
 *
 * @author Pavel Castornii
 */
public final class TilesViewMode extends AbstractViewMode {

    private int columnCount;

    public TilesViewMode(int columnCount) {
        super(ViewModeType.TILES);
        setColumnCount(columnCount);
    }

    @Override
    public boolean hasColumnCount() {
        return true;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(int columnCount) {
        if (columnCount < 1) {
            throw new IllegalArgumentException("columnCount must be at least 1, got " + columnCount);
        }
        this.columnCount = columnCount;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TilesViewMode other && this.columnCount == other.columnCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(TilesViewMode.class, columnCount);
    }
}
