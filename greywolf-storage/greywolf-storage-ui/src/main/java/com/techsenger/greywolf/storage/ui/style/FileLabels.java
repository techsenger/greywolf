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

package com.techsenger.greywolf.storage.ui.style;

import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.base.FileType;
import com.techsenger.shellfx.storage.base.FileEntryType;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Human-readable labels of the entry types and file types.
 *
 * @author Pavel Castornii
 */
public final class FileLabels {

    private static final Map<FileEntryType, String> ENTRY_LABELS = new EnumMap<>(FileEntryType.class);

    private static final Map<FileType, String> LABELS = new EnumMap<>(FileType.class);

    static {
        ENTRY_LABELS.put(FileEntryType.DIRECTORY, "Folder");
        ENTRY_LABELS.put(FileEntryType.FILE, "File");
        ENTRY_LABELS.put(FileEntryType.LINK, "Link");
        ENTRY_LABELS.put(FileEntryType.OTHER, "Other");
        LABELS.put(FileType.ARCHIVE, "Archive");
        LABELS.put(FileType.CODE, "Code");
        LABELS.put(FileType.TEXT, "Text");
        LABELS.put(FileType.DOCUMENT, "Document");
        LABELS.put(FileType.IMAGE, "Image");
        LABELS.put(FileType.AUDIO, "Audio");
        LABELS.put(FileType.VIDEO, "Video");
        LABELS.put(FileType.FONT, "Font");
        LABELS.put(FileType.APPLICATION, "Application");
        LABELS.put(FileType.UNKNOWN, "Unknown");
    }

    /**
     * Returns the label of {@code entryType}.
     *
     * @param entryType the entry type to label.
     */
    public static String ofEntryType(FileEntryType entryType) {
        return Objects.requireNonNull(ENTRY_LABELS.get(entryType), "No label for entry type: " + entryType);
    }

    /**
     * Returns the label of {@code type}.
     *
     * @param type the file type to label.
     */
    public static String ofFileType(FileType type) {
        return Objects.requireNonNull(LABELS.get(type), "No label for file type: " + type);
    }

    /**
     * Returns the label describing the type of {@code file}: the label of its file type for a regular file, otherwise
     * the label of its entry type.
     *
     * @param file the file to describe.
     */
    public static String ofEntryOrFileType(GreyWolfFile file) {
        return switch (file.getEntryType()) {
            case FILE -> ofFileType(Objects.requireNonNull(file.getType(),
                    "A regular file without a type: " + file.getUri()));
            case DIRECTORY, LINK, OTHER -> ofEntryType(file.getEntryType());
        };
    }

    private FileLabels() {
        // empty
    }
}
