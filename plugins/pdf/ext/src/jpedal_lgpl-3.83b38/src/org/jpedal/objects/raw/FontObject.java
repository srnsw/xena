/**
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.jpedal.org
 *
 * (C) Copyright 2007, IDRsolutions and Contributors.
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

  * PdfFontObject.java
  * ---------------
  * (C) Copyright 2007, by IDRsolutions and Contributors.
  *
  *
  * --------------------------
 */
package org.jpedal.objects.raw;

import org.jpedal.fonts.StandardFonts;

public class FontObject extends PdfObject {

	//unknown CMAP as String 
	String unknownValue=null;
	
    //mapped onto Type1
    final private static int MMType1=1230852645;

    //mapped onto Type1
    final private static int Type1C=1077224796;
    
    final private static int ZaDb=707859506;
    
    final private static int ZapfDingbats=1889256112;
    
    final private static int Symbol=1026712197;

    private PdfObject CharProcs=null, CIDSystemInfo=null, CIDToGIDMap=null, 
    DescendantFonts=null,
    FontDescriptor=null,FontFile,
    FontFile2, FontFile3, ToUnicode;
    
    int BaseEncoding=PdfDictionary.Unknown;
    
    int FirstChar=1,LastChar=255,Flags=0,MissingWidth=0,DW=-1,StemV=0, Supplement=0;

    float[] Widths=null,FontBBox=null;

    double[] FontMatrix=null;    
    
    byte[][] Differences=null;

    private byte[] rawBaseFont=null,rawCharSet=null, rawCMapName=null,
    rawFontName=null,rawFontStretch=null,
    rawOrdering=null, rawRegistry=null, rawW=null;

    private String BaseFont=null,CharSet=null, CMapName=null,
    FontName=null,FontStretch=null, Ordering=null,Registry=null, W=null;

    public FontObject(String ref) {
        super(ref);
    }

    public FontObject(int ref, int gen) {
       super(ref,gen);
    }


    public FontObject(int type) {  	
    	super(type);
	}

    public PdfObject getDictionary(int id){

        switch(id){

            case PdfDictionary.CharProcs:
                return CharProcs;

            case PdfDictionary.CIDSystemInfo:
	        	return CIDSystemInfo;
	        
	        case PdfDictionary.CIDToGIDMap:
	        	return CIDToGIDMap;
	        	
            case PdfDictionary.DescendantFonts:
                return DescendantFonts;

            case PdfDictionary.Encoding:
                return Encoding;

            case PdfDictionary.FontDescriptor:
                return FontDescriptor;

            case PdfDictionary.FontFile:
                return FontFile;

            case PdfDictionary.FontFile2:
                return FontFile2;

            case PdfDictionary.FontFile3:
                return FontFile3;
                    
            case PdfDictionary.ToUnicode:
                return ToUnicode;

            default:
                return super.getDictionary(id);
        }
    }

    public void setIntNumber(int id,int value){

        switch(id){

	        case PdfDictionary.DW:
	            DW=value;
	        break;
        
            case PdfDictionary.FirstChar:
                FirstChar=value;
            break;
            
            case PdfDictionary.Flags:
                Flags=value;
            break;

            case PdfDictionary.LastChar:
                LastChar=value;
            break;

            case PdfDictionary.MissingWidth:
                MissingWidth=value;
            break;

            case PdfDictionary.StemV:
                StemV=value;
            break;

            case PdfDictionary.Supplement:
            	Supplement=value;
            break;
            
            default:
            	super.setIntNumber(id, value);
        }
    }

    public int getInt(int id){

        switch(id){

	        case PdfDictionary.DW:
	            return DW;
	            
            case PdfDictionary.FirstChar:
                return FirstChar;
 	
            case PdfDictionary.Flags:
                return Flags;

            case PdfDictionary.LastChar:
                return LastChar;

            case PdfDictionary.MissingWidth:
                return MissingWidth;

            case PdfDictionary.StemV:
                return StemV;
                
            case PdfDictionary.Supplement:
                return Supplement;
                
            default:
            	return super.getInt(id);
        }
    }

