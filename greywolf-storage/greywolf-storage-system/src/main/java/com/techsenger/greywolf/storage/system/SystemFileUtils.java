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

package com.techsenger.greywolf.storage.system;

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.storage.base.GreyWolfEntryLink;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.base.DefaultGreyWolfEntryLink;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/**
 *
 * @author Pavel Castornii
 */
final class SystemFileUtils {

    static byte[] readFileHeader(URI uri, int size) throws IOException {
        if (size < 0) {
            throw new IllegalArgumentException(
                    "Size must not be negative: " + size);
        }
        Path path = Paths.get(uri);
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            int bytesToRead = (int) Math.min(size, channel.size());
            byte[] buffer = new byte[bytesToRead];
            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
            while (byteBuffer.hasRemaining()) {
                if (channel.read(byteBuffer) < 0) {
                    break;
                }
            }
            return buffer;
        }
    }

    static InputStream openInputStream(URI uri) throws IOException {
        Path path = Paths.get(uri);
        return Files.newInputStream(path);
    }

    static OutputStream openOutputStream(URI uri) throws IOException {
        Path path = Paths.get(uri);
        return Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);
    }

    static void delete(URI uri) throws IOException {
        Path path = Paths.get(uri);
        Files.delete(path);
    }

    /**
     * Moves the entry in place and atomically, so a move that would require copying (another file system) fails
     * instead of being silently done as a black-box copy that can't be paused or cancelled. Nothing is checked
     * beforehand except the target; when the move fails, the cause is diagnosed to report the exceptions that
     * {@link Files#move} itself doesn't document.
     *
     * @param sourceUri the URI of the entry to move.
     * @param targetUri the URI the entry should get.
     * @throws NoSuchFileException if the source or the parent directory of the target doesn't exist.
     * @throws FileAlreadyExistsException if a different entry already exists at {@code targetUri}; checked
     *     beforehand, because for an atomic move it is implementation specific whether an existing target is
     *     replaced or reported.
     * @throws AccessDeniedException if the parent directory of the source or of the target is not writable.
     * @throws AtomicMoveNotSupportedException if the move would require copying, e.g. the target is on another
     *     file store.
     * @throws IOException for any other failure.
     */
    static void move(URI sourceUri, URI targetUri) throws IOException {
        var source = Paths.get(sourceUri);
        var target = Paths.get(targetUri);
        if (Files.exists(target, LinkOption.NOFOLLOW_LINKS) && !Files.isSameFile(source, target)) {
            throw new FileAlreadyExistsException(target.toString());
        }
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (FileSystemException ex) {
            throw ex;
        } catch (IOException ex) {
            if (!Files.exists(source, LinkOption.NOFOLLOW_LINKS)) {
                throw new NoSuchFileException(source.toString());
            }
            var targetParent = target.getParent();
            if (targetParent != null && !Files.exists(targetParent)) {
                throw new NoSuchFileException(targetParent.toString());
            }
            if (targetParent != null && !Files.isWritable(targetParent)) {
                throw new AccessDeniedException(targetParent.toString());
            }
            var sourceParent = source.getParent();
            if (sourceParent != null && !Files.isWritable(sourceParent)) {
                throw new AccessDeniedException(sourceParent.toString());
            }
            throw ex;
        }
    }

    /**
     * Creates the link of a file.
     *
     * @param targetUri the URI of the path the link states
     * @param resolvedUri the URI of the entry at the end of the chain of links, or {@code null} if there is none
     * @param target the file at the end of the chain of links, or {@code null} if the second loading phase has to
     *     find it or the link is broken
     * @return the link, never {@code null}
     */
    static GreyWolfEntryLink createLink(URI targetUri, @Nullable URI resolvedUri, @Nullable GreyWolfFile target) {
        var link = new DefaultGreyWolfEntryLink();
        link.setTargetUri(targetUri);
        link.setResolvedUri(resolvedUri);
        link.setTarget(target);
        return link;
    }

    private SystemFileUtils() {
        // empty
    }
}
