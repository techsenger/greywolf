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

package com.techsenger.greywolf.system.navigator.location;

import com.techsenger.annotations.Nullable;
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.shellfx.core.area.AreaPort;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;

/**
 *
 * @author Pavel Castornii
 */
public interface PlacesAreaPort extends AreaPort {

    /**
     * Returns this area's current directory, even when it isn't one of the Personal/Storages entries below it.
     */
    @Nullable GreyWolfFile getDirectory();

    /**
     * Sets this area's current directory, highlighting it below if it matches a Personal/Storages entry.
     *
     * @param directory the directory to highlight.
     */
    void setDirectory(GreyWolfFile directory);

    ObjectProperty<@Nullable GreyWolfFile> directoryProperty();

    /**
     * Returns the entry in {@link #getPersonalPlaces()}/{@link #getStoragePlaces()} that
     * {@link #getDirectory()} currently matches, or {@code null} if it matches neither.
     */
    @Nullable Place getPlace();

    ReadOnlyObjectProperty<@Nullable Place> placeProperty();

    @Unmodifiable ObservableList<Place> getPersonalPlaces();

    @Unmodifiable ObservableList<Place> getStoragePlaces();
}
