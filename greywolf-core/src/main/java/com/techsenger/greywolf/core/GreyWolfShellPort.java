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

package com.techsenger.greywolf.core;

import com.techsenger.greywolf.core.workspace.WorkspacePort;
import com.techsenger.shellfx.core.ShellPort;

/**
 *
 * @author Pavel Castornii
 */
public interface GreyWolfShellPort extends ShellPort {

    interface ComposerAccess extends ShellPort.ComposerAccess {

        @Override
        WorkspacePort getWorkspacePort();
    }

    @Override
    ComposerAccess getComposerAccess();

    @Override
    GreyWolfShellContextAccess getContextAccess();

    /**
     * Tells the shell that the set of registered plugin controls has changed, so the menu bar and the workspace
     * can be rebuilt from the registries.
     */
    void onPluginsUpdated();
}
