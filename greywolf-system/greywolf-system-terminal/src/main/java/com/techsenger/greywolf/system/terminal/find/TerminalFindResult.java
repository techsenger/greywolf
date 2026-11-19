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

import com.techsenger.jeditermfx.core.compatibility.Point;
import com.techsenger.jeditermfx.core.model.CharBuffer;
import com.techsenger.jeditermfx.core.util.Pair;
import com.techsenger.jeditermfx.ui.FindResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link FindResult} built by {@link TerminalFinder}, keyed by the exact char-buffer instances the terminal
 * buffer holds so matched ranges keep highlighting correctly on repaint.
 *
 * @author Pavel Castornii
 */
final class TerminalFindResult implements FindResult {

    private record TerminalMatch(String text, int index, Point start, Point end) implements FindResult.Match {

        @Override
        public String getText() {
            return text;
        }

        @Override
        public int getIndex() {
            return index;
        }

        @Override
        public Point getStart() {
            return start;
        }

        @Override
        public Point getEnd() {
            return end;
        }
    }

    private final List<FindResult.Match> matches = new ArrayList<>();

    private final Map<CharBuffer, List<Pair<Integer, Integer>>> rangesByBuffer = new HashMap<>();

    private int selectedMatch;

    @Override
    public List<Pair<Integer, Integer>> getRanges(CharBuffer characters) {
        return rangesByBuffer.getOrDefault(characters, List.of());
    }

    @Override
    public List<FindResult.Match> getMatches() {
        return matches;
    }

    @Override
    public FindResult.Match selectedMatch() {
        assertNotEmpty();
        return matches.get(selectedMatch);
    }

    @Override
    public FindResult.Match nextMatch() {
        assertNotEmpty();
        selectedMatch = (selectedMatch + 1) % matches.size();
        return selectedMatch();
    }

    @Override
    public FindResult.Match prevMatch() {
        assertNotEmpty();
        selectedMatch = (selectedMatch + matches.size() - 1) % matches.size();
        return selectedMatch();
    }

    void addMatch(String text, Point start, Point end) {
        matches.add(new TerminalMatch(text, matches.size() + 1, start, end));
    }

    void addRange(CharBuffer buffer, int start, int end) {
        rangesByBuffer.computeIfAbsent(buffer, (b) -> new ArrayList<>()).add(new Pair<>(start, end));
    }

    private void assertNotEmpty() {
        if (matches.isEmpty()) {
            throw new AssertionError("No matches");
        }
    }
}
