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

  * PdfObject.java
  * ---------------
  * (C) Copyright 2007, by IDRsolutions and Contributors.
  *
  *
  * --------------------------
 */
package org.jpedal.objects.raw;

import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

import org.jpedal.fonts.StandardFonts;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.io.PdfReader;
import org.jpedal.io.ObjectStore;
import org.jpedal.objects.acroforms.utils.ConvertToString;
import org.jpedal.utils.LogWriter;

/**
 * holds actual data for PDF file to process
 */
public class PdfObject implements Cloneable{

	protected boolean isIndexed;

    /**
     * states
     */
    public static final int DECODED=0;
    public static final int UNDECODED_REF=1;
    public static final int UNDECODED_DIRECT=2;

    private int status=DECODED;

    byte[] unresolvedData=null;
    
    //hold Other dictionary values 
    Map otherValues=new HashMap(); 

    protected int pageNumber = -1;
    
    int PDFkeyInt=-1;
    
    //our type which may not be same as /Type
    int objType=PdfDictionary.Unknown;
    
    //key of object
    private int id=-1;

    protected int colorspace=PdfDictionary.Unknown, subtype=PdfDictionary.Unknown,type=PdfDictionary.Unknown;

    private int BitsPerComponent=-1, BitsPerCoordinate=-1, BitsPerFlag=-1, Count=0, FormType=-1, Length=-1,Length1=-1,Length2=-1,Length3=-1,Rotate=-1; //-1 shows unset

    private float[] ArtBox, BBox, BleedBox, CropBox, Decode,Domain, Matrix, MediaBox, Range, TrimBox;

    protected  PdfObject ColorSpace=null, DecodeParms=null, Encoding=null,Function=null, 
    Resources=null,Shading=null, SMask=null;

    private boolean ignoreRecursion=false;
    
    //used by font code
    protected boolean isZapfDingbats=false, isSymbol=false;

    private boolean isCompressedStream=false;

    protected int generalType=PdfDictionary.Unknown; // some Dictionaries can be a general type (ie /ToUnicode /Identity-H)

    private String generalTypeAsString=null; //some values (ie CMAP can have unknown settings)

    //flag to show if we want parents (generally NO as will scan all up tree every time to root)
    protected boolean includeParent=false;

    private String Creator=null, Parent=null,Name=null, Title=null;
    private byte[] rawCreator,rawParent,rawName=null, rawS, rawTitle=null;
    public static boolean debug=false;

    protected String ref=null;
    int intRef,gen;

    protected boolean hasStream=false;

    public byte[] stream=null;
    private byte[] DecodedStream=null;

    //use for caching
    private long startStreamOnDisk=-1;
    private PdfObjectReader currentPdfFile=null;
    private String cacheName=null;

    private byte[][] Filter=null, TR=null;

    private byte[][] keys;

    private byte[][] values;

    private PdfObject[] objs;
    
    //used by /Other
    protected Object currentKey=null;

    //used to track AP
    protected int parentType=-1;

    protected PdfObject(){

    }
    
    public PdfObject(byte[] bytes) {
	}

    public PdfObject(int intRef, int gen) {
    	setRef(intRef,  gen);
    }
    
    public void setRef(int intRef, int gen){
    	this.intRef=intRef;
        this.gen=gen;
        
        //force reset as may have changed
        ref=null;
        
    }
    
    public void setRef(String ref){
    	
        this.ref=ref;
        
    }

    public PdfObject(String ref){
        this.ref=ref;
        
        //int ptr=ref.indexOf(" ");
        //if(ptr>0)
        //	intRef=PdfReader.parseInt(0, ptr, ref.getBytes());
        
    }

    public PdfObject(int type) {
        this.generalType=type;
    }

    protected static boolean[] deepCopy(boolean[] input){

        if(input==null)
        return null;

        int count=input.length;

        boolean[] deepCopy=new boolean[count];
        System.arraycopy(input,0,deepCopy,0,count);

        return deepCopy;
    }

    public int getStatus(){
        return status;
    }

    public byte[] getUnresolvedData(){
        return unresolvedData;
    }

    public int getPDFkeyInt(){
        return PDFkeyInt;
    }

