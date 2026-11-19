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
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfHandler;
import com.techsenger.greywolf.system.navigator.area.NavigatorAreaPort;
import java.util.function.Supplier;
import javafx.event.ActionEvent;

/**
 * The base of the handlers of the items that act on a navigator area: the owner of an item says where the area
 * comes from, so the same handler serves the main menu (the area in focus) and the menu of an area itself.
 *
 * @author Pavel Castornii
 */
public abstract class AbstractAreaItemHandler extends AbstractGreyWolfHandler<ActionEvent> {

    private final Supplier<@Nullable NavigatorAreaPort> areaSupplier;

    /**
     * Creates a handler.
     *
     * @param greyWolf the application
     * @param areaSupplier provides the area to act on when the item is activated, or {@code null} for none
     */
    protected AbstractAreaItemHandler(GreyWolf greyWolf, Supplier<@Nullable NavigatorAreaPort> areaSupplier) {
        super(greyWolf);
        this.areaSupplier = areaSupplier;
    }

    @Override
    public final void handle(ActionEvent event) {
        var area = areaSupplier.get();
        if (area != null) {
            handle(area);
        }
    }

    /**
     * Performs the action of the item.
     *
     * @param area the area to act on
     */
    protected abstract void handle(NavigatorAreaPort area);
}
