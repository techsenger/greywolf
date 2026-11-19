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

package com.techsenger.greywolf.core.workspace;

import com.techsenger.shellfx.core.area.AreaParams;
import com.techsenger.shellfx.layout.tabhost.TabHostComposer;
import com.techsenger.shellfx.layout.tabhost.TabHostViewModel;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;

/**
 *
 * @author Pavel Castornii
 */
public final class WorkspaceViewModel extends TabHostViewModel<TabHostComposer> implements WorkspacePort {

    private final ObservableSource<Void> upgradeTabHeaderSource = new SimpleObservableSource<>();

    public WorkspaceViewModel(AreaParams params) {
        super(params);
    }

    @Override
    public void onPluginsUpdated() {
        upgradeTabHeaderSource.next(null);
    }

    ObservableSource<Void> upgradeTabHeaderSource() {
        return upgradeTabHeaderSource;
    }
}
