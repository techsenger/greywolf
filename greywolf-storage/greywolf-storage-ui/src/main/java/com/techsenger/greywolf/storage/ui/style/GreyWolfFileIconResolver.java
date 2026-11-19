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

package com.techsenger.greywolf.storage.ui.style;

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.base.PersonalDirectories;
import com.techsenger.greywolf.storage.base.StorageCategory;
import com.techsenger.shellfx.base.icon.FontIcon;
import com.techsenger.shellfx.storage.base.FileEntryType;
import com.techsenger.shellfx.storage.ui.style.DefaultFileIconResolver;
import com.techsenger.shellfx.storage.ui.style.FileDecorationType;

/**
 * Gives the files the icons that are specific to GreyWolf: a regular file has the icon of its type, and a well-known
 * personal directory has its own icon. Any other file, a file that is still to be refined, and a recorded file whose
 * real file was not found get the default icon.
 *
 * @author Pavel Castornii
 */
public class GreyWolfFileIconResolver extends DefaultFileIconResolver<GreyWolfFile> {

    @Override
    public FontIcon<?> resolve(GreyWolfFile file, FileDecorationType type) {
        if (type == FileDecorationType.FINAL && !isError(file)) {
            var icon = resolveSpecific(file);
            if (icon != null) {
                return icon;
            }
        }
        return super.resolve(file, type);
    }

    @Override
    protected boolean isError(GreyWolfFile file) {
        // a file that is still in the recent storage is one whose storage was not found
        return super.isError(file) || file.getStorage().getCategory() == StorageCategory.RECENT;
    }

    private @Nullable FontIcon<?> resolveSpecific(GreyWolfFile file) {
        if (file.getEntryType() == FileEntryType.FILE) {
            var type = file.getType();
            return type != null ? type.getIcon() : null;
        }
        if (file.getEntryType() == FileEntryType.DIRECTORY) {
            var personalDirectory = PersonalDirectories.getDirectoriesByUri().get(file.getUri());
            return personalDirectory != null ? personalDirectory.icon() : null;
        }
        return null;
    }
}
