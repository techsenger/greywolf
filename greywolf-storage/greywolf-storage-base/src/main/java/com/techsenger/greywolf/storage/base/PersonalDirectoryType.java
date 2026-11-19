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

package com.techsenger.greywolf.storage.base;

/**
 * The role a {@link PersonalDirectory} plays for the current user, as reported by the OS - distinct from its
 * display label or icon, both of which are a UI concern left to each consumer of {@link PersonalDirectories}.
 *
 * @author Pavel Castornii
 */
public enum PersonalDirectoryType {

    HOME,

    DESKTOP,

    DOCUMENTS,

    DOWNLOADS,

    MUSIC,

    PICTURES,

    VIDEOS
}
