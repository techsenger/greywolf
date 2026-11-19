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

package com.techsenger.greywolf.storage.ui.view;

import com.techsenger.annotations.Nullable;
import javafx.beans.value.ObservableValue;

/**
 * Implemented by the composer of a top-level tab to tell which file view the main menu acts on. A tab that holds
 * the file view itself returns it, a tab that holds other tabs, such as dual panels, returns the one of the tab
 * that is current in it.
 *
 * @author Pavel Castornii
 */
public interface FileViewProvider {

    /**
     * Returns the file view the main menu acts on; it is {@code null} while the tab has none.
     */
    ObservableValue<@Nullable FileView<?>> observeFileView();
}
