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

package com.techsenger.greywolf.system.terminal.area;

import com.google.errorprone.annotations.concurrent.LazyInit;
import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.system.terminal.find.FindPanelParams;
import com.techsenger.greywolf.system.terminal.find.FindPanelPort;
import com.techsenger.greywolf.system.terminal.find.FindPanelView;
import com.techsenger.greywolf.system.terminal.find.FindPanelViewModel;
import com.techsenger.jeditermfx.ui.DefaultHyperlinkFilter;
import com.techsenger.jeditermfx.ui.TerminalPanel;
import com.techsenger.shellfx.core.area.AbstractAreaView;
import com.techsenger.shellfx.shared.find.FindPanelConfig;
import com.techsenger.toolkit.fx.utils.NodeUtils;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class TerminalAreaView extends AbstractAreaView<TerminalAreaViewModel> {

    public class Composer extends AbstractAreaView<TerminalAreaViewModel>.Composer implements TerminalAreaComposer {

        private final TerminalAreaView view = TerminalAreaView.this;

        @Override
        public FindPanelPort openFindPanel(FindPanelConfig config) {
            if (view.findPanel == null) {
                view.findPanel = createFindPanel(config);
                getModifiableChildren().add(view.findPanel);
                view.node.getChildren().add(view.findPanel.getNode());
            }
            return view.findPanel.getViewModel();
        }

        @Override
        public void closeFindPanel() {
            if (view.findPanel == null) {
                return;
            }
            getModifiableChildren().remove(view.findPanel);
            view.node.getChildren().remove(view.findPanel.getNode());
            view.findPanel.deinitialize();
            view.findPanel = null;
        }

        @Override
        public @Nullable FindPanelPort getFindPanel() {
            return view.findPanel == null ? null : view.findPanel.getViewModel();
        }

        protected FindPanelView createFindPanel(FindPanelConfig config) {
            var params = new FindPanelParams(config, widget.getTerminalTextBuffer());
            var viewModel = new FindPanelViewModel(params);
            var findPanelView = new FindPanelView(viewModel, widget.getTerminalPanel());
            findPanelView.initialize();
            return findPanelView;
        }
    }

    @LazyInit
    private TabJediTermFxWidget widget;

    private @Nullable FindPanelView findPanel;

    private final VBox node = new VBox();

    public TerminalAreaView(TerminalAreaViewModel viewModel) {
        super(viewModel);
    }

    @Override
    public void requestFocus() {
        NodeUtils.requestFocus(widget.getTerminalPanel().getCanvas());
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
    protected void build() {
        super.build();
        VBox.setVgrow(node, Priority.ALWAYS);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        var viewModel = getViewModel();
        viewModel.createTerminalSource().addListener((v) -> createTerminal());
        viewModel.commandSource().addListener((command) -> applyCommand(command));
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        var viewModel = getViewModel();
        node.addEventFilter(KeyEvent.KEY_PRESSED, (e) -> {
            if (this.findPanel != null && e.getCode() == KeyCode.ESCAPE) {
                viewModel.onHideFind();
                e.consume();
            }
        });
    }

    @Override
    protected void unbuild() {
        super.unbuild();
        widget.close();
    }

    @Override
    protected Composer createComposer() {
        return new TerminalAreaView.Composer();
    }

    private void createTerminal() {
        var viewModel = getViewModel();
        this.widget = new TabJediTermFxWidget(80, 24, viewModel.getSettingsProvider(), () -> viewModel.onShowFind());
        widget.setTtyConnector(viewModel.getTtyConnector());
        widget.addHyperlinkFilter(new DefaultHyperlinkFilter() {

            @Override
            protected void open(String url) {
                viewModel.onOpenLink(url);
            }
        });
        VBox.setVgrow(widget.getPane(), Priority.ALWAYS);
        widget.start();
        viewModel.selectedTextWrapper().bind(getTerminalPanel().selectedTextProperty());
        this.node.getChildren().add(widget.getPane());
    }

    private void applyCommand(TerminalCommand command) {
        var panel = getTerminalPanel();
        switch (command) {
            case CLEAR -> panel.clearBuffer();
            case COPY -> panel.handleCopy(false, false);
            case PASTE -> panel.handlePaste();
            case SELECT_ALL -> panel.selectAll();
            case PAGE_UP -> panel.pageUp();
            case PAGE_DOWN -> panel.pageDown();
            case LINE_UP -> panel.scrollUp();
            case LINE_DOWN -> panel.scrollDown();
        }
    }

    private TerminalPanel getTerminalPanel() {
        return this.widget.getTerminalPanel();
    }
}
