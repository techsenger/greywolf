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

import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfHandler;
import com.techsenger.greywolf.system.navigator.location.LocationMode;
import com.techsenger.greywolf.system.navigator.location.LocationTabParams;
import com.techsenger.greywolf.system.navigator.utils.ComponentUtils;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckMenuItem;

/**
 * Toggles the location tab of the file tab that currently has focus within the current panel manager tab's
 * panel into the mode; a no-op when no such tab is focused. The item's {@code selected} state is recomputed from
 * the tab's actual {@link LocationMode}, so checking one mode's item automatically unchecks the previously
 * checked one; unchecking the currently active mode's item closes the location tab entirely.
 *
 * @author Pavel Castornii
 */
public class LocationModeItemHandler extends AbstractGreyWolfHandler<ActionEvent> {

    private final CheckMenuItem item;

    private final LocationMode mode;

    public LocationModeItemHandler(GreyWolf greyWolf, CheckMenuItem item, LocationMode mode) {
        super(greyWolf);
        this.item = item;
        this.mode = mode;
    }

    @Override
    public void handle(ActionEvent event) {
        var managerTab = ComponentUtils.getCurrentManagerTab(getShell());
        if (managerTab == null) {
            return;
        }
        var composer = managerTab.getComposer();
        if (item.isSelected()) {
            var port = composer.getLocationTabPort();
            if (port == null) {
                var context = getShell().getContext();
                var params = new LocationTabParams(mode, context.getFileStorageRegistry(), context.getSettings());
                var locationPort = composer.openLocationTab(params);
                var directory = composer.getAreaPort().getDirectory();
                if (directory != null) {
                    locationPort.setDirectory(directory);
                }
            } else {
                port.setMode(mode);
            }
        } else {
            var port = composer.getLocationTabPort();
            if (port != null) {
                port.close();
            }
        }
    }
}
