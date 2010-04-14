
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

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.IOException;
import com.softhub.ps.filter.BinaryCodec;
import com.softhub.ps.util.CharStream;
import com.softhub.ps.util.CharWidth;

public class Type1Decoder extends AbstractFontDecoder {

	public boolean debug = false;

	// The command set:
	final static byte HSTEM = 1;
	final static byte VSTEM = 3;
	final static byte VMOVETO = 4;
	final static byte RLINETO = 5;
	final static byte HLINETO = 6;
	final static byte VLINETO = 7;
	final static byte RCURVETO = 8;
	final static byte CLOSEPATH = 9;
	final static byte CALLSUBR = 10;
	final static byte RETURN = 11;
	final static byte ESCAPE = 12;
	final static byte HSBW = 13;
	final static byte ENDCHAR = 14;
	final static byte RMOVETO = 21;
	final static byte HMOVETO = 22;
	final static byte VHCURVETO = 30;
	final static byte HVCURVETO = 31;
	// positive 2 byte integers
	final static byte POS20 = (byte) 247;
	final static byte POS21 = (byte) 248;
	final static byte POS22 = (byte) 249;
	final static byte POS23 = (byte) 250;
	// negative 2 byte integers
	final static byte NEG20 = (byte) 251;
	final static byte NEG21 = (byte) 252;
	final static byte NEG22 = (byte) 253;
	final static byte NEG23 = (byte) 254;
	// 4 byte integers
	final static byte NUM4  = (byte) 255;

	// The extended command set:
	final static byte EX_DOTSECTION = 0;
	final static byte EX_VSTEM3 = 1;
	final static byte EX_HSTEM3 = 2;
	final static byte EX_SEAC = 6;
	final static byte EX_SBW = 7;
	final static byte EX_DIV = 12;
	final static byte EX_CALLOTHERSUBR = 16;
	final static byte EX_POP = 17;
	final static byte EX_SETCURRENTPOINT = 33;

	/**
	 * The glyph dictionary.
	 */
	private DictType charstringdict;

	/**
	 * The "Private" dictionary.
	 */
	private DictType privatedict;

	/**
	 * The paint type.
	 */
	private int paintType;

	/**
	 * The stroke width.
	 */
	private float strokeWidth;

	/**
	 * The subroutines.
	 */
	private ArrayType subroutines;

	/**
	 * The skip count for decoding.
	 */
	private int skipcount = 4;

	/**
	 * The parameter stack.
	 */
	private int charstack[] = new int[128];

	/**
	 * The parameter stack pointer.
	 */
	private int charstackindex;

	public Type1Decoder(Interpreter ip, DictType font) {
		super(ip, font);
		charstringdict = (DictType) font.get("CharStrings");
		privatedict = (DictType) font.get("Private");
		paintType = ((IntegerType) font.get("PaintType")).intValue();
		int flags = privatedict.saveAccessFlags();
		Any skip = privatedict.get("lenIV");
		Any subrs = privatedict.get("Subrs");
		privatedict.restoreAccessFlags(flags);
		if (skip instanceof IntegerType) {
			skipcount = ((IntegerType) skip).intValue();
		}
		if (subrs instanceof ArrayType) {
			subroutines = (ArrayType) subrs;
		}
		Any optStrokeWidth = font.get("StrokeWidth");
		if (optStrokeWidth instanceof NumberType) {
			strokeWidth = ((NumberType) optStrokeWidth).floatValue();
		}
	}

	public Object clone() throws CloneNotSupportedException {
		return (Type1Decoder) super.clone();
	}

	public static void checkFont(Interpreter ip, DictType font) {
		ArrayType fontBBox = (ArrayType) font.get("FontBBox", ARRAY);
		if (!fontBBox.check(NUMBER, 4))
			throw new Stop(INVALIDFONT, "FontBBox");
		font.get("Encoding", ARRAY);
		ArrayType fontMatrix = (ArrayType) font.get("FontMatrix", ARRAY);
		if (!fontMatrix.isMatrix())
			throw new Stop(INVALIDFONT, "FontMatrix");
		font.get("Private", DICT);
		font.get("PaintType", INTEGER);
	}

