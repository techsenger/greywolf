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

package com.techsenger.greywolf.system.navigator.location;

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.shellfx.base.icon.FontIcon;

/**
 * A single row shown in the Places tab: either a group header (&quot;Personal&quot;, &quot;Storages&quot;)
 * with {@code directory}/{@code icon} both {@code null}, or a leaf row navigating to {@code directory}.
 *
 * @param directory the directory this row navigates to, or {@code null} for a group header row.
 * @param icon the icon shown next to {@link #label}, or {@code null} for a group header row and for a row whose
 *     icon comes from its storage.
 * @param label the row's display text, independent of {@link GreyWolfFile#getName()} since a Personal row's
 *     label (e.g. &quot;Home&quot;) need not match the underlying directory's actual name.
 * @param group which section of the Places tab this row belongs to.
 * @param index the position of this row within its group's list, or {@code -1} for a group header row.
 * @author Pavel Castornii
 */
public record Place(@Nullable GreyWolfFile directory, @Nullable FontIcon<?> icon, String label,
        PlaceGroup group, int index) { }
