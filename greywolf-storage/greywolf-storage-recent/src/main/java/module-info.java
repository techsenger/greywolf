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

module com.techsenger.greywolf.storage.recent {
    requires com.techsenger.annotations;
    requires com.google.errorprone.annotations;
    requires com.techsenger.shellfx.base;
    requires com.techsenger.shellfx.core;
    requires com.techsenger.shellfx.material;
    requires com.techsenger.shellfx.storage.base;
    requires com.techsenger.greywolf.storage.base;
    requires com.techsenger.toolkit.core;
    requires com.techsenger.weaverbird.core;
    requires java.xml;

    provides com.techsenger.greywolf.storage.base.spi.RecentStorageService
            with com.techsenger.greywolf.storage.recent.RecentStorageProvider;
    provides com.techsenger.weaverbird.core.spi.module.ModuleActivator
            with com.techsenger.greywolf.storage.recent.ModuleActivatorProvider;
}
