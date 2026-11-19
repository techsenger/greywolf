/*
 * Copyright 2024-2026 Pavel Castornii.
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

package com.techsenger.greywolf.storage.base;

import com.techsenger.shellfx.storage.base.OperationType;
import com.techsenger.shellfx.storage.base.async.DefaultFileRefiner;
import com.techsenger.shellfx.storage.base.async.LinkUtils;
import java.io.IOException;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The file refiner of GreyWolf. Besides what the default refiner does, it gives the regular files whose name is
 * not enough their type by the start of the content.
 *
 * @author Pavel Castornii
 */
public class GreyWolfFileRefiner extends DefaultFileRefiner<GreyWolfFile> {

    private static final Logger logger = LoggerFactory.getLogger(GreyWolfFileRefiner.class);

    private static final int HEADER_SIZE = 2048;

    /**
     * Creates a refiner over the storages that are there at the moment of each operation.
     *
     * @param storageProvider tells the storages the targets of links may lie in
     */
    public GreyWolfFileRefiner(GreyWolfStorageProvider storageProvider) {
        super(storageProvider);
    }

    @Override
    public boolean isRefinable(GreyWolfFile file) {
        return needsHeader(file) || super.isRefinable(file);
    }

    @Override
    protected Runnable refine(GreyWolfFile file) throws IOException {
        var actions = new ArrayList<Runnable>();
        if (needsHeader(file)) {
            var resolution = readType(file);
            actions.add(() -> FileTypeResolver.apply(file, resolution));
        }
        if (LinkUtils.isUnresolved(file)) {
            actions.add(super.refine(file));
        }
        return () -> actions.forEach(Runnable::run);
    }

    /**
     * Tells whether the type of the file is still unknown, as its name is not enough and the start of its content
     * must be read. A virtual file has no content to read.
     */
    private boolean needsHeader(GreyWolfFile file) {
        return file.isFile() && !file.isVirtual() && file.getType() == null;
    }

    private FileTypeResolver.Resolution readType(GreyWolfFile file) {
        try {
            var header = file.getStorage().readFileHeader(file.getUri(), HEADER_SIZE, OperationType.SECONDARY);
            return FileTypeResolver.resolveByHeader(file.getName(), header);
        } catch (IOException ex) {
            // the content can't be read, so the file is an ordinary one, not a failed one
            logger.error("Error reading the header of {}", file.getUri(), ex);
            return new FileTypeResolver.Resolution(FileType.APPLICATION, FileTypeResolver.OCTET_STREAM);
        }
    }
}
