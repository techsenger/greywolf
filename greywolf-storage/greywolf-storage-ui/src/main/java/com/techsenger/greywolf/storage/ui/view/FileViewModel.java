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
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.patternfx.mvvm.Composer;
import com.techsenger.patternfx.mvvm.ParentViewModel;
import javafx.beans.property.ReadOnlyObjectProperty;

/**
 * The view model of a component that shows the content of a directory of a storage.
 *
 * @param <C> the type of the composer of the component
 * @author Pavel Castornii
 */
public interface FileViewModel<C extends Composer> extends ParentViewModel<C> {

    /**
     * Returns the file the component currently acts on, such as the focused or context menu entry, or {@code null}
     * if there is none.
     */
    @Nullable GreyWolfFile getFile();

    ReadOnlyObjectProperty<@Nullable GreyWolfFile> fileProperty();
}
