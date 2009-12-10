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
* AxialContext.java
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

public class AxialContext implements PaintContext {
	
	private float maxPDFX=-9999,maxPDFY=-9999,minPDFX=9999,minPDFY=9999;
	
	GenericColorSpace shadingColorSpace;
	
	private float scaling=1f;
	
	private boolean[] isExtended;
	
	private float x0=0,x1=0,y0=0,y1=0,t0=0.0f,t1=1.0f;

	private PDFFunction function;	

    private boolean isPrinting;
    //private static Object[][] cachedXY=new Object[1000][1000];

    private AxialContext(){}
	
	private int pageHeight;

	private boolean colorsReversed;
	
	private int minX=0;

	private float[] background;

	private float offX,offY;
	
	private float printScale=4;

    private static float lastScaling=-1;
    
    private static boolean useCaching=false;

    static{
        String flag=System.getProperty("org.jpedal.faster_color_decoding");
        if(flag!=null && flag.toLowerCase().equals("true"))
        	useCaching=true;

    }

    
    //@printIssue - this is where we pass values through
    public AxialContext(float printScale,boolean isPrinting, int offX,int offY, int minX,int pHeight,float scaling,boolean[] isExtended,float[] domain,
                        float[] coords,GenericColorSpace shadingColorSpace,boolean colorsReversed,float[] background, PDFFunction function){

        //reset cached
//        if(scaling!=lastScaling){
//             cachedXY=new Object[1000][1000];
//            lastScaling=scaling;
//        }

    	this.printScale=printScale;
        this.isPrinting=isPrinting;

        this.offX=offX;
		this.offY=offY;
		
		this.colorsReversed=colorsReversed;
		this.pageHeight=pHeight;
		this.isExtended=isExtended;
		this.t0=domain[0];
		this.t1=domain[1];
		this.background=background;
		
		x0=coords[0];
		x1=coords[2];
		y0=coords[1];
		y1=coords[3];
		
		this.shadingColorSpace=shadingColorSpace;
		
		this.function = function;
		this.scaling=scaling;
		
		this.minX=minX;

    }
	public void dispose() {}
	
	public ColorModel getColorModel() { return ColorModel.getRGBdefault(); }
	
	/**
	 * setup the raster with the colors
	 * */
	public static int x=0;

	public Raster getRaster(int xstart, int ystart, int w, int h) {

		//sets up the array of pixel values
		WritableRaster raster =getColorModel().createCompatibleWritableRaster(w, h);
        
		//just average
		boolean isTooSmall=w/scaling<=1f || h/scaling<=1f;
		
        //create buffer to hold all this data
		int rastSize=(w * h * 4);
		
		int[] data = new int[rastSize];
		
		//System.out.println("Area="+xstart+" ystart="+ystart+" w="+w+" h="+h+" "+data.length);
        
		float xx,t=0f,lastT=-1f;
		final float dx=x1-x0,dy=y1-y0;
		
		//workout outside loop as constant
		float divisor=((dx*dx)+(dy*dy));

        //workout color range
		Color col0;
		
		if(colorsReversed)
			col0= calculateColor(t1,t0, t1);
		else
			col0= calculateColor(t0,t0, t1);

		//set current calues to default
		int cr=col0.getRed(),cg=col0.getGreen(),cb=col0.getBlue();

		if(background!=null){
			//y co-ordinates
			for (int y = 0; y < h; y++) {
				
				//x co-ordinates			
				for (int x = 0; x < w; x++) {

					shadingColorSpace.setColor(background,4);
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
		
		float pdfX=0;
        float pdfY=0;
		
		//y co-ordinates
		for (int y = 0; y < h; y++) {
			
			//x co-ordinates			
			for (int x = 0; x < w; x++) {
				/**
				 *take x and y and pass through conversion with domain values - this gives us xx
				 */
				
                //hack for MAC which is f**king broken
                //now appears to work in latest JREs so removed
                if(1==2 && PdfDecoder.isRunningOnMac)
					xx=((dx*((x+xstart)-x0))+(dy*((y1+y+ystart)-y0)))/divisor;
				else {

                    //cache what is quite a slow operation
                    float[] xy=null;
//					if(x<1000 && y<1000){
//                        xy= (float[]) cachedXY[x][y];
//                    }
                    
                    //if(xy==null){
                        xy = PixelFactory.convertPhysicalToPDF(isPrinting, pdfX, pdfY, x, y, offX, offY, scaling, xstart, ystart, minX, pageHeight);
                      //  cachedXY[x][y]=xy;
                    //}

                    pdfX = xy[0];
                    pdfY = xy[1];

                    if(isTooSmall){
                    	xx=0.5f;
                    }else
                    	xx=((dx*(pdfX-x0))+(dy*((pdfY)-y0)))/divisor;
                    
                    //invert for print as wrong way round
                    float yDiff=y0-y1;
                    //if(yDiff<0)
                    	//yDiff=-yDiff;
                    
                    //System.out.println("Ydiff=================="+yDiff);
                    if(isPrinting && yDiff<0)
                    	xx=1-xx;
                    
                    /**debug code*/
                    if(pdfX>maxPDFX)
                    	maxPDFX=pdfX;
                    if(pdfX<minPDFX)
                    	minPDFX=pdfX;
                    
                    if(pdfY>maxPDFY)
                    	maxPDFY=pdfY;
                    if(pdfY<minPDFY)
                    	minPDFY=pdfY;
                    /**/
                    
                }

                /**check range*/
				t=-1f; //default is no valid setting
				if((xx<0)&&(isExtended[0])){
					t=t0;
				}else if((xx>1)&&(isExtended[1])){
					t=t1;
				}else{
					t=t0+((t1-t0)*xx);
					//t=1;
				}
				
				if(isTooSmall)
					t=0.5f;

                /**
				 * proceed if valid
				 */
				if(t>=0 && t<=1){

                    if(colorsReversed)
						t=1-t;

                    if(t!=lastT){ //cache unchanging values
						
						lastT=t;
						
						/**
						 * workout values
						 */
						
						Color c=calculateColor(t,t0, t1);
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
		}
		
		//set values
		raster.setPixels(0, 0, w, h, data);

        return raster;
	}
	
	/**workout rgb color*/
	private Color calculateColor(float val,float t0, float t1) {

		Color col=null;

		if(useCaching){
	        col=shadingColorSpace.getCachedShadingColor(val);
	
	        if(col!=null)
	        return col;
		}

        float[] values={val};
		float[] colValues=function.compute(values);
		
		/**
		 * this value is converted to a color
		 */
		shadingColorSpace.setColor(colValues,colValues.length);

        col=(Color) shadingColorSpace.getColor();

        if(useCaching)
        shadingColorSpace.setShadedColor(val,col);

        return col;

	}	
}
