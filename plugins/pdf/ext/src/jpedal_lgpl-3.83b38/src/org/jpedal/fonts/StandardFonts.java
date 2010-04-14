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
* StandardFonts.java
* ---------------
*/
package org.jpedal.fonts;

import java.io.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

import org.jpedal.utils.LogWriter;
import org.jpedal.fonts.tt.*;
import org.jpedal.fonts.objects.FontData;
import org.jpedal.fonts.glyph.T1Glyphs;

public class StandardFonts {
	
	/**holds names of every character*/
	static private Map unicode_name_mapping_table = new Hashtable();
	
	/**holds lookup to map char values for decoding char into name*/
	private static String[][] unicode_char_decoding_table = new String[7][335];
	
	public final static int PDF = 6;
	
	/**flag used to identify ZAPF encoding*/
	public final static int ZAPF = 5;
	
	/**flag used to identify Symbol encoding*/
	public final static int SYMBOL = 4;

	/**flag used to identify MacExpert encoding*/
	public final static int MACEXPERT = 3;
	
	/**flag used to identify WIN encoding*/
	public final static int WIN = 2;

	/**flag used to identify STD encoding*/
	public final static int STD = 1;

	/**flag used to identify mac encoding*/
	public final static int MAC = 0;

    /**mapped onto CID0 or CID2*/
    final public static int TYPE0=1228944676;


    public final static int TYPE1=1228944677;
	public final static int TRUETYPE=1217103210;

    
    public final static int TYPE3=1228944679;
	public final static int CIDTYPE0=-1684566726;
	public final static int CIDTYPE2=-1684566724;

    public final static int OPENTYPE=6;

    public final static int TRUETYPE_COLLECTION=7;
    
    public final static int FONT_UNSUPPORTED=8;
    
    /**constant value for ellipsis*/
    private static String ellipsis= String.valueOf((char) Integer.parseInt("2026", 16));
    
    /**must use windows encoding because files were edited on Windows*/
	private static final String enc = "Cp1252";
	
	/**only in 1 encoding*/
	private static Map uniqueValues=null;
	
	/**lookup table to workout index from glyf*/
	private static Map[] glyphToChar= new Hashtable[7];

	/**holds lookup to map char values for decoding NAME into encoded char*/
	private static String[] MAC_char_encoding_table;
	
	/**holds lookup to map char values for decoding NAME into encoded char*/
	private static String[] WIN_char_encoding_table;
	
	/**holds lookup to map char values for decoding NAME into encoded char*/
	private static String[] STD_char_encoding_table;

	/**holds lookup to map char values for decoding NAME into encoded char*/
	private static String[] PDF_char_encoding_table;

	/**holds lookup to map char values for decoding NAME into encoded char*/
	private static String[] ZAPF_char_encoding_table;
	
	/**holds lookup to map char values for decoding NAME into encoded char*/
	private static String[] SYMBOL_char_encoding_table;
	
	/**holds lookup to map char values for decoding NAME into encoded char*/
	private static String[] MACEXPERT_char_encoding_table;
	
	
	/**loader to load data from jar*/
	private static ClassLoader loader = StandardFonts.class.getClassLoader();
	
	/**list of standard fonts*/
	private static Hashtable standardFileList=new Hashtable();
	
	/**flag if standard font loaded*/
	private static  Hashtable standardFontLoaded=new Hashtable();
	
	/**lookup for standard fonts which we read when object created*/
	private static Map widthTableStandard = new Hashtable();
	
