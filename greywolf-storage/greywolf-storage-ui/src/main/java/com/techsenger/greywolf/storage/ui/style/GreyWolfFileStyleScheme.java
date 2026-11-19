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

package com.techsenger.greywolf.storage.ui.style;

import com.techsenger.shellfx.storage.ui.style.FileStyleScheme;
import com.techsenger.shellfx.storage.ui.style.FileStyleSettings;

/**
 * The styles of all kinds of files of GreyWolf for one theme: the kinds of files the storage knows plus a style for
 * every type of a file, and the masks of the states (executable, marked, unavailable) that are laid over the style of
 * the kind.
 *
 * @author Pavel Castornii
 */
public class GreyWolfFileStyleScheme extends FileStyleScheme {

    private FileStyleSettings archiveFile = new FileStyleSettings();

    private FileStyleSettings codeFile = new FileStyleSettings();

    private FileStyleSettings textFile = new FileStyleSettings();

    private FileStyleSettings documentFile = new FileStyleSettings();

    private FileStyleSettings imageFile = new FileStyleSettings();

    private FileStyleSettings audioFile = new FileStyleSettings();

    private FileStyleSettings videoFile = new FileStyleSettings();

    private FileStyleSettings fontFile = new FileStyleSettings();

    private FileStyleSettings applicationFile = new FileStyleSettings();

    private FileStyleSettings executable = new FileStyleSettings();

    private FileStyleSettings marked = new FileStyleSettings();

    /**
     * The mask of a file whose storage was not found. Unlike the error mask, which also repaints the icon, it only
     * paints the text as failed, because the kind of such a file is known and its icon keeps the color of the kind.
     */
    private FileStyleSettings unavailable = new FileStyleSettings();

    public FileStyleSettings getArchiveFile() {
        return this.archiveFile;
    }

    public void setArchiveFile(FileStyleSettings archiveFile) {
        this.archiveFile = archiveFile;
    }

    public FileStyleSettings getCodeFile() {
        return this.codeFile;
    }

    public void setCodeFile(FileStyleSettings codeFile) {
        this.codeFile = codeFile;
    }

    public FileStyleSettings getTextFile() {
        return this.textFile;
    }

    public void setTextFile(FileStyleSettings textFile) {
        this.textFile = textFile;
    }

    public FileStyleSettings getDocumentFile() {
        return this.documentFile;
    }

    public void setDocumentFile(FileStyleSettings documentFile) {
        this.documentFile = documentFile;
    }

    public FileStyleSettings getImageFile() {
        return this.imageFile;
    }

    public void setImageFile(FileStyleSettings imageFile) {
        this.imageFile = imageFile;
    }

    public FileStyleSettings getAudioFile() {
        return this.audioFile;
    }

    public void setAudioFile(FileStyleSettings audioFile) {
        this.audioFile = audioFile;
    }

    public FileStyleSettings getVideoFile() {
        return this.videoFile;
    }

    public void setVideoFile(FileStyleSettings videoFile) {
        this.videoFile = videoFile;
    }

    public FileStyleSettings getFontFile() {
        return this.fontFile;
    }

    public void setFontFile(FileStyleSettings fontFile) {
        this.fontFile = fontFile;
    }

    public FileStyleSettings getApplicationFile() {
        return this.applicationFile;
    }

    public void setApplicationFile(FileStyleSettings applicationFile) {
        this.applicationFile = applicationFile;
    }

    public FileStyleSettings getExecutable() {
        return this.executable;
    }

    public void setExecutable(FileStyleSettings executable) {
        this.executable = executable;
    }

    public FileStyleSettings getMarked() {
        return this.marked;
    }

    public void setMarked(FileStyleSettings marked) {
        this.marked = marked;
    }

    public FileStyleSettings getUnavailable() {
        return this.unavailable;
    }

    public void setUnavailable(FileStyleSettings unavailable) {
        this.unavailable = unavailable;
    }
}
