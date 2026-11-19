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

package com.techsenger.greywolf.core.internal.about;

import com.techsenger.greywolf.core.GreyWolfSettings;
import com.techsenger.shellfx.core.dialog.DialogParams;
import com.techsenger.shellfx.core.window.WindowType;
import java.util.Objects;
import javafx.application.HostServices;

/**
 *
 * @author Pavel Castornii
 */
public class AboutDialogParams extends DialogParams {

    private final HostServices hostServices;

    /**
     * Creates params.
     *
     * @param config the dialog config
     * @param settings the application settings
     * @param hostServices the host services used to open a URL in the browser of the user
     */
    public AboutDialogParams(AboutDialogConfig config, GreyWolfSettings settings, HostServices hostServices) {
        super(config, WindowType.TOP_LEVEL, settings);
        this.hostServices = hostServices;
    }

    public HostServices getHostServices() {
        return hostServices;
    }

    @Override
    public AboutDialogConfig getConfig() {
        return (AboutDialogConfig) super.getConfig();
    }

    @Override
    public void validate() {
        super.validate();
        Objects.requireNonNull(getConfig());
        Objects.requireNonNull(hostServices);
    }
}
