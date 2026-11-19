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

package com.techsenger.greywolf.core.registrar;

import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.core.GreyWolfShellView;
import javafx.event.Event;
import javafx.event.EventHandler;

/**
 * Base handler of the controls GreyWolf and its plugins register, such as the action of a menu item.
 *
 * @param <E> the type of the handled event
 * @author Pavel Castornii
 */
public abstract class AbstractGreyWolfHandler<E extends Event> implements EventHandler<E> {

    private final GreyWolf greyWolf;

    protected AbstractGreyWolfHandler(GreyWolf greyWolf) {
        this.greyWolf = greyWolf;
    }

    protected GreyWolf getGreyWolf() {
        return greyWolf;
    }

    protected GreyWolfShellView getShell() {
        return greyWolf.getShell();
    }
}
