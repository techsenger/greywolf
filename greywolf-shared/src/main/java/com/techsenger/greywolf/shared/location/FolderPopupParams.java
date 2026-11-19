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

package com.techsenger.greywolf.shared.location;

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.shellfx.core.popup.PopupParams;
import com.techsenger.shellfx.core.settings.ShellSettings;
import com.techsenger.shellfx.core.window.WindowType;
import java.util.List;

/**
 * The parameters of the popup that lists folders.
 *
 * @author Pavel Castornii
 */
public class FolderPopupParams extends PopupParams {

    private final List<GreyWolfFile> files;

    private final @Nullable GreyWolfFile initialFile;

    private final boolean showStorageIcon;

    /**
     * Creates the parameters.
     *
     * @param settings the settings of the shell
     * @param files the files to list
     * @param initialFile the file selected when the popup opens, or {@code null} for none
     * @param showStorageIcon whether a storage root is listed with its icon
     */
    public FolderPopupParams(ShellSettings settings, List<GreyWolfFile> files, @Nullable GreyWolfFile initialFile,
            boolean showStorageIcon) {
        super(null, WindowType.TOP_LEVEL, settings);
        this.files = List.copyOf(files);
        this.initialFile = initialFile;
        this.showStorageIcon = showStorageIcon;
    }

    public List<GreyWolfFile> getFiles() {
        return files;
    }

    public @Nullable GreyWolfFile getInitialFile() {
        return initialFile;
    }

    public boolean isShowStorageIcon() {
        return showStorageIcon;
    }
}
