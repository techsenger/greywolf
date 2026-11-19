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

package com.techsenger.greywolf.system.terminal.registrars;

import atlantafx.base.theme.Styles;
import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfControlRegistrar;
import com.techsenger.greywolf.shared.style.SharedIcons;
import com.techsenger.greywolf.system.terminal.area.TerminalAreaPort;
import com.techsenger.greywolf.system.terminal.area.TerminalCommand;
import com.techsenger.greywolf.system.terminal.area.TerminalPaletteType;
import com.techsenger.greywolf.system.terminal.style.TerminalIcons;
import com.techsenger.greywolf.system.terminal.slots.ToolBarSlots;
import com.techsenger.greywolf.system.terminal.toolbar.ToolBarView;
import com.techsenger.shellfx.base.icon.FontIcon;
import com.techsenger.shellfx.core.registries.SimpleControlProvider;
import com.techsenger.shellfx.core.registries.SimpleGroupProvider;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.style.StyleClasses;
import com.techsenger.toolkit.fx.value.ValueUtils;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Callback;

/**
 * Registers the controls of the Terminal tool bar - the tool bar itself, its group and the buttons, the spacer
 * and the palette selector in it. Where they sit is set by {@link ToolBarSlotRegistrar}.
 *
 * @author Pavel Castornii
 */
public class ToolBarControlRegistrar extends AbstractGreyWolfControlRegistrar {

    /**
     * Provides a flat icon button that can be disabled by a condition derived from the area of the tool bar.
     */
    private static final class ButtonProvider extends SimpleControlProvider<ToolBarView, Button> {

        private final FontIcon<?> icon;

        private final String tooltip;

        private final @Nullable Function<TerminalAreaPort, ObservableValue<Boolean>> disabled;

        private final Consumer<TerminalAreaPort> action;

        ButtonProvider(FontIcon<?> icon, String tooltip,
                @Nullable Function<TerminalAreaPort, ObservableValue<Boolean>> disabled,
                Consumer<TerminalAreaPort> action) {
            this.icon = icon;
            this.tooltip = tooltip;
            this.disabled = disabled;
            this.action = action;
        }

        @Override
        public void initialize(ToolBarView view) {
            super.initialize(view);
            var area = view.getComposer().getAreaPort();
            var button = new Button(null, new FontIconView(icon));
            button.getStyleClass().addAll(StyleClasses.SQUARE, Styles.FLAT);
            button.setTooltip(new Tooltip(tooltip));
            if (disabled != null) {
                button.disableProperty().bind(disabled.apply(area));
            }
            button.setOnAction(e -> action.accept(area));
            setControl(button);
        }

        @Override
        public void deinitialize(ToolBarView view) {
            super.deinitialize(view);
            if (disabled != null) {
                getControl().disableProperty().unbind();
            }
        }
    }

