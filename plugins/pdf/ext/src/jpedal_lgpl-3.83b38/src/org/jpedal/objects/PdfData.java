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
* PdfData.java
* ---------------
*/
package org.jpedal.objects;

import java.util.*;
import java.io.*;

import org.jpedal.PdfDecoder;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.utils.Fonts;
import org.jpedal.utils.LineBreaker;
import org.jpedal.utils.repositories.Vector_Int;


/**
 * <p>
 * holds text data for extraction & manipulation
 * </p>
 * <p>
 * Pdf routines create 'raw' text data
 * </p>
 * <p>
 * grouping routines will attempt to intelligently stitch together and leave as
 * 'processed data' in this class
 * </p>
 * <p>
 * <b>NOTE ONLY methods (NOT public variables) are part of API </b>
 * </p>
 * We then transfer the data into our generic Storypad object store (which also
 * serves as an examaple of how the data can be used).
 *  
 */
public class PdfData extends StoryData
{

	private static final long serialVersionUID = 8229354993149694377L;

	Vector_Int itemsOnEachPage=new Vector_Int(5);
	Vector_Int displacementForEachPage=new Vector_Int(5);

	/**identify type as text*/
	public static final int TEXT = 0;

	/**identify type as image*/
	public static final int IMAGE = 1;

	/**test orientation*/
	public static final int HORIZONTAL_LEFT_TO_RIGHT = 0;

	public static final int HORIZONTAL_RIGHT_TO_LEFT = 1;

	public static final int VERTICAL_TOP_TO_BOTTOM = 2;

	public static final int VERTICAL_BOTTOM_TO_TOP = 3;


	/**
	 * list of elements - each element is a text fragment from the pdf
	 */
	private java.util.List  text_objects;

	/**holds fields extractred by regexp*/
	Map extractedTextFields=new HashMap(),extractedXMLFields=new HashMap();

	private int pointer=0;

	private int additionalPageCount=0;

	/**original size*/
	private int originalSize=-1;

	/**holds items we used to create the story*/
	public String[] fragments=new String[max];

	public int[] parent=new int[max];

	public int[] childrenParent =new int[max];

	/**flag to show x co-ord has been embedded in content*/
	private boolean widthIsEmbedded=false;

	/**local store for max and widthheight of page*/
	public float maxY=0,maxX=0;

	public int[] fontSize;

    public int[] rotation=new int[max];

    /**vlaues same for all records in file*/
	private Map globalValues=new HashMap();
	private Map globalSettings=new HashMap();


    /** create empty object to hold content*/
	public PdfData()
	{
		text_objects = new Vector();

		//set all parent values
		for(int i=0;i<max;i++)
			parent[i]=-1;

		//set all parent values
		for(int i=0;i<max;i++)
			childrenParent[i]=-1;
	}

	/**
	 * get data as a Map
	 */
	final public Map getTextElementAt( int i )
	{
		return (Map)text_objects.get( i );
	}


	/**
	 * get number of raw objects on page
	 */
	final public int getRawTextElementCount()
	{

		return pointer;
	}

	/**
	 * reset values
	 */
	final public void resetTextList( java.util.List new_text_objects )
	{
		text_objects = new_text_objects;


	}

	//////////////////////////////////////////////////////////////////
	/**
	 * clear store of objects once written out
	 * to reclaim memory. If flag set, sets data to
	 * state after page decoded before grouping for reparse
	 */
	final public void flushTextList( boolean reinit )
	{
		text_objects=new Vector();


		if( reinit == false ){

			pointer=0;

			max=2000;

			contents=new String[max];
			f_writingMode=new int[max];
			f_font_used=new String[max];
			font_data=new String[max];
			text_length=new int[max];
			lineCount=new int[max];
			move_command=new int[max];
			f_character_spacing=new float[max];
			token_counter=new int[max];
			rotation=new int[max];
			f_end_font_size=new int[max];
			space_width=new float[max];
			f_x1=new float[max];
			f_x2=new float[max];
			f_y1=new float[max];
			f_y2=new float[max];

			colorTag=new String[max];



		}

		itemsOnEachPage.clear();
		displacementForEachPage.clear();

		additionalPageCount=0;
	}

	/////////////////////////////////////////////////////////////////
	/**
	 * get number of objects at end
	 */
	final public int getTextElementCount()
	{
		return text_objects.size();
	}

	/**
	 * store line of raw text for later processing
	 */
	final public void addImageElement( float x1, float y1, float width, float height,
			String imageName){

		f_x1[pointer]=x1;
		objectType[pointer]=IMAGE;
		f_x2[pointer]=x1+width;
		f_y1[pointer]=y1+height;
		f_y2[pointer]=y1;
		fragments[pointer]= String.valueOf(pointer);
		contents[pointer]=imageName;
		unformattedContent[pointer]=imageName;

		pointer++;

		//resize pointers
		if(pointer==max){
			resizeArrays(0);
		}

	}

