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

import java.net.URI;

/**
 * Converts between the URI of a storage entry and the human-readable text of its path (e.g. a native OS path for a
 * local storage), which is what the user sees and types into a location bar or a target-path field. The syntax of
 * the text is up to the storage that provides the converter.
 *
 * @author Pavel Castornii
 */
public interface LocationConverter {

    /**
     * Converts the URI of an entry of the storage to the text of its location.
     *
     * @param uri the URI to convert, must belong to the storage
     * @return the text, never {@code null}
     */
    String toLocation(URI uri);

    /**
     * Parses a location text, e.g. typed by the user, into a URI. A text that ends with a path separator denotes a
     * directory, so the path of the result ends with {@code /}. The result is not checked against the storage, so
     * it may refer to an entry that doesn't exist.
     *
     * @param text the text to parse
     * @return the URI, never {@code null}
     * @throws FormatException if {@code text} is not a valid path for the storage
     */
    URI fromLocation(String text) throws FormatException;
}
