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

package com.techsenger.greywolf.storage.base.style;

import com.techsenger.shellfx.base.icon.FontIcon;
import com.techsenger.shellfx.icons.Fonts;
import com.techsenger.shellfx.material.icon.PlainFontIcon;

/**
 *
 * @author Pavel Castornii
 */
public final class BaseStorageIcons {

    public static final FontIcon<?> IMAGE_FILE = new PlainFontIcon(0xF0EB0);

    public static final FontIcon<?> VIDEO_FILE = new PlainFontIcon(0xF0E2C);

    public static final FontIcon<?> CODE_FILE = new PlainFontIcon(0xF102B);

    public static final FontIcon<?> TEXT_FILE = new PlainFontIcon(0xF09EE);

    public static final FontIcon<?> ARCHIVE_FILE =
            new PlainFontIcon(0xE000, Fonts.TECHSENGER_ICONS_MDI.getFamily());

    public static final FontIcon<?> AUDIO_FILE =
            new PlainFontIcon(0xE001, Fonts.TECHSENGER_ICONS_MDI.getFamily());

    public static final FontIcon<?> DOCUMENT_FILE =
            new PlainFontIcon(0xE002, Fonts.TECHSENGER_ICONS_MDI.getFamily());

    public static final FontIcon<?> FONT_FILE =
            new PlainFontIcon(0xE003, Fonts.TECHSENGER_ICONS_MDI.getFamily());

    public static final FontIcon<?> APPLICATION_FILE =
            new PlainFontIcon(0xE004, Fonts.TECHSENGER_ICONS_MDI.getFamily());

    public static final FontIcon<?> HOME_FOLDER = new PlainFontIcon(0xF10B5);

    public static final FontIcon<?> DOWNLOADS_FOLDER = new PlainFontIcon(0xF024D);

    public static final FontIcon<?> MUSIC_FOLDER = new PlainFontIcon(0xF1359);

    public static final FontIcon<?> PICTURES_FOLDER = new PlainFontIcon(0xF024F);

    public static final FontIcon<?> VIDEOS_FOLDER =
            new PlainFontIcon(0xE009, Fonts.TECHSENGER_ICONS_MDI.getFamily());

    public static final FontIcon<?> DOCUMENTS_FOLDER = new PlainFontIcon(0xF0C82);

    public static final FontIcon<?> DESKTOP_FOLDER =
            new PlainFontIcon(0xE00B, Fonts.TECHSENGER_ICONS_MDI.getFamily());

    private BaseStorageIcons() {
        // empty
    }
}
