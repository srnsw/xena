/**
* ===========================================
* Java Pdf Extraction Decoding Access Library
* ===========================================
*
* Project Info:  http://www.jpedal.org
* (C) Copyright 1997-2008, IDRsolutions and Contributors.
*
* 	This file is part of JPedal
*
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA


*
* ---------------
* ColorSpaces.java
* ---------------
*/
package org.jpedal.color;

import java.awt.RenderingHints;

public class ColorSpaces {
	
	public static final int ICC = 1247168582;
	public static final int CalGray = 391471749;
	public static final int DeviceGray = 1568372915;
	public static final int DeviceN = 960981604;
	public static final int Separation = -2073385820;
	public static final int Pattern = 1146450818;
	public static final int Lab = 1847602;
	public static final int Indexed = 895578984;
	public static final int DeviceRGB = 1785221209;
	public static final int CalRGB = 1008872003;
	public static final int DeviceCMYK = 1498837125;

	/**hint for conversion ops*/
	public static RenderingHints hints = null;
	
	
	static {
		hints =
			new RenderingHints(
				RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		hints.put(
			RenderingHints.KEY_RENDERING,
			RenderingHints.VALUE_RENDER_QUALITY);
		hints.put(
			RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
		hints.put(
			RenderingHints.KEY_DITHERING,
			RenderingHints.VALUE_DITHER_ENABLE);
		hints.put(
			RenderingHints.KEY_COLOR_RENDERING,
			RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		hints.put(
			RenderingHints.KEY_FRACTIONALMETRICS,
			RenderingHints.VALUE_FRACTIONALMETRICS_ON);

	}
	
	/**method to convert a name to an ID values*/
	final public  static int convertNameToID(String name){
		
		int id=-1;

        if ((name.indexOf("Indexed") != -1))
			id=Indexed;
        else if ((name.indexOf("Separation") != -1))
			id=Separation;
		else if (name.indexOf("DeviceN") != -1)
			id=DeviceN;	
		else if ((name.indexOf("DeviceCMYK") != -1)| (name.indexOf("CMYK") != -1))
			id=DeviceCMYK;
		else if (name.indexOf("CalGray") != -1)
			id=CalGray;
		else if (name.indexOf("CalRGB") != -1)
			id=CalRGB;
		else if (name.indexOf("Lab") != -1) 
			id=Lab;
		else if (name.indexOf("ICCBased") != -1) 
			id=ICC;
		else if (name.indexOf("Pattern") != -1)
			id=Pattern;
		else if ((name.indexOf("DeviceRGB") != -1)|(name.indexOf("RGB") != -1))
			id=DeviceRGB;
		else if ((name.indexOf("DeviceGray") != -1)|(name.indexOf('G') != -1))
			id=DeviceGray;			
		
		return id;
	}

}
