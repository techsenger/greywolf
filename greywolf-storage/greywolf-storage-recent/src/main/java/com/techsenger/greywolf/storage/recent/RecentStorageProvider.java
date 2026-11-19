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

import com.techsenger.greywolf.storage.base.RecentStorage;
import com.techsenger.greywolf.storage.base.spi.RecentStorageService;

/**
 * Contributes the single {@link DefaultRecentStorage} instance to the file storage registry. The instance is static,
 * not per provider instance (the service loader creates a new one at every load), so {@link ModuleActivatorProvider}
 * - which has no other way to reach it - can restore and save it. The static state is per class loader, which is
 * what a module layer is.
 *
 * @author Pavel Castornii
 */
public final class RecentStorageProvider implements RecentStorageService {

    private static final DefaultRecentStorage INSTANCE = new DefaultRecentStorage();

    static DefaultRecentStorage getInstance() {
        return INSTANCE;
    }

    @Override
    public RecentStorage getStorage() {
        return INSTANCE;
    }
}
