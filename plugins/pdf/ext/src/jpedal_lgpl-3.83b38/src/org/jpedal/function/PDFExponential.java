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
* PDFExponential.java
* ---------------
*/
package org.jpedal.function;

/**
 * Class to handle Type 2 shading (Exponential)
 */
public class PDFExponential extends PDFGenericFunction implements PDFFunction {
	
	private float[] C0={0.0f}, C1={1.0f};
	
	private float N;
	
	int returnValues;
	
	public PDFExponential(float N, float[] C0,float[] C1,float[] domain, float[] range){
		
		super(domain, range);
		
		this.N=N;
		
		if (C0!=null)
			this.C0=C0;
		
		if (C1!=null)
			this.C1=C1;

        //note C0 might be null so use this.C0
        returnValues=this.C0.length;

    }
	
	
	/**
	 * Compute the required values for exponential shading (Only used by Stitching)
	 * @param subinput : input values
	 * @return The shading values as float[]
	 */
	public float[] computeStitch(float[] subinput) {
		return compute(subinput);
		
	}
	
	public float[] compute(float[] values) {

		float[] output=new float[returnValues];
		float[] result=new float[returnValues];

		//Only first value required
		float x=min(max(values[0],domain[0*2]),domain[0*2+1]); 
		
		if (N==1f){// special case
			
			for (int i=0; i<C0.length; i++){
				//x^1 = x so don't bother finding the power
				output[i] = C0[i] + x * (C1[i]-C0[i]); 
				
				//clip to range if present
				if (range!=null)
					output[i] = min(max(output[i],range[i*2]),range[i*2+1]); //Clip output
				
				result[i]=output[i];
				
			}
		}else{
			for (int i=0; i<C0.length; i++){
				output[i] = C0[i] + (float)Math.pow(x, N) * (C1[i]-C0[i]);
				
				//clip to range if present.
				if (range!=null)
					output[i] = min(max(output[i],range[i*2]),range[i*2+1]); //Clip output
				
				result[i]=output[i];
				
			}
		}

		return result;
	}
}
