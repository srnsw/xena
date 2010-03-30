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
import java.awt.Rectangle;

import javax.swing.ButtonModel;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalScrollButton;

/**
 * Renders the arrow buttons in scroll bars and spinners.
 *
 * @author Karsten Lentzsch
 * @version $Revision$
 */
class PlasticArrowButton extends MetalScrollButton {

	private final Color shadowColor;
	private final Color highlightColor;

	protected boolean isFreeStanding;


	public PlasticArrowButton(int direction, int width, boolean freeStanding) {
		super(direction, width, freeStanding);
	    shadowColor    = UIManager.getColor("ScrollBar.darkShadow");
	    highlightColor = UIManager.getColor("ScrollBar.highlight");
		isFreeStanding = freeStanding;
	}


    public void setFreeStanding(boolean freeStanding) {
    	super.setFreeStanding(freeStanding);
	    isFreeStanding = freeStanding;
    }


	public void paint(Graphics g) {
		boolean leftToRight = PlasticUtils.isLeftToRight(this);
		boolean isEnabled   = getParent().isEnabled();
		boolean isPressed   = getModel().isPressed();

		Color arrowColor = isEnabled
				? PlasticLookAndFeel.getControlInfo()
				: PlasticLookAndFeel.getControlDisabled();
		int width  = getWidth();
		int height = getHeight();
		int w = width;
		int h = height;
        int arrowHeight = calculateArrowHeight(height, width);
        int arrowOffset = calculateArrowOffset();
        boolean paintNorthBottom = isPaintingNorthBottom();

		g.setColor(isPressed ? PlasticLookAndFeel.getControlShadow() : getBackground());
		g.fillRect(0, 0, width, height);

		if (getDirection() == NORTH) {
			paintNorth(g, leftToRight, isEnabled, arrowColor, isPressed,
				width, height, w, h, arrowHeight, arrowOffset, paintNorthBottom);
		} else if (getDirection() == SOUTH) {
			paintSouth(g, leftToRight, isEnabled, arrowColor, isPressed,
				width, height, w, h, arrowHeight, arrowOffset);
		} else if (getDirection() == EAST) {
			paintEast(g, isEnabled, arrowColor, isPressed,
				width, height, w, h, arrowHeight);
		} else if (getDirection() == WEST) {
			paintWest(g, isEnabled, arrowColor, isPressed,
				width, height, w, h, arrowHeight);
		}
		if (PlasticUtils.is3D("ScrollBar."))
			paint3D(g);
	}

    /**
     * Computes and returns the arrow height based on the specified
     * buttons height and width.
     *
     * @param height the height of the button to be used for calculation.
     * @param width the width of the button to be used for calculation.
     * @return the height of the arrow
     */
    protected int calculateArrowHeight(int height, int width) {
        return (height + 1) / 4;
    }

    protected int calculateArrowOffset() {
        return 0;
    }

    protected boolean isPaintingNorthBottom() {
        return false;
    }


	private void paintWest(Graphics g, boolean isEnabled, Color arrowColor,
		boolean isPressed, int width, int height, int w, int h, int arrowHeight) {

		if (!isFreeStanding) {
			height += 2;
			width  += 1;
			g.translate(-1, 0);
		}

		// Draw the arrow
		g.setColor(arrowColor);

		int startX = (((w + 1) - arrowHeight) / 2);
		int startY = (h / 2);

		for (int line = 0; line < arrowHeight; line++) {
			g.drawLine(
				startX + line,
				startY - line,
				startX + line,
				startY + line + 1);
		}

		if (isEnabled) {
			g.setColor(highlightColor);

			if (!isPressed) {
				g.drawLine(1, 1, width - 1, 1);
				g.drawLine(1, 1, 1, height - 3);
			}
			g.drawLine(1, height - 1, width - 1, height - 1);

			g.setColor(shadowColor);
			g.drawLine(0, 0, width - 1, 0);
			g.drawLine(0, 0, 0, height - 2);
			g.drawLine(1, height - 2, width - 1, height - 2);
		} else {
			PlasticUtils.drawDisabledBorder(g, 0, 0, width + 1, height);
		}

		if (!isFreeStanding) {
			height -= 2;
			width  -= 1;
			g.translate(1, 0);
		}
	}


