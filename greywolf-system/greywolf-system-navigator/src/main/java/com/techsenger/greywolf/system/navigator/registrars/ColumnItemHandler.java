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

package com.techsenger.greywolf.system.navigator.registrars;

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.system.navigator.area.NavigatorAreaPort;
import com.techsenger.shellfx.material.table.TableColumnName;
import java.util.function.Supplier;
import javafx.scene.control.CheckMenuItem;

/**
 * Shows or hides a single table column of the area, following the selected state of the item.
 *
 * @author Pavel Castornii
 */
public class ColumnItemHandler extends AbstractAreaItemHandler {

    private final CheckMenuItem item;

    private final TableColumnName columnName;

    public ColumnItemHandler(GreyWolf greyWolf, Supplier<@Nullable NavigatorAreaPort> areaSupplier,
            CheckMenuItem item, TableColumnName columnName) {
        super(greyWolf, areaSupplier);
        this.item = item;
        this.columnName = columnName;
    }

    @Override
    protected void handle(NavigatorAreaPort area) {
        if (item.isSelected()) {
            area.onAddColumn(columnName);
        } else {
            area.onRemoveColumn(columnName);
        }
    }
}
