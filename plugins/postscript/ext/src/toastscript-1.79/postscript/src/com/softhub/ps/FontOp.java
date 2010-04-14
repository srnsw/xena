
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Random;
import com.softhub.ps.device.Device;

final class FontOp implements Stoppable, Types {

	private final static String OPNAMES[] = {
		"findfont", "definefont", "undefinefont", "scalefont", "makefont",
		"setfont", "selectfont", "currentfont", "show", "ashow", "widthshow",
		"awidthshow", "kshow", "stringwidth", "charpath", "xshow", "yshow",
		"xyshow", "cshow"
	};

	static void install(Interpreter ip) {
		ip.installOp(OPNAMES, FontOp.class);
	}

	static void findfont(Interpreter ip) {
		Any key = ip.ostack.pop();
		if (!ResourceOp.handleFindResource(ip, "Font", key)) {
			// handler has altered the stack -> restore
			ip.ostack.pushRef(key);
			throw new Stop(UNDEFINED, "check font resources");
		}
	}

	static void definefont(Interpreter ip) {
		DictType font = (DictType) ip.ostack.pop(DICT);
		Any name = ip.ostack.pop();
		Any fontname = mapFontName(ip, name);
		if (fontname != null) {
			name = fontname;
		}
		int id, type = ((IntegerType) font.get("FontType", INTEGER)).intValue();
		checkFont(ip, font, type);
		Any uid = font.get("UniqueID");
		if (uid instanceof IntegerType) {
			id = ((IntegerType) uid).intValue();
		} else {
			id = new Random().nextInt();
		}
		if ((name.typeID() & (NAME | STRING)) == 0) {
			name = new NameType("anonymous font [" + id + "]");
		}
		FontIDType fontID = new FontIDType((type << 24) + id, name.toString());
		font.put(ip.vm, new NameType("FID"), fontID);
		font.setFontAttr();
		if (fontname != null) {
			// fix the font name for clones
			int flags = font.saveAccessFlags();
			font.put(ip.vm, "FontName", name);
			font.restoreAccessFlags(flags);
		}
		DictType dir = getFontDirectory(ip);
		dir.put(ip.vm, name, font);
		ip.ostack.pushRef(font);
	}

	static void undefinefont(Interpreter ip) {
		Any name = ip.ostack.pop();
		DictType dir = getFontDirectory(ip);
		if (dir.known(name)) {
			dir.remove(ip.vm, name);
		}
	}

	static void scalefont(Interpreter ip) {
		NumberType scale = (NumberType) ip.ostack.pop(NUMBER);
		DictType font = (DictType) ip.ostack.pop(DICT);
		if (!font.getFontAttr())
			throw new Stop(INVALIDFONT);
		NameType key = new NameType("FontMatrix");
		ArrayType fontmatrix = (ArrayType) font.get(key);
		ArrayType newfontmat = new ArrayType(ip.vm, fontmatrix.length());
		newfontmat.putinterval(ip.vm, 0, fontmatrix);
		int flags = newfontmat.saveAccessFlags();
		MatrixOp.scale(ip, newfontmat, scale, scale);
		newfontmat.restoreAccessFlags(flags);
		DictType newfontdict = new DictType(ip.vm, font);
		newfontdict.put(ip.vm, key, newfontmat);
		ip.ostack.pushRef(newfontdict);
	}

	static void makefont(Interpreter ip) {
		ArrayType matrix = (ArrayType) ip.ostack.pop(ARRAY);
		if (!matrix.isMatrix())
			throw new Stop(TYPECHECK);
		DictType font = (DictType) ip.ostack.pop(DICT);
		if (!font.getFontAttr())
			throw new Stop(INVALIDFONT);
		NameType key = new NameType("FontMatrix");
		ArrayType fontmatrix = (ArrayType) font.get(key);
		ArrayType newfontmat = new ArrayType(ip.vm, fontmatrix.length());
		newfontmat.putinterval(ip.vm, 0, fontmatrix);
		int flags = newfontmat.saveAccessFlags();
		MatrixOp.concat(ip, newfontmat, matrix, newfontmat);
		newfontmat.restoreAccessFlags(flags);
		DictType newfontdict = new DictType(ip.vm, font);
		newfontdict.put(ip.vm, key, newfontmat);
		ip.ostack.pushRef(newfontdict);
	}

	static void selectfont(Interpreter ip) {
		Any scale = ip.ostack.pop(NUMBER | ARRAY);
		Any key = ip.ostack.pop();
		DictType dir = getFontDirectory(ip);
		DictType font = (DictType) dir.get(key, DICT);
		if (font == null) {
			ip.ostack.pushRef(key);
			ip.estack.run(ip, new NameType("findfont").cvx());
		} else {
			ip.ostack.pushRef(font);
		}
		ip.ostack.pushRef(scale);
		if (scale.typeOf(NUMBER)) {
			scalefont(ip);
		} else {
			makefont(ip);
		}
		setfont(ip);
	}

	static void setfont(Interpreter ip) {
		DictType font = (DictType) ip.ostack.pop(DICT);
		if (!font.getFontAttr())
			throw new Stop(INVALIDFONT);
		FontDecoder decoder = createFontDecoder(ip, font);
		GraphicsState gstate = ip.getGraphicsState();
		gstate.setFontDecoder(decoder);
		Device device = gstate.currentdevice();
		device.setFont(decoder);
	}

