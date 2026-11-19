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

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.storage.base.FileType;
import com.techsenger.greywolf.storage.base.StorageCategory;
import com.techsenger.shellfx.storage.base.FileEntryType;
import com.techsenger.shellfx.storage.base.FileStorageType;
import java.time.Instant;
import java.util.Objects;

/**
 * A recently used file or directory, identified by its URI, last-known name, entry type and {@link FileType}
 * (only for a regular file), plus its owning storage's category, type, and own identity URI - enough to find
 * that exact storage again through the registry without holding a direct, potentially stale reference to a
 * storage instance that may since have been replaced or unregistered, and enough to still show something
 * meaningful (the real name and icon, not a generic placeholder) if that storage currently can't be found.
 * Matching by the storage's own URI (not just its category and type) matters when two storages happen to share
 * a category and type - otherwise one being unreachable could resolve to the other by mistake and wrongly
 * report the file as deleted.
 *
 * <p>{@code visitCount} and {@code lastUsedAt} track usage over time - not for display ordering (entries are
 * always listed newest-{@code lastUsedAt}-first, which the list's own order already reflects), but to score
 * entries against each other when {@link DefaultRecentStorage} must evict one to stay within its per-kind
 * limit.
 *
 * @author Pavel Castornii
 */
record RecentEntry(String uri, String name, FileEntryType entryType, @Nullable FileType type,
        StorageCategory storageCategory, FileStorageType storageType, String storageUri, int visitCount,
        Instant lastUsedAt) {

    /**
     * Builds a {@link RecentEntry}; every value except {@code type} must be set before {@link #build()}.
     */
    static final class Builder {

        private @Nullable String uri;

        private @Nullable String name;

        private @Nullable FileEntryType entryType;

        private @Nullable FileType type;

        private @Nullable StorageCategory storageCategory;

        private @Nullable FileStorageType storageType;

        private @Nullable String storageUri;

        private int visitCount;

        private @Nullable Instant lastUsedAt;

        Builder uri(String uri) {
            this.uri = uri;
            return this;
        }

        Builder name(String name) {
            this.name = name;
            return this;
        }

        Builder entryType(FileEntryType entryType) {
            this.entryType = entryType;
            return this;
        }

        Builder type(@Nullable FileType type) {
            this.type = type;
            return this;
        }

        Builder storageCategory(StorageCategory storageCategory) {
            this.storageCategory = storageCategory;
            return this;
        }

        Builder storageType(FileStorageType storageType) {
            this.storageType = storageType;
            return this;
        }

        Builder storageUri(String storageUri) {
            this.storageUri = storageUri;
            return this;
        }

        Builder visitCount(int visitCount) {
            this.visitCount = visitCount;
            return this;
        }

        Builder lastUsedAt(Instant lastUsedAt) {
            this.lastUsedAt = lastUsedAt;
            return this;
        }

        RecentEntry build() {
            return new RecentEntry(Objects.requireNonNull(uri), Objects.requireNonNull(name),
                    Objects.requireNonNull(entryType), type, Objects.requireNonNull(storageCategory),
                    Objects.requireNonNull(storageType), Objects.requireNonNull(storageUri), visitCount,
                    Objects.requireNonNull(lastUsedAt));
        }
    }

    static Builder builder() {
        return new Builder();
    }
}
