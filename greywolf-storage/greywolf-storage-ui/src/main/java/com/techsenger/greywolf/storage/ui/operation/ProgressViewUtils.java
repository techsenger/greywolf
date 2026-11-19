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

import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * Setup of the nodes shared by the progress dialogs of file operations.
 *
 * @author Pavel Castornii
 */
public final class ProgressViewUtils {

    /**
     * Keeps each label from shrinking below its preferred width, so the label is never truncated.
     */
    public static void pinWidth(Label... labels) {
        for (var label : labels) {
            label.setMinWidth(Label.USE_PREF_SIZE);
        }
    }

    /**
     * Lets the bar take all the width its grid cell offers.
     */
    public static void setupBar(ProgressBar bar) {
        bar.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(bar, Priority.ALWAYS);
    }

    /**
     * Pins the label's width (via the {@code .percent-label} rule in {@code progress-dialog.css}) to 3em so the
     * percent column doesn't reflow every time the digit count changes (e.g. "5%" vs "42%" vs "100%").
     */
    public static void setupPercentLabel(Label label) {
        label.getStyleClass().add("percent-label");
    }

    /**
     * Truncates with a leading ellipsis (e.g. "…dir/deeply/nested/file.txt") instead of wrapping or overflowing,
     * so the end of the path - almost always the more useful part - stays visible when the dialog is too
     * narrow for the whole thing.
     */
    public static void setupPathLabel(Label label) {
        label.setMaxWidth(Double.MAX_VALUE);
        label.setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);
    }

    private ProgressViewUtils() {
        // empty
    }
}
