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

import com.techsenger.shellfx.core.dialog.DialogConfig;
import java.io.Serial;

/**
 * Keeps whether the user left the dialog's details section expanded, so it comes back the same way next time.
 * The height is never kept: it follows the dialog's content, and a stored one would pin the dialog and keep it
 * from shrinking when the details are hidden.
 *
 * @author Pavel Castornii
 */
public class DetailsDialogConfig extends DialogConfig {

    @Serial
    private static final long serialVersionUID = 1L;

    private boolean detailsVisible;

    public DetailsDialogConfig() {
        this.detailsVisible = true;
    }

    @Override
    public double getHeight() {
        return -1;
    }

    @Override
    public void setHeight(double height) {
        // ignored
    }

    public boolean isDetailsVisible() {
        return detailsVisible;
    }

    public void setDetailsVisible(boolean detailsVisible) {
        this.detailsVisible = detailsVisible;
    }
}
