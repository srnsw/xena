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
* CIDFontType2.java
* ---------------
*/
package org.jpedal.fonts;

import java.util.Map;

import org.jpedal.fonts.tt.TTGlyphs;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.io.ObjectStore;
import org.jpedal.utils.LogWriter;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;


/**
 * handles truetype specifics
 *  */
public class CIDFontType2 extends TrueType {

	/**get handles onto Reader so we can access the file*/
	public CIDFontType2(PdfObjectReader currentPdfFile, String substituteFontFile) {

		isCIDFont=true;
		TTstreamisCID=true;

		glyphs=new TTGlyphs();

		init(currentPdfFile);

        this.substituteFontFile=substituteFontFile;

    }

	/**get handles onto Reader so we can access the file*/
	public CIDFontType2(PdfObjectReader currentPdfFile,boolean ttflag) {

		isCIDFont=true;
		TTstreamisCID=ttflag;

		glyphs=new TTGlyphs();
		
		init(currentPdfFile);

	}

	/**read in a font and its details from the pdf file*/
	public void createFont(PdfObject pdfObject, String fontID, boolean renderPage, ObjectStore objectStore, Map substitutedFonts) throws Exception{

		LogWriter.writeMethod("{readFontType0 "+fontID+ '}', 0);

		fontTypes=StandardFonts.CIDTYPE2;
		this.fontID=fontID;
		
		PdfObject Descendent=pdfObject.getDictionary(PdfDictionary.DescendantFonts);
        PdfObject pdfFontDescriptor=Descendent.getDictionary(PdfDictionary.FontDescriptor);

		createCIDFont(pdfObject,Descendent);

		if (pdfFontDescriptor!= null) {

            byte[] stream=null;
            PdfObject FontFile2=pdfFontDescriptor.getDictionary(PdfDictionary.FontFile2);
            if(FontFile2!=null){
                stream=currentPdfFile.readStream(FontFile2,true,true,false, false,false, null);

            	if(stream!=null)
            		readEmbeddedFont(stream,null,hasEncoding, false);
			}
		}

		

		//setup and substitute font
        if(renderPage && !isFontEmbedded && substituteFontFile!=null){
            this.substituteFontUsed(substituteFontFile,substituteFontName);
			isFontSubstituted=true;
			this.isFontEmbedded=true;

            glyphs.setFontEmbedded(true);
        }

			//make sure a font set
			if (renderPage)
				setFont(getBaseFontName(), 1);
			
			if(!isFontEmbedded)
				selectDefaultFont();

	}
}
