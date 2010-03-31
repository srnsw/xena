
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
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import com.softhub.ps.util.CharWidth;

public interface Reusable {

	/**
	 * Draw the object.
	 *	@param g the graphics context
	 */
	void draw(Graphics2D g);

	/**
	 * Normalize the object.
	 * @param xform a transformation matrix
	 */
	void normalize(AffineTransform xform) throws NoninvertibleTransformException;

	/**
	 * @return true if this object is resolution dependent
	 */
	boolean isResolutionDependent();

	/**
	 * @return the bounding box
	 */
	Rectangle2D getBounds2D();

	/**
	 * @return the character code or null if not a character
	 */
	String getCharCode();

	/**
	 * @return the character width or null if not a character
	 */
	CharWidth getCharWidth();

}
