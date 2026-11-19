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
import com.techsenger.greywolf.storage.ui.style.GreyWolfFileIconResolver;
import com.techsenger.shellfx.base.icon.FontIcon;
import com.techsenger.shellfx.storage.ui.style.FileDecorationType;

/**
 * Gives the files shown by the navigator their icons: going up is an action, not a folder to enter, so the parent
 * entry gets the icon of the Up button, and any other file gets the icon GreyWolf gives it.
 *
 * @author Pavel Castornii
 */
public final class NavigatorIconResolver extends GreyWolfFileIconResolver {

    @Override
    public FontIcon<?> resolve(GreyWolfFile file, FileDecorationType type) {
        return file.isParentEntry() ? NavigatorIcons.UP : super.resolve(file, type);
    }
}
