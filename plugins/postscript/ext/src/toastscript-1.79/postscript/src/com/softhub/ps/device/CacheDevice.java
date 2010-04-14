
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

import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import com.softhub.ps.graphics.CharacterShape;
import com.softhub.ps.graphics.DisplayList;
import com.softhub.ps.graphics.Reusable;
import com.softhub.ps.util.Cache;
import com.softhub.ps.util.CharWidth;

public class CacheDevice extends DisplayListDevice {

	private Cache cache = new Cache(2048);
	private Device target;
	private String character;
	private CharWidth charWidth;
	private Rectangle2D bounds;

	/**
	 * Create a new cache device.
	 */
	public CacheDevice() {
	}

	/**
	 * @return a newly created display list
	 */
	protected DisplayList createDisplayList() {
		return new CharacterShape();
	}

	/**
	 * @return the device name
	 */
	public String getName() {
		return "cachedevice";
	}

	/**
	 * Set the device target.
	 * @param target the target device
	 */
	public void setTarget(Device target) {
		this.target = target;
	}

	/**
	 * Set the character width.
	 * @param cw the character width
	 */
	public void setCharWidth(CharWidth cw) {
		this.charWidth = cw;
	}

	/**
	 * Set the character bounds.
	 * @param bounds the character bounding box
	 */
	public void setCharBounds(Rectangle2D bounds) {
		this.bounds = bounds;
	}

	/**
	 * @return the resolution in dots per inch
	 */
	public float getResolution() {
		return target.getResolution();
	}

	/**
	 * @return the scale factor
	 */
	public float getScale() {
		return target.getScale();
	}

	/**
	 * @return the default matrix
	 */
	public AffineTransform getDefaultMatrix() {
		return target.getDefaultMatrix();
	}

	/**
	 * @return the current cache size
	 */
	public int getCacheSize() {
		return cache.getSize();
	}

	/**
	 * @return the current cache size
	 */
	public int getMaxCacheSize() {
		return cache.getMaximumSize();
	}

	/**
	 * @return the current cache size
	 */
	public void setMaxCacheSize(int size) {
		cache.setMaximumSize(size);
	}

	/**
	 * Clear the cache.
	 */
	public void clearCache() {
		cache.clear();
	}

	/**
	 * Flush the cache device.
	 * @param info the font info
	 * @param ctm the current transformation matrix
	 */
	public void flush(FontInfo info, AffineTransform ctm) {
		try {
			if (character != null) {
				displayList.trimToSize();
				CharacterShape charShape = (CharacterShape) displayList;
				charShape.setCharCode(character);
				charShape.setCharWidth(charWidth);
				charShape.normalize(ctm);
				CacheEntry key = new CacheEntry(info, character, charWidth, charShape);
				cache.put(key, key);	// TODO: redundant, we should use set instead of map
			    show(charShape, ctm);
			}
		} catch (NoninvertibleTransformException ex) {
			System.err.println("warning: " + ex);
		} finally {
			character = null;
			charWidth = null;
			bounds = null;
		}
	}

	/**
	 * Show the cached object.
	 * @param info the font info
	 * @param character the encoded character
	 */
	public CharWidth showCachedCharacter(FontInfo info, AffineTransform ctm,
										 Point2D curpt, String character)
	{
		this.character = character;
		CacheEntry key = new CacheEntry(info, character);
		CacheEntry val = (CacheEntry) cache.get(key);
		if (val == null)
			return null;
		ctm.translate(curpt.getX(), curpt.getY());
		AffineTransform ftm = info.getFontMatrix();
		ctm.concatenate(ftm);
		show(val.getObject(), ctm);
		return val.getCharWidth().transform(ftm);
	}

	/**
	 * Show the cached object.
	 * @param obj the cached object
	 */
	public void show(Reusable obj, AffineTransform xform) {
		target.show(obj, xform);
	}

	/**
	 * @return the cliprect
	 */
	protected Rectangle2D cliprect() {
		return bounds;
	}

	/**
	 * @return the device size
	 */
	public Dimension getSize() {
		Dimension d = target.getSize();
		float s = getResolution() * getScale() / 72f;
		int wbits = range(s * (float) bounds.getWidth(), 1, d.width);
		int hbits = range(s * (float) bounds.getHeight(), 1, d.height);
		return new Dimension(wbits, hbits);
	}

	/**
	 * Assert round(val) is between min and max.
	 * @param val the value
	 * @param min the minimum value
	 * @param max the maximum value
	 * @return value rounded in interval min <= val <= max
	 */
	private static int range(float val, int min, int max) {
		int ival = Math.round(val);
		if (ival < min)
			return min;
		if (ival > max)
			return max;
		return ival;
	}

}