	public CharWidth buildchar(Interpreter ip, int index, boolean render) {
		AffineTransform fx = getFontMatrix();
		ip.gsave();
		try {
			GraphicsState gstate = ip.getGraphicsState();
			if (render) {
				Point2D curpt = gstate.currentpoint();
				gstate.translate(curpt.getX(), curpt.getY());
			}
			gstate.concat(fx);
			gstate.newpath();
			gstate.moveto(0,0);
			buildglyph(ip, index);
			if (render) {
				gstate.setcachedevice(new CharWidth(charwidth), getFontBBox());
				if (paintType == 2) {
					gstate.setlinewidth(strokeWidth);
					gstate.stroke();
				} else {
					gstate.fill(GeneralPath.WIND_NON_ZERO);
				}
			}
		} finally {
			ip.grestore();
		}
		if (debug) {
			System.err.println("\nstack count: " + count());
		}
		return charwidth.transform(fx);
	}

	public void buildglyph(Interpreter ip, int index) {
		Any ch = encode(index);
		if (ch == null || ch instanceof NullType)
			throw new Stop(INVALIDFONT, "bad encoding vector");
		Any charstr = charstringdict.get(ch);
		if (!(charstr instanceof StringType))
			throw new Stop(INVALIDFONT, "buildglyph: [" + index + " " + ch + " " + charstr + "]");
		clear();
		charwidth.init();
		try {
			GraphicsState gstate = ip.getGraphicsState();
			buildglyph(gstate, new CharFilter((StringType) charstr));
			CharWidth cw = getMetrics(ip, index);
			if (cw != null) {
				charwidth = cw;
			}
		} catch (IOException ex) {
			throw new Stop(INVALIDFONT, "bad char strings");
		}
	}

	private void buildglyph(GraphicsState gstate, CharFilter param) throws IOException {
		boolean done = false;
		for (int i = 0; i < skipcount; i++) {
			param.decode();
		}
		int c;
		while (!done && (c = param.decode()) >= 0) {
			done = exec(gstate, c, param);
		}
	}

	private boolean exec(GraphicsState gstate, int cmd, CharFilter param) throws IOException {
		if (debug) {
			System.err.println("cmd: " + cmd);
		}
		switch (cmd) {
		case HSTEM:
			exec_hstem(gstate);
			break;
		case VSTEM:
			exec_vstem(gstate);
			break;
		case VMOVETO:
			exec_vmoveto(gstate);
			break;
		case RLINETO:
			exec_rlineto(gstate);
			break;
		case HLINETO:
			exec_hlineto(gstate);
			break;
		case VLINETO:
			exec_vlineto(gstate);
			break;
		case RCURVETO:
			exec_rcurveto(gstate);
			break;
		case CLOSEPATH:
			gstate.closepath();
			break;
		case CALLSUBR:
			exec_callsubr(gstate);
			break;
		case RETURN:
			exec_return(gstate);
			break;
		case ESCAPE:
			exec_escape(gstate, param);
			break;
		case HSBW:
			exec_hsbw(gstate);
			break;
		case ENDCHAR:
			return true;
		case RMOVETO:
			exec_rmoveto(gstate);
			break;
		case HMOVETO:
			exec_hmoveto(gstate);
			break;
		case VHCURVETO:
			exec_vhcurveto(gstate);
			break;
		case HVCURVETO:
			exec_hvcurveto(gstate);
			break;
		default:
			push(cmd, param);
			break;
		}
		if (debug) {
			System.err.println();
		}
		return false;
	}

	private void exec_hstem(GraphicsState gstate) {
		int y = pop();
		int x = pop();
		if (debug) {
			System.err.println(" hstem: " + x + " " + y);
		}
	}

