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

import java.awt.Adjustable;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalScrollBarUI;


/**
 * The JGoodies Plastic L&amp;F implementation of <code>ScrollBarUI</code>.
 * Can add a pseudo 3D effect and honors the Plastic Option
 * <tt>ScrollBar.maxBumpsWidth</tt> to limit the with of the scroll bar bumps.
 *
 * @author  Karsten Lentzsch
 * @version $Revision$
 */

public final class PlasticScrollBarUI extends MetalScrollBarUI {

	private static final String PROPERTY_PREFIX	  = "ScrollBar.";
	public  static final String MAX_BUMPS_WIDTH_KEY = PROPERTY_PREFIX + "maxBumpsWidth";

    private Color shadowColor;
    private Color highlightColor;
    private Color darkShadowColor;
    private Color thumbColor;
    private Color thumbShadow;
    private Color thumbHighlightColor;

    private PlasticBumps bumps;

	public static ComponentUI createUI(JComponent b) {
		return new PlasticScrollBarUI();
	}


    protected void installDefaults() {
    	super.installDefaults();
		bumps = new PlasticBumps(10, 10, thumbHighlightColor, thumbShadow, thumbColor);
    }


	protected JButton createDecreaseButton(int orientation) {
		decreaseButton = new PlasticArrowButton(orientation, scrollBarWidth, isFreeStanding);
		return decreaseButton;
	}


	protected JButton createIncreaseButton(int orientation) {
		increaseButton = new PlasticArrowButton(orientation, scrollBarWidth, isFreeStanding);
		return increaseButton;
	}


    protected void configureScrollBarColors() {
        super.configureScrollBarColors();
        shadowColor         = UIManager.getColor(PROPERTY_PREFIX + "shadow");
        highlightColor      = UIManager.getColor(PROPERTY_PREFIX + "highlight");
        darkShadowColor     = UIManager.getColor(PROPERTY_PREFIX + "darkShadow");
        thumbColor          = UIManager.getColor(PROPERTY_PREFIX + "thumb");
        thumbShadow         = UIManager.getColor(PROPERTY_PREFIX + "thumbShadow");
        thumbHighlightColor = UIManager.getColor(PROPERTY_PREFIX + "thumbHighlight");
    }


	protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
		g.translate(trackBounds.x, trackBounds.y);

		boolean leftToRight = PlasticUtils.isLeftToRight(c);

