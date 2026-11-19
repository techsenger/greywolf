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
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.greywolf.storage.base.style.BaseStorageIcons;
import com.techsenger.shellfx.base.icon.FontIcon;
import dev.dirs.UserDirectories;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The current user's well-known personal directories (Home, Desktop, Documents, Downloads, Music, Pictures,
 * Videos), resolved once via {@link UserDirectories} - the single place that knows this set, so every other
 * concern that cares about it (icons, the Places sidebar, excluding them from Recent, ...) stays in sync
 * without each re-resolving it independently.
 *
 * @author Pavel Castornii
 */
public final class PersonalDirectories {

    /**
     * In the order the directories are listed in {@link #build()}. If two of them are the same directory (e.g.
     * Documents set to the home directory), the one that comes first is kept.
     */
    private static final Map<URI, PersonalDirectory> DIRECTORIES_BY_URI = build();

    /**
     * Returns every personal directory the OS reported for the current user; one a given platform doesn't
     * report (e.g. no {@code Music} folder) is simply absent, not represented by a placeholder.
     *
     * A directory that is the same as one listed earlier is left out.
     *
     * @return an unmodifiable collection, never {@code null}, may be empty.
     */
    public static @Unmodifiable Collection<PersonalDirectory> getAll() {
        return DIRECTORIES_BY_URI.values();
    }

    /**
     * Returns the personal directories keyed by their own URI, in the order of {@link #getAll()}.
     *
     * @return an unmodifiable map, never {@code null}, may be empty.
     */
    public static @Unmodifiable Map<URI, PersonalDirectory> getDirectoriesByUri() {
        return DIRECTORIES_BY_URI;
    }

    /**
     * Returns whether {@code uri} is exactly one of {@link #getAll()}'s own directories - not whether it is
     * merely located under one of them.
     *
     * @param uri the URI to check.
     * @return {@code true} if {@code uri} is a personal directory's own URI.
     */
    public static boolean isPersonalDirectory(URI uri) {
        return DIRECTORIES_BY_URI.containsKey(uri);
    }

    private static Map<URI, PersonalDirectory> build() {
        var userDirs = UserDirectories.get();
        var result = new LinkedHashMap<URI, PersonalDirectory>();
        addIfPresent(result, PersonalDirectoryType.HOME, userDirs.homeDir, BaseStorageIcons.HOME_FOLDER);
        addIfPresent(result, PersonalDirectoryType.DESKTOP, userDirs.desktopDir, BaseStorageIcons.DESKTOP_FOLDER);
        addIfPresent(result, PersonalDirectoryType.DOCUMENTS, userDirs.documentDir,
                BaseStorageIcons.DOCUMENTS_FOLDER);
        addIfPresent(result, PersonalDirectoryType.DOWNLOADS, userDirs.downloadDir,
                BaseStorageIcons.DOWNLOADS_FOLDER);
        addIfPresent(result, PersonalDirectoryType.MUSIC, userDirs.audioDir, BaseStorageIcons.MUSIC_FOLDER);
        addIfPresent(result, PersonalDirectoryType.PICTURES, userDirs.pictureDir,
                BaseStorageIcons.PICTURES_FOLDER);
        addIfPresent(result, PersonalDirectoryType.VIDEOS, userDirs.videoDir, BaseStorageIcons.VIDEOS_FOLDER);
        return Collections.unmodifiableMap(result);
    }

    private static void addIfPresent(Map<URI, PersonalDirectory> result, PersonalDirectoryType type,
            @Nullable String rawPath, FontIcon<?> icon) {
        if (rawPath != null) {
            var uri = Path.of(rawPath).toUri();
            result.putIfAbsent(uri, new PersonalDirectory(type, uri, icon));
        }
    }

    private PersonalDirectories() {
        // empty
    }
}
