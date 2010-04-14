
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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.io.Serializable;
import com.softhub.ps.device.Bitmap;

public class DisplayList
	implements Drawable, Serializable, Cloneable
{
	public static boolean debug = false;

	private final static int INITIAL_SIZE = 256;

	protected int index;
	protected Command buffer[];
	private boolean interrupted;
	private boolean resolutionDependent;
	private Color currentColor;
	private Stroke currentStroke;
	private Paint currentPaint;
	private Rectangle cliprect;
	private float scale = 1;

	public DisplayList() {
		this(INITIAL_SIZE);
	}

	public DisplayList(int size) {
		buffer = new Command[Math.max(1, size)];
	}

	public DisplayList copy() {
		try {
			return (DisplayList) clone();
		} catch (CloneNotSupportedException ex) {
			throw new InternalError();
		}
	}

	public void trimToSize() {
		if (index < buffer.length) {
			Command buf[] = new Command[Math.max(1, index)];
			System.arraycopy(buffer, 0, buf, 0, index);
			buffer = buf;
		}
	}

	public int getSize() {
		return buffer.length;
	}

	private void append(Command cmd) {
		if (index >= buffer.length) {
			Command tmp[] = buffer;
			int n = buffer.length * 2;
			buffer = new Command[n];
			System.arraycopy(tmp, 0, buffer, 0, index);
		}
		buffer[index++] = cmd;
	}

	public Rectangle getClipBounds() {
		return cliprect;
	}

	public void draw(Graphics2D g) {
		cliprect = g.getClipBounds();
		for (int i = 0; i < index && !interrupted; i++) {
			buffer[i].exec(g);
		}
		interrupted = false;
	}

	public void interrupt() {
		interrupted = true;
	}

	public void normalize(AffineTransform xform)
		throws NoninvertibleTransformException
	{
		AffineTransform ix = xform.createInverse();
		for (int i = 0; i < index; i++) {
			buffer[i].transform(ix);
		}
	}

	public boolean isResolutionDependent() {
		return resolutionDependent;
	}

	public void show(Reusable obj, AffineTransform xform) {
		append(new ReusedObject(obj, xform));
	}

	public void fill(Shape shape) {
		append(new FillCommand(shape));
	}

	public void stroke(Shape shape) {
		append(new StrokeCommand(shape));
	}

	public void initclip() {
		append(new ClipInit(this));
	}

	public void clip(Shape shape) {
		append(new ClipShape(shape, this));
	}

	public void image(Bitmap bitmap, AffineTransform xform) {
		append(new ImageCommand(bitmap, xform));
		resolutionDependent = true;
	}

	public void setColor(Color color) {
		if (!color.equals(currentColor)) {
			append(new ColorCommand(color));
			currentColor = color;
		}
	}

	public Color getColor() {
		return currentColor;
	}

	public void setStroke(Stroke stroke) {
		if (!stroke.equals(currentStroke)) {
			append(new PenCommand(stroke));
			currentStroke = stroke;
		}
	}

	public Stroke getStroke() {
		return currentStroke;
	}

	public void setPaint(Paint paint) {
		if (!paint.equals(currentPaint)) {
			append(new PaintCommand(paint));
			currentPaint = paint;
		}
	}

	public Paint getPaint() {
		return currentPaint;
	}

	public String toString() {
		return "display-list<" + index + ">";
	}

}
