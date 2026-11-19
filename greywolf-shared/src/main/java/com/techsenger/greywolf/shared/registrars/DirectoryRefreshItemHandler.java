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

import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.core.registrar.AbstractGreyWolfHandler;
import com.techsenger.greywolf.shared.directory.DirectoryTreePort;
import javafx.event.ActionEvent;

/**
 * Refreshes the folder of the node of the directory tree the context menu was requested for.
 *
 * @author Pavel Castornii
 */
public class DirectoryRefreshItemHandler extends AbstractGreyWolfHandler<ActionEvent> {

    private final DirectoryTreePort tree;

    public DirectoryRefreshItemHandler(GreyWolf greyWolf, DirectoryTreePort tree) {
        super(greyWolf);
        this.tree = tree;
    }

    @Override
    public void handle(ActionEvent event) {
        var directory = tree.getFile();
        if (directory != null) {
            tree.onDirectoryRefresh(directory);
        }
    }
}
