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
import com.techsenger.greywolf.storage.base.GreyWolfStorageProvider;
import com.techsenger.shellfx.core.config.ConfigManager;
import com.techsenger.shellfx.core.settings.ShellSettings;
import java.util.List;
import java.util.Objects;

/**
 * Params of the dialog of a transfer started from the source side: the sources are known (marked or selected in the
 * area), and the dialog lets the user edit the target.
 *
 * @author Pavel Castornii
 */
public final class SourceDialogParams extends TransferDialogParams {

    private final GreyWolfFile initialTarget;

    private final GreyWolfStorageProvider storageProvider;

    private final @Nullable GreyWolfFile currentFile;

    /**
     * Creates params.
     *
     * @param initialTarget the directory currently shown on the target side, the default target of the transfer.
     * @param storageProvider tells every storage the entered target may belong to, each time they are needed; the
     *     storage of the target directory is tried first.
     * @param currentFile the file currently selected on the source side, whose entry type a renamed target gets;
     *     {@code null} when a single source may not be given a new name while it is transferred.
     */
    public SourceDialogParams(SourceDialogConfig config, ShellSettings settings, ConfigManager configManager,
            TransferOperationType operationType, List<GreyWolfFile> sourceFiles, GreyWolfFile initialTarget,
            GreyWolfStorageProvider storageProvider, @Nullable GreyWolfFile currentFile) {
        super(config, settings, configManager, operationType, sourceFiles);
        this.initialTarget = initialTarget;
        this.storageProvider = storageProvider;
        this.currentFile = currentFile;
    }

    public GreyWolfFile getInitialTarget() {
        return initialTarget;
    }

    public GreyWolfStorageProvider getStorageProvider() {
        return storageProvider;
    }

    public @Nullable GreyWolfFile getCurrentFile() {
        return currentFile;
    }

    @Override
    public SourceDialogConfig getConfig() {
        return (SourceDialogConfig) super.getConfig();
    }

    @Override
    public void validate() {
        super.validate();
        Objects.requireNonNull(initialTarget, "Initial target can't be null");
        Objects.requireNonNull(storageProvider, "Storage provider can't be null");
    }
}
