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

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.base.GreyWolfStorage;
import com.techsenger.greywolf.storage.base.GreyWolfStorageProvider;
import com.techsenger.greywolf.storage.base.FormatException;
import com.techsenger.shellfx.core.config.ConfigUtils;
import com.techsenger.shellfx.dialogs.alert.AlertDialogParams;
import com.techsenger.shellfx.dialogs.alert.AlertDialogType;
import com.techsenger.shellfx.material.UiExecutor;
import com.techsenger.shellfx.material.button.ResultButtonName;
import com.techsenger.shellfx.storage.base.OperationType;
import com.techsenger.shellfx.storage.base.UriUtils;
import java.net.URI;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * View model of the dialog of a transfer started from the source side. The target is typed by the user and resolved
 * against the storages when OK is clicked; a single source may also be given a new name in an existing folder, if
 * the params allow it.
 *
 * @param <C> the composer type.
 * @author Pavel Castornii
 */
public final class SourceDialogViewModel<C extends TransferDialogComposer>
        extends AbstractTransferDialogViewModel<C> implements SourceDialogPort {

    /**
     * The outcome of resolving the entered target: the target, or the reason it couldn't be resolved.
     */
    private record Resolution(@Nullable GreyWolfFile target, @Nullable String warning) {
    }

    private final GreyWolfStorageProvider storageProvider;

    private final @Nullable GreyWolfFile currentFile;

    private final GreyWolfFile initialTarget;

    /**
     * The live text currently shown in the target field, exactly as typed. Arbitrary in-progress keystrokes are
     * not a storage-confirmed value; only {@code resolveTarget} may turn it into one.
     */
    private final StringProperty target = new SimpleStringProperty("");

    /**
     * The targets the user has transferred to, as the URIs kept in the config.
     */
    private final ObservableList<URI> targetUris = FXCollections.observableArrayList();

    /**
     * The path texts of {@link #targetUris}, as they are shown to the user.
     */
    private final ObservableList<String> targets = FXCollections.observableArrayList();

    private final ReadOnlyObjectWrapper<@Nullable GreyWolfFile> resultTarget = new ReadOnlyObjectWrapper<>();

    private boolean resolving;

    public SourceDialogViewModel(SourceDialogParams params) {
        super(params);
        this.storageProvider = params.getStorageProvider();
        this.currentFile = params.getCurrentFile();
        this.initialTarget = params.getInitialTarget();
        var storage = initialTarget.getStorage();
        setTarget(storage.getLocationConverter().toLocation(initialTarget.getUri()));
    }

    @Override
    public GreyWolfFile getInitialTarget() {
        return initialTarget;
    }

    @Override
    public String getTarget() {
        return target.get();
    }

    @Override
    public void setTarget(String target) {
        this.target.set(target);
    }

    @Override
    public StringProperty targetProperty() {
        return target;
    }

    @Override
    public ObservableList<String> getTargets() {
        return targets;
    }

    @Override
    public @Nullable GreyWolfFile getResultTarget() {
        return resultTarget.get();
    }

    @Override
    public ReadOnlyObjectProperty<@Nullable GreyWolfFile> resultTargetProperty() {
        return resultTarget.getReadOnlyProperty();
    }

    @Override
    protected void loadStateFromConfig() {
        super.loadStateFromConfig();
        updateTargets(getConfig().getTargets());
    }

    @Override
    protected void observeStateForConfig() {
        super.observeStateForConfig();
        var config = getConfig();
        targetUris.addListener((ListChangeListener<URI>) change -> {
            config.setTargets(new ArrayList<>(targetUris));
            config.notifyListeners();
        });
    }

    @Override
    protected SourceDialogConfig getConfig() {
        return (SourceDialogConfig) super.getConfig();
    }

    @Override
    protected void confirm(ResultButtonName buttonName) {
        if (resolving) {
            return;
        }
        resolving = true;
        // everything the resolution needs from the dialog is read here, it doesn't run on the UI thread
        var text = getTarget();
        var candidates = candidateStorages();
        Thread.startVirtualThread(() -> {
            var resolution = resolveTarget(text, candidates);
            UiExecutor.execute(() -> onTargetResolved(resolution, buttonName));
        });
    }

    private void showWarning(String text) {
        var params = new AlertDialogParams(getWindowType(), getSettings(), AlertDialogType.WARNING);
        var dialog = getComposer().openAlertDialog(params);
        dialog.setMessage(text);
        dialog.setVisible(true);
    }

    /**
     * Every storage {@code resolveTarget} should try, the storage of the target directory first - it is the most
     * likely match, so it gets a dedicated first attempt instead of landing wherever it does in the pool of
     * {@link #storageProvider}.
     */
    private List<GreyWolfStorage> candidateStorages() {
        var candidates = new ArrayList<GreyWolfStorage>();
        var candidate = getInitialTarget().getStorage();
        candidates.add(candidate);
        for (var storage : storageProvider.getStorages()) {
            if (!storage.getUri().equals(candidate.getUri())) {
                candidates.add(storage);
            }
        }
        return candidates;
    }

    /**
     * Resolves the entered text into the directory to transfer into: tried against the location converter of each
     * candidate storage in turn, since a plain path's syntax alone doesn't say which storage it belongs
     * to. An existing folder is the target itself; a text that doesn't exist is a new name, see
     * {@link #resolveNewFile}. Blocks on the storage, so it must not run on the UI thread.
     */
    private Resolution resolveTarget(String text, List<GreyWolfStorage> candidates) {
        GreyWolfStorage targetStorage = null;
        URI targetUri = null;
        for (var storage : candidates) {
            try {
                var candidate = storage.getLocationConverter().fromLocation(text);
                if (storage.refersToStorage(candidate)) {
                    targetStorage = storage;
                    targetUri = candidate;
                    break;
                }
            } catch (FormatException ex) {
                // This storage doesn't recognize the text's syntax at all; try the next candidate.
            }
        }
        if (targetStorage == null || targetUri == null) {
            return new Resolution(null, "Couldn't resolve target '" + text + "'.");
        }
        GreyWolfFile resolved;
        try {
            resolved = targetStorage.getFile(targetUri, OperationType.SECONDARY);
        } catch (NoSuchFileException ex) {
            return resolveNewFile(targetStorage, targetUri, text);
        } catch (Exception ex) {
            return new Resolution(null, "'" + text + "' does not exist.");
        }
        if (!resolved.isDirectory()) {
            return new Resolution(null, "'" + text + "' is not a folder.");
        }
        return new Resolution(resolved, null);
    }

    /**
     * Resolves the target of a rename: the entered path doesn't exist, so it is the new file of the single
     * transferred object in an existing folder. The result is a virtual file with the name, the URI and the entry
     * type of the current file. A path that ends with a separator can only denote a folder. Blocks on the storage,
     * so it must not run on the UI thread.
     */
    private Resolution resolveNewFile(GreyWolfStorage storage, URI uri, String text) {
        var parentUri = UriUtils.getParentUri(storage.getUri(), uri);
        if (currentFile == null || parentUri == null || uri.getPath().endsWith("/")) {
            return new Resolution(null, "'" + text + "' does not exist.");
        }
        if (getSourceFiles().size() != 1) {
            return new Resolution(null,
                    "'" + text + "' does not exist. A new name can be given to a single object only.");
        }
        GreyWolfFile parent;
        try {
            parent = storage.getFile(parentUri, OperationType.SECONDARY);
        } catch (Exception ex) {
            return new Resolution(null, "The folder of '" + text + "' does not exist.");
        }
        if (!parent.isDirectory()) {
            return new Resolution(null, "The folder of '" + text + "' is not a folder.");
        }
        var segments = UriUtils.getPathSegments(storage.getUri(), uri);
        return new Resolution(storage.createVirtual(uri, currentFile.getEntryType(),
                segments.get(segments.size() - 1)), null);
    }

    /**
     * Continues {@link #confirm} on the UI thread once the target is resolved.
     */
    private void onTargetResolved(Resolution resolution, ResultButtonName buttonName) {
        resolving = false;
        var resolved = resolution.target();
        if (resolved == null) {
            showWarning(Objects.requireNonNull(resolution.warning()));
            return;
        }
        // a new file is not remembered
        if (!resolved.isVirtual()) {
            var updatedTargets = new ArrayList<>(targetUris);
            ConfigUtils.addFirst(updatedTargets, resolved.getUri());
            ConfigUtils.limit(updatedTargets);
            updateTargets(updatedTargets);
        }
        resultTarget.set(resolved);
        accept(buttonName);
    }

    /**
     * Replaces the remembered targets, with the texts shown for them: a target is shown by the path of the storage
     * it belongs to.
     */
    private void updateTargets(List<URI> uris) {
        var texts = new ArrayList<String>(uris.size());
        for (var uri : uris) {
            var text = UriUtils.toHumanString(uri);
            for (var storage : storageProvider.getStorages()) {
                if (storage.refersToStorage(uri)) {
                    text = storage.getLocationConverter().toLocation(uri);
                    break;
                }
            }
            texts.add(text);
        }
        targetUris.setAll(uris);
        targets.setAll(texts);
    }
}
