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

package com.techsenger.greywolf.storage.system;

import com.techsenger.greywolf.storage.base.FormatException;
import com.techsenger.greywolf.storage.base.LocationConverter;
import java.net.URI;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

/**
 * Converts between URIs and native paths of the file system of the platform.
 *
 * @author Pavel Castornii
 */
final class SystemLocationConverter implements LocationConverter {

    @Override
    public String toLocation(URI uri) {
        return Paths.get(uri).toString();
    }

    @Override
    public URI fromLocation(String text) throws FormatException {
        try {
            var path = Paths.get(text);
            var uri = path.toUri();
            // a path drops its trailing separator, but the user's text with it can only denote a directory
            var separator = path.getFileSystem().getSeparator();
            if ((text.endsWith("/") || text.endsWith(separator)) && !uri.getPath().endsWith("/")) {
                return URI.create(uri.toString() + "/");
            }
            return uri;
        } catch (InvalidPathException ex) {
            throw new FormatException("'" + text + "' is not a valid path.", ex);
        }
    }
}
