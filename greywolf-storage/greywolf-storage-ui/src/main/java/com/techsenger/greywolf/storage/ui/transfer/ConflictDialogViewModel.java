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
import com.techsenger.shellfx.core.close.CloseCheckResult;
import com.techsenger.shellfx.core.close.ClosePreparationResult;
import com.techsenger.shellfx.core.config.ConfigUtils;
import com.techsenger.shellfx.core.dialog.AbstractDialogViewModel;
import com.techsenger.shellfx.core.window.WindowComposer;
import com.techsenger.shellfx.dialogs.style.DialogIcons;
import java.util.function.Consumer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Pavel Castornii
 */
public final class ConflictDialogViewModel<C extends WindowComposer> extends AbstractDialogViewModel<C>
        implements ConflictDialogPort {

    private final StringProperty filePath = new SimpleStringProperty();

    private final BooleanProperty applyToAllSelected = new SimpleBooleanProperty();

    public ConflictDialogViewModel(ConflictDialogParams params) {
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
    public @Nullable String getFilePath() {
        return filePath.get();
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
    public boolean isApplyToAllSelected() {
        return applyToAllSelected.get();
    }

    @Override
    public void setApplyToAllSelected(boolean applyToAllSelected) {
        this.applyToAllSelected.set(applyToAllSelected);
    }

    @Override
    public BooleanProperty applyToAllSelectedProperty() {
        return applyToAllSelected;
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setTitle("File Already Exists");
        setIcon(DialogIcons.WARNING);
        setRightButtons(ConflictDialogButtons.SKIP, ConflictDialogButtons.RENAME, ConflictDialogButtons.OVERWRITE);
        setButtonDefault(ConflictDialogButtons.OVERWRITE, true);
        setResizable(true);
    }

    @Override
    protected void loadStateFromConfig() {
        super.loadStateFromConfig();
        setApplyToAllSelected(getConfig().isApplyToAllSelected());
    }

    @Override
    protected void observeStateForConfig() {
        super.observeStateForConfig();
        var config = getConfig();
        ConfigUtils.observe(applyToAllSelected, config, ConflictDialogConfig::setApplyToAllSelected);
    }

    @Override
    protected ConflictDialogConfig getConfig() {
        return (ConflictDialogConfig) super.getConfig();
    }
}
