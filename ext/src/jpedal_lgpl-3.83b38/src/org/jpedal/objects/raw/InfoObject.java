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

  * PdfInfoObject.java
  * ---------------
  * (C) Copyright 2008, by IDRsolutions and Contributors.
  *
  *
  * --------------------------
 */
package org.jpedal.objects.raw;

public class InfoObject extends PdfObject {

    private String Author, CreationDate, Creator, ModDate, Producer, Keywords, Subject, Title, Trapped;

    private byte[] rawAuthor, rawCreationDate, rawCreator, rawModDate, rawProducer, rawKeywords, rawSubject, rawTitle, rawTrapped;

    //unknown CMAP as String
    //String unknownValue=null;

    //private float[] Matrix;

    //boolean ImageMask=false;

    //int FormType=0, Height=1, Width=1;

    //private PdfObject OPI=null, XObject=null;

    public InfoObject(String ref) {
        super(ref);
    }

    public InfoObject(int ref, int gen) {
        super(ref,gen);
    }


    public InfoObject(int type) {
        super(type);
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

//	        case PdfDictionary.OPI:
//	        	return OPI;
//
//            case PdfDictionary.XObject:
//                return XObject;

            default:
                return super.getDictionary(id);
        }
    }

    public void setIntNumber(int id,int value){

        switch(id){

//	        case PdfDictionary.FormType:
//	        	FormType=value;
//	        break;
//
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

//        	case PdfDictionary.FormType:
//            return FormType;
//
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

//	        case PdfDictionary.OPI:
//	        	OPI=value;
//			break;
//
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

                //System.out.println((char)next);
                next=next-48;

                id=id+((next)<<x);

                x=x+8;
            }

            /**
             * not standard
             */
            switch(id){

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


            default:
                super.setConstant(pdfKeyType,id);

        }

        return PDFvalue;
    }

    public int getParameterConstant(int key) {

        //System.out.println("Get constant for "+key +" "+this);
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
            default:
                return super.getFloatArray(id);

        }
    }

    public void setFloatArray(int id,float[] value) {

        switch(id){

//	        case PdfDictionary.Matrix:
//	            Matrix=value;
//	        break;

            default:
                super.setFloatArray(id, value);
        }
    }

    public void setName(int id,byte[] value) {

        switch(id){

            case PdfDictionary.Trapped:
                rawTrapped=value;
                break;

            default:
                super.setName(id,value);

        }
    }

        public void setTextStreamValue(int id,byte[] value) {

        switch(id){

            case PdfDictionary.Author:
                rawAuthor=value;
                break;

            case PdfDictionary.CreationDate:
                rawCreationDate=value;
                break;

            case PdfDictionary.Creator:
                rawCreator=value;
                break;

            case PdfDictionary.Keywords:
                rawKeywords=value;
                break;

            case PdfDictionary.ModDate:
                rawModDate=value;
                break;

            case PdfDictionary.Producer:
                rawProducer=value;
                break;

            case PdfDictionary.Subject:
                rawSubject=value;
                break;

            case PdfDictionary.Title:
                rawTitle=value;
                break;


            default:
                super.setTextStreamValue(id,value);

        }

    }

    public byte[] getTextStreamValueAsByte(int id) {

        switch(id){

            case PdfDictionary.Author:
                return rawAuthor;

            case PdfDictionary.CreationDate:
                return rawCreationDate;

            case PdfDictionary.Creator:
                return rawCreator;

            case PdfDictionary.Keywords:
                return rawKeywords;

            case PdfDictionary.ModDate:
                return rawModDate;

            case PdfDictionary.Producer:
                return rawProducer;

            case PdfDictionary.Subject:
                return rawSubject;

            case PdfDictionary.Title:
                return rawTitle;


            default:
                super.getTextStreamValueAsByte(id);

        }

        return null;

    }

    public String getName(int id) {

        switch(id){

            case PdfDictionary.Trapped:

            //setup first time
            if(Trapped==null && rawTrapped!=null)
                Trapped=new String(rawTrapped);

            return Trapped;

            default:
                return super.getName(id);

        }
    }

    public String getTextStreamValue(int id) {

        switch(id){

            case PdfDictionary.Author:

                //setup first time
                if(Author==null && rawAuthor!=null)
                    Author=new String(rawAuthor);

                return Author;

            case PdfDictionary.CreationDate:

                //setup first time
                if(CreationDate==null && rawCreationDate!=null)
                    CreationDate=new String(rawCreationDate);

                return CreationDate;

            case PdfDictionary.Creator:

                //setup first time
                if(Creator==null && rawCreator!=null)
                    Creator=new String(rawCreator);

                return Creator;

            case PdfDictionary.Keywords:

                //setup first time
                if(Keywords==null && rawKeywords!=null)
                    Keywords=new String(rawKeywords);

                return Keywords;

            case PdfDictionary.ModDate:

                //setup first time
                if(ModDate==null && rawModDate!=null)
                    ModDate=new String(rawModDate);

                return ModDate;

            case PdfDictionary.Producer:

                //setup first time
                if(Producer==null && rawProducer!=null)
                    Producer=new String(rawProducer);

                return Producer;

            case PdfDictionary.Subject:

                //setup first time
                if(Subject==null && rawSubject!=null)
                    Subject=new String(rawSubject);

                return Subject;


            case PdfDictionary.Title:

                //setup first time
                if(Title==null && rawTitle!=null)
                    Title=new String(rawTitle);

                return Title;


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
        return false;
    }

    public int getObjectType(){
        return PdfDictionary.Metadata;
    }
}