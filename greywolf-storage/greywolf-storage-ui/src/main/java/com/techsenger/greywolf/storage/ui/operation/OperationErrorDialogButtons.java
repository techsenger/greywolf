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

package com.techsenger.greywolf.storage.ui.operation;

import com.techsenger.greywolf.storage.base.operation.ErrorResolution;
import com.techsenger.shellfx.material.button.DefaultResultButtonName;

/**
 *
 * @author Pavel Castornii
 */
public final class OperationErrorDialogButtons {

    private static final class ErrorButtonName extends DefaultResultButtonName implements ErrorResultButtonName {

        private final ErrorResolution errorResolution;

        ErrorButtonName(ErrorResolution errorResolution) {
            this.errorResolution = errorResolution;
        }

        @Override
        public ErrorResolution getErrorResolution() {
            return errorResolution;
        }
    }

    public static final ErrorResultButtonName SKIP = new ErrorButtonName(ErrorResolution.SKIP);

    public static final ErrorResultButtonName SKIP_ALL = new ErrorButtonName(ErrorResolution.SKIP_ALL);

    public static final ErrorResultButtonName RETRY = new ErrorButtonName(ErrorResolution.RETRY);

    public static final ErrorResultButtonName ABORT = new ErrorButtonName(ErrorResolution.ABORT);

    private OperationErrorDialogButtons() {
        // empty
    }
}
