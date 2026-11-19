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

module com.techsenger.greywolf.devtools.base {
    requires com.techsenger.annotations;
    requires com.techsenger.toolkit.core;
    requires com.techsenger.weaverbird.core;
    requires com.techsenger.greywolf.core;
    requires com.techsenger.greywolf.storage.base;
    requires com.techsenger.greywolf.storage.ui;
    requires com.techsenger.patternfx.core;
    requires com.techsenger.patternfx.mvvm;
    requires com.techsenger.shellfx.base;
    requires com.techsenger.shellfx.core;
    requires com.techsenger.shellfx.dialogs;
    requires com.techsenger.shellfx.material;
    requires com.techsenger.shellfx.storage.base;
    requires com.google.errorprone.annotations;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;

    requires static com.techsenger.greywolf.system.navigator;

    exports com.techsenger.greywolf.devtools.base.slots;

    provides com.techsenger.weaverbird.core.spi.module.ModuleActivator
            with com.techsenger.greywolf.devtools.base.ModuleActivatorProvider;
}
