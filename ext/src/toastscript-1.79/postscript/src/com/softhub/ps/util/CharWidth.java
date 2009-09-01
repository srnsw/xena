
package com.softhub.ps.util;

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

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class CharWidth {

	// Side bearing of current character.
	private float sx, sy;

	// Width of current character.
	private float wx, wy;

	public CharWidth() {
	}

	public CharWidth(float wx) {
		this.wx = wx;
	}

	public CharWidth(float sx, float wx) {
		this.sx = sx;
		this.wx = wx;
	}

	public CharWidth(float sx, float sy, float wx, float wy) {
		this.sx = sx;
		this.sy = sy;
		this.wx = wx;
		this.wy = wy;
	}

	public CharWidth(CharWidth cw) {
		this.sx = cw.sx;
		this.sy = cw.sy;
		this.wx = cw.wx;
		this.wy = cw.wy;
	}

	public void init() {
		this.sx = 0;
		this.sy = 0;
		this.wx = 0;
		this.wy = 0;
	}

	public float getSideBearingX() {
		return sx;
	}

	public float getSideBearingY() {
		return sy;
	}

	public float getDeltaX() {
		return wx;
	}

	public float getDeltaY() {
		return wy;
	}

	public void setWidth(float sx) {
		this.sx = sx;
	}

	public void setWidth(float sx, float wx) {
		this.sx = sx;
		this.wx = wx;
	}

	public void setWidth(float sx, float sy, float wx, float wy) {
		this.sx = sx;
		this.sy = sy;
		this.wx = wx;
		this.wy = wy;
	}

	public void setWidth(CharWidth cw) {
		this.sx = cw.sx;
		this.sy = cw.sy;
		this.wx = cw.wx;
		this.wy = cw.wy;
	}

	public CharWidth transform(AffineTransform xform) {
		Point2D spt = xform.deltaTransform(new Point2D.Float(sx, sy), null);
		Point2D wpt = xform.deltaTransform(new Point2D.Float(wx, wy), null);
		return new CharWidth(
			(float) spt.getX(), (float) spt.getY(),
			(float) wpt.getX(), (float) wpt.getY()
		);
	}

	public String toString() {
		return "[" + sx + ", " + sy + ", " + wx + ", " + wy + "]";
	}

}
