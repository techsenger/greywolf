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
import com.techsenger.greywolf.storage.ui.style.StorageUiIcons;
import com.techsenger.shellfx.core.close.CloseCheckResult;
import com.techsenger.shellfx.core.close.ClosePreparationResult;
import com.techsenger.shellfx.core.config.ConfigUtils;
import com.techsenger.shellfx.core.dialog.AbstractDialogViewModel;
import com.techsenger.shellfx.core.settings.ShellSettings;
import com.techsenger.shellfx.material.button.ResultButtonName;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * Base view model of a dialog that configures a transfer: the files to transfer, the mask and the options. What
 * the target is and how it is confirmed on OK depends on the side that started the transfer, so it is left to the
 * subclasses.
 *
 * @param <C> the composer type.
 * @author Pavel Castornii
 */
public abstract class AbstractTransferDialogViewModel<C extends TransferDialogComposer>
        extends AbstractDialogViewModel<C> implements TransferDialogPort {

    private final TransferOperationType operationType;

    private final List<GreyWolfFile> sourceFiles;

    private final StringProperty mask = new SimpleStringProperty();

    private final ObservableList<String> masks = FXCollections.observableArrayList();

    private final BooleanProperty caseSensitiveSelected = new SimpleBooleanProperty();

    private final BooleanProperty regexSelected = new SimpleBooleanProperty();

    private final BooleanProperty followLinksSelected = new SimpleBooleanProperty();

    private final BooleanProperty stableSymlinksSelected = new SimpleBooleanProperty();

    private final BooleanProperty preserveAttributesSelected = new SimpleBooleanProperty();

    private final BooleanProperty checkDataIntegritySelected = new SimpleBooleanProperty();

    protected AbstractTransferDialogViewModel(TransferDialogParams params) {
        super(params);
        this.sourceFiles = params.getSourceFiles();
        this.operationType = params.getOperationType();
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
    public void onResult(ResultButtonName buttonName) {
        if (buttonName == TransferDialogButtons.OK) {
            confirm(buttonName);
        } else {
            super.onResult(buttonName);
        }
    }

    @Override
    public @Unmodifiable List<GreyWolfFile> getSourceFiles() {
        return sourceFiles;
    }

    @Override
    public String getMask() {
        return mask.get();
    }

    @Override
    public void setMask(String mask) {
        this.mask.set(mask);
    }

    @Override
    public StringProperty maskProperty() {
        return mask;
    }

    @Override
    public ObservableList<String> getMasks() {
        return masks;
    }

    @Override
    public boolean isCaseSensitiveSelected() {
        return caseSensitiveSelected.get();
    }

    @Override
    public void setCaseSensitiveSelected(boolean caseSensitiveSelected) {
        this.caseSensitiveSelected.set(caseSensitiveSelected);
    }

    @Override
    public BooleanProperty caseSensitiveSelectedProperty() {
        return caseSensitiveSelected;
    }

    @Override
    public boolean isRegexSelected() {
        return regexSelected.get();
    }

    @Override
    public void setRegexSelected(boolean regexSelected) {
        this.regexSelected.set(regexSelected);
    }

    @Override
    public BooleanProperty regexSelectedProperty() {
        return regexSelected;
    }

    @Override
    public boolean isFollowLinksSelected() {
        return followLinksSelected.get();
    }

    @Override
    public void setFollowLinksSelected(boolean followLinksSelected) {
        this.followLinksSelected.set(followLinksSelected);
    }

    @Override
    public BooleanProperty followLinksSelectedProperty() {
        return followLinksSelected;
    }

    @Override
    public boolean isStableSymlinksSelected() {
        return stableSymlinksSelected.get();
    }

    @Override
    public void setStableSymlinksSelected(boolean stableSymlinksSelected) {
        this.stableSymlinksSelected.set(stableSymlinksSelected);
    }

    @Override
    public BooleanProperty stableSymlinksSelectedProperty() {
        return stableSymlinksSelected;
    }

    @Override
    public boolean isPreserveAttributesSelected() {
        return preserveAttributesSelected.get();
    }

    @Override
    public void setPreserveAttributesSelected(boolean preserveAttributesSelected) {
        this.preserveAttributesSelected.set(preserveAttributesSelected);
    }

    @Override
    public BooleanProperty preserveAttributesSelectedProperty() {
        return preserveAttributesSelected;
    }

    @Override
    public boolean isCheckDataIntegritySelected() {
        return checkDataIntegritySelected.get();
    }

    @Override
    public void setCheckDataIntegritySelected(boolean checkDataIntegritySelected) {
        this.checkDataIntegritySelected.set(checkDataIntegritySelected);
    }

    @Override
    public BooleanProperty checkDataIntegritySelectedProperty() {
        return checkDataIntegritySelected;
    }

    @Override
    protected ShellSettings getSettings() {
        return (ShellSettings) super.getSettings();
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setTitle(operationType == TransferOperationType.MOVE ? "Move" : "Copy");
        setIcon(operationType == TransferOperationType.MOVE ? StorageUiIcons.MOVE : StorageUiIcons.COPY_TO);
        setResizable(false);
        setRightButtons(TransferDialogButtons.CANCEL, TransferDialogButtons.OK);
        setButtonDefault(TransferDialogButtons.OK, true);
    }

    @Override
    protected void loadStateFromConfig() {
        super.loadStateFromConfig();
        var config = getConfig();
        setCaseSensitiveSelected(config.isCaseSensitiveSelected());
        setRegexSelected(config.isRegexSelected());
        setFollowLinksSelected(config.isFollowLinksSelected());
        setStableSymlinksSelected(config.isStableSymlinksSelected());
        setPreserveAttributesSelected(config.isPreserveAttributesSelected());
        setCheckDataIntegritySelected(config.isCheckDataIntegritySelected());
        var configMasks = config.getMasks();
        masks.setAll(configMasks);
        // Always sets a mask, not only when configMasks is non-empty: closing via Cancel never adds anything to
        // it, so leaving it empty here is a real case.
        setMask(configMasks.isEmpty() ? "*" : configMasks.get(0));
    }

    @Override
    protected void observeStateForConfig() {
        super.observeStateForConfig();
        var config = getConfig();
        ConfigUtils.observe(caseSensitiveSelected, config, TransferDialogConfig::setCaseSensitiveSelected);
        ConfigUtils.observe(regexSelected, config, TransferDialogConfig::setRegexSelected);
        ConfigUtils.observe(followLinksSelected, config, TransferDialogConfig::setFollowLinksSelected);
        ConfigUtils.observe(stableSymlinksSelected, config, TransferDialogConfig::setStableSymlinksSelected);
        ConfigUtils.observe(preserveAttributesSelected, config,
                TransferDialogConfig::setPreserveAttributesSelected);
        ConfigUtils.observe(checkDataIntegritySelected, config,
                TransferDialogConfig::setCheckDataIntegritySelected);
        masks.addListener((ListChangeListener<String>) change -> {
            config.setMasks(new ArrayList<>(masks));
            config.notifyListeners();
        });
    }

    @Override
    protected TransferDialogConfig getConfig() {
        return (TransferDialogConfig) super.getConfig();
    }

    /**
     * Validates the target on OK and finishes the dialog through {@link #accept}; when the target is not valid,
     * tells the user and leaves the dialog open.
     *
     * @param buttonName the OK button
     */
    protected abstract void confirm(ResultButtonName buttonName);

    /**
     * Accepts what the user entered and finishes the dialog the usual way, so the callback registered by the caller
     * sees OK with the results set. The mask is remembered as the latest one.
     *
     * @param buttonName the OK button
     */
    protected final void accept(ResultButtonName buttonName) {
        var updatedMasks = new ArrayList<>(masks);
        ConfigUtils.addFirst(updatedMasks, getMask());
        ConfigUtils.limit(updatedMasks);
        masks.setAll(updatedMasks);
        super.onResult(buttonName);
    }
}
