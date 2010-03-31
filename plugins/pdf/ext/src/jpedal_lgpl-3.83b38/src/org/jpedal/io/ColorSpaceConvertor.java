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
* ColorSpaceConvertor.java
* ---------------
*/
package org.jpedal.io;

import org.jpedal.color.ColorSpaces;
import org.jpedal.color.DeviceCMYKColorSpace;
import org.jpedal.exception.PdfException;
import org.jpedal.utils.LogWriter;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * set of static methods to save/load objects to convert images between 
 * different colorspaces - 
 * 
 * Several methods are very similar and I should recode my code to use a common
 * method for the RGB conversion 
 * 
 * LogWriter is JPedal logging class
 * 
 */
public class ColorSpaceConvertor {

	/**defines rgb colorspace*/
	private static ColorSpace rgbCS;

	/**conversion Op for translating rasters or images*/
	private static ColorConvertOp CSToRGB = null;

    private static ColorConvertOp ccopWithHints = new ColorConvertOp(ColorSpaces.hints);

	/**rgb colormodel*/
	private static ColorModel rgbModel = null;

    public static boolean wasRemoved;
    
    /** Flag to trigger raster printing */
    public static boolean isUsingARGB = false;

    /**initialise all the colorspaces when first needed */
	private static void initCMYKColorspace() throws PdfException {

		try {

		    /**create CMYK colorspace using icm profile*/
		    ICC_Profile p =ICC_Profile.getInstance(ColorSpaceConvertor.class.getResourceAsStream("/org/jpedal/res/cmm/cmyk.icm"));
		    //ICC_ColorSpace cmykCS = new ICC_ColorSpace(p);

			/**create CMYK colorspace using icm profile*
		    ICC_ColorSpace
		    ICC_Profile p =
				ICC_Profile.getInstance(
					loader.getResourceAsStream("org/jpedal/res/cmm/cmyk.icm"));
			cmykCS = new ICC_ColorSpace(p);
*/
			/**create RGB colorspace and model*/
			ICC_Profile rgbProfile = ICC_Profile.getInstance(ColorSpace.CS_sRGB);
			rgbCS = new ICC_ColorSpace(rgbProfile);
			rgbModel = new ComponentColorModel(rgbCS, new int[] { 8, 8, 8 }, false, false, ColorModel.OPAQUE, DataBuffer.TYPE_BYTE);

            /**define the conversion. PdfColor.hints can be replaced with null or some hints*/
			CSToRGB = new ColorConvertOp(new ICC_ColorSpace(p), rgbCS, ColorSpaces.hints);

		} catch (Exception e) {
			LogWriter.writeLog(
				"Exception " + e + " initialising color components");
			
			throw new PdfException("[PDF] Unable to create CMYK colorspace. Check cmyk.icm in jar file");

		}
	}

	/**
	 * save raw CMYK data by converting to RGB using algorithm method - 
	 * pdfsages supplied the C source and I have converted -  
	 * This works very well on most colours but not dark shades which are 
	 * all rolled into black - 
	 * 
	 * This is what xpdf seems to use - 
	 * 
	 * We pass it the name of the file we have previously stored in 
	 * CMYK dir (we just save the encoded DCT stream as xxx.jpg)
	 *
	 
	private static BufferedImage algorithmicConvertCMYKImageToRGBXX() {

		BufferedImage image = null;

		try {

			FileInputStream in =
				new FileInputStream(cmyk_dir + image_name + ".jpg");
			ImageReader currentImageReader =
				(ImageReader) ImageIO
					.getImageReadersByFormatName("JPEG")
					.next();
			ImageIO.setUseCache(false);
			ImageInputStream iin = ImageIO.createImageInputStream(in);
			currentImageReader.setInput(iin, true);

			Raster currentRaster = currentImageReader.readRaster(0, null);

			in.close();
			iin.close();
			currentImageReader.dispose();
			//VERY IMPORTANT - seems to be a memory bug in Java

			int width = currentRaster.getWidth();
			int height = currentRaster.getHeight();
			int pixelCount = width * height;

			int c[] = new int[pixelCount];
			currentRaster.getSamples(0, 0, width, height, 0, c);
			int m[] = new int[pixelCount];
			currentRaster.getSamples(0, 0, width, height, 1, m);
			int y[] = new int[pixelCount];
			currentRaster.getSamples(0, 0, width, height, 2, y);
			int k[] = new int[pixelCount];
			currentRaster.getSamples(0, 0, width, height, 3, k);

			int r, g, b;
			int max_r = 0, max_g = 0, max_b = 0;
			byte[] image_data = new byte[pixelCount * 3];
			for (int i = 0; i < pixelCount; i++) {

				//convert the colours
				r = (c[i] + k[i]);

				if (r > max_r)
					max_r = r;

				if (r > 256)
					r = 0;
				g = (m[i] + k[i]);

				if (g > max_g)
					max_g = g;

				if (g > 256)
					g = 0;
				b = (y[i] + k[i]);
				if (b > max_b)
					max_b = b;
				if (b > 256)
					b = 0;

				image_data[(i * 3)] = (byte) (-r);
				image_data[(i * 3) + 1] = (byte) (-g);
				image_data[(i * 3) + 2] = (byte) (-b);

			}

			DataBuffer db = new DataBufferByte(image_data, image_data.length);

			try {
				int[] bands = new int[3];
				bands[0] = 0;
				bands[1] = 1;
				bands[2] = 2;
				image =
					new BufferedImage(
						width,
						height,
						BufferedImage.TYPE_INT_RGB);
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
			} catch (Exception e) {
				LogWriter.writeLog("Exception " + e + " with 24 bit RGB image");
			}

		} catch (Exception ee) {
			image = null;
			LogWriter.writeLog("Couldn't read JPEG, not even raster: " + ee);
		}

		return image;
	}*/
	
