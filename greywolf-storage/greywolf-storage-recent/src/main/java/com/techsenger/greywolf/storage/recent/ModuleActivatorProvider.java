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

import com.techsenger.weaverbird.core.spi.module.ModuleActivator;
import com.techsenger.weaverbird.core.spi.module.ModuleContext;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Restores {@link DefaultRecentStorage}'s tracked entries from its data file on activation, and saves them
 * back on deactivation - implements {@link ModuleActivator} directly, rather than through
 * {@code greywolf-core}'s plugin base class, since this module has no UI contribution and shouldn't need a
 * dependency on greywolf-core just to resolve its own data directory.
 *
 * @author Pavel Castornii
 */
public final class ModuleActivatorProvider implements ModuleActivator {

    private static final String DATA_FILE_NAME = "recent-storage.xml";

    @Override
    public void activate(ModuleContext context) throws Exception {
        var file = resolveDataFile(context);
        if (Files.exists(file)) {
            RecentStorageProvider.getInstance().restore(file);
        }
    }

    @Override
    public void deactivate(ModuleContext context) throws Exception {
        var file = resolveDataFile(context);
        Files.createDirectories(file.getParent());
        RecentStorageProvider.getInstance().save(file);
    }

    private Path resolveDataFile(ModuleContext context) {
        var dataPath = context.getFramework().getPathManager().getPathResolver()
                .resolveDataDirectory(context.getComponent().getDescriptor().getConfig());
        return dataPath.resolve(DATA_FILE_NAME);
    }
}
