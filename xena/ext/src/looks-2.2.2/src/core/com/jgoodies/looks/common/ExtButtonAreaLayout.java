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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.plaf.basic.BasicOptionPaneUI;

import com.jgoodies.looks.LookUtils;

/**
 * Unlike its superclass, this layout uses a minimum button width
 * that complies with Mac and Windows UI style guides.
 *
 * @author  Karsten Lentzsch
 * @version $Revision$
 */

public final class ExtButtonAreaLayout
    extends BasicOptionPaneUI.ButtonAreaLayout {

    /**
     * Constructs an <code>ExtButtonAreaLayout</code>.
     *
     * @param syncAllWidths  true indicates that all buttons get the same size
     * @param padding        the padding between buttons
     */
    public ExtButtonAreaLayout(boolean syncAllWidths, int padding) {
        super(syncAllWidths, padding);
    }

    public void layoutContainer(Container container) {
        Component[] children = container.getComponents();

        if (children != null && children.length > 0) {
            int numChildren = children.length;
            Dimension[] sizes = new Dimension[numChildren];
            int counter;
            int yLocation = container.getInsets().top;

            if (syncAllWidths) {
                int maxWidth = getMinimumButtonWidth();

                for (counter = 0; counter < numChildren; counter++) {
                    sizes[counter] = children[counter].getPreferredSize();
                    maxWidth = Math.max(maxWidth, sizes[counter].width);
                }

                int xLocation;
                int xOffset;

                if (getCentersChildren()) {
                    xLocation =
                        (container.getSize().width
                            - (maxWidth * numChildren
                                + (numChildren - 1) * padding))
                            / 2;
                    xOffset = padding + maxWidth;
                } else {
                    if (numChildren > 1) {
                        xLocation = 0;
                        xOffset =
                            (container.getSize().width
                                - (maxWidth * numChildren))
                                / (numChildren - 1)
                                + maxWidth;
                    } else {
                        xLocation = (container.getSize().width - maxWidth) / 2;
                        xOffset = 0;
                    }
                }
                boolean ltr = container.getComponentOrientation()
                                       .isLeftToRight();
                for (counter = 0; counter < numChildren; counter++) {
                    int index = (ltr)
                              ? counter
                              : numChildren - counter - 1;
                    children[index].setBounds(
                        xLocation,
                        yLocation,
                        maxWidth,
                        sizes[index].height);
                    xLocation += xOffset;
                }
            } else {
                int totalWidth = 0;

                for (counter = 0; counter < numChildren; counter++) {
                    sizes[counter] = children[counter].getPreferredSize();
                    totalWidth += sizes[counter].width;
                }
                totalWidth += ((numChildren - 1) * padding);

                boolean cc = getCentersChildren();
                int xOffset;
                int xLocation;

                if (cc) {
                    xLocation = (container.getSize().width - totalWidth) / 2;
                    xOffset = padding;
                } else {
                    if (numChildren > 1) {
                        xOffset =
                            (container.getSize().width - totalWidth)
                                / (numChildren - 1);
                        xLocation = 0;
                    } else {
                        xLocation =
                            (container.getSize().width - totalWidth) / 2;
                        xOffset = 0;
                    }
                }

                boolean ltr = container.getComponentOrientation()
                                       .isLeftToRight();
                for (counter = 0; counter < numChildren; counter++) {
                    int index = (ltr)
                              ? counter
                              : numChildren - counter - 1;
                    children[index].setBounds(
                        xLocation,
                        yLocation,
                        sizes[index].width,
                        sizes[index].height);
                    xLocation += xOffset + sizes[index].width;
                }
            }
        }
    }

    public Dimension minimumLayoutSize(Container c) {
        if (c != null) {
            Component[] children = c.getComponents();

            if (children != null && children.length > 0) {
                Dimension aSize;
                int numChildren = children.length;
                int height = 0;
                Insets cInsets = c.getInsets();
                int extraHeight = cInsets.top + cInsets.bottom;

                if (syncAllWidths) {
                    int maxWidth = getMinimumButtonWidth();

                    for (int counter = 0; counter < numChildren; counter++) {
                        aSize = children[counter].getPreferredSize();
                        height = Math.max(height, aSize.height);
                        maxWidth = Math.max(maxWidth, aSize.width);
                    }
                    return new Dimension(
                        maxWidth * numChildren + (numChildren - 1) * padding,
                        extraHeight + height);
                }
                int totalWidth = 0;

                for (int counter = 0; counter < numChildren; counter++) {
                    aSize = children[counter].getPreferredSize();
                    height = Math.max(height, aSize.height);
                    totalWidth += aSize.width;
                }
                totalWidth += ((numChildren - 1) * padding);
                return new Dimension(totalWidth, extraHeight + height);
            }
        }
        return new Dimension(0, 0);
    }

    /**
     * Computes and answers the minimum button width.
     * The MS UX guide recommends a minimum width of 50 Dialog units (DLU).<p>
     *
     * This current implementation assumes an 8pt Tahoma and honors resolutions
     * of 96dpi and 120dpi. This leads to a good approximation of the 50dlu
     * for the vast majority of today's target systems. And it sure is
     * an improvement over the superclass' value of 0.<p>
     *
     * A better implementation would use a conversion from dlu to pixel
     * for the given buttons (that may have different font render contexts
     * if located in different graphics environments in a multi-screen context).
     * The JGoodies Forms provides such a converter and offers a better button
     * layout by means of the {@code ButtonBarBuilder2} class.
     *
     * @return the minimum button width
     */
    private int getMinimumButtonWidth() {
        return LookUtils.IS_LOW_RESOLUTION ? 75 : 100;
    }

}