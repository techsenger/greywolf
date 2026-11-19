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

package com.techsenger.greywolf.core.panels;

import com.techsenger.shellfx.layout.dockhost.TabDockViewModel;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;

/**
 *
 * @author Pavel Castornii
 */
public final class PanelTabDockViewModel extends TabDockViewModel<PanelTabDockComposer>
        implements PanelTabDockPort, ActivatedPanelTabDockPort {

    private final Panel panel;

    private final ReadOnlyBooleanWrapper active = new ReadOnlyBooleanWrapper(false);

    public PanelTabDockViewModel(PanelTabDockParams params) {
        super(params);
        this.panel = params.getPanel();
    }

    @Override
    public PanelTabDockPort.ComposerAccess getComposerAccess() {
        return getComposer();
    }

    @Override
    public Panel getPanel() {
        return panel;
    }

    @Override
    public boolean isActive() {
        return active.get();
    }

    @Override
    public void setActive(boolean active) {
        this.active.set(active);
    }

    @Override
    public ReadOnlyBooleanProperty activeProperty() {
        return active.getReadOnlyProperty();
    }
}
