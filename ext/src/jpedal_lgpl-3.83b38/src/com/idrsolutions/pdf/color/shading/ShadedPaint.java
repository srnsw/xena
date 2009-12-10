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
* ShadedPaint.java
* ---------------
*/
package com.idrsolutions.pdf.color.shading;

import org.jpedal.PdfDecoder;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.color.PdfPaint;
import org.jpedal.function.FunctionFactory;
import org.jpedal.function.PDFFunction;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.utils.Matrix;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.io.Serializable;

/**
 * template for all shading operations
 */
public class ShadedPaint implements PdfPaint,Paint, Serializable {

	public static final int FUNCTION = 1;
	public static final int AXIAL = 2;
	public static final int RADIAL = 3;
	public static final int FREEFORM = 4;
	public static final int LATTICEFORM = 5;
	public static final int COONS = 6;
	public static final int TENSOR = 7;

	static final private boolean debug=false;

	protected PDFFunction function;

	protected int pageHeight;

	/**colorspace to use for shading*/
	protected GenericColorSpace shadingColorSpace=null;

    private PdfObject Shading;

	/**optional bounding box*/
	//protected float[] BBox=null;

	protected float[] coords;

	/**optional flag*/
	protected boolean AntiAlias=false;

	/**type used - see values in ShadingFactory*/
	protected int shadingType;

	protected float[] domain={0.0f,1.0f};

	private int type;

	private boolean[] isExtended=new boolean[2];
	private boolean colorsReversed;
	private float scaling;
	private int cropX;
	
	private int textX,textY;
	private int cropH;
	private float[] background;
	private boolean isPrinting;

    public ShadedPaint(){}

	/**read general values*/
	public ShadedPaint(PdfObject Shading, boolean isPrinting,int pageY,int type,GenericColorSpace shadingColorSpace,PdfObjectReader currentPdfFile,float[][] matrix,int pageHeight,boolean colorsReversed){


        //@newspeed can we lose currentPdfFile above?

        this.isPrinting=isPrinting;
		this.colorsReversed=colorsReversed;
		this.type=type;
		this.pageHeight=pageHeight;

		init(Shading, pageY, shadingColorSpace, currentPdfFile, matrix);

	}

	public void init(PdfObject Shading, int pageY,GenericColorSpace shadingColorSpace,PdfObjectReader currentPdfFile,float[][] matrix){

		/**
		 * read axial specific values not read in generic
		 */
        boolean[] extension=Shading.getBooleanArray(PdfDictionary.Extend);
        if(extension!=null)
                isExtended=extension;

        /**
		 * get colorspace
		 */
		this.shadingColorSpace=shadingColorSpace;
        this.Shading=Shading;

		/**
		 * read standard shading values
		 */
		shadingType=Shading.getInt(PdfDictionary.ShadingType);

		background=Shading.getFloatArray(PdfDictionary.Background);

		//BBox=Shading.getFloatArray(PdfDictionary.BBox);

        AntiAlias=Shading.getBoolean(PdfDictionary.AntiAlias);

        /**
		 * these values appear in several types of shading but not all
		 */
        PdfObject functionObj=Shading.getDictionary(PdfDictionary.Function);

        /** setup the translation function */
		if(functionObj!=null)
			function = FunctionFactory.getFunction(functionObj, currentPdfFile);


        float[] newDomain=Shading.getFloatArray(PdfDictionary.Domain);
        if(newDomain!=null)
                domain=newDomain;

        float[] Coords=Shading.getFloatArray(PdfDictionary.Coords);
        if(Coords!=null){

            int len=Coords.length;
            coords=new float[len];
            System.arraycopy(Coords,0,coords,0,len);

            if (matrix != null) {

				if(debug)
					Matrix.show(matrix);

				float a = matrix[0][0], b = matrix[0][1], c = matrix[1][0], 
				d = matrix[1][1], tx = matrix[2][0], ty = matrix[2][1];

				float x,y,x1,y1;

				if(type==AXIAL){ //axial
					x = coords[0]; y = coords[1]; x1 = coords[2]; y1 = coords[3];
					coords[0] = (a * x) + (c * y) + tx;
					coords[1] = (b * x) + (d * y) + ty;
					coords[2] = (a * x1) + (c * y1) + tx;
					coords[3] = (b * x1) + (d * y1) + ty;

					if(debug){
						System.out.println(coords[0]+" "+coords[1]);
						System.out.println(coords[2]+" "+coords[3]);
						System.out.println("=============================");
					}

				}else if(type==RADIAL){  //radial

					//x,y
					x = coords[0]; y = coords[1]; x1 = coords[3]; y1 = coords[4];
					coords[0] = (a * x) + (c * y) + tx;
					coords[1] = (b * x) + (d * y) + ty;
					coords[3] = (a * x1) + (c * y1) + tx;
					coords[4] = (b * x1) + (d * y1) + ty;

					/**/
					//r0
					x1 = coords[2];
					float mx = (a * x1);
					float my = (b * x1);

					coords[2] = (float) Math.sqrt((mx * mx) + (my * my));

					//r1
					x1 = coords[5];
					mx = (a * x1);
					my = (b * x1);			

					coords[5] = (float) Math.sqrt((mx * mx) + (my * my));
					/**/

					if(d<0){
						float tmp = coords[5];
						coords[5] = coords[2];
						coords[2] = tmp;
						colorsReversed=true;
					}

					if(debug){
						System.out.println("x0 = "+coords[0]+" y0 = "+coords[1]+" r0 = "+coords[2]);
						System.out.println("x1 = "+coords[3]+" y1 = "+coords[4]+" r1 = "+coords[5]);
						System.out.println("=============================");
					}
				}
			}else if(type==AXIAL && PdfDecoder.isRunningOnMac){
				if(coords[1]>coords[3])
					colorsReversed=true;
			}
		}
	}

