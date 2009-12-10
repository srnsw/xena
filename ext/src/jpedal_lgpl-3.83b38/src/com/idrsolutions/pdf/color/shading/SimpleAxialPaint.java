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
* SimpleAxialPaint.java
* ---------------
*/
package com.idrsolutions.pdf.color.shading;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.util.Map;

import org.jpedal.color.GenericColorSpace;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.raw.PdfObject;

/**
 *
 */
public class SimpleAxialPaint extends ShadedPaint {

	/**
	 */
	public SimpleAxialPaint(PdfObject Shading, GenericColorSpace shadingColorSpace,PdfObjectReader currentPdfFile,float[][] matrix) {
		
		init(Shading, 0,shadingColorSpace,currentPdfFile,matrix);
		
	}
	
	public Paint generatePaint() {
		
		//set values
		float t0 = domain[0];
		float t1 = domain[1];
		
		/**workout colors*/
		/**
		 * pass default value into function
		 * */
		Color col1 = calculateColor(t0,t0, t1);
		Color col2 = calculateColor(t1,t0, t1);

		return new GradientPaint(coords[0],coords[1],col1,coords[2],coords[3],col2,false);
	}
	
/**
	public PaintContext createContext(ColorModel cm,

		      Rectangle deviceBounds, Rectangle2D userBounds,

		      AffineTransform xform, RenderingHints hints) {

		    return new AxialContext(isExtended,domain,coords,f,shadingColorSpace);

	}
*/

	private Color calculateColor(float val,float t0, float t1) {

        float[] values={val};
        float[] colValues=function.compute(values);

		/**
		 * this value is converted to a color
		 */
		shadingColorSpace.setColor(colValues,colValues.length);

		return (Color) shadingColorSpace.getColor();

	}
}
