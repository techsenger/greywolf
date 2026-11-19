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
import com.techsenger.shellfx.core.area.AreaParams;
import java.util.Objects;

/**
 *
 * @author Pavel Castornii
 */
public class PanelTabDockParams extends AreaParams {

    private final Panel panel;

    public PanelTabDockParams(AreaConfig config, Panel panel) {
        super(config);
        this.panel = panel;
    }

    public Panel getPanel() {
        return panel;
    }

    @Override
    public void validate() {
        super.validate();
        Objects.requireNonNull(getConfig());
        Objects.requireNonNull(panel);
    }
}
