
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

import com.softhub.ps.device.Bitmap;
import com.softhub.ps.device.CacheDevice;
import com.softhub.ps.device.Device;
import com.softhub.ps.device.NullDevice;
import com.softhub.ps.device.PageDevice;
import com.softhub.ps.device.PageEvent;
import com.softhub.ps.device.PageEventListener;
import com.softhub.ps.image.PixelSourceFactory;
import com.softhub.ps.image.CMYKPixelSource;
import com.softhub.ps.image.GrayPixelSource;
import com.softhub.ps.image.RGBPixelSource;
import com.softhub.ps.util.CharStream;
import com.softhub.ps.util.CharWidth;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.*;
import java.io.IOException;
import java.util.*;

public class GraphicsState extends GStateType implements PageEventListener {

	private final static double SMALL_NUM = 1e-7;
	private final static float DEFAULT_FLATNESS = 1;
	private final static float DEFAULT_MITERLIMIT = 10;
	private final static double TWO_PI = 2.0 * Math.PI;
	private final static Screen NULL_SCREEN = new Screen(0, 0, null);
	private final static AffineTransform IDENT_MATRIX = new AffineTransform();

	/**
	 * The null device.
	 */
	private NullDevice nulldevice = new NullDevice();

	/**
	 * The cache device.
	 */
	private CacheDevice cachedevice = new CacheDevice();

	/**
	 * The default page device.
	 */
	private Device defaultdevice = nulldevice;

	/**
	 * The page device.
	 */
	private Device pagedevice = nulldevice;

	/**
	 * The current output device.
	 */
	private Device currentdevice = nulldevice;

	/**
	 * The current transformation matrix.
	 */
	private AffineTransform ctm = new AffineTransform();

	/**
	 * The current path.
	 */
	private GeneralPath currentpath = new GeneralPath();

	/**
	 * The current point.
	 */
	private Point2D currentpoint;

	/**
	 * Stroke changed flag.
	 */
	private boolean strokeChanged;

	/**
	 * The current line width.
	 */
	private float currentwidth;

	/**
	 * The current line cap.
	 */
	private int currentcap;

	/**
	 * The current line join.
	 */
	private int currentjoin;

	/**
	 * The current dash array.
	 */
	private ArrayType currentdasharray;

	/**
	 * The current dash offset.
	 */
	private float currentdashphase;

	/**
	 * The current flatness for curves.
	 */
	private float currentflat = DEFAULT_FLATNESS;

	/**
	 * The current miterlimit.
	 */
	private float currentmiterlimit = DEFAULT_MITERLIMIT;

	/**
	 * The current stroke adjustment.
	 */
	private boolean strokeAdjustment;

	/**
	 * The current overprint.
	 */
	private boolean overprint;

	/**
	 * The current gray.
	 */
	private float currentgray;

	/**
	 * The current red rgb color.
	 */
	private float currentred;

	/**
	 * The current green rgb color.
	 */
	private float currentgreen;

	/**
	 * The current blue rgb color.
	 */
	private float currentblue;

	/**
	 * The current halftone phase in X.
	 */
	private int halftonePhaseX;

	/**
	 * The current halftone phase in Y.
	 */
	private int halftonePhaseY;

	/**
	 * The gray halftone dictionary.
	 */
	private DictType halftone;

	/**
	 * The current halftone screen.
	 */
	private Screen currentredscreen;

	/**
	 * The current halftone screen.
	 */
	private Screen currentgreenscreen;

	/**
	 * The current halftone screen.
	 */
	private Screen currentbluescreen;

	/**
	 * The pattern dictionary.
	 */
	private DictType pattern;

	/**
	 * The color rendering dictionary.
	 */
	private DictType colorRendering;

	/**
	 * The current gray color transfer proc.
	 */
	private ArrayType currentgraytransfer;

	/**
	 * The current red color transfer proc.
	 */
	private ArrayType currentredtransfer;

	/**
	 * The current green color transfer proc.
	 */
	private ArrayType currentgreentransfer;

	/**
	 * The current blue color transfer proc.
	 */
	private ArrayType currentbluetransfer;

	/**
	 * The current black generation proc.
	 */
	private ArrayType currentblackgeneration;

	/**
	 * The current under color removal proc.
	 */
	private ArrayType currentundercolorremoval;

	/**
	 * The current colorspace.
	 */
	private ColorSpace colorspace;

	/**
	 * The font decoder.
	 */
	private FontDecoder fontdecoder;

	/**
	 * Bitmap font request flag.
	 */
	private boolean bitmapwidths;

	/**
	 * The system font dictionary.
	 */
	private DictType systemfontdict;

	/**
	 * The device state.
	 */
	private Object deviceState;

	/**
	 * Create graphics state.
	 */
	public GraphicsState() {
	}

	/**
	 * Called after gsave has pushed graphics state
	 * onto gstack.
	 * @param level the current save level
	 */
	public void save(int level) {
		deviceState = currentdevice.save();
		setSaveLevel(level);
	}

	/**
	 * Called after grestore has popped graphics state
	 * from gstack.
	 * @param gstate the graphics state to restore to
	 */
	public void restore(GStateType gstate) {
		GraphicsState gc = (GraphicsState) gstate;
		if (gc.currentdevice instanceof CacheDevice) {
			// flush the cache device
			AffineTransform xform = (AffineTransform) gc.ctm.clone();
			cachedevice.flush(gc.fontdecoder, xform);
		}
		currentdevice.restore(gc.deviceState);
		strokeChanged = gc.strokeChanged;
	}

	/**
	 * @return a clone of the graphics state
	 */
	public Object clone() throws CloneNotSupportedException {
		GraphicsState gc = (GraphicsState) super.clone();
		gc.ctm = (AffineTransform) ctm.clone();
		if (gc.currentpath != null)
			gc.currentpath = (GeneralPath) currentpath.clone();
		if (gc.currentpoint != null)
			gc.currentpoint = (Point2D) currentpoint.clone();
		if (currentdasharray != null)
			gc.currentdasharray = (ArrayType) currentdasharray.clone();
		// clone the halftone screens
		if (gc.currentredscreen != null)
			gc.currentredscreen = (Screen) currentredscreen.clone();
		if (gc.currentgreenscreen != null)
			gc.currentgreenscreen = (Screen) currentgreenscreen.clone();
		if (gc.currentbluescreen != null)
			gc.currentbluescreen = (Screen) currentbluescreen.clone();
		// clone the halftone
		if (gc.halftone != null)
			gc.halftone = (DictType) halftone.clone();
		// clone the pattern
		if (gc.pattern != null)
			gc.pattern = (DictType) pattern.clone();
		// clone the pattern
		if (gc.colorRendering != null)
			gc.colorRendering = (DictType) colorRendering.clone();
		// clone the color transfer functions
		if (currentredtransfer != null)
			gc.currentredtransfer = (ArrayType) currentredtransfer.clone();
		if (currentgreentransfer != null)
			gc.currentgreentransfer = (ArrayType) currentgreentransfer.clone();
		if (currentbluetransfer != null)
			gc.currentbluetransfer = (ArrayType) currentbluetransfer.clone();
		if (currentgraytransfer != null)
			gc.currentgraytransfer = (ArrayType) currentgraytransfer.clone();
		if (currentblackgeneration != null)
			gc.currentblackgeneration = (ArrayType) currentblackgeneration.clone();
		if (currentundercolorremoval != null)
			gc.currentundercolorremoval = (ArrayType) currentundercolorremoval.clone();
		if (fontdecoder != null)
			gc.fontdecoder = (FontDecoder) fontdecoder.clone();
		return gc;
	}

