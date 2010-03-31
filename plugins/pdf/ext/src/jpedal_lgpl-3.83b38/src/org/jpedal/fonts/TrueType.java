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
* TrueType.java
* ---------------
*/
package org.jpedal.fonts;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.awt.*;

import org.jpedal.exception.PdfFontException;
import org.jpedal.fonts.tt.TTGlyphs;
import org.jpedal.fonts.objects.FontData;
import org.jpedal.fonts.glyph.PdfJavaGlyphs;
//<start-jfr>
import org.jpedal.io.PdfObjectReader;
import org.jpedal.io.ObjectStore;
//<end-jfr>
import org.jpedal.utils.LogWriter;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;

/**
 * handles truetype specifics
 *  */
public class  TrueType extends PdfFont {

    private boolean subfontAlreadyLoaded;
    private Map fontsLoaded;

	public TrueType(){

	}

	public void readFontData(byte[] fontDataAsArray, FontData fontData){

		LogWriter.writeMethod("{readFontData}", 0);

        if(subfontAlreadyLoaded){
            glyphs= (PdfJavaGlyphs) fontsLoaded.get(substituteFont);

            fontTypes=glyphs.getType();
        }else{

            if(!isCIDFont)
            fontsLoaded.put(substituteFont,glyphs); 

		fontTypes=glyphs.readEmbeddedFont(TTstreamisCID,fontDataAsArray, fontData);
        }
        //does not see to be accurate on all PDFs tested
        //this.FontBBox=glyphs.getFontBoundingBox();

    }

	/**allows us to substitute a font to use for display
	 * @throws PdfFontException */
	protected void substituteFontUsed(String substituteFontFile,String substituteFontName) throws PdfFontException{

		InputStream from=null;

		//process the font data
		try {

            //try in jar first
            from =loader.getResourceAsStream("org/jpedal/res/fonts/" + substituteFontFile);

            //try as straight file
            if(from==null)
            from =new FileInputStream(substituteFontFile);

        } catch (Exception e) {
			System.err.println("Exception " + e + " reading "+substituteFontFile+" Check cid  jar installed");
			LogWriter.writeLog("Exception " + e + " reading "+substituteFontFile+" Check cid  jar installed");
		}

		if(from==null)
			throw new PdfFontException("Unable to load font "+substituteFontFile);

		try{

			//create streams
			ByteArrayOutputStream to = new ByteArrayOutputStream();

			//write
			byte[] buffer = new byte[65535];
			int bytes_read;
			while ((bytes_read = from.read(buffer)) != -1)
				to.write(buffer, 0, bytes_read);

			to.close();
			from.close();

            FontData fontData=null;//new FontData(to.toByteArray());

            readFontData(to.toByteArray(),fontData);

            glyphs.setEncodingToUse(hasEncoding,this.getFontEncoding(false),true,isCIDFont);

			isFontEmbedded=true;

		} catch (Exception e) {
			System.err.println("Exception " + e + " reading "+substituteFontFile+" Check cid  jar installed");
			LogWriter.writeLog("Exception " + e + " reading "+substituteFontFile+" Check cid  jar installed");
		}

	}

    /**entry point when using generic renderer*/
	public TrueType(String substituteFont) {

		glyphs=new TTGlyphs();
		
		fontsLoaded=new HashMap();

        //<start-jfr>
        init(null);
        /**
        //<end-jfr>
        //setup font size and initialise objects
		if(isCIDFont)
			maxCharCount=65536;

		glyphs.init(maxCharCount,isCIDFont);
        /**/

        this.substituteFont=substituteFont;

	}

    //<start-jfr>
	/**get handles onto Reader so we can access the file*/
	public TrueType(PdfObjectReader current_pdf_file,String substituteFont) {

			glyphs=new TTGlyphs();

			init(current_pdf_file);
			this.substituteFont=substituteFont;

	}