    public ToolBarControlRegistrar(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void register() {
        register(ToolBarSlots.TOOL_BAR, () -> new SimpleControlProvider<>(new ToolBar()));
        register(ToolBarSlots.DEFAULT_GROUP, SimpleGroupProvider::new);

        registerNewButton();
        registerClearButton();
        registerCopyButton();
        registerPasteButton();
        registerSelectAllButton();
        registerFindButton();
        registerPageUpButton();
        registerPageDownButton();
        registerLineUpButton();
        registerLineDownButton();
        registerSpacer();
        registerPaletteLabel();
        registerPaletteComboBox();
    }

    private void registerNewButton() {
        register(ToolBarSlots.DEFAULT_GROUP, 100, () -> new ButtonProvider(SharedIcons.ADD, "New",
                null, TerminalAreaPort::onAddNew));
    }

    private void registerClearButton() {
        register(ToolBarSlots.DEFAULT_GROUP, 200, () -> new ButtonProvider(SharedIcons.CLEAR, "Clear",
                null, area -> area.onCommand(TerminalCommand.CLEAR)));
    }

    private void registerCopyButton() {
        register(ToolBarSlots.DEFAULT_GROUP, 300, () -> new ButtonProvider(SharedIcons.COPY, "Copy",
                area -> area.selectedTextProperty().isNull(), area -> area.onCommand(TerminalCommand.COPY)));
    }

    private void registerPasteButton() {
        register(ToolBarSlots.DEFAULT_GROUP, 400, () -> new ButtonProvider(SharedIcons.PASTE, "Paste",
                null, area -> area.onCommand(TerminalCommand.PASTE)));
    }

    private void registerSelectAllButton() {
        register(ToolBarSlots.DEFAULT_GROUP, 500, () -> new ButtonProvider(SharedIcons.SELECT_ALL, "Select All",
                null, area -> area.onCommand(TerminalCommand.SELECT_ALL)));
    }

    private void registerFindButton() {
        register(ToolBarSlots.DEFAULT_GROUP, 600, () -> new ButtonProvider(SharedIcons.FIND, "Find",
                null, TerminalAreaPort::onShowFind));
    }

    private void registerPageUpButton() {
        register(ToolBarSlots.DEFAULT_GROUP, 700, () -> new ButtonProvider(TerminalIcons.PAGE_UP, "Page Up",
                null, area -> area.onCommand(TerminalCommand.PAGE_UP)));
    }

    private void registerPageDownButton() {
        register(ToolBarSlots.DEFAULT_GROUP, 800, () -> new ButtonProvider(TerminalIcons.PAGE_DOWN, "Page Down",
                null, area -> area.onCommand(TerminalCommand.PAGE_DOWN)));
    }

    private void registerLineUpButton() {
        register(ToolBarSlots.DEFAULT_GROUP, 900, () -> new ButtonProvider(TerminalIcons.LINE_UP, "Line Up",
                null, area -> area.onCommand(TerminalCommand.LINE_UP)));
    }

    private void registerLineDownButton() {
        register(ToolBarSlots.DEFAULT_GROUP, 1000, () -> new ButtonProvider(TerminalIcons.LINE_DOWN, "Line Down",
                null, area -> area.onCommand(TerminalCommand.LINE_DOWN)));
    }

    /**
     * Pushes the palette selector to the right edge. A group of the tool bar holds controls only, so the spacer is an
     * empty label that takes all the free width.
     */
    private void registerSpacer() {
        register(ToolBarSlots.DEFAULT_GROUP, 10000, () -> {
            var spacer = new Label();
            spacer.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(spacer, Priority.ALWAYS);
            return new SimpleControlProvider<>(spacer);
        });
    }

    private void registerPaletteLabel() {
        register(ToolBarSlots.DEFAULT_GROUP, 10050, () -> new SimpleControlProvider<>(new Label("Palette")));
    }

    private void registerPaletteComboBox() {
        register(ToolBarSlots.DEFAULT_GROUP, 10100, () -> new SimpleControlProvider<ToolBarView,
                ComboBox<TerminalPaletteType>>() {

            private @Nullable ChangeListener<TerminalPaletteType> paletteTypeListener;

            @Override
            public void initialize(ToolBarView view) {
                super.initialize(view);
                var area = view.getComposer().getAreaPort();
                var comboBox = new ComboBox<TerminalPaletteType>(
                        FXCollections.observableArrayList(TerminalPaletteType.values()));
                comboBox.setStyle("-fx-min-width:16em");
                Callback<ListView<TerminalPaletteType>, ListCell<TerminalPaletteType>> cellFactory = (p) -> {
                    return new ListCell<TerminalPaletteType>() {

                        @Override
                        protected void updateItem(TerminalPaletteType item, boolean empty) {
                            super.updateItem(item, empty);
                            if (item == null || empty) {
                                setGraphic(null);
                            } else {
                                setText(item.getName());
                            }
                        }
                    };
                };
                comboBox.setCellFactory(cellFactory);
                comboBox.setButtonCell(cellFactory.call(null));
                paletteTypeListener = (ov, oldV, newV) -> comboBox.setValue(newV);
                ValueUtils.callAndAddListener(area.paletteTypeProperty(), paletteTypeListener);
                comboBox.setOnAction(e -> area.setPaletteType(comboBox.getValue()));
                setControl(comboBox);
            }

            @Override
            public void deinitialize(ToolBarView view) {
                super.deinitialize(view);
                if (paletteTypeListener != null) {
                    view.getComposer().getAreaPort().paletteTypeProperty().removeListener(paletteTypeListener);
                }
            }
        });
    }
}