    public void setUnresolvedData(byte[] unresolvedData,int PDFkeyInt){
        this.unresolvedData=unresolvedData;
        this.PDFkeyInt=PDFkeyInt;
        /**
        int len=unresolvedData.length;
        
        //if ref get first value as int
        if(unresolvedData[len-1]=='R'){
        	
        	int ptr=0, ii=0;
        	while(ii<len){
        		
        		ii++;
        		
        		if(unresolvedData[ii]==' '){
        			ptr=ii;
        			break;
        		}
        		
        	}
        	if(ptr>0)
            	intRef=PdfReader.parseInt(0, ptr, unresolvedData);
        	
        }/**/
    }

    public void setStatus(int status){
        this.status=status;
        this.unresolvedData=null;
    }

    protected static float[] deepCopy(float[] input){

        if(input==null)
        return null;

        int count=input.length;

        float[] deepCopy=new float[count];
        System.arraycopy(input,0,deepCopy,0,count);

        return deepCopy;
    }

    protected static double[] deepCopy(double[] input){

        if(input==null)
        return null;

        int count=input.length;

        double[] deepCopy=new double[count];
        System.arraycopy(input,0,deepCopy,0,count);

        return deepCopy;
    }

    protected static int[] deepCopy(int[] input){

        if(input==null)
        return null;

        int count=input.length;

        int[] deepCopy=new int[count];
        System.arraycopy(input,0,deepCopy,0,count);

        return deepCopy;
    }

    protected static byte[][] deepCopy(byte[][] input){

        if(input==null)
        return null;

        int count=input.length;

        byte[][] deepCopy=new byte[count][];
        System.arraycopy(input,0,deepCopy,0,count);

        return deepCopy;
    }

    public PdfObject getDictionary(int id){

        switch(id){

	        case PdfDictionary.ColorSpace:
	            return ColorSpace;

            case PdfDictionary.DecodeParms:
                return DecodeParms;

            case PdfDictionary.Function:
                return Function;

            case PdfDictionary.Resources:
                return Resources;

            case PdfDictionary.Shading:
                return Shading;
                
            case PdfDictionary.SMask:
	        	return SMask;

            default:


                return null;
        }
    }

    public int getGeneralType(int id){

        //special case
        if(id==PdfDictionary.Encoding && isZapfDingbats) //note this is Enc object so local
            return StandardFonts.ZAPF;
        else if(id==PdfDictionary.Encoding && isSymbol) //note this is Enc object so local
            return StandardFonts.SYMBOL;
        else
            return generalType;
    }

    public String getGeneralStringValue(){
        return generalTypeAsString;
    }

    public void setGeneralStringValue(String generalTypeAsString){
        this.generalTypeAsString=generalTypeAsString;
    }

    public void setIntNumber(int id,int value){

        switch(id){

	        case PdfDictionary.BitsPerComponent:
	    		BitsPerComponent=value;
	    		break;

            case PdfDictionary.BitsPerCoordinate:
	    		BitsPerCoordinate=value;
	    		break;

            case PdfDictionary.BitsPerFlag:
	    		BitsPerFlag=value;
	    		break;

            case PdfDictionary.Count:
                Count=value;
                break;

            case PdfDictionary.FormType:
                FormType=value;
                break;

            case PdfDictionary.Length:
                Length=value;
                break;

            case PdfDictionary.Length1:
                Length1=value;
                break;
            
            case PdfDictionary.Length2:
                Length2=value;
                break;
                
            case PdfDictionary.Length3:
                Length3=value;
                break;
                
            case PdfDictionary.Rotate:
    		    Rotate=value;
    		    break;

            default:

        }
    }

    public void setFloatNumber(int id,float value){

        switch(id){

//	        case PdfDictionary.BitsPerComponent:
//	    		BitsPerComponent=value;
//	    		break;

            default:

        }
    }

