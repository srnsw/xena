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
* TextState.java
* ---------------
*/
package org.jpedal.objects;

import org.jpedal.utils.LogWriter;

/**
 * holds the current text state
 */
public class TextState
{

	/** current text mode is horizontal or vertical @deprecated*/
	public boolean isHorizontalWritingMode = true;
	
	/**orientation of text using contstants from PdfData*/
	public int writingMode=0;
	
	/**last Tm value*/
	private float[][] TmAtStart = new float[3][3];

    /**last Tm value*/
    private float[][] TmAtStartNoRotation = new float[3][3];


    /**matrix operations for calculating start of text*/
	public float[][] Tm = new float[3][3];

    /**used by storypad so we can unrotate text for scaling*/
    public float[][] TmNoRotation = new float[3][3];

    private String font_ID="";

	/**name of font being used*/
	private String font_family_name = "";

	/**leading setin text*/
	private float TL = 0;

	/**gap between chars set by Tc command*/
	private float character_spacing = 0;

	/**current Tfs value*/
	private float Tfs = 0;

	/** text rise set in stream*/
	private float text_rise = 0;

	/**text height - see also Tfs*/
	private float th = 1;

	/**gap inserted with spaces - set by Tw*/
	private float word_spacing;

	/**font size as whole number*/
	private int current_font_size = 0;

	/**
	 * set Trm values
	 */
	public TextState()
	{
		Tm[0][0] = 1;
		Tm[0][1] = 0;
		Tm[0][2] = 0;
		Tm[1][0] = 0;
		Tm[1][1] = 1;
		Tm[1][2] = 0;
		Tm[2][0] = 0;
		Tm[2][1] = 0;
		Tm[2][2] = 1;

        TmAtStart[0][0] = 1;
		TmAtStart[0][1] = 0;
		TmAtStart[0][2] = 0;
		TmAtStart[1][0] = 0;
		TmAtStart[1][1] = 1;
		TmAtStart[1][2] = 0;
		TmAtStart[2][0] = 0;
		TmAtStart[2][1] = 0;
		TmAtStart[2][2] = 1;

        TmNoRotation[0][0] = 1;
		TmNoRotation[0][1] = 0;
		TmNoRotation[0][2] = 0;
		TmNoRotation[1][0] = 0;
		TmNoRotation[1][1] = 1;
		TmNoRotation[1][2] = 0;
		TmNoRotation[2][0] = 0;
		TmNoRotation[2][1] = 0;
		TmNoRotation[2][2] = 1;
    }
	
	/**
	 * get Tm at start of line
	 */
	public float[][] getTMAtLineStart() {
		return TmAtStart;
	}

	/**
	 * set Tm at start of line
	 */
	public void setTMAtLineStart() {

		//keep position in case we need
		TmAtStart[0][0] = Tm[0][0];
		TmAtStart[0][1] = Tm[0][1];
		TmAtStart[0][2] = Tm[0][2];
		TmAtStart[1][0] = Tm[1][0];
		TmAtStart[1][1] = Tm[1][1];
		TmAtStart[1][2] = Tm[1][2];
		TmAtStart[2][0] = Tm[2][0];
		TmAtStart[2][1] = Tm[2][1];
		TmAtStart[2][2] = Tm[2][2];
		
	}

    /**
	 * get Tm at start of line
	 */
	public float[][] getTMAtLineStartNoRotation() {
		return TmAtStartNoRotation;
	}

	/**
	 * set Tm at start of line
	 */
	public void setTMAtLineStartNoRotation() {

		//keep position in case we need
		TmAtStartNoRotation[0][0] = TmNoRotation[0][0];
		TmAtStartNoRotation[0][1] = TmNoRotation[0][1];
		TmAtStartNoRotation[0][2] = TmNoRotation[0][2];
		TmAtStartNoRotation[1][0] = TmNoRotation[1][0];
		TmAtStartNoRotation[1][1] = TmNoRotation[1][1];
		TmAtStartNoRotation[1][2] = TmNoRotation[1][2];
		TmAtStartNoRotation[2][0] = TmNoRotation[2][0];
		TmAtStartNoRotation[2][1] = TmNoRotation[2][1];
		TmAtStartNoRotation[2][2] = TmNoRotation[2][2];

	}

    //////////////////////////////////////////////////////////////////////////
	/**
	 * set Horizontal Scaling
	 */
	final public void setHorizontalScaling( float th )
	{
		this.th = th;
	}
	///////////////////////////////////////////////////////////////////////
	/**
	 * get font id
	 */
	final public String getFontID()
	{
		return font_ID;
	}
	///////////////////////////////////////////////////////////////////////////
	/**
	 * get Text rise
	 */
	final public float getTextRise()
	{
		return text_rise;
	}
	////////////////////////////////////////////////////////////////////////
	/**
	 * get character spacing
	 */
	final public float getCharacterSpacing()
	{
		return character_spacing;
	}
	/////////////////////////////////////////////////////////////////////////
	/**
	 * get word spacing
	 */
	final public float getWordSpacing()
	{
		return word_spacing;
	}
	///////////////////////////////////////////////////////////////////////////
	/**
	 * set font tfs
	 */
	final public void setLeading( float TL )
	{
		this.TL = TL;
	}
	/////////////////////////////////////////////////////////////////////////
	/**
	 * get font tfs
	 */
	final public float getTfs()
	{
		return Tfs;
	}
	
