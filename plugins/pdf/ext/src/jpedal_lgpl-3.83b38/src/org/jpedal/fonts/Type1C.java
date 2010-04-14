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
* Type1C.java
* ---------------
*/
package org.jpedal.fonts;

import java.io.*;
import java.util.Map;
import java.awt.*;

import org.jpedal.fonts.glyph.T1Glyphs;
import org.jpedal.fonts.glyph.PdfJavaGlyphs;
import org.jpedal.fonts.objects.FontData;

//<start-jfr>
import org.jpedal.io.PdfObjectReader;
import org.jpedal.io.ObjectStore;
import org.jpedal.parser.PdfStreamDecoder;
//<end-jfr>

import org.jpedal.utils.LogWriter;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.objects.raw.PdfDictionary;


/**
 * handlestype1 specifics
 */
public class Type1C extends Type1{

	static final boolean  debugFont=false;
	
	static final boolean debugDictionary=false;
	
	int ROS=-1,CIDFontVersion=0,CIDFontRevision=0,CIDFontType=0,CIDcount=0,UIDBase=-1,FDArray=-1,FDSelect=-1;
	
	final String[] OneByteCCFDict={"version","Notice","FullName","FamilyName","Weight",
			"FontBBox","BlueValues","OtherBlues","FamilyBlues","FamilyOtherBlues",
			"StdHW","StdVW","Escape","UniqueID","XUID",
			"charset","Encoding","CharStrings","Private", "Subrs",
			"defaultWidthX","nominalWidthX","-reserved-","-reserved-","-reserved-",
			"-reserved-","-reserved-","-reserved-","shortint","longint",
			"BCD","-reserved-"};
	
	final String[] TwoByteCCFDict={"Copyright","isFixedPitch","ItalicAngle","UnderlinePosition","UnderlineThickness",
			"PaintType","CharstringType","FontMatrix","StrokeWidth","BlueScale",
			"BlueShift","BlueFuzz","StemSnapH","StemSnapV","ForceBold",
			"-reserved-","-reserved-","LanguageGroup","ExpansionFactor","initialRandomSeed",
			"SyntheticBase","PostScript","BaseFontName","BaseFontBlend","-reserved-",
			"-reserved-","-reserved-","-reserved-","-reserved-","-reserved-",
			"ROS","CIDFontVersion","CIDFontRevision","CIDFontType","CIDCount",
			"UIDBase","FDArray","FDSelect","FontName"};
	
	//current location in file
	private int top = 0;

	private int charset = 0;

	private int enc = 0;

	private int charstrings = 0;
	
	private int stringIdx;

	private int stringStart;

	private int stringOffSize;
	
	private boolean hasFontBBox=false,hasFontMatrix=false;

	private int privateDict = -1,privateDictOffset=-1;

	/** decoding table for Expert */
	private static final int ExpertSubCharset[] = { // 87
																					  // elements
		0,
			1,
			231,
			232,
			235,
			236,
			237,
			238,
			13,
			14,
			15,
			99,
			239,
			240,
			241,
			242,
			243,
			244,
			245,
			246,
			247,
			248,
			27,
			28,
			249,
			250,
			251,
			253,
			254,
			255,
			256,
			257,
			258,
			259,
			260,
			261,
			262,
			263,
			264,
			265,
			266,
			109,
			110,
			267,
			268,
			269,
			270,
			272,
			300,
			301,
			302,
			305,
			314,
			315,
			158,
			155,
			163,
			320,
			321,
			322,
			323,
			324,
			325,
			326,
			150,
			164,
			169,
			327,
			328,
			329,
			330,
			331,
			332,
			333,
			334,
			335,
			336,
			337,
			338,
			339,
			340,
			341,
			342,
			343,
			344,
			345,
			346 };


