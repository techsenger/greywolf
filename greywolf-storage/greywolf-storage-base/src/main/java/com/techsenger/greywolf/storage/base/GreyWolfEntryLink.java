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

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.storage.base.EntryLink;

/**
 * The target of a link of GreyWolf: the file it finally points to is a {@link GreyWolfFile}.
 *
 * @author Pavel Castornii
 */
public interface GreyWolfEntryLink extends EntryLink {

    @Override
    @Nullable GreyWolfFile getTarget();
}