	/////////////////////////////////////////////////////////////////////////
	/**
	 * get Horizontal Scaling
	 */
	final public float getHorizontalScaling()
	{
		return th;
	}
	///////////////////////////////////////////////////////////////////////
	/**
	 * get font name
	 */
	final public String getFontName()
	{
		return font_family_name;
	}
	///////////////////////////////////////////////////////////////////////////
	/**
	 * set Text rise
	 */
	final public void setTextRise( float text_rise )
	{
		this.text_rise = text_rise;
	}
	///////////////////////////////////////////////////////////////////////
	/**
	 * get current font size
	 */
	final public int getCurrentFontSize()
	{
		int value = current_font_size;

		//trap for Postscript where Tm never used
		if( value == 0 )
			value = (int)this.Tfs;
		return value;
	}
	////////////////////////////////////////////////////////////////////////
	/**
	 * set current font size
	 */
	final public void setCurrentFontSize( int value )
	{
		this.current_font_size = value;
	}

	///////////////////////////////////////////////////////////////////////////
	/**
	 * get font tfs
	 */
	final public float getLeading()
	{
		return TL;
	}


	/////////////////////////////////////////////////////////////////////////
	/**
	 * clone object
	 */
	/*final public Object clone()
	{
		Object o = null;
		try
		{
			o = super.clone();
		}
		catch( Exception e )
		{
			LogWriter.writeLog( "Unable to clone " + e );
		}
		return o;
	}*/
	/////////////////////////////////////////////////////////////////////////
	
	final public Object clone(){
		
		TextState ts = new TextState();

		ts.isHorizontalWritingMode = isHorizontalWritingMode ;
		
		ts.writingMode = writingMode;
		
		if(TmAtStart != null){
			for(int i=0;i<3;i++){
                System.arraycopy(TmAtStart[i], 0, ts.TmAtStart[i], 0, 3);
			}
		}

		if(TmAtStartNoRotation != null){
			for(int i=0;i<3;i++){
                System.arraycopy(TmAtStartNoRotation[i], 0, ts.TmAtStartNoRotation[i], 0, 3);
			}
		}

		if(Tm != null){
			for(int i=0;i<3;i++){
                System.arraycopy(Tm[i], 0, ts.Tm[i], 0, 3);
			}
		}

		if(TmNoRotation != null){
			for(int i=0;i<3;i++){
                System.arraycopy(TmNoRotation[i], 0, ts.TmNoRotation[i], 0, 3);
			}
		}

		if(font_ID!=null)
			ts.font_ID=new String(font_ID.getBytes());

		if(font_family_name!=null)
			ts.font_family_name=new String(font_family_name.getBytes());

		ts.TL = TL;

		ts.character_spacing = character_spacing;

		ts.Tfs = Tfs;

		ts.text_rise = text_rise;

		ts.th = th;

		ts.word_spacing = word_spacing;

		ts.current_font_size = current_font_size;
		
		return ts;
	}
	/**
	 * set word spacing
	 */
	final public void setWordSpacing( float word_spacing )
	{
		this.word_spacing = word_spacing;
	}

	/////////////////////////////////////////////////////////////////////////
	/**
	 * set font name
	 */
	final public void setFont( String font_family_name,String font_ID )
	{
		this.font_family_name = font_family_name;
		this.font_ID = font_ID;
	}
	/////////////////////////////////////////////////////////////////////////
	/**
	 * set character spacing
	 */
	final public void setCharacterSpacing( float character_spacing )
	{
		this.character_spacing = character_spacing;
	}
	//////////////////////////////////////////////////////////////////////////
	/**
	 * set font tfs to default
	 */
	final public void resetTm()
	{
		Tm[0][0] = 1;
		Tm[0][1] = 0;
		Tm[0][2] = 0;
		Tm[1][0] = 0;
		Tm[1][1] = 1;
		Tm[1][2] = 0;
		Tm[2][0] = 0;
		Tm[2][1] = 0;
		Tm[2][2] = 1;

        TmNoRotation[0][0] = 1;
		TmNoRotation[0][1] = 0;
		TmNoRotation[0][2] = 0;
		TmNoRotation[1][0] = 0;
		TmNoRotation[1][1] = 1;
		TmNoRotation[1][2] = 0;
		TmNoRotation[2][0] = 0;
		TmNoRotation[2][1] = 0;
		TmNoRotation[2][2] = 1;


    }

	//////////////////////////////////////////////////////////////////////////
	/**
	 * set font tfs
	 */
	final public void setFontTfs( float Tfs )
	{
		this.Tfs = Tfs;
	}
}