	/**
	 * Install device driver.
	 * @param device the device
	 */
	public void install(Device device) {
		defaultdevice = pagedevice = device;
	}

	/**
	 * Initialize the graphics state.
	 */
	public void initgraphics() {
		currentdevice.init();
		initclip(currentdevice);
		initmatrix(currentdevice);
		newpath();
		currentwidth = 0;
		currentcap = 0;
		currentjoin = 0;
		currentdashphase = 0;
		currentdasharray = null;
		strokeChanged = true;
		setgray(0);
		setmiterlimit(10);
	}

	private void setdevice(Device device) {
		currentdevice = device;
		device.init();
	}

	public void pageDeviceChanged(PageEvent evt) {
		int type = evt.getType();
		if (type == PageEvent.RESIZE) {
			PageDevice device = evt.getPageDevice();
			initmatrix(device);
		}
	}

	/**
	 * Set the page device and make it the current
	 * output device.
	 * @param devic the new page device
	 */
	public void setpagedevice(Device device) {
		pagedevice = currentdevice = device;
		initgraphics();
	}

	/**
	 * @return the page device
	 */
	public Device pagedevice() {
		return pagedevice;
	}

	/**
	 * @return the default page device
	 */
	public Device defaultdevice() {
		return defaultdevice;
	}

	/**
	 * @return the current output device
	 */
	public Device currentdevice() {
		return currentdevice;
	}

	/**
	 * @return the cache device
	 */
	public CacheDevice cachedevice() {
		return cachedevice;
	}

	/**
	 * @return the current path
	 */
	public GeneralPath currentpath() {
		return currentpath;
	}

	/**
	 * Initialize the current transformation
	 * matrix (CTM).
	 * @param device the output device
	 */
	public void initmatrix(Device device) {
		setmatrix(device.getDefaultMatrix());
	}

	/**
	 * @return the current matrix (CTM)
	 */
	public AffineTransform currentmatrix() {
		return ctm;
	}

	public void setmatrix(AffineTransform xform) {
		ctm.setTransform(xform);
	}

	public void translate(double tx, double ty) {
		ctm.translate(tx, ty);
	}

	public void scale(double sx, double sy) {
		ctm.scale(sx, sy);
	}

	public void rotate(double angle) {
		ctm.rotate(angle);
	}

	public void concat(AffineTransform xform) {
		ctm.concatenate(xform);
	}

	public void newpath() {
		currentpath = new GeneralPath();
		currentpoint = null;
	}

	public Point2D currentpoint() {
		if (currentpoint == null)
			throw new Stop(NOCURRENTPOINT);
		Point2D icurpt = null;
		try {
			icurpt = ctm.inverseTransform(currentpoint, null);
		} catch (NoninvertibleTransformException ex) {
			throw new Stop(UNDEFINEDRESULT);
		}
		return icurpt;
	}

	public Point2D currentdevicepoint() {
		if (currentpoint == null)
			throw new Stop(NOCURRENTPOINT, "currentdevicepoint");
		return currentpoint;
	}

	public void moveto(double x, double y) {
		moveto(new Point2D.Double(x, y));
	}

	public void moveto(Point2D pt) {
		currentpoint = ctm.transform(pt, null);
		currentpath.moveTo((float) currentpoint.getX(), (float) currentpoint.getY());
	}

	public void rmoveto(double x, double y) {
		rmoveto(new Point2D.Double(x, y));
	}

	public void rmoveto(Point2D v) {
		if (currentpoint == null)
			throw new Stop(NOCURRENTPOINT, "rmoveto");
		Point2D dv = ctm.deltaTransform(v, null);
		double x = currentpoint.getX() + dv.getX();
		double y = currentpoint.getY() + dv.getY();
		currentpoint = new Point2D.Double(x, y);
		currentpath.moveTo((float) x, (float) y);
	}

	public void lineto(double x, double y) {
		lineto(new Point2D.Double(x, y));
	}

	public void lineto(Point2D pt) {
		if (currentpoint == null)
			throw new Stop(NOCURRENTPOINT, "lineto");
		currentpoint = ctm.transform(pt, null);
		currentpath.lineTo((float) currentpoint.getX(), (float) currentpoint.getY());
	}

	public void rlineto(double x, double y) {
		rlineto(new Point2D.Double(x, y));
	}

	public void rlineto(Point2D v) {
		if (currentpoint == null)
			throw new Stop(NOCURRENTPOINT, "rlineto");
		Point2D dv = ctm.deltaTransform(v, null);
		double x = currentpoint.getX() + dv.getX();
		double y = currentpoint.getY() + dv.getY();
		currentpoint = new Point2D.Double(x, y);
		currentpath.lineTo((float) x, (float) y);
	}

	public void curveto(Point2D p1, Point2D p2, Point2D p3) {
		if (currentpoint == null)
			throw new Stop(NOCURRENTPOINT, "curveto");
		Point2D a = ctm.transform(p1, null);
		Point2D b = ctm.transform(p2, null);
		currentpoint = ctm.transform(p3, null);
		currentpath.curveTo(
			(float) a.getX(), (float) a.getY(),
			(float) b.getX(), (float) b.getY(),
			(float) currentpoint.getX(), (float) currentpoint.getY()
		);
	}

	public void rcurveto(Point2D v1, Point2D v2, Point2D v3) {
		if (currentpoint == null)
			throw new Stop(NOCURRENTPOINT, "rcurveto");
		Point2D dv1 = ctm.deltaTransform(v1, null);
		Point2D dv2 = ctm.deltaTransform(v2, null);
		Point2D dv3 = ctm.deltaTransform(v3, null);
		double ax = currentpoint.getX() + dv1.getX();
		double ay = currentpoint.getY() + dv1.getY();
		double bx = currentpoint.getX() + dv2.getX();
		double by = currentpoint.getY() + dv2.getY();
		double cx = currentpoint.getX() + dv3.getX();
		double cy = currentpoint.getY() + dv3.getY();
		currentpoint = new Point2D.Double(cx, cy);
		currentpath.curveTo(
			(float) ax, (float) ay,
			(float) bx, (float) by,
			(float) cx, (float) cy
		);
	}

