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

  * PdfPageObject.java
  * ---------------
  * (C) Copyright 2008, by IDRsolutions and Contributors.
  *
  *
  * --------------------------
 */
package org.jpedal.objects.raw;

public class PageObject extends PdfObject {
	
    private byte[][] Annots, Contents, Kids;
    
    PdfObject AcroForm, Group, OCProperties, Properties, Metadata, Outlines, Pages, MarkInfo, Names,StructTreeRoot;
    
    private int StructParents=-1, pageMode=-1;

    public PageObject(String ref) {
        super(ref);
    }

    public PageObject(int ref, int gen) {
       super(ref,gen);
    }


    public PageObject(int type) {
    	super(type);
	}

    public int getObjectType() {
		return PdfDictionary.Page;
	}
    
    public boolean getBoolean(int id){

        switch(id){
	
//        case PdfDictionary.EncodedByteAlign:
//        	return EncodedByteAlign; 

            default:
            	return super.getBoolean(id);
        }

    }
    
    public void setBoolean(int id,boolean value){

        switch(id){
        	
//        case PdfDictionary.EncodedByteAlign:
//        	EncodedByteAlign=value;
//        	break;

            default:
                super.setBoolean(id, value);
        }
    }

    public PdfObject getDictionary(int id){

        switch(id){

            case PdfDictionary.AcroForm:
	        	return AcroForm;

            case PdfDictionary.Group:
	        	return Group;

            case PdfDictionary.MarkInfo:
                return MarkInfo;

            case PdfDictionary.Metadata:
	        	return Metadata;

            case PdfDictionary.OCProperties:
                return OCProperties;

            case PdfDictionary.Outlines:
	        	return Outlines;

            case PdfDictionary.Pages:
                return Pages;

            case PdfDictionary.Properties:
                return Properties;

            case PdfDictionary.Names:
	        	return Names;

            case PdfDictionary.StructTreeRoot:
                return StructTreeRoot;

            default:
            	return super.getDictionary(id);
        }
    }

    public void setIntNumber(int id,int value){

    	switch(id){

    	case PdfDictionary.StructParents:
    		StructParents=value;
    		break;
    		
    	default:
    		super.setIntNumber(id, value);
    	}
    }

    public int getInt(int id){

        switch(id){

        case PdfDictionary.StructParents:
        	return StructParents;

            default:
            	return super.getInt(id);
        }
    }


    public void setDictionary(int id,PdfObject value){

    	value.setID(id);
    	
        switch(id){

            case PdfDictionary.AcroForm:
	        	AcroForm=value;
	        break;

            case PdfDictionary.Group:
	        	Group=value;
	        break;

            case PdfDictionary.OCProperties:
                OCProperties=value;
            break;

            case PdfDictionary.MarkInfo:
	        	MarkInfo=value;
	        break;

            case PdfDictionary.Metadata:
	        	Metadata=value;
	        break;

            case PdfDictionary.Outlines:
	        	Outlines=value;
	        break;

            case PdfDictionary.Pages:
                Pages=value;
            break;

            case PdfDictionary.Properties:
                Properties=value;
            break;

            case PdfDictionary.Names:
	        	Names=value;
	        break;

            case PdfDictionary.StructTreeRoot:
	        	StructTreeRoot=value;
	        break;

            default:
            	super.setDictionary(id, value);
        }
    }


    public int setConstant(int pdfKeyType, int keyStart, int keyLength, byte[] raw) {

        int PDFvalue =PdfDictionary.Unknown;

        int id=0,x=0,next;

        //convert token to unique key which we can lookup

        for(int i2=keyLength-1;i2>-1;i2--){

            next=raw[keyStart+i2];

            next=next-48;

            id=id+((next)<<x);

            x=x+8;
        }

        switch(id){

            case PdfDictionary.Page:
                return super.setConstant(pdfKeyType,PdfDictionary.Page);

            case PdfDictionary.Pages:
                return super.setConstant(pdfKeyType,PdfDictionary.Pages);

            case PdfDictionary.PageMode:
                pageMode=id;
                break;

            default:

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


        //System.out.println(pdfKeyType+"="+PDFvalue);
        switch(pdfKeyType){

//        	case PdfDictionary.BaseEncoding:
//        		BaseEncoding=PDFvalue;
//        		break;

        }

        return PDFvalue;
    }

    public int getParameterConstant(int key) {

        switch(key){

                case PdfDictionary.PageMode:
                    return pageMode;


//            case PdfDictionary.BaseEncoding:
//
//            	//special cases first
//            	if(key==PdfDictionary.BaseEncoding && Encoding!=null && Encoding.isZapfDingbats)
//            		return StandardFonts.ZAPF;
//            	else if(key==PdfDictionary.BaseEncoding && Encoding!=null && Encoding.isSymbol)
//            		return StandardFonts.SYMBOL;
//            	else
//            		return BaseEncoding;

            //check general values
            default:
                return super.getParameterConstant(key);
        }

    }

    public void setStream(){

        hasStream=true;
    }


    public PdfArrayIterator getMixedArray(int id) {

    	switch(id){

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

    public byte[][] getKeyArray(int id) {

        switch(id){

            case PdfDictionary.Annots:
       		    return deepCopy(Annots);

            case PdfDictionary.Contents:
       		    return deepCopy(Contents);

            case PdfDictionary.Kids:
       		    return deepCopy(Kids);

            default:
            	return super.getKeyArray(id);
        }
    }

    public void setKeyArray(int id,byte[][] value) {

        switch(id){

            case PdfDictionary.Annots:
                Annots=value;
            break;

            case PdfDictionary.Kids:
                Kids=value;
            break;

            case PdfDictionary.Contents:
                Contents=value;
            break;

            default:
            	super.setKeyArray(id, value);
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

//	        case PdfDictionary.FontBBox:
//	            FontBBox=value;
//	        break;

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

            default:
                super.setTextStreamValue(id,value);

        }

    }

    public String getName(int id) {

        switch(id){

            case PdfDictionary.BaseFont:

            //setup first time
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
//            case PdfStrings.STANDARD:
//
//                //setup first time
//                if(data!=null)
//                    return new String(data);
//                else
//                    return null;


            default:
                throw new RuntimeException("Value not defined in getName(int,mode)");
        }

    }

   

}