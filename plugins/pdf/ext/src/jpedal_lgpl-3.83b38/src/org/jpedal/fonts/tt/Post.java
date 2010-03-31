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
* Post.java
* ---------------
*/
package org.jpedal.fonts.tt;

import java.util.HashMap;
import java.util.Map;

import org.jpedal.utils.LogWriter;


public class Post extends Table {
	
	/**map glyphs onto ID values in font*/
	Map translateToID=new HashMap();
	
	final private String[] macEncoding = { ".notdef", ".null",
			"nonmarkingreturn", "space", "exclam", "quotedbl", "numbersign",
			"dollar", "percent", "ampersand", "quotesingle", "parenleft",
			"parenright", "asterisk", "plus", "comma", "hyphen", "period",
			"slash", "zero", "one", "two", "three", "four", "five", "six",
			"seven", "eight", "nine", "colon", "semicolon", "less", "equal",
			"greater", "question", "at", "A", "B", "C", "D", "E", "F", "G",
			"H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
			"U", "V", "W", "X", "Y", "Z", "bracketleft", "backslash",
			"bracketright", "asciicircum", "underscore", "grave", "a", "b",
			"c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o",
			"p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "braceleft",
			"bar", "braceright", "asciitilde", "Adieresis", "Aring",
			"Ccedilla", "Eacute", "Ntilde", "Odieresis", "Udieresis", "aacute",
			"agrave", "acircumflex", "adieresis", "atilde", "aring",
			"ccedilla", "eacute", "egrave", "ecircumflex", "edieresis",
			"iacute", "igrave", "icircumflex", "idieresis", "ntilde", "oacute",
			"ograve", "ocircumflex", "odieresis", "otilde", "uacute", "ugrave",
			"ucircumflex", "udieresis", "dagger", "degree", "cent", "sterling",
			"section", "bullet", "paragraph", "germandbls", "registered",
			"copyright", "trademark", "acute", "dieresis", "notequal", "AE",
			"Oslash", "infinity", "plusminus", "lessequal", "greaterequal",
			"yen", "mu", "partialdiff", "summation", "product", "pi",
			"integral", "ordfeminine", "ordmasculine", "Omega", "ae", "oslash",
			"questiondown", "exclamdown", "logicalnot", "radical", "florin",
			"approxequal", "Delta", "guillemotleft", "guillemotright",
			"ellipsis", "nonbreakingspace", "Agrave", "Atilde", "Otilde", "OE",
			"oe", "endash", "emdash", "quotedblleft", "quotedblright",
			"quoteleft", "quoteright", "divide", "lozenge", "ydieresis",
			"Ydieresis", "fraction", "currency", "guilsinglleft",
			"guilsinglright", "fi", "fl", "daggerdbl", "periodcentered",
			"quotesinglbase", "quotedblbase", "perthousand", "Acircumflex",
			"Ecircumflex", "Aacute", "Edieresis", "Egrave", "Iacute",
			"Icircumflex", "Idieresis", "Igrave", "Oacute", "Ocircumflex",
			"apple", "Ograve", "Uacute", "Ucircumflex", "Ugrave", "dotlessi",
			"circumflex", "tilde", "macron", "breve", "dotaccent", "ring",
			"cedilla", "hungarumlaut", "ogonek", "caron", "Lslash", "lslash",
			"Scaron", "scaron", "Zcaron", "zcaron", "brokenbar", "Eth", "eth",
			"Yacute", "yacute", "Thorn", "thorn", "minus", "multiply",
			"onesuperior", "twosuperior", "threesuperior", "onehalf",
			"onequarter", "threequarters", "franc", "Gbreve", "gbreve",
			"Idotaccent", "Scedilla", "scedilla", "Cacute", "cacute", "Ccaron",
			"ccaron", "dcroat" }; 
	
