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

package com.techsenger.greywolf.system.navigator.actions;

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.base.bundle.BundleName;
import com.techsenger.shellfx.base.bundle.TextKey;
import com.techsenger.shellfx.core.action.Action;
import com.techsenger.shellfx.core.action.ActionGroupName;
import com.techsenger.shellfx.core.action.ActionName;
import java.util.List;
import java.util.ResourceBundle;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 * The actions of the Navigator plugin: what a user can do with files and with the way they are shown. Every item
 * of a menu of the plugin stands for one action, so all the items of an action, in the main menu and in the context
 * menu, follow the same hotkey. The names of the actions and of their groups are also the keys of their texts.
 *
 * @author Pavel Castornii
 */
public final class NavigatorActions {

    /**
     * The name of the bundle with the texts of the actions and of their groups.
     */
    public enum Bundle implements BundleName {

        NAVIGATOR;

        @Override
        public @Nullable String getText() {
            return null;
        }
    }

    /**
     * The groups the actions are listed in.
     */
    @SuppressWarnings("ImmutableEnumChecker")
    public enum Group implements ActionGroupName {

        FILE("group.file"),

        MARK("group.mark"),

        CLIPBOARD("group.clipboard"),

        OPEN("group.open"),

        OTHER_PANEL("group.other.panel"),

        VIEW("group.view");

        private final TextKey textKey;

        Group(String key) {
            this.textKey = new TextKey(Bundle.NAVIGATOR, key);
        }

        @Override
        public TextKey getTextKey() {
            return textKey;
        }

        @Override
        public @Nullable String getText() {
            return null;
        }
    }

    /**
     * The names of the actions; each name is also the key of the text of its action.
     */
    @SuppressWarnings("ImmutableEnumChecker")
    public enum Name implements ActionName {

        NEW_NAVIGATOR("action.new.navigator"),

        CREATE_FOLDER("action.create.folder"),

        CREATE_FILE("action.create.file"),

        COPY("action.copy"),

        MOVE("action.move"),

        RENAME("action.rename"),

        DELETE("action.delete"),

        PROPERTIES("action.properties"),

        MARK("action.mark"),

        UNMARK("action.unmark"),

        INVERT_MARKING("action.invert.marking"),

        MARK_ALL("action.mark.all"),

        UNMARK_ALL("action.unmark.all"),

        COPY_TO_CLIPBOARD("action.copy.to.clipboard"),

        CUT_TO_CLIPBOARD("action.cut.to.clipboard"),

        PASTE_FROM_CLIPBOARD("action.paste.from.clipboard"),

        PASTE_INTO_FOLDER("action.paste.into.folder"),

        RUN("action.run"),

        OPEN("action.open"),

        OPEN_IN_NEW_TAB("action.open.in.new.tab"),

        OPEN_IN_SYSTEM("action.open.in.system"),

        SEND_TO("action.send.to"),

        SEND_CURRENT("action.send.current"),

        MODE_LIST("action.mode.list"),

        MODE_TILES("action.mode.tiles"),

        MODE_DETAILS("action.mode.details"),

        COLUMN_SIZE("action.column.size"),

        COLUMN_TYPE("action.column.type"),

        COLUMN_LAST_MODIFIED("action.column.last.modified"),

        LOCATION_TREE("action.location.tree"),

        LOCATION_PLACES("action.location.places"),

        LOCATION_TREE_AND_PLACES("action.location.tree.and.places");

        private final TextKey textKey;

        Name(String key) {
            this.textKey = new TextKey(Bundle.NAVIGATOR, key);
        }

        @Override
        public TextKey getTextKey() {
            return textKey;
        }

        @Override
        public @Nullable String getText() {
            return null;
        }
    }

    /**
     * Creates the actions of the plugin with their default hotkeys, one for every name.
     *
     * @return a new list, so every activation of the plugin starts from the defaults
     */
    public static List<Action> createActions() {
        return List.of(
                new Action(Name.NEW_NAVIGATOR, Group.FILE,
                        new KeyCodeCombination(KeyCode.B, KeyCombination.CONTROL_DOWN)),
                new Action(Name.CREATE_FOLDER, Group.FILE, new KeyCodeCombination(KeyCode.F7)),
                new Action(Name.CREATE_FILE, Group.FILE, new KeyCodeCombination(KeyCode.F4, KeyCombination.SHIFT_DOWN)),
                new Action(Name.COPY, Group.FILE, new KeyCodeCombination(KeyCode.F5)),
                new Action(Name.MOVE, Group.FILE, new KeyCodeCombination(KeyCode.F6)),
                new Action(Name.RENAME, Group.FILE, new KeyCodeCombination(KeyCode.F2)),
                new Action(Name.DELETE, Group.FILE, new KeyCodeCombination(KeyCode.F8)),
                new Action(Name.PROPERTIES, Group.FILE, null),
                new Action(Name.MARK, Group.MARK, new KeyCodeCombination(KeyCode.ADD)),
                new Action(Name.UNMARK, Group.MARK, new KeyCodeCombination(KeyCode.SUBTRACT)),
                new Action(Name.INVERT_MARKING, Group.MARK, new KeyCodeCombination(KeyCode.MULTIPLY)),
                new Action(Name.MARK_ALL, Group.MARK, new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN)),
                new Action(Name.UNMARK_ALL, Group.MARK, new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN)),
                new Action(Name.COPY_TO_CLIPBOARD, Group.CLIPBOARD, null),
                new Action(Name.CUT_TO_CLIPBOARD, Group.CLIPBOARD, null),
                new Action(Name.PASTE_FROM_CLIPBOARD, Group.CLIPBOARD, null),
                new Action(Name.PASTE_INTO_FOLDER, Group.CLIPBOARD, null),
                new Action(Name.RUN, Group.OPEN, null),
                new Action(Name.OPEN, Group.OPEN, null),
                new Action(Name.OPEN_IN_NEW_TAB, Group.OPEN, null),
                new Action(Name.OPEN_IN_SYSTEM, Group.OPEN, null),
                new Action(Name.SEND_TO, Group.OTHER_PANEL,
                        new KeyCodeCombination(KeyCode.K, KeyCombination.CONTROL_DOWN)),
                new Action(Name.SEND_CURRENT, Group.OTHER_PANEL,
                        new KeyCodeCombination(KeyCode.K, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN)),
                new Action(Name.MODE_LIST, Group.VIEW, null),
                new Action(Name.MODE_TILES, Group.VIEW, null),
                new Action(Name.MODE_DETAILS, Group.VIEW, null),
                new Action(Name.COLUMN_SIZE, Group.VIEW, null),
                new Action(Name.COLUMN_TYPE, Group.VIEW, null),
                new Action(Name.COLUMN_LAST_MODIFIED, Group.VIEW, null),
                new Action(Name.LOCATION_TREE, Group.VIEW, null),
                new Action(Name.LOCATION_PLACES, Group.VIEW, null),
                new Action(Name.LOCATION_TREE_AND_PLACES, Group.VIEW, null));
    }

    /**
     * Loads the bundle with the texts of the actions for the language of the application.
     *
     * @return the bundle, to be registered under {@link Bundle#NAVIGATOR}
     */
    public static ResourceBundle loadBundle() {
        return ResourceBundle.getBundle(NavigatorActions.class.getPackageName() + ".actions");
    }

    private NavigatorActions() {
        // empty
    }
}
