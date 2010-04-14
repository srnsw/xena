
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
 *
 * A matrix is defined as an array of 6 numbers.
 *
 * The matrix
 *				| a c tx |
 *				| b d ty |
 *				| 0 0 1  |
 *
 * is representet by this array:
 *
 *				[ a b c d tx ty ]
 */

import java.awt.geom.*;

final class MatrixOp implements Stoppable, Types {

	private final static String OPNAMES[] = {
		"matrix", "initmatrix", "identmatrix", "defaultmatrix", "currentmatrix",
		"setmatrix", "translate", "scale", "rotate", "concat", "concatmatrix",
		"transform", "itransform", "dtransform", "idtransform", "invertmatrix"
	};

	static void install(Interpreter ip) {
		ip.installOp(OPNAMES, MatrixOp.class);
	}

	static void matrix(Interpreter ip) {
		ArrayType matrix = new ArrayType(ip.vm, 6);
		matrix.put(ip.vm, 0, new IntegerType(1));
		matrix.put(ip.vm, 1, new IntegerType(0));
		matrix.put(ip.vm, 2, new IntegerType(0));
		matrix.put(ip.vm, 3, new IntegerType(1));
		matrix.put(ip.vm, 4, new IntegerType(0));
		matrix.put(ip.vm, 5, new IntegerType(0));
		ip.ostack.pushRef(matrix);
	}

	static void initmatrix(Interpreter ip) {
		GraphicsState gstate = ip.getGraphicsState();
		gstate.initmatrix(gstate.currentdevice());
	}

	static void identmatrix(Interpreter ip) {
		ArrayType matrix = (ArrayType) ip.ostack.top(ARRAY);
		if (matrix.length() != 6)
			throw new Stop(RANGECHECK);
		if (!matrix.check(NUMBER | NULL))
			throw new Stop(TYPECHECK);
		matrix.put(ip.vm, 0, new RealType(1));
		matrix.put(ip.vm, 1, new RealType(0));
		matrix.put(ip.vm, 2, new RealType(0));
		matrix.put(ip.vm, 3, new RealType(1));
		matrix.put(ip.vm, 4, new RealType(0));
		matrix.put(ip.vm, 5, new RealType(0));
	}

	static void defaultmatrix(Interpreter ip) {
		ArrayType matrix = (ArrayType) ip.ostack.top(ARRAY);
		matrix.put(ip.vm, ip.getGraphicsState().currentdevice().getDefaultMatrix());
	}

	static void currentmatrix(Interpreter ip) {
		ArrayType matrix = (ArrayType) ip.ostack.top(ARRAY);
		matrix.put(ip.vm, ip.getGraphicsState().currentmatrix());
	}

	static void setmatrix(Interpreter ip) {
		ArrayType matrix = (ArrayType) ip.ostack.pop(ARRAY);
		ip.getGraphicsState().setmatrix(matrix.toTransform());
	}

	static void translate(Interpreter ip) {
		Any any = ip.ostack.pop(NUMBER | ARRAY);
		if (any instanceof ArrayType) {
			ArrayType matrix = (ArrayType) any;
			if (!matrix.check(NUMBER | NULL, 6))
				throw new Stop(TYPECHECK);
			NumberType ty = (NumberType) ip.ostack.pop(NUMBER);
			NumberType tx = (NumberType) ip.ostack.pop(NUMBER);
			matrix.put(ip.vm, 0, new IntegerType(1));
			matrix.put(ip.vm, 1, new IntegerType(0));
			matrix.put(ip.vm, 2, new IntegerType(0));
			matrix.put(ip.vm, 3, new IntegerType(1));
			matrix.put(ip.vm, 4, tx);
			matrix.put(ip.vm, 5, ty);

			ip.ostack.pushRef(matrix);
		} else {
			NumberType ty = (NumberType) any;
			NumberType tx = (NumberType) ip.ostack.pop(NUMBER);
			ip.getGraphicsState().translate(tx.realValue(), ty.realValue());
		}
	}

