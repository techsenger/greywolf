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

package com.techsenger.greywolf.storage.ui.transfer;

import com.techsenger.shellfx.core.dialog.DialogParams;
import com.techsenger.shellfx.core.settings.ShellSettings;
import com.techsenger.shellfx.core.window.WindowType;
import java.util.Objects;

/**
 * Always opened nested within the panel manager tab running the transfer, so a plain {@link WindowType#NESTED}
 * window is enough - it never floats independently. The Apply-to-all checkbox state is shared across every
 * instance of this dialog via {@link ConflictDialogConfig}.
 *
 * @author Pavel Castornii
 */
public class ConflictDialogParams extends DialogParams {

    public ConflictDialogParams(ConflictDialogConfig config, ShellSettings settings) {
        super(config, WindowType.NESTED, settings);
    }

    @Override
    public ConflictDialogConfig getConfig() {
        return (ConflictDialogConfig) super.getConfig();
    }

    @Override
    public void validate() {
        super.validate();
        Objects.requireNonNull(getConfig());
    }
}