	/** lookup table for names for type 1C glyphs */
	private static final String type1CStdStrings[] = { // 391
																					   // elements
		".notdef",
			"space",
			"exclam",
			"quotedbl",
			"numbersign",
			"dollar",
			"percent",
			"ampersand",
			"quoteright",
			"parenleft",
			"parenright",
			"asterisk",
			"plus",
			"comma",
			"hyphen",
			"period",
			"slash",
			"zero",
			"one",
			"two",
			"three",
			"four",
			"five",
			"six",
			"seven",
			"eight",
			"nine",
			"colon",
			"semicolon",
			"less",
			"equal",
			"greater",
			"question",
			"at",
			"A",
			"B",
			"C",
			"D",
			"E",
			"F",
			"G",
			"H",
			"I",
			"J",
			"K",
			"L",
			"M",
			"N",
			"O",
			"P",
			"Q",
			"R",
			"S",
			"T",
			"U",
			"V",
			"W",
			"X",
			"Y",
			"Z",
			"bracketleft",
			"backslash",
			"bracketright",
			"asciicircum",
			"underscore",
			"quoteleft",
			"a",
			"b",
			"c",
			"d",
			"e",
			"f",
			"g",
			"h",
			"i",
			"j",
			"k",
			"l",
			"m",
			"n",
			"o",
			"p",
			"q",
			"r",
			"s",
			"t",
			"u",
			"v",
			"w",
			"x",
			"y",
			"z",
			"braceleft",
			"bar",
			"braceright",
			"asciitilde",
			"exclamdown",
			"cent",
			"sterling",
			"fraction",
			"yen",
			"florin",
			"section",
			"currency",
			"quotesingle",
			"quotedblleft",
			"guillemotleft",
			"guilsinglleft",
			"guilsinglright",
			"fi",
			"fl",
			"endash",
			"dagger",
			"daggerdbl",
			"periodcentered",
			"paragraph",
			"bullet",
			"quotesinglbase",
			"quotedblbase",
			"quotedblright",
			"guillemotright",
			"ellipsis",
			"perthousand",
			"questiondown",
			"grave",
			"acute",
			"circumflex",
			"tilde",
			"macron",
			"breve",
			"dotaccent",
			"dieresis",
			"ring",
			"cedilla",
			"hungarumlaut",
			"ogonek",
			"caron",
			"emdash",
			"AE",
			"ordfeminine",
			"Lslash",
			"Oslash",
			"OE",
			"ordmasculine",
			"ae",
			"dotlessi",
			"lslash",
			"oslash",
			"oe",
			"germandbls",
			"onesuperior",
			"logicalnot",
			"mu",
			"trademark",
			"Eth",
			"onehalf",
			"plusminus",
			"Thorn",
			"onequarter",
			"divide",
			"brokenbar",
			"degree",
			"thorn",
			"threequarters",
			"twosuperior",
			"registered",
			"minus",
			"eth",
			"multiply",
			"threesuperior",
			"copyright",
			"Aacute",
			"Acircumflex",
			"Adieresis",
			"Agrave",
			"Aring",
			"Atilde",
			"Ccedilla",
			"Eacute",
			"Ecircumflex",
			"Edieresis",
			"Egrave",
			"Iacute",
			"Icircumflex",
			"Idieresis",
			"Igrave",
			"Ntilde",
			"Oacute",
			"Ocircumflex",
			"Odieresis",
			"Ograve",
			"Otilde",
			"Scaron",
			"Uacute",
			"Ucircumflex",
			"Udieresis",
			"Ugrave",
			"Yacute",
			"Ydieresis",
			"Zcaron",
			"aacute",
			"acircumflex",
			"adieresis",
			"agrave",
			"aring",
			"atilde",
			"ccedilla",
			"eacute",
			"ecircumflex",
			"edieresis",
			"egrave",
			"iacute",
			"icircumflex",
			"idieresis",
			"igrave",
			"ntilde",
			"oacute",
			"ocircumflex",
			"odieresis",
			"ograve",
			"otilde",
			"scaron",
			"uacute",
			"ucircumflex",
			"udieresis",
			"ugrave",
			"yacute",
			"ydieresis",
			"zcaron",
			"exclamsmall",
			"Hungarumlautsmall",
			"dollaroldstyle",
			"dollarsuperior",
			"ampersandsmall",
			"Acutesmall",
			"parenleftsuperior",
			"parenrightsuperior",
			"twodotenleader",
			"onedotenleader",
			"zerooldstyle",
			"oneoldstyle",
			"twooldstyle",
			"threeoldstyle",
			"fouroldstyle",
			"fiveoldstyle",
			"sixoldstyle",
			"sevenoldstyle",
			"eightoldstyle",
			"nineoldstyle",
			"commasuperior",
			"threequartersemdash",
			"periodsuperior",
			"questionsmall",
			"asuperior",
			"bsuperior",
			"centsuperior",
			"dsuperior",
			"esuperior",
			"isuperior",
			"lsuperior",
			"msuperior",
			"nsuperior",
			"osuperior",
			"rsuperior",
			"ssuperior",
			"tsuperior",
			"ff",
			"ffi",
			"ffl",
			"parenleftinferior",
			"parenrightinferior",
			"Circumflexsmall",
			"hyphensuperior",
			"Gravesmall",
			"Asmall",
			"Bsmall",
			"Csmall",
			"Dsmall",
			"Esmall",
			"Fsmall",
			"Gsmall",
			"Hsmall",
			"Ismall",
			"Jsmall",
			"Ksmall",
			"Lsmall",
			"Msmall",
			"Nsmall",
			"Osmall",
			"Psmall",
			"Qsmall",
			"Rsmall",
			"Ssmall",
			"Tsmall",
			"Usmall",
			"Vsmall",
			"Wsmall",
			"Xsmall",
			"Ysmall",
			"Zsmall",
			"colonmonetary",
			"onefitted",
			"rupiah",
			"Tildesmall",
			"exclamdownsmall",
			"centoldstyle",
			"Lslashsmall",
			"Scaronsmall",
			"Zcaronsmall",
			"Dieresissmall",
			"Brevesmall",
			"Caronsmall",
			"Dotaccentsmall",
			"Macronsmall",
			"figuredash",
			"hypheninferior",
			"Ogoneksmall",
			"Ringsmall",
			"Cedillasmall",
			"questiondownsmall",
			"oneeighth",
			"threeeighths",
			"fiveeighths",
			"seveneighths",
			"onethird",
			"twothirds",
			"zerosuperior",
			"foursuperior",
			"fivesuperior",
			"sixsuperior",
			"sevensuperior",
			"eightsuperior",
			"ninesuperior",
			"zeroinferior",
			"oneinferior",
			"twoinferior",
			"threeinferior",
			"fourinferior",
			"fiveinferior",
			"sixinferior",
			"seveninferior",
			"eightinferior",
			"nineinferior",
			"centinferior",
			"dollarinferior",
			"periodinferior",
			"commainferior",
			"Agravesmall",
			"Aacutesmall",
			"Acircumflexsmall",
			"Atildesmall",
			"Adieresissmall",
			"Aringsmall",
			"AEsmall",
			"Ccedillasmall",
			"Egravesmall",
			"Eacutesmall",
			"Ecircumflexsmall",
			"Edieresissmall",
			"Igravesmall",
			"Iacutesmall",
			"Icircumflexsmall",
			"Idieresissmall",
			"Ethsmall",
			"Ntildesmall",
			"Ogravesmall",
			"Oacutesmall",
			"Ocircumflexsmall",
			"Otildesmall",
			"Odieresissmall",
			"OEsmall",
			"Oslashsmall",
			"Ugravesmall",
			"Uacutesmall",
			"Ucircumflexsmall",
			"Udieresissmall",
			"Yacutesmall",
			"Thornsmall",
			"Ydieresissmall",
			"001.000",
			"001.001",
			"001.002",
			"001.003",
			"Black",
			"Bold",
			"Book",
			"Light",
			"Medium",
			"Regular",
			"Roman",
			"Semibold" };