	/**names of CID fonts supplied by Adobe*/
	private static String[] CIDFonts =
	{
				"83pv-RKSJ-H",
						"90msp-RKSJ-H",
				"90msp-RKSJ-V",
						"90ms-RKSJ-H",
				"90ms-RKSJ-UCS2",
						"90ms-RKSJ-V",
				"90pv-RKSJ-H",
						"90pv-RKSJ-UCS2",
				"90pv-RKSJ-UCS2C",
						"Add-RKSJ-H",
				"Add-RKSJ-V",
						"Adobe-CNS1-3",
				"Adobe-CNS1-UCS2",
						"Adobe-GB1-4",
				"Adobe-GB1-UCS2",
						"Adobe-Japan1-4",
				"Adobe-Japan1-UCS2",
						"Adobe-Korea1-2",
				"Adobe-Korea1-UCS2",
						"B5pc-H",
				"B5pc-UCS2",
						"B5pc-UCS2C",
				"B5pc-V",
						"CNS-EUC-H",
				"CNS-EUC-V",
						"ETen-B5-H",
				"ETen-B5-UCS2",
						"ETen-B5-V",
				"euc-h",
						"euc-v",
				"Ext-RKSJ-H",
						"Ext-RKSJ-V",
				"gb-euc-h",
						"gb-euc-v",
				"gbk2k-h",
						"gbk2k-v",
				"GBK-EUC-H",
						"GBK-EUC-UCS2",
				"GBK-EUC-V",
						"GBKp-EUC-H",
				"GBKp-EUC-V",
						"GBpc-EUC-H",
				"GBpc-EUC-UCS2",
						"GBpc-EUC-UCS2C",
				"GBpc-EUC-V",
						"GBT-EUC-H",
				"GBT-EUC-V",
						"h",
				"HKscs-B5-H",
						"HKscs-B5-V",
				"KSC-EUC-H",
						"KSC-EUC-V",
				"KSCms-UHC-H",
						"KSCms-UHC-HW-H",
				"KSCms-UHC-HW-V",
						"KSCms-UHC-UCS2",
				"KSCms-UHC-V",
						"KSCpc-EUC-H",
				"KSCpc-EUC-UCS2",
						"KSCpc-EUC-UCS2C",
				"UniCNS-UCS2-H",
						"UniCNS-UCS2-V",
				"UniGB-UCS2-H",
						"UniGB-UCS2-V",
				"UniJIS-UCS2-H",
						"UniJIS-UCS2-HW-H",
				"UniJIS-UCS2-HW-V",
						"UniJIS-UCS2-V",
				"UniKS-UCS2-H",
						"UniKS-UCS2-V",
	"v" };
	
	/**lookup table for java fonts*/
	protected static Map javaFontList=new Hashtable();
	
	/**java font versions of fonts*/
	protected static String javaFonts[] =
	{"Courier",
			"Courier-Bold",
			"Courier",
					"Courier-Bold",
			"Arial",
					"Arial-Bold",
			"Arial",
					"Arial-Italic",
			"Symbol",
					"Times New Roman",
			"Times New Roman",
					"Times New Roman",
			"Times New Roman",
"Wingdings" };
	
	/**names of 14 local fonts used in pdf*/
	protected static String files_names[] =
	{
				"Courier",
						"Courier-Bold",
				"Courier-BoldOblique",
						"Courier-Oblique",
				"Helvetica",
						"Helvetica-Bold",
				"Helvetica-BoldOblique",
						"Helvetica-Oblique",
				"Symbol",
						"Times-Bold",
				"Times-BoldItalic",
						"Times-Italic",
				"Times-Roman",
	"ZapfDingbats" };

	/**alternative names of 14 local fonts used in pdf*/
	protected static String files_names_bis[] =
	{
				"CourierNew",
						"CourierNew,Bold",
				"CourierNew,BoldItalic",
						"CourierNew,Italic",
				"Arial",
						"Arial,Bold",
				"Arial,BoldItalic",
						"Arial,Italic",
				"Symbol",
						"TimesNewRoman,Bold",
				"TimesNewRoman,BoldItalic",
						"TimesNewRoman,Italic",
				"TimesNewRoman",
	"ZapfDingbats" };

	/**holds lookup values used by truetype font mapping*/
	private static Hashtable adobeMap=null;

    //hold bounds
    private static Map fontBounds=new HashMap();
    
