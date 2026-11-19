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

package com.techsenger.greywolf.shared.location;

import com.techsenger.annotations.Nullable;
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.shellfx.core.area.AreaPort;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;

/**
 * Provides full access to the component's client API.
 *
 * @author Pavel Castornii
 */
public interface LocationBarPort extends AreaPort {

    @Unmodifiable ObservableList<GreyWolfFile> getPathFiles();

    boolean isShowStorageIcon();

    /**
     * Shows or hides the icon on the root (storage) breadcrumb segment. Applies to the current breadcrumbs
     * immediately and to any popup opened afterward; a popup already open when this changes keeps showing
     * whatever it showed when it was opened.
     *
     * @param visible {@code true} to show the storage icon, {@code false} to hide it
     */
    void setShowStorageIcon(boolean visible);

    BooleanProperty showStorageIconProperty();

    boolean isEditMode();

    ReadOnlyBooleanProperty editModeProperty();

    @Nullable GreyWolfFile getDirectory();

    void setDirectory(GreyWolfFile directory);

    ObjectProperty<@Nullable GreyWolfFile> directoryProperty();

    /**
     * Returns the folder of the segment the context menu was last requested for, so the handlers of the items of that
     * menu know what to act on.
     */
    @Nullable GreyWolfFile getFile();

    ReadOnlyObjectProperty<@Nullable GreyWolfFile> fileProperty();
}