	/**read in a font and its details from the pdf file*/
	public void createFont(PdfObject pdfObject, String fontID, boolean renderPage, ObjectStore objectStore, Map substitutedFonts) throws Exception{

		LogWriter.writeMethod("{readTrueTypeFont}", 0);

		fontTypes=StandardFonts.TRUETYPE;

        this.fontsLoaded=substitutedFonts;

		//generic setup
		init(fontID, renderPage);
		
		/**
		 * get FontDescriptor object - if present contains metrics on glyphs
		 */
		PdfObject pdfFontDescriptor=pdfObject.getDictionary(PdfDictionary.FontDescriptor);

		setBoundsAndMatrix(pdfFontDescriptor);
		
		setName(pdfObject, fontID);
		setEncoding(pdfObject, pdfFontDescriptor);

		if(renderPage){

            if (pdfFontDescriptor!= null) {

                byte[] stream=null;
                PdfObject FontFile2=pdfFontDescriptor.getDictionary(PdfDictionary.FontFile2);
                if(FontFile2!=null)
                    stream=currentPdfFile.readStream(FontFile2,true,true,false, false,false, null);

                if(stream!=null)
                	readEmbeddedFont(stream,null,hasEncoding, false);

			}

			if(!isFontEmbedded && substituteFont!=null){

				//over-ride font remapping if substituted
				if(glyphs.remapFont)
					glyphs.remapFont=false;

                subfontAlreadyLoaded= !isCIDFont && fontsLoaded.containsKey(substituteFont);

                File fontFile;
                FontData fontData=null;
                int objSize=0;

				/**
				 * see if we cache or read
				 */
                if(!subfontAlreadyLoaded){
                    fontFile=new File(substituteFont);

				    objSize=(int)fontFile.length();
                }

				if(FontData.maxSizeAllowedInMemory>=0 && objSize>FontData.maxSizeAllowedInMemory){

                    if(!subfontAlreadyLoaded)
                    fontData=new FontData(substituteFont);

					readEmbeddedFont(null,fontData,false,true);
				}else if(subfontAlreadyLoaded){
                    readEmbeddedFont(null,null,false,true);
				}else{

					//read details
					BufferedInputStream from;

					InputStream jarFile = loader.getResourceAsStream(substituteFont);
					if(jarFile==null)
						from=new BufferedInputStream(new FileInputStream(substituteFont));
					else
						from= new BufferedInputStream(jarFile);

					//create streams
					ByteArrayOutputStream to = new ByteArrayOutputStream();

					//write
					byte[] buffer = new byte[65535];
					int bytes_read;
					while ((bytes_read = from.read(buffer)) != -1)
						to.write(buffer, 0, bytes_read);

					to.close();
					from.close();

					readEmbeddedFont(to.toByteArray(),null,false,true);
				}

				isFontSubstituted=true;

			}
		}

		readWidths(pdfObject,true);
		
		//make sure a font set
		if (renderPage)
			setFont(glyphs.fontName, 1);

        glyphs.setDiffValues(diffTable);

	}
    //<end-jfr>

    /**read in a font and its details for generic usage*/
	public void createFont(String fontName) throws Exception{

		fontTypes=StandardFonts.TRUETYPE;

		setBaseFontName(fontName);
		
		 /**
         * see if we cache or read
         */
        File fontFile=new File(substituteFont);
        
        int objSize=(int)fontFile.length();

		if(FontData.maxSizeAllowedInMemory>=0 && objSize>FontData.maxSizeAllowedInMemory){
        	FontData fontData=new FontData(substituteFont);

            readEmbeddedFont(null,fontData,false,true);
        }else{
			//read details
			BufferedInputStream from;
	
			InputStream jarFile = loader.getResourceAsStream(substituteFont);
			if(jarFile==null)
				from=new BufferedInputStream(new FileInputStream(substituteFont));
			else
				from= new BufferedInputStream(jarFile);
	
			//create streams
			ByteArrayOutputStream to = new ByteArrayOutputStream();
	
			//write
			byte[] buffer = new byte[65535];
			int bytes_read;
			while ((bytes_read = from.read(buffer)) != -1)
				to.write(buffer, 0, bytes_read);
	
			to.close();
			from.close();
	
	        readEmbeddedFont(to.toByteArray(),null,false,true);
        }
		
		isFontSubstituted=true;

        glyphs.setDiffValues(diffTable);

	}

	/**
	 * read truetype font data and also install font onto System
	 * so we can use
	 */
	final protected void readEmbeddedFont(byte[] fontDataAsArray, FontData fontDataAsObject,boolean hasEncoding,boolean isSubstituted) {

		LogWriter.writeMethod("{readEmbeddedFont}", 0);

		/**
		try {
			java.io.FileOutputStream fos=new java.io.FileOutputStream(getBaseFontName().substring(7)+fontDataAsArray.length+".ttf");
			fos.write(fontDataAsArray);
			fos.close();
			System.out.println(getBaseFontName().substring(7)+".ttf");

		} catch (Exception e1) {
			e1.printStackTrace();
		}/***/

		//process the font data
		try {

			LogWriter.writeLog("Embedded TrueType font used");

//			System.out.println("init font "+this.baseFontName);
//			javaFont=Font.createFont(Font.TRUETYPE_FONT,new ByteArrayInputStream(font_data));
//			System.out.println(javaFont);
//
			readFontData(fontDataAsArray,fontDataAsObject);

			isFontEmbedded=true;

            glyphs.setFontEmbedded(true);

            glyphs.setEncodingToUse(hasEncoding,this.getFontEncoding(false),isSubstituted,TTstreamisCID);

		} catch (Exception e) {
			LogWriter.writeLog("Exception " + e + " processing TrueType font");
			e.printStackTrace();
		}
	}

    /**
         * get bounding box to highlight
         * @return
         */
        public Rectangle getBoundingBox() {


            if(isFontEmbedded && !isFontSubstituted)
                return new Rectangle((int)FontBBox[0], (int)FontBBox[1], (int)(FontBBox[2]-FontBBox[0]), (int)(FontBBox[3]-FontBBox[1]));  //To change body of created methods use File | Settings | File Templates.
            else
                return super.getBoundingBox();
        }


}
