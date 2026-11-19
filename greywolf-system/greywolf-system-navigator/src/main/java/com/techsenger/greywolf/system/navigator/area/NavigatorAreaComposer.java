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

package com.techsenger.greywolf.system.navigator.area;

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.core.GreyWolfSettings;
import com.techsenger.greywolf.system.navigator.mark.MarkDialogParams;
import com.techsenger.greywolf.system.navigator.mark.MarkDialogPort;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.ui.delete.DeleteOperation;
import com.techsenger.greywolf.storage.ui.name.NameDialogParams;
import com.techsenger.greywolf.storage.ui.properties.FilePropertiesDialogParams;
import com.techsenger.greywolf.storage.ui.transfer.CopyOperation;
import com.techsenger.greywolf.storage.ui.transfer.MoveOperation;
import com.techsenger.greywolf.storage.ui.transfer.TransferDialogParams;
import com.techsenger.patternfx.mvvm.ChildComposer;
import com.techsenger.shellfx.core.config.ConfigManager;
import com.techsenger.shellfx.core.dialog.DialogPort;
import com.techsenger.shellfx.dialogs.alert.AlertDialogParams;
import com.techsenger.shellfx.dialogs.alert.AlertDialogPort;
import com.techsenger.shellfx.dialogs.text.TextsDialogPort;

/**
 * The extra capabilities {@link NavigatorAreaView} exposes to {@link NavigatorAreaViewModel} beyond the
 * plain {@code AreaView.Composer} - creating/showing every dialog specific to browsing a directory.
 *
 * @author Pavel Castornii
 */
public interface NavigatorAreaComposer extends ChildComposer {

    /**
     * Creates and shows the mark-by-mask dialog described by {@code params}.
     *
     * @param params the dialog configuration.
     * @return the dialog's port, so the view model can react to Apply/OK/Cancel.
     */
    MarkDialogPort openMarkDialog(MarkDialogParams params);

    /**
     * Creates and shows the file properties dialog described by {@code params}.
     *
     * @param params the dialog configuration.
     * @return the port of the opened dialog.
     */
    DialogPort openFilePropertiesDialog(FilePropertiesDialogParams params);

    /**
     * Creates and shows the dialog that asks for the name of a file or a folder, described by {@code params}.
     *
     * @param params the dialog configuration.
     * @return the dialog's port, so the view model can validate the entered name, perform the storage I/O,
     *     and decide when to close it.
     */
    TextsDialogPort openNameDialog(NameDialogParams params);

    /**
     * Shows an alert dialog, used to report a create-file/create-folder failure without closing the dialog the
     * user is still typing into.
     *
     * @param params the dialog configuration.
     * @return the port of the shown dialog, for setting its message.
     */
    AlertDialogPort openAlertDialog(AlertDialogParams params);

    /**
     * Creates a copy run without starting it - the view model must feed it the other inputs, then call
     * {@code start} itself once ready.
     *
     * @param params the copy dialog's configuration.
     * @param targetDirectory the directory to copy into when the copy is started from the target side, or
     *     {@code null} when the dialog asks for it.
     * @return the not-yet-started operation.
     */
    CopyOperation createCopyOperation(TransferDialogParams params, @Nullable GreyWolfFile targetDirectory);

    /**
     * Creates a move run without starting it - the view model must feed it the other inputs, then call
     * {@code start} itself once ready.
     *
     * @param params the move dialog's configuration.
     * @param targetDirectory the directory to move into when the move is started from the target side, or
     *     {@code null} when the dialog asks for it.
     * @return the not-yet-started operation.
     */
    MoveOperation createMoveOperation(TransferDialogParams params, @Nullable GreyWolfFile targetDirectory);

    /**
     * Creates a delete run, without starting it - the view model must feed it the marked sources, then call
     * {@code DeleteOperation#start} itself once ready.
     *
     * @param configManager provides the configs of the dialogs the run opens.
     * @param settings the settings of those dialogs.
     * @return the not-yet-started operation.
     */
    DeleteOperation createDeleteOperation(ConfigManager configManager, GreyWolfSettings settings);
}