	public Post(FontFile2 currentFontFile){
	
		
		LogWriter.writeMethod("{readPostTable}", 0);
		
		//move to start and check exists
		int startPointer=currentFontFile.selectTable(FontFile2.POST);
		//System.out.println(startPointer);
		//read 'head' table
		if(startPointer==0)
			LogWriter.writeLog("No Post table found");
		else{
			
			//read format (actually 1,2,2.5 but multiplied so switch works
			int id=(int)(10*currentFontFile.getFixed());
			
			//System.out.println("id="+id);
			
			//for(int i=0;i<)
			//read rest of table which we ignore
			currentFontFile.getFixed();// 	italicAngle  	Italic angle in degrees
			currentFontFile.getFWord();//FWord 	underlinePosition 	Underline position
			currentFontFile.getFWord();//FWord 	underlineThickness 	Underline thickness
			currentFontFile.getNextUint16();//uint16 	isFixedPitch 	Font is monospaced; set to 1 if the font is monospaced and 0 otherwise
			currentFontFile.getNextUint16();//uint16 	reserved 	Reserved, set to 0
			currentFontFile.getNextUint32();//uint32 	minMemType42 	Minimum memory usage when a TrueType font is downloaded as a Type 42 font
			currentFontFile.getNextUint32();//uint32 	maxMemType42 	Maximum memory usage when a TrueType font is downloaded as a Type 42 font
			currentFontFile.getNextUint32();//uint32 	minMemType1 	Minimum memory usage when a TrueType font is downloaded as a Type 1 font
			currentFontFile.getNextUint32();//uint32 	maxMemType1 	Maximum memory usage when a TrueType font is downloaded as a Type 1 font
			
			/**
			 * create lookup table for mac format
			 */
			int numberOfGlyphs;

            //standard mac os table
			if(id!=30){
				for(int i=0;i<258;i++)
					this.translateToID.put(this.macEncoding[i],new Integer(i));
			}
			
			switch(id){
			
				case 20:
					numberOfGlyphs=currentFontFile.getNextUint16();
					
					int[] glyphNameIndex=new int[numberOfGlyphs];
					int numberOfNewGlyphs=0;
					
					/**read glyphs and work out how many strings*/
					for(int i=0;i<numberOfGlyphs;i++){
						glyphNameIndex[i]=currentFontFile.getNextUint16();
						if(glyphNameIndex[i]>257 && glyphNameIndex[i]<32768)
							numberOfNewGlyphs++;
					}
					
					/**now read the strings*/
					String[] names=new String[numberOfNewGlyphs];
					for(int i=0;i<numberOfNewGlyphs;i++){
						names[i]=currentFontFile.getString();
                        //System.out.println("names["+i+"]="+names[i]);
                    }
					/**now add in non-standard strings*/
					for(int i=0;i<numberOfGlyphs;i++){
                       // System.out.println("i="+i+"/"+numberOfGlyphs);
                        if(glyphNameIndex[i]>257 && glyphNameIndex[i]<32768){
							this.translateToID.put(names[glyphNameIndex[i]-258],new Integer(i));
                            //System.out.println(i+"="+names[glyphNameIndex[i]-258]);
                        }
                    }
						
					break;
					
				case 25:
					
					numberOfGlyphs=currentFontFile.getNextUint16();
					
					int[] glyphOffset=new int[numberOfGlyphs];
					
					/**read glyphs and work out how many strings*/
					for(int i=0;i<numberOfGlyphs;i++){
						glyphOffset[i]=currentFontFile.getNextint8();
						translateToID.put(macEncoding[glyphOffset[i]+i],new Integer(glyphOffset[i]));
					}
					
					break;
			}
		}
	}

	/**
	 * lookup glyph in post table (return -1 if no match
	 */
	public int convertGlyphToCharacterCode(String glyph) {

		Integer newID=(Integer) translateToID.get(glyph);
		
		if(newID==null)
			return 0;
		else
			return newID.intValue();
	}
}
