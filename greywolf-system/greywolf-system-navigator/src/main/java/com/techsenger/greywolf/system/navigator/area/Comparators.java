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

package com.techsenger.greywolf.system.navigator.area;

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import java.util.Comparator;

/**
 *
 * @author Pavel Castornii
 */
final class Comparators {

    /**
     * Wraps {@code aggregateComparator} with a higher-priority rule so the synthetic parent entry always sorts
     * first, then applies the directory-first ordering of the platform (links to directories included). Does not
     * modify the given comparator.
     *
     * @param aggregateComparator the user-defined column sorting logic to delegate to within each category; a
     *     {@code null} value is treated as always-equal.
     * @return a comparator that enforces parent-entry-first and directory-first ordering on top of
     *     {@code aggregateComparator}.
     */
    static <T extends GreyWolfFile> Comparator<T> directoryFirst(
            @Nullable Comparator<? super T> aggregateComparator) {
        Comparator<T> directories = com.techsenger.shellfx.storage.base.Comparators.directoryFirst(aggregateComparator);
        return (a, b) -> {
            boolean aParent = a.isParentEntry();
            boolean bParent = b.isParentEntry();

            if (aParent != bParent) {
                return aParent ? -1 : 1;
            }

            return directories.compare(a, b);
        };
    }

    private Comparators() {
        // empty
    }
}
