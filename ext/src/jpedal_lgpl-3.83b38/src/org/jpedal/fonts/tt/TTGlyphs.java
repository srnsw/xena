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
* TTGlyphs.java
* ---------------
*/
package org.jpedal.fonts.tt;

import java.util.Iterator;
import java.util.Map;

import org.jpedal.fonts.glyph.GlyphFactory;
import org.jpedal.fonts.glyph.PdfGlyph;
import org.jpedal.fonts.glyph.PdfJavaGlyphs;

import org.jpedal.fonts.FontMappings;
import org.jpedal.fonts.StandardFonts;
import org.jpedal.fonts.objects.FontData;
import org.jpedal.PdfDecoder;

public class TTGlyphs extends PdfJavaGlyphs {

	protected boolean hasGIDtoCID;

	protected int[] CIDToGIDMap;

    float[] FontBBox=new float[]{0f,0f,1000f,1000f};

    private String[] diffTable=null;

    private CMAP currentCMAP;
	private Post currentPost;
	private Glyf currentGlyf;
	private Hmtx currentHmtx;

	private FontFile2 fontTable;

	//private Head currentHead;
	//private Name currentName;
	//private Mapx currentMapx;
	//private Loca currentLoca;
	//private Cvt currentCvt;
	//private Fpgm currentFpgm;
	//private Hhea currentHhea;

    private CFF currentCFF;

    int glyphCount=0;

    //assume TT and set to OTF further down
    int type= StandardFonts.TRUETYPE;

	private int unitsPerEm;

    private boolean hasCFF;

	private boolean isCID;

    /**
	 * used by  non type3 font
	 */
	public PdfGlyph getEmbeddedGlyph(GlyphFactory factory, String glyph, float[][]Trm, int rawInt, 
			String displayValue, float currentWidth, String key) {

		int id=rawInt;
		if(hasGIDtoCID)
			rawInt=CIDToGIDMap[rawInt];

		/**flush cache if needed*/
		if((lastTrm[0][0]!=Trm[0][0])|(lastTrm[1][0]!=Trm[1][0])|
				(lastTrm[0][1]!=Trm[0][1])|(lastTrm[1][1]!=Trm[1][1])){
			lastTrm=Trm;
			flush();
		}

		//either calculate the glyph to draw or reuse if alreasy drawn
		PdfGlyph transformedGlyph2 = getEmbeddedCachedShape(id);

		if (transformedGlyph2 == null) {

            //use CMAP to get actual glyph ID
            int idx=rawInt;


            if(!isCID || !isIdentity())
                idx=currentCMAP.convertIndexToCharacterCode(glyph,rawInt,remapFont,isSubsetted, diffTable);

            //if no value use post to lookup
            if(idx<1)
                idx=currentPost.convertGlyphToCharacterCode(glyph);

            //shape to draw onto
			try{
                if(hasCFF){
                  
                	transformedGlyph2=currentCFF.getCFFGlyph(factory,glyph,Trm,idx, displayValue,currentWidth,key);
                    
                    //set raw width to use for scaling
                	if(transformedGlyph2!=null)
                    transformedGlyph2.setWidth(getUnscaledWidth(glyph, rawInt, displayValue,false));
                    
                   
                }else
                    transformedGlyph2=getTTGlyph(idx,glyph,rawInt, displayValue,currentWidth);
			}catch(Exception e){
				transformedGlyph2=null;

			}

			//save so we can reuse if it occurs again in this TJ command
			setEmbeddedCachedShape(id, transformedGlyph2);
		}

		return transformedGlyph2;
	}

/*
	 * creates glyph from truetype font commands
	 */
	public PdfGlyph getTTGlyph(int idx,String glyph,int rawInt, String displayValue, float currentWidth) {

		//System.out.println(glyph);

		PdfGlyph currentGlyph=null;
		/**
		if(rawInt>glyphCount){
			LogWriter.writeLog("Font index out of bounds using defaul t"+glyphCount);
			rawInt=0;

		}*/

		try{
			//final boolean debug=(rawInt==2465);
			final boolean debug=false;


			if(idx!=-1){
				//move the pointer to the commands
				int p=currentGlyf.getCharString(idx);

				if(p!=-1){
					currentGlyph=new TTGlyph(glyph,debug,currentGlyf, fontTable,currentHmtx,idx,(unitsPerEm/1000f));
					if(debug)
						System.out.println(">>"+p+ ' ' +rawInt+ ' ' +displayValue+ ' ' +baseFontName);
				}
			}

		}catch(Exception ee){
			ee.printStackTrace();

		}

		//if(glyph.equals("fl"))

		return currentGlyph;
	}

