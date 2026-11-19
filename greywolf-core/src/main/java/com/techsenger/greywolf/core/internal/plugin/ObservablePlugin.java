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

package com.techsenger.greywolf.core.internal.plugin;

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.core.plugin.Plugin;
import com.techsenger.greywolf.core.plugin.PluginCategory;
import com.techsenger.greywolf.core.plugin.PluginPricing;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;

/**
 * Adds JavaFX properties for the fields shown in the plugin table, so table columns can bind to them
 * directly instead of needing a manual repaint after each change.
 *
 * @author Pavel Castornii
 */
interface ObservablePlugin extends Plugin {

    ReadOnlyObjectProperty<@Nullable PluginCategory> categoryProperty();

    ReadOnlyObjectProperty<@Nullable PluginPricing> pricingProperty();

    ReadOnlyStringProperty nameProperty();

    void setState(PluginState state);

    ObjectProperty<PluginState> stateProperty();

    boolean isSelected();

    void setSelected(boolean selected);

    BooleanProperty selectedProperty();
}
