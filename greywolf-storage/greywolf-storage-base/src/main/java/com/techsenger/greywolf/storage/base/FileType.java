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

package com.techsenger.greywolf.storage.base;

import com.techsenger.greywolf.storage.base.style.BaseStorageIcons;
import com.techsenger.shellfx.base.icon.FontIcon;
import com.techsenger.shellfx.storage.ui.style.StorageIcons;

/**
 * The kind of content of a regular file, as shown to the user. Directories, links and special entries have no
 * such type: they are told apart by their entry type. Each type has a default icon, which a storage may replace
 * for a particular entry.
 *
 * @author Pavel Castornii
 */
@SuppressWarnings("ImmutableEnumChecker")
public enum FileType {

    /**
     * A compressed or packaged archive, e.g. zip, tar, gzip, 7z, rar. Resolved via an explicit archive-extension
     * list rather than generic MIME detection, since several archive formats share their container format with
     * other categories.
     */
    ARCHIVE(BaseStorageIcons.ARCHIVE_FILE),

    /**
     * A source code, markup, or data file recognized by name or extension via the GitHub Linguist language
     * list. Takes priority over {@code TEXT} and {@code APPLICATION} despite being plain text, since its type
     * is more specific.
     */
    CODE(BaseStorageIcons.CODE_FILE),

    /**
     * A plain text file with no recognized language or format, e.g. {@code .txt}, {@code .log}, or unrecognized
     * {@code text/*} content. The residual category for text that isn't {@code CODE} or {@code APPLICATION}.
     */
    TEXT(BaseStorageIcons.TEXT_FILE),

    /**
     * A structured document format opened in a dedicated viewer, e.g. PDF, Word, Excel, PowerPoint,
     * OpenDocument, RTF, or EPUB. Resolved via an explicit MIME whitelist, since some are zip-based or
     * misreported as {@code text/*} (e.g. RTF).
     */
    DOCUMENT(BaseStorageIcons.DOCUMENT_FILE),

    /**
     * An image file, e.g. PNG, JPEG, GIF, SVG, or BMP, identified by a {@code image/*} MIME type.
     */
    IMAGE(BaseStorageIcons.IMAGE_FILE),

    /**
     * An audio file, e.g. MP3, WAV, FLAC, or OGG, identified by an {@code audio/*} MIME type.
     */
    AUDIO(BaseStorageIcons.AUDIO_FILE),

    /**
     * A video file, e.g. MP4, MKV, WebM, or AVI, identified by a {@code video/*} MIME type.
     */
    VIDEO(BaseStorageIcons.VIDEO_FILE),

    /**
     * A font file, e.g. TTF, OTF, WOFF, or WOFF2, identified by a {@code font/*} MIME type or, in older
     * databases, a legacy {@code application/font-*} type. Resolved via an explicit whitelist so it isn't
     * absorbed into {@code APPLICATION}.
     */
    FONT(BaseStorageIcons.FONT_FILE),

    /**
     * A binary file that doesn't fall into a more specific category, e.g. an executable or installer, or a
     * generic {@code application/*} MIME type not covered by {@code DOCUMENT}, {@code ARCHIVE}, or
     * {@code FONT}.
     */
    APPLICATION(BaseStorageIcons.APPLICATION_FILE),

    /**
     * A regular file whose type could not be determined, or one that falls under a rare/unsupported MIME supertype
     * (e.g. {@code model/*}, {@code message/*}, {@code multipart/*}, {@code chemical/*}).
     */
    UNKNOWN(StorageIcons.FILE);

    private final FontIcon<?> icon;

    FileType(FontIcon<?> icon) {
        this.icon = icon;
    }

    /**
     * Returns this type's default icon. It is only a default: the icon of an entry is not bound to its type, and
     * the storage that lists the entry may give it another one - e.g. a well-known folder gets its own icon, and a
     * link gets one by what it points to.
     *
     * @return the default icon, never {@code null}
     */
    public FontIcon<?> getIcon() {
        return icon;
    }
}
