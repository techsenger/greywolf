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

package com.techsenger.greywolf.storage.ui.properties;

import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.shellfx.core.dialog.DialogParams;
import com.techsenger.shellfx.core.settings.ShellSettings;
import com.techsenger.shellfx.core.window.WindowType;
import java.util.Objects;

/**
 * This dialog is always displayed as a nested window, since it is opened from within a file tab and never
 * needs to float independently of the main application window.
 *
 * @author Pavel Castornii
 */
public class FilePropertiesDialogParams extends DialogParams {

    private final GreyWolfFile file;

    public FilePropertiesDialogParams(FilePropertiesDialogConfig config, ShellSettings settings,
            GreyWolfFile file) {
        super(config, WindowType.NESTED, settings);
        this.file = file;
    }

    public GreyWolfFile getFile() {
        return file;
    }

    @Override
    public FilePropertiesDialogConfig getConfig() {
        return (FilePropertiesDialogConfig) super.getConfig();
    }

    @Override
    public void validate() {
        super.validate();
        Objects.requireNonNull(getConfig());
        Objects.requireNonNull(file, "File can't be null");
    }
}