		if (scrollbar.getOrientation() == Adjustable.VERTICAL) {
			if (!isFreeStanding) {
				if (!leftToRight) {
					trackBounds.width += 1;
					g.translate(-1, 0);
				} else {
					trackBounds.width += 2;
				}
			}

			if (c.isEnabled()) {
				g.setColor(darkShadowColor);
				g.drawLine(0, 0, 0, trackBounds.height - 1);
				g.drawLine(trackBounds.width - 2, 0, trackBounds.width - 2, trackBounds.height - 1);
				g.drawLine(1, trackBounds.height - 1, trackBounds.width - 1, trackBounds.height - 1);
				g.drawLine(1, 0, trackBounds.width - 2, 0);

				g.setColor(shadowColor);
				//	g.setColor( Color.red);
				g.drawLine(1, 1, 1, trackBounds.height - 2);
				g.drawLine(1, 1, trackBounds.width - 3, 1);
				if (scrollbar.getValue() != scrollbar.getMaximum()) { // thumb shadow
					int y = thumbRect.y + thumbRect.height - trackBounds.y;
					g.drawLine(1, y, trackBounds.width - 1, y);
				}
				g.setColor(highlightColor);
				g.drawLine(trackBounds.width - 1, 0, trackBounds.width - 1, trackBounds.height - 1);
			} else {
				PlasticUtils.drawDisabledBorder(g, 0, 0, trackBounds.width, trackBounds.height);
			}

			if (!isFreeStanding) {
				if (!leftToRight) {
					trackBounds.width -= 1;
					g.translate(1, 0);
				} else {
					trackBounds.width -= 2;
				}
			}
		} else { // HORIZONTAL
			if (!isFreeStanding) {
				trackBounds.height += 2;
			}

			if (c.isEnabled()) {
				g.setColor(darkShadowColor);
				g.drawLine(0, 0, trackBounds.width - 1, 0); // top
				g.drawLine(0, 1, 0, trackBounds.height - 2); // left
				g.drawLine(0, trackBounds.height - 2, trackBounds.width - 1, trackBounds.height - 2);
				// bottom
				g.drawLine(trackBounds.width - 1, 1, trackBounds.width - 1, trackBounds.height - 1);

				// right
				g.setColor(shadowColor);
				//	g.setColor( Color.red);
				g.drawLine(1, 1, trackBounds.width - 2, 1); // top
				g.drawLine(1, 1, 1, trackBounds.height - 3); // left
				g.drawLine(0, trackBounds.height - 1, trackBounds.width - 1, trackBounds.height - 1);
				// bottom
				if (scrollbar.getValue() != scrollbar.getMaximum()) { // thumb shadow
					int x = thumbRect.x + thumbRect.width - trackBounds.x;
					g.drawLine(x, 1, x, trackBounds.height - 1);
				}
			} else {
				PlasticUtils.drawDisabledBorder(g, 0, 0, trackBounds.width, trackBounds.height);
			}

			if (!isFreeStanding) {
				trackBounds.height -= 2;
			}
		}
		g.translate(-trackBounds.x, -trackBounds.y);
	}


	protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
		if (!c.isEnabled()) {
			return;
		}

		boolean leftToRight = PlasticUtils.isLeftToRight(c);

		g.translate(thumbBounds.x, thumbBounds.y);

		if (scrollbar.getOrientation() == Adjustable.VERTICAL) {
			if (!isFreeStanding) {
				if (!leftToRight) {
					thumbBounds.width += 1;
					g.translate(-1, 0);
				} else {
					thumbBounds.width += 2;
				}

			}

			g.setColor(thumbColor);
			g.fillRect(0, 0, thumbBounds.width - 2, thumbBounds.height - 1);

			g.setColor(thumbShadow);
			g.drawRect(0, 0, thumbBounds.width - 2, thumbBounds.height - 1);

			g.setColor(thumbHighlightColor);
			g.drawLine(1, 1, thumbBounds.width - 3, 1);
			g.drawLine(1, 1, 1, thumbBounds.height - 2);

			paintBumps(g, c, 3, 4, thumbBounds.width - 6, thumbBounds.height - 7);

			if (!isFreeStanding) {
				if (!leftToRight) {
					thumbBounds.width -= 1;
					g.translate(1, 0);
				} else {
					thumbBounds.width -= 2;
				}
			}
		} else { // HORIZONTAL
			if (!isFreeStanding) {
				thumbBounds.height += 2;
			}

			g.setColor(thumbColor);
			g.fillRect(0, 0, thumbBounds.width - 1, thumbBounds.height - 2);

			g.setColor(thumbShadow);
			g.drawRect(0, 0, thumbBounds.width - 1, thumbBounds.height - 2);

			g.setColor(thumbHighlightColor);
			g.drawLine(1, 1, thumbBounds.width - 2, 1);
			g.drawLine(1, 1, 1, thumbBounds.height - 3);

			paintBumps(g, c, 4, 3, thumbBounds.width - 7, thumbBounds.height - 6);

			if (!isFreeStanding) {
				thumbBounds.height -= 2;
			}
		}
		g.translate(-thumbBounds.x, -thumbBounds.y);

		if (PlasticUtils.is3D(PROPERTY_PREFIX))
			paintThumb3D(g, thumbBounds);

	}


	private void paintBumps(Graphics g, JComponent c, int x, int y, int width, int height) {
		if (!useNarrowBumps()) {
			bumps.setBumpArea(width, height);
			bumps.paintIcon(c, g, x, y);
		} else {
			int maxWidth = UIManager.getInt(MAX_BUMPS_WIDTH_KEY);
	    	int myWidth  = Math.min(maxWidth, width);
    		int myHeight = Math.min(maxWidth, height);
    		int myX      = x + (width  - myWidth) / 2;
	    	int myY      = y + (height - myHeight) / 2;
			bumps.setBumpArea(myWidth, myHeight);
			bumps.paintIcon(c, g, myX, myY);
		}
	}


    private void paintThumb3D(Graphics g, Rectangle thumbBounds) {
		boolean isHorizontal = scrollbar.getOrientation() == Adjustable.HORIZONTAL;
		int width   = thumbBounds.width  - (isHorizontal ? 3 : 1);
		int height  = thumbBounds.height - (isHorizontal ? 1 : 3);
		Rectangle r = new Rectangle(thumbBounds.x + 2, thumbBounds.y + 2, width, height);
		PlasticUtils.addLight3DEffekt(g, r, isHorizontal);
	}


	// Accessing Special Client Properties **********************************************

	private boolean useNarrowBumps() {
		Object value = UIManager.get(MAX_BUMPS_WIDTH_KEY);
		return value != null && value instanceof Integer;
	}
}