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

import com.techsenger.shellfx.core.dialog.DialogConfig;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * The state shared by the dialogs that configure a transfer, whichever side started it, and worth keeping between
 * sessions.
 *
 * @author Pavel Castornii
 */
public class TransferDialogConfig extends DialogConfig {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<String> masks;

    private boolean caseSensitiveSelected;

    private boolean regexSelected;

    private boolean followLinksSelected;

    private boolean stableSymlinksSelected;

    private boolean preserveAttributesSelected;

    private boolean checkDataIntegritySelected;

    protected TransferDialogConfig() {
        setWidth(600);
        this.masks = new ArrayList<>();
        this.caseSensitiveSelected = false;
        this.regexSelected = false;
        this.followLinksSelected = false;
        this.stableSymlinksSelected = false;
        this.preserveAttributesSelected = true;
        this.checkDataIntegritySelected = false;
    }

    public List<String> getMasks() {
        return masks;
    }

    public void setMasks(List<String> masks) {
        this.masks = masks;
    }

    public boolean isCaseSensitiveSelected() {
        return caseSensitiveSelected;
    }

    public void setCaseSensitiveSelected(boolean caseSensitiveSelected) {
        this.caseSensitiveSelected = caseSensitiveSelected;
    }

    public boolean isRegexSelected() {
        return regexSelected;
    }

    public void setRegexSelected(boolean regexSelected) {
        this.regexSelected = regexSelected;
    }

    public boolean isFollowLinksSelected() {
        return followLinksSelected;
    }

    public void setFollowLinksSelected(boolean followLinksSelected) {
        this.followLinksSelected = followLinksSelected;
    }

    public boolean isStableSymlinksSelected() {
        return stableSymlinksSelected;
    }

    public void setStableSymlinksSelected(boolean stableSymlinksSelected) {
        this.stableSymlinksSelected = stableSymlinksSelected;
    }

    public boolean isPreserveAttributesSelected() {
        return preserveAttributesSelected;
    }

    public void setPreserveAttributesSelected(boolean preserveAttributesSelected) {
        this.preserveAttributesSelected = preserveAttributesSelected;
    }

    public boolean isCheckDataIntegritySelected() {
        return checkDataIntegritySelected;
    }

    public void setCheckDataIntegritySelected(boolean checkDataIntegritySelected) {
        this.checkDataIntegritySelected = checkDataIntegritySelected;
    }
}
