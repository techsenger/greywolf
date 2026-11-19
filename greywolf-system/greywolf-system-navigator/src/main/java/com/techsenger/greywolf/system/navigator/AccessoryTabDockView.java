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

package com.techsenger.greywolf.system.navigator;

import com.techsenger.shellfx.layout.dockhost.TabDockComposer;
import com.techsenger.shellfx.layout.dockhost.TabDockView;
import com.techsenger.shellfx.layout.dockhost.TabDockViewModel;

/**
 * A {@code TabDock} that can exist without an owning {@code DockHost}, so an accessory panel can be shown
 * outside a full docking layout until it is later promoted into one.
 *
 * @author Pavel Castornii
 */
final class AccessoryTabDockView extends TabDockView<TabDockViewModel<TabDockComposer>> {

    final class Composer extends TabDockView<TabDockViewModel<TabDockComposer>>.Composer {

        @Override
        public void close() {
            if (getDockHost() == null) {
                onEmbeddedClose.run();
            } else {
                super.close();
            }
        }
    }

    private final Runnable onEmbeddedClose;

    AccessoryTabDockView(TabDockViewModel<TabDockComposer> viewModel, Runnable onEmbeddedClose) {
        super(viewModel);
        this.onEmbeddedClose = onEmbeddedClose;
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new Composer();
    }
}
