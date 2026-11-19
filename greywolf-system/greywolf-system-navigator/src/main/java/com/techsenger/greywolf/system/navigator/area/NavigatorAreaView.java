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

package com.techsenger.greywolf.system.navigator.area;

import com.google.errorprone.annotations.concurrent.LazyInit;
import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.core.GreyWolfSettings;
import com.techsenger.greywolf.core.panels.PanelTabDockView;
import com.techsenger.greywolf.system.navigator.NavigatorFileColumns;
import com.techsenger.greywolf.system.navigator.NavigatorTabView;
import com.techsenger.greywolf.system.navigator.mark.MarkDialogParams;
import com.techsenger.greywolf.system.navigator.mark.MarkDialogPort;
import com.techsenger.greywolf.system.navigator.mark.MarkDialogView;
import com.techsenger.greywolf.system.navigator.mark.MarkDialogViewModel;
import com.techsenger.greywolf.system.navigator.slots.NavigatorAreaSlots;
import com.techsenger.greywolf.system.navigator.style.NavigatorIconResolver;
import com.techsenger.greywolf.system.navigator.style.NavigatorIcons;
import com.techsenger.greywolf.system.navigator.style.NavigatorStyleResolver;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.ui.delete.DefaultDeleteComposer;
import com.techsenger.greywolf.storage.ui.delete.DeleteOperation;
import com.techsenger.greywolf.storage.ui.name.NameDialogParams;
import com.techsenger.greywolf.storage.ui.name.NameDialogView;
import com.techsenger.greywolf.storage.ui.name.NameDialogViewModel;
import com.techsenger.greywolf.storage.ui.properties.FilePropertiesDialogParams;
import com.techsenger.greywolf.storage.ui.properties.FilePropertiesDialogView;
import com.techsenger.greywolf.storage.ui.properties.FilePropertiesDialogViewModel;
import com.techsenger.greywolf.storage.ui.transfer.CopyOperation;
import com.techsenger.greywolf.storage.ui.transfer.DefaultTransferComposer;
import com.techsenger.greywolf.storage.ui.transfer.MoveOperation;
import com.techsenger.greywolf.storage.ui.transfer.TransferDialogParams;
import com.techsenger.greywolf.storage.ui.view.FileView;
import com.techsenger.shellfx.core.area.AbstractAreaView;
import com.techsenger.shellfx.core.config.ConfigManager;
import com.techsenger.shellfx.core.dialog.DialogPort;
import com.techsenger.shellfx.core.dialog.DialogView;
import com.techsenger.shellfx.core.registries.ControlBuilder;
import com.techsenger.shellfx.core.registries.ControlUtils;
import com.techsenger.shellfx.core.registries.Controls;
import com.techsenger.shellfx.core.registries.StylesheetRegistry;
import com.techsenger.shellfx.core.tab.TabContainerView;
import com.techsenger.shellfx.core.window.AcceleratorAware;
import com.techsenger.shellfx.dialogs.alert.AlertDialogParams;
import com.techsenger.shellfx.dialogs.alert.AlertDialogPort;
import com.techsenger.shellfx.dialogs.alert.AlertDialogView;
import com.techsenger.shellfx.dialogs.alert.AlertDialogViewModel;
import com.techsenger.shellfx.dialogs.text.TextsDialogPort;
import com.techsenger.shellfx.material.column.ColumnListCell;
import com.techsenger.shellfx.material.column.ColumnListView;
import com.techsenger.shellfx.material.column.ColumnTileView;
import com.techsenger.shellfx.material.column.ColumnViewUtils;
import com.techsenger.shellfx.material.column.TileCell;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.pane.WaitingPane;
import com.techsenger.shellfx.material.style.Spacing;
import com.techsenger.shellfx.material.style.StyleClasses;
import com.techsenger.shellfx.material.table.FlexColumnResizePolicy;
import com.techsenger.shellfx.material.table.TableColumnInfoAdapter;
import com.techsenger.shellfx.material.table.TableColumnManager;
import com.techsenger.shellfx.material.table.TableColumnName;
import com.techsenger.shellfx.material.table.TableViewUtils;
import com.techsenger.shellfx.storage.ui.DecorationCellUtils;
import com.techsenger.shellfx.storage.ui.FileCellUpdater;
import com.techsenger.shellfx.storage.ui.FileColumns;
import com.techsenger.shellfx.storage.ui.style.FileDecorator;
import com.techsenger.shellfx.storage.ui.style.FileIconResolver;
import com.techsenger.shellfx.storage.ui.style.FileStyleResolver;
import com.techsenger.toolkit.fx.Spacer;
import com.techsenger.toolkit.fx.utils.ScrollPosition;
import com.techsenger.toolkit.fx.utils.TableUtils;
import com.techsenger.toolkit.fx.value.ValueUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public final class NavigatorAreaView extends AbstractAreaView<NavigatorAreaViewModel>
        implements AcceleratorAware, FileView<NavigatorAreaViewModel> {

    private static boolean isMarkRangeKey(KeyCode code) {
        return code == KeyCode.UP || code == KeyCode.DOWN || code == KeyCode.LEFT || code == KeyCode.RIGHT
                || code == KeyCode.HOME || code == KeyCode.END;
    }

    public class Composer extends AbstractAreaView<NavigatorAreaViewModel>.Composer
            implements NavigatorAreaComposer, FileView.Composer {

        @LazyInit
        private NavigatorTabView tab;

        public NavigatorTabView getTab() {
            return this.tab;
        }

        @Override
        public @Nullable TabContainerView<?> getTabContainer() {
            return tab.getComposer().getParent();
        }

        @Override
        public MarkDialogPort openMarkDialog(MarkDialogParams params) {
            var view = createMarkDialog(params);
            addDialog(view);
            return view.getViewModel();
        }

        @Override
        public TextsDialogPort openNameDialog(NameDialogParams params) {
            var view = createNameDialog(params);
            addDialog(view);
            return view.getViewModel();
        }

        @Override
        public AlertDialogPort openAlertDialog(AlertDialogParams params) {
            var view = createAlertDialog(params);
            addDialog(view);
            return view.getViewModel();
        }

        @Override
        public CopyOperation createCopyOperation(TransferDialogParams params, @Nullable GreyWolfFile targetDirectory) {
            var composer = new DefaultTransferComposer(getTab().getComposer(), getStylesheetRegistry());
            return new CopyOperation(composer, params, targetDirectory);
        }

        @Override
        public MoveOperation createMoveOperation(TransferDialogParams params, @Nullable GreyWolfFile targetDirectory) {
            var composer = new DefaultTransferComposer(getTab().getComposer(), getStylesheetRegistry());
            return new MoveOperation(composer, params, targetDirectory);
        }

        @Override
        public DeleteOperation createDeleteOperation(ConfigManager configManager, GreyWolfSettings settings) {
            var composer = new DefaultDeleteComposer(getTab().getComposer(), getStylesheetRegistry());
            return new DeleteOperation(composer, configManager, settings);
        }

        @Override
        public DialogPort openFilePropertiesDialog(FilePropertiesDialogParams params) {
            var view = createFilePropertiesDialog(params);
            addDialog(view);
            return view.getViewModel();
        }

        protected AlertDialogView<?> createAlertDialog(AlertDialogParams params) {
            var viewModel = new AlertDialogViewModel<>(params);
            var view = new AlertDialogView<>(viewModel, getStylesheetRegistry());
            view.initialize();
            return view;
        }

        protected FilePropertiesDialogView createFilePropertiesDialog(FilePropertiesDialogParams params) {
            var viewModel = new FilePropertiesDialogViewModel<>(params);
            var dialogView = new FilePropertiesDialogView(viewModel, getStylesheetRegistry());
            dialogView.initialize();
            return dialogView;
        }

        protected MarkDialogView createMarkDialog(MarkDialogParams params) {
            var viewModel = new MarkDialogViewModel<>(params);
            var dialogView = new MarkDialogView(viewModel, getStylesheetRegistry());
            dialogView.initialize();
            return dialogView;
        }

        protected NameDialogView createNameDialog(NameDialogParams params) {
            var viewModel = new NameDialogViewModel<>(params);
            var dialogView = new NameDialogView(viewModel, getStylesheetRegistry());
            dialogView.initialize();
            return dialogView;
        }

        private StylesheetRegistry getStylesheetRegistry() {
            return getTab().getComposer().getShell().getContext().getStylesheetRegistry();
        }

        private void addDialog(DialogView<?> dialog) {
            getTab().getComposer().addDialog(dialog);
        }

        private void setTab(NavigatorTabView tab) {
            this.tab = tab;
        }
    }

    private final class FileListCell extends ColumnListCell<GreyWolfFile> {

        private final FontIconView iconView = new FontIconView();

        private final FileCellUpdater<GreyWolfFile> updater = new FileCellUpdater<>(file ->
                DecorationCellUtils.updateDecoration(this, iconView, decorator.getDecoration(file)));

        private FileListCell() {
            addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                if (e.getClickCount() == 2 && !isEmpty()) {
                    getViewModel().onOpen();
                }
            });
        }

        @Override
        protected void updateItem(GreyWolfFile item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                updater.unobserve();
                DecorationCellUtils.updateStyles(this, iconView, null);
                setGraphic(null);
                setText(null);
            } else {
                decorator.decorate(item);
                updater.observe(item, decorator::decorationValue);
                setGraphic(iconView);
                setText(item.isParentEntry() ? ".." : item.getName());
            }
        }
    }

    private final class FileTileCell extends TileCell<GreyWolfFile> {

        private final FontIconView iconView = new FontIconView();

        private final FileCellUpdater<GreyWolfFile> updater = new FileCellUpdater<>(file ->
                DecorationCellUtils.updateDecoration(this, iconView, decorator.getDecoration(file)));

        private FileTileCell() {
            addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                if (e.getClickCount() == 2 && !isEmpty()) {
                    getViewModel().onOpen();
                }
            });
        }

        @Override
        protected void updateItem(GreyWolfFile item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                updater.unobserve();
                DecorationCellUtils.updateStyles(this, iconView, null);
                setGraphic(null);
                setText(null);
            } else {
                decorator.decorate(item);
                updater.observe(item, decorator::decorationValue);
                setGraphic(iconView);
                setText(item.isParentEntry() ? ".." : item.getName());
            }
        }
    }

    private final class FileTableRow extends TableRow<GreyWolfFile> {

        private FileTableRow() {
            setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !isEmpty()) {
                    getViewModel().onOpen();
                }
            });
        }
    }

    private final FileDecorator<GreyWolfFile> decorator;

    private final StackPane node = new StackPane();

    private final VBox contentBox = new VBox();

    private final TableView<GreyWolfFile> tableView = new TableView<>();

    private final TableColumnManager<GreyWolfFile> columnManager = new TableColumnManager<>(tableView);

    private final ColumnListView<GreyWolfFile> listView = new ColumnListView<>();

    private final ColumnTileView<GreyWolfFile> tileView = new ColumnTileView<>();

    private final VBox fileBox = new VBox();

    private final Label fileInfoLabel = new Label();

    private final Separator infoDivider = new Separator(Orientation.VERTICAL);

    private final FontIconView folderCountIconView = new FontIconView(NavigatorIcons.FOLDER);

    private final Label folderCountLabel = new Label();

    private final FontIconView fileCountIconView = new FontIconView(NavigatorIcons.FILE);

    private final Label fileCountLabel = new Label();

    private final FontIconView markedCountIconView = new FontIconView(NavigatorIcons.MARKED_COUNT);

    private final Label markedCountLabel = new Label();

    private final HBox infoBox = new HBox(fileInfoLabel, new Spacer(Orientation.HORIZONTAL),
            infoDivider, folderCountIconView, folderCountLabel, fileCountIconView, fileCountLabel,
            markedCountIconView, markedCountLabel);

    private Controls<NavigatorAreaView, ContextMenu> contextMenuControls;

    /**
     * Set while the columns of {@link #tableView} are being replaced as a whole - the changes the column manager
     * reports meanwhile are not user's and must not reach the view model.
     */
    private boolean applyingColumns;


    public NavigatorAreaView(NavigatorAreaViewModel viewModel, NavigatorTabView tab) {
        this(viewModel, tab, new NavigatorIconResolver(), new NavigatorStyleResolver());
    }

    public NavigatorAreaView(NavigatorAreaViewModel viewModel, NavigatorTabView tab,
            FileIconResolver<? super GreyWolfFile> iconResolver,
            FileStyleResolver<? super GreyWolfFile> styleResolver) {
        super(viewModel);
        getComposer().setTab(tab);
        this.decorator = new FileDecorator<>(iconResolver, styleResolver, viewModel::getDecorationType,
                () -> viewModel.getSettings().getAppearance().getTheme());
    }

    @Override
    public void requestFocus() {
        if (getViewModel().getViewModeType() == ViewModeType.LIST) {
            this.listView.requestFocus();
        } else if (getViewModel().getViewModeType() == ViewModeType.TILES) {
            this.tileView.requestFocus();
        } else {
            this.tableView.requestFocus();
        }
    }

    @Override
    public Region getNode() {
        return node;
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    public List<MenuItem> getAcceleratorItems() {
        var tab = getComposer().getTab();
        var panelActive = tab.getComposer().getParent() instanceof PanelTabDockView dock
                && dock.getViewModel().isActive();
        return tab.getViewModel().isSelected() && panelActive
                ? ControlUtils.getMenuItems(contextMenuControls) : List.of();
    }

    @Override
    protected Composer createComposer() {
        return new NavigatorAreaView.Composer();
    }

    @Override
    protected void build() {
        super.build();

        tableView.getStyleClass().addAll(StyleClasses.NO_BORDER, "file-table");
        tableView.setPlaceholder(new Label());
        this.tableView.setSortPolicy(tv -> {
            // bottom layer: column comparators (user-defined column rules)
            // middle layer: TableView aggregate comparator (built from sortOrder)
            // top layer: sorting execution hook (applies comparator to items)
            Comparator<GreyWolfFile> base = tv.getComparator();
            Comparator<GreyWolfFile> decorated = Comparators.directoryFirst(base);
            FXCollections.sort(tv.getItems(), decorated);
            return true;
        });
        tableView.setRowFactory(tv -> createFileRow());
        var columnBuilder = new NavigatorColumnBuilder(getViewModel().getSettings().getAppearance().getRegularFont(),
                decorator);
        columnManager.registerColumnFactory(FileColumns.NAME,
                () -> columnBuilder.buildNameColumn());
        columnManager.registerColumnFactory(FileColumns.SIZE, () -> {
            var column = columnBuilder.buildSizeColumn();
            var prefWidth = column.getMaxWidth();
            column.setResizable(true);
            column.setMinWidth(prefWidth / 2);
            column.setMaxWidth(Double.MAX_VALUE);
            column.setPrefWidth(prefWidth);
            return column;
        });
        columnManager.registerColumnFactory(NavigatorFileColumns.TYPE,
                () -> columnBuilder.buildTypeColumn());
        columnManager.registerColumnFactory(FileColumns.LAST_MODIFIED, () -> {
            var column = columnBuilder.buildLastModifiedColumn();
            var prefWidth = column.getMaxWidth();
            column.setResizable(true);
            column.setMinWidth(prefWidth / 2);
            column.setMaxWidth(Double.MAX_VALUE);
            column.setPrefWidth(prefWidth);
            return column;
        });
        // Listeners must be wired before addColumns(): createColumn() attaches a per-column width/sortType
        // listener immediately, and building the columns can itself trigger an initial resize (via
        // setColumnResizePolicy below), which fires those listeners synchronously.
        this.columnManager.setWidthListener((name, width) -> {
            if (!applyingColumns) {
                getViewModel().onColumnWidthChanged(name, width);
            }
        });
        this.columnManager.setSortTypeListener((name, sortType) -> {
            if (!applyingColumns) {
                getViewModel().onColumnSortTypeChanged(name, sortType);
            }
        });
        this.columnManager.setIndexListener((name, index) -> {
            if (!applyingColumns) {
                getViewModel().onColumnIndexChanged(name, index);
            }
        });
        this.columnManager.setSortIndexListener((name, index) -> {
            if (!applyingColumns) {
                getViewModel().onColumnSortIndexChanged(name, index);
            }
        });
        columnManager.addColumns(getViewModel().getColumnInfos().values());
        updateColumnResizePolicy();

        listView.setItems(tableView.getItems());
        listView.setManualRefresh(true);
        listView.setCellFactory(lv -> {
            var cell = new FileListCell();
            cell.addEventFilter(MouseEvent.MOUSE_PRESSED, createCellMarkHandler(cell));
            return cell;
        });
        listView.getStyleClass().addAll(StyleClasses.NO_BORDER, "file-list");

        tileView.setItems(tableView.getItems());
        tileView.setManualRefresh(true);
        tileView.setCellFactory(tv -> {
            var cell = new FileTileCell();
            cell.addEventFilter(MouseEvent.MOUSE_PRESSED, createCellMarkHandler(cell));
            return cell;
        });
        tileView.getStyleClass().addAll(StyleClasses.NO_BORDER, "file-tiles");

        VBox.setVgrow(fileBox, Priority.ALWAYS);
        applyFileBox(tableView);

        fileInfoLabel.setTooltip(new Tooltip());
        HBox.setHgrow(fileInfoLabel, Priority.ALWAYS);
        fileInfoLabel.setMaxWidth(Double.MAX_VALUE);

        infoDivider.setPadding(new Insets(0));
        infoBox.getStyleClass().add("info-box");
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setSpacing(Spacing.getHorizontalHalf());
        infoBox.setPadding(new Insets(Spacing.getVerticalHalf(), Spacing.getHorizontal(),
                Spacing.getVerticalHalf(), Spacing.getHorizontal()));

        contentBox.getChildren().addAll(fileBox, infoBox);
        node.getChildren().add(contentBox);
        VBox.setVgrow(node, Priority.ALWAYS);

        var context = getComposer().getTab().getComposer().getShell().getContext();
        var controlBuilder = new ControlBuilder(context.getSlotRegistry(), context.getControlRegistry());
        this.contextMenuControls = controlBuilder.buildContextMenu(this, NavigatorAreaSlots.AreaMenu.MENU);
        var contextMenu = contextMenuControls.root();
        tableView.setContextMenu(contextMenu);
        listView.setContextMenu(contextMenu);
        tileView.setContextMenu(contextMenu);
    }

    @Override
    protected void unbuild() {
        contextMenuControls.root().setOnHidden(null);
        tableView.setContextMenu(null);
        listView.setContextMenu(null);
        tileView.setContextMenu(null);
        contextMenuControls.deinitializeAll(this);
        super.unbuild();
    }

    @Override
    protected void bind() {
        super.bind();
        var viewModel = getViewModel();
        viewModel.fileComparatorWrapper().bind(Bindings.createObjectBinding(
                () -> Comparators.<GreyWolfFile>directoryFirst(tableView.getComparator()),
                tableView.comparatorProperty()));
        fileInfoLabel.textProperty().bind(viewModel.fileInfoProperty());
        fileInfoLabel.getTooltip().textProperty().bind(viewModel.fileInfoProperty());
        folderCountLabel.textProperty().bind(viewModel.folderCountProperty().asString());
        fileCountLabel.textProperty().bind(viewModel.fileCountProperty().asString());
        markedCountLabel.textProperty().bind(viewModel.markedCountProperty().asString());
        WaitingPane.install(node, viewModel.waitingProperty());
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        var viewModel = getViewModel();
        viewModel.getFiles().addListener((ListChangeListener<GreyWolfFile>) change -> {
            while (change.next()) {
                decorator.reset(change.getRemoved());
            }
            applyFiles(viewModel.getFiles());
        });
        viewModel.fileSource().addListener((file) -> applyFile(file));
        viewModel.fileIndexSource().addListener((index) -> applyFileIndex(index));
        ValueUtils.callAndAddListener(viewModel.viewModeProperty(), (ov, oldV, newV) -> updateViewMode());
        viewModel.updateViewModeSource().addListener((v) -> updateViewModeContent());
        viewModel.decorateSource().addListener((files) -> updateDecorations(files, false));
        viewModel.restyleFileSource().addListener((file) -> updateDecoration(file));
        viewModel.restyleFilesSource().addListener((files) -> updateDecorations(files, true));
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        var keyPressedHandler = createKeyPressedHandler();
        // addEventFilter, not addEventHandler/setOnKeyPressed: ColumnListView/ColumnTileView install their
        // own unconditional UP/DOWN/LEFT/RIGHT/HOME/END navigation via setOnKeyPressed in their constructors,
        // and a bubbling-phase handler on the same node still runs even after this one already consumed the
        // event (JavaFX's CompositeEventHandler doesn't check the consumed flag between handlers on one
        // node). A filter runs earlier, during the capturing phase, so consuming Shift+arrow here actually
        // prevents that unconditional navigation from also firing; keys this handler doesn't consume (plain
        // arrows, ENTER, TAB, INSERT) still reach it normally afterward.
        tableView.addEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);
        listView.addEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);
        tileView.addEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);
        var mousePressedHandler = createMousePressedHandler();
        // Only resets the mark-range anchor here; the Ctrl/Shift mark-click dispatch itself is attached
        // per-cell/row instead (see createCellMarkHandler(), wired in at cell/row creation time in
        // build()), since each cell/row already knows its own index and there's no need to resolve it
        // from an ancestor. Kept at this ancestor level (not per-cell) specifically because a press on
        // empty space below the last entry should still reset the anchor.
        tableView.addEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        listView.addEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        tileView.addEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        // A filter, so the file the menu is asked for is known before the menu of the control is shown.
        var contextMenuHandler = createContextMenuHandler();
        tableView.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, contextMenuHandler);
        listView.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, contextMenuHandler);
        tileView.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, contextMenuHandler);
        contextMenuControls.root().setOnHidden(e -> getViewModel().contextTargetWrapper().set(ContextTarget.NONE));
        // Holding Shift while using the mouse wheel is commonly translated by the platform into a horizontal
        // scroll gesture (deltaX set, deltaY zero) instead of the usual vertical one - harmless for listView,
        // which already scrolls horizontally, but tableView/tileView only scroll vertically and have nothing
        // bound to a horizontal delta, so Shift+wheel silently does nothing on them. Redirect it back to a
        // vertical scroll of the same magnitude.
        tableView.addEventFilter(ScrollEvent.SCROLL, createShiftScrollHandler(tableView));
        tileView.addEventFilter(ScrollEvent.SCROLL, createShiftScrollHandler(tileView));
    }

    TableView<GreyWolfFile> getTableView() {
        return tableView;
    }

    TableColumnManager<GreyWolfFile> getColumnManager() {
        return columnManager;
    }

    ColumnListView<GreyWolfFile> getListView() {
        return listView;
    }

    ColumnTileView<GreyWolfFile> getTileView() {
        return tileView;
    }

    VBox getFileBox() {
        return fileBox;
    }

    Label getFileInfoLabel() {
        return fileInfoLabel;
    }

    Separator getInfoDivider() {
        return infoDivider;
    }

    HBox getInfoBox() {
        return infoBox;
    }

    /**
     * Returns the selected index of whichever of the table/list/tiles controls is currently active.
     */
    private int getFileIndex() {
        return switch (getViewModel().getViewModeType()) {
            case LIST -> listView.getSelectionModel().getSelectedIndex();
            case TILES -> tileView.getSelectionModel().getSelectedIndex();
            case DETAILS -> tableView.getSelectionModel().getSelectedIndex();
        };
    }

    private EventHandler<KeyEvent> createKeyPressedHandler() {
        return e -> {
            var viewModel = getViewModel();
            if (!e.isShiftDown()) {
                viewModel.onMarkRangeFinished();
            }
            if (e.getCode() == KeyCode.ENTER) {
                viewModel.onOpen();
            } else if (e.getCode() == KeyCode.TAB) {
                e.consume();
                viewModel.onChangePanel();
            } else if (e.getCode() == KeyCode.INSERT) {
                viewModel.onMark(getFileIndex());
                selectNextFile();
            } else if (e.getCode() == KeyCode.BACK_SPACE) {
                viewModel.onUp();
            } else if (e.isControlDown() && e.getCode() == KeyCode.R) {
                viewModel.onRefresh();
            } else if (e.isAltDown() && e.getCode() == KeyCode.LEFT) {
                e.consume();
                viewModel.onBack();
            } else if (e.isAltDown() && e.getCode() == KeyCode.RIGHT) {
                e.consume();
                viewModel.onForward();
            } else if (e.isShiftDown() && isMarkRangeKey(e.getCode())) {
                e.consume();
                markRange(e.getCode());
            }
        };
    }

    /**
     * Creates the handler that finds the file the context menu is asked for - the one under the pointer, or the
     * focused one when the menu is asked for with the keyboard - and selects it without scrolling to it; on an empty
     * place there is no such file, and the target of the menu is the place itself.
     */
    private EventHandler<ContextMenuEvent> createContextMenuHandler() {
        return e -> {
            var viewModel = getViewModel();
            GreyWolfFile file;
            if (e.isKeyboardTrigger()) {
                file = viewModel.getFile();
            } else {
                file = findEntry(e.getTarget());
                if (file != null) {
                    selectEntry(e.getSource(), file);
                }
            }
            viewModel.contextTargetWrapper().set(file == null ? ContextTarget.SPACE : ContextTarget.FILE);
        };
    }

    /**
     * Selects {@code file} in {@code control} the way a click does, leaving the scroll position alone: the entry is
     * already visible, since it was clicked.
     */
    private void selectEntry(Object control, GreyWolfFile file) {
        if (control == tableView) {
            tableView.getSelectionModel().select(file);
        } else if (control == listView) {
            listView.getSelectionModel().select(file);
        } else if (control == tileView) {
            tileView.getSelectionModel().select(file);
        }
    }

    /**
     * Returns the entry shown by the cell that {@code target} belongs to, or {@code null} if it is not in a cell
     * or the cell is empty.
     */
    private @Nullable GreyWolfFile findEntry(Object target) {
        var node = target instanceof Node n ? n : null;
        while (node != null) {
            if (node instanceof IndexedCell<?> cell && !(cell instanceof TableCell<?, ?>)) {
                return !cell.isEmpty() && cell.getItem() instanceof GreyWolfFile file ? file : null;
            }
            node = node.getParent();
        }
        return null;
    }

    /**
     * Resets the Shift+arrow mark range's anchor on any plain (non-Shift) mouse press, the mouse
     * counterpart of the same reset the key handler above does for any non-Shift key.
     */
    private EventHandler<MouseEvent> createMousePressedHandler() {
        return e -> {
            if (!e.isShiftDown()) {
                getViewModel().onMarkRangeFinished();
            }
        };
    }

    /**
     * Ctrl+press toggles {@code cell}'s mark, Shift+press extends the mark range to it (same as
     * Shift+arrow) - attached directly to each cell/row at creation time (see {@code build()}), since it
     * already knows its own index; neither consumes the event, so the press still moves the selection
     * there as usual afterward.
     */
    private EventHandler<MouseEvent> createCellMarkHandler(IndexedCell<?> cell) {
        return e -> {
            if (cell.isEmpty() || e.getClickCount() != 1) {
                return;
            }
            if (e.isShiftDown()) {
                getViewModel().onMarkRange(cell.getIndex());
            } else if (e.isControlDown()) {
                getViewModel().onMark(cell.getIndex());
            }
        };
    }

    /**
     * Redirects a Shift+wheel scroll gesture on {@code control} back into a vertical scroll, undoing the
     * platform's own translation of it into a horizontal one - see the comment at the call site in
     * {@link #addHandlers()}.
     */
    private EventHandler<ScrollEvent> createShiftScrollHandler(Node control) {
        return e -> {
            if (e.isShiftDown() && e.getDeltaY() == 0 && e.getDeltaX() != 0) {
                var flow = (VirtualFlow<?>) control.lookup(".virtual-flow");
                if (flow != null) {
                    flow.scrollPixels(-e.getDeltaX());
                    e.consume();
                }
            }
        };
    }

    /**
     * Resolves the Shift+{@code code} target index for the current mode and moves the mark range there,
     * delegating the actual marking to the view model.
     */
    private void markRange(KeyCode code) {
        var newIndex = switch (getViewModel().getViewModeType()) {
            case LIST -> resolveListRangeIndex(code);
            case TILES -> resolveTilesRangeIndex(code);
            case DETAILS -> resolveDetailsRangeIndex(code);
        };
        if (newIndex < 0) {
            return;
        }
        getViewModel().onMarkRange(newIndex);
        selectRangeIndex(newIndex);
    }

    /**
     * Moves the selection to {@code index}, scrolling only when it isn't already fully visible - landing at
     * the trailing edge when moving forward, the leading edge when moving backward - unlike
     * {@link #applyFileIndex}, which always re-centers regardless of current visibility.
     */
    private void selectRangeIndex(int index) {
        if (getViewModel().getViewModeType() == ViewModeType.LIST) {
            var position = index > listView.getSelectionModel().getSelectedIndex()
                    ? ScrollPosition.END : ScrollPosition.START;
            ColumnViewUtils.scrollToIfNeeded(listView, index, position);
            listView.getSelectionModel().select(index);
        } else if (getViewModel().getViewModeType() == ViewModeType.TILES) {
            var position = index > tileView.getSelectionModel().getSelectedIndex()
                    ? ScrollPosition.END : ScrollPosition.START;
            ColumnViewUtils.scrollToIfNeeded(tileView, index, position);
            tileView.getSelectionModel().select(index);
        } else {
            var position = index > tableView.getSelectionModel().getSelectedIndex()
                    ? ScrollPosition.END : ScrollPosition.START;
            TableUtils.scrollToIfNeeded(tableView, index, position);
            tableView.getSelectionModel().select(index);
        }
    }

    /**
     * Details mode is a single, flat sequence, so only Up/Down/Home/End move the range; Left/Right have no
     * meaningful direction here and are ignored.
     */
    private int resolveDetailsRangeIndex(KeyCode code) {
        var current = tableView.getSelectionModel().getSelectedIndex();
        var last = tableView.getItems().size() - 1;
        return switch (code) {
            case UP -> current > 0 ? current - 1 : -1;
            case DOWN -> current < last ? current + 1 : -1;
            case HOME -> current > 0 ? 0 : -1;
            case END -> current < last ? last : -1;
            default -> -1;
        };
    }

    /**
     * List mode lays files out in vertical columns, so Left/Right land on the same row in the
     * next/previous column, same as plain (non-range) navigation - falling back to Home/End at the
     * first/last column.
     */
    private int resolveListRangeIndex(KeyCode code) {
        var current = listView.getSelectionModel().getSelectedIndex();
        var last = listView.getItems().size() - 1;
        var rowCount = listView.getRowCount();
        return switch (code) {
            case UP -> current > 0 ? current - 1 : -1;
            case DOWN -> current < last ? current + 1 : -1;
            case LEFT -> current - rowCount >= 0 ? current - rowCount : (current > 0 ? 0 : -1);
            case RIGHT -> listView.resolveColumnIndex(current) >= listView.getColumnCount() - 1
                    ? (current < last ? last : -1)
                    : Math.min(current + rowCount, last);
            case HOME -> current > 0 ? 0 : -1;
            case END -> current < last ? last : -1;
            default -> -1;
        };
    }

    /**
     * Tiles mode lays files out row-major, so Up/Down land on the same column in the next/previous row,
     * same as plain (non-range) navigation - falling back to Home/End at the first/last row.
     */
    private int resolveTilesRangeIndex(KeyCode code) {
        var current = tileView.getSelectionModel().getSelectedIndex();
        var last = tileView.getItems().size() - 1;
        var columnCount = tileView.getColumnCount();
        return switch (code) {
            case LEFT -> current > 0 ? current - 1 : -1;
            case RIGHT -> current < last ? current + 1 : -1;
            case UP -> current - columnCount >= 0 ? current - columnCount : (current > 0 ? 0 : -1);
            case DOWN -> tileView.resolveRowIndex(current) >= tileView.getRowCount() - 1
                    ? (current < last ? last : -1)
                    : Math.min(current + columnCount, last);
            case HOME -> current > 0 ? 0 : -1;
            case END -> current < last ? last : -1;
            default -> -1;
        };
    }

    /**
     * Advances the selection to the next entry in whichever of the table/list/tiles controls is currently
     * active, if one exists; does nothing when the current selection is already the last entry. Scrolls only
     * when needed, landing the entry at the trailing edge of the viewport rather than centering it &mdash;
     * unlike {@link #applyFileIndex}, which always scrolls to center a specific, already-known entry.
     * Called directly after the view model marks the entry: advancing the cursor is this view's own
     * navigation concern, not something the view model needs to command.
     *
     * <p>Scrolls before selecting, deliberately not the other way around: the list/tile views only apply a
     * selection highlight to columns/rows currently positioned in the virtual flow, so selecting an index
     * whose column/row hasn't been scrolled into position yet would leave the highlight unapplied until an
     * unrelated rebuild.
     */
    private void selectNextFile() {
        if (getViewModel().getViewModeType() == ViewModeType.LIST) {
            var index = listView.getSelectionModel().getSelectedIndex() + 1;
            if (index < listView.getItems().size()) {
                ColumnViewUtils.scrollToIfNeeded(listView, index, ScrollPosition.END);
                listView.getSelectionModel().select(index);
            }
        } else if (getViewModel().getViewModeType() == ViewModeType.TILES) {
            var index = tileView.getSelectionModel().getSelectedIndex() + 1;
            if (index < tileView.getItems().size()) {
                ColumnViewUtils.scrollToIfNeeded(tileView, index, ScrollPosition.END);
                tileView.getSelectionModel().select(index);
            }
        } else {
            var index = tableView.getSelectionModel().getSelectedIndex() + 1;
            if (index < tableView.getItems().size()) {
                TableUtils.scrollToIfNeeded(tableView, index, ScrollPosition.END);
                tableView.getSelectionModel().select(index);
            }
        }
    }

    /**
     * Scrolls {@link #listView} so the column holding {@code itemIndex} is fully visible, among the fixed
     * number of columns the current list mode shows at once. Deliberately not the generic geometry-based
     * centering utility: that reads the currently rendered viewport column count, which can still reflect the
     * previous directory's layout right after navigating and show columns only partially. The target column
     * lands at the middle slot instead, unless it is the very first or last column overall, where scrolling to
     * that edge already shows it fully with no approximation needed.
     */
    private void scrollListViewToColumn(int itemIndex) {
        // Forces listView's pending row-height/offset resolution to run right now, synchronously, before
        // resolveColumnIndex()/getColumnCount() below are read - see the equivalent comment history on the
        // former NavigatorTabView#scrollListViewToColumn this was moved from.
        fileBox.applyCss();
        fileBox.layout();
        var columnIndex = listView.resolveColumnIndex(itemIndex);
        if (columnIndex == 0) {
            listView.scrollToFirstColumn(0);
        } else if (columnIndex == listView.getColumnCount() - 1) {
            listView.scrollToLastColumn(columnIndex);
        } else {
            var listMode = (ListViewMode) getViewModel().getViewMode();
            var offset = (listMode.getColumnCount() - 1) / 2;
            listView.scrollToFirstColumn(Math.max(0, columnIndex - offset));
        }
    }

    /**
     * The tile-view counterpart of {@link #scrollListViewToColumn(int)} - forces the same synchronous geometry
     * resolution first and for the same reason (see there).
     */
    private void scrollTileViewToRow(int itemIndex) {
        fileBox.applyCss();
        fileBox.layout();
        ColumnViewUtils.scrollTo(this.tileView, itemIndex, ScrollPosition.CENTER);
    }

    private TableRow<GreyWolfFile> createFileRow() {
        var row = new FileTableRow();
        row.addEventFilter(MouseEvent.MOUSE_PRESSED, createCellMarkHandler(row));
        return row;
    }

    private void applyFileBox(Node node) {
        fileBox.getChildren().setAll(node);
        VBox.setVgrow(node, Priority.ALWAYS);
    }

    private void applyFiles(List<GreyWolfFile> files) {
        this.tableView.getItems().setAll(files);
        this.tableView.scrollTo(0);
        if (getViewModel().getViewModeType() == ViewModeType.LIST) {
            this.listView.refresh();
        } else if (getViewModel().getViewModeType() == ViewModeType.TILES) {
            this.tileView.refresh();
        }
    }

    private void applyFile(@Nullable GreyWolfFile file) {
        if (file == null) {
            this.tableView.getSelectionModel().clearSelection();
            this.listView.getSelectionModel().clearSelection();
            this.tileView.getSelectionModel().clearSelection();
            return;
        }
        if (getViewModel().getViewModeType() == ViewModeType.LIST) {
            this.listView.getSelectionModel().select(file);
            if (!file.isParentEntry()) {
                var index = this.listView.getItems().indexOf(file);
                scrollListViewToColumn(index);
            }
        } else if (getViewModel().getViewModeType() == ViewModeType.TILES) {
            this.tileView.getSelectionModel().select(file);
            if (!file.isParentEntry()) {
                var index = this.tileView.getItems().indexOf(file);
                scrollTileViewToRow(index);
            }
        } else {
            this.tableView.getSelectionModel().select(file);
            if (!file.isParentEntry()) {
                var index = this.tableView.getItems().indexOf(file);
                TableUtils.scrollTo(this.tableView, index, ScrollPosition.CENTER);
            }
        }
    }

    private void applyFileIndex(int index) {
        if (getViewModel().getViewModeType() == ViewModeType.LIST) {
            this.listView.getSelectionModel().select(index);
            scrollListViewToColumn(index);
        } else if (getViewModel().getViewModeType() == ViewModeType.TILES) {
            this.tileView.getSelectionModel().select(index);
            scrollTileViewToRow(index);
        } else {
            this.tableView.getSelectionModel().select(index);
            TableUtils.scrollTo(this.tableView, index, ScrollPosition.CENTER);
        }
    }

    /**
     * Brings the table to the state of {@code infos} in place: hides and shows columns, then applies widths,
     * sort types, sort order and the order of columns. The column instances that stay are kept, since a
     * background listing may be sorting with a comparator built on them.
     */
    private void updateColumns(List<TableColumnInfoAdapter> infos) {
        applyingColumns = true;
        try {
            var columnsByName = columnManager.getColumnsByName();
            var infosByName = new HashMap<TableColumnName, TableColumnInfoAdapter>();
            for (var info : infos) {
                infosByName.put(info.getName(), info);
            }
            var structureChanged = false;
            for (var name : new ArrayList<>(columnsByName.keySet())) {
                var info = infosByName.get(name);
                if (info == null || !info.isVisible()) {
                    columnManager.removeColumn(name);
                    structureChanged = true;
                }
            }
            for (var info : infos) {
                if (info.isVisible() && !columnsByName.containsKey(info.getName())) {
                    columnManager.addColumn(info);
                    structureChanged = true;
                }
            }
            var indexesByColumn = new IdentityHashMap<Object, Integer>();
            var sortedInfos = new ArrayList<TableColumnInfoAdapter>();
            for (var entry : columnsByName.entrySet()) {
                var info = Objects.requireNonNull(infosByName.get(entry.getKey()));
                var column = entry.getValue();
                indexesByColumn.put(column, info.getIndex());
                if (info.getWidth() != null) {
                    column.setPrefWidth(info.getWidth());
                }
                if (info.getSortType() != null) {
                    column.setSortType(info.getSortType());
                }
                if (info.getSortIndex() != null) {
                    sortedInfos.add(info);
                }
            }
            sortedInfos.sort(Comparator.comparingInt(TableColumnInfoAdapter::getSortIndex));
            var sortOrder = new ArrayList<TableColumn<GreyWolfFile, ?>>();
            for (var info : sortedInfos) {
                sortOrder.add(columnsByName.get(info.getName()));
            }
            if (!sortOrder.equals(tableView.getSortOrder())) {
                tableView.getSortOrder().setAll(sortOrder);
            }
            FXCollections.sort(tableView.getColumns(),
                    Comparator.comparingInt((TableColumn<GreyWolfFile, ?> column) -> indexesByColumn.get(column)));
            if (structureChanged) {
                updateColumnResizePolicy();
            }
        } finally {
            applyingColumns = false;
        }
    }

    private void updateColumnResizePolicy() {
        var nameColumn = Objects.requireNonNull(columnManager.getColumnsByName().get(FileColumns.NAME));
        tableView.setColumnResizePolicy(new FlexColumnResizePolicy(nameColumn));
    }

    /**
     * Makes the decoration of the files outdated and decorates again the files that are shown: the cells of the
     * current view mode that exist in its virtual flow are walked. The other files are decorated by the cells when
     * they come into view.
     */
    private void updateDecorations(List<GreyWolfFile> files, boolean styleOnly) {
        if (styleOnly) {
            decorator.resetStyle(files);
        } else {
            decorator.reset(files);
        }
        switch (getViewModel().getViewModeType()) {
            case LIST -> {
                ColumnViewUtils.forEachItem(listView, true, decorator::decorate);
                ColumnViewUtils.invalidateCells(listView, true);
            }
            case TILES -> {
                ColumnViewUtils.forEachItem(tileView, true, decorator::decorate);
                ColumnViewUtils.invalidateCells(tileView, true);
            }
            case DETAILS -> TableViewUtils.forEachItem(tableView, true, decorator::decorate);
        }
    }

    /**
     * Makes the style of the file outdated and resolves it again at once; the cell that shows the file is subscribed
     * to its decoration, so the flow is not walked.
     */
    private void updateDecoration(GreyWolfFile file) {
        decorator.resetStyle(List.of(file));
        decorator.decorate(file);
    }

    /**
     * Applies what changed inside the current view mode of the view model - the column count of the list or tiles
     * mode, or the columns of the table - to the controls.
     */
    private void updateViewModeContent() {
        var mode = getViewModel().getViewMode();
        if (mode instanceof ListViewMode listMode) {
            listView.setVisibleColumnCount(listMode.getColumnCount());
            listView.refresh();
        } else if (mode instanceof TilesViewMode tilesMode) {
            tileView.setColumnCount(tilesMode.getColumnCount());
            tileView.refresh();
        }
        updateColumns(List.copyOf(getViewModel().getColumnInfos().values()));
    }

    /**
     * Shows the control of the current view mode of the view model - table, list or tiles.
     */
    private void updateViewMode() {
        var mode = getViewModel().getViewMode();
        if (mode instanceof ListViewMode listMode) {
            listView.setVisibleColumnCount(listMode.getColumnCount());
            applyFileBox(listView);
            listView.refresh();
        } else if (mode instanceof TilesViewMode tilesMode) {
            tileView.setColumnCount(tilesMode.getColumnCount());
            applyFileBox(tileView);
            tileView.refresh();
        } else {
            applyFileBox(tableView);
            TableViewUtils.forEachItem(tableView, true, decorator::decorate);
        }
        // the selection of the view model is the selection of the control that is shown, whoever changes it
        var viewModel = getViewModel();
        viewModel.fileWrapper().unbind();
        viewModel.fileIndexWrapper().unbind();
        // unbinding keeps the index of the control that has been replaced; the new control selects it before being
        // bound, as all of them show the same entries - and only now, since showing it clears its selection
        var index = viewModel.getFileIndex();
        if (index >= 0) {
            applyFileIndex(index);
        }
        switch (viewModel.getViewModeType()) {
            case LIST -> {
                viewModel.fileWrapper().bind(listView.getSelectionModel().selectedItemProperty());
                viewModel.fileIndexWrapper().bind(listView.getSelectionModel().selectedIndexProperty());
            }
            case TILES -> {
                viewModel.fileWrapper().bind(tileView.getSelectionModel().selectedItemProperty());
                viewModel.fileIndexWrapper().bind(tileView.getSelectionModel().selectedIndexProperty());
            }
            case DETAILS -> {
                viewModel.fileWrapper().bind(tableView.getSelectionModel().selectedItemProperty());
                viewModel.fileIndexWrapper().bind(tableView.getSelectionModel().selectedIndexProperty());
            }
        }
    }
}
