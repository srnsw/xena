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
* ShadingFactory.java
* ---------------
*/
package com.idrsolutions.pdf.color.shading;

import org.jpedal.color.GenericColorSpace;
import org.jpedal.color.PdfColor;
import org.jpedal.color.PdfPaint;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.objects.raw.PdfDictionary;


/**
 * provides factory method to decode 
 * shading into required value
 */
public class ShadingFactory {
	/**
	 * setup shading object (matrix is used in Pattern as optional value but not in sh()
	 */
	public static PdfPaint createShading(PdfObject Shading, boolean isPrinting, int pageY,GenericColorSpace shadingColorSpace, PdfObjectReader currentPdfFile,float[][] matrix,int pageHeight, boolean colorsReversed){
		
        int shadingType=Shading.getInt(PdfDictionary.ShadingType);

		/**
		 * create shading object
		 */
		if(shadingType==ShadedPaint.AXIAL){
//			SimpleAxialPaint ap=new SimpleAxialPaint(values, currentPdfFile,matrix);
//			return ap.generatePaint();
			
			return new ShadedPaint(Shading, isPrinting,pageY,shadingType,shadingColorSpace, currentPdfFile,matrix,pageHeight,colorsReversed);
		}else if(shadingType==ShadedPaint.RADIAL)
			return new ShadedPaint(Shading, isPrinting,pageY,shadingType,shadingColorSpace, currentPdfFile,matrix,pageHeight,colorsReversed);
        else if(shadingType==ShadedPaint.COONS){ // not all at present

            //if(Shading.getInt(PdfDictionary.BitsPerComponent)==8 && Shading.getInt(PdfDictionary.BitsPerComponent)==8)
                return new ShadedPaint(Shading, isPrinting,pageY,shadingType,shadingColorSpace, currentPdfFile,matrix,pageHeight,colorsReversed);
            //else{

                
           //     return new PdfColor(1.0f,1.0f,1.0f);
           // }
        }else
			if(shadingType==ShadedPaint.TENSOR){
/**
				byte[] stream=Shading.DecodedStream;
	            
	            //have not verified stream is correctly decoded with new code
			
            TensorContext tc = new TensorContext(stream);//(isPrinting,pageY,shadingType,values, currentPdfFile,matrix,pageHeight,colorsReversed);
/**/
            //			return new ShadedPaint(isPrinting,pageY,shadingType,values, currentPdfFile,matrix,pageHeight,colorsReversed);
		} //else
			return new PdfColor(1.0f,1.0f,1.0f);
	}
	
	
}
