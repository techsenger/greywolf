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

package com.techsenger.greywolf.core.plugin;

/**
 *
 * @author Pavel Castornii
 */
public enum PluginCategory {

    /**
     * Foundational environment tools, such as a navigator, terminal, process manager, resource monitor,
     * services, and environment variables.
     */
    SYSTEM("System"),

    /**
     * File copying, archiving, comparison, search, splitting, directory comparison, indexing, organizing files
     * by type, bookmarked navigation, backup, deduplication, and disk usage.
     */
    TOOLS("Tools"),

    /**
     * Tools for viewing file contents without editing them.
     */
    VIEWERS("Viewers"),

    /**
     * Tools for editing file contents.
     */
    EDITORS("Editors"),

    /**
     * Network file system providers, such as Samba, FTP, SFTP, WebDAV, and cloud storage.
     */
    STORAGES("Storages"),

    /**
     * Tools for inspecting and debugging GreyWolf itself and the frameworks it is built on.
     */
    DEVTOOLS("DevTools");

    private final String text;

    PluginCategory(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
