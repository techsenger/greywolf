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

package com.techsenger.greywolf.storage.ui.operation;

import com.techsenger.toolkit.core.file.FileUtils;

/**
 * Text formatting shared by the progress dialogs of file operations.
 *
 * @author Pavel Castornii
 */
public final class ProgressFormats {

    /**
     * Formats a done/total byte pair with a single shared unit when {@link FileUtils#formatSize(long)} would pick
     * the same one for both (e.g. "10.0 / 20.0 MB", instead of repeating the unit twice). Falls back to its
     * separately-formatted output for each value when the units differ - sharing one regardless would round a
     * much smaller {@code done} (e.g. 100 KB out of 100 GB) down to "0.0".
     */
    public static String formatBytesPair(long done, long total) {
        var doneText = FileUtils.formatSize(done);
        var totalText = FileUtils.formatSize(total);
        var doneUnit = doneText.substring(doneText.lastIndexOf(' ') + 1);
        var totalUnit = totalText.substring(totalText.lastIndexOf(' ') + 1);
        if (!doneUnit.equals(totalUnit)) {
            return doneText + " / " + totalText;
        }
        var doneValue = doneText.substring(0, doneText.lastIndexOf(' '));
        return doneValue + " / " + totalText;
    }

    /**
     * Formats {@code millis} as {@code HH:mm:ss}, or {@code --:--:--} when it is negative (not known yet).
     */
    public static String formatDuration(long millis) {
        if (millis < 0) {
            return "--:--:--";
        }
        var totalSeconds = millis / 1000;
        var hours = totalSeconds / 3600;
        var minutes = (totalSeconds % 3600) / 60;
        var seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * The fraction {@link #formatPercent(double)} and a progress bar both need - computed once so the two never
     * drift apart, and so a zero total never divides by zero.
     */
    public static double fraction(long done, long total) {
        return total <= 0 ? 0 : (double) done / total;
    }

    public static String formatPercent(double fraction) {
        return Math.round(fraction * 100) + "%";
    }

    private ProgressFormats() {
        // empty
    }
}
