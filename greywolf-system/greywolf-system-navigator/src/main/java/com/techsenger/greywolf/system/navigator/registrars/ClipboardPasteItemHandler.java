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
 * Pastes the content of the clipboard into a directory; does nothing when the area or the directory is not
 * available.
 *
 * @author Pavel Castornii
 */
public class ClipboardPasteItemHandler extends AbstractAreaItemHandler {

    private final Function<NavigatorAreaPort, @Nullable GreyWolfFile> directorySelector;

    /**
     * Creates a handler.
     *
     * @param greyWolf the application
     * @param areaSupplier provides the area that performs the paste, or {@code null} for none
     * @param directorySelector tells the directory to paste into, or {@code null} for none, such as the current
     *     directory of the area
     */
    public ClipboardPasteItemHandler(GreyWolf greyWolf, Supplier<@Nullable NavigatorAreaPort> areaSupplier,
            Function<NavigatorAreaPort, @Nullable GreyWolfFile> directorySelector) {
        super(greyWolf, areaSupplier);
        this.directorySelector = directorySelector;
    }

    @Override
    protected void handle(NavigatorAreaPort area) {
        var directory = directorySelector.apply(area);
        if (directory != null) {
            area.onPasteIntoFolder(directory);
        }
    }
}