   public static void dispose(){
	   
	   unicode_name_mapping_table =null;
		
		unicode_char_decoding_table =null;
		
		uniqueValues=null;
		
		glyphToChar=null;

		MAC_char_encoding_table=null;
		
		WIN_char_encoding_table=null;
		
		STD_char_encoding_table=null;

		PDF_char_encoding_table=null;

		ZAPF_char_encoding_table=null;
		
		SYMBOL_char_encoding_table=null;
		
		MACEXPERT_char_encoding_table=null;
		
		
		loader = null;
		
		standardFileList=null;
		
		standardFontLoaded=null;
		
		widthTableStandard = null;
		
		/**names of CID fonts supplied by Adobe*/
		CIDFonts =null;
		
		/**lookup table for java fonts*/
		javaFontList=null;
		
		/**java font versions of fonts*/
		javaFonts=null;
		
		files_names=null;

		files_names_bis=null;

		adobeMap=null;

	    fontBounds=null;
   }

    //////////////////////////////////////////////////
	/**
	 * create lookup array so we can quickly test if 
	 * we have one of the 14 fonts so we can test quickly
	 */
	static{
		// loop to read widths from default fonts
		for (int i = 0; i < files_names.length; i++) {
			standardFileList.put(files_names_bis[i],new Integer(i));
			standardFileList.put(files_names[i],new Integer(i));
		}
		
		loadAdobeMap();
	}

    /**return font type based on file ending or FONT_UNSUPPORTED
     * if not recognised
     * @param name
     * @return int - defined in StandardFonts (no value is FONT_UNSUPPORTED)
     */
    public static int getFontType(String name) {

        int type= FONT_UNSUPPORTED;

        if (name.endsWith(".ttf"))
            type=TRUETYPE;
        else if(name.endsWith(".otf"))
            type=OPENTYPE;
        else if(name.endsWith(".ttc"))
            type=TRUETYPE_COLLECTION;
        //else if(name.endsWith(".afm"))
          //  type=TYPE1;
        else if(name.endsWith(".pfb"))
            type=TYPE1;

        return type;
    }

    /**
     * get FontBonds set in afm file 
     */
    public static float[] getFontBounds(String fontName){
       return (float[]) fontBounds.get(fontName);
    }
    

    public static String getUnicodeName(String key){
		return (String) unicode_name_mapping_table.get(key);
	}
	
	public static String getUnicodeChar(int i,int key){

        return unicode_char_decoding_table[i][key];
	}

    public static Map getUniqueMappings(){

    	if(uniqueValues==null){
    		
	        uniqueValues=new HashMap();
	
	        for(int ii=0;ii<256;ii++){
	            if(WIN_char_encoding_table[ii]==null && MAC_char_encoding_table[ii]!=null ){
	                //System.out.println(ii+" MAC only="+MAC_char_encoding_table[ii]);
	                uniqueValues.put(new Integer(ii), new Integer(1));
	            }
	
	            if(WIN_char_encoding_table[ii]!=null && MAC_char_encoding_table[ii]==null){
	                //System.out.println(ii+" WIN only="+WIN_char_encoding_table[ii]);
	                uniqueValues.put(new Integer(ii), new Integer(-1));
	            }
	        }
    	}
    	
        return uniqueValues;
    }
	
	public static Float getStandardWidth(String font,String key){
	    
	    Object value=widthTableStandard.get(font+key);
	    if(value==null){
	        String altfont=font;
	        int p=altfont.indexOf(',');
	        if(p!=-1){
	            altfont=altfont.substring(0,p);
	            value=widthTableStandard.get(altfont+key);
	        }
	    }
	    
		return (Float) value;
	}
	
