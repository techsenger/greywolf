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

import com.techsenger.shellfx.core.close.CloseCheckResult;
import com.techsenger.shellfx.core.close.ClosePreparationResult;
import com.techsenger.shellfx.core.dialog.AbstractDialogViewModel;
import com.techsenger.shellfx.core.window.WindowComposer;
import com.techsenger.shellfx.dialogs.style.DialogIcons;
import java.util.function.Consumer;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Pavel Castornii
 */
public final class OperationErrorDialogViewModel<C extends WindowComposer> extends AbstractDialogViewModel<C>
        implements OperationErrorDialogPort {

    private final StringProperty message = new SimpleStringProperty();

    private final StringProperty filePath = new SimpleStringProperty();

    public OperationErrorDialogViewModel(OperationErrorDialogParams params) {
        super(params);
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
    public String getMessage() {
        return this.message.get();
    }

    @Override
    public void setMessage(String message) {
        this.message.set(message);
    }

    @Override
    public StringProperty messageProperty() {
        return message;
    }

    @Override
    public String getFilePath() {
        return this.filePath.get();
    }

    @Override
    public void setFilePath(String filePath) {
        this.filePath.set(filePath);
    }

    @Override
    public StringProperty filePathProperty() {
        return filePath;
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setTitle("Error");
        setIcon(DialogIcons.ERROR);
        setResizable(true);
        setRightButtons(OperationErrorDialogButtons.ABORT, OperationErrorDialogButtons.RETRY,
                OperationErrorDialogButtons.SKIP_ALL, OperationErrorDialogButtons.SKIP);
        setButtonDefault(OperationErrorDialogButtons.SKIP, true);
    }
}
