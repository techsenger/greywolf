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
import com.techsenger.greywolf.storage.base.transfer.AbstractTransferer;
import com.techsenger.greywolf.storage.base.transfer.TransferProgress;
import com.techsenger.greywolf.storage.ui.operation.AbstractDetailsDialogViewModel;
import com.techsenger.greywolf.storage.ui.style.StorageUiIcons;
import com.techsenger.shellfx.core.close.CloseCheckResult;
import com.techsenger.shellfx.core.close.ClosePreparationResult;
import com.techsenger.shellfx.core.window.WindowComposer;
import java.util.function.Consumer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * A pure progress view onto a transfer operation-owned {@link AbstractTransferer} run - it neither starts nor
 * owns the transfer itself, so it can be closed and the underlying run keeps going regardless.
 *
 * @author Pavel Castornii
 */
public final class TransferringDialogViewModel<C extends WindowComposer> extends AbstractDetailsDialogViewModel<C>
        implements TransferringDialogPort {

    private final TransferOperationType operationType;

    private final ObjectProperty<@Nullable TransferProgress> progress = new SimpleObjectProperty<>();

    public TransferringDialogViewModel(TransferringDialogParams params) {
        super(params);
        this.operationType = params.getOperationType();
    }

    @Override
    public TransferOperationType getOperationType() {
        return operationType;
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public @Nullable TransferProgress getProgress() {
        return progress.get();
    }

    @Override
    public void setProgress(TransferProgress progress) {
        this.progress.set(progress);
    }

    @Override
    public ObjectProperty<@Nullable TransferProgress> progressProperty() {
        return progress;
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setTitle(operationType == TransferOperationType.MOVE ? "Moving Files" : "Copying Files");
        setIcon(StorageUiIcons.OPERATION_PROGRESS);
        setResizable(false);
        setRightButtons(TransferringDialogButtons.CANCEL);
    }
}
