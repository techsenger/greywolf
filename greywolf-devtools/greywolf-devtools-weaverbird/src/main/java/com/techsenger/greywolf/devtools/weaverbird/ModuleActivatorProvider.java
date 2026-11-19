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

package com.techsenger.greywolf.devtools.weaverbird;

import com.google.errorprone.annotations.concurrent.LazyInit;
import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.core.config.AbstractConfigModuleActivator;
import com.techsenger.greywolf.devtools.weaverbird.registrars.ShellControlRegistrar;
import com.techsenger.shellfx.base.registry.Registration;
import com.techsenger.toolkit.fx.color.ColorUtils;
import com.techsenger.weaverbird.core.spi.module.ModuleContext;
import com.techsenger.weaverbird.gui.settings.DiagramSettings;
import com.techsenger.weaverbird.gui.settings.LayoutEngine;
import com.techsenger.weaverbird.gui.settings.LineType;
import com.techsenger.weaverbird.gui.style.WeaverbirdIconStylesheets;

/**
 *
 * @author Pavel Castornii
 */
public class ModuleActivatorProvider extends AbstractConfigModuleActivator {

    @LazyInit
    private ShellControlRegistrar controlRegistrar;

    @LazyInit
    private Registration stylesheetRegistration;

    @Override
    public void activate(ModuleContext mc) throws Exception {
        super.activate(mc);
        var settings = new DiagramSettings(); // TEMP TEMP TEMP
        settings.setLayoutEngine(LayoutEngine.SMETANA);
        settings.setLimitSize(10000);
        settings.setLineType(LineType.ORTHO);
        var greyWolf = GreyWolf.getInstance(mc.getFramework().getContext());
        var greyWolfContext = greyWolf.getShell().getContext();
        var appearance = greyWolfContext.getSettings().getAppearance();
        settings.setLayerColor(ColorUtils.toColor(appearance.getTheme().getPalette().getAccent3Color()));
        settings.setModuleColor(ColorUtils.toColor(appearance.getTheme().getPalette().getDanger3Color()));
        this.controlRegistrar = new ShellControlRegistrar(greyWolf, settings);
        this.controlRegistrar.register();
        this.stylesheetRegistration = greyWolf.getStylesheetRegistry().register(WeaverbirdIconStylesheets.getAll());
    }

    @Override
    public void deactivate(ModuleContext mc) throws Exception {
        this.controlRegistrar.unregister();
        this.stylesheetRegistration.unregister();
        super.deactivate(mc);
    }
}