	private static int arcToCurve(double ang1, double ang2, boolean ccw, Point2D result[/*13*/]) {
		double ang = ccw ? angDiff(ang1, ang2) : angDiff(ang2, ang1);
		double angle = ang360(ang);
		int n = (int) ((2 * Math.abs(angle) - SMALL_NUM) / Math.PI) + 1;
		if (Math.abs(ang) < SMALL_NUM)
			return 0;
		double ainc = angle / n * (ccw ? 1 : -1);
		double w = btan(ainc / 2);
		double s = Math.sin(ang1);
		double c = Math.cos(ang1);
		result[0] = new Point2D.Double(c, s);
		int m = 3 * n;
		for (int i = 1; i < m; i += 3) {
			result[i] = new Point2D.Double(c - w * s, s + w * c);
			ang1 += ainc;
			s = Math.sin(ang1);
			c = Math.cos(ang1);
			result[i+1] = new Point2D.Double(c + w * s, s - w * c);
			result[i+2] = new Point2D.Double(c, s);
		}
		return n;
	}

	private static double ang360(double a) {
		if (a >= TWO_PI)
			return TWO_PI;
		else if (a <= -TWO_PI)
			return -TWO_PI;
		return a;
	}

	private static double angDiff(double ang1, double ang2) {
		return (ang2 >= ang1) ? ang2 - ang1 : ang2 + TWO_PI - ang1;
	}

	private static double btan(double alpha) {
		double a = 1 - Math.cos(alpha);
		double b = Math.tan(alpha);
		double c = Math.sqrt(1 + b*b) - 1 + a;
		return 4.0 / 3.0 * a * b / c;
	}

	public void arc(Point2D center, double r, double ang1, double ang2, boolean ccw) {
		Point2D points[] = new Point2D[13];
		int n = arcToCurve(ang1, ang2, ccw, points);
		if (n <= 0) {
			return;
		}
		int m = 3 * n + 1;
		AffineTransform xform = (AffineTransform) ctm.clone();
		xform.translate(center.getX(), center.getY());
		xform.scale(r, r);
		xform.transform(points, 0, points, 0, m);
		float x0 = (float) points[0].getX();
		float y0 = (float) points[0].getY();
		if (currentpoint != null && (currentpoint.getX() != x0 || currentpoint.getY() != y0)) {
			currentpath.lineTo(x0, y0);
			currentpoint.setLocation(x0, y0);
		} else {
			currentpath.moveTo(x0, y0);
			currentpoint = new Point2D.Double(x0, y0);
		}
		int i, j, k;
		for (i = 1, j = 2, k = 3; i < m; i += 3, j += 3, k += 3) {
			currentpath.curveTo(
				(float) points[i].getX(), (float) points[i].getY(),
				(float) points[j].getX(), (float) points[j].getY(),
				(float) points[k].getX(), (float) points[k].getY()
			);
		}
		currentpoint.setLocation(points[n-1]);
	}

	public void arct(Point2D pt1, Point2D pt2, double r) {
		if (currentpoint == null)
			throw new Stop(NOCURRENTPOINT, "arct");
		arcto(pt1, pt2, r);
	}

	public double[] arcto(Point2D pt1, Point2D pt2, double r) {
		if (currentpoint == null)
			throw new Stop(NOCURRENTPOINT, "arcto");
		Point2D pt0 = currentpoint();
		double p0x = pt0.getX();
		double p0y = pt0.getY();
		double p1x = pt1.getX();
		double p1y = pt1.getY();
		double p2x = pt2.getX();
		double p2y = pt2.getY();
		double v0x = p0x - p1x;
		double v0y = p0y - p1y;
		double v1x = p2x - p1x;
		double v1y = p2y - p1y;
/*
System.err.println("arcto pt0: " + pt0);
System.err.println("arcto pt1: " + pt1);
System.err.println("arcto pt2: " + pt2);
System.err.println("arcto v0: " + v0x + "," + v0y);
System.err.println("arcto v1: " + v1x + "," + v1y);
*/
		double alpha = Math.atan2(v0y*v1x - v0x*v1y, v0x*v1x + v0y*v1y);
//System.err.println("arcto dy: " + (v0y*v1x - v0x*v1y) + " dx: " + (v0x*v1x + v0y*v1y));
		double a = 1.0 / Math.tan(alpha / 2) * r;
		double f = a / Math.sqrt(v0x*v0x + v0y*v0y);
		double g = a / Math.sqrt(v1x*v1x + v1y*v1y);
//System.err.println("arcto a: " + a + " alpha: " + alpha);
		double v0px = v0x * f;
		double v0py = v0y * f;
		double v1px = v1x * g;
		double v1py = v1y * g;
		double p0tx = p1x + v0px;
		double p0ty = p1y + v0py;
		double p1tx = p1x + v1px;
		double p1ty = p1y + v1py;
		double b = Math.sqrt(a*a + r*r);
		double wx = (p1tx - p0tx) / 2;
		double wy = (p1ty - p0ty) / 2;
		double ux = v0px + wx;
		double uy = v0py + wy;
		double h = b / Math.sqrt(ux*ux + uy*uy);
//System.err.println("arcto b: " + b + " h: " + h);
		double mx = p1x + ux * h;
		double my = p1y + uy * h;
		double v0ppx = p0tx - mx;
		double v0ppy = p0ty - my;
		double v1ppx = p1tx - mx;
		double v1ppy = p1ty - my;
//System.err.println("arcto atan2: " + v0ppy + " " + v0ppx);
//System.err.println("arcto atan2: " + v1ppy + " " + v1ppx);
		double ang0 = Math.atan2(v0ppy, v0ppx);
		double ang1 = Math.atan2(v1ppy, v1ppx);
//System.err.println("arcto angle: " + ang0 + " " + ang1 + " r: " + r);
		arc(new Point2D.Double(mx, my), r, ang0, ang1, true);
		if ((v1px*v1px + v1py*v1py) > 0) {
			lineto(p2x, p2y);
		}
		double result[] = new double[4];
		result[0] = p0tx;
		result[1] = p0ty;
		result[2] = p1tx;
		result[3] = p1ty;
		return result;
	}

	public void closepath() {
		if (currentpoint == null)
			return;
		currentpath.closePath();
	}

	public void setFontDecoder(FontDecoder fontdecoder) {
		this.fontdecoder = fontdecoder;
	}

	public FontDecoder getFontDecoder() {
		return fontdecoder;
	}

	public void setRequestBitmapWidths(Interpreter ip, Any bool) {
		if (!(bool instanceof BoolType))
			throw new Stop(TYPECHECK, "invalid bitmap font request");
		bitmapwidths = ((BoolType) bool).booleanValue();
	}

	public boolean getRequestBitmapWidths() {
		return bitmapwidths;
	}

	public void setSystemFonts(Interpreter ip, Any assoc) {
		if (!(assoc instanceof DictType))
			throw new Stop(TYPECHECK, "invalid bitmap font association");
		systemfontdict = (DictType) assoc;
	}

	public DictType getSystemFonts() {
		return systemfontdict;
	}

