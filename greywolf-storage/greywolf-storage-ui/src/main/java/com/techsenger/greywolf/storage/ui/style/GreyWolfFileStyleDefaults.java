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

import com.techsenger.shellfx.material.theme.AtlantaFxTheme;
import com.techsenger.shellfx.material.theme.Theme;
import com.techsenger.shellfx.storage.ui.style.FileStyleDefaults;
import static com.techsenger.shellfx.storage.ui.style.FileStyleDefaults.style;
import com.techsenger.shellfx.storage.ui.style.FileStyleSettings;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * The default styles of the files for every theme. All colors of a theme follow these rules:
 *
 * <ol>
 * <li>Every color is calculated from the colors of the palette of the theme, as listed in the file
 * {@code palettes.html} of the shellfx-material module: the hue comes from the palette colors, the lightness and the
 * saturation are then adjusted by the rules below.</li>
 * <li>The saturation (HSL) is at most 70%, so the icons do not strain the eyes. The colors of the states
 * (the text of marked and the icon of error) have the saturation from 55% to 70%, so they never become dull;
 * marked on light themes is the exception. The directory color on light themes is not limited by the saturation,
 * otherwise a dark orange turns into brown; on dark themes it follows the common limit.</li>
 * <li>The WCAG contrast ratio against the default background of the theme is at least 4.5:1 for every icon and text
 * color.</li>
 * <li>CIEDE2000 (&Delta;E<sub>00</sub>) is at least 15 between all colors. The folder color is not compared with the
 * colors of the file types. Between the grays (the main color, other and text file) it may go down to 10-12, because
 * they have to fit into a narrow range of lightness. The palettes of the Nord themes are tight, so there it
 * goes down to 14.6 (Nord Dark: audio and other, Nord Light: text file and marked).</li>
 * <li>The colors of the states (the text of marked and the icon of error) are based on the colors of the palette.
 * On light themes marked is the lightest accent color that gives the contrast of 4.5:1 (accent-6 in Cupertino Light,
 * accent-5 in Primer Light), error is the hue of danger-7 with the saturation of 70% and the lightness of
 * 38-46%, so it is a vivid red and not a dark brown. On dark themes marked is accent-5 and error is danger-5.
 * Marked on these light themes is not shifted, because a darker blue is hard to tell from the main text color. The
 * accent colors of Nord Light are dull, so marked there is based on accent-5 with the saturation raised to 60% and
 * the lightness lowered only as far as the contrast of 4.5:1 needs. The other colors may be shifted to avoid
 * conflicts with the rest of the palette, but they stay close to their palette color: the lightness differs from it
 * by at most 10 percentage points (HSL), and if the palette color does not give the contrast of 4.5:1, it is
 * shifted only as far as the contrast needs.</li>
 * <li>A color of a state is laid over the color of the file by its alpha, channel by channel: the result is
 * (1 - alpha) * file + alpha * color. Marked colors the text with the alpha of 1, and hidden dims the icon and the
 * text by laying the background color of the theme over them at the alpha of 0.3. A marked file is not dimmed.</li>
 * <li>Marked is the only state with both a text color and an icon color, and they differ: its icon color is the text
 * color darkened, so that the icon gets darker than the file and not just duller.
 * The darkening is like laying black over the icon at the alpha d (0.4 on dark themes, 0.2 on light ones) and then
 * the text color at the alpha of 0.3. Black has zero channels, so this is the same as laying one color at the alpha
 * a = 1 - (1 - d) * 0.7 (0.58 and 0.44) whose channels are those of the text color multiplied by 0.3 / a (about
 * 0.52 and 0.68). To choose the icon color of marked, take the text color and apply this formula.</li>
 * <li>A color keeps its meaning: its hue shifts by at most 15 degrees from the target, and its lightness is limited
 * (about HSL 45-85% on dark themes and 20-55% on light themes), so it is never almost white or almost black.</li>
 * </ol>
 *
 * <p>The colors of the icons:
 *
 * <ul>
 * <li>directory: orange, based on chart-2 of the palette;</li>
 * <li>other: dark gray;</li>
 * <li>text file: light gray;</li>
 * <li>archive: teal;</li>
 * <li>code: cyan;</li>
 * <li>document: yellow;</li>
 * <li>image: magenta or purple, calmer than video: its HSL saturation is lower and its CIELAB chroma is lower by
 * at least 12; on light themes it is not too dark (HSL lightness at least 40%);</li>
 * <li>video: bright magenta, the most saturated of the pink-purple icons: the highest chroma among them;</li>
 * <li>audio: indigo or blue, based on chart-5 of the palette, so it differs in hue from the pink-purple image;</li>
 * <li>application: pale red, always paler than the error color: lighter and less saturated.</li>
 * </ul>
 *
 * <p>A file with an error (a broken link, a file of an unavailable storage) has the error color on its icon only, its
 * text keeps the color of the file.
 *
 * <p>An executable file keeps the colors of its kind and only its text is bold.
 *
 * <p>The other kinds of files (font, unknown file) are not colored by the palette and use the main color of
 * the theme.
 *
 * @author Pavel Castornii
 */
