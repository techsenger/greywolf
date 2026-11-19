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

import atlantafx.base.theme.Styles;
import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfControlRegistrar;
import com.techsenger.greywolf.system.navigator.area.NavigatorAreaPort;
import com.techsenger.greywolf.system.navigator.slots.ToolBarSlots;
import com.techsenger.greywolf.system.navigator.style.NavigatorIcons;
import com.techsenger.greywolf.system.navigator.toolbar.ToolBarView;
import com.techsenger.greywolf.storage.ui.style.StorageUiIcons;
import com.techsenger.shellfx.base.icon.FontIcon;
import com.techsenger.shellfx.core.registries.SimpleControlProvider;
import com.techsenger.shellfx.core.registries.SimpleGroupProvider;
import com.techsenger.shellfx.material.OverlayUtils;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.style.StyleClasses;
import com.techsenger.toolkit.fx.value.ValueUtils;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;

/**
 * Registers the controls of the Navigator tool bar - the tool bar itself, its groups and their buttons. Where
 * they sit is set by {@link ToolBarSlotRegistrar}.
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

        private final @Nullable Function<NavigatorAreaPort, ObservableValue<Boolean>> disabled;

        private final Consumer<NavigatorAreaPort> action;

        private final boolean mouseAction;

        ButtonProvider(FontIcon<?> icon, String tooltip,
                @Nullable Function<NavigatorAreaPort, ObservableValue<Boolean>> disabled,
                Consumer<NavigatorAreaPort> action, boolean mouseAction) {
            this.icon = icon;
            this.tooltip = tooltip;
            this.disabled = disabled;
            this.action = action;
            this.mouseAction = mouseAction;
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
            if (mouseAction) {
                button.setOnAction(e -> OverlayUtils.runMouseAction(button, e2 -> action.accept(area)));
            } else {
                button.setOnAction(e -> action.accept(area));
            }
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

    private static final int MIN_COLUMN_COUNT = 2;

    private static final int MAX_COLUMN_COUNT = 10;

    private static final List<Integer> COLUMN_COUNTS = IntStream.rangeClosed(MIN_COLUMN_COUNT, MAX_COLUMN_COUNT)
            .boxed().toList();

    public ToolBarControlRegistrar(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void register() {
        register(ToolBarSlots.TOOL_BAR, () -> new SimpleControlProvider<>(new ToolBar()));
        register(ToolBarSlots.NAVIGATION_GROUP, SimpleGroupProvider::new);
        register(ToolBarSlots.CLIPBOARD_GROUP, SimpleGroupProvider::new);
        register(ToolBarSlots.BASE_GROUP, SimpleGroupProvider::new);
        register(ToolBarSlots.PANELS_GROUP, SimpleGroupProvider::new);
        register(ToolBarSlots.OTHER_GROUP, SimpleGroupProvider::new);

        registerBackButton();
        registerForwardButton();
        registerUpButton();
        registerRefreshButton();

        registerCutButton();
        registerCopyButton();
        registerPasteButton();

        registerCreateFolderButton();
        registerCreateFileButton();
        registerRenameButton();
        registerDeleteButton();

        registerCopyFilesButton();
        registerMoveFilesButton();

        registerFilePropertiesButton();
        registerColumnCountIcon();
        registerColumnCountComboBox();
    }

    private void registerBackButton() {
        register(ToolBarSlots.NAVIGATION_GROUP, 100, () -> new ButtonProvider(NavigatorIcons.BACK,
                "Back", area -> area.backAvailableProperty().not(), NavigatorAreaPort::onBack, false));
    }

    private void registerForwardButton() {
        register(ToolBarSlots.NAVIGATION_GROUP, 200, () -> new ButtonProvider(NavigatorIcons.FORWARD,
                "Forward", area -> area.forwardAvailableProperty().not(), NavigatorAreaPort::onForward,
                false));
    }

    private void registerUpButton() {
        register(ToolBarSlots.NAVIGATION_GROUP, 300, () -> new ButtonProvider(NavigatorIcons.UP,
                "Up", area -> area.upAvailableProperty().not(), NavigatorAreaPort::onUp, false));
    }

    private void registerRefreshButton() {
        register(ToolBarSlots.NAVIGATION_GROUP, 400, () -> new ButtonProvider(NavigatorIcons.REFRESH,
                "Refresh", null, NavigatorAreaPort::onRefresh, false));
    }

    private void registerCutButton() {
        register(ToolBarSlots.CLIPBOARD_GROUP, 100, () -> new ButtonProvider(StorageUiIcons.CUT, "Cut",
                AreaConditions::noActionableFile, NavigatorAreaPort::onCutToClipboard, true));
    }

    private void registerCopyButton() {
        register(ToolBarSlots.CLIPBOARD_GROUP, 200, () -> new ButtonProvider(StorageUiIcons.COPY,
                "Copy to Clipboard", AreaConditions::noActionableFile,
                NavigatorAreaPort::onCopyToClipboard, true));
    }

    private void registerPasteButton() {
        register(ToolBarSlots.CLIPBOARD_GROUP, 300, () -> new ButtonProvider(StorageUiIcons.PASTE,
                "Paste from Clipboard", area -> AreaConditions.pasteDisabled(getGreyWolf().getClipboard()),
                NavigatorAreaPort::onPasteFromClipboard, true));
    }

    private void registerCreateFolderButton() {
        register(ToolBarSlots.BASE_GROUP, 100, () -> new ButtonProvider(StorageUiIcons.CREATE_FOLDER,
                "Create Folder", null, NavigatorAreaPort::onCreateFolder, true));
    }

    private void registerCreateFileButton() {
        register(ToolBarSlots.BASE_GROUP, 200, () -> new ButtonProvider(StorageUiIcons.CREATE_FILE,
                "Create File", null, NavigatorAreaPort::onCreateFile, true));
    }

    private void registerRenameButton() {
        register(ToolBarSlots.BASE_GROUP, 300, () -> new ButtonProvider(StorageUiIcons.RENAME, "Rename",
                AreaConditions::noActionableFile, NavigatorAreaPort::onRename, true));
    }

    private void registerDeleteButton() {
        register(ToolBarSlots.BASE_GROUP, 400, () -> new ButtonProvider(StorageUiIcons.DELETE, "Delete",
                AreaConditions::noActionableFile, NavigatorAreaPort::onDelete, true));
    }

    private void registerCopyFilesButton() {
        register(ToolBarSlots.PANELS_GROUP, 100, () -> new ButtonProvider(StorageUiIcons.COPY_TO,
                "Copy to Other Panel", AreaConditions::noActionableFile,
                NavigatorAreaPort::onCopyToPanel, true));
    }

    private void registerMoveFilesButton() {
        register(ToolBarSlots.PANELS_GROUP, 200, () -> new ButtonProvider(StorageUiIcons.MOVE,
                "Move to Other Panel", AreaConditions::noActionableFile,
                NavigatorAreaPort::onMoveToPanel, true));
    }

    private void registerFilePropertiesButton() {
        register(ToolBarSlots.OTHER_GROUP, 100, () -> new ButtonProvider(StorageUiIcons.PROPERTIES, "Properties",
                AreaConditions::noActionableFile, NavigatorAreaPort::onFileProperties, true));
    }

    private void registerColumnCountIcon() {
        register(ToolBarSlots.OTHER_GROUP, 200,
                () -> new SimpleControlProvider<>(new Label(null, new FontIconView(NavigatorIcons.COLUMNS))));
    }

    private void registerColumnCountComboBox() {
        register(ToolBarSlots.OTHER_GROUP, 300, () -> new SimpleControlProvider<ToolBarView, ComboBox<Integer>>() {

            private @Nullable ChangeListener<Number> columnCountListener;

            @Override
            public void initialize(ToolBarView view) {
                super.initialize(view);
                var area = view.getComposer().getAreaPort();
                var comboBox = new ComboBox<>(FXCollections.observableArrayList(COLUMN_COUNTS));
                comboBox.setTooltip(new Tooltip("Column Count"));
                comboBox.disableProperty().bind(area.viewModeProperty().map(mode -> !mode.hasColumnCount()));
                columnCountListener = (ov, oldV, newV) -> comboBox.setValue(newV.intValue());
                ValueUtils.callAndAddListener(area.columnCountProperty(), columnCountListener);
                comboBox.setOnAction(e -> {
                    var value = comboBox.getValue();
                    if (value != null) {
                        area.onColumnCountChanged(value);
                    }
                });
                setControl(comboBox);
            }

            @Override
            public void deinitialize(ToolBarView view) {
                super.deinitialize(view);
                if (columnCountListener != null) {
                    view.getComposer().getAreaPort().columnCountProperty().removeListener(columnCountListener);
                }
                getControl().disableProperty().unbind();
            }
        });
    }
}
