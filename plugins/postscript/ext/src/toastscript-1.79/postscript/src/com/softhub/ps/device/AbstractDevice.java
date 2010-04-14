
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
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.*;
import com.softhub.ps.graphics.Reusable;
import com.softhub.ps.device.Bitmap;
import com.softhub.ps.device.DefaultBitmap;
import com.softhub.ps.device.FontInfo;

public abstract class AbstractDevice implements Device, BeanInfo {

	/**
	 * The device transformation.
	 */
	protected AffineTransform dtm = new AffineTransform(1,0,0,1,0,0);

	/**
	 * The graphics context for clipping.
	 */
	protected Graphics2D clipGraphics;

	/**
	 * Initialize the device.
	 */
	public void init() {
		initClipGraphics();
	}

	protected void initClipGraphics() {
		BufferedImage image = createClipImage();
		clipGraphics = image.createGraphics();
	}

	/**
	 * Save the device state.
	 * @param gstate the graphics state
	 */
	public Object save() {
		return new State();
	}

	/**
	 * Restore the device state.
	 * @param gstate the graphics state
	 */
	public void restore(Object state) {
		((State) state).restore();
	}

	/**
	 * @return the device name
	 */
	public String getName() {
		return getClass().getName();
	}

	/**
	 * Create RGB color.
	 * @param r the red component
	 * @param g the green component
	 * @param b the blue component
	 * @return the color
	 */
	public Color createColor(float r, float g, float b) {
		return new Color(r, g, b);
	}

	/**
	 * Create stroke.
	 * @param width the stroke width
	 * @param cap the stroke cap index
	 * @param join the stroke join index
	 * @param miter the miter limit
	 * @param array the dash array
	 * @param phase the dash phase
	 * @return the stroke
	 */
	public Stroke createStroke(
		float width, int cap, int join, float miter, float array[], float phase)
	{
		float widthlimit = Math.max(0.001f, width);
		float miterlimit = Math.max(1, miter);
		return new BasicStroke(widthlimit, cap, join, miterlimit, array, phase);
	}

	/**
	 * Create a bitmap.
	 * @param width the width of the bitmap
	 * @param height the height of the bitmap
	 * @return a bitmap of specified size
	 */
	public Bitmap createBitmap(int width, int height) {
		return new DefaultBitmap(width, height);
	}

	/**
	 * @return a buffered image the size of the device for clipping
	 */
	protected BufferedImage createClipImage() {
		Dimension d = getSize();
		int w = Math.max(1, d.width);
		int h = Math.max(1, d.height);
		return new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
	}

	protected Shape getClip() {
		Shape shape = clipGraphics.getClip();
		return shape != null ? shape : cliprect();
	}

	/**
	 * @return the clipping boundary
	 */
	protected abstract Rectangle2D cliprect();

	/**
	 * Initialize the clip bounds.
	 */
	public void initclip() {
		clipGraphics.setClip(cliprect());
	}

	/**
	 * Clip to some path boundary.
	 * @param path the clip path
	 */
	public void clip(Shape shape) {
		clipGraphics.clip(shape);
	}

	/**
	 * @return the current clip shape
	 */
	public Shape clippath() {
		Shape shape = clipGraphics.getClip();
		return shape != null ? shape : cliprect();
	}

	/**
	 * Set the default matrix.
	 * @param xform the default matrix
	 */
	public void setDefaultMatrix(AffineTransform xform) {
		dtm.setTransform(xform);
	}

	/**
	 * @return the default matrix
	 */
	public AffineTransform getDefaultMatrix() {
		return dtm;
	}

	/**
	 * Set the current color.
	 * @param color the new color
	 */
	public void setColor(Color color) {
	}

	/**
	 * Set the current line stroke.
	 * @param stroke the pen to use
	 */
	public void setStroke(Stroke stroke) {
	}

	/**
	 * Set the current paint.
	 * @param paint the paint to use
	 */
	public void setPaint(Paint paint) {
	}

	/**
	 * Set the current font.
	 * @param info the font info
	 */
	public void setFont(FontInfo info) {
	}

	/**
	 * Show some cached object.
	 * @param obj the reusable object
	 * @param xform the tranformation matrix
	 */
	public void show(Reusable obj, AffineTransform xform) {
	}

	/**
	 * Draw an image.
	 * @param bitmap the bitmap to draw
	 * @param xform the image transformation
	 */
	public void image(Bitmap bitmap, AffineTransform xform) {
	}

	/**
	 * Fill a path.
	 * @param path the path to fill
	 */
	public void fill(Shape shape) {
	}

	/**
	 * Stoke a path using the settings of the current stroke.
	 * @param path the path to stroke
	 */
	public void stroke(Shape shape) {
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

	/**
	 * @return bean descriptor for this device
	 */
	public BeanDescriptor getBeanDescriptor() {
		return null;
	}

	/**
	 * @return property descriptors for this device
	 */
	public PropertyDescriptor[] getPropertyDescriptors() {
		return null;
	}

	public int getDefaultPropertyIndex() {
		return -1;
	}

	public EventSetDescriptor[] getEventSetDescriptors() {
		return null;
	}

	public int getDefaultEventIndex() {
		return -1;
	}

	public MethodDescriptor[] getMethodDescriptors() {
		return null;
	}

	public BeanInfo[] getAdditionalBeanInfo() {
		return null;
	}

	public Image getIcon(int iconKind) {
		return null;
	}

	public Image loadImage(final String resourceName) {
		return null;
	}

	protected class State {

		protected Shape clipShape;

		protected State() {
			clipShape = getClip();
		}

		protected void restore() {
			clipGraphics.setClip(clipShape);
		}

	}

}
