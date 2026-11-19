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

package com.techsenger.greywolf.devtools.shellfx;

import com.google.errorprone.annotations.concurrent.LazyInit;
import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.core.config.AbstractConfigModuleActivator;
import com.techsenger.greywolf.devtools.shellfx.registrars.ShellControlRegistrar;
import com.techsenger.weaverbird.core.spi.module.ModuleContext;

/**
 *
 * @author Pavel Castornii
 */
public class ModuleActivatorProvider extends AbstractConfigModuleActivator  {

    @LazyInit
    private ShellControlRegistrar controlRegistrar;

    @Override
    public void activate(ModuleContext mc) throws Exception {
        super.activate(mc);
        var greyWolf = GreyWolf.getInstance(mc.getFramework().getContext());
        this.controlRegistrar = new ShellControlRegistrar(greyWolf);
        this.controlRegistrar.register();
    }

    @Override
    public void deactivate(ModuleContext mc) throws Exception {
        this.controlRegistrar.unregister();
        super.deactivate(mc);
    }
}