    public void setDiffValues(String[] diffTable) {

        this.diffTable=diffTable;
    }

	public void setEncodingToUse(boolean hasEncoding, int fontEncoding, boolean isSubstituted, boolean isCIDFont) {

			if(currentCMAP!=null)
			currentCMAP.setEncodingToUse(hasEncoding,fontEncoding,isSubstituted,isCIDFont);

	}

	public int getConvertedGlyph(int idx){
		
		if(currentCMAP==null)
			return idx;
		else
			return currentCMAP.convertIndexToCharacterCode(null,idx,false,false,diffTable);

	}

	/*
	 * creates glyph from truetype font commands
	 */
	public float getTTWidth(String glyph,int rawInt, String displayValue,boolean TTstreamisCID) {

        //use CMAP if not CID
		int idx=rawInt;

		float width=0;

		try{
			if((!TTstreamisCID))
				idx=currentCMAP.convertIndexToCharacterCode(glyph,rawInt,remapFont,isSubsetted,diffTable);

			//if no value use post to lookup
			if(idx<1)
				idx=currentPost.convertGlyphToCharacterCode(glyph);

			//if(idx!=-1)
				width=currentHmtx.getWidth(idx);

		}catch(Exception e){

		}

        return width;
	}
	
	/*
	 * creates glyph from truetype font commands
	 */
	private float getUnscaledWidth(String glyph,int rawInt, String displayValue,boolean TTstreamisCID) {

        //use CMAP if not CID
		int idx=rawInt;

		float width=0;

		try{
			if((!TTstreamisCID))
				idx=currentCMAP.convertIndexToCharacterCode(glyph, rawInt, remapFont, isSubsetted, diffTable);

			//if no value use post to lookup
			if(idx<1)
				idx=currentPost.convertGlyphToCharacterCode(glyph);

			//if(idx!=-1)
				width=currentHmtx.getUnscaledWidth(idx);

		}catch(Exception e){

		}

        return width;
	}


	public void setGIDtoCID(int[] cidToGIDMap) {

		hasGIDtoCID=true;
		this.CIDToGIDMap=cidToGIDMap;

	}

    /**
     * return name of font or all fonts if TTC
     * NAME will be LOWERCASE to avoid issues of capitalisation
     * when used for lookup - if no name, will default to  null
     *
     * @mode is PdfDecoder.SUBSTITUTE_* CONSTANT. RuntimeException will be thrown on invalid value
     */
    public static String[] readFontNames(FontData fontData,int mode) {

        String[] fontNames=new String[0];

        /**setup read the table locations*/
        FontFile2 currentFontFile=new FontFile2(fontData);

        //get type
        int fontType=currentFontFile.getType();

        int fontCount=currentFontFile.getFontCount();
  
        fontNames=new String[fontCount];

        /**read tables for names*/
        for(int i=0;i<fontCount;i++){

            currentFontFile.setSelectedFontIndex(i);

            Name currentName=new Name(currentFontFile);

            String name=null;

            if(mode==PdfDecoder.SUBSTITUTE_FONT_USING_POSTSCRIPT_NAME)
                name=currentName.getString(Name.POSTSCRIPT_NAME);
            else if(mode==PdfDecoder.SUBSTITUTE_FONT_USING_FAMILY_NAME)
                name=currentName.getString(Name.FONT_FAMILY_NAME);
            else if(mode==PdfDecoder.SUBSTITUTE_FONT_USING_FULL_FONT_NAME)
                name=currentName.getString(Name.FULL_FONT_NAME);
            else //tell user if invalid
                throw new RuntimeException("Unsupported mode "+mode+". Unable to resolve font names");

            if(name==null)
                fontNames[i]=null;
            else
                fontNames[i]=name.toLowerCase();
        }
        
        if(fontData!=null)
        	fontData.close();

        return fontNames;
    }
    
