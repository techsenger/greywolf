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

package com.techsenger.greywolf.shared.location;

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.shellfx.core.close.CloseCheckResult;
import com.techsenger.shellfx.core.close.ClosePreparationResult;
import com.techsenger.shellfx.core.popup.AbstractPopupViewModel;
import com.techsenger.shellfx.core.popup.PopupComposer;
import com.techsenger.shellfx.material.theme.Theme;
import java.util.List;
import java.util.function.Consumer;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

/**
 * The view model of the popup that lists folders.
 *
 * @param <C> the composer type
 * @author Pavel Castornii
 */
public class FolderPopupViewModel<C extends PopupComposer> extends AbstractPopupViewModel<C>
        implements FolderPopupPort {

    private final List<GreyWolfFile> files;

    private final @Nullable GreyWolfFile initialFile;

    private final boolean showStorageIcon;

    private final ReadOnlyObjectWrapper<@Nullable GreyWolfFile> file = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyBooleanWrapper submitted = new ReadOnlyBooleanWrapper();

    public FolderPopupViewModel(FolderPopupParams params) {
        super(params);
        this.files = params.getFiles();
        this.initialFile = params.getInitialFile();
        this.showStorageIcon = params.isShowStorageIcon();
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public @Nullable GreyWolfFile getFile() {
        return file.get();
    }

    @Override
    public ReadOnlyObjectProperty<@Nullable GreyWolfFile> fileProperty() {
        return file.getReadOnlyProperty();
    }

    @Override
    public boolean isSubmitted() {
        return submitted.get();
    }

    @Override
    public ReadOnlyBooleanProperty submittedProperty() {
        return submitted.getReadOnlyProperty();
    }

    @Override
    protected Theme getTheme() {
        // the view reads the theme to pick the storage icons
        return getSettings().getAppearance().getTheme();
    }

    protected List<GreyWolfFile> getFiles() {
        return files;
    }

    protected @Nullable GreyWolfFile getInitialFile() {
        return initialFile;
    }

    protected boolean isShowStorageIcon() {
        return showStorageIcon;
    }

    /**
     * Called by the view when the user submits the current file with a click or Enter; does nothing if there is none.
     */
    protected void onSubmit() {
        if (file.get() != null) {
            submitted.set(true);
        }
    }

    /**
     * Written directly by the view, which binds it to the selection of its list; direct invocation by user code
     * results in undefined behavior.
     */
    ReadOnlyObjectWrapper<@Nullable GreyWolfFile> fileWrapper() {
        return file;
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setAutoHide(true);
    }
}
