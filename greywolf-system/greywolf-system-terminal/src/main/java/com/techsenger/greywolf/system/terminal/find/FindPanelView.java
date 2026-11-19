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

package com.techsenger.greywolf.system.terminal.find;

import com.techsenger.jeditermfx.ui.TerminalPanel;
import com.techsenger.shellfx.shared.find.AbstractFindPanelView;
import com.techsenger.toolkit.fx.value.ValueUtils;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 *
 * @author Pavel Castornii
 */
public final class FindPanelView extends AbstractFindPanelView<FindPanelViewModel> {

    private final TerminalPanel terminalPanel;

    public FindPanelView(FindPanelViewModel viewModel, TerminalPanel terminalPanel) {
        super(viewModel);
        this.terminalPanel = terminalPanel;
    }

    @Override
    protected void build() {
        super.build();
        GridPane.setHgrow(getFindComboBoxWrapper(), Priority.ALWAYS);
        getFindComboBox().setMaxWidth(Double.MAX_VALUE);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        var viewModel = getViewModel();
        ValueUtils.callAndAddListener(viewModel.findResultProperty(),
                (ov, oldV, newV) -> terminalPanel.setFindResult(newV == null ? null : newV.getResult()));
        viewModel.selectNextMatchSource().addListener((v) -> terminalPanel.selectNextFindMatch());
        viewModel.selectPrevMatchSource().addListener((v) -> terminalPanel.selectPrevFindMatch());
        ValueUtils.callAndAddListener(getHighlightButton().selectedProperty(), (ov, oldV, newV) -> {
            terminalPanel.setFindMatchHighlighted(newV);
            terminalPanel.repaint();
        });
    }

    @Override
    protected void unbuild() {
        super.unbuild();
        terminalPanel.setFindResult(null);
    }
}
