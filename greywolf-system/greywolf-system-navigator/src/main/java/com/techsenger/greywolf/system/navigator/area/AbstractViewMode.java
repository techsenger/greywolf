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
 *
 * @author Pavel Castornii
 */
public abstract class AbstractViewMode implements ViewMode {

    private final ViewModeType type;

    protected AbstractViewMode(ViewModeType type) {
        this.type = type;
    }

    @Override
    public ViewModeType getType() {
        return type;
    }

    @Override
    public boolean hasColumnCount() {
        return false;
    }
}
