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

import java.awt.*;

import javax.swing.JButton;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

/**
 * Paints nicely rendered one touch triangles.
 *
 * @author  Karsten Lentzsch
 * @version $Revision$
 *
 * @see WindowsSplitPaneUI
 */
final class WindowsSplitPaneDivider extends BasicSplitPaneDivider {

    private static final int EXT_ONE_TOUCH_SIZE   = 5;
    private static final int EXT_ONE_TOUCH_OFFSET = 2;
    private static final int EXT_BLOCKSIZE        = 6;

    /**
     * Used to lay out a WindowsSplitPaneDivider. Layout for the divider
     * involves appropriately moving the left/right buttons around.
     * <p>
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of MetalSplitPaneDivider.
     */
    public final class ExtWindowsDividerLayout implements LayoutManager {
        public void layoutContainer(Container c) {
            JButton theLeftButton = getLeftButtonFromSuper();
            JButton theRightButton = getRightButtonFromSuper();
            JSplitPane theSplitPane = getSplitPaneFromSuper();
            int theOrientation = getOrientationFromSuper();
            int oneTouchSize = getOneTouchSize();
            int oneTouchOffset = getOneTouchOffset();
            int blockSize = 5;
            //getBlockSize(); //Math.min(getDividerSize(), oneTouchSize);

            // This layout differs from the one used in BasicSplitPaneDivider.
            // It does not center justify the oneTouchExpadable buttons.
            // This was necessary in order to meet the spec of the Metal
            // splitpane divider.
            if (theLeftButton != null
                && theRightButton != null
                && c == WindowsSplitPaneDivider.this) {
                if (theSplitPane.isOneTouchExpandable()) {
                    if (theOrientation == JSplitPane.VERTICAL_SPLIT) {
                        theLeftButton.setBounds(
                            oneTouchOffset,
                            0,
                            blockSize * 2,
                            blockSize);
                        theRightButton.setBounds(
                            oneTouchOffset + oneTouchSize * 2,
                            0,
                            blockSize * 2,
                            blockSize);
                    } else {
                        theLeftButton.setBounds(
                            0,
                            oneTouchOffset,
                            blockSize,
                            blockSize * 2);
                        theRightButton.setBounds(
                            0,
                            oneTouchOffset + oneTouchSize * 2,
                            blockSize,
                            blockSize * 2);
                    }
                } else {
                    theLeftButton.setBounds(-5, -5, 1, 1);
                    theRightButton.setBounds(-5, -5, 1, 1);
                }
            }
        }

        public Dimension minimumLayoutSize(Container c) {
            return new Dimension(0, 0);
        }
        public Dimension preferredLayoutSize(Container c) {
            return new Dimension(0, 0);
        }
        public void removeLayoutComponent(Component c) {
            // Unused method; implements LayoutManager.
        }
        public void addLayoutComponent(String string, Component c) {
            // Unused method; implements LayoutManager.
        }
    }

    public WindowsSplitPaneDivider(BasicSplitPaneUI ui) {
        super(ui);
        setLayout(new ExtWindowsDividerLayout());
    }

    /**
     * Creates and return an instance of JButton that can be used to
     * collapse the left component in the metal split pane.
     */
    protected JButton createLeftOneTouchButton() {
        JButton b = new JButton() {
                // Sprite buffer for the arrow image of the left button
    int[][] buffer = { { 0, 0, 0, 2, 2, 0, 0, 0, 0 }, {
                    0, 0, 2, 1, 1, 1, 0, 0, 0 }, {
                    0, 2, 1, 1, 1, 1, 1, 0, 0 }, {
                    2, 1, 1, 1, 1, 1, 1, 1, 0 }, {
                    0, 3, 3, 3, 3, 3, 3, 3, 3 }
            };

            public void setBorder(Border border) {
                // Ignore borders
            }

            public void paint(Graphics g) {
                JSplitPane theSplitPane = getSplitPaneFromSuper();
                if (theSplitPane != null) {
                    int theOrientation = getOrientationFromSuper();
                    int blockSize = buffer.length + 1;
                    //Math.min(getDividerSize(), oneTouchSize);

                    // Initialize the color array
                    Color[] colors =
                        {
                            this.getBackground(),
                            UIManager.getColor("controlDkShadow"),
                            Color.black,
                        //UIManager.getColor(),
                        UIManager.getColor("controlLtHighlight")};

                    // Fill the background first ...
                    g.setColor(this.getBackground());
                    g.fillRect(0, 0, this.getWidth(), this.getHeight());

                    // ... then draw the arrow.
                    if (getModel().isPressed()) {
                        // Adjust color mapping for pressed button state
                        colors[1] = colors[2];
                    }
                    if (theOrientation == JSplitPane.VERTICAL_SPLIT) {
                        // Draw the image for a vertical split
                        for (int i = 1; i <= buffer[0].length; i++) {
                            for (int j = 1; j < blockSize; j++) {
                                if (buffer[j - 1][i - 1] == 0) {
                                    continue;
                                }
                                g.setColor(colors[buffer[j - 1][i - 1]]);
                                g.drawLine(i - 1, j, i - 1, j);
                            }
                        }
                    } else {
                        // Draw the image for a horizontal split
                        // by simply swaping the i and j axis.
                        // Except the drawLine() call this code is
                        // identical to the code block above. This was done
                        // in order to remove the additional orientation
                        // check for each pixel.
                        for (int i = 1; i <= buffer[0].length; i++) {
                            for (int j = 1; j < blockSize; j++) {
                                if (buffer[j - 1][i - 1] == 0) {
                                    // Nothing needs
                                    // to be drawn
                                    continue;
                                }
                                // Set the color from the
                                // color map
                                g.setColor(colors[buffer[j - 1][i - 1]]);
                                // Draw a pixel
                                g.drawLine(j - 1, i, j - 1, i);
                            }
                        }
                    }
                }
            }

        };
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setFocusable(false);
        b.setOpaque(false);
        return b;
    }

