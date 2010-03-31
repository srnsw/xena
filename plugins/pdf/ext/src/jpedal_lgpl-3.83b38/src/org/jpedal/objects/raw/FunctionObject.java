/**
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.jpedal.org
 *
 * (C) Copyright 2008, IDRsolutions and Contributors.
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

  * PdfShadingObject.java
  * ---------------
  * (C) Copyright 2008, by IDRsolutions and Contributors.
  *
  *
  * --------------------------
 */
package org.jpedal.objects.raw;

public class FunctionObject extends PdfObject {

	//unknown CMAP as String
	//String unknownValue=null;

    byte[][] Functions;

    float[] Bounds, C0,C1, Encode;

    int[] Size;

    int BitsPerSample=-1, FunctionType=-1;
    
    float N=-1;
    
    public FunctionObject(String ref) {
        super(ref);
    }

    public FunctionObject(int ref, int gen) {
       super(ref,gen);
    }
    
    public int[] getIntArray(int id) {

    	switch(id){

	    	case PdfDictionary.Size:
	    		return deepCopy(Size);
    		
            default:
            	return super.getIntArray(id);
        }
    }

    public void setIntArray(int id,int[] value) {

    	switch(id){

    	case PdfDictionary.Size:
    		Size=value;
    		break;
    		
            default:
            	super.setIntArray(id, value);
        }
    }


    public FunctionObject(int type) {
    	super(type);
	}

    public boolean getBoolean(int id){

        switch(id){

//        case PdfDictionary.ImageMask:
//        	return ImageMask;
//

            default:
            	return super.getBoolean(id);
        }

    }

    public byte[][] getKeyArray(int id) {

        switch(id){

            case PdfDictionary.Functions:
       		    return deepCopy(Functions);

            default:
            	return super.getKeyArray(id);
        }
    }

    public void setKeyArray(int id,byte[][] value) {

        switch(id){

            case PdfDictionary.Functions:
                Functions=value;
            break;

            default:
            	super.setKeyArray(id, value);
        }

    }

    public void setBoolean(int id,boolean value){

        switch(id){

//        case PdfDictionary.ImageMask:
//        	ImageMask=value;
//        	break;

            default:
                super.setBoolean(id, value);
        }
    }

    public PdfObject getDictionary(int id){

        switch(id){

//            case PdfDictionary.XObject:
//                return XObject;

            default:
                return super.getDictionary(id);
        }
    }

    public void setIntNumber(int id,int value){

        switch(id){
        
        case PdfDictionary.BitsPerSample:
        	BitsPerSample=value;
        	break;

	        case PdfDictionary.FunctionType:
	            FunctionType=value;
	        break;

	        case PdfDictionary.N:
	            N=value;
	        break;

            default:
            	super.setIntNumber(id, value);
        }
    }

    public int getInt(int id){

        switch(id){

        case PdfDictionary.BitsPerSample:
        	return BitsPerSample;
        
	        case PdfDictionary.FunctionType:
	            return FunctionType;
	
            default:
            	return super.getInt(id);
        }
    }
    
    public float getFloatNumber(int id){

        switch(id){

	        case PdfDictionary.N:
	        	return N;
	        
            default:
            	return super.getFloatNumber(id);
        }
    }

    public void setFloatNumber(int id,float value){

        switch(id){

        	case PdfDictionary.N:
		        	N=value;
	    	break;
    	
            default:
            	super.setFloatNumber(id, value);
        }
    }


    public void setDictionary(int id,PdfObject value){

    	value.setID(id);
    	
        switch(id){

//            case PdfDictionary.XObject:
//            	XObject=value;
//    		break;

            default:
            	super.setDictionary(id, value);
        }
    }


    public int setConstant(int pdfKeyType, int keyStart, int keyLength, byte[] raw) {

        int PDFvalue =PdfDictionary.Unknown;

        int id=0,x=0,next;

        try{

            //convert token to unique key which we can lookup

            for(int i2=keyLength-1;i2>-1;i2--){

            	next=raw[keyStart+i2];

            	next=next-48;

                id=id+((next)<<x);

                x=x+8;
            }

            switch(id){


//            case PdfDictionary.Form:
//                PDFvalue =PdfDictionary.Form;
//            break;

//                case StandardFonts.CIDTYPE0:
//                    PDFvalue =StandardFonts.CIDTYPE0;
//                break;


                default:

//                	if(pdfKeyType==PdfDictionary.Encoding){
//                		PDFvalue=PdfCIDEncodings.getConstant(id);
//
//                		if(PDFvalue==PdfDictionary.Unknown){
//
//                			byte[] bytes=new byte[keyLength];
//
//                            System.arraycopy(raw,keyStart,bytes,0,keyLength);
//
//                			unknownValue=new String(bytes);
//                		}
//
//                		if(debug && PDFvalue==PdfDictionary.Unknown){
//                			System.out.println("Value not in PdfCIDEncodings");
//
//                           	 byte[] bytes=new byte[keyLength];
//
//                               System.arraycopy(raw,keyStart,bytes,0,keyLength);
//                               System.out.println("Add to CIDEncodings and as String");
//                               System.out.println("key="+new String(bytes)+" "+id+" not implemented in setConstant in PdfFont Object");
//
//                               System.out.println("final public static int CMAP_"+new String(bytes)+"="+id+";");
//                               
//                		}
//                	}else
                		PDFvalue=super.setConstant(pdfKeyType,id);

                    if(PDFvalue==-1){


                         if(debug){

                        	 byte[] bytes=new byte[keyLength];

                            System.arraycopy(raw,keyStart,bytes,0,keyLength);
                            System.out.println("key="+new String(bytes)+" "+id+" not implemented in setConstant in "+this);

                            System.out.println("final public static int "+new String(bytes)+"="+id+";");
                            
                        }

                    }

                    break;

            }

        }catch(Exception ee){
            ee.printStackTrace();
        }

        //System.out.println(pdfKeyType+"="+PDFvalue);
        switch(pdfKeyType){
//
//        	case PdfDictionary.Subtype:
//        		subtype=PDFvalue;
//        		break;
//
        }

        return PDFvalue;
    }

