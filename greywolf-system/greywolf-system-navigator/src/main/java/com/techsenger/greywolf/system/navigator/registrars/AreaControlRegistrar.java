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
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfControlRegistrar;
import com.techsenger.greywolf.system.navigator.actions.NavigatorActions.Name;
import com.techsenger.greywolf.system.navigator.area.ContextTarget;
import com.techsenger.greywolf.system.navigator.area.NavigatorAreaPort;
import com.techsenger.greywolf.system.navigator.area.NavigatorAreaView;
import com.techsenger.greywolf.system.navigator.slots.NavigatorAreaSlots;
import com.techsenger.greywolf.storage.ui.slots.FileViewSlots;
import com.techsenger.greywolf.storage.ui.style.StorageUiIcons;
import com.techsenger.shellfx.core.registries.ActionRegistry;
import com.techsenger.shellfx.core.registries.SimpleControlProvider;
import com.techsenger.shellfx.core.registries.SimpleGroupProvider;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.menu.DynamicContextMenu;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.MenuItem;

/**
 * Registers the controls of the context menu of the navigator area. The menu is built once with the area, and its
 * items keep their visibility actual themselves, depending on the entry the menu was requested for. Where the groups
 * sit is set by {@link AreaSlotRegistrar}.
 *
 * @author Pavel Castornii
 */
public class AreaControlRegistrar extends AbstractGreyWolfControlRegistrar {

    private static final class AreaMenuProvider
            extends SimpleControlProvider<NavigatorAreaView, DynamicContextMenu> {

        private @Nullable ObservableValue<Boolean> notParentEntry;

        AreaMenuProvider() {
            super(new DynamicContextMenu());
        }

        @Override
        public void initialize(NavigatorAreaView view) {
            super.initialize(view);
            var area = view.getViewModel();
            var condition = Bindings.createBooleanBinding(() -> {
                var file = area.getFile();
                return !(area.getContextTarget() == ContextTarget.FILE && file != null && file.isParentEntry());
            }, area.contextTargetProperty(), area.fileProperty());
            notParentEntry = condition;
            getControl().addVisibleCondition(condition);
        }

        @Override
        public void deinitialize(NavigatorAreaView view) {
            super.deinitialize(view);
            if (notParentEntry != null) {
                getControl().removeVisibleCondition(notParentEntry);
                notParentEntry = null;
            }
        }
    }

    private static ObservableValue<Boolean> onFile(NavigatorAreaPort area) {
        return area.contextTargetProperty().map(target -> target == ContextTarget.FILE);
    }

    private static ObservableValue<Boolean> onFolder(NavigatorAreaPort area) {
        return Bindings.createBooleanBinding(() -> {
            var file = area.getFile();
            return area.getContextTarget() == ContextTarget.FILE && file != null
                    && (file.isDirectory() || file.isDirectoryLink());
        }, area.contextTargetProperty(), area.fileProperty());
    }

    private static ObservableValue<Boolean> onRunnable(NavigatorAreaPort area) {
        return Bindings.createBooleanBinding(() -> {
            var file = area.getFile();
            return area.getContextTarget() == ContextTarget.FILE && file != null && file.isRunnable();
        }, area.contextTargetProperty(), area.fileProperty());
    }

    private static ObservableValue<Boolean> onEmptyPlace(NavigatorAreaPort area) {
        return area.contextTargetProperty().map(target -> target == ContextTarget.SPACE);
    }

    private final ActionRegistry actionRegistry;

    public AreaControlRegistrar(GreyWolf greyWolf) {
        super(greyWolf);
        this.actionRegistry = greyWolf.getActionRegistry();
    }

    @Override
    public void register() {
        register(NavigatorAreaSlots.AreaMenu.MENU, AreaMenuProvider::new);
        register(FileViewSlots.RUN_GROUP, SimpleGroupProvider::new);
        register(FileViewSlots.OPEN_GROUP, SimpleGroupProvider::new);
        register(NavigatorAreaSlots.AreaMenu.CREATE_GROUP, SimpleGroupProvider::new);
        register(NavigatorAreaSlots.AreaMenu.CLIPBOARD_GROUP, SimpleGroupProvider::new);
        register(NavigatorAreaSlots.AreaMenu.OTHER_PANEL_GROUP, SimpleGroupProvider::new);
        register(NavigatorAreaSlots.AreaMenu.MODIFY_GROUP, SimpleGroupProvider::new);
        register(NavigatorAreaSlots.AreaMenu.MARK_GROUP, SimpleGroupProvider::new);
        register(NavigatorAreaSlots.AreaMenu.PROPERTIES_GROUP, SimpleGroupProvider::new);

        registerRunItems();
        registerOpenItems();
        registerCreateItems();
        registerClipboardItems();
        registerSendItems();
        registerModifyItems();
        registerMarkItems();
        registerPropertiesItems();
    }