    /**
     * Creates and return an instance of JButton that can be used to
     * collapse the right component in the metal split pane.
     */
    protected JButton createRightOneTouchButton() {
        JButton b = new JButton() {
                // Sprite buffer for the arrow image of the right button
    int[][] buffer = { { 2, 2, 2, 2, 2, 2, 2, 2 }, {
                    0, 1, 1, 1, 1, 1, 1, 3 }, {
                    0, 0, 1, 1, 1, 1, 3, 0 }, {
                    0, 0, 0, 1, 1, 3, 0, 0 }, {
                    0, 0, 0, 0, 3, 0, 0, 0 }
            };

            public void setBorder(Border border) {
                // Ignore borders
            }

            public void paint(Graphics g) {
                JSplitPane theSplitPane = getSplitPaneFromSuper();
                if (theSplitPane != null) {
                    int theOrientation = getOrientationFromSuper();
                    int blockSize = buffer.length + 1;
                    //Math.min(getDividerSize(), oneTouchSize);

                    // Initialize the color array
                    Color[] colors =
                        {
                            this.getBackground(),
                            UIManager.getColor("controlDkShadow"),
                            Color.black,
                        //UIManager.getColor("controlDkShadow"),
                        UIManager.getColor("controlLtHighlight")};

                    // Fill the background first ...
                    g.setColor(this.getBackground());
                    g.fillRect(0, 0, this.getWidth(), this.getHeight());

                    // ... then draw the arrow.
                    if (getModel().isPressed()) {
                        // Adjust color mapping for pressed button state
                        colors[1] = colors[2];
                    }
                    if (theOrientation == JSplitPane.VERTICAL_SPLIT) {
                        // Draw the image for a vertical split
                        for (int i = 1; i <= buffer[0].length; i++) {
                            for (int j = 1; j < blockSize; j++) {
                                if (buffer[j - 1][i - 1] == 0) {
                                    continue;
                                }
                                g.setColor(colors[buffer[j - 1][i - 1]]);
                                g.drawLine(i, j, i, j);
                            }
                        }
                    } else {
                        // Draw the image for a horizontal split
                        // by simply swaping the i and j axis.
                        // Except the drawLine() call this code is
                        // identical to the code block above. This was done
                        // in order to remove the additional orientation
                        // check for each pixel.
                        for (int i = 1; i <= buffer[0].length; i++) {
                            for (int j = 1; j < blockSize; j++) {
                                if (buffer[j - 1][i - 1] == 0) {
                                    // Nothing needs
                                    // to be drawn
                                    continue;
                                }
                                // Set the color from the
                                // color map
                                g.setColor(colors[buffer[j - 1][i - 1]]);
                                // Draw a pixel
                                g.drawLine(j - 1, i, j - 1, i);
                            }
                        }
                    }
                }
            }
        };
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setFocusable(false);
        b.setOpaque(false);
        return b;
    }

    int getBlockSize() {
        return EXT_BLOCKSIZE;
    }

    int getOneTouchOffset() {
        return EXT_ONE_TOUCH_OFFSET;
    }

    int getOneTouchSize() {
        return EXT_ONE_TOUCH_SIZE;
    }

    int getOrientationFromSuper() {
        return super.orientation;
    }

    JButton getLeftButtonFromSuper() {
        return super.leftButton;
    }

    JButton getRightButtonFromSuper() {
        return super.rightButton;
    }

    JSplitPane getSplitPaneFromSuper() {
        return super.splitPane;
    }

    public void paint(Graphics g) {
        if (splitPane.isOpaque()) {
            Color bgColor = splitPane.hasFocus()
                ? UIManager.getColor("SplitPane.shadow")
                : getBackground();

            if (bgColor != null) {
                g.setColor(bgColor);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
        super.paint(g);
    }

}