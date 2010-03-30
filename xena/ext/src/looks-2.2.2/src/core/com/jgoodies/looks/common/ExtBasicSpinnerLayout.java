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

import javax.swing.UIManager;


/**
 * A simple layout manager for the editor and the next/previous buttons.
 * See the BasicSpinnerUI javadoc for more information about exactly how
 * the components are arranged.
 *
 * @author  Karsten Lentzsch
 * @version $Revision$
 */
public final class ExtBasicSpinnerLayout implements LayoutManager {

    /**
     * Used by the default LayoutManager class - SpinnerLayout for
     * missing (null) editor/nextButton/previousButton children.
     */
    private static final Dimension ZERO_SIZE = new Dimension(0, 0);


    private Component nextButton     = null;
    private Component previousButton = null;
    private Component editor         = null;


    public void addLayoutComponent(String name, Component c) {
        if ("Next".equals(name)) {
            nextButton = c;
        } else if ("Previous".equals(name)) {
            previousButton = c;
        } else if ("Editor".equals(name)) {
            editor = c;
        }
    }


    public void removeLayoutComponent(Component c) {
        if (c == nextButton) {
            c = null;
        } else if (c == previousButton) {
            previousButton = null;
        } else if (c == editor) {
            editor = null;
        }
    }


    private Dimension preferredSize(Component c) {
        return (c == null) ? ZERO_SIZE : c.getPreferredSize();
    }


    public Dimension preferredLayoutSize(Container parent) {
        Dimension nextD = preferredSize(nextButton);
        Dimension previousD = preferredSize(previousButton);
        Dimension editorD = preferredSize(editor);

        Dimension size = new Dimension(editorD.width, editorD.height);
        size.width += Math.max(nextD.width, previousD.width);
        Insets insets = parent.getInsets();
        size.width += insets.left + insets.right;
        size.height += insets.top + insets.bottom;
        return size;
    }


    public Dimension minimumLayoutSize(Container parent) {
        return preferredLayoutSize(parent);
    }


    private void setBounds(Component c, int x, int y, int width, int height) {
        if (c != null) {
            c.setBounds(x, y, width, height);
        }
    }


    public void layoutContainer(Container parent) {
        int width = parent.getWidth();
        int height = parent.getHeight();

        Insets insets = parent.getInsets();
        Dimension nextD = preferredSize(nextButton);
        Dimension previousD = preferredSize(previousButton);
        int buttonsWidth = Math.max(nextD.width, previousD.width);
        int editorHeight = height - (insets.top + insets.bottom);

        // The arrowButtonInsets value is used instead of the JSpinner's
        // insets if not null. Defining this to be (0, 0, 0, 0) causes the
        // buttons to be aligned with the outer edge of the spinner's
        // border, and leaving it as "null" places the buttons completely
        // inside the spinner's border.
        Insets buttonInsets = UIManager
                .getInsets("Spinner.arrowButtonInsets");
        if (buttonInsets == null) {
            buttonInsets = insets;
        }

        /*
         * Deal with the spinner's componentOrientation property.
         */
        int editorX, editorWidth, buttonsX;
        if (parent.getComponentOrientation().isLeftToRight()) {
            editorX = insets.left;
            editorWidth = width - insets.left - buttonsWidth
                    - buttonInsets.right;
            buttonsX = width - buttonsWidth - buttonInsets.right;
        } else {
            buttonsX = buttonInsets.left;
            editorX = buttonsX + buttonsWidth;
            editorWidth = width - buttonInsets.left - buttonsWidth
                    - insets.right;
        }

        int nextY = buttonInsets.top;
        int nextHeight = (height / 2) + (height % 2) - nextY;
        int previousY = buttonInsets.top + nextHeight;
        int previousHeight = height - previousY - buttonInsets.bottom;

        setBounds(editor, editorX, insets.top, editorWidth, editorHeight);
        setBounds(nextButton, buttonsX, nextY, buttonsWidth, nextHeight);
        setBounds(previousButton, buttonsX, previousY, buttonsWidth,
                previousHeight);
    }
}