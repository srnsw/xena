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
* FunctionContext.java
* ---------------
*/
package com.idrsolutions.pdf.color.shading;

import java.awt.Color;
import java.awt.PaintContext;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;


import org.jpedal.PdfDecoder;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.function.PDFFunction;

public class CoonsContext implements PaintContext {

	GenericColorSpace shadingColorSpace;

	private float scaling=1f;

	private PDFFunction function;

	private CoonsContext(){}

	private int pageHeight;

	private boolean colorsReversed;

	private int xstart,ystart;

	private float[] domain;

	public CoonsContext(PdfObject shadingObj, int pHeight,float scaling,float[] domain,GenericColorSpace shadingColorSpace,boolean colorsReversed, PDFFunction function){

		this.colorsReversed=colorsReversed;
		this.pageHeight=pHeight;
		this.domain=domain;

		this.shadingColorSpace=shadingColorSpace;
		this.function = function;
		this.scaling=scaling;

        //specifc coons values
        int BitsPerComponent=shadingObj.getInt(PdfDictionary.BitsPerComponent);
        int BitsPerFlag=shadingObj.getInt(PdfDictionary.BitsPerFlag);
        int BitsPerCoordinate=shadingObj.getInt(PdfDictionary.BitsPerCoordinate);

        System.out.println(BitsPerComponent+" "+BitsPerCoordinate+" "+BitsPerFlag);
       

	}
	public void dispose() {}

	public ColorModel getColorModel() { return ColorModel.getRGBdefault(); }

	/**
	 * setup the raster with the colors
	 * */
	public Raster getRaster(int xstart, int ystart, int w, int h) {

		this.xstart=xstart;
		this.ystart=ystart;

		//sets up the array of pixel values
		WritableRaster raster =getColorModel().createCompatibleWritableRaster(w, h);

		//create buffer to hold all this data
		int[] data = new int[w * h * 4];

		//workout color range
		Color col0;

		if(colorsReversed)
			col0= calculateColor(0,0);
		else
			col0= calculateColor(w,h);

		//set current calues to default
		int cr=col0.getRed(),cg=col0.getGreen(),cb=col0.getBlue();

		//y co-ordinates
		for (int y = 0; y < h; y++) {

			//x co-ordinates
			for (int x = 0; x < w; x++) {

				Color c=calculateColor(x,y);

				/**
				 * workout values
				 */
				if(colorsReversed){
					cr=255-c.getRed();
					cg=255-c.getGreen();
					cb=255-c.getBlue();
				}else{
					cr=c.getRed();
					cg=c.getGreen();
					cb=c.getBlue();
				}
				

				//set color for the pixel with values
				int base = (y * w + x) * 4;
				data[base] = cr;
				data[base + 1] = cg;
				data[base + 2] = cb;
				data[base + 3] = 255;//(int)(col.getAlpha());

			}
		}

		//set values
		raster.setPixels(0, 0, w, h, data);

		return raster;
	}

	/**workout rgb color*/
	private Color calculateColor(float x, float y) {

        if(1==1)
            return new Color(255,0,0);
        
		float cx,cy;

		/**
		 *take x and y and pass through conversion with domain values - this gives us xx
		 */
		//hack for MAC which is f**king broken
		if(PdfDecoder.isRunningOnMac){
			cx=scaling*(x+xstart);
			cy=scaling*(y+ystart);
		}else{
			cx=scaling*(x+xstart);
			cy=scaling*(pageHeight-(y+ystart));
		}

		float[] values={cx,cy};

		float[] colValues=function.compute(values);

		/**
		 * this value is converted to a color
		 */
		int count=colValues.length;
		shadingColorSpace.setColor(colValues,count);

        return (Color) shadingColorSpace.getColor();
	}
}