	//////////////////////////////////////////////////////////////////////////
	/**
	 * create mapping tables for pdf values
	 * for Zapf and Symbol (not fully implmented yet)
	 */
	final static private void readStandardMappingTable(
			int key,
			String file_name){
		String char_value, NAME, VAL, line = null,hexVal=null;
		int value = 0;
		BufferedReader input_stream = null;
		
		glyphToChar[key]=new Hashtable();
		
		
		try {
		
			
			input_stream =
			(file_name.equals("symbol.cfg"))
			? new BufferedReader(
					new InputStreamReader(
							loader.getResourceAsStream(
									"org/jpedal/res/pdf/" + file_name),
							enc))
			: new BufferedReader(
					new InputStreamReader(
							loader.getResourceAsStream(
									"org/jpedal/res/pdf/" + file_name),
							"UTF-16"));

			// trap problems
			if (input_stream == null) {
				LogWriter.writeLog(
						"Unable to open "
						+ file_name
						+ " to read standard encoding");
			}
			

			//read in lines and place in map tables for fast lookup
			while (true) {
				line = input_stream.readLine();
				if (line == null)
					break;
				
				//write values to table, converting from Octal
				StringTokenizer values = new StringTokenizer(line);
				
				//trap for space and lines which cause problems in Zapf
				if ((line.indexOf("space") == -1) && (values.countTokens() > 1)) {
					
					//ignore first as token but read as char
					if (values.countTokens() == 3) {
						char_value = values.nextToken();
						NAME = values.nextToken();
						VAL = values.nextToken();
						
					}else if (values.countTokens() == 4) {
						hexVal=values.nextToken();
						char_value = values.nextToken();
						NAME = values.nextToken();
						VAL = values.nextToken();
						
						char_value=Character.toString((char)Integer.parseInt(hexVal,16));
						
					} else { //zapf values
						if(values.countTokens()==2){
							char_value = " ";
							NAME = values.nextToken();
							VAL = values.nextToken();
						}else{
							char_value = values.nextToken();
							NAME = values.nextToken();
							VAL = values.nextToken();
						}
					}
					
					unicode_name_mapping_table.put(key + NAME, char_value);
					
					glyphToChar[key].put(NAME, new Integer(Integer.parseInt(VAL)));
					
					//20021104 added to make sure names in list as well
					//if (file_name.equals("zapf.cfg"))
						unicode_name_mapping_table.put(NAME, char_value);
					
					//convert if there is a value
					if (Character.isDigit(VAL.charAt(0))) {
						
						value = Integer.parseInt(VAL, 8);
						
						if(key==ZAPF)
							ZAPF_char_encoding_table[value] = char_value;
						else if(key==SYMBOL)
							SYMBOL_char_encoding_table[value] = char_value;
						else if(key==MACEXPERT)
							MACEXPERT_char_encoding_table[value] = char_value;
						
						unicode_char_decoding_table[key][value]=NAME;
						
					}
				}
			}
		} catch (Exception e) {
			LogWriter.writeLog("Exception " + e + " reading lookup table for pdf");		
		}
		
		if(input_stream!=null){
			try{
				input_stream.close();
			}catch (Exception e) {
				LogWriter.writeLog(
						"Exception " + e + " reading lookup table for pdf  for abobe map");
			}		
		}
	}
	//////////////////////////////////////////////
	/**
	 * create mapping tables for 'standard' pdf values
	 * by reading in data from files (which are tables
	 * taken from Adobe's standard documentation
	 */
	final static private void readStandardMappingTable(int idx) {

        String char_value = null, NAME, STD_value, MAC_value, WIN_value,PDF_value,raw;
		int mac_value = 0, win_value = 0, std_value = 0;
		String line = null;
		BufferedReader input_stream = null;
		
		//needed for comparison table
		if(idx==MAC)
			checkLoaded(WIN);
		
		
		try {
		
			//initialise inverse lookup
			glyphToChar[idx]=new Hashtable();
			
			input_stream =
			new BufferedReader(
					new InputStreamReader(
							loader.getResourceAsStream(
							"org/jpedal/res/pdf/standard_encoding.cfg"),enc));
			
			// trap problems
			if (input_stream == null)
				LogWriter.writeLog(
				"Unable to open standard_encoding.cfg from jar");
			
			//read in lines and place in map tables for fast lookup
			while (true) {
				line = input_stream.readLine();
				if (line == null)
					break;
				
				//write values to table, converting from Octal
				StringTokenizer values = new StringTokenizer(line);
				int count=values.countTokens();
				
				//format is NAME, STD,MAC,WIN,PDF, unicode value (as hex) char from PDF reference ignored) or 
				//NAME, STD,MAC,WIN,PDF,  char (used for fi,fl and other double values)
				//ignore first as token but read as char
				
				NAME = values.nextToken();
				STD_value = values.nextToken();
				MAC_value = values.nextToken();
				WIN_value = values.nextToken();
				PDF_value =values.nextToken();
				raw=values.nextToken();
				
				if(count==7)				
					char_value=Character.toString((char)Integer.parseInt(raw,16));					
				else
					char_value=raw;
				

				//convert if possible
				if((idx==MAC) &&(Character.isDigit(MAC_value.charAt(0)))) {
					mac_value = Integer.parseInt(MAC_value, 8);

                    //substitute ellipsis
                    if(mac_value==201)
                        char_value=ellipsis;
                    
					MAC_char_encoding_table[mac_value] =char_value;
					unicode_char_decoding_table[MAC][mac_value]=NAME;
					
					glyphToChar[MAC].put(NAME, new Integer(mac_value));
					
					//build a comparison table to test encoding
					if (Character.isDigit(WIN_value.charAt(0)))
						win_value = Integer.parseInt(WIN_value, 8);
					
				}else if ((idx==STD)&&(Character.isDigit(STD_value.charAt(0)))) {
					std_value = Integer.parseInt(STD_value, 8);

                    //substitute ellipsis
                    if(std_value==188)
                        char_value=ellipsis;
                    
					STD_char_encoding_table[std_value] =char_value;
					unicode_char_decoding_table[STD][std_value]=NAME;
					
					glyphToChar[STD].put(NAME, new Integer(std_value));
				
				}else if ((idx==PDF)&&(Character.isDigit(PDF_value.charAt(0)))) {
					std_value = Integer.parseInt(PDF_value, 8);

                    //substitute ellipsis
                    if(std_value==131)
                        char_value=ellipsis;
                    
					PDF_char_encoding_table[std_value] =char_value;
					unicode_char_decoding_table[PDF][std_value]=NAME;
					
				}else if ((idx== WIN)&&(Character.isDigit(WIN_value.charAt(0)))) {
					win_value = Integer.parseInt(WIN_value, 8);

                    //substitute ellipsis
                    if(win_value==133)
                        char_value=ellipsis;
                    
					WIN_char_encoding_table[win_value] =char_value;
					unicode_char_decoding_table[WIN][win_value]=NAME;
					
					glyphToChar[WIN].put(NAME, new Integer(win_value));


				}
				
				//save details for later
				unicode_name_mapping_table.put(NAME, char_value);
				
			
			}

            //add in alternative MAC space   312 octal == space
            if(idx==MAC)
            MAC_char_encoding_table[202]=" ";

            //add in alternative WIN values
            if(idx== WIN){
            	WIN_char_encoding_table[160]=" ";
            	WIN_char_encoding_table[255]="-";
            	
            	unicode_char_decoding_table[WIN][160]="space";
    			
            }
            
        } catch (Exception e) {
			LogWriter.writeLog(
					"Exception " + e + " reading lookup table for pdf  for "+idx);
		}
		
		if(input_stream!=null){
			try{
				input_stream.close();
			}catch (Exception e) {
				LogWriter.writeLog(
						"Exception " + e + " reading lookup table for pdf  for abobe map");
			}		
		}
	}
	
