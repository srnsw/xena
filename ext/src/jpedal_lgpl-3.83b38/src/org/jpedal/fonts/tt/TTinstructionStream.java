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
* TTinstructionStream.java
* ---------------
*/
package org.jpedal.fonts.tt;

/**
 * holds stream and provides easy access
 */
public class TTinstructionStream {
	
	//instruction stream data
	private int[] data;
	
	//curent byte
	private int pointer=0;

	public TTinstructionStream(int[] data){
		this.data=data;
	}
	
	public int getInt(){
		return  (data[pointer++] & 0xff);
	}
	
	public int getWord(){
		int result = 0;
		for (int i = 0; i < 4; i++) {
			result = (result << 8) + (data[pointer++] & 0xff);

		}
		return result;
		
	}
	
	public int get16Word(){
		int result =((data[pointer] & 0xff) << 8) + (data[pointer+1] & 0xff);

		pointer=pointer+2;
		return result;
		
	}
	
	public double getF26Dot6(){
		int result = 0;
		for (int i = 0; i < 4; i++) {
			result = (result << 8) + (data[pointer++] & 0xff);

		}
		
		return ((result>>6))+((result & 63)/64f);
		
	}

}