	public void show(Interpreter ip, StringType string) {
		checkShowState();
		Enumeration e = string.elements();
		while (e.hasMoreElements()) {
			int c = ((IntegerType) e.nextElement()).intValue();
			CharWidth cw = fontdecoder.show(ip, c);
			rmoveto(cw.getDeltaX(), cw.getDeltaY());
		}
	}

	public void ashow(Interpreter ip, double ax, double ay, StringType string) {
		checkShowState();
		Enumeration e = string.elements();
		while (e.hasMoreElements()) {
			int c = ((IntegerType) e.nextElement()).intValue();
			CharWidth cw = fontdecoder.show(ip, c);
			rmoveto(ax + cw.getDeltaX(), ay + cw.getDeltaY());
		}
	}

	public void widthshow(Interpreter ip, double cx, double cy, int c, StringType string) {
		checkShowState();
		Enumeration e = string.elements();
		while (e.hasMoreElements()) {
			int cc = ((IntegerType) e.nextElement()).intValue();
			CharWidth cw = fontdecoder.show(ip, cc);
			if (c == cc) {
				rmoveto(cx + cw.getDeltaX(), cy + cw.getDeltaY());
			} else {
				rmoveto(cw.getDeltaX(), cw.getDeltaY());
			}
		}
	}

	public void awidthshow(Interpreter ip, double cx, double cy, int c, double ax, double ay, StringType string) {
		checkShowState();
		Enumeration e = string.elements();
		while (e.hasMoreElements()) {
			int cc = ((IntegerType) e.nextElement()).intValue();
			CharWidth cw = fontdecoder.show(ip, cc);
			if (c == cc) {
				rmoveto(ax + cx + cw.getDeltaX(), ay + cy + cw.getDeltaY());
			} else {
				rmoveto(ax + cw.getDeltaX(), ay + cw.getDeltaY());
			}
		}
	}

	public void kshow(Interpreter ip, ArrayType proc, StringType string) {
		checkShowState();
		Enumeration e = string.elements();
		int i = 0, c0 = 0, c1;
		while (e.hasMoreElements()) {
			c1 = ((IntegerType) e.nextElement()).intValue();
			if (i++ >= 1) {
				ip.ostack.pushRef(new IntegerType(c0));
				ip.ostack.pushRef(new IntegerType(c1));
				ip.estack.run(ip, proc);
			}
			CharWidth cw = fontdecoder.show(ip, c1);
			rmoveto(cw.getDeltaX(), cw.getDeltaY());
			c0 = c1;
		}
	}

	public void cshow(Interpreter ip, ArrayType proc, StringType string) {
		checkShowState();
		Enumeration e = string.elements();
		while (e.hasMoreElements()) {
			int c = ((IntegerType) e.nextElement()).intValue();
			CharWidth cw = fontdecoder.charwidth(ip, c);
			ip.ostack.pushRef(new IntegerType(c));
			ip.ostack.pushRef(new RealType(cw.getDeltaX()));
			ip.ostack.pushRef(new RealType(cw.getDeltaY()));
			ip.estack.run(ip, proc);
			// TODO:
		}
	}

	public void xshow(Interpreter ip, StringType string, Any displacement) {
		checkShowState();
		Enumeration e = string.elements();
		while (e.hasMoreElements()) {
			int c = ((IntegerType) e.nextElement()).intValue();
			CharWidth cw = fontdecoder.show(ip, c);
			// TODO:
			rmoveto(cw.getDeltaX(), cw.getDeltaY());
		}
	}

	public void yshow(Interpreter ip, StringType string, Any displacement) {
		checkShowState();
		Enumeration e = string.elements();
		while (e.hasMoreElements()) {
			int c = ((IntegerType) e.nextElement()).intValue();
			CharWidth cw = fontdecoder.show(ip, c);
			// TODO:
			rmoveto(cw.getDeltaX(), cw.getDeltaY());
		}
	}

	public void xyshow(Interpreter ip, StringType string, Any displacement) {
		checkShowState();
		Enumeration e = string.elements();
		while (e.hasMoreElements()) {
			int c = ((IntegerType) e.nextElement()).intValue();
			CharWidth cw = fontdecoder.show(ip, c);
			// TODO:
			rmoveto(cw.getDeltaX(), cw.getDeltaY());
		}
	}

	public float[] stringwidth(Interpreter ip, StringType string) {
		if (fontdecoder == null)
			throw new Stop(INVALIDFONT, "no current font");
		float result[] = new float[2];
		Enumeration e = string.elements();
		while (e.hasMoreElements()) {
			int c = ((IntegerType) e.nextElement()).intValue();
			CharWidth cw = fontdecoder.charwidth(ip, c);
			result[0] += cw.getDeltaX();
			result[1] += cw.getDeltaY();
		}
		return result;
	}

	public void charpath(Interpreter ip, StringType string, boolean stroked) {
		checkShowState();
		Enumeration e = string.elements();
		while (e.hasMoreElements()) {
			int c = ((IntegerType) e.nextElement()).intValue();
			CharWidth cw = fontdecoder.charpath(ip, c);
			rmoveto(cw.getDeltaX(), cw.getDeltaY());
		}
//		if (stroked) {
//			appendShape(currentpath, new GeneralPath(createStroke().createStrokedShape(currentpath)));
//		}
	}

	public void checkShowState() {
		if (currentpoint == null)
			throw new Stop(NOCURRENTPOINT, "checkShowState");
		if (fontdecoder == null)
			throw new Stop(INTERNALERROR, "no current font");
	}

	public void image(
		Interpreter ip,
		int width,
		int height,
		int bits,
		AffineTransform ximg,
		ArrayType proc
	) {
		Bitmap bitmap;
		switch (getColorSpace()) {
		case ColorSpace.TYPE_GRAY:
			GrayPixelSource gps = PixelSourceFactory.createGrayPixelSource(ip, proc, bits);
			bitmap = createBitmap(ip, width, height, gps);
			break;
		case ColorSpace.TYPE_RGB:
			RGBPixelSource cps = PixelSourceFactory.createRGBPixelSource(ip, proc, bits);
			bitmap = createColorBitmap(ip, width, height, cps);
			break;
		default:
			throw new Stop(INTERNALERROR, "unsupported colorspace");
		}
		AffineTransform xform = createImageXform(ximg);
		currentdevice.image(bitmap, xform);
	}

	public void imagemask(
		Interpreter ip,
		int width,
		int height,
		int bits,
		boolean polarity,
		AffineTransform ximg,
		ArrayType proc
	) {
		GrayPixelSource ps = PixelSourceFactory.createGrayPixelSource(ip, proc, bits);
		Bitmap bitmap = createBitmapMask(ip, width, height, polarity, ps);
		AffineTransform xform = createImageXform(ximg);
		currentdevice.image(bitmap, xform);
	}

