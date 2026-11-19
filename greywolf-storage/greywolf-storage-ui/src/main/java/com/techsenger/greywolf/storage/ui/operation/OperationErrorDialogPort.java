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

import com.techsenger.shellfx.core.dialog.DialogPort;
import javafx.beans.property.StringProperty;

/**
 * Lets the view model that opens this dialog update its message in place, so the same dialog instance can be
 * reused for consecutive failures instead of opening a new one each time.
 *
 * @author Pavel Castornii
 */
public interface OperationErrorDialogPort extends DialogPort {

    String getMessage();

    void setMessage(String message);

    StringProperty messageProperty();

    String getFilePath();

    void setFilePath(String filePath);

    StringProperty filePathProperty();
}
