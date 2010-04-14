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
* Fonts.java
* ---------------
*/
package org.jpedal.utils;

import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * general font routines to create XML font token or extract information
 * from the token or create a font object
 */
public class Fonts {
	
	public static String fe="</font>";
	
	public static String fb="<font ";
	
	//////////////////////////////////////////////////////////////////////////
	/**
	 * Take data which has just been joined and tidy up fonts<br> Removes
	 * duplicate font commands where a font is turned off with /FONT tag and
	 * then turned back on again <BR>
	 * This arises because we put font tokens into the data and then may merge
	 * it together.
	 */
	final public static String cleanupTokens(String input) {
		StringBuffer output_data = new StringBuffer();
		String current_token = "", current_font = "";
		int pointer = 0;
		boolean next_font_is_identical = false;
		StringTokenizer data_As_tokens = new StringTokenizer(input, "<>", true);
		String next_item = data_As_tokens.nextToken();
			
		//work through all tokens in the data
		while (data_As_tokens.hasMoreTokens()) {

			if ((next_item.equals("<")) & ((data_As_tokens.hasMoreTokens()))) {

				//get token
				current_token =
					next_item
						+ data_As_tokens.nextToken()
						+ data_As_tokens.nextToken();
				pointer = pointer + current_token.length();
				//where we are in original data
				next_item = ""; //set to no value

				//track font in use so we can eliminate font off/same font on in data
				if ((current_token.startsWith(fb)))
					current_font = current_token;
/**
				//reset font tracking on td so table fonts preserved
				if ((current_token.toLowerCase().startsWith("</td"))|
				(current_token.toLowerCase().startsWith("</right"))
				|(current_token.toLowerCase().startsWith("</center"))|(current_token.toLowerCase().startsWith("</p"))) {
					current_font = "";
					next_font_is_identical = false;
				}
*/
										
					//ignore if next font the same - otherewise keep
				if ((current_token.equals(fe))) {
					
					/**don't lose if if we are about to end a token pair 
					 * 
					 */
					int nextToken=input.indexOf('<', pointer - 1);
					int nextEndToken=input.indexOf("</", pointer - 1);

					if(nextToken==nextEndToken){
						output_data.append(current_token);
					}else{
					
						int next_font_pointer_s =input.indexOf(fb, pointer - 1);
						int next_font_pointer_e =input.indexOf('>', next_font_pointer_s);
						next_font_is_identical = false;
	
						if ((next_font_pointer_s != -1)
							&& (next_font_pointer_e != -1)) {
							String next_font =input.substring(next_font_pointer_s,next_font_pointer_e + 1);
							if (next_font.equals(current_font))
								next_font_is_identical = true;
						}
						
						//add if no matches
						if (next_font_is_identical == false)
							output_data.append(current_token);
					}
				} else if (
					(current_token.startsWith(fb))
						& (next_font_is_identical == true))
					next_font_is_identical = false; //ignore next font command
				else
					output_data.append(current_token);
			} else {
				
				//not token so put in data
				output_data.append(next_item);
				pointer = pointer + next_item.length();
				//where we are in original data
				next_item = "";
			}
			
			//read next item if not read already
			if ((data_As_tokens.hasMoreTokens())){
				next_item = data_As_tokens.nextToken();
			
				/**allow for it being the last item*/
				if (!data_As_tokens.hasMoreTokens()){
					//not token so put in data
					output_data.append(next_item);
					pointer = pointer + next_item.length();
				}
				
			}
		}

		return output_data.toString();
	}
	/////////////////////////////////////////////////////////////////////////
	/**
	 * get list of fonts from string so we can generate a list of fonts used
	 */
	final public static String extractFontsList(String input) {
		StringBuffer output_data = new StringBuffer();
		String current_token = "", next_item = "";
		Map fonts_found = new Hashtable();
		StringTokenizer data_As_tokens = new StringTokenizer(input, "<>", true);

		//work through all tokens
		while (data_As_tokens.hasMoreTokens()) {
			next_item = data_As_tokens.nextToken();

			//get token
			if ((next_item.equals("<")) & ((data_As_tokens.hasMoreTokens()))) {
				current_token =
					next_item
						+ data_As_tokens.nextToken()
						+ data_As_tokens.nextToken();

				//see if its a start font
				if ((current_token.startsWith(fb))) {

					//add to list if not there
					//map used to track as faster
					if (fonts_found.get(current_token) == null) {
						fonts_found.put(current_token, "x");
						output_data.append(current_token); //add to data
					}
				}
			}
		}
		return output_data.toString();
	}
	
