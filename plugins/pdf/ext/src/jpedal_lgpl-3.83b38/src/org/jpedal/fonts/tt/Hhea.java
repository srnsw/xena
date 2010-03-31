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
* Hhea.java
* ---------------
*/
package org.jpedal.fonts.tt;

import org.jpedal.utils.LogWriter;


public class Hhea extends Table {
		
	private int numberOfHMetrics;
	
	public Hhea(FontFile2 currentFontFile){
	
		LogWriter.writeMethod("{readHheaTable}", 0);
		
		//move to start and check exists
		int startPointer=currentFontFile.selectTable(FontFile2.HHEA);
		
		//read 'head' table
		if(startPointer!=0){
			
		currentFontFile.getNextUint32(); //version
		currentFontFile.getFWord();//ascender
		currentFontFile.getFWord();//descender
		currentFontFile.getFWord();//lineGap
		currentFontFile.readUFWord();//advanceWidthMax
		currentFontFile.getFWord();//minLeftSideBearing
		currentFontFile.getFWord();//minRightSideBearing
		currentFontFile.getFWord();//xMaxExtent
		currentFontFile.getNextInt16();//caretSlopeRise
		currentFontFile.getNextInt16();//caretSlopeRun
		currentFontFile.getFWord();//caretOffset
		
		//reserved values
		for( int i = 0; i < 4; i++ )
			currentFontFile.getNextUint16();
		
		currentFontFile.getNextInt16();//metricDataFormat
		numberOfHMetrics =currentFontFile.getNextUint16();
		
		
		}
	}
	
	public int getNumberOfHMetrics(){
		return numberOfHMetrics;
	}
	
}
