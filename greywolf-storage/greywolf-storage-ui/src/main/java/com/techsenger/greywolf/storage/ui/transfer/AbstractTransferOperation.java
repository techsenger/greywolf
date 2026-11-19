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
import com.techsenger.greywolf.storage.base.operation.ErrorResolution;
import com.techsenger.greywolf.storage.base.operation.NameMask;
import com.techsenger.greywolf.storage.base.operation.ScanListener;
import com.techsenger.greywolf.storage.base.transfer.AbstractTransferer;
import com.techsenger.greywolf.storage.base.transfer.ConflictDecision;
import com.techsenger.greywolf.storage.base.transfer.ConflictResolution;
import com.techsenger.greywolf.storage.base.transfer.CopyConflict;
import com.techsenger.greywolf.storage.base.transfer.FailedOperation;
import com.techsenger.greywolf.storage.base.transfer.TransferListener;
import com.techsenger.greywolf.storage.base.transfer.TransferPlan;
import com.techsenger.greywolf.storage.base.transfer.TransferProgress;
import com.techsenger.greywolf.storage.base.transfer.TransferScanner;
import com.techsenger.greywolf.storage.ui.operation.DialogParamsFactory;
import com.techsenger.greywolf.storage.ui.operation.DialogTimer;
import com.techsenger.greywolf.storage.ui.operation.OperationDialogs;
import com.techsenger.greywolf.storage.ui.operation.OperationTimings;
import com.techsenger.shellfx.core.config.ConfigManager;
import com.techsenger.shellfx.core.window.WindowType;
import com.techsenger.shellfx.dialogs.alert.AlertDialogType;
import com.techsenger.shellfx.dialogs.progress.ProgressDialogButtons;
import com.techsenger.shellfx.dialogs.progress.ProgressDialogPort;
import com.techsenger.shellfx.material.UiExecutor;
import com.techsenger.shellfx.material.button.ResultButtonName;
import com.techsenger.shellfx.storage.base.OperationType;
import com.techsenger.toolkit.core.file.FileUtils;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Runs one transfer (copy or move) end to end - the configuration dialog, then scanning, then the actual
 * transfer - by opening each dialog through the {@link TransferComposer} given to it once at construction, so it
 * keeps working across the whole run regardless of which of those dialogs is currently open or already closed.
 * Owns both the {@link TransferScanner} (listened to by {@link ScanHandler}) and the {@link AbstractTransferer}
 * (listened to by {@link TransferHandler}) directly, rather than leaving them to a dedicated dialog view model,
 * so the run survives independently of any one dialog's lifecycle - each dialog is purely a progress view,
 * interacted with only through its port, never its concrete view or view model. The scan phase has nothing
 * dialog-specific left to show at all, so it reuses the generic progress dialog. Every dialog this run may
 * need - including Conflict/Error/completion alerts - is opened by the composer, never nested inside another
 * dialog, since a dialog that owns none of these decisions may not even exist (e.g. once this run can be
 * backgrounded, with no dialog open at all).
 *
 * <p>The exact workflow is described by each concrete operation.
 *
 * @author Pavel Castornii
 */
public abstract class AbstractTransferOperation {

    /**
     * The stage of the run; only moves forward, and {@link #FINISHED} is reached from any stage that ends the run.
     */
    private enum Phase {
        NOT_STARTED,
        CONFIGURING,
        SCANNING,
        TRANSFERRING,
        FINISHED
    }

    private final class ScanHandler implements ScanListener<TransferPlan> {

        private final ProgressDialogPort dialog;

        private final DialogTimer timer;

        ScanHandler(ProgressDialogPort dialog, DialogTimer timer) {
            this.dialog = dialog;
            this.timer = timer;
        }

        @Override
        public void onScanProgress(int foldersFound, int filesFound, long bytesFound) {
            UiExecutor.execute(() -> dialog.setMessage("Found " + foldersFound + " folder(s), "
                    + filesFound + " file(s), " + FileUtils.formatSize(bytesFound) + "."));
        }

        @Override
        public void onScanCompleted(TransferPlan plan) {
            UiExecutor.execute(() -> timer.finish(() -> {
                closeDialog();
                openTransferringDialog(plan);
            }));
        }

        @Override
        public void onScanFailed(Exception ex) {
            UiExecutor.execute(() -> timer.finish(() -> {
                closeDialog();
                phase = Phase.FINISHED;
                dialogs.showAlert(AlertDialogType.ERROR, "Couldn't scan the marked files: " + ex.getMessage());
            }));
        }

        @Override
        public void onScanCancelled() {
            UiExecutor.execute(() -> timer.finish(() -> {
                closeDialog();
                phase = Phase.FINISHED;
            }));
        }

        private void closeDialog() {
            if (timer.isShown()) {
                dialog.closeSafely();
            }
        }
    }

    private final class TransferHandler implements TransferListener {

