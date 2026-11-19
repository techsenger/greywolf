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

package com.techsenger.greywolf.shared.internal;

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.ui.view.FolderContentListener;
import com.techsenger.patternfx.mvvm.ChildView;
import com.techsenger.patternfx.mvvm.ParentView;
import com.techsenger.shellfx.core.window.WindowContainerView;
import com.techsenger.shellfx.core.window.WindowType;
import com.techsenger.shellfx.core.window.WindowView;

/**
 * Finds what a component needs from the components above it in the tree: the container its dialogs are added to and
 * the components that want to know that a folder was changed.
 *
 * @author Pavel Castornii
 */
public final class ContainerUtils {

    /**
     * Returns the nearest window container above {@code view}, such as the tab it is placed in.
     *
     * @param view the component to start from
     * @return the composer of the container, or {@code null} if no container is above the component
     */
    public static @Nullable WindowContainerView.Composer findContainer(ParentView<?> view) {
        for (var parent = getParent(view); parent != null; parent = getParent(parent)) {
            if (parent instanceof WindowContainerView<?> container) {
                return container.getComposer();
            }
        }
        return null;
    }

    /**
     * Tells the components above {@code view} that implement the listener that the content of a folder has changed.
     *
     * @param view the component to start from
     * @param folder the folder whose content has changed
     */
    public static void notifyContentChanged(ParentView<?> view, GreyWolfFile folder) {
        for (var parent = getParent(view); parent != null; parent = getParent(parent)) {
            if (parent.getViewModel() instanceof FolderContentListener listener) {
                listener.onContentChanged(folder);
            }
        }
    }

    private static @Nullable ParentView<?> getParent(ParentView<?> view) {
        // a top-level window has no parent in the component tree, and asking it for one throws
        if (view instanceof WindowView<?> window && window.getViewModel().getWindowType() == WindowType.TOP_LEVEL) {
            return null;
        }
        return view instanceof ChildView<?> child ? child.getComposer().getParent() : null;
    }

    private ContainerUtils() {
        // empty
    }
}
