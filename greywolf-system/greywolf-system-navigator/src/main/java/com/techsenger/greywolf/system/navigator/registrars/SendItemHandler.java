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
import com.techsenger.greywolf.system.navigator.utils.ComponentUtils;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Sends a file to the tab of the other panel; nothing happens if that tab does not accept it or there
 * is no file to send.
 *
 * @author Pavel Castornii
 */
public class SendItemHandler extends AbstractAreaItemHandler {

    private final Function<NavigatorAreaPort, @Nullable GreyWolfFile> fileSelector;

    /**
     * Creates a handler.
     *
     * @param greyWolf the application
     * @param areaSupplier provides the area whose other panel receives the file, or {@code null} for none
     * @param fileSelector tells the file to send, or {@code null} for none, such as the selected file or the current
     *     folder of the area
     */
    public SendItemHandler(GreyWolf greyWolf, Supplier<@Nullable NavigatorAreaPort> areaSupplier,
            Function<NavigatorAreaPort, @Nullable GreyWolfFile> fileSelector) {
        super(greyWolf, areaSupplier);
        this.fileSelector = fileSelector;
    }

    @Override
    protected void handle(NavigatorAreaPort area) {
        var recipient = ComponentUtils.getOtherPanelRecipient(getShell(), area.getPlacement());
        if (recipient == null) {
            return;
        }
        var file = fileSelector.apply(area);
        if (file != null) {
            recipient.receive(file);
        }
    }
}
