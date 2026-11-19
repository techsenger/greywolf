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

package com.techsenger.greywolf.shared.location;

import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.shellfx.core.popup.AbstractPopupView;
import com.techsenger.shellfx.core.registries.StylesheetRegistry;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.storage.ui.style.DefaultStorageIconResolver;
import com.techsenger.shellfx.storage.ui.style.DefaultStorageStyleResolver;
import com.techsenger.shellfx.storage.ui.style.StorageDecorator;
import com.techsenger.toolkit.fx.utils.NodeUtils;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 * The popup that lists folders and lets the user pick one.
 *
 * @param <VM> the view model type
 * @author Pavel Castornii
 */
public class FolderPopupView<VM extends FolderPopupViewModel<?>> extends AbstractPopupView<VM> {

    private final ListView<GreyWolfFile> listView = new ListView<>();

    private final StorageDecorator storageDecorator;

    public FolderPopupView(VM viewModel, StylesheetRegistry stylesheetRegistry, Window owner) {
        super(viewModel, stylesheetRegistry, owner);
        this.storageDecorator = new StorageDecorator(new DefaultStorageIconResolver(),
                new DefaultStorageStyleResolver(), viewModel::getTheme);
    }

    @Override
    public void requestFocus() {
        NodeUtils.requestFocus(listView);
    }

    @Override
    protected void build() {
        super.build();
        var css = FolderPopupView.class.getResource("folder-popup.css").toExternalForm();
        listView.getStylesheets().add(css);
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(GreyWolfFile item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else if (item.isRoot()) {
                    setText(item.getStorage().getName());
                    setGraphic(getViewModel().isShowStorageIcon() ? createStorageIconView(item) : null);
                } else {
                    setText(item.getName());
                    setGraphic(null);
                }
            }
        });
        VBox.setVgrow(listView, Priority.ALWAYS);
        getContentBox().getChildren().add(listView);
    }

    @Override
    protected void bind() {
        super.bind();
        getViewModel().fileWrapper().bind(listView.getSelectionModel().selectedItemProperty());
        listView.getItems().setAll(getViewModel().getFiles());
        var initialFile = getViewModel().getInitialFile();
        if (initialFile != null) {
            listView.getSelectionModel().select(initialFile);
            listView.scrollTo(initialFile);
        }
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        listView.setOnMouseClicked(e -> getViewModel().onSubmit());
        listView.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER) {
                e.consume();
                getViewModel().onSubmit();
            }
        });
    }

    private FontIconView createStorageIconView(GreyWolfFile root) {
        return new FontIconView(storageDecorator.decorate(root.getStorage()).icon());
    }
}
