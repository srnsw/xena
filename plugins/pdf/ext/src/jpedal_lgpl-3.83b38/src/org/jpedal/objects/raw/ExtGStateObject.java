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

  * PdfExtGSStateObject.java
  * ---------------
  * (C) Copyright 2008, by IDRsolutions and Contributors.
  *
  *
  * --------------------------
 */
package org.jpedal.objects.raw;

public class ExtGStateObject extends PdfObject {

	//unknown CMAP as String
	//String unknownValue=null;

	private float[] Matrix;

    float CA=-1,ca=-1, LW=-1,OPM=-1;

    
    
    boolean AIS=false,op=false, OP=false, SA=false;

    PdfObject TR;
    private byte[][] BM;

    public ExtGStateObject(String ref) {
        super(ref);
    }

    public ExtGStateObject(int ref, int gen) {
       super(ref,gen);
    }


    public ExtGStateObject(int type) {
    	super(type);
	}

    public float getFloatNumber(int id){

        switch(id){

	        case PdfDictionary.CA:
	        	return CA;
	        	
	        case PdfDictionary.ca:
	        	return ca;
        	
	        case PdfDictionary.LW:
	        	return LW;

	        case PdfDictionary.OPM:
	        	return OPM;
	        	
            default:
            	return super.getFloatNumber(id);
        }
    }

    public void setFloatNumber(int id,float value){

        switch(id){

        	case PdfDictionary.CA:
		        	CA=value;
	    	break;
    	
    		case PdfDictionary.ca:
		        	ca=value;
	    	break;
	    	
	        case PdfDictionary.LW:
	        	LW=value;
	        	break;
	        	
	        case PdfDictionary.OPM:
	        	OPM=value;
	        	break;

            default:
            	super.setFloatNumber(id, value);
        }
    }

    public boolean getBoolean(int id){

        switch(id){

        case PdfDictionary.AIS:
        	return AIS;

        case PdfDictionary.op:
        	return op;
        	
        case PdfDictionary.OP:
        	return OP;

            default:
            	return super.getBoolean(id);
        }

    }

    public void setBoolean(int id,boolean value){

        switch(id){

        case PdfDictionary.AIS:
        	AIS=value;
        	break;
        	
        case PdfDictionary.OP:
        	OP=value;
    	break;
    	
    	case PdfDictionary.op:
        	op=value;
    	break;
    	
    	case PdfDictionary.SA:
        	SA=value;
        	break;

            default:
                super.setBoolean(id, value);
        }
    }

    public PdfObject getDictionary(int id){

        switch(id){

            case PdfDictionary.TR:
                return TR;

            default:
                return super.getDictionary(id);
        }
    }

    public void setIntNumber(int id,int value){

        switch(id){

//	        case PdfDictionary.Height:
//	            Height=value;
//	        break;
//
//	        case PdfDictionary.Width:
//	            Width=value;
//	        break;

            default:
            	super.setIntNumber(id, value);
        }
    }

    public int getInt(int id){

        switch(id){

//        	case PdfDictionary.Height:
//            return Height;
//
//	        case PdfDictionary.Width:
//	            return Width;

            default:
            	return super.getInt(id);
        }
    }

    public void setDictionary(int id,PdfObject value){

    	value.setID(id);
    	
        switch(id){

            case PdfDictionary.TR:
            	TR=value;
    		break;

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

            case PdfDictionary.Image:
                PDFvalue =PdfDictionary.Image;
            break;

            case PdfDictionary.Form:
                PDFvalue =PdfDictionary.Form;
            break;

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
        //switch(pdfKeyType){

//        	case PdfDictionary.Subtype:
//        		subtype=PDFvalue;
//        		break;

        //}

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

            case PdfDictionary.BM:
                
                return new PdfArrayIterator(BM);

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


            case PdfDictionary.BM:
	
                BM=value;
                break;

            default:
            	super.setMixedArray(id, value);
        }
    }

    public float[] getFloatArray(int id) {

        switch(id){

        	case PdfDictionary.Matrix:
        		return deepCopy(Matrix);

            default:
            	return super.getFloatArray(id);

        }
    }

    public void setFloatArray(int id,float[] value) {

        switch(id){

	        case PdfDictionary.Matrix:
	            Matrix=value;
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
}