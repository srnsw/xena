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
* PdfGroupingAlgorithms.java
* ---------------
*/
package org.jpedal.grouping;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.jpedal.PdfDecoder;
import org.jpedal.parser.PdfStreamDecoder;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.exception.PdfException;
import org.jpedal.objects.PdfData;
import org.jpedal.objects.StoryData;
import org.jpedal.utils.Fonts;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Sorts;
import org.jpedal.utils.Strip;
import org.jpedal.utils.repositories.Vector_Float;
import org.jpedal.utils.repositories.Vector_Int;
import org.jpedal.utils.repositories.Vector_Object;
import org.jpedal.utils.repositories.Vector_Rectangle;
import org.jpedal.utils.repositories.Vector_String;

/**
 * Applies heuristics to unstructured PDF text to create content
 */
public class PdfGroupingAlgorithms {
    
    private boolean includeHTMLtags=false;
    
    public int wordDetectionTechnique = 0;
    
    public final int USER_DEFINED_LIST_ONLY = 0;
    public final int SURROUND_BY_ANY_PUNCTUATION = 1;
    
    public static String SystemSeparator = System.getProperty("line.separator");
    
    public PdfGroupingAlgorithms() {
		
	}
	
	/** ==============START OF ARRAYS================ */
	/**
	 * content is stored in a set of arrays. We have tried various methods (ie
	 * create composite object, etc) and none are entirely satisfactory. The
	 * beauty of this method is speed.
	 */

	/**
	 * flag to show this item has been merged into another and should be
	 * ignored. This allows us to repeat operations on live elements without
	 * lots of deleting.
	 */
	private boolean[] isUsed;
	
	/**
	 * List of punctuation to allow before or after text
	 * and still count as a whole word during search
	 * Currently supported are </br>
	 * (Space) ( ) ! ; . , � / - = + ? \ " ' � �
	 */
	private String punctuation = " ()!;.,�/-=+?\\\"\'�����[]{}";

	/** co-ords of object (x1,y1 is top left) */
	private float[] f_x1, f_x2, f_y1, f_y2;
	
	/**track if we removed space from end*/
	private boolean[] hadSpace;
	
	/**hold colour info*/
	private String[] f_colorTag;
	
	/**hold writing mode*/
	private int[] writingMode;
	
	/**hold move type*/
	private int[] moveType;

	/** font sizes in pixels */
	private int[] fontSize;

	/** amount of space a space uses in this font/size */
	private float[] spaceWidth;

	/** actual text */
	private StringBuffer[] content;

	/** raw number of text characters */
	private int[] textLength;

	/** ==============END OF ARRAYS================ */

	/**
	 * unicode characters which are spaces (ie hyphens, soft-hyphens, hypens in
	 * different character sets
	 */
	private String hyphen_values = "";

	/**
	 * handle on page data object. We extract data from this into local arrays
	 * and return grouped content into object at end. This is done for speed.
	 */
	private PdfData pdf_data;

	/** flag to show if output for table is CSV or XHTML */
	private boolean isXHTML = true;

	/** slot to insert next value - used when we split fragments for table code */
	private int nextSlot;

	/** vertical breaks for table calculation */
	private Vector_Int lineBreaks = new Vector_Int();

	/** holds details as we scan lines for table */
	private Vector_Object lines;

	/** lookup table used to sort into correct order for table */
	private Vector_Int lineY2;

	/**
	 * marker char used in content (we bury location for each char so we can
	 * split)
	 */
	private static final String MARKER = StoryData.marker;
	public static char MARKER2= MARKER.charAt(0);

	/** counters for cols and rows and pointer to final object we merge into */
	private int max_rows = 0, master = 0;
	
	/**flag to show color info is being extracted*/
	private boolean colorExtracted=false;
	
	/** used to calculate correct order for table lines */
	private int[] line_order;

	/** amount we resize arrays holding content with if no space */
	private final static int increment = 100;

	//<start=storypad>
	private boolean IS_LEGACY=true;
	//<end-storypad>

	/**Flag used to debug new text routines*/
	public static boolean oldTextExtraction=false;

	public static boolean useUnrotatedCoords;

	/**end points if text located*/
	private float[] endPoints;

	/**flag to show if tease created on findText*/
	private boolean includeTease;

	/**teasers for findtext*/
	private String[] teasers;

	/**track last word for teaser*/
	private StringBuffer tease;

	/**point reached scanning along line in looking for text*/
	private float endX;

	private List multipleTermTeasers = new ArrayList();

	private boolean usingMultipleTerms = false;

	/** fields used to track the position of whole words when searching for whole words only */
	private String isWholeWordLastLine;
	private int isWholeWordCurrentIndex;
	private Point isWholeWordPosition;

	private boolean removeInvalidXMLValues = true;

	/*
	 * Variables to allow cross line search results
	 */
	/**Search for results across multiple lines*/
	private boolean findAcrossLines = false;
	/**Only check start of line as we already have partial result at end of last*/
	private boolean onlyCheckStart = false;
	/**Partial search result found at end of line*/
	private boolean foundAcrossLine = false;
	/**Remainder of search value to find*/
	private String remainderOfSearch = "";
	/**Coords of first partial search result*/
	private float[] partialFindCoords;
	/**Teaser of first partial search result*/
	private String partialFindTeaser;
	/**Value placed between result areas to show they are part of the same result*/
	private int linkedSearchAreas=-101;
	
	/** create a new instance, passing in raw data */
	public PdfGroupingAlgorithms(PdfData pdf_data) {
		this.pdf_data = pdf_data;
		
		colorExtracted=pdf_data.isColorExtracted();
	}
	public void setSeparator(String sep){
		SystemSeparator = sep;
	}
	/**
	 * workout if we should use space, CR or no separator when joining lines
	 */
	final private String getLineDownSeparator(
		int l,
		int c,
		StringBuffer rawLine1,
		StringBuffer rawLine2) {

		String returnValue = " "; //space is default

		boolean hasUnderline = false;
		/**not currently live
		hasUnderline =page_data.testHorizontalBetween(
				-1,f_x1[l],f_x2[l],f_y1[l],f_y2[l],f_x1[c],f_x2[c],f_y1[c],f_y2[c]); */

		/**get 2 lines without any XML or spaces so we can look at last char*/
		StringBuffer line1,line2;
		if(PdfDecoder.isXMLExtraction()){
			line1 = Strip.stripXML(rawLine1);
			line2 = Strip.stripXML(rawLine2);
		}else{
			line1 = Strip.trim(rawLine1);
			line2 = Strip.trim(rawLine2);
		}
		
		/**get lengths and if appropriate perform tests*/
		int line1Len = line1.length();
		int line2Len = line2.length();
		//System.out.println(line1Len+" "+line2Len);
		if((line1Len>1)&&(line2Len>1)){
			//System.out.println("a");
			/**get chars to test*/
			char line1Char2 = line1.charAt(line1Len - 1);
			char line1Char1 = line1.charAt(line1Len - 2);
			char line2Char1 = line2.charAt(0);
			char line2Char2 = line2.charAt(1);
			//System.out.println("b");
			//deal with hyphenation first - ignore unless :- or space-
			if (hyphen_values.indexOf(line1Char2) != -1) {
				returnValue = ""; //default of nothing
				if (line1Char1 == ':')
					returnValue = "\n";
				if (line1Char2 == ' ')
					returnValue = " ";
	
				//paragraph breaks if full stop and next line has ascii char or Capital Letter
			} else if (
				((line1Char1 == '.') | (line1Char2 == '.'))
					& (Character.isUpperCase(line2Char1)
						| (line2Char1 == '&')
						| Character.isUpperCase(line2Char2)
						| (line2Char2 == '&'))){
				if(PdfDecoder.isXMLExtraction())
					returnValue = "<p></p>\n";
				else
					returnValue="\n";
			}

		}
		
		//add an underline if appropriate
		if (hasUnderline){
			if(PdfDecoder.isXMLExtraction())
				returnValue = returnValue + "<p></p>\n";
			else
				returnValue=returnValue+ '\n';
		}
		
		return returnValue;
	}