	/**
	 * break line in half
	 */
	final public void breakLineInHalf(int original, float x1, float y1, float x2, float y2,boolean debugFlag){

		if(debugFlag)
			System.out.println("id="+original+" x1,y1="+x1+ ' ' +y1+" x2,y2="+x2+ ' ' +y2+ '\n' +contents[original]);

		/**clone values*/
		f_writingMode[pointer]=f_writingMode[original];
		font_data[pointer]=font_data[original];
		move_command[pointer]=move_command[original];
		f_character_spacing[pointer]=f_character_spacing[original];
		colorTag[pointer]=colorTag[original];

		objectType[pointer]=TEXT;

		token_counter[pointer]=token_counter[original];
		rotation[pointer]=rotation[original];

		f_end_font_size[pointer]=f_end_font_size[original];

		f_font_used[pointer]=f_font_used[original];

		space_width[pointer]=space_width[original];


		LineBreaker breaker=new LineBreaker();

		breaker.breakLine(contents[original], x1, y1, x2, y2, debugFlag);

		//reset co-ordinates

		f_x1[pointer]=breaker.endX;
		f_x2[pointer]=f_x2[original]; //DO THIS BEFORE WE RESET BELOW!

		f_x1[original]=f_x1[original];
		f_x2[original]=breaker.startX;


		f_y1[pointer]=f_y1[original];
		f_y2[pointer]=f_y2[original];

		//alter text  (adding XML tags)
		text_length[pointer]=breaker.charsCounted;
		text_length[original]=text_length[original]-breaker.charsCounted;

		StringBuffer truncated=new StringBuffer();
		truncated.append(contents[original].substring(0,breaker.brk).trim());

		StringBuffer removedString=new StringBuffer();
		removedString.append(contents[original].substring(breaker.brk,contents[original].length()).trim());

		//add tokens
		if(PdfDecoder.isXMLExtraction()){
			removedString.insert( 0, font_data[pointer] );
			truncated.append( Fonts.fe );
		}

		//add color token
		if(isColorExtracted()){
			removedString.insert( 0, colorTag[pointer] );
			truncated.append( GenericColorSpace.ce );
		}


		contents[original]=truncated.toString();
		contents[pointer]=removedString.toString();

		lineCount[pointer]=lineCount[original];

		/**/
		//System.out.println("start,end="+startX+" "+endX);
		//System.out.println("P="+p+" "+contents[original].length()+" j="+original);
		if(debugFlag){
			System.out.println("orig="+org.jpedal.grouping.PdfGroupingAlgorithms.removeHiddenMarkers(truncated.toString())+ '<');
			System.out.println("original at "+f_x1[original]+ ' ' +f_y1[original]+ ' ' +f_x2[original]+ ' ' +f_y2[original]);

			System.out.println("removed="+org.jpedal.grouping.PdfGroupingAlgorithms.removeHiddenMarkers(removedString.toString())+ '<');
			System.out.println("removed at "+f_x1[pointer]+ ' ' +f_y1[pointer]+ ' ' +f_x2[pointer]+ ' ' +f_y2[pointer]);

			//if(org.jpedal.grouping.PdfGroupingAlgorithms.removeHiddenMarkers(truncated.toString()).length()==7){
			//	System.out.println("exting");
			
			//}
		}


		//
		//System.out.println("second at "+f_x1[pointer]+" "+f_y1[pointer]+" "+f_x2[pointer]+" "+f_y2[pointer]);

		pointer++;

		//resize pointers
		if(pointer==max)
			resizeArrays(0);

	}

	/**
	 * store line of raw text for later processing
	 */
	final public void addRawTextElement( float character_spacing,int writingMode,
			String font_as_string,  float current_space, TextState current_text_state,
			float x1, float y1, float x2, float y2, int move_type,
			StringBuffer processed_line, int token_number,
            int current_text_length, String currentColorTag, int rotation )
	{

		//if( ( x1 >= 0 ) && ( y1 >= 0 ) && ( x2 > 0 ) && ( y2 > 0 ) && (processed_line.length()>0)  )
		if(processed_line.length()>0){

			//add fonts to processed data - duplicates stripped out at end
			//if(PdfDecoder.isXMLExtraction())
			//processed_line = translateCharacters( processed_line );

			//add tokens
			if(PdfDecoder.isXMLExtraction()){
				processed_line.insert( 0, font_as_string );
				processed_line.append( Fonts.fe );
			}

			//add color token
			if(isColorExtracted()){
				processed_line.insert( 0, currentColorTag );
				processed_line.append( GenericColorSpace.ce );
			}

			f_writingMode[pointer]=writingMode;
			font_data[pointer]=font_as_string;
			text_length[pointer]=current_text_length;
			move_command[pointer]=move_type;
			f_character_spacing[pointer]=character_spacing;
			f_x1[pointer]=x1;
			colorTag[pointer]=currentColorTag;
			f_x2[pointer]=x2;
			f_y1[pointer]=y1;
			f_y2[pointer]=y2;
			objectType[pointer]=TEXT;
			lineCount[pointer]=1;
			//raw_data[pointer]=commands_processed.toString();
			contents[pointer]=processed_line.toString();
			token_counter[pointer]=token_number;

			int font_size = current_text_state.getCurrentFontSize();
			f_end_font_size[pointer]=font_size;

			f_font_used[pointer]=current_text_state.getFontName();

			space_width[pointer]=current_space*1000;

            this.rotation[pointer]=rotation;

            pointer++;

			//resize pointers
			if(pointer==max)
				resizeArrays(0);
		}
	}

	/**
	 * store line of raw text for later processing
	 */
	final public void addRawBrokenFragment(int j, StringBuffer text, float x1,float x2,
			float y1,float y2,String plotString ,String currentColorTag){

		//make sure valid positive co-ords
		if( ( x1 > 0 ) & ( y1 > 0 ) & ( x2 > 0 ) & ( y2 > 0 ) )
		{

			//add fonts to processed data - duplicates stripped out at end
			//processed_line = translateCharacters( processed_line );

			//add token to close
			if(PdfDecoder.isXMLExtraction()){
				if((!text.toString().endsWith(Fonts.fe))&&
						(!text.toString().endsWith(GenericColorSpace.ce)))
					text.append(Fonts.fe );
			}

			f_writingMode[pointer]=f_writingMode[j];
			font_data[pointer]=font_data[j];
			text_length[pointer]=text_length[j];
			lineCount[pointer]=lineCount[j];
			move_command[pointer]=move_command[j];
			f_character_spacing[pointer]=f_character_spacing[j];
			f_x1[pointer]=x1;
			f_x2[pointer]=x2;
			f_y1[pointer]=y1;
			f_y2[pointer]=y2;
			colorTag[pointer]=currentColorTag;

			//System.out.println("broken before="+contents[pointer]);
			contents[pointer]=text.toString();
			token_counter[pointer]=token_counter[j];
            rotation[pointer]=rotation[j];

            f_end_font_size[pointer]=f_end_font_size[j];
			f_font_used[pointer]=f_font_used[j];
			space_width[pointer]=space_width[j];

			pointer++;

			//resize pointers
			if(pointer==max)
				resizeArrays(0);
		}
	}

