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
* PDFCalculator.java
* ---------------
*/

package org.jpedal.function;

/**
 * Class to handle Type 4 shading (PostScript Calculator) from a Pdf
 */
public class PDFCalculator extends PDFGenericFunction implements PDFFunction {

	int returnValues;

	byte[] stream;

	public PDFCalculator(byte[] stream, float[] domain, float[] range) {

		super(domain, range);

		returnValues=range.length/2;

		/**
		 * set stream
		 */
		this.stream=stream; //raw data

	}

	/**
	 * Calculate the output values for this point in the shading object. (Only used by Stitching)
	 * @return returns the shading values for this point
	 */
	public float[] computeStitch(float[] subinput) {
		return compute(subinput);
		
	}
	
	public float[] compute(float[] values) {
		

		float[] output=new float[returnValues];
		float[] result=new float[returnValues];


		try{
			PostscriptFactory post=new PostscriptFactory(stream);

			post.resetStacks(values);
			double[] stack=post.executePostscript();

			if((domain.length / 2)==1){
				for (int i=0,imax=range.length / 2; i<imax; i++){
					output[i] = (float)(stack[i]);   // take output from stack
					result[i]=min(max(output[i],range[i*2]),range[i*2+1]);
				}
			}else{
				for (int i=0,imax=range.length / 2; i<imax; i++){
					output[i] = (float)(stack[i]);   // take output from stack
					result[i]=min(max(output[i],range[i*2]),range[i*2+1]);
				}
			}
		
		}catch(Exception e){
		}
		
		return result;
	}
}
