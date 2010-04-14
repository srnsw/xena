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

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalButtonUI;

/**
 * The JGoodies Plastic L&amp;F implementation of <code>ButtonUI</code>.
 * It differs from the superclass <code>MetalButtonUI</code> in that
 * it can add a pseudo 3D effect and that the border can paint the focus.
 *
 * @author Karsten Lentzsch
 * @version $Revision$
 */
public class PlasticButtonUI extends MetalButtonUI {

    private static final PlasticButtonUI INSTANCE = new PlasticButtonUI();

    private boolean borderPaintsFocus;

    public static ComponentUI createUI(JComponent b) {
        return INSTANCE;
    }

    /**
     * Installs defaults.
     */
    public void installDefaults(AbstractButton b) {
        super.installDefaults(b);
        borderPaintsFocus =
            Boolean.TRUE.equals(UIManager.get("Button.borderPaintsFocus"));
    }


    // Painting ***************************************************************

    public void update(Graphics g, JComponent c) {
        if (c.isOpaque()) {
            AbstractButton b = (AbstractButton) c;
            if (isToolBarButton(b)) {
                c.setOpaque(false);
            } else if (b.isContentAreaFilled()) {
                g.setColor(c.getBackground());
                g.fillRect(0, 0, c.getWidth(), c.getHeight());

                if (is3D(b)) {
                    Rectangle r =
                        new Rectangle(
                            1,
                            1,
                            c.getWidth() - 2,
                            c.getHeight() - 1);
                    PlasticUtils.add3DEffekt(g, r);
                }
            }
        }
        paint(g, c);
    }

    /**
     * Paints the focus with close to the button's border.
     */
    protected void paintFocus(
        Graphics g,
        AbstractButton b,
        Rectangle viewRect,
        Rectangle textRect,
        Rectangle iconRect) {

        if (borderPaintsFocus) {
            return;
        }

        boolean isDefault =
            b instanceof JButton && ((JButton) b).isDefaultButton();
        int topLeftInset = isDefault ? 3 : 2;
        int width = b.getWidth() - 1 - topLeftInset * 2;
        int height = b.getHeight() - 1 - topLeftInset * 2;

        g.setColor(getFocusColor());
        g.drawRect(topLeftInset, topLeftInset, width - 1, height - 1);
    }

    // Private Helper Code **************************************************************

    /**
     * Checks and answers if this is button is in a tool bar.
     *
     * @param b   the button to check
     * @return true if in tool bar, false otherwise
     */
    protected boolean isToolBarButton(AbstractButton b) {
        Container parent = b.getParent();
        return parent != null
            && (parent instanceof JToolBar
                || parent.getParent() instanceof JToolBar);
    }

    /**
     * Checks and answers if this button shall use a pseudo 3D effect.
     *
     * @param b  the button to check
     * @return true indicates a 3D effect, false flat
     */
    protected boolean is3D(AbstractButton b) {
        if (PlasticUtils.force3D(b))
            return true;
        if (PlasticUtils.forceFlat(b))
            return false;
        ButtonModel model = b.getModel();
        return PlasticUtils.is3D("Button.")
            && b.isBorderPainted()
            && model.isEnabled()
            && !(model.isPressed() && model.isArmed());
    }

}