	public void image(
		Interpreter ip,
		int width,
		int height,
		int bits,
		AffineTransform ximg,
		CharStream src
	) {
		Bitmap bitmap;
		switch (getColorSpace()) {
		case ColorSpace.TYPE_GRAY:
			GrayPixelSource gps = PixelSourceFactory.createGrayPixelSource(src, bits);
			bitmap = createBitmap(ip, width, height, gps);
			break;
		case ColorSpace.TYPE_RGB:
			RGBPixelSource cps = PixelSourceFactory.createRGBPixelSource(src, bits);
			bitmap = createColorBitmap(ip, width, height, cps);
			break;
		default:
			throw new Stop(INTERNALERROR, "unsupported colorspace");
		}
		AffineTransform xform = createImageXform(ximg);
		currentdevice.image(bitmap, xform);
	}

	public void imagemask(
		Interpreter ip,
		int width,
		int height,
		int bits,
		boolean polarity,
		AffineTransform ximg,
		CharStream src
	) {
		Bitmap bitmap;
		switch (getColorSpace()) {
		case ColorSpace.TYPE_GRAY:
			GrayPixelSource gps = PixelSourceFactory.createGrayPixelSource(src, bits);
			bitmap = createBitmapMask(ip, width, height, polarity, gps);
			break;
		case ColorSpace.TYPE_RGB:
			RGBPixelSource cps = PixelSourceFactory.createRGBPixelSource(src, bits);
			bitmap = createColorBitmapMask(ip, width, height, polarity, cps);
			break;
		default:
			throw new Stop(INTERNALERROR, "unsupported colorspace");
		}
		AffineTransform xform = createImageXform(ximg);
		currentdevice.image(bitmap, xform);
	}

	public void colorimage(
		Interpreter ip, int w, int h, int bits, int ncomp,
		boolean multi, AffineTransform ximg, Any src[]
	) {
		RGBPixelSource ps;
		Bitmap bitmap;
		switch (ncomp) {
		case 1:
			if (src[0] instanceof ArrayType) {
				if (multi) {
					ps = PixelSourceFactory.createRGBPixelSource(ip, src, bits);
				} else {
					ps = PixelSourceFactory.createRGBPixelSource(ip, src[0], bits);
				}
			} else if (src[0] instanceof CharStream) {
				if (multi) {
					ps = PixelSourceFactory.createRGBPixelSource((CharStream[]) src, bits);
				} else {
					ps = PixelSourceFactory.createRGBPixelSource((CharStream) src[0], bits);
				}
			} else {
				throw new Stop(TYPECHECK, "bad source");
			}
			bitmap = createColorBitmap(ip, w, h, ps);
			break;
		case 3:
			if (arrayTypeCheck(src, ArrayType.class)) {
				if (multi) {
					ps = PixelSourceFactory.createRGBPixelSource(ip, src, bits);
				} else {
					ps = PixelSourceFactory.createRGBPixelSource(ip, src[0], bits);
				}
			} else if (arrayTypeCheck(src, CharStream.class)) {
				if (multi) {
					ps = PixelSourceFactory.createRGBPixelSource((CharStream[]) src, bits);
				} else {
					ps = PixelSourceFactory.createRGBPixelSource((CharStream) src[0], bits);
				}
			} else {
				throw new Stop(TYPECHECK, "bad RGB source");
			}
			bitmap = createColorBitmap(ip, w, h, ps);
			break;
		case 4:
			CMYKPixelSource cps;
			if (arrayTypeCheck(src, ArrayType.class)) {
				if (multi) {
					cps = PixelSourceFactory.createCMYKPixelSource(ip, src, bits);
				} else {
					cps = PixelSourceFactory.createCMYKPixelSource(ip, src[0], bits);
				}
			} else if (arrayTypeCheck(src, CharStream.class)) {
				if (multi) {
					cps = PixelSourceFactory.createCMYKPixelSource((CharStream[]) src, bits);
				} else {
					cps = PixelSourceFactory.createCMYKPixelSource((CharStream) src[0], bits);
				}
			} else {
				throw new Stop(TYPECHECK, "bad CMYK source");
			}
			bitmap = createColorBitmap(ip, w, h, cps);
			break;
		default:
			throw new Stop(UNDEFINEDRESULT);
		}
		AffineTransform xform = createImageXform(ximg);
		currentdevice.image(bitmap, xform);
	}

	private Bitmap createBitmap(Interpreter ip, int w, int h, GrayPixelSource ps) {
		Bitmap bitmap = currentdevice.createBitmap(w, h);
		int x = 0; int y = 0, i = 0, count = w * h;
		try {
			while (i < count) {
				int pixel = ps.nextPixel();
				if (pixel < 0)
					break;
				if (currentgraytransfer != null) {
					ip.ostack.push(new RealType((float) pixel / 255f));
					ip.estack.run(ip, currentgraytransfer);
					pixel = Math.round(((NumberType) ip.ostack.pop(NUMBER)).floatValue() * 255f);
				}
				bitmap.draw(x, y, 0xff000000 | (pixel << 16) | (pixel << 8) | pixel);
				if (++x >= w) {
					x = 0;
					y++;
				}
				i++;
			}
		} catch (IOException ex) {
			throw new Stop(IOERROR, "createBitmap");
		}
		return bitmap;
	}

	private Bitmap createBitmapMask(
		Interpreter ip, int w, int h, boolean polarity, GrayPixelSource ps
	) {
		w = (w + 7) / 8 * 8;	// some type-3 fonts rely on that
		Bitmap bitmap = currentdevice.createBitmap(w, h);
		int x = 0; int y = 0, i = 0, count = w * h;
		int curcolor = getRGB();
		try {
			while (i < count) {
				int pixel = ps.nextPixel();
				if (pixel < 0)
					break;
				if ((pixel != 0) == polarity) {
					bitmap.draw(x, y, curcolor);
				}
				if (++x >= w) {
					x = 0;
					y++;
				}
				i++;
			}
		} catch (IOException ex) {
			throw new Stop(IOERROR, "createBitmapMask");
		}
		return bitmap;
	}

	private Bitmap createColorBitmap(Interpreter ip, int w, int h, RGBPixelSource ps) {
		Bitmap bitmap = currentdevice.createBitmap(w, h);
		int x = 0; int y = 0, i = 0, count = w * h;
		int curcolor = getRGB();
		try {
			while (i < count) {
				int r = ps.nextRedComponent();
				if (r < 0)
					break;
				int g = ps.nextGreenComponent();
				if (g < 0)
					break;
				int b = ps.nextBlueComponent();
				if (b < 0)
					break;
				if (currentredtransfer != null) {
					ip.ostack.push(new RealType((float) r / 255f));
					ip.estack.run(ip, currentredtransfer);
					float val = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
					r = Math.round(val * 255f) & 0xff;
				}
				if (currentgreentransfer != null) {
					ip.ostack.push(new RealType((float) g / 255f));
					ip.estack.run(ip, currentgreentransfer);
					float val = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
					g = Math.round(val * 255f) & 0xff;
				}
				if (currentbluetransfer != null) {
					ip.ostack.push(new RealType((float) b / 255f));
					ip.estack.run(ip, currentbluetransfer);
					float val = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
					b = Math.round(val * 255f) & 0xff;
				}
				bitmap.draw(x, y, 0xff000000 | (r << 16) | (g << 8) | b);
				if (++x >= w) {
					x = 0;
					y++;
				}
				i++;
			}
		} catch (IOException ex) {
			throw new Stop(IOERROR, ex.getMessage());
		}
		return bitmap;
	}

