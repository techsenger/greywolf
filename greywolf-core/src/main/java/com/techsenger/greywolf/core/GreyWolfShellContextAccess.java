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

import com.techsenger.greywolf.core.clipboard.GreyWolfClipboard;
import com.techsenger.greywolf.core.plugin.PluginManager;
import com.techsenger.greywolf.core.registries.FileStorageRegistry;
import com.techsenger.shellfx.core.ShellContextAccess;
import com.techsenger.toolkit.core.version.Version;
import com.techsenger.weaverbird.core.api.Framework;

/**
 * The part of the GreyWolf context that is available to view models and ports: the services of the application
 * without the registries of the view side.
 *
 * @author Pavel Castornii
 */
public interface GreyWolfShellContextAccess extends ShellContextAccess {

    Version getVersion();

    Framework getWeaverbird();

    @Override
    GreyWolfSettings getSettings();

    FileStorageRegistry getFileStorageRegistry();

    PluginManager getPluginManager();

    GreyWolfClipboard getClipboard();
}