	static void scale(Interpreter ip) {
		Any any = ip.ostack.pop(NUMBER | ARRAY);
		if (any instanceof ArrayType) {
			ArrayType matrix = (ArrayType) any;
			if (!matrix.check(NUMBER | NULL, 6))
				throw new Stop(TYPECHECK);
			NumberType sy = (NumberType) ip.ostack.pop(NUMBER);
			NumberType sx = (NumberType) ip.ostack.pop(NUMBER);
			matrix.put(ip.vm, 0, sx);
			matrix.put(ip.vm, 1, new IntegerType(0));
			matrix.put(ip.vm, 2, new IntegerType(0));
			matrix.put(ip.vm, 3, sy);
			matrix.put(ip.vm, 4, new IntegerType(0));
			matrix.put(ip.vm, 5, new IntegerType(0));
			ip.ostack.pushRef(matrix);
		} else {
			NumberType sy = (NumberType) any;
			NumberType sx = (NumberType) ip.ostack.pop(NUMBER);
			ip.getGraphicsState().scale(sx.realValue(), sy.realValue());
		}
	}

	static void scale(Interpreter ip, ArrayType matrix, NumberType sx, NumberType sy) {
		NumberType m0 = (NumberType) matrix.get(0);
		NumberType m1 = (NumberType) matrix.get(1);
		NumberType m2 = (NumberType) matrix.get(2);
		NumberType m3 = (NumberType) matrix.get(3);
		matrix.put(ip.vm, 0, ArithOp.mul(sx, m0));
		matrix.put(ip.vm, 1, ArithOp.mul(sx, m1));
		matrix.put(ip.vm, 2, ArithOp.mul(sy, m2));
		matrix.put(ip.vm, 3, ArithOp.mul(sy, m3));
	}

	static void rotate(Interpreter ip) {
		Any any = ip.ostack.pop(NUMBER | ARRAY);
		if (any instanceof ArrayType) {
			NumberType degree = (NumberType) ip.ostack.pop(NUMBER);
			AffineTransform xform = ((ArrayType) any).toTransform();
			xform.rotate((float) (degree.realValue() * Math.PI / 180));
			ip.ostack.pushRef(((ArrayType) any).put(ip.vm, xform));
		} else {
			ip.getGraphicsState().rotate((float) (((NumberType) any).realValue() * Math.PI / 180));
		}
	}

	static void concat(Interpreter ip) {
		ArrayType matrix = (ArrayType) ip.ostack.pop(ARRAY);
		ip.getGraphicsState().concat(matrix.toTransform());
	}

	static void concatmatrix(Interpreter ip) {
		ArrayType matrix3 = (ArrayType) ip.ostack.pop(ARRAY);
		ArrayType matrix2 = (ArrayType) ip.ostack.pop(ARRAY);
		ArrayType matrix1 = (ArrayType) ip.ostack.pop(ARRAY);
		concat(ip, matrix1, matrix2, matrix3);
		ip.ostack.pushRef(matrix3);
	}

	static void concat(Interpreter ip, ArrayType m1, ArrayType m2, ArrayType m3) {
		AffineTransform xform1 = m1.toTransform();
		AffineTransform xform2 = m2.toTransform();
		xform2.concatenate(xform1);
		m3.put(ip.vm, xform2);
	}

	static void transform(Interpreter ip) {
		Any any = ip.ostack.pop(NUMBER | ARRAY);
		if (any instanceof ArrayType) {
			ArrayType matrix = (ArrayType) any;
			if (!matrix.isMatrix())
				throw new Stop(TYPECHECK);
			NumberType ty = (NumberType) ip.ostack.pop(NUMBER);
			NumberType tx = (NumberType) ip.ostack.pop(NUMBER);
			NumberType m0 = (NumberType) matrix.get(0);
			NumberType m1 = (NumberType) matrix.get(1);
			NumberType m2 = (NumberType) matrix.get(2);
			NumberType m3 = (NumberType) matrix.get(3);
			NumberType m4 = (NumberType) matrix.get(4);
			NumberType m5 = (NumberType) matrix.get(5);
			ip.ostack.pushRef(ArithOp.add(ArithOp.add(ArithOp.mul(m0, tx), ArithOp.mul(m2, ty)), m4));
			ip.ostack.pushRef(ArithOp.add(ArithOp.add(ArithOp.mul(m1, tx), ArithOp.mul(m3, ty)), m5));
		} else {
			float ty = ((NumberType) any).floatValue();
			float tx = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
			AffineTransform ctm = ip.getGraphicsState().currentmatrix();
			Point2D t = new Point2D.Double(tx, ty);
			Point2D tt = ctm.transform(t, null);
			ip.ostack.pushRef(new RealType(tt.getX()));
			ip.ostack.pushRef(new RealType(tt.getY()));
		}
	}

