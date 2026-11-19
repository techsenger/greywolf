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

package com.techsenger.greywolf.core.plugin;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.weaverbird.core.api.component.ComponentException;
import com.techsenger.weaverbird.core.api.component.UnknownComponentException;
import java.util.List;

/**
 * Plugin manager is a frontend for Component manager.
 *
 * @author Pavel Castornii
 */
public interface PluginManager {

    /**
     * Returns all available plugins discovered by the system.
     *
     * <p>This operation is potentially heavy, as it reads and parses plugin configuration files from disk.
     *
     * @throws ComponentException if a plugin configuration cannot be read.
     * @throws UnknownComponentException if a discovered component cannot be resolved as a plugin.
     */
    @Unmodifiable List<Plugin> getPlugins() throws ComponentException, UnknownComponentException;
}
