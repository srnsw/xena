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
* PdfJavaGlyphs.java
* ---------------
*/
package org.jpedal.fonts.glyph;

//<start-jfr>
import org.jpedal.fonts.FontMappings;
import org.jpedal.fonts.StandardFonts;
import org.jpedal.fonts.objects.FontData;

//<end-jfr>

import java.awt.geom.Area;
import java.awt.geom.AffineTransform;
import java.awt.font.GlyphVector;
import java.awt.font.FontRenderContext;
import java.awt.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PdfJavaGlyphs implements PdfGlyphs,Serializable{

	/**shapes we have already drawn to speed up plotting, or <code>null</code> if there are none*/
	private Area[] cachedShapes = null;
	private AffineTransform[] cachedAt = null;

	public boolean isFontInstalled = false;

	/**default font to use in display*/
	public String defaultFont = "Lucida Sans";

	/**lookup for font names less any + suffix*/
	public String fontName="default";
	
	//some fonts need to be remapped (ie Arial-BoldMT to Arial,Bold)
	public String logicalfontName="default";
	
	Map chars=new HashMap();
	Map displayValues=new HashMap();
	Map embeddedChars=new HashMap();
	
	/**flag is CID font is identity matrix*/
	private boolean isIdentity=false;
    private boolean isFontEmbedded =false;
    private boolean hasWidths=true;

    public void flush(){
        cachedShapes = null;
        cachedAt = null;
    }

    public String getBaseFontName() {
		return baseFontName;
	}

	public void setBaseFontName(String baseFontName) {
		this.baseFontName=baseFontName;
	}

	public String baseFontName="";

	public boolean isSubsetted;


	/**copy of Trm so we can choose if cache should be flushed*/
	public float[][] lastTrm=new float[3][3];

	/**current font to plot, or <code>null</code> if not used yet*/
	private Font unscaledFont = null;

	public boolean isArialInstalledLocally;
	private int maxCharCount=255;
	public boolean isCIDFont;

	/**make 256 value fonts to f000 range if flag set*/
	public boolean remapFont=false;

	public String font_family_name;

	public int style;


	/**used to render page by drawing routines*/
	public static FontRenderContext frc =new FontRenderContext(null, true, true);

	/**list of installed fonts*/
	public static String[] fontList;


	/**
	 * used for standard non-substituted version
	 * @param Trm
	 * @param rawInt
	 * @param displayValue
	 * @param currentWidth
	 */
	public Area getStandardGlyph(float[][]Trm, int rawInt, String displayValue, float currentWidth, boolean isSTD) {

		/**flush cache if needed*/
		if(lastTrm[0][0]!=Trm[0][0] || lastTrm[1][0]!=Trm[1][0] || lastTrm[0][1]!=Trm[0][1] || lastTrm[1][1]!=Trm[1][1]){
			lastTrm =Trm;
			cachedShapes = null;
		}

		//either calculate the glyph to draw or reuse if already drawn
		Area transformedGlyph2 = getCachedShape(rawInt);

        if (transformedGlyph2 == null) {

            double dY = -1,dX=1,y=0;

            AffineTransform at;

            //allow for text running up the page
            if ((Trm[1][0] < 0 && Trm[0][1] >= 0)||(Trm[0][1] < 0 && Trm[1][0] >= 0)) {
                dX=1f;
                dY=-1f;
            }

			if (isSTD) {

				transformedGlyph2=getGlyph(rawInt, displayValue, currentWidth);

                //hack to fix problem with Java Arial font
				if(transformedGlyph2!=null && rawInt==146 && isArialInstalledLocally)
                        y=-(transformedGlyph2.getBounds().height-transformedGlyph2.getBounds().y);
            }else {

				//remap font if needed
				String xx=displayValue;
				if(remapFont &&(getUnscaledFont().canDisplay(xx.charAt(0))==false))
					xx= String.valueOf((char) (rawInt + 0xf000));

				GlyphVector gv1 =null;

				//do not show CID fonts as Lucida unless match
				if(!isCIDFont|| isFontInstalled)
					gv1=getUnscaledFont().createGlyphVector(frc, xx);

				if(gv1!=null){

					transformedGlyph2 = new Area(gv1.getOutline());

					//put glyph into display position
					double glyphX=gv1.getOutline().getBounds2D().getX();

                    //ensure inside box
					if(glyphX<0){
						glyphX=-glyphX;
						at =AffineTransform.getTranslateInstance(glyphX*2,0);
						transformedGlyph2.transform(at);
						//x=-glyphX*2;
					}

					double glyphWidth=gv1.getVisualBounds().getWidth()+(glyphX*2);
					double scaleFactor=currentWidth/glyphWidth;
					if(scaleFactor<1)
						dX=dX*scaleFactor;									
				}
			}
			
			//create shape for text using transformation to make correct size
            at =new AffineTransform(dX*Trm[0][0],dX*Trm[0][1],dY*Trm[1][0],dY*Trm[1][1] ,0,y);

            if(transformedGlyph2!=null){
				transformedGlyph2.transform(at);
			}
			
			//save so we can reuse if it occurs again in this TJ command
			setCachedShape(rawInt, transformedGlyph2,at);
		}

		return transformedGlyph2;
	}

	/**returns a generic glyph using inbuilt fonts*/
    public Area getGlyph(int rawInt, String displayValue,float currentWidth){

        boolean fontMatched=true;

        /**use default if cannot be displayed*/
        GlyphVector gv1=null;

        //remap font if needed
        String xx=displayValue;

        if((remapFont)&&(getUnscaledFont().canDisplay(xx.charAt(0))==false))
            xx= String.valueOf((char) (rawInt + 0xf000));

        /**commented out 18/8/04 when font code updated*/
        //if cannot display return to Lucida
        if(getUnscaledFont().canDisplay(xx.charAt(0))==false){
            xx=displayValue;
            fontMatched=false;
        }

        if(this.isCIDFont && isFontEmbedded && fontMatched){
            gv1=null;
        }else if(fontMatched){
            gv1 =getUnscaledFont().createGlyphVector(frc, xx);
        }else{
            Font tempFont = new Font(defaultFont, 0, 1);
            if(tempFont.canDisplay(xx.charAt(0))==false)
                tempFont = new Font("lucida", 0, 1);
            if(tempFont.canDisplay(xx.charAt(0)))
                gv1 =tempFont.createGlyphVector(frc, xx);
        }

        //gv1 =getUnscaledFont().createGlyphVector(frc, xx);

        Area transformedGlyph2 = null;
        if(gv1!=null){
            transformedGlyph2=new Area(gv1.getOutline());

            //put glyph into display position
            double glyphX=gv1.getOutline().getBounds2D().getX();
            //double glyphY=gv1.getOutline().getBounds2D().getY();
            double width=gv1.getOutline().getBounds2D().getWidth();

            AffineTransform at;

            if(!hasWidths){ //center for looks
                //try standard values which are indexed under NAME of char
                String charName = StandardFonts.getUnicodeChar(StandardFonts.WIN, rawInt);
                float leading=(float)(currentWidth-(width+glyphX+glyphX))/2;

                if(leading>0){
                    at =AffineTransform.getTranslateInstance(leading,0);
                    transformedGlyph2.transform(at);
                }
            }else{ 
            	
            	if(glyphX<0){ //ensure inside box
            		glyphX=-glyphX;
	                at =AffineTransform.getTranslateInstance(glyphX,0);
	                transformedGlyph2.transform(at);
            	}
            	
                double scaleFactor=currentWidth/(transformedGlyph2.getBounds2D().getWidth());
                if(scaleFactor<1)	{
                    at =AffineTransform.getScaleInstance(scaleFactor,1);
                    transformedGlyph2.transform(at);
                }
            }
        }

        return transformedGlyph2;
    }

	/**
		 * Caches the specified shape.
		 */
		public final void setCachedShape(int idx, Area shape, AffineTransform at) {
			// using local variable instead of sync'ing
			Area[] cache = cachedShapes;
            AffineTransform[] atCache=cachedAt;

			if (cache == null){
				cachedShapes = cache = new Area[maxCharCount];
				cachedAt = atCache = new AffineTransform[maxCharCount];
            }

			if(shape==null)
				cache[idx] = cache[idx] =null;
			else
				cache[idx] = cache[idx] = (Area)shape.clone();

            if(shape!=null && at!=null)
                atCache[idx]=at;
		}

    /**
	 * Returns the specified shape from the cache, or <code>null</code> if the shape
	 * is not in the cache.
	 */
	public final AffineTransform getCachedTransform(int idx) {
		// using local variable instead of sync'ing
		AffineTransform[] cache = cachedAt;

		if(cache==null)
			return null;
		else
			return cache[idx];

	}

	/**
	 * Returns the specified shape from the cache, or <code>null</code> if the shape
	 * is not in the cache.
	 */
	public final Area getCachedShape(int idx) {
		// using local variable instead of sync'ing
		Area[] cache = cachedShapes;

		if(cache==null)
			return null;
		else{
			Area currentShape=cache[idx];

			if(currentShape==null)
				return null;
			else
				return  (Area)currentShape.clone();
		}
		//return cache == null ? null : (Area)cache[idx].clone();
		//return cache == null ? null : cache[idx];
	}

	public void init(int maxCharCount, boolean isCIDFont) {
		this.maxCharCount=maxCharCount;
		this.isCIDFont=isCIDFont;
    }

	/**set the font being used or try to approximate*/
		public final Font setFont(String name,int size) {
                      
			if(name.equals("Helv"))
				name="Helvetica";
			else if(name.equals("HeBo"))
				name="Helvetica-BOLD";
			else if(name.equals("ZaDb"))
				name="ZapfDingbats";

			//set defaults
			this.font_family_name=name;
			this.style =Font.PLAIN;

			String weight =null,mappedName=null;

			if(font_family_name==null)
				font_family_name=this.fontName;

			String testFont=font_family_name;
			if(font_family_name!=null)
				testFont=font_family_name.toLowerCase();

			//pick up any weight in type 3 font or - standard font mapped to Java
			int pointer = font_family_name.indexOf(',');
			if ((pointer == -1))//&&(StandardFonts.javaFontList.get(font_family_name)!=null))
				pointer = font_family_name.indexOf('-');

            //<start-jfr>
            if (pointer != -1) {

				//see if present with ,
				mappedName=(String) FontMappings.fontSubstitutionAliasTable.get(testFont);


				weight =testFont.substring(pointer + 1, testFont.length());

				style = getWeight(weight);

				font_family_name = font_family_name.substring(0, pointer).toLowerCase();

				testFont=font_family_name;

				if(testFont.endsWith("mt"))
					testFont=testFont.substring(0,testFont.length()-2);

			}

			//remap if not type 3 match
			if(mappedName==null)
			mappedName=(String) FontMappings.fontSubstitutionAliasTable.get(testFont);
            //<end-jfr>

            if((mappedName!=null)&&(mappedName.equals("arialbd")))
				mappedName="arial-bold";

			if(mappedName!=null){

				font_family_name=mappedName;

				pointer = font_family_name.indexOf('-');
				if(pointer!=-1){

					font_family_name=font_family_name.toLowerCase();

					weight =font_family_name.substring(pointer + 1, font_family_name.length());

					style = getWeight(weight);

					font_family_name = font_family_name.substring(0, pointer);
				}

				testFont=font_family_name.toLowerCase();

				if(testFont.endsWith("mt"))
					testFont=testFont.substring(0,testFont.length()-2);

			}

			//see if installed
			if(fontList!=null){
				int count = fontList.length;
				for (int i = 0; i < count; i++) {
					if ((fontList[i].equals(testFont))||((weight==null)&&(testFont.startsWith(fontList[i])))) {
						isFontInstalled = true;
						font_family_name=fontList[i];
						i = count;
					}
				}

				//hack for windows as some odd things going on
				if(isFontInstalled && font_family_name.equals("arial")){
					isArialInstalledLocally=true;
				}
			}



			/**approximate display if not installed*/
			if (isFontInstalled == false) {

				//try to approximate font
				if(weight==null){

					//pick up any weight
					String test = font_family_name.toLowerCase();
					style=getWeight(test);

				}

				font_family_name = defaultFont;
			}


			unscaledFont = new Font(font_family_name, style, size);

			

			return unscaledFont;
		}

	/**
	 * work out style (ITALIC, BOLD)
	 */
	private int getWeight(String weight) {

		int style=Font.PLAIN;

		if(weight.endsWith("mt"))
			weight=weight.substring(0,weight.length()-2);

		if (weight.indexOf("heavy") != -1)
			style = Font.BOLD;
		else if (weight.indexOf("bold") != -1)
			style = Font.BOLD;
		else if (weight.indexOf("roman") != -1)
			style = Font.ROMAN_BASELINE;

		if (weight.indexOf("italic") != -1)
			style = style+Font.ITALIC;
		else if (weight.indexOf("oblique") != -1)
			style = style+Font.ITALIC;

		return style;
	}


	/**
		 * Returns the unscaled font, initializing it first if it hasn't been used before.
		 */
		public final Font getUnscaledFont() {

			/**commenting out  this broke originaldoc.pdf*/
			if (unscaledFont == null)
				unscaledFont = new Font(defaultFont, Font.PLAIN, 1);

			return unscaledFont;
		}

	protected PdfGlyph[] cachedEmbeddedShapes=null;

	protected int localBias=0,globalBias=0;

	/**
	 * Caches the specified shape.
	 */
	public final void setEmbeddedCachedShape(int idx, PdfGlyph shape) {
		// using local variable instead of sync'ing
		PdfGlyph[] cache = cachedEmbeddedShapes;
        if (cache == null)
			cachedEmbeddedShapes = cache = new PdfGlyph[maxCharCount];

        cache[idx]  = shape;
	}

	/**
	 * Returns the specified shape from the cache, or <code>null</code> if the shape
	 * is not in the cache.
	 */
	public final PdfGlyph getEmbeddedCachedShape(int idx) {
		// using local variable instead of sync'ing
		PdfGlyph[] cache = cachedEmbeddedShapes;

        if(cache==null)
			return null;
		else{
            PdfGlyph currentShape=cache[idx];

			if(currentShape==null)
				return null;
			else
				return currentShape;
		}
		//return cache == null ? null : (Area)cache[idx].clone();
		//return cache == null ? null : cache[idx];
	}

	/**
	 * template used by t1/t3/tt fonts
	 */
	public PdfGlyph getEmbeddedGlyph(GlyphFactory factory, String glyph, float[][] trm, int rawInt, String displayValue, float currentWidth, String key) {
		return null;
	}

	public void setGIDtoCID(int[] cidToGIDMap) {
	}

	public void setEncodingToUse(boolean hasEncoding, int fontEncoding, boolean b, boolean isCIDFont) {

	}


	public int readEmbeddedFont(boolean TTstreamisCID, byte[] fontDataAsArray, FontData fontData) {
        return 0;
	}

	public void setIsSubsetted(boolean b) {
		isSubsetted=b;
	}

	public void setT3Glyph(int key, int altKey, PdfGlyph glyph) {

	}


	public void setCharString(String s, byte[] bytes) {
	}

	public int getNumber(FontData fontData, int p, double[] op, int i, boolean b) {

        return 0;
	}

    public int getNumber(byte[] fontDataAsArray, int p, double[] op, int i, boolean b) {

        return 0;
	}

    public boolean is1C() {
		return false;
	}

	public void setis1C(boolean b) {
	}

	
	public void setValuesForGlyph(int rawInt, String charGlyph, String displayValue, String embeddedChar) {
		Integer key=new Integer(rawInt);
		chars.put(key,charGlyph);
		displayValues.put(key,displayValue);
		embeddedChars.put(key,embeddedChar);
	}

	public String getDisplayValue(Integer key) {
		return (String) displayValues.get(key);
	}

	public String getCharGlyph(Integer key) {
		return (String) chars.get(key);
	}

	public String getEmbeddedEnc(Integer key) {
		
		return (String) embeddedChars.get(key);
	}
	
	public Map getDisplayValues() {
		return displayValues;
	}

	public Map getCharGlyphs() {
		return chars;
	}

	public Map getEmbeddedEncs() {
		
		return  embeddedChars;
	}
	
	public void setDisplayValues(Map displayValues) {
		this.displayValues=displayValues;
	}

	public void setCharGlyphs(Map chars) {
		this.chars=chars;
	}

	public void setEmbeddedEncs(Map embeddedChars) {
		
		this.embeddedChars=embeddedChars;
	}

	public void setLocalBias(int i) {
		localBias=i;
		
	}
	
	public void setGlobalBias(int i) {
		globalBias=i;
		
	}

    public float getTTWidth(String charGlyph, int rawInt, String displayValue, boolean b) {
    	
    	
        return 0;  //To change body of created methods use File | Settings | File Templates.
    }

	public String getPostName(int rawInt) {
		return "notdef";
	}

	/**
	 * should never be called - just to allow TTGlyphs to extend
	 * @param rawInt
	 */
	public int getConvertedGlyph(int rawInt) {
		return -1;
	}

	/**
	 * flag for CID TT fonts
	 * @param isIdentity
	 */
	public void setIsIdentity(boolean isIdentity) {
		this.isIdentity=isIdentity;
	}
	
	/**
	 * flag to show if CID TT fonts have identity matrix
	 * @return
	 */
	public boolean isIdentity() {
		return isIdentity;
	}

    public float[] getFontBoundingBox() {
        return new float[]{0f, 0f, 1000f, 1000f}; 
    }

    public void setFontEmbedded(boolean isSet) {
        isFontEmbedded =isSet;
    }

    public int getType() {
        return 0;  //To change body of created methods use File | Settings | File Templates.
    }

    public void setHasWidths(boolean hasWidths) {
        this.hasWidths=hasWidths;
    }

    public void setDiffValues(String[] diffTable) {
    }
}
