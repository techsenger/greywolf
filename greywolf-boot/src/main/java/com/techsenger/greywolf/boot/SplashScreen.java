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

package com.techsenger.greywolf.boot;

import com.techsenger.fontjars.inter.Inter;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 *
 * @author Pavel Castornii
 */
final class SplashScreen {

    private static final long MIN_DISPLAY_MS = 4000;

    private static final int WIDTH = 500;

    private static final int HEIGHT = 300;

    private static final int MARGIN = 32;

    private static final int BAR_Y = 250;

    private static final int BAR_HEIGHT = 10;

    private static final Color BG = new Color(0x1C1C1E);

    private static final Color BORDER = new Color(0x3A3A3C);

    private static final Color BRAND_FG = new Color(0xEEEEEE);

    private static final Color TITLE_FG = new Color(0xFFFFFF);

    private static final Color VERSION_FG = new Color(0xEEEEEE);

    private static final Color STATUS_FG = new Color(0x8D8D93);

    private static final Color BAR_BG = new Color(0x2C2C2E);

    private static final Color BAR_FG = new Color(0x0055B3);

    private static Font loadFont(Inter inter, float size) throws IOException, FontFormatException {
        try (var stream = inter.getUrl().openStream()) {
            return Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(size);
        }
    }

    private final long createdAt = System.currentTimeMillis();

    // There is no point in using a modal dialog here because we have two systems — AWT and JavaFX.
    // And even if the AWT window is modal, JavaFX window is still accessible.
    private final Frame frame;

    private final Canvas canvas;

    private final Font brandFont;

    private final Font titleFont;

    private final Font versionFont;

    private final Font statusFont;

    private final String versionText;

    private double progress = 0.0;

    private String statusText = "";

    private final BufferedImage buffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);

    SplashScreen() throws IOException, FontFormatException {
        // AWT and JavaFX keep separate font registries, so the splash screen loads its own fonts.
        brandFont = loadFont(Inter.REGULAR, 22f);
        titleFont = loadFont(Inter.REGULAR, 40f);
        versionFont = loadFont(Inter.REGULAR, 14f);
        statusFont = loadFont(Inter.REGULAR, 13f);
        versionText = ProjectInfo.getVersion() != null ? ProjectInfo.getVersion().toString() : "";
        frame = new Frame();
        frame.setUndecorated(true);
        frame.setAlwaysOnTop(true);
        frame.setSize(WIDTH, HEIGHT);
        frame.setLocationRelativeTo(null);

        // Double buffering: render everything onto an off-screen image first,
        // then draw it to the screen in a single operation to prevent flickering.
        canvas = new Canvas() {

            @Override
            public void paint(Graphics g) {
                Graphics2D g2d = buffer.createGraphics();
                render(g2d);
                g2d.dispose();
                g.drawImage(buffer, 0, 0, null);
            }

            @Override
            public void update(Graphics g) {
                paint(g);
            }
        };

        frame.add(canvas);
        frame.setVisible(true);
    }

    void update(double progress) {
        EventQueue.invokeLater(() -> {
            this.progress = Math.min(1.0, Math.max(0.0, progress));
            canvas.repaint();
        });
    }

    void update(String status) {
        EventQueue.invokeLater(() -> {
            this.statusText = status != null ? status : "";
            canvas.repaint();
        });
    }

    void close() {
        long elapsed = System.currentTimeMillis() - createdAt;
        long remaining = MIN_DISPLAY_MS - elapsed;
        if (remaining > 0) {
            try {
                Thread.sleep(remaining);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        EventQueue.invokeLater(() -> {
            frame.setVisible(false);
            frame.dispose();
        });
    }

    private void render(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(BG);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(BORDER);
        g.drawRect(0, 0, WIDTH - 1, HEIGHT - 1);
        renderBrand(g);
        renderTitle(g);
        renderVersion(g);
        renderProgressBar(g);
        renderStatus(g);
    }

    private void renderBrand(Graphics2D g) {
        g.setColor(BRAND_FG);
        g.setFont(brandFont);
        g.drawString("Techsenger", MARGIN - 2, 40);
    }

    private void renderTitle(Graphics2D g) {
        g.setColor(TITLE_FG);
        g.setFont(titleFont);
        drawCentered(g, "GreyWolf", 140);
    }

    private void renderVersion(Graphics2D g) {
        g.setColor(VERSION_FG);
        g.setFont(versionFont);
        drawCentered(g, versionText, 170);
    }

    private void drawCentered(Graphics2D g, String text, int baseline) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, (WIDTH - fm.stringWidth(text)) / 2, baseline);
    }

    private void renderProgressBar(Graphics2D g) {
        int barW = WIDTH - 2 * MARGIN;
        int arc = BAR_HEIGHT;
        g.setColor(BAR_BG);
        g.fillRoundRect(MARGIN, BAR_Y, barW, BAR_HEIGHT, arc, arc);
        int filled = (int) (barW * progress);
        if (filled > 0) {
            g.setColor(BAR_FG);
            g.fillRoundRect(MARGIN, BAR_Y, filled, BAR_HEIGHT, arc, arc);
        }
    }

    private void renderStatus(Graphics2D g) {
        g.setFont(statusFont);
        g.setColor(STATUS_FG);
        g.drawString(statusText, MARGIN, BAR_Y + BAR_HEIGHT + 22);
    }
}
