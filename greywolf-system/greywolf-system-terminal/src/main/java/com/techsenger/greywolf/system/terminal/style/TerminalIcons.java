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

package com.techsenger.greywolf.system.terminal.style;

import com.techsenger.shellfx.base.icon.FontIcon;
import com.techsenger.shellfx.material.icon.PlainFontIcon;

/**
 *
 * @author Pavel Castornii
 */
public final class TerminalIcons {

    public static final FontIcon<?> TERMINAL = new PlainFontIcon(0xF018D);

    public static final FontIcon<?> PAGE_UP = new PlainFontIcon(0xF013F);

    public static final FontIcon<?> PAGE_DOWN = new PlainFontIcon(0xF013C);

    public static final FontIcon<?> LINE_UP = new PlainFontIcon(0xF0143);

    public static final FontIcon<?> LINE_DOWN = new PlainFontIcon(0xF0140);

    private TerminalIcons() {
        // empty
    }
}
