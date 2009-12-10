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
* PdfColor.java
* ---------------
*/
package org.jpedal.color;

import java.awt.Color;
import java.awt.Paint;

/**
 * template for all shading operations
 */
public class PdfColor extends Color implements PdfPaint,Paint{
	
	public PdfColor(float r, float g, float b) {
		super(r, g, b);
	}
	
	public PdfColor(int r, int g, int b) {
		super(r, g, b);
	}
	
	public PdfColor(int r, int g, int b,int a) {
		super(r, g, b,a);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected boolean isPattern=false;
	
	float scaling=1f;

	private int cropX;

	private int cropH;
	
	public void setScaling(double cropX,double cropH,float scaling, float textX, float textY){
		this.scaling=scaling;
		this.cropX=(int)cropX;
		this.cropH=(int)cropH;
	}
	
	public boolean isPattern(){
		return isPattern;
	}
	
	//constructor for pattern color
	public void setPattern(int dummy){
		isPattern=true;
	}

}
