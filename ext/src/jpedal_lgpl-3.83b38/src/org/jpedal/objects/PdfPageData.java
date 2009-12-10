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
* PdfPageData.java
* ---------------
*/
package org.jpedal.objects;

import java.io.Serializable;
import java.util.StringTokenizer;

import org.jpedal.utils.Strip;
import org.jpedal.utils.repositories.Vector_String;
import org.jpedal.utils.repositories.Vector_Int;
import org.jpedal.utils.repositories.Vector_Object;

/**
 * store data relating to page sizes set in PDF (MediaBox, CropBox, rotation)
 */
public class PdfPageData implements Serializable{

	private boolean valuesSet=false;

	private boolean useCustomRounding = true;
	
	private int lastPage=-1;

	private int raw_rotation = 0;

	private int pagesRead=-1;

    private int pageCount=1; //number of pages

    private float[] defaultMediaBox=null;

	/** any rotation on page (defined in degress) */
	private int rotation = 0;

	/** max media string for page */
    private Vector_Object mediaBoxes = new Vector_Object(500);
    private Vector_Object cropBoxes = new Vector_Object(500);

    private Vector_Int rotations=null;

    /** current x and y read from page info */
	private float cropBoxX = -99999, cropBoxY = -1,
	cropBoxW = -1, cropBoxH = -1;

	/** current x and y read from page info */
	private float mediaBoxX=-1, mediaBoxY, mediaBoxW, mediaBoxH;

	/** string representation of crop box */
	private float scalingValue = 1f;

    private float[] mediaBox,cropBox;

    /** string representation of media box */
	private int defaultrotation;
	private float defaultcropBoxX,defaultcropBoxY,defaultcropBoxW,defaultcropBoxH;
	private float defaultmediaBoxX,defaultmediaBoxY,defaultmediaBoxW,defaultmediaBoxH;

	public PdfPageData(){}

	/**
	 * make sure a value set for crop and media box (used internally to trap 'odd' settings and insure setup correctly)
	 */
	 public void checkSizeSet(int pageNumber) {

        //use default
        if(mediaBox ==null)
            mediaBox = defaultMediaBox;

        //value we keep
        if(cropBox!=null &&
                (cropBox[0]!=mediaBox[0] || cropBox[1]!=mediaBox[1] || cropBox[2]!=mediaBox[2] || cropBox[3]!=mediaBox[3])){

            mediaBoxes.setElementAt(mediaBox, pageNumber);
            cropBoxes.setElementAt(cropBox, pageNumber);

        }else if(mediaBox!=null &&
                (defaultMediaBox[0]!=mediaBox[0] || defaultMediaBox[1]!=mediaBox[1] || defaultMediaBox[2]!=mediaBox[2] || defaultMediaBox[3]!=mediaBox[3])) //if matches default don't save
            mediaBoxes.setElementAt(mediaBox, pageNumber);

        //track which pages actually read
        if(pagesRead<pageNumber)
            pagesRead=pageNumber;

        mediaBox=null;
        cropBox=null;
    }
	 
	 
	 /**
	  * return height of mediaBox
	  */
	 final public int getMediaBoxHeight(int pageNumber) {

		 //check values correctly set
		 setSizeForPage(pageNumber);
		 
		 return (int)mediaBoxH;
	 }

	 /**
	  * return mediaBox y value
	  */
	 final public int getMediaBoxY(int pageNumber) {

		 //check values correctly set
		 setSizeForPage(pageNumber);

		 return (int)mediaBoxY;
	 }

	 /**
	  * return mediaBox x value
	  */
	 final public int getMediaBoxX(int pageNumber) {

		 //check values correctly set
		 setSizeForPage(pageNumber);

		 return (int)mediaBoxX;
	 }

	 /**
	  * set string with raw values and assign values to crop and media size
	  */
	 public void setMediaBox(float[] mediaBox) {

         this.mediaBox=mediaBox;
         cropBox=null;

         if(defaultMediaBox==null)
            defaultMediaBox=mediaBox;

     }

	 /**
	  * set crop with values and align with media box
	  */
	 public void setCropBox(float[] cropBox) {

         this.cropBox=cropBox;

     }

	 public int setPageRotation(int value, int pageNumber) {

		 raw_rotation =  value;

		 //convert negative
		 if (raw_rotation < 0)
			 raw_rotation = 360 + raw_rotation;

         //only create if we need and set value
         if(raw_rotation!=0 || rotations!=null){
             if(rotations==null){
                if(pageNumber<2000)
                    rotations=new Vector_Int(2000);
                 else
                    rotations=new Vector_Int(pageNumber*2);
             }

             rotations.setElementAt(raw_rotation,pageNumber);

         }
         return raw_rotation;
	 }

