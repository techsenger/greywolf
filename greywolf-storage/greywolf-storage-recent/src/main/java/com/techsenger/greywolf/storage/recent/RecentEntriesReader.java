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

package com.techsenger.greywolf.storage.recent;

import com.techsenger.greywolf.storage.base.FileType;
import com.techsenger.greywolf.storage.base.StorageCategory;
import com.techsenger.shellfx.storage.base.FileEntryType;
import com.techsenger.shellfx.storage.base.FileStorageType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Pavel Castornii
 */
final class RecentEntriesReader {

    List<RecentEntry> read(Path path) throws Exception {
        var entries = new ArrayList<RecentEntry>();
        try (var input = Files.newInputStream(path)) {
            XMLStreamReader xml = XMLInputFactory.newInstance().createXMLStreamReader(input);
            try {
                while (xml.hasNext()) {
                    if (xml.next() == XMLStreamConstants.START_ELEMENT && "File".equals(xml.getLocalName())) {
                        entries.add(readEntry(xml));
                    }
                }
            } finally {
                xml.close();
            }
        }
        return entries;
    }

    private RecentEntry readEntry(XMLStreamReader xml) {
        var typeText = xml.getAttributeValue(null, "type");
        var type = typeText != null ? FileType.valueOf(typeText) : null;
        return RecentEntry.builder()
                .uri(xml.getAttributeValue(null, "uri"))
                .name(xml.getAttributeValue(null, "name"))
                .entryType(FileEntryType.valueOf(xml.getAttributeValue(null, "entryType")))
                .type(type)
                .storageCategory(StorageCategory.valueOf(xml.getAttributeValue(null, "storageCategory")))
                .storageType(FileStorageType.valueOf(xml.getAttributeValue(null, "storageType")))
                .storageUri(xml.getAttributeValue(null, "storageUri"))
                .visitCount(Integer.parseInt(xml.getAttributeValue(null, "visitCount")))
                .lastUsedAt(Instant.parse(xml.getAttributeValue(null, "lastUsedAt")))
                .build();
    }
}
