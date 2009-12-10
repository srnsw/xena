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
* RadialContext.java
* ---------------
*/
package com.idrsolutions.pdf.color.shading;

import java.awt.Color;
import java.awt.PaintContext;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;


import org.jpedal.PdfDecoder;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.function.PDFFunction;

public class RadialContext implements PaintContext {
	
	GenericColorSpace shadingColorSpace;
	
	private float scaling=1f;
	
	private boolean[] isExtended;
	
	private float x0=0,x1=0,y0=0,y1=0,r0,r1,t0=0.0f,t1=1.0f;
	
	private PDFFunction function;
	
	private float[] cx,cy,cr,crSquared;
	private Color[] circleColor;
	
	private int xstart,ystart,circleCount;

	boolean isCone;
	
	private boolean circlesInitialised=false;
	
	
	private int pageHeight;

	private int minX;

	private float[] background;

	private boolean colorsReversed;

    private boolean isPrinting;

    private int offX,offY;

    public RadialContext(boolean isPrinting, int offX,int offY,int minX,int pHeight,float scaling,boolean[] isExtended,
                         float[] domain,float[] coords,GenericColorSpace shadingColorSpace,
                         boolean colorsReversed, float[] background, PDFFunction function){

        //@printIssue not currently used
        this.offX=offX;
        this.offY=offY;

        this.isPrinting=isPrinting;

        this.pageHeight=pHeight;
		this.isExtended=isExtended;
		this.t0=domain[0];
		this.t1=domain[1];
		this.minX = minX;
		this.background = background;
		this.colorsReversed=colorsReversed;
		
		x0=coords[0];
		x1=coords[3];
		r0=coords[2];
		y0=coords[1];
		y1=coords[4];
		r1=coords[5];
		this.shadingColorSpace=shadingColorSpace;
		this.function = function;
		this.scaling=scaling;

    }
    
    public void dispose() {

    }

	
	public ColorModel getColorModel() { return ColorModel.getRGBdefault(); }
	