	/**
	 * used by Storypad
	 *
    public PdfData restorePageContent(int pageNumber,int start,int length,int xOffset) {


        //object to extract data into
        PdfData newData=new PdfData();

        newData.resizeArrays(-length);

        int max=length;

        //this.pointer=length;

        System.arraycopy( contents, start, newData.contents, 0, length );

        System.arraycopy( rawData, start, newData.rawData, 0, length );

        System.arraycopy( unformattedContent, start, newData.unformattedContent, 0, length );

        System.arraycopy( f_writingMode, start, newData.f_writingMode, 0, length );

        System.arraycopy( f_font_used, start, newData.f_font_used, 0, length );

        System.arraycopy( colorTag, start, newData.colorTag, 0, length );

        System.arraycopy( font_data, start, newData.font_data, 0, length );

        System.arraycopy( fragments, start, newData.fragments, 0, length );

        for(int jj=0;jj<length;jj++){

            if(newData.fragments[jj]!=null){
                StringTokenizer tokens=new StringTokenizer(newData.fragments[jj]);

                StringBuffer newValue=new StringBuffer();
                int count=tokens.countTokens();
                for(int ii=0;ii<count;ii++){
                    int id=Integer.parseInt(tokens.nextToken())-start;
                    newValue.append(id);
                    newValue.append(' ');
                }

                newData.fragments[jj]=newValue.toString();
            }
        }

        System.arraycopy( text_length, start, newData.text_length, 0, length );

        System.arraycopy( lineCount, start, newData.lineCount, 0, length );

        System.arraycopy( move_command, start, newData.move_command, 0, length );

        System.arraycopy( f_character_spacing, start, newData.f_character_spacing, 0, length );

        System.arraycopy( parent, start, newData.parent, 0, length );

        for(int jj=length;jj<max;jj++){
            if(newData.parent[jj]!=-1)
                newData.parent[jj]=newData.parent[jj]-start;
        }

        System.arraycopy( prefix, start, newData.prefix, 0, length );

        System.arraycopy( token_counter, start, newData.token_counter, 0, length );

        System.arraycopy( links, start, newData.links, 0, length );

        for(int jj=length;jj<max;jj++){

            if(newData.links[jj]!=null){
                int[] current=(int[])newData.links[jj];
                int count=current.length;

                for(int jj2=0;jj2<count;jj2++){

                    if(current[jj2]!=-1)
                        current[jj2]=current[jj2]-start;
                }

                newData.links[jj]=current;

            }
        }

        System.arraycopy( category, start, newData.category, 0, length );

        System.arraycopy( objectType, start, newData.objectType, 0, length );

        System.arraycopy( f_end_font_size, start, newData.f_end_font_size, 0, length );

        System.arraycopy( space_width, start, newData.space_width, 0, length );

        System.arraycopy( f_x1, start, newData.f_x1, 0, length );

        System.arraycopy( f_x2, start, newData.f_x2, 0, length );

        System.arraycopy( f_y1, start, newData.f_y1, 0, length );

        System.arraycopy( f_y2, start, newData.f_y2, 0, length );

        for(int j=0;j<length;j++){
            if(newData.f_x1[j]>=xOffset){
                newData.f_x1[j]= newData.f_x1[j]-xOffset;
                newData.f_x2[j]= newData.f_x2[j]-xOffset;
            }
        }

        return newData;
    }/**/

	/**
	 * used by Storypad
	 */
	public PdfData cloneData(){

		int length=contents.length;

		final int start=0;

		//object to extract data into
		PdfData newData=new PdfData();

		newData.resizeArrays(-length);

		//copy arrays

		System.arraycopy( contents, start, newData.contents, 0, length );

		System.arraycopy( rawData, start, newData.rawData, 0, length );

		System.arraycopy( unformattedContent, start, newData.unformattedContent, 0, length );

		System.arraycopy( f_writingMode, start, newData.f_writingMode, 0, length );

		System.arraycopy( f_font_used, start, newData.f_font_used, 0, length );

		System.arraycopy( colorTag, start, newData.colorTag, 0, length );

		System.arraycopy( font_data, start, newData.font_data, 0, length );

		System.arraycopy( fragments, start, newData.fragments, 0, length );

        System.arraycopy( rotation, start, newData.rotation, 0, length );

        System.arraycopy( text_length, start, newData.text_length, 0, length );

		System.arraycopy( lineCount, start, newData.lineCount, 0, length );

		System.arraycopy( move_command, start, newData.move_command, 0, length );

		System.arraycopy( f_character_spacing, start, newData.f_character_spacing, 0, length );

		System.arraycopy( parent, start, newData.parent, 0, length );

		System.arraycopy(childrenParent, start, newData.childrenParent, 0, length );

		System.arraycopy( prefix, start, newData.prefix, 0, length );

		System.arraycopy( token_counter, start, newData.token_counter, 0, length );

        System.arraycopy( rotation, start, newData.rotation, 0, length );

        System.arraycopy( links, start, newData.links, 0, length );

		System.arraycopy( children, start, newData.children, 0, length );

		System.arraycopy( category, start, newData.category, 0, length );

		System.arraycopy( objectType, start, newData.objectType, 0, length );

		System.arraycopy( f_end_font_size, start, newData.f_end_font_size, 0, length );

		System.arraycopy( space_width, start, newData.space_width, 0, length );

		System.arraycopy( f_x1, start, newData.f_x1, 0, length );

		System.arraycopy( f_x2, start, newData.f_x2, 0, length );

		System.arraycopy( f_y1, start, newData.f_y1, 0, length );

		System.arraycopy( f_y2, start, newData.f_y2, 0, length );

		//align all other variables

		//java.util.List  text_objects;

		//Map extractedTextFields=new HashMap(),extractedXMLFields=new HashMap();

		newData.pointer=pointer;


		newData.additionalPageCount=additionalPageCount;

		newData.originalSize=originalSize;

		newData.widthIsEmbedded=widthIsEmbedded;

		newData.maxY=maxY;

		newData.maxX=maxX;

		newData.max = max;

		//private Map globalValues=new HashMap();
		//private Map globalSettings=new HashMap();

		return newData;

	}


