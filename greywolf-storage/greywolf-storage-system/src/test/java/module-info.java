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

open module com.techsenger.greywolf.storage.system {
    requires com.techsenger.annotations;
    requires com.techsenger.greywolf.storage.base;
    requires com.techsenger.shellfx.icons;
    requires com.techsenger.shellfx.material;
    requires com.techsenger.shellfx.storage.base;
    requires com.techsenger.shellfx.storage.system;
    requires com.techsenger.toolkit.core;
    requires com.techsenger.drivekit.core;
    requires javafx.swing;

    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;
    requires org.assertj.core;
    requires com.google.common.jimfs;
}
