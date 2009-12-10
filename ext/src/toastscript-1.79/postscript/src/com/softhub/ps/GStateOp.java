
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

import java.awt.color.ColorSpace;
import java.awt.geom.*;
import java.beans.*;
import java.lang.reflect.*;
import com.softhub.ps.device.CacheDevice;
import com.softhub.ps.device.Device;
import com.softhub.ps.util.CharWidth;

final class GStateOp implements Stoppable, Types {

	private final static String DEVICE_GRAY = "DeviceGray";
	private final static String DEVICE_RGB = "DeviceRGB";
	private final static String DEVICE_CMYK = "DeviceCMYK";

	private final static String OPNAMES[] = {
		"initgraphics", "gsave", "grestore", "grestoreall", "currentpoint", "setlinewidth",
		"currentlinewidth", "setflat", "currentflat", "setlinejoin", "currentlinejoin",
		"setlinecap", "currentlinecap", "setmiterlimit", "currentmiterlimit", "setgray",
		"setcolor", "currentcolor", "currentgray", "setrgbcolor", "currentrgbcolor",
		"sethsbcolor", "setcolorspace", "currentcolorspace", "currenthsbcolor",
		"setcmykcolor", "currentcmykcolor", "setdash", "currentdash", "setscreen",
		"currentscreen", "settransfer", "currenttransfer", "setcolortransfer",
		"currentcolortransfer", "setcolorscreen", "currentcolorscreen", "setblackgeneration",
		"setundercolorremoval", "currentblackgeneration", "currentundercolorremoval",
		"setpagedevice", "currentpagedevice", "setcharwidth", "setcachedevice",
		"setcachedevice2", "nulldevice", "cachestatus", "setcachelimit", "setcacheparams",
		"currentcacheparams", "setstrokeadjust", "currentstrokeadjustment", "setoverprint",
		"currentoverprint", "sethalftone", "currenthalftone", "setcolorrendering",
		"currentcolorrendering", "sethalftonephase", "currenthalftonephase"
	};

	static void install(Interpreter ip) {
		ip.installOp(OPNAMES, GStateOp.class);
	}

	/**
	 * Init graphics state.
	 */
	static void initgraphics(Interpreter ip) {
		ip.getGraphicsState().initgraphics();
	}

	/**
	 * Save the current graphics context.
	 */
	static void gsave(Interpreter ip) {
		ip.gsave();
	}

	/**
	 * Restore the graphics context.
	 */
	static void grestore(Interpreter ip) {
		ip.grestore();
	}

	/**
	 * Restore the graphics context down to the initial state.
	 */
	static void grestoreall(Interpreter ip) {
		ip.grestoreAll();
	}

	/**
	 * Get the currentpoint.
	 */
	static void currentpoint(Interpreter ip) {
		Point2D pt = ip.getGraphicsState().currentpoint();
		ip.ostack.pushRef(new RealType(pt.getX()));
		ip.ostack.pushRef(new RealType(pt.getY()));
	}

	/**
	 * Set the current line width.
	 */
	static void setlinewidth(Interpreter ip) {
		ip.getGraphicsState().setlinewidth(((NumberType) ip.ostack.pop(NUMBER)).floatValue());
	}

	/**
	 * Get the current line width.
	 */
	static void currentlinewidth(Interpreter ip) {
		ip.ostack.pushRef(new RealType(ip.getGraphicsState().currentlinewidth()));
	}

	/**
	 * Set the current flatness.
	 */
	static void setflat(Interpreter ip) {
		ip.getGraphicsState().setflat(((NumberType) ip.ostack.pop(NUMBER)).floatValue());
	}

	/**
	 * Get the current flatness.
	 */
	static void currentflat(Interpreter ip) {
		ip.ostack.pushRef(new RealType(ip.getGraphicsState().currentflat()));
	}

	/**
	 * Set the current line join.
	 */
	static void setlinejoin(Interpreter ip) {
		ip.getGraphicsState().setlinejoin(ip.ostack.popInteger());
	}

	/**
	 * Get the current line join.
	 */
	static void currentlinejoin(Interpreter ip) {
		ip.ostack.pushRef(new IntegerType(ip.getGraphicsState().currentlinejoin()));
	}

