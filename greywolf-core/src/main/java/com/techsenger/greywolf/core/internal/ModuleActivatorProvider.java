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

package com.techsenger.greywolf.core.internal;

import com.techsenger.greywolf.core.config.AbstractConfigModuleActivator;
import com.techsenger.weaverbird.core.spi.module.ModuleContext;
import javafx.application.Platform;

/**
 *
 * @author Pavel Castornii
 */
public class ModuleActivatorProvider extends AbstractConfigModuleActivator {

    @Override
    public void activate(ModuleContext context) throws Exception {
        // Fonts must be registered before the GreyWolf creates its settings, which look the font families up.
        GreyWolfApplication.loadFonts();
        // Activators activated after this one can access the GreyWolf instance
        GreyWolfImpl.create(context.getFramework(), context.getComponent().getDescriptor());
        // The config store of the core is added to the config manager of the GreyWolf like the stores of plugins
        super.activate(context);
        // JavaFX Application.launch() blocks the calling thread until the application exits.
        // To prevent blocking the activator, we start it in a separate not daemon thread.
        var thread = new Thread(() -> {
            GreyWolfImpl.launch();
            context.getFramework().shutdown();
        });
        thread.start();
    }

    @Override
    public void deactivate(ModuleContext mc) throws Exception {
        super.deactivate(mc);
        Platform.exit();
    }

}
