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
* FunctionFactory.java
* ---------------
*/
package org.jpedal.function;

import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.FunctionObject;
import org.jpedal.objects.raw.PdfObject;

/**
 * return the correct PDF function - all implement PDF function interface
 */
public class FunctionFactory {

	/**
	 * Get the correct Function for the shading objct
	 * @param stream : Shading input data
	 * @param domain :: The input value limits
	 * @param range :: The ouput value limits
	 * @param type :: Shading type
	 * @param currentPdfFile :: Current PDF File data
	 * @return The required function
	 */
	public static PDFFunction getFunction(PdfObject functionObj, PdfObjectReader currentPdfFile) {

		PDFFunction newFunction=null;

		/**
		 * get values and handle reference
		 **/
        byte[] stream=functionObj.getDecodedStream();

        float[] domain=functionObj.getFloatArray(PdfDictionary.Domain);
        float[] range=functionObj.getFloatArray(PdfDictionary.Range);

        int type =functionObj.getInt(PdfDictionary.FunctionType);
        int bits=functionObj.getInt(PdfDictionary.BitsPerSample);
		float[] decode=functionObj.getFloatArray(PdfDictionary.Decode);
		float[] C0=functionObj.getFloatArray(PdfDictionary.C0);
		float[] C1=functionObj.getFloatArray(PdfDictionary.C1);
		int[] size=functionObj.getIntArray(PdfDictionary.Size);
		float[] encode=functionObj.getFloatArray(PdfDictionary.Encode);
		float[] bounds=functionObj.getFloatArray(PdfDictionary.Bounds);

		//set order
		float N=0,newN=functionObj.getFloatNumber(PdfDictionary.N);
		if(newN!=-1)
			N=newN;

		byte[][] keys=functionObj.getKeyArray(PdfDictionary.Functions);
		int functionCount=0;
		if(keys!=null)
			functionCount=keys.length;

		PDFFunction[] functions=null;
		PdfObject function;
		if(keys!=null){
			
			PdfObject[] subFunction=new PdfObject[functionCount];
			
			String id;
			for(int i=0;i<functionCount;i++){
				
				id=new String(keys[i]);

                if(id.startsWith("<<")){
                    function=new FunctionObject(1);
                    currentPdfFile.readDictionaryAsObject(function,"",0,keys[i], keys[i].length,"", true);           
                }else{
                    function=new FunctionObject(id);
				    currentPdfFile.readObject(function);
                }
                subFunction[i]=function;
			}

			functions = new PDFFunction[subFunction.length];

            /**
             * get values for sub stream Function
             */
            for (int i1 =0,imax= subFunction.length; i1 <imax; i1++)
                functions[i1] = FunctionFactory.getFunction(subFunction[i1], currentPdfFile);

		}

		switch (type) {
		case 0 :

			newFunction=new PDFSampled(stream,bits, domain, range, encode, decode, size);
			break;
		case 2 :
			newFunction=new PDFExponential(N,C0, C1, domain,range);
			break;
		case 3 :
			newFunction = new PDFStitching(functions, encode, bounds, domain,range);
			break;
		case 4 :
			newFunction=new PDFCalculator(stream,domain,range);
			break;
		}


		return newFunction;
	}

	
}