	/**
	 * Set the current line cap.
	 */
	static void setlinecap(Interpreter ip) {
		ip.getGraphicsState().setlinecap(ip.ostack.popInteger());
	}

	/**
	 * Get the current line cap.
	 */
	static void currentlinecap(Interpreter ip) {
		ip.ostack.pushRef(new IntegerType(ip.getGraphicsState().currentlinecap()));
	}

	/**
	 * Set the current line miterlimit.
	 */
	static void setmiterlimit(Interpreter ip) {
		ip.getGraphicsState().setmiterlimit(((NumberType) ip.ostack.pop(NUMBER)).floatValue());
	}

	/**
	 * Get the current line miterlimit.
	 */
	static void currentmiterlimit(Interpreter ip) {
		ip.ostack.pushRef(new RealType(ip.getGraphicsState().currentmiterlimit()));
	}

	/**
	 * Set the current stroke adjustment.
	 */
	static void setstrokeadjust(Interpreter ip) {
		ip.getGraphicsState().setStrokeAdjustment(ip.ostack.popBoolean());
	}

	/**
	 * Get the current stroke adjustment.
	 */
	static void currentstrokeadjustment(Interpreter ip) {
		ip.ostack.pushRef(new BoolType(ip.getGraphicsState().getStrokeAdjustment()));
	}

	/**
	 * Set the current overprint.
	 */
	static void setoverprint(Interpreter ip) {
		ip.getGraphicsState().setOverprint(ip.ostack.popBoolean());
	}

	/**
	 * Get the current overprint.
	 */
	static void currentoverprint(Interpreter ip) {
		ip.ostack.pushRef(new BoolType(ip.getGraphicsState().getOverprint()));
	}

	/**
	 * Set the current gray value.
	 */
	static void setgray(Interpreter ip) {
		GraphicsState gc = ip.getGraphicsState();
		ArrayType transferproc = gc.currenttransfer();
		if (transferproc != null) {
			ip.estack.run(ip, transferproc);
		}
		gc.setgray(((NumberType) ip.ostack.pop(NUMBER)).floatValue());
	}

	/**
	 * Get the current gray value.
	 */
	static void currentgray(Interpreter ip) {
		ip.ostack.pushRef(new RealType(ip.getGraphicsState().currentgray()));
	}

	/**
	 * Set the current color.
	 */
	static void setcolor(Interpreter ip) {
		Any any = ip.ostack.top();
		if (any instanceof DictType) {
			setpattern(ip);
		} else {
			GraphicsState gc = ip.getGraphicsState();
			int colorSpaceCode = gc.getColorSpace();
			switch (colorSpaceCode) {
			case ColorSpace.TYPE_GRAY:
				setgray(ip);
				break;
			case ColorSpace.TYPE_RGB:
				setrgbcolor(ip);
				break;
			case ColorSpace.TYPE_CMYK:
				setcmykcolor(ip);
				break;
			default:
				throw new Stop(INTERNALERROR);
			}
		}
	}

	/**
	 * Get the current color.
	 */
	static void currentcolor(Interpreter ip) {
		GraphicsState gc = ip.getGraphicsState();
		int colorSpaceCode = gc.getColorSpace();
		switch (colorSpaceCode) {
		case ColorSpace.TYPE_GRAY:
			currentgray(ip);
			break;
		case ColorSpace.TYPE_RGB:
			currentrgbcolor(ip);
			break;
		case ColorSpace.TYPE_CMYK:
			currentcmykcolor(ip);
			break;
		default:
			throw new Stop(INTERNALERROR);
		}
	}

	/**
	 * Set the current rgb color.
	 */
	static void setrgbcolor(Interpreter ip) {
		GraphicsState gc = ip.getGraphicsState();
		ArrayType transferproc = gc.currentbluetransfer();
		if (transferproc != null) {
			ip.estack.run(ip, transferproc);
		}
		float blue = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
		transferproc = gc.currentgreentransfer();
		if (transferproc != null) {
			ip.estack.run(ip, transferproc);
		}
		float green = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
		transferproc = gc.currentredtransfer();
		if (transferproc != null) {
			ip.estack.run(ip, transferproc);
		}
		float red = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
		gc.setrgbcolor(red, green, blue);
	}

