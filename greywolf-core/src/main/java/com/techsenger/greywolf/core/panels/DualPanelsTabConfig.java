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

import com.techsenger.shellfx.core.area.AreaConfig;
import com.techsenger.shellfx.core.tab.HostTabConfig;
import com.techsenger.shellfx.layout.dockhost.DockHostConfig;
import java.io.Serial;

/**
 *
 * @author Pavel Castornii
 */
public class DualPanelsTabConfig extends HostTabConfig {

    @Serial
    private static final long serialVersionUID = 1L;

    private DockHostConfig dockHost = new DockHostConfig();

    private AreaConfig firstPanel = new AreaConfig();

    private AreaConfig secondPanel = new AreaConfig();

    public DockHostConfig getDockHost() {
        return dockHost;
    }

    public void setDockHost(DockHostConfig dockHost) {
        this.dockHost = dockHost;
    }

    public AreaConfig getFirstPanel() {
        return firstPanel;
    }

    public void setFirstPanel(AreaConfig firstPanel) {
        this.firstPanel = firstPanel;
    }

    public AreaConfig getSecondPanel() {
        return secondPanel;
    }

    public void setSecondPanel(AreaConfig secondPanel) {
        this.secondPanel = secondPanel;
    }
}