	/**used internally when we needed to convert bytes to MacROman to build new tables*/
	/**private static String byteToEncodedString(String value,String enc) throws Exception{
		
		String s=null;
		
			//Create the encoder and decoder for ISO-8859-1
	    Charset charset = Charset.forName(enc);
	    CharsetDecoder decoder = charset.newDecoder();
	    CharsetEncoder encoder = charset.newEncoder();
	    
	        // Convert a string to ISO-LATIN-1 bytes in a ByteBuffer
	        // The new ByteBuffer is ready to be read.
	    		java.nio.ByteBuffer bbuf = encoder.encode(CharBuffer.wrap("a"));
	    		bbuf.clear();
	        //java.nio.ByteBuffer bbuf=new ByteBuffer();
	        bbuf.put(0,(byte) Integer.parseInt(value,16));
	        // Convert ISO-LATIN-1 bytes in a ByteBuffer to a character ByteBuffer and then to a string.
	        // The new ByteBuffer is ready to be read.
	        CharBuffer cbuf = decoder.decode(bbuf);
	        s = cbuf.toString();
	        
	        System.out.println(Integer.toHexString((int)s.charAt(0)));
	   
	    return s;
	}*/
	
	static public String  getEncodedChar(int font_encoding,int char_int){
		
		String return_character=null; 
		
		if(font_encoding== WIN)
			return_character=WIN_char_encoding_table[char_int];
		else if(font_encoding==STD)
			return_character=STD_char_encoding_table[char_int];
		else if(font_encoding==MAC)
			return_character=MAC_char_encoding_table[char_int];
		else if(font_encoding==PDF)
			return_character=PDF_char_encoding_table[char_int];
		else if(font_encoding==ZAPF)
			return_character=ZAPF_char_encoding_table[char_int];
		else if(font_encoding==SYMBOL)
			return_character=SYMBOL_char_encoding_table[char_int];	
		else if(font_encoding==MACEXPERT)
			return_character=MACEXPERT_char_encoding_table[char_int];
		
		if (return_character== null)
			return_character = "&#" + char_int + ';';
		
		return return_character;
	}
	
