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

package com.techsenger.greywolf.system.terminal.area;

import com.google.errorprone.annotations.concurrent.LazyInit;
import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.core.GreyWolfSettings;
import com.techsenger.greywolf.system.terminal.TerminalTabPort;
import com.techsenger.greywolf.system.terminal.find.FindPanelPort;
import com.techsenger.jeditermfx.core.util.Platform;
import com.techsenger.shellfx.core.area.AbstractAreaViewModel;
import com.techsenger.shellfx.core.config.ConfigUtils;
import com.techsenger.shellfx.core.settings.SettingsListener;
import com.techsenger.shellfx.material.theme.Theme;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author Pavel Castornii
 */
public class TerminalAreaViewModel extends AbstractAreaViewModel<TerminalAreaComposer>
        implements TerminalAreaPort {

    private final GreyWolfSettings settings;

    private final TerminalTabPort tab;

    private final String path;

    private final ObservableSource<Void> createTerminalSource = new SimpleObservableSource<>();

    private final ObservableSource<TerminalCommand> commandSource = new SimpleObservableSource<>();

    private final ObjectProperty<TerminalPaletteType> paletteType = new SimpleObjectProperty<>();

    @LazyInit
    private TerminalPalette terminalPalette;

    @LazyInit
    private PtyProcessTtyConnector ttyConnector;

    @LazyInit
    private TerminalSettingsProvider settingsProvider;

    private final ReadOnlyStringWrapper selectedText = new ReadOnlyStringWrapper();

    private final SettingsListener settingsListener = this::applySettings;

    @LazyInit
    private Theme appliedTheme;

    public TerminalAreaViewModel(TerminalAreaParams params) {
        super(params);
        this.settings = params.getSettings();
        this.tab = params.getTab();
        this.path = params.getPath();
    }

    @Override
    public void onAddNew() {
        tab.getComposerAccess().openNewTerminal(path);
    }

    @Override
    public void onCommand(TerminalCommand command) {
        commandSource.next(command);
        requestFocus();
    }

    @Override
    public void onChangeDirectory(String path) {
        String command;
        if (Platform.isWindows()) {
            command = "cd /d \"" + path + "\"\r";
        } else {
            command = "cd '" + path.replace("'", "'\\''") + "'\r";
        }
        try {
            ttyConnector.write(command);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void onRunFile(String path) {
        String command;
        if (Platform.isWindows()) {
            command = "\"" + path + "\"\r";
        } else {
            command = "'" + path.replace("'", "'\\''") + "'\r";
        }
        try {
            ttyConnector.write(command);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public TerminalPaletteType getPaletteType() {
        return this.paletteType.get();
    }

    @Override
    public void setPaletteType(TerminalPaletteType type) {
        this.paletteType.set(type);
    }

    @Override
    public ObjectProperty<TerminalPaletteType> paletteTypeProperty() {
        return this.paletteType;
    }

    @Override
    public @Nullable String getSelectedText() {
        return this.selectedText.get();
    }

    @Override
    public ReadOnlyStringProperty selectedTextProperty() {
        return this.selectedText.getReadOnlyProperty();
    }

    @Override
    public void onShowFind() {
        var composer = getComposer();
        FindPanelPort findPanel = composer.getFindPanel();
        if (findPanel == null) {
            findPanel = composer.openFindPanel(getConfig().getFindPanel());
            findPanel.setOnCloseRequest(() -> onHideFind());
        }
        var text = selectedText.get();
        if (text != null) {
            findPanel.setEditedFindText(text);
        }
        findPanel.requestFocus();
    }

    public TerminalSettingsProvider getSettingsProvider() {
        return settingsProvider;
    }

    public PtyProcessTtyConnector getTtyConnector() {
        return ttyConnector;
    }

    @Override
    protected TerminalAreaConfig getConfig() {
        return (TerminalAreaConfig) super.getConfig();
    }

    @Override
    protected void loadStateFromConfig() {
        super.loadStateFromConfig();
        setPaletteType(getConfig().getPaletteType());
    }

    @Override
    protected void observeStateForConfig() {
        super.observeStateForConfig();
        ConfigUtils.observe(this.paletteType, getConfig(), TerminalAreaConfig::setPaletteType);
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        this.ttyConnector = createTtyConnector(path);
        this.settingsProvider = createSettingsProvider();
        this.paletteType.addListener((ov, oldV, newV) -> {
            terminalPalette.setPaletteType(newV);
            requestFocus();
        });
        createTerminalSource.next(null);
        settings.addListener(settingsListener);
    }

    @Override
    protected void postDeinitialize() {
        super.postDeinitialize();
        this.ttyConnector.close();
        settings.removeListener(settingsListener);
    }

    protected void onOpenLink(String url) {
//        var settings = tab.getShell().getContext().getSettings();
//        if (settings.getWebManager().isUsedByDefault()) {
//            getComposer().addWebManager(url);
//        } else {
//            WebManager.open(url);
//        }
    }

    protected void onHideFind() {
        var composer = getComposer();
        if (composer.getFindPanel() == null) {
            return;
        }
        composer.closeFindPanel();
        requestFocus();
    }

    ObservableSource<Void> createTerminalSource() {
        return createTerminalSource;
    }

    ObservableSource<TerminalCommand> commandSource() {
        return commandSource;
    }

    ReadOnlyStringWrapper selectedTextWrapper() {
        return selectedText;
    }

    private TerminalSettingsProvider createSettingsProvider() {
        this.appliedTheme = settings.getAppearance().getTheme();
        this.terminalPalette = new TerminalPalette(appliedTheme, getPaletteType());
        return new TerminalSettingsProvider(settings.getAppearance().getMonospaceFont(), terminalPalette);
    }

    private PtyProcessTtyConnector createTtyConnector(String directory) {
        try {
            Map<String, String> envs = System.getenv();
            String[] command;
            if (Platform.isWindows()) {
                command = new String[]{"cmd.exe"};
            } else {
                command = new String[]{"/bin/bash", "--login"};
                envs = new HashMap<>(System.getenv());
                envs.put("TERM", "xterm-256color");
            }
            PtyProcess process = new PtyProcessBuilder()
                    .setDirectory(directory)
                    .setCommand(command).setEnvironment(envs).start();
            return new PtyProcessTtyConnector(process, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void applySettings() {
        var theme = settings.getAppearance().getTheme();
        if (theme != appliedTheme) {
            appliedTheme = theme;
            terminalPalette.setTheme(theme);
            requestFocus();
        }
    }
}