	/**
	 * slightly contrived but very effective way to convert to RGB 
	 */
	public static BufferedImage convertFromICCCMYK(
		int width,
		int height,
		byte[] data,
		ColorSpace cs) {



	    if(cs==null)
	        cs=rgbCS;


        BufferedImage image = null;
		try {

			/**make sure data big enough and pad out if not*/
			int size = width * height * 4;
			if (data.length < size) {
				byte[] newData = new byte[size];
				System.arraycopy(data, 0, newData, 0, data.length);
				data = newData;
			}

			/**turn it into a BufferedImage so we can filter*/
			DataBuffer db = new DataBufferByte(data, data.length);

            int[] bands = { 0, 1, 2, 3 };

            //fix for 1.6 issue
            if(1==1){
            if(rgbModel==null)
                initCMYKColorspace();
            
            WritableRaster rgbRaster=rgbModel.createCompatibleWritableRaster(width, height);
            	CSToRGB.filter(Raster.createInterleavedRaster(db,
                        width, height, width*4, 4, bands, null), rgbRaster);
            	image =new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
                image.setData(rgbRaster);
                if(1==1)
                    return image;
            }

			WritableRaster raster =
				Raster.createInterleavedRaster(
					db,
					width,
					height,
					width * 4,
					4,
					bands,
					null);

			ColorModel cmykModel =
				new ComponentColorModel(
					cs,
					new int[] { 8, 8, 8, 8 },
					false,
					false,
					ColorModel.OPAQUE,
					DataBuffer.TYPE_BYTE);

			image = new BufferedImage(cmykModel, raster, false, null);

		} catch (Exception ee) {
			LogWriter.writeLog(
				"Exception  " + ee + " converting from ICC colorspace");
            ee.printStackTrace();
        }

		return image;

	}

	/**
	 * slightly contrived but very effective way to convert  to RGB 
	 */
	public static BufferedImage convertFromICCCMYK(
		int width,
		int height,
		DataBuffer db,
		ColorSpace cs) {

		BufferedImage image = null;
		try {

			int[] bands = { 0, 1, 2, 3 };

			WritableRaster raster =
				Raster.createInterleavedRaster(
					db,
					width,
					height,
					width * 4,
					4,
					bands,
					null);

			ColorModel cmykModel =
				new ComponentColorModel(
					cs,
					new int[] { 8, 8, 8, 8 },
					false,
					false,
					ColorModel.OPAQUE,
					DataBuffer.TYPE_BYTE);

			image = new BufferedImage(cmykModel, raster, false, null);

		} catch (Exception ee) {
			LogWriter.writeLog(
				"Exception  " + ee + " converting from ICC colorspace");
		}

		return image;

	}
		