    /**
     * Add font details to Map so we can access later
     */
    public static void addStringValues(FontData fontData, Map fontDetails) {

        /**setup read the table locations*/
        FontFile2 currentFontFile=new FontFile2(fontData);

        //get type
        //int fontType=currentFontFile.getType();

        int fontCount=currentFontFile.getFontCount();
  
        /**read tables for names*/
        for(int i=0;i<fontCount;i++){

        	currentFontFile.setSelectedFontIndex(i);

            Name currentName=new Name(currentFontFile);

            Map stringValues= currentName.getStrings();
            
            
            if(stringValues!=null){
            	Iterator keys=stringValues.keySet().iterator();
            	while(keys.hasNext()){
            		Integer currentKey=(Integer) keys.next();
            		
            		int keyInt=currentKey.intValue();
            		if(keyInt<Name.stringNames.length)
            		fontDetails.put(Name.stringNames[currentKey.intValue()], stringValues.get(currentKey));
            	}
            }   
        }
        
        if(fontData!=null)
        	fontData.close();
    }

    public int readEmbeddedFont(boolean TTstreamisCID, byte[] fontDataAsArray,FontData fontData) {

        FontFile2 currentFontFile;

        isCID=TTstreamisCID;
        
        /**setup read the table locations*/
        if(fontDataAsArray!=null)
            currentFontFile=new FontFile2(fontDataAsArray);
       	else
            currentFontFile=new FontFile2(fontData);
        
		//<start-jfr>
        //select font if TTC
        //does nothing if TT
		if(FontMappings.fontSubstitutionFontID==null){
			currentFontFile.setPointer(0);
		}else{
	        Integer fontID= (Integer) FontMappings.fontSubstitutionFontID.get(fontName.toLowerCase());
	
	        if(fontID!=null)
	            currentFontFile.setPointer(fontID.intValue());
	        else
	            currentFontFile.setPointer(0);
		}
		//<end-jfr>
		
        /**read tables*/
		Head currentHead=new Head(currentFontFile);

		currentPost=new Post(currentFontFile);

		//currentName=new Name(currentFontFile);
        
		Mapx currentMapx=new Mapx(currentFontFile);
		glyphCount=currentMapx.getGlyphCount();
		Loca currentLoca=new Loca(currentFontFile,glyphCount,currentHead.getFormat());
		currentGlyf=new Glyf(currentFontFile,glyphCount,currentLoca.getIndices());

        currentCFF=new CFF(currentFontFile,isCID);

        hasCFF=currentCFF.hasCFFData();
        if(hasCFF)
        	type= StandardFonts.OPENTYPE;

        //currentCvt=new Cvt(currentFontFile);
		//currentFpgm=new Fpgm(currentFontFile);
		Hhea currentHhea=new Hhea(currentFontFile);

        FontBBox=currentHead.getFontBBox();

        currentHmtx=new Hmtx(currentFontFile,glyphCount,currentHhea.getNumberOfHMetrics(),(int)FontBBox[3]);

        //not all files have CMAPs
		//if(!TTstreamisCID){
			int startPointer=currentFontFile.selectTable(FontFile2.CMAP);

			if(startPointer!=0)
			currentCMAP=new CMAP(currentFontFile,startPointer,currentGlyf);

		//}
           
        unitsPerEm=currentHead.getUnitsPerEm();

		currentFontFile=null;
		fontTable=new FontFile2(currentGlyf.getTableData(),true);
		
		if(fontData!=null)
        	fontData.close();
		
        return type;
    }

     public float[] getFontBoundingBox() {
        return FontBBox;
    }
    
    public int getType() {
        return type; 
    }
    
}
