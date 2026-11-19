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

package com.techsenger.greywolf.core.config;

import com.google.errorprone.annotations.concurrent.LazyInit;
import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.shellfx.core.FileStore;
import com.techsenger.weaverbird.core.spi.module.ModuleActivator;
import com.techsenger.weaverbird.core.spi.module.ModuleContext;

/**
 * Base class of the module activators of the Weaverbird components of GreyWolf, the core and the plugins. It gives
 * the component its own config file. Only one activator of a component may extend it, as there is one file for the
 * layer of the component.
 *
 * @author Pavel Castornii
 */
public abstract class AbstractConfigModuleActivator implements ModuleActivator {

    private static final String STORE_FILE_NAME = "config-store.bin";

    @LazyInit
    private LayerConfigPersistence configPersistence;

    @Override
    public void activate(ModuleContext mc) throws Exception {
        var dataPath = mc.getFramework().getPathManager().getPathResolver()
                .resolveDataDirectory(mc.getComponent().getDescriptor().getConfig());
        var file = new FileStore<LayerConfigData>(dataPath.resolve(getStoreFileName()), getClass().getClassLoader(),
                LayerConfigData::new);
        var configManager = GreyWolf.getInstance(mc.getFramework().getContext()).getConfigManager();
        configPersistence = new LayerConfigPersistence(mc.getComponent().getLayer(), file, configManager);
        configPersistence.restore();
    }

    @Override
    public void deactivate(ModuleContext mc) throws Exception {
        configPersistence.save();
    }

    protected String getStoreFileName() {
        return STORE_FILE_NAME;
    }
}
