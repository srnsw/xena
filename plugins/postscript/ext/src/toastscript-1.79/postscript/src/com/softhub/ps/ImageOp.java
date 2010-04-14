
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

import com.softhub.ps.util.CharStream;
import java.awt.geom.AffineTransform;

final class ImageOp implements Stoppable, Types {

	private final static String OPNAMES[] = {
		"image", "imagemask", "colorimage", "execform",
		"makepattern"
	};

	static void install(Interpreter ip) {
		ip.installOp(OPNAMES, ImageOp.class);
	}

	static void image(Interpreter ip) {
		Any src = ip.ostack.pop(ARRAY | DICT | FILE | STRING);
		if (src instanceof DictType) {
			image(ip, (DictType) src, false);
		} else if (src instanceof ArrayType) {
			image(ip, (ArrayType) src);
		} else if (src instanceof CharStream) {
			image(ip, (CharStream) src);
		} else {
			throw new Stop(INTERNALERROR, "image: " + src.typeName());
		}
	}

	static void imagemask(Interpreter ip) {
		Any src = ip.ostack.pop(ARRAY | DICT | FILE | STRING);
		if (src instanceof DictType) {
			image(ip, (DictType) src, true);
		} else if (src instanceof ArrayType) {
			imagemask(ip, (ArrayType) src);
		} else if (src instanceof CharStream) {
			imagemask(ip, (CharStream) src);
		} else {
			throw new Stop(INTERNALERROR, "imagemask: " + src.typeName());
		}
	}

	private static void image(Interpreter ip, ArrayType proc) {
		GraphicsState gstate = ip.getGraphicsState();
		AffineTransform xform = ((ArrayType) ip.ostack.pop(ARRAY)).toTransform();
		int bits = ip.ostack.popInteger();
		int height = ip.ostack.popInteger();
		int width = ip.ostack.popInteger();
		if (width < 0 || height < 0)
			throw new Stop(UNDEFINEDRESULT);
		gstate.image(ip, width, height, bits, xform, proc);
	}

	private static void image(Interpreter ip, CharStream src) {
		GraphicsState gstate = ip.getGraphicsState();
		AffineTransform xform = ((ArrayType) ip.ostack.pop(ARRAY)).toTransform();
		int bits = ip.ostack.popInteger();
		int height = ip.ostack.popInteger();
		int width = ip.ostack.popInteger();
		if (width < 0 || height < 0)
			throw new Stop(UNDEFINEDRESULT);
		gstate.image(ip, width, height, bits, xform, src);
	}

	private static void imagemask(Interpreter ip, ArrayType proc) {
		GraphicsState gstate = ip.getGraphicsState();
		AffineTransform xform = ((ArrayType) ip.ostack.pop(ARRAY)).toTransform();
		boolean polarity = ip.ostack.popBoolean();
		int height = ip.ostack.popInteger();
		int width = ip.ostack.popInteger();
		if (width < 0 || height < 0)
			throw new Stop(UNDEFINEDRESULT);
		gstate.imagemask(ip, width, height, 1, polarity, xform, proc);
	}

	private static void imagemask(Interpreter ip, CharStream src) {
		GraphicsState gstate = ip.getGraphicsState();
		AffineTransform xform = ((ArrayType) ip.ostack.pop(ARRAY)).toTransform();
		boolean polarity = ip.ostack.popBoolean();
		int height = ip.ostack.popInteger();
		int width = ip.ostack.popInteger();
		if (width < 0 || height < 0)
			throw new Stop(UNDEFINEDRESULT);
		gstate.imagemask(ip, width, height, 1, polarity, xform, src);
	}

