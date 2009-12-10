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
import java.awt.Graphics;

import javax.swing.plaf.metal.MetalLookAndFeel;

import com.jgoodies.looks.LookUtils;


/**
 * Consists exclusively of static methods that provide convenience behavior.
 *
 * @author  Karsten Lentzsch
 * @version $Revision$
 */

public final class PlasticXPUtils {

    private PlasticXPUtils() {
        // Overrides default constructor; prevents instantiation.
    }

    /**
     * Draws a plain border for an xp button.
     */
    static void drawPlainButtonBorder(Graphics g, int x, int y, int w, int h) {
        drawButtonBorder(g, x, y, w, h,
                    PlasticLookAndFeel.getControl(),
                    PlasticLookAndFeel.getControlDarkShadow(),
                    LookUtils.getSlightlyBrighter(
                        PlasticLookAndFeel.getControlDarkShadow(),
                        1.25f)
                      );
    }

    /**
     * Draws a border for a pressed xp button.
     */
    static void drawPressedButtonBorder(Graphics g, int x, int y, int w, int h) {
        drawPlainButtonBorder(g, x, y, w, h);
        Color darkColor =
                translucentColor(PlasticLookAndFeel.getControlDarkShadow(),
                                 128);
        Color lightColor =
                translucentColor(PlasticLookAndFeel.getControlHighlight(),
                                 80);
        g.translate(x, y);
        g.setColor(darkColor);
        g.fillRect(2, 1,  w-4, 1);

        g.setColor(lightColor);
        g.fillRect(2, h-2,  w-4, 1);
        g.translate(-x, -y);
    }

    /**
     * Draws a border for a default xp button.
     */
    static void drawDefaultButtonBorder(Graphics g, int x, int y, int w, int h) {
        drawPlainButtonBorder(g, x, y, w, h);
        drawInnerButtonDecoration(g, x, y, w, h,
                    PlasticLookAndFeel.getPrimaryControlDarkShadow());
    }

    /**
     * Draws a border for a focused xp button.
     */
    static void drawFocusedButtonBorder(Graphics g, int x, int y, int w, int h) {
        drawPlainButtonBorder(g, x, y, w, h);
        drawInnerButtonDecoration(g, x, y, w, h,
                    PlasticLookAndFeel.getFocusColor());
    }

    /**
     * Draws a border for a disabled xp button.
     */
    static void drawDisabledButtonBorder(Graphics g, int x, int y, int w, int h) {
        drawButtonBorder(g, x, y, w, h,
                    PlasticLookAndFeel.getControl(),
                    MetalLookAndFeel.getControlShadow(),
                    LookUtils.getSlightlyBrighter(
                        MetalLookAndFeel.getControlShadow()));
    }


    /**
     * Draws a button border for an xp button with the given colors.
     */
    public static void drawButtonBorder(
        Graphics g,
        int x, int y, int w, int h,
        Color backgroundColor,
        Color edgeColor,
        Color cornerColor) {

        g.translate(x, y);
        // Edges
        g.setColor(edgeColor);
        drawRect(g, 0, 0,  w-1, h-1);

        // Near corners
        g.setColor(cornerColor);
        g.fillRect(0,   0,   2, 2);
        g.fillRect(0,   h-2, 2, 2);
        g.fillRect(w-2, 0,   2, 2);
        g.fillRect(w-2, h-2, 2, 2);

        // Corners
        g.setColor(backgroundColor);
        g.fillRect(0,   0,   1, 1);
        g.fillRect(0,   h-1, 1, 1);
        g.fillRect(w-1, 0,   1, 1);
        g.fillRect(w-1, h-1, 1, 1);

        g.translate(-x, -y);
    }


    /**
     * Draws a button border for an xp button with the given colors.
     */
    private static void drawInnerButtonDecoration(
        Graphics g,
        int x, int y, int w, int h,
        Color baseColor) {

        Color lightColor  = translucentColor(baseColor,  90);
        Color mediumColor = translucentColor(baseColor, 120);
        Color darkColor   = translucentColor(baseColor, 200);

        g.translate(x, y);
        g.setColor(lightColor);
        g.fillRect(2, 1,  w-4, 1);

        g.setColor(mediumColor);
        g.fillRect (1,   2,  1,   h-4);
        g.fillRect (w-2, 2,  1,   h-4);
        drawRect(g, 2,   2,  w-5, h-5);

        g.setColor(darkColor);
        g.fillRect(2,   h-2,  w-4, 1);
        g.translate(-x, -y);
    }


    /**
     * An optimized version of Graphics.drawRect.
     */
    static void drawRect(Graphics g, int x, int y, int w, int h) {
        g.fillRect(x,   y,   w+1, 1);
        g.fillRect(x,   y+1, 1,   h);
        g.fillRect(x+1, y+h, w,   1);
        g.fillRect(x+w, y+1, 1,   h);
    }


    /**
     * Returns a color that is a translucent copy of the given color.
     *
     * @param baseColor     the base color
     * @param alpha         the alpha value
     * @return the translucent color with specified alpha
     */
    private static Color translucentColor(Color baseColor, int alpha) {
        return new Color(baseColor.getRed(),
                          baseColor.getGreen(),
                          baseColor.getBlue(),
                          alpha);
    }

}