	static void dtransform(Interpreter ip) {
		Any any = ip.ostack.pop(NUMBER | ARRAY);
		if (any instanceof ArrayType) {
			ArrayType matrix = (ArrayType) any;
			if (!matrix.isMatrix())
				throw new Stop(TYPECHECK);
			NumberType ty = (NumberType) ip.ostack.pop(NUMBER);
			NumberType tx = (NumberType) ip.ostack.pop(NUMBER);
			NumberType m0 = (NumberType) matrix.get(0);
			NumberType m1 = (NumberType) matrix.get(1);
			NumberType m2 = (NumberType) matrix.get(2);
			NumberType m3 = (NumberType) matrix.get(3);
			ip.ostack.pushRef(ArithOp.add(ArithOp.mul(m0, tx), ArithOp.mul(m2, ty)));
			ip.ostack.pushRef(ArithOp.add(ArithOp.mul(m1, tx), ArithOp.mul(m3, ty)));
		} else {
			float ty = ((NumberType) any).floatValue();
			float tx = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
			AffineTransform ctm = ip.getGraphicsState().currentmatrix();
			Point2D t = new Point2D.Double(tx, ty);
			Point2D tt = ctm.deltaTransform(t, null);
			ip.ostack.pushRef(new RealType(tt.getX()));
			ip.ostack.pushRef(new RealType(tt.getY()));
		}
	}

	static void itransform(Interpreter ip) {
		Any any = ip.ostack.pop(NUMBER | ARRAY);
		AffineTransform xform;
		float tx, ty;
		if (any instanceof ArrayType) {
			ty = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
			tx = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
			xform = ((ArrayType) any).toTransform();
		} else {
			ty = ((NumberType) any).floatValue();
			tx = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
			xform = ip.getGraphicsState().currentmatrix();
		}
		try {
			Point2D t = new Point2D.Double(tx, ty);
			Point2D tt = xform.inverseTransform(t, null);
			ip.ostack.pushRef(new RealType(tt.getX()));
			ip.ostack.pushRef(new RealType(tt.getY()));
		} catch (NoninvertibleTransformException ex) {
			throw new Stop(UNDEFINEDRESULT, "non invertible matrix");
		}
	}

	static void idtransform(Interpreter ip) {
		Any any = ip.ostack.pop(NUMBER | ARRAY);
		AffineTransform xform;
		float tx, ty;
		if (any instanceof ArrayType) {
			ty = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
			tx = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
			xform = ((ArrayType) any).toTransform();
		} else {
			ty = ((NumberType) any).floatValue();
			tx = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
			xform = ip.getGraphicsState().currentmatrix();
		}
		try {
			double flatmatrix[] = new double[6];
			xform.getMatrix(flatmatrix);
			flatmatrix[4] = flatmatrix[5] = 0;
			xform = new AffineTransform(flatmatrix);
			Point2D t = new Point2D.Double(tx, ty);
			Point2D tt = xform.inverseTransform(t, null);
			ip.ostack.pushRef(new RealType(tt.getX()));
			ip.ostack.pushRef(new RealType(tt.getY()));
		} catch (NoninvertibleTransformException ex) {
			throw new Stop(UNDEFINEDRESULT, "non invertible matrix");
		}
	}

	static void invertmatrix(Interpreter ip) {
		ArrayType matrix2 = (ArrayType) ip.ostack.pop(ARRAY);
		ArrayType matrix1 = (ArrayType) ip.ostack.pop(ARRAY);
		AffineTransform xform1 = matrix1.toTransform();
		AffineTransform xform2 = null;
		try {
			xform2 = xform1.createInverse();
		} catch (NoninvertibleTransformException ex) {
			throw new Stop(UNDEFINEDRESULT, "non invertible matrix");
		}
		ip.ostack.pushRef(matrix2.put(ip.vm, xform2));
	}

}
