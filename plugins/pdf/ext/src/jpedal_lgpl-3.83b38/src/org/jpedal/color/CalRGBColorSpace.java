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
* CalRGBColorSpace.java
* ---------------
*/
package org.jpedal.color;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;

import org.jpedal.utils.LogWriter;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.sun.image.codec.jpeg.JPEGCodec;


/**
 * handle CalRGBColorSpace
 */
public class CalRGBColorSpace
extends  GenericColorSpace{

	private static final long serialVersionUID = 4569336292751894930L;

	private int r,g,b;

//	private final double[][] xyzrgb = {
//	{  3.240449, -1.537136, -0.498531 },
//	{ -0.969265,  1.876011,  0.041556 },
//	{  0.055643, -0.204026,  1.057229 }};


	/**cache for values to stop recalculation*/
	private float lastC=-255,lastI=-255,lastE=-255;	

	public CalRGBColorSpace(float[] whitepoint,float[] blackpoint,float[] matrix,float[] gamma) {

		cs = ColorSpace.getInstance(ColorSpace.CS_CIEXYZ);

		setCIEValues(whitepoint,blackpoint,null,matrix,gamma);
		value = ColorSpaces.CalRGB;

	}

	/**
	 * convert to RGB and return as an image
	 */
	final public BufferedImage  dataToRGB(byte[] data,int width,int height) {

		BufferedImage image = null;
		DataBuffer db = new DataBufferByte(data, data.length);
		int size = width * height;
		
		try {

			for (int i = 0;
			i < size * 3;
			i = i + 3) { //convert all values to rgb

				float cl = data[i] & 255;
				float ca = data[i + 1] & 255;
				float cb = data[i+ 2] & 255;

				convertToRGB(cl, ca, cb);

				db.setElem(i, r);
				db.setElem(i + 1, g);
				db.setElem(i + 2, b);

			}

			int[] bands = { 0, 1, 2 };
			image =
				new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Raster raster =
				Raster.createInterleavedRaster(
						db,
						width,
						height,
						width * 3,
						3,
						bands,
						null);
			image.setData(raster);

		} catch (Exception ee) {
			image = null;
			LogWriter.writeLog("Couldn't read JPEG, not even raster: " + ee);
		}

		return image;

	}

	/**
	 * set CalRGB color (in terms of rgb)
	 */
	final public void setColor(String[] number_values,int items) {

		float[] colValues=new float[items];
		
		for(int ii=0;ii<items;ii++)
			colValues[ii]=Float.parseFloat(number_values[ii]);
		
		setColor(colValues,items);
	}
	
	/**
	 * reset any defaults if reused
	 */
	final public void reset(){
		lastC=-255;
		lastI=-255;
		lastE=-255;	
		
		r=0;
		g=0;
		b=0;
		
		currentColor = new PdfColor(0,0,0);
	}
	
	/**
	 * set CalRGB color (in terms of rgb)
	 */
	final public void setColor(float[] number_values,int items) {

		//get values (and allow for mapped from separation where only 1 value
		float[] A = { 1.0f, 1.0f, 1.0f };

		//allow for use as alt colorspace which only has one value
		if (items == 3) {
			for (int i = 0; i < items; i++){
				A[i] =number_values[i];
				if(A[i]>1)
					return;
			}
		}

		convertToRGB(A[0],A[1],A[2]);
		this.currentColor= new PdfColor(r,g,b);
		
		//System.out.println(A[0]+" "+A[1]+" "+A[2]+" rgb="+r+" "+g+" "+b+" "+currentColor);
	}

	final private void convertToRGB(float C,float I, float E){

		if((lastC==C)&&(lastI==I)&&(lastE==E)){	

		}else{
			/**
                //thanks Leonard for the formula
                double ag = Math.pow( C, G[0] );
                double bg = Math.pow( I, G[1] );
                double cg = Math.pow( E, G[2] );

                double X = Ma[0]*ag + Ma[3]*bg + Ma[6]*cg;
                double Y = Ma[1]*ag + Ma[4]*bg + Ma[7]*cg;
                double Z = Ma[2]*ag + Ma[5]*bg + Ma[8]*cg;

                // convert XYZ to RGB, including gamut mapping and gamma correction
                double rawR = xyzrgb[0][0] * X + xyzrgb[0][1] * Y + xyzrgb[0][2] * Z;
                double rawG = xyzrgb[1][0] * X + xyzrgb[1][1] * Y + xyzrgb[1][2] * Z;
                double rawB = xyzrgb[2][0] * X + xyzrgb[2][1] * Y + xyzrgb[2][2] * Z;

                // compute the white point adjustment
                double kr = 1 / ((xyzrgb[0][0] * W[0]) + (xyzrgb[0][1] * W[1]) + (xyzrgb[0][2] *W[2]));
                double kg = 1 / ((xyzrgb[1][0] * W[0]) + (xyzrgb[1][1] * W[1]) + (xyzrgb[1][2] *W[2]));
                double kb = 1 / ((xyzrgb[2][0] * W[0]) + (xyzrgb[2][1] *  W[1]) +(xyzrgb[2][2] *W[2]));

                // compute final values based on
                r = (int) (255*Math.pow(clip(rawR * kr), 0.5));
                g = (int) (255*Math.pow(clip(rawG * kg), 0.5));
                b = (int) (255*Math.pow(clip(rawB * kb), 0.5));
			 */

			//calcuate using Tristimulus values
			r=(int)(C*255);
			g=(int)(I*255);
			b =(int)(E*255);

			lastC=C;
			lastI=I;
			lastE=E;	
		}
	}

	/**
	 * convert Index to RGB
	 */
	final public byte[] convertIndexToRGB(byte[] index){

        isConverted=true;
		/**
		//array
		int size=index.length;
		byte[] newData=new byte[size];

		for(int i=0;i<size-1;i=i+3){

			float C=(float)index[i]-128f;
			float I=(float)index[i+1]- 128f;
			float E=(float)index[i+2]-128f;

			convertToRGB(C,I,E);

			//System.out.println(C+" "+I+" "+E+" = "+r+" "+g+" "+b);

			newData[i]=(byte) r;
			newData[i+1]=(byte) g;
			newData[i+2]=(byte) b;

	  	}
		 */

		return index;
	}	

}
