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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Cancel/pause switches shared by a long-running worker (scan, copy, delete) and the code controlling it. The
 * worker calls {@link #awaitIfPaused()} between steps and checks {@link #isCancelled()}; any other thread calls
 * {@link #pause()}, {@link #resume()} and {@link #cancel()}. One instance serves exactly one run.
 *
 * @author Pavel Castornii
 */
public final class RunControl {

    private final AtomicBoolean cancelled = new AtomicBoolean();

    private final AtomicBoolean paused = new AtomicBoolean();

    private final Object pauseLock = new Object();

    /**
     * Requests cancellation and wakes up a paused worker, so a cancel requested while paused takes effect right
     * away instead of waiting for {@link #resume()} first.
     */
    public void cancel() {
        cancelled.set(true);
        synchronized (pauseLock) {
            pauseLock.notifyAll();
        }
    }

    /**
     * Suspends the worker before its next step, without losing any progress.
     */
    public void pause() {
        paused.set(true);
    }

    /**
     * Lets a paused worker continue; has no effect if it isn't paused.
     */
    public void resume() {
        synchronized (pauseLock) {
            paused.set(false);
            pauseLock.notifyAll();
        }
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    /**
     * Blocks the calling (virtual) thread while paused, waking up as soon as the run is resumed or cancelled. An
     * interrupt of the waiting thread is treated as a cancel.
     */
    public void awaitIfPaused() {
        synchronized (pauseLock) {
            while (paused.get() && !cancelled.get()) {
                try {
                    pauseLock.wait();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    cancelled.set(true);
                    return;
                }
            }
        }
    }
}
