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

import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.shellfx.material.button.ResultButtonName;

/**
 * View model of the dialog of a transfer started from the target side. The target is the directory given in the
 * params, so OK needs no validation.
 *
 * @param <C> the composer type.
 * @author Pavel Castornii
 */
public final class TargetDialogViewModel<C extends TransferDialogComposer>
        extends AbstractTransferDialogViewModel<C> implements TargetDialogPort {

    private final GreyWolfFile sourceDirectory;

    public TargetDialogViewModel(TargetDialogParams params) {
        super(params);
        this.sourceDirectory = params.getSourceDirectory();
    }

    @Override
    public GreyWolfFile getSourceDirectory() {
        return sourceDirectory;
    }

    @Override
    protected void confirm(ResultButtonName buttonName) {
        accept(buttonName);
    }
}
