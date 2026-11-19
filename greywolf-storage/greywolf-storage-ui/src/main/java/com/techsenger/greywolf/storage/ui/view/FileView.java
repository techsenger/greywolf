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
import com.techsenger.patternfx.mvvm.ParentView;
import com.techsenger.shellfx.core.tab.TabContainerView;

/**
 * A view of a component that acts on a file of a storage, such as the focused entry of a directory listing. Plugins
 * register their controls for this type, so they work with any such component without depending on its module.
 *
 * @param <VM> the type of the view model of the component
 * @author Pavel Castornii
 */
public interface FileView<VM extends FileViewModel<?>> extends ParentView<VM> {

    interface Composer extends ParentView.Composer {

        /**
         * Returns the container of the tab the component belongs to, such as a panel dock or the workspace, so a
         * plugin can open its own tab beside it.
         *
         * @return the container, or {@code null} while the tab is not attached to one
         */
        @Nullable TabContainerView<?> getTabContainer();
    }

    @Override
    Composer getComposer();
}
