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

import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.base.DefaultGreyWolfFile;
import com.techsenger.greywolf.storage.base.StorageCategory;
import com.techsenger.shellfx.storage.base.StorageFile;
import com.techsenger.shellfx.storage.ui.style.DefaultFileStyleResolver;
import com.techsenger.shellfx.storage.ui.style.FileDecorationType;
import com.techsenger.shellfx.storage.ui.style.FileStyleSettings;

/**
 * Styles the files with the scheme of the current theme: a regular file by the style of its type, and over the style
 * of its kind the masks of an executable, a failed, a hidden and a marked file are laid in this order.
 *
 * @author Pavel Castornii
 */
public class GreyWolfFileStyleResolver extends DefaultFileStyleResolver<GreyWolfFile, GreyWolfFileStyleScheme> {

    /**
     * Creates a resolver that styles the files with the scheme of the theme they are shown in.
     */
    public GreyWolfFileStyleResolver() {
        super(GreyWolfFileStyleDefaults::getScheme);
    }

    @Override
    protected FileStyleSettings selectSettings(StorageFile file, GreyWolfFileStyleScheme scheme) {
        if (file instanceof GreyWolfFile greyWolfFile && greyWolfFile.isFile()) {
            var type = greyWolfFile.getType();
            if (type != null) {
                return switch (type) {
                    case ARCHIVE -> scheme.getArchiveFile();
                    case CODE -> scheme.getCodeFile();
                    case TEXT -> scheme.getTextFile();
                    case DOCUMENT -> scheme.getDocumentFile();
                    case IMAGE -> scheme.getImageFile();
                    case AUDIO -> scheme.getAudioFile();
                    case VIDEO -> scheme.getVideoFile();
                    case FONT -> scheme.getFontFile();
                    case APPLICATION -> scheme.getApplicationFile();
                    case UNKNOWN -> scheme.getFile();
                };
            }
        }
        return super.selectSettings(file, scheme);
    }

    @Override
    protected FileStyleSettings applyMasks(FileStyleSettings style, GreyWolfFile file, FileDecorationType type,
            GreyWolfFileStyleScheme scheme) {
        var result = style;
        if (DefaultGreyWolfFile.cast(getStyledFile(file)).isExecutable()) {
            result = overlay(result, scheme.getExecutable());
        }
        if (isRecentError(file, type)) {
            result = overlay(result, scheme.getUnavailable());
        }
        result = super.applyMasks(result, file, type, scheme);
        if (file.isMarked()) {
            result = overlay(result, scheme.getMarked());
        }
        return result;
    }

    @Override
    protected boolean isHidden(GreyWolfFile file) {
        // the mark must stay visible, so a marked file is not dimmed
        return super.isHidden(file) && !file.isMarked();
    }

    private boolean isRecentError(GreyWolfFile file, FileDecorationType type) {
        // a file that is still in the recent storage is one whose storage was not found
        return type == FileDecorationType.FINAL && file.getStorage().getCategory() == StorageCategory.RECENT;
    }
}