	/**
	 * Get the current rgb color.
	 */
	static void currentrgbcolor(Interpreter ip) {
		float color[] = ip.getGraphicsState().currentrgbcolor();
		ip.ostack.pushRef(new RealType(color[0]));
		ip.ostack.pushRef(new RealType(color[1]));
		ip.ostack.pushRef(new RealType(color[2]));
	}

	/**
	 * Set the current hsb color.
	 */
	static void sethsbcolor(Interpreter ip) {
		float b = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
		float s = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
		float h = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
		ip.getGraphicsState().sethsbcolor(h, s, b);
	}

	/**
	 * Get the current hsb color.
	 */
	static void currenthsbcolor(Interpreter ip) {
		float color[] = ip.getGraphicsState().currenthsbcolor();
		ip.ostack.pushRef(new RealType(color[0]));
		ip.ostack.pushRef(new RealType(color[1]));
		ip.ostack.pushRef(new RealType(color[2]));
	}

	/**
	 * Set the current cmyk color.
	 */
	static void setcmykcolor(Interpreter ip) {
		float black = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
		float yellow = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
		float magenta = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
		float cyan = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
		ip.getGraphicsState().setcmykcolor(cyan, magenta, yellow, black);
	}

	/**
	 * Get the current cmyk color.
	 */
	static void currentcmykcolor(Interpreter ip) {
		float color[] = ip.getGraphicsState().currentcmykcolor();
		ip.ostack.pushRef(new RealType(color[0]));
		ip.ostack.pushRef(new RealType(color[1]));
		ip.ostack.pushRef(new RealType(color[2]));
		ip.ostack.pushRef(new RealType(color[3]));
	}

	/**
	 * Set the current color rendering dictionary.
	 */
	static void setcolorrendering(Interpreter ip) {
		DictType dict = (DictType) ip.ostack.pop(DICT);
		ip.getGraphicsState().setColorRendering(dict);
	}

	/**
	 * Get the current color rendering dictionary.
	 */
	static void currentcolorrendering(Interpreter ip) {
		ip.ostack.push(ip.getGraphicsState().getColorRendering());
	}

	/**
	 * Set the current colorspace.
	 */
	static void setcolorspace(Interpreter ip) {
		Any colorSpace = ip.ostack.pop(STRING | NAME | ARRAY);
		String colorSpaceName;
		if (colorSpace instanceof ArrayType) {
			ArrayType array = (ArrayType) colorSpace;
			colorSpaceName = array.get(0).toString();
		} else {
			colorSpaceName = colorSpace.toString();
		}
		GraphicsState gc = ip.getGraphicsState();
		if (colorSpaceName.equals(DEVICE_GRAY)) {
			gc.setColorSpace(ColorSpace.CS_GRAY);
		} else if (colorSpaceName.equals(DEVICE_RGB)) {
			gc.setColorSpace(ColorSpace.CS_sRGB);
		} else if (colorSpaceName.equals(DEVICE_CMYK)) {
			gc.setColorSpace(ColorSpace.TYPE_CMYK);
		} else if (colorSpaceName.equals("CIEBasedABC")) {
			// TODO: setcolorspace
			gc.setColorSpace(ColorSpace.CS_sRGB);
		} else if (colorSpaceName.equals("CIEBasedA")) {
			// TODO: setcolorspace
		} else if (colorSpaceName.equals("Separation")) {
			// TODO: setcolorspace
		} else if (colorSpaceName.equals("Indexed")) {
			// TODO: setcolorspace
		} else if (colorSpaceName.equals("Pattern")) {
			// TODO: setcolorspace
		} else {
			throw new Stop(INTERNALERROR, colorSpaceName + " not implemented");
		}
	}

