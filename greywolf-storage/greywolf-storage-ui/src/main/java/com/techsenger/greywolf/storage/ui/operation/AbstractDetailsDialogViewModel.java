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

package com.techsenger.greywolf.storage.ui.operation;

import com.techsenger.shellfx.core.config.ConfigUtils;
import com.techsenger.shellfx.core.dialog.AbstractDialogViewModel;
import com.techsenger.shellfx.core.window.WindowComposer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Base view model of a dialog with a collapsible details section: owns the expanded state and keeps it in the
 * dialog's config.
 *
 * @param <C> the composer type.
 * @author Pavel Castornii
 */
public abstract class AbstractDetailsDialogViewModel<C extends WindowComposer> extends AbstractDialogViewModel<C>
        implements DetailsDialogPort {

    private final BooleanProperty detailsVisible = new SimpleBooleanProperty();

    protected AbstractDetailsDialogViewModel(DetailsDialogParams params) {
        super(params);
    }

    @Override
    public boolean isDetailsVisible() {
        return detailsVisible.get();
    }

    @Override
    public void setDetailsVisible(boolean detailsVisible) {
        this.detailsVisible.set(detailsVisible);
    }

    @Override
    public BooleanProperty detailsVisibleProperty() {
        return detailsVisible;
    }

    @Override
    protected void loadStateFromConfig() {
        super.loadStateFromConfig();
        setDetailsVisible(getConfig().isDetailsVisible());
    }

    @Override
    protected void observeStateForConfig() {
        super.observeStateForConfig();
        ConfigUtils.observe(detailsVisible, getConfig(), DetailsDialogConfig::setDetailsVisible);
    }

    @Override
    protected DetailsDialogConfig getConfig() {
        return (DetailsDialogConfig) super.getConfig();
    }

    /**
     * Called by the view when the user clicks the details toggle button; a plain button carries no state of its
     * own, so this view model decides the next value itself.
     */
    protected void onDetails() {
        setDetailsVisible(!isDetailsVisible());
    }
}
