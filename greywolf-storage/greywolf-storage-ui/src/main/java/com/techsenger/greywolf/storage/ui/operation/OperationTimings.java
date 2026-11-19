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

/**
 * Timings shared by every file operation (copy, move, delete) that scans first and then runs behind a progress
 * dialog.
 *
 * @author Pavel Castornii
 */
public final class OperationTimings {

    /**
     * How long scanning may run before the scan dialog appears at all - a scan that finishes faster than this
     * never shows a dialog the user couldn't have reacted to anyway.
     */
    public static final long SCAN_SHOW_DELAY_MILLIS = 500;

    /**
     * The minimum time the scan dialog stays visible once shown, so it doesn't flash off screen if scanning
     * happens to finish moments after it appeared.
     */
    public static final long SCAN_MIN_VISIBLE_MILLIS = 1000;

    /**
     * How long the operation's progress dialog stays visible after the run concludes (completed, cancelled, or
     * aborted) before actually closing, so a very fast run doesn't just flash off screen.
     */
    public static final long RUN_MIN_VISIBLE_MILLIS = 500;

    private OperationTimings() {
        // empty
    }
}
