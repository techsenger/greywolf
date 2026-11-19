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

package com.techsenger.greywolf.storage.ui.operation;

import com.techsenger.greywolf.storage.base.operation.ErrorResolution;
import com.techsenger.shellfx.core.config.ConfigManager;
import com.techsenger.shellfx.core.settings.ShellSettings;
import com.techsenger.shellfx.core.window.WindowType;
import com.techsenger.shellfx.dialogs.alert.AlertDialogButtons;
import com.techsenger.shellfx.dialogs.alert.AlertDialogParams;
import com.techsenger.shellfx.dialogs.alert.AlertDialogType;
import com.techsenger.shellfx.material.UiExecutor;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * The small dialogs every file operation opens on its own behalf - an alert, a yes/no confirmation and the
 * "how to proceed after a failure" question - all nested in the window of the composer they are opened through.
 *
 * @author Pavel Castornii
 */
public final class OperationDialogs {

    /**
     * Runs {@code uiAction} on the JavaFX application thread and blocks the calling thread until the action
     * completes the future it is given, returning that value. Must never be called from the application thread
     * itself.
     *
     * @param uiAction opens a dialog and completes the future from the dialog's result handler.
     * @param <T> the type of the answer.
     * @return the answer the user gave.
     */
    public static <T> T blockingAsk(Consumer<CompletableFuture<T>> uiAction) {
        var future = new CompletableFuture<T>();
        UiExecutor.execute(() -> uiAction.accept(future));
        try {
            return future.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        } catch (ExecutionException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private final OperationComposer composer;

    private final ConfigManager configManager;

    private final ShellSettings settings;

    public OperationDialogs(OperationComposer composer, ConfigManager configManager, ShellSettings settings) {
        this.composer = composer;
        this.configManager = configManager;
        this.settings = settings;
    }

    /**
     * Shows an alert with a single message.
     *
     * @param type the severity of the alert.
     * @param message the text to show.
     */
    public void showAlert(AlertDialogType type, String message) {
        var params = new AlertDialogParams(WindowType.NESTED, settings, type);
        var dialog = composer.openAlertDialog(params);
        dialog.setMessage(message);
        dialog.setVisible(true);
    }

    /**
     * Asks a yes/no question; exactly one of the callbacks runs, after the dialog has been answered.
     *
     * @param message the question.
     * @param onYes runs when the user confirms.
     * @param onNo runs when the user declines.
     */
    public void confirm(String message, Runnable onYes, Runnable onNo) {
        var params = new AlertDialogParams(WindowType.NESTED, settings, AlertDialogType.CONFIRMATION);
        var dialog = composer.openAlertDialog(params);
        dialog.setMessage(message);
        dialog.setOnResult((name) -> {
            if (name == AlertDialogButtons.YES) {
                onYes.run();
            } else {
                onNo.run();
            }
            dialog.closeSafely();
        });
        dialog.setVisible(true);
    }

    /**
     * Asks how to proceed after a failed task and blocks the calling thread until the user answers, so it must
     * never be called from the JavaFX application thread.
     *
     * @param message what went wrong.
     * @param filePath the path of the entry the task failed on.
     * @return the user's decision.
     */
    public ErrorResolution askErrorResolution(String message, String filePath) {
        return blockingAsk(future -> {
            var config = configManager.getOrCreateConfig(OperationErrorDialogConfig.class,
                    OperationErrorDialogConfig::new);
            var dialog = composer.openOperationErrorDialog(new OperationErrorDialogParams(config, settings));
            dialog.setMessage(message);
            dialog.setFilePath(filePath);
            dialog.setOnResult((buttonName) -> {
                var resolution = buttonName instanceof ErrorResultButtonName errorButton
                        ? errorButton.getErrorResolution() : ErrorResolution.SKIP;
                dialog.closeSafely();
                future.complete(resolution);
            });
            dialog.setVisible(true);
        });
    }
}
