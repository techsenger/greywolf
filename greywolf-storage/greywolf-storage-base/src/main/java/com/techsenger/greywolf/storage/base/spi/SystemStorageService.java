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

package com.techsenger.greywolf.storage.base.spi;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.greywolf.storage.base.GreyWolfStorage;
import java.util.List;

/**
 * SPI for contributing the system storages, those discovered from the operating system, to the file storage registry.
 *
 * @author Pavel Castornii
 */
public interface SystemStorageService {

    /**
     * Returns the latest system storages, asked again whenever the registry learns that the drives have changed.
     *
     * <p>A storage that is equal to one given before must be that very instance, as the registry follows storages
     * by their listeners; a new instance is given only if something of the storage has changed. The identifiers
     * of the storages given together must differ.
     *
     * @return the storages, never {@code null}, may be empty
     */
    @Unmodifiable List<GreyWolfStorage> getStorages();
}
