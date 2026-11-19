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

package com.techsenger.greywolf.storage.base;

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.storage.base.DefaultStorageFile;
import com.techsenger.shellfx.storage.base.StorageFile;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 * @author Pavel Castornii
 */
public class DefaultGreyWolfFile extends DefaultStorageFile implements GreyWolfFile {

    /**
     * Casts the file to a writable one, so that its values can be set.
     *
     * @param file the file to write to
     * @return the same file
     * @throws IllegalArgumentException if the file is not a {@code DefaultGreyWolfFile}
     */
    public static DefaultGreyWolfFile cast(StorageFile file) {
        if (!(file instanceof DefaultGreyWolfFile defaultFile)) {
            throw new IllegalArgumentException("Only DefaultGreyWolfFile can be written to: " + file);
        }
        return defaultFile;
    }

    private final ReadOnlyObjectWrapper<@Nullable FileType> type = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyStringWrapper mimeType = new ReadOnlyStringWrapper();

    private boolean parentEntry;

    private boolean executable;

    private final BooleanProperty marked = new SimpleBooleanProperty(this, "marked");

    @Override
    public GreyWolfStorage getStorage() {
        return (GreyWolfStorage) super.getStorage();
    }

    @Override
    public @Nullable GreyWolfEntryLink getLink() {
        return (GreyWolfEntryLink) super.getLink();
    }

    @Override
    public @Nullable FileType getType() {
        return type.get();
    }

    @Override
    public ReadOnlyObjectProperty<@Nullable FileType> typeProperty() {
        return type.getReadOnlyProperty();
    }

    public void setType(@Nullable FileType type) {
        this.type.set(type);
    }

    @Override
    public @Nullable String getMimeType() {
        return mimeType.get();
    }

    @Override
    public ReadOnlyStringProperty mimeTypeProperty() {
        return mimeType.getReadOnlyProperty();
    }

    public void setMimeType(@Nullable String mimeType) {
        this.mimeType.set(mimeType);
    }

    @Override
    public boolean isParentEntry() {
        return parentEntry;
    }

    public void setParentEntry(boolean parentEntry) {
        this.parentEntry = parentEntry;
    }

    @Override
    public boolean isExecutable() {
        return executable;
    }

    public void setExecutable(boolean executable) {
        this.executable = executable;
    }

    @Override
    public boolean isMarked() {
        return marked.get();
    }

    @Override
    public void setMarked(boolean marked) {
        this.marked.set(marked);
    }

    @Override
    public BooleanProperty markedProperty() {
        return marked;
    }
}
