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

package com.techsenger.greywolf.core.internal.about;

import com.techsenger.greywolf.core.project.ProjectInfo;
import com.techsenger.greywolf.core.style.CoreIcons;
import com.techsenger.shellfx.core.close.CloseCheckResult;
import com.techsenger.shellfx.core.close.ClosePreparationResult;
import com.techsenger.shellfx.core.dialog.AbstractDialogViewModel;
import com.techsenger.shellfx.core.window.WindowComposer;
import java.util.function.Consumer;
import javafx.application.HostServices;

/**
 *
 * @author Pavel Castornii
 */
public final class AboutDialogViewModel<C extends WindowComposer> extends AbstractDialogViewModel<C> {

    private static final String CONTRIBUTE_URL = "https://github.com/techsenger/greywolf#contributing";

    private static final String DONATE_URL = "https://github.com/sponsors/techsenger";

    private final HostServices hostServices;

    private final String version;

    public AboutDialogViewModel(AboutDialogParams params) {
        super(params);
        this.hostServices = params.getHostServices();
        var projectVersion = ProjectInfo.getVersion();
        this.version = projectVersion.toString();
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Returns the version of the application, read from the build, so it never has to be edited by hand.
     *
     * @return the version, or {@code unknown} if the build information is unavailable
     */
    public String getVersion() {
        return version;
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setTitle("About Techsenger GreyWolf");
        setIcon(CoreIcons.ABOUT);
        setResizable(false);
        setLeftButtons(AboutDialogButtons.CONTRIBUTE);
        setRightButtons(AboutDialogButtons.DONATE);
        setOnResult(name -> {
            if (name == AboutDialogButtons.CONTRIBUTE) {
                onOpenUrl(CONTRIBUTE_URL);
            } else if (name == AboutDialogButtons.DONATE) {
                onOpenUrl(DONATE_URL);
            }
        });
    }

    /**
     * Opens the address in the browser of the user.
     *
     * @param url the address to open
     */
    void onOpenUrl(String url) {
        hostServices.showDocument(url);
    }
}