	private Bitmap createColorBitmapMask(
		Interpreter ip, int w, int h, boolean polarity, RGBPixelSource ps
	) {
		Bitmap bitmap = currentdevice.createBitmap(w, h);
		int x = 0; int y = 0, i = 0, count = w * h;
		int curcolor = getRGB();
		try {
			while (i < count) {
				int r = ps.nextRedComponent();
				if (r < 0)
					break;
				int g = ps.nextGreenComponent();
				if (g < 0)
					break;
				int b = ps.nextBlueComponent();
				if (b < 0)
					break;
				bitmap.draw(x, y, 0xff000000 | r | g | b);
				if (++x >= w) {
					x = 0;
					y++;
				}
				i++;
			}
		} catch (IOException ex) {
			throw new Stop(IOERROR, "createColorBitmapMask");
		}
		return bitmap;
	}

	private Bitmap createColorBitmap(Interpreter ip, int w, int h, CMYKPixelSource ps) {
		Bitmap bitmap = currentdevice.createBitmap(w, h);
		int x = 0; int y = 0, i = 0, count = w * h;
		try {
			while (i < count) {
				int cyan = ps.nextCyanComponent();
				if (cyan < 0)
					break;
				int magenta = ps.nextMagentaComponent();
				if (magenta < 0)
					break;
				int yellow = ps.nextYellowComponent();
				if (yellow < 0)
					break;
				int black = ps.nextBlackComponent();
				if (black < 0)
					break;
				Color color = convertCMYK2RGB(cyan, magenta, yellow, black);
				bitmap.draw(x, y, color);
				if (++x >= w) {
					x = 0;
					y++;
				}
				i++;
			}
		} catch (IOException ex) {
			throw new Stop(IOERROR, "createColorBitmap");
		}
		return bitmap;
	}

	private static Color convertCMYK2RGB(int c, int m, int y, int k) {
		float c0 = c / 255f;
		float m0 = m / 255f;
		float y0 = y / 255f;
		float k0 = k / 255f;
		float r = 1f - (c0 * (1 - k0) + k0);
		float g = 1f - (m0 * (1 - k0) + k0);
		float b = 1f - (y0 * (1 - k0) + k0);
		return new Color(r, g, b);
	}

	private boolean arrayTypeCheck(Object array[], Class type) {
		for (int i = 0; i < array.length; i++) {
			if (!type.isInstance(array[i]))
				return false;
		}
		return true;
	}

	private AffineTransform createImageXform(AffineTransform ximg) {
		AffineTransform xinv = createInverse(ximg);
		AffineTransform xform = (AffineTransform) ctm.clone();
		xform.concatenate(xinv);
		return xform;
	}

	public void fill(int rule) {
		currentpath.setWindingRule(rule);
		if (currentflat != DEFAULT_FLATNESS) {
			currentdevice.fill(createFlatShape(currentpath, currentflat));
		} else {
			currentdevice.fill(currentpath);
		}
		newpath();
	}

	public void rectfill(float llx, float lly, float width, float height) {
		currentdevice.fill(createTransformedRect(llx, lly, width, height));
	}

	public void stroke() {
		if (strokeChanged) {
			Stroke stroke = createStroke();
			if (stroke != null) {
				currentdevice.setStroke(stroke);
			}
			strokeChanged = false;
		}
		if (currentflat != DEFAULT_FLATNESS) {
			currentdevice.stroke(createFlatShape(currentpath, currentflat));
		} else {
			currentdevice.stroke(currentpath);
		}
		newpath();
	}

	public void rectstroke(float llx, float lly, float width, float height) {
		currentdevice.stroke(createTransformedRect(llx, lly, width, height));
	}

	private static Shape createFlatShape(Shape shape, float flatness) {
		GeneralPath path = new GeneralPath();
		float flatfix = flatness / 20;	// TODO: this fixes "poster.ps"
		path.append(shape.getPathIterator(IDENT_MATRIX, flatfix), false);
		return path;
	}

	public Stroke createStroke() {
		float width = transformLength(currentwidth);
		float array[] = transformLength(currentdasharray);
		float phase = transformLength(currentdashphase);
		return currentdevice.createStroke(
			width, currentcap, currentjoin, currentmiterlimit, array, phase
		);
	}

	public void strokepath() {
		Stroke stroke = createStroke();
		currentpath = new GeneralPath(stroke.createStrokedShape(currentpath));
	}

	public double[] pathbbox() {
		if (currentpoint == null)
			throw new Stop(NOCURRENTPOINT);
		AffineTransform ixform;
		ixform = createInverse(ctm);
		Rectangle2D box = currentpath.getBounds2D();
		double x = box.getX();
		double y = box.getY();
		double w = box.getWidth();
		double h = box.getHeight();
		Point2D lb = new Point2D.Double(x, y+h);
		Point2D rt = new Point2D.Double(x + w, y);
		ixform.transform(lb, lb);
		ixform.transform(rt, rt);
		double result[] = new double[4];
		result[0] = lb.getX();
		result[1] = lb.getY();
		result[2] = rt.getX();
		result[3] = rt.getY();
		return result;
	}

	private float transformLength(double length) {
		Point2D v = ctm.deltaTransform(new Point2D.Double(length, 0), null);
		double x = v.getX();
		double y = v.getY();
		return (float) Math.sqrt(x * x + y * y);
	}

	public float[] transformLength(ArrayType array) {
		if (array == null || array.length() == 0)
			return null;
		int i, n = array.length();
		float result[] = new float[n];
		for (i = 0; i < n; i++) {
			result[i] = transformLength(((NumberType) array.get(i)).realValue());
		}
		return result;
	}

	public void setflat(float tol) {
		currentflat = tol;
	}

	public float currentflat() {
		return currentflat;
	}

	public void setlinewidth(float width) {
		strokeChanged |= width != currentwidth;
		currentwidth = width;
	}

	public double currentlinewidth() {
		return currentwidth;
	}

	public void setlinecap(int cap) {
		strokeChanged |= cap != currentcap;
		currentcap = cap;
	}

	public int currentlinecap() {
		return currentcap;
	}

	public void setlinejoin(int join) {
		strokeChanged |= join != currentjoin;
		currentjoin = join;
	}

	public int currentlinejoin() {
		return currentjoin;
	}

	public void setmiterlimit(float limit) {
		strokeChanged |= limit != currentmiterlimit;
		currentmiterlimit = limit;
	}

	public float currentmiterlimit() {
		return currentmiterlimit;
	}