	private void paintEast(Graphics g, boolean isEnabled, Color arrowColor,
		boolean isPressed, int width, int height, int w, int h, int arrowHeight) {
		if (!isFreeStanding) {
			height += 2;
			width  += 1;
		}

		// Draw the arrow
		g.setColor(arrowColor);

		int startX = (((w + 1) - arrowHeight) / 2) + arrowHeight - 1;
		int startY = (h / 2);
		for (int line = 0; line < arrowHeight; line++) {
			g.drawLine(
				startX - line,
				startY - line,
				startX - line,
				startY + line + 1);
		}

		if (isEnabled) {
			g.setColor(highlightColor);
			if (!isPressed) {
				g.drawLine(0, 1, width - 3, 1);
				g.drawLine(0, 1, 0, height - 3);
			}
			g.drawLine(width - 1, 1, width - 1, height - 1);
			g.drawLine(0, height - 1, width - 1, height - 1);

			g.setColor(shadowColor);
			g.drawLine(0, 0, width - 2, 0);
			g.drawLine(width - 2, 1, width - 2, height - 2);
			g.drawLine(0, height - 2, width - 2, height - 2);
		} else {
			PlasticUtils.drawDisabledBorder(g, -1, 0, width + 1, height);
		}
		if (!isFreeStanding) {
			height -= 2;
			width  -= 1;
		}
	}


	protected void paintSouth(Graphics g, boolean leftToRight, boolean isEnabled,
		Color arrowColor, boolean isPressed,
        int width, int height, int w, int h, int arrowHeight, int arrowOffset) {

		if (!isFreeStanding) {
			height += 1;
			if (!leftToRight) {
				width += 1;
				g.translate(-1, 0);
			} else {
				width += 2;
			}
		}

		// Draw the arrow
		g.setColor(arrowColor);

		int startY = (((h + 0) - arrowHeight) / 2) + arrowHeight - 1; // KL was h + 1
		int startX = w / 2;

		//	    System.out.println( "startX2 :" + startX + " startY2 :"+startY);

		for (int line = 0; line < arrowHeight; line++) {
            g.fillRect(startX - line - arrowOffset, startY - line, 2 * (line + 1), 1);
		}

		if (isEnabled) {
			g.setColor(highlightColor);
			if (!isPressed) {
				g.drawLine(1, 0, width - 3, 0);
				g.drawLine(1, 0, 1, height - 3);
			}
			g.drawLine(0, height - 1, width - 1, height - 1);
			g.drawLine(width - 1, 0, width - 1, height - 1);

			g.setColor(shadowColor);
			g.drawLine(0, 0, 0, height - 2);
			g.drawLine(width - 2, 0, width - 2, height - 2);
			g.drawLine(1, height - 2, width - 2, height - 2);
		} else {
			PlasticUtils.drawDisabledBorder(g, 0, -1, width, height + 1);
		}

		if (!isFreeStanding) {
			height -= 1;
			if (!leftToRight) {
				width -= 1;
				g.translate(1, 0);
			} else {
				width -= 2;
			}
		}
	}


	protected void paintNorth(Graphics g, boolean leftToRight, boolean isEnabled,
		Color arrowColor, boolean isPressed,
		int width, int height, int w, int h, int arrowHeight, int arrowOffset,
        boolean paintBottom) {
		if (!isFreeStanding) {
			height += 1;
			g.translate(0, -1);
			if (!leftToRight) {
				width += 1;
				g.translate(-1, 0);
			} else {
				width += 2;
			}
		}

		// Draw the arrow
		g.setColor(arrowColor);
		int startY = ((h + 1) - arrowHeight) / 2;  // KL was (h + 1)
		int startX = w / 2;
		// System.out.println( "startX :" + startX + " startY :"+startY);
		for (int line = 0; line < arrowHeight; line++) {
            g.fillRect(startX - line - arrowOffset, startY + line, 2*(line + 1), 1);
		}

		if (isEnabled) {
			g.setColor(highlightColor);

			if (!isPressed) {
				g.drawLine(1, 1, width - 3, 1);
				g.drawLine(1, 1, 1, height - 1);
			}

			g.drawLine(width - 1, 1, width - 1, height - 1);

			g.setColor(shadowColor);
			g.drawLine(0, 0, width - 2, 0);
			g.drawLine(0, 0, 0, height - 1);
			g.drawLine(width - 2, 1, width - 2, height - 1);
            if (paintBottom) {
                g.fillRect(0, height - 1, width - 1, 1);
            }
		} else {
			PlasticUtils.drawDisabledBorder(g, 0, 0, width, height + 1);
            if (paintBottom) {
                g.setColor(PlasticLookAndFeel.getControlShadow());
                g.fillRect(0, height - 1, width - 1, 1);
            }
		}
		if (!isFreeStanding) {
			height -= 1;
			g.translate(0, 1);
			if (!leftToRight) {
				width -= 1;
				g.translate(1, 0);
			} else {
				width -= 2;
			}
		}
	}


	private void paint3D(Graphics g) {
		ButtonModel buttonModel = getModel();
		if (buttonModel.isArmed() && buttonModel.isPressed() || buttonModel.isSelected())
			return;

		int width  = getWidth();
		int height = getHeight();
		if (getDirection() == EAST)
			width -= 2;
		else if (getDirection() == SOUTH)
			height -= 2;

		Rectangle r = new Rectangle(1, 1, width, height);
		boolean isHorizontal = (getDirection() == EAST || getDirection() == WEST);
		PlasticUtils.addLight3DEffekt(g, r, isHorizontal);
	}
}