	/**
	 * remove shadows from text created by double printing of text and drowned
	 * items where text inside other text
	 */
	private final void cleanupShadowsAndDrownedObjects(boolean avoidSpaces) {

		//get list of items
		int[] items = getUnusedFragments();
		int count = items.length;
		int c, n;
		String separator = "";
        float diff=0;

        //work through objects and eliminate shadows or roll together overlaps
		for (int p = 0; p < count; p++) {

			//master item
			c = items[p];

			//ignore used items
			if (isUsed[c] == false) {

				//work out mid point in text
				float midX = (f_x1[c] + f_x2[c]) / 2;
				float midY = (f_y1[c] + f_y2[c]) / 2;
				
				for (int p2 = p + 1;p2 < count;p2++) {

					//item to test against
					n = items[p2];
					if ((isUsed[n] == false) && (isUsed[c] == false)) {

						float fontDiff=this.fontSize[n]-fontSize[c];
						if(fontDiff<0)
							fontDiff=-fontDiff;

                        diff = (f_x2[n] - f_x1[n]) - (f_x2[c] - f_x1[c]);
                        if(diff<0)
                                diff=-diff;

                        /** stop spurious matches on overlapping text*/
						if (fontDiff==0 && (midX > f_x1[n])&& (midX < f_x2[n])
							&& (diff< 10)
							&& (midY < f_y1[n])&& (midY > f_y2[n])) {
							
							isUsed[n] = true;
							
							//pick up drowned text items (item inside another)			
						} else {
				
							boolean a_in_b =
								(f_x1[n] > f_x1[c])&& (f_x2[n] < f_x2[c])
									&& (f_y1[n] < f_y1[c])&& (f_y2[n] > f_y2[c]);
							boolean b_in_a =
								(f_x1[c] > f_x1[n])&& (f_x2[c] < f_x2[n])
									&& (f_y1[c] < f_y1[n])&& (f_y2[c] > f_y2[n]);
							
							//merge together
							if ((a_in_b) || (b_in_a)) {
								//get order right - bottom y2 underneath
								if (f_y2[c] > f_y2[n]) {
									separator =getLineDownSeparator(n,c,content[c],content[n]);
									if((avoidSpaces==false)||(separator.indexOf(' ')==-1)){
										merge(c,n,separator,true);
									}
								} else {
									separator =getLineDownSeparator(n,c,content[n],content[c]);
									if((avoidSpaces==false)||(separator.indexOf(' ')==-1)){
										merge(n,c,separator,true);
									}
								}
								
								//recalculate as may have changed
								midX = (f_x1[c] + f_x2[c]) / 2;
								midY = (f_y1[c] + f_y2[c]) / 2;
								
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * general routine to see if we add a space between 2 text fragments
	 */
	final private String isGapASpace(int c, int l, float actualGap,boolean addMultiplespaceXMLTag,int writingMode) {
		String sep = "";
		float gap;

		//use smaller gap
		float gapA = spaceWidth[c] * fontSize[c];
		float gapB = spaceWidth[l] * fontSize[l];

		if (gapA > gapB)
			gap = gapB;
		else
			gap = gapA;


        int spaceCount=0;
        if(PdfStreamDecoder.runningStoryPad){

            spaceCount = (int) (actualGap / (gap / 1000));

        }else{
            gap = (actualGap / (gap / 1000));

            //Round values to closest full integer as float -> int conversion rounds down
            if(gap > 0.6f && gap<1)
                gap = 1;

            spaceCount = (int) gap;
        }
		if (spaceCount > 0)
			sep = " ";

		/** add an XML tag to flag multiple spaces */
		if ((spaceCount > 1)&&(addMultiplespaceXMLTag)&&((writingMode==PdfData.HORIZONTAL_LEFT_TO_RIGHT)|(oldTextExtraction)))
			sep = " <SpaceCount space=\"" + spaceCount + "\" />";
        //@kieran
        //else 
        	//if (spaceCount > 0)
        		//for(int s=0; s!=spaceCount; s++)
        			//sep = sep+" ";

		return sep;
	}

	/** generic decode merely clean up data and remove our embedded information */
	final public void cleanupText(PdfData pdf_data) {
		
		/**get local copy of data*/
		this.pdf_data = pdf_data;

		/**copy data into local arrays*/
		copyToArrays();

		//removed the embedded widths (set by flag in PdfObjects)
		removeEncoding();

		//create finished data in PdfData object
		writeFromArrays();
	}

	/**
	 * merge 2 text fragments together and update co-ordinates
	 */
	final private void merge(int m,int c,String separator,boolean moveFont) {

			//update co-ords
			if (f_x1[m] > f_x1[c])
				f_x1[m] = f_x1[c];
			if (f_y1[m] < f_y1[c])
				f_y1[m] = f_y1[c];
			if (f_x2[m] < f_x2[c])
				f_x2[m] = f_x2[c];
			if (f_y2[m] > f_y2[c])
				f_y2[m] = f_y2[c];

			if(PdfDecoder.isXMLExtraction()){
				String test=Fonts.fe;

				//add color tag if needed and changes
				if(colorExtracted)
					test=Fonts.fe+GenericColorSpace.ce;

				//move </Font> if needed and add separator
				if ((moveFont) && (content[m].toString().lastIndexOf(test)!=-1)) {
					String master = content[m].toString();
					content[m] =new StringBuffer(master.substring(0, master.lastIndexOf(test)));
					content[m].append(separator);
					content[m].append(master.substring(master.lastIndexOf(test)));	
				} else{
					content[m].append(separator);	
				}

                //Only map out space if text length is longer than 1
				if(textLength[c]>1 && content[m].toString().endsWith(" ")){
					content[m].deleteCharAt(content[m].lastIndexOf(" "));
				}
				//use font size of second text (ie at end of merged text)
				fontSize[m] = fontSize[c];
				
				//add together
				content[m] = content[m].append(content[c]);
				
				//track length of text less all tokens
				textLength[m] = textLength[m] + textLength[c];

				//set objects to null to flush and log as used
				isUsed[c] = true;		
				content[c] = null;
			}else{

				//use font size of second text (ie at end of merged text)
				fontSize[m] = fontSize[c];

				//add together
				content[m] = content[m].append(separator + content[c]);

				//track length of text less all tokens
				textLength[m] = textLength[m] + textLength[c];

				//set objects to null to flush and log as used
				isUsed[c] = true;		
				content[c] = null;
			}
	}

	/**
	 * put merged data back from local arrays into PdfData object. This allows
	 * us to eliminate items which have been merged. We could have used a
	 * linked list but this allows us maximum speed on merging.
	 */
	final private void writeFromArrays() {

		//holds data
		StringBuffer processedValue;
		int count = 1;

		//get list of items
		int[] items = getUnusedFragments();
		count = items.length;

		//create new data object
		java.util.List updatedData = new Vector();

		//now write data back into PdfData
		for (int pointer = 0; pointer < count; pointer++) {
			int i = items[pointer];
			processedValue = content[i]; //get
																   // processed
																   // data

			//allow for various text angles in calculation (make sure x1,y1
			// always top left)
			int y1 = (int) f_y1[i];
			int y2 = (int) f_y2[i];
			if (y1 < y2) {
				int temp = y1;
				y1 = y2;
				y2 = temp;
			}

			
			
			//output text with values
			if (processedValue.toString().trim().length() > 0) {
				Map new_value = new Hashtable();

				//cleanup data by removing duplicate font tokens
				if(PdfDecoder.isXMLExtraction())
				processedValue =new StringBuffer(
					Fonts.cleanupTokens(processedValue.toString()));

				//write out text object values
				new_value.put("content", processedValue);

				//information to rebuild without having to do whole stream 
				//(All stored as String values
				new_value.put("x1", String.valueOf(f_x1[i]));
				new_value.put("x2", String.valueOf(f_x2[i]));
				new_value.put("y1", String.valueOf(f_y1[i]));
				new_value.put("y2", String.valueOf(f_y2[i]));
				if(this.colorExtracted)
				new_value.put("color",f_colorTag[i]);
				updatedData.add(new_value);
			}
		}

		//update data
		pdf_data.resetTextList(updatedData);
	}
	
	/**
	 * remove width data we may have buried in data
	 */
	final private void removeEncoding() {

		// get list of items
		int[] items = getUnusedFragments();
		int count = items.length;
		int current;

		// work through objects and eliminate shadows or roll together overlaps
		for (int p = 0; p < count; p++) {

			// master item
			current = items[p];

			// ignore used items and remove widths we hid in data
			if (isUsed[current] == false)
				content[current] = removeHiddenMarkers(current);
		}
	}

	/**
	 * put raw data into Arrays for quick merging breakup_fragments shows if we
	 * break on vertical lines and spaces
	 */
	final private void copyToArrays() {

		colorExtracted=pdf_data.isColorExtracted();
		
		int count = pdf_data.getRawTextElementCount();

		//local lists for faster access
		isUsed = new boolean[count];
		fontSize = new int[count];
		writingMode=new int[count];
		spaceWidth = new float[count];
		content = new StringBuffer[count];
		textLength = new int[count];

		f_x1 = new float[count];
		f_colorTag=new String[count];
		f_x2 = new float[count];
		f_y1 = new float[count];
		f_y2 = new float[count];
		moveType=new int[count];


		
		//set values
		for (int i = 0; i < count; i++) {
			content[i] = new StringBuffer(pdf_data.contents[i]);

			fontSize[i] = pdf_data.f_end_font_size[i];
			writingMode[i]=pdf_data.f_writingMode[i];
			f_x1[i] = pdf_data.f_x1[i];
			f_colorTag[i]=pdf_data.colorTag[i];
			f_x2[i] = pdf_data.f_x2[i];
			f_y1[i] = pdf_data.f_y1[i];
			f_y2[i] = pdf_data.f_y2[i];
			moveType[i]=pdf_data.move_command[i];

			spaceWidth[i] = pdf_data.space_width[i];
			textLength[i] = pdf_data.text_length[i];
		}
	}
	
	/**
	 * get list of unused fragments and put in list
	 */
	private int[] getUnusedFragments() {
		int total_fragments = isUsed.length;

		//get unused item pointers
		int ii = 0;
		int temp_index[] = new int[total_fragments];
		for (int i = 0; i < total_fragments; i++) {
			if (isUsed[i] == false) {
				temp_index[ii] = i;
				ii++;
			}
		}
		
		//put into correctly sized array
		int[] items = new int[ii];
        System.arraycopy(temp_index, 0, items, 0, ii);
		return items;
	}


	/**
	 * strip the hidden numbers of position we encoded into the data
	 * (could be coded to be faster by not using Tokenizer)
	 */
	private StringBuffer removeHiddenMarkers(int c) {

		//make sure has markers and ignore if not
		if (content[c].indexOf(MARKER) == -1)
			return content[c];
		
		//strip the markers
		StringTokenizer tokens =new StringTokenizer(content[c].toString(), MARKER, true);
		String temp;
		StringBuffer processedData = new StringBuffer();
		
		//with a token to make sure cleanup works
		while (tokens.hasMoreTokens()) {

			//strip encoding in data
			temp = tokens.nextToken(); //see if first marker
			
			if (temp.equals(MARKER)) {
				tokens.nextToken(); //point character starts
				tokens.nextToken(); //second marker
				tokens.nextToken(); //width
				tokens.nextToken(); //third marker

				//put back chars
				processedData = processedData.append(tokens.nextToken());
				
			} else
				processedData = processedData.append(temp);
		}
		
		return processedData;
	}
	
	/**
	 * look for a value in data
	 */
	private ScanLinePair scanLineForValue(StringBuffer[] rawContents,StringBuffer line, String value,int x1,boolean isCaseSensitive,boolean matchWholeWordsOnly, float fx, float fy) {

		StringBuffer lastWord = null;// xWord=null;
		int pointer = 0, end = line.length();
		char c;
		char[] chars = line.toString().toCharArray();

		boolean isWholeWord = false;

		if (includeTease) {
			tease = new StringBuffer();
			// xWord=new StringBuffer();
			lastWord = new StringBuffer();
		}

		// run though the string extracting our markers and look at same time
		float x = -1, finalX = -1, currentX = 0;

		// strip the markers
		String temp_token, width, text;
		StringBuffer processed_data = new StringBuffer();

		// if not case sensitive convert value to find to lower case outside
		// loop
		if (!isCaseSensitive)
			value = value.toLowerCase();

		//with a token to make sure cleanup works
		while (pointer < end) {
			
			//find first marker
			while (pointer < end) {
				// System.out.println(line.charAt(pointer));
				if (chars[pointer] == MARKER2)
					break;
				pointer++;
			}

			if (chars[pointer] == MARKER2) {
				pointer++;

				// find second marker and get width
				int startPointer = pointer;
				while (pointer < end) {
					if (chars[pointer] == MARKER2)
						break;
					pointer++;
				}

				currentX = Float.parseFloat(line.substring(startPointer, pointer));
				pointer++;

				// find third marker
				startPointer = pointer;
				while (pointer < end) {
					if (chars[pointer] == MARKER2)
						break;

					pointer++;
				}
				width = line.substring(startPointer, pointer);
				pointer++;

				// find third marker
				startPointer = pointer;
				while (pointer < end) {
					if (chars[pointer] == MARKER2)
						break;

					pointer++;
				}

				text = line.substring(startPointer, pointer);

				// start comparison after character x position passes x1
				if (currentX < x1)
					continue;

				boolean hasToken = false;
				// strip any tokens
				if (PdfDecoder.isXMLExtraction()) {
					int p = text.indexOf('<');
					if (p != -1) {
						if (text.indexOf("<link:") != -1)
							hasToken = true;

						text = text.substring(0, p);
					}

					// rest tease
					// p=text.indexOf("<link:");
					// xWord=new StringBuffer();
					// lastWord.append(' ');
					// if(p!=-1){ //link in text

					// }
				}

				/**
				 * //allow for any spaces int p2=text.indexOf(" "); if(p2!=-1){
				 * text=text.substring(p2); //System.out.println(text+"<<--");
				 * 
				 * processed_data.setLength(0); }
				 */

				// if(processed_data.length()>0)
				// text=text.trim();
				if (includeTease) {

					// trim to last word
					int i = lastWord.lastIndexOf(" ");
					if (i != -1 && text.indexOf(' ') != -1) {
						// System.out.println(lastWord+"<>"+lastWord.substring(i+1,lastWord.length()));
						lastWord = new StringBuffer(lastWord.substring(i + 1, lastWord.length()));
					}

					// add on fragment
					lastWord.append(text);

					if (hasToken)
						lastWord.append(' ');
				}

				// put back chars
				processed_data.append(text);

				// value
				String test = processed_data.toString();// .trim();
				
				// if not case sensitive convert each value to lower case
				if (!isCaseSensitive)
					test = test.toLowerCase();
				
				/*
				 * We only want to check the start of the line.
				 * If at any point it doesn't match the value then stop.
				 */				
				if(onlyCheckStart && !(test.length()>1) && value.indexOf(test)!=0){
					break;
				}
				
				

				// set on if spaces at start or not set
				if (x == -1 || test.length() == 0)
					x = currentX;

				//find take-away if we search for takeaway
				int ptr2=test.length();
				
				if(findAcrossLines){
					if(pointer == end && test.length()>1 /*&& test.endsWith("-")*/ && value.indexOf(test.substring(0, test.length()-1)) ==0 && !value.equals(test)){
						foundAcrossLine = true;
						if(processed_data.toString().endsWith("-"))
							remainderOfSearch = value.substring(processed_data.toString().length()-1);
						else
							remainderOfSearch = value.substring(processed_data.toString().length());
					}else{
//						Set to false if issues occur with multiple highlights for result
						foundAcrossLine = false;
						remainderOfSearch = "";
					}
				}else{
					if(ptr2>0 && test.endsWith("-") && value.length()>0 && value.indexOf(test)==-1){  //ignore hyphens

						test=test.substring(0,ptr2-1);
						processed_data.deleteCharAt(ptr2-1);

					}
				}
				
				if (test.indexOf(value) != -1 || foundAcrossLine){
					finalX = x;
					endX = currentX + Float.parseFloat(width);

					if(matchWholeWordsOnly)
						if(foundAcrossLine)
							isWholeWord = isWholeWord(new StringBuffer(line.toString().toLowerCase()), pointer, end, processed_data.toString().toLowerCase(), (int)fx, (int)fy);
						else
							isWholeWord = isWholeWord(new StringBuffer(line.toString().toLowerCase()), pointer, end, value.toLowerCase(), (int)fx, (int)fy);
					
					if (includeTease) {
						// tease.append(xWord);
						// tease.append(lastWord);

						tease.append(Strip.stripXML(lastWord));
						if(onlyCheckStart)
							tease.append("</b>");
						
						if (lastWord.toString().endsWith(" "))
							tease.append(' ');

						// tease.append('x');

						/**
						 * add rest more words
						 */
						if (line != null)
							createTease(rawContents, line, pointer, end);
					} 

				} else if (value.startsWith(test) == false) {

					processed_data = new StringBuffer();
					x = -1;

					if (text.equals(" "))
						lastWord = new StringBuffer();

				}

				if (finalX >= 0)
					break;
			}
		}
		
		return new ScanLinePair(finalX, isWholeWord);
	}

    /**
     * sets if we include HTML in teasers
     * (do we want this is <b>word</b> or this is word as teaser)
     * @param value
     */
    public void setIncludeHTML(boolean value) {
        includeHTMLtags=value;
    }

    private static class ScanLinePair {
		private boolean isWholeWord;
		private float finalX;

		public ScanLinePair(float finalX, boolean isWholeWord) {
			this.finalX = finalX;
			this.isWholeWord = isWholeWord;
		}
	}
	
	private boolean isWholeWord(StringBuffer line, int pointer, int end, String value, int x, int y) {
		
		//System.out.println("line = "+line);
        // System.out.println("line = "+removeHiddenMarkers(line.toString()));

		String rawLine = Strip.stripXML(removeHiddenMarkers(line.toString())).toString();
		//System.out.println("rawLine =" + rawLine);
		
		int indexOfValue;
		//Check position as well just incase the page has multiple identicle lines of text
		if(rawLine.equals(isWholeWordLastLine) && (isWholeWordPosition!=null && (isWholeWordPosition.getX()!=x || isWholeWordPosition.getY()!=y))) { 
			/** 
			 * this is the same line as before so there must be more than one match in this line
			 * We need to move along it to find the next match
			 */
			
			indexOfValue = rawLine.indexOf(value, isWholeWordCurrentIndex + 1);
		} else {
			isWholeWordCurrentIndex = 0;
			
			if(value.endsWith(" "))
				indexOfValue = rawLine.indexOf(value.substring(0, value.length()-1));
			else
				indexOfValue = rawLine.indexOf(value);
		}
		
        //System.out.println("indexOfValue = "+indexOfValue);
		
        if(indexOfValue == -1) // this really shouldn't happen, but just in case say there is no match
        	return false;
        
        isWholeWordLastLine = rawLine;
        isWholeWordPosition = new Point(x,y);
        isWholeWordCurrentIndex = indexOfValue;
        
        switch(wordDetectionTechnique){
        case USER_DEFINED_LIST_ONLY:
        	/** check char before */
        	if (indexOfValue > 0) {
        		char charBefore = rawLine.charAt(indexOfValue - 1);
        		// System.out.println("charBefore = "+charBefore);
        		if (punctuation.indexOf(charBefore) == -1)
        			return false;
        	}
        	/** check char after */
        	if (indexOfValue + value.length() < rawLine.length()) {
        		char charAfter = rawLine.charAt(indexOfValue + value.length());
        		// System.out.println("charAfter = "+charAfter);
        		if (punctuation.indexOf(charAfter) == -1)
        			return false;
        	}
        	break;
        case SURROUND_BY_ANY_PUNCTUATION:
        	/** check char before */
        	if (indexOfValue > 0) {
        		char charBefore = rawLine.charAt(indexOfValue - 1);
        		// System.out.println("charBefore = "+charBefore);
        		if (Character.isLetterOrDigit(charBefore))
        			return false;
        	}
        	/** check char after */
        	if (indexOfValue + value.length() < rawLine.length()) {
        		char charAfter = rawLine.charAt(indexOfValue + value.length());
        		// System.out.println("charAfter = "+charAfter);
        		if (Character.isLetterOrDigit(charAfter))
        			return false;
        	}
        	break;
        }

        
        
        return true;
		
		
// line = Strip.stripXML(line);
//		
//		System.out.println("line = "+line);
//		
//		String punctuation = " ()!;.,\\/\"\"\'\'";
//		
//		int i = pointer;
//		int markers = 0;
//		int testCharIndex = value.length() - 1;
//		boolean lastChar = false;
//		
//		if(i == end){ // last char if the line
//			
//			lastChar = true;
//			
//			i--;
//			
//			markers = 0;
//			while(i >= 0) {
//				if(line.charAt(i)==MARKER2)
//					markers++;
//				
//				if (markers == 1) {
//					i+=2;
//					break;
//				}
//				
//				i--;
//			}
//			
//		} else {
//		
//			/** check to make sure there is a space after the last character */
//			while(i < end) {
//				if(line.charAt(i)==MARKER2)
//					markers++;
//				
//				if (markers == 3) {
//					char testChar1 = line.charAt(i);
//					char testChar2 = line.charAt(i + 1);
////					char testChar3 = line.charAt(i + 2);
//					
//					if(testChar1 == MARKER2 && punctuation.indexOf(testChar2) == -1 /*&& testChar3 == MARKER2*/)
//						return false;
//					
//					break;
//				}
//				i++;
//			}
//		
//		}
//		
//		int neededMarkers = lastChar ? 0 : 3;
//		
//		markers = 0;
//		while(testCharIndex >= 0) {
//			if(line.charAt(i)==MARKER2)
//				markers++;
//			
//			if (markers == neededMarkers) {
//				char testChar = line.charAt(i - 1);
//				
//				neededMarkers = 3;
//				
//				if(testChar != value.charAt(testCharIndex))
//					return false;
//				
//				testCharIndex--;
//				
//				i--;
//				
//				markers = 0;
//			}
//			
//			i--;
//		}
//		
//		markers = 0;
//		while(i >= 0) {
//			if(line.charAt(i)==MARKER2)
//				markers++;
//			
//			if (markers == 3) {
//				char testChar1 = line.charAt(i);
//				char testChar2 = line.charAt(i - 1);
//				char testChar3 = line.charAt(i - 2);
//				
//				if(testChar1 == MARKER2 && punctuation.indexOf(testChar2) == -1 && testChar3 == MARKER2)
//					return false;
//				
//				break;
//			}
//			
//			i--;
//		}
//		
//		return true;
	}

	private void createTease(StringBuffer[] rawContent,StringBuffer line, int pointer,int end) {
		
		int startPointer=0,wordCount=3;
		//int pointer=pointer,end=end;
		StringBuffer teaseLine=line;
		String text="";
		
		boolean match=false;
		
		//with a token to make sure cleanup works
		while ((pointer<end)&&(wordCount>0)) {

			//find first marker
			while(pointer<end){
				if(teaseLine.charAt(pointer)==MARKER2)
					break;
				pointer++;
			}
			
			boolean hasLink=false;
			
			if((teaseLine.charAt(pointer)==MARKER2)|(pointer==0)){
				
				pointer++;
				
				//find next 3 markers
				for(int j=0;j<3;j++){
					startPointer=pointer;
					while(pointer<end){
						if(teaseLine.charAt(pointer)==MARKER2)
							break;
						pointer++;
					}
					pointer++;
				}
				pointer--;
				
				text= teaseLine.substring(startPointer, pointer);
				
				//strip any tokens
				if(PdfDecoder.isXMLExtraction()){
					
					int p=text.indexOf("<link:");
					
					if(p!=-1){ //link in text
						int end1=text.indexOf('>',p);
						int id=Integer.parseInt(text.substring(p+6,end1));
						
						pointer=0;

                        if(id>=rawContent.length)
                        return;
                        
                        teaseLine=rawContent[id];

                        if(teaseLine==null)
                        return;

                        end=teaseLine.length();

                        hasLink=true;
					    
					}//else{
						p=text.indexOf('<');
						if(p!=-1)
							text=text.substring(0,p);		
					//}
				}	
			}
			
			if(text.equals(" "))
				wordCount--;
			
			tease.append(Strip.stripXML(text));
			if(text.endsWith(" "))
				tease.append(' ');
			
			//add space so nicely formatted
			if(hasLink){
				char lastChar=text.charAt(text.length()-1);
				if((lastChar!='-')&&(lastChar!=' '))
						tease.append(' ');
			}
		}
		
		/**
		  *If file does not allow extract/copy
		  *use this to get new punctuation values
		  *to ignore
		 */
		//System.out.println(tease);
	}

	
	/**
	 * method to show data without encoding
	 */
	public static String removeHiddenMarkers(String contents) {

		//trap null
		if(contents==null)
			return null;
		
		//run though the string extracting our markers

		//make sure has markers and ignore if not
		if (contents.indexOf(MARKER) == -1)
			return contents;

		//strip the markers
		StringTokenizer tokens = new StringTokenizer(contents, MARKER, true);
		String temp_token;
		StringBuffer processed_data = new StringBuffer();
		
		//with a token to make sure cleanup works
		while (tokens.hasMoreTokens()) {

			//encoding in data
			temp_token = tokens.nextToken(); //see
																		 // if
																		 // first
																		 // marker
			if (temp_token.equals(MARKER)) {
				tokens.nextToken(); //point character starts
				tokens.nextToken(); //second marker
				tokens.nextToken(); //width
				tokens.nextToken(); //third marker

				//put back chars
				processed_data = processed_data.append(tokens.nextToken());
				//value
			} else
				processed_data = processed_data.append(temp_token);
		}
		return processed_data.toString();
	}

	/**
	 * Method to try and find vertical lines in close data
	 * (not as efficient as it could be)
	 * @throws PdfException
	 */
	private void findVerticalLines(float minX,float minY,float maxX,float maxY,int currentWritingMode) throws PdfException {

		//hold counters on all x values
		HashMap xLines = new HashMap();

		//counter on most popular item
		int most_frequent = 0, count = pdf_data.getRawTextElementCount();
		float x1, x2, y1, y2;
		String raw = "";

		for (int i = 0; i < count; i++) {
			float currentX = 0, lastX = 0;
			Integer intX;

			//extract values for data
			raw = this.pdf_data.contents[i];

			/**
			 * set pointers so left to right text
			 */
			if((oldTextExtraction)|(currentWritingMode==PdfData.HORIZONTAL_LEFT_TO_RIGHT)){
				x1=this.f_x1[i];
				x2=this.f_x2[i];
				y1=this.f_y1[i];
				y2=this.f_y2[i];
			}else if(currentWritingMode==PdfData.HORIZONTAL_RIGHT_TO_LEFT){
				x2=this.f_x1[i];
				x1=this.f_x2[i];
				y1=this.f_y1[i];
				y2=this.f_y2[i];
			}else if(currentWritingMode==PdfData.VERTICAL_BOTTOM_TO_TOP){
				x1=this.f_y1[i];
				x2=this.f_y2[i];
				y1=this.f_x2[i];
				y2=this.f_x1[i];
			}else if(currentWritingMode==PdfData.VERTICAL_TOP_TO_BOTTOM){
				x1=this.f_y2[i];
				x2=this.f_y1[i];
				y2=this.f_x1[i];
				y1=this.f_x2[i];
			}else{
				throw new PdfException("Illegal value "+currentWritingMode+"for currentWritingMode");
			}
			
			//if in the area, process
			if ((x1 > minX - .5)&& (x2 < maxX + .5)&& (y2 > minY - .5)&& (y1 < maxY + .5)) {

				//run though the string extracting our markers to get x values
				StringTokenizer tokens =new StringTokenizer(raw, MARKER, true);
				String value = "", lastValue = "";
				Object currentValue = null;

				while (tokens.hasMoreTokens()) {

					//encoding in data
					value = tokens.nextToken(); //see if first marker
					if (value.equals(MARKER)) {

						value = tokens.nextToken(); //point character starts

						if (value.length() > 0) {

							lastX = currentX;
							currentX = Float.parseFloat(value);
							try {

								//add x to list or increase counter at start
								// or on space
								//add points either side of space
								if (lastValue.length() == 0 || (lastValue.indexOf(' ') != -1)) {

									intX = new Integer((int) currentX);
									currentValue = xLines.get(intX);
									if (currentValue == null) {
										xLines.put(intX, new Integer(1));
									} else {
										int countReached =((Integer) currentValue).intValue();
										countReached++;

										if (countReached > most_frequent)
											most_frequent = countReached;

										xLines.put(intX,new Integer(countReached));
									}

									//work out the middle
									int middle =(int) (lastX+ ((currentX - lastX) / 2));

									if (lastX != 0) {
										intX = new Integer( middle);
										currentValue = xLines.get(intX);
										if (currentValue == null) {
											xLines.put(intX, new Integer(1));
										} else {
											int count_reached =((Integer) currentValue).intValue();
											count_reached++;

											if (count_reached > most_frequent)
												most_frequent = count_reached;

											xLines.put(intX,new Integer(count_reached));
										}
									}
								}

							} catch (Exception e) {
								LogWriter.writeLog(
									"Exception " + e + " stripping x values");
							}
						}

						String marker = tokens.nextToken(); //second marker
						marker = tokens.nextToken(); //glyph  width
						marker = tokens.nextToken(); //third marker
						value = tokens.nextToken(); //put back chars
						lastValue = value;

					}
				}
			}
		}

		//now analyse the data
		Iterator keys = xLines.keySet().iterator();
		int minimum_needed =  most_frequent / 2;

		while (keys.hasNext()) {
			Integer current_key = (Integer) keys.next();
			int current_count = ((Integer) xLines.get(current_key)).intValue();

			if (current_count > minimum_needed)
				lineBreaks.addElement(current_key.intValue());

		}
	}

	/**
	 * Method splitFragments adds raw frgaments to processed fragments breaking
	 * up any with vertical lines through or what looks like tabbed spaces
	 * @throws PdfException
	 */
	private void copyToArrays(
			float minX,float minY,float maxX,float maxY,
			boolean keepFont,boolean breakOnSpace,boolean findLines,String punctuation, boolean isWordlist) throws PdfException {
		
		
		final boolean debugSplit=false;
		
		//initialise local arrays allow for extra space
		int count = pdf_data.getRawTextElementCount() + increment;
		
		f_x1 = new float[count];
		f_colorTag=new String[count];
		hadSpace=new boolean[count];
		f_x2 = new float[count];
		f_y1 = new float[count];
		f_y2 = new float[count];
		
		spaceWidth = new float[count];
		content = new StringBuffer[count];
		fontSize = new int[count];
		textLength = new int[count];
		writingMode=new int[count];
		isUsed=new boolean[count];
		moveType=new int[count];
		
		//flag to find lines based on orientation of first text item*/
		boolean linesScanned=false;
		
		//set defaults and calculate dynamic values
		int text_length;
		count = count-increment;
		float last_pt,min,max,pt,x1,x2,y1,y2,
		linePos = -1,space,character_spacing = 0;
		String raw = "", char_width = "",currentColor="";
		StringBuffer text = new StringBuffer();
		
		//work through fragments
		for (int i = 0; i < count; i++) {
			
			//extract values
			character_spacing = pdf_data.f_character_spacing[i];
			raw = pdf_data.contents[i];
			x1 = pdf_data.f_x1[i];
			currentColor=pdf_data.colorTag[i];
			x2 = pdf_data.f_x2[i];
			y1 = pdf_data.f_y1[i];
			y2 = pdf_data.f_y2[i];
			text_length = pdf_data.text_length[i];
			int mode=pdf_data.f_writingMode[i];
			int moveType=pdf_data.move_command[i];
			
			/**
			 * see if in area
			 */
			boolean accepted=false;
			
			if(debugSplit)
			System.out.println(raw);
			
			//if at least partly in the area, process
			if (((oldTextExtraction==true)|(mode==PdfData.HORIZONTAL_LEFT_TO_RIGHT)|(mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT))&&
					(y2 > minY)&& (y1 < maxY)&&(x1<maxX)&&(x2>minX)) 
				accepted=true;
			else if((oldTextExtraction==true)){
			}else if(((mode==PdfData.VERTICAL_BOTTOM_TO_TOP)|(mode==PdfData.VERTICAL_TOP_TO_BOTTOM))&&
					(x1 > minX)&&(x2 < maxX)&&(y1>minY)&&(y2<maxY)) 
				accepted=true;
			
			if(accepted){
				
				/**find lines*/
				//look for possible vertical or horizontal lines in the data
				if((!linesScanned)&&(findLines)){
					findVerticalLines(minX, minY, maxX, maxY,mode);
					linesScanned=true;
				}
				
				/**
				 * initialise pointers and work out an 
				 * 'average character space'
				 **/
				if ((oldTextExtraction==true)|(mode==PdfData.HORIZONTAL_LEFT_TO_RIGHT)|(mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT)){
					space = (x2 - x1) / text_length;
					pt = x1;
					last_pt = x1;
					min=minX;
					max=maxX;
				}else{ //vertical text
					space = (y1 - y2) / text_length;
					pt = y2;
					last_pt = y2;
					min=minY;
					max=maxY;
				}
				
				linePos = -1;
				
				/**
				 * work through text, using embedded markers to work out whether
				 * each letter is IN or OUT
				 */
				char[] line=raw.toCharArray();
				
				int end=line.length;
				int pointer=0;
				
				String value = "", textValue = "", pt_reached = "";
				
				//allow for reset of leading edge
				boolean resetPointer=false;
				
				//allow for no tokens and return all text fragment
				if (raw.indexOf(MARKER) == -1)
					text = new StringBuffer(raw);
				
				boolean isFirstValue=true;
				
				/**
				 * work through text, using embedded markers to work out whether
				 * each letter is IN or OUT
				 */
				while(pointer<end){
					
					//only data between min and y locations
					while (true) {
						
						/**
						 * read value
						 */
						
						if(line[pointer]!=MARKER2){
							//find second marker and get width
							int startPointer=pointer;
							while((pointer<end)&&(line[pointer]!=MARKER2))
									pointer++;
							value = raw.substring(startPointer,pointer);
							
						}else{//if (value.equals(MARKER)) { // read the next token and its location and width
							
							//find first marker
							while((pointer<end)&&(line[pointer]!=MARKER2))
									pointer++;
							
							pointer++;
								
							//find second marker and get width
							int startPointer=pointer;
							while((pointer<end)&&(line[pointer]!=MARKER2))
									pointer++;
							pt_reached = raw.substring(startPointer,pointer);
							pointer++;
							
							//find third marker
							startPointer=pointer;
							while((pointer<end)&&(line[pointer]!=MARKER2))
									pointer++;
							
							char_width=raw.substring(startPointer,pointer);
							pointer++;
								
							//find next marker
							startPointer=pointer;
							while((pointer<end)&&(line[pointer]!=MARKER2))
								pointer++;
							
							value = raw.substring(startPointer,pointer);
							
							textValue = value; //keep value with no spaces
						
							if (pt_reached.length() > 0) { //set point character starts
								last_pt = pt;
								pt = Float.parseFloat(pt_reached);	
							}
							
							//add font start if needed
							if ((PdfDecoder.isXMLExtraction())&&(last_pt < min)&& (pt > min)&& (!value.startsWith(Fonts.fb)))
								value = Fonts.getActiveFontTag(raw, "")+ value;
							
						}
						
						if ((pt > min) & (pt < max))
							break;
						
						value = "";
						textValue = "";
						
						if(pointer>=end)
							break;
					}
					
					/**make sure font not sliced off on first value*/
					if((isFirstValue)){
						
						isFirstValue=false;
						if((PdfDecoder.isXMLExtraction())&&(keepFont)&&(!value.startsWith(Fonts.fb))&&(!value.startsWith(GenericColorSpace.cb)))//&&(!text.toString().startsWith(Fonts.fb))))
						text.append(Fonts.getActiveFontTag(text.toString(), raw));
					}
					
					/**reset pointer if needed*/
					if(resetPointer){
						resetPointer=false;
						if((oldTextExtraction)|(mode==PdfData.HORIZONTAL_LEFT_TO_RIGHT))
							x1 = pt;
						else if(mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT)
							x2 = pt;
						else if(mode==PdfData.VERTICAL_BOTTOM_TO_TOP)
							y2=pt;	
						else if(mode==PdfData.VERTICAL_TOP_TO_BOTTOM)
							y1=pt;
					}
					
					/**
					 * we now have a valid value inside the selected area so perform tests
					 */
					//see if a break occurs
					boolean is_broken = false;
					if((findLines)&&(character_spacing > 0)&& (text.toString().endsWith(" "))) {
						int counts = lineBreaks.size();
						for (int jj = 0; jj < counts; jj++) {
							int test_x = lineBreaks.elementAt(jj);
							if ((last_pt < test_x) & (pt > test_x)) {
								jj = counts;
								is_broken = true;
							}
						}
					}
					
					boolean endsWithPunctuation = checkForPunctuation(textValue,punctuation);
					
					if (is_broken) { //break on double-spaces or larger
						
						if(debugSplit)
						System.out.println("Break 1 is_broken");
							
						float Nx1=x1,Nx2=x2,Ny1=y1,Ny2=y2;
						if((oldTextExtraction)|(mode==PdfData.HORIZONTAL_LEFT_TO_RIGHT))
							Nx2 = last_pt + Float.parseFloat(char_width);
						else if(mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT)
							Nx1 = last_pt + Float.parseFloat(char_width);
						else if(mode==PdfData.VERTICAL_BOTTOM_TO_TOP)
							Ny1=last_pt + Float.parseFloat(char_width);
						else if(mode==PdfData.VERTICAL_TOP_TO_BOTTOM)
							Ny2=last_pt + Float.parseFloat(char_width);
						
						addFragment(moveType,i,text,Nx1,Nx2,Ny1,Ny2,text_length,keepFont,currentColor,isWordlist);
						text =new StringBuffer(Fonts.getActiveFontTag(text.toString(), raw));
						text.append(value);
						
						if((oldTextExtraction)|(mode==PdfData.HORIZONTAL_LEFT_TO_RIGHT))
							x1 = pt;
						else if(mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT)
							x2 = pt;
						else if(mode==PdfData.VERTICAL_BOTTOM_TO_TOP)
							y2=pt;	
						else if(mode==PdfData.VERTICAL_TOP_TO_BOTTOM)
							y1=pt;
						
					} else if ((endsWithPunctuation)|
							((breakOnSpace) && ((textValue.indexOf(' ') != -1)||(value.endsWith(" "))))|((textValue.indexOf("   ") != -1))) {//break on double-spaces or larger
						if(debugSplit)
						System.out.println("Break 2 "+endsWithPunctuation+" textValue="+textValue+ '<');
						
						if (!endsWithPunctuation)
						text.append(value.trim());
						
						if((oldTextExtraction)|(mode==PdfData.HORIZONTAL_LEFT_TO_RIGHT))
							addFragment(moveType,i,text,x1,pt,y1,y2,text_length,keepFont,currentColor,isWordlist);
						else if(mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT)
							addFragment(moveType,i,text,pt,x2,y1,y2,text_length,keepFont,currentColor,isWordlist);
						else if(mode==PdfData.VERTICAL_BOTTOM_TO_TOP)
							addFragment(moveType,i,text,x1,x2,pt,y2,text_length,keepFont,currentColor,isWordlist);
						else if(mode==PdfData.VERTICAL_TOP_TO_BOTTOM)
							addFragment(moveType,i,text,x1,x2,y1,pt,text_length,keepFont,currentColor,isWordlist);
					
						if(char_width.length()>0)
							pt=pt+ Float.parseFloat(char_width);
						
						//store fact it had a space in case we generate wordlist
						if((breakOnSpace)&(nextSlot>0))
						hadSpace[nextSlot-1]=true;
						
						text =new StringBuffer(Fonts.getActiveFontTag(text.toString(), raw));
						if((oldTextExtraction)|(mode==PdfData.HORIZONTAL_LEFT_TO_RIGHT))
							x1 = pt;// + space;
						else if(mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT)
							x2 = pt;// - space;
						else if(mode==PdfData.VERTICAL_BOTTOM_TO_TOP)
							y2 = pt;// + space;
						else if(mode==PdfData.VERTICAL_TOP_TO_BOTTOM)
							y1 = pt;// - space;						
						
					} else if ((linePos != -1) & (pt > linePos)) {//break on a vertical line
						
						if((oldTextExtraction)|(mode==PdfData.HORIZONTAL_LEFT_TO_RIGHT))
							addFragment(moveType,i,text,x1,linePos,y1,y2,text_length,keepFont,currentColor,isWordlist);
						else if(mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT)
							addFragment(moveType,i,text,linePos,x2,y1,y2,text_length,keepFont,currentColor,isWordlist);
						else if(mode==PdfData.VERTICAL_BOTTOM_TO_TOP)
							addFragment(moveType,i,text,x1,x2,linePos,y2,text_length,keepFont,currentColor,isWordlist);
						else if(mode==PdfData.VERTICAL_TOP_TO_BOTTOM)
							addFragment(moveType,i,text,x1,x2,y1,linePos,text_length,keepFont,currentColor,isWordlist);
						
						text =new StringBuffer(Fonts.getActiveFontTag(text.toString(), raw));
						text.append(value);
						
						if((oldTextExtraction)|(mode==PdfData.HORIZONTAL_LEFT_TO_RIGHT))
							x1 = linePos;
						else if(mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT)
							x2 = linePos;
						else if(mode==PdfData.VERTICAL_BOTTOM_TO_TOP)
							y2 = linePos;
						else if(mode==PdfData.VERTICAL_TOP_TO_BOTTOM)
							y1 = linePos;
						
						linePos = -1;
						
					} else { //allow for space used as tab
						if ((PdfDecoder.isXMLExtraction())&&(value.endsWith(' ' +Fonts.fe))) {
							value = Fonts.fe;
							textValue = "";
							
							if((oldTextExtraction)|(mode==PdfData.HORIZONTAL_LEFT_TO_RIGHT))
							x2 = last_pt; 
							else if(mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT)
								x1=last_pt;
							else if(mode==PdfData.VERTICAL_BOTTOM_TO_TOP)
								y1 = last_pt;
							else if(mode==PdfData.VERTICAL_TOP_TO_BOTTOM)
								y2 = last_pt;		
						}
						text.append(value);
					}
					
				}
				
				//trap scenario we found if all goes through with no break at end
				if((keepFont)&&(PdfDecoder.isXMLExtraction())&&
						(!text.toString().endsWith(Fonts.fe))&&
						(!text.toString().endsWith(GenericColorSpace.ce)))
					text.append(Fonts.fe);
				
				//create new line with what is left and output
				if ((oldTextExtraction)|(mode==PdfData.HORIZONTAL_LEFT_TO_RIGHT)|(mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT)){	
					if (x1 < x2)
						addFragment(moveType,i,text,x1,x2,y1,y2,text_length,keepFont,currentColor,isWordlist);
				}else if ((oldTextExtraction)|(mode==PdfData.VERTICAL_BOTTOM_TO_TOP)|(mode==PdfData.VERTICAL_TOP_TO_BOTTOM)){
					if(y1 > y2)
						addFragment(moveType,i,text,x1,x2,y1,y2,text_length,keepFont,currentColor,isWordlist);
				}
				text = new StringBuffer();
				
			}
		}

		//local lists for faster access
		isUsed = new boolean[nextSlot];

	}

	/**
	 * @param textValue
	 * @return
	 */
	private static boolean checkForPunctuation(String textValue,String punctuation) {
		
		if(punctuation==null)
			return false;
		
		/** see if ends with punctuation */
		boolean endsWithPunctuation = false;
		int textLength = textValue.length();
		int ii = textLength - 1;
		if (textLength > 0) { //strip any spaces and tags in test
			char testChar = textValue.charAt(ii);
			boolean inTag = (testChar == '>');
			while (((inTag) | (testChar == ' ')) & (ii > 0)) {
				
				if (testChar == '<')
					inTag = false;
				
				ii--;
				testChar = textValue.charAt(ii);
				
				if (testChar == '>')
					inTag = true;
			}
			
			//stop  matches on &;
			if((testChar==';')){
				//ignore if looks like &xxx;
				endsWithPunctuation = true;
				ii--;
				while(ii>-1){
					
					testChar=textValue.charAt(ii);
					if((testChar=='&')||(testChar=='#')){
						endsWithPunctuation = false;
						ii=0;
					}
					
					if((ii==0)||(testChar==' ')||(!Character.isJavaLetterOrDigit(testChar)))
							break;
					
					ii--;
				}
			}else if (punctuation.indexOf(testChar) != -1)
				endsWithPunctuation = true;
			
		}
		return endsWithPunctuation;
	}

	/**
	 * add an object to our new XML list
	 */
	private void addFragment(
			int moveType,
			int index,
			StringBuffer contentss,
			float x1,
			float x2,
			float y1,
			float y2,
			int text_len,
			boolean keepFontTokens,String currentColorTag,boolean isWordlist) {

		StringBuffer current_text = contentss;
		String str=current_text.toString();
		
		//strip <> or ascii equivalents
		if(isWordlist){
			if(str.indexOf("&#")!=-1)
				current_text=Strip.stripAmpHash(current_text);
			
			if((PdfDecoder.isXMLExtraction())&&((str.indexOf("&lt;")!=-1)||(str.indexOf("&gt;")!=-1)))
				current_text=Strip.stripXMLArrows(current_text);
			else if((!PdfDecoder.isXMLExtraction())&&((str.indexOf('<')!=-1)||(str.indexOf('>')!=-1)))
				current_text=Strip.stripArrows(current_text);
		}
		
//		StringBuffer justText=Strip.stripXML(current_text);

		//ignore blank space objects
		//if (justText.length() == 0) {
			
		if(this.getFirstChar(current_text)==-1){
			//System.err.println(current_text+"<");
		} else {
			
			//strip tags or pick up missed </font> if ends with space
			if (keepFontTokens == false) {

				//strip fonts if required
				current_text = Strip.stripXML(current_text);

			} else if (PdfDecoder.isXMLExtraction()){
				
				//no color tag
			    if(pdf_data.isColorExtracted()&&(!current_text.toString().endsWith(GenericColorSpace.ce))){
			    	
			    	//se
			    	//if ends </font> add </color>
			    	//otherwise add </font></color>
			    	if(!current_text.toString().endsWith(Fonts.fe))
			    		current_text = current_text.append(Fonts.fe);
			    	current_text = current_text.append(GenericColorSpace.ce);
			    	
			    }else if((!pdf_data.isColorExtracted())&&(!current_text.toString().endsWith(Fonts.fe)))       
			        	current_text = current_text.append(Fonts.fe);        		    
			}
			
			/***/
			//add to vacant slot or create new slot
			int count = f_x1.length;
			
			if (nextSlot < count) {

				f_x1[nextSlot] = x1;
				f_colorTag[nextSlot]=currentColorTag;
				f_x2[nextSlot] = x2;
				f_y1[nextSlot] = y1;
				f_y2[nextSlot] = y2;
				this.moveType[nextSlot]=moveType;

				fontSize[nextSlot] = pdf_data.f_end_font_size[index];
				writingMode[nextSlot]=pdf_data.f_writingMode[index];
				textLength[nextSlot] = text_len;

				spaceWidth[nextSlot] = pdf_data.space_width[index];
				content[nextSlot] = current_text;

				nextSlot++;
			} else {
				count = count + increment;
				float[] t_x1 = new float[count];
				String[] t_colorTag=new String[count];
				float[] t_x2 = new float[count];
				float[] t_y1 = new float[count];
				float[] t_y2 = new float[count];
				float[] t_spaceWidth = new float[count];

				StringBuffer[] t_content = new StringBuffer[count];

				int[] t_font_size = new int[count];
				int[] t_text_len = new int[count];
				int[] t_writingMode=new int[count];
				
				int[] t_moveType=new int[count];
				
				boolean[] t_isUsed = new boolean[count];
				
				boolean[]t_hadSpace=new boolean[count];
				
				//copy in existing
				for (int i = 0; i < count - increment; i++) {
					t_x1[i] = f_x1[i];
					t_colorTag[i]=f_colorTag[i];
					t_x2[i] = f_x2[i];
					t_y1[i] = f_y1[i];
					t_y2[i] = f_y2[i];
					t_hadSpace[i]=hadSpace[i];
					t_spaceWidth[i] = spaceWidth[i];
					t_content[i] = content[i];
					t_font_size[i] = fontSize[i];
					t_writingMode[i]=writingMode[i];
					t_text_len[i] = textLength[i];
					t_isUsed[i] = isUsed[i];
					t_moveType[i]=this.moveType[i];
				}

				f_x1 = t_x1;
				f_colorTag=t_colorTag;
				hadSpace=t_hadSpace;
				f_x2 = t_x2;
				f_y1 = t_y1;
				f_y2 = t_y2;
				isUsed=t_isUsed;
				
				fontSize = t_font_size;
				writingMode=t_writingMode;
				textLength = t_text_len;

				spaceWidth = t_spaceWidth;

				content = t_content;
				
				this.moveType=t_moveType;

				f_x1[nextSlot] = x1;
				f_colorTag[nextSlot]=currentColorTag;
				f_x2[nextSlot] = x2;
				f_y1[nextSlot] = y1;
				f_y2[nextSlot] = y2;

				fontSize[nextSlot] = pdf_data.f_end_font_size[index];
				writingMode[nextSlot]=pdf_data.f_writingMode[index];
				t_text_len[nextSlot] = text_len;
				content[nextSlot] = current_text;

				spaceWidth[nextSlot] = pdf_data.space_width[index];
				
				this.moveType[nextSlot]=moveType;

				nextSlot++;

			} /***/

		}
	}

	//////////////////////////////////////////////////////////////////////
	/**
	 * put rows together into one object with start and end
	 */
	private void mergeTableRows(int border_width) {

		//merge row contents
		String separator = "\n";
		
		//if(PdfDecoder.isXMLExtraction())
			separator ="</tr>\n<tr>";
		
		if (isXHTML == false)
			separator = "\n";

		master = ((Vector_Int) lines.elementAt(line_order[0])).elementAt(0);

		int item;
		for (int rr = 1; rr < max_rows; rr++) {

			item =((Vector_Int) lines.elementAt(line_order[rr])).elementAt(0);
			if(content[master]==null)
				master=item;
			else if(content[item]!=null)
			merge(master,item,separator,false);
		}

		//add start/end marker
		if (isXHTML) {
			if (border_width == 0){
				content[master].insert(0,"<TABLE>\n<tr>");
				content[master].append("</tr>\n</TABLE>\n");
			}else{
				StringBuffer startTag=new StringBuffer("<TABLE border='");
				startTag.append(String.valueOf(border_width));
				startTag.append( "'>\n<tr>");
				startTag.append(content[master]);
				content[master]=startTag;
				content[master].append("</tr>\n</TABLE>\n");
			}
		}

	}

	//////////////////////////////////////////////////
	/**
	 * get list of unused fragments and put in list and sort in sorted_items
	 */
	final private int[] getsortedUnusedFragments(
		boolean sortOnX,
		boolean use_y1) {
		int total_fragments = isUsed.length;

		//get unused item pointers
		int ii = 0;
		int sorted_temp_index[] = new int[total_fragments];
		for (int i = 0; i < total_fragments; i++) {
			if (isUsed[i] == false) {
				sorted_temp_index[ii] = i;
				ii++;
			}
		}
		
		int[] unsorted_items = new int[ii];
		int[] sorted_items = null;
		int[] sorted_temp_x1 = new int[ii];
		int[] sorted_temp_y1 = new int[ii];
		int[] sorted_temp_y2 = new int[ii];

		//put values in array and get x/y for sort
		for (int pointer = 0; pointer < ii; pointer++) {
			int i = sorted_temp_index[pointer];
			unsorted_items[pointer] = i;
			
			sorted_temp_x1[pointer] = (int) f_x1[i];

			//negative values to get sort in 'wrong' order from top of page
			sorted_temp_y1[pointer] = (int) f_y1[i];
			sorted_temp_y2[pointer] = (int) f_y2[i];

		}

		//sort
		if (sortOnX == false) {
			if (use_y1 == true)
				sorted_items =
					Sorts.quicksort(
						sorted_temp_y1,
						sorted_temp_x1,
						unsorted_items);
			else
				sorted_items =
					Sorts.quicksort(
						sorted_temp_y2,
						sorted_temp_x1,
						unsorted_items);
		} else
			sorted_items =
				Sorts.quicksort(sorted_temp_x1, sorted_temp_y1, unsorted_items);
		
		return sorted_items;
	}

	//////////////////////////////////////////////////////////////////////
	/**
	 * create rows of data from preassembled indices, adding separators. Each
	 * row is built to a temp array and then row created - we don't know how
	 * many columns until the table is built
	 * @throws PdfException
	 */
	private void createTableRows(
		boolean keep_alignment_information,
		boolean keep_width_information,int currentWritingMode) throws PdfException {

		/**
		 * create local copies of arrays 
		 */
		float[] f_x1=null,f_x2=null,f_y1=null,f_y2=null;
		
		/**
		 * set pointers so left to right text
		 */
		if((oldTextExtraction)|(currentWritingMode==PdfData.HORIZONTAL_LEFT_TO_RIGHT)){
			f_x1=this.f_x1;
			f_x2=this.f_x2;
			f_y1=this.f_y1;
			f_y2=this.f_y2;
		}else if(currentWritingMode==PdfData.HORIZONTAL_RIGHT_TO_LEFT){
			f_x2=this.f_x1;
			f_x1=this.f_x2;
			f_y1=this.f_y1;
			f_y2=this.f_y2;
		}else if(currentWritingMode==PdfData.VERTICAL_BOTTOM_TO_TOP){
			f_x1=this.f_y2;
			f_x2=this.f_y1;
			f_y1=this.f_x2;
			f_y2=this.f_x1;
		}else if(currentWritingMode==PdfData.VERTICAL_TOP_TO_BOTTOM){
			f_x1=this.f_y1;
			f_x2=this.f_y2;
			f_y2=this.f_x1;
			f_y1=this.f_x2;
			
			/**
			 * fiddle x,y co-ords so it works
			 */
			
			//get max size
			int maxX=0;
			for(int ii=0;ii<f_x1.length;ii++){
				if(maxX<f_x1[ii])
					maxX=(int)f_x1[ii];
			}
			
			maxX++; //allow for fp error
			//turn around
			for(int ii=0;ii<f_x2.length;ii++){
				f_x1[ii]=maxX-f_x1[ii];
				f_x2[ii]=maxX-f_x2[ii];
			}
			
		}else{
			throw new PdfException("Illegal value "+currentWritingMode+"for currentWritingMode");
		}

		int item, i, current_col = -1;

		int itemsInTable = 0, items_added = 0;
		//pointer to current element on each row
		int[] currentItem = new int[max_rows];

		Vector_Int[] rowContents = new Vector_Int[max_rows];
		Vector_String alignments = new Vector_String(); //text alignment
		Vector_Float widths = new Vector_Float(); //cell widths
		Vector_Float cell_x1 = new Vector_Float(); //cell widths
		String separator = "", empty_cell = "&nbsp;";

		if (isXHTML == false) {
			separator = "\",\"";
			empty_cell = "";
		}

		/**
		 * set number of items on each line, column count and populate empty rows
		 */
		int[] itemCount = new int[max_rows];
		for (i = 0; i < max_rows; i++) {
			itemCount[i] = ((Vector_Int) lines.elementAt(i)).size() - 1;

			//total number of items
			itemsInTable = itemsInTable + itemCount[i];

			//reset other values
			currentItem[i] = 0;
			rowContents[i] = new Vector_Int(20);
		}

		//now work through and split any overlapping items until all done
		while (true) {

			//size of column and pointers
			float x1 = 9999,min_x2 = 9999,x2 = 9999,current_x1 = 0,current_x2 = 0,c_x1 = 0,next_x1 = 9999,c_x2 = 0,items_in_column = 0;
			
			current_col++;
			boolean all_done = true; //flag to exit at end
			float total_x1 = 0, total_x2 = 0, left_gap = 0, right_gap = 0;

			String alignment = "center";

			if (items_added < itemsInTable) {

				/** 
				 * work out cell x boundaries on basis of objects 
				 */
				for (i = 0; i < max_rows; i++) { //get width for column
					if (itemCount[i] > currentItem[i]) { //item  id
						
						item = ((Vector_Int) lines.elementAt(i)).elementAt(currentItem[i]);
						current_x1 = f_x1[item];
						current_x2 = f_x2[item];
						
						if (current_x1 < x1) //left margin
							x1 = current_x1;
						if (current_x2 < min_x2) //right margin if appropriate
							min_x2 = current_x2;
						
					}
				}
				
				cell_x1.addElement(x1); //save left margin
				x2 = min_x2; //set default right margin

				/** 
				 * workout end and next column start by scanning all items
				 */
				for (i = 0;i < max_rows;i++) { //slot the next item on each row together work out item
					item = ((Vector_Int) lines.elementAt(i)).elementAt(currentItem[i]);
					c_x1 = f_x1[item];
					c_x2 = f_x2[item];

					//max item width of this column
					if ((c_x1 >= x1) & (c_x1 < min_x2) & (c_x2 > x2))
						x2 = c_x2;

					if (currentItem[i] < itemCount[i]) { //next left margin

						item =((Vector_Int) lines.elementAt(i)).elementAt(currentItem[i] + 1);
						current_x1 = f_x1[item];
						if ((current_x1 > min_x2) & (current_x1 < next_x1))
							next_x1 = current_x1;
					}
				}
				//allow for last column
				if (next_x1 == 9999)
					next_x1 = x2;
			
				/**
				 * count items in table and workout raw totals for alignment.
				 * Also work out widest x2 in column
				 */
				for (i = 0;i < max_rows;i++) { //slot the next item on each row together

					//work out item
					item =((Vector_Int) lines.elementAt(i)).elementAt(currentItem[i]);
					c_x1 = f_x1[item];
					c_x2 = f_x2[item];

					//use items in first column of single colspan
					if ((c_x1 >= x1) & (c_x1 < min_x2) & (c_x2 <= next_x1)) {

						//running totals to calculate alignment
						total_x1 = total_x1 + c_x1;
						total_x2 = total_x2 + c_x2;
						items_in_column++;

					}
				}
				
				/**
				 * work out gap and include empty space between cols and save
				 */
				if (i == 0)
					left_gap = x1;
				if (next_x1 == -1)
					right_gap = 0;
				else
					right_gap = (int) ((next_x1 - x2) / 2);

				int width = (int) (x2 - x1 + right_gap + left_gap);
				left_gap = right_gap;
				widths.addElement(width);

				/** workout the alignment */
				float x1_diff = (total_x1 / items_in_column) - x1;
				float x2_diff = x2 - (total_x2 / items_in_column);
				if (x1_diff < 1)
					alignment = "left";
				else if (x2_diff < 1)
					alignment = "right";
				alignments.addElement(alignment);

				for (i = 0;i < max_rows;i++) { //slot the next item on each row together
					master = ((Vector_Int) lines.elementAt(i)).elementAt(0);
					//get next item on line or -1 for no more
					if (itemCount[i] > currentItem[i]) {
						//work out item
						item =((Vector_Int) lines.elementAt(i)).elementAt(currentItem[i]);
						c_x1 = f_x1[item];
						c_x2 = f_x2[item];
						all_done = false;

					} else {
						item = -1;
						c_x1 = -1;
						c_x2 = -1;
					}

					if ((item == -1) & (items_added <= itemsInTable)) {
						//all items in table so just filling in gaps
						rowContents[i].addElement(-1);
						
					} else if ((c_x1 >= x1) & (c_x1 < x2)) {
						//fits into cell so add in and roll on marker

						rowContents[i].addElement(item);
						currentItem[i]++;
						
						items_added++;
					} else if (c_x1 > x2) { //empty cell
						rowContents[i].addElement(-1);
					}
				}
			}
			if (all_done)
				break;
		}

		//===================================================================
		/**
		 * now assemble rows
		 */
		for (int row = 0; row < max_rows; row++) {
			StringBuffer line_content = new StringBuffer();
			
			int count = rowContents[row].size() - 1;
			master = ((Vector_Int) lines.elementAt(row)).elementAt(0);

			for (i = 0; i < count; i++) {
				item = rowContents[row].elementAt(i);

				if (isXHTML) {

					//get width
					float current_width = widths.elementAt(i);
					String current_alignment = alignments.elementAt(i);
					int test = -1, colspan = 1, pointer = i + 1;

					if (item != -1) {

						//look for colspan
						while (true) {
							test = rowContents[row].elementAt(i + 1);
							if ((test != -1) | (count == i + 1))
								break;

							//break if over another col - roll up single value on line
							if ((itemCount[row] > 1)& (cell_x1.elementAt(i + 1) > f_x2[item]))
								break;

							count--;
							rowContents[row].removeElementAt(i + 1);
							colspan++;

							//update width
							current_width =current_width + widths.elementAt(pointer);
							pointer++;
						}
					}
					line_content.append("<td");

					if (keep_alignment_information) {
						line_content.append(" align='");
						line_content.append(current_alignment);
						line_content.append('\'');
						if (colspan > 1)
                            line_content.append(" colspan='").append(colspan).append('\'');
					}

					if (keep_width_information)
                        line_content.append(" width='").append((int) current_width).append('\'');

					line_content.append(" nowrap>");
					if (item == -1)
						line_content.append(empty_cell);
					else
						line_content.append(content[item]);
					line_content.append("</td>");

				} else { //csv
					if (item == -1) //empty col
						line_content.append("\"\",");
					else{ //value
						line_content.append('\"');
						line_content.append(content[item]);
						line_content.append("\",");
					}
				}

				//merge to update other values
				if ((item != -1) && (master != item)) //merge tracks the shape
					merge(master,item,separator,false);

			}
			//substitute our 'hand coded' value
			content[master] = line_content;

		}
	}

	/**
	 * work through data and create a set of rows and return an object with
	 * refs for each line
	 * @throws PdfException
	 */
	private Vector_Object createLinesInTable(int itemCount, int[] items,boolean addSpaceXMLTag,int mode) throws PdfException {
		
		/**
		 * create local copies of arrays 
		 */
		float[] f_x1=null,f_x2=null,f_y1=null,f_y2=null;
		
		/**
		 * reverse order if text right to left
		 */
		if((!oldTextExtraction)&&((mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT)))
			items=reverse(items);
		
		/**
		 * set pointers so left to right text
		 */
		if((oldTextExtraction)|(mode==PdfData.HORIZONTAL_LEFT_TO_RIGHT)){
			f_x1=this.f_x1;
			f_x2=this.f_x2;
			f_y1=this.f_y1;
			f_y2=this.f_y2;
		}else if(mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT){
			f_x2=this.f_x1;
			f_x1=this.f_x2;
			f_y1=this.f_y1;
			f_y2=this.f_y2;
		}else if(mode==PdfData.VERTICAL_BOTTOM_TO_TOP){
			f_x1=this.f_y1;
			f_x2=this.f_y2;
			f_y1=this.f_x2;
			f_y2=this.f_x1;
			
		}else if(mode==PdfData.VERTICAL_TOP_TO_BOTTOM){
			f_x1=this.f_y2;
			f_x2=this.f_y1;
			f_y2=this.f_x1;
			f_y1=this.f_x2;
			items = this.getsortedUnusedFragments(false, true);
			items=reverse(items);
		}else{
			throw new PdfException("Illegal value "+mode+"for currentWritingMode");
		}
		
		//holds line we're working on
		Vector_Int current_line = new Vector_Int(20);
		
		String separator = "";
		
		int current_cols = 0;
		
		for (int j = 0; j < itemCount; j++) {

			int c=items[j];
				int id = -1, i,last = c;
				float smallest_gap = -1, gap, yMidPt;

				if((isUsed[c] == false)&&((this.writingMode[c]==mode)|(oldTextExtraction))) {

					//reset pointer and add this element
					current_line = new Vector_Int(20);
					current_line.addElement(c);
					lineY2.addElement((int) f_y2[c]);

					//used for later sorting
					//look for a match
					while (true) {
						for (int ii = 0; ii < itemCount; ii++) {

							i = items[ii];

							if ((isUsed[i] == false)&&(i!=c)&&((writingMode[c]==mode)|(oldTextExtraction))&&
									(((f_x1[i] > f_x1[c])&&((mode!=PdfData.VERTICAL_TOP_TO_BOTTOM)|(oldTextExtraction)))|
											((f_x1[i] < f_x1[c])&&(!oldTextExtraction)&&(mode==PdfData.VERTICAL_TOP_TO_BOTTOM)))) { //see if on right

								gap = (f_x1[i] - f_x2[c]);

								if((!oldTextExtraction)&&((mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT)|(mode==PdfData.VERTICAL_TOP_TO_BOTTOM)))
									gap=-gap;

								//allow for fp error
								if ((gap < 0) & (gap > -2))
									gap = 0;

								//make sure on right
								yMidPt = (f_y1[i] + f_y2[i]) / 2;

								//see if line & if only or better fit
								if ((yMidPt < f_y1[c])&& (yMidPt > f_y2[c])&&((smallest_gap < 0)| (gap < smallest_gap))) {
									smallest_gap = gap;
									id = i;
								}
							}
						}

						if (id == -1) //exit when no more matches
							break;

						//merge in best match if fit found with last
						//merge if overlaps by less than half a space,otherwise join
						float t = f_x1[id] - f_x2[last];
						float possSpace=f_x1[id]-f_x2[c];
						float average_char1 =(float) 1.5* ((f_x2[id] - f_x1[id])/ textLength[id]);
						float average_char2 =(float) 1.5* ((f_x2[last] - f_x1[last]) / textLength[last]);

						if((!oldTextExtraction)&&
								((mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT)|(mode==PdfData.VERTICAL_TOP_TO_BOTTOM))){
							possSpace=-possSpace;
							t=-t;
							average_char1=-average_char1;
							average_char2=-average_char2;

						}

						if ((t < average_char1) & (t < average_char2)) {
							separator = isGapASpace(id, last, possSpace,addSpaceXMLTag,mode);

							merge(last,id,separator,true);
						} else {

							current_line.addElement(id);
							last = id;
						}

						//flag used and reset variables used
						isUsed[id] = true;
						id = -1;
						smallest_gap = 1000000;
						current_cols++;

					}

					//add line and reinit for next line
					lines.addElement(current_line);
					current_line = new Vector_Int(20);
					max_rows++;
			}
		}
		
		return lines;
	}

	/**
	 * 
	 * calls various low level merging routines on merge - 
	 * 
	 * isCSV sets if output is XHTML or CSV format -
	 * 
	 * XHTML also has options to include font tags (keepFontInfo), 
	 * preserve widths (keepWidthInfo), try to preserve alignment 
	 * (keepAlignmentInfo), and set a table border width (borderWidth) 
	 *  - AddCustomTags should always be set to false
	 * 
	 */
	public final Map extractTextAsTable(
		int x1,
		int y1,
		int x2,
		int y2,
		int pageNumber,
		boolean isCSV,
		boolean keepFontInfo,
		boolean keepWidthInfo,
		boolean keepAlignmentInfo,
		int borderWidth,
		boolean AddCustomTags)
		throws PdfException {

		//check in correct order and throw exception if not
		validateCoordinates(x1, y1, x2, y2);

		/** return the content as an Element */
		Map table_content = new Hashtable();

		LogWriter.writeLog("extracting Text As Table");

		//flag type of table so we can add correct separators
		if (isCSV == true) {
			isXHTML = false;
		} else {
			isXHTML = true;
		}

		//init table variables
		lines = new Vector_Object(20);
		lineY2 = new Vector_Int(20);
		max_rows = 0;

		//init store for data
		copyToArrays(x1, y2, x2, y1, keepFontInfo, false,true,null,false);

		//initial grouping and delete any hidden text
		removeEncoding();

		//eliminate shadows and also merge overlapping text
		cleanupShadowsAndDrownedObjects(false);

		int[] items = this.getsortedUnusedFragments(true, false);
		int item_count = items.length; //number of items

		if(item_count==0)
			return table_content;
		
		/**
		 * check orientation and get preferred. Items not correct will
		 * be ignored
		 */
		int writingMode=getWritingMode(items,item_count);
		
		String message ="Table Merging algorithm being applied " + (item_count) + " items";
		LogWriter.writeLog(message);
		
		/**
		 * scan all items joining best fit to right of each fragment to build
		 * lines
		 */
		if (item_count > 1) {

			//workout the raw lines
			createLinesInTable(item_count, items,isXHTML,writingMode);

			/**
			 * generate lookup with lines in correct order (minus used to get
			 * correct order down the page)
			 */
			int dx=1;
			if((oldTextExtraction)|(writingMode==PdfData.HORIZONTAL_LEFT_TO_RIGHT)|(writingMode==PdfData.VERTICAL_TOP_TO_BOTTOM))
				dx=-1;
			
			line_order = new int[max_rows];
			int[] line_y=new int[max_rows];

			for (int i = 0; i < max_rows; i++) {
				line_y[i] = dx*lineY2.elementAt(i);
				line_order[i] = i;
			}

			line_order = Sorts.quicksort(line_y, line_order);
			
			//assemble the rows and columns
			createTableRows(keepAlignmentInfo, keepWidthInfo,writingMode);
			
			//assemble the rows and columns
			mergeTableRows(borderWidth);
			
		}

		content[master]=cleanup(content[master]);
		
		String processed_value = content[master].toString();

		if(processed_value!=null){
			
//			cleanup data if needed by removing duplicate font tokens
			if (!isCSV)
				processed_value = Fonts.cleanupTokens(processed_value);

			table_content.put("content", processed_value);
			table_content.put("x1", String.valueOf(x1));
			table_content.put("x2", String.valueOf(x2));
			table_content.put("y1", String.valueOf(y1));
			table_content.put("y2", String.valueOf(y2));
		}
		
		return table_content;
	}

	/** make sure co-ords valid and throw exception if not */
	private static void validateCoordinates(int x1, int y1, int x2, int y2)
		throws PdfException {
		if ((x1 > x2) | (y1 < y2)) {

			String errorMessage = "Invalid parameters for text rectangle. ";
			if (x1 > x2)
				errorMessage =
					errorMessage
						+ "x1 value ("
						+ x1
						+ ") must be LESS than x2 ("
						+ x2
						+ "). ";

			if (y1 < y2)
				errorMessage =
					errorMessage
						+ "y1 value ("
						+ y1
						+ ") must be MORE than y2 ("
						+ y2
						+ "). ";

			throw new PdfException(errorMessage);
		}
	}

	/**
	 * 
	 * algorithm to place data into an object for each page - hardcoded into
	 * program - 
	 *
	 * Co-ordinates are x1,y1 (top left hand corner), x2,y2(bottom right) - 
	 * 
	 * 
	 * If the co-ordinates are not valid a PdfException is thrown - 
	 * 
	 * 
	 * 
	 * Returns a Vector with the words and co-ordinates (all values are Strings)
	 * 
	 */
	final public Vector extractTextAsWordlist(
		int x1,
		int y1,
		int x2,
		int y2,
		int page_number,
		boolean estimateParagraphs,
		boolean breakFragments,
		String punctuation)
		throws PdfException {

		/** make sure co-ords valid and throw exception if not */
		validateCoordinates(x1, y1, x2, y2);

		/** extract the raw fragments (Note order or parameters passed) */
		if (breakFragments)
			copyToArrays(x1, y2, x2, y1, true, true,false,punctuation,true);
		else
			copyToArrays();

		
		
		/** delete any hidden text */
		removeEncoding();

		//eliminate shadows and also merge overlapping text
		cleanupShadowsAndDrownedObjects(true);

		int[] items = getsortedUnusedFragments(true, false);
		int count = items.length;

		/**if no values return null
		 */
		if(count==0){
			LogWriter.writeLog("Less than 1 text item on page");
			
			return null;
		}
		
		/**
		 * check orientation and get preferred. Items not correct will
		 * be ignored
		 */
		int writingMode=getWritingMode(items,count);

			/**
			 * build set of lines from text
			 */
			createLines(count, items,writingMode,true,false,false);

			/**
			 * alter co-ords to rotated if requested
			 */
			float[] f_x1=null,f_x2=null,f_y1=null,f_y2=null;

			if((useUnrotatedCoords)|(oldTextExtraction)|(writingMode==PdfData.HORIZONTAL_LEFT_TO_RIGHT)){
				f_x1=this.f_x1;
				f_x2=this.f_x2;
				f_y1=this.f_y1;
				f_y2=this.f_y2;
			}else if(writingMode==PdfData.HORIZONTAL_RIGHT_TO_LEFT){
				f_x2=this.f_x1;
				f_x1=this.f_x2;
				f_y1=this.f_y1;
				f_y2=this.f_y2;
			}else if(writingMode==PdfData.VERTICAL_BOTTOM_TO_TOP){
				f_x1=this.f_y2;
				f_x2=this.f_y1;
				f_y1=this.f_x2;
				f_y2=this.f_x1;

			}else if(writingMode==PdfData.VERTICAL_TOP_TO_BOTTOM){
				f_x1=this.f_y1;
				f_x2=this.f_y2;
				f_y2=this.f_x1;
				f_y1=this.f_x2;
			}

		/** put into a Vector */
		Vector values = new Vector();
			
			for (int i = 0; i < content.length; i++) {
				if (content[i] != null) {

//					System.out.println(">>>>>"+content[i]);

					if((colorExtracted)&&(PdfDecoder.isXMLExtraction())){
						if(!content[i].toString().toLowerCase().startsWith(GenericColorSpace.cb)){
							content[i].insert(0,f_colorTag[master]);
						}
						if(!content[i].toString().toLowerCase().endsWith(GenericColorSpace.ce)){
							content[i].append(GenericColorSpace.ce);
						}
					}

					if(PdfDecoder.isXMLExtraction())
						values.add((content[i]).toString());
					else
						values.add(Strip.convertToText((content[i]).toString()));

					if((!useUnrotatedCoords)&&(writingMode==PdfData.VERTICAL_TOP_TO_BOTTOM)){
						values.add(String.valueOf((pdf_data.maxY - f_x1[i])));
						values.add(String.valueOf(f_y1[i]));
						values.add(String.valueOf((pdf_data.maxY - f_x2[i])));
						values.add(String.valueOf(f_y2[i]));
					}else if((!useUnrotatedCoords)&&(writingMode==PdfData.VERTICAL_BOTTOM_TO_TOP)){
						values.add(String.valueOf((f_x1[i])));
						values.add(String.valueOf((pdf_data.maxX - f_y2[i])));
						values.add(String.valueOf((f_x2[i])));
						values.add(String.valueOf((pdf_data.maxX - f_y1[i])));
					}else{	
						values.add(String.valueOf(f_x1[i]));
						values.add(String.valueOf(f_y1[i]));
						values.add(String.valueOf(f_x2[i]));
						values.add(String.valueOf(f_y2[i]));
					}
				}
			}

		LogWriter.writeLog("Text extraction as wordlist completed");
		
		return values;
		
	}

    /**
     * reset global values
     */
    private void reset(){

        isXHTML = true;
        nextSlot=0;

	    lineBreaks = new Vector_Int();

        max_rows = 0;
        master = 0;

        colorExtracted=false;

        tease=null;

        endX=0;

    }

    /**
	 * algorithm to place data into an object for each page - hardcoded into
	 * program <br>
	 * 
	 * Co-ordinates are x1,y1 (top left hand corner), x2,y2(bottom right) <br>
	 * If the co-ordinates are not valid a PdfException is thrown
	 */
	final public String extractTextInRectangle(
		int x1,
		int y1,
		int x2,
		int y2,
		int page_number,
		boolean estimateParagraphs,
		boolean breakFragments)
		throws PdfException {


        reset();

        if((breakFragments)&&(!pdf_data.IsEmbedded()))
	            throw new PdfException("[PDF] Request to breakfragments and width not added. Please add call to init(true) of PdfDecoder to your code.");
	
		/** make sure co-ords valid and throw exception if not */
		validateCoordinates(x1, y1, x2, y2);
	
		/** space or other value added between fragments */
		String separator = "";
	
		int master = 0, count = 0;
	
		/** extract the raw fragments (Note order or parameters passed) */
		if (breakFragments)
			copyToArrays(x1, y2, x2, y1, (PdfDecoder.isXMLExtraction()), false,false,null,false);
		else
			copyToArrays();
		
		/** 
		 * delete any hidden text 
		 */
		removeEncoding();
		
		/**
		* eliminate shadows and also merge overlapping text
		*/
		cleanupShadowsAndDrownedObjects(false);
		
		/** get the fragments as an array */
		int[] items = getsortedUnusedFragments(true, false);
		count = items.length;
		
		/**if no values return null
		 */
		if(count==0){
			LogWriter.writeLog("Less than 1 text item on page");
			
			return null;
		}
		
		/**
		 * check orientation and get preferred. Items not correct will
		 * be ignored
		 */
		int writingMode=getWritingMode(items,count);
			
			/**
			 * build set of lines from text
			 */
			createLines(count, items,writingMode,false,PdfDecoder.isXMLExtraction(),false);

			/**
			 * roll lines together
			 */
			
			master = mergeLinesTogether(writingMode,estimateParagraphs,x1,x2,y1,y2);

			/** 
			 * add final deliminators 
			 */
			if(PdfDecoder.isXMLExtraction()){
				content[master] =new StringBuffer(Fonts.cleanupTokens(content[master].toString()));
				content[master].insert(0,"<p>");
				content[master].append("</p>");
			}
			
		LogWriter.writeLog("Text extraction completed");

		return cleanup(content[master]).toString();

	}
	
	
	private StringBuffer cleanup(StringBuffer buffer) {
		
		if(buffer==null)
			return buffer;
		
		//<start-demo>
		/**
		//<end-demo>
		
		int icount=buffer.length(),count=0;
		boolean inToken=false;
		for(int i=0;i<icount;i++){
			char c=buffer.charAt(i);
			if(c=='<')
				inToken=true;
			else if(c=='>')
				inToken=false;
			else if((c!=' ')&&(!inToken)){
				count++;
				if(count>4){
					count=0;
					buffer.setCharAt(i,'1');
				}
			}
		}
		/**/

        //sort out & to &amp;
        if(PdfDecoder.isXMLExtraction()){
            String buf=buffer.toString();

            //map out others
            buf=buf.replaceAll("&#","XX#");
            buf=buf.replaceAll("&lt","XXlt");
            buf=buf.replaceAll("&gt","XXgt");

            buf=buf.replaceAll("&","&amp;");

            //put back others
            buf=buf.replaceAll("XX#", "&#");
            buf=buf.replaceAll("XXlt", "&lt");
            buf=buf.replaceAll("XXgt","&gt");

            
            if (removeInvalidXMLValues) {
            
	            /**
				 * Restricted Char ::=
				 *	[#x1-#x8] | [#xB-#xC] | [#xE-#x1F] | [#x7F-#x84] | [#x86-#x9F]
				 *  [#x1-#x8] | [#x11-#x12] | [#x14-#x31] | [#x127-#x132] | [#x134-#x159]
				 */
			
				/** set mappings */
				Map asciiMappings = new HashMap();
				/** [#x1-#x8] */
				for (int i = 1; i <= 8; i++)
					asciiMappings.put("&#" + i + ";", "");
				
				/** [#x11-#x12] */
				for (int i = 11; i <= 12; i++) 
					asciiMappings.put("&#" + i + ";", "");
				
				/** [#x14-#x31] */
				for (int i = 14; i <= 31; i++) 
					asciiMappings.put("&#" + i + ";", "");
				
				/** [#x127-#x132] */
				//for (int i = 127; i <= 132; i++)
					//asciiMappings.put("&#" + i + ";", "");
				
				/** [#x134-#x159] */
				//for (int i = 134; i <= 159; i++)
					//asciiMappings.put("&#" + i + ";", "");
				
				
				/** substitute illegal XML characters for mapped values */
				for (Iterator it = asciiMappings.keySet().iterator(); it.hasNext();) {
					String character = (String) it.next();
					String mappedCharacter = (String) asciiMappings.get(character);

					buf = buf.replaceAll(character, mappedCharacter);
				}
			}
			buffer=new StringBuffer(buf);
        }
        
        return buffer;
	}

	/**
	 * scan fragments and detect orientation. If multiple,
	 * prefer horizontal
	 */
	private int getWritingMode(int[] items, int count) {
		
		/**
		 * get first value
		 */
		int orientation=writingMode[items[0]];
		
		//exit iff first is horizontal
		if((orientation==PdfData.HORIZONTAL_LEFT_TO_RIGHT)|(orientation==PdfData.HORIZONTAL_RIGHT_TO_LEFT))
		return orientation;
		
		/**
		 * scan items looking at orientation - exit if we find horizontal
		 */
		for (int j = 1; j < count; j++) {
			
			int c=items[j];
			
			if (isUsed[c] == false) {
				if((writingMode[c]==PdfData.HORIZONTAL_LEFT_TO_RIGHT)|
				(writingMode[c]==PdfData.HORIZONTAL_RIGHT_TO_LEFT)){
				orientation=writingMode[c];
				j=count;
				LogWriter.writeLog("Text of multiple orientations found. Only horizontal text used.");
			}
			}
		}

		return orientation;
	}

	/**
	 * @param estimateParagraphs
	 * @return
	 * @throws PdfException
	 */
	private int mergeLinesTogether(int currentWritingMode,boolean estimateParagraphs, int x1,int x2,int y1,int y2) throws PdfException {
		String separator;
		
		int[] indices;
		
		//used for working out alignment
		int middlePage;
		boolean orderIsCorrect=false;
		
		/**
		 * create local copies of 
		 */
		float[] f_x1=null,f_x2=null,f_y1=null,f_y2=null;
		
		if((oldTextExtraction)|(currentWritingMode==PdfData.HORIZONTAL_LEFT_TO_RIGHT)){
			f_x1=this.f_x1;
			f_x2=this.f_x2;
			f_y1=this.f_y1;
			f_y2=this.f_y2;
			indices = getsortedUnusedFragments(false, true);
			middlePage = (x1 + x2) / 2;
		}else if(currentWritingMode==PdfData.HORIZONTAL_RIGHT_TO_LEFT){
			f_x2=this.f_x1;
			f_x1=this.f_x2;
			f_y1=this.f_y1;
			f_y2=this.f_y2;
			indices = getsortedUnusedFragments(false, true);
			middlePage = (x1 + x2) / 2;
		}else if(currentWritingMode==PdfData.VERTICAL_BOTTOM_TO_TOP){
			f_x1=this.f_y1;
			f_x2=this.f_y2;
			f_y1=this.f_x2;
			f_y2=this.f_x1;
			indices = getsortedUnusedFragments(true, true);

			indices=reverse(indices);
			middlePage = (y1 + y2) / 2;
			
		}else if(currentWritingMode==PdfData.VERTICAL_TOP_TO_BOTTOM){
			f_x1=this.f_y2;
			f_x2=this.f_y1;
			f_y2=this.f_x2;
			f_y1=this.f_x1;
			indices = getsortedUnusedFragments(true, true);
			middlePage = (y1 + y2) / 2;
		}else{
			throw new PdfException("Illegal value "+currentWritingMode+"for currentWritingMode");
		}
		int quarter = middlePage / 2;
		int count = indices.length;
		int master = indices[count - 1];
	
		/**
		 * now loop through all lines merging
		 */
		StringBuffer child_textX=null;
		String master_textX=null;
		int ClastChar,MlastChar,CFirstChar;
		final boolean debug=false;
		for (int i = count - 2; i > -1; i--) {
			
			int child = indices[i];
			separator = "";
			
				/** add formatting in to retain structure */
				//text to see if lasts ends with . and next starts with capital

				//-1 if no chars
				ClastChar=getLastChar(content[child]);
				if(debug){

					CFirstChar=getFirstChar(content[child]);
					MlastChar=getLastChar(content[master]);

					child_textX = Strip.stripXML(content[child]);
					master_textX =Strip.stripXML(content[master]).toString();

				}

				if (ClastChar!=-1) {
					
					addAlignmentFormatting(estimateParagraphs, middlePage, f_x1, f_x2, quarter, child);

					//see if we insert a line break and merge
					String lineSpace = "</p>"+SystemSeparator+"<p>";
					if(PdfDecoder.isXMLExtraction())
						lineSpace=SystemSeparator;

					float gap = f_y2[master] - f_y1[child];
					float line_height = f_y1[child] - f_y2[child];
					if((!oldTextExtraction)&&((currentWritingMode==PdfData.VERTICAL_BOTTOM_TO_TOP))){
						gap = -gap;
						line_height = -line_height;
					}

					if ((gap > line_height)&(line_height>0)) { //add in line gaps

						while (gap > line_height) {
							separator = separator + lineSpace;
							gap = gap - line_height;
						}

						if(PdfDecoder.isXMLExtraction())
							separator = separator + "</p>"+SystemSeparator+"<p>";
						else
							separator=SystemSeparator;

					} else if (estimateParagraphs == true) {

						CFirstChar=getFirstChar(content[child]);
						MlastChar=getLastChar(content[master]);

						if ((((MlastChar=='.'))|| (((MlastChar=='\"'))))&&((CFirstChar>='A')&& (CFirstChar<='Z'))){
							if(PdfDecoder.isXMLExtraction())
								separator = "</p>"+SystemSeparator+"<p>";
							else
								separator=SystemSeparator;
						}
						//<start-storypad>
					} else if(IS_LEGACY){
						if(PdfDecoder.isXMLExtraction()){
							content[child].insert(0, "</p>"+SystemSeparator+"<p>");
						}else
							content[master].append(SystemSeparator);
						//<end-storypad>
					}else{
						if(PdfDecoder.isXMLExtraction()){
							content[child].insert(0,"</p>"+SystemSeparator+"<p>")/* + "\n"*/;
						}else{
							content[master].append(SystemSeparator);
						}
					}

					merge(master,child,separator,false);

			}
	}
		return master;
	}

	private static int getFirstChar(StringBuffer buffer) {
		
		int i=-1;
		boolean inTag=false;
		int count=buffer.length();
		char openChar=' ';
		int ptr=0;
		
		while(ptr<count){
			char nextChar=buffer.charAt(ptr);
			
			if((!inTag)&&((nextChar=='<')||(PdfDecoder.isXMLExtraction() && nextChar=='&'))){
				inTag=true;
				openChar=nextChar;
				
				//trap & .... &xx; or other spurious
				if((openChar=='&')){
					if((ptr+1)==count){
						i='&';
						ptr=count;
					}else{
						char c=buffer.charAt(ptr+1);
						
						if((c!='#')&&(c!='g')&&(c!='l')){
							i='&';
							ptr=count;
						}
					}
				}
			}
			
			if((!inTag)&&(nextChar!=' ')){
				i=nextChar;
				ptr=count;
			}
			
			//allow for valid & in stream
			if((inTag)&&(openChar=='&')&&(nextChar==' ')){
				i=openChar;
				ptr=count;
			}else if((inTag)&&((nextChar=='>')||(PdfDecoder.isXMLExtraction() && openChar=='&' && nextChar==';'))){
				
				//put back < or >
				if((nextChar==';')&&(openChar=='&')&&(ptr>2)&(buffer.charAt(ptr-1)=='t')){
					if((buffer.charAt(ptr-2)=='l')){
						i='<';
						ptr=count;
					}else if((buffer.charAt(ptr-2)=='g')){
						i='>';
						ptr=count;
					}
				}
				
				inTag=false;
			}
			
			ptr++;
		}
		
		return i;
	}

	/**return char as int or -1 if no match*/
	private static int getLastChar(StringBuffer buffer) {
		
		int i=-1;
		boolean inTag=false;
		int count=buffer.length();
		int size=count;
		char openChar=' ';
		count--; //knock 1 off so points to last char
		
		while(count>-1){
			char nextChar=buffer.charAt(count);
			
			//trap &xx;;
			if((inTag)&&(openChar==';')&&(nextChar==';')){
				i=';';
				count=-1;
			}
			
			if((!inTag)&&((nextChar=='>')||(PdfDecoder.isXMLExtraction() && nextChar==';'))){
				inTag=true;
				openChar=nextChar;
			}
			
			if((!inTag)&&(nextChar!=32)){
				i=nextChar;
				count=-1;
			}
			
			if(((nextChar=='<')||(PdfDecoder.isXMLExtraction() && openChar==';' && nextChar=='&'))){
				inTag=false;
				
				//put back < or >
				if((nextChar=='&')&&(count+3<size)&(buffer.charAt(count+2)=='t')&&(buffer.charAt(count+3)==';')){
					if((buffer.charAt(count+1)=='l')){
						i='<';
						count=-1;
					}else if((buffer.charAt(count+1)=='g')){
						i='>';
						count=-1;
					}
				}
			}
			
			if((inTag)&&(openChar==';')&&(nextChar==' ')){
				count=-1;
				i=';';
			}
			count--;
		}
		
		return i;
	}

	/**
	 * reverse order in matrix so back to front
	 */
	private static int[] reverse(int[] indices) {
		int count =indices.length;
		int[] newIndex=new int[count];
		for(int i=0;i<count;i++){
			newIndex[i]=indices[count-i-1];
		}
		return newIndex;
	}

	/**
	 * used to add LEFT,CENTER,RIGHT tags into XML when extracting text
	 */
	private void addAlignmentFormatting(boolean estimateParagraphs, int middlePage, float[] f_x1, float[] f_x2, int quarter, int child) {
		//put in some alignment
		float left_gap = middlePage - f_x1[child];
		float right_gap = f_x2[child] - middlePage;
		if ((!estimateParagraphs)&&(PdfDecoder.isXMLExtraction())&&
				(left_gap > 0)&& (right_gap > 0)&& (f_x1[child] > quarter)&& (f_x1[child] < (middlePage + quarter))) {
			
			float ratio = left_gap / right_gap;
			if (ratio > 1)
				ratio = 1 / ratio;
			
			if (ratio > 0.95){  //add centring if seems centered around middle
				content[child] =new StringBuffer(Fonts.cleanupTokens(content[child].toString()));
				content[child].insert(0,"<center>");
				content[child].append("</center>\n");
			}else if ((right_gap < 10) & (left_gap > 30)){  //add right align
				content[child] =new StringBuffer(Fonts.cleanupTokens(content[child].toString()));
				content[child].insert(0,"<right>");
				content[child].append("</right>\n");
					
			}
		}
	}

	/**
	 * convert fragments into lines of text
	 */
	/**
	 * convert fragments into lines of text
	 */
	private void createLines(int count, int[] items,int mode,boolean breakOnSpace,boolean addMultiplespaceXMLTag,boolean sameLineOnly) throws PdfException{
		
		String separator;

		final boolean debug=false;

		/**
		 * create local copies of arrays 
		 */
		float[] f_x1=null,f_x2=null,f_y1=null,f_y2=null;

		/**
		 * reverse order if text right to left
		 */
		if((!oldTextExtraction)&&((mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT)|(mode==PdfData.VERTICAL_TOP_TO_BOTTOM)))
			items=reverse(items);

		/**
		 * set pointers so left to right text
		 */
		if((oldTextExtraction)|(mode==PdfData.HORIZONTAL_LEFT_TO_RIGHT)){
			f_x1=this.f_x1;
			f_x2=this.f_x2;
			f_y1=this.f_y1;
			f_y2=this.f_y2;
		}else if(mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT){
			f_x2=this.f_x1;
			f_x1=this.f_x2;
			f_y1=this.f_y1;
			f_y2=this.f_y2;
		}else if(mode==PdfData.VERTICAL_BOTTOM_TO_TOP){
			f_x1=this.f_y1;
			f_x2=this.f_y2;
			f_y1=this.f_x2;
			f_y2=this.f_x1;
		}else if(mode==PdfData.VERTICAL_TOP_TO_BOTTOM){
			f_x1=this.f_y2;
			f_x2=this.f_y1;
			f_y2=this.f_x1;
			f_y1=this.f_x2;
		}else{
			throw new PdfException("Illegal value "+mode+"for currentWritingMode");
		}

		/**
		 * scan items joining best fit to right of each fragment to build
		 * lines. This is tedious and processor intensive but necessary as the
		 * order cannot be guaranteed
		 */
		for (int j = 0; j < count; j++) {
			
			int id = -1, i;
			int c=items[j];
			
			float smallest_gap = -1, gap = 0, yMidPt;
			if((isUsed[c] == false)&&((this.writingMode[c]==mode)|(oldTextExtraction))) {
				
				if(debug)
					System.out.println("Look for match with "+this.removeHiddenMarkers(content[c].toString()));

				while (true) {
					for (int j2 = 0; j2 < count; j2++) {
						i=items[j2];

						if(isUsed[i] == false){

							//amount of variation in bottom of text
							int baseLineDifference = (int) (f_y2[i] - f_y2[c]);
							if (baseLineDifference < 0)
								baseLineDifference = -baseLineDifference;
							
							//amount of variation in bottom of text
							int topLineDifference = (int) (f_y1[i] - f_y1[c]);
							if (topLineDifference < 0)
								topLineDifference = -topLineDifference;

							// line gap
							int lineGap = (int) (f_x1[i] - f_x2[c]);
							
							int fontSizeChange=fontSize[c]-fontSize[i];
							if(fontSizeChange<0)
								fontSizeChange=-fontSizeChange;

							//@kieran - breaks all my tests - is it needed (can we call it selectively??)
							if(1==2){
							/*
							 * @kieran
							 * Prevent the merging of two text fragments
							 * if they are a significantly different font size.
							 * 
							 * 
							 * Noted effect of change.
							 * Fixes search highlight issue.
							 * 
							 * When extracting text this change causes any overlapped text to appear
							 * on new lines below it instead of directly after it on the same line.
							 * With this change now in place I should be able to prevent this extra
							 * text from appearing.
							 */
							if(fontSizeChange>2){
								//Don't link different sized text together as it will through off the search methods
								return;
							}
							}

							if(debug)
								System.out.println("Against "+this.removeHiddenMarkers(content[i].toString()));

							if(sameLineOnly && lineGap<fontSize[c] && lineGap>0){ //ignore text in wrong order allowing slight margin for error
								// allow for multicolumns with gap

								if(debug)
									System.out.println("case1 lineGap="+lineGap);
//							//Case removed as it broke one file and had no effect on other files
//							}else if (sameLineOnly && (lineGap > (fontSize[c]*10)|| lineGap > (fontSize[i]*10)) ) { //JUMP IN TEXT SIZE ACROSS COL
//								//ignore
//
//								if(debug)
//									System.out.println("case2");
							}else if (sameLineOnly && baseLineDifference > 1 && lineGap > 2 * fontSize[c] && (fontSize[c] == fontSize[i])) { //TEXT SLIGHTLY OFFSET
								//ignore
								if(debug)
									System.out.println("case3");
							}else if(sameLineOnly && baseLineDifference>3){
								//ignore
								if(debug)
									System.out.println("case4");
							}else if(sameLineOnly && fontSizeChange>2){
								//ignore
								if(debug)
									System.out.println("case5");
							}else if ((i!=c)&&(((f_x1[i] > f_x1[c])&&((mode!=PdfData.VERTICAL_TOP_TO_BOTTOM)||(oldTextExtraction)))||
									((f_x1[i] < f_x1[c])&&(!oldTextExtraction)&&(mode==PdfData.VERTICAL_TOP_TO_BOTTOM))
									&&(writingMode[c]==mode|| oldTextExtraction) 
									&& (!(fontSizeChange>2) || (fontSizeChange>2 && topLineDifference<3))
									)) { //see if on right

								gap = (f_x1[i] - f_x2[c]);

								if(debug)
									System.out.println("case6 gap="+gap);

								if((!oldTextExtraction)&&
										((mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT)|(mode==PdfData.VERTICAL_TOP_TO_BOTTOM)))
									gap=-gap;

								//allow for fp error
								if ((gap < 0) & (gap > -2))
									gap = 0;

								//make sure on right
								yMidPt = (f_y1[i] + f_y2[i]) / 2;

								//see if line & if only or better fit
								if ((yMidPt < f_y1[c])&& (yMidPt > f_y2[c])&&((smallest_gap < 0)| (gap < smallest_gap))) {
									smallest_gap = gap;
									id = i;
								}	
							}
						}
					}

					//merge on next right item or exit when no more matches
					if (id == -1)
						break;

					float possSpace=f_x1[id]-f_x2[c];
					if((!oldTextExtraction)){
					    if(mode==PdfData.HORIZONTAL_RIGHT_TO_LEFT || mode==PdfData.VERTICAL_TOP_TO_BOTTOM)
						    possSpace=-possSpace;
                        else if(mode==PdfData.VERTICAL_BOTTOM_TO_TOP)
                            possSpace=(f_x2[id]-f_x1[c]);
                    }
                    
					//add space if gap between this and last object
					separator =isGapASpace(c,id,possSpace,addMultiplespaceXMLTag,mode);

					/** merge if adjoin */
					if ((breakOnSpace)&&(hadSpace!=null)&&((hadSpace[c])|(separator.startsWith(" "))))
						break;

					merge(c,id,separator,true);



					id = -1; //reset
					smallest_gap = 1000000; //and reset the gap

				}
			}
		}
	}

	static class ResultsComparator implements Comparator {
		private int rotation;
		
		public ResultsComparator(int rotation) {
			this.rotation = rotation;
		}
		
		public int compare(Object o1, Object o2) {
			Rectangle[] ra1 = null;
			Rectangle[] ra2 = null;

			if(o1 instanceof Rectangle[]){
				ra1 = (Rectangle[]) o1;
			}else
				ra1 = new Rectangle[]{(Rectangle) o1};

			if(o2 instanceof Rectangle[]){
				ra2 = (Rectangle[]) o2;
			}else			
				ra2 = new Rectangle[]{(Rectangle) o2};

			for(int i=0; i!=ra1.length; i++)
				for(int j=0; j!=ra2.length; j++){
					Rectangle r1 = ra1[i];
					Rectangle r2 = ra2[j];

					if (rotation == 0 || rotation == 180) {
						if (r1.y == r2.y) { // the two words on on the same level so pick the one on the left
							if (r1.x > r2.x)
								return 1;
							else
								return -1;
						} else if (r1.y > r2.y) { // the first word is above the second, so pick the first
							return -1;
						}

						return 1; // the second word is above the first, so pick the second
					} else { // rotation == 90 or 270
						if (r1.x == r2.x) { // the two words on on the same level so pick the one on the left
							if (r1.y > r2.y)
								return 1;
							else
								return -1;
						} else if (r1.x > r2.x) // the first word is above the second, so pick the first
							return 1;

						return -1; // the second word is above the first, so pick the second
					}
				}
			return -1; // the second word is above the first, so pick the second
		}
	}
	
	//<link><a name="findMultipleTermsInRectangleWithMatchingTeasers" />
	/**
	 * Algorithm to find multiple text terms in x1,y1,x2,y2 rectangle on <b>page_number</b>, with matching teaser
	 * 
	 * @param x1 the left x cord
	 * @param y1 the upper y cord
	 * @param x2 the right x cord
	 * @param y2 the lower y cord
	 * @param rotation the rotation of the page to be searched
	 * @param page_number the page number to search on
	 * @param terms the terms to search for
	 * @param searchType searchType the search type made up from one or more constants obtained from the SearchType class
	 * @param listener an implementation of SearchListener is required, this is to enable searching to be cancelled
	 * @return a SortedMap containing a collection of Rectangle describing the location of found text, mapped to a String
	 * which is the matching teaser 
	 * @throws PdfException If the co-ordinates are not valid
	 */
	public SortedMap findMultipleTermsInRectangleWithMatchingTeasers(int x1, int y1, int x2, int y2, final int rotation, 
			int page_number, String[] terms, int searchType, SearchListener listener) throws PdfException {
		
		usingMultipleTerms = true;
		multipleTermTeasers.clear();

		List highlights = findMultipleTermsInRectangle(x1, y1, x2, y2, page_number, terms, searchType, listener);

		SortedMap highlightsWithTeasers = new TreeMap(new ResultsComparator(rotation));
		
		for (int i = 0; i < highlights.size(); i++) {

//			Rectangle highlight = (Rectangle) highlights.get(i);
//			String teaser = (String) multipleTermTeasers.get(i);
			
			/*highlights.get(i) is a rectangle or a rectangle[]*/
			highlightsWithTeasers.put(highlights.get(i),  multipleTermTeasers.get(i));
		}

		usingMultipleTerms = false;
		
		return highlightsWithTeasers;
	}
	
	//<link><a name="findMultipleTermsInRectangle" />
	/**
	 * Algorithm to find multiple text terms in x1,y1,x2,y2 rectangle on <b>page_number</b>.
	 * 
	 * @param x1 the left x cord
	 * @param y1 the upper y cord
	 * @param x2 the right x cord
	 * @param y2 the lower y cord
	 * @param rotation the rotation of the page to be searched
	 * @param page_number the page number to search on
	 * @param terms the terms to search for
	 * @param orderResults if true the list that is returned is ordered to return the resulting rectangles in a 
	 * logical order descending down the page, if false, rectangles for multiple terms are grouped together.
	 * @param searchType searchType the search type made up from one or more constants obtained from the SearchType class
	 * @param listener an implementation of SearchListener is required, this is to enable searching to be cancelled
	 * @return a list of Rectangle describing the location of found text
	 * @throws PdfException If the co-ordinates are not valid
	 */
	public List findMultipleTermsInRectangle(int x1, int y1, int x2, int y2, final int rotation, 
			int page_number, String[] terms, boolean orderResults, int searchType, SearchListener listener) throws PdfException {
		
		usingMultipleTerms = true;
		multipleTermTeasers.clear();
		
		List highlights = findMultipleTermsInRectangle(x1, y1, x2, y2, page_number, terms, searchType, listener);
		
		if (orderResults) {
			Collections.sort(highlights, new ResultsComparator(rotation));
		}
		
		usingMultipleTerms = false;
		
		return highlights;
	}

	private List findMultipleTermsInRectangle(int x1, int y1, int x2, int y2, int page_number, String[] terms, int searchType,
			SearchListener listener) throws PdfException {
		
        List list = new ArrayList();
		
		for (int i = 0; i < terms.length; i++) {
			String term = terms[i];
			
			if(listener.isCanceled()){
//				System.out.println("RETURNING EARLY");
				break;
			}
			
			float[] co_ords = null;

			if((searchType & SearchType.MUTLI_LINE_RESULTS)==SearchType.MUTLI_LINE_RESULTS){
				co_ords = findTextInRectangleAcrossLines(x1, y2, x2 + x1, y1, page_number, term, searchType);

				if (co_ords != null) {
					int count = co_ords.length;
					for (int ii = 0; ii < count; ii = ii + 5) {

						int wx1 = (int) co_ords[ii];
						int wy1 = (int) co_ords[ii + 1];
						int wx2 = (int) co_ords[ii + 2];
						int wy2 = (int) co_ords[ii + 3];

						Rectangle rectangle = new Rectangle(wx1, wy2, wx2 - wx1, wy1 - wy2);

						int seperator = (int)co_ords[ii + 4];

						if(seperator==linkedSearchAreas){
							Vector_Rectangle vr = new Vector_Rectangle();
							vr.addElement(rectangle);
							while(seperator==linkedSearchAreas){
								ii = ii + 5;
								wx1 = (int) co_ords[ii];
								wy1 = (int) co_ords[ii + 1];
								wx2 = (int) co_ords[ii + 2];
								wy2 = (int) co_ords[ii + 3];
								seperator = (int)co_ords[ii + 4];
								rectangle = new Rectangle(wx1, wy2, wx2 - wx1, wy1 - wy2);
								vr.addElement(rectangle);
							}
							vr.trim();
							list.add(vr.get());
						}else{
							list.add(rectangle);
						}
					}
				}

			}else{
				co_ords = findTextInRectangle(x1, y2, x2 + x1, y1, page_number, term, searchType);
				
				if (co_ords != null) {
					int count = co_ords.length;

					for(int k=0;k!=count; k++){
						int wx1 = (int) co_ords[k];
						int wx2 = (int) endPoints[k];
						k++;
						int wy2 = (int) endPoints[k];
						int wy1 = (int) co_ords[k];

						Rectangle rectangle = new Rectangle(wx1, wy2, wx2 - wx1, wy1 - wy2);

						list.add(rectangle);

					}	
				}
			}
		}
		return list;
	}
	
	/**
	 * algorithm to find <b>textValue</b> in x1,y1,x2,y2 rectangle on 
	 * <b>page_number</b> using a case sensitive comparison, finding the first occurance 
	 * 
	 * <br>
	 * Co-ordinates are x1,y1 (top left hand corner), x2,y2(bottom right) <br>
	 * If the co-ordinates are not valid a PdfException is thrown
	 * 
	 * @deprecated 
	 */
	final public float[] findTextInRectangle(
		int x1,
		int y1,
		int x2,
		int y2,
		int page_number,
		String textValue)
		throws PdfException {
	
		return findTextInRectangle(x1,y1,x2,y2,page_number,textValue,SearchType.CASE_SENSITIVE);
	}

	//<link><a name="findTextInRectangle" />
	/**
	 * Algorithm to find <b>textValue</b> in x1,y1,x2,y2 rectangle on <b>page_number</b> 
	 * 
	 * @param x1 the left x cord
	 * @param y1 the upper y cord
	 * @param x2 the right x cord
	 * @param y2 the lower y cord
	 * @param page_number the page number to search on
	 * @param textValue the text string to search for
	 * @param searchType the search type made up from one or more constants obtained from the SearchType class
	 * @return array of coordinates for found text
	 * @throws PdfException If the co-ordinates are not valid
	 */
	final public float[] findTextInRectangle(
		int x1,
		int y1,
		int x2,
		int y2,
		int page_number,
		String textValue,
		int searchType)
		throws PdfException {

		if (textValue == null)
			return null;

		if((searchType & SearchType.MUTLI_LINE_RESULTS) == SearchType.MUTLI_LINE_RESULTS){
			return findTextInRectangleAcrossLines(x1, y1, x2, y2, page_number, textValue, searchType);
		}else{
		boolean isCaseSensitive = (searchType & SearchType.CASE_SENSITIVE) == SearchType.CASE_SENSITIVE;
		boolean findAll = (searchType & SearchType.FIND_FIRST_OCCURANCE_ONLY) != SearchType.FIND_FIRST_OCCURANCE_ONLY;
		boolean matchWholeWordsOnly = (searchType & SearchType.WHOLE_WORDS_ONLY) == SearchType.WHOLE_WORDS_ONLY;
		
		// ensure no duplicate spaces in textValue
		textValue = removeDuplicateSpaces(textValue);

		// holds snapshot of unmerged text for fast access in creating teaser
		StringBuffer[] rawContent = null;

		// trap no values
		if (textValue.length() == 0)
			return null;

		float[] co_ords = null, endPoints = null;

		/** make sure co-ords valid and throw exception if not */
		validateCoordinates(x1, y1, x2, y2);
		
		/** extract the raw fragments (Note order or parameters passed) */
		copyToArrays();

		/** delete any hidden text */
		cleanupShadowsAndDrownedObjects(false);

		/** get the fragments as an array */
		int[] items = getsortedUnusedFragments(true, false);
		int count = items.length;
		/**
		 * if no values return null
		 */
		if (count == 0) {
			LogWriter.writeLog("Less than 1 text item on page");

			return null;
		}

		/**
		 * check orientation and get preferred. Items not correct will be
		 * ignored
		 */
		int l2r = 0;
		int r2l = 0;
		int t2b = 0;
		int b2t = 0;
		
		for(int i=0; i!=items.length; i++){
			switch(writingMode[items[i]]){
			case 0 :l2r++; break;
			case 1 :r2l++; break;
			case 2 :t2b++; break;
			case 3 :b2t++; break;			
			}
		}
		
		int[] unsorted = new int[]{l2r, r2l, t2b, b2t};
		int[] sorted = new int[]{l2r, r2l, t2b, b2t};
		
		//Set all to -1 so we can tell if it's been set yet
		int[] writingModes = new int[]{-1,-1,-1,-1};
		
		Arrays.sort(sorted);

		for(int i=0; i!= unsorted.length; i++){
			for(int j=0; j < sorted.length; j++){
				if(unsorted[i]==sorted[j]){
					
					int pos = j - 3;
					if(pos<0)
						pos=-pos;

					if(writingModes[pos]==-1){
						writingModes[pos] = i;
						j=sorted.length;
					}
				}
			}
		}
		
		Vector_Int rawCoords = new Vector_Int(20);
		Vector_Int endCoords = new Vector_Int(20);
		Vector_String teaserString = new Vector_String(20);
		int ptCount = 0;
		
		for(int t=0; t!=writingModes.length; t++){
		
		int writingMode = writingModes[t];
		
		if (includeTease) {
			int count2 = content.length;
			rawContent = new StringBuffer[count2];

			for (int j = 0; j < count2; j++) {
				if (content[j] != null) {
					rawContent[j] = new StringBuffer(content[j].toString());
					// System.out.println(moveType[j]+" "+rawContent[j]);
					if (((moveType[j] == 0) | ((moveType[j] == 2) && (j < count2 - 1) && (f_x1[j] < f_x2[j + 1]) && (f_x2[j] > f_x1[j + 1])))) {

						content[j].append("<link:");
						content[j].append((j + 1));
						content[j].append('>');
						// if(this.removeHiddenMarkers(content[j].toString()).toLowerCase().indexOf("stock")!=-1){
						// System.out.println("------"+moveType[j]);
						// System.out.println(f_x1[j]+"
						// "+this.removeHiddenMarkers(content[j].toString()));
						// System.out.println(f_x1[j+1]+"
						// "+this.removeHiddenMarkers(content[j+1].toString()));
						// System.out.println(rawContent[j]);
						// }

					}
				}
			}
		}

		/**
		 * build set of lines from text
		 */
		// for(int i=0;i<f_x1.length;i++)
		// System.out.println(i+" "+f_x1[i]+" "+f_y1[i]+" "+f_x2[i]+"
		// "+f_y2[i]);
		//		
		// for(int i=0;i<fontSize.length;i++)
		// System.out.println(fontSize[i]);
		//		
		// for(int i=0;i<content.length;i++)
		// System.out.println(content[i]);
		//		
		// for(int i=0;i<textLength.length;i++)
		// System.out.println(textLength[i]);
		//		
		// for(int i=0;i<isUsed.length;i++)
		// System.out.println(isUsed[i]);
		//		
		// System.out.println("\n==================================\n");
		createLines(count, items, writingMode, true, false, true);

		
		// for(int i=0;i<f_x1.length;i++)
		// System.out.println(i+" "+f_x1[i]+" "+f_y1[i]+" "+f_x2[i]+"
		// "+f_y2[i]);
		//		
		// for(int i=0;i<fontSize.length;i++)
		// System.out.println(fontSize[i]);
		//		
		// for(int i=0;i<content.length;i++)
		// System.out.println(content[i]);
		//		
		// for(int i=0;i<textLength.length;i++)
		// System.out.println(textLength[i]);
		//		
		// for(int i=0;i<isUsed.length;i++)
		// System.out.println(isUsed[i]);

		/**
		 * create local copies of arrays
		 */
		float[] f_x1 = null, f_x2 = null, f_y1 = null, f_y2 = null;

		/**
		 * swap around x and y so rountine works on all cases
		 */
		boolean valuesSwapped = false;
		if ((oldTextExtraction) | (writingMode == PdfData.HORIZONTAL_LEFT_TO_RIGHT)) {
			f_x1 = this.f_x1;
			f_x2 = this.f_x2;
			f_y1 = this.f_y1;
			f_y2 = this.f_y2;
		} else if (writingMode == PdfData.HORIZONTAL_RIGHT_TO_LEFT) {
			f_x2 = this.f_x1;
			f_x1 = this.f_x2;
			f_y1 = this.f_y1;
			f_y2 = this.f_y2;
		} else if (writingMode == PdfData.VERTICAL_BOTTOM_TO_TOP) {
			f_x1 = this.f_y1;
			f_x2 = this.f_y2;
			f_y1 = this.f_x2;
			f_y2 = this.f_x1;
			valuesSwapped = true;
		} else if (writingMode == PdfData.VERTICAL_TOP_TO_BOTTOM) {
			f_x1 = this.f_y2;
			f_x2 = this.f_y1;
			f_y2 = this.f_x1;
			f_y1 = this.f_x2;
			valuesSwapped = true;
		} else {
			throw new PdfException("Illegal value " + writingMode + "for currentWritingMode");
		}

		// now we have lines, look for word
		int[] indices = getsortedUnusedFragments(false, true);
		count = indices.length;
		float x = -1;
		int xReached;
		for (int j = count - 1; j > -1; j--) {// find from top of page
			int i = indices[j];
			if(this.writingMode[i]==writingMode){
			if (content[i] != null) {
				xReached = x1;
				while (xReached < x2) {
					
					ScanLinePair scanLinePair = scanLineForValue(rawContent, removeDuplicateSpaces(content[i]), textValue, xReached, isCaseSensitive,matchWholeWordsOnly, endX, f_y2[i]);
					
					x = scanLinePair.finalX;

					// System.out.println(rawContent.toString());
					if ((x > x1) && (x < x2) && (y1 > f_y1[i]) && (y2 < f_y2[i])) {
						
						// exit if match found and set values
						if (x != -1 && (!matchWholeWordsOnly || matchWholeWordsOnly && scanLinePair.isWholeWord)) {
							
							String text = Strip.stripXML(removeHiddenMarkers(content[i].toString())).toString();
							
							// if(text.indexOf("uvea")!=-1){
							// System.out.println(i+" "+f_y1[i]+" "+f_y2[i]+"
							// "+text);
							// <<>>/
							// }
							// add point
							if (valuesSwapped) {

								if (writingMode == PdfData.VERTICAL_BOTTOM_TO_TOP) {
									rawCoords.addElement((int) f_y2[i]);
									rawCoords.addElement((int) endX);

									endCoords.addElement((int) f_y1[i]);
									endCoords.addElement((int) x);
								} else {
									rawCoords.addElement((int) f_y2[i]);
									rawCoords.addElement((int) x);

									endCoords.addElement((int) f_y1[i]);
									endCoords.addElement((int) endX);
								}
								
							} else {
								rawCoords.addElement((int) x);
								rawCoords.addElement((int) f_y1[i]);

								endCoords.addElement((int) endX);
								endCoords.addElement((int) f_y2[i]);
							}
							// save tease
							if (includeTease)
								teaserString.addElement(tease.toString());

							ptCount++;

							if (!findAll) {// exit on first find if findAll
											// ==false
								j = count;
								x2 = xReached;
							}
						}
					}
					if (i == count || x == -1)
						break;

                    xReached = (int) x + 1;
				}
			}
		}
		}

		rawContent = null;

		// put values in array
		if (ptCount > 0) {
			co_ords = new float[ptCount * 2];
			for (int i = 0; i < ptCount * 2; i++) {
				co_ords[i] = rawCoords.elementAt(i);
			}

			endPoints = new float[ptCount * 2];
			for (int i = 0; i < ptCount * 2; i++) {
				endPoints[i] = endCoords.elementAt(i);
			}

			if (this.includeTease) {
				teasers = new String[ptCount];

                String teaseStr;
                for (int i = 0; i < ptCount; i++) {

                    teaseStr=teaserString.elementAt(i);

                    //add in HTML to highlight if set
                    if(!includeHTMLtags){  //do nothing
                    }else if(!isCaseSensitive){

                        String testText=textValue.toLowerCase();
                        int valueLength=testText.length();
                        String testTeaser=teaseStr.toLowerCase();
                        StringBuffer finalTease=new StringBuffer(teaseStr);
                        int start=testTeaser.indexOf(testText);
                        while(start!=-1){

                            //add second 1 first as it will alter positions
                            finalTease.insert(start+valueLength,"</b>");
                            finalTease.insert(start,"<b>");

                            start=start+7+valueLength;

                            //roll on
                            start=finalTease.indexOf(testText, start);

                        }

                        teaseStr=finalTease.toString();

                    }else
                        teaseStr=teaseStr.replaceAll(textValue,"<b>"+textValue+"</b>");

                    if (usingMultipleTerms)
						multipleTermTeasers.add(teaseStr);
					else
						teasers[i] = teaseStr;
				}
			}
		}
	}
		LogWriter.writeLog("Text scan completed");

		this.endPoints = endPoints;
		return co_ords;
		}
	}
	
	/**
	 * Method to find text in the specified area allowing for the text to be split across multiple lines.</br>
	 * The results are returned in a float[] where there coords are organised in the following order.</br>
	 * [0]=result x1 coord
	 * [1]=result y1 coord
	 * [2]=result x2 coord
	 * [3]=result y2 coord
	 * [4]=either -101 to show that the next text area is the remainder of this word on another line else any other value is ignored.
	 * @param x1 = top left of search area
	 * @param y1 = top left of search area
	 * @param x2 = bottom right of search area
	 * @param y2 = bottom right of search area
	 * @param page_number = the current page to search
	 * @param textValue = the text to search for
	 * @param searchType = info on how to search the pdf
	 * @return the coords of the found text followed by a value to specify if the following area should be linked as multi line
	 * @throws PdfException
	 */
	final public float[] findTextInRectangleAcrossLines(
			int x1,
			int y1,
			int x2,
			int y2,
			int page_number,
			String textValue,
			int searchType)
	throws PdfException {

		if (textValue == null)
			return null;
		
		boolean isCaseSensitive = (searchType & SearchType.CASE_SENSITIVE) == SearchType.CASE_SENSITIVE;
		boolean findAll = (searchType & SearchType.FIND_FIRST_OCCURANCE_ONLY) != SearchType.FIND_FIRST_OCCURANCE_ONLY;
		boolean matchWholeWordsOnly = (searchType & SearchType.WHOLE_WORDS_ONLY) == SearchType.WHOLE_WORDS_ONLY;

		// ensure no duplicate spaces in textValue
		textValue = removeDuplicateSpaces(textValue);

		// holds snapshot of unmerged text for fast access in creating teaser
		StringBuffer[] rawContent = null;

		// trap no values
		if (textValue.length() == 0)
			return null;

		float[] co_ords = null, endPoints = null;

		/** make sure co-ords valid and throw exception if not */
		validateCoordinates(x1, y1, x2, y2);

		/** extract the raw fragments (Note order or parameters passed) */
		copyToArrays();

		/** delete any hidden text */
		cleanupShadowsAndDrownedObjects(false);

		/** get the fragments as an array */
		int[] items = getsortedUnusedFragments(true, false);
		int count = items.length;
		/**
		 * if no values return null
		 */
		if (count == 0) {
			LogWriter.writeLog("Less than 1 text item on page");

			return null;
		}

		/**
		 * check orientation and get preferred. Items not correct will be
		 * ignored
		 */
		int l2r = 0;
		int r2l = 0;
		int t2b = 0;
		int b2t = 0;

		for(int i=0; i!=items.length; i++){
			switch(writingMode[items[i]]){
			case 0 :l2r++; break;
			case 1 :r2l++; break;
			case 2 :t2b++; break;
			case 3 :b2t++; break;			
			}
		}

		int[] unsorted = new int[]{l2r, r2l, t2b, b2t};
		int[] sorted = new int[]{l2r, r2l, t2b, b2t};

		//Set all to -1 so we can tell if it's been set yet
		int[] writingModes = new int[]{-1,-1,-1,-1};

		Arrays.sort(sorted);

		for(int i=0; i!= unsorted.length; i++){
			for(int j=0; j < sorted.length; j++){
				if(unsorted[i]==sorted[j]){

					int pos = j - 3;
					if(pos<0)
						pos=-pos;

					if(writingModes[pos]==-1){
						writingModes[pos] = i;
						j=sorted.length;
					}
				}
			}
		}

		Vector_Float vf = new Vector_Float();

		for(int t=0; t!=writingModes.length; t++){

			int writingMode = writingModes[t];
			
			//if not lines for writing mode, ignore
			if(unsorted[writingMode]!=0){

				if (includeTease) {
					int count2 = content.length;
					rawContent = new StringBuffer[count2];

					for (int j = 0; j < count2; j++) {
						if (content[j] != null) {
							rawContent[j] = new StringBuffer(content[j].toString());

							if (((moveType[j] == 0) | ((moveType[j] == 2) && (j < count2 - 1) && (f_x1[j] < f_x2[j + 1]) && (f_x2[j] > f_x1[j + 1])))) {

								content[j].append("<link:");
								content[j].append((j + 1));
								content[j].append('>');
							}
						}
					}
				}

				/**
				 * build set of lines from text
				 */
				createLines(count, items, writingMode, true, false, true);

				/*Turn on flag to search across lines*/
				findAcrossLines = true;

				/*Get search coordinates*/
				co_ords = findValueCoords(writingMode, x1, x2, y1, y2, rawContent, textValue, isCaseSensitive, matchWholeWordsOnly, findAll);

				/*Turn off flag*/
				findAcrossLines = false;

				/*Allow for the handling of multiple writing modes*/
				if(co_ords!=null){
					for(int i=0; i!=co_ords.length; i++){
						vf.addElement(co_ords[i]);
					}
				}

			}
		}
		
		LogWriter.writeLog("Text scan completed");
		
		vf.trim();
		co_ords = vf.get();
		
		if(co_ords.length>0){
			return co_ords;  
		}else
			return null;

	}
	
	private float[] findValueCoords(int writingMode, int x1, int x2, int y1, int y2,
			StringBuffer rawContent[], String textValue, boolean isCaseSensitive,
			boolean matchWholeWordsOnly, boolean findAll
	){

		float[] co_ords = null, endPoints = null;
		Vector_Int rawCoords = new Vector_Int(20);
		Vector_Int endCoords = new Vector_Int(20);
		Vector_String teaserString = new Vector_String(20);
		int ptCount = 0;

		foundAcrossLine = false;
		remainderOfSearch = "";
		
		/**
		 * create local copies of arrays
		 */
		float[] f_x1 = this.f_x1, f_x2 = this.f_x2, f_y1 = this.f_y1, f_y2 = this.f_y2;
		
		/**
		 * swap around x and y so rountine works on all cases
		 */
		boolean valuesSwapped = false;
		if ((oldTextExtraction) | (writingMode == PdfData.HORIZONTAL_LEFT_TO_RIGHT)) {
			f_x1 = this.f_x1;
			f_x2 = this.f_x2;
			f_y1 = this.f_y1;
			f_y2 = this.f_y2;
		} else if (writingMode == PdfData.HORIZONTAL_RIGHT_TO_LEFT) {
			f_x2 = this.f_x1;
			f_x1 = this.f_x2;
			f_y1 = this.f_y1;
			f_y2 = this.f_y2;
		} else if (writingMode == PdfData.VERTICAL_BOTTOM_TO_TOP) {
			f_x1 = this.f_y1;
			f_x2 = this.f_y2;
			f_y1 = this.f_x2;
			f_y2 = this.f_x1;
			valuesSwapped = true;
		} else if (writingMode == PdfData.VERTICAL_TOP_TO_BOTTOM) {
			f_x1 = this.f_y2;
			f_x2 = this.f_y1;
			f_y2 = this.f_x1;
			f_y1 = this.f_x2;
			valuesSwapped = true;
		}
		
		// now we have lines, look for word
		int[] indices = getsortedUnusedFragments(false, true);
		int count = indices.length;
		float x = -1;
		int xReached;
		for (int j = count - 1; j > -1; j--) {// find from top of page
			int i = indices[j];
			if(this.writingMode[i]==writingMode){
				if (content[i] != null) {
					xReached = x1;
					while (xReached < x2) {

						ScanLinePair scanLinePair = scanLineForValue(rawContent, removeDuplicateSpaces(content[i]), textValue, xReached, isCaseSensitive,matchWholeWordsOnly, endX, f_y2[i]);

						x = scanLinePair.finalX;

						//Was value found on page
						boolean foundOnPage = false;
						
						/**
						 * Check to see if values need to be rotated to find on page
						 */
						if(valuesSwapped){
							if ((f_y1[i] > x1) && (f_y1[i] < x2) && (f_y2[i] > x1) && (f_y2[i] < x2) && (y1 > x) && (y2 < x))
								foundOnPage=true;
						}else{
							if ((x > x1) && (x < x2) && (y1 > f_y1[i]) && (y2 < f_y2[i]))
								foundOnPage=true;
						}
						
						// System.out.println(rawContent.toString());
						if (foundOnPage) {

							// exit if match found and set values
							if (x != -1 && (!matchWholeWordsOnly || matchWholeWordsOnly && scanLinePair.isWholeWord)) {

								// String text = Strip.stripXML(removeHiddenMarkers(content[i].toString())).toString();
								// if(text.indexOf("uvea")!=-1){
								// System.out.println(i+" "+f_y1[i]+" "+f_y2[i]+"
								// "+text);
								// <<>>/
								// }
								// add point
								if(foundAcrossLine){
									onlyCheckStart = true;
									partialFindCoords = new float[]{x, f_y1[i], endX, f_y2[i]};
									String firstValue = textValue.substring(0, textValue.length() - remainderOfSearch.length());
									if(tease!=null){
										int startBold = tease.toString().indexOf(firstValue);
										//Sometimes line may not have firstValue
										if(startBold!=-1){
											tease.insert(startBold, "<b>");
											partialFindTeaser = tease.toString();
										}
									}
								}else{
									
									
									if (valuesSwapped) {
										
										if (writingMode == PdfData.VERTICAL_BOTTOM_TO_TOP) {
											rawCoords.addElement((int) f_y2[i]);
											rawCoords.addElement((int) endX);
											rawCoords.addElement((int) 0);
											
											endCoords.addElement((int) f_y1[i]);
											endCoords.addElement((int) x);
											endCoords.addElement((int) 0);
										} else {
											rawCoords.addElement((int) f_y2[i]);
											rawCoords.addElement((int) x);
											rawCoords.addElement((int) 0);

											endCoords.addElement((int) f_y1[i]);
											endCoords.addElement((int) endX);
											endCoords.addElement((int) 0);
										}

									} else {
										rawCoords.addElement((int) x);
										rawCoords.addElement((int) f_y1[i]);
										rawCoords.addElement((int) 0);

										endCoords.addElement((int) endX);
										endCoords.addElement((int) f_y2[i]);
										endCoords.addElement((int) 0);
									}
									
									// save tease
									if (includeTease)
										teaserString.addElement(tease.toString());

									ptCount++;

									if (!findAll) {// exit on first find if findAll
										// ==false
										j = count;
										x2 = xReached;
									}
								}
								//System.out.println("content=="+content[i]);
								//System.out.println("coords=="+f_x1[i]+" , "+f_y1[i]+" , "+f_x2[i]+" , "+f_y2[i]);
								//System.out.println();
							}
						}

						if(onlyCheckStart){
							//If split across lines and next is a space this is handled by the new line, ignore
							if(remainderOfSearch.startsWith(" ")){
								remainderOfSearch = remainderOfSearch.substring(1);
							}
							
							if(j>0){

								int lastPtr = i;
								float lastX = f_x1[lastPtr]+((f_x2[lastPtr]-f_x1[lastPtr])/2);

								int temp = j-1;
								int ptr = indices[temp];

								float possNext = f_y2[lastPtr]-(f_y1[lastPtr]-f_y2[lastPtr]);

								boolean found = false;

								/*
								 * Lines can be divided into multiple content values
								 * therefore check from left to right for values underneath
								 */

								//Check start of line for a new line below
								while(!(f_x1[lastPtr]<f_x2[ptr] && f_x1[lastPtr]>=f_x1[ptr] &&
										(possNext)<f_y1[ptr] && (possNext)>f_y2[ptr]) && temp!=0 ){

									temp--;
									if(temp!=j){
										ptr = indices[temp];
									}
								}
								if((f_x1[lastPtr]<f_x2[ptr] && f_x1[lastPtr]>=f_x1[ptr] &&
										(possNext)<f_y1[ptr] && (possNext)>f_y2[ptr])){
									found=true;
								}


								if(!found){
									temp = j-1;
									ptr = indices[temp];
									//Check middle of line for a new line below
									while(!(lastX<f_x2[ptr] && lastX>f_x1[ptr] &&
											(possNext)<f_y1[ptr] && (possNext)>f_y2[ptr]) && temp!=0 ){

										temp--;
										if(temp!=j){
											ptr = indices[temp];
										}
									}
									if((lastX<f_x2[ptr] && lastX>f_x1[ptr] &&
											(possNext)<f_y1[ptr] && (possNext)>f_y2[ptr])){
										found=true;
									}

									if(!found){
										temp = j-1;
										ptr = indices[temp];
										//Check end of line for a new line below
										while(!(f_x2[lastPtr]<=f_x2[ptr] && f_x2[lastPtr]>f_x1[ptr] &&
												(possNext)<f_y1[ptr] && (possNext)>f_y2[ptr]) && temp!=0 ){

											temp--;
											if(temp!=j){
												ptr = indices[temp];
											}
										}
										if((f_x2[lastPtr]<=f_x2[ptr] && f_x2[lastPtr]>f_x1[ptr] &&
												(possNext)<f_y1[ptr] && (possNext)>f_y2[ptr])){
											found=true;
										}
									}
									
									if(!found){
										temp = j-1;
										ptr = indices[temp];
										//Check for lines that are beneath but not at start, end or middle
										while(!(f_x2[lastPtr]>f_x2[ptr] && f_x1[lastPtr]<f_x1[ptr] &&
												(possNext)<f_y1[ptr] && (possNext)>f_y2[ptr]) && temp!=0 ){
											temp--;
											if(temp!=j){
												ptr = indices[temp];
											}
										}
										if((f_x2[lastPtr]>f_x2[ptr] && f_x1[lastPtr]<f_x1[ptr] &&
												(possNext)<f_y1[ptr] && (possNext)>f_y2[ptr])){
											found=true;
										}
									}
								}

								//if content is not null and we have found a line below
								if(found && content[ptr]!=null){

									scanLinePair = scanLineForValue(rawContent, removeDuplicateSpaces(content[ptr]), remainderOfSearch, xReached, isCaseSensitive,matchWholeWordsOnly, endX, f_y2[ptr]);

									x = scanLinePair.finalX;

									// System.out.println(rawContent.toString());
									if ((x > x1) && (x < x2) && (y1 > f_y1[ptr]) && (y2 < f_y2[ptr])) {

										// exit if match found and set values
										if (x != -1 && (!matchWholeWordsOnly || matchWholeWordsOnly && scanLinePair.isWholeWord)) {

											// String text = Strip.stripXML(removeHiddenMarkers(content[ptr].toString())).toString();
											// if(text.indexOf("uvea")!=-1){
											// System.out.println(i+" "+f_y1[i]+" "+f_y2[i]+"
											// "+text);
											// <<>>/
											// }
											// add point

											if (valuesSwapped) {

												if (writingMode == PdfData.VERTICAL_BOTTOM_TO_TOP) {
													rawCoords.addElement((int) partialFindCoords[3]);
													rawCoords.addElement((int) partialFindCoords[2]);
													rawCoords.addElement((int) linkedSearchAreas);

													endCoords.addElement((int) partialFindCoords[1]);
													endCoords.addElement((int) partialFindCoords[0]);
													endCoords.addElement((int) linkedSearchAreas);

													rawCoords.addElement((int) f_y2[ptr]);
													rawCoords.addElement((int) endX);
													rawCoords.addElement((int) 0);

													endCoords.addElement((int) f_y1[ptr]);
													endCoords.addElement((int) x);
													endCoords.addElement((int) 0);
												} else {
													rawCoords.addElement((int) partialFindCoords[3]);
													rawCoords.addElement((int) partialFindCoords[0]);
													rawCoords.addElement((int) linkedSearchAreas);

													endCoords.addElement((int) partialFindCoords[1]);
													endCoords.addElement((int) partialFindCoords[2]);
													endCoords.addElement((int) linkedSearchAreas);

													rawCoords.addElement((int) f_y2[ptr]);
													rawCoords.addElement((int) x);
													rawCoords.addElement((int) 0);

													endCoords.addElement((int) f_y1[ptr]);
													endCoords.addElement((int) endX);
													endCoords.addElement((int) 0);
												}

											} else {
												rawCoords.addElement((int) partialFindCoords[0]);
												rawCoords.addElement((int) partialFindCoords[1]);
												rawCoords.addElement((int) linkedSearchAreas);

												endCoords.addElement((int) partialFindCoords[2]);
												endCoords.addElement((int) partialFindCoords[3]);
												endCoords.addElement((int) linkedSearchAreas);

												rawCoords.addElement((int) x);
												rawCoords.addElement((int) f_y1[ptr]);
												rawCoords.addElement((int) 0);

												endCoords.addElement((int) endX);
												endCoords.addElement((int) f_y2[ptr]);
												endCoords.addElement((int) 0);
											}
											// save tease
											if (includeTease){
												//Combine teasers for cross line results
												teaserString.addElement(partialFindTeaser + tease);
											}
											ptCount++;
											ptCount++;

											if (!findAll) {// exit on first find if !findAll
												// ==false
												j = count;
												x2 = xReached;
											}
										}
									}
									onlyCheckStart=false;
								}
							}
						}
						if (i == count || x == -1)
							break;

						xReached = (int) x + 1;
					}
				}
			}
		}

		rawContent = null;

		// put values in array
		if (ptCount > 0) {
			
			rawCoords.trim();
			endCoords.trim();
			
			co_ords = new float[rawCoords.size()-1];
			for (int i = 0; i < co_ords.length; i++) {
				co_ords[i] = rawCoords.elementAt(i);
			}

			endPoints = new float[endCoords.size()-1];
			for (int i = 0; i < endPoints.length; i++) {
				endPoints[i] = endCoords.elementAt(i);
			}

			if (this.includeTease) {
				teasers = new String[ptCount];

				String teaseStr;
				for (int i = 0; i < ptCount; i++) {

					teaseStr=teaserString.elementAt(i);

					//add in HTML to highlight if set
					if(!includeHTMLtags){  //do nothing
					}else if(!isCaseSensitive){

						String testText=textValue.toLowerCase();
						int valueLength=testText.length();
						String testTeaser=teaseStr.toLowerCase();
						StringBuffer finalTease=new StringBuffer(teaseStr);
						int start=testTeaser.indexOf(testText);
						while(start!=-1){

							//add second 1 first as it will alter positions
							finalTease.insert(start+valueLength,"</b>");
							finalTease.insert(start,"<b>");

							start=start+7+valueLength;

							//roll on
							start=finalTease.indexOf(testText, start);

						}

						teaseStr=finalTease.toString();

					}else
						teaseStr=teaseStr.replaceAll(textValue,"<b>"+textValue+"</b>");

					if (usingMultipleTerms)
						multipleTermTeasers.add(teaseStr);
					else
						teasers[i] = teaseStr;
				}
			}
		}
		
		if(co_ords!=null && endPoints!=null){
			Vector_Float vf = new Vector_Float();
			for(int i=0; i!=co_ords.length; i=i+3){
				vf.addElement(co_ords[i]);
				vf.addElement(co_ords[i+1]);
				vf.addElement(endPoints[i]);
				vf.addElement(endPoints[i+1]);
				vf.addElement(endPoints[i+2]);
			}

			vf.trim();
			co_ords = vf.get();
		}
		
		
		this.endPoints = endPoints;
		return co_ords;
	}

	private static String removeDuplicateSpaces(String textValue) {
		
		if(textValue.indexOf("  ")!=-1){
			
			StringBuffer cleanedText=removeDuplicateSpaces(new StringBuffer(textValue));
			textValue=cleanedText.toString();
			
		}
		return textValue;
	}
	
	private static StringBuffer removeDuplicateSpaces(StringBuffer cleanedText) {
		
		if(cleanedText.indexOf("  ")!=-1){
			
			int count=cleanedText.length()-1;
				
			for(int i=0;i<count;i++){
				if((cleanedText.charAt(i)==' ')&&(cleanedText.charAt(i+1)==' ')){
					cleanedText.deleteCharAt(i+1);
					count--;
					i--;
				}				
			}			
		}
		
		return cleanedText;
	}
	
	/**return endpoints from last findtext*/
	public float[] getEndPoints() {
		return endPoints;
	}

	/**return text teasers from findtext if generateTeasers() called  
	 * before find
	 */
	public String[] getTeasers() {
		
		return teasers;
	}
	
	/**
	 * tell find text to generate teasers as well
	 */
	public void generateTeasers() {
		
		includeTease=true;
	}

	public void removeInvalidXMLValues(boolean removeInvalidXMLValues) {
		this.removeInvalidXMLValues = removeInvalidXMLValues;
	}

	public void setPunctuation(String punctuation) {
		this.punctuation = punctuation;
	}
	
	public void addToPunctuation(String punctuation) {
		this.punctuation = this.punctuation+punctuation;
	}

	/**
	 * Get the value of the word detection technique
	 * @return the int value of the word detection technique
	 */
	public int getWordDetectionTechnique() {
		return wordDetectionTechnique;
	}

	/**
	 * Set the word detection technique based on a set of final variables</br>
	 *  public final int USER_DEFINED_LIST_ONLY = 0;</br>
     *  public final int SURROUND_BY_ANY_PUNCTUATION = 1;</br>
	 * @param wordDetectionTechnique the int value for the word detection technique
	 */
	public void setWordDetectionTechnique(int wordDetectionTechnique) {
		this.wordDetectionTechnique = wordDetectionTechnique;
	}
}