	/**flag fi a valid mac char - must have loaded mac encoding*/
	public static boolean isValidMacEncoding(int idx){
		
		//if(MAC_char_encoding_table[idx]!=null)
		//	System.out.println("mac="+MAC_char_encoding_table[idx]);
		
		return MAC_char_encoding_table[idx]!=null;
	}
	
	/**flag fi a valid win char - must have loaded win encoding*/
	public static boolean isValidWinEncoding(int idx){

		//if(WIN_char_encoding_table[idx]!=null)
		//	System.out.println("win="+WIN_char_encoding_table[idx]);
		
        return WIN_char_encoding_table[idx]!=null;
	}
	
	/**load required mappings*/
	public  static void checkLoaded( int enc) {
		
		
		/**load mapping if we need it and initialise storage*/
		if((enc==MAC)&&(MAC_char_encoding_table==null)){
			
			MAC_char_encoding_table = new String[335];
			readStandardMappingTable(enc);
			
		}else if((enc== WIN)&&(WIN_char_encoding_table==null)){
			
			WIN_char_encoding_table = new String[335];
			readStandardMappingTable(enc);
			
		}else if((enc==STD)&&(STD_char_encoding_table==null)){
			
			STD_char_encoding_table = new String[335];
			readStandardMappingTable(enc);
		
		}else if((enc==PDF)&&(PDF_char_encoding_table==null)){
			
			PDF_char_encoding_table = new String[335];
			readStandardMappingTable(enc);
		
		}else if((enc==SYMBOL)&&(SYMBOL_char_encoding_table==null)){
			
			SYMBOL_char_encoding_table = new String[335];
			readStandardMappingTable(SYMBOL, "symbol.cfg");
			
		}else if((enc==ZAPF)&&(ZAPF_char_encoding_table==null)){
			
			ZAPF_char_encoding_table = new String[335];
			readStandardMappingTable(ZAPF, "zapf.cfg");
			
		}else if((enc==MACEXPERT)&&(MACEXPERT_char_encoding_table==null)){
			
			MACEXPERT_char_encoding_table = new String[335];
			readStandardMappingTable(MACEXPERT, "mac_expert.cfg");
			
		}
	}
	
