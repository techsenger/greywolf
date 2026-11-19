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

package com.techsenger.greywolf.storage.ui.operation;

import com.techsenger.shellfx.dialogs.alert.AlertDialogParams;
import com.techsenger.shellfx.dialogs.alert.AlertDialogPort;
import com.techsenger.shellfx.dialogs.progress.ProgressDialogParams;
import com.techsenger.shellfx.dialogs.progress.ProgressDialogPort;

/**
 * Creates the dialogs every long-running file operation (copy, move, delete) needs besides its own, and hands
 * them back as ports, so the operation never touches a concrete view or view model and can be tested with a stub.
 *
 * @author Pavel Castornii
 */
public interface OperationComposer {

    /**
     * Creates a progress dialog without adding it to the window; the caller decides when to show it.
     *
     * @param params the dialog params
     * @return the created dialog, not shown yet
     */
    DeferredDialog<ProgressDialogPort> createProgressDialog(ProgressDialogParams params);

    /**
     * Opens the dialog asking how to proceed after a failed task.
     *
     * @param params the dialog params
     * @return the port of the opened dialog
     */
    OperationErrorDialogPort openOperationErrorDialog(OperationErrorDialogParams params);

    /**
     * Opens an alert dialog (message or confirmation).
     *
     * @param params the dialog params
     * @return the port of the opened dialog
     */
    AlertDialogPort openAlertDialog(AlertDialogParams params);
}
