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

import com.techsenger.shellfx.shared.find.NavigableFindResult;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;

/**
 * Adapts a {@link TerminalFindResult} to {@link NavigableFindResult}. The actual match navigation (moving the
 * viewport, repainting the highlight) is owned by {@code TerminalPanel}, not by this class or by
 * {@link TerminalFindResult} itself, so {@link #nextMatch()}/{@link #previousMatch()} request it through the given
 * callbacks and only then read back the resulting position.
 *
 * @author Pavel Castornii
 */
final class TerminalNavigableFindResult implements NavigableFindResult {

    private final TerminalFindResult result;

    private final Runnable selectNextMatch;

    private final Runnable selectPrevMatch;

    private final ReadOnlyIntegerWrapper currentMatch = new ReadOnlyIntegerWrapper();

    TerminalNavigableFindResult(TerminalFindResult result, Runnable selectNextMatch, Runnable selectPrevMatch) {
        this.result = result;
        this.selectNextMatch = selectNextMatch;
        this.selectPrevMatch = selectPrevMatch;
        this.currentMatch.set(currentIndex());
    }

    @Override
    public int getTotalMatches() {
        return result.getMatches().size();
    }

    @Override
    public int getCurrentMatch() {
        return currentMatch.get();
    }

    @Override
    public ReadOnlyIntegerProperty currentMatchProperty() {
        return currentMatch.getReadOnlyProperty();
    }

    @Override
    public void nextMatch() {
        selectNextMatch.run();
        currentMatch.set(currentIndex());
    }

    @Override
    public void previousMatch() {
        selectPrevMatch.run();
        currentMatch.set(currentIndex());
    }

    TerminalFindResult getResult() {
        return result;
    }

    private int currentIndex() {
        return result.getMatches().isEmpty() ? -1 : result.selectedMatch().getIndex() - 1;
    }
}