	 /**
	  * return width of media box
	  */
	 final public int getMediaBoxWidth(int pageNumber) {

		 //check values correctly set
		 setSizeForPage(pageNumber);

		 return (int)mediaBoxW;
	 }

	 /**
	  * return mediaBox string found in PDF file
	  */
	 public String getMediaValue(int currentPage) {

		float[] mediaBox=null;
		 
		//use default
        if(mediaBoxes ==null)
        	mediaBox=(float[]) mediaBoxes.elementAt(currentPage);
        else
        	mediaBox=defaultMediaBox;
        
        StringBuffer returnValue=new StringBuffer();

        for(int j=0;j<4;j++){
        	returnValue.append(mediaBox[j]);
        	returnValue.append(' ');
        }
		
        return returnValue.toString();
	 }

	 /**
	  * return cropBox string found in PDF file
	  */
	 public String getCropValue(int currentPage) {

		float[] cropBox=null;
		 
		//use default
		if(cropBoxes!=null)
			cropBox=(float[]) cropBoxes.elementAt(currentPage);
		else if(cropBox!=null)
			cropBox=(float[]) mediaBoxes.elementAt(currentPage);
		
		if(cropBox==null)
        	cropBox=defaultMediaBox;
        
        StringBuffer returnValue=new StringBuffer();

        for(int j=0;j<4;j++){
        	returnValue.append(cropBox[j]);
        	returnValue.append(' ');
        }
		
        return returnValue.toString();
	 }
	 
	 /**
	  * return Scaled x value for cropBox
	  */
	 public int getScaledCropBoxX(int pageNumber) {

		 //check values correctly set
		 setSizeForPage(pageNumber);

		 return roundFloat(cropBoxX*scalingValue);
	 }

	 /**
	  * return Scaled cropBox width
	  */
	 public int getScaledCropBoxWidth(int pageNumber) {

		 //check values correctly set
		 setSizeForPage(pageNumber);

		 return roundFloat(cropBoxW*scalingValue);
	 }

	 /**
	  * return Scaled y value for cropox
	  */
	 public int getScaledCropBoxY(int pageNumber) {

		 //check values correctly set
		 setSizeForPage(pageNumber);

		 return roundFloat(cropBoxY*scalingValue);
	 }

	 /**
	  * return Scaled cropBox height
	  */
	 public int getScaledCropBoxHeight(int pageNumber) {

		 //check values correctly set
		 setSizeForPage(pageNumber);

		 return roundFloat(cropBoxH*scalingValue);
	 }
	 
	 
	 /**
	  * return x value for cropBox
	  */
	 public int getCropBoxX(int pageNumber) {

		 //check values correctly set
		 setSizeForPage(pageNumber);

		 return (int)cropBoxX;
	 }

	 /**
	  * return cropBox width
	  */
	 public int getCropBoxWidth(int pageNumber) {

		 //check values correctly set
		 setSizeForPage(pageNumber);

		 return (int)cropBoxW;
	 }

	 /**
	  * return y value for cropox
	  */
	 public int getCropBoxY(int pageNumber) {

		 //check values correctly set
		 setSizeForPage(pageNumber);
		 
		 return (int)cropBoxY;
	 }

	 /**
	  * return cropBox height
	  */
	 public int getCropBoxHeight(int pageNumber) {

		 //check values correctly set
		 setSizeForPage(pageNumber);

		 return (int)cropBoxH;
	 }
	 
