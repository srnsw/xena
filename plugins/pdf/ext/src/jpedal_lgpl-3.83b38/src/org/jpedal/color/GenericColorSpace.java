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
* GenericColorSpace.java
* ---------------
*/
package org.jpedal.color;

//standard java
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

//<start-jfr>
import org.jpedal.PdfDecoder;
//<end-jfr>
import org.jpedal.exception.PdfException;
import org.jpedal.io.ColorSpaceConvertor;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Strip;


/**
 * Provides Color functionality and conversion for pdf
 * decoding
 */
public class GenericColorSpace  implements Cloneable, Serializable {

    boolean isConverted=false;

	/**any intent*/
	protected String intent=null;
	
	/** actual raw value*/
	protected float[] rawValues;

    protected Map patterns; //holds new PdfObjects

    /**for Patterns*/
	float[][] CTM;
	
	/**handle shading*/
	float[] inputs;

    /**size for indexed colorspaces*/
	protected int size=0;

	/**holds cmyk values if present*/
	protected float c=-1,y=-1,m=-1,k=-1;

	/**matrices for calculating CIE XYZ colour*/
	protected float[] W, G, Ma, B, R;

	/**defines rgb colorspace*/
	static protected ColorSpace rgbCS;

	public static final String cb = "<color ";

	public static final String ce = "</color>";

	//ID of colorspace (ie DeviceRGB)
	protected int value = ColorSpaces.DeviceRGB;

	/**conversion Op for translating rasters or images*/
	protected static ColorConvertOp CSToRGB = null;

	protected ColorSpace cs;

	protected PdfPaint currentColor = new PdfColor(0,0,0);

	/**rgb colormodel*/
	protected static ColorModel rgbModel = null;

    /**currently does nothing but added so we can introduce
     * profile matching
     */
    private static ICC_Profile ICCProfile=null;

	//flag to show problem with colors
	protected boolean failed=false;

	protected int alternative=PdfDictionary.Unknown;

    private PdfObject decodeParms=null;

    private boolean hasYCCKimages=false;

    Object[] cache;

	/**initialise all the colorspaces when first needed */
    protected static void initCMYKColorspace() throws PdfException {

        try {

            if(ICCProfile==null){
            rgbModel =
                new ComponentColorModel(
                        rgbCS,
                    new int[] { 8, 8, 8 },
                    false,
                    false,
                    ColorModel.OPAQUE,
                    DataBuffer.TYPE_BYTE);
            }else{
                int compCount=rgbCS.getNumComponents();
                int[] values=new int[compCount];
                for(int i=0;i<compCount;i++)
                    values[i]=8;

                rgbModel =
                new ComponentColorModel( rgbCS,
                    values,
                    false,
                    false,
                    ColorModel.OPAQUE,
                    DataBuffer.TYPE_BYTE);
            }

            /**create CMYK colorspace using icm profile*/
            ICC_Profile p =ICC_Profile.getInstance(GenericColorSpace.class.getResourceAsStream(
            "/org/jpedal/res/cmm/cmyk.icm"));
            ICC_ColorSpace cmykCS = new ICC_ColorSpace(p);

            /**define the conversion. PdfColor.hints can be replaced with null or some hints*/
            CSToRGB = new ColorConvertOp(cmykCS, rgbCS, ColorSpaces.hints);

        } catch (Exception e) {
            LogWriter.writeLog(
                "Exception " + e.getMessage() + " initialising color components");
            throw new PdfException("[PDF] Unable to create CMYK colorspace. Check cmyk.icm in jar file");

        }
    }
    
    /**
	 * reset any defaults if reused
	 */
	public void reset(){
		
		currentColor = new PdfColor(0,0,0);

	}

	//show if problem and we should default to Alt�
	public boolean isInvalid(){
		return failed;
	}