	private void exec_vstem(GraphicsState gstate) {
		int y = pop();
		int x = pop();
		if (debug) {
			System.err.println(" vstem: " + x + " " + y);
		}
	}

	private void exec_vmoveto(GraphicsState gstate) {
		int y = pop();
		if (debug) {
			System.err.println(" vmoveto: " + y);
		}
		gstate.rmoveto(0, y);
	}

	private void exec_rlineto(GraphicsState gstate) {
		int y = pop();
		int x = pop();
		if (debug) {
			System.err.println(" rlineto: " + x + " " + y);
		}
		gstate.rlineto(x, y);
	}

	private void exec_hlineto(GraphicsState gstate) {
		int x = pop();
		if (debug) {
			System.err.println(" hlineto: " + x);
		}
		gstate.rlineto(x, 0);
	}

	private void exec_vlineto(GraphicsState gstate) {
		int y = pop();
		if (debug) {
			System.err.println(" vlineto: " + y);
		}
		gstate.rlineto(0, y);
	}

	private void exec_rcurveto(GraphicsState gstate) {
		int y3 = pop();
		int x3 = pop();
		int y2 = pop();
		int x2 = pop();
		int y1 = pop();
		int x1 = pop();
		int ax2 = x1 + x2;
		int ay2 = y1 + y2;
		int ax3 = ax2 + x3;
		int ay3 = ay2 + y3;
		gstate.rcurveto(
			new Point2D.Float(x1, y1),
			new Point2D.Float(ax2, ay2),
			new Point2D.Float(ax3, ay3)
		);
		if (debug) {
			System.err.println(" rcurveto: " + x1 + " " + y1 + ", " + ax2 + " " + ay2 + ", " + ax3 + " " + ay3);
		}
	}

	private void exec_callsubr(GraphicsState gstate) {
		int index = pop();
		if (debug) {
			System.err.println(" callsubr: " + index);
		}
		int flags = subroutines.saveAccessFlags();
		try {
			Any any = subroutines.get(index);
			subroutines.restoreAccessFlags(flags);
			if (!(any instanceof StringType))
				throw new Stop(INVALIDFONT, "callsubr");
			buildglyph(gstate, new CharFilter((StringType) any));
		} catch (IOException e) {
			subroutines.restoreAccessFlags(flags);
			throw new Stop(INVALIDFONT, "exec_callsubr: io-error");
		}
	}

	private void exec_return(GraphicsState gstate) {
		if (debug) {
			System.err.println(" return");
		}
	}

	private void exec_hsbw(GraphicsState gstate) {
		switch (count()) {
		case 1:
			pop_sx();
			break;
		case 2:
			pop_wx_sx();
			break;
		case 4:
			pop_wy_wx_sy_sx();
			break;
		default:
			throw new Stop(INVALIDFONT, "hsbw");
		}
		if (debug) {
			System.err.println(" hsbw: " + charwidth);
		}
		float sx = charwidth.getSideBearingX();
		float sy = charwidth.getSideBearingY();
		gstate.rmoveto(sx, sy);
	}

	private void pop_sx() {
		int sx = pop();
		charwidth.setWidth(sx);
	}

	private void pop_wx_sx() {
		int wx = pop();
		int sx = pop();
		charwidth.setWidth(sx, wx);
	}

	private void pop_wy_wx_sy_sx() {
		int wy = pop();
		int wx = pop();
		int sy = pop();
		int sx = pop();
		charwidth.setWidth(sx, sy, wx, wy);
	}

	private void exec_rmoveto(GraphicsState gstate) {
		int y = pop();
		int x = pop();
		if (debug) {
			System.err.println(" rmoveto: " + x + " " + y);
		}
		gstate.rmoveto(x, y);
	}

	private void exec_hmoveto(GraphicsState gstate) {
		int x = pop();
		if (debug) {
			System.err.println(" hmoveto: " + x);
		}
		gstate.rmoveto(x, 0);
	}

