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

package com.techsenger.greywolf.storage.ui.operation;

import com.techsenger.shellfx.core.config.ConfigManager;
import com.techsenger.shellfx.core.settings.ShellSettings;
import com.techsenger.shellfx.core.window.WindowType;
import com.techsenger.shellfx.dialogs.progress.ProgressDialogConfig;
import com.techsenger.shellfx.dialogs.progress.ProgressDialogParams;

/**
 * Builds params of the standard shellfx dialogs, taking their configs from the config manager, so the dialogs
 * of every plugin share them.
 *
 * @author Pavel Castornii
 */
public final class DialogParamsFactory {

    public static ProgressDialogParams createProgressParams(ConfigManager configManager, WindowType windowType,
            ShellSettings settings) {
        var config = configManager.getOrCreateConfig(ProgressDialogConfig.class, ProgressDialogConfig::new);
        return new ProgressDialogParams(config, windowType, settings);
    }

    private DialogParamsFactory() {
        // empty
    }
}
