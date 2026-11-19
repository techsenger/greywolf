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

import com.techsenger.shellfx.base.icon.FontIcon;
import java.net.URI;

/**
 * One of the current user's well-known personal directories, as reported by the OS.
 *
 * @param type which personal directory this is.
 * @param uri its location.
 * @param icon the icon this directory is shown with wherever it is listed among other entries.
 * @author Pavel Castornii
 */
public record PersonalDirectory(PersonalDirectoryType type, URI uri, FontIcon<?> icon) {
}