	/**
	 * used by Storypad
	 */
	public void merge(PdfData pdf_data, int xDisplacement, int rawCount,boolean trackOffset) {

		final boolean debug=false;

		if(debug){
			System.out.println("----------------Merge-------------------------");
			System.out.println("Original data="+this.f_x1.length+ ' ' +pointer);
			System.out.println("new data="+pdf_data.f_x1.length+ ' ' +pdf_data.pointer);
			System.out.println("dx="+xDisplacement);
		}

		additionalPageCount++;

		if(originalSize==-1)
			originalSize=f_x1.length;

		int existItems= this.pointer;

		if(debug)
			System.out.println("exist="+existItems+" rawCount="+rawCount+" orig="+originalSize+" f_x1.length="+f_x1.length);

		if(existItems>f_x1.length)
			existItems=f_x1.length;
		int newItems= pdf_data.pointer;

		if(newItems>pdf_data.f_x1.length)
			newItems=pdf_data.f_x1.length;

		int max=existItems+newItems+1;

		if(debug)
			System.out.println("combined size="+max);

		this.pointer=max;

		//first add xDisplacement onto X2
		for(int i=0;i<newItems;i++){
			pdf_data.f_x1[i]=xDisplacement+pdf_data.f_x1[i];
			pdf_data.f_x2[i]=xDisplacement+pdf_data.f_x2[i];
		}

		float[] temp_f;
		int[] temp_i;
		boolean[] temp_b;
		String[] temp_s;

		//save lookup for displacement
		if(trackOffset){
			itemsOnEachPage.addElement(existItems);
			displacementForEachPage.addElement(xDisplacement);

			//System.out.println(existItems+" added to "+this);
		}

		temp_s=contents;
		contents = new String[max];
		System.arraycopy( temp_s, 0, contents, 0, existItems );
		System.arraycopy( pdf_data.contents, 0, contents, existItems, newItems);

		temp_s=rawData;
		rawData = new String[max];
		System.arraycopy( temp_s, 0, rawData, 0, existItems );
		System.arraycopy( pdf_data.rawData, 0, rawData, existItems, newItems );

		for(int jj=existItems;jj<existItems+newItems;jj++){

			if(rawData[jj]!=null){

				//loop to get raw fragments
				StringTokenizer values=new StringTokenizer(rawData[jj],PdfData.hiddenMarker);

				StringBuffer newVersion=new StringBuffer();

				while(values.hasMoreTokens()){

					//get data
					String metaData=values.nextToken();
					String text=values.nextToken();

					//get metadata embedded within raw text (assumes starts at zero)
					StringTokenizer metaValues=new StringTokenizer(metaData," ");
					float x1=Float.parseFloat(metaValues.nextToken())+xDisplacement;
					float y1=Float.parseFloat(metaValues.nextToken());
					float x2=Float.parseFloat(metaValues.nextToken())+xDisplacement;
					float y2=Float.parseFloat(metaValues.nextToken());
					int originalID=Integer.parseInt(metaValues.nextToken())+rawCount;

					newVersion.append(PdfData.hiddenMarker).append(x1).append(' ').append(y1).append(' ').append(x2).append(' ').append(y2).append(' ').append(originalID).append(PdfData.hiddenMarker).append(text);
				}

				rawData[jj]=newVersion.toString();

			}

		}

		temp_s=unformattedContent;
		unformattedContent = new String[max];
		System.arraycopy( temp_s, 0, unformattedContent, 0, existItems );
		System.arraycopy( pdf_data.unformattedContent, 0, unformattedContent, existItems, newItems );

		temp_i=f_writingMode;
		f_writingMode=new int[max];
		f_writingMode = new int[max];
		System.arraycopy( temp_i, 0, f_writingMode, 0, existItems );
		System.arraycopy( pdf_data.f_writingMode, 0, f_writingMode, existItems, newItems );

		temp_s=f_font_used;
		f_font_used = new String[max];
		System.arraycopy( temp_s, 0, f_font_used, 0, existItems );
		System.arraycopy( pdf_data.f_font_used, 0, f_font_used, existItems, newItems);

		temp_s=colorTag;
		colorTag = new String[max];
		System.arraycopy( temp_s, 0, colorTag, 0, existItems );
		System.arraycopy( pdf_data.colorTag, 0, colorTag, existItems, newItems );

		temp_s=font_data;
		font_data = new String[max];
		System.arraycopy( temp_s, 0, font_data, 0, existItems );
		System.arraycopy( pdf_data.font_data, 0, font_data, existItems, newItems );

		temp_s=fragments;
		fragments = new String[max];
		System.arraycopy( temp_s, 0, fragments, 0, existItems );
		System.arraycopy( pdf_data.fragments, 0, fragments, existItems, newItems );

        temp_i=rotation;
        rotation = new int[max];
        System.arraycopy( temp_i, 0, rotation, 0, existItems );
        System.arraycopy( pdf_data.rotation, 0, rotation, existItems, newItems );


        for(int jj=existItems;jj<existItems+newItems;jj++){

			if(fragments[jj]!=null){
				StringTokenizer tokens=new StringTokenizer(fragments[jj]);

				StringBuffer newValue=new StringBuffer();
				int count=tokens.countTokens();
				for(int ii=0;ii<count;ii++){

					int id=Integer.parseInt(tokens.nextToken())+rawCount;
					newValue.append(id);
					newValue.append(' ');
				}

				fragments[jj]=newValue.toString();
			}
		}

		temp_i=text_length;
		text_length = new int[max];
		System.arraycopy( temp_i, 0, text_length, 0, existItems );
		System.arraycopy( pdf_data.text_length, 0, text_length, existItems, newItems);

		temp_i=lineCount;
		lineCount = new int[max];
		System.arraycopy( temp_i, 0, lineCount, 0, existItems );
		System.arraycopy( pdf_data.lineCount, 0, lineCount, existItems, newItems);

		temp_i=move_command;
		move_command = new int[max];
		System.arraycopy( temp_i, 0, move_command, 0, existItems );
		System.arraycopy( pdf_data.move_command, 0, move_command, existItems, newItems);

		temp_f=f_character_spacing;
		f_character_spacing = new float[max];
		System.arraycopy( temp_f, 0, f_character_spacing, 0, existItems );
		System.arraycopy( pdf_data.f_character_spacing, 0, f_character_spacing, existItems, newItems);

		temp_i=parent;
		parent = new int[max];
		//set to default -1
		for(int ii=existItems;ii<max;ii++)
			parent[ii]=-1;
		System.arraycopy( temp_i, 0, parent, 0, existItems );
		System.arraycopy( pdf_data.parent, 0, parent, existItems, newItems);

		for(int jj=existItems;jj<max;jj++){

			if(parent[jj]!=-1)
				parent[jj]=parent[jj]+existItems;
		}

		temp_i= childrenParent;
		childrenParent = new int[max];
		//set to default -1
		for(int ii=existItems;ii<max;ii++)
			childrenParent[ii]=-1;
		System.arraycopy( temp_i, 0, childrenParent, 0, existItems );
		System.arraycopy( pdf_data.childrenParent, 0, childrenParent, existItems, newItems);

		for(int jj=existItems;jj<max;jj++){

			if(childrenParent[jj]!=-1)
				childrenParent[jj]= childrenParent[jj]+existItems;
		}

		temp_s=prefix;
		prefix = new String[max];
		//set to default -1
		for(int ii=existItems;ii<max;ii++)
			prefix[ii]=null;
		System.arraycopy( temp_s, 0, prefix, 0, existItems );
		System.arraycopy( pdf_data.prefix, 0, prefix, existItems, newItems);

        temp_i=rotation;
        rotation = new int[max];
        System.arraycopy( temp_i, 0, rotation, 0, existItems );
        System.arraycopy( pdf_data.rotation, 0, rotation, existItems, newItems);

        temp_i=token_counter;
		token_counter = new int[max];
		System.arraycopy( temp_i, 0, token_counter, 0, existItems );
		System.arraycopy( pdf_data.token_counter, 0, token_counter, existItems, newItems);

		Object[] temp_o=links;
		links = new Object[max];
		System.arraycopy( temp_o, 0, links, 0, existItems );
		System.arraycopy( pdf_data.links, 0, links, existItems, newItems);

		temp_o=children;
		children = new Object[max];
		System.arraycopy( temp_o, 0, children, 0, existItems );
		System.arraycopy( pdf_data.children, 0, children, existItems, newItems);


		for(int jj=existItems;jj<max;jj++){
			if(links[jj]!=null){
				int[] current=(int[])links[jj];

				int count=current.length;

				for(int jj2=0;jj2<count;jj2++){

					if(current[jj2]!=-1)
						current[jj2]=current[jj2]+existItems;
				}

				links[jj]=current;
			}
		}

		for(int jj=existItems;jj<max;jj++){
			if(children[jj]!=null){
				int[] current=(int[])children[jj];

				int count=current.length;

				for(int jj2=0;jj2<count;jj2++){

					if(current[jj2]!=-1)
						current[jj2]=current[jj2]+existItems;
				}

				children[jj]=current;
			}
		}

		temp_s=category;
		category = new String[max];
		System.arraycopy( temp_s, 0, category, 0, existItems );
		System.arraycopy( pdf_data.category, 0, category, existItems, newItems);

		temp_i=objectType;
		objectType = new int[max];
		System.arraycopy( temp_i, 0, objectType, 0, existItems );
		System.arraycopy( pdf_data.objectType, 0, objectType, existItems, newItems);


		temp_i=f_end_font_size;
		f_end_font_size = new int[max];
		System.arraycopy( temp_i, 0, f_end_font_size, 0, existItems );
		System.arraycopy( pdf_data.f_end_font_size, 0, f_end_font_size, existItems, newItems);


		temp_f=space_width;
		space_width = new float[max];
		System.arraycopy( temp_f, 0, space_width, 0, existItems );
		System.arraycopy( pdf_data.space_width, 0, space_width, existItems, newItems);

		/**
		 * resize arrays holding location of data
		 */
		 temp_f=f_x1;
		f_x1 = new float[max];
		System.arraycopy( temp_f, 0, f_x1, 0, existItems );
		System.arraycopy( pdf_data.f_x1, 0, f_x1, existItems, newItems);

		temp_f=f_x2;
		f_x2 = new float[max];
		System.arraycopy( temp_f, 0, f_x2, 0, existItems );
		System.arraycopy( pdf_data.f_x2, 0, f_x2, existItems, newItems);

		temp_f=f_y1;
		f_y1 = new float[max];
		System.arraycopy( temp_f, 0, f_y1, 0, existItems );
		System.arraycopy( pdf_data.f_y1, 0, f_y1, existItems, newItems);


		temp_f=f_y2;
		f_y2 = new float[max];
		System.arraycopy( temp_f, 0, f_y2, 0, existItems );
		System.arraycopy( pdf_data.f_y2, 0, f_y2, existItems, newItems);

		/**
		 * resize arrays holding location of data AND any linked objects
		 */
		if(lx1!=null){
			temp_f=lx1;
			lx1 = new float[max];
			System.arraycopy( temp_f, 0, lx1, 0, existItems );
			System.arraycopy( pdf_data.lx1, 0, lx1, existItems, newItems);

			temp_f=lx2;
			lx2 = new float[max];
			System.arraycopy( temp_f, 0, lx2, 0, existItems );
			System.arraycopy( pdf_data.lx2, 0, lx2, existItems, newItems);

			temp_f=ly1;
			ly1 = new float[max];
			System.arraycopy( temp_f, 0, ly1, 0, existItems );
			System.arraycopy( pdf_data.ly1, 0, ly1, existItems, newItems);


			temp_f=ly2;
			ly2 = new float[max];
			System.arraycopy( temp_f, 0, ly2, 0, existItems );
			System.arraycopy( pdf_data.ly2, 0, ly2, existItems, newItems);
			/***/
		}

	}


