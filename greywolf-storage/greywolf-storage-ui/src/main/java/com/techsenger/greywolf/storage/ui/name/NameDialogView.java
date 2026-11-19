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

import com.techsenger.shellfx.core.registries.StylesheetRegistry;
import com.techsenger.shellfx.dialogs.text.TextsDialogView;

/**
 *
 * @author Pavel Castornii
 */
public final class NameDialogView extends TextsDialogView<NameDialogViewModel<?>> {

    public NameDialogView(NameDialogViewModel<?> viewModel, StylesheetRegistry stylesheetRegistry) {
        super(viewModel, stylesheetRegistry);
    }

    @Override
    protected void build() {
        super.build();
        getTextLabel().setText(switch (getViewModel().getType()) {
            case CREATE_FILE -> "File Name";
            case CREATE_FOLDER -> "Folder Name";
            case RENAME -> "New Name";
        });
    }
}
