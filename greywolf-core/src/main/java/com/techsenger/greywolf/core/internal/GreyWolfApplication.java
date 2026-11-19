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

import com.google.errorprone.annotations.concurrent.LazyInit;
import com.techsenger.greywolf.core.GreyWolfShellConfig;
import com.techsenger.greywolf.core.GreyWolfShellParams;
import com.techsenger.greywolf.core.GreyWolfShellView;
import com.techsenger.greywolf.core.GreyWolfShellViewModel;
import com.techsenger.greywolf.core.registrars.PanelControlRegistrar;
import com.techsenger.greywolf.core.registrars.PanelSlotRegistrar;
import com.techsenger.greywolf.core.registrars.ShellControlRegistrar;
import com.techsenger.greywolf.core.registrars.ShellSlotRegistrar;
import com.techsenger.greywolf.core.registrars.WorkspaceControlRegistrar;
import com.techsenger.greywolf.core.registrars.WorkspaceSlotRegistrar;
import com.techsenger.greywolf.core.workspace.WorkspaceView;
import com.techsenger.greywolf.core.workspace.WorkspaceViewModel;
import com.techsenger.fontjars.inter.Inter;
import com.techsenger.fontjars.jetbrainsmono.JetBrainsMono;
import com.techsenger.shellfx.base.registrar.RegistrarManager;
import com.techsenger.shellfx.core.area.AreaParams;
import com.techsenger.shellfx.core.window.AbstractWindowView;
import com.techsenger.shellfx.icons.Fonts;
import com.techsenger.shellfx.icons.IconStylesheetFactory;
import com.techsenger.shellfx.material.icon.FontIconView;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public class GreyWolfApplication extends Application {

    private static final Logger logger = LoggerFactory.getLogger(GreyWolfApplication.class);

    private static final double FONT_SIZE = 14;

    @LazyInit
    private static GreyWolfShellView shell;

    static GreyWolfShellView getShell() {
        return shell;
    }

    /**
     * Registers the bundled Inter and JetBrains Mono fonts. A font that cannot be loaded is only logged, the GreyWolf
     * then keeps the system fonts.
     */
    static void loadFonts() {
        var urls = Stream.concat(
                Arrays.stream(Inter.values()).map(Inter::getUrl),
                Arrays.stream(JetBrainsMono.values()).map(JetBrainsMono::getUrl)).toList();
        for (var url : urls) {
            if (Font.loadFont(url.toExternalForm(), FONT_SIZE) == null) {
                logger.warn("Font {} could not be loaded", url);
            }
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
            logger.error("Uncaught exception in {}", thread.getName(), throwable)
        );
        var greyWolf = GreyWolfImpl.getInstance();
        shell = createShell(stage, greyWolf);
        var workspace = createWorkspace();
        shell.getComposer().addWorkspace(workspace);
        new RegistrarManager(List.of(
                new ShellSlotRegistrar(greyWolf),
                new PanelSlotRegistrar(greyWolf),
                new WorkspaceSlotRegistrar(greyWolf),
                new ShellControlRegistrar(greyWolf),
                new PanelControlRegistrar(greyWolf),
                new WorkspaceControlRegistrar(greyWolf)))
                .registerAll();
        shell.getViewModel().onPluginsUpdated();
        shell.getViewModel().setVisible(true);
    }

    private GreyWolfShellView createShell(Stage stage, GreyWolfImpl greyWolf) {
        // application stylesheets
        var defaultIconFont = Fonts.MATERIAL_DESIGN_ICONS;
        FontIconView.setDefaultIconFamily(defaultIconFont.getFamily());
        var stylesheetRegistry = greyWolf.getStylesheetRegistry();
        stylesheetRegistry.register(AbstractWindowView.createDefaultStylesheets());
        stylesheetRegistry.register(IconStylesheetFactory.forDefaultFont(defaultIconFont));
        stylesheetRegistry.register(IconStylesheetFactory.forAllModules());

        // greyWolf is the context of the shellfx
        greyWolf.setHostServices(getHostServices());
        var config = greyWolf.getConfigManager().getOrCreateConfig(GreyWolfShellConfig.class,
                GreyWolfShellConfig::new);
        var params = new GreyWolfShellParams(config, greyWolf);
        var viewModel = new GreyWolfShellViewModel(params);
        var view = new GreyWolfShellView(viewModel, greyWolf, stage);
        view.initialize();
        viewModel.setTitle("Techsenger GreyWolf");
        return view;
    }

    private WorkspaceView createWorkspace() {
        var params = new AreaParams(null);
        var viewModel = new WorkspaceViewModel(params);
        var view = new WorkspaceView(viewModel, shell.getContext());
        view.initialize();
        return view;
    }
}
