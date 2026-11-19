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

import com.techsenger.greywolf.storage.ui.operation.DetailsDialogParams;
import com.techsenger.shellfx.core.settings.ShellSettings;
import com.techsenger.shellfx.core.window.WindowType;
import java.util.Objects;

/**
 * Always opened nested within the panel manager tab that started the transfer, never as an independent top-level
 * window. The transfer's own progress is never worth remembering across runs, but whether the details section
 * was left expanded or collapsed is, so a config is still wired up for that.
 *
 * @author Pavel Castornii
 */
public class TransferringDialogParams extends DetailsDialogParams {

    private final TransferOperationType operationType;

    public TransferringDialogParams(TransferringDialogConfig config, ShellSettings settings,
            TransferOperationType operationType) {
        super(config, WindowType.NESTED, settings);
        this.operationType = operationType;
    }

    public TransferOperationType getOperationType() {
        return operationType;
    }

    @Override
    public TransferringDialogConfig getConfig() {
        return (TransferringDialogConfig) super.getConfig();
    }

    @Override
    public void validate() {
        super.validate();
        Objects.requireNonNull(getConfig());
        Objects.requireNonNull(operationType, "Operation type can't be null");
    }
}
