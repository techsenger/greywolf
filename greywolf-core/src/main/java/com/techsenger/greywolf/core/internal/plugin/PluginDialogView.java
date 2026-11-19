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

package com.techsenger.greywolf.core.internal.plugin;

import atlantafx.base.theme.Styles;
import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.core.style.CoreIcons;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.ui.style.GreyWolfFileIconResolver;
import com.techsenger.greywolf.storage.ui.style.GreyWolfFileStyleResolver;
import com.techsenger.shellfx.core.dialog.AbstractDialogView;
import com.techsenger.shellfx.core.dialog.DialogView;
import com.techsenger.shellfx.core.registries.StylesheetRegistry;
import com.techsenger.shellfx.core.window.AbstractFrameWindowView;
import com.techsenger.shellfx.dialogs.alert.AlertDialogParams;
import com.techsenger.shellfx.dialogs.alert.AlertDialogPort;
import com.techsenger.shellfx.dialogs.alert.AlertDialogView;
import com.techsenger.shellfx.dialogs.alert.AlertDialogViewModel;
import com.techsenger.shellfx.dialogs.progress.ProgressDialogParams;
import com.techsenger.shellfx.dialogs.progress.ProgressDialogPort;
import com.techsenger.shellfx.dialogs.progress.ProgressDialogView;
import com.techsenger.shellfx.dialogs.progress.ProgressDialogViewModel;
import com.techsenger.shellfx.material.button.ResultButton;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.style.Spacing;
import com.techsenger.shellfx.material.style.StyleClasses;
import com.techsenger.shellfx.storage.ui.chooser.FileChooserDialogParams;
import com.techsenger.shellfx.storage.ui.chooser.FileChooserDialogPort;
import com.techsenger.shellfx.storage.ui.chooser.FileChooserDialogView;
import com.techsenger.shellfx.storage.ui.chooser.FileChooserDialogViewModel;
import com.techsenger.shellfx.storage.ui.style.DefaultStorageIconResolver;
import com.techsenger.shellfx.storage.ui.style.DefaultStorageStyleResolver;
import com.techsenger.toolkit.fx.Spacer;
import com.techsenger.toolkit.fx.value.ValueUtils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

/**
 *
 * @author Pavel Castornii
 */
public final class PluginDialogView extends AbstractDialogView<PluginDialogViewModel> {

    public class Composer extends AbstractFrameWindowView<PluginDialogViewModel>.Composer
            implements PluginDialogComposer {

        @Override
        public AlertDialogPort openAlertDialog(AlertDialogParams params) {
            var viewModel = new AlertDialogViewModel<>(params);
            var view = new AlertDialogView<>(viewModel, getStylesheetRegistry());
            view.initialize();
            initDialogStage(view);
            return viewModel;
        }

        @Override
        public ProgressDialogPort openProgressDialog(ProgressDialogParams params) {
            var viewModel = new ProgressDialogViewModel<>(params);
            var view = new ProgressDialogView<>(viewModel, getStylesheetRegistry());
            view.initialize();
            initDialogStage(view);
            return viewModel;
        }

        @Override
        public FileChooserDialogPort<GreyWolfFile> openFileChooser(FileChooserDialogParams<GreyWolfFile> params) {
            var viewModel = new FileChooserDialogViewModel<>(params);
            var view = new FileChooserDialogView<>(viewModel, getStylesheetRegistry(),
                    new GreyWolfFileIconResolver(), new GreyWolfFileStyleResolver(),
                    new DefaultStorageIconResolver(), new DefaultStorageStyleResolver());
            view.initialize();
            initDialogStage(view);
            return viewModel;
        }

        private void initDialogStage(DialogView<?> dialog) {
            dialog.getWindow().initModality(Modality.APPLICATION_MODAL);
            dialog.getWindow().initOwner(getWindow());
        }
    }

    private final Button addButton = new Button(null, new FontIconView(CoreIcons.ADD));

    private final Button removeButton = new Button(null, new FontIconView(CoreIcons.REMOVE));

    private final Button installButton = new Button(null, new FontIconView(CoreIcons.INSTALL));