	static void currentfont(Interpreter ip) {
		GraphicsState gstate = ip.getGraphicsState();
		FontDecoder fontdecoder = gstate.getFontDecoder();
		if (fontdecoder == null)
			throw new Stop(INTERNALERROR, "no current font");
		ip.ostack.push(fontdecoder.getFontDictionary());
	}

	static void show(Interpreter ip) {
		ip.getGraphicsState().show(ip, (StringType) ip.ostack.pop(STRING));
	}

	static void ashow(Interpreter ip) {
		StringType string = (StringType) ip.ostack.pop(STRING);
		double ay = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		double ax = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		ip.getGraphicsState().ashow(ip, ax, ay, string);
	}

	static void widthshow(Interpreter ip) {
		StringType string = (StringType) ip.ostack.pop(STRING);
		int c = ip.ostack.popInteger();
		double cy = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		double cx = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		ip.getGraphicsState().widthshow(ip, cx, cy, c, string);
	}

	static void awidthshow(Interpreter ip) {
		StringType string = (StringType) ip.ostack.pop(STRING);
		double ay = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		double ax = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		int c = ip.ostack.popInteger();
		double cy = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		double cx = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		ip.getGraphicsState().awidthshow(ip, cx, cy, c, ax, ay, string);
	}

	static void kshow(Interpreter ip) {
		StringType string = (StringType) ip.ostack.pop(STRING);
		ArrayType proc = (ArrayType) ip.ostack.pop(ARRAY);
		ip.getGraphicsState().kshow(ip, proc, string);
	}

	static void cshow(Interpreter ip) {
		StringType string = (StringType) ip.ostack.pop(STRING);
		ArrayType proc = (ArrayType) ip.ostack.pop(ARRAY);
		ip.getGraphicsState().cshow(ip, proc, string);
	}

	static void xshow(Interpreter ip) {
		Any displacement = ip.ostack.pop(ARRAY | STRING);
		StringType string = (StringType) ip.ostack.pop(STRING);
		ip.getGraphicsState().xshow(ip, string, displacement);
	}

	static void yshow(Interpreter ip) {
		Any displacement = ip.ostack.pop(ARRAY | STRING);
		StringType string = (StringType) ip.ostack.pop(STRING);
		ip.getGraphicsState().yshow(ip, string, displacement);
	}

	static void xyshow(Interpreter ip) {
		Any displacement = ip.ostack.pop(ARRAY | STRING);
		StringType string = (StringType) ip.ostack.pop(STRING);
		ip.getGraphicsState().xyshow(ip, string, displacement);
	}

	static void stringwidth(Interpreter ip) {
		StringType s = (StringType) ip.ostack.pop(STRING);
		float cw[] = ip.getGraphicsState().stringwidth(ip, s);
		ip.ostack.pushRef(new RealType(cw[0]));
		ip.ostack.pushRef(new RealType(cw[1]));
	}

	static void charpath(Interpreter ip) {
		boolean stroked = ip.ostack.popBoolean();
		StringType string = (StringType) ip.ostack.pop(STRING);
		ip.getGraphicsState().charpath(ip, string, stroked);
	}

	static DictType getFontDirectory(Interpreter ip) {
		DictType dir;
		if (ip.vm.isGlobal()) {
			dir = (DictType) ip.systemdict.get("GlobalFontDirectory", DICT);
		} else {
			dir = (DictType) ip.systemdict.get("FontDirectory", DICT);
		}
		return dir;
	}

	static Any mapFontName(Interpreter ip, Any name) {
		DictType statusdict = ip.getStatusDict();
		DictType fontnames = (DictType) statusdict.get("fontnamedict");
		return fontnames != null ? fontnames.get(name) : null;
	}

	static FontDecoder createFontDecoder(Interpreter ip, DictType font) {
		try {
			int fontType = ((IntegerType) font.get("FontType")).intValue();
			Class clazz = loadFontDecoder(fontType);
			Class types[] = new Class[2];
			types[0] = Interpreter.class;
			types[1] = DictType.class;
			Constructor constructor = clazz.getConstructor(types);
			Object params[] = new Object[2];
			params[0] = ip;
			params[1] = font;
			return (FontDecoder) constructor.newInstance(params);
		} catch (Exception ex) {
			throw new Stop(INVALIDFONT, ex.getMessage());
		}
	}

	static void checkFont(Interpreter ip, DictType font, int fontType) {
		try {
			Class clazz = loadFontDecoder(fontType);
			Class types[] = { Interpreter.class, DictType.class };
			Method method = clazz.getMethod("checkFont", types);
			Object params[] = new Object[2];
			params[0] = ip;
			params[1] = font;
			method.invoke(clazz, params);
		} catch (Exception ex) {
			throw new Stop(INVALIDFONT, ex.getMessage() + " type: " + fontType);
		}
	}

	private static Class loadFontDecoder(int type)
		throws ClassNotFoundException
	{
		String name = "com.softhub.ps.Type" + type + "Decoder";
		return Class.forName(name);
	}

}
