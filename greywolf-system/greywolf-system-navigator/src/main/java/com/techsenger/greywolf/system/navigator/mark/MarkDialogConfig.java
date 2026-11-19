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

import com.techsenger.greywolf.storage.base.FileType;
import com.techsenger.shellfx.core.dialog.DialogConfig;
import com.techsenger.shellfx.storage.base.FileEntryType;
import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Pavel Castornii
 */
public class MarkDialogConfig extends DialogConfig {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Returns all entry types, each mapped to {@code true} &mdash; the default, equivalent to no per-type
     * filtering at all.
     */
    public static Map<FileEntryType, Boolean> createDefaultSelectionsByEntryType() {
        var selections = new HashMap<FileEntryType, Boolean>();
        for (var type : FileEntryType.values()) {
            selections.put(type, true);
        }
        return selections;
    }

    /**
     * Returns all file types, each mapped to {@code true} &mdash; the default, equivalent to no per-type
     * filtering at all.
     */
    public static Map<FileType, Boolean> createDefaultSelectionsByFileType() {
        var selections = new HashMap<FileType, Boolean>();
        for (var type : FileType.values()) {
            selections.put(type, true);
        }
        return selections;
    }

    private List<String> masks;

    private boolean caseSensitiveSelected;

    private boolean regexSelected;

    private Map<FileEntryType, Boolean> selectionsByEntryType;

    private Map<FileType, Boolean> selectionsByFileType;

    public MarkDialogConfig() {
        setWidth(450);
        this.masks = new ArrayList<>();
        this.caseSensitiveSelected = false;
        this.regexSelected = false;
        this.selectionsByEntryType = createDefaultSelectionsByEntryType();
        this.selectionsByFileType = createDefaultSelectionsByFileType();
    }

    public List<String> getMasks() {
        return masks;
    }

    public void setMasks(List<String> masks) {
        this.masks = masks;
    }

    public boolean isCaseSensitiveSelected() {
        return caseSensitiveSelected;
    }

    public void setCaseSensitiveSelected(boolean caseSensitiveSelected) {
        this.caseSensitiveSelected = caseSensitiveSelected;
    }

    public boolean isRegexSelected() {
        return regexSelected;
    }

    public void setRegexSelected(boolean regexSelected) {
        this.regexSelected = regexSelected;
    }

    public Map<FileEntryType, Boolean> getSelectionsByEntryType() {
        return selectionsByEntryType;
    }

    public void setSelectionsByEntryType(Map<FileEntryType, Boolean> selectionsByEntryType) {
        this.selectionsByEntryType = selectionsByEntryType;
    }

    public Map<FileType, Boolean> getSelectionsByFileType() {
        return selectionsByFileType;
    }

    public void setSelectionsByFileType(Map<FileType, Boolean> selectionsByFileType) {
        this.selectionsByFileType = selectionsByFileType;
    }
}
