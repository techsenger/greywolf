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

import com.techsenger.shellfx.core.settings.ShellSettings;
import com.techsenger.shellfx.core.window.WindowType;
import com.techsenger.shellfx.dialogs.text.TextsDialogParams;

/**
 * This dialog is always displayed as a nested window.
 *
 * @author Pavel Castornii
 */
public class NameDialogParams extends TextsDialogParams {

    private final NameDialogType type;

    public NameDialogParams(NameDialogConfig config, ShellSettings settings, NameDialogType type) {
        super(config, WindowType.NESTED, settings);
        this.type = type;
    }

    public NameDialogType getType() {
        return type;
    }

    @Override
    public NameDialogConfig getConfig() {
        return (NameDialogConfig) super.getConfig();
    }
}