	/**
	 * setup the raster with the colors
	 * */
	public Raster getRaster(int xstart, int ystart, int w, int h) {


		this.xstart=xstart;
		this.ystart=ystart;
		
		//setup circles ONCE
		if(!circlesInitialised){
			circlesInitialised=true;
			initialiseCircles();
		}
		
		//sets up the array of pixel values
		WritableRaster raster =getColorModel().createCompatibleWritableRaster(w, h);

        //create buffer to hold all this data
		int[] data = new int[w * h * 4];

        if(background!=null){
			//y co-ordinates
			for (int y = 0; y < h; y++) {
				
				//x co-ordinates			
				for (int x = 0; x < w; x++) {

					shadingColorSpace.setColor(background,shadingColorSpace.getColorComponentCount());
					Color c=(Color) shadingColorSpace.getColor();
			
					//set color for the pixel with values
					int base = (y * w + x) * 4;
					data[base] = c.getRed();
					data[base + 1] = c.getGreen();
					data[base + 2] = c.getBlue();
					data[base + 3] = 255;
					
				}
			}
		}
		
		//y co-ordinates
		for (int y = 0; y < h; y++) {
			
			//x co-ordinates			
			for (int x = 0; x < w; x++) {
				
				int i=calculateColor(x,y);

				if(i>=0){
					setColor(w, data, y, x, i);
				}
			}
		}
		
		//set values
		raster.setPixels(0, 0, w, h, data);
		
		return raster;
	}
	private void setColor(int w, int[] data, int y, int x, int i) {
		
		int cr;
		int cg;
		int cb;

		Color c=this.circleColor[i];
		cr=c.getRed();
		cg=c.getGreen();
		cb=c.getBlue();
		
		//set color for the pixel with values
		int base = (y * w + x) * 4;
		data[base] = cr;
		data[base + 1] = cg;
		data[base + 2] = cb;
		data[base + 3] = 255;//(int)(col.getAlpha());
		
	}
	float pdfX=0;
    float pdfY=0;
	/**workout rgb color*/
	private int calculateColor(float x, float y) {
		
		/**
		 *take x and y and pass through conversion with domain values - this gives us xx
		 */
		//hack for MAC which is f**king broken
		if(PdfDecoder.isRunningOnMac){
			x=x+xstart;
			y=y+ystart;
		}else{
            //System.out.println(scaling+" "+x+" "+y);

            int oldX=(int)x;
            int oldY=(int)y;
            
            if(isPrinting){

            	pdfX=scaling*(((x+xstart))+minX);
            	pdfY=scaling*(pageHeight-((y+ystart)));

            }else{
            	pdfX=scaling*(x+xstart+minX);
            	pdfY=scaling*(pageHeight-(y+ystart));
            }
            
            float[] xy = PixelFactory.convertPhysicalToPDF(isPrinting, pdfX, pdfY, x, y, offX, offY, scaling, xstart, ystart, minX, pageHeight);
            pdfX = xy[0];
            pdfY = xy[1];
            
//            if(oldX==0 && oldY==0 && ShadedPaint.debugP){
//                    System.out.println(">>"+x+" "+y+" scaling="+scaling+"  xstart="+xstart+" ystart="+ystart+" minx="+minX+" pageHeight="+pageHeight);
//                }
        }
		
		//number of circles
		int j=-1; //not in
		float rSquared=0;

		/** draw the circles */
		for (int i = circleCount; i > 0; i--) { // draw all the circles
			rSquared = ((pdfX - cx[i]) * (pdfX - cx[i])) + ((pdfY - cy[i]) * (pdfY - cy[i]));
			if (((rSquared <= crSquared[i]) && (rSquared >= crSquared[i - 1]))
					|| ((rSquared >= crSquared[i]) && (rSquared <= crSquared[i - 1]))) {
				j = i;
				
				break;
			}
		}
		
		/** fill the gaps between the circles */
		if (cr[0] < cr[1]) { // shade from small circle to big
			if (j == -1) {
				for (int i = 0; i < circleCount; i++) {
					rSquared = ((pdfX - cx[i]) * (pdfX - cx[i])) + ((pdfY - cy[i]) * (pdfY - cy[i]));
					float nextRSquared = ((pdfX - cx[i+1]) * (pdfX - cx[i+1])) + ((pdfY - cy[i+1]) * (pdfY - cy[i+1]));
					
					if ((rSquared > crSquared[i]) && (nextRSquared < crSquared[i+1])){
						j = i;
						break;
					}
				}
			}
		} 
		else {
			for (int i = circleCount; i > 0; i--) {
				rSquared = ((pdfX - cx[i]) * (pdfX - cx[i])) + ((pdfY - cy[i]) * (pdfY - cy[i]));
				float nextRSquared = ((pdfX - cx[i-1]) * (pdfX - cx[i-1])) + ((pdfY - cy[i-1]) * (pdfY - cy[i-1]));
				
				if ((rSquared > crSquared[i]) && (nextRSquared < crSquared[i-1])){
					j = i;
					break;
				}
			}
		}
		
		return j;
	}
	
