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
 * SeparationColorSpace.java
 * ---------------
 */

package org.jpedal.color;

import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.util.*;

/**
 * handle Separation ColorSpace and some DeviceN functions
 */
public class SeparationColorSpace extends GenericColorSpace {

	protected GenericColorSpace altCS;

	final private static int Black=1009857357;
	final private static int Cyan=323563838;
	final private static int Magenta=895186280;
	final public static int Yellow=1010591868;

	protected ColorMapping colorMapper;

    //avoid rereading colorspaces
    protected Map cachedValues=new HashMap();

	private float[] domain;

	/*if we use CMYK*/
	protected int cmykMapping=NOCMYK;

    protected boolean isProcess=false;

	final static protected int NOCMYK=-1;
	final static protected int MYK=1;
	final static protected int CMY=2;
	final static protected int CMK=4;
	final static protected int CY=5;
    final static protected int MY=6;

	public SeparationColorSpace() {}

	public SeparationColorSpace(PdfObjectReader currentPdfFile,PdfObject colorSpace, PdfObject rawSpace, Map colorspacesObjects) {

		value = ColorSpaces.Separation;

        if(colorspacesObjects!=null)
        cachedValues=colorspacesObjects;

		processColorToken(currentPdfFile, colorSpace,rawSpace);
	}

	protected void processColorToken(PdfObjectReader currentPdfFile, PdfObject colorSpace,PdfObject rawSpace) {

		domain = null;

        if(rawSpace.getDictionary(PdfDictionary.Process)!=null)
            isProcess=true;

		//name of color if separation or Components if device and component count
		byte[] name=null;
		byte[][] components=null;
		if(value==ColorSpaces.Separation){
			name=rawSpace.getStringValueAsByte(PdfDictionary.Name);
			componentCount=1;
		}else{
			components=rawSpace.getStringArray(PdfDictionary.Components);	
			componentCount=components.length;
		}

		//test values

		cmykMapping=NOCMYK;


		if(componentCount==3){

			int[] values=new int[3];
			for(int ii=0;ii<3;ii++){
				values[ii]=PdfDictionary.generateChecksum(1, components[ii].length-1, components[ii]);
            }


			if(values[0]==Magenta && values[1]==Yellow && values[2]==Black)
				cmykMapping=MYK;
			else if(values[0]==Cyan && values[1]==Magenta && values[2]==Yellow)
				cmykMapping=CMY;
			else if(values[0]==Cyan && values[1]==Magenta && values[2]==Black)
				cmykMapping=CMK;

		}else if(componentCount==2){

            int[] values=new int[2];
			for(int ii=0;ii<2;ii++){
				values[ii]=PdfDictionary.generateChecksum(1, components[ii].length-1, components[ii]);            
			}

            if(values[0]==Cyan && values[1]==Yellow){
                cmykMapping=CY;
            }else if(values[0]==Magenta && values[1]==Yellow){
                cmykMapping=MY;
            }
        }

		//hard-code myk and cmy
		if(cmykMapping!=NOCMYK){

			altCS=new DeviceCMYKColorSpace();

		}else{

			/**
			 * work out colorspace (can also be direct ie /Pattern)
			 */
			colorSpace=colorSpace.getDictionary(PdfDictionary.AlternateSpace);

			// set alt colorspace
            String ref=colorSpace.getObjectRefAsString();

            //@markee
            if(1==2 && cachedValues.containsKey(ref)){
                altCS= (GenericColorSpace) cachedValues.get(ref);
            }else{
                //System.out.println("Set uncached AlCS "+colorSpace.getObjectRefAsString()+" "+this);
			    altCS =ColorspaceFactory.getColorSpaceInstance(false, currentPdfFile, colorSpace,
                        cachedValues, null, false);
                //cachedValues.put(ref,altCS);
            }
		}

		if(name!=null){
			int len=name.length,jj=0,topHex,bottomHex;
			byte[] tempName=new byte[len];
			for(int i=0;i<len;i++){
				if(name[i]=='#'){
					//roll on past #
					i++;

					topHex=name[i];

					//convert to number
					if(topHex>='A' && topHex<='F')
						topHex = topHex - 55;	
					else if(topHex>='a' && topHex<='f')
						topHex = topHex - 87;
					else if(topHex>='0' && topHex<='9')
						topHex = topHex - 48;

					i++;

					while(name[i]==32 || name[i]==10 || name[i]==13)
						i++;

					bottomHex=name[i];

					if(bottomHex>='A' && bottomHex<='F')
						bottomHex = bottomHex - 55;	
					else if(bottomHex>='a' && bottomHex<='f')
						bottomHex = bottomHex - 87;
					else if(bottomHex>='0' && bottomHex<='9')
						bottomHex = bottomHex - 48;

					tempName[jj]=(byte) (bottomHex+(topHex<<4));
				}else{
					tempName[jj]=name[i];
				}

				jj++;
			}

			//resize
			if(jj!=len){
				name=new byte[jj];
				System.arraycopy(tempName, 0, name, 0, jj);

			}

			pantoneName=new String(name);
		}

		/**
		 * setup transformation
		 **/
		PdfObject functionObj=rawSpace.getDictionary(PdfDictionary.tintTransform);
		colorMapper=new ColorMapping(currentPdfFile,functionObj);
		domain=functionObj.getFloatArray(PdfDictionary.Domain);


	}

	/**private method to do the calculation*/
	private void setColor(float value){

		try{

			//adjust size if needed
			int elements=1;

			if(domain!=null)
				elements=domain.length/2;

			float[] values = new float[elements];
			for(int j=0;j<elements;j++)
				values[j] = value;

			float[] operand =colorMapper.getOperandFloat(values);

            altCS.setColor(operand,operand.length);

		}catch(Exception e){
		}
	}

