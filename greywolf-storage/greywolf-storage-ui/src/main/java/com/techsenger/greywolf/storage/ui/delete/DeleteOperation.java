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

package com.techsenger.greywolf.storage.ui.delete;

import com.techsenger.annotations.Nullable;
import com.techsenger.greywolf.storage.base.GreyWolfFile;
import com.techsenger.greywolf.storage.base.delete.DeleteListener;
import com.techsenger.greywolf.storage.base.delete.DeletePlan;
import com.techsenger.greywolf.storage.base.delete.DeleteProgress;
import com.techsenger.greywolf.storage.base.delete.DeleteScanner;
import com.techsenger.greywolf.storage.base.delete.Deleter;
import com.techsenger.greywolf.storage.base.delete.FailedDeletion;
import com.techsenger.greywolf.storage.base.operation.ErrorResolution;
import com.techsenger.greywolf.storage.base.operation.ScanListener;
import com.techsenger.greywolf.storage.ui.operation.DialogParamsFactory;
import com.techsenger.greywolf.storage.ui.operation.DialogTimer;
import com.techsenger.greywolf.storage.ui.operation.OperationDialogs;
import com.techsenger.greywolf.storage.ui.operation.OperationTimings;
import com.techsenger.shellfx.core.config.ConfigManager;
import com.techsenger.shellfx.core.settings.ShellSettings;
import com.techsenger.shellfx.core.window.WindowType;
import com.techsenger.shellfx.dialogs.alert.AlertDialogType;
import com.techsenger.shellfx.dialogs.progress.ProgressDialogButtons;
import com.techsenger.shellfx.dialogs.progress.ProgressDialogPort;
import com.techsenger.shellfx.material.UiExecutor;
import com.techsenger.toolkit.core.file.FileUtils;
import java.util.List;
import java.util.Objects;

/**
 * Runs one deletion end to end - the confirmation, then scanning, then the actual deleting - by opening each
 * dialog through the {@link DeleteComposer} given to it once at construction, so it keeps working across the whole
 * run regardless of which of those dialogs is currently open or already closed. Owns both the
 * {@link DeleteScanner} and the {@link Deleter} directly, rather than leaving them to a dialog view model, so the
 * run survives independently of any one dialog's lifecycle - each dialog is purely a progress view, interacted
 * with only through its port. The scan phase has nothing dialog-specific to show, so it reuses the generic
 * progress dialog.
 *
 * <p>Workflow (each line is one branch; "end" means the run stops and nothing else opens):
 * <pre>{@code
 * Confirmation alert
 *   No                            -> end
 *   Yes                           -> ScanDialog
 *
 * ScanDialog (appears only if scanning outlasts the show delay)
 *   scan completed                -> DeletingDialog
 *   scan failed                   -> Error alert -> end
 *   scan cancelled                -> end
 *   Cancel clicked                -> Confirm alert (scan paused meanwhile)
 *     No                          -> scan resumes
 *     Yes                         -> scan cancelled -> end
 *
 * DeletingDialog
 *   entry failed                  -> OperationErrorDialog
 *     Skip / Skip all / Retry     -> continues
 *     Abort                       -> aborted (see below)
 *   Cancel clicked                -> Confirm alert (deleting paused meanwhile)
 *     No                          -> deleting resumes
 *     Yes                         -> cancelled (see below)
 *   all entries processed, no failures -> completed
 *   all entries processed, failures    -> completed, then Warning alert with deleted/skipped/failed counts
 *   cancelled                     -> DeletingDialog closes -> end
 *   aborted                       -> DeletingDialog closes -> Error alert with the reason -> end
 * }</pre>
 * The finished callback fires once the DeletingDialog has closed after completion, cancellation or abort -
 * never when the run ends earlier (confirmation declined, scan failed or cancelled).
 *
 * @author Pavel Castornii
 */
public final class DeleteOperation {

    /**
     * The stage of the run; only moves forward, and {@link #FINISHED} is reached from any stage that ends the run.
     */
    private enum Phase {
        NOT_STARTED,
        CONFIRMING,
        SCANNING,
        DELETING,
        FINISHED
    }

    private final class ScanHandler implements ScanListener<DeletePlan> {

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
        public void onScanCompleted(DeletePlan plan) {
            UiExecutor.execute(() -> timer.finish(() -> {
                closeDialog();
                openDeletingDialog(plan);
            }));
        }

