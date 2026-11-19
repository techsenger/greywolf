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

module com.techsenger.greywolf.system.terminal {
    requires org.slf4j;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires atlantafx.base;
    requires pty4j;
    requires com.techsenger.patternfx.core;
    requires com.techsenger.patternfx.mvvm;
    requires com.techsenger.toolkit.fx;
    requires com.techsenger.toolkit.core;
    requires com.techsenger.jeditermfx.ui;
    requires com.techsenger.jeditermfx.core;
    requires com.techsenger.shellfx.material;
    requires com.techsenger.shellfx.base;
    requires com.techsenger.shellfx.core;
    requires com.techsenger.shellfx.shared;
    requires com.techsenger.shellfx.layout;
    requires com.techsenger.greywolf.shared;
    requires com.techsenger.greywolf.core;
    requires com.techsenger.greywolf.storage.base;
    requires com.techsenger.greywolf.storage.ui;
    requires com.techsenger.annotations;
    requires com.techsenger.weaverbird.core;
    requires com.google.errorprone.annotations;

    provides com.techsenger.weaverbird.core.spi.module.ModuleActivator
            with com.techsenger.greywolf.system.terminal.internal.ModuleActivatorProvider;

    exports com.techsenger.greywolf.system.terminal;
    exports com.techsenger.greywolf.system.terminal.area;
    exports com.techsenger.greywolf.system.terminal.find;
    exports com.techsenger.greywolf.system.terminal.slots;
    exports com.techsenger.greywolf.system.terminal.style;
    exports com.techsenger.greywolf.system.terminal.toolbar;
}