    public int getInt(int id){

        switch(id){

        	case PdfDictionary.BitsPerComponent:
        		return BitsPerComponent;

            case PdfDictionary.BitsPerCoordinate:
                return BitsPerCoordinate;

            case PdfDictionary.BitsPerFlag:
                return BitsPerFlag;

            case PdfDictionary.Count:
                return Count;

            case PdfDictionary.FormType:
                return FormType;

            case PdfDictionary.Length:
                return Length;
            
            case PdfDictionary.Length1:
                return Length1;
                
            case PdfDictionary.Length2:
                return Length2;
            
            case PdfDictionary.Length3:
                return Length3;

            case PdfDictionary.Rotate:
                return Rotate;

            default:

                return PdfDictionary.Unknown;
        }
    }

    public float getFloatNumber(int id){

        switch(id){

//        	case PdfDictionary.BitsPerComponent:
//        		return BitsPerComponent;

            default:

                return PdfDictionary.Unknown;
        }
    }

    public boolean getBoolean(int id){

        switch(id){


            default:

        }

        return false;
    }

    public void setBoolean(int id,boolean value){

        switch(id){


            default:

        }
    }



    public void setDictionary(int id,PdfObject value){

    	value.setID(id);
    	
        switch(id){

        	case PdfDictionary.ColorSpace:
        		ColorSpace=value;
        		break;
        		
            case PdfDictionary.DecodeParms:
                DecodeParms=value;
                break;

            case PdfDictionary.Function:
                Function=value;
                break;

            case PdfDictionary.Resources:
                Resources=value;
                break;

            case PdfDictionary.Shading:
                Shading=value;
            break;
            
            case PdfDictionary.SMask:
	        	SMask=value;
			break;

            default:

                setOtherValues(id, value);

        }
    }

    /**
     * some values stored in a MAP for AP or Structurede Content
     */
    protected void setOtherValues(int id, PdfObject value) {

        if(objType== PdfDictionary.Form || objType==PdfDictionary.MCID || currentKey!=null){

            //if(1==1)
                //throw new RuntimeException("xx="+currentKey+" id="+id);

            otherValues.put(currentKey,value);
            currentKey=null;
        }
    }

    public void setID(int id) {

        this.id=id;
		
	}
    
    public int getID() {
		 return id;
		
	}

    /**
     * only used internally for some forms - please do not use
     * @return
     */
    public int getParentID(){
        return parentType;
    }

    public void setParentID(int parentType){
        this.parentType=parentType;
    }

	/**
     * flag set for embedded data
     */
    public boolean hasStream() {
        return hasStream;
    }


//    public int setConstant(int pdfKeyType, int keyStart, int keyLength, byte[] raw) {
//
//
//        return PdfDictionary.Unknown;
//    }

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

//            case PdfDictionary.Image:
//                PDFvalue =PdfDictionary.Image;
//            break;
//
//            case PdfDictionary.Form:
//                PDFvalue =PdfDictionary.Form;
//            break;

                

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