	/**
	 * resize arrays to add newItems to end (-1 makes it grow)
	 */
	public void resizeArrays(int newItems) {

		float[] temp_f;
		int[] temp_i;
		boolean[] temp_b;
		String[] temp_s;

		if(newItems<0){
			max=-newItems;
			pointer=max;
		}else if(newItems==0){
			if(max<5000)
				max=max*5;
			else if(max<10000)
				max=max*2;
			else
				max=max+1000;
		}else{
			max=contents.length+newItems-1;
			pointer=contents.length;
		}

		temp_s=contents;
		contents = new String[max];
		System.arraycopy( temp_s, 0, contents, 0, pointer );

		temp_s=rawData;
		rawData = new String[max];
		System.arraycopy( temp_s, 0, rawData, 0, pointer );

		temp_s=unformattedContent;
		unformattedContent = new String[max];
		System.arraycopy( temp_s, 0, unformattedContent, 0, pointer );

		temp_i=f_writingMode;
		f_writingMode=new int[max];
		f_writingMode = new int[max];
		System.arraycopy( temp_i, 0, f_writingMode, 0, pointer );

		temp_s=f_font_used;
		f_font_used = new String[max];
		System.arraycopy( temp_s, 0, f_font_used, 0, pointer );

		temp_s=colorTag;
		colorTag = new String[max];
		System.arraycopy( temp_s, 0, colorTag, 0, pointer );

		temp_s=font_data;
		font_data = new String[max];
		System.arraycopy( temp_s, 0, font_data, 0, pointer );

		temp_s=fragments;
		fragments = new String[max];
		System.arraycopy( temp_s, 0, fragments, 0, pointer );

		temp_i=text_length;
		text_length = new int[max];
		System.arraycopy( temp_i, 0, text_length, 0, pointer );

        temp_i=rotation;
        rotation = new int[max];
        System.arraycopy( temp_i, 0, rotation, 0, pointer );

        temp_i=lineCount;
		lineCount = new int[max];
		System.arraycopy( temp_i, 0, lineCount, 0, pointer );

		temp_i=move_command;
		move_command = new int[max];
		System.arraycopy( temp_i, 0, move_command, 0, pointer );

		temp_f=f_character_spacing;
		f_character_spacing = new float[max];
		System.arraycopy( temp_f, 0, f_character_spacing, 0, pointer );

		temp_i=parent;
		parent = new int[max];
		//set to default -1
		for(int ii=pointer;ii<max;ii++)
			parent[ii]=-1;
		System.arraycopy( temp_i, 0, parent, 0, pointer );

		temp_i= childrenParent;
		childrenParent = new int[max];
		//set to default -1
		for(int ii=pointer;ii<max;ii++)
			childrenParent[ii]=-1;
		System.arraycopy( temp_i, 0, childrenParent, 0, pointer );

		temp_s=prefix;
		prefix = new String[max];
		//set to default -1
		for(int ii=pointer;ii<max;ii++)
			prefix[ii]=null;
		System.arraycopy( temp_s, 0, prefix, 0, pointer );

		temp_i=token_counter;
		token_counter = new int[max];
		System.arraycopy( temp_i, 0, token_counter, 0, pointer );

        temp_i=rotation;
        rotation = new int[max];
        System.arraycopy( temp_i, 0, rotation, 0, pointer );


        Object[] temp_o=links;
		links = new Object[max];
		System.arraycopy( temp_o, 0, links, 0, pointer );

		temp_o=children;
		children = new Object[max];
		System.arraycopy( temp_o, 0, children, 0, pointer );

		temp_s=category;
		category = new String[max];
		System.arraycopy( temp_s, 0, category, 0, pointer );

		temp_i=objectType;
		objectType = new int[max];
		System.arraycopy( temp_i, 0, objectType, 0, pointer );


		temp_i=f_end_font_size;
		f_end_font_size = new int[max];
		System.arraycopy( temp_i, 0, f_end_font_size, 0, pointer );

		temp_f=space_width;
		space_width = new float[max];
		System.arraycopy( temp_f, 0, space_width, 0, pointer );

		temp_f=f_x1;
		f_x1 = new float[max];
		System.arraycopy( temp_f, 0, f_x1, 0, pointer );

		temp_f=f_x2;
		f_x2 = new float[max];
		System.arraycopy( temp_f, 0, f_x2, 0, pointer );

		temp_f=f_y1;
		f_y1 = new float[max];
		System.arraycopy( temp_f, 0, f_y1, 0, pointer );

		temp_f=f_y2;
		f_y2 = new float[max];
		System.arraycopy( temp_f, 0, f_y2, 0, pointer );
	}

