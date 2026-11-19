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

import atlantafx.base.theme.Styles;
import com.google.errorprone.annotations.concurrent.LazyInit;
import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.shared.slots.LocationBarSlots.SegmentMenu;
import com.techsenger.greywolf.shared.style.SharedIcons;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.ui.view.FileView;
import com.techsenger.shellfx.core.area.AbstractAreaView;
import com.techsenger.shellfx.core.registries.ControlBuilder;
import com.techsenger.shellfx.core.registries.Controls;
import com.techsenger.shellfx.core.registries.StylesheetRegistry;
import com.techsenger.shellfx.core.tab.TabContainerView;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.style.Spacing;
import com.techsenger.shellfx.material.style.StyleClasses;
import com.techsenger.shellfx.storage.ui.style.DefaultStorageIconResolver;
import com.techsenger.shellfx.storage.ui.style.DefaultStorageStyleResolver;
import com.techsenger.shellfx.storage.ui.style.StorageDecorator;
import com.techsenger.toolkit.fx.value.ValueUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TextField;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Window;

/**
 *
 * @author Pavel Castornii
 */
public class LocationBarView<VM extends LocationBarViewModel<?>> extends AbstractAreaView<VM>
        implements FileView<VM> {

    private static final PseudoClass EDIT_MODE = PseudoClass.getPseudoClass("edit-mode");

    private static final double SPACING = 0;

    protected class Composer extends AbstractAreaView<VM>.Composer implements LocationBarComposer, FileView.Composer {

        @LazyInit
        private StylesheetRegistry stylesheetRegistry;

        @LazyInit
        private Supplier<@Nullable TabContainerView<?>> tabContainerSupplier;

        private @Nullable FolderPopupPort folderPopup;

        @Override
        public @Nullable TabContainerView<?> getTabContainer() {
            return tabContainerSupplier.get();
        }

        @Override
        public @Nullable FolderPopupPort openFolderPopup(FolderPopupParams params) {
            if (folderPopup != null) {
                folderPopup.closeSafely();
            }
            var anchor = clickedButton;
            if (anchor == null) {
                return null;
            }
            var view = createFolderPopup(params, anchor.getScene().getWindow());
            FolderPopupPort port = view.getViewModel();
            port.setOnClosed(() -> {
                this.folderPopup = null;
                if (arrowNodes.contains(anchor)) {
                    anchor.setGraphic(new FontIconView(SharedIcons.MENU_RIGHT));
                }
            });
            if (arrowNodes.contains(anchor)) {
                anchor.setGraphic(new FontIconView(SharedIcons.MENU_DOWN));
            }
            this.folderPopup = port;
            // the popup is a top-level window, so it is placed under the button in the screen coordinates
            var bounds = anchor.localToScreen(anchor.getBoundsInLocal());
            port.setX(bounds.getMinX());
            port.setY(bounds.getMaxY());
            return port;
        }

        @Override
        public @Nullable FolderPopupPort getFolderPopupPort() {
            return folderPopup;
        }

        protected FolderPopupView<?> createFolderPopup(FolderPopupParams params, Window owner) {
            var viewModel = new FolderPopupViewModel<>(params);
            var view = new FolderPopupView<>(viewModel, stylesheetRegistry, owner);
            view.initialize();
            return view;
        }

        private void setDependencies(StylesheetRegistry stylesheetRegistry,
                Supplier<@Nullable TabContainerView<?>> tabContainerSupplier) {
            this.stylesheetRegistry = stylesheetRegistry;
            this.tabContainerSupplier = tabContainerSupplier;
        }
    }

    private final HBox root = new HBox();

    private final List<GreyWolfFile> segments = new ArrayList<>();

    /** Parallel to the segments list: {@code segmentNodes.get(i)} is the button for {@code segments.get(i)}. */
    private final List<Node> segmentNodes = new ArrayList<>();

    /**
     * The trailing arrow for each segment, including the last, whose arrow lets the user browse its children
     * without entering edit mode. When segments collapse starting at index {@code t}, {@code
     * arrowNodes.get(t - 1)} connects the ellipsis to the first visible segment.
     */
    private final List<Node> arrowNodes = new ArrayList<>();

    private final Button ellipsisButton = new Button(null, new FontIconView(SharedIcons.HORIZONTAL_DOTS));

    private final Region trailingRegion = new Region();

    /**
     * Kept permanently mounted ({@code managed = true}) so its preferred height always participates in the
     * toolbar's row-height calculation, even while segments are shown instead of the text field.
     */
    private final TextField pathTextField = new TextField();

    /**
     * Index into the segments list of the first currently visible segment. {@code 0} means the full path is
     * shown (no ellipsis); a positive value means segments {@code [0, firstVisibleIndex)} are collapsed behind
     * the ellipsis.
     */
    private int firstVisibleIndex = 0;

    /**
     * The button the user clicked last, which the popup of the clicked segment, arrow or ellipsis is shown under.
     * Cleared whenever the path changes, since a stale button would no longer exist.
     */
    private @Nullable Button clickedButton;

    private final ControlBuilder controlBuilder;

    private final StorageDecorator storageDecorator;

    @LazyInit
    private Controls<LocationBarView<VM>, ContextMenu> contextMenuControls;

    /**
     * Creates the location bar.
     *
     * @param viewModel the view model
     * @param stylesheetRegistry the registry of the stylesheets that every window applies
     * @param controlBuilder builds the context menu of a segment from what is registered for its slot
     * @param tabContainerSupplier provides the container of the tab the bar belongs to, or {@code null} while the tab
     *     is not attached to one
     */
    public LocationBarView(VM viewModel, StylesheetRegistry stylesheetRegistry, ControlBuilder controlBuilder,
            Supplier<@Nullable TabContainerView<?>> tabContainerSupplier) {
        super(viewModel);
        this.controlBuilder = controlBuilder;
        this.storageDecorator = new StorageDecorator(new DefaultStorageIconResolver(),
                new DefaultStorageStyleResolver(), viewModel::getTheme);
        getComposer().setDependencies(stylesheetRegistry, tabContainerSupplier);
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    public void requestFocus() {
        root.requestFocus();
    }

    @Override
    public Region getNode() {
        return root;
    }

    @Override
    protected Composer createComposer() {
        return new Composer();
    }

    @Override
    protected void build() {
        super.build();
        ellipsisButton.setManaged(false);
        ellipsisButton.setVisible(false);
        ellipsisButton.getStyleClass().addAll(StyleClasses.SIZE_S, StyleClasses.SQUARE, Styles.FLAT);
        HBox.setHgrow(trailingRegion, Priority.ALWAYS);
        pathTextField.getStyleClass().add(StyleClasses.NO_BORDER);
        pathTextField.setManaged(true);
        pathTextField.setVisible(false);
        pathTextField.setMaxWidth(0); // zero footprint while hidden — see applyEnterEditMode/applyExitEditMode
        var css = LocationBarView.class.getResource("location-bar.css").toExternalForm();
        root.getStylesheets().add(css);
        root.getStyleClass().add("location-bar");
        root.setMaxWidth(Double.MAX_VALUE);
        root.setMinWidth(0);
        root.getChildren().addAll(ellipsisButton, trailingRegion, pathTextField);
        this.contextMenuControls = controlBuilder.buildContextMenu(this, SegmentMenu.MENU);
    }

    @Override
    protected void unbuild() {
        contextMenuControls.deinitializeAll(this);
        super.unbuild();
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        var viewModel = getViewModel();
        pathTextField.focusedProperty().addListener((obs, was, isFocused) -> {
            if (!isFocused && viewModel.isEditMode()) {
                viewModel.onEditCancelled();
            }
        });
        root.widthProperty().addListener((obs, oldW, newW) -> {
            if (!viewModel.isEditMode() && !segments.isEmpty()) {
                applyFit();
            }
        });
        ValueUtils.callAndAddListener(viewModel.showStorageIconProperty(), (ov, oldV, newV) ->
                applyShowStorageIcon(newV));
        viewModel.themeProperty().addListener((ov, oldV, newV) ->
                applyShowStorageIcon(viewModel.isShowStorageIcon()));
        viewModel.getPathFiles().addListener((ListChangeListener<GreyWolfFile>) change ->
                applySegments(viewModel.getPathFiles()));
        ValueUtils.callAndAddListener(viewModel.editModeProperty(), (ov, oldV, newV) -> {
            if (newV) {
                applyEnterEditMode(viewModel.getEditText());
            } else {
                applyExitEditMode();
            }
        });
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        var viewModel = getViewModel();
        ellipsisButton.setOnAction(e -> {
            clickedButton = ellipsisButton;
            viewModel.onHiddenSegmentsClicked(segments.subList(0, firstVisibleIndex));
        });
        trailingRegion.setOnMouseClicked(e -> viewModel.onEditPath());
        pathTextField.setOnAction(e -> viewModel.onPathEntered(pathTextField.getText()));
        pathTextField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                viewModel.onEditCancelled();
            }
        });
    }

    /**
     * Rebuilds the segment and arrow button nodes to match the current segments list, mounted unmanaged and
     * invisible until {@code applyFit()} decides which to show.
     */
    private void rebuildNodes() {
        root.getChildren().removeAll(segmentNodes);
        root.getChildren().removeAll(arrowNodes);
        segmentNodes.clear();
        arrowNodes.clear();
        clickedButton = null; // a stale button would no longer exist once the path changes
        var insertionIndex = 1; // right after ellipsisButton
        for (int i = 0; i < segments.size(); i++) {
            var segment = segments.get(i);
            var node = createSegmentNode(segment);
            node.setManaged(false);
            node.setVisible(false);
            segmentNodes.add(node);
            root.getChildren().add(insertionIndex++, node);

            var arrow = createArrowNode(segment); // every segment gets a trailing arrow, including the last
            arrow.setManaged(false);
            arrow.setVisible(false);
            arrowNodes.add(arrow);
            root.getChildren().add(insertionIndex++, arrow);
        }
        root.applyCss();
    }

    /**
     * Chooses how many trailing segments fit in the current width, reading already-mounted node widths — safe
     * to call on every resize pulse without creating nodes or causing flicker.
     */
    private void applyFit() {
        var n = segments.size();
        if (n == 0) {
            return;
        }
        var availableWidth = root.getWidth() - root.getPadding().getLeft() - root.getPadding().getRight();
        var fullWidth = widthFor(0, n, SPACING);
        var t = 0;
        if (fullWidth > availableWidth) {
            t = n - 1; // fallback: always show at least the last segment (and its trailing arrow), even if
                       // it still overflows
            for (int candidate = 1; candidate < n; candidate++) {
                var candidateWidth = widthFor(candidate, n, SPACING);
                if (candidateWidth <= availableWidth) {
                    t = candidate;
                    break;
                }
            }
        }
        firstVisibleIndex = t;
        setContentVisible(true);
    }

    /**
     * Computes the total width required to show segments {@code [tailStart, n)} — each with its own trailing
     * arrow — plus the ellipsis and its connecting arrow when {@code tailStart > 0}.
     */
    private double widthFor(int tailStart, int n, double spacing) {
        double width = 0;
        var nodeCount = 0;
        if (tailStart > 0) {
            width += ellipsisButton.prefWidth(-1) + arrowNodes.get(tailStart - 1).prefWidth(-1);
            nodeCount += 2;
        }
        for (int i = tailStart; i < n; i++) {
            width += segmentNodes.get(i).prefWidth(-1);
            width += arrowNodes.get(i).prefWidth(-1);
            nodeCount += 2;
        }
        return width + spacing * (nodeCount - 1);
    }

    private void setContentVisible(boolean visible) {
        ellipsisButton.setManaged(visible && firstVisibleIndex > 0);
        ellipsisButton.setVisible(visible && firstVisibleIndex > 0);
        for (int i = 0; i < segmentNodes.size(); i++) {
            var v = visible && i >= firstVisibleIndex;
            segmentNodes.get(i).setManaged(v);
            segmentNodes.get(i).setVisible(v);
        }
        for (int i = 0; i < arrowNodes.size(); i++) {
            var v = visible && i >= firstVisibleIndex - 1;
            arrowNodes.get(i).setManaged(v);
            arrowNodes.get(i).setVisible(v);
        }
        trailingRegion.setManaged(visible);
        trailingRegion.setVisible(visible);
    }

    private FontIconView createStorageIconView(GreyWolfFile root) {
        return new FontIconView(storageDecorator.decorate(root.getStorage()).icon());
    }

    private Node createSegmentNode(GreyWolfFile segment) {
        var isStorage = segment.isRoot();
        var button = new Button(isStorage ? segment.getStorage().getName() : segment.getName());
        button.setMinWidth(Button.USE_PREF_SIZE);
        button.getStyleClass().addAll("location-bar-segment", StyleClasses.SIZE_S, Styles.FLAT);
        if (isStorage) {
            button.getStyleClass().add("location-bar-storage");
            button.setGraphicTextGap(Spacing.getHorizontalThird());
            if (getViewModel().isShowStorageIcon()) {
                button.setGraphic(createStorageIconView(segment));
            }
        }
        button.setContextMenu(contextMenuControls.root());
        // a filter, so the segment the menu is asked for is known before the menu is shown
        button.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED,
                e -> getViewModel().onContextRequested(segment));
        button.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                clickedButton = button;
                getViewModel().onSegmentClicked(segment);
            }
        });
        return button;
    }

    private Node createArrowNode(GreyWolfFile precedingSegment) {
        var button = new Button(null, new FontIconView(SharedIcons.MENU_RIGHT));
        button.getStyleClass().addAll("location-bar-arrow", StyleClasses.SQUARE, StyleClasses.SIZE_S,
                Styles.FLAT);
        button.setOnMouseClicked(e -> {
            clickedButton = button;
            getViewModel().onArrowClicked(precedingSegment);
        });
        return button;
    }

    private void applyShowStorageIcon(boolean showStorageIcon) {
        if (!segmentNodes.isEmpty()) {
            Button button = (Button) segmentNodes.get(0);
            if (showStorageIcon) {
                button.setGraphic(createStorageIconView(segments.get(0)));
            } else {
                button.setGraphic(null);
            }
        }
    }

    private void applySegments(List<GreyWolfFile> newSegments) {
        segments.clear();
        segments.addAll(newSegments);
        rebuildNodes();
        if (!getViewModel().isEditMode()) {
            applyFit();
        }
    }

    private void applyEnterEditMode(String initialText) {
        root.pseudoClassStateChanged(EDIT_MODE, true);
        setContentVisible(false);
        pathTextField.setText(initialText);
        HBox.setHgrow(pathTextField, Priority.ALWAYS);
        pathTextField.setMaxWidth(Double.MAX_VALUE);
        pathTextField.setManaged(true);
        pathTextField.setVisible(true);
        pathTextField.requestFocus();
        pathTextField.selectAll();
    }

    private void applyExitEditMode() {
        root.pseudoClassStateChanged(EDIT_MODE, false);
        pathTextField.setManaged(true);
        pathTextField.setVisible(false);
        pathTextField.setMaxWidth(0);
        applyFit();
    }
}
