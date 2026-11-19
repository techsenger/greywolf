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

import com.techsenger.greywolf.storage.base.GreyWolfFile;
import java.net.URI;

/**
 * A single planned copy step: one source entry (file or directory) and the URI it should be copied to.
 *
 * @param source the source entry to copy.
 * @param targetUri the URI the entry should be copied to.
 * @author Pavel Castornii
 */
public record TransferTask(GreyWolfFile source, URI targetUri) {

}
