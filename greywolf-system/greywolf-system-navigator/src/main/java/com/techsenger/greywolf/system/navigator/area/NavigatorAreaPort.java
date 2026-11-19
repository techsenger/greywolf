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

import com.techsenger.annotations.Nullable;
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.greywolf.core.panels.Placement;
import com.techsenger.greywolf.system.navigator.mark.MarkOperationType;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.shellfx.core.area.AreaPort;
import com.techsenger.shellfx.material.RequestSetter;
import com.techsenger.shellfx.material.table.TableColumnInfoAdapter;
import com.techsenger.shellfx.material.table.TableColumnName;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.ObservableList;

/**
 * Provides full access to the component's client API.
 *
 * @author Pavel Castornii
 */
public interface NavigatorAreaPort extends AreaPort {

    @Nullable Placement getPlacement();

    /**
     * Tells this area where its owning tab is placed, so it can switch to the view modes remembered for it and
     * the columns - set once for the initial attach and again on every later move between panels or to/from a
     * top-level tab.
     *
     * @param placement the tab's new placement, or {@code null} while unattached.
     */
    void setPlacement(@Nullable Placement placement);

    ObjectProperty<@Nullable Placement> placementProperty();

    /**
     * Returns the directory currently listed by this area.
     */
    GreyWolfFile getDirectory();

    /**
     * Navigates this area to {@code dir}, replacing whatever directory it currently lists; the location bar and
     * location tab are kept in sync with the outcome.
     *
     * @param dir the directory to navigate to.
     */
    @RequestSetter
    void setDirectory(GreyWolfFile dir);

    ReadOnlyObjectProperty<GreyWolfFile> directoryProperty();

    /**
     * Returns the URIs of the directories this area has visited, oldest first, as a read-only view; the entry at
     * {@link #getHistoryIndex()} is the directory currently shown.
     */
    @Unmodifiable ObservableList<URI> getHistoryUris();

    /**
     * Returns the position of the currently shown directory within {@link #getHistoryUris()}, or {@code -1}
     * before this area has navigated anywhere.
     */
    int getHistoryIndex();

    /**
     * Navigates this area to the history entry at {@code index}. The index is only confirmed once the directory
     * has been read; if reading it fails, it goes back to the previous value and an error is shown.
     *
     * @param index the position in the history to navigate to; an out-of-range value is ignored.
     */
    @RequestSetter
    void setHistoryIndex(int index);

    IntegerProperty historyIndexProperty();

    boolean isBackAvailable();

    ReadOnlyBooleanProperty backAvailableProperty();

    boolean isForwardAvailable();

    ReadOnlyBooleanProperty forwardAvailableProperty();

    boolean isUpAvailable();

    ReadOnlyBooleanProperty upAvailableProperty();

    /**
     * Tells whether the area is loading a directory: its content is covered then and does not react to the user.
     */
    boolean isWaiting();

    ReadOnlyBooleanProperty waitingProperty();

    /**
     * Returns the column count of the last view mode that has one (list or tiles); stays unchanged while the area
     * is in a mode without a column count, see {@link ViewMode#hasColumnCount()}.
     */
    int getColumnCount();

    ReadOnlyIntegerProperty columnCountProperty();

    /**
     * Returns the entries currently shown, including the synthetic parent (&quot;..&quot;) entry when one
     * applies. Reflects the last listing pushed by {@link #getDirectory}.
     */
    @Unmodifiable ObservableList<GreyWolfFile> getFiles();

    /**
     * Returns the entry the user currently has focused/selected, or {@code null} if nothing is selected.
     */
    @Nullable GreyWolfFile getFile();

    /**
     * Selects {@code file}, or clears the selection when {@code file} is {@code null}.
     *
     * @param file the entry to select, or {@code null} to clear the selection.
     */
    @RequestSetter
    void setFile(@Nullable GreyWolfFile file);

    ReadOnlyObjectProperty<@Nullable GreyWolfFile> fileProperty();

    /**
     * Returns what the context menu is open for: the selected file or an empty place of the area, or
     * {@link ContextTarget#NONE} while the menu is closed.
     */
    ContextTarget getContextTarget();

    ReadOnlyObjectProperty<ContextTarget> contextTargetProperty();

    /**
     * Returns the index of {@link #getFile()} within {@link #getFiles()}, or {@code -1} if nothing is
     * selected.
     */
    int getFileIndex();

    /**
     * Selects the entry at {@code index}.
     *
     * @param index the position to select.
     */
    @RequestSetter
    void setFileIndex(int index);