	/**
	 * remove a Text Object
	 */
	final public void removeTextElementAt( int counter )
	{
		text_objects.remove( new Integer(counter) );

	}

	/**
	 * return id of raw fragments stored as space deliminated string
	 */
	public String getFragments(int i) {

		int maxCount=fragments.length;

		while(i<maxCount && fragments[i]==null)
			i++;

		if(i==maxCount)
			return null;
		else
			return fragments[i];
	}

	/**
	 * get parent item for linked items
	 */
	public int getParent(int item_selected) {
		return parent[item_selected];
	}

	/**
	 * get parent item for linked items for children
	 */
	public int getParentChild(int item_selected) {
		return childrenParent[item_selected];
	}

	/**
	 * set prefix item for linked items
	 */
	public String getPrefix(int item_selected) {
		return prefix[item_selected];
	}

	/**
	 * set prefix
	 */
	public void setPrefix(int item, String prefixValue){

		if(item!=-1){
			//resize if needed
			int size=prefix.length;
			if(item>=size){
				max=prefix.length+100;
				String[] temp_i=prefix;
				prefix = new String[max];
				System.arraycopy( temp_i, 0, prefix, 0, size );

				//fill up gaps with -1
				for(int j=size;j<max;j++)
					prefix[j]=null;
			}

			prefix[item]=prefixValue;
		}
	}

