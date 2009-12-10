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
* PixelFactory.java
* ---------------
*/
package com.idrsolutions.pdf.color.shading;

public class PixelFactory {

	//return both pdfX at [0] & pdfY at [1]
	public static float[] convertPhysicalToPDF(boolean isPrinting, float pdfX, float pdfY, float x, float y, float offX, float offY, float scaling, int xstart, int ystart, int minX, int pageHeight) {
		
		if(isPrinting){
        	
            pdfX=((x+xstart)-offX)*scaling;
            pdfY=((y+ystart)-offY)*scaling;
            
        }else{
        	pdfX=(scaling*(x+xstart+minX-offX));
            pdfY=(scaling*((pageHeight-(y+ystart-offY))));
            
        }
		return new float[] {pdfX, pdfY};
	}
	
	//return both pdfX at [0] & pdfY at [1]
	public static float[] convertPhysicalToPDF(float x, float y,float scaling, int pageHeight, int cropX, int cropY, int cropW, int cropH) {
		
		//System.out.println("Sacling: " + scaling + " x: " + x + " y: " + y + " pageHeight: " + pageHeight + " cropX: " +cropX+" cropY: " +cropY + " cropW: " +cropW + " cropH: " +cropH);

		float pdfX=(scaling*(x-cropX));
		float pdfY=(scaling*(((cropY+cropH)-y)));		
            
        
		return new float[] {pdfX, pdfY};
	}

}