	/** set color (translate and set in alt colorspace */
	public void setColor(float[] operand,int opCount) {

		setColor(operand[0]);

	}

	/** set color (translate and set in alt colorspace */
	public void setColor(String[] operand,int opCount) {

		float[] f=new float[1];
		f[0]=Float.parseFloat(operand[0]);

		setColor(f,1);

	}
	
	/**
	 * convert data stream to srgb image
	 */
	public BufferedImage JPEGToRGBImage(
			byte[] data,int ww,int hh,float[] decodeArray,int pX,int pY, boolean arrayInverted) {

		BufferedImage image = null;
		ByteArrayInputStream in = null;

		ImageReader iir=null;
		ImageInputStream iin=null;

		try {

			//read the image data
			in = new ByteArrayInputStream(data);
			
//iir = (ImageReader)ImageIO.getImageReadersByFormatName("JPEG").next();
			
			//suggestion from Carol
            Iterator iterator = ImageIO.getImageReadersByFormatName("JPEG");

            while (iterator.hasNext())
            {
                Object o = iterator.next();
                iir = (ImageReader) o;
                if (iir.canReadRaster())
                    break;
            }
			
			ImageIO.setUseCache(false);
			iin = ImageIO.createImageInputStream((in));
			iir.setInput(iin, true);   
			Raster ras=iir.readRaster(0, null);

			ras=cleanupRaster(ras,0,pX,pY,1); //note uses 1 not count

			int w = ras.getWidth(), h = ras.getHeight();

			DataBufferByte rgb = (DataBufferByte) ras.getDataBuffer();
			byte[] rawData=rgb.getData();

			//convert the image
			image=createImage(w, h, rawData, arrayInverted);

		} catch (Exception ee) {
			image = null;
			LogWriter.writeLog("Couldn't read JPEG, not even raster: " + ee);
		}

		try {
			in.close();
			iir.dispose();
			iin.close();
		} catch (Exception ee) {

			LogWriter.writeLog("Problem closing  " + ee);
		}

		return image;

	}	

	/**
	 * convert separation stream to RGB and return as an image
	 */
	public BufferedImage  dataToRGB(byte[] data,int w,int h) {

		BufferedImage image=null;

		try {

			//convert data
			image=createImage(w, h, data, false);

		} catch (Exception ee) {
			image = null;
			LogWriter.writeLog("Couldn't convert Separation colorspace data: " + ee);
		}

		return image;

	}

	/**
	 * turn raw data into an image
	 */
	private BufferedImage createImage(int w, int h, byte[] rgb, boolean arrayInverted) {

		BufferedImage image;

		int pixelCount=3*w*h;
		byte[] imageData=new byte[pixelCount];

		//convert data to RGB format
		int byteCount=rgb.length;
		int pixelReached=0;
		float[][] lookuptable=new float[3][256];
		for(int i=0;i<255;i++)
			lookuptable[0][i]=-1;

		for(int i=0;i<byteCount;i++){

			int value=(rgb[i] & 255);

			if(lookuptable[0][value]==-1){
				if(arrayInverted)
					setColor(1f-(value/255f));
				else
					setColor(value/255f);
				lookuptable[0][value]=((Color)this.getColor()).getRed();
				lookuptable[1][value]=((Color)this.getColor()).getGreen();
				lookuptable[2][value]=((Color)this.getColor()).getBlue();

			}

			for(int comp=0;comp<3;comp++){
				imageData[pixelReached]= (byte) lookuptable[comp][value];
				pixelReached++;
			}

		}

		//create the RGB image
		int[] bands = {0,1,2};
		DataBuffer db = new DataBufferByte(imageData, imageData.length);
		image =new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
		Raster raster =Raster.createInterleavedRaster(db,w,h,w * 3,3,bands,null);
		image.setData(raster);

		return image;
	}

	/**
	 * create rgb index for color conversion
	 */
	public byte[] convertIndexToRGB(byte[] data){

        isConverted=true;
        
		byte[] newdata=new byte[3*256]; //converting to RGB so size known

		try {

			int outputReached=0;
			float[] opValues=new float[1];
			Color currentCol=null;
			float[] operand;
			int byteCount=data.length;
			float[] values = new float[componentCount];

			//scan each byte and convert
			for(int i=0;i<byteCount;i=i+componentCount){

				//turn into rgb and store
				if(this.componentCount==1 && value==ColorSpaces.Separation){ //separation (fix bug with 1 component DeviceN with second check)
					opValues=new float[1];
					opValues[1]= (float)(data[i] & 255);
					setColor(opValues,1);
					currentCol=(Color)this.getColor();
				}else{ //convert deviceN

					for(int j=0;j<componentCount;j++)
						values[j] = (data[i+j] & 255)/255f;

					operand = colorMapper.getOperandFloat(values);

					altCS.setColor(operand,operand.length);
					currentCol=(Color)altCS.getColor();

				}

				newdata[outputReached]=(byte) currentCol.getRed();
				outputReached++;
				newdata[outputReached]=(byte)currentCol.getGreen();
				outputReached++;
				newdata[outputReached]=(byte)currentCol.getBlue();
				outputReached++;

			}

		} catch (Exception ee) {


			LogWriter.writeLog("Exception  " + ee + " converting colorspace");
			
			
		}

		return newdata;		
	}
	
	/**
	 * get color
	 */
	public PdfPaint getColor() {
		
		return altCS.getColor();
		
	}

	/**
	 * get alt colorspace for separation colorspace
	 */
	public GenericColorSpace getAltColorSpace()
	{
		return altCS;
	}

}