    public void setDictionary(int id,PdfObject value){

    	value.setID(id);
    	
        //flag embedded data
        if(id==PdfDictionary.FontFile || id==PdfDictionary.FontFile2 || id==PdfDictionary.FontFile3)
            hasStream=true;


        switch(id){

            case PdfDictionary.CharProcs:
    		CharProcs=value;
    		break;

            case PdfDictionary.CIDSystemInfo:
	        	CIDSystemInfo=value;
	        break;
	        
	        case PdfDictionary.CIDToGIDMap:
	        	CIDToGIDMap=value;
            break;
            
            case PdfDictionary.DescendantFonts:
                DescendantFonts=value;
            break;

            case PdfDictionary.Encoding:
                Encoding=value;
            break;

            case PdfDictionary.FontDescriptor:
                FontDescriptor=value;
            break;

            case PdfDictionary.FontFile:
                FontFile=value;
            break;

            case PdfDictionary.FontFile2:
                FontFile2=value;
            break;

            case PdfDictionary.FontFile3:
                FontFile3=value;
            break;

            case PdfDictionary.ToUnicode:
            	ToUnicode=value;
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

                case StandardFonts.CIDTYPE0:
                    PDFvalue =StandardFonts.CIDTYPE0;
                break;
                
                case PdfDictionary.CIDFontType0C:
                    PDFvalue =PdfDictionary.CIDFontType0C;
                break;

                case StandardFonts.CIDTYPE2:
                    PDFvalue =StandardFonts.CIDTYPE2;
                break;

                case PdfDictionary.CMap:
                    PDFvalue =PdfDictionary.CMap;
                break;
                
                case PdfDictionary.Encoding:
                    PDFvalue =PdfDictionary.Encoding;
                break;

                case PdfDictionary.Identity_H:
                    PDFvalue =PdfDictionary.Identity_H;
                break;
                
                case PdfDictionary.Identity_V:
                    PDFvalue =PdfDictionary.Identity_V;
                break;
                
                case PdfDictionary.MacExpertEncoding:
                    PDFvalue =StandardFonts.MACEXPERT;
                break;
                
                case PdfDictionary.MacRomanEncoding:
                    PDFvalue =StandardFonts.MAC;
                break;
               
                case PdfDictionary.PDFDocEncoding:
                    PDFvalue =StandardFonts.PDF;
                break;

                case MMType1:
                    PDFvalue =StandardFonts.TYPE1;
                break;
             
                case PdfDictionary.StandardEncoding:
                    PDFvalue =StandardFonts.STD;
                break;

                case StandardFonts.TYPE0:
                    PDFvalue =StandardFonts.TYPE0;
                break;

                case StandardFonts.TYPE1:
                    PDFvalue =StandardFonts.TYPE1;
                break;

                case Type1C:
                    PDFvalue =StandardFonts.TYPE1;
                break;

                case StandardFonts.TYPE3:
                    PDFvalue =StandardFonts.TYPE3;
                break;

                case StandardFonts.TRUETYPE:
                    PDFvalue =StandardFonts.TRUETYPE;
                break;

                case PdfDictionary.WinAnsiEncoding:
                    PDFvalue =StandardFonts.WIN;
                break;

                default:

                	if(pdfKeyType==PdfDictionary.Encoding){
                		PDFvalue=CIDEncodings.getConstant(id);
                		
                		if(PDFvalue==PdfDictionary.Unknown){
                			
                			byte[] bytes=new byte[keyLength];

                            System.arraycopy(raw,keyStart,bytes,0,keyLength);
                           
                			unknownValue=new String(bytes);
                		}
                		
                		if(debug && PDFvalue==PdfDictionary.Unknown){
                			System.out.println("Value not in PdfCIDEncodings");
                			   
                           	 byte[] bytes=new byte[keyLength];

                               System.arraycopy(raw,keyStart,bytes,0,keyLength);
                               System.out.println("Add to CIDEncodings and as String");
                               System.out.println("key="+new String(bytes)+" "+id+" not implemented in setConstant in PdfFont Object");

                               System.out.println("final public static int CMAP_"+new String(bytes)+"="+id+";");
                               
                		}
                	}else
                		PDFvalue=super.setConstant(pdfKeyType,id);

                    if(PDFvalue==-1){


                         if(debug){
                            
                        	 byte[] bytes=new byte[keyLength];

                            System.arraycopy(raw,keyStart,bytes,0,keyLength);
                            System.out.println("key="+new String(bytes)+" "+id+" not implemented in setConstant in PdfFont Object");

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

        	case PdfDictionary.BaseEncoding:
        		BaseEncoding=PDFvalue;
        		break;
        		
	        case PdfDictionary.Encoding:
	        	generalType=PDFvalue;
	            break;
            
//            case PdfDictionary.Subtype:
//                subtype=PDFvalue;
//                //System.out.println("value set to "+subtype);
//                break;
            case PdfDictionary.ToUnicode:
                generalType=PDFvalue;
                break;
            default:
            	super.setConstant(pdfKeyType, PDFvalue);
        }
        
        return PDFvalue;
    }

    public int getParameterConstant(int key) {

        int def= PdfDictionary.Unknown;

        switch(key){
        
                   
            case PdfDictionary.BaseEncoding:
            	
            	//special cases first
            	if(key==PdfDictionary.BaseEncoding && Encoding!=null && Encoding.isZapfDingbats)
            		return StandardFonts.ZAPF;
            	else if(key==PdfDictionary.BaseEncoding && Encoding!=null && Encoding.isSymbol)
            		return StandardFonts.SYMBOL;
            	else
            		return BaseEncoding;
        }

        //check general values
        def=super.getParameterConstant(key);

        return def;
    }

    public void setStream(){

        hasStream=true;
    }
    
    
    public PdfArrayIterator getMixedArray(int id) {
		
    	switch(id){

            case PdfDictionary.Differences:
                return new PdfArrayIterator(Differences);

            default:
            	return super.getMixedArray(id);
			
        }
	}
    
    public byte[][] getByteArray(int id){
    	
    	switch(id){

    	case PdfDictionary.Differences:
	        return Differences;

        default:
        	return super.getByteArray(id);
		
    	}
    
    }

    public double[] getDoubleArray(int id) {

        switch(id){

            case PdfDictionary.FontMatrix:
                return deepCopy(FontMatrix);

            default:
            	return super.getDoubleArray(id);
        }

    }

    public void setDoubleArray(int id,double[] value) {

        switch(id){

            case PdfDictionary.FontMatrix:
                FontMatrix=value;
            break;

            default:
            	super.setDoubleArray(id, value);
        }

    }
    
    public void setMixedArray(int id,byte[][] value) {

        switch(id){

            case PdfDictionary.Differences:
                Differences=value;
            break;
            
            default:
            	super.setMixedArray(id, value);
          
        }

    }

    public float[] getFloatArray(int id) {

        switch(id){

        	case PdfDictionary.FontBBox:
        		return deepCopy(FontBBox);

            case PdfDictionary.Widths:
                return deepCopy(Widths);
               
            default:
                return super.getFloatArray(id);
        }
    }

    public void setFloatArray(int id,float[] value) {

        switch(id){

	        case PdfDictionary.FontBBox:
	            FontBBox=value;
	        break;
        
            case PdfDictionary.Widths:
                Widths=value;
            break;

            default:
            	super.setFloatArray(id, value);
        }
    }

    public void setName(int id,byte[] value) {

        switch(id){

            case PdfDictionary.BaseFont:
                rawBaseFont=value;
                
                //track if font called ZapfDingbats and flag
                int checksum = PdfDictionary.generateChecksum(0, value.length, value);
                
                isZapfDingbats=(checksum==ZapfDingbats || checksum==ZaDb);
                isSymbol=(checksum==Symbol);
                
                //store in both as we can't guarantee creation order
                if(Encoding!=null){
                	Encoding.isZapfDingbats=isZapfDingbats;
                	Encoding.isSymbol=isSymbol;
                }
                
              break;

            case PdfDictionary.CMapName:
                rawCMapName=value;
            break;
            
            case PdfDictionary.FontName:
                rawFontName=value;
            break;

            case PdfDictionary.FontStretch:
                rawFontStretch=value;
            break;


            default:
                super.setName(id,value);

        }

    }
    
    public void setTextStreamValue(int id,byte[] value) {

        switch(id){

	        case PdfDictionary.CharSet:
	            rawCharSet=value;
	        break;
	        
            case PdfDictionary.Ordering:
                rawOrdering=value;
            break;
            
            case PdfDictionary.Registry:
                rawRegistry=value;
            break;
            
            case PdfDictionary.W:
                rawW=value;
            break;
            
            default:
                super.setTextStreamValue(id,value);

        }

    }

    public String getName(int id) {

        switch(id){

            case PdfDictionary.BaseFont:

            //setup first time
            if(BaseFont==null && rawBaseFont!=null)
                BaseFont=new String(rawBaseFont);

            return BaseFont;
            
            case PdfDictionary.CMapName:

                //setup first time
                if(CMapName==null && rawCMapName!=null)
                	CMapName=new String(rawCMapName);

                return CMapName;

            case PdfDictionary.FontName:

            //setup first time
            if(FontName==null && rawFontName!=null)
            FontName=new String(rawFontName);

            return FontName;

            case PdfDictionary.FontStretch:
            	//setup first time
                if(FontStretch==null && rawFontStretch!=null)
                	FontStretch=new String(rawFontStretch);

                return FontStretch;

            case PdfDictionary.W:

                //setup first time
                if(W==null && rawW!=null)
                W=new String(rawW);

                return W;
                
            default:
                return super.getName(id);

        }
    }
    
    public String getTextStreamValue(int id) {

        switch(id){

	        case PdfDictionary.CharSet:
	
	            //setup first time
	            if(CharSet==null && rawCharSet!=null)
	            	CharSet=new String(rawCharSet);
	
	            return CharSet;
        
            case PdfDictionary.Ordering:

                //setup first time
                if(Ordering==null && rawOrdering!=null)
                	Ordering=new String(rawOrdering);

                return Ordering;
            
            case PdfDictionary.Registry:

                //setup first time
                if(Registry==null && rawRegistry!=null)
                	Registry=new String(rawRegistry);

                return Registry;

            case PdfDictionary.W:

                //setup first time
                if(W==null && rawW!=null)
                	W=new String(rawW);

                return W;

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

            case PdfDictionary.BaseFont:
                data=rawBaseFont;
                break;
            
            case PdfDictionary.CMapName:
                data=rawCMapName;
                break;
                
            case PdfDictionary.FontName:
                data=rawFontName;
                break;

            case PdfDictionary.FontStretch:
                data=rawFontStretch;
                break;
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
                    }else if(len>7 && data[len-7]=='+'){ //lose +ABCDEF if present
                        int length=len-7;
                        byte[] newData=new byte[length];
                        System.arraycopy(data, 0, newData, 0, length);
                        return new String(newData);
                    }else
                		return new String(data);
                }else
                    return null;    

            default:
                throw new RuntimeException("Value not defined in getName(int,mode)");
        }
    }
    
    public int getObjectType() {
		return PdfDictionary.Font;
	}
   
    
    public boolean decompressStreamWhenRead() {
		return true;
	}
}
