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
* CFF.java
* ---------------
*/
package org.jpedal.fonts.tt;

import org.jpedal.utils.LogWriter;
import org.jpedal.fonts.Type1C;
import org.jpedal.fonts.objects.FontData;
import org.jpedal.fonts.glyph.PdfGlyph;
import org.jpedal.fonts.glyph.PdfJavaGlyphs;
import org.jpedal.fonts.glyph.T1Glyphs;
import org.jpedal.fonts.glyph.GlyphFactory;

public class CFF extends Table {

    Type1C cffData;

    PdfJavaGlyphs glyphs;

    boolean hasCFFdata=false;

    public CFF(FontFile2 currentFontFile,boolean isCID){

		LogWriter.writeMethod("{readCFFTable}", 0);

        glyphs=new T1Glyphs(isCID);
        if(isCID)
        glyphs.init(65536,true);
        
        //move to start and check exists
		int startPointer=currentFontFile.selectTable(FontFile2.CFF);

        //read 'cff' table
		if(startPointer!=0){

            int length=currentFontFile.getTableSize(FontFile2.CFF);

            byte[] data=currentFontFile.readBytes(startPointer, length) ;

            //FontData fontData=new FontData(data);
            try {
                cffData=new Type1C(data,null,glyphs);

                hasCFFdata=true;
            } catch (Exception e) {
                e.printStackTrace(); 
            }
		}
    }

    public boolean hasCFFData() {
        return hasCFFdata;
    }


    public PdfGlyph getCFFGlyph(GlyphFactory factory,String glyph, float[][] Trm,int rawInt, String displayValue, float currentWidth,String key) {

        return glyphs.getEmbeddedGlyph(factory, glyph, Trm, rawInt, displayValue, currentWidth, key);

    }
}