	/**
	 * slightly contrived but very effective way to convert CMYK CMAP to RGB -
	 * I've treated the CMAP as an image and converted the values
	 * 
	 * Default is CMYK, but I am trying to allow for other ColorSpaces
	 */
	public static byte[] convertIndexToRGB(byte[] data, ColorSpace cs) {

		try {

			/**turn it into a BufferedImage so we can convert then extract the data*/
			int width = data.length / 4;
			int height = 1;
			DataBuffer db = new DataBufferByte(data, data.length);

			int[] bands = { 0, 1, 2, 3 };
			WritableRaster raster =
				Raster.createInterleavedRaster(
					db,
					width,
					height,
					width * 4,
					4,
					bands,
					null);
			
			WritableRaster rgbRaster =
				rgbModel.createCompatibleWritableRaster(width, height);
			
			//init on first usage
			if(CSToRGB==null)
				initCMYKColorspace();
			CSToRGB.filter(raster, rgbRaster);

			/**put into byte array*/
			int size = width * height * 3;
			data = new byte[size];

			DataBuffer convertedData = rgbRaster.getDataBuffer();

			for (int ii = 0; ii < size; ii++)
				data[ii] = (byte) convertedData.getElem(ii);

		} catch (Exception ee) {
			LogWriter.writeLog("Exception  " + ee + " converting colorspace");
		}

		return data;

	}


	
	/**
	 * convert any BufferedImage to RGB colourspace
	 */
	final public static BufferedImage convertToRGB(BufferedImage image) {

        //don't bother if already rgb or ICC
		if ((image.getType() != BufferedImage.TYPE_INT_RGB)) {

			try{
			    /**/
				BufferedImage raw_image = image;
				image =
					new BufferedImage(
						image.getWidth(),
						image.getHeight(),
						BufferedImage.TYPE_INT_RGB);
				//ColorConvertOp xformOp = new ColorConvertOp(ColorSpaces.hints);/**/

				//THIS VERSION IS AT LEAST 5 TIMES SLOWER!!!
				//ColorConvertOp colOp = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_sRGB), ColorSpaces.hints);
				//image=colOp.filter(image,null);

				//xformOp.filter(raw_image, image);
                ccopWithHints.filter(raw_image, image);
                //image = raw_image;
			} catch (Exception e) {

                e.printStackTrace();
                LogWriter.writeLog(
					"Exception " + e.toString() + " converting to RGB");
			} catch (Error ee) {

                ee.printStackTrace();
                LogWriter.writeLog(
					"Error " + ee.toString() + " converting to RGB");

                image=null;
            }
		}
		
		return image;
	}
	
	/**
	 * convert a BufferedImage to RGB colourspace (used when I clip the image)
	 */
	final public static BufferedImage convertToARGB(BufferedImage image) {

		//don't bother if already rgb
		if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
			try {
				BufferedImage raw_image = image;
				image =
					new BufferedImage(
						raw_image.getWidth(),
						raw_image.getHeight(),
						BufferedImage.TYPE_INT_ARGB);
				ColorConvertOp xformOp = new ColorConvertOp(null);
				xformOp.filter(raw_image, image);
			} catch (Exception e) {
				LogWriter.writeLog("Exception " + e + " creating argb image");
			}
		}
		
		isUsingARGB = true;
		
