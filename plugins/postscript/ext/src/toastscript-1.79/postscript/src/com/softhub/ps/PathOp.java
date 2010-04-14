
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
import java.util.*;

final class PathOp implements Stoppable, Types {

	private final static String OPNAMES[] = {
		"newpath", "moveto", "rmoveto", "lineto", "rlineto", "curveto",
		"rcurveto", "arc", "arcn", "arct", "arcto", "closepath",
		"flattenpath", "reversepath", "strokepath", "initclip", "clippath",
		"clip", "eoclip", "rectclip", "setbbox", "pathbbox", "pathforall",
		"fill", "eofill", "stroke", "rectstroke", "rectfill",
		"uappend", "upath", "ufill", "ueofill", "ustroke", "ustrokepath",
		"ucache", "ucachestatus"
	};

	static void install(Interpreter ip) {
		ip.installOp(OPNAMES, PathOp.class);
	}

	private static Point2D popPoint2D(Interpreter ip) {
		double y = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		double x = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		return new Point2D.Double(x, y);
	}

	static void newpath(Interpreter ip) {
		ip.getGraphicsState().newpath();
	}

	static void moveto(Interpreter ip) {
		ip.getGraphicsState().moveto(popPoint2D(ip));
	}

	static void rmoveto(Interpreter ip) {
		ip.getGraphicsState().rmoveto(popPoint2D(ip));
	}

	static void lineto(Interpreter ip) {
		ip.getGraphicsState().lineto(popPoint2D(ip));
	}

	static void rlineto(Interpreter ip) {
		ip.getGraphicsState().rlineto(popPoint2D(ip));
	}

	static void curveto(Interpreter ip) {
		Point2D c = popPoint2D(ip);
		Point2D b = popPoint2D(ip);
		Point2D a = popPoint2D(ip);
		ip.getGraphicsState().curveto(a, b, c);
	}

	static void rcurveto(Interpreter ip) {
		Point2D c = popPoint2D(ip);
		Point2D b = popPoint2D(ip);
		Point2D a = popPoint2D(ip);
		ip.getGraphicsState().rcurveto(a, b, c);
	}

	private static void arc(Interpreter ip, boolean ccw) {
		float ang2 = (float) (((NumberType) ip.ostack.pop(NUMBER)).realValue() * Math.PI / 180);
		float ang1 = (float) (((NumberType) ip.ostack.pop(NUMBER)).realValue() * Math.PI / 180);
		float r  = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
		Point2D center = popPoint2D(ip);
		ip.getGraphicsState().arc(center, r, ang1, ang2, ccw);
	}

	static void arc(Interpreter ip) {
		arc(ip, true);
	}

	static void arcn(Interpreter ip) {
		arc(ip, false);
	}

	static void arct(Interpreter ip) {
		float r  = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
		Point2D p2 = popPoint2D(ip);
		Point2D p1 = popPoint2D(ip);
		ip.getGraphicsState().arct(p1, p2, r);
	}

	static void arcto(Interpreter ip) {
		float r  = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
		Point2D p2 = popPoint2D(ip);
		Point2D p1 = popPoint2D(ip);
		double result[] = ip.getGraphicsState().arcto(p1, p2, r);
		ip.ostack.pushRef(new RealType(result[0]));
		ip.ostack.pushRef(new RealType(result[1]));
		ip.ostack.pushRef(new RealType(result[2]));
		ip.ostack.pushRef(new RealType(result[3]));
	}

	static void closepath(Interpreter ip) {
		ip.getGraphicsState().closepath();
	}

	static void flattenpath(Interpreter ip) {
		try {
			GraphicsState gstate = ip.getGraphicsState();
			AffineTransform ctm = gstate.currentmatrix();
			GeneralPath path = (GeneralPath) gstate.currentpath().clone();
			float flatness = gstate.currentflat();
			float coords[] = new float[6];
			PathIterator iter = path.getPathIterator(ctm.createInverse());
			PathIterator fiter = new FlatteningPathIterator(iter, flatness);
			gstate.newpath();
			while (!fiter.isDone()) {
				switch (fiter.currentSegment(coords)) {
				case PathIterator.SEG_MOVETO:
					gstate.moveto(coords[0], coords[1]);
					break;
				case PathIterator.SEG_LINETO:
					gstate.lineto(coords[0], coords[1]);
					break;
				case PathIterator.SEG_CLOSE:
					gstate.closepath();
					break;
				default:
					throw new Stop(INTERNALERROR, "flattenpath");
				}
				fiter.next();
			}
		} catch (Exception ex) {
			throw new Stop(UNDEFINEDRESULT);
		}
	}

