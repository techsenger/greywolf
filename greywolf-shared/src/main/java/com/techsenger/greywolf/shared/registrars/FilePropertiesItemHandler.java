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
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfHandler;
import com.techsenger.greywolf.shared.internal.ContainerUtils;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.ui.properties.FilePropertiesDialogButtons;
import com.techsenger.greywolf.storage.ui.properties.FilePropertiesDialogConfig;
import com.techsenger.greywolf.storage.ui.properties.FilePropertiesDialogParams;
import com.techsenger.greywolf.storage.ui.properties.FilePropertiesDialogView;
import com.techsenger.greywolf.storage.ui.properties.FilePropertiesDialogViewModel;
import com.techsenger.patternfx.mvvm.ParentView;
import java.util.function.Supplier;
import javafx.event.ActionEvent;

/**
 * Opens the properties of a file in the container of the component the menu belongs to; does nothing when there is no
 * file or no container.
 *
 * @author Pavel Castornii
 */
public class FilePropertiesItemHandler extends AbstractGreyWolfHandler<ActionEvent> {

    private final ParentView<?> view;

    private final Supplier<@Nullable GreyWolfFile> fileSupplier;

    /**
     * Creates a handler.
     *
     * @param greyWolf the application
     * @param view the component the menu belongs to
     * @param fileSupplier tells the file to show the properties of, or {@code null} for none
     */
    public FilePropertiesItemHandler(GreyWolf greyWolf, ParentView<?> view,
            Supplier<@Nullable GreyWolfFile> fileSupplier) {
        super(greyWolf);
        this.view = view;
        this.fileSupplier = fileSupplier;
    }

    @Override
    public void handle(ActionEvent event) {
        var file = fileSupplier.get();
        var container = ContainerUtils.findContainer(view);
        if (file == null || container == null) {
            return;
        }
        var config = getGreyWolf().getConfigManager().getOrCreateConfig(FilePropertiesDialogConfig.class,
                FilePropertiesDialogConfig::new);
        var params = new FilePropertiesDialogParams(config, getGreyWolf().getSettings(), file);
        var viewModel = new FilePropertiesDialogViewModel<>(params);
        var dialogView = new FilePropertiesDialogView(viewModel, getGreyWolf().getStylesheetRegistry());
        dialogView.initialize();
        container.addDialog(dialogView);
        var dialog = dialogView.getViewModel();
        dialog.setOnResult((name) -> {
            if (name == FilePropertiesDialogButtons.CLOSE) {
                dialog.closeSafely();
            }
        });
        dialog.setVisible(true);
    }
}
