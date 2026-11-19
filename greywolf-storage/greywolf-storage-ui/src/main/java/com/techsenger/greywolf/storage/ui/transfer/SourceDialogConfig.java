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

package com.techsenger.greywolf.storage.ui.transfer;

import java.io.Serial;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Keeps, besides the shared state, the targets the user has transferred to.
 *
 * @author Pavel Castornii
 */
public class SourceDialogConfig extends TransferDialogConfig {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<URI> targets = new ArrayList<>();

    public List<URI> getTargets() {
        return targets;
    }

    public void setTargets(List<URI> targets) {
        this.targets = targets;
    }
}
