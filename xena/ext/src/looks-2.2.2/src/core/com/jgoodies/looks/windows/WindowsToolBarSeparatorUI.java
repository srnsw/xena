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

import javax.swing.JComponent;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolBarSeparatorUI;

import com.jgoodies.looks.LookUtils;

/**
 * A Windows tool bar separator that honors the tool bar's border.
 * Used in in 1.4.0, 1.4.1 and 1.4.2 with XP turned off.
 * In addition this class reuses a single UI instance.
 *
 * @author Karsten Lentzsch
 * @version $Revision$
 */
public final class WindowsToolBarSeparatorUI
    extends BasicToolBarSeparatorUI {

    /** See bug #4773490 in Sun's bug database. */
    private static final int VERTICAL = LookUtils.IS_JAVA_1_4_2_OR_LATER
            ? SwingConstants.VERTICAL
            : SwingConstants.HORIZONTAL;

    /** Shared UI component. */
    private static WindowsToolBarSeparatorUI toolBarSeparatorUI;

    public static ComponentUI createUI(JComponent c) {
        if (toolBarSeparatorUI == null) {
            toolBarSeparatorUI = new WindowsToolBarSeparatorUI();
        }
        return toolBarSeparatorUI;
    }

    public void paint(Graphics g, JComponent c) {
        Color temp = g.getColor();

        Color shadowColor    = UIManager.getColor("ToolBar.shadow");
        Color highlightColor = UIManager.getColor("ToolBar.highlight");

        Dimension size = c.getSize();

        if (((JSeparator) c).getOrientation() == VERTICAL) {
            int x = (size.width / 2) - 1;
            g.setColor(shadowColor);
            g.drawLine(x, 0, x, size.height - 1);
            g.setColor(highlightColor);
            g.drawLine(x + 1, 0, x + 1, size.height - 1);
        } else {
            int y = (size.height / 2) - 1;
            g.setColor(shadowColor);
            g.drawLine(0, y, size.width - 1, y);
            g.setColor(highlightColor);
            g.drawLine(0, y + 1, size.width - 1, y + 1);
        }
        g.setColor(temp);
    }
}