	 /**see if current figures generated for this page and setup if not*/
	 private void setSizeForPage(int pageNumber){

         if(pageNumber>pageCount)
                 pageCount=pageNumber;
		 /**calculate values if first call for this page*/
		 if(pageNumber>pagesRead){

			 //set values if no value
			 mediaBoxX=0;
			 mediaBoxY=0;
			 mediaBoxW = 0;
			 mediaBoxH = 0;

			 //set values if no value
			 cropBoxX=0;
			 cropBoxY=0;
			 cropBoxW = 0;
			 cropBoxH = 0;

		 }else if((pageNumber>0)&&(lastPage!=pageNumber)){

			 lastPage=pageNumber;

			 boolean usingDefault=false;

			 float[] cropBox=(float[])cropBoxes.elementAt(pageNumber);
			 float[] mediaBox=(float[])mediaBoxes.elementAt(pageNumber);
			 if(mediaBox==null && defaultMediaBox!=null){
				 mediaBox=defaultMediaBox;
				 usingDefault=true;
			 }

			 
			 //set rotation
			 if(rotations!=null)
				  rotation=rotations.elementAt(pageNumber);
			 else
				  rotation=defaultrotation;


			 if(valuesSet && usingDefault){

				 cropBoxX=defaultcropBoxX;
				 mediaBoxX=defaultmediaBoxX;
				 cropBoxY=defaultcropBoxY;
				 mediaBoxY=defaultmediaBoxY;
				 cropBoxW=defaultcropBoxW;
				 mediaBoxW=defaultmediaBoxW;
				 cropBoxH=defaultcropBoxH;
				 mediaBoxH=defaultmediaBoxH;

			 }else{

				  /**
				   * set mediaBox, cropBox and default if none
				   */
				  
				  //set values if no value
				  mediaBoxX=0;
				  mediaBoxY=0;
				  mediaBoxW = 800;
				  mediaBoxH = 800;

				  if(mediaBox!=null){
					  mediaBoxX=mediaBox[0];
					  mediaBoxY=mediaBox[1];
					  mediaBoxW=mediaBox[2]-mediaBoxX;
					  mediaBoxH=mediaBox[3]-mediaBoxY;

                      if(mediaBoxY>0 && mediaBoxH==-mediaBoxY){
                          mediaBoxH = -mediaBoxH;
                          mediaBoxY=0;
                      }
                  }
				  
				  /**
				   * set crop
				   */
				  if(cropBox!=null){
					  
					  cropBoxX=cropBox[0];
					  cropBoxY=cropBox[1];
					  cropBoxW=cropBox[2];
					  cropBoxH=cropBox[3];

					  if(cropBoxX>cropBoxW){
						  float temp = cropBoxX;
						  cropBoxX = cropBoxW;
						  cropBoxW = temp;
					  }
					  if(cropBoxY>cropBoxH){
						  float temp = cropBoxY;
						  cropBoxY = cropBoxH;
						  cropBoxH = temp;
					  }

					  cropBoxW = cropBoxW-cropBoxX;
					  cropBoxH = cropBoxH-cropBoxY;

                      if(cropBoxY>0 && cropBoxH==-cropBoxY){
                          cropBoxH = -cropBoxH;
                          cropBoxY=0;
                      }

                  }else{
					  cropBoxX = mediaBoxX;
					  cropBoxY = mediaBoxY;
					  cropBoxW = mediaBoxW;
					  cropBoxH = mediaBoxH;
				  }
			 }

			 if(usingDefault && !valuesSet){

				 defaultrotation=rotation;
				 defaultcropBoxX=cropBoxX;
				 defaultmediaBoxX=mediaBoxX;
				 defaultcropBoxY=cropBoxY;
				 defaultmediaBoxY=mediaBoxY;
				 defaultcropBoxW=cropBoxW;
				 defaultmediaBoxW=mediaBoxW;
				 defaultcropBoxH=cropBoxH;
				 defaultmediaBoxH=mediaBoxH;

				 valuesSet=true;
			 }
		 }
	 }

	 /**
	  * Get the scaling value currently being used
	  */
	 public float getScalingValue() {
		 return scalingValue;
	 }


	 /**
	  * Scaling value to apply to all values
	  */
	 public void setScalingValue(float scalingValue) {
		 this.scalingValue = scalingValue;
	 }
	 
	 private int roundFloat(float origValue){
		 int roundedValue = (int)origValue;
		 
		 if(useCustomRounding){
			 float frac = origValue - roundedValue;
			 if(frac>0.3)
				 roundedValue = roundedValue + 1;
		 }
		 return roundedValue;

	 }

    /**
     * get page count
     */
    final public int getPageCount(){
        return pageCount;
    }
	 
	 /** return rotation value (for outside class) */
	 final public int getRotation(int pageNumber) {

		 //check values correctly set
		 setSizeForPage(pageNumber);

		 return rotation;
	 }

	/**
	  * return Scaled height of mediaBox
	  */
	 final public int getScaledMediaBoxHeight(int pageNumber) {
	
		 //check values correctly set
		 setSizeForPage(pageNumber);
		 
		 return roundFloat(mediaBoxH*scalingValue);
	 }

	/**
	  * return Scaled width of media box
	  */
	 final public int getScaledMediaBoxWidth(int pageNumber) {
	
		 //check values correctly set
		 setSizeForPage(pageNumber);
	
		 return roundFloat(mediaBoxW*scalingValue);
	 }

	/**
	  * return Scaled mediaBox x value
	  */
	 final public int getScaledMediaBoxX(int pageNumber) {
	
		 //check values correctly set
		 setSizeForPage(pageNumber);
		 
		 return roundFloat(mediaBoxX*scalingValue);
	 }

	/**
	  * return Scaled mediaBox y value
	  */
	 final public int getScaledMediaBoxY(int pageNumber) {
	
		 //check values correctly set
		 setSizeForPage(pageNumber);
	
		 return roundFloat(mediaBoxY*scalingValue);
	 }
	 
}