	static void reversepath(Interpreter ip) {
		GraphicsState gstate = ip.getGraphicsState();
		AffineTransform ctm = gstate.currentmatrix();
		GeneralPath path = gstate.currentpath();
		Vector v = new Vector(100);
		float coords[] = new float[6];
		float lastmove[] = new float[2];
		try {
			PathIterator iter = path.getPathIterator(ctm.createInverse());
			while (!iter.isDone()) {
				int type = iter.currentSegment(coords);
				switch (type) {
				case PathIterator.SEG_MOVETO:
					lastmove[0] = coords[0];
					lastmove[1] = coords[1];
					v.addElement(new Segment(type, coords, 2));
					break;
				case PathIterator.SEG_LINETO:
					v.addElement(new Segment(type, coords, 2));
					break;
				case PathIterator.SEG_CUBICTO:
					v.addElement(new Segment(type, coords, 6));
					break;
				case PathIterator.SEG_CLOSE:
					v.addElement(new Segment(type, lastmove, 2));
					break;
				}
				iter.next();
			}
			gstate.newpath();
			Segment lastseg = null;
			int i = v.size();
			while (--i >= 0) {
				Segment seg = (Segment) v.elementAt(i);
				if (lastseg != null) {
					switch (lastseg.type) {
					case PathIterator.SEG_MOVETO:
						break;
					case PathIterator.SEG_LINETO:
						gstate.lineto(seg.getLastPoint());
						break;
					case PathIterator.SEG_CUBICTO:
						gstate.curveto(
							lastseg.getPointAt(1),
							lastseg.getPointAt(0),
							seg.getLastPoint()
						);
						break;
					case PathIterator.SEG_CLOSE:
						gstate.lineto(seg.getLastPoint());
						break;
					}
					lastseg = seg;
				} else {
					do {
						lastseg = (Segment) v.elementAt(--i);
					} while (lastseg.type == PathIterator.SEG_MOVETO);
					gstate.moveto(lastseg.getLastPoint());
				}
			}
		} catch (NoninvertibleTransformException ex) {
			throw new Stop(UNDEFINEDRESULT);
		}
	}

	static class Segment {

		int type;
		float coords[];

		Segment(int type, float coords[], int len) {
			this.type = type;
			this.coords = new float[len];
			System.arraycopy(coords, 0, this.coords, 0, len);
		}

		Point2D getPointAt(int index) {
			int j = 2 * index;
			return new Point2D.Float(coords[j], coords[j+1]);
		}

		Point2D getLastPoint() {
			switch (type) {
			case PathIterator.SEG_MOVETO:
				return new Point2D.Float(coords[0], coords[1]);
			case PathIterator.SEG_LINETO:
				return new Point2D.Float(coords[0], coords[1]);
			case PathIterator.SEG_CUBICTO:
				return new Point2D.Float(coords[4], coords[5]);
			case PathIterator.SEG_CLOSE:
				return new Point2D.Float(coords[0], coords[1]);
			}
			throw new Stop(INTERNALERROR, "getLastPoint: " + type);
		}

	}

	static void strokepath(Interpreter ip) {
		ip.getGraphicsState().strokepath();
	}

	static void initclip(Interpreter ip) {
		GraphicsState gc = ip.getGraphicsState();
		gc.initclip(gc.currentdevice());
	}

	static void clippath(Interpreter ip) {
		ip.getGraphicsState().clippath();
	}

	static void clip(Interpreter ip) {
		ip.getGraphicsState().clip(GeneralPath.WIND_NON_ZERO);
	}

	static void eoclip(Interpreter ip) {
		ip.getGraphicsState().clip(GeneralPath.WIND_EVEN_ODD);
	}

	static void rectclip(Interpreter ip) {
		Any any = ip.ostack.pop(NUMBER | ARRAY | STRING);
		GraphicsState gstate = ip.getGraphicsState();
		float llx, lly, width, height;
		if (any instanceof NumberType) {
			height = ((NumberType) any).floatValue();
			width = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
			lly = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
			llx = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
			gstate.rectclip(llx, lly, width, height);
		} else if (any instanceof ArrayType) {
			ArrayType array = (ArrayType) any;
			if (!array.check(NUMBER))
				throw new Stop(TYPECHECK);
			gstate.rectclip(array.toFloatArray());
		} else {
			// TODO: implement encoded string
			throw new Stop(INTERNALERROR, "<string> rectclip not yet implemented");
		}
	}

