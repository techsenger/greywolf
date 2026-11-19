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

package com.techsenger.greywolf.storage.base.transfer;

import com.techsenger.greywolf.storage.base.GreyWolfStorage;
import java.io.IOException;
import java.net.URI;

/**
 * Copies every task of a {@link TransferPlan}: directories are created, files are copied stream to stream, and
 * the sources are left untouched.
 *
 * @author Pavel Castornii
 */
public final class Copier extends AbstractTransferer {

    @Override
    protected void transfer(TransferTask task, URI effectiveTarget, GreyWolfStorage targetStorage,
            TransferPlan plan, TransferListener listener) throws IOException {
        if (task.source().isDirectory()) {
            createDirectoryIfNeeded(targetStorage, effectiveTarget);
        } else {
            copyFile(task, effectiveTarget, targetStorage, plan, listener);
        }
    }
}