	/**
	 * Get the current colorspace.
	 */
	static void currentcolorspace(Interpreter ip) {
		GraphicsState gc = ip.getGraphicsState();
		int colorSpaceCode = gc.getColorSpace();
		String colorSpaceName = null;
		switch (colorSpaceCode) {
		case ColorSpace.TYPE_GRAY:
			colorSpaceName = DEVICE_GRAY;
			break;
		case ColorSpace.TYPE_RGB:
			colorSpaceName = DEVICE_RGB;
			break;
		case ColorSpace.TYPE_CMYK:
			colorSpaceName = DEVICE_CMYK;
			break;
		default:
			throw new Stop(INTERNALERROR, colorSpaceName + " not yet implemented");
		}
		ArrayType array = new ArrayType(ip.vm, 1);
		array.put(ip.vm, 0, new NameType(colorSpaceName));
		ip.ostack.pushRef(array);
	}

	/**
	 * Set the current pattern.
	 */
	static void setpattern(Interpreter ip) {
		DictType pattern = (DictType) ip.ostack.pop(DICT);
		// TODO: check pattern
		ip.getGraphicsState().setPattern(pattern);
	}

	/**
	 * Get the current pattern.
	 */
	static void currentpattern(Interpreter ip) {
		ip.ostack.push(ip.getGraphicsState().getPattern());
	}

	/**
	 * Set dash parameters.
	 */
	static void setdash(Interpreter ip) {
		float phase = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
		ArrayType array = (ArrayType) ip.ostack.pop(ARRAY);
		ip.getGraphicsState().setdash(array, phase);
	}

	/**
	 * Get dash parameters.
	 */
	static void currentdash(Interpreter ip) {
		GraphicsState gc = ip.getGraphicsState();
		ArrayType array = gc.currentdasharray();
		if (array == null) {
			ip.ostack.pushRef(new ArrayType(ip.vm, 0));
		} else {
			ip.ostack.push(array);
		}
		ip.ostack.pushRef(new RealType(gc.currentdashphase()));
	}

	/**
	 * Set the current halftone screen function.
	 */
	static void setscreen(Interpreter ip) {
		GraphicsState gstate = ip.getGraphicsState();
		Any any = ip.ostack.pop(ARRAY | DICT);
		if (any instanceof ArrayType) {
			ArrayType proc = (ArrayType) any;
			NumberType angle = (NumberType) ip.ostack.pop(NUMBER);
			NumberType freq = (NumberType) ip.ostack.pop(NUMBER);
			DictType dict = createHalftone(ip, freq, angle, proc);
			setHalftone(gstate, dict);
		} else {
			setHalftone(gstate, (DictType) any);
		}
	}

	/**
	 * Get the current halftone screen parameters.
	 */
	static void currentscreen(Interpreter ip) {
		DictType halftone = ip.getGraphicsState().getHalftone();
		if (halftone == null)
			throw new Stop(INTERNALERROR, "no initial halftone defined");
		ip.ostack.pushRef(halftone.get("Frequency"));
		ip.ostack.pushRef(halftone.get("Angle"));
		ip.ostack.push(halftone.get("SpotFunction"));
	}

	/**
	 * Set the current halftone dictionary.
	 */
	static void sethalftone(Interpreter ip) {
		DictType dict = (DictType) ip.ostack.pop(DICT);
		GraphicsState gstate = ip.getGraphicsState();
		setHalftone(gstate, dict);
	}

	/**
	 * Set the current halftone dictionary.
	 */
	static void setHalftone(GraphicsState gstate, DictType dict) {
		if (!dict.known("HalftoneType"))
			throw new Stop(TYPECHECK, "HalftoneType");
		if (!dict.known("Frequency"))
			throw new Stop(TYPECHECK, "Frequency");
		if (!dict.known("Angle"))
			throw new Stop(TYPECHECK, "Angle");
		if (!dict.known("SpotFunction"))
			throw new Stop(TYPECHECK, "SpotFunction");
		gstate.setHalftone(dict);
	}

	/**
	 * Set the current halftone dictionary.
	 */
	static DictType createHalftone(Interpreter ip,
		NumberType freq, NumberType angle, ArrayType proc)
	{
		DictType halftone = new DictType(ip.vm, 4);
		halftone.put(ip.vm, "HalftoneType", new IntegerType(1));
		halftone.put(ip.vm, "Frequency", freq);
		halftone.put(ip.vm, "Angle", angle);
		halftone.put(ip.vm, "SpotFunction", proc);
		return halftone;
	}

