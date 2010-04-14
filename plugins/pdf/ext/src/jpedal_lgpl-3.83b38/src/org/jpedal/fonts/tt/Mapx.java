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
* Mapx.java
* ---------------
*/
package org.jpedal.fonts.tt;

public class Mapx extends Table {
	
	/**max number of glyphs*/
	private int glyphCount=0;
	
	public Mapx(FontFile2 currentFontFile){
	
		//LogWriter.writeMethod("{readMapxTable}", 0);
		
		//move to start and check exists
		int startPointer=currentFontFile.selectTable(FontFile2.MAPX);
		
		//read 'head' table
		if(startPointer!=0){
			
			currentFontFile.getNextUint32(); //id
			glyphCount=currentFontFile.getNextUint16();
			
		}
	}
	
	public int getGlyphCount(){
		return glyphCount;
	}
}
