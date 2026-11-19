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
import com.techsenger.shellfx.core.dialog.DialogPort;
import com.techsenger.shellfx.storage.base.FileEntryType;
import java.util.List;
import java.util.Map;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

/**
 * Provides full access to the component's client API.
 *
 * @author Pavel Castornii
 */
public interface MarkDialogPort extends DialogPort {

    MarkOperationType getOperationType();

    String getMask();

    void setMask(String mask);

    StringProperty maskProperty();

    @Unmodifiable ObservableList<String> getMasks();

    void setMasks(List<String> masks);

    boolean isCaseSensitiveSelected();

    void setCaseSensitiveSelected(boolean value);

    BooleanProperty caseSensitiveSelectedProperty();

    boolean isRegexSelected();

    void setRegexSelected(boolean value);

    BooleanProperty regexSelectedProperty();

    @Unmodifiable ObservableMap<FileEntryType, Boolean> getSelectionsByEntryType();

    /**
     * Replaces which entry types are checked, in {@code selectionsByEntryType}'s own iteration order.
     *
     * @param selectionsByEntryType the checked state to apply, keyed by entry type.
     */
    void setSelectionsByEntryType(Map<FileEntryType, Boolean> selectionsByEntryType);

    @Unmodifiable ObservableMap<FileType, Boolean> getSelectionsByFileType();

    /**
     * Replaces which file types are checked under the File entry type, in {@code selectionsByFileType}'s own
     * iteration order.
     *
     * @param selectionsByFileType the checked state to apply, keyed by file type.
     */
    void setSelectionsByFileType(Map<FileType, Boolean> selectionsByFileType);
}