	/**work out sets of circles and colors for each*/
	private void initialiseCircles() {

		circleCount=100;

		circleCount++;
		//create lookups
		cx=new float[circleCount];
		cy=new float[circleCount];
		cr=new float[circleCount];
		crSquared=new float[circleCount];
		circleColor=new Color[circleCount];
		circleCount--;
		
		float td=(t1-t0);
		float xd=(x1-x0);
		float yd=(y1-y0);
		float rd=(r1-r0);
		
		//see if 1 contained in 0
		boolean c0Inc1=(((x0-r0)>(x1-r1))&&((x0+r0)<(x1+r1))&&
		((y0-r0)>(y1-r1))&&((y0+r0)<(y1+r1)));
		
		boolean c1Inc0=(((x1-r1)>(x0-r0))&&((x1+r1)<(x0+r0))&&
				((y1-r1)>(y0-r0))&&((y1+r1)<(y0+r0)));
		
		if(!c0Inc1 && !c1Inc0)
			isCone=true;
		
		// setup values (from largest first so we get the biggest circle at the
		// start
		int i = 0;
		float s;
		float t = t0;
		while (true) {
			t = (t1 - t0) * i / circleCount;
			s = (t - t0) / td;

			cx[i] = x0 + (s * xd);
			cy[i] = y0 + (s * yd);
			cr[i] = r0 + (s * rd);

			crSquared[i] = cr[i] * cr[i]; // square it
			
			if(colorsReversed)
				circleColor[i] = calculateColor(1-t, t0, t1);
			else
				circleColor[i] = calculateColor(t, t0, t1);
			
			if (i == circleCount)
				break;

			i++;
		}


		if (isExtended[0]) {
			i = 0;
			t = (t1 - t0) * i / circleCount;
			s = (t - t0) / td;
			
			if (cr[0] < cr[1]) { // goes from small circle to large
				while ((r0 + (s * rd)) > 0) { // while radius is greater than 0 
					t = (t1 - t0) * -i / circleCount;
					s = (t - t0) / td;

					i++;
				}
			}

			else { // goes from large circle to small
				while ((r0 + (s * rd)) < 500) { // until radius encoumpases entire box
					t = (t1 - t0) * -i / circleCount;
					s = (t - t0) / td;

					i++;
				}
			}

			if(i != 0){
				float[] ex = new float[i], ey = new float[i], er = new float[i];
				float[] erSquared = new float[i];
				Color[] ecircleColor = new Color[i];
	
				i--;
				int count = 0;
				while (i >= 0) {
					t = (t1 - t0) * -i / circleCount;
					s = (t - t0) / td;
	
					ex[count] = x0 + (s * xd);
					ey[count] = y0 + (s * yd);
					er[count] = r0 + (s * rd);
	
					erSquared[count] = er[count] * er[count]; // square it
					ecircleColor[count] = circleColor[0];
	
					count++;
					i--;
				}
	
				cx = concat(ex, cx);
				cy = concat(ey, cy);
				cr = concat(er, cr);
	
				crSquared = concat(erSquared, crSquared);
				circleColor = concat(ecircleColor, circleColor);
	
				circleCount = cx.length - 1;
			}
		}
		
		if(isExtended[1]) {
			i=circleCount + 1;
			if (cr[0] > cr[1]) {
				while ((r0 + (s * rd)) > 0) {
					t = (t1 - t0) * i / circleCount;
					s = (t - t0) / td;

					i++;
				}
			}

			else {
				while ((r0 + (s * rd)) < 500) {
					t = (t1 - t0) * i / circleCount;
					s = (t - t0) / td;

					i++;
				}
			}

			float[] ex = new float[i - (circleCount+1)], ey = new float[i - (circleCount+1)], er = new float[i - (circleCount+1)];
			float[] erSquared = new float[i - (circleCount+1)];
			Color[] ecircleColor = new Color[i - (circleCount+1)];

			i--;
			int count = i-(circleCount+1);
			
			while (i > circleCount) {
				t = (t1 - t0) * i / circleCount;
				s = (t - t0) / td;

				ex[count] = x0 + (s * xd);
				ey[count] = y0 + (s * yd);
				er[count] = r0 + (s * rd);

				erSquared[count] = er[count] * er[count]; // square it
				ecircleColor[count] = circleColor[circleCount];

				count--;
				i--;
			}

			cx = concat(cx, ex);
			cy = concat(cy, ey);
			cr = concat(cr, er);

			crSquared = concat(crSquared, erSquared);
			circleColor = concat(circleColor, ecircleColor);

			circleCount = cx.length - 1;
		}
	}
	
	/**workout rgb color*/
	private Color calculateColor(float val,float t0, float t1) {

        float[] values={val};
		float[] colValues=function.compute(values);
		
		/**
		 * this value is converted to a color
		 */
		shadingColorSpace.setColor(colValues,colValues.length);

        return (Color) shadingColorSpace.getColor();

	}
	
	private static float[] concat(float[] A, float[] B) {
		float[] C= new float[A.length+B.length];
		System.arraycopy(A, 0, C, 0, A.length);
		System.arraycopy(B, 0, C, A.length, B.length);
		
		return C;
	}

	private static Color[] concat(Color[] A, Color[] B) {
		Color[] C= new Color[A.length+B.length];
		System.arraycopy(A, 0, C, 0, A.length);
		System.arraycopy(B, 0, C, A.length, B.length);
		
		return C;
	}	
}
