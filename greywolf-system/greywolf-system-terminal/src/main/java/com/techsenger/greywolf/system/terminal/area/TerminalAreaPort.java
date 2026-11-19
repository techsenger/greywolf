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

package com.techsenger.greywolf.system.terminal.area;

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.core.area.AreaPort;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;

/**
 * Provides full access to the component's client API.
 *
 * @author Pavel Castornii
 */
public interface TerminalAreaPort extends AreaPort {

    TerminalPaletteType getPaletteType();

    void setPaletteType(TerminalPaletteType type);

    ObjectProperty<TerminalPaletteType> paletteTypeProperty();

    @Nullable String getSelectedText();

    ReadOnlyStringProperty selectedTextProperty();

    void onAddNew();

    void onCommand(TerminalCommand command);

    void onShowFind();

    /**
     * Changes the current directory of the command interpreter (cmd, bash) running in this terminal by typing
     * the command into it.
     *
     * @param path the directory to change to
     */
    void onChangeDirectory(String path);

    /**
     * Runs the program at {@code path} in the command interpreter running in this terminal by typing its name
     * into it.
     *
     * @param path the executable file to run
     */
    void onRunFile(String path);
}