    ReadOnlyIntegerProperty fileIndexProperty();

    ViewModeType getViewModeType();

    /**
     * Switches this area to the given view mode type - keeps whatever column count that type last had, rather
     * than resetting it to a default.
     *
     * @param type the view mode type to switch to.
     */
    void setViewModeType(ViewModeType type);

    ObjectProperty<ViewModeType> viewModeTypeProperty();

    /**
     * Returns the view mode this area is currently in - one of list, tiles or details, told apart by
     * {@link ViewMode#getType()}.
     */
    ViewMode getViewMode();

    ReadOnlyObjectProperty<ViewMode> viewModeProperty();

    /**
     * Returns this area's own, live column state, one entry per known column whether currently shown or hidden.
     */
    @Unmodifiable Map<TableColumnName, TableColumnInfoAdapter> getColumnInfos();

    /**
     * Returns the number of entries in {@link #getFiles()} (excluding the synthetic parent &quot;..&quot;
     * entry) that are folders, or {@code -1} before the first listing has loaded.
     */
    int getFolderCount();

    ReadOnlyIntegerProperty folderCountProperty();

    /**
     * Returns the number of entries in {@link #getFiles()} (excluding the synthetic parent &quot;..&quot;
     * entry) that are regular files, or {@code -1} before the first listing has loaded.
     */
    int getFileCount();

    ReadOnlyIntegerProperty fileCountProperty();

    /**
     * Returns the number of currently marked entries in {@link #getFiles()}, or {@code -1} before the first
     * listing has loaded.
     */
    int getMarkedCount();

    ReadOnlyIntegerProperty markedCountProperty();

    /**
     * Returns the text describing the selected entry (its name, &quot;..&quot; for the parent entry, or the path a
     * link points to), or {@code null} if nothing is selected.
     */
    @Nullable String getFileInfo();

    ReadOnlyStringProperty fileInfoProperty();

    /**
     * Returns the comparator currently applied to {@link #getFiles()}.
     */
    Comparator<GreyWolfFile> getFileComparator();

    ReadOnlyObjectProperty<Comparator<GreyWolfFile>> fileComparatorProperty();

    /**
     * Returns the marked entries in {@link #getFiles()}, in their current display order, or a single-element
     * list containing {@link #getFile()} if nothing is marked &mdash; excluding the synthetic parent
     * (&quot;..&quot;) entry either way. Empty if there is nothing to copy.
     */
    List<GreyWolfFile> getMarkedFilesInOrder();

    /**
     * Reloads this area's directory listing from storage and, if present in the new listing, selects
     * {@code fileToSelect} &mdash; used after an out-of-band storage change (e.g. creating a new file/folder) so
     * the area reflects it immediately instead of waiting for the next navigation.
     *
     * @param fileToSelect the entry to re-select if still present after the reload, or {@code null} to select the
     *     usual fallback.
     */
    void refresh(@Nullable GreyWolfFile fileToSelect);

    /**
     * Reloads this area's directory listing after {@code removed} has been taken out of it (deleted or moved
     * away), keeping the cursor in place: the selected entry stays selected if it was not removed, otherwise the
     * nearest remaining entry below it is selected, or above it if there is none below.
     *
     * @param removed the entries that are gone from the directory; may contain entries not shown in this area.
     */
    void refreshAfterRemoval(List<? extends GreyWolfFile> removed);

    /**
     * Reloads this area's directory listing if it shows {@code folder}, whose content was changed from outside this
     * area, such as by a paste started in a directory tree; does nothing for any other folder.
     *
     * @param folder the folder whose content has changed
     */
    void onContentChanged(GreyWolfFile folder);

    /**
     * Moves this area one step back in its navigation history; does nothing if already at the oldest entry.
     */
    void onBack();

    /**
     * Moves this area one step forward in its navigation history; does nothing if already at the newest entry.
     */
    void onForward();

    /**
     * Navigates to the synthetic parent (&quot;..&quot;) entry of this area's listing, if one is present; does
     * nothing at a storage's root, where no parent entry exists.
     */
    void onUp();

    /**
     * Reloads this area's current directory listing, preserving the current selection.
     */
    void onRefresh();

    /**
     * Notifies this area that the column-count control was changed to {@code count} - applied to the current
     * mode if it is list or tiles; ignored when the current mode is details, which has no column count, since
     * any mode may be active at the moment this fires.
     *
     * @param count the column count that was picked.
     */
    void onColumnCountChanged(int count);