        @Override
        public void onScanFailed(Exception ex) {
            UiExecutor.execute(() -> timer.finish(() -> {
                closeDialog();
                phase = Phase.FINISHED;
                dialogs.showAlert(AlertDialogType.ERROR, "Couldn't scan the files to delete: " + ex.getMessage());
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

    private final class DeleteHandler implements DeleteListener {

        private final DeletingDialogPort dialog;

        DeleteHandler(DeletingDialogPort dialog) {
            this.dialog = dialog;
        }

        @Override
        public void onProgress(DeleteProgress progress) {
            UiExecutor.execute(() -> dialog.setProgress(progress));
        }

        @Override
        public ErrorResolution onError(FailedDeletion failure) {
            return dialogs.askErrorResolution(failure.message(), failure.file().getUri().toString());
        }

        @Override
        public void onCompleted(int deleted, int skipped, List<FailedDeletion> failures) {
            UiExecutor.execute(() -> finishDeletion(() -> {
                if (!failures.isEmpty()) {
                    dialogs.showAlert(AlertDialogType.WARNING,
                            deleted + " deleted, " + skipped + " skipped, " + failures.size() + " failed.");
                }
            }));
        }

        @Override
        public void onCancelled() {
            UiExecutor.execute(() -> finishDeletion(() -> { }));
        }

        @Override
        public void onAborted(String reason) {
            UiExecutor.execute(() -> finishDeletion(
                    () -> dialogs.showAlert(AlertDialogType.ERROR, "Delete aborted: " + reason)));
        }

        /**
         * Waits the minimum visible time, then closes the delete dialog, reports {@code onFinished}, and finally
         * runs {@code afterClose} - so a summary/abort alert opens only once the delete dialog itself is actually
         * gone, not layered on top of it.
         */
        private void finishDeletion(Runnable afterClose) {
            DialogTimer.runAfter(OperationTimings.RUN_MIN_VISIBLE_MILLIS, () -> {
                dialog.closeSafely();
                Objects.requireNonNull(onFinished).run();
                phase = Phase.FINISHED;
                afterClose.run();
            });
        }
    }

    private final DeleteComposer composer;

    private final ConfigManager configManager;

    private final ShellSettings settings;

    private final OperationDialogs dialogs;

    private final DeleteScanner scanner = new DeleteScanner();

    private final Deleter deleter = new Deleter();

    private @Nullable List<? extends GreyWolfFile> sources;

    private @Nullable Runnable onFinished;

    private Phase phase = Phase.NOT_STARTED;

    public DeleteOperation(DeleteComposer composer, ConfigManager configManager, ShellSettings settings) {
        this.composer = composer;
        this.configManager = configManager;
        this.settings = settings;
        this.dialogs = new OperationDialogs(composer, configManager, settings);
    }

    public void setSources(List<? extends GreyWolfFile> sources) {
        this.sources = sources;
    }

    /**
     * Sets the callback run once the delete dialog has closed after the run completed, was cancelled or aborted -
     * i.e. whenever something may have been deleted.
     *
     * @param onFinished the callback to run.
     */
    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    /**
     * Asks the user to confirm the deletion, the first step of this run.
     */
    public void start() {
        if (phase != Phase.NOT_STARTED) {
            throw new IllegalStateException("Already started");
        }
        phase = Phase.CONFIRMING;
        dialogs.confirm(createConfirmationMessage(Objects.requireNonNull(sources)), this::openScanDialog,
                this::onConfirmationDeclined);
    }

    private void onConfirmationDeclined() {
        phase = Phase.FINISHED;
    }

    private String createConfirmationMessage(List<? extends GreyWolfFile> files) {
        var hasFolder = files.stream().anyMatch(GreyWolfFile::isDirectory);
        if (files.size() == 1) {
            var name = files.get(0).getName();
            return hasFolder ? "Delete '" + name + "' and everything inside it?" : "Delete '" + name + "'?";
        }
        return "Delete " + files.size() + " selected items" + (hasFolder ? " and everything inside the folders" : "")
                + "?";
    }

    /**
     * Starts scanning right away, but only shows the scan dialog - and so only actually blocks input on it -
     * once the scan show delay has passed and scanning is still running; a scan that finishes before then never
     * shows anything.
     */
    private void openScanDialog() {
        phase = Phase.SCANNING;
        var params = DialogParamsFactory.createProgressParams(configManager, WindowType.NESTED, settings);
        var deferredDialog = composer.createProgressDialog(params);
        ProgressDialogPort dialog = deferredDialog.port();
        dialog.setTitle("Scanning");
        dialog.setRightButtons(ProgressDialogButtons.CANCEL);
        dialog.setOnResult((name) -> {
            if (name == ProgressDialogButtons.CANCEL) {
                confirmCancel("Cancel the delete operation?", scanner::pause, scanner::cancel, scanner::resume);
            }
        });

        var timer = new DialogTimer(OperationTimings.SCAN_SHOW_DELAY_MILLIS,
                OperationTimings.SCAN_MIN_VISIBLE_MILLIS, deferredDialog::show);
        timer.start();

        scanner.scan(Objects.requireNonNull(sources), new ScanHandler(dialog, timer));
    }

    private void openDeletingDialog(DeletePlan plan) {
        phase = Phase.DELETING;
        var config = configManager.getOrCreateConfig(DeletingDialogConfig.class, DeletingDialogConfig::new);
        DeletingDialogPort dialog =
                composer.openDeletingDialog(new DeletingDialogParams(config, settings));
        dialog.setOnResult((name) -> {
            if (name == DeletingDialogButtons.CANCEL) {
                confirmCancel("Cancel the delete operation?", deleter::pause, deleter::cancel, deleter::resume);
            }
        });
        dialog.setVisible(true);
        deleter.start(plan, new DeleteHandler(dialog));
    }

    /**
     * Pauses the in-progress run (scan or deleting) immediately via {@code pause} - before the confirmation
     * dialog even opens - so it doesn't keep going while the user is still deciding; {@code resume} runs on No,
     * {@code cancel} runs (for good) on Yes.
     */
    private void confirmCancel(String message, Runnable pause, Runnable cancel, Runnable resume) {
        pause.run();
        dialogs.confirm(message, cancel, resume);
    }
}
