
package com.softhub.ps;

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

import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import com.softhub.ps.device.CacheDevice;
import com.softhub.ps.util.CharWidth;

public abstract class AbstractFontDecoder
	implements FontDecoder, Types, Cloneable, Stoppable
{
	/**
	 * The font dictionary.
	 */
	private DictType fontDict;

	/**
	 * The "FontMatrix" value.
	 */
	private AffineTransform fontMatrix;

	/**
	 * The "FontBBox" value.
	 */
	private Rectangle2D fontBBox;

	/**
	 * The encoding vector.
	 */
	private ArrayType encodingVector;

	/**
	 * The FontID.
	 */
	private FontIDType fontID;

	/**
	 * The corresponding bitmap font.
	 */
	private Font systemfont;

	/**
	 * The font type.
	 */
	private int fontType;

	/**
	 * The current char width.
	 */
	protected CharWidth charwidth = new CharWidth();

	public AbstractFontDecoder(Interpreter ip, DictType font) {
		fontID = (FontIDType) font.get("FID", FONTID);
		fontMatrix = ((ArrayType) font.get("FontMatrix")).toTransform();
		ArrayType fontbbox = (ArrayType) font.get("FontBBox");
		// FontBBox not required in type 0 fonts
		if (fontbbox != null) {
			setFontBBox(fontbbox);
		}
		encodingVector = (ArrayType) font.get("Encoding");
		fontType = ((IntegerType) font.get("FontType")).intValue();
		fontDict = font;
	}

	public Object clone() throws CloneNotSupportedException {
		AbstractFontDecoder decoder = (AbstractFontDecoder) super.clone();
		decoder.fontMatrix = (AffineTransform) fontMatrix.clone();
		return decoder;
	}

	public CharWidth show(Interpreter ip, int index) {
		GraphicsState gstate = ip.getGraphicsState();
		CacheDevice cachedevice = gstate.cachedevice();
		AffineTransform ctm = (AffineTransform) gstate.currentmatrix().clone();
		Point2D curpt = gstate.currentpoint();
		String character = encode(index).toString();
		CharWidth cw = cachedevice.showCachedCharacter(this, ctm, curpt, character);
		return cw != null ? cw : buildchar(ip, index, true);
	}

	public abstract CharWidth buildchar(Interpreter ip, int index, boolean render);

	public abstract void buildglyph(Interpreter ip, int index);

	public AffineTransform getFontMatrix() {
		return fontMatrix;
	}

	public void setCharWidth(CharWidth cw) {
		charwidth.setWidth(cw);
	}

	public CharWidth getCharWidth() {
		return charwidth;
	}

	protected FontIDType getFontID() {
		return fontID;
	}

	public int getFontType() {
		return fontType;
	}

	public Font getSystemFont() {
		return systemfont;
	}

	protected void setFontBBox(ArrayType fontbbox) {
		Any x0 = fontbbox.get(0);
		if (!(x0 instanceof NumberType))
			throw new Stop(INVALIDFONT, "bad font bbox<0>");
		Any y0 = fontbbox.get(1);
		if (!(y0 instanceof NumberType))
			throw new Stop(INVALIDFONT, "bad font bbox<1>");
		Any x1 = fontbbox.get(2);
		if (!(x1 instanceof NumberType))
			throw new Stop(INVALIDFONT, "bad font bbox<2>");
		Any y1 = fontbbox.get(3);
		if (!(y1 instanceof NumberType))
			throw new Stop(INVALIDFONT, "bad font bbox<3>");
		float x = ((NumberType) x0).floatValue();
		float y = ((NumberType) y0).floatValue();
		float w = ((NumberType) x1).floatValue() - x;
		float h = ((NumberType) y1).floatValue() - y;
		fontBBox = new Rectangle2D.Float(x, y, w, h);
	}

	public Rectangle2D getFontBBox() {
		return fontBBox;
	}

	public DictType getFontDictionary() {
		return fontDict;
	}

	public CharWidth charwidth(Interpreter ip, int index) {
		CharWidth cw = getMetrics(ip, index);
		return cw != null ? cw : buildchar(ip, index, false);
	}

	protected CharWidth getMetrics(Interpreter ip, int index) {
		Any metrics = fontDict.get("Metrics");
		if (metrics == null)
			return null;
		if (!(metrics instanceof DictType))
			throw new Stop(INVALIDFONT, "metrics entry not a dict");
		Any any = ((DictType) metrics).get(encode(index));
		if (any instanceof NumberType)
			return new CharWidth(((NumberType) any).floatValue());
		if (!(any instanceof ArrayType))
			return null;
		ArrayType array = (ArrayType) any;
		NumberType wx, wy, sx, sy;
		switch (array.length()) {
		case 2:
			if (!array.check(Any.NUMBER))
				throw new Stop(INVALIDFONT, "bad metrics entry<2>");
			sx = (NumberType) array.get(0);
			wx = (NumberType) array.get(1);
			return new CharWidth(sx.floatValue(), wx.floatValue());
		case 4:
			if (!array.check(Any.NUMBER))
				throw new Stop(INVALIDFONT, "bad metrics entry<4>");
			sx = (NumberType) array.get(0);
			sy = (NumberType) array.get(1);
			wx = (NumberType) array.get(2);
			wy = (NumberType) array.get(3);
			return new CharWidth(
				sx.floatValue(), sy.floatValue(),
				wx.floatValue(), wy.floatValue()
			);
		default:
			throw new Stop(INVALIDFONT, "bad metric value");
		}
	}

	public CharWidth charpath(Interpreter ip, int index) {
		GraphicsState gstate = ip.getGraphicsState();
		AffineTransform ctm = (AffineTransform) gstate.currentmatrix().clone();
		AffineTransform fx = getFontMatrix();
		Point2D oldpt = gstate.currentpoint();
		gstate.concat(fx);
		Point2D curpt = gstate.currentpoint();
		double x = curpt.getX();
		double y = curpt.getY();
		gstate.translate(x, y);
		gstate.moveto(0, 0);
		buildglyph(ip, index);
		gstate.setmatrix(ctm);
		gstate.moveto(oldpt.getX(), oldpt.getY());
		return charwidth.transform(fx);
	}

	public Any encode(int index) {
		return encodingVector.get(index & 0xff);
	}

	public String getFontName() {
		return fontID.getFontName();
	}

	public Object getFontUniqueID() {
		return fontID;
	}

	public String getCharName(int index) {
		return encode(index).toString();
	}

	private void setSystemFont(Interpreter ip) {
		DictType dict = ip.getGraphicsState().getSystemFonts();
		if (dict == null)
			return;
		String fontname = getFontName();
		Any sysname = dict.get(fontname);
		if (sysname == null)
			return;
		if (!(sysname instanceof NameType || sysname instanceof StringType))
			throw new Stop(TYPECHECK, "bad sysname: " + fontname + " -> "+ sysname);
		int size = 12, style = Font.PLAIN;
		String name = sysname.toString();
		int i = name.indexOf('-');
		if (i > 0) {
			String str = name;
			name = str.substring(0, i);
			str = str.substring(i+1);
			if ((i = str.indexOf('-')) >= 0) {
				if (str.startsWith("bold-")) {
					style = Font.BOLD;
				} else if (str.startsWith("italic-")) {
					style = Font.ITALIC;
				} else if (str.startsWith("bolditalic-")) {
					style = Font.BOLD | Font.ITALIC;
				}
				str = str.substring(i + 1);
			}
			try {
				size = Integer.valueOf(str).intValue();
			} catch (NumberFormatException e) {}
		}
		systemfont = new Font(name, style, size);
	}

}