	/**
	 * Get the current halftone parameters.
	 */
	static void currenthalftone(Interpreter ip) {
		GraphicsState gstate = ip.getGraphicsState();
		DictType halftone = gstate.getHalftone();
		if (halftone == null)
			throw new Stop(INTERNALERROR, "no initial halftone defined");
		ip.ostack.push(halftone);
	}

	/**
	 * Set the current halftone phase.
	 */
	static void sethalftonephase(Interpreter ip) {
		int y = ip.ostack.popInteger();
		int x = ip.ostack.popInteger();
		GraphicsState gstate = ip.getGraphicsState();
		gstate.setHalftonePhase(x, y);
	}

	/**
	 * Get the current halftone phase.
	 */
	static void currenthalftonephase(Interpreter ip) {
		GraphicsState gstate = ip.getGraphicsState();
		int phase[] = gstate.getHalftonePhase();
		ip.ostack.pushRef(new IntegerType(phase[0]));
		ip.ostack.pushRef(new IntegerType(phase[1]));
	}

	/**
	 * Set the current transfer function.
	 */
	static void settransfer(Interpreter ip) {
		ip.getGraphicsState().settransfer((ArrayType) ip.ostack.pop(ARRAY));
	}

	/**
	 * Get the current transfer function.
	 */
	static void currenttransfer(Interpreter ip) {
		pushTransferProc(ip, ip.getGraphicsState().currenttransfer());
	}

	/**
	 * If proc is null, make one up and push onto stack.
	 */
	private static void pushTransferProc(Interpreter ip, ArrayType proc) {
		if (proc == null) {
			proc = new ArrayType(ip.vm, 0);
			proc.cvx();
		}
		ip.ostack.push(proc);
	}

	/**
	 * Set the current color transfer function.
	 */
	static void setcolortransfer(Interpreter ip) {
		ArrayType gray = (ArrayType) ip.ostack.pop(ARRAY);
		ArrayType blue = (ArrayType) ip.ostack.pop(ARRAY);
		ArrayType green = (ArrayType) ip.ostack.pop(ARRAY);
		ArrayType red = (ArrayType) ip.ostack.pop(ARRAY);
		ip.getGraphicsState().setcolortransfer(red, green, blue, gray);
	}

	/**
	 * Get the current color transfer function.
	 */
	static void currentcolortransfer(Interpreter ip) {
		GraphicsState gc = ip.getGraphicsState();
		pushTransferProc(ip, gc.currentredtransfer());
		pushTransferProc(ip, gc.currentgreentransfer());
		pushTransferProc(ip, gc.currentbluetransfer());
		pushTransferProc(ip, gc.currenttransfer());
	}

	/**
	 * Set the current color transfer functions.
	 */
	static void setcolorscreen(Interpreter ip) {
		ArrayType grayproc = (ArrayType) ip.ostack.pop(ARRAY);
		NumberType grayang = (NumberType) ip.ostack.pop(NUMBER);
		NumberType grayfreq = (NumberType) ip.ostack.pop(NUMBER);
		ArrayType blueproc = (ArrayType) ip.ostack.pop(ARRAY);
		double blueang = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		double bluefreq = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		ArrayType greenproc = (ArrayType) ip.ostack.pop(ARRAY);
		double greenang = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		double greenfreq = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		ArrayType redproc = (ArrayType) ip.ostack.pop(ARRAY);
		double redang = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		double redfreq = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		Screen bluescreen = new Screen(bluefreq, blueang, blueproc);
		Screen greenscreen = new Screen(greenfreq, greenang, greenproc);
		Screen redscreen = new Screen(redfreq, redang, redproc);
		DictType dict = createHalftone(ip, grayfreq, grayang, grayproc);
		GraphicsState gstate = ip.getGraphicsState();
		gstate.setHalftone(dict);
		gstate.setColorScreen(redscreen, greenscreen, bluescreen);
	}

