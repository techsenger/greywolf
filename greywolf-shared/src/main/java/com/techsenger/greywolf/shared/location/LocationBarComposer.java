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

package com.techsenger.greywolf.shared.location;

import com.techsenger.annotations.Nullable;
import com.techsenger.patternfx.mvvm.ChildComposer;

/**
 * The composer of the location bar.
 *
 * @author Pavel Castornii
 */
public interface LocationBarComposer extends ChildComposer {

    /**
     * Opens the popup that lists folders under the button the user clicked last, closing the popup that is already
     * open, and makes it visible.
     *
     * @param params the parameters of the popup
     * @return the port of the popup, or {@code null} if there is no such button any more and nothing was opened
     */
    @Nullable FolderPopupPort openFolderPopup(FolderPopupParams params);

    /**
     * Returns the port of the open popup that lists folders.
     *
     * @return the port, or {@code null} if no such popup is open
     */
    @Nullable FolderPopupPort getFolderPopupPort();
}
