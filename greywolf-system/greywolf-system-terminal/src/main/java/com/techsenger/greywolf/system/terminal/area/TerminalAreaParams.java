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

import com.techsenger.greywolf.core.GreyWolfSettings;
import com.techsenger.greywolf.system.terminal.TerminalTabPort;
import com.techsenger.shellfx.core.area.AreaParams;
import java.util.Objects;

/**
 *
 * @author Pavel Castornii
 */
public class TerminalAreaParams extends AreaParams {

    private final GreyWolfSettings settings;

    private final TerminalTabPort tab;

    private final String path;

    public TerminalAreaParams(TerminalAreaConfig config, GreyWolfSettings settings, TerminalTabPort tab,
            String path) {
        super(config);
        this.settings = settings;
        this.tab = tab;
        this.path = path;
    }

    public GreyWolfSettings getSettings() {
        return settings;
    }

    public TerminalTabPort getTab() {
        return tab;
    }

    public String getPath() {
        return path;
    }

    @Override
    public TerminalAreaConfig getConfig() {
        return (TerminalAreaConfig) super.getConfig();
    }

    @Override
    public void validate() {
        super.validate();
        Objects.requireNonNull(getConfig());
        Objects.requireNonNull(settings);
        Objects.requireNonNull(tab);
        Objects.requireNonNull(path);
    }
}
