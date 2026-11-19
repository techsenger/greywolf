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
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.shellfx.core.popup.PopupPort;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;

/**
 * Provides full access to the client API of the popup that lists folders.
 *
 * @author Pavel Castornii
 */
public interface FolderPopupPort extends PopupPort {

    /**
     * Returns the file the list currently has selected, or {@code null} if it has none.
     *
     * @return the current file
     */
    @Nullable GreyWolfFile getFile();

    ReadOnlyObjectProperty<@Nullable GreyWolfFile> fileProperty();

    /**
     * Returns whether the user has submitted the current file with a click or Enter. It becomes {@code true} once,
     * as a popup serves a single pick, and the popup does not close itself after that.
     *
     * @return {@code true} if the current file has been submitted
     */
    boolean isSubmitted();

    ReadOnlyBooleanProperty submittedProperty();
}
