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

package com.techsenger.greywolf.core.internal.plugin;

import com.techsenger.greywolf.core.plugin.Plugin;
import com.techsenger.toolkit.core.xml.IndentingXmlStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
class ActivatedPluginsWriter {

    private static final Logger logger = LoggerFactory.getLogger(ActivatedPluginsWriter.class);

    void write(List<? extends Plugin> plugins, Path path) throws Exception {
        XMLStreamWriter rawStreamWriter = XMLOutputFactory.newInstance()
                .createXMLStreamWriter(Files.newOutputStream(path), "UTF-8");

        try (var xml = new IndentingXmlStreamWriter(rawStreamWriter)) {
            xml.writeStartDocument("UTF-8", "1.0");
            xml.writeStartElement("ActivatedPlugins");
            for (Plugin plugin : plugins) {
                if (plugin.getState() == PluginState.ACTIVATED) {
                    xml.writeEmptyElement("Plugin");
                    xml.writeAttribute("name", plugin.getComponentConfig().getName());
                    xml.writeAttribute("version", plugin.getComponentConfig().getVersion().getFull());
                }
            }
            xml.writeEndElement();
            xml.writeEndDocument();
            xml.flush();
        }
    }
}
