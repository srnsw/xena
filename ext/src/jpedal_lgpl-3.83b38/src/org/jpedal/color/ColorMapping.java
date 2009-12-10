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
* ColorMapping.java
* ---------------
*/
package org.jpedal.color;

import java.io.Serializable;

import org.jpedal.function.FunctionFactory;
import org.jpedal.function.PDFFunction;
import org.jpedal.io.PdfObjectReader;

import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;

public class ColorMapping implements Serializable {
	
	private PDFFunction function;
	
    private int functionType;
	
	public ColorMapping (PdfObjectReader currentPdfFile, PdfObject functionObj) {

        //needed for this class
        functionType =functionObj.getInt(PdfDictionary.FunctionType);

		/** setup the translation function */
		function = FunctionFactory.getFunction(functionObj, currentPdfFile);

	}

	public float[] getOperandFloat(float[] values){
	
		return function.compute(values);
			
	}
	
	public int getFunctionType(){
		
		return functionType;
			
	}
}