    //allow user to replace sRGB colorspace
    static{

        if(ICCProfile!=null){
            System.out.println("setup "+ICCProfile);
            rgbCS=new ICC_ColorSpace(ICCProfile);

        }else
            rgbCS=ColorSpace.getInstance(ColorSpace.CS_sRGB);

    }

    /**
	 * get size
	 */
	public int getIndexSize(){
		return size;
	}

	/**
	 * get color
	 */
	public PdfPaint getColor()
	{
		return currentColor;
	}

	/**return the set Java colorspace*/
	public ColorSpace getColorSpace() {
		return cs;
	}

	public  GenericColorSpace() {

		cs=rgbCS;
	}

	protected void setAlternateColorSpace(int alt){
		alternative = alt;
	}
	
	public int getAlternateColorSpace(){
		return alternative;
	}

	/**
	 * clone graphicsState
	 */
	final public Object clone()
	{
		Object o = null;
		try{
			o = super.clone();
		}catch( Exception e ){
			throw new RuntimeException("Uanble to clone object");
		}

		return o;
	}

	/**any indexed colormap*/
	protected byte[] IndexedColorMap = null;

	/**pantone name if present*/
    public String pantoneName=null;

    /**number of colors*/
	protected int componentCount=3;

	/**handle to graphics state / only set and used by Pattern*/
	protected GraphicsState gs;

	protected int pageHeight;

