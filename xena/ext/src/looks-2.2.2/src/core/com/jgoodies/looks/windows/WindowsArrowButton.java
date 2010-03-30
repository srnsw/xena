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

package com.jgoodies.looks.windows;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicArrowButton;

/**
 * The JGoodies Windows Look&amp;Feel implementation for
 * arrow buttons used in scrollbars and comboboxes.
 * <p>
 * It differs from <code>BasicArrowButton</code> in that the preferred size
 * is always a square.
 * It differs from <code>WindowsScrollBarUI.WindowsArrowButton</code>
 * in that the triangle is black and positioned correctly.
 *
 * @author  Karsten Lentzsch
 * @version $Revision$
 */
final class WindowsArrowButton extends BasicArrowButton {

    public WindowsArrowButton(int direction) {
        super(direction);
    }

    public Dimension getPreferredSize() {
        int width = Math.max(5, UIManager.getInt("ScrollBar.width"));
        return new Dimension(width, width);
    }

    public void paintTriangle(
        Graphics g,
        int x,
        int y,
        int size,
        int triangleDirection,
        boolean isEnabled) {
        Color oldColor = g.getColor();
        int mid, i, j;

        j = 0;
        size = Math.max(size, 2);
        mid = (size - 1) / 2; // Modified by JGoodies

        g.translate(x, y);
        if (isEnabled)
            g.setColor(Color.black);
        else
            g.setColor(UIManager.getColor("controlShadow"));

        switch (triangleDirection) {
            case NORTH :
                for (i = 0; i < size; i++) {
                    g.drawLine(mid - i, i, mid + i, i);
                }
                if (!isEnabled) {
                    g.setColor(UIManager.getColor("controlLtHighlight"));
                    g.drawLine(mid - i + 2, i, mid + i, i);
                }
                break;
            case SOUTH :
                if (!isEnabled) {
                    g.translate(1, 1);
                    g.setColor(UIManager.getColor("controlLtHighlight"));
                    for (i = size - 1; i >= 0; i--) {
                        g.drawLine(mid - i, j, mid + i, j);
                        j++;
                    }
                    g.translate(-1, -1);
                    g.setColor(UIManager.getColor("controlShadow"));
                }

                j = 0;
                for (i = size - 1; i >= 0; i--) {
                    g.drawLine(mid - i, j, mid + i, j);
                    j++;
                }
                break;
            case WEST :
                for (i = 0; i < size; i++) {
                    g.drawLine(i, mid - i, i, mid + i);
                }
                if (!isEnabled) {
                    g.setColor(UIManager.getColor("controlLtHighlight"));
                    g.drawLine(i, mid - i + 2, i, mid + i);
                }
                break;
            case EAST :
                if (!isEnabled) {
                    g.translate(1, 1);
                    g.setColor(UIManager.getColor("controlLtHighlight"));
                    for (i = size - 1; i >= 0; i--) {
                        g.drawLine(j, mid - i, j, mid + i);
                        j++;
                    }
                    g.translate(-1, -1);
                    g.setColor(UIManager.getColor("controlShadow"));
                }

                j = 0;
                for (i = size - 1; i >= 0; i--) {
                    g.drawLine(j, mid - i, j, mid + i);
                    j++;
                }
                break;
        }
        g.translate(-x, -y);
        g.setColor(oldColor);
    }
}
