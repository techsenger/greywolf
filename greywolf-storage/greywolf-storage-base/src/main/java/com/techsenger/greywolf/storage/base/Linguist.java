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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.yaml.snakeyaml.Yaml;

/**
 * Classifies files as source code using GitHub Linguist's language definitions.
 * <p>
 * Matches by extension or by exact filename, since some files are recognized by name alone (e.g. Dockerfile,
 * Makefile, .gitignore).
 *
 * @author Pavel Castornii
 */
final class Linguist {

    /**
     * Language names excluded from classification: catch-all buckets for files with no real format (e.g.
     * {@code .txt}, {@code README}, {@code LICENSE}) rather than a genuine code/markup/data format, so they
     * resolve to text instead of code.
     */
    private static final Set<String> EXCLUDED_LANGUAGE_NAMES = Set.of("Text", "Vim Help File", "Adblock Filter List");

    private final Set<String> extensions;

    private final Set<String> filenames;

    /**
     * Loads the classifier from a languages.yml resource on the classpath.
     */
    @SuppressWarnings("unchecked")
    Linguist() {
        String resourceName = "languages.yml";
        Map<String, Object> root;
        try (InputStream input = getClass().getResourceAsStream(resourceName)) {
            if (input == null) {
                throw new IllegalArgumentException("Resource not found: " + resourceName);
            }
            root = new Yaml().load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read resource: " + resourceName, e);
        }

        Set<String> extensionSet = new LinkedHashSet<>();
        Set<String> filenameSet = new LinkedHashSet<>();

        for (Map.Entry<String, Object> languageEntry : root.entrySet()) {
            if (EXCLUDED_LANGUAGE_NAMES.contains(languageEntry.getKey())) {
                continue;
            }

            Map<String, Object> entry = (Map<String, Object>) languageEntry.getValue();

            Object rawExtensions = entry.get("extensions");
            if (rawExtensions instanceof List<?> list) {
                for (Object ext : list) {
                    extensionSet.add(normalizeExtension((String) ext));
                }
            }

            Object rawFilenames = entry.get("filenames");
            if (rawFilenames instanceof List<?> list) {
                for (Object name : list) {
                    filenameSet.add((String) name);
                }
            }
        }

        this.extensions = extensionSet;
        this.filenames = filenameSet;
    }

    /**
     * Matches by exact file name or by extension, per the Linguist language definitions.
     *
     * @param fileName file name, may or may not include a path
     * @param extension the file's extension
     */
    public boolean isCode(String fileName, @Nullable String extension) {
        String name = Path.of(fileName).getFileName().toString();

        if (filenames.contains(name)) {
            return true;
        }
        return extension != null && extensions.contains(extension);
    }

    private static String normalizeExtension(String extension) {
        String stripped = extension.startsWith(".") ? extension.substring(1) : extension;
        return stripped.toLowerCase(Locale.ROOT);
    }
}
