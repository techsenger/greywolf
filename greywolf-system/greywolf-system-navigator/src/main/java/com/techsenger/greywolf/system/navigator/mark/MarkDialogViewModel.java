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

package com.techsenger.greywolf.system.navigator.mark;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.greywolf.storage.base.FileType;
import com.techsenger.shellfx.core.close.CloseCheckResult;
import com.techsenger.shellfx.core.close.ClosePreparationResult;
import com.techsenger.shellfx.core.config.ConfigUtils;
import com.techsenger.shellfx.core.dialog.AbstractDialogViewModel;
import com.techsenger.shellfx.core.window.WindowComposer;
import com.techsenger.shellfx.material.button.ResultButtonName;
import com.techsenger.shellfx.storage.base.FileEntryType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

/**
 *
 * @author Pavel Castornii
 */
public final class MarkDialogViewModel<C extends WindowComposer> extends AbstractDialogViewModel<C>
        implements MarkDialogPort {

    private static String titleFor(MarkOperationType operationType) {
        return switch (operationType) {
            case MARK -> "Marking";
            case UNMARK -> "Unmarking";
        };
    }

    private final MarkOperationType operationType;

    private final StringProperty mask = new SimpleStringProperty();

    private final ObservableList<String> modifiableMasks = FXCollections.observableArrayList();

    private final ObservableList<String> masks = FXCollections.unmodifiableObservableList(modifiableMasks);

    private final BooleanProperty caseSensitiveSelected = new SimpleBooleanProperty();

    private final BooleanProperty regexSelected = new SimpleBooleanProperty();

    private final ObservableMap<FileEntryType, Boolean> modifiableSelectionsByEntryType =
            FXCollections.observableMap(MarkDialogConfig.createDefaultSelectionsByEntryType());

    private final ObservableMap<FileEntryType, Boolean> selectionsByEntryType =
            FXCollections.unmodifiableObservableMap(modifiableSelectionsByEntryType);

    private final ObservableMap<FileType, Boolean> modifiableSelectionsByFileType =
            FXCollections.observableMap(MarkDialogConfig.createDefaultSelectionsByFileType());

    private final ObservableMap<FileType, Boolean> selectionsByFileType =
            FXCollections.unmodifiableObservableMap(modifiableSelectionsByFileType);

    public MarkDialogViewModel(MarkDialogParams params) {
        super(params);
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
    public MarkOperationType getOperationType() {
        return operationType;
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
    public @Unmodifiable ObservableList<String> getMasks() {
        return masks;
    }

    @Override
    public void setMasks(List<String> masks) {
        this.modifiableMasks.setAll(masks);
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
    public @Unmodifiable ObservableMap<FileEntryType, Boolean> getSelectionsByEntryType() {
        return selectionsByEntryType;
    }

    @Override
    public void setSelectionsByEntryType(Map<FileEntryType, Boolean> selectionsByEntryType) {
        this.modifiableSelectionsByEntryType.putAll(selectionsByEntryType);
    }

    @Override
    public @Unmodifiable ObservableMap<FileType, Boolean> getSelectionsByFileType() {
        return selectionsByFileType;
    }

    @Override
    public void setSelectionsByFileType(Map<FileType, Boolean> selectionsByFileType) {
        this.modifiableSelectionsByFileType.putAll(selectionsByFileType);
    }

    @Override
    public void onResult(ResultButtonName buttonName) {
        if (buttonName == MarkDialogButtons.APPLY || buttonName == MarkDialogButtons.OK) {
            var texts = new ArrayList<>(masks);
            ConfigUtils.addFirst(texts, getMask());
            ConfigUtils.limit(texts);
            modifiableMasks.setAll(texts);
        }
        super.onResult(buttonName);
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setTitle(titleFor(operationType));
        setResizable(false);
        setRightButtons(MarkDialogButtons.CANCEL, MarkDialogButtons.APPLY, MarkDialogButtons.OK);
        setButtonDefault(MarkDialogButtons.OK, true);
    }

    @Override
    protected void loadStateFromConfig() {
        super.loadStateFromConfig();
        var config = getConfig();
        var configMasks = config.getMasks();
        modifiableMasks.setAll(configMasks);
        // Always sets a mask, not only when configMasks is non-empty: closing via Cancel never adds anything to
        // it, so leaving it empty here is a real case.
        setMask(configMasks.isEmpty() ? "*" : configMasks.get(0));
        setCaseSensitiveSelected(config.isCaseSensitiveSelected());
        setRegexSelected(config.isRegexSelected());
        setSelectionsByEntryType(config.getSelectionsByEntryType());
        setSelectionsByFileType(config.getSelectionsByFileType());
    }

    @Override
    protected void observeStateForConfig() {
        super.observeStateForConfig();
        var config = getConfig();
        modifiableMasks.addListener((ListChangeListener<String>) change -> {
            config.setMasks(new ArrayList<>(modifiableMasks));
            config.notifyListeners();
        });
        ConfigUtils.observe(caseSensitiveSelected, config, MarkDialogConfig::setCaseSensitiveSelected);
        ConfigUtils.observe(regexSelected, config, MarkDialogConfig::setRegexSelected);
        modifiableSelectionsByEntryType.addListener((MapChangeListener<FileEntryType, Boolean>) change -> {
            config.setSelectionsByEntryType(new HashMap<>(modifiableSelectionsByEntryType));
            config.notifyListeners();
        });
        modifiableSelectionsByFileType.addListener((MapChangeListener<FileType, Boolean>) change -> {
            config.setSelectionsByFileType(new HashMap<>(modifiableSelectionsByFileType));
            config.notifyListeners();
        });
    }

    @Override
    protected MarkDialogConfig getConfig() {
        return (MarkDialogConfig) super.getConfig();
    }

    /**
     * Called by the view when the user (un)checks {@code entryType}'s checkbox. Checking/unchecking File also forces
     * every file type to the same state, since File is the master gate for all regular files &mdash; a type left
     * checked while File is off would never be considered anyway. The file type checkboxes follow through the
     * observable selections map.
     *
     * @param entryType the entry type whose checkbox changed.
     * @param selected the new checked state of that checkbox.
     */
    void onEntryTypeChanged(FileEntryType entryType, boolean selected) {
        modifiableSelectionsByEntryType.put(entryType, selected);
        if (entryType == FileEntryType.FILE) {
            for (var type : new ArrayList<>(modifiableSelectionsByFileType.keySet())) {
                modifiableSelectionsByFileType.put(type, selected);
            }
        }
    }

    /**
     * Called by the view when the user (un)checks {@code type}'s checkbox under File. Also syncs File to whether any
     * file type is still checked &mdash; File checked with zero types checked would mean it never actually matches
     * a file, so the two are kept consistent either way.
     *
     * @param type the file type whose checkbox changed.
     * @param selected the new checked state of that checkbox.
     */
    void onFileTypeChanged(FileType type, boolean selected) {
        modifiableSelectionsByFileType.put(type, selected);
        modifiableSelectionsByEntryType.put(FileEntryType.FILE, anyFileTypeSelected());
    }

    private boolean anyFileTypeSelected() {
        for (var value : modifiableSelectionsByFileType.values()) {
            if (value) {
                return true;
            }
        }
        return false;
    }
}
