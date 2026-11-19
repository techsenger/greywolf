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
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Opens a folder in the area, as a click on it does; does nothing when the area or the folder is not available.
 *
 * @author Pavel Castornii
 */
public class OpenInItemHandler extends AbstractAreaItemHandler {

    private final Function<NavigatorAreaPort, @Nullable GreyWolfFile> folderSelector;

    /**
     * Creates a handler.
     *
     * @param greyWolf the application
     * @param areaSupplier provides the area that opens the folder, or {@code null} for none
     * @param folderSelector tells the folder to open, or {@code null} for none
     */
    public OpenInItemHandler(GreyWolf greyWolf, Supplier<@Nullable NavigatorAreaPort> areaSupplier,
            Function<NavigatorAreaPort, @Nullable GreyWolfFile> folderSelector) {
        super(greyWolf, areaSupplier);
        this.folderSelector = folderSelector;
    }

    @Override
    protected void handle(NavigatorAreaPort area) {
        var folder = folderSelector.apply(area);
        if (folder != null) {
            area.onOpenIn(folder);
        }
    }
}
