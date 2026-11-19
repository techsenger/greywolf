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

import com.techsenger.toolkit.core.xml.IndentingXmlStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author Pavel Castornii
 */
final class RecentEntriesWriter {

    void write(List<RecentEntry> entries, Path path) throws Exception {
        XMLStreamWriter rawStreamWriter = XMLOutputFactory.newInstance()
                .createXMLStreamWriter(Files.newOutputStream(path), "UTF-8");

        try (var xml = new IndentingXmlStreamWriter(rawStreamWriter)) {
            xml.writeStartDocument("UTF-8", "1.0");
            xml.writeStartElement("RecentFiles");
            for (RecentEntry entry : entries) {
                writeEntry(xml, entry);
            }
            xml.writeEndElement();
            xml.writeEndDocument();
            xml.flush();
        }
    }

    private void writeEntry(XMLStreamWriter xml, RecentEntry entry) throws XMLStreamException {
        xml.writeEmptyElement("File");
        xml.writeAttribute("uri", entry.uri());
        xml.writeAttribute("name", entry.name());
        xml.writeAttribute("entryType", entry.entryType().name());
        var type = entry.type();
        if (type != null) {
            xml.writeAttribute("type", type.name());
        }
        xml.writeAttribute("storageCategory", entry.storageCategory().name());
        xml.writeAttribute("storageType", entry.storageType().name());
        xml.writeAttribute("storageUri", entry.storageUri());
        xml.writeAttribute("visitCount", Integer.toString(entry.visitCount()));
        xml.writeAttribute("lastUsedAt", entry.lastUsedAt().toString());
    }
}