	public void setStrokeAdjustment(boolean strokeAdjustment) {
		this.strokeAdjustment = strokeAdjustment;
	}

	public boolean getStrokeAdjustment() {
		return strokeAdjustment;
	}

	public void setOverprint(boolean overprint) {
		this.overprint = overprint;
	}

	public boolean getOverprint() {
		return overprint;
	}

	public void setdash(ArrayType array, float phase) {
		if (array == null) {
			strokeChanged |= currentdasharray != null && currentdasharray.length() > 0;
			currentdasharray = null;
			currentdashphase = 0;
		} else if (currentdasharray == null) {
			strokeChanged = true;
			currentdasharray = array;
			currentdashphase = phase;
		} else {
			int arraylen = array.length();
			if (arraylen > 0) {
				int currentlen = currentdasharray.length();
				strokeChanged |= arraylen != currentlen;
				for (int i = 0;  !strokeChanged && i < arraylen; i++) {
					strokeChanged = array.get(i) != currentdasharray.get(i);
				}
				strokeChanged |= phase != currentdashphase;
				if (!strokeChanged) {
					currentdasharray = array;
					currentdashphase = phase;
				}
			} else {
				currentdasharray = null;
				currentdashphase = phase;
				strokeChanged = true;
			}
		}
	}

	public double currentdashphase() {
		return currentdashphase;
	}

	public ArrayType currentdasharray() {
		return currentdasharray;
	}

	public void setColorScreen(Screen red, Screen green, Screen blue) {
		currentredscreen = red;
		currentgreenscreen = green;
		currentbluescreen = blue;
	}

	public Screen currentredscreen() {
		return currentredscreen != null ? currentredscreen : NULL_SCREEN;
	}

	public Screen currentgreenscreen() {
		return currentgreenscreen != null ? currentgreenscreen : NULL_SCREEN;
	}

	public Screen currentbluescreen() {
		return currentbluescreen != null ? currentbluescreen : NULL_SCREEN;
	}

	public void setHalftonePhase(int x, int y) {
		halftonePhaseX = x;
		halftonePhaseY = y;
	}

	public int[] getHalftonePhase() {
		int phase[] = new int[2];
		phase[0] = halftonePhaseX;
		phase[1] = halftonePhaseY;
		return phase;
	}

	public void setHalftone(DictType halftone) {
		this.halftone = halftone;
/*
		int type = ((IntegerType) halftone.get("HalftoneType", INTEGER)).intValue();
		if (type != 1) // TODO: halftone type 3
			return;
		double freq = ((NumberType) halftone.get("Frequency", NUMBER)).realValue();
		if (freq <= 1e-4)
			throw new Stop(TYPECHECK, "Frequency: " + freq);
		double angle = ((NumberType) halftone.get("Angle", NUMBER)).realValue();
		ArrayType proc = (ArrayType) halftone.get("SpotFunction", ARRAY);
		double res = currentdevice.getResolution();
		double scale = currentdevice.getScale();
		double dpi = res * scale;
		if (dpi <= 1)
			throw new Stop(INTERNALERROR, "dpi: " + dpi);
		double phi = Math.IEEEremainder(angle + 45, 90);
		double rad = phi * Math.PI / 180;
		double dots = dpi * Math.sqrt(2) * Math.cos(rad) / freq;
		double width = dots - 1;
		int ndots = ((int) Math.abs(width) - 1) | 1; // make sure ndots is odd
		if (ndots <= 1)
			throw new Stop(TYPECHECK, "ndots: " + ndots);
		double step = 2.0 / width;
		double norm = width / dots;
		double dy = -norm;
System.out.println("ndots: " + ndots + " dpi: " + dpi + " step: " + step + " norm: " + norm);
		for (int j = 0; j < ndots; j++) {
			for (int i = 0; i < ndots; i++) {
			}
		}
*/
	}

	public DictType getHalftone() {
		return halftone;
	}

	public void setPattern(DictType halftone) {
		this.pattern = halftone;
	}

	public DictType getPattern() {
		return pattern;
	}

	public int getRGB() {
		Color color = getColor();
		return color != null ? color.getRGB() : 0;
	}

	public Color getColor() {
		Color color;
		switch (getColorSpace()) {
		case ColorSpace.TYPE_GRAY:
			color = currentdevice.createColor(currentgray, currentgray, currentgray);
			break;
		default:
			color = currentdevice.createColor(currentred, currentgreen, currentblue);
			break;
		}
		return color;
	}

	public void setgray(float gray) {
		currentgray = gray > 1 ? 1 : (gray < 0 ? 0 : gray);
		Color color = currentdevice.createColor(currentgray, currentgray, currentgray);
		if (color != null) {
			currentdevice.setColor(color);
		}
		setColorSpace(ColorSpace.CS_GRAY);
	}

	public float currentgray() {
		return currentgray;
	}

	public void setrgbcolor(float red, float green, float blue) {
		currentred = red > 1 ? 1 : (red < 0 ? 0 : red);
		currentgreen = green > 1 ? 1 : (green < 0 ? 0 : green);
		currentblue = blue > 1 ? 1 : (blue < 0 ? 0 : blue);
		Color color = currentdevice.createColor(currentred, currentgreen, currentblue);
		if (color != null) {
			currentdevice.setColor(color);
		}
		setColorSpace(ColorSpace.CS_sRGB);
	}

	public float[/* 3 */] currentrgbcolor() {
		float color[] = new float[3];
		color[0] = currentred;
		color[1] = currentgreen;
		color[2] = currentblue;
		return color;
	}

	public void sethsbcolor(float h, float s, float b) {
		Color color = Color.getHSBColor(h, s, b);
		currentred = color.getRed() / 255f;
		currentgreen = color.getGreen() / 255f;
		currentblue = color.getBlue() / 255f;
		currentdevice.setColor(color);
		setColorSpace(ColorSpace.CS_sRGB);
	}

	public float[/* 3 */] currenthsbcolor() {
		int red = (int) (currentred * 255);
		int green = (int) (currentgreen * 255);
		int blue = (int) (currentblue * 255);
		return Color.RGBtoHSB(red, green, blue, null);
	}

	public void setcmykcolor(float cyan, float magenta, float yellow, float black) {
		currentred = 1f - Math.min(1f, cyan + black);
		currentgreen = 1f - Math.min(1f, magenta + black);
		currentblue = 1f - Math.min(1f, yellow + black);
		Color color = currentdevice.createColor(currentred, currentgreen, currentblue);
		if (color != null) {
			currentdevice.setColor(color);
		}
		setColorSpace(ColorSpace.CS_sRGB);
	}

	public float[/* 4 */] currentcmykcolor() {
		float color[] = new float[4];
		color[0] = 1f - currentred;
		color[1] = 1f - currentgreen;
		color[2] = 1f - currentblue;
		color[3] = 1f - Math.min(Math.min(color[0], color[1]), color[2]);
		return color;
	}

