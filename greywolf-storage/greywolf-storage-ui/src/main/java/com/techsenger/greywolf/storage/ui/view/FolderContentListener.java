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

import com.techsenger.greywolf.storage.base.GreyWolfFile;

/**
 * A component that shows folders and wants to know that the content of a folder was changed by an operation that did
 * not run in the component itself, such as a paste started from a context menu of a component inside it.
 *
 * @author Pavel Castornii
 */
public interface FolderContentListener {

    /**
     * Tells that the content of the folder has changed.
     *
     * @param folder the folder whose content has changed
     */
    void onContentChanged(GreyWolfFile folder);
}
