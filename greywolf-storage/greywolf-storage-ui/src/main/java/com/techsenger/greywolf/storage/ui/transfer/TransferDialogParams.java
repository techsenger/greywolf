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

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.shellfx.core.config.ConfigManager;
import com.techsenger.shellfx.core.dialog.DialogParams;
import com.techsenger.shellfx.core.settings.ShellSettings;
import com.techsenger.shellfx.core.window.WindowType;
import java.util.List;
import java.util.Objects;

/**
 * Always opened nested within the panel manager tab that started the transfer, so a plain {@link WindowType#NESTED}
 * window is enough - it never floats independently.
 *
 * <p>The dialog is one of two kinds, by the side that started the transfer: {@link SourceDialogParams} or
 * {@link TargetDialogParams}.
 *
 * @author Pavel Castornii
 */
public class TransferDialogParams extends DialogParams {

    private final ConfigManager configManager;

    private final TransferOperationType operationType;

    private final List<GreyWolfFile> sourceFiles;

    protected TransferDialogParams(TransferDialogConfig config, ShellSettings settings, ConfigManager configManager,
            TransferOperationType operationType, List<GreyWolfFile> sourceFiles) {
        super(config, WindowType.NESTED, settings);
        this.configManager = configManager;
        this.operationType = operationType;
        this.sourceFiles = List.copyOf(sourceFiles);
    }

    /**
     * Forwarded, unused by this dialog's own config, to the dialogs the transfer operation opens once scanning
     * completes, and used by this dialog for the standard dialogs it opens itself.
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * The kind of the transfer the dialog configures.
     */
    public TransferOperationType getOperationType() {
        return operationType;
    }

    /**
     * The entries to transfer, in the order they should be transferred.
     */
    public @Unmodifiable List<GreyWolfFile> getSourceFiles() {
        return sourceFiles;
    }

    @Override
    public TransferDialogConfig getConfig() {
        return (TransferDialogConfig) super.getConfig();
    }

    @Override
    public ShellSettings getSettings() {
        return (ShellSettings) super.getSettings();
    }

    @Override
    public void validate() {
        super.validate();
        Objects.requireNonNull(getConfig());
        Objects.requireNonNull(configManager, "Config manager can't be null");
        Objects.requireNonNull(operationType, "Operation type can't be null");
        if (sourceFiles.isEmpty()) {
            throw new IllegalArgumentException("Source files can't be empty");
        }
    }
}
