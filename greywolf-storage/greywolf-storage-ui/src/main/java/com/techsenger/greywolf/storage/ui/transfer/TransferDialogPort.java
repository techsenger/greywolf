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

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.shellfx.core.dialog.DialogPort;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

/**
 * Provides full access to the client API of a dialog that configures a transfer.
 *
 * <p>What the user entered is validated inside the dialog on OK, so the results are only meaningful once a
 * {@code setOnResult} callback registered on this port sees the {@code OK} button - an invalid target never reaches
 * that callback.
 *
 * @author Pavel Castornii
 */
public interface TransferDialogPort extends DialogPort {

    @Unmodifiable List<GreyWolfFile> getSourceFiles();

    String getMask();

    void setMask(String mask);

    StringProperty maskProperty();

    ObservableList<String> getMasks();

    boolean isCaseSensitiveSelected();

    void setCaseSensitiveSelected(boolean value);

    BooleanProperty caseSensitiveSelectedProperty();

    boolean isRegexSelected();

    void setRegexSelected(boolean value);

    BooleanProperty regexSelectedProperty();

    boolean isFollowLinksSelected();

    void setFollowLinksSelected(boolean value);

    BooleanProperty followLinksSelectedProperty();

    boolean isStableSymlinksSelected();

    void setStableSymlinksSelected(boolean value);

    BooleanProperty stableSymlinksSelectedProperty();

    boolean isPreserveAttributesSelected();

    void setPreserveAttributesSelected(boolean value);

    BooleanProperty preserveAttributesSelectedProperty();

    boolean isCheckDataIntegritySelected();

    void setCheckDataIntegritySelected(boolean value);

    BooleanProperty checkDataIntegritySelectedProperty();
}