	private void exec_vhcurveto(GraphicsState gstate) {
		int x3 = pop();
		int y2 = pop();
		int x2 = pop();
		int y1 = pop();
		if (debug) {
			System.err.println(" vhcurveto: " + y1 + " " + x2 + ", " + y2 + " " + x3);
		}
		int ay2 = y1 + y2;
		gstate.rcurveto(
			new Point2D.Float(0, y1),
			new Point2D.Float(x2, ay2),
			new Point2D.Float(x2 + x3, ay2)
		);
	}

	private void exec_hvcurveto(GraphicsState gstate) {
		int y3 = pop();
		int y2 = pop();
		int x2 = pop();
		int x1 = pop();
		if (debug) {
			System.err.println(" hvcurveto: " + x1 + " " + x2 + ", " + y2 + " " + y3);
		}
		int ax2 = x1 + x2;
		gstate.rcurveto(
			new Point2D.Float(x1, 0),
			new Point2D.Float(ax2, y2),
			new Point2D.Float(ax2, y2 + y3)
		);
	}

	private void exec_escape(GraphicsState gstate, CharFilter param) throws IOException {
		int cmd = param.decode();
		if (debug) {
			System.err.println(" escape: " + cmd);
		}
		switch (cmd) {
		case EX_DOTSECTION:
			break;
		case EX_VSTEM3:
			pop(); pop(); pop(); pop(); pop(); pop();
			break;
		case EX_HSTEM3:
			pop(); pop(); pop(); pop(); pop(); pop();
			break;
		case EX_SEAC:
			pop(); pop(); pop(); pop();
			break;
		case EX_SBW:
			break;
		case EX_DIV:
			pop();
			break;
		case EX_CALLOTHERSUBR:
			ex_callothersubr();
			break;
		case EX_POP:
			pop();
			break;
		case EX_SETCURRENTPOINT:
			ex_setcurrentpoint(gstate);
			break;
		}
	}

	private void ex_callothersubr() {
		int cmd = top();
		switch (cmd) {
		default:
			if (debug) {
				System.err.println(" ex_cmd: " + cmd + " ");
			}
		}
	}

	private void ex_setcurrentpoint(GraphicsState gstate) {
		int y = pop();
		int x = pop();
		gstate.moveto(x, y);
	}

	private void push(int cmd, CharFilter param) throws IOException {
		int num = 0;
		if (cmd >= 32 && cmd < 247) {
			num = cmd - 139;
		} else if (cmd >= 247 && cmd <= 250) {
			num = (cmd - 247 << 8) + 108 + param.decode();
		} else if (cmd >= 251 && cmd <= 254) {
			num = -((cmd - 251 << 8) + 108 + param.decode());
		} else if (cmd == 255) {
			for (int i = 0; i < 4; i++) {
				num = (num << 8) + param.decode();
			}
		} else {
			if (debug) {
				System.err.println(" bad: " + num);
			}
		}
		if (charstackindex >= charstack.length)
			throw new Stop(INVALIDFONT, "stack overflow");
		charstack[charstackindex++] = num;
	}

	private int pop() {
		if (charstackindex <= 0)
			throw new Stop(INVALIDFONT, "stack underflow");
		return charstack[--charstackindex];
	}

	private int top() {
		if (charstackindex <= 0)
			throw new Stop(INVALIDFONT, "stack underflow");
		return charstack[charstackindex-1];
	}

	private void clear() {
		charstackindex = 0;
	}

	private int count() {
		return charstackindex;
	}

	private String stackTrace() {
		String s = "";
		for (int i = 0; i < charstackindex; i++)
			s += charstack[i] + " ";
		return s;
	}

	private void print(String msg) {
		if (debug) {
			System.out.print(msg);
		}
	}

	static class CharFilter extends BinaryCodec {

		public CharFilter(StringType charstring) {
			super(CHARSTRING_SEED);
			try {
				open((CharStream) charstring.clone(), CharStream.READ_MODE);	// TODO: why clone?
			} catch (Exception ex) {
				throw new Stop(INTERNALERROR, "CharFilter");
			}
		}

	}

}
