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

package com.techsenger.greywolf.boot;

import com.techsenger.toolkit.core.version.Version;
import com.techsenger.weaverbird.core.api.registry.ComponentEntry;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Pavel Castornii
 */
class ActivatedPluginsReader {

    List<ComponentEntry> read(Path path) throws Exception {
        var plugins = new ArrayList<ComponentEntry>();
        try (var input = Files.newInputStream(path)) {
            XMLStreamReader xml = XMLInputFactory.newInstance().createXMLStreamReader(input);
            try {
                while (xml.hasNext()) {
                    if (xml.next() == XMLStreamConstants.START_ELEMENT && "Plugin".equals(xml.getLocalName())) {
                        var name = xml.getAttributeValue(null, "name");
                        var version = Version.of(xml.getAttributeValue(null, "version"));
                        plugins.add(new ComponentEntry(name, version));
                    }
                }
            } finally {
                xml.close();
            }
        }
        return plugins;
    }
}
