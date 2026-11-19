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

package com.techsenger.greywolf.samples.plugin.registrars;

import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfHandler;
import com.techsenger.greywolf.system.navigator.area.NavigatorAreaView;
import com.techsenger.shellfx.core.window.WindowType;
import com.techsenger.shellfx.dialogs.alert.AlertDialogParams;
import com.techsenger.shellfx.dialogs.alert.AlertDialogType;
import javafx.event.ActionEvent;

/**
 * Shows the name of the file that is currently selected in the area, or a note that no file is selected.
 *
 * <p>A control acts on the level of a view, so the handler works with the view of the area, not with its port. A port
 * gives state and operations of a component, but only the composer of a view can add a new component, such as a
 * dialog, and put it where it must be shown. The same handler serves every control of the plugin, such as the tool
 * bar button and the item of the area context menu, that trigger this action.
 *
 * @author Pavel Castornii
 */
public class ShowFileItemHandler extends AbstractGreyWolfHandler<ActionEvent> {

    private final NavigatorAreaView areaView;

    public ShowFileItemHandler(GreyWolf greyWolf, NavigatorAreaView areaView) {
        super(greyWolf);
        this.areaView = areaView;
    }

    @Override
    public void handle(ActionEvent event) {
        var greyWolf = getGreyWolf();
        var file = areaView.getViewModel().getFile();
        var params = new AlertDialogParams(WindowType.NESTED, greyWolf.getSettings(), AlertDialogType.INFO);
        var dialog = areaView.getComposer().openAlertDialog(params);
        dialog.setMessage(file != null ? "Current file: " + file.getName() : "No file is selected");
        dialog.setVisible(true);
    }
}
