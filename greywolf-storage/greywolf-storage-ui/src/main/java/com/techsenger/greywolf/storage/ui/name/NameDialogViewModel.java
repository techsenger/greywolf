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

package com.techsenger.greywolf.storage.ui.name;

import com.techsenger.greywolf.storage.ui.style.StorageUiIcons;
import com.techsenger.shellfx.core.config.ConfigUtils;
import com.techsenger.shellfx.core.window.WindowComposer;
import com.techsenger.shellfx.dialogs.text.TextDialogButtons;
import com.techsenger.shellfx.dialogs.text.TextsDialogViewModel;
import com.techsenger.shellfx.material.button.ResultButtonName;
import java.util.ArrayList;
import javafx.collections.ListChangeListener;

/**
 * Collects the name of a file or a folder and remembers it in config; the actual storage I/O is done by whoever
 * opens this dialog.
 *
 * @author Pavel Castornii
 */
public final class NameDialogViewModel<C extends WindowComposer> extends TextsDialogViewModel<C> {

    private final NameDialogType type;

    public NameDialogViewModel(NameDialogParams params) {
        super(params);
        this.type = params.getType();
    }

    public NameDialogType getType() {
        return type;
    }

    @Override
    public void onResult(ResultButtonName buttonName) {
        if (buttonName == TextDialogButtons.OK) {
            var trimmed = getText().trim();
            if (!trimmed.isEmpty()) {
                var updated = new ArrayList<>(getTexts());
                ConfigUtils.addFirst(updated, trimmed);
                ConfigUtils.limit(updated);
                getTexts().setAll(updated);
            }
        }
        super.onResult(buttonName);
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setTitle(switch (type) {
            case CREATE_FILE -> "Create File";
            case CREATE_FOLDER -> "Create Folder";
            case RENAME -> "Rename";
        });
        setIcon(switch (type) {
            case CREATE_FILE -> StorageUiIcons.CREATE_FILE;
            case CREATE_FOLDER -> StorageUiIcons.CREATE_FOLDER;
            case RENAME -> StorageUiIcons.RENAME;
        });
        setResizable(false);
    }

    @Override
    protected void loadStateFromConfig() {
        super.loadStateFromConfig();
        var names = getConfig().getNames();
        getTexts().setAll(names);
        setText(names.isEmpty() ? "" : names.get(0));
    }

    @Override
    protected void observeStateForConfig() {
        super.observeStateForConfig();
        var config = getConfig();
        getTexts().addListener((ListChangeListener<String>) change -> {
            config.setNames(new ArrayList<>(getTexts()));
            config.notifyListeners();
        });
    }

    @Override
    protected NameDialogConfig getConfig() {
        return (NameDialogConfig) super.getConfig();
    }
}
