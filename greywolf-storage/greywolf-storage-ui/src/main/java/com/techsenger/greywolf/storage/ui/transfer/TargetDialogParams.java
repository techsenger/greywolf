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
import com.techsenger.shellfx.core.config.ConfigManager;
import com.techsenger.shellfx.core.settings.ShellSettings;
import java.util.List;
import java.util.Objects;

/**
 * Params of the dialog of a transfer started from the target side (e.g. a clipboard paste): the dialog only
 * displays where the sources are, the target is known to whoever started the transfer.
 *
 * @author Pavel Castornii
 */
public final class TargetDialogParams extends TransferDialogParams {

    private final GreyWolfFile sourceDirectory;

    /**
     * Creates params.
     *
     * @param sourceDirectory the directory the source files are in; all of them are in one directory.
     */
    public TargetDialogParams(TargetDialogConfig config, ShellSettings settings, ConfigManager configManager,
            TransferOperationType operationType, List<GreyWolfFile> sourceFiles, GreyWolfFile sourceDirectory) {
        super(config, settings, configManager, operationType, sourceFiles);
        this.sourceDirectory = sourceDirectory;
    }

    public GreyWolfFile getSourceDirectory() {
        return sourceDirectory;
    }

    @Override
    public TargetDialogConfig getConfig() {
        return (TargetDialogConfig) super.getConfig();
    }

    @Override
    public void validate() {
        super.validate();
        Objects.requireNonNull(sourceDirectory, "Source directory can't be null");
    }
}
