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

import com.techsenger.shellfx.material.table.TableConfig;

/**
 * The details (table) mode. Holds the last state of the table - columns, their order and widths, and sorting -
 * of the placement (a panel or the workspace) this instance belongs to.
 *
 * @author Pavel Castornii
 */
public class DetailsViewMode extends AbstractViewMode {

    private TableConfig tableConfig;

    public DetailsViewMode() {
        super(ViewModeType.DETAILS);
        this.tableConfig = TableConfigUtils.createDefault();
    }

    public TableConfig getTableConfig() {
        return tableConfig;
    }

    public void setTableConfig(TableConfig tableConfig) {
        this.tableConfig = tableConfig;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DetailsViewMode;
    }

    @Override
    public int hashCode() {
        return DetailsViewMode.class.hashCode();
    }
}
