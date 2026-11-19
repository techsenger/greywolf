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

package com.techsenger.greywolf.system.navigator.internal;

import com.google.errorprone.annotations.concurrent.LazyInit;
import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.core.config.AbstractConfigModuleActivator;
import com.techsenger.greywolf.system.navigator.registrars.ActionRegistrar;
import com.techsenger.greywolf.system.navigator.registrars.AreaControlRegistrar;
import com.techsenger.greywolf.system.navigator.registrars.AreaSlotRegistrar;
import com.techsenger.greywolf.system.navigator.registrars.BundleRegistrar;
import com.techsenger.greywolf.system.navigator.registrars.DirectoryTreeControlRegistrar;
import com.techsenger.greywolf.system.navigator.registrars.LocationBarControlRegistrar;
import com.techsenger.greywolf.system.navigator.registrars.ShellControlRegistrar;
import com.techsenger.greywolf.system.navigator.registrars.ShellSlotRegistrar;
import com.techsenger.greywolf.system.navigator.registrars.ToolBarControlRegistrar;
import com.techsenger.greywolf.system.navigator.registrars.ToolBarSlotRegistrar;
import com.techsenger.greywolf.system.navigator.registrars.WorkspaceControlRegistrar;
import com.techsenger.shellfx.base.registrar.RegistrarManager;
import com.techsenger.weaverbird.core.spi.module.ModuleContext;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public class ModuleActivatorProvider extends AbstractConfigModuleActivator {

    @LazyInit
    private RegistrarManager registrarManager;

    @Override
    public void activate(ModuleContext mc) throws Exception {
        super.activate(mc);
        var greyWolf = GreyWolf.getInstance(mc.getFramework().getContext());
        registrarManager = new RegistrarManager(List.of(
                new BundleRegistrar(greyWolf),
                new ActionRegistrar(greyWolf),
                new ShellSlotRegistrar(greyWolf),
                new ToolBarSlotRegistrar(greyWolf),
                new AreaSlotRegistrar(greyWolf),
                new ShellControlRegistrar(greyWolf),
                new WorkspaceControlRegistrar(greyWolf),
                new ToolBarControlRegistrar(greyWolf),
                new AreaControlRegistrar(greyWolf),
                new LocationBarControlRegistrar(greyWolf),
                new DirectoryTreeControlRegistrar(greyWolf)));
        registrarManager.registerAll();
    }

    @Override
    public void deactivate(ModuleContext mc) throws Exception {
        registrarManager.unregisterAll();
        super.deactivate(mc);
    }
}