public final class GreyWolfFileStyleDefaults {

    private static final Map<Theme, Consumer<GreyWolfFileStyleScheme>> FILLERS_BY_THEME = Map.of(
            AtlantaFxTheme.CUPERTINO_LIGHT, GreyWolfFileStyleDefaults::cupertinoLight,
            AtlantaFxTheme.CUPERTINO_DARK, GreyWolfFileStyleDefaults::cupertinoDark,
            AtlantaFxTheme.PRIMER_LIGHT, GreyWolfFileStyleDefaults::primerLight,
            AtlantaFxTheme.PRIMER_DARK, GreyWolfFileStyleDefaults::primerDark,
            AtlantaFxTheme.NORD_LIGHT, GreyWolfFileStyleDefaults::nordLight,
            AtlantaFxTheme.NORD_DARK, GreyWolfFileStyleDefaults::nordDark,
            AtlantaFxTheme.DRACULA, GreyWolfFileStyleDefaults::dracula);

    private static final Map<Theme, GreyWolfFileStyleScheme> SCHEMES_BY_THEME = new ConcurrentHashMap<>();

    /**
     * Returns the default scheme of a theme: the styles of shellfx plus the styles of GreyWolf. The scheme is
     * shared, so the caller must not change it.
     *
     * @param theme the theme
     * @return the scheme of the theme
     * @throws IllegalArgumentException if there are no default styles for the theme
     */
    public static GreyWolfFileStyleScheme getScheme(Theme theme) {
        // the palette of a theme is created on first use, so a scheme is created when it is asked for
        return SCHEMES_BY_THEME.computeIfAbsent(theme, key -> {
            var filler = FILLERS_BY_THEME.get(key);
            if (filler == null) {
                throw new IllegalArgumentException("No file styles for theme " + key);
            }
            var scheme = new GreyWolfFileStyleScheme();
            FileStyleDefaults.fill(scheme, key);
            filler.accept(scheme);
            // an executable file differs from the others only by its bold text
            var executable = new FileStyleSettings();
            executable.getText().setBold(true);
            scheme.setExecutable(executable);
            // a file of an unavailable storage keeps the error glyph and shows it in the error color
            var unavailable = new FileStyleSettings();
            unavailable.getIcon().setColor(scheme.getError().getIcon().getColor());
            scheme.setUnavailable(unavailable);
            return scheme;
        });
    }

    private static void cupertinoLight(GreyWolfFileStyleScheme scheme) {
        scheme.setArchiveFile(style(0x1E8476FF, null));
        scheme.setCodeFile(style(0x105056FF, null));
        scheme.setTextFile(style(0x76767AFF, null));
        scheme.setDocumentFile(style(0x91681CFF, null));
        scheme.setImageFile(style(0x7F4195FF, null));
        scheme.setAudioFile(style(0x1B1A93FF, null));
        scheme.setVideoFile(style(0xD72998FF, null));
        scheme.setApplicationFile(style(0xAD6063FF, null));
        scheme.setMarked(style(0x00479470, 0x0068D9FF));
    }

