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
import java.io.Serializable;

import javax.swing.*;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;

import com.jgoodies.looks.LookUtils;

/**
 * Factory class that vends <code>Icon</code>s for the JGoodies
 * Plastic XP look&amp;feel.
 * These icons are used extensively in PlasticXP via the defaults mechanism.
 * While other look and feels often use GIFs for icons, creating icons
 * in code facilitates switching to other themes.
 * <p>
 * Each method in this class returns either an <code>Icon</code> or
 * <code>null</code>, where <code>null</code> implies that there is
 * no default icon.
 *
 * @author  Karsten Lentzsch
 * @version $Revision$
 */
public final class PlasticXPIconFactory {

    private static CheckBoxIcon    checkBoxIcon;
    private static RadioButtonIcon radioButtonIcon;


    private PlasticXPIconFactory() {
        // Overrides default constructor; prevents instantiation.
    }


    /**
     * Lazily creates and returns the check box icon.
     *
     * @return the check box icon
     */
    static Icon getCheckBoxIcon() {
        if (checkBoxIcon == null) {
            checkBoxIcon = new CheckBoxIcon();
        }
        return checkBoxIcon;
    }

    /**
     * Lazily creates and returns the radio button icon.
     *
     * @return the check box icon
     */
    static Icon getRadioButtonIcon() {
        if (radioButtonIcon == null) {
            radioButtonIcon = new RadioButtonIcon();
        }
        return radioButtonIcon;
    }


    /**
     * Paints the the icon and focus border for Plastic XP check boxes.
     */
    private static final class CheckBoxIcon implements Icon, UIResource, Serializable {

        private static final int SIZE = LookUtils.IS_LOW_RESOLUTION ? 13 : 15;

        public int getIconWidth()  { return SIZE; }
        public int getIconHeight() { return SIZE; }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            JCheckBox cb = (JCheckBox) c;
            ButtonModel model = cb.getModel();
            Graphics2D g2 = (Graphics2D) g;
            boolean paintFocus =    (model.isArmed() && !model.isPressed())
                                 || (cb.hasFocus() && isBlank(cb.getText()));

            final RenderingHints.Key key = RenderingHints.KEY_ANTIALIASING;
            Object newAAHint = RenderingHints.VALUE_ANTIALIAS_ON;
            Object oldAAHint = g2.getRenderingHint(key);
            if (newAAHint != oldAAHint) {
                g2.setRenderingHint(key, newAAHint);
            } else {
                oldAAHint = null;
            }

            drawBorder(g2, model.isEnabled(), x, y, SIZE - 1, SIZE - 1);
            drawFill(g2, model.isPressed(), x + 1, y + 1, SIZE - 2, SIZE - 2);
            if (paintFocus) {
                drawFocus(g2, x + 1, y + 1, SIZE - 3, SIZE - 3);
            }
            if (model.isSelected()) {
                drawCheck(g2, model.isEnabled(), x + 3, y + 3, SIZE - 7, SIZE - 7);
            }

            if (oldAAHint != null) {
                g2.setRenderingHint(key, oldAAHint);
            }
        }

        private void drawBorder(Graphics2D g2, boolean enabled, int x, int y, int width, int height) {
            g2.setColor(enabled
                    ? PlasticLookAndFeel.getControlDarkShadow()
                    : MetalLookAndFeel.getControlDisabled());
            g2.drawRect(x, y, width, height);
        }

        private void drawCheck(Graphics2D g2, boolean enabled, int x, int y, int width, int height) {
            g2.setColor(enabled
                    ? UIManager.getColor("CheckBox.check")
                    : MetalLookAndFeel.getControlDisabled());
            int right  = x + width;
            int bottom = y + height;
            int startY = y + height / 3;
            int turnX  = x + width  / 2 - 2;
            g2.drawLine(x        , startY    , turnX, bottom-3);
			g2.drawLine(x        , startY + 1, turnX, bottom-2);
			g2.drawLine(x        , startY + 2, turnX, bottom-1);
			g2.drawLine(turnX + 1, bottom - 2, right, y       );
			g2.drawLine(turnX + 1, bottom - 1, right, y + 1   );
			g2.drawLine(turnX + 1, bottom    , right, y + 2   );
        }

        private void drawFill(Graphics2D g2, boolean pressed, int x, int y, int w, int h) {
            Color upperLeft;
            Color lowerRight;
            if (pressed) {
                upperLeft  = MetalLookAndFeel.getControlShadow();
                lowerRight = PlasticLookAndFeel.getControlHighlight();
            } else {
                upperLeft  = PlasticLookAndFeel.getControl();
                lowerRight = PlasticLookAndFeel.getControlHighlight().brighter();
            }
            g2.setPaint(new GradientPaint(x, y, upperLeft, x + w, y + h, lowerRight));
            g2.fillRect(x, y, w, h);
        }

