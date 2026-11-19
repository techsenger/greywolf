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
import com.techsenger.shellfx.storage.base.StorageFile;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;

/**
 *
 * @author Pavel Castornii
 */
public interface GreyWolfFile extends StorageFile {

    @Override
    GreyWolfStorage getStorage();

    @Override
    @Nullable GreyWolfEntryLink getLink();

    /**
     * Returns the type of the content of this entry, or {@code null} if it is not a regular file or the type is not
     * resolved yet.
     */
    @Nullable FileType getType();

    ReadOnlyObjectProperty<@Nullable FileType> typeProperty();

    /**
     * Returns the MIME type of the content of this entry, or {@code null} if it is not a regular file or its content
     * is not resolved yet.
     */
    @Nullable String getMimeType();

    ReadOnlyStringProperty mimeTypeProperty();

    /**
     * Returns whether this entry represents the parent directory entry (e.g. "..") rather than a regular entry
     * from the current directory, letting callers distinguish it in a listing that mixes both.
     */
    boolean isParentEntry();

    /**
     * Returns whether this entry is a regular file that can be run, e.g. a program or a script. A directory is
     * never executable.
     */
    boolean isExecutable();

    /**
     * Returns whether this entry can be run by GreyWolf: an executable file of the file system of the machine.
     */
    default boolean isRunnable() {
        return isExecutable() && getStorage().getCategory() == StorageCategory.SYSTEM;
    }

    /**
     * Returns whether the user has marked this entry, e.g. to apply an operation to it.
     */
    boolean isMarked();

    void setMarked(boolean marked);

    BooleanProperty markedProperty();
}