    public int getParameterConstant(int key) {

        switch(key){


//            case PdfDictionary.BaseEncoding:
//
//            	//special cases first
//            	if(key==PdfDictionary.BaseEncoding && Encoding!=null && Encoding.isZapfDingbats)
//            		return StandardFonts.ZAPF;
//            	else if(key==PdfDictionary.BaseEncoding && Encoding!=null && Encoding.isSymbol)
//            		return StandardFonts.SYMBOL;
//            	else
//            		return BaseEncoding;
        default:
        	return super.getParameterConstant(key);

        }
    }

//    public void setStream(){
//
//        hasStream=true;
//    }


    public PdfArrayIterator getMixedArray(int id) {

    	switch(id){

            //case PdfDictionary.Differences:
            //    return new PdfArrayIterator(Differences);

            default:
			return super.getMixedArray(id);
        }
	}

    public double[] getDoubleArray(int id) {

        switch(id){
            default:
            	return super.getDoubleArray(id);
        }
    }

    public void setDoubleArray(int id,double[] value) {

        switch(id){

//            case PdfDictionary.FontMatrix:
//                FontMatrix=value;
//            break;

            default:
            	super.setDoubleArray(id, value);
        }
    }

    public void setMixedArray(int id,byte[][] value) {

        switch(id){

//            case PdfDictionary.Differences:
//                Differences=value;
//            break;

            default:
            	super.setMixedArray(id, value);
        }
    }

    public float[] getFloatArray(int id) {

        switch(id){

        	case PdfDictionary.Bounds:
        		return deepCopy(Bounds);

            case PdfDictionary.C0:
        		return deepCopy(C0);

            case PdfDictionary.C1:
        		return deepCopy(C1);

            case PdfDictionary.Encode:
        		return deepCopy(Encode);

            default:
            	return super.getFloatArray(id);

        }
    }

    public void setFloatArray(int id,float[] value) {

        switch(id){

	        case PdfDictionary.Bounds:
	            Bounds=value;
	        break;

            case PdfDictionary.C0:
	            C0=value;
	        break;

            case PdfDictionary.C1:
	            C1=value;
	        break;

            case PdfDictionary.Encode:
	            Encode=value;
	        break;

            default:
            	super.setFloatArray(id, value);
        }
    }

    public void setName(int id,byte[] value) {

        switch(id){


//            case PdfDictionary.CMapName:
//                rawCMapName=value;
//            break;

            default:
                super.setName(id,value);

        }

    }

    public void setTextStreamValue(int id,byte[] value) {

        switch(id){

//	        case PdfDictionary.CharSet:
//	            rawCharSet=value;
//	        break;
//

            default:
                super.setTextStreamValue(id,value);

        }

    }

    public String getName(int id) {

        switch(id){

//            case PdfDictionary.BaseFont:
//
//            //setup first time
//            if(BaseFont==null && rawBaseFont!=null)
//                BaseFont=new String(rawBaseFont);
//
//            return BaseFont;

            default:
                return super.getName(id);

        }
    }

    public String getTextStreamValue(int id) {

        switch(id){

//	        case PdfDictionary.CharSet:
//
//	            //setup first time
//	            if(CharSet==null && rawCharSet!=null)
//	            	CharSet=new String(rawCharSet);
//
//	            return CharSet;

            default:
                return super.getTextStreamValue(id);

        }
    }

    /**
     * unless you need special fucntions,
     * use getStringValue(int id) which is faster
     */
    public String getStringValue(int id,int mode) {

        byte[] data=null;

        //get data
        switch(id){

//            case PdfDictionary.BaseFont:
//                data=rawBaseFont;
//                break;

        }


        //convert
        switch(mode){
            case PdfDictionary.STANDARD:

                //setup first time
                if(data!=null)
                    return new String(data);
                else
                    return null;


            case PdfDictionary.LOWERCASE:

                //setup first time
                if(data!=null)
                    return new String(data);
                else
                    return null;

            case PdfDictionary.REMOVEPOSTSCRIPTPREFIX:

                //setup first time
                if(data!=null){
                	int len=data.length;
                	if(len>6 && data[6]=='+'){ //lose ABCDEF+ if present
                		int length=len-7;
                		byte[] newData=new byte[length];
                		System.arraycopy(data, 7, newData, 0, length);
                		return new String(newData);
                	}else
                		return new String(data);
                }else
                    return null;

            default:
                throw new RuntimeException("Value not defined in getName(int,mode) in "+this);
        }
    }
    
    public boolean decompressStreamWhenRead() {
		return true;
	}
    
    public int getObjectType() {
		return PdfDictionary.Function;
	}
}