    private void registerRunItems() {
        register(FileViewSlots.RUN_GROUP, 100, NavigatorAreaView.class, () -> new ActionControlProvider<>(
                new MenuItem("_Run"), Name.RUN, actionRegistry) {

            @Override
            public void initialize(NavigatorAreaView view) {
                super.initialize(view);
                var area = view.getViewModel();
                getControl().visibleProperty().bind(onRunnable(area));
                getControl().setOnAction(new RunItemHandler(getGreyWolf(), () -> area));
            }

            @Override
            public void deinitialize(NavigatorAreaView view) {
                super.deinitialize(view);
                getControl().visibleProperty().unbind();
            }
        });
    }

    private void registerOpenItems() {
        register(FileViewSlots.OPEN_GROUP, 100, NavigatorAreaView.class, () -> new ActionControlProvider<>(
                new MenuItem("_Open"), Name.OPEN, actionRegistry) {

            @Override
            public void initialize(NavigatorAreaView view) {
                super.initialize(view);
                var area = view.getViewModel();
                getControl().visibleProperty().bind(onFolder(area));
                getControl().setOnAction(new OpenItemHandler(getGreyWolf(), () -> area));
            }

            @Override
            public void deinitialize(NavigatorAreaView view) {
                super.deinitialize(view);
                getControl().visibleProperty().unbind();
            }
        });

        register(FileViewSlots.OPEN_GROUP, 200, NavigatorAreaView.class, () -> new ActionControlProvider<>(
                new MenuItem("Open in New _Tab"), Name.OPEN_IN_NEW_TAB, actionRegistry) {

            @Override
            public void initialize(NavigatorAreaView view) {
                super.initialize(view);
                var area = view.getViewModel();
                getControl().visibleProperty().bind(onFolder(area));
                getControl().setOnAction(new OpenInNewTabItemHandler(
                        getGreyWolf(), () -> area, NavigatorAreaPort::getFile));
            }

            @Override
            public void deinitialize(NavigatorAreaView view) {
                super.deinitialize(view);
                getControl().visibleProperty().unbind();
            }
        });

        register(FileViewSlots.OPEN_GROUP, 300, NavigatorAreaView.class, () -> new ActionControlProvider<>(
                new MenuItem("Open in _System"), Name.OPEN_IN_SYSTEM, actionRegistry) {

            @Override
            public void initialize(NavigatorAreaView view) {
                super.initialize(view);
                var area = view.getViewModel();
                getControl().visibleProperty().bind(onFile(area));
                getControl().setOnAction(new OpenInSystemItemHandler(getGreyWolf(), () -> area));
            }

            @Override
            public void deinitialize(NavigatorAreaView view) {
                super.deinitialize(view);
                getControl().visibleProperty().unbind();
            }
        });
    }

    private void registerClipboardItems() {
        register(NavigatorAreaSlots.AreaMenu.CLIPBOARD_GROUP, 100, () -> new ActionControlProvider<>(
                new MenuItem("_Copy", new FontIconView(StorageUiIcons.COPY)),
                Name.COPY_TO_CLIPBOARD, actionRegistry) {

            @Override
            public void initialize(NavigatorAreaView view) {
                super.initialize(view);
                var area = view.getViewModel();
                getControl().visibleProperty().bind(onFile(area));
                getControl().setOnAction(new ClipboardCopyItemHandler(getGreyWolf(), () -> area));
            }

            @Override
            public void deinitialize(NavigatorAreaView view) {
                super.deinitialize(view);
                getControl().visibleProperty().unbind();
            }
        });

        register(NavigatorAreaSlots.AreaMenu.CLIPBOARD_GROUP, 200, () -> new ActionControlProvider<>(
                new MenuItem("Cu_t", new FontIconView(StorageUiIcons.CUT)),
                Name.CUT_TO_CLIPBOARD, actionRegistry) {

            @Override
            public void initialize(NavigatorAreaView view) {
                super.initialize(view);
                var area = view.getViewModel();
                getControl().visibleProperty().bind(onFile(area));
                getControl().setOnAction(new ClipboardCutItemHandler(getGreyWolf(), () -> area));
            }

            @Override
            public void deinitialize(NavigatorAreaView view) {
                super.deinitialize(view);
                getControl().visibleProperty().unbind();
            }
        });

        register(NavigatorAreaSlots.AreaMenu.CLIPBOARD_GROUP, 300, () -> new ActionControlProvider<>(
                new MenuItem("_Paste", new FontIconView(StorageUiIcons.PASTE)),
                Name.PASTE_FROM_CLIPBOARD, actionRegistry) {

            @Override
            public void initialize(NavigatorAreaView view) {
                super.initialize(view);
                var area = view.getViewModel();
                getControl().visibleProperty().bind(onEmptyPlace(area));
                getControl().disableProperty().bind(AreaConditions.pasteDisabled(getGreyWolf().getClipboard()));
                getControl().setOnAction(new ClipboardPasteItemHandler(
                        getGreyWolf(), () -> area, NavigatorAreaPort::getDirectory));
            }

            @Override
            public void deinitialize(NavigatorAreaView view) {
                super.deinitialize(view);
                getControl().visibleProperty().unbind();
                getControl().disableProperty().unbind();
            }
        });

        register(NavigatorAreaSlots.AreaMenu.CLIPBOARD_GROUP, 400, () -> new ActionControlProvider<>(
                new MenuItem("Paste into _Folder", new FontIconView(StorageUiIcons.PASTE)),
                Name.PASTE_INTO_FOLDER, actionRegistry) {

            @Override
            public void initialize(NavigatorAreaView view) {
                super.initialize(view);
                var area = view.getViewModel();
                getControl().visibleProperty().bind(onFolder(area));
                getControl().disableProperty().bind(AreaConditions.pasteDisabled(getGreyWolf().getClipboard()));
                getControl().setOnAction(new ClipboardPasteItemHandler(
                        getGreyWolf(), () -> area, NavigatorAreaPort::getFile));
            }

            @Override
            public void deinitialize(NavigatorAreaView view) {
                super.deinitialize(view);
                getControl().visibleProperty().unbind();
                getControl().disableProperty().unbind();
            }
        });
    }

