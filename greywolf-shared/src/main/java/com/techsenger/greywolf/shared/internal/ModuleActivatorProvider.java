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

package com.techsenger.greywolf.shared.internal;

import com.google.errorprone.annotations.concurrent.LazyInit;
import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.shared.registrars.DirectoryTreeControlRegistrar;
import com.techsenger.greywolf.shared.registrars.DirectoryTreeSlotRegistrar;
import com.techsenger.greywolf.shared.registrars.LocationBarControlRegistrar;
import com.techsenger.greywolf.shared.registrars.LocationBarSlotRegistrar;
import com.techsenger.shellfx.base.registrar.RegistrarManager;
import com.techsenger.weaverbird.core.spi.module.ModuleActivator;
import com.techsenger.weaverbird.core.spi.module.ModuleContext;
import java.util.List;

/**
 * Registers the context menus of the location bar and of the directory tree. It must be activated after the core
 * module, which creates the GreyWolf instance.
 *
 * @author Pavel Castornii
 */
public class ModuleActivatorProvider implements ModuleActivator {

    @LazyInit
    private RegistrarManager registrarManager;

    @Override
    public void activate(ModuleContext mc) throws Exception {
        var greyWolf = GreyWolf.getInstance(mc.getFramework().getContext());
        registrarManager = new RegistrarManager(List.of(
                new LocationBarSlotRegistrar(greyWolf),
                new DirectoryTreeSlotRegistrar(greyWolf),
                new LocationBarControlRegistrar(greyWolf),
                new DirectoryTreeControlRegistrar(greyWolf)));
        registrarManager.registerAll();
    }

    @Override
    public void deactivate(ModuleContext mc) throws Exception {
        registrarManager.unregisterAll();
    }
}
