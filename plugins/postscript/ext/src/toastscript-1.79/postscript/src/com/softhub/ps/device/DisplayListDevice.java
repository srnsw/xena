
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
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import com.softhub.ps.device.Bitmap;
import com.softhub.ps.graphics.DisplayList;
import com.softhub.ps.graphics.Reusable;

public abstract class DisplayListDevice extends AbstractDevice {

	/**
	 * The current display list.
	 */
	protected DisplayList displayList;

	/**
	 * Initialize the device.
	 */
	public void init() {
		this.displayList = createDisplayList();
		super.init();
	}

	/**
	 * Save the device state.
	 * @param gstate the graphics state
	 */
	public Object save() {
		return new DisplayState();
	}

	/**
	 * Restore the device state.
	 * @param gstate the graphics state
	 */
	public void restore(Object state) {
		((DisplayState) state).restore();
	}

	/**
	 * Initialize the clip bounds.
	 */
	public void initclip() {
		super.initclip();
		displayList.initclip();
	}

	/**
	 * Clip to some path boundary.
	 * @param path the clip path
	 */
	public void clip(Shape shape) {
		super.clip(shape);
		displayList.clip(shape);
	}

	/**
	 * Set the current color.
	 * @param color the new color
	 */
	public void setColor(Color color) {
		displayList.setColor(color);
	}

	/**
	 * Set the current line stroke.
	 * @param stroke the pen to use
	 */
	public void setStroke(Stroke stroke) {
		displayList.setStroke(stroke);
	}

	/**
	 * Set the current paint.
	 * @param paint the paint to use
	 */
	public void setPaint(Paint paint) {
		displayList.setPaint(paint);
	}

	/**
	 * Show some cached object.
	 * @param obj the reusable object
	 * @param xform the tranformation matrix
	 */
	public void show(Reusable obj, AffineTransform xform) {
		displayList.show(obj, xform);
	}

	/**
	 * Draw an image.
	 * @param bitmap the bitmap to draw
	 * @param xform the image transformation
	 */
	public void image(Bitmap bitmap, AffineTransform xform) {
		displayList.image(bitmap, xform);
	}

	/**
	 * Fill a shape.
	 * @param shape the shape to fill
	 */
	public void fill(Shape shape) {
		displayList.fill(shape);
	}

	/**
	 * Stoke a shape using the settings of the current stroke.
	 * @param shape the shape to stroke
	 */
	public void stroke(Shape shape) {
		displayList.stroke(shape);
	}

	/**
	 * @return a newly created display list
	 */
	protected DisplayList createDisplayList() {
		return new DisplayList();
	}

	protected class DisplayState extends State {

		private Color color;
		private Paint paint;
		private Stroke stroke;

		protected DisplayState() {
			color = displayList.getColor();
			paint = displayList.getPaint();
			stroke = displayList.getStroke();
		}

		protected void restore() {
			if (color != null) {
			    displayList.setColor(color);
			}
			if (paint != null) {
			    displayList.setPaint(paint);
			}
			if (stroke != null) {
			    displayList.setStroke(stroke);
			}
			displayList.clip(clipShape);
			super.restore();
		}

	}

}