    private final Button uninstallButton = new Button(null, new FontIconView(CoreIcons.UNINSTALL));

    private final Button activateButton = new Button(null, new FontIconView(CoreIcons.START));

    private final Button deactivateButton = new Button(null, new FontIconView(CoreIcons.STOP));

    private final Button refreshButton = new Button(null, new FontIconView(CoreIcons.REFRESH));

    private final ToolBar toolBar = new ToolBar(addButton, removeButton, new Separator(Orientation.VERTICAL),
            installButton, uninstallButton, new Separator(Orientation.VERTICAL),
            activateButton, deactivateButton, new Spacer(Orientation.HORIZONTAL), refreshButton);

    private final TableView<ObservablePlugin> tableView = new TableView<ObservablePlugin>();

    private final VBox leftBox = new VBox(toolBar, tableView);

    private final Label nameLabel = new Label("Name");

    private final TextField nameTextField = new TextField();

    private final Label versionLabel = new Label("Version");

    private final TextField versionTextField = new TextField();

    private final Label stateLabel = new Label("State");

    private final TextField stateTextField = new TextField();

    private final Label categoryLabel = new Label("Category");

    private final TextField categoryTextField = new TextField();

    private final Label vendorLabel = new Label("Vendor");

    private final TextField vendorTextField = new TextField();

    private final Label pricingLabel = new Label("Pricing");

    private final TextField pricingTextField = new TextField();

    private final Label licenseLabel = new Label("License");

    private final TextField licenseTextField = new TextField();

    private final Label homepageLabel = new Label("Homepage");

    private final Hyperlink homepageHyperlink = new Hyperlink();

    private final Label descriptionLabel = new Label("Description");

    private final TextArea descriptionTextArea = new TextArea();

    private final GridPane gridPane = new GridPane();

    private final SplitPane splitPane = new SplitPane(leftBox, gridPane);

    private final ResultButton closeButton = new ResultButton(PluginDialogButtons.CLOSE, "Close");

    private final CheckBox selectAllCheckBox = new CheckBox();

    public PluginDialogView(PluginDialogViewModel viewModel, StylesheetRegistry stylesheetRegistry) {
        super(viewModel, stylesheetRegistry);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new PluginDialogView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        var css = PluginDialogView.class.getResource("plugin-dialog.css").toExternalForm();
        getContentBox().getStylesheets().add(css);
        buildToolBar();
        buildTable();
        leftBox.setPadding(new Insets(0, Spacing.getHorizontal(), 0, 0));
        buildGridPane();
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        registerButtons(closeButton);
    }

    @Override
    protected void bind() {
        super.bind();
        var viewModel = getViewModel();
        tableView.setItems(viewModel.getPlugins());
        removeButton.disableProperty().bind(viewModel.removeDisabledProperty());
        installButton.disableProperty().bind(viewModel.installDisabledProperty());
        uninstallButton.disableProperty().bind(viewModel.uninstallDisabledProperty());
        activateButton.disableProperty().bind(viewModel.activateDisabledProperty());
        deactivateButton.disableProperty().bind(viewModel.deactivateDisabledProperty());
        viewModel.pluginWrapper().bind(tableView.getSelectionModel().selectedItemProperty());
        viewModel.dividerPositionWrapper().bind(splitPane.getDividers().get(0).positionProperty());
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        var viewModel = getViewModel();
        ValueUtils.callAndAddListener(viewModel.pluginProperty(), (ov, oldV, newV) -> applyPlugin(newV));
        ValueUtils.callAndAddListener(viewModel.selectAllProperty(),
                (ov, oldV, newV) -> updateSelectAll(newV));
        viewModel.pluginSource().addListener((plugin) -> {
            if (plugin == null) {
                tableView.getSelectionModel().clearSelection();
            } else {
                tableView.getSelectionModel().select(plugin);
            }
        });
        viewModel.dividerPositionSource().addListener((value) -> applyDividerPosition(value));
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        var viewModel = getViewModel();
        addButton.setOnAction(e -> viewModel.onAdd());
        removeButton.setOnAction(e -> viewModel.onRemove());
        installButton.setOnAction(e -> viewModel.onInstall());
        uninstallButton.setOnAction(e -> viewModel.onUninstall());
        activateButton.setOnAction(e -> viewModel.onActivate());
        deactivateButton.setOnAction(e -> viewModel.onDeactivate());
        refreshButton.setOnAction(e -> viewModel.onRefresh());
        selectAllCheckBox.setOnAction(e -> viewModel.onSelectAll(selectAllCheckBox.isSelected()));
    }

