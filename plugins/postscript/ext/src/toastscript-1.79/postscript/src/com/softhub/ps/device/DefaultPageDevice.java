
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

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.beans.*;
import java.util.*;
import com.softhub.ps.graphics.Drawable;

public class DefaultPageDevice extends DisplayListDevice
	implements PageDevice
{
	/**
	 * Vector of page event listeners.
	 */
	private Vector listeners = new Vector();

	/**
	 * The width of the page in 1/72 of an inch.
	 */
	private float width;

	/**
	 * The height of the page in 1/72 of an inch.
	 */
	private float height;

	/**
	 * The scale factor.
	 */
	private float scale;

	/**
	 * The screen resulution.
	 */
	private float dpi;

	/**
	 * The page orientation.
	 */
	private int orientation;

	/**
	 * The number of copies.
	 */
	private int copies = 1;

	/**
	 * The bitmap type.
	 */
	private int bitmapType = BufferedImage.TYPE_INT_ARGB;

	/**
	 * True if size has changed. Cleared in init method.
	 */
	private boolean sizeHasChanged;

	/**
	 * Create a new page device.
	 */
	public DefaultPageDevice() {
		this(612, 792, 1, Toolkit.getDefaultToolkit().getScreenResolution());
	}

	/**
	 * Create a new page device.
	 * @param width the width in 1/72 inch
	 * @param height the height in 1/72 inch
	 * @param scale the scale factor
	 * @param dpi the output resolution
	 */
	public DefaultPageDevice(float width, float height, float scale, float dpi) {
		this.width = width;
		this.height = height;
		this.scale = scale;
		this.dpi = dpi;
	}

	/**
	 * Initialize the device.
	 */
	public void init() {
		initMatrix();
		super.init();
		if (sizeHasChanged) {
			firePageEvent(PageEvent.RESIZE);
			sizeHasChanged = false;
		}
	}

	/**
	 * Initialize the device matrix.
	 */
	private void initMatrix() {
		float s = scale * dpi / 72;
		switch (orientation) {
		case 0:
		    setDefaultMatrix(new AffineTransform(s, 0, 0, -s, 0, height * s));
			break;
		case 1:
		    setDefaultMatrix(new AffineTransform(0, s, s, 0, 0, 0));
			break;
		}
	}

	/**
	 * @return the name of the device
	 */
	public String getName() {
		return "pagedevice";
	}

	/**
	 * @return the resolution in dots per inch
	 */
	public float getResolution() {
		return dpi;
	}

	/**
	 * Set the page size.
	 * @param size array of 2 elements {width, height}
	 */
	public void setPageSize(float size[/* 2 */]) {
		width = size[0];
		height = size[1];
		sizeHasChanged = true;
	}

	/**
	 * Get the page size.
	 * @return size array of 2 elements {width, height}
	 */
	public float[/* 2 */] getPageSize() {
		float size[] = new float[2];
		size[0] = width;
		size[1] = height;
		return size;
	}

	/**
	 * Set the scale factor.
	 * @param the absolute scale factor in percent
	 */
	public void setScale(float scale) {
		this.scale = scale;
		sizeHasChanged = true;
	}

	/**
	 * @return the scale as a percentage
	 */
	public float getScale() {
		return scale;
	}

	/**
	 * Set the number of copies for showpage to print.
	 * @param the number of copies
	 */
	public void setNumCopies(int copies) {
		this.copies = copies;
	}

	/**
	 * @return the number of copies
	 */
	public int getNumCopies() {
		return copies;
	}

	/**
	 * Set the page orientation.
	 * @param mode [0..3]
	 */
	public void setOrientation(int orientation) {
		this.orientation = orientation;
		sizeHasChanged = true;
	}

	/**
	 * Get the page orientation.
	 * @return orientation [0..3]
	 */
	public int getOrientation() {
		return orientation;
	}

	/**
	 * Set the type of bitmap used by this device.
	 * The constants are declared in BufferedImage.java
	 * @param the bitmap type
	 */
	public void setBitmapType(int type) {
		this.bitmapType = type;
	}

	/**
	 * Get the type of bitmap used by this device.
	 * The constants are declared in BufferedImage.java
	 * @return the bitmap type
	 */
	public int getBitmapType() {
		return bitmapType;
	}

	/**
	 * @return the page width in 1/72 inch
	 */
	public float getPageWidth() {
		return width;
	}

	/**
	 * @return the page height in 1/72 inch
	 */
	public float getPageHeight() {
		return height;
	}

	/**
	 * @return the current page content
	 */
	public Drawable getContent() {
		return displayList;
	}

	/**
	 * Show the current page and erase it.
	 */
	public void showpage() {
		displayList.trimToSize();
		firePageEvent(PageEvent.SHOWPAGE);
		displayList = createDisplayList();
	}

	/**
	 * Copy and show the current page.
	 */
	public void copypage() {
		firePageEvent(PageEvent.COPYPAGE);
		displayList = displayList.copy();
	}

	/**
	 * Erase the current page.
	 */
	public void erasepage() {
		firePageEvent(PageEvent.ERASEPAGE);
		displayList = createDisplayList();
	}

	/**
	 * Begin of job.
	 */
	public void beginJob() {
		firePageEvent(PageEvent.BEGINJOB);
	}

	/**
	 * End of job.
	 */
	public void endJob() {
		firePageEvent(PageEvent.ENDJOB);
	}

	/**
	 * Error in job.
	 * @param msg the error message
	 */
	public void error(String msg) {
		firePageEvent(PageEvent.ERROR);
	}

	/**
	 * Create a bitmap.
	 * @param wbits the width of the bitmap
	 * @param hbits the height of the bitmap
	 * @return a bitmap of specified size
	 */
	public Bitmap createBitmap(int wbits, int hbits) {
		return new DefaultBitmap(wbits, hbits, bitmapType);
	}

	/**
	 * Get the default clip rect.
	 * @return the clip rect
	 */
	protected Rectangle2D cliprect() {
		float s = scale * dpi / 72;
		return new Rectangle2D.Float(0, 0, s * width, s * height);
	}

	/**
	 * @return the device size
	 */
	public Dimension getSize() {
		double s = scale * dpi / 72;
		int wbits = (int) Math.round(s * width);
		int hbits = (int) Math.round(s * height);
		return new Dimension(wbits, hbits);
	}

	/**
	 * The device is treated like a bean, which is (in this case) it's
	 * own bean descriptor. Any properties exposed here can be used in the
	 * dictionary parameter to setpagedevice.
	 * @return an array of properties
	 */
	public PropertyDescriptor[] getPropertyDescriptors() {
		try {
			PropertyDescriptor properties[] = {
				new PropertyDescriptor("PageSize", DefaultPageDevice.class),
				new PropertyDescriptor("Scale", DefaultPageDevice.class),
				new PropertyDescriptor("NumCopies", DefaultPageDevice.class),
				new PropertyDescriptor("Orientation", DefaultPageDevice.class),
				new PropertyDescriptor("BitmapType", DefaultPageDevice.class)
			};
			return properties;
		} catch (Exception ex) {
			System.err.println(ex);
		}
		return null;
	}

	/**
	 * Convert the property. If the Java type of the property
	 * does not correpond to the PostScript type as required
	 * by the signature of the getter/setter method, we convert
	 * it here.
	 * @param name the name of the property
	 * @param value the value of the property
	 */
	public Object convertType(String name, Object value) {
		if ("PageSize".equals(name)) {
			Number numbers[] = (Number[]) value;
			float array[] = new float[2];
			array[0] = numbers[0].floatValue();
			array[1] = numbers[1].floatValue();
			value = array;
		} else if ("Orientation".equals(name)) {
			Number number = (Number) value;
			value = new Integer(number.intValue());
		} else if ("Scale".equals(name)) {
			Number number = (Number) value;
			value = new Float(number.floatValue());
		}
		return value;
	}

	/**
	 * Add a page event listener.
	 * @param the listener to add
	 */
	public void addPageEventListener(PageEventListener listener) {
		listeners.addElement(listener);
	}

	/**
	 * Remove a page event listener.
	 * @param the listener to remove
	 */
	public void removePageEventListener(PageEventListener listener) {
		listeners.removeElement(listener);
	}

	/**
	 * Send page event to all listeners.
	 */
	private void firePageEvent(int type) {
		PageEvent evt = new PageEvent(type, this);
		Enumeration e = listeners.elements();
		while (e.hasMoreElements()) {
			((PageEventListener) e.nextElement()).pageDeviceChanged(evt);
		}
	}

}
