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

package com.techsenger.greywolf.system.navigator.area;

import com.techsenger.greywolf.system.navigator.NavigatorFileColumns;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.ui.style.FileLabels;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.table.NamedTableColumn;
import com.techsenger.shellfx.material.table.TextFieldTableCell;
import com.techsenger.shellfx.storage.base.StorageFile;
import com.techsenger.shellfx.storage.ui.DecorationCellUtils;
import com.techsenger.shellfx.storage.ui.FileCellUpdater;
import com.techsenger.shellfx.storage.ui.FileColumnBuilder;
import com.techsenger.shellfx.storage.ui.FileColumns;
import com.techsenger.shellfx.storage.ui.FileStringConverter;
import com.techsenger.shellfx.storage.ui.style.FileDecorator;
import java.util.Comparator;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

/**
 *
 * @author Pavel Castornii
 */
final class NavigatorColumnBuilder extends FileColumnBuilder<GreyWolfFile> {

    private static String getDisplayName(StorageFile file) {
        if (file instanceof GreyWolfFile greyWolfFile && greyWolfFile.isParentEntry()) {
            return "..";
        }
        return file.getName();
    }

    NavigatorColumnBuilder(Font font, FileDecorator<GreyWolfFile> decorator) {
        super(font, decorator);
    }

    @Override
    public NamedTableColumn<GreyWolfFile, GreyWolfFile> buildNameColumn() {
        var nameColumn = new NamedTableColumn<GreyWolfFile, GreyWolfFile>(FileColumns.NAME, "Name");
        nameColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<GreyWolfFile>(data.getValue()));
        var converter = new FileStringConverter<GreyWolfFile>();
        nameColumn.setCellFactory(col -> new TextFieldTableCell<GreyWolfFile, GreyWolfFile>(converter) {

            private final FontIconView iconView = new FontIconView();

            private final FileCellUpdater<GreyWolfFile> updater = new FileCellUpdater<>(file ->
                    DecorationCellUtils.updateDecoration(this, iconView, getDecorator().getDecoration(file)));

            @Override
            public void updateItem(GreyWolfFile file, boolean empty) {
                super.updateItem(file, empty);
                if (file == null || empty) {
                    updater.unobserve();
                    DecorationCellUtils.updateStyles(this, iconView, null);
                    setGraphic(null);
                    setText(null);
                } else {
                    getDecorator().decorate(file);
                    updater.observe(file, getDecorator()::decorationValue);
                    setGraphic(iconView);
                    setText(getDisplayName(file));
                }
            }

            @Override
            protected HBox buildEditGraphic() {
                return new HBox(iconView, getTextField());
            }

            @Override
            protected void updateDisplay() {
                var file = getItem();
                if (file != null) {
                    setGraphic(iconView);
                    setText(getDisplayName(file));
                }
            }
        });
        nameColumn.setComparator(Comparator.comparing(StorageFile::getName, String.CASE_INSENSITIVE_ORDER));
        return nameColumn;
    }

    public NamedTableColumn<GreyWolfFile, GreyWolfFile> buildTypeColumn() {
        var typeColumn = new NamedTableColumn<GreyWolfFile, GreyWolfFile>(NavigatorFileColumns.TYPE, "Type");
        typeColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<GreyWolfFile>(data.getValue()));
        typeColumn.setCellFactory(callBack -> new TableCell<GreyWolfFile, GreyWolfFile>() {

                private final FileCellUpdater<GreyWolfFile> updater = new FileCellUpdater<>(file -> {
                    DecorationCellUtils.updateDecoration(this, null, getDecorator().getDecoration(file));
                    setText(FileLabels.ofEntryOrFileType(file));
                });

                @Override
                protected void updateItem(GreyWolfFile file, boolean empty) {
                    super.updateItem(file, empty);
                    if (file == null || empty) {
                        updater.unobserve();
                        DecorationCellUtils.updateStyles(this, null, null);
                        setText(null);
                    } else {
                        getDecorator().decorate(file);
                        updater.observe(file, getDecorator()::decorationValue, GreyWolfFile::typeProperty);
                    }
                }
            }
        );
        typeColumn.setComparator(Comparator.comparing((GreyWolfFile f) -> FileLabels.ofEntryOrFileType(f),
                Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER)));
        typeColumn.setResizable(true);
        return typeColumn;
    }
}
