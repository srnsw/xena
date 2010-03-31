
package com.softhub.ps.graphics;

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

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import com.softhub.ps.device.Bitmap;

class ImageCommand extends Command {

	private Bitmap bitmap;
	private Rectangle2D bounds;
	private AffineTransform xform;

	ImageCommand(Bitmap bitmap, AffineTransform xform) {
		this.bitmap = bitmap;
		this.xform = xform;
	}

	void exec(Graphics2D g) {
		bitmap.drawImage(g, xform);
	}

	void transform(AffineTransform xform) {
		this.xform.preConcatenate(xform);
	}

	Rectangle2D getBounds2D() {
		if (bounds == null) {
			double x = xform.getTranslateX();
			double y = xform.getTranslateY();
			int w = bitmap.getWidth();
			int h = bitmap.getHeight();
			Point2D v = new Point2D.Double(w, h);
			xform.deltaTransform(v, v);
			bounds = new Rectangle2D.Double(x, y, v.getX(), v.getY());
		}
		return bounds;
	}

}
