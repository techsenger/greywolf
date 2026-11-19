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

package com.techsenger.greywolf.storage.ui.transfer;

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

/**
 * Provides full access to the client API of the dialog of a transfer started from the source side.
 *
 * @author Pavel Castornii
 */
public interface SourceDialogPort extends TransferDialogPort {

    /**
     * The directory currently shown on the target side, the default target of the transfer.
     */
    GreyWolfFile getInitialTarget();

    String getTarget();

    void setTarget(String target);

    StringProperty targetProperty();

    /**
     * The path texts of the targets the user has transferred to.
     */
    ObservableList<String> getTargets();

    /**
     * The target the user confirmed: an existing directory to transfer into or, when a single source is renamed, a
     * virtual file with the new name in an existing directory.
     *
     * @return the target, or {@code null} until the dialog is confirmed
     */
    @Nullable GreyWolfFile getResultTarget();

    ReadOnlyObjectProperty<@Nullable GreyWolfFile> resultTargetProperty();
}