	/**
	 * set parent
	 */
	public void setParent(int item, int parentID){
		//System.out.println("set parent="+item+" "+parentID);
		//resize if needed
		int size=parent.length;
		if(item>=size){
			max=parent.length+100;
			int[] temp_i=parent;
			parent = new int[max];
			System.arraycopy( temp_i, 0, parent, 0, size );

			//fill up gaps with -1
			for(int j=size;j<max;j++)
				parent[j]=-1;
		}

		parent[item]=parentID;
	}

	/**
	 * set parentof children object
	 */
	public void setParentChild(int item, int parentID){
		//System.out.println("set parent="+item+" "+parentID);
		//resize if needed
		int size= childrenParent.length;
		if(item>=size){
			max= childrenParent.length+100;
			int[] temp_i= childrenParent;
			childrenParent = new int[max];
			System.arraycopy( temp_i, 0, childrenParent, 0, size );

			//fill up gaps with -1
			for(int j=size;j<max;j++)
				childrenParent[j]=-1;
		}

		childrenParent[item]=parentID;
	}


	/**
	 * set flag to show width in text
	 */
	public void widthIsEmbedded() {

		widthIsEmbedded=true;

	}

	/**
	 * show if width in text
	 */
	public boolean IsEmbedded() {

		return widthIsEmbedded;

	}

	/**fonts size of object - if it changes in object will be FIRST value*/
	public int getFontSize(int currentStory) {
		return fontSize[currentStory];
	}

	/**tag font at start of object*/
	public String getFontTag(int currentStory) {
		return f_font_used[currentStory];
	}

	public String getGlobalField(String globalField) {


		return (String) globalValues.get(globalField);
	}

	public void setGlobalField(String globalNameForCategory,String value) {
		globalValues.put(globalNameForCategory,value);
	}

	public void setMetaFieldUserDefinable(String globalNameForCategory) {
		globalSettings.put("editable-"+globalNameForCategory,"x");
	}

	public boolean isMetaFieldUserDefinable(String globalNameForCategory) {
		return globalSettings.get("editable-"+globalNameForCategory)!=null;
	}

	public void setMetaFieldValues(String globalNameForCategory,String value) {
		globalSettings.put("values-"+globalNameForCategory,value);
	}

	public String getMetaFieldValues(String globalNameForCategory) {
		return (String) globalSettings.get("values-"+globalNameForCategory);
	}


	public Object[] getGlobalFields() {
		return globalValues.keySet().toArray();
	}

	public int getGlobalCategoryCount() {
		return globalValues.size();
	}

	/**
	 * flag set when regexp applied
	 */
	public int getRegExpStatus(int i) {

		return regExpStatus[i];
	}

	/**
	 * set regexp status
	 */
	public void setRegExpStatus(int item, int value){

		//resize if needed
		int size=regExpStatus.length;
		if(item>=size){
			max=regExpStatus.length+100;
			if(item>max)
				max=item+20;
			int[] temp_i=regExpStatus;
			regExpStatus = new int[max];
			System.arraycopy( temp_i, 0, regExpStatus, 0, size );

			//fill up gaps with -1
			for(int j=size;j<max;j++)
				regExpStatus[j]=-1;
		}

		regExpStatus[item]=value;
	}



	public void addExtractedXMLField(String fieldName, String data,int id) {

		if(!fieldName.equals("trash")){
			Integer key=new Integer(id);

			//get existing value and create if needed
			Map currentExtractedFields=(Map) extractedXMLFields.get(key);
			if(currentExtractedFields==null)
				currentExtractedFields=new HashMap();

			//add to existing value and store
			String currentValue=(String) currentExtractedFields.get(fieldName);
			if(currentValue!=null)
				data=currentValue+data;
			//System.out.println("add "+fieldName);
			currentExtractedFields.put(fieldName,data);

			//save at end
			extractedXMLFields.put(key,currentExtractedFields);
		}
	}

	public Map getExtractedTextFields(int id) {

		return (Map) extractedTextFields.get(new Integer(id));
	}

	public Map getExtractedXMLFields(int id) {

		return (Map) extractedXMLFields.get(new Integer(id));
	}

	public void resetExtractedTextFields(int id,Map values) {

		extractedTextFields.put(new Integer(id),values);
	}

	public void flushExtractedFields() {
		extractedTextFields.clear();
		extractedXMLFields.clear();

	}

	/**used by Storypad to show how many pages stored*/
	public int getPageCount() {
		return 1+this.additionalPageCount; 
	}

