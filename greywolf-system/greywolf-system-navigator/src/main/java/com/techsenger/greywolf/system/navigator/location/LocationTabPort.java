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
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.shellfx.core.tab.TabPort;
import javafx.beans.property.ObjectProperty;

/**
 * A single location tab that composes a directory tree and/or a flat Personal/Storages places list,
 * depending on {@link #getMode()} - one tab, one set of chrome (title, close button), rather than a separate
 * tab per combination.
 *
 * @author Pavel Castornii
 */
public interface LocationTabPort extends TabPort {

    LocationMode getMode();

    /**
     * Switches this tab to {@code mode}, composing/discarding its directory-tree and/or places halves as
     * needed and catching whichever remain (or are newly composed) up with this tab's current directory.
     *
     * @param mode the mode to switch to.
     */
    void setMode(LocationMode mode);

    ObjectProperty<LocationMode> modeProperty();

    /**
     * Returns this tab's current directory, kept in sync with both external pushes and the user's own picks
     * in this tab's composed directory-tree/places halves.
     */
    @Nullable GreyWolfFile getDirectory();

    /**
     * Sets this tab's current directory, pushing it to whichever of the directory-tree/places halves are
     * currently composed, for highlighting/expand-to-node.
     *
     * @param directory the area's current directory.
     */
    void setDirectory(GreyWolfFile directory);

    ObjectProperty<@Nullable GreyWolfFile> directoryProperty();
}