        private void drawFocus(Graphics2D g2, int x, int y, int width, int height) {
            g2.setPaint(new GradientPaint(
                    x,
                    y,
                    PlasticLookAndFeel.getFocusColor().brighter(),
                    width,
                    height,
                    PlasticLookAndFeel.getFocusColor() /*.darker()*/
            ));
            g2.drawRect(x, y, width, height);
            g2.drawRect(x + 1, y + 1, width - 2, height - 2);
        }

    }


    /**
     * Paints the the icon and focus border for Plastic XP check boxes.
     */
    private static final class RadioButtonIcon implements Icon, UIResource, Serializable {

        private static final int SIZE = LookUtils.IS_LOW_RESOLUTION ? 13 : 15;

        private static final Stroke FOCUS_STROKE = new BasicStroke(2);

        public int getIconWidth()  { return SIZE; }
        public int getIconHeight() { return SIZE; }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g;
            AbstractButton rb = (AbstractButton) c;
            ButtonModel model = rb.getModel();
            boolean paintFocus =    (model.isArmed() && !model.isPressed())
                                 || (rb.hasFocus() && isBlank(rb.getText()));

            final RenderingHints.Key key = RenderingHints.KEY_ANTIALIASING;
            Object newAAHint = RenderingHints.VALUE_ANTIALIAS_ON;
            Object oldAAHint = g2.getRenderingHint(key);
            if (newAAHint != oldAAHint) {
                g2.setRenderingHint(key, newAAHint);
            } else {
                oldAAHint = null;
            }

            drawFill(g2, model.isPressed(), x, y, SIZE - 1, SIZE - 1);
            if (paintFocus) {
                drawFocus(g2, x + 1, y + 1, SIZE - 3, SIZE - 3);
            }
            if (model.isSelected()) {
                drawCheck(g2, c, model.isEnabled(), x + 4, y + 4, SIZE - 8, SIZE - 8);
            }
        	drawBorder(g2, model.isEnabled(), x, y, SIZE-1, SIZE-1);

            if (oldAAHint != null) {
                g2.setRenderingHint(key, oldAAHint);
            }
        }

        private void drawBorder(Graphics2D g2, boolean enabled, int x, int y, int w, int h) {
            g2.setColor(enabled
                ? PlasticLookAndFeel.getControlDarkShadow()
                : MetalLookAndFeel.getControlDisabled());
            g2.drawOval(x, y, w, h);
        }

        private void drawCheck(Graphics2D g2, Component c, boolean enabled, int x, int y, int w, int h) {
            g2.translate(x,y);
            if (enabled) {
                g2.setColor(UIManager.getColor("RadioButton.check"));
                g2.fillOval(0,0,w,h);
                UIManager.getIcon("RadioButton.checkIcon").paintIcon(c, g2, 0,0);
            } else {
                g2.setColor(MetalLookAndFeel.getControlDisabled());
                g2.fillOval(0,0,w,h);
            }
            g2.translate(-x, -y);
        }

        private void drawFill(Graphics2D g2, boolean pressed, int x, int y, int w, int h) {
            Color upperLeft;
            Color lowerRight;
            if (pressed) {
                upperLeft  = MetalLookAndFeel.getControlShadow();
                lowerRight = PlasticLookAndFeel.getControlHighlight();
            } else {
                upperLeft  = PlasticLookAndFeel.getControl();
                lowerRight = PlasticLookAndFeel.getControlHighlight().brighter();
            }
            g2.setPaint(new GradientPaint(x, y, upperLeft, x + w, y + h, lowerRight));
            g2.fillOval(x, y, w, h);
        }

        private void drawFocus(Graphics2D g2, int x, int y, int w, int h) {
            g2.setPaint(
                new GradientPaint(
                    x,
                    y,
                    PlasticLookAndFeel.getFocusColor().brighter(),
                    w,
                    h,
                    PlasticLookAndFeel.getFocusColor()));
            Stroke stroke = g2.getStroke();
            g2.setStroke(FOCUS_STROKE);
            g2.drawOval(x, y, w, h);
            g2.setStroke(stroke);
        }

    }


    // Helper Code ************************************************************

    /**
     * Checks and answers if the given string is whitespace,
     * empty ("") or <code>null</code>.
     *
     * <pre>
     * isBlank(null)    == true
     * isBlank("")      == true
     * isBlank(" ")     == true
     * isBlank(" abc")  == false
     * isBlank("abc ")  == false
     * isBlank(" abc ") == false
     * </pre>
     *
     * @param str   the string to check, may be <code>null</code>
     * @return <code>true</code> if the string is whitespace, empty
     *    or <code>null</code>
     */
    private static boolean isBlank(String str) {
        int length;
        if ((str == null) || ((length = str.length()) == 0))
            return true;
        for (int i = length-1; i >= 0; i--) {
            if (!Character.isWhitespace(str.charAt(i)))
                return false;
        }
        return true;
    }


}
