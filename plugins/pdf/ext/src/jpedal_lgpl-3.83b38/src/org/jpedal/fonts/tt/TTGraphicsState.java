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
* TTGraphicsState.java
* ---------------
*/
package org.jpedal.fonts.tt;

/**
 * holds the graphics state variables
 */
public class TTGraphicsState {
	
	public final static int x_axis=0;

	public boolean autoFlip=true;
	
	public double cutIn=17/16;
	
	public double deltaBase=9;
	
	public double deltaWidth=3;
	
	public double dualProjectionVector=0;
	
	public int freedomVector=x_axis;
	
	public int instructControl=0;
	
	public int loop=1;
	
	public float minimumDistance=1;
	
	public int projectionVector=x_axis;
	
	public int roundState=1;
	
	public int rp0=0;
	
	public int rp1=0;
	
	public int rp2=0;
	
	public boolean scanControl=false;
	
	public float singleWidthCutIn=0;
	
	public float singleWidthValue=0;
	
	public int zp0=1;
	
	public int zp1=1;
	
	public int zp2=1;
	
}
