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

package com.jgoodies.looks.common;

import java.awt.*;
import java.awt.print.PrinterGraphics;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicGraphicsUtils;

import com.jgoodies.looks.LookUtils;

/**
 * Provides convenience behavior used by the JGoodies Looks.<p>
 *
 * <strong>Note:</strong> This class is not part of the public Looks API.
 * It should be treated as library internal and should not be used by
 * API users. It may be removed or changed with the Looks version
 * without further notice.
 *
 * @author  Karsten Lentzsch
 * @author  Andrej Golovnin
 * @version $Revision$
 *
 * @since 2.0
 */
public final class RenderingUtils {

    private static final String PROP_DESKTOPHINTS = "awt.font.desktophints";

    private static final String SWING_UTILITIES2_NAME =
        LookUtils.IS_JAVA_6_OR_LATER
             ? "sun.swing.SwingUtilities2"
             : "com.sun.java.swing.SwingUtilities2";

    /**
     * In Java 5 or later, this field holds the public static method
     * <code>SwingUtilities2#drawStringUnderlinedAt</code> that has been added
     * for Java 5.
     */
    private static Method drawStringUnderlineCharAtMethod = null;

    static {
        if (LookUtils.IS_JAVA_5_OR_LATER) {
            drawStringUnderlineCharAtMethod = getMethodDrawStringUnderlineCharAt();
        }
    }


    private RenderingUtils() {
        // Overrides default constructor; prevents instantiation.
    }

    /**
     * Draws the string at the specified location underlining the specified
     * character.
     *
     * @param c JComponent that will display the string, may be null
     * @param g Graphics to draw the text to
     * @param text String to display
     * @param underlinedIndex Index of a character in the string to underline
     * @param x X coordinate to draw the text at
     * @param y Y coordinate to draw the text at
     */
    public static void drawStringUnderlineCharAt(JComponent c,Graphics g,
                           String text, int underlinedIndex, int x,int y) {
        if (LookUtils.IS_JAVA_5_OR_LATER) {
            if (drawStringUnderlineCharAtMethod != null) {
                try {
                    drawStringUnderlineCharAtMethod.invoke(null,
                            new Object[] {c, g, text, new Integer(underlinedIndex),
                                          new Integer(x), new Integer(y)});
                    return;
                } catch (IllegalArgumentException e) {
                    // Use the BasicGraphicsUtils as fallback
                } catch (IllegalAccessException e) {
                    // Use the BasicGraphicsUtils as fallback
                } catch (InvocationTargetException e) {
                    // Use the BasicGraphicsUtils as fallback
                }
            }
            Graphics2D g2 = (Graphics2D) g;
            Map oldRenderingHints = installDesktopHints(g2);
            BasicGraphicsUtils.drawStringUnderlineCharAt(g, text, underlinedIndex, x, y);
            if (oldRenderingHints != null) {
                g2.addRenderingHints(oldRenderingHints);
            }
            return;
        }
        BasicGraphicsUtils.drawStringUnderlineCharAt(g, text, underlinedIndex, x, y);
    }


    // Private Helper Code ****************************************************


    private static Method getMethodDrawStringUnderlineCharAt() {
        try {
            Class clazz = Class.forName(SWING_UTILITIES2_NAME);
            return clazz.getMethod(
                    "drawStringUnderlineCharAt",
                    new Class[] {JComponent.class, Graphics.class, String.class, Integer.TYPE, Integer.TYPE, Integer.TYPE}
                    );
        } catch (ClassNotFoundException e) {
            // returns null
        } catch (SecurityException e) {
            // returns null
        } catch (NoSuchMethodException e) {
            // returns null
        }
        return null;
    }


    private static Map installDesktopHints(Graphics2D g2) {
        Map oldRenderingHints = null;
        if (LookUtils.IS_JAVA_6_OR_LATER) {
            Map desktopHints = desktopHints(g2);
            if (desktopHints != null && !desktopHints.isEmpty()) {
                oldRenderingHints = new HashMap(desktopHints.size());
                RenderingHints.Key key;
                for (Iterator i = desktopHints.keySet().iterator(); i.hasNext();) {
                    key = (RenderingHints.Key) i.next();
                    oldRenderingHints.put(key, g2.getRenderingHint(key));
                }
                g2.addRenderingHints(desktopHints);
            }
        }
        return oldRenderingHints;
    }


    private static Map desktopHints(Graphics2D g2) {
        if (isPrinting(g2)) {
            return null;
        }
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        GraphicsDevice device = g2.getDeviceConfiguration().getDevice();
        Map desktopHints = (Map) toolkit.getDesktopProperty(
                PROP_DESKTOPHINTS + '.' + device.getIDstring());
        if (desktopHints == null) {
            desktopHints = (Map) toolkit.getDesktopProperty(PROP_DESKTOPHINTS);
        }
        // It is possible to get a non-empty map but with disabled AA.
        if (desktopHints != null) {
            Object aaHint = desktopHints.get(RenderingHints.KEY_TEXT_ANTIALIASING);
            if ((aaHint == RenderingHints.VALUE_TEXT_ANTIALIAS_OFF) ||
                (aaHint == RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT)) {
                desktopHints = null;
            }
        }
        return desktopHints;
    }


    private static boolean isPrinting(Graphics g) {
        return g instanceof PrintGraphics || g instanceof PrinterGraphics;
    }


}