        return id;
    }

    public int getParameterConstant(int key) {
        int def= PdfDictionary.Unknown;

        switch(key){

	        case PdfDictionary.ColorSpace:
	            return colorspace;
            
            case PdfDictionary.Subtype:
                return subtype;

            case PdfDictionary.Type:
                return type;

        }

        return def;
    }

    /**
     * common values shared between types
     */
    public int setConstant(int pdfKeyType, int id) {
        int PDFvalue =id;


        /**
         * map non-standard
         */
        switch(id){

            case PdfDictionary.FontDescriptor:
                PDFvalue =PdfDictionary.Font;
                break;
           
        }


        switch(pdfKeyType){

        	case PdfDictionary.ColorSpace:
        		colorspace=PDFvalue;
        		break;
        		
        	case PdfDictionary.Subtype:
    		subtype=PDFvalue;
    		break;
    		
            case PdfDictionary.Type:

                //@speed if is temp hack as picks up types on some subobjects
                //if(type==PdfDictionary.Unknown)
                this.type=PDFvalue;

                break;
        }

        return PDFvalue;
    }

    public float[] getFloatArray(int id) {

        float[] array=null;
        switch(id){

            case PdfDictionary.ArtBox:
        		return deepCopy(ArtBox);

            case PdfDictionary.BBox:
        		return deepCopy(BBox);

            case PdfDictionary.BleedBox:
                return deepCopy(BleedBox);

            case PdfDictionary.CropBox:
                return deepCopy(CropBox);
            
            case PdfDictionary.Decode:
        		return deepCopy(Decode);

            case PdfDictionary.Domain:
                return deepCopy(Domain);

            case PdfDictionary.Matrix:
        		return deepCopy(Matrix);

            case PdfDictionary.MediaBox:
                return deepCopy(MediaBox);
                
            case PdfDictionary.Range:
                return deepCopy(Range);

            case PdfDictionary.TrimBox:
                return deepCopy(TrimBox);

            default:

        }

        return deepCopy(array);
    }

     public byte[][] getKeyArray(int id) {

        switch(id){


            default:

        }

        return null;
    }

    public double[] getDoubleArray(int id) {

        double[] array=null;
        switch(id){

            default:

        }

        return deepCopy(array);
    }

    public boolean[] getBooleanArray(int id) {

        boolean[] array=null;
        switch(id){

            default:

        }

        return deepCopy(array);
    }
    
    public int[] getIntArray(int id) {

        int[] array=null;
        switch(id){

            default:

        }

        return deepCopy(array);
    }

    public void setFloatArray(int id,float[] value) {

        switch(id){

            case PdfDictionary.ArtBox:
	            ArtBox=value;
	            break;

            case PdfDictionary.BBox:
	            BBox=value;
	            break;

            case PdfDictionary.BleedBox:
                BleedBox=value;
                break;
            
            case PdfDictionary.CropBox:
                CropBox=value;
                break;

            case PdfDictionary.Decode:
	        	Decode=ignoreIdentity(value);
	        break;
	        
            case PdfDictionary.Domain:
	            Domain=value;
	        break;
	        
	        case PdfDictionary.Matrix:
	            Matrix=value;
	        break;
        
            case PdfDictionary.MediaBox:
                MediaBox=value;
                break;

            case PdfDictionary.Range:
                Range=value;
                break;
            
            case PdfDictionary.TrimBox:
                TrimBox=value;
                break;

            default:

        }

    }

    /**ignore identity value which makes no change*/
    private static float[] ignoreIdentity(float[] value) {

        boolean isIdentity =true;
        if(value!=null){

            int count=value.length;
            for(int aa=0;aa<count;aa=aa+2){
                if(value[aa]==0f && value[aa+1]==1f){
                    //okay
                }else{
                    isIdentity =false;
                    aa=count;
                }
            }
        }

        if(isIdentity)
            return null;
        else
            return value;
    }

    public void setIntArray(int id,int[] value) {

        switch(id){

            default:

        }

    }

    public void setBooleanArray(int id,boolean[] value) {

        switch(id){

            default:

        }

    }
    
    public void setDoubleArray(int id,double[] value) {

        switch(id){

            default:

        }

    }


    public void setMixedArray(int id,byte[][] value) {

        switch(id){

            case PdfDictionary.Filter:
	
                Filter=value;
	            break;
            
               
            default:

        }

    }


     public String getStringValue(int id,int mode) {

        byte[] data=null;

        //get data
        switch(id){

            case PdfDictionary.Name:

                data=rawName;
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
                    }else
                		return new String(data);
                }else
                    return null;

            default:
                throw new RuntimeException("Value not defined in getName(int,mode)");
        }
    }

    //return as constant we can check
    public int getNameAsConstant(int id) {
        //return PdfDictionary.generateChecksum(0,raw.length,raw);
        return PdfDictionary.Unknown;
    }

    public String getName(int id) {

        String str=null;
        switch(id){

            case PdfDictionary.Name:

            //setup first time
            if(Name==null && rawName!=null)
            Name=new String(rawName);

            return Name;
            


//            case PdfDictionary.Parent:
//
//                //setup first time
//                if(Filter==null && rawParent!=null)
//                    Parent=new String(rawParent);
//
//                return Parent;

            default:

        }

        return str;
    }

    public String getStringKey(int id) {

        String str=null;
        switch(id){

            case PdfDictionary.Parent:

                //setup first time
                if(Parent==null && rawParent!=null)
                    Parent=new String(rawParent);

                return Parent;

            default:

        }

        return str;
    }

    public String getTextStreamValue(int id) {

        String str=null;
        switch(id){

            case PdfDictionary.Creator:

            //setup first time
            if(Creator==null && rawCreator!=null)
            Creator= PdfReader.getTextString(rawCreator, false);

            return Creator;

            //can also be stream in OCProperties
            case PdfDictionary.Name:

	            //setup first time
	            if(Name==null && rawName!=null)
	            	Name=PdfReader.getTextString(rawName, false);

	            return Name;

             case PdfDictionary.Title:

	            //setup first time
	            if(Title==null && rawTitle!=null)
	            	Title=PdfReader.getTextString(rawTitle, false);

	            return Title;

//            case PdfDictionary.Filter:
//
//            //setup first time
//            if(Filter==null && rawFilter!=null)
//            Filter=PdfReader.getTextString(rawFilter);
//
//            return Filter;

            default:

        }

        return str;
    }

    public void setName(int id,byte[] value) {

        switch(id){

             case PdfDictionary.Name:
                rawName=value;
            break;

             case PdfDictionary.S:
                rawS=value;
            break;

            case PdfDictionary.Parent:

                //gets into endless loop if any obj so use sparingly
                if(includeParent)
                rawParent=value;

                break;

            default:

                if(objType==PdfDictionary.MCID){

            		//if(1==1)
            			//throw new RuntimeException("xx="+currentKey+" id="+id);
            		otherValues.put(currentKey,value);
            		//System.out.println("id="+id+" "+value+" "+type+" "+objType+" "+this+" "+otherValues);
            	}else{

                }
        }

    }

    public void setName(Object id,String value) {

            otherValues.put(id,value);
           // System.out.println("id="+id+" "+value+" "+type+" "+objType+" "+this+" "+otherValues);
    }

    public void setStringKey(int id,byte[] value) {

           switch(id){

               case PdfDictionary.Parent:
                   rawParent=value;
                   break;

               default:

           }

       }


    public void setTextStreamValue(int id,byte[] value) {

        switch(id){

            case PdfDictionary.Creator:
                rawCreator=value;
            break;

            case PdfDictionary.Name:
                rawName=value;
            break;

            case PdfDictionary.Title:
                rawTitle=value;
            break;

            default:

        }

    }

    public byte[] getDecodedStream() {

        if(isCached()){
            byte[] cached=null;

            try{

            File f=new File(getCachedStreamFile(currentPdfFile));
            BufferedInputStream bis=new BufferedInputStream(new FileInputStream(f));
            cached=new byte[(int)f.length()];

                //System.out.println(cached.length+" "+DecodedStream.length);
                bis.read(cached);
                bis.close();

             //System.out.println(new String(cached));
            }catch(Exception ee){
                ee.printStackTrace();
            }
           return cached;
        }else

        return DecodedStream;
    }

    /**public byte[] getStream() {
    	
    	if(DecodedStream==null)
    		return null;
    	
    	//make a a DEEP copy so we cant alter
		int len=DecodedStream.length;
		byte[] copy=new byte[len];
		System.arraycopy(DecodedStream, 0, copy, 0, len);
		
        return copy;
    }/**/

    public void setStream(byte[] stream) {
        this.stream=stream;
        
        if(this.getObjectType()==PdfDictionary.ColorSpace)
        	hasStream=true;
    }
    
    public void setDecodedStream(byte[] stream) {
        this.DecodedStream=stream;
    }

    public String getObjectRefAsString() {

        if(ref==null)
            ref=intRef+" "+gen+" R";

        return this.ref;
    }

    public int getObjectRefID() {

        return intRef;
    }

    public int getObjectRefGeneration() {

        return gen;
    }

    public PdfArrayIterator getMixedArray(int id) {

        switch(id){

            case PdfDictionary.Filter:
                return new PdfArrayIterator(Filter);

            default:

                return null;
        }
    }

    public void setDictionaryPairs(byte[][] keys, byte[][] values, PdfObject[] objs) {

        this.keys=keys;
        this.values=values;
        this.objs=objs;


    }

    public PdfKeyPairsIterator getKeyPairsIterator() {
        return new PdfKeyPairsIterator(keys,values,objs);
    }

    public void setKeyArray(int id, byte[][] keyValues) {

        switch(id){

            default:

        }
    }
    
    public void setStringArray(int id, byte[][] keyValues) {

        switch(id){

        case PdfDictionary.TR:
        	TR=keyValues;
        	break;
        	
            default:

        }
    }

    
    public byte[][] getStringArray(int id) {

        switch(id){

        case PdfDictionary.TR:
        	return deepCopy(TR);

            default:

        }

		return null;
	}

    public Object[] getObjectArray(int id) {

        switch(id){

//        case PdfDictionary.TR:
//        	return deepCopy(TR);

            default:

        }

		return null;
	}

    public void setObjectArray(int id, Object[] objectValues) {

        switch(id){

//        case PdfDictionary.TR:
//        	TR=keyValues;
//        	break;

            default:

        }
    }

    final public Object clone()
	{
		Object o = null;
		try
		{
			o = super.clone();
		}
		catch( Exception e ){
        }

		return o;
	}

    

	public boolean decompressStreamWhenRead() {
		return false;
	}

	public int getObjectType() {
		return objType;
	}

	public byte[] getStringValueAsByte(int id) {
		return null;
	}

    public boolean isCompressedStream() {
        return isCompressedStream;
    }

     public void setCompressedStream(boolean isCompressedStream) {
        this.isCompressedStream=isCompressedStream;
    }

	/**do not cascade down whole tree*/
    public boolean ignoreRecursion() {
		return ignoreRecursion;
	}
    
    /**do not cascade down whole tree*/
    public void ignoreRecursion(boolean ignoreRecursion) {
		this.ignoreRecursion=ignoreRecursion;
	}

	public byte[] getTextStreamValueAsByte(int id) {
		return null;
	}

	public byte[][] getByteArray(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setTextStreamValue(int id2, String value) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * used in Forms code where keys can be page numbers
	 * @return
	 */
	public Map getOtherDictionaries() {
		
		return otherValues;
	}

	public void setCurrentKey(Object key) {
		currentKey=key;
	}

    //convenience method to return array as String
    public String toString(float[] floatArray, boolean appendPageNumber) {

        if(floatArray==null)
        return null;

        StringBuffer value=new StringBuffer();

        if(appendPageNumber){
            value.append(pageNumber);
            value.append(' ');
        }

        int items=floatArray.length;
        for(int ii=0;ii<items;ii++){
            value.append(floatArray[ii]);
            value.append(' ');
        }

        return value.toString();
    }

    /**
	 * @return the page this field is associated to
	 */
	public int getPageNumber() {
		return pageNumber;
	}

    /**
     * set the page number for this form
     */
    public void setPageNumber(int number) {

        pageNumber = number;
    }


    /**
     * set the page number for this form
     */
    public void setPageNumber(Object field) {
        if (field instanceof String) {
            try{
            	pageNumber = Integer.parseInt((String) field);
            }catch(NumberFormatException e){
            	pageNumber = 1;
            }
        } else {
            LogWriter.writeFormLog("{FormObject.setPageNumber} pagenumber being set to UNKNOWN type", false);
        }
    }


    public void setCache(long offset, PdfObjectReader pdfReader) {
        this.startStreamOnDisk=offset;
        this.currentPdfFile=pdfReader;
    }

    public boolean isCached() {

        return startStreamOnDisk!=-1;
    }

    public String getCachedStreamFile(PdfObjectReader currentPdfFile){

        File tmpFile=null;

        if(startStreamOnDisk!=-1){ //cached so we need to read it

            try{

                tmpFile=File.createTempFile("jpedal-", ".bin",new File(ObjectStore.temp_dir));
                tmpFile.deleteOnExit();
                
                //put raw data on disk
                currentPdfFile.spoolStreamDataToDisk(tmpFile,startStreamOnDisk);

                //set name for access
                cacheName=tmpFile.getAbsolutePath();

                //System.out.println("cached file size="+tmpFile.length()+" "+this.getObjectRefAsString());
            }catch(Exception e){

            }finally{
                //remove at end
                if(tmpFile!=null)
                    tmpFile.deleteOnExit();
            }
        }

        //decrypt and decompress
        currentPdfFile.readStream(this,true,true,false, getObjectType()==PdfDictionary.Metadata, isCompressedStream(), cacheName);


        return cacheName;
    }

	public boolean isColorSpaceIndexed() {
		return isIndexed;
	}
}
