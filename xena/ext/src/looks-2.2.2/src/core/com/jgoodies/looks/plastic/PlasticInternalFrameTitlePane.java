/*
 * Copyright (c) 2001-2009 JGoodies Karsten Lentzsch. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of JGoodies Karsten Lentzsch nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jgoodies.looks.plastic;

import java.awt.*;

import javax.swing.Icon;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalInternalFrameTitlePane;

/**
 * Paints the internal frame title. Uses JGoodies Plastic colors.
 *
 * @author  Karsten Lentzsch
 * @version $Revision$
 */
public final class PlasticInternalFrameTitlePane
    extends MetalInternalFrameTitlePane {

    private PlasticBumps paletteBumps;

    private final PlasticBumps activeBumps =
        new PlasticBumps(
            0,
            0,
            PlasticLookAndFeel.getPrimaryControlHighlight(),
            PlasticLookAndFeel.getPrimaryControlDarkShadow(),
            PlasticLookAndFeel.getPrimaryControl());

    private final PlasticBumps inactiveBumps =
        new PlasticBumps(
            0,
            0,
            PlasticLookAndFeel.getControlHighlight(),
            PlasticLookAndFeel.getControlDarkShadow(),
            PlasticLookAndFeel.getControl());

    public PlasticInternalFrameTitlePane(JInternalFrame frame) {
        super(frame);
    }

    public void paintPalette(Graphics g) {
        boolean leftToRight = PlasticUtils.isLeftToRight(frame);

        int width = getWidth();
        int height = getHeight();

        if (paletteBumps == null) {
            paletteBumps =
                new PlasticBumps(
                    0,
                    0,
                    PlasticLookAndFeel.getPrimaryControlHighlight(),
                    PlasticLookAndFeel.getPrimaryControlInfo(),
                    PlasticLookAndFeel.getPrimaryControlShadow());
        }

        Color background = PlasticLookAndFeel.getPrimaryControlShadow();
        Color darkShadow = PlasticLookAndFeel.getControlDarkShadow();

        g.setColor(background);
        g.fillRect(0, 0, width, height);

        g.setColor(darkShadow);
        g.drawLine(0, height - 1, width, height - 1);

        int buttonsWidth = getButtonsWidth();
        int xOffset = leftToRight ? 4 : buttonsWidth + 4;
        int bumpLength = width - buttonsWidth - 2 * 4;
        int bumpHeight = getHeight() - 4;
        paletteBumps.setBumpArea(bumpLength, bumpHeight);
        paletteBumps.paintIcon(this, g, xOffset, 2);
    }

    public void paintComponent(Graphics g) {
        if (isPalette) {
            paintPalette(g);
            return;
        }

        boolean leftToRight = PlasticUtils.isLeftToRight(frame);
        boolean isSelected = frame.isSelected();

        int width = getWidth();
        int height = getHeight();

        Color background = null;
        Color foreground = null;
        Color shadow = null;

        PlasticBumps bumps;

        if (isSelected) {
            background = PlasticLookAndFeel.getWindowTitleBackground();
            foreground = PlasticLookAndFeel.getWindowTitleForeground();
            bumps = activeBumps;
        } else {
            background = PlasticLookAndFeel.getWindowTitleInactiveBackground();
            foreground = PlasticLookAndFeel.getWindowTitleInactiveForeground();
            bumps = inactiveBumps;
        }
        // JGoodies: darkShadow is always controlDarkShadow
        shadow = PlasticLookAndFeel.getControlDarkShadow();

        /*

                if (isSelected) {
                    if (selectedBackgroundKey != null) {
                        background = UIManager.getColor(selectedBackgroundKey);
                    }
                    if (background == null) {
                        background = PlasticLookAndFeel.getWindowTitleBackground();
                    }
                    if (selectedForegroundKey != null) {
                        foreground = UIManager.getColor(selectedForegroundKey);
                    }
                    if (selectedShadowKey != null) {
                        shadow = UIManager.getColor(selectedShadowKey);
                    }
                    if (shadow == null) {
                        shadow = PlasticLookAndFeel.getPrimaryControlDarkShadow();
                    }
                    if (foreground == null) {
                        foreground = PlasticLookAndFeel.getWindowTitleForeground();
                    }
                    activeBumps.setBumpColors(activeBumpsHighlight, activeBumpsShadow,
                                              background);
                    bumps = activeBumps;
                } else {
                    background = PlasticLookAndFeel.getWindowTitleInactiveBackground();
                    foreground = PlasticLookAndFeel.getWindowTitleInactiveForeground();
                    shadow = PlasticLookAndFeel.getControlDarkShadow();
                    bumps = inactiveBumps;
                }
        */
        g.setColor(background);
        g.fillRect(0, 0, width, height);

        g.setColor(shadow);
        g.drawLine(0, height - 1, width, height - 1);
        g.drawLine(0, 0, 0, 0);
        g.drawLine(width - 1, 0, width - 1, 0);

        int titleLength = 0;
        int xOffset = leftToRight ? 5 : width - 5;
        String frameTitle = frame.getTitle();

        Icon icon = frame.getFrameIcon();
        if (icon != null) {
            if (!leftToRight)
                xOffset -= icon.getIconWidth();
            int iconY = ((height / 2) - (icon.getIconHeight() / 2));
            icon.paintIcon(frame, g, xOffset, iconY);
            xOffset += leftToRight ? icon.getIconWidth() + 5 : -5;
        }

        if (frameTitle != null) {
            Font f = getFont();
            g.setFont(f);
            FontMetrics fm = g.getFontMetrics();
            //int fHeight = fm.getHeight();

            g.setColor(foreground);

            int yOffset = ((height - fm.getHeight()) / 2) + fm.getAscent();

            Rectangle rect = new Rectangle(0, 0, 0, 0);
            if (frame.isIconifiable()) {
                rect = iconButton.getBounds();
            } else if (frame.isMaximizable()) {
                rect = maxButton.getBounds();
            } else if (frame.isClosable()) {
                rect = closeButton.getBounds();
            }
            int titleW;

            if (leftToRight) {
                if (rect.x == 0) {
                    rect.x = frame.getWidth() - frame.getInsets().right - 2;
                }
                titleW = rect.x - xOffset - 4;
                frameTitle = getTitle(frameTitle, fm, titleW);
            } else {
                titleW = xOffset - rect.x - rect.width - 4;
                frameTitle = getTitle(frameTitle, fm, titleW);
                xOffset -= SwingUtilities.computeStringWidth(fm, frameTitle);
            }

            titleLength = SwingUtilities.computeStringWidth(fm, frameTitle);
            g.drawString(frameTitle, xOffset, yOffset);
            xOffset += leftToRight ? titleLength + 5 : -5;
        }

        int bumpXOffset;
        int bumpLength;
        int buttonsWidth = getButtonsWidth();
        if (leftToRight) {
            bumpLength = width - buttonsWidth - xOffset - 5;
            bumpXOffset = xOffset;
        } else {
            bumpLength = xOffset - buttonsWidth - 5;
            bumpXOffset = buttonsWidth + 5;
        }
        int bumpYOffset = 3;
        int bumpHeight = getHeight() - (2 * bumpYOffset);
        bumps.setBumpArea(bumpLength, bumpHeight);
        bumps.paintIcon(this, g, bumpXOffset, bumpYOffset);
    }

    protected String getTitle(
        String text,
        FontMetrics fm,
        int availTextWidth) {
        if ((text == null) || (text.equals("")))
            return "";
        int textWidth = SwingUtilities.computeStringWidth(fm, text);
        String clipString = "\u2026";
        if (textWidth > availTextWidth) {
            int totalWidth = SwingUtilities.computeStringWidth(fm, clipString);
            int nChars;
            for (nChars = 0; nChars < text.length(); nChars++) {
                totalWidth += fm.charWidth(text.charAt(nChars));
                if (totalWidth > availTextWidth) {
                    break;
                }
            }
            text = text.substring(0, nChars) + clipString;
        }
        return text;
    }

    private int getButtonsWidth() {
        boolean leftToRight = PlasticUtils.isLeftToRight(frame);

        int w = getWidth();
        int x = leftToRight ? w : 0;
        int spacing;

        // assumes all buttons have the same dimensions
        // these dimensions include the borders
        int buttonWidth = closeButton.getIcon().getIconWidth();

        if (frame.isClosable()) {
            if (isPalette) {
                spacing = 3;
                x += leftToRight ? -spacing - (buttonWidth + 2) : spacing;
                if (!leftToRight)
                    x += (buttonWidth + 2);
            } else {
                spacing = 4;
                x += leftToRight ? -spacing - buttonWidth : spacing;
                if (!leftToRight)
                    x += buttonWidth;
            }
        }

        if (frame.isMaximizable() && !isPalette) {
            spacing = frame.isClosable() ? 10 : 4;
            x += leftToRight ? -spacing - buttonWidth : spacing;
            if (!leftToRight)
                x += buttonWidth;
        }

        if (frame.isIconifiable() && !isPalette) {
            spacing =
                frame.isMaximizable() ? 2 : (frame.isClosable() ? 10 : 4);
            x += leftToRight ? -spacing - buttonWidth : spacing;
            if (!leftToRight)
                x += buttonWidth;
        }

        return leftToRight ? w - x : x;
    }

}
