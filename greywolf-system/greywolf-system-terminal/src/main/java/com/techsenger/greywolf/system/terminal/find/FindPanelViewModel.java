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

import com.techsenger.jeditermfx.core.model.TerminalTextBuffer;
import com.techsenger.patternfx.mvvm.ChildComposer;
import com.techsenger.shellfx.shared.find.AbstractFindPanelViewModel;
import com.techsenger.shellfx.shared.find.FindTrigger;
import com.techsenger.shellfx.shared.find.TextMatcherFactory;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import java.util.concurrent.CompletableFuture;

/**
 *
 * @author Pavel Castornii
 */
public final class FindPanelViewModel extends AbstractFindPanelViewModel<ChildComposer, TerminalNavigableFindResult>
        implements FindPanelPort {

    private final TerminalTextBuffer textBuffer;

    private final ObservableSource<Void> selectNextMatchSource = new SimpleObservableSource<>();

    private final ObservableSource<Void> selectPrevMatchSource = new SimpleObservableSource<>();

    public FindPanelViewModel(FindPanelParams params) {
        super(params, FindTrigger.ON_SUBMIT);
        this.textBuffer = params.getTextBuffer();
    }

    @Override
    protected void loadStateFromConfig() {
        super.loadStateFromConfig();
        updateWholeWordAvailability();
    }

    @Override
    protected CompletableFuture<TerminalNavigableFindResult> onFind() {
        var matcher = TextMatcherFactory.create(getEditedFindText(), isMatchCaseSelected(), isWholeWordSelected(),
                isRegExpSelected());
        var result = matcher == null ? new TerminalFindResult() : TerminalFinder.find(textBuffer, matcher.pattern());
        return CompletableFuture.completedFuture(new TerminalNavigableFindResult(result,
                () -> selectNextMatchSource.next(null), () -> selectPrevMatchSource.next(null)));
    }

    @Override
    protected void onFindCleared() {
        // nothing extra to do: findResult already went to null, the View reacts to that to clear the terminal
        // highlight
    }

    @Override
    protected void onMatchCase() {
        super.onMatchCase();
        runFind();
    }

    @Override
    protected void onWholeWord() {
        super.onWholeWord();
        runFind();
    }

    @Override
    protected void onRegExp() {
        super.onRegExp();
        updateWholeWordAvailability();
        runFind();
    }

    ObservableSource<Void> selectNextMatchSource() {
        return selectNextMatchSource;
    }

    ObservableSource<Void> selectPrevMatchSource() {
        return selectPrevMatchSource;
    }

    private void updateWholeWordAvailability() {
        var disabled = isRegExpSelected();
        setWholeWordDisabled(disabled);
        if (disabled) {
            setWholeWordSelected(false);
        }
    }
}
