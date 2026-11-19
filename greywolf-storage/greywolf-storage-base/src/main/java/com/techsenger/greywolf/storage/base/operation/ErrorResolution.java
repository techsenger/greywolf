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

/**
 * How to proceed after a failed task; {@code SKIP_ALL} applies to every later failure in the same
 * operation run without asking again, and {@code ABORT} stops the whole run.
 *
 * @author Pavel Castornii
 */
public enum ErrorResolution {

    SKIP, SKIP_ALL, RETRY, ABORT
}
