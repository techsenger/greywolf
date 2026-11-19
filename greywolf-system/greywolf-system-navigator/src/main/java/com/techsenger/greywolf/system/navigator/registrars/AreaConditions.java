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

package com.techsenger.greywolf.system.navigator.registrars;

import com.techsenger.greywolf.core.clipboard.ClipboardFiles;
import com.techsenger.greywolf.core.clipboard.GreyWolfClipboard;
import com.techsenger.greywolf.system.navigator.area.NavigatorAreaPort;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;

/**
 * Conditions on the state of a navigator area that the controls of several registrars derive their own state
 * from, so each rule is written once.
 *
 * @author Pavel Castornii
 */
final class AreaConditions {

    private AreaConditions() {
        // empty
    }

    /**
     * Returns {@code true} while the area has no file an action can work on: nothing is selected, or the selected
     * entry is the parent entry.
     */
    static ObservableValue<Boolean> noActionableFile(NavigatorAreaPort area) {
        return Bindings.createBooleanBinding(() -> {
            var file = area.getFile();
            return file == null || file.isParentEntry();
        }, area.fileProperty());
    }

    /**
     * Returns {@code true} while the clipboard holds no files to paste.
     */
    static ObservableValue<Boolean> pasteDisabled(GreyWolfClipboard clipboard) {
        return ClipboardFiles.presentIn(clipboard).not();
    }
}