        private final TransferringDialogPort dialog;

        TransferHandler(TransferringDialogPort dialog) {
            this.dialog = dialog;
        }

        @Override
        public ConflictDecision onConflict(CopyConflict conflict) {
            return OperationDialogs.blockingAsk(future -> {
                var config = configManager.getOrCreateConfig(ConflictDialogConfig.class, ConflictDialogConfig::new);
                var params = new ConflictDialogParams(config, dialogParams.getSettings());
                ConflictDialogPort conflictDialog = composer.openConflictDialog(params);
                conflictDialog.setFilePath(conflict.task().targetUri().toString());
                conflictDialog.setOnResult((buttonName) -> {
                    var decision = toConflictDecision(buttonName, conflictDialog.isApplyToAllSelected());
                    conflictDialog.closeSafely();
                    future.complete(decision);
                });
                conflictDialog.setVisible(true);
            });
        }

        @Override
        public void onProgress(TransferProgress progress) {
            UiExecutor.execute(() -> dialog.setProgress(progress));
        }

        @Override
        public ErrorResolution onError(FailedOperation failure) {
            return dialogs.askErrorResolution(failure.message(), failure.task().source().getUri().toString());
        }

        @Override
        public void onCompleted(int filesCopied, int filesSkipped, List<FailedOperation> failures) {
            UiExecutor.execute(() -> finishTransfer(() -> {
                if (!failures.isEmpty()) {
                    dialogs.showAlert(AlertDialogType.WARNING,
                            filesCopied + " " + pastTense + ", " + filesSkipped + " skipped, " + failures.size()
                            + " failed.");
                }
            }));
        }

        @Override
        public void onCancelled() {
            UiExecutor.execute(() -> finishTransfer(() -> { }));
        }

        @Override
        public void onAborted(String reason) {
            UiExecutor.execute(() -> finishTransfer(() -> dialogs.showAlert(AlertDialogType.ERROR,
                    Character.toUpperCase(verb.charAt(0)) + verb.substring(1) + " aborted: " + reason)));
        }

        /**
         * Waits the minimum visible time, then closes the transfer dialog, reports {@code onFinished}, and finally
         * runs {@code afterClose} - so a summary/abort alert opens only once the transfer dialog itself is
         * actually gone, not layered on top of it.
         */
        private void finishTransfer(Runnable afterClose) {
            DialogTimer.runAfter(OperationTimings.RUN_MIN_VISIBLE_MILLIS, () -> {
                dialog.closeSafely();
                Objects.requireNonNull(onFinished).accept(Objects.requireNonNull(targetDirectory));
                phase = Phase.FINISHED;
                afterClose.run();
            });
        }
    }

    private final TransferComposer composer;

    private final TransferOperationType operationType;

    private final TransferDialogParams dialogParams;

    private final @Nullable GreyWolfFile initialTarget;

    private final ConfigManager configManager;

    private final OperationDialogs dialogs;

    private final String verb;

    private final String pastTense;

    private final TransferScanner scanner = new TransferScanner();

    private final AbstractTransferer transferer;

    private @Nullable Consumer<GreyWolfFile> onFinished;

    private @Nullable GreyWolfFile targetDirectory;

    private Phase phase = Phase.NOT_STARTED;

    /**
     * Creates an operation.
     *
     * @param initialTarget the directory to transfer into when the transfer is started from the target
     *     side, so the dialog doesn't ask for it; {@code null} when the dialog asks for the target.
     */
    protected AbstractTransferOperation(TransferComposer composer, TransferDialogParams dialogParams,
            @Nullable GreyWolfFile initialTarget, TransferOperationType operationType,
            AbstractTransferer transferer) {
        if (dialogParams instanceof TargetDialogParams && initialTarget == null) {
            throw new IllegalArgumentException("A transfer started from the target side needs its target directory");
        }
        this.composer = composer;
        this.initialTarget = initialTarget;
        this.transferer = transferer;
        this.operationType = operationType;
        var move = operationType == TransferOperationType.MOVE;
        this.verb = move ? "move" : "copy";
        this.pastTense = move ? "moved" : "copied";
        this.dialogParams = dialogParams;
        this.configManager = dialogParams.getConfigManager();
        this.dialogs = new OperationDialogs(composer, configManager, dialogParams.getSettings());
    }

    public void setOnFinished(Consumer<GreyWolfFile> onFinished) {
        this.onFinished = onFinished;
    }

    /**
     * Opens the configuration dialog, the first step of this run.
     */
    public void start() {
        if (phase != Phase.NOT_STARTED) {
            throw new IllegalStateException("Already started");
        }
        phase = Phase.CONFIGURING;
        openConfigDialog();
    }