	/**
	 * <p>Convert DCT encoded image bytestream to sRGB</p>
	 * <p>It uses the internal Java classes
	 * and the Adobe icm to convert CMYK and YCbCr-Alpha - the data is still DCT encoded.</p>
	 * <p>The Sun class JPEGDecodeParam.java is worth examining because it contains lots
	 * of interesting comments</p>
	 * <p>I tried just using the new IOImage.read() but on type 3 images, all my clipping code
	 * stopped working so I am still using 1.3</p>
	 */
	final protected BufferedImage nonRGBJPEGToRGBImage(
            byte[] data, int w, int h, float[] decodeArray,int pX,int pY) {

        boolean isProcessed=false;

        BufferedImage image = null;
		ByteArrayInputStream in = null;


        ImageReader iir=null;
		ImageInputStream iin=null;
		
		try {

            if(CSToRGB==null)
            initCMYKColorspace();

            CSToRGB = new ColorConvertOp(cs, rgbCS, ColorSpaces.hints);

            in = new ByteArrayInputStream(data);

            int cmykType=getJPEGTransform(data);

            //suggestion from Carol
            try{
                Iterator iterator = ImageIO.getImageReadersByFormatName("JPEG");

                while (iterator.hasNext())
                {
                    Object o = iterator.next();
                    iir = (ImageReader) o;
                    if (iir.canReadRaster())
                        break;
                }
                
            }catch(Exception e){
                LogWriter.writeLog("Unable to find JAI jars on classpath");
                return null;
            }

            //iir = (ImageReader)ImageIO.getImageReadersByFormatName("JPEG").next();
            ImageIO.setUseCache(false);

            iin = ImageIO.createImageInputStream((in));
            iir.setInput(iin, true);   //new MemoryCacheImageInputStream(in));

            Raster ras=iir.readRaster(0,null);

            //invert
            if(decodeArray!=null){

            	//decodeArray=Strip.removeArrayDeleminators(decodeArray).trim();

            	if((decodeArray.length==6 && decodeArray[0]==1f && decodeArray[1]==0f &&
            			decodeArray[2]==1f && decodeArray[3]==0f &&
            			decodeArray[4]==1f && decodeArray[5]==0f )||
            			(decodeArray.length>2 && 
            					decodeArray[0]==1f && decodeArray[1]==0)){

                        DataBuffer buf=ras.getDataBuffer();

                        int count=buf.getSize();

                        for(int ii=0;ii<count;ii++)
                           buf.setElem(ii,255-buf.getElem(ii));
            	}else if(decodeArray.length==6 && 
            			decodeArray[0]==0f && decodeArray[1]==1f &&
            			decodeArray[2]==0f && decodeArray[3]==1f &&
            			decodeArray[4]==0f && decodeArray[5]==1f){ 
               // }else if(decodeArray.indexOf("0 1 0 1 0 1 0 1")!=-1){//identity
               // }else if(decodeArray.indexOf("0.0 1.0 0.0 1.0 0.0 1.0 0.0 1.0")!=-1){//identity
                }else if(decodeArray!=null && decodeArray.length>0){
                }
            }

            if(cs.getNumComponents()==4){ //if 4 col CMYK of ICC translate

                isProcessed=true;

                try{

                    if(cmykType==2){

                        hasYCCKimages=true;
                        String iccFlag=System.getProperty("org.jpedal.useICC");
                        if(iccFlag!=null){
                            image = ColorSpaceConvertor.iccConvertCMYKImageToRGB(((DataBufferByte)ras.getDataBuffer()).getData(),w,h,iccFlag);
                        }else{

                        	ras=cleanupRaster(ras,0,pX,pY,4);
                        	w=ras.getWidth();
                        	h=ras.getHeight();

                            image = ColorSpaceConvertor.algorithmicConvertCMYKImageToRGB(ras.getDataBuffer(),w,h,false);
                        }
                    }else{

                    	ras=cleanupRaster(ras,0,pX,pY,4);
                    	w=ras.getWidth();
                    	h=ras.getHeight();

                        /**generate the rgb image*/
                        WritableRaster rgbRaster =rgbModel.createCompatibleWritableRaster(w, h);

                        // if(cmykType!=0)
                        CSToRGB.filter(ras, rgbRaster);
                        image =new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
                        image.setData(rgbRaster);

                        //slower in tests
                        //image=new BufferedImage(rgbModel,rgbRaster,false,null);

                    }
                }catch(Exception e){
                    e.printStackTrace();
                    LogWriter.writeLog("Problem with JPEG conversion");
                }
            }else if(cmykType!=0){

            	image=iir.read(0);

            	image=cleanupImage(image,pX,pY);

                isProcessed=true;

            }

            if(!isProcessed){
            /**1.3 version or vanilla version*/
                WritableRaster rgbRaster;

                in = new ByteArrayInputStream(data);

                //access the file
                com.sun.image.codec.jpeg.JPEGImageDecoder decoder = com.sun.image.codec.jpeg.JPEGCodec.createJPEGDecoder(in);
                Raster currentRaster = decoder.decodeAsRaster();
                //we have to call regardless to get params

                int colorType = decoder.getJPEGDecodeParam().getEncodedColorID();

                if (colorType == 4) { //CMYK

                	currentRaster=cleanupRaster(currentRaster,0,pX,pY,4);

                	int width = currentRaster.getWidth();
                	int height = currentRaster.getHeight();


                    rgbRaster =rgbModel.createCompatibleWritableRaster(width, height);
                    CSToRGB.filter(currentRaster, rgbRaster);
                    image =new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
                    image.setData(rgbRaster);

                } else { //type 7 - these seem to crash the new 1.4 IO routines as far as I can see

                    LogWriter.writeLog("COLOR_ID_YCbCrA image");

                    //I reread the image which is inefficient but I can't currently see any alternative...
                    in = new ByteArrayInputStream(data);
                    decoder = com.sun.image.codec.jpeg.JPEGCodec.createJPEGDecoder(in);
                    image = decoder.decodeAsBufferedImage();

                    image=cleanupImage(image,pX,pY);

                    image = ColorSpaceConvertor.convertToRGB(image);

                    // Convert from CMYK now - loses black
                    //image = CSToRGB.filter(image, null);

                }

                /**/
            }

        } catch (Exception ee) {
			image = null;
            ee.printStackTrace();
            LogWriter.writeLog("Couldn't read JPEG, not even raster: " + ee);
        }catch(Error err ){
        	//System.out.println("Error="+err);
			if(iir!=null)
				iir.dispose();
			if(iin!=null){
				try {
					iin.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
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


	private static BufferedImage cleanupImage(BufferedImage image,int pX, int pY){
		/**
		image= ColorSpaceConvertor.convertToRGB(image);

        Raster ras=cleanupRaster(image.getData(),1, pX, pY, image.getColorModel().getNumColorComponents());
		image =new BufferedImage(ras.getWidth(),ras.getHeight(), BufferedImage.TYPE_INT_RGB);
        image.setData(ras);
        /**/
        return image;
	}

	protected static Raster cleanupRaster(Raster ras,int type,int pX,int pY,int comp) {

		byte[] buffer=null;
		int[] intBuffer=null;
		if(type==1)
			intBuffer=((DataBufferInt)ras.getDataBuffer()).getData();
		else{
			int layerCount=ras.getNumBands();
			if(layerCount==comp){
				buffer=((DataBufferByte)ras.getDataBuffer()).getData();
			}else if(layerCount==1){
				byte[] rawBuffer=((DataBufferByte)ras.getDataBuffer()).getData();
				int size=rawBuffer.length;
				int realSize=size*comp;
				int j=0,i=0;
				buffer=new byte[realSize];
				while(true){
					for(int a=0;a<comp;a++){
						buffer[j]=rawBuffer[i];
						j++;
					}
					i++;

					if(i>=size)
						break;
				}
			}else{
			}
		}
		int w=ras.getWidth();
		int h=ras.getHeight();


		int sampling=1; //keep as multiple of 2
		int newW=w,newH=h;

		if(pX>0 && pY>0){

			int smallestH=pY<<2; //double so comparison works
			int smallestW=pX<<2;

			//cannot be smaller than page
			while(newW>smallestW && newH>smallestH){
				sampling=sampling<<1;
				newW=newW>>1;
				newH=newH>>1;
			}

			int scaleX=w/pX;
			if(scaleX<1)
				scaleX=1;

			int scaleY=h/pY;
			if(scaleY<1)
				scaleY=1;

			//choose smaller value so at least size of page
			sampling=scaleX;
			if(sampling>scaleY)
				sampling=scaleY;

			//switch to 8 bit and reduce bw image size by averaging
			if(sampling>1){

				newW=w/sampling;
				newH=h/sampling;

				int x=0,y=0,xx=0,yy=0,jj=0,origLineLength=w;
				try{


				byte[] newData=new byte[newW*newH*comp];

				if(type==0)
					origLineLength= w*comp;

				for(y=0;y<newH;y++){
					for(x=0;x<newW;x++){

						//allow for edges in number of pixels left
						int wCount=sampling,hCount=sampling;
						int wGapLeft=w-x;
						int hGapLeft=h-y;
						if(wCount>wGapLeft)
							wCount=wGapLeft;
						if(hCount>hGapLeft)
							hCount=hGapLeft;

						for(jj=0;jj<comp;jj++){
							int byteTotal=0,count=0;
							//count pixels in sample we will make into a pixel (ie 2x2 is 4 pixels , 4x4 is 16 pixels)
							for(yy=0;yy<hCount;yy++){
								for(xx=0;xx<wCount;xx++){
									if(type==0)
										byteTotal=byteTotal+(buffer[((yy+(y*sampling))*origLineLength)+(((x*sampling*comp)+(xx*comp)+jj))] & 255);
									else
										byteTotal=byteTotal+((intBuffer[((yy+(y*sampling))*origLineLength)+(x*sampling)+xx]>>(8*(2-jj))) & 255);

									count++;
								}
							}

							//set value as white or average of pixels
							if(count>0)
								newData[jj+(x*comp)+(newW*y*comp)]=(byte)((byteTotal)/count);
						}
					}
				}

				int[] bands=new int[comp];
				for(int jj2=0;jj2<comp;jj2++)
					bands[jj2]=jj2;

					ras=Raster.createInterleavedRaster(new DataBufferByte(newData,newData.length), newW, newH, newW*comp, comp, bands, null);
				}catch(Exception e){
                    
					e.printStackTrace();
					LogWriter.writeLog("Problem with Image");
				}
			}
		}

		return ras;
	}

	/**Toms routine to read the image type - you can also use
	 * int colorType = decoder.getJPEGDecodeParam().getEncodedColorID();
	 */
	static final protected  int getJPEGTransform(byte[] data) {
		int xform = 0;

		for (int i=0,imax=data.length-2; i<imax; ) {

			int type = data[i+1] & 0xff;	// want unsigned bytes!
			//out_.println("+"+i+": "+Integer.toHexString(type)/*+", len="+len*/);
			i += 2;	// 0xff and type

			if (type==0x01 || (0xd0 <= type&&type <= 0xda)) {

			} else if (type==0xda) {
				i = i + ((data[i]&0xff)<<8) + (data[i+1]&0xff);
				while (true) {
					for ( ; i<imax; i++) if ((data[i]&0xff)==0xff && data[i+1]!=0) break;
					int rst = data[i+1]&0xff;
					if (0xd0 <= rst&&rst <= 0xd7) i+=2; else break;
				}

			} else {
				/*if (0xc0 <= type&&type <= 0xcf) {	// SOF
				 Nf = data[i+7] & 0xff;	// 1, 3=YCbCr, 4=YCCK or CMYK
				 } else*/ if (type == 0xee) {	// Adobe
				 	if (data[i+2]=='A' && data[i+3]=='d' && data[i+4]=='o' && data[i+5]=='b' && data[i+6]=='e') { xform = data[i+13]&0xff; break; }
				 }
				 i = i + ((data[i]&0xff)<<8) + (data[i+1]&0xff);
			}
		}

		return xform;
	}


	public void setIndex(byte[] IndexedColorMap,int size) {

		//		set the data for an object
		this.IndexedColorMap = IndexedColorMap;
		this.size=size;

	//	System.out.println("Set index ="+IndexedColorMap);
	}

	public void setIndex(String CMap,String name,int count) {

		StringBuffer rawValues = new StringBuffer();
		this.size=count;

        //see if hex or octal values and make a lisr
		if (CMap.startsWith("(\\")) {

			//get out the octal values to hex
			StringTokenizer octal_values =new StringTokenizer(CMap, "(\\)");

			while (octal_values.hasMoreTokens()) {
				int next_value = Integer.parseInt(octal_values.nextToken(), 8);
				String hex_value = Integer.toHexString(next_value);
				//pad with 0 if required
				if (hex_value.length() < 2)
					rawValues.append('0');

				rawValues.append(hex_value);
			}
		} else if (CMap.startsWith("(")) {

			//should never happen as remapped in ObjectReader

		} else {

			//get rest of hex data minus any <>
			if (CMap.startsWith("<"))
				CMap =CMap.substring(1, CMap.length() - 1).trim();
			rawValues = new StringBuffer(CMap);

		}

		//workout components size
		int total_components = 1;
		if ((name.indexOf("RGB") != -1)|(name.indexOf("ICC") != -1))
			total_components = 3;
		else if (name.indexOf("CMYK") != -1)
			total_components = 4;

		IndexedColorMap = new byte[(count + 1) * total_components];

		//make sure no spaces in array
		rawValues=Strip.stripAllSpaces(rawValues);

        //put into lookup array
		for (int entries = 0; entries < count + 1; entries++) {
			for (int comp = 0; comp < total_components; comp++) {
				int p = (entries * total_components * 2) + (comp * 2);

				int col_value =Integer.parseInt(rawValues.substring(p, p + 2),16);
				IndexedColorMap[(entries * total_components) + comp] =(byte) col_value;

			}
		}
    }

	/**
	 * lookup a component for index colorspace
	 */
	protected int getIndexedColorComponent(int count) {
		int value =  255;

		if(IndexedColorMap!=null){
			value=IndexedColorMap[count];

			if (value < 0)
				value = 256 + value;

		}
		return value;

	}

	/**return indexed COlorMap
		 */
	public byte[] getIndexedMap() {
		
		//return IndexedColorMap;
		/**/
		if(IndexedColorMap==null)
			return null;
		
		int size=IndexedColorMap.length;
		byte[] copy=new byte[size];
		System.arraycopy(IndexedColorMap, 0, copy, 0, size);
		
		return copy;
		/**/
	}

	/**
	 * convert color value to sRGB color
	 */
	public void setColor(String[] value,int operandCount){

	}


	/**
	 * convert color value to sRGB color
	 */
	public void setColor(float[] value,int operandCount){


	}


    //<start-jfr>
    /**
	 * convert byte[] datastream JPEG to an image in RGB
	 */
	public BufferedImage JPEGToRGBImage(byte[] data,int w,int h,float[] decodeArray,int pX,int pY, boolean arrayInverted) {


		BufferedImage image = null;
		ByteArrayInputStream bis=null;

		try {


			/**1.4 code*/
			//
			bis=new ByteArrayInputStream(data);
			if(PdfDecoder.use13jPEGConversion){
				com.sun.image.codec.jpeg.JPEGImageDecoder decoder =com.sun.image.codec.jpeg.JPEGCodec.createJPEGDecoder(bis);
				image = decoder.decodeAsBufferedImage();
				decoder =null;
			}else{
				ImageIO.setUseCache(false);
				image =ImageIO.read(bis);
			}

			if(image!=null){

				if(value!=ColorSpaces.DeviceGray) //crashes Linux
				image=cleanupImage(image,pX,pY);

				image=ColorSpaceConvertor.convertToRGB(image);

			}

        } catch (Exception ee) {
			image = null;
			LogWriter.writeLog("Problem reading JPEG: " + ee);
			ee.printStackTrace();
		}

		if(bis!=null){
			try {
				bis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return image;
	}
    //<end-jfr>

    /**
	 * convert byte[] datastream JPEG to an image in RGB
	 * @throws PdfException
	 */
	public BufferedImage  JPEG2000ToRGBImage(byte[] data,int w,int h,float[] decodeArray,int pX,int pY) throws PdfException{

        BufferedImage image = null;

		ByteArrayInputStream in = null;

		try {
			in = new ByteArrayInputStream(data);

			/**1.4 code*/
			//standard java 1.4 IO

			ImageReader iir = (ImageReader)ImageIO.getImageReadersByFormatName("JPEG2000").next();
        //	ImageIO.setUseCache(false);
			ImageInputStream iin = ImageIO.createImageInputStream(in);
			try{
				iir.setInput(iin, true);   //new MemoryCacheImageInputStream(in));
				image = iir.read(0);
				iir.dispose();
				iin.close();
				in.close();
			}catch(Exception e){

                LogWriter.writeLog("Problem reading JPEG 2000: " + e);

                e.printStackTrace();
                return null;
			}

            image=cleanupImage(image,pX,pY);

            //ensure white background
            if(image.getType()== BufferedImage.TYPE_BYTE_INDEXED){
                BufferedImage oldImage=image;
                int newW=image.getWidth();
                int newH=image.getHeight();
                image=new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2= (Graphics2D) image.getGraphics();
                g2.setPaint(Color.WHITE);
                g2.fillRect(0,0,newW,newH);
                g2.drawImage(oldImage,0,0,null);

            }

            image=ColorSpaceConvertor.convertToRGB(image);
            
        } catch (Exception ee) {
			image = null;
            LogWriter.writeLog("Problem reading JPEG 2000: " + ee);

			throw new PdfException("JPeg 2000 Images need both JAI (imageio.jar) on classpath, " +
				"and the VM parameter -Dorg.jpedal.jai=true switch turned on");
			//ee.printStackTrace();
		} catch (Error ee2) {
			image = null;
            ee2.printStackTrace();
            LogWriter.writeLog("Problem reading JPEG 2000: " + ee2);

			throw new PdfException("JPeg 2000 Images need both JAI (imageio.jar) on classpath, " +
				"and the VM parameter -Dorg.jpedal.jai=true switch turned on");
			//ee.printStackTrace();
		}

		return image;
	}

	/**
	 * convert color content of data to sRGB data
	  */
	public BufferedImage dataToRGB(byte[] data,int w,int h){

			int[] bands = {0,1,2};
			DataBuffer db = new DataBufferByte(data, data.length);
			BufferedImage image =new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
			Raster raster =Raster.createInterleavedRaster(db,w,h,w * 3,3,bands,null);
			image.setData(raster);

			return image;
	}

	/**
	 * convert image to sRGB image
	  */
	public static BufferedImage BufferedImageToRGBImage(BufferedImage image){

			return image;
	}

	/**get colorspace ID*/
	public int getID(){
		return value;
	}

	/**
	 * create a CIE values for conversion to RGB colorspace
	 */
	final public void setCIEValues(float[] W,float[] B,float[] R,float[] Ma, float[] G){

		/**set to CIEXYZ colorspace*/
		cs = ColorSpace.getInstance(ColorSpace.CS_CIEXYZ);

		//set values
		this.G = G;
		this.Ma = Ma;
		this.W = W;
		this.B = B;
		this.R = R;

	}

	/**
	 * convert 4 component index to 3
	  */
	final protected byte[] convert4Index(byte[] data){
		return convertIndex(data,4);
	}

	/**
	 * convert 4 component index to 3
	  */
	protected byte[] convertIndex(byte[] data,int compCount){
		try {

			/**turn it into a BufferedImage so we can convert then extract the data*/
			int width = data.length / compCount;
			int height = 1;
			DataBuffer db = new DataBufferByte(data, data.length);
			int[] bands;
			WritableRaster raster;
            DataBuffer convertedData =null;

			int[] bands4 = { 0, 1, 2, 3 };
			int[] bands3 = { 0, 1, 2};
			if(compCount==4)
				bands=bands4;
			else
				bands=bands3;

            //possible ICC code unused below
            //System.out.println(compCount+" data="+data.length+" w="+width+" h="+height);
            String iccFlag=System.getProperty("org.jpedal.useICC");
            if(iccFlag!=null && 1==2){
                BufferedImage image = ColorSpaceConvertor.iccConvertCMYKImageToRGB(data,width,height,iccFlag);
                convertedData = image.getRaster().getDataBuffer();
                
                System.out.println(image);
                System.out.println(convertedData);
                //ShowGUIMessage.showGUIMessage("x",image,"x");

                /**put into byte array*/
                int size = width * height * 3,jj=0;
                data = new byte[size];


                int i,r,g,b,j=0;
                for (int ii = 0; ii < size; ii++){
                    i= convertedData.getElem(ii);
                    //System.out.println(i+" "+(i>>8)+" "+Integer.toHexString(i));
                    r=(i>>16)&255;
                    g=(i>>8)&255;
                    b=i & 255;

                    data[j]=(byte)r;
                    j++;
                    data[j]=(byte)g;
                    j++;
                    data[j]=(byte)b;
                    j++;
                    //data[ii] = (byte) convertedData.getElem(ii);
                    System.out.println(ii+"="+i+" "+r+" "+g+" "+b);
                }

            }else if(1==2){
                BufferedImage image =  ColorSpaceConvertor.algorithmicConvertCMYKImageToRGB(db,width,height,false);
                System.out.println(image);
                convertedData = image.getRaster().getDataBuffer();

            }else{
                raster =Raster.createInterleavedRaster(db,width,height,width * compCount,compCount,bands,null);

                if(CSToRGB==null)
                    initCMYKColorspace();
                CSToRGB = new ColorConvertOp(cs, rgbCS, ColorSpaces.hints);

                WritableRaster rgbRaster =
                    rgbModel.createCompatibleWritableRaster(width, height);

                CSToRGB.filter(raster, rgbRaster);

                convertedData = rgbRaster.getDataBuffer();

                /**put into byte array*/
                int size = width * height * 3;
                data = new byte[size];


                for (int ii = 0; ii < size; ii++){
                    data[ii] = (byte) convertedData.getElem(ii);
                    //System.out.println(ii+"="+data[ii]);
                }
            }
		} catch (Exception ee) {
			LogWriter.writeLog("Exception  " + ee + " converting colorspace");
		}

		return data;
	}

	/**
	 * convert Index to RGB
	  */
	public byte[] convertIndexToRGB(byte[] index){

		return index;
	}

	/**
	 * get an xml string with the color info
	 */
	public String getXMLColorToken(){

		String colorToken="";

		//only cal if not set
		if(c==-1){ //approximate
			if(currentColor instanceof Color){
				Color col=(Color)currentColor;
				float c=(255-col.getRed())/255f;
				float m=(255-col.getGreen())/255f;
				float y=(255-col.getBlue())/255f;
				float k=c;
				if(k<m)
					k=m;
				if(k<y)
					k=y;

				if(pantoneName==null)
				    colorToken=GenericColorSpace.cb+"C='"+c+"' M='"+m+"' Y='"+y+"' K='"+k+"' >";
				else
				    colorToken=GenericColorSpace.cb+"C='"+c+"' M='"+m+"' Y='"+y+"' K='"+k+"' pantoneName='"+pantoneName+"' >";
			}else{
				colorToken=GenericColorSpace.cb+"type='shading'>";
			}
		}else{
		    if(pantoneName==null)
			    colorToken=GenericColorSpace.cb+"C='"+c+"' M='"+m+"' Y='"+y+"' K='"+k+"' >";
			else
			    colorToken=GenericColorSpace.cb+"C='"+c+"' M='"+m+"' Y='"+y+"' K='"+k+"' pantoneName='"+pantoneName+"' >";
		}

		return colorToken;
	}

	/**
	 * pass in list of patterns
	 */
	public void setPattern(Map patterns,int pageHeight,float[][] CTM) {

        this.patterns=patterns;
        
        this.pageHeight=pageHeight;
        this.CTM=CTM;
        //System.out.println("set pattern called");
	}

	/** used by generic decoder to asign color*/
	public void setColor(PdfPaint col) {
		this.currentColor=col;
	}
	
	/** used by generic decoder to assign color if invisible*/
	public void setColorIsTransparent() {
		this.currentColor=new PdfColor(255,0,0,0);
	}

	/**return number of values used for color (ie 3 for rgb)*/
	public int getColorComponentCount() {
		
		return componentCount;
	}

	/**pattern colorspace needs access to graphicsState*/
	public void setGS(GraphicsState currentGraphicsState) {
		
		this.gs=currentGraphicsState;
		
	}

	public void setIntent(String intent) {
		this.intent = intent;
	}

	/**return raw values - only currently works for CMYK*/
	public float[] getRawValues() {
		return rawValues;
	}

    /**
     * flag to show if YCCK image decoded so we can draw attention to user
     * @return
     */
    public boolean isImageYCCK() {
        return hasYCCKimages;
    }

    public void setDecodeParms(PdfObject parms) {
        this.decodeParms=parms;
    }

    public boolean isIndexConverted() {
        return isConverted; 
    }

    final int multiplier=100000;

    public Color getCachedShadingColor(float val) {

        if(cache==null)
            return null;
        else
            return (Color) cache[(int) (val * multiplier)];
    }

    public void setShadedColor(float val, Color col) {

        if(cache==null)
        cache=new Object[multiplier+1];

        cache[((int) (val * multiplier))]=col;
    }
}
