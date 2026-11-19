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

import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfControlRegistrar;
import com.techsenger.greywolf.storage.base.StorageCategory;
import com.techsenger.greywolf.storage.ui.slots.FileViewSlots;
import com.techsenger.greywolf.storage.ui.view.FileView;
import com.techsenger.greywolf.system.terminal.style.TerminalIcons;
import com.techsenger.shellfx.core.registries.SimpleControlProvider;
import com.techsenger.shellfx.material.icon.FontIconView;
import javafx.scene.control.MenuItem;

/**
 * Registers the controls the Terminal plugin contributes to the components that show files - the &quot;Open in
 * Terminal&quot; and &quot;Run in Terminal&quot; items, which only the Terminal plugin can create.
 *
 * @author Pavel Castornii
 */
public class FileViewControlRegistrar extends AbstractGreyWolfControlRegistrar {

    public FileViewControlRegistrar(GreyWolf greyWolf) {
        super(greyWolf);
    }

    @Override
    public void register() {
        register(FileViewSlots.RUN_GROUP, 200, () -> new SimpleControlProvider<FileView<?>, MenuItem>(
                new MenuItem("Run in _Terminal", new FontIconView(TerminalIcons.TERMINAL))) {

            @Override
            public void initialize(FileView<?> view) {
                super.initialize(view);
                var file = view.getViewModel().fileProperty();
                getControl().visibleProperty().bind(file.map(f -> f.isRunnable()));
                getControl().setOnAction(new RunInTerminalItemHandler(getGreyWolf(), () -> view));
            }

            @Override
            public void deinitialize(FileView<?> view) {
                super.deinitialize(view);
                getControl().visibleProperty().unbind();
                getControl().setOnAction(null);
            }
        });

        register(FileViewSlots.OPEN_GROUP, 400, () -> new SimpleControlProvider<FileView<?>, MenuItem>(
                new MenuItem("Open in _Terminal", new FontIconView(TerminalIcons.TERMINAL))) {

            @Override
            public void initialize(FileView<?> view) {
                super.initialize(view);
                var file = view.getViewModel().fileProperty();
                getControl().visibleProperty().bind(file.map(
                        f -> f.isDirectory() && f.getStorage().getCategory() == StorageCategory.SYSTEM));
                getControl().setOnAction(new OpenInTerminalItemHandler(getGreyWolf(), () -> view));
            }

            @Override
            public void deinitialize(FileView<?> view) {
                super.deinitialize(view);
                getControl().visibleProperty().unbind();
                getControl().setOnAction(null);
            }
        });
    }
}
