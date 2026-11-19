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

/**
 * A dialog that is already created and configurable through its port, but is added to the window, and made visible,
 * only when {@link #show()} is called.
 *
 * @param <P> the type of the dialog's port
 * @param port the dialog's port
 * @param showAction adds the dialog to the window and makes it visible
 * @author Pavel Castornii
 */
public record DeferredDialog<P>(P port, Runnable showAction) {

    /**
     * Adds the dialog to the window and makes it visible.
     */
    public void show() {
        showAction.run();
    }
}
