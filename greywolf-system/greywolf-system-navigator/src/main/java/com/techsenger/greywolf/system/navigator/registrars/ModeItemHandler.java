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
import com.techsenger.greywolf.system.navigator.area.ViewModeType;
import java.util.function.Supplier;

/**
 * Switches the view mode of the area and gives it the focus.
 *
 * @author Pavel Castornii
 */
public class ModeItemHandler extends AbstractAreaItemHandler {

    private final ViewModeType modeType;

    public ModeItemHandler(GreyWolf greyWolf, Supplier<@Nullable NavigatorAreaPort> areaSupplier,
            ViewModeType modeType) {
        super(greyWolf, areaSupplier);
        this.modeType = modeType;
    }

    @Override
    protected void handle(NavigatorAreaPort area) {
        area.setViewModeType(modeType);
        area.requestFocus();
    }
}
