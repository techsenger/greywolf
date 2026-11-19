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

import javafx.animation.PauseTransition;
import javafx.util.Duration;

/**
 * Times the visibility of a dialog that tracks a background run: it is shown only if the run outlasts a delay, and
 * once shown it stays visible for a minimum time, so a fast run never makes it flash on screen.
 *
 * <p>Not thread-safe; every method must be called on the JavaFX application thread.
 *
 * @author Pavel Castornii
 */
public final class DialogTimer {

    /**
     * Runs {@code action} on the JavaFX application thread once {@code delayMillis} has passed.
     *
     * @param delayMillis the delay in milliseconds
     * @param action the action to run
     */
    public static void runAfter(long delayMillis, Runnable action) {
        var delay = new PauseTransition(Duration.millis((double) delayMillis));
        delay.setOnFinished(event -> action.run());
        delay.play();
    }

    private final long showDelayMillis;

    private final long minVisibleMillis;

    private final Runnable show;

    private boolean shown;

    private boolean finished;

    private long shownAt;

    /**
     * Creates a timer.
     *
     * @param showDelayMillis how long the run may last before the dialog is shown
     * @param minVisibleMillis the minimum time the dialog stays visible once shown
     * @param show shows the dialog
     */
    public DialogTimer(long showDelayMillis, long minVisibleMillis, Runnable show) {
        this.showDelayMillis = showDelayMillis;
        this.minVisibleMillis = minVisibleMillis;
        this.show = show;
    }

    /**
     * Returns whether the dialog has actually been shown, meaning the run outlasted the show delay.
     */
    public boolean isShown() {
        return shown;
    }

    /**
     * Starts the show delay; the dialog is shown when it passes, unless {@link #finish(Runnable)} came first.
     */
    public void start() {
        runAfter(showDelayMillis, () -> {
            if (!finished) {
                shown = true;
                shownAt = System.currentTimeMillis();
                show.run();
            }
        });
    }

    /**
     * Marks the run as over and runs {@code afterMinVisible} as soon as the dialog, if it was ever shown, has been
     * visible for its minimum time; right away if it was never shown.
     *
     * @param afterMinVisible typically hides the dialog and continues with whatever follows the run
     */
    public void finish(Runnable afterMinVisible) {
        finished = true;
        if (!shown) {
            afterMinVisible.run();
            return;
        }
        var remaining = minVisibleMillis - (System.currentTimeMillis() - shownAt);
        if (remaining <= 0) {
            afterMinVisible.run();
            return;
        }
        runAfter(remaining, afterMinVisible);
    }
}
