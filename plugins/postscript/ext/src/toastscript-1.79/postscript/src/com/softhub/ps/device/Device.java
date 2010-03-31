
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
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import com.softhub.ps.graphics.Reusable;
import com.softhub.ps.device.Bitmap;
import com.softhub.ps.device.FontInfo;

public interface Device {

	/**
	 * Initialize the device. This method is called
	 * by the initgraphics operator.
	 */
	void init();

	/**
	 * @return the name of the device
	 */
	String getName();

	/**
	 * Save the state of the device. This method is
	 * called by the save operator.
	 * @return the state object
	 */
	Object save();

	/**
	 * Restore the device state from the state object
	 * returned by the save method.
	 * @param state the device state
	 */
	void restore(Object state);

	/**
	 * Create color object.
	 * @param r the red component
	 * @param g the green component
	 * @param b the blue component
	 * @return the RGB color
	 */
	Color createColor(float r, float g, float b);

	/**
	 * Create stroke object.
	 * @param width the width of the stroke
	 * @param cap the cap code
	 * @param join the join code
	 * @param miter the miter limit
	 * @param array the dash array
	 * @param phase the phase of the dash
	 * @return the stroke
	 */
	Stroke createStroke(float width, int cap, int join, float miter,
						float array[], float phase);

	/**
	 * Create bitmap object.
	 * @param width the width of the bitmap
	 * @param height the height of the bitmap
	 * @return the bitmap
	 */
	Bitmap createBitmap(int width, int height);

	/**
	 * @return the device matrix
	 */
	AffineTransform getDefaultMatrix();

	/**
	 * @return the device resolution
	 */
	float getResolution();

	/**
	 * @return the device scale factor
	 */
	float getScale();

	/**
	 * @return the device dimensions in pixels
	 */
	Dimension getSize();

	/**
	 * @param color the current color
	 */
	void setColor(Color color);

	/**
	 * @param stroke the current stroke
	 */
	void setStroke(Stroke stroke);

	/**
	 * @param paint the current paint
	 */
	void setPaint(Paint paint);

	/**
	 * @param font the current font
	 */
	void setFont(FontInfo font);

	/**
	 * @param obj the cached character representation
	 * @param xform the transformation matrix
	 */
	void show(Reusable obj, AffineTransform xform);

	/**
	 * @param bitmap the image representation
	 * @param xform the transformation matrix
	 */
	void image(Bitmap bitmap, AffineTransform xform);

	/**
	 * @param shape the shape to fill
	 */
	void fill(Shape shape);

	/**
	 * @param shape the shape to stroke
	 */
	void stroke(Shape shape);

	/**
	 * Initialize the clip rectangle to the device
	 * boundaries.
	 */
	void initclip();

	/**
	 * @param shape the shape to clip to
	 */
	void clip(Shape shape);

	/**
	 * @return the current clip shape
	 */
	Shape clippath();

	/**
	 * Show the current page and erase it.
	 */
	void showpage();

	/**
	 * Copy and show the current page.
	 */
	void copypage();

	/**
	 * Erase the current page.
	 */
	void erasepage();

	/**
	 * Called by jobserver to indicate that
	 * a save/restore encapsulated job begins.
	 */
	void beginJob();

	/**
	 * Called by jobserver to indicate that
	 * the current job is done.
	 */
	void endJob();

	/**
	 * Error in job.
	 * @param msg the error message
	 */
	void error(String msg);

	/**
	 * Convert device property.
	 * @param name the name of the property
	 * @param value the value of the property
	 */
	Object convertType(String name, Object value);

}
