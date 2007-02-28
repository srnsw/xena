
package com.softhub.ts;

/**
 * Copyright 1998 by Christian Lehner.
 *
 * This file is part of ToastScript.
 *
 * ToastScript is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ToastScript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ToastScript; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

import java.awt.*;
import javax.swing.border.AbstractBorder;

public class PageBorder extends AbstractBorder {

	private int shadowWidth = 4;
	private Color shadowColor = Color.black;
	private Color backgroundColor = Color.white;

    public PageBorder() {
    }

	public void setShadowWidth(int width) {
		this.shadowWidth = width;
	}

	public int getShadowWidth() {
		return shadowWidth;
	}

	public void setShadowColor(Color color) {
		this.shadowColor = color;
	}

	public Color getShadowColor() {
		return shadowColor;
	}

	public void setBackgroundColor(Color color) {
		this.backgroundColor = color;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void paintBorder(
		Component c, Graphics g, int x, int y, int width, int height)
	{
		Color currentColor = g.getColor();
		int rx = x + width - shadowWidth;
		int ry = y + height - shadowWidth;
		g.setColor(backgroundColor);
		g.fillRect(rx, y, shadowWidth, shadowWidth);
		g.fillRect(0, ry, shadowWidth, shadowWidth);
		g.setColor(shadowColor);
		g.drawLine(x, 0, rx, 0);
		g.drawLine(0, y, 0, ry);
		g.drawLine(rx, y, rx, y + shadowWidth);
		g.drawLine(0, ry, shadowWidth, ry);
		g.fillRect(rx, shadowWidth, shadowWidth, height);
		g.fillRect(shadowWidth, ry, width, shadowWidth);
		g.setColor(currentColor);
	}

	public Insets getBorderInsets(Component c) {
	    return new Insets(1, 1, shadowWidth+1, shadowWidth+1);
	}

	public Insets getBorderInsets(Component c, Insets insets) {
		insets.left = 1;
		insets.top = 1;
		insets.right = shadowWidth+1;
		insets.bottom = shadowWidth+1;
	    return insets;
	}

}
