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

package com.techsenger.greywolf.shared.directory;

import com.google.errorprone.annotations.concurrent.LazyInit;
import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.shared.slots.DirectoryTreeSlots.NodeMenu;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.base.GreyWolfStorage;
import com.techsenger.greywolf.storage.base.StorageCategory;
import com.techsenger.greywolf.storage.ui.style.GreyWolfFileIconResolver;
import com.techsenger.greywolf.storage.ui.style.GreyWolfFileStyleResolver;
import com.techsenger.greywolf.storage.ui.view.FileView;
import com.techsenger.shellfx.core.area.AbstractAreaView;
import com.techsenger.shellfx.core.registries.ControlBuilder;
import com.techsenger.shellfx.core.registries.Controls;
import com.techsenger.shellfx.core.tab.TabContainerView;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.style.StyleClasses;
import com.techsenger.shellfx.storage.ui.DecorationCellUtils;
import com.techsenger.shellfx.storage.ui.FileCellUpdater;
import com.techsenger.shellfx.storage.ui.style.DefaultStorageIconResolver;
import com.techsenger.shellfx.storage.ui.style.DefaultStorageStyleResolver;
import com.techsenger.shellfx.storage.ui.style.FileDecorationType;
import com.techsenger.shellfx.storage.ui.style.FileDecorator;
import com.techsenger.shellfx.storage.ui.style.StorageDecorator;
import com.techsenger.toolkit.fx.utils.ScrollPosition;
import com.techsenger.toolkit.fx.utils.TreeViewUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public class DirectoryTreeView<VM extends DirectoryTreeViewModel<?>> extends AbstractAreaView<VM>
        implements FileView<VM> {

    private static final class FileCell extends TreeCell<GreyWolfFile> {

        private final FontIconView iconView = new FontIconView();

        private final FileDecorator<GreyWolfFile> fileDecorator;

        private final StorageDecorator storageDecorator;

        private final FileCellUpdater<GreyWolfFile> updater;

        FileCell(FileDecorator<GreyWolfFile> fileDecorator, StorageDecorator storageDecorator) {
            this.fileDecorator = fileDecorator;
            this.storageDecorator = storageDecorator;
            this.updater = new FileCellUpdater<>(file -> DecorationCellUtils.updateDecoration(
                    this, iconView, fileDecorator.getDecoration(file)));
        }

        @Override
        protected void updateItem(@Nullable GreyWolfFile item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                updater.unobserve();
                DecorationCellUtils.updateStyles(this, iconView, null);
                setText(null);
                setGraphic(null);
            } else {
                setText(item.getName());
                if (item.isRoot()) {
                    updater.unobserve();
                    DecorationCellUtils.updateDecoration(this, iconView, storageDecorator.decorate(item.getStorage()));
                } else {
                    fileDecorator.decorate(item);
                    updater.observe(item, fileDecorator::decorationValue);
                }
                setGraphic(iconView);
            }
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(DirectoryTreeView.class);

    /**
     * Recursively removes {@code item}'s descendants from {@code index} before its children are replaced, so
     * previously indexed subtrees don't leak as orphaned entries.
     *
     * @return the removed files, for logging
     */
    private static List<GreyWolfFile> removeSubtreeFromIndex(TreeItem<GreyWolfFile> item,
            Map<GreyWolfFile, TreeItem<GreyWolfFile>> index) {
        var removed = new ArrayList<GreyWolfFile>();
        for (var child : item.getChildren()) {
            var value = child.getValue();
            if (value != null) {
                index.remove(value);
                removed.add(value);
            }
            removed.addAll(removeSubtreeFromIndex(child, index));
        }
        return removed;
    }

    public class Composer extends AbstractAreaView<VM>.Composer implements FileView.Composer {

        @LazyInit
        private Supplier<@Nullable TabContainerView<?>> tabContainerSupplier;

        @Override
        public @Nullable TabContainerView<?> getTabContainer() {
            return tabContainerSupplier.get();
        }

        private void setTabContainerSupplier(Supplier<@Nullable TabContainerView<?>> tabContainerSupplier) {
            this.tabContainerSupplier = tabContainerSupplier;
        }
    }

    private final VBox node = new VBox();

    private final TreeView<GreyWolfFile> treeView = new TreeView<>();

    private final TreeItem<GreyWolfFile> root = new TreeItem<>();

    /**
     * Maps each file to its TreeItem for O(1) lookup, since the view model references tree nodes only by
     * GreyWolfFile and never by TreeItem.
     */
    private final Map<GreyWolfFile, TreeItem<GreyWolfFile>> index = new HashMap<>();

    private final FileDecorator<GreyWolfFile> fileDecorator;

    private final StorageDecorator storageDecorator;

    private final ControlBuilder controlBuilder;

    @LazyInit
    private Controls<DirectoryTreeView<VM>, ContextMenu> contextMenuControls;

    /**
     * Creates the directory tree.
     *
     * @param viewModel the view model
     * @param controlBuilder builds the context menu of a node from what is registered for its slot
     * @param tabContainerSupplier provides the container of the tab the tree belongs to, or {@code null} while the
     *     tab is not attached to one
     */
    public DirectoryTreeView(VM viewModel, ControlBuilder controlBuilder,
            Supplier<@Nullable TabContainerView<?>> tabContainerSupplier) {
        super(viewModel);
        this.controlBuilder = controlBuilder;
        getComposer().setTabContainerSupplier(tabContainerSupplier);
        this.fileDecorator = new FileDecorator<>(new GreyWolfFileIconResolver(), new GreyWolfFileStyleResolver(),
                file -> FileDecorationType.FINAL, viewModel::getTheme);
        this.storageDecorator = new StorageDecorator(new DefaultStorageIconResolver(),
                new DefaultStorageStyleResolver(), viewModel::getTheme);
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
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
    protected Composer createComposer() {
        return new Composer();
    }

    @Override
    protected void build() {
        super.build();
        treeView.setRoot(root);
        treeView.setShowRoot(false);
        treeView.setCellFactory(tv -> new FileCell(fileDecorator, storageDecorator));
        this.contextMenuControls = controlBuilder.buildContextMenu(this, NodeMenu.MENU);
        treeView.setContextMenu(contextMenuControls.root());
        treeView.getStyleClass().add(StyleClasses.NO_BORDER);
        node.getChildren().add(treeView);
        VBox.setVgrow(treeView, Priority.ALWAYS);
    }

    @Override
    protected void unbuild() {
        treeView.setContextMenu(null);
        contextMenuControls.deinitializeAll(this);
        super.unbuild();
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        var viewModel = getViewModel();
        viewModel.getStorages().addListener((ListChangeListener<GreyWolfStorage>) change -> {
            while (change.next()) {
                removeStorages(change.getRemoved());
                addStorages(change.getFrom(), change.getAddedSubList());
            }
        });
        viewModel.getChildrenByDirectory().addListener((MapChangeListener<GreyWolfFile, List<GreyWolfFile>>)
                change -> {
                    if (change.wasAdded()) {
                        applyChildren(change.getKey(), change.getValueAdded());
                    }
                });
        viewModel.themeProperty().addListener((ov, oldV, newV) -> {
            fileDecorator.clear();
            treeView.refresh();
        });
        viewModel.expandPathSource().addListener((path) -> applyExpandPath(path));
        viewModel.directorySource().addListener((dir) -> applyDirectory(dir));
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        // A filter, so the node the menu is asked for is known before the menu is shown; where there is no node
        // there is nothing to act on, so the menu is not shown.
        treeView.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, e -> {
            GreyWolfFile file = null;
            if (e.isKeyboardTrigger()) {
                var selectedItem = treeView.getSelectionModel().getSelectedItem();
                file = selectedItem == null ? null : selectedItem.getValue();
            } else {
                file = findFile(e.getTarget());
            }
            if (file == null) {
                e.consume();
            } else {
                getViewModel().onContextRequested(file);
            }
        });
        // Only a left click or Enter opens a node; selecting it otherwise, such as with the right button or the
        // arrow keys, leaves the directory as it is.
        treeView.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                var file = findFile(e.getTarget());
                if (file != null) {
                    getViewModel().onNodeOpened(file);
                }
            }
        });
        treeView.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            var selectedItem = treeView.getSelectionModel().getSelectedItem();
            if (e.getCode() == KeyCode.ENTER && selectedItem != null && selectedItem.getValue() != null) {
                getViewModel().onNodeOpened(selectedItem.getValue());
            }
        });
    }

    private @Nullable GreyWolfFile findFile(Object target) {
        var node = target instanceof Node targetNode ? targetNode : null;
        while (node != null && !(node instanceof TreeCell)) {
            if (node.getStyleClass().contains("tree-disclosure-node")) {
                return null; // the arrow that expands a node is not the node itself
            }
            node = node.getParent();
        }
        return node instanceof TreeCell<?> cell && cell.getItem() instanceof GreyWolfFile file ? file : null;
    }

    private TreeItem<GreyWolfFile> createItem(GreyWolfFile file, boolean expandable) {
        var item = new TreeItem<>(file);
        if (expandable) {
            // dummy child (value == null) only to make the disclosure arrow appear before real data is loaded
            item.getChildren().add(new TreeItem<>());
        }
        item.expandedProperty().addListener((obs, was, isExpanded) -> {
            if (isExpanded && isDummy(item)) {
                getViewModel().onNodeExpanded(file);
            }
        });
        return item;
    }

    private boolean isDummy(TreeItem<GreyWolfFile> item) {
        return item.getChildren().size() == 1 && item.getChildren().get(0).getValue() == null;
    }

    private void addStorages(int position, List<? extends GreyWolfStorage> storages) {
        var items = new ArrayList<TreeItem<GreyWolfFile>>(storages.size());
        for (var storage : storages) {
            var dir = storage.getRoot();
            // a storage root is assumed to be expandable, except the recent one: it is a flat list of files that
            // belong to other storages, which show them in their own branches
            var item = createItem(dir, storage.getCategory() != StorageCategory.RECENT);
            index.put(dir, item);
            items.add(item);
        }
        root.getChildren().addAll(Math.min(position, root.getChildren().size()), items);
    }

    private void removeStorages(List<? extends GreyWolfStorage> storages) {
        for (var storage : storages) {
            var item = index.remove(storage.getRoot());
            if (item != null) {
                fileDecorator.reset(removeSubtreeFromIndex(item, index));
                root.getChildren().remove(item);
            }
        }
    }

    private void applyChildren(GreyWolfFile parent, List<GreyWolfFile> children) {
        var item = index.get(parent);
        if (item == null) {
            // node might have been removed from the tree in the meantime
            return;
        }
        var removed = removeSubtreeFromIndex(item, index);
        fileDecorator.reset(removed);
        logger.debug("{} Cleared {} stale index entries before setting children of {}",
                getDescriptor().getLogPrefix(), removed.size(), parent.getUri());
        // clears the dummy placeholder as a side effect too
        item.getChildren().clear();
        for (var child : children) {
            var childItem = createItem(child, child.isDirectory() || child.isDirectoryLink());
            index.put(child, childItem);
            item.getChildren().add(childItem);
        }
        // if children is empty, no dummy is re-added, so the disclosure arrow disappears on its own
    }

    private void applyExpandPath(List<GreyWolfFile> path) {
        for (var file : path) {
            var item = index.get(file);
            if (item != null) {
                item.setExpanded(true);
            }
        }
    }

    private void applyDirectory(GreyWolfFile directory) {
        var item = index.get(directory);
        if (item != null) {
            treeView.getSelectionModel().select(item);
            TreeViewUtils.scrollToIfNeeded(treeView, treeView.getRow(item), ScrollPosition.CENTER);
        }
    }
}
