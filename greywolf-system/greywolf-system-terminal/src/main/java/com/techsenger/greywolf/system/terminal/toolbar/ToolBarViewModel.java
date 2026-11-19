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

package com.techsenger.greywolf.system.terminal.toolbar;

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.core.panels.Placement;
import com.techsenger.shellfx.core.area.AbstractAreaViewModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author Pavel Castornii
 */
public class ToolBarViewModel extends AbstractAreaViewModel<ToolBarComposer> implements ToolBarPort {

    private final ObjectProperty<@Nullable Placement> placement = new SimpleObjectProperty<>();

    public ToolBarViewModel(ToolBarParams params) {
        super(params);
    }

    @Override
    public @Nullable Placement getPlacement() {
        return placement.get();
    }

    @Override
    public void setPlacement(@Nullable Placement placement) {
        this.placement.set(placement);
    }

    @Override
    public ObjectProperty<@Nullable Placement> placementProperty() {
        return placement;
    }

    @Override
    public ToolBarPort.ComposerAccess getComposerAccess() {
        return getComposer();
    }
}