    private void registerCreateItems() {
        register(NavigatorAreaSlots.AreaMenu.CREATE_GROUP, 100, () -> new ActionControlProvider<>(
                new MenuItem("Create _Folder"), Name.CREATE_FOLDER, actionRegistry) {

            @Override
            public void initialize(NavigatorAreaView view) {
                super.initialize(view);
                var area = view.getViewModel();
                getControl().visibleProperty().bind(onEmptyPlace(area));
                getControl().setOnAction(new CreateFolderItemHandler(getGreyWolf(), () -> area));
            }

            @Override
            public void deinitialize(NavigatorAreaView view) {
                super.deinitialize(view);
                getControl().visibleProperty().unbind();
            }
        });

        register(NavigatorAreaSlots.AreaMenu.CREATE_GROUP, 200, () -> new ActionControlProvider<>(
                new MenuItem("Create F_ile"), Name.CREATE_FILE, actionRegistry) {

            @Override
            public void initialize(NavigatorAreaView view) {
                super.initialize(view);
                var area = view.getViewModel();
                getControl().visibleProperty().bind(onEmptyPlace(area));
                getControl().setOnAction(new CreateFileItemHandler(getGreyWolf(), () -> area));
            }

            @Override
            public void deinitialize(NavigatorAreaView view) {
                super.deinitialize(view);
                getControl().visibleProperty().unbind();
            }
        });
    }

    private void registerSendItems() {
        register(NavigatorAreaSlots.AreaMenu.OTHER_PANEL_GROUP, 100, () -> new ActionControlProvider<>(
                new MenuItem("Co_py to Other Panel"), Name.COPY, actionRegistry) {

            @Override
            public void initialize(NavigatorAreaView view) {
                super.initialize(view);
                var area = view.getViewModel();
                getControl().visibleProperty().bind(onFile(area));
                getControl().setOnAction(new CopyItemHandler(getGreyWolf(), () -> area));
            }

            @Override
            public void deinitialize(NavigatorAreaView view) {
                super.deinitialize(view);
                getControl().visibleProperty().unbind();
            }
        });

        register(NavigatorAreaSlots.AreaMenu.OTHER_PANEL_GROUP, 200, () -> new ActionControlProvider<>(
                new MenuItem("_Move to Other Panel"), Name.MOVE, actionRegistry) {

            @Override
            public void initialize(NavigatorAreaView view) {
                super.initialize(view);
                var area = view.getViewModel();
                getControl().visibleProperty().bind(onFile(area));
                getControl().setOnAction(new MoveItemHandler(getGreyWolf(), () -> area));
            }

            @Override
            public void deinitialize(NavigatorAreaView view) {
                super.deinitialize(view);
                getControl().visibleProperty().unbind();
            }
        });

        register(NavigatorAreaSlots.AreaMenu.OTHER_PANEL_GROUP, 300, () -> new ActionControlProvider<>(
                new MenuItem("Send _to Other Panel"), Name.SEND_TO, actionRegistry) {

            @Override
            public void initialize(NavigatorAreaView view) {
                super.initialize(view);
                var area = view.getViewModel();
                getControl().visibleProperty().bind(onFile(area));
                getControl().setOnAction(new SendItemHandler(
                        getGreyWolf(), () -> area, NavigatorAreaPort::getFile));
            }

            @Override
            public void deinitialize(NavigatorAreaView view) {
                super.deinitialize(view);
                getControl().visibleProperty().unbind();
            }
        });

        register(NavigatorAreaSlots.AreaMenu.OTHER_PANEL_GROUP, 400, () -> new ActionControlProvider<>(
                new MenuItem("Send Current to _Other Panel"), Name.SEND_CURRENT, actionRegistry) {

            @Override
            public void initialize(NavigatorAreaView view) {
                super.initialize(view);
                var area = view.getViewModel();
                // it is here only to carry the hotkey of the action, so it is never shown
                getControl().setVisible(false);
                getControl().setOnAction(new SendItemHandler(
                        getGreyWolf(), () -> area, NavigatorAreaPort::getDirectory));
            }
        });
    }

