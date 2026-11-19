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

import atlantafx.base.theme.Styles;
import com.google.errorprone.annotations.concurrent.LazyInit;
import com.techsenger.greywolf.core.slots.PanelTabDockSlots;
import com.techsenger.greywolf.core.style.CoreIcons;
import com.techsenger.patternfx.mvvm.ViewUtils;
import com.techsenger.shellfx.core.registries.ControlBuilder;
import com.techsenger.shellfx.core.registries.Controls;
import com.techsenger.shellfx.core.tab.TabView;
import com.techsenger.shellfx.layout.dockhost.TabDockView;
import com.techsenger.shellfx.layout.tabhost.TabHostView;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.style.Spacing;
import com.techsenger.shellfx.material.style.StyleClasses;
import com.techsenger.toolkit.fx.value.ValueUtils;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

/**
 *
 * @author Pavel Castornii
 */
public final class PanelTabDockView extends TabDockView<PanelTabDockViewModel> {

    private static final PseudoClass INACTIVE_PSEUDO_CLASS = PseudoClass.getPseudoClass("inactive");

    public class Composer extends TabDockView<PanelTabDockViewModel>.Composer implements PanelTabDockComposer {

        @LazyInit
        private DualPanelsTabView dualPanelsTab;

        private final PanelTabDockView view = PanelTabDockView.this;

        @Override
        public void compose() {
            super.compose();
            var menu = view.newMenuControls.root();
            if (!menu.getItems().isEmpty()) {
                menu.getItems().get(0).fire();
            }
        }

        /**
         * Returns the {@code DualPanelsTab} this dock belongs to - every {@code PanelTabDock} lives inside
         * exactly one.
         */
        public DualPanelsTabView getDualPanelsTab() {
            return dualPanelsTab;
        }

        /**
         * Moves the tab out of this dock into another tab host, and gives the focus to the tab.
         *
         * @param tab the tab of this dock to move
         * @param target the tab host to move the tab into
         */
        public void moveTab(TabView<?> tab, TabHostView<?> target) {
            removeTab(tab);
            target.getComposer().addTab(tab);
            tab.requestFocus();
        }

        @Override
        public DualPanelsTabPort getDualPanelsTabPort() {
            return dualPanelsTab.getViewModel();
        }
    }

    @LazyInit
    private Controls<PanelTabDockView, ContextMenu> newMenuControls;

    private final Button newButton = new Button(null, new FontIconView(CoreIcons.ADD));

    public PanelTabDockView(PanelTabDockViewModel viewModel, DualPanelsTabView dualPanels) {
        super(viewModel);
        getComposer().dualPanelsTab = dualPanels;
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new PanelTabDockView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        getNode().getStyleClass().add("panel-tab-dock");
        var css = PanelTabDockView.class.getResource("panel-tab-dock.css").toExternalForm();
        getNode().getStylesheets().add(css);
        newButton.getStyleClass().addAll(StyleClasses.SQUARE, Styles.FLAT, StyleClasses.SIZE_S);
        newButton.setTooltip(new Tooltip("New"));
        var buttonBox = new HBox(newButton);
        buttonBox.setPadding(new Insets(0, 0, 1, Spacing.getHorizontalHalf()));
        buttonBox.setAlignment(Pos.CENTER);
        getTabHeaderArea().getStickyArea().getChildren().add(buttonBox);
        var context = getComposer().getDualPanelsTab().getComposer().getShell().getContext();
        var controlBuilder = new ControlBuilder(context.getSlotRegistry(), context.getControlRegistry());
        this.newMenuControls = controlBuilder.buildContextMenu(this, PanelTabDockSlots.NewMenu.MENU);
    }

    @Override
    protected void unbuild() {
        newMenuControls.deinitializeAll(this);
        super.unbuild();
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        ValueUtils.callAndAddListener(getViewModel().activeProperty(), (ov, oldV, newV) ->
                getNode().pseudoClassStateChanged(INACTIVE_PSEUDO_CLASS, !newV));
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        newButton.setOnAction(e -> showNewMenu());
    }

    @Override
    protected ContextMenu createTabContextMenu(ObservableList<Tab> tabs, Tab tab) {
        var contextMenu = super.createTabContextMenu(tabs, tab);
        // we use a weak reference as a workaround for JDK-8283449
        var tabRef = new WeakReference<>(tab);
        var moveToTopLevel = new MenuItem("Move Tab to Top Level");
        moveToTopLevel.setOnAction(e -> {
            var t = tabRef.get();
            if (t != null) {
                var dualPanelsTab = getComposer().getDualPanelsTab();
                var shell = dualPanelsTab.getComposer().getShell();
                moveTab(t, shell.getComposer().getWorkspace());
            }
        });
        var moveToOtherPanel = new MenuItem("Move Tab to Other Panel");
        moveToOtherPanel.setOnAction(e -> {
            var t = tabRef.get();
            if (t != null) {
                var dualPanelsTab = getComposer().getDualPanelsTab();
                var other = getViewModel().getPanel() == Panel.FIRST ? Panel.SECOND : Panel.FIRST;
                moveTab(t, dualPanelsTab.getComposer().getPanelTabDock(other));
            }
        });
        contextMenu.getItems().addAll(0, List.of(moveToTopLevel, moveToOtherPanel, new SeparatorMenuItem()));
        // a panel is never left without tabs, which would close it
        var singleTab = getComposer().tabCountProperty().lessThanOrEqualTo(1);
        moveToTopLevel.disableProperty().bind(singleTab);
        moveToOtherPanel.disableProperty().bind(singleTab);
        return contextMenu;
    }

    private void moveTab(Tab tab, TabHostView<?> target) {
        var tabView = (TabView<?>) Objects.requireNonNull(ViewUtils.getView(tab), "The tab has no view");
        getComposer().moveTab(tabView, target);
    }

    /**
     * Shows the &quot;New&quot; popup built when the dock was created; shows nothing if no plugin has contributed an
     * item. The popup never changes while the dock lives, since plugins change only when all components are closed.
     */
    private void showNewMenu() {
        var contextMenu = newMenuControls.root();
        if (contextMenu.getItems().isEmpty()) {
            return;
        }
        contextMenu.show(newButton, Side.BOTTOM, 0, 0);
    }
}
