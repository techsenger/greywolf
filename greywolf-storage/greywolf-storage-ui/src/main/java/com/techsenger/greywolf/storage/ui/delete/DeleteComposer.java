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

package com.techsenger.greywolf.storage.ui.delete;

import com.techsenger.greywolf.storage.ui.operation.OperationComposer;

/**
 * Creates the dialogs a delete operation opens during its run and hands them back as ports, so the operation
 * never touches a concrete view or view model and can be tested with a stub.
 *
 * @author Pavel Castornii
 */
public interface DeleteComposer extends OperationComposer {

    /**
     * Opens the dialog showing the progress of the deletion.
     *
     * @param params the dialog params
     * @return the port of the opened dialog
     */
    DeletingDialogPort openDeletingDialog(DeletingDialogParams params);
}