	/** Lookup table to map values */
	private static final int ISOAdobeCharset[] = { // 229
																					  // elements
		0,
			1,
			2,
			3,
			4,
			5,
			6,
			7,
			8,
			9,
			10,
			11,
			12,
			13,
			14,
			15,
			16,
			17,
			18,
			19,
			20,
			21,
			22,
			23,
			24,
			25,
			26,
			27,
			28,
			29,
			30,
			31,
			32,
			33,
			34,
			35,
			36,
			37,
			38,
			39,
			40,
			41,
			42,
			43,
			44,
			45,
			46,
			47,
			48,
			49,
			50,
			51,
			52,
			53,
			54,
			55,
			56,
			57,
			58,
			59,
			60,
			61,
			62,
			63,
			64,
			65,
			66,
			67,
			68,
			69,
			70,
			71,
			72,
			73,
			74,
			75,
			76,
			77,
			78,
			79,
			80,
			81,
			82,
			83,
			84,
			85,
			86,
			87,
			88,
			89,
			90,
			91,
			92,
			93,
			94,
			95,
			96,
			97,
			98,
			99,
			100,
			101,
			102,
			103,
			104,
			105,
			106,
			107,
			108,
			109,
			110,
			111,
			112,
			113,
			114,
			115,
			116,
			117,
			118,
			119,
			120,
			121,
			122,
			123,
			124,
			125,
			126,
			127,
			128,
			129,
			130,
			131,
			132,
			133,
			134,
			135,
			136,
			137,
			138,
			139,
			140,
			141,
			142,
			143,
			144,
			145,
			146,
			147,
			148,
			149,
			150,
			151,
			152,
			153,
			154,
			155,
			156,
			157,
			158,
			159,
			160,
			161,
			162,
			163,
			164,
			165,
			166,
			167,
			168,
			169,
			170,
			171,
			172,
			173,
			174,
			175,
			176,
			177,
			178,
			179,
			180,
			181,
			182,
			183,
			184,
			185,
			186,
			187,
			188,
			189,
			190,
			191,
			192,
			193,
			194,
			195,
			196,
			197,
			198,
			199,
			200,
			201,
			202,
			203,
			204,
			205,
			206,
			207,
			208,
			209,
			210,
			211,
			212,
			213,
			214,
			215,
			216,
			217,
			218,
			219,
			220,
			221,
			222,
			223,
			224,
			225,
			226,
			227,
			228 };
	/** lookup data to convert Expert values */
	private static final int ExpertCharset[] = { // 166
																				// elements
		0,
			1,
			229,
			230,
			231,
			232,
			233,
			234,
			235,
			236,
			237,
			238,
			13,
			14,
			15,
			99,
			239,
			240,
			241,
			242,
			243,
			244,
			245,
			246,
			247,
			248,
			27,
			28,
			249,
			250,
			251,
			252,
			253,
			254,
			255,
			256,
			257,
			258,
			259,
			260,
			261,
			262,
			263,
			264,
			265,
			266,
			109,
			110,
			267,
			268,
			269,
			270,
			271,
			272,
			273,
			274,
			275,
			276,
			277,
			278,
			279,
			280,
			281,
			282,
			283,
			284,
			285,
			286,
			287,
			288,
			289,
			290,
			291,
			292,
			293,
			294,
			295,
			296,
			297,
			298,
			299,
			300,
			301,
			302,
			303,
			304,
			305,
			306,
			307,
			308,
			309,
			310,
			311,
			312,
			313,
			314,
			315,
			316,
			317,
			318,
			158,
			155,
			163,
			319,
			320,
			321,
			322,
			323,
			324,
			325,
			326,
			150,
			164,
			169,
			327,
			328,
			329,
			330,
			331,
			332,
			333,
			334,
			335,
			336,
			337,
			338,
			339,
			340,
			341,
			342,
			343,
			344,
			345,
			346,
			347,
			348,
			349,
			350,
			351,
			352,
			353,
			354,
			355,
			356,
			357,
			358,
			359,
			360,
			361,
			362,
			363,
			364,
			365,
			366,
			367,
			368,
			369,
			370,
			371,
			372,
			373,
			374,
			375,
			376,
			377,
			378 };


    /** needed so CIDFOnt0 can extend */
    public Type1C() {}


        //<start-jfr>

        /** get handles onto Reader so we can access the file */
		public Type1C(PdfObjectReader current_pdf_file,String substituteFont) {

				glyphs=new T1Glyphs(false);

				init(current_pdf_file);

				this.substituteFont=substituteFont;
		}