	private static void image(Interpreter ip, DictType dict, boolean mask) {
		GraphicsState gstate = ip.getGraphicsState();
		int type = ((IntegerType) dict.get("ImageType", INTEGER)).intValue();
		int width = ((IntegerType) dict.get("Width", INTEGER)).intValue();
		int height = ((IntegerType) dict.get("Height", INTEGER)).intValue();
		if (width < 0 || height < 0)
			throw new Stop(UNDEFINEDRESULT);
		int bits = ((IntegerType) dict.get("BitsPerComponent", INTEGER)).intValue();
		Any any = dict.get("Interpolate");
		boolean interpolate = (any instanceof BoolType) ? ((BoolType) any).booleanValue() : false;
		any = dict.get("MultipleDataSources");
		boolean mds = (any instanceof BoolType) ? ((BoolType) any).booleanValue() : false;
		ArrayType array = (ArrayType) dict.get("ImageMatrix", ARRAY);
		if (!array.isMatrix())
			throw new Stop(TYPECHECK, "ImageMatrix");
		AffineTransform xform = array.toTransform();
		ArrayType decode = (ArrayType) dict.get("Decode", ARRAY);
		if (mds) {
			ArrayType src = (ArrayType) dict.get("DataSource", ARRAY);
			if (mask) {
				gstate.imagemask(ip, width, height, bits, true, xform, src);
			} else {
				gstate.image(ip, width, height, bits, xform, src);
			}
		} else {
			Any datasrc = dict.get("DataSource");
			if (datasrc instanceof CharStream) {
				CharStream src = (CharStream) datasrc;
				if (mask) {
					gstate.imagemask(ip, width, height, bits, true, xform, src);
				} else {
					gstate.image(ip, width, height, bits, xform, src);
				}
			} else if (datasrc instanceof ArrayType) {
				ArrayType src = (ArrayType) datasrc;
				if (mask) {
					gstate.imagemask(ip, width, height, bits, true, xform, src);
				} else {
					gstate.image(ip, width, height, bits, xform, src);
				}
			} else {
				throw new Stop(TYPECHECK, "DataSource: " + datasrc);
			}
		}
	}

	static void colorimage(Interpreter ip) {
		GraphicsState gstate = ip.getGraphicsState();
		int ncomp = ((IntegerType) ip.ostack.pop()).intValue();
		if (!(ncomp == 1 || ncomp == 3 || ncomp == 4))
			throw new Stop(UNDEFINEDRESULT, "ncomp: " + ncomp);
		Any src[];
		boolean multi = ip.ostack.popBoolean();
		if (multi) {
			src = new Any[ncomp];
			for (int i = ncomp-1; i >= 0; i--) {
				src[i] = ip.ostack.pop(ARRAY | STRING | FILE);
			}
		} else {
			src = new Any[1];
			src[0] = ip.ostack.pop(ARRAY | STRING | FILE);
		}
		AffineTransform xform = ((ArrayType) ip.ostack.pop(ARRAY)).toTransform();
		int bitsPerComp = ip.ostack.popInteger();
		int height = ip.ostack.popInteger();
		int width = ip.ostack.popInteger();
		if (width < 0 || height < 0)
			throw new Stop(UNDEFINEDRESULT);
		gstate.colorimage(ip, width, height, bitsPerComp, ncomp, multi, xform, src);
	}

	static void execform(Interpreter ip) {
		// TODO: implement form caching
		DictType dict = (DictType) ip.ostack.pop(DICT);
		ArrayType proc = (ArrayType) dict.get("PaintProc", ARRAY);
		if (proc.isLiteral())
			throw new Stop(TYPECHECK, "PaintProc");
		ArrayType matrix = (ArrayType) dict.get("Matrix");
		if (!matrix.isMatrix())
			throw new Stop(TYPECHECK, "Matrix");
		ArrayType bbox = (ArrayType) dict.get("BBox", ARRAY);
		if (bbox.length() != 4 || !bbox.check(NUMBER))
			throw new Stop(TYPECHECK, "BBox");
		float llx = ((NumberType) bbox.get(0)).floatValue();
		float lly = ((NumberType) bbox.get(1)).floatValue();
		float width = ((NumberType) bbox.get(2)).floatValue() - llx;
		float height = ((NumberType) bbox.get(3)).floatValue() - lly;
		try {
			ip.gsave();
			GraphicsState gstate = ip.getGraphicsState();
			gstate.concat(matrix.toTransform());
			gstate.rectclip(llx, lly, width, height);
			ip.estack.run(ip, proc);
		} finally {
			ip.grestore();
		}
	}

	static void makepattern(Interpreter ip) {
		ArrayType matrix = (ArrayType) ip.ostack.pop(ARRAY);
		DictType dict = (DictType) ip.ostack.pop(DICT);
		if (!matrix.isMatrix())
			throw new Stop(TYPECHECK, "matrix");
		// TODO: implement pattern
		DictType pattern = new DictType(ip.vm, dict.length());
		dict.copyTo(ip.vm, pattern);
		ip.ostack.push(pattern);
	}

}
