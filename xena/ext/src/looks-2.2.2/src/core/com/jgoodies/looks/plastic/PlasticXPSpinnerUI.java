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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalLookAndFeel;


/**
 * The JGoodies PlasticXP Look&amp;Feel implementation of <code>SpinnerUI</code>.
 * Configures the default editor to adjust font baselines and component
 * bounds. Also, changes the border of the buttons and the size of the arrows.
 *
 * @author Karsten Lentzsch
 * @version $Revision$
 */
public final class PlasticXPSpinnerUI extends PlasticSpinnerUI {

	public static ComponentUI createUI(JComponent b) {
		return new PlasticXPSpinnerUI();
	}


    protected Component createArrowButton(int direction) {
        return new SpinnerXPArrowButton(direction);
    }


    /**
     * It differs from its superclass in that it uses the same formula as JDK
     * to calculate the arrow height.
     */
    private static final class SpinnerXPArrowButton extends PlasticArrowButton {

        SpinnerXPArrowButton(int direction) {
            // If you change the value of the button width, don't forget
            // to change it in PlasticXPBorders#XPSpinnerBorder too.
            super(direction, UIManager.getInt("ScrollBar.width") - 1, false);
        }

        protected int calculateArrowHeight(int height, int width) {
            int arrowHeight = Math.min((height - 4) / 3, (width - 4) / 3);
            return Math.max(arrowHeight, 3);
        }

        protected boolean isPaintingNorthBottom() {
            return true;
        }

        protected int calculateArrowOffset() {
            return 1;
        }

        protected void paintNorth(Graphics g, boolean leftToRight, boolean isEnabled,
                Color arrowColor, boolean isPressed,
                int width, int height, int w, int h, int arrowHeight, int arrowOffset,
                boolean paintBottom) {
            if (!isFreeStanding) {
                height += 1;
                g.translate(0, -1);
                if (!leftToRight) {
                    width += 1;
                    g.translate(-1, 0);
                } else {
                    width += 2;
                }
            }

            // Draw the arrow
            g.setColor(arrowColor);
            int startY = 1 + ((h + 1) - arrowHeight) / 2; // KL was (h + 1)
            int startX = w / 2;
            // System.out.println( "startX :" + startX + " startY :"+startY);
            for (int line = 0; line < arrowHeight; line++) {
                g.fillRect(startX - line - arrowOffset, startY + line,
                        2 * (line + 1), 1);
            }

            paintNorthBorder(g, isEnabled, width, height, paintBottom);

            if (!isFreeStanding) {
                height -= 1;
                g.translate(0, 1);
                if (!leftToRight) {
                    width -= 1;
                    g.translate(1, 0);
                } else {
                    width -= 2;
                }
            }
        }

        private void paintNorthBorder(Graphics g, boolean isEnabled, int w, int h, boolean paintBottom) {
            if (isEnabled) {
                boolean isPressed = model.isPressed() && model.isArmed();
                if (isPressed) {
                    PlasticXPUtils.drawPressedButtonBorder(g, 0, 1, w - 2, h);
                } else {
                    PlasticXPUtils.drawPlainButtonBorder(g, 0, 1, w - 2, h);
                }
            } else {
                PlasticXPUtils.drawDisabledButtonBorder(g, 0, 1, w - 2, h + 1);
            }
            // Paint one pixel on the arrow button's left hand side.
            g.setColor(isEnabled
                    ? PlasticLookAndFeel.getControlDarkShadow()
                    : MetalLookAndFeel.getControlShadow());
            g.fillRect(0, 1, 1, 1);

            if (paintBottom) {
                g.fillRect(0, h - 1, w - 1, 1);
            }
        }


        protected void paintSouth(Graphics g, boolean leftToRight, boolean isEnabled,
                Color arrowColor, boolean isPressed,
                int width, int height, int w, int h, int arrowHeight, int arrowOffset) {

            if (!isFreeStanding) {
                height += 1;
                if (!leftToRight) {
                    width += 1;
                    g.translate(-1, 0);
                } else {
                    width += 2;
                }
            }

            // Draw the arrow
            g.setColor(arrowColor);

            int startY = (((h + 0) - arrowHeight) / 2) + arrowHeight - 2; // KL was h + 1
            int startX = w / 2;

            //System.out.println( "startX2 :" + startX + " startY2 :"+startY);

            for (int line = 0; line < arrowHeight; line++) {
                g.fillRect(startX - line - arrowOffset, startY - line,
                        2 * (line + 1), 1);
            }

            paintSouthBorder(g, isEnabled, width, height);

            if (!isFreeStanding) {
                height -= 1;
                if (!leftToRight) {
                    width -= 1;
                    g.translate(1, 0);
                } else {
                    width -= 2;
                }
            }
        }

        private void paintSouthBorder(Graphics g, boolean isEnabled, int w, int h) {
            if (isEnabled) {
                boolean isPressed = model.isPressed() && model.isArmed();
                if (isPressed) {
                    PlasticXPUtils.drawPressedButtonBorder(g, 0, -2, w - 2, h + 1);
                } else {
                    PlasticXPUtils.drawPlainButtonBorder(g, 0, -2, w - 2, h + 1);
                }
            } else {
                PlasticXPUtils.drawDisabledButtonBorder(g, 0, -2, w-2, h + 1);
            }
            // Paint one pixel on the arrow button's left hand side.
            g.setColor(isEnabled
                    ? PlasticLookAndFeel.getControlDarkShadow()
                    : MetalLookAndFeel.getControlShadow());
            g.fillRect(0, h - 2, 1, 1);
        }

    }

}