	///////////////////////////////////////////////////////////////////////
	/**
	 * get smallest font from string by size
	 */
	final public static String extractSmallestFont(String input) {
		String current_token = "", next_item = "", return_value = "";
		int size = 100000;
		Map fonts_found = new Hashtable();
		StringTokenizer data_As_tokens = new StringTokenizer(input, "<>", true);
		while (data_As_tokens.hasMoreTokens()) {
			next_item = data_As_tokens.nextToken();

			//get token
			if ((next_item.equals("<")) & ((data_As_tokens.hasMoreTokens()))) {
				current_token =
					next_item
						+ data_As_tokens.nextToken()
						+ data_As_tokens.nextToken();
				if ((current_token.startsWith(fb))) { //track fonts

					//examine if not already checked
					if (fonts_found.get(current_token) == null) {

						//use hashmap to flag font tokens we have already seen
						fonts_found.put(current_token, "x");

						//see if smaller and set new value if this token smaller
						int new_size = extractFontSize(current_token);
						if (new_size < size) {
							size = new_size;
							return_value = current_token;
						}
					}
				}
			}
		}
		return return_value;
	}
	//////////////////////////////////////////////////////////////////////////
	/**
	 * extract font size from font string. If a value is not found
	 * in the first value, the second will be used.
	 */
	final public static String getActiveFontTag(
		String raw_string,
		String full_value) {
		int start, end;
		String return_value = "";
		start = raw_string.lastIndexOf(fb);

		if (start > -1) {
			end = raw_string.indexOf("\">", start);
			if (end > 0)
				return_value = raw_string.substring(start, end + 2);
		} else {
			start = full_value.lastIndexOf(fb);

			if (start > -1) {
				end = full_value.indexOf("\">", start);
				if (end > 0)
					return_value = full_value.substring(start, end + 2);
			}
		}

		return return_value;
	}
	//////////////////////////////////////////////////////////////////////////
	/**
	 * extract font size from font string
	 */
	final public static int extractFontSize(String raw_string) {
		int start, end;
		int return_value = -1;
		start = raw_string.indexOf("style=\"") + 17;
		if (start > 17) {
			end = raw_string.indexOf("pt", start);
			if (end > 0)
				return_value =
					Integer.parseInt(raw_string.substring(start, end));
		}
		return return_value;
	}

	/////////////////////////////////////////////////////////////////////////
	/**
	 * extract font name from XML font string
	 */
	final public static String extractFontName(String raw_string) {
		int pointer_start, pointer_end;
		String return_string = "";
		pointer_start = raw_string.indexOf("face=\"") + 6;
		if (pointer_start > 6) {
			pointer_end = raw_string.indexOf('\"', pointer_start);
			if (pointer_end > 0)
				return_string =
					raw_string.substring(pointer_start, pointer_end);
		}
		return return_string;
	}
	///////////////////////////////////////////////////////////////////////
	/**
	 * create XML font token for putting into stream
	 */
	final public static String createFontToken(
		String font_name,
		int font_size) {
		String font_token = "";

		//set font used and include styles for truetype (ie Arial,Bold)
		int pointer = font_name.indexOf(',');
		if (pointer != -1) {
			String weight = font_name.substring(pointer + 1);
			font_name = font_name.substring(0, pointer);
			font_token =fb+
				"face=\""
					+ font_name
					+ "\" style=\"font-size:"
					+ font_size
					+ "pt;font-style:"
					+ weight
					+ "\">";
		} else
			font_token =fb+
				"face=\""
					+ font_name
					+ "\" style=\"font-size:"
					+ font_size
					+ "pt\">";
		return font_token;
	}
	
	///////////////////////////////////////////////////////////////////////
	/**
	 * create XML font token for putting into stream
	 */
	final public static String createFontToken(
			String font_name,
			int font_size,int c,int m,int y,int k) {
		String font_token = "";

		//set font used and include styles for truetype (ie Arial,Bold)
		int pointer = font_name.indexOf(',');
		if (pointer != -1) {
			String weight = font_name.substring(pointer + 1);
			font_name = font_name.substring(0, pointer);
			font_token =fb+
			"face=\""
			+ font_name
			+ "\" style=\"font-size:"
			+ font_size
			+ "pt;font-style:"
			+ weight
			+ "\">";
		} else
			font_token =fb+
			"face=\""
			+ font_name
			+ "\" style=\"font-size:"
			+ font_size
			+ "pt\">";
		return font_token;
	}
}
