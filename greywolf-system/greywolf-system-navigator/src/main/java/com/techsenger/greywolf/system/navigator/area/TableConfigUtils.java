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

import com.techsenger.greywolf.system.navigator.NavigatorFileColumns;
import com.techsenger.shellfx.material.table.TableColumnInfo;
import com.techsenger.shellfx.material.table.TableColumnInfoAdapter;
import com.techsenger.shellfx.material.table.TableColumnName;
import com.techsenger.shellfx.material.table.TableConfig;
import com.techsenger.shellfx.storage.ui.FileColumns;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import javafx.scene.control.TableColumn;

/**
 * Helpers for the column state a navigator keeps in a {@link TableConfig}: independent copies, snapshots of a
 * live column state and comparison of two states.
 *
 * @author Pavel Castornii
 */
final class TableConfigUtils {

    /**
     * Width differences below this many pixels are not considered a change, since a resize policy may
     * recompute a width slightly differently on every layout.
     */
    private static final double WIDTH_EPSILON = 0.5;

    private static final double DEFAULT_COLUMN_WIDTH = 125;

    private TableConfigUtils() {
        // empty
    }

    /**
     * Returns the column state a navigator starts from: name, size, type and last modified columns in this
     * order, sorted by name ascending.
     */
    static TableConfig createDefault() {
        var nameInfo = new TableColumnInfo(FileColumns.NAME);
        nameInfo.setIndex(0);
        nameInfo.setSortIndex(0);
        nameInfo.setSortType(TableColumn.SortType.ASCENDING);

        var sizeInfo = new TableColumnInfo(FileColumns.SIZE);
        sizeInfo.setIndex(1);
        sizeInfo.setWidth(DEFAULT_COLUMN_WIDTH);

        var typeInfo = new TableColumnInfo(NavigatorFileColumns.TYPE);
        typeInfo.setIndex(2);
        typeInfo.setWidth(DEFAULT_COLUMN_WIDTH);

        var lastModifiedInfo = new TableColumnInfo(FileColumns.LAST_MODIFIED);
        lastModifiedInfo.setIndex(3);
        lastModifiedInfo.setWidth(DEFAULT_COLUMN_WIDTH);

        return new TableConfig(List.of(nameInfo, sizeInfo, typeInfo, lastModifiedInfo));
    }

    /**
     * Returns a config holding independent copies of {@code infos}.
     *
     * @param infos the column state to snapshot.
     */
    static TableConfig snapshot(Collection<TableColumnInfoAdapter> infos) {
        return new TableConfig(infos.stream().map(adapter -> new TableColumnInfo(adapter.getInfo())).toList());
    }

    /**
     * Tells whether {@code first} and {@code second} describe the same columns, with the same positions, widths
     * and sorting. A width that is not set on either side (the column is flexible) is not a difference.
     *
     * @param first the first column state.
     * @param second the second column state.
     */
    static boolean isSame(Collection<TableColumnInfo> first, Collection<TableColumnInfo> second) {
        if (first.size() != second.size()) {
            return false;
        }
        var secondByName = new HashMap<TableColumnName, TableColumnInfo>();
        for (var info : second) {
            secondByName.put(info.getName(), info);
        }
        for (var info : first) {
            var other = secondByName.get(info.getName());
            if (other == null || !isSame(info, other)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isSame(TableColumnInfo first, TableColumnInfo second) {
        return first.getIndex() == second.getIndex()
                && isSameWidth(first.getWidth(), second.getWidth())
                && Objects.equals(first.getSortIndex(), second.getSortIndex())
                && first.getSortType() == second.getSortType();
    }

    private static boolean isSameWidth(Double first, Double second) {
        if (first == null || second == null) {
            return true;
        }
        return Math.abs(first - second) < WIDTH_EPSILON;
    }
}