    private static void cupertinoDark(GreyWolfFileStyleScheme scheme) {
        scheme.setArchiveFile(style(0x94ECC5FF, null));
        scheme.setCodeFile(style(0x3EA8A6FF, null));
        scheme.setTextFile(style(0xD1D1D6FF, null));
        scheme.setDocumentFile(style(0xD8B43CFF, null));
        scheme.setImageFile(style(0xE6AFE7FF, null));
        scheme.setAudioFile(style(0x7F76E6FF, null));
        scheme.setVideoFile(style(0xDC3EC9FF, null));
        scheme.setApplicationFile(style(0xE1A698FF, null));
        scheme.setMarked(style(0x154E7094, 0x2897D8FF));
    }

    private static void primerLight(GreyWolfFileStyleScheme scheme) {
        scheme.setArchiveFile(style(0x1E8559FF, null));
        scheme.setCodeFile(style(0x2D8287FF, null));
        scheme.setTextFile(style(0x6E7781FF, null));
        scheme.setDocumentFile(style(0x8B6C34FF, null));
        scheme.setImageFile(style(0x77428AFF, null));
        scheme.setAudioFile(style(0x1B1A93FF, null));
        scheme.setVideoFile(style(0xD526A4FF, null));
        scheme.setApplicationFile(style(0xB15D6AFF, null));
        scheme.setMarked(style(0x06489570, 0x0969DAFF));
    }

    private static void primerDark(GreyWolfFileStyleScheme scheme) {
        scheme.setArchiveFile(style(0x51E0A1FF, null));
        scheme.setCodeFile(style(0x2DCAD9FF, null));
        scheme.setTextFile(style(0x949DA7FF, null));
        scheme.setDocumentFile(style(0xE9DC8CFF, null));
        scheme.setImageFile(style(0xD485C2FF, null));
        scheme.setAudioFile(style(0xB7B1EDFF, null));
        scheme.setVideoFile(style(0xD828C4FF, null));
        scheme.setApplicationFile(style(0xE4AEA4FF, null));
        scheme.setMarked(style(0x17417094, 0x2D7DD9FF));
    }

    private static void nordLight(GreyWolfFileStyleScheme scheme) {
        scheme.setArchiveFile(style(0x1E816FFF, null));
        scheme.setCodeFile(style(0x1F7B95FF, null));
        scheme.setTextFile(style(0x6A7485FF, null));
        scheme.setDocumentFile(style(0x826831FF, null));
        scheme.setImageFile(style(0x7F309CFF, null));
        scheme.setAudioFile(style(0x1A1A93FF, null));
        scheme.setVideoFile(style(0xCD25AAFF, null));
        scheme.setApplicationFile(style(0xB05A5CFF, null));
        scheme.setMarked(style(0x224F8870, 0x3274C7FF));
    }

    private static void nordDark(GreyWolfFileStyleScheme scheme) {
        scheme.setArchiveFile(style(0x28D889FF, null));
        scheme.setCodeFile(style(0x67E0E4FF, null));
        scheme.setTextFile(style(0xBFC6D2FF, null));
        scheme.setDocumentFile(style(0xC0AB25FF, null));
        scheme.setImageFile(style(0xEDC2EBFF, null));
        scheme.setAudioFile(style(0xB3ADF0FF, null));
        scheme.setVideoFile(style(0xE571D3FF, null));
        scheme.setApplicationFile(style(0xE8B4B2FF, null));
        scheme.setMarked(style(0x2D537494, 0x57A1E1FF));
    }

    private static void dracula(GreyWolfFileStyleScheme scheme) {
        scheme.setArchiveFile(style(0x44A279FF, null));
        scheme.setCodeFile(style(0x39C9DBFF, null));
        scheme.setTextFile(style(0xC0C0C4FF, null));
        scheme.setDocumentFile(style(0xD7BB26FF, null));
        scheme.setImageFile(style(0xEEC4EDFF, null));
        scheme.setAudioFile(style(0x5B92E1FF, null));
        scheme.setVideoFile(style(0xE362B5FF, null));
        scheme.setApplicationFile(style(0xE0AC97FF, null));
        scheme.setMarked(style(0x594C7994, 0xAC92EAFF));
    }

    private GreyWolfFileStyleDefaults() {
        // Utility class.
    }
}
