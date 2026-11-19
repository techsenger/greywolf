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

import atlantafx.base.theme.Styles;
import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfControlRegistrar;
import com.techsenger.greywolf.system.navigator.slots.ToolBarSlots;
import com.techsenger.greywolf.system.navigator.toolbar.ToolBarView;
import com.techsenger.greywolf.samples.plugin.style.PluginIcons;
import com.techsenger.shellfx.core.registries.SimpleControlProvider;
import com.techsenger.shellfx.material.OverlayUtils;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.style.StyleClasses;
import java.util.Objects;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

/**
 * Adds the sample plugin button to the Navigator tool bar. The button shows the name of the file that is
 * currently selected in the area of the tool bar.
 *
 * <p>The control acts on the level of a view: the handler of the button gets the view of the area, not a port, as
 * only a view can add new components, such as dialogs, to the place where they must be shown.
 *
 * @author Pavel Castornii
 */
public class ToolBarControlRegistrar extends AbstractGreyWolfControlRegistrar {

    public ToolBarControlRegistrar(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void register() {
        register(ToolBarSlots.OTHER_GROUP, 1000, () -> new SimpleControlProvider<ToolBarView, Button>() {

            @Override
            public void initialize(ToolBarView view) {
                super.initialize(view);
                var button = new Button(null, new FontIconView(PluginIcons.ROCKET));
                button.getStyleClass().addAll(StyleClasses.SQUARE, Styles.FLAT);
                button.setTooltip(new Tooltip("Sample plugin button"));
                // The tool bar is built before its tab owns it, so the area is looked up when the button is pressed.
                // The dialog covers the button, so it is opened through OverlayUtils to keep the button from
                // staying pressed.
                button.setOnAction(e -> OverlayUtils.runMouseAction(button, e2 -> {
                    var tab = Objects.requireNonNull(view.getComposer().getParent());
                    new ShowFileItemHandler(getGreyWolf(), tab.getComposer().getArea()).handle(e2);
                }));
                setControl(button);
            }
        });
    }
}