	static void setbbox(Interpreter ip) {
		Point2D ur = popPoint2D(ip);
		Point2D ll = popPoint2D(ip);
//		ip.getGraphicsState().currentpath().setbbox(ll, ur);
	}

	static void pathbbox(Interpreter ip) {
		double r[] = ip.getGraphicsState().pathbbox();
		ip.ostack.pushRef(new RealType(r[0]));
		ip.ostack.pushRef(new RealType(r[1]));
		ip.ostack.pushRef(new RealType(r[2]));
		ip.ostack.pushRef(new RealType(r[3]));
	}

	static void pathforall(Interpreter ip) {
		Any close = ip.ostack.pop(ARRAY | NAME);
		Any curve = ip.ostack.pop(ARRAY | NAME);
		Any line  = ip.ostack.pop(ARRAY | NAME);
		Any move  = ip.ostack.pop(ARRAY | NAME);
		// Some page descriptions use literal names instead of procs.
		move.cvx(); line.cvx(); curve.cvx(); close.cvx();
		GraphicsState gstate = ip.getGraphicsState();
		AffineTransform ctm = gstate.currentmatrix();
		GeneralPath path = gstate.currentpath();
		try {
			float coords[] = new float[6];
			PathIterator e = path.getPathIterator(ctm.createInverse());
			while (!e.isDone()) {
				switch (e.currentSegment(coords)) {
				case PathIterator.SEG_MOVETO:
					ip.ostack.pushRef(new RealType(coords[0]));
					ip.ostack.pushRef(new RealType(coords[1]));
					ip.estack.run(ip, move);
					break;
				case PathIterator.SEG_LINETO:
					ip.ostack.pushRef(new RealType(coords[0]));
					ip.ostack.pushRef(new RealType(coords[1]));
					ip.estack.run(ip, line);
					break;
				case PathIterator.SEG_CUBICTO:
					ip.ostack.pushRef(new RealType(coords[0]));
					ip.ostack.pushRef(new RealType(coords[1]));
					ip.ostack.pushRef(new RealType(coords[2]));
					ip.ostack.pushRef(new RealType(coords[3]));
					ip.ostack.pushRef(new RealType(coords[4]));
					ip.ostack.pushRef(new RealType(coords[5]));
					ip.estack.run(ip, curve);
					break;
				case PathIterator.SEG_CLOSE:
					ip.estack.run(ip, close);
					break;
				}
				e.next();
			}
		} catch (NoninvertibleTransformException ex) {
			throw new Stop(UNDEFINEDRESULT);
		}
	}

	static void fill(Interpreter ip) {
		ip.getGraphicsState().fill(GeneralPath.WIND_NON_ZERO);
	}

	static void eofill(Interpreter ip) {
		ip.getGraphicsState().fill(GeneralPath.WIND_EVEN_ODD);
	}

	static void stroke(Interpreter ip) {
		ip.getGraphicsState().stroke();
	}

	static void rectstroke(Interpreter ip) {
		Any any = ip.ostack.pop(NUMBER | ARRAY | STRING);
		GraphicsState gstate = ip.getGraphicsState();
		float llx, lly, width, height;
		if (any instanceof NumberType) {
			height = ((NumberType) any).floatValue();
			width = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
			lly = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
			llx = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
			gstate.rectstroke(llx, lly, width, height);
		} else if (any instanceof ArrayType) {
			// TODO: implement matrix case
			ArrayType array = (ArrayType) any;
			if (!array.check(NUMBER))
				throw new Stop(TYPECHECK);
//			gstate.rectfill(array.toFloatArray());
			// TODO: implement multiple rectangles
			throw new Stop(INTERNALERROR, "<array> rectfill not yet implemented");
		} else {
			// TODO: implement encoded string
			throw new Stop(INTERNALERROR, "<string> rectfill not yet implemented");
		}
	}

	static void rectfill(Interpreter ip) {
		Any any = ip.ostack.pop(NUMBER | ARRAY | STRING);
		GraphicsState gstate = ip.getGraphicsState();
		float llx, lly, width, height;
		if (any instanceof NumberType) {
			height = ((NumberType) any).floatValue();
			width = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
			lly = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
			llx = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
			gstate.rectfill(llx, lly, width, height);
		} else if (any instanceof ArrayType) {
			ArrayType array = (ArrayType) any;
			if (!array.check(NUMBER))
				throw new Stop(TYPECHECK);
//			gstate.rectfill(array.toFloatArray());
			// TODO: implement multiple rectangles
			throw new Stop(INTERNALERROR, "<array> rectfill not yet implemented");
		} else {
			// TODO: implement encoded string
			throw new Stop(INTERNALERROR, "<string> rectfill not yet implemented");
		}
	}

