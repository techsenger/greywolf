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

package com.techsenger.greywolf.system.navigator.location;

import com.google.errorprone.annotations.concurrent.LazyInit;
import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.system.navigator.area.NavigatorAreaPort;
import com.techsenger.greywolf.shared.directory.DirectoryTreeParams;
import com.techsenger.greywolf.shared.directory.DirectoryTreePort;
import com.techsenger.greywolf.shared.directory.DirectoryTreeView;
import com.techsenger.greywolf.shared.directory.DirectoryTreeViewModel;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.shellfx.core.ShellView;
import com.techsenger.shellfx.core.registries.ControlBuilder;
import com.techsenger.shellfx.core.tab.AbstractTabView;
import com.techsenger.shellfx.core.tab.TabContainerView;
import com.techsenger.shellfx.material.pane.CollapsibleSplitPane;
import com.techsenger.shellfx.material.pane.SplitPaneItem;
import com.techsenger.shellfx.material.style.Spacing;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * A single location tab that composes a {@link DirectoryTreeView} and/or a {@link PlacesAreaView} in
 * a vertical {@link CollapsibleSplitPane} (tree first - it shows where the user currently is, a stronger
 * orientation signal than the curated places shortcuts below it), depending on
 * {@link LocationTabViewModel#getMode()}.
 *
 * @author Pavel Castornii
 */
public final class LocationTabView extends AbstractTabView<LocationTabViewModel> {

    public class Composer extends AbstractTabView<LocationTabViewModel>.Composer implements LocationTabComposer {

        private final CollapsibleSplitPane<String> splitPane = new CollapsibleSplitPane<>();

        private @Nullable DirectoryTreeView<?> directoryTree;

        private @Nullable PlacesAreaView placesArea;

        @LazyInit
        private Supplier<@Nullable TabContainerView<?>> tabContainerSupplier;

        @LazyInit
        private Supplier<NavigatorAreaPort> areaPortSupplier;

        public Composer() {
            splitPane.setHeaderFactory(this::createHeader);
        }

        @Override
        public @Nullable DirectoryTreePort getDirectoryTreePort() {
            return directoryTree == null ? null : directoryTree.getViewModel();
        }

        @Override
        public @Nullable PlacesAreaPort getPlacesAreaPort() {
            return placesArea == null ? null : placesArea.getViewModel();
        }

        @Override
        public NavigatorAreaPort getAreaPort() {
            return areaPortSupplier.get();
        }

        @Override
        public void updateMode(LocationMode mode, @Nullable DirectoryTreeParams directoryTreeParams,
                @Nullable PlacesAreaParams placesParams) {
            var dual = mode == LocationMode.TREE_AND_PLACES;
            if (mode == LocationMode.TREE || dual) {
                if (directoryTree == null) {
                    directoryTree = createDirectoryTree(Objects.requireNonNull(directoryTreeParams));
                    getModifiableChildren().add(directoryTree);
                    forwardDirectory(directoryTree.getViewModel().directoryProperty());
                }
            } else {
                discardDirectoryTree();
            }
            if (mode == LocationMode.PLACES || dual) {
                if (placesArea == null) {
                    placesArea = createPlacesArea(Objects.requireNonNull(placesParams));
                    getModifiableChildren().add(placesArea);
                    forwardDirectory(placesArea.getViewModel().directoryProperty());
                }
            } else {
                discardPlacesArea();
            }
            getContentBox().getChildren().setAll(dual ? buildSplitPane() : bareNode(mode));
        }

        protected DirectoryTreeView<?> createDirectoryTree(DirectoryTreeParams params) {
            var viewModel = new DirectoryTreeViewModel<>(params);
            var context = getShell().getContext();
            var builder = new ControlBuilder(context.getSlotRegistry(), context.getControlRegistry());
            var view = new DirectoryTreeView<>(viewModel, builder, tabContainerSupplier);
            view.initialize();
            return view;
        }

        protected PlacesAreaView createPlacesArea(PlacesAreaParams params) {
            var viewModel = new PlacesAreaViewModel(params);
            var view = new PlacesAreaView(viewModel);
            view.initialize();
            return view;
        }

        /**
         * Passes the directory chosen in a child area to the tab. Done here, not in the view model, because the
         * children are created and discarded dynamically as the mode changes, and the view model has no hook for it.
         */
        private void forwardDirectory(ObservableValue<@Nullable GreyWolfFile> childDirectory) {
            childDirectory.addListener((ov, oldV, newV) -> {
                if (newV != null) {
                    getViewModel().setDirectory(newV);
                }
            });
        }

        private void discardDirectoryTree() {
            if (directoryTree == null) {
                return;
            }
            directoryTree.getViewModel().deinitialize();
            getModifiableChildren().remove(directoryTree);
            this.directoryTree = null;
        }

        private void discardPlacesArea() {
            if (placesArea == null) {
                return;
            }
            placesArea.getViewModel().deinitialize();
            getModifiableChildren().remove(placesArea);
            this.placesArea = null;
        }

        /**
         * Returns the single composed area's node for {@code mode}, detached from wherever it currently sits
         * (e.g. inside a previous, now-discarded {@link #splitPane} item).
         */
        private Node bareNode(LocationMode mode) {
            var area = mode == LocationMode.TREE ? directoryTree.getNode() : placesArea.getNode();
            detachFromParent(area);
            VBox.setVgrow(area, Priority.ALWAYS);
            return area;
        }

        /**
         * Rebuilds {@link #splitPane}'s two items from {@link #directoryTree}/{@link #placesArea}'s current
         * nodes, resetting any previous collapsed state back to an even split.
         */
        private CollapsibleSplitPane<String> buildSplitPane() {
            splitPane.getItems().setAll(List.of(
                    new SplitPaneItem<>(directoryTree.getNode(), "Folder Tree"),
                    new SplitPaneItem<>(placesArea.getNode(), "User Places")));
            VBox.setVgrow(splitPane, Priority.ALWAYS);
            return splitPane;
        }

        private void detachFromParent(Node node) {
            if (node.getParent() instanceof Pane parent) {
                parent.getChildren().remove(node);
            }
        }

        /**
         * Builds one {@link #splitPane} item's header: its title, then the item's own ready-made collapse
         * button pinned to the right via a growing spacer, styled like {@code TabDock}'s own close/minimize
         * buttons in shellfx-layout, followed by a 1px separator line kept outside the header row's own box so
         * it never eats into the padding around the button.
         */
        private Region createHeader(SplitPaneItem<String> item) {
            var label = new Label(item.getValue());
            label.setPadding(new Insets(Spacing.getVerticalHalf(), 0, Spacing.getVerticalHalf(), 0));
            var spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            var headerBox = new HBox(label, spacer, item.getCollapseButton());
            headerBox.setAlignment(Pos.CENTER_LEFT);
            headerBox.setSpacing(Spacing.getHorizontalHalf());
            headerBox.setPadding(new Insets(0, Spacing.getHorizontalThird(), 0, Spacing.getHorizontal()));

            var separator = new Region();
            separator.getStyleClass().add("section-separator");

            return new VBox(headerBox, separator);
        }

        private void setSuppliers(Supplier<@Nullable TabContainerView<?>> tabContainerSupplier,
                Supplier<NavigatorAreaPort> areaPortSupplier) {
            this.tabContainerSupplier = tabContainerSupplier;
            this.areaPortSupplier = areaPortSupplier;
        }
    }

    /**
     * Creates the location tab.
     *
     * @param viewModel the view model
     * @param shell the shell the tab belongs to
     * @param tabContainerSupplier provides the container of the navigator tab this tab belongs to, or
     *     {@code null} while that tab is not attached to one
     * @param areaPortSupplier provides the area of the navigator tab this tab belongs to
     */
    public LocationTabView(LocationTabViewModel viewModel, ShellView<?> shell,
            Supplier<@Nullable TabContainerView<?>> tabContainerSupplier,
            Supplier<NavigatorAreaPort> areaPortSupplier) {
        super(viewModel, shell);
        getComposer().setSuppliers(tabContainerSupplier, areaPortSupplier);
    }

    @Override
    public void requestFocus() {
        var area = getComposer().directoryTree != null ? getComposer().directoryTree : getComposer().placesArea;
        if (area != null) {
            area.requestFocus();
        }
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new LocationTabView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        var css = LocationTabView.class.getResource("location-tab.css").toExternalForm();
        getContentBox().getStylesheets().add(css);
    }
}
