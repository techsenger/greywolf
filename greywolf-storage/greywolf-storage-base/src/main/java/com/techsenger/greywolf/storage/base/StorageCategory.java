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

/**
 * Classifies how the storages of the file storage registry were obtained.
 *
 * @author Pavel Castornii
 */
public enum StorageCategory {

    /**
     * Storages discovered from the OS (e.g. local disk volumes) and kept up to date as the drives change.
     */
    SYSTEM,

    /**
     * Storages contributed once and kept for the registry's lifetime (e.g. a future FTP or cloud drive provider).
     */
    CUSTOM,

    /**
     * The single storage retrievable via {@code FileStorageRegistry#getRecentStorage()}.
     */
    RECENT
}
