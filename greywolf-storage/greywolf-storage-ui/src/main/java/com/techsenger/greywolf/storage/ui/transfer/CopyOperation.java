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
import com.techsenger.greywolf.storage.base.transfer.Copier;

/**
 * Copies the marked entries to a target; the sources are left untouched. Workflow (each line is one branch;
 * "end" means the run stops and nothing else opens):
 * <pre>{@code
 * SourceDialog or TargetDialog
 *   Cancel                        -> end
 *   OK, target can't be resolved  -> warning inside the dialog, stays open
 *   OK, target resolved           -> ScanDialog
 *
 * ScanDialog (appears only if scanning outlasts the show delay)
 *   scan completed                -> TransferringDialog
 *   scan failed                   -> Error alert -> end
 *   scan cancelled                -> end
 *   Cancel clicked                -> Confirm alert "Cancel the copy operation?" (scan paused meanwhile)
 *     No                          -> scan resumes
 *     Yes                         -> scan cancelled -> end
 *
 * TransferringDialog
 *   source and target are the same entry -> entry skipped, nothing is touched
 *   target already exists         -> ConflictDialog: Overwrite / Rename / Skip (optionally for all) -> continues
 *   task failed                   -> OperationErrorDialog
 *     Skip / Skip all / Retry     -> continues
 *     Abort                       -> aborted (see below)
 *   Cancel clicked                -> Confirm alert "Cancel the copy operation?" (transfer paused meanwhile)
 *     No                          -> transfer resumes
 *     Yes                         -> cancelled (see below)
 *   file transferred              -> source stays as it is
 *   all tasks processed, no failures -> completed
 *   all tasks processed, failures    -> completed, then Warning alert with copied/skipped/failed counts
 *   cancelled                     -> TransferringDialog closes -> end
 *   aborted                       -> TransferringDialog closes -> Error alert "Copy aborted: reason" -> end
 * }</pre>
 * The finished callback fires once the TransferringDialog has closed after completion, cancellation or abort -
 * never when the run ends earlier (dialog cancelled, scan failed or cancelled).
 *
 * @author Pavel Castornii
 */
public final class CopyOperation extends AbstractTransferOperation {

    public CopyOperation(TransferComposer composer, TransferDialogParams dialogParams,
            @Nullable GreyWolfFile initialTarget) {
        super(composer, dialogParams, initialTarget, TransferOperationType.COPY,
                new Copier());
    }
}