	/**
	 * Get the current color transfer functions.
	 */
	static void currentcolorscreen(Interpreter ip) {
		GraphicsState gc = ip.getGraphicsState();
		Screen redscreen = gc.currentredscreen();
		ip.ostack.pushRef(new RealType(redscreen.getFrequency()));
		ip.ostack.pushRef(new RealType(redscreen.getAngle()));
		ip.ostack.push(redscreen.getProcedure(ip.vm));
		Screen greenscreen = gc.currentgreenscreen();
		ip.ostack.pushRef(new RealType(greenscreen.getFrequency()));
		ip.ostack.pushRef(new RealType(greenscreen.getAngle()));
		ip.ostack.push(greenscreen.getProcedure(ip.vm));
		Screen bluescreen = gc.currentbluescreen();
		ip.ostack.pushRef(new RealType(bluescreen.getFrequency()));
		ip.ostack.pushRef(new RealType(bluescreen.getAngle()));
		ip.ostack.push(bluescreen.getProcedure(ip.vm));
		DictType halftone = ip.getGraphicsState().getHalftone();
		ip.ostack.pushRef(halftone.get("Frequency"));
		ip.ostack.pushRef(halftone.get("Angle"));
		ip.ostack.push(halftone.get("SpotFunction"));
	}

	/**
	 * Set the current black generation function.
	 */
	static void setblackgeneration(Interpreter ip) {
		ip.getGraphicsState().setblackgeneration((ArrayType) ip.ostack.pop(ARRAY));
	}

	/**
	 * Get the current black generation function.
	 */
	static void currentblackgeneration(Interpreter ip) {
		pushTransferProc(ip, ip.getGraphicsState().currentblackgeneration());
	}

	/**
	 * Set the current under color removal function.
	 */
	static void setundercolorremoval(Interpreter ip) {
		ip.getGraphicsState().setundercolorremoval((ArrayType) ip.ostack.pop(ARRAY));
	}

	/**
	 * Get the current under color removal function.
	 */
	static void currentundercolorremoval(Interpreter ip) {
		pushTransferProc(ip, ip.getGraphicsState().currentundercolorremoval());
	}

	/**
	 * Set the page device.
	 */
	static void setpagedevice(Interpreter ip) {
		DictType dict = (DictType) ip.ostack.pop(DICT);
		GraphicsState gstate = ip.getGraphicsState();
		Device device = loadDevice(gstate, dict);
		PropertyDescriptor desc[] = getDeviceProperties(device);
		if (desc != null) {
			for (int i = 0; i < desc.length; i++) {
				PropertyDescriptor pd = desc[i];
				String name = pd.getName();
				Any any = dict.get(name);
				if (any != null) {
					try {
						Object val = device.convertType(name, any.cvj());
						Object args[] = new Object[1];
						args[0] = val;
						Method setter = pd.getWriteMethod();
						setter.invoke(device, args);
					} catch (Exception ex) {
						System.out.println("setpagedevice: " + ex);
					}
				}
			}
		}
		gstate.setpagedevice(device);
	}

	/**
	 * Get the page device properties.
	 */
	static void currentpagedevice(Interpreter ip) {
		DictType dict = new DictType(ip.vm, 10);
		GraphicsState gstate = ip.getGraphicsState();
		Device device = loadDevice(gstate, dict);
		PropertyDescriptor desc[] = getDeviceProperties(device);
		if (desc != null) {
			for (int i = 0; i < desc.length; i++) {
				PropertyDescriptor pd = desc[i];
				String name = pd.getName();
				Method getter = pd.getReadMethod();
				if (getter != null) {
					try {
						Object obj = getter.invoke(device, null);
						Any value;
						if (obj instanceof Integer) {
							int val = ((Integer) obj).intValue();
							value = new IntegerType(val);
						} else if (obj instanceof Float) {
							float val = ((Float) obj).floatValue();
							value = new RealType(val);
						} else if (obj instanceof float[]) {
							float val[] = (float[]) obj;
							int n = val.length;
							ArrayType a = new ArrayType(ip.vm, n);
							for (int j = 0; j < n; j++) {
								a.put(ip.vm, j, new RealType(val[j]));
							}
							value = a;
						} else {
							value = new StringType(ip.vm, obj.toString());
						}
						dict.put(ip.vm, name, value);
					} catch (Exception ex) {
						System.out.println(ex);
					}
				}
			}
		}
		ip.ostack.pushRef(dict);
	}