	public void serialiseAsObject(String fileName) {

		try{
			OutputStream bos = new BufferedOutputStream(new DataOutputStream(new FileOutputStream(fileName)));
			ObjectOutputStream out = new ObjectOutputStream(bos);

			out.writeObject(this);

			out.close();

		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public static PdfData deserialiseAsObject(String savedPage) {

		// PdfData restoredObj=this;
		PdfData restoredObj=new PdfData();

		try{
			InputStream bos = new BufferedInputStream(new DataInputStream(new FileInputStream(savedPage)));
			ObjectInputStream in = new ObjectInputStream(bos);

			restoredObj= (PdfData) in.readObject();
		}catch(Exception ex){
			ex.printStackTrace();
		}

		return restoredObj;
	}


	public void serialise(String fileName) {

		try{
			OutputStream bos = new BufferedOutputStream(new DataOutputStream(new FileOutputStream(fileName)));
			ObjectOutputStream out = new ObjectOutputStream(bos);

			out.writeInt(additionalPageCount);
			out.writeInt(originalSize);
			out.writeBoolean(widthIsEmbedded);
			out.writeBoolean(isColorExtracted);
			out.writeInt(pointer);
			out.writeInt(max);
			out.writeFloat(maxX);
			out.writeFloat(maxY);
			out.writeObject(f_x1);
			out.writeObject(f_y1);
			out.writeObject(f_x2);
			out.writeObject(f_y2);

			out.writeObject(lx1);
			out.writeObject(ly1);
			out.writeObject(lx2);
			out.writeObject(ly2);

			out.writeObject(contents);
			out.writeObject(rawData);
			out.writeObject(unformattedContent);

			out.writeObject(f_writingMode);
			out.writeObject(f_font_used);
			out.writeObject(colorTag);

			out.writeObject(font_data);
			out.writeObject(fragments);
			out.writeObject(text_length);

			out.writeObject(lineCount);
			out.writeObject(move_command);
			out.writeObject(f_character_spacing);

			out.writeObject(parent);
			out.writeObject(prefix);
			out.writeObject(token_counter);

            out.writeObject(rotation);

            out.writeObject(links);

			out.writeObject(children);
			out.writeObject(childrenParent);

			out.writeObject(category);
			out.writeObject(objectType);
			out.writeObject(f_end_font_size);
			out.writeObject(space_width);
			out.writeObject(fontSize);
			out.writeObject(regExpStatus);

			out.close();

		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public PdfData deserialise(String savedPage) {

		//PdfData restoredObj=this;
		PdfData restoredObj=new PdfData();

		extractedTextFields.clear();
		extractedXMLFields.clear();
		text_objects.clear();
		globalValues.clear();
		globalSettings.clear();
		newShapes.clear();

		try{
			InputStream bos = new BufferedInputStream(new DataInputStream(new FileInputStream(savedPage)));
			ObjectInputStream in = new ObjectInputStream(bos);

			restoredObj.additionalPageCount=in.readInt();
			restoredObj.originalSize=in.readInt();
			restoredObj.widthIsEmbedded=in.readBoolean();
			restoredObj.isColorExtracted=in.readBoolean();
			restoredObj.pointer=in.readInt();
			restoredObj.max=in.readInt();
			restoredObj.maxX=in.readInt();
			restoredObj.maxY=in.readInt();

			restoredObj.f_x1= (float[]) in.readObject();
			restoredObj.f_y1= (float[]) in.readObject();
			restoredObj.f_x2= (float[]) in.readObject();
			restoredObj.f_y2= (float[]) in.readObject();

			restoredObj.lx1= (float[]) in.readObject();
			restoredObj.ly1= (float[]) in.readObject();
			restoredObj.lx2= (float[]) in.readObject();
			restoredObj.ly2= (float[]) in.readObject();
			restoredObj.contents= (String[]) in.readObject();
			restoredObj.rawData= (String[]) in.readObject();
			restoredObj.unformattedContent= (String[]) in.readObject();

			restoredObj.f_writingMode= (int[]) in.readObject();
			restoredObj.f_font_used= (String[]) in.readObject();
			restoredObj.colorTag= (String[]) in.readObject();

			restoredObj.font_data= (String[]) in.readObject();
			restoredObj.fragments= (String[]) in.readObject();
			restoredObj.text_length= (int[]) in.readObject();

			restoredObj.lineCount= (int[]) in.readObject();
			restoredObj.move_command= (int[]) in.readObject();
			restoredObj.f_character_spacing= (float[]) in.readObject();

			restoredObj.parent= (int[]) in.readObject();
			restoredObj.prefix= (String[]) in.readObject();
			restoredObj.token_counter= (int[]) in.readObject();
            restoredObj.rotation= (int[]) in.readObject();

            restoredObj.links= (Object[]) in.readObject();

			restoredObj.children=(Object[]) in.readObject();
			restoredObj.childrenParent =(int[]) in.readObject();

			restoredObj.category= (String[]) in.readObject();
			restoredObj.objectType= (int[]) in.readObject();

			restoredObj.f_end_font_size= (int[]) in.readObject();
			restoredObj.space_width= (float[]) in.readObject();
			restoredObj.fontSize= (int[]) in.readObject();
			restoredObj.regExpStatus= (int[]) in.readObject();

			in.close();

		}catch(Exception ex){
			ex.printStackTrace();
		}

		return restoredObj;
	}

	public int getDisplacementForObject(int current_story) {

		int displacement=0;

		//find correct page
		int pageCount=this.itemsOnEachPage.size();
		int currentPage=0;
		int ptReached=0;

		//allow for first page
		if(current_story>=itemsOnEachPage.elementAt(0)){

			while(currentPage<pageCount){

				ptReached=ptReached+itemsOnEachPage.elementAt(currentPage);

				if(current_story>ptReached){
					break;
				}else{
					currentPage++;

				}
			}

			displacement=displacementForEachPage.elementAt(currentPage);
		}
		return displacement;
	}

	/**update changed links*/
	public void resetTotalArea(int current_story, int head) {

		if(this.lx1!=null){
			lx1[head]=lx1[current_story];
			lx2[head]=lx2[current_story];
			ly1[head]=ly1[current_story];
			ly2[head]=ly2[current_story];
		}
	}
	
	public void dispose(){
		
		super.dispose();
		
		itemsOnEachPage=null;
		displacementForEachPage=null;

		text_objects=null;

		extractedTextFields=null;
		
		extractedXMLFields=null;

		fragments=null;

		parent=null;

		childrenParent=null;


		fontSize=null;

	    rotation=null;

	    globalValues=null;
		
	    globalSettings=null;

	}
}
