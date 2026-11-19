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

import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.base.FileType;
import com.techsenger.greywolf.storage.base.operation.NameMask;
import com.techsenger.shellfx.storage.base.FileEntryType;
import java.util.Map;
import java.util.Objects;

/**
 * Filters which entries a mark operation applies to: a name mask (wildcard {@code *}/{@code ?}, or, when
 * {@code regex} is {@code true}, a full regular expression), whether matching is case-sensitive, and which entry
 * types and, for regular files, which file types are eligible.
 *
 * @author Pavel Castornii
 */
public final class MarkCriteria {

    /**
     * Counts how many of {@code selections}'s entries are checked ({@code true}).
     */
    private static int countSelected(Map<?, Boolean> selections) {
        var count = 0;
        for (var selected : selections.values()) {
            if (Boolean.TRUE.equals(selected)) {
                count++;
            }
        }
        return count;
    }

    private final Map<FileEntryType, Boolean> selectionsByEntryType;

    private final Map<FileType, Boolean> selectionsByFileType;

    /**
     * Precomputed once here (rather than on every {@link #matches} call) so the common case &mdash; every
     * file type checked, i.e. no per-type filtering at all &mdash; skips the per-file map lookup entirely.
     */
    private final boolean allFileTypesSelected;

    private final NameMask nameMask;

    public MarkCriteria(String mask, boolean caseSensitive, boolean regex,
            Map<FileEntryType, Boolean> selectionsByEntryType, Map<FileType, Boolean> selectionsByFileType) {
        this.selectionsByEntryType = selectionsByEntryType;
        this.selectionsByFileType = selectionsByFileType;
        this.allFileTypesSelected = countSelected(selectionsByFileType) == FileType.values().length;
        this.nameMask = new NameMask(mask, caseSensitive, regex);
    }

    public String getMask() {
        return nameMask.getMask();
    }

    public boolean isCaseSensitive() {
        return nameMask.isCaseSensitive();
    }

    public boolean isRegex() {
        return nameMask.isRegex();
    }

    public Map<FileEntryType, Boolean> getSelectionsByEntryType() {
        return selectionsByEntryType;
    }

    public Map<FileType, Boolean> getSelectionsByFileType() {
        return selectionsByFileType;
    }

    /**
     * Returns whether {@code file} is eligible for the operation: not the synthetic parent (&quot;..&quot;)
     * entry, of a checked entry type (and, for regular files, of a file type checked in
     * {@link #getSelectionsByFileType()}), and matching {@link #getMask()} in full. Always {@code false} when
     * {@code mask}/{@code regex} do not compile into a valid pattern.
     *
     * @param file the entry to test.
     */
    public boolean matches(GreyWolfFile file) {
        if (file.isParentEntry()) {
            return false;
        }
        var entryType = file.getEntryType();
        if (!Objects.requireNonNull(selectionsByEntryType.get(entryType))) {
            return false;
        }
        if (entryType == FileEntryType.FILE && !allFileTypesSelected) {
            var type = file.getType();
            if (type != null && !Objects.requireNonNull(selectionsByFileType.get(type))) {
                return false;
            }
        }
        return nameMask.matches(file.getName());
    }
}