	/////////////////////////////////////////////////////////////////////////
	/**
	 * read default widths for 14 standard fonts supplied
	 * by Adobe
	 */
	final private static void loadStandardFont(int i) throws IOException{
		String line = "", next_command = "", char_name = "";
		BufferedReader input_stream = null;
		float width = 200;
		//int x1 = 0, y1 = 0, x2 = 0, y2 = 0;
		//int b_x1 = 0, b_y1 = 0, b_x2 = 0, b_y2 = 0;

		// loop to read widths from default fonts
		{
			//open the file
			input_stream =
			new BufferedReader(
					new InputStreamReader(
							loader.getResourceAsStream(
									"org/jpedal/res/pdf/defaults/"
									+ files_names[i]
									+ ".afm"),
							enc));
			
			boolean char_mapping_table = false;
			//flag if in correct part of file
			while (true) { //read the lines and extract width info
				line = input_stream.readLine();
				
				if (line == null)
					break;
				if (line.startsWith("EndCharMetrics"))
					char_mapping_table = false;

				//extract bounding box
				if (line.startsWith("FontBBox")) {

                    float[] fontBBox=new float[4];
                    StringTokenizer values = new StringTokenizer(line);
					//drop FontBBox
                    values.nextToken();

                    for(int a=0;a<4;a++)
                    fontBBox[a] = Integer.parseInt(values.nextToken());

                    fontBounds.put(files_names[i],fontBBox);

					
                }
				
				if (char_mapping_table == true) { //extract info from the line
					StringTokenizer values = new StringTokenizer(line, " ;");

					//extract values
					while (values.hasMoreTokens()) {
						next_command = values.nextToken();
						/**
						if (next_command.equals("C"))
							char_number = values.nextToken();*/
						if (next_command.equals("WX"))
							width = Float.parseFloat(values.nextToken()) / 1000;
						else if (next_command.equals("N"))
							char_name = values.nextToken();
						/**
						if (next_command.equals("B")) {
							x1 = Integer.parseInt(values.nextToken());
							y1 = Integer.parseInt(values.nextToken());
							x2 = Integer.parseInt(values.nextToken());
							y2 = Integer.parseInt(values.nextToken());
						}*/
					}

					//store width
					widthTableStandard.put(files_names_bis[i] + char_name,new Float(width));
					widthTableStandard.put(files_names[i]  + char_name,new Float(width));

				}
				if (line.startsWith("StartCharMetrics"))
					char_mapping_table = true;
			}
		}
		
		if(input_stream!=null){
			try{
				input_stream.close();
			}catch (Exception e) {
				LogWriter.writeLog(
						"Exception " + e + " reading lookup table for pdf  for abobe map");
			}		
		}
	}
	
	/**
	 * check if one of 14 standard fonts and if so load widths 
	 */
	protected static void loadStandardFontWidth(String fontName){
		
		//get name of font if standard
		Integer fileNumber=(Integer) standardFileList.get(fontName);
		
		if((fileNumber!=null)&&(standardFontLoaded.get(fileNumber)==null)){
			standardFontLoaded.put(fileNumber,"x");
			try{
				loadStandardFont(fileNumber.intValue());
			} catch (Exception e) {
				LogWriter.writeLog("[PDF] " + e + " problem reading lookup table for pdf font "+fontName+ ' ' +fontName);
			}		
		}
	}

	/**
	 * converts glyph into character index
	 */
	public static int lookupCharacterIndex(String glyph,int idx) {
		
		Object value=glyphToChar[idx].get(glyph);
		if(value==null)
			return 0;
		else
			return ((Integer)value).intValue();
	}

	/**
	 * load the adobe unicode mapping table for truetype fonts
	 */
	private static void loadAdobeMap() {
		
		BufferedReader input_stream =null;
		
		/**load if not already loaded*/
		if(adobeMap==null){
			try {
				//initialise 
				adobeMap=new Hashtable();
			
				input_stream =new BufferedReader(
						new InputStreamReader(
								loader.getResourceAsStream(
								"org/jpedal/res/pdf/glyphlist.cfg"),
								enc));
	
				// trap problems
				if (input_stream == null)
					LogWriter.writeLog(
					"Unable to open glyphlist.cfg from jar");
				
				//read in lines and place in map tables for fast lookup
				while (true) {
					String line = input_stream.readLine();
					if (line == null)
						break;
					
					if((!line.startsWith("#"))&&(line.indexOf(';')!=-1)){
						
						StringTokenizer vals=new StringTokenizer(line,";");
						String key=vals.nextToken();
						String operand=vals.nextToken();
						int space=operand.indexOf(' ');
						if(space!=-1)
							operand=operand.substring(0,space);
						int opVal=Integer.parseInt(operand,16);
						adobeMap.put(key,new Integer(opVal));
						
						unicode_name_mapping_table.put(key,Character.toString((char)opVal));
						
					}
				}
			} catch (Exception e) {
				LogWriter.writeLog(
						"Exception " + e + " reading lookup table for pdf  for abobe map");
				e.printStackTrace();
			}
		}
		
		if(input_stream!=null){
			try{
				input_stream.close();
			}catch (Exception e) {
				LogWriter.writeLog(
						"Exception " + e + " reading lookup table for pdf  for abobe map");
			}		
		}
		
	}
	/**
	 * @return Returns the adobe mapping for truetype case 3,1
	 */
	public static int getAdobeMap(String key){

        Object value=adobeMap.get(key);
        if(value==null)
            return -1;
        else
            return((Integer)value).intValue();
		
	}

