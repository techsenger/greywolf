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

import com.techsenger.annotations.Nullable;
import com.techsenger.toolkit.core.TextDetector;
import com.techsenger.toolkit.core.file.FileUtils;
import java.util.Locale;
import java.util.Set;
import org.apache.tika.Tika;

/**
 *
 * @author Pavel Castornii
 */
public final class FileTypeResolver {

    /**
     * What is resolved about the content of a regular file.
     *
     * @param type the type of the content
     * @param mimeType the MIME type of the content
     */
    public record Resolution(FileType type, String mimeType) {

    }

    /**
     * The MIME type of content that no detector recognized.
     */
    static final String OCTET_STREAM = "application/octet-stream";

    /**
     * Only these top-level MIME types are recognized directly; anything else resolves to {@code FileType.UNKNOWN}.
     */
    private static final Set<String> USED_MIME_TYPES = Set.of("image", "video", "audio", "font", "text", "application");

    /**
     * The extension list from https://github.com/dyne/file-extension-list/blob/master/data/categories/archive.csv .
     */
    private static final Set<String> ARCHIVE_EXTENSIONS = Set.of(
            "7z",
            "a",
            "aar",
            "apk",
            "ar",
            "br",
            "bz2",
            "cab",
            "cpio",
            "deb",
            "dmg",
            "egg",
            "gz",
            "iso",
            "jar",
            "lha",
            "lz",
            "lz4",
            "lzma",
            "lzo",
            "mar",
            "pak",
            "pea",
            "rar",
            "rpm",
            "s7z",
            "shar",
            "tar",
            "tbz2",
            "tgz",
            "tlz",
            "txz",
            "war",
            "whl",
            "xpi",
            "xz",
            "zip",
            "zipx",
            "zst"
    );