		return image;
	}

	/**
	 * save raw CMYK data by converting to RGB using algorithm method -  
	 * pdfsages supplied the C source and I have converted - 
	 * This works very well on most colours but not dark shades which are 
	 * all rolled into black 
	 * 
	 * This is what xpdf seems to use - 
	 * <b>Note</b> we store the output data in our input queue to reduce memory 
	 * usage - we have seen raw 2000 * 2000 images and having input and output 
	 * buffers is a LOT of memory - 
	 * I have kept the doubles in as I just rewrote Leonard's code -  
	 * I haven't really looked at optimisation beyond memory issues
	 */
	public static BufferedImage algorithmicConvertCMYKImageToRGB(
		byte[] buffer,
		int w,
		int h) {

		BufferedImage image = null;
		byte[] new_data = new byte[w * h * 3];

		int pixelCount = w * h*4;
			
		double lastC=-1,lastM=-1.12,lastY=-1.12,lastK=-1.21;
		double x=255;
		

		double c, m, y, aw, ac, am, ay, ar, ag, ab;
		double outRed=0, outGreen=0, outBlue=0;
	
		int pixelReached = 0;
		for (int i = 0; i < pixelCount; i = i + 4) {
			
			double inCyan = (buffer[i]&0xff)/x ;
			double inMagenta = (buffer[i + 1]&0xff) / x;
			double inYellow = (buffer[i + 2]&0xff) / x;
			double inBlack = (buffer[i + 3]&0xff) / x;
		
			if((lastC==inCyan)&&(lastM==inMagenta)&&
			        (lastY==inYellow)&&(lastK==inBlack)){
				 //use existing values   
				}else{//work out new
					double k = 1;
					c = clip01(inCyan + inBlack);
					m = clip01(inMagenta + inBlack);
					y = clip01(inYellow + inBlack);
					aw = (k - c) * (k - m) * (k - y);
					ac = c * (k - m) * (k - y);
					am = (k - c) * m * (k - y);
					ay = (k - c) * (k - m) * y;
					ar = (k - c) * m * y;
					ag = c * (k - m) * y;
					ab = c * m * (k - y);
					outRed = x*clip01(aw + 0.9137 * am + 0.9961 * ay + 0.9882 * ar);
					outGreen = x*clip01(aw + 0.6196 * ac + ay + 0.5176 * ag);
					outBlue =
						x*clip01(
							aw
								+ 0.7804 * ac
								+ 0.5412 * am
								+ 0.0667 * ar
								+ 0.2118 * ag
								+ 0.4863 * ab);
					
					lastC=inCyan;
					lastM=inMagenta;
			        	lastY=inYellow;
			        	lastK=inBlack;
				}
			
				new_data[pixelReached++] =(byte)(outRed);
				new_data[pixelReached++] = (byte) (outGreen);
				new_data[pixelReached++] = (byte) (outBlue);

			}

			try {
				/***/
				int[] b = {0,1,2};
				
				DataBuffer db = new DataBufferByte(new_data, new_data.length);
				image =new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);

				Raster raster =Raster.createInterleavedRaster(db,w,h,w * 3,3,b,null);
				image.setData(raster);
			
			} catch (Exception e) {
				LogWriter.writeLog("Exception " + e + " with 24 bit RGB image");
			}

		return image;
	}	
	
	/**
	 * save raw CMYK data by converting to RGB using algorithm method -
	 * pdfsages supplied the C source and I have converted - 
	 * This works very well on most colours but not dark shades which are 
	 * all rolled into black 
	 *
	 */
	public static BufferedImage algorithmicConvertCMYKImageToRGB(
		DataBuffer data,
		int w,
		int h,boolean debug) {

        wasRemoved=false;

        byte[] buffer=((DataBufferByte)data).getData();


        BufferedImage image = null;
		byte[] new_data = new byte[w * h * 4];

		int pixelCount = w * h*4;

        boolean nonTransparent=false,isAllBlack=true;

		int r=0,g=0,b=0;
		int lastC=-1,lastM=-1,lastY=-1,lastK=-1;
		int pixelReached = 0;
		       
		for (int i = 0; i < pixelCount; i = i + 4) {
			
			int Y = ((buffer[i] & 255));
			int Cb = ((buffer[1+i] & 255))-128;
			int Cr = ((buffer[2+i] & 255))-128;
			int CENTER = ((buffer[3+i] & 255));

            int a=255;
            if((lastC==Y)&&(lastM==Cb)&&(lastY==Cr)&&(lastK==CENTER)){
			 //use existing values
            }else{//work out new

                if (debug)
                    System.out.println(Y + " " + Cb + ' ' + Cr + ' ' + CENTER);

                r = (int)(Y - CENTER + 1.402f * Cr);
                if(r<0)
                r=0;
                if(r>255)
                r=255;

                g = (int)((Y - CENTER) - 0.34414f * Cb - 0.71414f * Cr);
                if(g<0)
                g=0;
                if(g>255)
                g=255;

                b = (int)((Y - CENTER) + 1.772f * Cb);
                if(b<0)
                b=0;
                if(b>255)
                b=255;

                //track blanks
                if(Y==255 && Cr==0 && Cb==0 && CENTER==255) {

                }else
                    isAllBlack=false;

                if (Y == 255 && Cr == Cb && (CENTER==0 && Cr!=0)) {

                   // System.out.println(Y + " " + Cb + " " + Cr + " " + CENTER);

                    r = 255;
                    g = 255;
                    b = 255;

                   if(CENTER==255)
                    a =0;
                   else
                    nonTransparent=true;

                }else
                     nonTransparent=true;
                
                if (debug)
                    System.out.println(r+" "+g+ ' ' +b);

				lastC=Y;
				lastM=Cb;
				lastY=Cr;
				lastK=CENTER;
				
			}

            new_data[pixelReached++] =(byte) (r);
			new_data[pixelReached++] = (byte) (g);
			new_data[pixelReached++] = (byte) (b);
            new_data[pixelReached++] = (byte) (a);

			}


            if(!nonTransparent || isAllBlack){

               wasRemoved=true;
               return null;
            }
        
            try {
				/***/
				int[] bands = {0,1,2,3};
				
				DataBuffer db = new DataBufferByte(new_data, new_data.length);
				image =new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);

				Raster raster =Raster.createInterleavedRaster(db,w,h,w * 4,4,bands,null);
				image.setData(raster);

            } catch (Exception e) {
			    System.out.println(e);
			    e.printStackTrace();
				LogWriter.writeLog("Exception " + e + " with 24 bit RGB image");
			}

		return image;
	}


    public static BufferedImage convertIndexedToFlat(int d,int w, int h, byte[] data, byte[] index, int size,boolean isARGB, boolean isDownsampled) {

        BufferedImage image;
        DataBuffer db;
        int[] bandsRGB = {0,1,2};
        int[] bandsARGB = {0,1,2,3};
        int[] bands;
        int components=3,id1,pt=0;

        if(isARGB){
            bands=bandsARGB;
            components=4;
        }else
            bands=bandsRGB;

        int length=(w*h*components);

        byte[] newData=new byte[length];

        int id=0;
        float ratio=0f;

        if(d==8){

            for(int ii=0;ii<data.length-1;ii++){

                if(isDownsampled)
                    ratio=(data[ii] & 0xff)/255f;
                else
                    id=(data[ii] & 0xff)*3;

                if(pt>=length)
                    break;

                //System.err.println(pt+"/"+newData.length+" "+//id+"/"+index.length);
                if(isDownsampled){
                    if(ratio>0){
                        newData[pt++]= (byte) ((255-index[0])*ratio);
                        newData[pt++]= (byte) ((255-index[1])*ratio);
                        newData[pt++]= (byte) ((255-index[2])*ratio);
                    }else
                        pt=pt+3;
                }else{
                    newData[pt++]=index[id];
                    newData[pt++]=index[id+1];
                    newData[pt++]=index[id+2];
                }

                if(isARGB){
                    if(id==0 && ratio==0)
                        newData[pt++]=(byte)255;
                    else
                        newData[pt++]=0;
                }
            }
        }else if(d==4){

            int[] shift={4,0};
            int widthReached=0;

            for(int ii=0;ii<data.length;ii++){

                for(int samples=0;samples<2;samples++){

                    id1=((data[ii]>>shift[samples]) & 15)*3;

                    if(pt>=length)
                        break;

                    newData[pt++]=index[id1];
                    newData[pt++]=index[id1+1];
                    newData[pt++]=index[id1+2];

                    if(isARGB){
                        if(id1==0)
                            newData[pt++]=(byte)0;
                        else
                            newData[pt++]=0;
                    }

                    //ignore filler bits
                    widthReached++;
                    if(widthReached==w){
                        widthReached=0;
                        samples=8;
                    }
                }
            }
        }else if(d==2){

            int[] shift={6,4,2,0};
            int widthReached=0;

            for(int ii=0;ii<data.length;ii++){

                for(int samples=0;samples<4;samples++){


                    id1=((data[ii]>>shift[samples]) & 3)*3;

                    if(pt>=length)
                        break;

                    newData[pt++]=index[id1];
                    newData[pt++]=index[id1+1];
                    newData[pt++]=index[id1+2];

                    if(isARGB){
                        if(id1==0)
                            newData[pt++]=(byte)0;
                        else
                            newData[pt++]=0;
                    }

                    //ignore filler bits
                    widthReached++;
                    if(widthReached==w){
                        widthReached=0;
                        samples=8;
                    }
                }
            }
        }else if(d==1){

            //work through the bytes
            int widthReached=0;
            for(int ii=0;ii<data.length-1;ii++){

                for(int bits=0;bits<8;bits++){

                    //int id=((data[ii] & (1<<bits)>>bits))*3;
                    id=((data[ii]>>(7-bits)) & 1)*3;

                    if(pt>=length)
                        break;

//					@itemtoFix
                    if(isARGB){


                        //System.out.println(id+" "+index[id]+" "+index[id+1]+" "+index[id+2]);

                        //treat white as transparent
                        if(1==2 && (index[id] & 255)>250 && (index[id+1] & 255)>250 && (index[id+2] & 255)>250){
                            pt=pt+4;
                        }else if(id==0){
                            newData[pt++]=index[id];
                            newData[pt++]=index[id+1];
                            newData[pt++]=index[id+2];

                            newData[pt++]=(byte)255;

                        }else{
                            newData[pt++]=index[id];
                            newData[pt++]=index[id+1];
                            newData[pt++]=index[id+2];

                            newData[pt++]=0;
                            //System.out.println(id+" "+index[id]+" "+index[id+1]+" "+index[id+2]);
                        }

                    }else{
                        newData[pt++]=index[id];
                        newData[pt++]=index[id+1];
                        newData[pt++]=index[id+2];

                    }
                    //ignore filler bits
                    widthReached++;
                    if(widthReached==w){
                        widthReached=0;
                        bits=8;
                    }
                }
            }

        }else{

        }

        /**create the image*/
        db = new DataBufferByte(newData, newData.length);
        if(isARGB)
            image =new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
        else
            image =new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);

        WritableRaster raster =Raster.createInterleavedRaster(db,w,h,w * components,components,bands,null);
        image.setData(raster);

        return image;
    }

    

    /**
	 * save raw CMYK data by converting to RGB using algorithm method -
	 * pdfsages supplied the C source and I have converted -
	 * This works very well on most colours but not dark shades which are
	 * all rolled into black
     *
     * profile is a set of comma deliminated values providing YCC, CMY and optionally CMYK from
     * -Dorg.jpedal.useICC (remember double quotes if it includes spaces)
     * example -Dorg.jpedal.useICC="YCC=C:/profiles/ycc.pf,CMY=C:/profiles/cmy.pf
     * (can include optional CMYK= values if you wish to change default CMYK profile used)
     *
	 *
	 */
    private static ICC_ColorSpace YCC=null,CMY=null;
    private static ColorSpace CMYK=null;

    private static int cachedValues =0;
    private static Map cache =new HashMap();

    public static BufferedImage iccConvertCMYKImageToRGB(
		byte[] buffer,
		int w,
		int h,String profile) throws IOException {

        /**
         * get profile values for colorspaces for ICC colorspaces
         */
        String YCCprofile=null,CMYprofile=null,CMYKprofile=null;

        profile=profile.replaceAll("\"","");

        StringTokenizer profiles=new StringTokenizer(profile,",");

        while(profiles.hasMoreTokens()){
            String nextProfile=profiles.nextToken();

            //extract value
            int index=nextProfile.indexOf('=');

            if(index==-1)
            throw new RuntimeException("Wrong parameter in "+profile+"\nPlease use comma-separated set of uses such as YCC=/desktop/ycc.pf,CMY=/desktop/cmy.pf");

            String key=nextProfile.substring(0,index).trim();
            String value=nextProfile.substring(index+1).trim();

            if(key.equals("YCC"))
                YCCprofile=value;
            else if(key.equals("CMY"))
                CMYprofile=value;
            else if(key.equals("CMYK"))
                CMYKprofile=value;
            else
                throw new RuntimeException("Unknown parameter ("+key+") in "+profile+"\nPlease use comma-separated set of uses such as YCC= CMY= or (optional value) CMYK=");

        }

        /**
         * set colorspaces and color models using profiles ifn ot previously initialised
         */
        if(CMYK==null){

            //optional alternative CMYK
            if(CMYKprofile==null)
                CMYK=new DeviceCMYKColorSpace().getColorSpace();
            else{
                try {
                    CMYK=new ICC_ColorSpace(ICC_Profile.getInstance(new FileInputStream(CMYKprofile)));
                } catch (Exception e) {
                    throw new RuntimeException("Unable to create CMYK colorspace with  "+CMYKprofile+"\nPlease check Path and file valid or use built-in");
                }
            }

            if(YCCprofile==null){
                throw new RuntimeException("Mandatory YCC value not supplied in "+profile+"\nPlease add ,YCC=yourPath/yourYCCprofile.pf");   
            }else{
                try {
                    YCC=new ICC_ColorSpace(ICC_Profile.getInstance(new FileInputStream(YCCprofile)));
                } catch (Exception e) {
                    throw new RuntimeException("Unable to create YCC colorspace with  "+YCCprofile+"\nPlease check Path and file valid");
                }
            }

            if(CMYprofile==null){
                throw new RuntimeException("Mandatory CMY value not supplied in "+profile+"\nPlease add ,CMY=yourPath/yourCMYprofile.pf");
            }else{
                try {
                    CMY=new ICC_ColorSpace(ICC_Profile.getInstance(new FileInputStream(CMYprofile)));
                } catch (Exception e) {
                    throw new RuntimeException("Unable to create CMY colorspace with  "+CMYprofile+"\nPlease check Path and file valid");
                }
            }

            //needed for sRGB conversion
            rgbCS = new ICC_ColorSpace(ICC_Profile.getInstance(ColorSpace.CS_sRGB));
            rgbModel = new ComponentColorModel(rgbCS, new int[] { 8, 8, 8 }, false, false, ColorModel.OPAQUE, DataBuffer.TYPE_BYTE);
            CSToRGB = new ColorConvertOp(CMYK, rgbCS, ColorSpaces.hints);

        }

		int pixelCount = w * h*4;
        int Y,Cb,Cr,CENTER,lastY=-1,lastCb=-1,lastCr=-1,lastCENTER=-1;
        float[] CMYvalues=new float[3],YCCvalues,CIEvalues;

        if(YCC==null)
            throw new RuntimeException("Unable to create YCC colorspace with  "+YCCprofile+"\nPlease check Path and file valid");

        if(CMY==null)
            throw new RuntimeException("Unable to create CMY colorspace with  "+CMYprofile+"\nPlease check Path and file valid");

        //turn YCC in Buffer to CYM using profile
        for (int i = 0; i < pixelCount; i = i + 4) {

            Y=(buffer[i] & 255);
            Cb = (buffer[i+1] & 255);
			Cr = (buffer[i+2] & 255);
			CENTER = (buffer[i+3] & 255);

            if(Y==lastY && Cb==lastCb && Cr==lastCr && CENTER==lastCENTER){
                //no change so use last value
            }else{ //new value

                //we store values to speedup operation
                Integer key=new Integer(Y+(Cb<<8)+(Cr<<16));
                Object cachedValue= cache.get(key);

                if(cachedValue==null){

                    //this is the slow bit
                    YCCvalues= new float[]{(Y / 255f), (Cb / 255f), (Cr / 255f)};
                    CIEvalues=YCC.toCIEXYZ(YCCvalues);
                    CMYvalues=CMY.fromCIEXYZ(CIEvalues);

                    //stop cache taking up too much memory by flushing if too many values
                    if(cachedValues >100000){
                        cachedValues =0;
                        cache.clear();
                    }
                    cache.put(key,CMYvalues);
                    cachedValues++;

                }else{
                    CMYvalues=(float[]) cachedValue;
                }
                //flag so we can just reuse if next value the same
                lastY=Y;
                lastCb=Cb;
                lastCr=Cr;
                lastCENTER=CENTER;
            }

            //put back as CMY
            buffer[i]=(byte)(CMYvalues[0]*255);
            buffer[i+1]=(byte)(CMYvalues[1]*255);
            buffer[i+2]=(byte)(CMYvalues[2]*255);
        }

        /**
         * create CMYK raster from buffer
         */
        Raster raster = Raster.createInterleavedRaster(new DataBufferByte(buffer,buffer.length), w,h,w * 4,4, new int[]{ 0, 1, 2, 3 },null);
        WritableRaster rgbRaster =rgbModel.createCompatibleWritableRaster(w, h);

        /**
         * convert to sRGB fwith profiles (I think this is done native as its much faster than my pure Java efforts)
         */
        CSToRGB.filter(raster, rgbRaster);

        //data now sRGB so create image
        BufferedImage image =new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        image.setData(rgbRaster);

        return image;
	}

    /**
	 * convert a BufferedImage to RGB colourspace
	 */
	final public static BufferedImage convertColorspace(
		BufferedImage image,
		int newType) {

		try {
			BufferedImage raw_image = image;
			image =
				new BufferedImage(
					raw_image.getWidth(),
					raw_image.getHeight(),
					newType);
			ColorConvertOp xformOp = new ColorConvertOp(null);
			xformOp.filter(raw_image, image);
		} catch (Exception e) {
			LogWriter.writeLog("Exception " + e + " converting image");

		}

		return image;
	}
	
	/**convenience method used to check value within bounds*/
	private static double clip01(double value) {

		if (value < 0)
			value = 0;

		if (value > 1)
			value = 1;

		return value;
	}	
}