    private void buildToolBar() {
        toolBar.getStyleClass().addAll(StyleClasses.BLEND, "inline");
        setupButton(this.addButton, "Add");
        setupButton(this.removeButton, "Remove");
        setupButton(this.installButton, "Install");
        setupButton(this.uninstallButton, "Uninstall");
        setupButton(this.activateButton, "Activate");
        setupButton(this.deactivateButton, "Deactivate");
        setupButton(this.refreshButton, "Refresh");
    }

    private void setupButton(Button button, String tooltip) {
        button.getStyleClass().addAll(Styles.FLAT, StyleClasses.SQUARE, StyleClasses.SIZE_M);
        button.setTooltip(new Tooltip(tooltip));
    }

    private void buildTable() {
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<ObservablePlugin, Boolean> checkColumn = new TableColumn<>();
        checkColumn.setCellFactory(column -> createCheckBoxCell());
        checkColumn.setSortable(false);
        checkColumn.setMinWidth(40);
        checkColumn.setMaxWidth(40);
        checkColumn.setGraphic(selectAllCheckBox);
        checkColumn.getStyleClass().add("check-column-header");
        this.tableView.getColumns().add(checkColumn);

        TableColumn<ObservablePlugin, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        nameColumn.setEditable(false);
        this.tableView.getColumns().add(nameColumn);

        TableColumn<ObservablePlugin, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(cellData -> {
            var cat = cellData.getValue().getCategory();
            return new ReadOnlyObjectWrapper<>(cat == null ? null : cat.getText());
        });
        categoryColumn.setEditable(false);
        this.tableView.getColumns().add(categoryColumn);

        TableColumn<ObservablePlugin, String> stateColumn = new TableColumn<>("State");
        stateColumn.setCellValueFactory(cellData -> cellData.getValue().stateProperty().map(PluginState::getText));
        stateColumn.setEditable(false);
        this.tableView.getColumns().add(stateColumn);

        TableColumn<ObservablePlugin, String> pricingColumn = new TableColumn<>("Pricing");
        pricingColumn.setCellValueFactory(cellData -> {
            var pricing = cellData.getValue().getPricing();
            return new ReadOnlyObjectWrapper<>(pricing == null ? null : pricing.getText());
        });
        pricingColumn.setEditable(false);
        this.tableView.getColumns().add(pricingColumn);
        VBox.setVgrow(tableView, Priority.ALWAYS);
    }

    private TableCell<ObservablePlugin, Boolean> createCheckBoxCell() {
        var cell = new TableCell<ObservablePlugin, Boolean>() {

            private final CheckBox checkBox = new CheckBox();

            private @Nullable ObservablePlugin boundPlugin;

            {
                setAlignment(Pos.CENTER);
                checkBox.setOnAction(e -> getViewModel().onPluginChecked(getIndex()));
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (boundPlugin != null) {
                    checkBox.selectedProperty().unbindBidirectional(boundPlugin.selectedProperty());
                    boundPlugin = null;
                }
                if (empty) {
                    setGraphic(null);
                } else {
                    boundPlugin = getTableRow().getItem();
                    checkBox.selectedProperty().bindBidirectional(boundPlugin.selectedProperty());
                    setGraphic(checkBox);
                }
            }
        };
        return cell;
    }