	private static Device loadDevice(GraphicsState gstate, DictType dict) {
		try {
			Device device;
			Any deviceClassName = dict.get("class");
			if (deviceClassName != null) {
				Class clazz = Class.forName(deviceClassName.toString());
				device = (Device) clazz.newInstance();
			} else {
				device = gstate.defaultdevice();
			}
			return (Device) device;
		} catch (Exception ex) {
			throw new Stop(UNDEFINED, ex.toString());
		}
	}

	private static PropertyDescriptor[] getDeviceProperties(Device device) {
		try {
			BeanInfo info = Introspector.getBeanInfo(device.getClass());
			return info.getPropertyDescriptors();
		} catch (Exception ex) {
			throw new Stop(INTERNALERROR, ex.toString());
		}
	}

	/**
	 * Set the null device.
	 */
	static void nulldevice(Interpreter ip) {
		ip.getGraphicsState().nulldevice();
	}

	/**
	 * Set width parameters.
	 */
	static void setcharwidth(Interpreter ip) {
		float wy  = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
		float wx  = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
		ip.getGraphicsState().setcharwidth(wx, wy);
	}

	/**
	 * Set cache parameters.
	 */
	static void setcachedevice(Interpreter ip) {
		double ury = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		double urx = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		double lly = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		double llx = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		float wy  = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
		float wx  = ((NumberType) ip.ostack.pop(NUMBER)).floatValue();
		CharWidth cw = new CharWidth(0, 0, wx, wy);
		Rectangle2D rect = new Rectangle2D.Double(llx, lly, urx-llx, ury-lly);
		ip.getGraphicsState().setcachedevice(cw, rect);
	}

	/**
	 * Set cache parameters.
	 */
	static void setcachedevice2(Interpreter ip) {
		double vy  = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		double vx  = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		double ury = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		double urx = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		double lly = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		double llx = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		double wy  = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		double wx  = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		double w0y = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		double w0x = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		ip.getGraphicsState().setcachedevice2(w0x, w0y, wx, wy, llx, lly, urx, ury, vx, vy);
	}

	static void cachestatus(Interpreter ip) {
		CacheDevice cdev = ip.getGraphicsState().cachedevice();
		int bsize = 1024;
		int bmax = 1024;
		int msize = 100;
		int mmax = 100;
		int csize = cdev.getCacheSize();
		int cmax = cdev.getMaxCacheSize();
		int blimit = 1024;
		ip.ostack.pushRef(new IntegerType(bsize));
		ip.ostack.pushRef(new IntegerType(bmax));
		ip.ostack.pushRef(new IntegerType(msize));
		ip.ostack.pushRef(new IntegerType(mmax));
		ip.ostack.pushRef(new IntegerType(csize));
		ip.ostack.pushRef(new IntegerType(cmax));
		ip.ostack.pushRef(new IntegerType(blimit));
	}

	static void setcachelimit(Interpreter ip) {
		ip.ostack.pop(INTEGER);
	}

	static void setcacheparams(Interpreter ip) {
		IntegerType upper = (IntegerType) ip.ostack.pop(INTEGER);
		IntegerType lower = (IntegerType) ip.ostack.pop(INTEGER);
		IntegerType size = (IntegerType) ip.ostack.pop(INTEGER);
		ip.ostack.cleartomark();
		CacheDevice cdev = ip.getGraphicsState().cachedevice();
		cdev.setMaxCacheSize(size.intValue());
	}

	static void currentcacheparams(Interpreter ip) {
		CacheDevice cdev = ip.getGraphicsState().cachedevice();
		int size = cdev.getMaxCacheSize();
		ip.ostack.pushRef(new MarkType());
		ip.ostack.pushRef(new IntegerType(size));
		ip.ostack.pushRef(new IntegerType(100));
		ip.ostack.pushRef(new IntegerType(200));
	}

}
