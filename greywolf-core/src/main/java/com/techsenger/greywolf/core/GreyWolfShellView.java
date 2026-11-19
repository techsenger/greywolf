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

package com.techsenger.greywolf.core;

import com.techsenger.greywolf.core.registrars.PluginsItemHandler;
import com.techsenger.greywolf.core.slots.GreyWolfShellSlots;
import com.techsenger.greywolf.core.workspace.WorkspacePort;
import com.techsenger.greywolf.core.workspace.WorkspaceView;
import com.techsenger.patternfx.core.AbstractDepthFirstIterator;
import com.techsenger.patternfx.core.TreeIterator;
import com.techsenger.patternfx.mvvm.ParentView;
import com.techsenger.shellfx.core.DefaultShellView;
import com.techsenger.shellfx.core.area.AreaView;
import com.techsenger.shellfx.core.registries.ControlUtils;
import com.techsenger.shellfx.core.window.AcceleratorAware;
import com.techsenger.shellfx.core.window.AcceleratorManager;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

/**
 *
 * @author Pavel Castornii
 */
public final class GreyWolfShellView extends DefaultShellView<GreyWolfShellViewModel> implements AcceleratorAware {

    public class Composer extends DefaultShellView<GreyWolfShellViewModel>.Composer
            implements GreyWolfShellComposer {

        private final GreyWolfShellView view = GreyWolfShellView.this;

        private final ChangeListener<Object> selectedTabListener = (ov, oldV, newV) -> view.updateAccelerators();

        @Override
        public void addWorkspace(AreaView<?> workspace) {
            super.addWorkspace(workspace);
            getWorkspace().getComposer().selectedTabProperty().addListener(selectedTabListener);
        }

        @Override
        public void removeWorkspace() {
            var workspace = getWorkspace();
            if (workspace != null) {
                workspace.getComposer().selectedTabProperty().removeListener(selectedTabListener);
            }
            super.removeWorkspace();
            view.updateAccelerators();
        }

        @Override
        public WorkspacePort getWorkspacePort() {
            return (WorkspacePort) super.getWorkspacePort();
        }

        @Override
        public WorkspaceView getWorkspace() {
            return (WorkspaceView) super.getWorkspace();
        }

        @Override
        public void openPluginsDialog() {
            new PluginsItemHandler((GreyWolf) getContext()).openDialog();
        }
    }

    private final AcceleratorManager acceleratorManager =
            new AcceleratorManager(() -> getWindow().getScene(), this::createAcceleratorIterator);

    public GreyWolfShellView(GreyWolfShellViewModel viewModel, GreyWolfShellContext context, Stage stage) {
        super(viewModel, context, stage, GreyWolfShellSlots.MAIN_MENU);
    }

    @Override
    public GreyWolfShellContext getContext() {
        return (GreyWolfShellContext) super.getContext();
    }

    @Override
    public void upgradeMenuBar() {
        super.upgradeMenuBar();
        updateAccelerators();
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    public List<MenuItem> getAcceleratorItems() {
        var menuBarControls = getMenuBarControls();
        return menuBarControls == null ? List.of() : ControlUtils.getMenuItems(menuBarControls);
    }

    /**
     * Puts the accelerators of the shell, of the workspace and of the components of the selected workspace tab into
     * the scene. It is called when the menu bar or the selected tab changes, and must be called when a component of
     * the tab changes its items, becomes used or unused, or when the hotkeys are reassigned.
     */
    public void updateAccelerators() {
        acceleratorManager.update();
    }

    @Override
    protected Composer createComposer() {
        return new GreyWolfShellView.Composer();
    }

    @Override
    protected void unbuild() {
        acceleratorManager.dispose();
        super.unbuild();
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        getViewModel().upgradeMenuBarSource().addListener((v) -> upgradeMenuBar());
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        getWindow().setOnShown(e -> getViewModel().onShown());
    }

    /**
     * Creates an iterator over the components that are asked for accelerators: the shell, the workspace and the
     * selected tab of the workspace with all of its components.
     */
    private TreeIterator<ParentView<?>> createAcceleratorIterator() {
        var workspace = getComposer().getWorkspace();
        var tab = workspace == null ? null : workspace.getComposer().getSelectedTab();
        return new AbstractDepthFirstIterator<ParentView<?>, ParentView<?>>(this) {

            @Override
            @SuppressWarnings("unchecked")
            protected List<ParentView<?>> getChildren(ParentView<?> parent) {
                if (parent == GreyWolfShellView.this) {
                    return workspace == null ? List.of() : List.<ParentView<?>>of(workspace);
                }
                if (parent == workspace) {
                    return tab == null ? List.of() : List.<ParentView<?>>of(tab);
                }
                return (List<ParentView<?>>) (List<?>) parent.getComposer().getChildren();
            }

            @Override
            protected ParentView<?> map(ParentView<?> value) {
                return value;
            }
        };
    }
}