    private void openConfigDialog() {
        switch (dialogParams) {
            case SourceDialogParams params -> {
                SourceDialogPort dialog = composer.openSourceDialog(params);
                handleConfigResult(dialog, () -> onSourceDialogReady(dialog));
                dialog.setVisible(true);
            }
            case TargetDialogParams params -> {
                TargetDialogPort dialog = composer.openTargetDialog(params);
                handleConfigResult(dialog, () -> openScanDialog(Objects.requireNonNull(initialTarget), null,
                        dialog));
                dialog.setVisible(true);
            }
            default -> throw new IllegalArgumentException("Unsupported params: "
                    + dialogParams.getClass().getName());
        }
    }

    private void onSourceDialogReady(SourceDialogPort dialog) {
        var resolved = Objects.requireNonNull(dialog.getResultTarget());
        if (!resolved.isVirtual()) {
            openScanDialog(resolved, null, dialog);
            return;
        }
        // the target is a new file in an existing folder, a rename
        var storage = resolved.getStorage();
        try {
            var parent = Objects.requireNonNull(storage.getParent(resolved, OperationType.SECONDARY));
            openScanDialog(parent, resolved.getName(), dialog);
        } catch (IOException ex) {
            phase = Phase.FINISHED;
            dialogs.showAlert(AlertDialogType.ERROR, "Couldn't open the folder of '" + resolved.getName() + "': "
                    + ex.getMessage());
        }
    }

    private void handleConfigResult(TransferDialogPort dialog, Runnable onOk) {
        dialog.setOnResult((name) -> {
            if (name == TransferDialogButtons.OK) {
                dialog.closeSafely();
                onOk.run();
            } else if (name == TransferDialogButtons.CANCEL) {
                dialog.closeSafely();
                phase = Phase.FINISHED;
            }
        });
    }

    /**
     * Starts scanning right away, but only shows the scan dialog - and so only actually blocks input on it -
     * once the scan show delay has passed and scanning is still running; a scan that finishes before then never
     * shows anything.
     */
    private void openScanDialog(GreyWolfFile targetDirectory, @Nullable String targetName,
            TransferDialogPort configDialog) {
        var targetStorage = targetDirectory.getStorage();
        var mask = new NameMask(configDialog.getMask(), configDialog.isCaseSensitiveSelected(),
                configDialog.isRegexSelected());
        this.targetDirectory = targetDirectory;
        phase = Phase.SCANNING;
        var params = DialogParamsFactory.createProgressParams(configManager, WindowType.NESTED,
                dialogParams.getSettings());
        var deferredDialog = composer.createProgressDialog(params);
        ProgressDialogPort dialog = deferredDialog.port();
        dialog.setTitle("Scanning");
        dialog.setRightButtons(ProgressDialogButtons.CANCEL);
        dialog.setOnResult((name) -> {
            if (name == ProgressDialogButtons.CANCEL) {
                confirmCancel(scanner::pause, scanner::cancel, scanner::resume);
            }
        });

        var timer = new DialogTimer(OperationTimings.SCAN_SHOW_DELAY_MILLIS,
                OperationTimings.SCAN_MIN_VISIBLE_MILLIS, deferredDialog::show);
        timer.start();

        scanner.scan(dialogParams.getSourceFiles(), targetDirectory, targetName, targetStorage, mask,
                new ScanHandler(dialog, timer));
    }

    private void openTransferringDialog(TransferPlan plan) {
        phase = Phase.TRANSFERRING;
        var config = configManager.getOrCreateConfig(TransferringDialogConfig.class,
                TransferringDialogConfig::new);
        var params = new TransferringDialogParams(config, dialogParams.getSettings(), operationType);
        TransferringDialogPort dialog = composer.openTransferringDialog(params);
        dialog.setOnResult((name) -> {
            if (name == TransferringDialogButtons.CANCEL) {
                confirmCancel(transferer::pause, transferer::cancel, transferer::resume);
            }
        });
        dialog.setVisible(true);
        transferer.start(plan, new TransferHandler(dialog));
    }

    /**
     * Pauses the in-progress run (scan or transfer, whichever is currently showing its Cancel-clicked dialog)
     * immediately via {@code pause} - before the confirmation dialog even opens - so it doesn't keep going while
     * the user is still deciding; {@code resume} runs on No, {@code cancel} runs (for good) on Yes.
     */
    private void confirmCancel(Runnable pause, Runnable cancel, Runnable resume) {
        pause.run();
        dialogs.confirm("Cancel the " + verb + " operation?", cancel, resume);
    }

    private ConflictDecision toConflictDecision(ResultButtonName buttonName, boolean applyToAll) {
        if (buttonName == ConflictDialogButtons.OVERWRITE) {
            return new ConflictDecision(ConflictResolution.OVERWRITE, applyToAll);
        }
        if (buttonName == ConflictDialogButtons.RENAME) {
            return new ConflictDecision(ConflictResolution.RENAME, applyToAll);
        }
        return new ConflictDecision(ConflictResolution.SKIP, applyToAll);
    }
}