    /**
     * Notifies this area that {@code name} was checked in the Columns menu - shown as this area's last column
     * if not already visible; any mode may be active at the moment this fires, so whether it is currently
     * meaningful to the user is this area's own call, not the caller's.
     *
     * @param name the column that was checked.
     */
    void onAddColumn(TableColumnName name);

    /**
     * Notifies this area that {@code name} was unchecked in the Columns menu - hidden if currently shown; any
     * mode may be active at the moment this fires, so whether it is currently meaningful to the user is this
     * area's own call, not the caller's.
     *
     * @param name the column that was unchecked.
     */
    void onRemoveColumn(TableColumnName name);

    /**
     * Marks every regular entry (i.e. excluding the synthetic parent &quot;..&quot; entry) in {@link #getFiles()}.
     */
    void onMarkAll();

    /**
     * Unmarks every entry in {@link #getFiles()}.
     */
    void onUnmarkAll();

    /**
     * Flips the mark of every regular entry (i.e. excluding the synthetic parent &quot;..&quot; entry) in
     * {@link #getFiles()}.
     */
    void onInvertMarks();

    /**
     * Opens the mark-by-mask dialog configured for {@code operationType}; once the user confirms via Apply/OK,
     * it is applied to every entry matching the dialog's mask and file/folder criteria.
     *
     * @param operationType the mark action to apply once the user confirms.
     */
    void onMark(MarkOperationType operationType);

    /**
     * Opens the file properties dialog for {@link #getFile()}; does nothing if nothing is selected.
     */
    void onFileProperties();

    /**
     * Opens the file properties dialog for {@code file}, which is not necessarily shown in this area.
     *
     * @param file the file to show the info of
     */
    void onFileProperties(GreyWolfFile file);

    /**
     * Opens the create-file dialog for this area's current directory.
     */
    void onCreateFile();

    /**
     * Opens the create-folder dialog for this area's current directory.
     */
    void onCreateFolder();

    /**
     * Places the marked entries in this area (or the selected entry, if none are marked) into the shared
     * clipboard as a cut, so a later paste elsewhere removes them from here; does nothing if there is nothing
     * marked or selected.
     */
    void onCutToClipboard();

    /**
     * Places the marked entries in this area (or the selected entry, if none are marked) into the shared
     * clipboard as a copy; does nothing if there is nothing marked or selected.
     */
    void onCopyToClipboard();

    /**
     * Transfers whatever the shared clipboard currently holds into this area's current directory; does nothing
     * if the clipboard is empty.
     */
    void onPasteFromClipboard();

    /**
     * Transfers whatever the shared clipboard currently holds into {@code folder}, which is not necessarily the
     * current directory of this area; does nothing if the clipboard is empty.
     *
     * @param folder the folder to transfer into
     */
    void onPasteIntoFolder(GreyWolfFile folder);

    /**
     * Copies the marked entries in this area (or the selected entry, if none are marked) onto the other
     * panel's current directory; does nothing if there is nothing to copy.
     */
    void onCopyToPanel();

    /**
     * Moves the marked entries in this area (or the selected entry, if none are marked) onto the other
     * panel's current directory; does nothing if there is nothing to move.
     */
    void onMoveToPanel();

    /**
     * Opens the rename dialog for the selected entry; does nothing if nothing is selected.
     */
    void onRename();

    /**
     * Deletes the marked entries in this area (or the selected entry, if none are marked) after the user
     * confirms; does nothing if there is nothing marked or selected.
     */
    void onDelete();

    /**
     * Opens the selected entry the way a double-click or Enter on it does: a directory, or a link to one, is
     * entered; a runnable file is run; anything else is opened with the platform's default application. Does
     * nothing if nothing is selected.
     */
    void onOpen();

    /**
     * Opens {@code folder} within this area, e.g. after a double-click or Enter on a directory entry.
     *
     * @param folder the folder to open.
     */
    void onOpenIn(GreyWolfFile folder);

    /**
     * Opens {@code folder} in a new navigator tab next to the tab of this area.
     *
     * @param folder the folder to open; a link to a folder is opened at the folder it points to.
     */
    void onOpenInNewTab(GreyWolfFile folder);

    /**
     * Opens {@code file} with the platform's default application, e.g. after a double-click or Enter on a
     * regular file entry.
     *
     * @param file the file to open.
     */
    void onOpenFile(GreyWolfFile file);

    /**
     * Runs {@code file} as a program detached from GreyWolf, in the directory of the file. A failure to start it
     * is reported to the user.
     *
     * @param file the executable file to run.
     */
    void onRunFile(GreyWolfFile file);
}
