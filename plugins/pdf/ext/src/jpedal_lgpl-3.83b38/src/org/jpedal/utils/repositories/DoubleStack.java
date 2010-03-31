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
* DoubleStack.java
* ---------------
*/
package org.jpedal.utils.repositories;

import org.jpedal.utils.repositories.Stack;

/**
 * provides a stack holding double values (primarily for type1 font renderer).
 * MUCH faster than Suns own stack
 */
public class DoubleStack implements Stack{
	
	/**default intial size*/
	private int size=100;
	
	private int nextSlot=0;
	
	private double[] elements=new double[size];
	
	public DoubleStack(int size){
		this.size=size;
		elements=new double[size];
	}

	/**
	 * take double value from stack
	 */
	public double pop() {
		if(nextSlot>0){
			nextSlot--;
			return elements[nextSlot];
		}else
			return 0;
	}
	
	/**
	 * get top double value from stack
	 */
	public double peek() {
		if(nextSlot>0)
			return elements[nextSlot-1];
		else 
			return 0;
	}

	/**
	 * add double value to stack
	 */
	public void push(double value) {
		
		//resize if needed*/
		if(nextSlot==size){
			
			if(size<1000){
				size=size*4;
			}else
				size=size+size;
			
			double[] temp = elements;
			elements = new double[size];
			
			System.arraycopy( temp, 0, elements, 0, temp.length );
			
		}
		
		elements[nextSlot]=value;
		
		nextSlot++;
		
	}

}
