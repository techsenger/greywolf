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

package com.techsenger.greywolf.shared.registrars;

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.core.clipboard.ClipboardFiles;
import com.techsenger.greywolf.core.clipboard.ClipboardOperation;
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfHandler;
import com.techsenger.greywolf.shared.internal.ContainerUtils;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.ui.transfer.CopyOperation;
import com.techsenger.greywolf.storage.ui.transfer.DefaultTransferComposer;
import com.techsenger.greywolf.storage.ui.transfer.MoveOperation;
import com.techsenger.greywolf.storage.ui.transfer.TargetDialogConfig;
import com.techsenger.greywolf.storage.ui.transfer.TargetDialogParams;
import com.techsenger.greywolf.storage.ui.transfer.TransferOperationType;
import com.techsenger.patternfx.mvvm.ParentView;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.event.ActionEvent;

/**
 * Pastes the files of the clipboard into a folder, in the container of the component the menu belongs to; does
 * nothing when there are no files in the clipboard, no folder or no container.
 *
 * @author Pavel Castornii
 */
public class PasteIntoFolderItemHandler extends AbstractGreyWolfHandler<ActionEvent> {

    private final ParentView<?> view;

    private final Supplier<@Nullable GreyWolfFile> folderSupplier;

    private final Consumer<GreyWolfFile> onContentChanged;

    /**
     * Creates a handler.
     *
     * @param greyWolf the application
     * @param view the component the menu belongs to
     * @param folderSupplier tells the folder to paste into, or {@code null} for none
     * @param onContentChanged called with the folder when the paste has changed its content
     */
    public PasteIntoFolderItemHandler(GreyWolf greyWolf, ParentView<?> view,
            Supplier<@Nullable GreyWolfFile> folderSupplier, Consumer<GreyWolfFile> onContentChanged) {
        super(greyWolf);
        this.view = view;
        this.folderSupplier = folderSupplier;
        this.onContentChanged = onContentChanged;
    }

    @Override
    public void handle(ActionEvent event) {
        var folder = folderSupplier.get();
        var container = ContainerUtils.findContainer(view);
        var clipboard = getGreyWolf().getClipboard();
        if (folder == null || container == null || !(clipboard.getContent() instanceof ClipboardFiles content)) {
            return;
        }
        var cut = content.getOperation() == ClipboardOperation.CUT;
        var configManager = getGreyWolf().getConfigManager();
        var config = configManager.getOrCreateConfig(TargetDialogConfig.class, TargetDialogConfig::new);
        var params = new TargetDialogParams(config, getGreyWolf().getSettings(), configManager,
                cut ? TransferOperationType.MOVE : TransferOperationType.COPY, content.getFiles(),
                content.getDirectory());
        var composer = new DefaultTransferComposer(container, getGreyWolf().getStylesheetRegistry());
        var operation = cut ? new MoveOperation(composer, params, folder) : new CopyOperation(composer, params, folder);
        operation.setOnFinished((target) -> {
            if (cut) {
                clipboard.setContent(null);
            }
            onContentChanged.accept(folder);
            ContainerUtils.notifyContentChanged(view, folder);
        });
        operation.start();
    }
}
