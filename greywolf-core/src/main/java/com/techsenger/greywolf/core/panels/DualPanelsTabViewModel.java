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

import com.techsenger.greywolf.core.style.CoreIcons;
import com.techsenger.shellfx.core.close.CloseCheckResult;
import com.techsenger.shellfx.core.close.ClosePreparationResult;
import com.techsenger.shellfx.core.tab.AbstractHostTabViewModel;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public final class DualPanelsTabViewModel extends AbstractHostTabViewModel<DualPanelsTabComposer>
        implements DualPanelsTabPort {

    public DualPanelsTabViewModel(DualPanelsTabParams params) {
        super(params);
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> cnsmr) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DualPanelsTabPort.ComposerAccess getComposerAccess() {
        return (DualPanelsTabPort.ComposerAccess) super.getComposerAccess();
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setTitle("Dual Panels");
        setIcon(CoreIcons.DUAL_PANELS);
    }

    @Override
    protected DualPanelsTabConfig getConfig() {
        return (DualPanelsTabConfig) super.getConfig();
    }
}
