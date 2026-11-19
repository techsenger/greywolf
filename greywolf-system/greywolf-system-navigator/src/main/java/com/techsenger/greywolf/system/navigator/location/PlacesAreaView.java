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

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.core.area.AbstractAreaView;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.style.StyleClasses;
import com.techsenger.shellfx.storage.ui.DecorationCellUtils;
import com.techsenger.shellfx.storage.ui.style.DefaultStorageIconResolver;
import com.techsenger.shellfx.storage.ui.style.DefaultStorageStyleResolver;
import com.techsenger.shellfx.storage.ui.style.StorageDecorator;
import com.techsenger.toolkit.fx.value.ValueUtils;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * The flat, grouped Personal/Storages half of the accessory panel - a plain sub-component composed by whichever
 * location tab currently wants it, with no header/label of its own; the tab composing this area is
 * responsible for any surrounding chrome (e.g. a section label above it).
 *
 * @author Pavel Castornii
 */
public final class PlacesAreaView extends AbstractAreaView<PlacesAreaViewModel> {

    private static final class PlaceCell extends TreeCell<Place> {

        private final FontIconView iconView = new FontIconView();

        private final StorageDecorator storageDecorator;

        PlaceCell(StorageDecorator storageDecorator) {
            this.storageDecorator = storageDecorator;
        }

        @Override
        protected void updateItem(@Nullable Place item, boolean empty) {
            super.updateItem(item, empty);
            var directory = empty || item == null ? null : item.directory();
            if (empty || item == null) {
                DecorationCellUtils.updateStyles(this, iconView, null);
                setText(null);
                setGraphic(null);
                setTooltip(null);
            } else if (directory == null) {
                DecorationCellUtils.updateStyles(this, iconView, null);
                setText(item.label());
                setGraphic(null);
                setTooltip(null);
            } else {
                setText(item.label());
                if (item.group() == PlaceGroup.STORAGE) {
                    DecorationCellUtils.updateDecoration(this, iconView,
                            storageDecorator.decorate(directory.getStorage()));
                } else {
                    DecorationCellUtils.updateStyles(this, iconView, null);
                    iconView.setIcon(item.icon());
                }
                setGraphic(iconView);
                setTooltip(new Tooltip(directory.getUri().toString()));
            }
        }
    }

    private final VBox node = new VBox();

    private final TreeItem<Place> root = new TreeItem<>();

    private final TreeItem<Place> personalGroup =
            new TreeItem<>(new Place(null, null, "Personal", PlaceGroup.PERSONAL, -1));

    private final TreeItem<Place> storageGroup =
            new TreeItem<>(new Place(null, null, "Storages", PlaceGroup.STORAGE, -1));

    private final TreeView<Place> treeView = new TreeView<>(root);

    private final StorageDecorator storageDecorator;

    public PlacesAreaView(PlacesAreaViewModel viewModel) {
        super(viewModel);
        this.storageDecorator = new StorageDecorator(new DefaultStorageIconResolver(),
                new DefaultStorageStyleResolver(), viewModel::getTheme);
    }

    @Override
    public void requestFocus() {
        treeView.requestFocus();
    }

    @Override
    public VBox getNode() {
        return node;
    }

    @Override
    protected void build() {
        super.build();
        treeView.setShowRoot(false);
        treeView.setCellFactory(v -> new PlaceCell(storageDecorator));
        treeView.getStyleClass().add(StyleClasses.NO_BORDER);
        root.getChildren().add(personalGroup);
        root.getChildren().add(storageGroup);
        personalGroup.setExpanded(true);
        storageGroup.setExpanded(true);
        VBox.setVgrow(treeView, Priority.ALWAYS);
        node.getChildren().add(treeView);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        var viewModel = getViewModel();
        viewModel.getPersonalPlaces().addListener((ListChangeListener<Place>) change ->
                personalGroup.getChildren().setAll(toTreeItems(viewModel.getPersonalPlaces())));
        viewModel.getStoragePlaces().addListener((ListChangeListener<Place>) change ->
                storageGroup.getChildren().setAll(toTreeItems(viewModel.getStoragePlaces())));
        viewModel.themeProperty().addListener((ov, oldV, newV) -> treeView.refresh());
        ValueUtils.callAndAddListener(viewModel.placeProperty(), (ov, oldV, newV) ->
                applySelectedPlace(newV));
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        treeView.setOnMouseClicked(e -> {
            var treeItem = treeView.getSelectionModel().getSelectedItem();
            if (treeItem == null) {
                return;
            }
            var place = treeItem.getValue();
            var directory = place == null ? null : place.directory();
            if (directory != null) {
                getViewModel().setDirectory(directory);
            }
        });
    }

    /**
     * Highlights {@code place}'s row, or clears the highlight entirely when {@code place} is
     * {@code null} - the current directory is none of the Personal or Storages rows. Both cases work the same
     * way: {@link Place#group()} picks {@link #personalGroup} or {@link #storageGroup}, and
     * {@link Place#index()} - already resolved by the ViewModel against the very same list that
     * group's rows were just built from 1:1 - is the child index to select, with nothing left here to search for.
     */
    private void applySelectedPlace(@Nullable Place place) {
        treeView.getSelectionModel().clearSelection();
        if (place == null || place.directory() == null) {
            return;
        }
        var group = switch (place.group()) {
            case PERSONAL -> personalGroup;
            case STORAGE -> storageGroup;
        };
        var index = place.index();
        if (index >= 0 && index < group.getChildren().size()) {
            treeView.getSelectionModel().select(group.getChildren().get(index));
        }
    }

    private List<TreeItem<Place>> toTreeItems(List<Place> places) {
        var result = new ArrayList<TreeItem<Place>>(places.size());
        for (var place : places) {
            result.add(new TreeItem<>(place));
        }
        return result;
    }
}