	public void setColorRendering(DictType colorRendering) {
		this.colorRendering = colorRendering;
	}

	public DictType getColorRendering() {
		return colorRendering;
	}

	public void settransfer(ArrayType proc) {
		currentgraytransfer = proc.length() > 0 ? proc : null;
	}

	public ArrayType currenttransfer() {
		return currentgraytransfer;
	}

	public void setcolortransfer(ArrayType red, ArrayType green, ArrayType blue, ArrayType gray) {
		currentredtransfer = red.length() > 0 ? red : null;
		currentgreentransfer = green.length() > 0 ? green : null;
		currentbluetransfer = blue.length() > 0 ? blue : null;
		currentgraytransfer = gray.length() > 0 ? gray : null;
	}

	public ArrayType currentredtransfer() {
		return currentredtransfer;
	}

	public ArrayType currentgreentransfer() {
		return currentgreentransfer;
	}

	public ArrayType currentbluetransfer() {
		return currentbluetransfer;
	}

	public void setblackgeneration(ArrayType proc) {
		currentblackgeneration = proc;
	}

	public ArrayType currentblackgeneration() {
		return currentblackgeneration;
	}

	public void setundercolorremoval(ArrayType proc) {
		currentundercolorremoval = proc;
	}

	public ArrayType currentundercolorremoval() {
		return currentundercolorremoval;
	}

	public void setColorSpace(int colorspace) {
		this.colorspace = ColorSpace.getInstance(colorspace);
	}

	public int getColorSpace() {
		return colorspace.getType();
	}

	public void initclip(Device device) {
		device.initclip();
	}

	public void clippath() {
		currentpath.reset();
		appendShape(currentpath, currentdevice.clippath());
		currentpoint = currentpath.getCurrentPoint();
	}

	private static void appendShape(GeneralPath path, Shape shape) {
		float coords[] = new float[6];
		if (shape != null) {
			PathIterator e = shape.getPathIterator(IDENT_MATRIX);
			while (!e.isDone()) {
				switch (e.currentSegment(coords)) {
				case PathIterator.SEG_MOVETO:
					path.moveTo(coords[0], coords[1]);
					break;
				case PathIterator.SEG_LINETO:
					path.lineTo(coords[0], coords[1]);
					break;
				case PathIterator.SEG_CUBICTO:
					path.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
					break;
				case PathIterator.SEG_CLOSE:
					path.closePath();
					break;
				}
				e.next();
			}
		}
	}

	private static Shape reduceShape(Shape shape) {
		if (shape instanceof Rectangle2D)
			return shape;
		boolean simple = true;
		int i = 0;
		float rect[] = new float[8];
		float coords[] = new float[6];
		PathIterator e = shape.getPathIterator(IDENT_MATRIX);
		while (!e.isDone()) {
			switch (e.currentSegment(coords)) {
			case PathIterator.SEG_MOVETO:
				if (i > 0)
					return shape;
				rect[i++] = coords[0];
				rect[i++] = coords[1];
				break;
			case PathIterator.SEG_LINETO:
				if (i >= 8)
					return shape;
				rect[i++] = coords[0];
				rect[i++] = coords[1];
				break;
			case PathIterator.SEG_CUBICTO:
				return shape;
			case PathIterator.SEG_CLOSE:
				break;
			}
			e.next();
		}
		if (i != 8)
			return shape;
		for (i = 0; i < 4; i++) {
			int j = i + 4;
			if (rect[i] > rect[j]) {
				float tmp = rect[i];
				rect[i] = rect[j];
				rect[j] = tmp;
			}
		}
		if (Math.abs(rect[0] - rect[2]) < SMALL_NUM && Math.abs(rect[1] - rect[3]) < SMALL_NUM &&
			Math.abs(rect[4] - rect[6]) < SMALL_NUM && Math.abs(rect[5] - rect[7]) < SMALL_NUM)
		{
			return new Rectangle2D.Float(rect[0], rect[1], rect[4] - rect[0], rect[5] - rect[1]);
		}
		return shape;
	}

	public void clip(int rule) {
		currentpath.setWindingRule(rule);
		currentdevice.clip(reduceShape(currentpath));
	}

	public void rectclip(float llx, float lly, float width, float height) {
		currentdevice.clip(createTransformedRect(llx, lly, width, height));
		newpath();
	}

	public void rectclip(float array[]) {
		newpath();
		currentpath.setWindingRule(GeneralPath.WIND_NON_ZERO);
		int i, n = array.length - 3;
		for (i = 0; i < n; i += 4) {
			moveto(array[0], array[1]);
			lineto(array[2], array[1]);
			lineto(array[2], array[3]);
			lineto(array[0], array[3]);
			closepath();
		}
		currentdevice.clip(currentpath);
		newpath();
	}

	public void nulldevice() {
		setdevice(nulldevice);
	}

	public void setcharwidth(float wx, float wy) {
		fontdecoder.setCharWidth(new CharWidth(0, 0, wx, wy));
	}

	public void setcachedevice(CharWidth cw, Rectangle2D box) {
		setcachedevice(cw, box.getX(), box.getY(), box.getWidth(), box.getHeight());
	}

	private void setcachedevice(CharWidth cw, double llx, double lly, double urx, double ury) {
		Point2D ll = new Point2D.Double(llx, ury);
		Point2D ur = new Point2D.Double(urx, lly);
		ctm.transform(ll, ll);
		ctm.transform(ur, ur);
		double x = ll.getX();
		double y = ll.getY();
		double w = ur.getX() - x;
		double h = ur.getY() - y;
		fontdecoder.setCharWidth(cw);
		cachedevice.setCharWidth(cw);
		cachedevice.setCharBounds(new Rectangle2D.Double(x, y, w, h));
		cachedevice.setTarget(currentdevice);
		setdevice(cachedevice);
	}

	public void setcachedevice2(double w0x, double w0y, double llx, double lly, double urx, double ury, double w1x, double w1y, double vx, double vy) {
	}

	private static AffineTransform createInverse(AffineTransform xform) {
		try {
			return xform.createInverse();
		} catch (NoninvertibleTransformException ex) {
			throw new Stop(UNDEFINEDRESULT, "non invertible matrix");
		}
	}

	private Rectangle2D createTransformedRect(float llx, float lly, float width, float height) {
		Point2D p = transform(llx, lly);
		Point2D v = deltaTransform(width, height);
		double x = p.getX();
		double y = p.getY();
		double w = v.getX();
		double h = v.getY();
		if (w < 0) {
			x += w;
			w = -w;
		}
		if (h < 0) {
			y += h;
			h = -h;
		}
		return new Rectangle2D.Double(x, y, w, h);
	}

	private Point2D transform(float x, float y) {
		Point2D pt = new Point2D.Float(x, y);
		return ctm.transform(pt, pt);
	}

	private Point2D deltaTransform(float dx, float dy) {
		Point2D v = new Point2D.Float(dx, dy);
		return ctm.deltaTransform(v, v);
	}

}
