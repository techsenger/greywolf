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
import com.techsenger.greywolf.storage.ui.view.FileRecipient;
import com.techsenger.greywolf.system.terminal.area.TerminalAreaPort;
import com.techsenger.shellfx.core.tab.TabPort;

/**
 * Provides full access to the component's client API.
 *
 * @author Pavel Castornii
 */
public interface TerminalTabPort extends TabPort, FileRecipient {

    interface ComposerAccess extends TabPort.ComposerAccess {

        /**
         * Opens a new terminal tab next to this one, in the same container.
         *
         * @param path the initial directory of the new terminal.
         */
        void openNewTerminal(String path);

        /**
         * Returns the port of the area of this tab, which owns the terminal itself.
         */
        TerminalAreaPort getAreaPort();
    }

    @Override
    ComposerAccess getComposerAccess();

    /**
     * Returns where this tab is currently placed - a top-level workspace tab or a panel manager panel -
     * derived from its actual container, or {@code null} while not currently attached to one.
     */
    @Nullable Placement getPlacement();
}
