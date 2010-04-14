
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

import java.awt.geom.*;
import com.softhub.ps.util.CharWidth;

public class Type0Decoder extends AbstractFontDecoder {

	private int fmaptype;

	private int escapeChar;

	private FontDecoder fontdecoder[];

	private FontDecoder currentfont;

	public Type0Decoder(Interpreter ip, DictType font) {
		super(ip, font);
		fmaptype = ((IntegerType) font.get("FMapType")).intValue();
		ArrayType fdepvector = (ArrayType) font.get("FDepVector");
		if (fmaptype == 3 || fmaptype == 7) {
			Any esc = font.get("EscChar");
			if (esc instanceof IntegerType) {
				escapeChar = ((IntegerType) esc).intValue();
			} else {
				escapeChar = 255;
			}
		}
		AffineTransform fontMatrix = getFontMatrix();
		int i, n = fdepvector.length();
		fontdecoder = new FontDecoder[n];
		for (i = 0; i < n; i++) {
			DictType fontDict = (DictType) fdepvector.get(i);
			ArrayType matrix = (ArrayType) fontDict.get("FontMatrix");
			AffineTransform xform = matrix.toTransform();
			AffineTransform xresult = (AffineTransform) fontMatrix.clone();
			xresult.concatenate(xform);
			matrix.put(ip.vm, xresult);
			fontDict.put(ip.vm, "FontMatrix", matrix);
			fontdecoder[i] = FontOp.createFontDecoder(ip, fontDict);
		}
	}

	public Object clone() throws CloneNotSupportedException {
		return (Type0Decoder) super.clone();
	}

	public static void checkFont(Interpreter ip, DictType font) {
		font.get("FMapType", INTEGER);
		font.get("Encoding", ARRAY);
		ArrayType fdepvector = (ArrayType) font.get("FDepVector", ARRAY);
		int i, n = fdepvector.length();
		for (i = 0; i < n; i++) {
			DictType fontDict = (DictType) fdepvector.get(i);
			int fontType = ((IntegerType) fontDict.get("FontType", INTEGER)).intValue();
			FontOp.checkFont(ip, fontDict, fontType);
		}
		ArrayType fontMatrix = (ArrayType) font.get("FontMatrix", ARRAY);
		if (!fontMatrix.isMatrix())
			throw new Stop(INVALIDFONT, "FontMatrix");
	}

	public CharWidth buildchar(Interpreter ip, int index, boolean render) {
		switch (fmaptype) {
		case 2:
			return buildcharFMapType2(ip, index, render);
		case 3:
			return buildcharFMapType3(ip, index, render);
		case 4:
			return buildcharFMapType4(ip, index, render);
		default:
			throw new Stop(INVALIDFONT, "FMapType " + fmaptype + " not implemented");
		}
	}

	private CharWidth buildcharFMapType2(Interpreter ip, int index, boolean render) {
		CharWidth cw;
		if (currentfont == null) {
			int fontcode = index & 0xff;
			Any fontindex = encode(fontcode);
			if (!(fontindex instanceof IntegerType))
				throw new Stop(TYPECHECK, "fontindex: " + fontindex);
			int fdecIndex = ((IntegerType) fontindex).intValue();
			currentfont = fontdecoder[fdecIndex];
			cw = new CharWidth();
		} else {
			int charcode = index & 0xff;
			cw = currentfont.show(ip, charcode);
			currentfont = null;
		}
		return cw;
	}

	private CharWidth buildcharFMapType3(Interpreter ip, int index, boolean render) {
		CharWidth cw;
		if (index == escapeChar) {
			currentfont = null;
			cw = new CharWidth();
		} else if (currentfont == null) {
			int fontcode = index & 0xff;
			Any fontindex = encode(fontcode);
			if (!(fontindex instanceof IntegerType))
				throw new Stop(TYPECHECK, "fontindex: " + fontindex);
			int fdecIndex = ((IntegerType) fontindex).intValue();
			currentfont = fontdecoder[fdecIndex];
			cw = new CharWidth();
		} else {
			int charcode = index & 0xff;
			cw = currentfont.show(ip, charcode);
		}
		return cw;
	}

	private CharWidth buildcharFMapType4(Interpreter ip, int index, boolean render) {
		int fontcode = (index & 0xff) >> 7;
		Any fontindex = encode(fontcode);
		if (!(fontindex instanceof IntegerType))
			throw new Stop(TYPECHECK, "fontindex: " + fontindex);
		int fdecIndex = ((IntegerType) fontindex).intValue();
		currentfont = fontdecoder[fdecIndex];
		int charcode = index & 0x7f;
		return currentfont.show(ip, charcode);
	}

	public void buildglyph(Interpreter ip, int index) {
	}

}