	static void ucache(Interpreter ip) {
		// TODO: implement ucache
	}

	static void ucachestatus(Interpreter ip) {
		int bsize = 1024;
		int bmax = 1024;
		int rsize = 100;
		int rmax = 100;
		int blimit = 1024;
		ip.ostack.pushRef(new MarkType());
		ip.ostack.pushRef(new IntegerType(bsize));
		ip.ostack.pushRef(new IntegerType(bmax));
		ip.ostack.pushRef(new IntegerType(rsize));
		ip.ostack.pushRef(new IntegerType(rmax));
		ip.ostack.pushRef(new IntegerType(blimit));
	}

	static void upath(Interpreter ip) {
		boolean ucache = ip.ostack.popBoolean();
		GraphicsState gstate = ip.getGraphicsState();
		AffineTransform ctm = gstate.currentmatrix();
		GeneralPath path = gstate.currentpath();
		try {
			float coords[] = new float[6];
			AffineTransform inverse = ctm.createInverse();
			ip.ostack.pushRef(new MarkType());
			if (ucache) {
				ip.ostack.pushRef(new NameType("ucache").cvx());
			}
			PathIterator e = path.getPathIterator(inverse);
			while (!e.isDone()) {
				switch (e.currentSegment(coords)) {
				case PathIterator.SEG_MOVETO:
					ip.ostack.pushRef(new RealType(coords[0]));
					ip.ostack.pushRef(new RealType(coords[1]));
					ip.ostack.pushRef(new NameType("moveto").cvx());
					break;
				case PathIterator.SEG_LINETO:
					ip.ostack.pushRef(new RealType(coords[0]));
					ip.ostack.pushRef(new RealType(coords[1]));
					ip.ostack.pushRef(new NameType("lineto").cvx());
					break;
				case PathIterator.SEG_CUBICTO:
					ip.ostack.pushRef(new RealType(coords[0]));
					ip.ostack.pushRef(new RealType(coords[1]));
					ip.ostack.pushRef(new RealType(coords[2]));
					ip.ostack.pushRef(new RealType(coords[3]));
					ip.ostack.pushRef(new RealType(coords[4]));
					ip.ostack.pushRef(new RealType(coords[5]));
					ip.ostack.pushRef(new NameType("curveto").cvx());
					break;
				case PathIterator.SEG_CLOSE:
					ip.ostack.pushRef(new NameType("closepath").cvx());
					break;
				}
				e.next();
			}
			int n = ip.ostack.counttomark();
			ArrayType array = new ArrayType(ip.vm, n, ip.ostack);
			ip.ostack.cleartomark();
			ip.ostack.pushRef(array);
		} catch (NoninvertibleTransformException ex) {
			throw new Stop(UNDEFINEDRESULT);
		}
	}

	static void uappend(Interpreter ip) {
		ArrayType array = (ArrayType) ip.ostack.pop(ARRAY);
		array.cvx();
		ip.estack.run(ip, array);
	}

	static void ufill(Interpreter ip) {
		upaint(ip, 0);
	}

	static void ueofill(Interpreter ip) {
		upaint(ip, 1);
	}

	static void ustroke(Interpreter ip) {
		upaint(ip, 2);
	}

	static void ustrokepath(Interpreter ip) {
		upaint(ip, 3);
	}

	private static void upaint(Interpreter ip, int operation) {
		ArrayType array = (ArrayType) ip.ostack.pop(ARRAY);
		AffineTransform xform = null;
		if (array.isMatrix()) {
			xform = array.toTransform();
			array = (ArrayType) ip.ostack.pop(ARRAY);
		}
		if (operation != 3) {
			ip.gsave();
		}
		GraphicsState gstate = ip.getGraphicsState();
		gstate.newpath();
		array.cvx();
		ip.estack.run(ip, array);
		AffineTransform ctm = null;
		if (xform != null) {
			ctm = gstate.currentmatrix();
			gstate.concat(xform);
		}
		switch (operation) {
		case 0:
			gstate.fill(GeneralPath.WIND_NON_ZERO);
			break;
		case 1:
			gstate.fill(GeneralPath.WIND_EVEN_ODD);
			break;
		case 2:
			gstate.stroke();
			break;
		case 3:
			gstate.strokepath();
			if (ctm != null) {
				gstate.setmatrix(ctm);
			}
			break;
		}
		if (operation != 3) {
			ip.grestore();
		}
	}

}
