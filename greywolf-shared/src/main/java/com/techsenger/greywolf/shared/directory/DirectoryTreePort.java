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

package com.techsenger.greywolf.shared.directory;

import com.techsenger.annotations.Nullable;
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.base.GreyWolfStorage;
import com.techsenger.shellfx.core.area.AreaPort;
import com.techsenger.shellfx.material.RequestSetter;
import java.util.List;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

/**
 * Provides full access to the component's client API.
 *
 * @author Pavel Castornii
 */
public interface DirectoryTreePort extends AreaPort {

    @Nullable GreyWolfFile getDirectory();

    @RequestSetter
    void setDirectory(GreyWolfFile directory);

    ReadOnlyObjectProperty<@Nullable GreyWolfFile> directoryProperty();

    @Unmodifiable ObservableList<GreyWolfStorage> getStorages();

    @Unmodifiable ObservableMap<GreyWolfFile, @Unmodifiable List<GreyWolfFile>> getChildrenByDirectory();

    /**
     * Returns the folder of the node the context menu was last requested for, so the handlers of the items of that
     * menu know what to act on.
     */
    @Nullable GreyWolfFile getFile();

    ReadOnlyObjectProperty<@Nullable GreyWolfFile> fileProperty();

    /**
     * Reloads the subfolders of the folder shown in the tree.
     *
     * @param directory the folder to refresh
     */
    void onDirectoryRefresh(GreyWolfFile directory);
}
