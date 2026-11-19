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

import com.techsenger.jeditermfx.core.StyledTextConsumer;
import com.techsenger.jeditermfx.core.TextStyle;
import com.techsenger.jeditermfx.core.compatibility.Point;
import com.techsenger.jeditermfx.core.model.CharBuffer;
import com.techsenger.jeditermfx.core.model.SubCharBuffer;
import com.techsenger.jeditermfx.core.model.TerminalTextBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Scans a terminal text buffer for matches of a compiled pattern, tracking the exact char-buffer instances each
 * matched character came from so the resulting {@link TerminalFindResult} can report ranges usable for highlight
 * rendering, not just plain text offsets.
 *
 * @author Pavel Castornii
 */
final class TerminalFinder {

    private record Token(int x, int y, CharBuffer buf, int offset, int textStart) { }

    static TerminalFindResult find(TerminalTextBuffer textBuffer, Pattern pattern) {
        var tokens = new ArrayList<Token>();
        var text = new StringBuilder();
        textBuffer.processHistoryAndScreenLines(-textBuffer.getHistoryLinesCount(), -1, new StyledTextConsumer() {

            @Override
            public void consume(int x, int y, TextStyle style, CharBuffer characters, int startRow) {
                var buf = characters;
                int offset = 0;
                if (buf instanceof SubCharBuffer subCharBuffer) {
                    buf = subCharBuffer.getParent();
                    offset = subCharBuffer.getOffset();
                }
                int length = characters.length();
                if (length > 0) {
                    tokens.add(new Token(x, y - startRow, buf, offset, text.length()));
                    text.append(buf, offset, offset + length);
                }
            }

            @Override
            public void consumeNul(int x, int y, int nulIndex, TextStyle style,
                      CharBuffer characters, int startRow) { }

            @Override
            public void consumeQueue(int x, int y, int nulIndex, int startRow) { }
        });

        var result = new TerminalFindResult();
        var matcher = pattern.matcher(text);
        while (matcher.find()) {
            if (matcher.end() > matcher.start()) {
                addMatch(result, tokens, text.substring(matcher.start(), matcher.end()), matcher.start(),
                        matcher.end() - 1);
            }
        }
        return result;
    }

    private static void addMatch(TerminalFindResult result, List<Token> tokens, String matchedText, int globalStart,
            int globalEnd) {
        int firstTokenIndex = locate(tokens, globalStart);
        int lastTokenIndex = locate(tokens, globalEnd);
        var firstToken = tokens.get(firstTokenIndex);
        var lastToken = tokens.get(lastTokenIndex);
        int firstBufIndex = firstToken.offset() + (globalStart - firstToken.textStart());
        int lastBufIndex = lastToken.offset() + (globalEnd - lastToken.textStart());
        if (firstTokenIndex == lastTokenIndex) {
            result.addRange(firstToken.buf(), firstBufIndex, lastBufIndex + 1);
        } else {
            result.addRange(firstToken.buf(), firstBufIndex, firstToken.buf().length());
            for (int i = firstTokenIndex + 1; i < lastTokenIndex; i++) {
                var middleToken = tokens.get(i);
                result.addRange(middleToken.buf(), 0, middleToken.buf().length());
            }
            result.addRange(lastToken.buf(), 0, lastBufIndex + 1);
        }
        var start = new Point(firstToken.x() + firstBufIndex, firstToken.y());
        var end = new Point(lastToken.x() + lastBufIndex, lastToken.y());
        result.addMatch(matchedText, start, end);
    }

    private static int locate(List<Token> tokens, int globalIndex) {
        int lo = 0;
        int hi = tokens.size() - 1;
        while (lo < hi) {
            int mid = (lo + hi + 1) >>> 1;
            if (tokens.get(mid).textStart() <= globalIndex) {
                lo = mid;
            } else {
                hi = mid - 1;
            }
        }
        return lo;
    }

    private TerminalFinder() {
        // empty
    }
}
