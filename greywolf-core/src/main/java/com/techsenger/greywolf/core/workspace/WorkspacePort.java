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

package com.techsenger.greywolf.core.workspace;

import com.techsenger.shellfx.layout.tabhost.TabHostPort;

/**
 * The port of the top-level workspace.
 *
 * @author Pavel Castornii
 */
public interface WorkspacePort extends TabHostPort {

    /**
     * Tells the workspace that the set of registered plugin controls has changed, so it can adapt its tab header
     * to what the plugins contribute.
     */
    void onPluginsUpdated();
}