    private void registerModifyItems() {
        register(NavigatorAreaSlots.AreaMenu.MODIFY_GROUP, 100, () -> new ActionControlProvider<>(
                new MenuItem("_Rename", new FontIconView(StorageUiIcons.RENAME)),
                Name.RENAME, actionRegistry) {

            @Override
            public void initialize(NavigatorAreaView view) {
                super.initialize(view);
                var area = view.getViewModel();
                getControl().visibleProperty().bind(onFile(area));
                getControl().setOnAction(new RenameItemHandler(getGreyWolf(), () -> area));
            }

            @Override
            public void deinitialize(NavigatorAreaView view) {
                super.deinitialize(view);
                getControl().visibleProperty().unbind();
            }
        });

        register(NavigatorAreaSlots.AreaMenu.MODIFY_GROUP, 200, () -> new ActionControlProvider<>(
                new MenuItem("_Delete", new FontIconView(StorageUiIcons.DELETE)),
                Name.DELETE, actionRegistry) {

            @Override
            public void initialize(NavigatorAreaView view) {
                super.initialize(view);
                var area = view.getViewModel();
                getControl().visibleProperty().bind(onFile(area));
                getControl().setOnAction(new DeleteItemHandler(getGreyWolf(), () -> area));
            }

            @Override
            public void deinitialize(NavigatorAreaView view) {
                super.deinitialize(view);
                getControl().visibleProperty().unbind();
            }
        });
    }

    private void registerMarkItems() {
        register(NavigatorAreaSlots.AreaMenu.MARK_GROUP, 100, () -> new ActionControlProvider<>(
                new MenuItem("Mark _All"), Name.MARK_ALL, actionRegistry) {

            @Override
            public void initialize(NavigatorAreaView view) {
                super.initialize(view);
                var area = view.getViewModel();
                getControl().setOnAction(new MarkItemHandler(getGreyWolf(), () -> area, true));
            }
        });

        register(NavigatorAreaSlots.AreaMenu.MARK_GROUP, 200, () -> new ActionControlProvider<>(
                new MenuItem("U_nmark All"), Name.UNMARK_ALL, actionRegistry) {

            @Override
            public void initialize(NavigatorAreaView view) {
                super.initialize(view);
                var area = view.getViewModel();
                getControl().setOnAction(new MarkItemHandler(getGreyWolf(), () -> area, false));
            }
        });

        register(NavigatorAreaSlots.AreaMenu.MARK_GROUP, 300, () -> new ActionControlProvider<>(
                new MenuItem("_Invert Marking"), Name.INVERT_MARKING, actionRegistry) {

            @Override
            public void initialize(NavigatorAreaView view) {
                super.initialize(view);
                var area = view.getViewModel();
                getControl().setOnAction(new InvertMarkItemHandler(getGreyWolf(), () -> area));
            }
        });
    }

    private void registerPropertiesItems() {
        register(NavigatorAreaSlots.AreaMenu.PROPERTIES_GROUP, 100, () -> new ActionControlProvider<>(
                new MenuItem("_Properties", new FontIconView(StorageUiIcons.PROPERTIES)),
                Name.PROPERTIES, actionRegistry) {

            @Override
            public void initialize(NavigatorAreaView view) {
                super.initialize(view);
                var area = view.getViewModel();
                getControl().visibleProperty().bind(onFile(area));
                getControl().setOnAction(new PropertiesItemHandler(getGreyWolf(), () -> area));
            }

            @Override
            public void deinitialize(NavigatorAreaView view) {
                super.deinitialize(view);
                getControl().visibleProperty().unbind();
            }
        });
    }
}
