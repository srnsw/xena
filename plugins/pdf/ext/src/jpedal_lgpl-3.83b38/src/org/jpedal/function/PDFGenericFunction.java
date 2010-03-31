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
* PDFGenericFunction.java
* ---------------
*/

package org.jpedal.function;

import java.io.Serializable;

/**
 * Contains code which are used by multiple Shading Classes
 */
public class PDFGenericFunction implements Serializable {

	/**
	 * values found in Function using names used in PDF reference
	 */
	protected float[] domain,encode,decode,range;
	
	
	public PDFGenericFunction(float[] domain, float[] range) {
		
		this.range=range;
		this.domain=domain;
		
	}

	/**
	 * Preform interpolation on the given values.
	 * @param x : current X value
	 * @param xmin : lowest x value
	 * @param xmax : highest x value
	 * @param ymin : lowest y value
	 * @param ymax : highest y value
	 * @return y value for the x value
	 */
	static float interpolate(float x,float xmin,float xmax, float ymin, float ymax){
		
		return ((x-xmin)*(ymax-ymin)/(xmax-xmin))+ymin;
		
	}

	/**
	 * Return the lowest of the input vairbles
	 * @param a : value 1 to check
	 * @param b : value 2 to check
	 * @return The lowest of the input values
	 */
	static float min(float a, float b) {

		if(a>b)
			return b;
		else
			return a;

	}
	
	/**
	 * Return the highest of the input vairbles
	 * @param a : value 1 to check
	 * @param b : value 2 to check
	 * @return The highest of the input values
	 */
	static float max(float a, float b) {

		if(a<b)
			return b;
		else
			return a;

	}
}
