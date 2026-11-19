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

import com.techsenger.shellfx.base.icon.FontIcon;
import com.techsenger.shellfx.material.icon.PlainFontIcon;

/**
 * The icons of the file operations: clipboard, copy, move, delete, rename, create and properties.
 *
 * @author Pavel Castornii
 */
public final class StorageUiIcons {

    public static final FontIcon<?> PROPERTIES = new PlainFontIcon(0xF107C);

    public static final FontIcon<?> CREATE_FILE = new PlainFontIcon(0xF0EED);

    public static final FontIcon<?> CREATE_FOLDER = new PlainFontIcon(0xF0257);

    public static final FontIcon<?> COPY_TO = new PlainFontIcon(0xF1032);

    public static final FontIcon<?> MOVE = new PlainFontIcon(0xF1031);

    public static final FontIcon<?> MATCH_CASE = new PlainFontIcon(0xF0B34);

    public static final FontIcon<?> REG_EXP = new PlainFontIcon(0xF0451);

    public static final FontIcon<?> RENAME = new PlainFontIcon(0xF1C18);

    public static final FontIcon<?> OPERATION_PROGRESS = new PlainFontIcon(0xF1217);

    public static final FontIcon<?> COPY = new PlainFontIcon(0xF018F);

    public static final FontIcon<?> CUT = new PlainFontIcon(0xF0190);

    public static final FontIcon<?> PASTE = new PlainFontIcon(0xF0192);

    public static final FontIcon<?> DELETE = new PlainFontIcon(0xF01B4);

    private StorageUiIcons() {
        // empty
    }
}