    private void buildGridPane() {
        gridPane.setVgap(Spacing.getVertical());
        gridPane.setHgap(Spacing.getHorizontal());
        gridPane.setPadding(new Insets(0, 0, 0, Spacing.getHorizontal()));
        int rowIndex = 0;

        gridPane.addRow(rowIndex++, nameLabel, nameTextField, versionLabel, versionTextField);
        nameLabel.setMinWidth(Label.USE_PREF_SIZE);
        nameTextField.setEditable(false);
        GridPane.setHgrow(nameTextField, Priority.ALWAYS);
        versionLabel.setMinWidth(Label.USE_PREF_SIZE);
        versionTextField.setEditable(false);
        GridPane.setHgrow(versionTextField, Priority.ALWAYS);

        gridPane.addRow(rowIndex++, categoryLabel, categoryTextField, stateLabel, stateTextField);
        categoryLabel.setMinWidth(Label.USE_PREF_SIZE);
        GridPane.setHgrow(categoryTextField, Priority.ALWAYS);
        categoryTextField.setEditable(false);
        stateLabel.setMinWidth(Label.USE_PREF_SIZE);
        stateTextField.setEditable(false);
        GridPane.setHgrow(stateTextField, Priority.ALWAYS);

        gridPane.addRow(rowIndex++, pricingLabel, pricingTextField, licenseLabel, licenseTextField);
        pricingLabel.setMinWidth(Label.USE_PREF_SIZE);
        GridPane.setHgrow(pricingTextField, Priority.ALWAYS);
        pricingTextField.setEditable(false);
        licenseLabel.setMinWidth(Label.USE_PREF_SIZE);
        licenseTextField.setEditable(false);
        GridPane.setHgrow(licenseTextField, Priority.ALWAYS);

        gridPane.add(vendorLabel, 0, rowIndex);
        vendorLabel.setMinWidth(Label.USE_PREF_SIZE);
        gridPane.add(vendorTextField, 1, rowIndex);
        vendorTextField.setEditable(false);
        GridPane.setHgrow(vendorTextField, Priority.ALWAYS);
        rowIndex++;

        gridPane.addRow(rowIndex++, homepageLabel, homepageHyperlink);
        homepageHyperlink.getStyleClass().add("plugin-dialog-link");
        homepageHyperlink.setOnAction(e -> {
            var url = homepageHyperlink.getText();
            if (url != null && !url.isBlank()) {
                getViewModel().onOpenUrl(url);
            }
        });
        homepageLabel.setMinWidth(Label.USE_PREF_SIZE);
        GridPane.setColumnSpan(homepageHyperlink, 3);
        GridPane.setHgrow(homepageHyperlink, Priority.ALWAYS);
        getContentBox().getChildren().add(splitPane);

        gridPane.addRow(rowIndex++, descriptionLabel, descriptionTextArea);
        GridPane.setValignment(descriptionLabel, VPos.TOP);
        descriptionLabel.setPadding(new Insets(8, 0, 0, 0));
        descriptionLabel.setMinWidth(Label.USE_PREF_SIZE);
        GridPane.setColumnSpan(descriptionTextArea, 3);
        descriptionTextArea.setWrapText(true);
        descriptionTextArea.setEditable(false);
        GridPane.setVgrow(descriptionTextArea, Priority.ALWAYS);
    }

    private void applyPlugin(@Nullable ObservablePlugin plugin) {
        if (plugin == null) {
            nameTextField.setText(null);
            versionTextField.setText(null);
            stateTextField.setText(null);
            categoryTextField.setText(null);
            pricingTextField.setText(null);
            vendorTextField.setText(null);
            licenseTextField.setText(null);
            homepageHyperlink.setText(null);
            descriptionTextArea.setText(null);
        } else {
            nameTextField.setText(plugin.getName());
            versionTextField.setText(plugin.getComponentConfig().getVersion().getFull());
            stateTextField.setText(plugin.getState().getText());
            categoryTextField.setText(plugin.getCategory() == null ? null : plugin.getCategory().getText());
            pricingTextField.setText(plugin.getPricing() == null ? null : plugin.getPricing().getText());
            vendorTextField.setText(plugin.getVendor());
            licenseTextField.setText(plugin.getLicense());
            homepageHyperlink.setText(plugin.getHomepage());
            descriptionTextArea.setText(plugin.getDescription());
        }
    }

    private void applyDividerPosition(double value) {
        splitPane.getDividers().get(0).setPosition(value);
    }

    private void updateSelectAll(boolean selected) {
        selectAllCheckBox.setSelected(selected);
    }
}
