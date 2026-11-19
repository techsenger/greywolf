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

package com.techsenger.greywolf.devtools.base;

import java.util.function.Supplier;

/**
 * Checks whether an optional {@code requires static} module is actually present in this module's layer at
 * runtime, by attempting to resolve one of its known public classes instead of matching on a module name string.
 *
 * @author Pavel Castornii
 */
public final class OptionalModules {

    /**
     * Returns {@code true} if the class supplied by {@code classSupplier} can be resolved, {@code false} if
     * resolving it fails because the module declaring it is absent from this module's layer.
     *
     * @param classSupplier supplies a class literal from the optional module being probed
     */
    public static boolean isPresent(Supplier<Class<?>> classSupplier) {
        try {
            var resolved = classSupplier.get();
            return resolved != null;
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }

    private OptionalModules() {
        // empty
    }
}
