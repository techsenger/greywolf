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

import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.ui.style.GreyWolfFileStyleResolver;
import com.techsenger.shellfx.material.theme.Theme;
import com.techsenger.shellfx.storage.ui.style.FileDecorationType;
import com.techsenger.shellfx.storage.ui.style.IconTextStyle;

/**
 * Styles the files shown by the navigator: going up is an action, not a file, so the parent entry is shown with
 * plain text and no colors, and any other file is styled the way GreyWolf styles it.
 *
 * @author Pavel Castornii
 */
public final class NavigatorStyleResolver extends GreyWolfFileStyleResolver {

    @Override
    public IconTextStyle resolve(GreyWolfFile file, FileDecorationType type, Theme theme) {
        return file.isParentEntry() ? new IconTextStyle(null, null) : super.resolve(file, type, theme);
    }
}