    private static final Set<String> DOCUMENT_MIME_TYPES = Set.of(

            // PDF and PDF-adjacent
            "application/pdf",
            "application/vnd.cups-pdf",
            "application/vnd.sealedmedia.softseal.pdf",

            // PostScript
            "application/postscript",
            "application/vnd.cups-postscript",

            // Rich Text Format
            "application/rtf",

            // Microsoft Word (legacy binary)
            "application/msword",
            "application/msword2",
            "application/msword5",

            // Microsoft Word (OOXML)
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.template",

            // Microsoft Excel (legacy binary)
            "application/vnd.ms-excel",
            "application/vnd.ms-excel.sheet.2",
            "application/vnd.ms-excel.sheet.3",
            "application/vnd.ms-excel.sheet.4",
            "application/vnd.ms-excel.workspace.3",
            "application/vnd.ms-excel.workspace.4",
            "application/vnd.ms-spreadsheetml",

            // Microsoft Excel (OOXML, incl. macro-enabled)
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.template",
            "application/vnd.ms-excel.addin.macroenabled.12",
            "application/vnd.ms-excel.sheet.binary.macroenabled.12",
            "application/vnd.ms-excel.sheet.macroenabled.12",
            "application/vnd.ms-excel.template.macroenabled.12",

            // Microsoft PowerPoint (legacy binary)
            "application/vnd.ms-powerpoint",

            // Microsoft PowerPoint (OOXML, incl. macro-enabled)
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.openxmlformats-officedocument.presentationml.slide",
            "application/vnd.openxmlformats-officedocument.presentationml.slideshow",
            "application/vnd.openxmlformats-officedocument.presentationml.template",
            "application/vnd.ms-powerpoint.addin.macroenabled.12",
            "application/vnd.ms-powerpoint.presentation.macroenabled.12",
            "application/vnd.ms-powerpoint.slide.macroenabled.12",
            "application/vnd.ms-powerpoint.slideshow.macroenabled.12",
            "application/vnd.ms-powerpoint.template.macroenabled.12",

            // Microsoft Publisher / Visio / Project / Works
            "application/vnd.ms-publisher",
            "application/vnd.ms-project",
            "application/vnd.ms-works",
            "application/x-tika-msworks-spreadsheet",
            "application/vnd.visio",
            "application/vnd.ms-visio.drawing",
            "application/vnd.ms-visio.drawing.macroEnabled.12",
            "application/vnd.ms-visio.stencil",
            "application/vnd.ms-visio.stencil.macroEnabled.12",
            "application/vnd.ms-visio.template",
            "application/vnd.ms-visio.template.macroEnabled.12",
            "application/x-tika-visio-ooxml",

            // Microsoft XPS
            "application/vnd.ms-xpsdocument",
            "application/oxps",

            // OpenDocument (LibreOffice/OpenOffice/OASIS)
            "application/vnd.oasis.opendocument.base",
            "application/vnd.oasis.opendocument.text",
            "application/vnd.oasis.opendocument.text-master",
            "application/vnd.oasis.opendocument.text-template",
            "application/vnd.oasis.opendocument.text-web",
            "application/vnd.oasis.opendocument.spreadsheet",
            "application/vnd.oasis.opendocument.spreadsheet-template",
            "application/vnd.oasis.opendocument.presentation",
            "application/vnd.oasis.opendocument.presentation-template",
            "application/vnd.oasis.opendocument.graphics",
            "application/vnd.oasis.opendocument.graphics-template",
            "application/vnd.oasis.opendocument.chart",
            "application/vnd.oasis.opendocument.chart-template",
            "application/vnd.oasis.opendocument.formula",
            "application/vnd.oasis.opendocument.formula-template",
            "application/vnd.oasis.opendocument.image",
            "application/vnd.oasis.opendocument.image-template",
            "application/vnd.oasis.opendocument.flat.text",
            "application/vnd.oasis.opendocument.flat.presentation",
            "application/vnd.oasis.opendocument.flat.spreadsheet",
            "application/vnd.oasis.opendocument.tika.flat.document",

            // StarOffice (OpenOffice predecessor)
            "application/vnd.stardivision.calc",
            "application/vnd.stardivision.draw",
            "application/vnd.stardivision.impress",
            "application/vnd.stardivision.math",
            "application/vnd.stardivision.writer",
            "application/vnd.stardivision.writer-global",

            // Apple iWork
            "application/vnd.apple.iwork",
            "application/vnd.apple.pages",
            "application/vnd.apple.numbers",
            "application/vnd.apple.keynote",
            "application/x-tika-iworks-protected",
            "application/x-appleworks",

            // Ebooks
            "application/epub+zip",
            "application/x-mobipocket-ebook",
            "application/vnd.amazon.ebook",

            // WordPerfect
            "application/vnd.wordperfect",
            "application/vnd.wordperfect;version=4.2",
            "application/vnd.wordperfect;version=5.0",
            "application/vnd.wordperfect;version=5.1",
            "application/vnd.wordperfect;version=6.x",
            "application/wordperfect5.1",

            // AbiWord
            "application/x-abiword",

            // FrameMaker
            "application/vnd.framemaker",

            // Lotus / IBM
            "application/vnd.lotus-1-2-3",
            "application/vnd.lotus-1-2-3;version=1",
            "application/vnd.lotus-1-2-3;version=2",
            "application/vnd.lotus-1-2-3;version=3",
            "application/vnd.lotus-1-2-3;version=4",
            "application/vnd.lotus-1-2-3;version=97+9.x",
            "application/vnd.lotus-approach",
            "application/vnd.lotus-freelance",
            "application/vnd.lotus-organizer",
            "application/vnd.lotus-wordpro",

            // Corel
            "application/x-corelpresentations",

            // Hangul Word Processor (Korean)
            "application/hwp+zip",
            "application/x-hwp",
            "application/x-hwp-v5",

            // Fuji Xerox DocuWorks
            "application/vnd.fujixerox.docuworks",
            "application/vnd.fujixerox.docuworks.binder"
    );

    private static final String TEXT_PLAIN = "text/plain";