        /** read details of any embedded fontFile */
		protected void readEmbeddedFont(PdfObject pdfFontDescriptor) throws Exception {
		
			if(substituteFont!=null){

				byte[] bytes=null;

				//read details
				BufferedInputStream from;
				//create streams
				ByteArrayOutputStream to = new ByteArrayOutputStream();

				InputStream jarFile = loader.getResourceAsStream(substituteFont);
				if(jarFile==null){
					/**
		            	from=new BufferedInputStream(new FileInputStream(substituteFont));

		              //write
		                byte[] buffer = new byte[65535];
		                int bytes_read;
		                while ((bytes_read = from.read(buffer)) != -1)
		                    to.write(buffer, 0, bytes_read);

		                to.close();
		                from.close();

		                /**/

					File file=new File(substituteFont);
					InputStream is = new FileInputStream(file);
					long length = file.length();
		            
					if (length > Integer.MAX_VALUE) {
						System.out.println("Sorry! Your given file is too large.");
						return;
					}
					
		                bytes = new byte[(int)length];
		                int offset = 0;
		                int numRead = 0;
		                while (offset < bytes.length && (numRead=is.read(bytes, 
		                            offset, bytes.length-offset)) >= 0) {
		                    offset += numRead;
		                }
		                if (offset < bytes.length) {
		                    throw new IOException("Could not completely read file "
		                                        + file.getName());
		                }
		                is.close();
					// new BufferedReader
		                  //              (new InputStreamReader(loader.getResourceAsStream("org/jpedal/res/cid/" + encodingName), "Cp1252"));
					/**
		                FileReader from2=null;
		                try {
		                    from2 = new FileReader(substituteFont);
		                                //new BufferedReader);
		                    //outputStream = new FileWriter("characteroutput.txt");

		                    int c;
		                    while ((c = from2.read()) != -1) {
		                        to.write(c);
		                    }
		                } finally {
		                    if (from2 != null) {
		                        from2.close();
		                    }
		                    if (to != null) {
		                        to.close();
		                    }
		                }/**/
				}else{
					from= new BufferedInputStream(jarFile);

					//write
					byte[] buffer = new byte[65535];
					int bytes_read;
					while ((bytes_read = from.read(buffer)) != -1)
						to.write(buffer, 0, bytes_read);

					to.close();
					from.close();

					bytes=to.toByteArray();
				}
				/**load the font*/
				try{
					isFontSubstituted=true;
					
					//if (substituteFont.indexOf(".afm") != -1)
					readType1FontFile(bytes);
					//else
					//  readType1CFontFile(to.toByteArray(),null);

					

				} catch (Exception e) {

					e.printStackTrace();
					LogWriter.writeLog("[PDF]Substitute font="+substituteFont+"Type 1 exception=" + e);
				}

				//over-ride font remapping if substituted
				if(isFontSubstituted && glyphs.remapFont)
					glyphs.remapFont=false;

			}else if(pdfFontDescriptor!=null){
		
				PdfObject FontFile=pdfFontDescriptor.getDictionary(PdfDictionary.FontFile);
		
				/** try type 1 first then type 1c/0c */
				if (FontFile != null) {
					try {
						byte[] stream=currentPdfFile.readStream(FontFile,true,true,false, false,false, null);
						if(stream!=null)
							readType1FontFile(stream);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}else{
		
					PdfObject FontFile3=pdfFontDescriptor.getDictionary(PdfDictionary.FontFile3);
					if(FontFile3!=null){
						byte[] stream=currentPdfFile.readStream(FontFile3,true,true,false, false,false, null);
						if(stream!=null){ //if it fails, null returned
							//check for type1c or ottf
							if(stream.length>3 && stream[0]==719 && stream[1]==84 && stream[2]==84 && stream[3]==79){
							}else //assume all standard cff for moment
								readType1CFontFile(stream,null);
						}
					}
				}
			}
		}

        /** read in a font and its details from the pdf file */
		public void createFont(
                PdfObject pdfObject,
                String fontID,
                boolean renderPage,
                ObjectStore objectStore, Map substitutedFonts)
			throws Exception {

			LogWriter.writeMethod("{readType1Font}", 0);

            fontTypes = StandardFonts.TYPE1;
			
			//generic setup
			init(fontID, renderPage);
			
			/**
			 * get FontDescriptor object - if present contains metrics on glyphs
			 */
			PdfObject pdfFontDescriptor=pdfObject.getDictionary(PdfDictionary.FontDescriptor);
			
			//FontBBox and FontMatix
			setBoundsAndMatrix(pdfFontDescriptor);
			
			setName(pdfObject, fontID);
			setEncoding(pdfObject, pdfFontDescriptor);

            try{
            readEmbeddedFont(pdfFontDescriptor);
            }catch(Exception ee){
                ee.printStackTrace();
            }
            
            //setWidths(pdfObject);
            readWidths(pdfObject,true);
			
            if(embeddedFontName!=null && is1C() && PdfStreamDecoder.runningStoryPad){
            	embeddedFontName= cleanupFontName(embeddedFontName);                
            	this.setBaseFontName(embeddedFontName);
            	this.setFontName(embeddedFontName);               
            }

            //make sure a font set
			if (renderPage)
				setFont(getBaseFontName(), 1);
			
		}
        //<end-jfr>


		

    /** Constructor for OTF fonts */
    public Type1C(byte[] fontDataAsArray,FontData fontData, PdfJavaGlyphs glyphs) throws Exception{

        this.glyphs=glyphs;
        
        readType1CFontFile(fontDataAsArray,fontData);

    }


    /** Handle encoding for type1C fonts. Also used for CIDFontType0C */
    final private void readType1CFontFile(byte[] fontDataAsArray,FontData fontDataAsObject) throws Exception{

        LogWriter.writeMethod("{readType1CFontFile}", 0);

        LogWriter.writeLog("Embedded Type1C font used");

        glyphs.setis1C(true);

        boolean isByteArray=(fontDataAsArray!=null);

        //debugFont=getBaseFontName().indexOf("LC")!=-1;

        if(debugFont)
            System.err.println(getBaseFontName());


        /**
         try {
            java.io.FileOutputStream fos=new java.io.FileOutputStream(getBaseFontName()+"_"+fontDataAsArray.length+".bfp");
            fos.write(fontDataAsArray);
            fos.close();
         } catch (Exception e1) {
            e1.printStackTrace();
         }/***/

         
        int start; //pointers within table
        int size=2;

        /**
         * read Header
         */
        int major,minor;
        if(isByteArray){
            major = fontDataAsArray[0];
            minor = fontDataAsArray[1];
        }else{
            major = fontDataAsObject.getByte(0);
            minor = fontDataAsObject.getByte(1);
        }

        if ((major != 1) || (minor != 0))
            LogWriter.writeLog("1C  format "+ major+ ':' + minor+ " not fully supported");

        if(debugFont)
            System.out.println("major="+major+" minor="+minor);

        // read header size to workout start of names index
        if(isByteArray)
            top = fontDataAsArray[2];
        else
            top = fontDataAsObject.getByte(2);

        /**
         * read names index
         */
        // read name index for the first font
        int count,offsize;
        if(isByteArray){
            count = getWord(fontDataAsArray, top, size);
            offsize = fontDataAsArray[top + size];
        }else{
            count = getWord(fontDataAsObject, top, size);
            offsize = fontDataAsObject.getByte(top + size);
        }

        /**
         * get last offset and use to move to top dict index
         */
        top += (size+1);  //move pointer to start of font names
        start = top + (count + 1) * offsize - 1; //move pointer to end of offsets
        if(isByteArray)
            top = start + getWord(fontDataAsArray, top + count * offsize, offsize);
        else
            top = start + getWord(fontDataAsObject, top + count * offsize, offsize);


        /**
         * read the dict index
         */
        if(isByteArray){
            count = getWord(fontDataAsArray, top, size);
            offsize = fontDataAsArray[top + size];
        }else{
            count = getWord(fontDataAsObject, top, size);
            offsize = fontDataAsObject.getByte(top + size);
        }

        top += (size+1); //update pointer
        start = top + (count + 1) * offsize - 1;

        int dicStart,dicEnd;
        if(isByteArray){
            dicStart = start + getWord(fontDataAsArray, top, offsize);
            dicEnd = start + getWord(fontDataAsArray, top + offsize, offsize);
        }else{
            dicStart = start + getWord(fontDataAsObject, top, offsize);
            dicEnd = start + getWord(fontDataAsObject, top + offsize, offsize);
        }

        /**
         * read string index
         */
        String[] strings=readStringIndex(fontDataAsArray, fontDataAsObject, start, offsize, count);

        /**
         * read global subroutines (top set by Strings code)
         */
        readGlobalSubRoutines(fontDataAsArray,fontDataAsObject);

        /**
         * decode the dictionary
         */
        decodeDictionary(fontDataAsArray, fontDataAsObject, dicStart, dicEnd, strings);

        /**
         * allow  for subdictionaries in CID  font
         */
        if(FDSelect!=-1 ){

            

                if(debugDictionary)
                    System.out.println("=============FDSelect===================="+getBaseFontName());

                int nextDic=FDArray;

                if(isByteArray){
                    count = getWord(fontDataAsArray, nextDic, size);
                    offsize = fontDataAsArray[nextDic + size];
                }else{
                    count = getWord(fontDataAsObject, nextDic, size);
                    offsize = fontDataAsObject.getByte(nextDic + size);
                }

                nextDic += (size+1); //update pointer
                start = nextDic + (count + 1) * offsize - 1;

                if(isByteArray){
                    dicStart = start+getWord(fontDataAsArray, nextDic, offsize);
                    dicEnd =start+getWord(fontDataAsArray, nextDic + offsize, offsize);
                }else{
                    dicStart = start+getWord(fontDataAsObject, nextDic, offsize);
                    dicEnd =start+getWord(fontDataAsObject, nextDic + offsize, offsize);
                }

                decodeDictionary(fontDataAsArray, fontDataAsObject, dicStart, dicEnd, strings);

                if(debugDictionary)
                    System.out.println("================================="+getBaseFontName());

        }
        
        /**
         * get number of glyphs from charstrings index
         */
        top = charstrings;

        int nGlyphs;

        if(isByteArray)
            nGlyphs = getWord(fontDataAsArray, top, size); //start of glyph index
        else
            nGlyphs = getWord(fontDataAsObject, top, size); //start of glyph index

        if(debugFont)
            System.out.println("nGlyphs="+nGlyphs);

        int[] names =readCharset(charset, nGlyphs, charstrings, fontDataAsObject,fontDataAsArray);

        if(debugFont){
            System.out.println("=======charset===============");
            int count2=names.length;
            for(int jj=0;jj<count2;jj++){
                System.out.println(jj+" "+names[jj]);
            }

            System.out.println("=======Encoding===============");
        }

        /**
         * set encoding if not set
         */
        setEncoding(fontDataAsArray, fontDataAsObject,nGlyphs,names);

        /**
         * read glyph index
         */
        top = charstrings;
        readGlyphs(fontDataAsArray, fontDataAsObject, nGlyphs, names);

         /**/
        if(privateDict!=-1){

                top = privateDict + privateDictOffset;

                int len,nSubrs;

                if(isByteArray)
                    len=fontDataAsArray.length;
                else
                    len=fontDataAsObject.length();

                if(top+2<len){
                    if(isByteArray)
                        nSubrs = getWord(fontDataAsArray, top, size);
                    else
                        nSubrs = getWord(fontDataAsObject, top, size);

                    if(nSubrs>0)
                        readSubrs(fontDataAsArray, fontDataAsObject, nSubrs);
                }else if(debugFont || debugDictionary){
                    System.out.println("Private subroutine out of range");
                }
            
        }
         /**/
        /**
         * set flags to tell software to use these descritpions
         */
        isFontEmbedded = true;

        glyphs.setFontEmbedded(true);

    }

	/**pick up encoding from embedded font*/
	final private void setEncoding(byte[] fontDataAsArray, FontData fontDataAsObject,int nGlyphs,int[] names){

		LogWriter.writeMethod("{setEncoding}", 0);


        boolean isByteArray=fontDataAsArray!=null;

		if(debugFont)
			System.out.println("Enc="+enc);
		
		// read encoding (glyph -> code mapping)
		if (enc == 0){
			embeddedEnc=StandardFonts.STD;
			if (fontEnc == -1)
			putFontEncoding(StandardFonts.STD);
		}else if (enc == 1){
			embeddedEnc=StandardFonts.MACEXPERT;
			if (fontEnc == -1)
			putFontEncoding(StandardFonts.MACEXPERT);
		}else { //custom mapping

			if(debugFont)
				System.out.println("custom mapping");
			
			top = enc;
            int encFormat,c;

            if(isByteArray)
                encFormat = (fontDataAsArray[top++] & 0xff);
            else
                encFormat = (fontDataAsObject.getByte(top++) & 0xff);

			String name;

			if ((encFormat & 0x7f) == 0) { //format 0

                int nCodes;

                if(isByteArray)
                    nCodes = 1 + (fontDataAsArray[top++] & 0xff);
                else
                    nCodes = 1 + (fontDataAsObject.getByte(top++) & 0xff);

				if (nCodes > nGlyphs)
					nCodes = nGlyphs;
				for (int i = 1; i < nCodes; ++i) {

                    if(isByteArray){
                        c =fontDataAsArray[top++] & 0xff;
                        name =getString(fontDataAsArray,names[i],stringIdx,stringStart,stringOffSize);

                    }else{
                        c =fontDataAsObject.getByte(top++) & 0xff;
                        name =getString(fontDataAsObject,names[i],stringIdx,stringStart,stringOffSize);
                    }

					putChar(c, name);

				}

			} else if ((encFormat & 0x7f) == 1) { //format 1

                int nRanges;
                if(isByteArray)
                    nRanges = (fontDataAsArray[top++] & 0xff);
                else
                    nRanges = (fontDataAsObject.getByte(top++) & 0xff);

				int nCodes = 1;
				for (int i = 0; i < nRanges; ++i) {

                    int nLeft;

                    if(isByteArray){
                        c = (fontDataAsArray[top++] & 0xff);
                        nLeft = (fontDataAsArray[top++] & 0xff);
                    }else{
                        c = (fontDataAsObject.getByte(top++) & 0xff);
                        nLeft = (fontDataAsObject.getByte(top++) & 0xff);
                    }

					for (int j = 0; j <= nLeft && nCodes < nGlyphs; ++j) {

                        if(isByteArray)
                            name =getString(fontDataAsArray,names[nCodes],stringIdx,stringStart,stringOffSize);
                        else
                            name =getString(fontDataAsObject,names[nCodes],stringIdx,stringStart,stringOffSize);

						putChar(c, name);

						nCodes++;
						c++;
					}
				}
			}

			if ((encFormat & 0x80) != 0) { //supplimentary encodings

                int nSups;

                if(isByteArray)
                    nSups = (fontDataAsArray[top++] & 0xff);
                else
                    nSups = (fontDataAsObject.getByte(top++) & 0xff);

				for (int i = 0; i < nSups; ++i) {

                    if(isByteArray)
                        c = (fontDataAsArray[top++] & 0xff);
                    else
                        c = (fontDataAsObject.getByte(top++) & 0xff);

                    int sid;

                    if(isByteArray)
                        sid = getWord(fontDataAsArray, top, 2);
                    else
                        sid = getWord(fontDataAsObject, top, 2);

					top += 2;

                    if(isByteArray)
                        name =getString(fontDataAsArray,sid,stringIdx,stringStart,stringOffSize);
                    else
                        name =getString(fontDataAsObject,sid,stringIdx,stringStart,stringOffSize);

					putChar(c, name);

				}
			}
		}
	}
	
	// LILYPONDTOOL
	private final void readSubrs(byte[] fontDataAsArray, FontData fontDataAsObject, int nSubrs) throws Exception {
		
        boolean isByteArray=fontDataAsArray!=null;

        int subrOffSize;

        if(isByteArray)
            subrOffSize = fontDataAsArray[top+2];
        else
            subrOffSize = fontDataAsObject.getByte(top+2);

		top+=3;
		int subrIdx = top;
		int subrStart = top + (nSubrs + 1) * subrOffSize - 1;

        int nextTablePtr= top+nSubrs*subrOffSize;

        if(isByteArray){
            if(nextTablePtr<fontDataAsArray.length) //allow for table at end of file
                    top = subrStart + getWord(fontDataAsArray,nextTablePtr, subrOffSize);
                else
                    top=fontDataAsArray.length-1;
        }else{
            if(nextTablePtr<fontDataAsArray.length) //allow for table at end of file
                    top = subrStart + getWord(fontDataAsObject, nextTablePtr, subrOffSize);
                else
                    top=fontDataAsObject.length()-1;    
        }

        int[] subrOffset = new int [nSubrs + 2];
		int ii = subrIdx;
		for (int jj = 0; jj<nSubrs+1; jj++) {

            if(isByteArray){
                if((ii+subrOffSize)<fontDataAsArray.length)
                subrOffset[jj] = subrStart + getWord(fontDataAsArray, ii, subrOffSize);
            }else{
                if((ii+subrOffSize)<fontDataAsObject.length())
                subrOffset[jj] = subrStart + getWord(fontDataAsObject, ii, subrOffSize);
            }

            ii += subrOffSize;
		}
		subrOffset[nSubrs + 1] = top;

		glyphs.setLocalBias(calculateSubroutineBias(nSubrs));

        //read the glyphs and store
		int current = subrOffset[0];

        for (int jj = 1; jj < nSubrs+1; jj++) {


            //skip if out of bounds
            if(current==0 || subrOffset[jj]>fontDataAsArray.length || subrOffset[jj]<0 || subrOffset[jj]==0)
            continue;

            ByteArrayOutputStream nextSubr = new ByteArrayOutputStream();
			
			for (int c = current; c < subrOffset[jj]; c++){
                if(!isByteArray && c<fontDataAsObject.length())
                    nextSubr.write(fontDataAsObject.getByte(c));
                
            }

            if(isByteArray){

                int length=subrOffset[jj]-current;
                
                if(length>0){
	                byte[] nextSub=new byte[length];
	
	                System.arraycopy(fontDataAsArray,current,nextSub,0,length);
	
	                glyphs.setCharString("subrs"+(jj-1),nextSub);
                }
            }else{
                nextSubr.close();
			
			    glyphs.setCharString("subrs"+(jj-1),nextSubr.toByteArray());
            }
            current = subrOffset[jj];

		}
    }

	
	private final void readGlyphs(byte[] fontDataAsArray, FontData fontDataAsObject,int nGlyphs,int[] names) throws Exception{

		LogWriter.writeMethod("{readGlyphs}"+nGlyphs, 0);

        boolean isByteArray=fontDataAsArray!=null;

            int glyphOffSize;

            if(isByteArray)
                glyphOffSize = fontDataAsArray[top + 2];
            else
                glyphOffSize = fontDataAsObject.getByte(top + 2);

		top += 3;
		int glyphIdx = top;
		int glyphStart = top + (nGlyphs + 1) * glyphOffSize - 1;

            if(isByteArray)
                top =glyphStart+ getWord(fontDataAsArray,top + nGlyphs * glyphOffSize,glyphOffSize);
            else
                top =glyphStart+ getWord(fontDataAsObject,top + nGlyphs * glyphOffSize,glyphOffSize);

		int[] glyphoffset = new int[nGlyphs + 2];

		int ii = glyphIdx;

		//read the offsets
		for (int jj = 0; jj < nGlyphs + 1; jj++) {

                if(isByteArray)
                    glyphoffset[jj] = glyphStart+getWord(fontDataAsArray,ii,glyphOffSize);
                else
                    glyphoffset[jj] = glyphStart+getWord(fontDataAsObject,ii,glyphOffSize);
                
			ii = ii + glyphOffSize;

		}

		glyphoffset[nGlyphs + 1] = top;

		//read the glyphs and store
		int current = glyphoffset[0];
		for (int jj = 1; jj < nGlyphs+1; jj++) {

            byte[] nextGlyph=new byte[glyphoffset[jj]-current];

            for (int c = current; c < glyphoffset[jj]; c++){

                if(isByteArray)
                    nextGlyph[c-current]=fontDataAsArray[c];
                else
                    nextGlyph[c-current]=fontDataAsObject.getByte(c);
            }

			if((isCID)){
				glyphs.setCharString(String.valueOf(names[jj - 1]),nextGlyph);
				
				if(debugFont)
					System.out.println("CIDglyph= "+names[jj-1]+" start="+current+" length="+glyphoffset[jj]);
				
			}else{
                    String name;

                    if(isByteArray)
                        name=getString(fontDataAsArray,names[jj-1],stringIdx,stringStart,stringOffSize);
                    else
                        name=getString(fontDataAsObject,names[jj-1],stringIdx,stringStart,stringOffSize);

				glyphs.setCharString(name,nextGlyph);
				
				if(debugFont)
					System.out.println("glyph= "+name+" start="+current+" length="+glyphoffset[jj]);
				
			
			}
			current = glyphoffset[jj];

		}
	}
	
	private static final int calculateSubroutineBias(int subroutineCount) {
		int bias;
		if (subroutineCount < 1240) {
			bias = 107;
		} else if (subroutineCount < 33900) {
			bias = 1131;
		} else {
			bias = 32768;
		}
		return bias;
	}
	
	private final void readGlobalSubRoutines(byte[] fontDataAsArray, FontData fontDataAsObject) throws Exception{

		LogWriter.writeMethod("{readGlobalSubRoutines}", 0);

        boolean isByteArray=(fontDataAsArray!=null);

        int subOffSize,count;

        if(isByteArray){
            subOffSize = (fontDataAsArray[top + 2] & 0xff);
		    count= getWord(fontDataAsArray, top, 2);
        }else{
            subOffSize = (fontDataAsObject.getByte(top + 2) & 0xff);
            count= getWord(fontDataAsObject, top, 2);
        }

		top += 3;
		if(count>0){

			int idx = top;
			int start = top + (count + 1) * subOffSize - 1;
            if(isByteArray)
                top =start+ getWord(fontDataAsArray,top + count * subOffSize,subOffSize);
            else
                top =start+ getWord(fontDataAsObject,top + count * subOffSize,subOffSize);

			int[] offset = new int[count + 2];

			int ii = idx;

			//read the offsets
			for (int jj = 0; jj < count + 1; jj++) {

                if(isByteArray)
                    offset[jj] = start + getWord(fontDataAsArray,ii,subOffSize);
                else
                    offset[jj] = start + getWord(fontDataAsObject,ii,subOffSize);

				ii = ii + subOffSize;

			}

			offset[count + 1] = top;

			glyphs.setGlobalBias(calculateSubroutineBias(count));
			
			//read the subroutines and store
			int current = offset[0];
			for (int jj = 1; jj < count+1; jj++) {

				ByteArrayOutputStream nextStream = new ByteArrayOutputStream();
				for (int c = current; c < offset[jj]; c++){
                    if(isByteArray)
                        nextStream.write(fontDataAsArray[c]);
                    else
                        nextStream.write(fontDataAsObject.getByte(c));
                }
				nextStream.close();

				//store
				glyphs.setCharString("global"+(jj-1),nextStream.toByteArray());
				
				//setGlobalSubroutine(new Integer(jj-1+bias),nextStream.toByteArray());
				current = offset[jj];

			}
		}
	}

	private void decodeDictionary(byte[] fontDataAsArray, FontData fontDataAsObject,int dicStart,int dicEnd,String[] strings){

        boolean fdReset=false;

		LogWriter.writeMethod("{decodeDictionary}", 0);

		if(debugDictionary)
			System.out.println("=============Read dictionary===================="+getBaseFontName());
		
        boolean isByteArray=fontDataAsArray!=null;

		int p = dicStart, nextVal,key;
		int i=0;
		double[] op = new double[48]; //current operand in dictionary

        while (p < dicEnd) {

            if(isByteArray)
                nextVal = fontDataAsArray[p] & 0xFF;
            else
                nextVal = fontDataAsObject.getByte(p) & 0xFF;

			if (nextVal <= 27 || nextVal == 31) { // operator

				key = nextVal;

				p++;
				
				if(debugDictionary && key!=12)
					System.out.println(key +" (1) "+OneByteCCFDict[key]);
				
				if (key == 0x0c) { //handle escaped keys

                    if(isByteArray)
                        key = fontDataAsArray[p] & 0xFF;
                    else
                        key = fontDataAsObject.getByte(p) & 0xFF;

					if(debugDictionary)
						System.out.println(key +" (2) "+TwoByteCCFDict[key]);
					
					p++;

                    if(key!=36 && key!=37 && key!=7 && FDSelect!=-1){
                        if(debugDictionary){
							System.out.println("Ignored as part of FDArray ");

                            for(int ii=0;ii<6;ii++)
							System.out.println(op[ii]);
                        }
                    }else if (key == 2) { //italic
						
                    	italicAngle=(int)op[0];
						if(debugDictionary)
							System.out.println("Italic="+op[0]);
					
                    }else if (key == 7) { //fontMatrix
                    	if(!hasFontMatrix)
                    		System.arraycopy(op, 0, FontMatrix, 0, 6);
                    	
						if(debugDictionary){
							for(int ii=0;ii<6;ii++)
							System.out.println(ii+"="+op[ii]+" "+this);
						}
						
						hasFontMatrix=true;
					}else if (key == 30) { //ROS
						ROS=(int)op[0];
						isCID=true;
						if(debugDictionary)
							System.out.println(op[0]);
					}else if(key==31){ //CIDFontVersion
						CIDFontVersion=(int)op[0];
						if(debugDictionary)
							System.out.println(op[0]);
					}else if(key==32){ //CIDFontRevision
						CIDFontRevision=(int)op[0];
						if(debugDictionary)
							System.out.println(op[0]);		
					}else if(key==33){ //CIDFontType
						CIDFontType=(int)op[0];
						if(debugDictionary)
							System.out.println(op[0]);			
					}else if(key==34){ //CIDcount
						CIDcount=(int)op[0];
						if(debugDictionary)
							System.out.println(op[0]);		
					}else if(key==35){ //UIDBase
						UIDBase=(int)op[0];
						if(debugDictionary)
							System.out.println(op[0]);	
					}else if(key==36){ //FDArray
						FDArray=(int)op[0];
						if(debugDictionary)
							System.out.println(op[0]);

                    }else if(key==37){ //FDSelect
						FDSelect=(int)op[0];

                        fdReset=true;

                        if(debugDictionary)
							System.out.println(op[0]);
                    }else if(key==0){ //copyright

                        int id=(int)op[0];
                        if(id>390)
                        id=id-390;
                        copyright=strings[id];
                        if(debugDictionary)
                        System.out.println("copyright= "+copyright);
                    }else if(key==21){ //Postscript


                        //postscriptFontName=strings[id];
                        if(debugDictionary)  {
                            int id=(int)op[0];
                            if(id>390)
                            id=id-390;

                            System.out.println("Postscript= "+strings[id]);
                            System.out.println(TwoByteCCFDict[key]+ ' ' +op[0]);
                        }
                    }else if(key==22){ //BaseFontname

                        //baseFontName=strings[id];
                        if(debugDictionary){

                            int id=(int)op[0];
                            if(id>390)
                            id=id-390;

                            System.out.println("BaseFontname= "+embeddedFontName);
                            System.out.println(TwoByteCCFDict[key]+ ' ' +op[0]);
                        }
                    }else if(key==38){ //fullname

                        //fullname=strings[id];
                        if(debugDictionary){

                            int id=(int)op[0];
                            if(id>390)
                            id=id-390;

                            System.out.println("fullname= "+strings[id]);
                            System.out.println(TwoByteCCFDict[key]+ ' ' +op[0]);
                        }

                    }else if(debugDictionary)
						System.out.println(op[0]);

				} else {

                    if(key==2){ //fullname

                        int id=(int)op[0];
                        if(id>390)
                        id=id-390;
                        embeddedFontName=strings[id];
                        if(debugDictionary){
                            System.out.println("name= "+embeddedFontName);
                            System.out.println(OneByteCCFDict[key]+ ' ' +op[0]);
                        }

                    }else if(key==3){ //familyname

                        //embeddedFamilyName=strings[id];
                        if(debugDictionary){
                            
                            int id=(int)op[0];
                            if(id>390)
                            id=id-390;

                            System.out.println("FamilyName= "+embeddedFamilyName);
                            System.out.println(OneByteCCFDict[key]+ ' ' +op[0]);
                        }

                    }else if (key == 5) { //fontBBox
						if(debugDictionary){
							for(int ii=0;ii<4;ii++)
							System.out.println(op[ii]);
						}
						for(int ii=0;ii<4;ii++){
							//System.out.println(" "+ii+" "+op[ii]);
							this.FontBBox[ii]=(float) op[ii];
						}
						
						hasFontBBox=true;
					}else if (key == 0x0f) { // charset
						charset = (int) op[0];
						
						if(debugDictionary)
							System.out.println(op[0]);
						
					} else if (key == 0x10) { // encoding
						enc = (int) op[0];
						
						if(debugDictionary)
							System.out.println(op[0]);
						
					} else if (key == 0x11) { // charstrings
						charstrings = (int) op[0];
						
						if(debugDictionary)
							System.out.println(op[0]);
						
						//System.out.println("charStrings="+charstrings);
					} else if (key == 18 && glyphs.is1C()) { // readPrivate
						privateDict = (int) op[1];
						privateDictOffset = (int) op[0];
						
						if(debugDictionary)
						System.out.println("privateDict="+op[0]+" Offset="+op[1]);

                    }else if(debugDictionary){
						
						// System.out.println(p+" "+key+" "+T1CcharCodes1Byte[key]+" <<<"+op);
						 
						System.out.println("Other value "+key);
						 /**if(op <type1CStdStrings.length)
						 System.out.println(type1CStdStrings[(int)op]);
						 else if((op-390) <strings.length)
						 System.out.println("interesting key:"+key);*/
					}
					//System.out.println(p+" "+key+" "+raw1ByteValues[key]+" <<<"+op);
				}

				i=0;

			}else{

                if(isByteArray)
                    p=glyphs.getNumber(fontDataAsArray, p,op,i,false);
                else
                    p=glyphs.getNumber(fontDataAsObject, p,op,i,false);

                i++;
			}
		}
		
		if(debugDictionary)
			System.out.println("================================="+getBaseFontName());

        //reset
        if(!fdReset)
        FDSelect=-1;

    }

	private String[] readStringIndex(byte[] fontDataAsArray,FontData fontDataAsObject,int start,int offsize,int count){

		LogWriter.writeMethod("{readStringIndex}", 0);

        int nStrings;

        boolean isByteArray=(fontDataAsArray!=null);

        if(isByteArray){
            top = start + getWord(fontDataAsArray, top + count * offsize, offsize);
		//start of string index
		    nStrings = getWord(fontDataAsArray, top, 2);
		    stringOffSize = fontDataAsArray[top + 2];
        }else{
            top = start + getWord(fontDataAsObject, top + count * offsize, offsize);
            //start of string index
            nStrings = getWord(fontDataAsObject, top, 2);
            stringOffSize = fontDataAsObject.getByte(top + 2);
        }

		top += 3;
		stringIdx = top;
		stringStart = top + (nStrings + 1) * stringOffSize - 1;

        if(isByteArray)
            top =stringStart+ getWord(fontDataAsArray,top + nStrings * stringOffSize,stringOffSize);
        else
            top =stringStart+ getWord(fontDataAsObject,top + nStrings * stringOffSize,stringOffSize);

		int[] offsets = new int[nStrings + 2];
		String[] strings = new String[nStrings + 2];

		int ii = stringIdx;
		//read the offsets
		for (int jj = 0; jj < nStrings + 1; jj++) {

            if(isByteArray)
                offsets[jj] = getWord(fontDataAsArray,ii,stringOffSize); //content[ii] & 0xff;
            else
                offsets[jj] = getWord(fontDataAsObject,ii,stringOffSize); //content[ii] & 0xff;
			//getWord(content,ii,stringOffSize);
			ii = ii + stringOffSize;

		}

		offsets[nStrings + 1] = top - stringStart;

		//read the strings
		int current = 0;
		for (int jj = 0; jj < nStrings + 1; jj++) {

			StringBuffer nextString = new StringBuffer();
			for (int c = current; c < offsets[jj]; c++){
                if(isByteArray)
                    nextString.append((char) fontDataAsArray[stringStart + c]);
                else
                    nextString.append((char) fontDataAsObject.getByte(stringStart + c));
            }

			if(debugFont)
				System.out.println("String "+jj+" ="+nextString);
			
			strings[jj] = nextString.toString();
			current = offsets[jj];

        }
		return strings;
	}

	/** Utility method used during processing of type1C files */
	static final private String getString(FontData fontDataAsObject,int sid,int idx,int start,int offsize) {

		int len;
		String result = null;

		if (sid < 391)
			result = type1CStdStrings[sid];
		else {
			sid -= 391;
			int idx0 =start+ getWord(fontDataAsObject, idx + sid * offsize,offsize);
			int idxPtr1 =start+ getWord(fontDataAsObject, idx + (sid + 1) * offsize,offsize);
			//System.out.println(sid+" "+idx0+" "+idxPtr1);
			if ((len = idxPtr1 - idx0) > 255)
				len = 255;

            result = new String(fontDataAsObject.getBytes(idx0,len));
		}
		return result;
	}

    /** Utility method used during processing of type1C files */
	static final private String getString(byte[] fontDataAsArray,int sid,int idx,int start,int offsize) {

		int len;
		String result = null;

        if (sid < 391)
			result = type1CStdStrings[sid];
		else {
			sid -= 391;
			int idx0 =start+ getWord(fontDataAsArray, idx + sid * offsize,offsize);
			int idxPtr1 =start+ getWord(fontDataAsArray, idx + (sid + 1) * offsize,offsize);
			//System.out.println(sid+" "+idx0+" "+idxPtr1);
			if ((len = idxPtr1 - idx0) > 255)
				len = 255;

            result=new String(fontDataAsArray,idx0,len);
            
		}
		return result;
	}



	/** get standard charset or extract from type 1C font */
	static final private int[] readCharset(int charset,int nGlyphs,int top,FontData fontDataAsObject, byte[] fontDataAsArray) {

		LogWriter.writeMethod("{get1CCharset}+"+charset+" glyphs="+nGlyphs, 0);

        boolean isByteArray=fontDataAsArray!=null;

		int glyphNames[] = null;
		int i, j;
		
		if(debugFont)
			System.out.println("charset="+charset);

		/**
		//handle CIDS first
		if(isCID){
			glyphNames = new int[nGlyphs];
			glyphNames[0] = 0;

			for (i = 1; i < nGlyphs; ++i) {
				glyphNames[i] = i;//getWord(fontData, top, 2);
				//top += 2;
				}


		// read appropriate non-CID charset
		}else */if (charset == 0)
			glyphNames = ISOAdobeCharset;
		else if (charset == 1)
			glyphNames = ExpertCharset;
		else if (charset == 2)
			glyphNames = ExpertSubCharset;
		else {
			glyphNames = new int[nGlyphs+1];
			glyphNames[0] = 0;
			top = charset;

            int charsetFormat;

            if(isByteArray)
                charsetFormat = fontDataAsArray[top++] & 0xff;
            else
                charsetFormat = fontDataAsObject.getByte(top++) & 0xff;

			if(debugFont)
				System.out.println("charsetFormat="+charsetFormat);
			
			if (charsetFormat == 0) {
				for (i = 1; i < nGlyphs; ++i) {
                    if(isByteArray)
                        glyphNames[i] = getWord(fontDataAsArray, top, 2);
                    else
                        glyphNames[i] = getWord(fontDataAsObject, top, 2);

					top += 2;
				}

			} else if (charsetFormat == 1) {
				
				i = 1;

                    int c,nLeft;
				while (i < nGlyphs) {

                        if(isByteArray)
                            c = getWord(fontDataAsArray, top, 2);
                        else
                            c = getWord(fontDataAsObject, top, 2);
					top += 2;
                        if(isByteArray)
                            nLeft = fontDataAsArray[top++] & 0xff;
                        else
                            nLeft = fontDataAsObject.getByte(top++) & 0xff;

					for (j = 0; j <= nLeft; ++j)
						glyphNames[i++] =c++;
					
				}
			} else if (charsetFormat == 2) {
				i = 1;

                int c,nLeft;

				while (i < nGlyphs) {
                    if(isByteArray)
                        c = getWord(fontDataAsArray, top, 2);
                    else
                        c = getWord(fontDataAsObject, top, 2);

					top += 2;

                    if(isByteArray)
                        nLeft = getWord(fontDataAsArray, top, 2);
                    else
                        nLeft = getWord(fontDataAsObject, top, 2);

					top += 2;
					for (j = 0; j <= nLeft; ++j)
						glyphNames[i++] =c++;
				}
			}
		}

		return glyphNames;
	}

	/** Utility method used during processing of type1C files */
	static final private int getWord(FontData fontDataAsObject, int index, int size) {
		int result = 0;
		for (int i = 0; i < size; i++) {
			result = (result << 8) + (fontDataAsObject.getByte(index + i) & 0xff);

		}
		return result;
	}

    /** Utility method used during processing of type1C files */
	static final private int getWord(byte[] fontDataAsArray, int index, int size) {
		int result = 0;
		for (int i = 0; i < size; i++) {
			result = (result << 8) + (fontDataAsArray[index + i] & 0xff);

		}
		return result;
	}

    /**
     * get bounding box to highlight
     * @return
     */
    public Rectangle getBoundingBox() {

        if(isFontEmbedded)
            return new Rectangle((int)FontBBox[0], (int)FontBBox[1],
                    (int)(FontBBox[2]-FontBBox[0]), (int)(FontBBox[3]-FontBBox[1]));  //To change body of created methods use File | Settings | File Templates.
        else
            return super.getBoundingBox();
    }


}



