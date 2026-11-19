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
 * SPI for contributing custom storages, those that are neither system nor recent, to the file storage registry once,
 * at its creation. A storage that appears later is registered with the registry directly.
 *
 * @author Pavel Castornii
 */
public interface CustomStorageService {

    /**
     * Returns the custom storages of this provider.
     *
     * @return the storages, never {@code null}, may be empty
     */
    @Unmodifiable List<GreyWolfStorage> getStorages();
}
