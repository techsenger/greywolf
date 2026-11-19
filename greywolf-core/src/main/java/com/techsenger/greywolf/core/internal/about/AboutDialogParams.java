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
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public class AboutDialogParams extends DialogParams {

    private final Consumer<String> urlOpener;

    /**
     * Creates params.
     *
     * @param config the dialog config
     * @param settings the application settings
     * @param urlOpener opens a URL in the browser of the user
     */
    public AboutDialogParams(AboutDialogConfig config, GreyWolfSettings settings, Consumer<String> urlOpener) {
        super(config, WindowType.TOP_LEVEL, settings);
        this.urlOpener = urlOpener;
    }

    public Consumer<String> getUrlOpener() {
        return urlOpener;
    }

    @Override
    public AboutDialogConfig getConfig() {
        return (AboutDialogConfig) super.getConfig();
    }

    @Override
    public void validate() {
        super.validate();
        Objects.requireNonNull(getConfig());
        Objects.requireNonNull(urlOpener, "URL opener can't be null");
    }
}
