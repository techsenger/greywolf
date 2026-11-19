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

package com.techsenger.greywolf.system.navigator.style;

import com.techsenger.shellfx.base.icon.FontIcon;
import com.techsenger.shellfx.icons.Fonts;
import com.techsenger.shellfx.material.icon.PlainFontIcon;

/**
 *
 * @author Pavel Castornii
 */
public final class NavigatorIcons {

    public static final FontIcon<?> NAVIGATOR = new PlainFontIcon(0xF018C);

    public static final FontIcon<?> BACK = new PlainFontIcon(0xF004D);

    public static final FontIcon<?> FORWARD = new PlainFontIcon(0xF0054);

    public static final FontIcon<?> UP = new PlainFontIcon(0xF005D);

    public static final FontIcon<?> REFRESH = new PlainFontIcon(0xF0450);

    public static final FontIcon<?> MARKED_COUNT = new PlainFontIcon(0xF0135);

    public static final FontIcon<?> FOLDER = new PlainFontIcon(0xF024B);

    public static final FontIcon<?> FILE = new PlainFontIcon(0xF0224);

    public static final FontIcon<?> COLUMNS = new PlainFontIcon(0xF1487);

    public static final FontIcon<?> HOME = new PlainFontIcon(0xF02DC);

    public static final FontIcon<?> DESKTOP =
            new PlainFontIcon(0xE00A, Fonts.TECHSENGER_ICONS_MDI.getFamily());

    public static final FontIcon<?> DOWNLOADS = new PlainFontIcon(0xF01DA);

    public static final FontIcon<?> DOCUMENTS = new PlainFontIcon(0xF09AA);

    public static final FontIcon<?> MUSIC = new PlainFontIcon(0xF075A);

    public static final FontIcon<?> PICTURES = new PlainFontIcon(0xF0100);

    public static final FontIcon<?> VIDEOS = new PlainFontIcon(0xF0567);

    private NavigatorIcons() {
        // empty
    }
}
