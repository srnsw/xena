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
* ColorspaceFactory.java
* ---------------
*/
package org.jpedal.color;



import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;

import java.util.Map;


/**
 * @author markee
 *
 * returns the correct colorspace, decoding the values
 */
public class ColorspaceFactory {

    private ColorspaceFactory(){}
    

	
	
	/**
	 * used by commands which implicitly set colorspace
	 * @param cacheCol
	  */
	final public static GenericColorSpace getColorSpaceInstance(boolean isPrinting,
			PdfObjectReader currentPdfFile, PdfObject colorSpace, Map colorspacesObjects,
            Map colorspacesSeen, boolean cacheCol) {

       //see if we already have it
        String key=null;
        if(colorSpace.getStatus()==PdfObject.DECODED)
            key=colorSpace.getObjectRefAsString();
        else
            key=new String(colorSpace.getUnresolvedData());

		//cache some colorspaces for speed
        if(cacheCol){
        	
        	
	        Object cachedValue=colorspacesObjects.get(key);
	        
	        if(cachedValue!=null){
	        	GenericColorSpace col=(GenericColorSpace) cachedValue;
	        	col.reset();
	        	
	            return col;
	        }
        }
        
        currentPdfFile.checkResolved(colorSpace);

		PdfObject rawSpace=colorSpace;	
        
		int ID=colorSpace.getParameterConstant(PdfDictionary.ColorSpace);

        if (ID==ColorSpaces.Separation){
			 return new SeparationColorSpace(currentPdfFile, colorSpace, colorSpace,colorspacesObjects);
        }else if (ID==ColorSpaces.DeviceN){
			 return new DeviceNColorSpace(currentPdfFile, colorSpace, colorSpace,colorspacesObjects);
		}else {
			
			boolean isIndexed=false;
			
			//default value
			GenericColorSpace currentColorData=new DeviceRGBColorSpace();
			
			/**setup colorspaces which map onto others*/
			if (ID==ColorSpaces.Indexed || ID==PdfDictionary.I){
			
				isIndexed=true;	
				
				//actual colorspace
				colorSpace=colorSpace.getDictionary(PdfDictionary.Indexed);
				
				ID=colorSpace.getParameterConstant(PdfDictionary.ColorSpace);
			     	
			}
			
			if (ID==ColorSpaces.Separation)
				currentColorData=new SeparationColorSpace(currentPdfFile,colorSpace,rawSpace,colorspacesObjects);
			else if (ID==ColorSpaces.DeviceN){				
				currentColorData=new DeviceNColorSpace(currentPdfFile, colorSpace,colorSpace, colorspacesObjects);
			}
			
			//set defaults
			float[] R = { -100f,100f, -100.0f, 100.0f };
			float[] W = { 0.0f, 1.0f, 0.0f };
			float[] B = { 0.0f, 0.0f, 0.0f };
			float[] G = { 1.0f, 1.0f, 1.0f };
			float[] Ma = { 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f };

			if (ID==ColorSpaces.DeviceGray){
				currentColorData=new DeviceGrayColorSpace();
			} else if (ID==ColorSpaces.DeviceRGB) {
				currentColorData=new DeviceRGBColorSpace();
			} else if (ID==ColorSpaces.DeviceCMYK) {
				currentColorData=new DeviceCMYKColorSpace();
			} else if (ID==ColorSpaces.CalGray) {
				
				float[] gammaArray=null;
				float[] blackpointArray=colorSpace.getFloatArray(PdfDictionary.BlackPoint);
				float[] whitepointArray=colorSpace.getFloatArray(PdfDictionary.WhitePoint);
				float rawGamma=colorSpace.getFloatNumber(PdfDictionary.Gamma);
				if(rawGamma!=-1){
					gammaArray=new float[1];
					gammaArray[0]=rawGamma;
				}
				
				if (whitepointArray != null)
		            W=whitepointArray;

				if (blackpointArray != null)
					B= blackpointArray;
				
				if (gammaArray != null)
					G = gammaArray;
			
				currentColorData=new CalGrayColorSpace(W,B,G);
				
			} else if (ID==ColorSpaces.CalRGB) {
			
				float[] gammaArray=colorSpace.getFloatArray(PdfDictionary.Gamma);
				float[] blackpointArray=colorSpace.getFloatArray(PdfDictionary.BlackPoint);
				float[] whitepointArray=colorSpace.getFloatArray(PdfDictionary.WhitePoint);
				float[] matrixArray=colorSpace.getFloatArray(PdfDictionary.Matrix);
				
				if (whitepointArray != null)
		            W=whitepointArray;

				if (blackpointArray != null)
					B= blackpointArray;

				if (gammaArray != null)
					G = gammaArray;
				
				if (matrixArray != null)
					Ma = matrixArray;
				
			    currentColorData=new CalRGBColorSpace(W,B,Ma,G);
			
			} else if (ID==ColorSpaces.Lab) {
				
				float[] blackpointArray=colorSpace.getFloatArray(PdfDictionary.BlackPoint);
				float[] whitepointArray=colorSpace.getFloatArray(PdfDictionary.WhitePoint);
				float[] rangeArray=colorSpace.getFloatArray(PdfDictionary.Range);
				
				if (whitepointArray != null)
		            W=whitepointArray;

				if (blackpointArray != null)
					B= blackpointArray;

				if (rangeArray != null)
					R = rangeArray;
				
				currentColorData=new LabColorSpace(W,B,R);
				
            }else if (ID==ColorSpaces.ICC)
				currentColorData=new ICCColorSpace(colorSpace);
			else if (ID==ColorSpaces.Pattern)
			    currentColorData=new PatternColorSpace(isPrinting, currentPdfFile,colorspacesObjects);

			/**handle CMAP as object or direct*/
			if(isIndexed){				
				int size=rawSpace.getInt(PdfDictionary.hival);

				byte[] lookup=rawSpace.getDictionary(PdfDictionary.Lookup).getDecodedStream();
				currentColorData.setIndex(lookup,size);				
			}	
			
			currentColorData.setAlternateColorSpace(colorSpace.getParameterConstant(PdfDictionary.Alternate));
			
            //cache for later use
            //System.out.println(key+" "+currentColorData);
            
            if(currentColorData.getID()==ColorSpaces.ICC && colorspacesSeen!=null && cacheCol){

                boolean isAlreadySeen=colorspacesSeen.containsKey(key);

                //only cache on second find
				if(isAlreadySeen){
					colorspacesObjects.put(key,currentColorData);

                }else
                    colorspacesSeen.put(key,"x");

            }
            
			return currentColorData;
		}   
	}
}
