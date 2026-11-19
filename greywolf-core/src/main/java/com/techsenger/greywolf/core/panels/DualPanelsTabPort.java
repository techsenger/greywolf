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

package com.techsenger.greywolf.core.panels;

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.core.GreyWolfShellPort;
import com.techsenger.shellfx.core.tab.HostTabPort;
import com.techsenger.shellfx.core.tab.TabPort;
import javafx.beans.property.ReadOnlyObjectProperty;

/**
 *
 * @author Pavel Castornii
 */
public interface DualPanelsTabPort extends HostTabPort {

    interface ComposerAccess extends HostTabPort.ComposerAccess {

        @Override
        GreyWolfShellPort getShellPort();

        Panel getCurrentPanel();

        ReadOnlyObjectProperty<Panel> currentPanelProperty();

        PanelTabDockPort getPanelTabDockPort(Panel panel);

        @Nullable TabPort getSelectedTabPort(Panel panel);

        @Nullable TabPort getCurrentTabPort();

    }

    @Override
    ComposerAccess getComposerAccess();
}
