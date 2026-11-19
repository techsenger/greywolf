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

package com.techsenger.greywolf.system.terminal;

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.core.panels.Placement;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.system.terminal.style.TerminalIcons;
import com.techsenger.greywolf.system.terminal.utils.TerminalUtils;
import com.techsenger.shellfx.core.close.CloseCheckResult;
import com.techsenger.shellfx.core.close.ClosePreparationResult;
import com.techsenger.shellfx.core.tab.AbstractTabViewModel;
import java.util.function.Consumer;
import javafx.beans.property.ReadOnlyObjectWrapper;

/**
 *
 * @author Pavel Castornii
 */
public final class TerminalTabViewModel extends AbstractTabViewModel<TerminalTabComposer>
        implements TerminalTabPort {

    private final String path;

    private final ReadOnlyObjectWrapper<@Nullable Placement> placement = new ReadOnlyObjectWrapper<>();

    public TerminalTabViewModel(TerminalTabParams params) {
        super(params);
        this.path = params.getPath();
    }

    @Override
    public @Nullable Placement getPlacement() {
        return placement.get();
    }

    @Override
    public boolean receive(GreyWolfFile file) {
        var directory = TerminalUtils.getDirectoryPath(file);
        if (directory == null) {
            return false;
        }
        getComposerAccess().getAreaPort().onChangeDirectory(directory);
        return true;
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected TerminalTabConfig getConfig() {
        return (TerminalTabConfig) super.getConfig();
    }

    @Override
    public TerminalTabPort.ComposerAccess getComposerAccess() {
        return getComposer();
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setTitle("Terminal");
        setIcon(TerminalIcons.TERMINAL);
        selectedProperty().addListener((ov, oldV, newV) -> {
            if (newV) {
                requestFocus();
            }
        });
        placement.addListener((ov, oldV, newV) -> getComposer().getToolBarPort().setPlacement(newV));
    }

    String getPath() {
        return this.path;
    }

    ReadOnlyObjectWrapper<@Nullable Placement> placementWrapper() {
        return placement;
    }
}
