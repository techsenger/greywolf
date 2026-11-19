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

package com.techsenger.greywolf.storage.base.spi;

import com.techsenger.greywolf.storage.base.RecentStorage;

/**
 * SPI for contributing the recent-files storage to the file storage registry.
 *
 * @author Pavel Castornii
 */
public interface RecentStorageService {

    /**
     * Returns the recent-files storage, which is always the same instance.
     *
     * @return the storage, never {@code null}
     */
    RecentStorage getStorage();
}
