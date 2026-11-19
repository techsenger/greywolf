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

package com.techsenger.greywolf.system.navigator.area;

/**
 * Tells what the context menu of an area is open for. The target is set when the menu is requested and is reset
 * when the menu is closed.
 *
 * @author Pavel Castornii
 */
public enum ContextTarget {

    /**
     * The context menu is closed, so it has no target.
     */
    NONE,

    /**
     * The context menu is open for a file of the area; the file is the selected one.
     */
    FILE,

    /**
     * The context menu is open for an empty place of the area, where there is no file.
     */
    SPACE
}