    /**
     * @return Returns a boolean if in Adobe map
     */
    public static boolean isValidGlyphName(String key){

        if(key==null)
            return false;
        else
            return(adobeMap.get(key)!=null);

    }


    /**
     * see if a standard font (ie Arial, Helvetica)
     */
    public static boolean isStandardFont(String fontName, boolean includeWeights) {

        boolean isStandard=(standardFileList.get(fontName)!=null);

        if(!isStandard && includeWeights){

            int ptr=fontName.indexOf("-");
            if(ptr!=-1){
                String rawName=fontName.substring(0,ptr);
                //System.out.println(ptr+"<>"+fontName+"<>"+rawName+"<>"+standardFileList.get(fontName)!=null);
                isStandard=(standardFileList.get(rawName)!=null);
            }

        }
        return isStandard;
    }

    /**
     * open font , read postscript, and return Map with font details
     */
    public static Map getFontDetails(int type, String subFont) {

    	Map fontDetails=new HashMap();
    	
        /**read in font data*/
        if(type==TRUETYPE || type==TRUETYPE_COLLECTION){

            TTGlyphs currentFont=new TTGlyphs();

            //FontData closed in routine
            currentFont.addStringValues(new FontData(subFont),fontDetails);

        }

        return fontDetails;
    }
    
    /**
     * open font , read postscript, family or full names and return array
     */
    public static String[] readNamesFromFont(int type, String subFont,int mode) throws Exception {

        String[] fontNames=new String[1];
        fontNames[0]="";


        /**read in font data*/
        if(type==TRUETYPE || type==TRUETYPE_COLLECTION){

            TTGlyphs currentFont=new TTGlyphs();

          //FontData closed in routine
            fontNames=currentFont.readFontNames(new FontData(subFont),mode);

        }else if(type==TYPE1){

            T1Glyphs currentFont=new T1Glyphs();

          //FontData closed in routine
            fontNames=currentFont.readFontNames(new FontData(subFont),mode);

        }

        return fontNames;
    }

    //allow for number value as well as glyph name (ie 68 rather than D)
    public static String convertNumberToGlyph(String mappedChar, boolean containsHexNumbers) {
        
    	int charCount=mappedChar.length();

        boolean isNumber=true; //assume true and disprove

        //System.out.println(mappedChar+" "+containsHexNumbers);
        
        if(charCount==2 || charCount==3){
	        for(int ii=0;ii<charCount;ii++){ //test all values to see if number
	            char c=mappedChar.charAt(ii);
	            if(c<'0' || c>'9'){  //fail on first and exit loop
	                isNumber=false;
	                ii=charCount;
	            }
	        }

            if(isNumber)
                mappedChar= String.valueOf((char) Integer.parseInt(mappedChar));
        }
		/**/
		
        return mappedChar;
    }

    /**
     * turn hashed key value into String
     */
    public static String getFontypeAsString(int fontType) {
        switch(fontType){

            case TRUETYPE:
                return "TrueType";        
            case TYPE1:
                return "Type1";
            case TYPE3:
                return "Type3";

            case CIDTYPE0:
                return "CIDFontType0";
            case CIDTYPE2:
                return "CIDFontType2";

            default:
                return "Unknown";
        }
    }
}
