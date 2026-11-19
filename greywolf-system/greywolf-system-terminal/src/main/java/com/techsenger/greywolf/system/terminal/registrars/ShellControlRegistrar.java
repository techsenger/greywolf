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

package com.techsenger.greywolf.system.terminal.registrars;

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.core.GreyWolfShellView;
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfControlRegistrar;
import com.techsenger.greywolf.core.slots.GreyWolfShellSlots;
import com.techsenger.greywolf.storage.base.StorageCategory;
import com.techsenger.greywolf.storage.ui.view.FileView;
import com.techsenger.greywolf.storage.ui.view.FileViewProvider;
import com.techsenger.greywolf.system.terminal.style.TerminalIcons;
import com.techsenger.shellfx.core.registries.SimpleControlProvider;
import com.techsenger.shellfx.material.icon.FontIconView;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.MenuItem;

/**
 * Registers the controls the Terminal plugin contributes to the GreyWolf shell - the &quot;Terminal&quot; item in the
 * New group of the File menu and the &quot;Run in Terminal&quot; and &quot;Open in Terminal&quot; items in the same
 * menu, which act on the file view that the selected top-level tab provides, if it provides one.
 *
 * @author Pavel Castornii
 */
public class ShellControlRegistrar extends AbstractGreyWolfControlRegistrar {

    private static ObservableValue<@Nullable FileView<?>> observeFileView(GreyWolfShellView shell) {
        var workspace = shell.getComposer().getWorkspace();
        return workspace.getComposer().selectedTabProperty().flatMap(tab -> {
            if (tab != null && tab.getComposer() instanceof FileViewProvider provider) {
                return provider.observeFileView();
            }
            return new SimpleObjectProperty<@Nullable FileView<?>>();
        });
    }

    public ShellControlRegistrar(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void register() {
        registerTerminalItem();
        registerRunInTerminalItem();
        registerOpenInTerminalItem();
    }

    private void registerTerminalItem() {
        register(GreyWolfShellSlots.FileMenu.NEW_GROUP, 200, () -> {
            var item = new MenuItem("Terminal", new FontIconView(TerminalIcons.TERMINAL));
            item.setOnAction(new TerminalItemHandler(getGreyWolf()));
            return new SimpleControlProvider<>(item);
        });
    }

    private void registerRunInTerminalItem() {
        register(GreyWolfShellSlots.FileMenu.RUN_GROUP, 200, () -> new SimpleControlProvider<GreyWolfShellView,
                MenuItem>(new MenuItem("Run in _Terminal", new FontIconView(TerminalIcons.TERMINAL))) {

            @Override
            public void initialize(GreyWolfShellView shell) {
                super.initialize(shell);
                var fileView = observeFileView(shell);
                getControl().visibleProperty().bind(fileView
                        .flatMap(view -> view.getViewModel().fileProperty())
                        .map(file -> file.isRunnable())
                        .orElse(false));
                getControl().setOnAction(new RunInTerminalItemHandler(getGreyWolf(), fileView::getValue));
            }

            @Override
            public void deinitialize(GreyWolfShellView shell) {
                super.deinitialize(shell);
                getControl().visibleProperty().unbind();
                getControl().setOnAction(null);
            }
        });
    }

    private void registerOpenInTerminalItem() {
        register(GreyWolfShellSlots.FileMenu.OPEN_GROUP, 400, () -> new SimpleControlProvider<GreyWolfShellView,
                MenuItem>(new MenuItem("Open in _Terminal", new FontIconView(TerminalIcons.TERMINAL))) {

            @Override
            public void initialize(GreyWolfShellView shell) {
                super.initialize(shell);
                var fileView = observeFileView(shell);
                getControl().visibleProperty().bind(fileView
                        .flatMap(view -> view.getViewModel().fileProperty())
                        .map(file -> file.isDirectory() && file.getStorage().getCategory() == StorageCategory.SYSTEM)
                        .orElse(false));
                getControl().setOnAction(new OpenInTerminalItemHandler(getGreyWolf(), fileView::getValue));
            }

            @Override
            public void deinitialize(GreyWolfShellView shell) {
                super.deinitialize(shell);
                getControl().visibleProperty().unbind();
                getControl().setOnAction(null);
            }
        });
    }
}