    private static final Tika tika = new Tika();

    private static final Linguist linguist = new Linguist();

    /**
     * Resolves the type of a regular file from its name alone, which is cheap and needs no access to the storage.
     *
     * @param fileName the file name, used for extension- and name-based detection
     * @return the type with the MIME type, or {@code null} if the name is not enough and the start of the content
     *     must be read, see {@link #resolveByHeader}
     */
    public static @Nullable Resolution resolveByName(String fileName) {
        var mimeType = tika.detect(fileName);
        var type = classify(mimeType, fileName);
        return type == null ? null : new Resolution(type, mimeType);
    }

    /**
     * Resolves the type of a regular file from the start of its content and its name, for the file whose name is not
     * enough. The text detector decides when the content is not recognized either.
     *
     * @param fileName the file name
     * @param header the start of the content of the file
     * @return the type with the MIME type, never {@code null}
     */
    public static Resolution resolveByHeader(String fileName, byte[] header) {
        var mimeType = tika.detect(header, fileName);
        if (mimeType.equals(OCTET_STREAM)) {
            return TextDetector.isText(header) ? new Resolution(FileType.TEXT, TEXT_PLAIN)
                    : new Resolution(FileType.APPLICATION, OCTET_STREAM);
        }
        var type = classify(mimeType, fileName);
        return new Resolution(type != null ? type : FileType.APPLICATION, mimeType);
    }

    /**
     * Gives a file the resolved type and MIME type.
     *
     * @param file the file that is not shown yet
     * @param resolution the resolved type and MIME type, may be {@code null}
     */
    static void apply(GreyWolfFile file, @Nullable Resolution resolution) {
        if (resolution != null) {
            var defaultFile = DefaultGreyWolfFile.cast(file);
            defaultFile.setType(resolution.type());
            defaultFile.setMimeType(resolution.mimeType());
        }
    }

    /**
     * Gives the type of the MIME type and the name, or {@code null} if only the content can tell it.
     */
    private static @Nullable FileType classify(String mimeType, String fileName) {
        String topLevelType = extractTopLevelType(mimeType);
        FileType fileType = isLegacyFontMimeType(mimeType) ? FileType.FONT : null;

        if (fileType == null) {
            fileType = USED_MIME_TYPES.contains(topLevelType)
                    ? FileType.valueOf(topLevelType.toUpperCase(Locale.ROOT))
                    : FileType.UNKNOWN;
        }
        var ext = FileUtils.getExtension(fileName);
        if (ext != null) {
            ext = ext.toLowerCase(Locale.ROOT);
        }

        if (fileType == FileType.APPLICATION) {
            if (ext != null && ARCHIVE_EXTENSIONS.contains(ext)) {
                fileType = FileType.ARCHIVE;
            } else if (DOCUMENT_MIME_TYPES.contains(mimeType)) {
                fileType = FileType.DOCUMENT;
            } else if (linguist.isCode(fileName, ext)) {
                fileType = FileType.CODE;
            } else if (ext == null && mimeType.equals(OCTET_STREAM)) {
                return null;
            }
        } else if (fileType == FileType.TEXT && linguist.isCode(fileName, ext)) {
            fileType = FileType.CODE;
        }
        return fileType;
    }

    private static String extractTopLevelType(String mimeType) {
        int slashIndex = mimeType.indexOf('/');
        return slashIndex > 0 ? mimeType.substring(0, slashIndex) : mimeType;
    }

    /**
     * Some mime databases still register fonts under application/* instead of the modern font/* top-level type, e.g.
     * application/font-woff, application/x-font-ttf, application/vnd.ms-fontobject.
     */
    private static boolean isLegacyFontMimeType(String mimeType) {
        return mimeType.startsWith("application/font")
                || mimeType.startsWith("application/x-font")
                || mimeType.equals("application/vnd.ms-fontobject")
                || mimeType.equals("application/vnd.ms-opentype");
    }

    private FileTypeResolver() {
        // empty
    }
}
