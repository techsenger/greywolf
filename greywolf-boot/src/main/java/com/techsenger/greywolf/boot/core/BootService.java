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

package com.techsenger.greywolf.boot.core;

import com.techsenger.greywolf.boot.Boot;
import java.util.concurrent.CompletableFuture;

/**
 *
 * @author Pavel Castornii
 */
public final class BootService {

    /**
     * Instructs the boot to finish loading whatever is not loaded yet, that is, to start the plugins. Intended to
     * be called by the core once the JavaFX application is ready; calls after the first one only return the future.
     *
     * @return a future completed when the boot is finished, that is, after the plugins are started and the splash
     *     screen is closed; already completed if the boot is not running
     */
    public static CompletableFuture<Void> complete() {
        return Boot.complete();
    }

    private BootService() {
        // empty
    }
}
