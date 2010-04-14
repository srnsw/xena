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
* DeviceGrayColorSpace.java
* ---------------
*/
package org.jpedal.color;

import java.awt.color.ColorSpace;


/**
 * handle GrayColorSpace
 */
public class DeviceGrayColorSpace extends GenericColorSpace {
		
	public DeviceGrayColorSpace(){	
		value = ColorSpaces.DeviceGray;
		cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
	}
	
	/**
	 * set  color (in terms of rgb)
	 */
	final public void setColor(String[] number_values,int opCount) {

		float[] colValues=new float[1];
		colValues[0] = Float.parseFloat(number_values[0]);
		
		setColor(colValues,1);
	}

	/**set color from grayscale values*/
	final public void setColor(float[] operand,int length) {

		int val;
		float tmp = operand[0];

		//handle float or int
		if(tmp<=1)
			val =(int) (255* tmp);
		else
			val =(int) (tmp);
		
		this.currentColor= new PdfColor(val, val, val);
		
	}	
	
	/**
	 * convert Index to RGB
	 */
	public byte[] convertIndexToRGB(byte[] index){

        isConverted=true;
        
		int count=index.length;
		byte[] newIndex=new byte[count*3];
		
		for(int i=0;i<count;i++){
			byte value=index[i];
			for(int j=0;j<3;j++)
				newIndex[(i*3)+j]=value;
			
		}
		
		return newIndex;
	}
}
