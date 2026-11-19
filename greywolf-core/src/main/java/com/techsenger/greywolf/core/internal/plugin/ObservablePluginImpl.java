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
import com.techsenger.greywolf.core.plugin.PluginCategory;
import com.techsenger.greywolf.core.plugin.PluginPricing;
import com.techsenger.weaverbird.core.api.component.ComponentConfig;
import java.util.Locale;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author Pavel Castornii
 */
class ObservablePluginImpl implements ObservablePlugin {

    public static final String CATEGORY = "category";

    public static final String PRICING = "pricing";

    public static final String VENDOR = "vendor";

    public static final String LICENSE = "license";

    public static final String HOMEPAGE = "homepage";

    public static final String NAME = "name.en";

    public static final String DESCRIPTION = "description.en";

    private final ComponentConfig config;

    private final BooleanProperty selected = new SimpleBooleanProperty();

    private final ObjectProperty<PluginState> state = new SimpleObjectProperty<>();

    private final ReadOnlyObjectWrapper<@Nullable PluginCategory> category = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<@Nullable PluginPricing> pricing = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyStringWrapper name = new ReadOnlyStringWrapper();

    private final @Nullable String vendor;

    private final @Nullable String license;

    private final @Nullable String homepage;

    private final @Nullable String description;

    ObservablePluginImpl(ComponentConfig config, PluginState state) {
        this.config = config;
        var metadata = config.getMetadata();

        var cat = metadata.get(CATEGORY);
        this.category.set(cat != null ? PluginCategory.valueOf(cat.toUpperCase(Locale.ROOT)) : null);

        var price = metadata.get(PRICING);
        this.pricing.set(price != null ? PluginPricing.valueOf(price.toUpperCase(Locale.ROOT)) : null);

        this.name.set(metadata.get(NAME));
        this.vendor = metadata.get(VENDOR);
        this.license = metadata.get(LICENSE);
        this.homepage = metadata.get(HOMEPAGE);
        this.description = metadata.get(DESCRIPTION);

        this.state.set(state);
    }

    @Override
    public ComponentConfig getComponentConfig() {
        return config;
    }

    @Override
    public @Nullable PluginCategory getCategory() {
        return category.get();
    }

    @Override
    public ReadOnlyObjectProperty<@Nullable PluginCategory> categoryProperty() {
        return category.getReadOnlyProperty();
    }

    @Override
    public @Nullable PluginPricing getPricing() {
        return pricing.get();
    }

    @Override
    public ReadOnlyObjectProperty<@Nullable PluginPricing> pricingProperty() {
        return pricing.getReadOnlyProperty();
    }

    @Override
    public @Nullable String getName() {
        return name.get();
    }

    @Override
    public ReadOnlyStringProperty nameProperty() {
        return name.getReadOnlyProperty();
    }

    @Override
    public @Nullable String getVendor() {
        return vendor;
    }

    @Override
    public @Nullable String getLicense() {
        return license;
    }

    @Override
    public @Nullable String getHomepage() {
        return homepage;
    }

    @Override
    public @Nullable String getDescription() {
        return description;
    }

    @Override
    public PluginState getState() {
        return state.get();
    }

    @Override
    public void setState(PluginState state) {
        this.state.set(state);
    }

    @Override
    public ObjectProperty<PluginState> stateProperty() {
        return state;
    }

    @Override
    public boolean isSelected() {
        return selected.get();
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    @Override
    public BooleanProperty selectedProperty() {
        return selected;
    }
}
