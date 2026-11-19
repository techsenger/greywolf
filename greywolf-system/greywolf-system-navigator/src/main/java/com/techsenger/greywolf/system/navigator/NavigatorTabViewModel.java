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

package com.techsenger.greywolf.system.navigator;

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.core.panels.Placement;
import com.techsenger.greywolf.system.navigator.location.LocationMode;
import com.techsenger.greywolf.system.navigator.location.LocationTabParams;
import com.techsenger.greywolf.system.navigator.style.NavigatorIcons;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.ui.view.FolderContentListener;
import com.techsenger.shellfx.core.close.CloseCheckResult;
import com.techsenger.shellfx.core.close.ClosePreparationResult;
import com.techsenger.shellfx.core.tab.AbstractHostTabViewModel;
import com.techsenger.toolkit.fx.value.ValueUtils;
import java.util.function.Consumer;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

/**
 *
 * @author Pavel Castornii
 */
public final class NavigatorTabViewModel extends AbstractHostTabViewModel<NavigatorTabComposer>
        implements NavigatorTabPort, FolderContentListener {

    /**
     * Tells whether both placements are of the same kind - both a panel (whichever one) or both the workspace -
     * since only a change of the kind changes the layout and the chrome of the tab.
     */
    private static boolean isSameKind(@Nullable Placement first, @Nullable Placement second) {
        if (first == null || second == null) {
            return first == second;
        }
        return first.isPanel() == second.isPanel();
    }

    private final GreyWolfFile initialDirectory;

    private final ReadOnlyObjectWrapper<@Nullable Placement> placement = new ReadOnlyObjectWrapper<>();

    public NavigatorTabViewModel(NavigatorTabParams params) {
        super(params);
        this.initialDirectory = params.getInitialDirectory();
    }

    @Override
    public NavigatorTabPort.ComposerAccess getComposerAccess() {
        return (NavigatorTabPort.ComposerAccess) super.getComposerAccess();
    }

    @Override
    public @Nullable Placement getPlacement() {
        return placement.get();
    }

    @Override
    public ReadOnlyObjectProperty<@Nullable Placement> placementProperty() {
        return placement.getReadOnlyProperty();
    }

    @Override
    public boolean receive(GreyWolfFile file) {
        if (file.isParentEntry() || !(file.isDirectory() || file.isDirectoryLink())) {
            return false;
        }
        getComposerAccess().getAreaPort().setDirectory(file);
        return true;
    }

    @Override
    public void onContentChanged(GreyWolfFile folder) {
        getComposerAccess().getAreaPort().onContentChanged(folder);
    }

    /**
     * Returns the directory the area should navigate to once composed. Read once, by the composer, while
     * composing this tab's area.
     */
    GreyWolfFile getInitialDirectory() {
        return initialDirectory;
    }

    /**
     * Framework contract: written directly by the view to report the actual placement of this tab. Direct
     * invocation by user code results in undefined behavior.
     */
    ReadOnlyObjectWrapper<@Nullable Placement> placementWrapper() {
        return placement;
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setIcon(NavigatorIcons.NAVIGATOR);
        ValueUtils.callAndAddListener(placement, (ov, oldV, newV) -> applyPlacement(oldV, newV));
    }

    @Override
    protected NavigatorTabConfig getConfig() {
        return (NavigatorTabConfig) super.getConfig();
    }

    /**
     * Brings the tab in line with its placement changing - both a transition between a panel and the workspace
     * (including the initial attach) and a move from one panel to the other. Layout, toolbar and location
     * tab only care whether the tab is in a panel or in the workspace, so a move between the panels skips
     * them, while the area is always renotified so it can switch to the (possibly different) remembered view
     * modes.
     */
    private void applyPlacement(@Nullable Placement oldPlacement, @Nullable Placement newPlacement) {
        var composer = getComposer();
        if (!isSameKind(oldPlacement, newPlacement)) {
            composer.getToolBarPort().setPlacement(newPlacement);
            composer.layout(newPlacement);
            if (newPlacement == Placement.WORKSPACE && composer.getLocationTabPort() == null) {
                var context = composer.getShellPort().getContextAccess();
                var params = new LocationTabParams(LocationMode.TREE_AND_PLACES, context.getFileStorageRegistry(),
                        context.getSettings());
                var locationPort = composer.openLocationTab(params);
                var directory = composer.getAreaPort().getDirectory();
                if (directory != null) {
                    locationPort.setDirectory(directory);
                }
            }
        }
        composer.getAreaPort().setPlacement(newPlacement);
    }
}
