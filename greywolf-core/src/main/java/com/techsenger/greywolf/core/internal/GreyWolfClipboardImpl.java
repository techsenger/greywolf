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

package com.techsenger.greywolf.core.internal;

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.core.clipboard.ClipboardContent;
import com.techsenger.greywolf.core.clipboard.GreyWolfClipboard;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

/**
 *
 * @author Pavel Castornii
 */
public final class GreyWolfClipboardImpl implements GreyWolfClipboard {

    private final ReadOnlyObjectWrapper<@Nullable ClipboardContent> content = new ReadOnlyObjectWrapper<>();

    @Override
    public @Nullable ClipboardContent getContent() {
        return content.get();
    }

    @Override
    public void setContent(@Nullable ClipboardContent content) {
        this.content.set(content);
    }

    @Override
    public ObjectProperty<@Nullable ClipboardContent> contentProperty() {
        return content;
    }
}
