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

package com.techsenger.greywolf.storage.base.operation;

import com.techsenger.annotations.Nullable;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A compiled name filter: a wildcard ({@code *}/{@code ?}), or, when {@code regex} is {@code true}, a full
 * regular expression, matched against an entry's name. Shared by {@link MarkCriteria} and the copy engine's
 * own mask filtering, so both use the exact same wildcard/regex semantics.
 *
 * @author Pavel Castornii
 */
public final class NameMask {

    private static String wildcardToRegex(String mask) {
        var builder = new StringBuilder();
        for (var i = 0; i < mask.length(); i++) {
            var c = mask.charAt(i);
            switch (c) {
                case '*' -> builder.append(".*");
                case '?' -> builder.append('.');
                default -> {
                    if ("\\^$.|+()[]{}".indexOf(c) >= 0) {
                        builder.append('\\');
                    }
                    builder.append(c);
                }
            }
        }
        return builder.toString();
    }

    private final String mask;

    private final boolean caseSensitive;

    private final boolean regex;

    private final @Nullable Pattern pattern;

    public NameMask(String mask, boolean caseSensitive, boolean regex) {
        this.mask = mask;
        this.caseSensitive = caseSensitive;
        this.regex = regex;
        var flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
        Pattern compiled;
        try {
            compiled = Pattern.compile(regex ? mask : wildcardToRegex(mask), flags);
        } catch (PatternSyntaxException ex) {
            compiled = null;
        }
        this.pattern = compiled;
    }

    public String getMask() {
        return mask;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public boolean isRegex() {
        return regex;
    }

    /**
     * Returns whether {@code name} matches this mask in full. Always {@code false} when the mask/regex does
     * not compile into a valid pattern.
     *
     * @param name the name to test.
     */
    public boolean matches(String name) {
        return pattern != null && pattern.matcher(name).matches();
    }
}
