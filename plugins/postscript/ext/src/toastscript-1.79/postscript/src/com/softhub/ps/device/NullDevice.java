
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import com.softhub.ps.device.Bitmap;
import com.softhub.ps.device.FontInfo;
import com.softhub.ps.device.NullBitmap;
import com.softhub.ps.graphics.Reusable;

public class NullDevice implements Device {

	/**
	 * Initialize the device. This method is called
	 * by the initgraphics operator.
	 */
	public void init() {
	}

	/**
	 * @return the name of the device
	 */
	public String getName() {
		return "nulldevice";
	}

	/**
	 * Save the state of the device. This method is
	 * called by the save operator.
	 * @return the state object
	 */
	public Object save() {
		return null;
	}

	/**
	 * Restore the device state from the state object
	 * returned by the save method.
	 * @param state the device state
	 */
	public void restore(Object state) {
	}

	/**
	 * Create color object.
	 * @param r the red component
	 * @param g the green component
	 * @param b the blue component
	 * @return the RGB color
	 */
	public Color createColor(float r, float g, float b) {
		return Color.black;
	}

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
	public Stroke createStroke(
		float width, int cap, int join, float miter, float array[], float phase
	) {
		return new BasicStroke();
	}

	/**
	 * Create bitmap object.
	 * @param width the width of the bitmap
	 * @param height the height of the bitmap
	 * @return the bitmap
	 */
	public Bitmap createBitmap(int width, int height) {
		return new NullBitmap(width, height);
	}

	/**
	 * @return the device matrix
	 */
	public AffineTransform getDefaultMatrix() {
		return new AffineTransform();
	}

	/**
	 * @return the device resolution
	 */
	public float getResolution() {
		return 0;
	}

	/**
	 * @return the device scale factor
	 */
	public float getScale() {
		return 0;
	}

	/**
	 * @return the device dimensions in pixels
	 */
	public Dimension getSize() {
		return new Dimension(0,0);
	}

	/**
	 * @param color the current color
	 */
	public void setColor(Color color) {
	}

	/**
	 * @param stroke the current stroke
	 */
	public void setStroke(Stroke stroke) {
	}

	/**
	 * @param paint the current paint
	 */
	public void setPaint(Paint paint) {
	}

	/**
	 * @param font the current font
	 */
	public void setFont(FontInfo info) {
	}

	/**
	 * @param obj the cached character representation
	 * @param xform the transformation matrix
	 */
	public void show(Reusable obj, AffineTransform xform) {
	}

	/**
	 * @param bitmap the image representation
	 * @param xform the transformation matrix
	 */
	public void image(Bitmap bitmap, AffineTransform xform) {
	}

	/**
	 * @param shape the shape to fill
	 */
	public void fill(Shape shape) {
	}

	/**
	 * @param shape the shape to stroke
	 */
	public void stroke(Shape shape) {
	}

	/**
	 * Initialize the clip rectangle to the device
	 * boundaries.
	 */
	public void initclip() {
	}

	/**
	 * @param shape the shape to clip to
	 */
	public void clip(Shape shape) {
	}

	/**
	 * @return the current clip shape
	 */
	public Shape clippath() {
		return new Rectangle2D.Float(0,0,0,0);
	}

	/**
	 * Show the current page and erase it.
	 */
	public void showpage() {
	}

	/**
	 * Copy and show the current page.
	 */
	public void copypage() {
	}

	/**
	 * Erase the current page.
	 */
	public void erasepage() {
	}

	/**
	 * Called by jobserver to indicate that
	 * a save/restore encapsulated job begins.
	 */
	public void beginJob() {
	}

	/**
	 * Called by jobserver to indicate that
	 * the current job is done.
	 */
	public void endJob() {
	}

	/**
	 * Error in job.
	 * @param msg the error message
	 */
	public void error(String msg) {
	}

	/**
	 * Convert device property.
	 * @param name the name of the property
	 * @param value the value of the property
	 */
	public Object convertType(String name, Object value) {
		return value;
	}

}
