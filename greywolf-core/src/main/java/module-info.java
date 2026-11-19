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

module com.techsenger.greywolf.core {
    requires org.slf4j;
    requires com.techsenger.annotations;
    requires com.google.errorprone.annotations;
    requires com.techsenger.tabpanepro.core;
    requires com.techsenger.greywolf.boot;
    requires com.techsenger.toolkit.core;
    requires com.techsenger.toolkit.fx;
    requires com.techsenger.weaverbird.core;
    requires com.techsenger.patternfx.core;
    requires com.techsenger.patternfx.mvvm;
    requires com.techsenger.shellfx.base;
    requires com.techsenger.shellfx.core;
    requires com.techsenger.shellfx.material;
    requires com.techsenger.shellfx.shared;
    requires com.techsenger.shellfx.layout;
    requires com.techsenger.shellfx.storage.base;
    requires com.techsenger.shellfx.storage.ui;
    requires com.techsenger.shellfx.icons;
    requires com.techsenger.shellfx.dialogs;
    requires com.techsenger.greywolf.storage.base;
    requires com.techsenger.greywolf.storage.ui;
    requires com.techsenger.drivekit.core;
    requires com.techsenger.fontjars.inter;
    requires com.techsenger.fontjars.jetbrainsmono;
    requires atlantafx.base;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires java.xml;

    exports com.techsenger.greywolf.core;
    exports com.techsenger.greywolf.core.clipboard;
    exports com.techsenger.greywolf.core.config;
    exports com.techsenger.greywolf.core.panels;
    exports com.techsenger.greywolf.core.plugin;
    exports com.techsenger.greywolf.core.registrar;
    exports com.techsenger.greywolf.core.registries;
    exports com.techsenger.greywolf.core.slots;
    exports com.techsenger.greywolf.core.style;
    exports com.techsenger.greywolf.core.workspace;

    opens com.techsenger.greywolf.core.internal to javafx.graphics;
    opens com.techsenger.greywolf.core.project to com.techsenger.toolkit.core;

    provides com.techsenger.weaverbird.core.spi.module.ModuleActivator
            with com.techsenger.greywolf.core.internal.ModuleActivatorProvider;

    uses com.techsenger.greywolf.storage.base.spi.SystemStorageService;
    uses com.techsenger.greywolf.storage.base.spi.RecentStorageService;
    uses com.techsenger.greywolf.storage.base.spi.CustomStorageService;
}