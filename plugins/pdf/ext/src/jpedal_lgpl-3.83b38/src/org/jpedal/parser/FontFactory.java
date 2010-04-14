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
* FontFactory.java
* ---------------
*/
package org.jpedal.parser;

import org.jpedal.fonts.PdfFont;
import org.jpedal.fonts.StandardFonts;
import org.jpedal.fonts.glyph.PdfGlyph;
//<start-jfr>
import org.jpedal.io.PdfObjectReader;
//<end-jfr>
import org.jpedal.objects.GraphicsState;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.utils.LogWriter;


public class FontFactory {

    //<start-jfr>
    public static PdfFont createFont(int fontType, PdfObjectReader currentPdfFile, String subFont) {

        switch(fontType){
            case StandardFonts.TYPE1:
                return new org.jpedal.fonts.Type1C(currentPdfFile,subFont);

            case StandardFonts.TRUETYPE:
                return new org.jpedal.fonts.TrueType(currentPdfFile,subFont);

            case StandardFonts.TYPE3:
                return new org.jpedal.fonts.Type3(currentPdfFile);
            
            case StandardFonts.CIDTYPE0:
                return new org.jpedal.fonts.CIDFontType0(currentPdfFile);

            case StandardFonts.CIDTYPE2:
                return new org.jpedal.fonts.CIDFontType2(currentPdfFile,subFont);

            default:


                //LogWriter.writeLog("Font type " + subtype + " not supported");
                return new PdfFont(currentPdfFile);
        }

    }
    //<end-jfr>

    public static PdfGlyph chooseGlyph(int glyphType, Object rawglyph) {
		
		if(glyphType==DynamicVectorRenderer.TYPE3){
			return (org.jpedal.fonts.glyph.T3Glyph)rawglyph;
		}else if(glyphType==DynamicVectorRenderer.TYPE1C){
			return (org.jpedal.fonts.glyph.T1Glyph)rawglyph;
		}else if(glyphType==DynamicVectorRenderer.TRUETYPE){
			return (org.jpedal.fonts.tt.TTGlyph)rawglyph;
		}else
			return null;
		
	}
}
