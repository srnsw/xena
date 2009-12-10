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
* Glyf.java
* ---------------
*/
package org.jpedal.fonts.tt;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.jpedal.utils.LogWriter;


public class Glyf extends Table {
	
	/**holds mappings for drawing the glpyhs*/
	private Hashtable charStrings=new Hashtable();
	
	/**holds list of empty glyphs*/
	private Map emptyCharStrings=new HashMap();
    private byte[] glyphTable;
	
	public Glyf(FontFile2 currentFontFile,int glyphCount,int[] glyphIndexStart){
	
		LogWriter.writeMethod("{readGlyfTable}", 0);
		
		//move to start and check exists
		int startPointer=currentFontFile.selectTable(FontFile2.LOCA);
		
		//read  table
		if(startPointer!=0){
			
			//read each gyf
			for(int i=0;i<glyphCount;i++){
				
				//just store in lookup table or flag as zero length
				if((glyphIndexStart[i]==glyphIndexStart[i+1])){
					charStrings.put(new Integer(i),new Integer(-1));
					emptyCharStrings.put(new Integer(i),"x");
				}else{
					charStrings.put(new Integer(i),new Integer(glyphIndexStart[i]));
				}
			}
            
            //read the actual glyph data
            glyphTable=currentFontFile.getTableBytes(FontFile2.GLYF);
                    
		}
	}

	public boolean isPresent(int glyph){
		
		Integer key=new Integer(glyph);
		
        Object value=charStrings.get(key);

        return value!=null && emptyCharStrings.get(key)==null;
	}
	
	public int getCharString(int glyph){
		
        Object value=charStrings.get(new Integer(glyph));


        if(value==null)
			return glyph;
		else
			return ((Integer) value).intValue();
	}
	
    public byte[] getTableData() {
        return glyphTable;
    }
}
