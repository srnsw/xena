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
* DeviceNColorSpace.java
* ---------------
*/
package org.jpedal.color;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBuffer;
import java.io.ByteArrayInputStream;

import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;

import javax.imageio.ImageReader;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

/**
 * handle Device ColorSpace
 */
public class DeviceNColorSpace
	extends SeparationColorSpace {
	
	public DeviceNColorSpace(){	
		
	}

    private Map cache=new HashMap();

	public DeviceNColorSpace(PdfObjectReader currentPdfFile,PdfObject colorSpace,PdfObject rawSpace, Map colorspacesObjects) {

        if(colorspacesObjects!=null)
                cachedValues=colorspacesObjects;
        
		value = ColorSpaces.DeviceN;
		
		processColorToken(currentPdfFile, colorSpace,rawSpace);

	}
	
	/** set color (translate and set in alt colorspace */
	public void setColor(String[] operand,int opCount) {


			float[] values = new float[opCount];
			for(int j=0;j<opCount;j++)
				values[j] = Float.parseFloat(operand[j]);
			
			setColor(values,opCount);
	}


	/** set color (translate and set in alt colorspace */
	public void setColor(float[] raw,int opCount) {


      //if(isProcess){

       // System.out.println(opCount);

//            //@colour
//			float[] newOp={raw[0],raw[0],raw[0],raw[0]};
//            //System.arraycopy(raw, 0, newOp, 0, 3);
//
//			altCS.setColor(newOp,newOp.length);
//        }else


        int[] lookup=new int[3];

        int opNumbers=raw.length;
        if(opNumbers>3)
        opNumbers=3;

        for(int i=0;i<opNumbers;i++){

			lookup[i]=(int)(raw[i]*255);

		}

        boolean isCached=false;

        if(opCount<4 && cache.get(new Integer((lookup[0] << 16) + (lookup[1] << 8) + lookup[2]))!=null){

            isCached=true;

            Object val=cache.get(new Integer((lookup[0] << 16) + (lookup[1] << 8) + lookup[2]));
            int rawValue = ((Integer) val).intValue();
            int r = ((rawValue >> 16) & 255);
            int g = ((rawValue >> 8) & 255);
            int b = ((rawValue) & 255);

            altCS.currentColor=new PdfColor(r,g,b);

        }else if(this.cmykMapping==MYK && opCount==3){ //special case coded in

			//@colour
			float[] newOp={0.0f,raw[0],raw[1],raw[2]};
            //System.arraycopy(raw, 0, newOp, 0, 3);

			altCS.setColor(newOp,newOp.length);

		}else if(this.cmykMapping==CMY && opCount==3){ //special case coded in

			//@colour
			float[] newOp={raw[0],raw[1],raw[2],0.0f};
            //System.arraycopy(raw, 1, newOp, 0, 3);

			altCS.setColor(newOp,newOp.length);

        }else if(this.cmykMapping==CMK && opCount==3){ //special case coded in

            //@colour
            float[] newOp={raw[0],raw[1],0f, raw[2]};
            //System.arraycopy(raw, 1, newOp, 0, 3);

            altCS.setColor(newOp,newOp.length);

        }else if(this.cmykMapping==CY && opCount==2){ //special case coded in

                //@colour
                float[] newOp={raw[0],0,raw[1], 0};
                //System.arraycopy(raw, 1, newOp, 0, 3);

                altCS.setColor(newOp,newOp.length);

        }else if(this.cmykMapping==MY && opCount==2){ //special case coded in

                        //@colour
                        float[] newOp={0,raw[0],raw[1], 0};
                        //System.arraycopy(raw, 1, newOp, 0, 3);

                        altCS.setColor(newOp,newOp.length);

        }else{
			
        	float[] operand =colorMapper.getOperandFloat(raw);

			altCS.setColor(operand,operand.length);


//            if(isProcess){
//                altCS.setColor(operand,operand.length);
//
//                System.out.println("raw="+raw.length+" "+raw[0]);
//                System.out.println(altCS+" "+operand.length+" "+operand[0]+" "+operand[1]+" "+operand[2]+" "+operand[3]);
//            }
		}

        if(!isCached){ //not used except as flag

            altCS.getColor().getRGB();
			int rawValue = altCS.getColor().getRGB();

            //System.out.println(rawValue+" "+size+" "+opCount);
				//store values in cache
				cache.put(new Integer((lookup[0] << 16) + (lookup[1] << 8) + lookup[2]),new Integer(rawValue));

            }

	}
	
	/**
	 * convert separation stream to RGB and return as an image
	  */
	public BufferedImage  dataToRGB(byte[] data,int w,int h) {

		BufferedImage image=null;
		
		try {
			
			//convert data
			image=createImage(w, h, data);
			
		} catch (Exception ee) {
			image = null;
			LogWriter.writeLog("Couldn't convert DeviceN colorspace data: " + ee);
		}
		
		return image;

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

                try{

                    //suggestion from Carol
                    Iterator iterator = ImageIO.getImageReadersByFormatName("JPEG");

                    while (iterator.hasNext()){
                        Object o = iterator.next();
                        iir = (ImageReader) o;
                        if (iir.canReadRaster())
                            break;
                    }

                }catch(Exception e){
                    LogWriter.writeLog("Unable to find JAI jars on classpath");
                    return null;
                }
                
                ImageIO.setUseCache(false);
                iin = ImageIO.createImageInputStream((in));
                iir.setInput(iin, true);
                Raster ras=iir.readRaster(0, null);
                int w = ras.getWidth(), h = ras.getHeight();

                ras=cleanupRaster(ras,0,pX,pY, componentCount);
                w=ras.getWidth();
                h=ras.getHeight();

                DataBufferByte rgb = (DataBufferByte) ras.getDataBuffer();

                //convert the image
                image=createImage(w, h, rgb.getData());

            } catch (Exception ee) {
                image = null;
                LogWriter.writeLog("Couldn't read JPEG, not even raster: " + ee);

                ee.printStackTrace();

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
         * turn raw data into an image
         */
        private BufferedImage createImage(int w, int h, byte[] rawData) {

            BufferedImage image;

            byte[] rgb=new byte[w*h*3];

            int bytesCount=rawData.length;

            //convert data to RGB format
            int byteCount= rawData.length/componentCount;
            
            float[] values=new float[componentCount];

            int j=0,j2=0;

            for(int i=0;i<byteCount;i++){

                if(j>=bytesCount)
                break;

                for(int comp=0;comp<componentCount;comp++){
                    values[comp]=((rawData[j] & 255)/255f);
                    j++;
                }

                setColor(values,componentCount);

                //set values
                int foreground =altCS.currentColor.getRGB();

                //System.out.println(currentColor+"<<<<<<"+altCS+" "+altCS.currentColor);

                rgb[j2]=(byte) ((foreground>>16) & 0xFF);
                rgb[j2+1]=(byte) ((foreground>>8) & 0xFF);
                rgb[j2+2]=(byte) ((foreground) & 0xFF);

                j2=j2+3;

            }

            //create the RGB image
            int[] bands = {0,1,2};
            DataBuffer dataBuf=new DataBufferByte(rgb, rgb.length);
            image =new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
            Raster raster =Raster.createInterleavedRaster(dataBuf,w,h,w*3,3,bands,null);
            image.setData(raster);

            //org.jpedal.gui.ShowGUIMessage.showGUIMessage("x",image,"x");
        
            return image;
        }
}
