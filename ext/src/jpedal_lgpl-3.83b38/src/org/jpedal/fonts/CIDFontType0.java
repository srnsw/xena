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
* CIDFontType0.java
* ---------------
*/
package org.jpedal.fonts;

import java.util.Map;

import org.jpedal.fonts.glyph.PdfJavaGlyphs;
import org.jpedal.fonts.glyph.T1Glyphs;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.io.ObjectStore;
import org.jpedal.utils.LogWriter;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;


/**
 * handles truetype specifics
 *  */
public class CIDFontType0 extends Type1C {

	/**used to display non-embedded fonts*/
	private CIDFontType2 subFont=null;

	/**get handles onto Reader so we can access the file*/
	public CIDFontType0(PdfObjectReader currentPdfFile) {

		glyphs=new T1Glyphs(true);

		isCIDFont=true;
		TTstreamisCID=true;
		init(currentPdfFile);
		this.currentPdfFile=currentPdfFile;

	}

	/**read in a font and its details from the pdf file*/
	public void createFont(PdfObject pdfObject, String fontID, boolean renderPage, ObjectStore objectStore, Map substitutedFonts) throws Exception{

		LogWriter.writeMethod("{readCIDFONT0 "+fontID+ '}', 0);

		fontTypes=StandardFonts.CIDTYPE0;
		this.fontID=fontID;
		
		PdfObject Descendent=pdfObject.getDictionary(PdfDictionary.DescendantFonts);
        PdfObject pdfFontDescriptor=Descendent.getDictionary(PdfDictionary.FontDescriptor);
       
        createCIDFont(pdfObject,Descendent);

		if (pdfFontDescriptor!= null){
			
			if(pdfFontDescriptor!=null){
                float[] newFontBBox=pdfFontDescriptor.getFloatArray(PdfDictionary.FontBBox);
                if(newFontBBox!=null)
                FontBBox=newFontBBox;
            }
         
			readEmbeddedFont(pdfFontDescriptor);
		}
		
		if(renderPage && !isFontEmbedded && substituteFontFile!=null){

			isFontSubstituted=true;
			subFont=new CIDFontType2(currentPdfFile,TTstreamisCID);

			subFont.substituteFontUsed(substituteFontFile,substituteFontName);
			this.isFontEmbedded=true;

            glyphs.setFontEmbedded(true);
        }

		if(!isFontEmbedded)
			selectDefaultFont();

		//make sure a font set
		if (renderPage)
			setFont(getBaseFontName(), 1);
		
	}


	/**
	 * used by  non type3 font
	 */
	public PdfJavaGlyphs getGlyphData(){

		if(subFont!=null)
			return subFont.getGlyphData();
		else
			return glyphs;
		
	}

}
