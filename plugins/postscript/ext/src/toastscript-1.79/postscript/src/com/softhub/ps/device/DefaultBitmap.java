
package com.softhub.ps.device;

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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class DefaultBitmap implements Bitmap {

	private BufferedImage image;

	public DefaultBitmap(int w, int h) {
		this(w, h, BufferedImage.TYPE_INT_ARGB);
	}

	public DefaultBitmap(int w, int h, int type) {
		image = new BufferedImage(Math.max(1, w), Math.max(1, h), type);
	}

	public int getWidth() {
		return image.getWidth();
	}

	public int getHeight() {
		return image.getHeight();
	}

	public void draw(int x, int y, int color) {
		image.setRGB(x, y, color);
	}

	public void draw(int x, int y, Color color) {
		draw(x, y, color.getRGB());
	}

	public void drawImage(Graphics2D g, AffineTransform xform) {
		g.drawImage(image, xform, null);
	}

	public Image getImage() {
		return image;
	}

}