	public PaintContext createContext(ColorModel cm,Rectangle db, Rectangle2D ub,
			AffineTransform xform, RenderingHints hints) {

		PaintContext pt=null;

		float printScale=1f;

		//@printIssue - creates the paintContext which converts physical pixels into PDF co-ords and
		//sets colour accordingly. The original code works on screen but not print
		int offX=0,offY=0;

		if(!isPrinting){
			if(DynamicVectorRenderer.marksNewCode){
				offX=(int) (xform.getTranslateX()+cropX-(textX*scaling));
				offY=(int) (xform.getTranslateY()-cropH+(textY*scaling));
			}else{
				offX=(int) (xform.getTranslateX()+cropX);
				offY=(int) (xform.getTranslateY()-cropH);
			}
		}else{
			offX=(int) xform.getTranslateX();
			offY=(int) xform.getTranslateY();
			scaling=(float)xform.getScaleY();
		}

		switch(type){
		case FUNCTION :

            pt= new FunctionContext(cropH,(float)(1f/xform.getScaleX()),domain, shadingColorSpace,colorsReversed, function);

			break;
		case AXIAL :

			pt= new AxialContext(printScale,isPrinting,offX,offY,cropX,cropH,1f/scaling,isExtended,domain,coords, shadingColorSpace,colorsReversed,background, function);
			
			break;
		case RADIAL :
			
			pt= new RadialContext(isPrinting,offX,offY,cropX,cropH,1f/scaling,isExtended,domain,coords, shadingColorSpace,colorsReversed,background, function);

			break;
		case FREEFORM :
			//shading=new FreeFormPaint(values, currentPdfFile,matrix);
			break;
		case LATTICEFORM :
			//shading=new LatticeFormPaint(values, currentPdfFile,matrix);
			break;
		case COONS :
			pt=new CoonsContext(Shading, cropH,(float)(1f/xform.getScaleX()),domain, shadingColorSpace,colorsReversed, function);
			break;
		case TENSOR :
			//TensorContext tc = new TensorContext(values, currentPdfFile,matrix);
			break;

		default:
		break;/**/
		}
		/**/

		return pt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Transparency#getTransparency()
	 */
	public int getTransparency() {
		return 0;
	}

	public void setScaling(double cropX,double cropH,float scaling, float textX, float textY){
		this.scaling=scaling;
		this.cropX=(int)cropX;
		this.cropH=(int)cropH;
		this.textX=(int)textX;
		this.textY=(int)textY;
	}

	public boolean isPattern() {
		return true;
	}

	public void setPattern(int dummy) {

	}

	public int getRGB() {
		return 0;
	}

}
