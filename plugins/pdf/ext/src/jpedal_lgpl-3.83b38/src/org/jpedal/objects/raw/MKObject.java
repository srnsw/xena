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

  * PdfMaskObject.java
  * ---------------
  * (C) Copyright 2008, by IDRsolutions and Contributors.
  *
  *
  * --------------------------
 */
package org.jpedal.objects.raw;

import org.jpedal.io.PdfReader;

public class MKObject extends FormObject {

	//unknown CMAP as String
	//String unknownValue=null;

	private float[] BC, BG;
	
	protected String AC, CA, RC;
	
	protected byte[] rawAC, rawCA, rawRC;

	private int TP=-1;

	int R=0;
	
	//boolean ImageMask=false;

	//int FormType=0, Height=1, Width=1;

	private PdfObject I=null;

    public MKObject(String ref) {
        super(ref);
    }

    public MKObject(int ref, int gen) {
       super(ref,gen);
    }


    public MKObject(int type) {
    	super(type);
	}

    public MKObject() {
		// TODO Auto-generated constructor stub
	}

	public boolean getBoolean(int id){

        switch(id){

       // case PdfDictionary.ImageMask:
       // 	return ImageMask;


            default:
            	return super.getBoolean(id);
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

	        case PdfDictionary.I:
	        	return I;
//
//            case PdfDictionary.XObject:
//                return XObject;

            default:
                return super.getDictionary(id);
        }
    }

    public void setIntNumber(int id,int value){

        switch(id){

        case PdfDictionary.R:
            R=value;
        break;
        
        case PdfDictionary.TP:
            TP=value;
        break;


            default:
            	super.setIntNumber(id, value);
        }
    }

    public int getInt(int id){

        switch(id){

        case PdfDictionary.R:
        	return R;
            
        case PdfDictionary.TP:
            return TP;


            default:
            	return super.getInt(id);
        }
    }

    public void setDictionary(int id,PdfObject value){

    	value.setID(id);
        switch(id){

	        case PdfDictionary.I:
	        	I=value;
			break;
//
//            case PdfDictionary.XObject:
//            	XObject=value;
//    		break;

            default:
            	super.setDictionary(id, value);
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

    public int[] getIntArray(int id) {

        switch(id){

            default:
            	return super.getIntArray(id);
        }
    }

    public void setIntArray(int id,int[] value) {

        switch(id){

            default:
            	super.setIntArray(id, value);
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
        
        case PdfDictionary.BC:
        	return BC;
        	
        case PdfDictionary.BG:
        	return BG;
        	
        	
            default:
            	return super.getFloatArray(id);

        }
    }

    public void setFloatArray(int id,float[] value) {

        switch(id){

        	case PdfDictionary.BC:
    		BC=value;
    		break;
    		
        	case PdfDictionary.BG:
        		BG=value;
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

    public byte[] getTextStreamValueAsByte(int id) {

        switch(id){

	        case PdfDictionary.AC:
	            return rawAC;
	            
	        case PdfDictionary.CA:
	            return rawCA;
	            
	        case PdfDictionary.RC:
	            return rawRC;    

            default:
                return super.getTextStreamValueAsByte(id);

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
    
    public void setTextStreamValue(int id,byte[] value) {

        switch(id){

	        case PdfDictionary.AC:
	            rawAC=value;
	    	break;
	    	
	        case PdfDictionary.CA:
	        	rawCA=value;
	    	break;
        
        	case PdfDictionary.RC:
	            rawRC=value;
        	break;
        
            default:
                super.setTextStreamValue(id,value);

        }

    }

    public String getTextStreamValue(int id) {

        switch(id){

        case PdfDictionary.AC:

            //setup first time
            if(AC==null && rawAC!=null)
            	AC=PdfReader.getTextString(rawAC, false);

            return AC; 
            
        case PdfDictionary.CA:

            //setup first time
            if(CA==null && rawCA!=null)
            	CA=PdfReader.getTextString(rawCA, false);
            return CA; 
               
            case PdfDictionary.RC:

	            //setup first time
	            if(RC==null && rawRC!=null)
	            	RC=PdfReader.getTextString(rawRC, false);
	
	            return RC;    
	            
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

    public byte[][] getKeyArray(int id) {

        switch(id){

            default:
            	return super.getKeyArray(id);
        }
    }

    public void setKeyArray(int id,byte[][] value) {

        switch(id){

            default:
            	super.setKeyArray(id, value);
        }

    }

    public boolean decompressStreamWhenRead() {
		return true;
	}

    public int getObjectType(){
        return PdfDictionary.MK;
    }
}