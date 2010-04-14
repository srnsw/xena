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
* GenericStreamDecoder.java
* ---------------
*/
package org.jfr.parser;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.jpedal.PdfDecoder;
import org.jpedal.color.PdfPaint;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.color.DeviceRGBColorSpace;
import org.jpedal.fonts.PdfFont;
import org.jpedal.fonts.StandardFonts;
import org.jpedal.fonts.TrueType;
import org.jpedal.fonts.glyph.PdfGlyph;
import org.jpedal.fonts.glyph.PdfJavaGlyphs;
import org.jpedal.fonts.glyph.GlyphFactory;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.TextState;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Matrix;

import org.jpedal.render.DynamicVectorRenderer;

/**holds code which is used generally and not for PDF*/
public class GenericStreamDecoder{

	static final boolean debug=false;
	
    final public static String version="0.1";

    /**current text state - updated and copied as file decode*/
    protected TextState currentTextState = new TextState();

    protected int glyphCount=0;    

    /**used to store font information from pdf and font functionality*/
    protected PdfFont currentFontData;

    protected GlyphFactory factory=null;

    /**flag to show we are drawing directly to g2 and not storing to render later*/
    protected boolean renderDirectly;

    protected DynamicVectorRenderer current;

    /**gap between characters*/
    protected float charSpacing = 0;

    /**hook onto g2 if we render directly*/
    protected Graphics2D g2;

    /**stroke colorspace*/
    protected GenericColorSpace strokeColorSpace=new DeviceRGBColorSpace();

    /**nonstroke colorspace*/
    protected GenericColorSpace nonstrokeColorSpace=new DeviceRGBColorSpace();
    
    /**current graphics state - updated and copied as file decode*/
    protected GraphicsState currentGraphicsState=new GraphicsState();
    
    /** flag to show if on mac so we can code around certain bugs */
    private static boolean isRunningOnMac = false;
    //private static boolean isRunningOnWindows = false;
    //private static boolean isRunningOnLinux = false;

    final private static  String separator = System.getProperty( "file.separator" );

	/**used to remap fonts onto truetype fonts (set internally)*/
	public static Map fontSubstitutionTable=new HashMap();

	/**used to remap fonts onto truetype fonts (set internally)*/
	public static Map fontSubstitutionLocation=new HashMap();

	/**used to remap fonts onto truetype fonts (set internally)*/
	public static Map fontSubstitutionAliasTable=new HashMap();

	/**flag to show if there must be a mapping value (program exits if none found)*/
	public static boolean enforceFontSubstitution=false;

	/**margin on page*/
	private double leftMargin,rightMargin;

    /**action to perform*/
    final private static int MEASURE=1;

    /**action to perform*/
    final private static int DRAW=2;

    /**listof fonts available on system*/
    String[] fontList=null;

    /**holds font objects*/
	private Map fontPool=new HashMap();
    private int fontSize=0;

    /**Any printer errors*/
    private String pageErrorMessages="";

    /**
	 * work out machine type so we can call OS X code to get around Java bugs.
	 */
	static {

		/**
		 * see if mac
		 */
		try {
			String name = System.getProperty("os.name");
			if (name.equals("Mac OS X"))
				isRunningOnMac = true;
//			else if(name.startsWith("Windows")){
//				isRunningOnWindows = true;
//			}else{
//				if(name.equals("Linux")){
//					isRunningOnLinux = true;
//				}
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}



	}
    public GenericStreamDecoder(DynamicVectorRenderer dvr) {

		this.current=dvr;

		/**
		 * pick up D options and use settings
         * Provide set of aliases to reference fonts
		 */
		try{
			String fontMaps=System.getProperty("org.jpedal.fontmaps");

			if(fontMaps!=null){

				StringTokenizer fontPaths=new StringTokenizer(fontMaps,",");

				while (fontPaths.hasMoreTokens()){

					String fontPath=fontPaths.nextToken();
					StringTokenizer values=new StringTokenizer(fontPath,"=:");

					int count=values.countTokens()-1;
					String nameInPDF[]=new String[count];
					String key=values.nextToken();
					for(int i=0;i<count;i++)
						nameInPDF[i]=values.nextToken();

					setSubstitutedFontAliases(key,nameInPDF); //$NON-NLS-1$

				}
			}//else
			//  throw new Exception("-Dorg.jpedal.fontmaps must be set");

		}catch(Exception e){
			LogWriter.writeLog("Unable to read FontMaps "+e.getMessage());
		}

		/**
		 * pick up D options and use settings - read actual fonts
		 */
		try{
			String fontDirs=System.getProperty("org.jpedal.fontdirs");

            if(fontDirs==null)
				addFontDirs("/Library/Fonts/,C:/win/fonts/,C:/WINDOWS/fonts/,/usr/X11R6/lib/X11/fonts/truetype/");//defaults
            else
                addFontDirs(fontDirs);

        }catch(Exception e){
			LogWriter.writeLog("Unable to read fontDirs "+e.getMessage());
		}

	}


	/**
	 * takes a comma separated list of font directories and add to substitution
	 */
	private void addFontDirs(String fontDirs) {

		StringTokenizer fontPaths=new StringTokenizer(fontDirs,",");

		while (fontPaths.hasMoreTokens()){

			String fontPath=fontPaths.nextToken();

			if(!fontPath.endsWith("/")&!fontPath.endsWith("\\"))
				fontPath=fontPath+separator;

			LogWriter.writeLog("Looking in "+fontPath+" for fonts");

			addFont(fontPath);
		}
	}


	/**
	 *add a font to available fonts
	 */
	private void addFont(String fontPath) {

		File currentDir=new File(fontPath);

		if((currentDir.exists())&&(currentDir.isDirectory())){

			String[] files=currentDir.list();

			if(files!=null){
				int count=files.length;

				for(int i=0;i<count;i++){
					String currentFont=files[i];

					if(currentFont.toLowerCase().endsWith(".ttf")){
						//see if root dir exists
						InputStream in=null;
						try {
							in = new FileInputStream(fontPath+currentFont);

						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}

						//if it does, add
						if(in!=null){

							String fontName;

							int pointer=currentFont.indexOf('.');
							if(pointer==-1)
								fontName=currentFont;
							else
								fontName=currentFont.substring(0,pointer);

							fontSubstitutionTable.put(fontName.toLowerCase(),"/TrueType");
							fontSubstitutionLocation.put(fontName.toLowerCase(),fontPath+currentFont);

							LogWriter.writeLog("Added truetype font "+fontName+" path="+fontPath+currentFont);

						}else
							LogWriter.writeLog("No fonts found at "+fontPath);
					}
				}
			}
		}
	}

	/**
	 * allows a number of fonts to be mapped onto an actual font and
	 * provides a way around slightly differing font naming when
	 * substituting fonts<p>
	 * So if arialMT existed on the target machine and the PDF contained
	 * arial and helvetica (which you wished to replace with arialmt), you would
	 * use the following code
	 * <pre>
	 * String[] aliases={"arial","helvetica"};
	 * currentPdfDecoder.setSubstitutedFontAliases("arialmt",aliases);
	 * </pre>
	 * <p>comparison is case-insensitive and file type/ending should not be
	 * included
	 * <p>
	 * For use in conjunction with -Dorg.jpedal.fontdirs options which allows user to
	 * pass a set of comma separated directories with Truetype fonts
	 * (directories do not need to exist so can be multi-platform setting)
	 * <p>
	 *
	 */
	public void setSubstitutedFontAliases(String fontFileName,String[] aliases){
		if(aliases!=null){

			int count=aliases.length;
			for(int i=0;i<count;i++){
				fontSubstitutionAliasTable.put(aliases[i].toLowerCase(),fontFileName.toLowerCase());
			}

		}
	}

	/**return width of unicode text stream*/
	public double getStringLength(String textString) {
		return ( setText(textString,MEASURE)-currentTextState.Tm[2][0]);
	}

	/**draw text onto screen*/
	public double setText(String textString) {

		return setText(textString,DRAW);
	}


	/**set current co-ordinats to matrix location - e and f are x and y*/
	public final void setTextLocationMatrix(float a,float b,float c,float d,float e, float f) {

		//set Tm matrix
		currentTextState.Tm[0][0] =a;
		currentTextState.Tm[0][1] =b;
		currentTextState.Tm[0][2] = 0;
		currentTextState.Tm[1][0] =c;
		currentTextState.Tm[1][1] =d;
		currentTextState.Tm[1][2] = 0;
		currentTextState.Tm[2][0] =e;
		currentTextState.Tm[2][1] =f;
		currentTextState.Tm[2][2] = 1;

        /**
         * rest of method just added for compatability
         */
        currentTextState.setLeading(1);

		//keep position in case we need
		currentTextState.setTMAtLineStart();

	}

    /**
     * renders the enclosed unicode text stream onto rasterizer at current screen co-ordinates or works out width
     */
	private float setText(String textString,int command) {

		/**reset global variables and initialise local ones*/
		float[][] Trm = new float[3][3];
		float[][] temp = new float[3][3];
		char rawChar = ' ';
		int fontSize = 0, rawInt = 0;
		float currentWidth = 0;
		String displayValue = "";
		float TFS = currentTextState.getTfs();
		charSpacing = currentTextState.getCharacterSpacing() / TFS;
		int type=currentFontData.getFontType();

		boolean isCID = currentFontData.isCIDFont();

		PdfJavaGlyphs glyphs = currentFontData.getGlyphData();

        /**set colors*/
		currentGraphicsState.setStrokeColor(strokeColorSpace.getColor());
		currentGraphicsState.setNonstrokeColor(nonstrokeColorSpace.getColor());

		/** create temp matrix for current text location and factor in scaling*/
		Trm = Matrix.multiply(currentTextState.Tm, currentGraphicsState.CTM);
		Trm[0][0]=Trm[0][0];
		Trm[0][1]=Trm[0][1];
		Trm[1][0]=Trm[1][0];
		Trm[1][1]=Trm[1][1];

		/**define matrix used for converting to correctly scaled matrix and multiply to set Trm*/
		temp[0][0] = TFS * currentTextState.getHorizontalScaling();
		temp[1][1] = TFS;
		temp[2][1] = currentTextState.getTextRise();
		temp[2][2] =1;
		Trm = Matrix.multiply(temp, Trm);

        /**workout if horizontal or vertical plot and set values*/
		if (Trm[1][1] != 0) {
			fontSize = Math.round(Trm[1][1] );

			if(fontSize==0)
				fontSize = Math.round(Trm[0][1] );
		} else {

			fontSize = Math.round(Trm[1][0] );

			if(fontSize==0)
				fontSize = Math.round(Trm[0][0] );

			if(fontSize<0){
				fontSize=-fontSize;

			}
		}

        if(fontSize==0)
			fontSize=1;

        /**extract starting x and y values (we update Trm as we work through text)*/
		float x = Trm[2][0];
		float y = Trm[2][1];

		/**
		 * now work through all glyphs and render/decode
		 */
		int i = 0;
		int dataPointer=textString.length();

		int numOfPrefixes=0;
		while (i < dataPointer) {

			//extract the nextchar
			rawChar = textString.charAt(i);
			rawInt=rawChar;
			displayValue = String.valueOf(rawChar);

			//MOVE pointer to next location by updating matrix
			temp[0][0] = 1;
			temp[0][1] = 0;
			temp[0][2] = 0;
			temp[1][0] = 0;
			temp[1][1] = 1;
			temp[1][2] = 0;
			temp[2][0] = (currentWidth); //tx;
			temp[2][1] = 0; //ty;
			temp[2][2] = 1;

			Trm = Matrix.multiply(temp, Trm); //multiply to get new Tm

            //force line return onto next line
			if((command==DRAW)&&(Trm[2][0]>rightMargin)){
				relativeMove(0, -currentTextState.getLeading()*TFS);
				Trm[2][0]=(float) leftMargin;
				Trm[2][1]=currentTextState.Tm[2][1];
			}

			//get glyph if not CID
			String charGlyph="notdef";

            //I have no idea why it needs dividing by 2 but it works
            currentWidth = currentFontData.getGlyphWidth( charGlyph, rawInt,  displayValue);

////if(rawInt==121)
           // System.out.println("=========="+" "+rawInt+" "+displayValue+" "+currentFontData+" "+currentFontData.getFontName()+" width="+currentWidth);

            if(!isCID){
				if(rawInt>255) //hack for unicode values
					rawChar=' ';
				else
					charGlyph=currentFontData.getMappedChar(rawInt,false);
			}


			/**if we have a valid character and we are rendering, draw it */
			if((command!=MEASURE)&&(rawChar!=' ')){

				if((currentFontData.isFontEmbedded)){

                    PdfGlyph glyph= glyphs.getEmbeddedGlyph( factory,charGlyph ,Trm, rawInt, displayValue, currentWidth,null);
					try{


						if(glyph!=null){

							//create shape for text using tranformation to make correct size
							AffineTransform at =new AffineTransform(Trm[0][0],Trm[0][1],Trm[1][0],Trm[1][1] ,Trm[2][0],Trm[2][1]-this.fontSize);

                            at.scale(currentFontData.FontMatrix[0],currentFontData.FontMatrix[3]);

                            //add to renderer
							int fontType=DynamicVectorRenderer.TRUETYPE;

                            if(renderDirectly){

								PdfPaint currentCol=null,textFillCol=null;
								int text_fill_type = currentGraphicsState.getTextRenderType();

								//for a fill
								if ((text_fill_type & GraphicsState.FILL) == GraphicsState.FILL)

									textFillCol=currentGraphicsState.getNonstrokeColor();

								//and/or do a stroke
								if ((text_fill_type & GraphicsState.STROKE) == GraphicsState.STROKE)
									currentCol=currentGraphicsState.getStrokeColor();

								current.renderEmbeddedText(currentGraphicsState,text_fill_type,glyph,fontType,g2,at,null,currentCol,textFillCol,
										currentGraphicsState.getStrokeAlpha(),currentGraphicsState.getNonStrokeAlpha(),null,0) ;
							}else
								current.drawEmbeddedText( Trm,fontSize,glyph,null,fontType,currentGraphicsState,at);

						}

					} catch (Exception e) {

						addPageFailureMessage("Exception "+e+" on embedded font renderer");


					}

				}else if((displayValue.length() > 0) && (!displayValue.startsWith("&#"))){

					/**set values used if rendering as well*/
					boolean isSTD=PdfDecoder.isRunningOnMac ||(StandardFonts.isStandardFont(glyphs.getBaseFontName(),false));
					Area transformedGlyph2= glyphs.getStandardGlyph(Trm, rawInt, displayValue, currentWidth, isSTD);
					
					if(transformedGlyph2!=null){
						//if its already generated we just need to move it
						//AffineTransform at2 =AffineTransform.getTranslateInstance(Trm[2][0],(Trm[2][1]));
						//transformedGlyph2.transform(at2);

						//add to renderer
						if(renderDirectly){
							PdfPaint currentCol=null,fillCol=null;
							int text_fill_type = currentGraphicsState.getTextRenderType();

							//for a fill
							if ((text_fill_type & GraphicsState.FILL) == GraphicsState.FILL)

								fillCol=currentGraphicsState.getNonstrokeColor();

							//and/or do a stroke
							if ((text_fill_type & GraphicsState.STROKE) == GraphicsState.STROKE)
								currentCol=currentGraphicsState.getStrokeColor();

                            AffineTransform def=g2.getTransform();
                            
                            if(DynamicVectorRenderer.marksNewCode)
			                    g2.translate(Trm[2][0], Trm[2][1]);

                            if(DynamicVectorRenderer.newCode2)
                                g2.scale(Trm[0][0],-Trm[1][1]);



							current.renderText(Trm[2][0],Trm[2][1], text_fill_type,transformedGlyph2,g2,null,currentCol,fillCol,
									currentGraphicsState.getStrokeAlpha(),currentGraphicsState.getNonStrokeAlpha());

                            g2.setTransform(def);

                        }else
							current.drawEmbeddedText(Trm,fontSize,null,transformedGlyph2,DynamicVectorRenderer.TEXT,currentGraphicsState,null);

					}
				}
			}

			/**now we have plotted it we update pointers*/
			currentWidth = currentWidth + charSpacing;

            i++;
		}

        /**all text is now drawn (if required) and text has been decoded*/

		//final move to get end of shape
		temp[0][0] = 1;
		temp[0][1] = 0;
		temp[0][2] = 0;
		temp[1][0] = 0;
		temp[1][1] = 1;
		temp[1][2] = 0;
		temp[2][0] = (currentWidth); //tx;
		temp[2][1] = 0; //ty;
		temp[2][2] = 1;
		Trm = Matrix.multiply(temp, Trm); //multiply to get new Tm

        //update Tm to cursor
		if(command!=MEASURE){
			currentTextState.Tm[2][0] = Trm[2][0];
			currentTextState.Tm[2][1] = Trm[2][1];
		}

		return Trm[2][0];
	}

	/**setup font name and size. Name is case insensitive.
	 * Font is cached once loaded
	 * @throws Exception */
	public void setFont(String fontName,int fontSize) throws Exception {

		
		fontName=fontName.toLowerCase();

		currentTextState.setFontTfs(fontSize);

		
		/**see if font loaded*/
		String fontPath=(String) fontSubstitutionLocation.get(fontName);

		if(fontPath==null){
			throw new Exception ("Font "+fontName+" does not have a truetype font available");
		}else{

			//load if cached
			currentFontData=(PdfFont) fontPool.get(fontName);

			//first use so we need to setup
			if(currentFontData==null){

                currentFontData=readFont(fontName);
				fontPool.put(fontName,currentFontData);

			}
		}

        this.fontSize=fontSize;
	}

	private PdfFont readFont(String fontName) {

		LogWriter.writeMethod("{readFonts}", 0);

		String subFont=null;

		PdfFont currentFontData=null;

		if(debug)
			System.out.println("Font name="+fontName);
		
		/**
		 * make sure it is a font
		 */

		/**handle any font remapping*/
		if((fontSubstitutionTable!=null))
			subFont=(String) fontSubstitutionLocation.get(fontName);

		if(debug)
			System.out.println("subfont="+subFont);
		
		try{

			currentFontData=new TrueType(subFont);
			currentFontData.createFont(fontName);

           // System.out.println("Read font "+fontName+" "+currentFontData);
		}catch(Exception e){
			LogWriter.writeLog("[PDF] Problem "+e+" reading Font  type "+subFont);
			addPageFailureMessage("Problem "+e+" reading Font  type "+subFont);
		}


		return currentFontData;
	}

	/**
	 * used in generic decoder to move down lineCount lines
	 */
	public  void lineDown(int lineCount) {
		relativeMove(0, -currentTextState.getLeading()*currentTextState.getTfs()*lineCount);

	}
	
	/**
	 * used in generic decoder to move cursor 1 tab length
	 */
	public  void tab() {
		setCurrentXpt(getCurrentXpt()+50);
		
	}

	/**
	 *
	 *set color used for text
	 */
	public void setForeground(PdfPaint col) {
		nonstrokeColorSpace.setColor(col);

	}

	public void setRightMargin(double i) {

		this.rightMargin=i;

	}

	public double getRightMargin() {

		return rightMargin;

	}

	public void setLeftMargin(double i) {

		this.leftMargin=i;

	}

	public double getLeftMargin() {

		return leftMargin;

	}

	/**current x location of cursor*/
	public float getCurrentXpt() {
		return currentTextState.Tm[2][0];
	}

	/**current y location of cursor*/
	public float getCurrentYpt() {
		return currentTextState.Tm[2][1];
	}

	/**current x location of cursor*/
	public void setCurrentXpt(double x) {
		currentTextState.Tm[2][0]=(float) x;
	}

	/**current y location of cursor*/
	public void setCurrentYpt(double y) {
		currentTextState.Tm[2][1]=(float) y;
	}

	/**
	 * return names of fonts available
	 */
	public String[] getFontList() {

		if(fontList==null){
			Object[] fonts= fontSubstitutionLocation.keySet().toArray();  //To change body of created methods use File | Settings | File Templates.

			System.out.println(fontSubstitutionLocation);
			int count=fonts.length;

			fontList=new String[count];

			for(int i=0;i<count;i++)
				fontList[i]=fonts[i].toString();
		}

		return fontList;
	}

    /**
     * used by TD and T* to move current co-ord
     */
    protected final void relativeMove(float new_x, float new_y) {

        //create matrix to update Tm
        float[][] temp = new float[3][3];

        currentTextState.Tm = currentTextState.getTMAtLineStart();

        //set Tm matrix
        temp[0][0] = 1;
        temp[0][1] = 0;
        temp[0][2] = 0;
        temp[1][0] = 0;
        temp[1][1] = 1;
        temp[1][2] = 0;
        temp[2][0] = new_x;
        temp[2][1] = new_y;
        temp[2][2] = 1;

        //multiply to get new Tm
        currentTextState.Tm = Matrix.multiply(temp, currentTextState.Tm);

        currentTextState.setTMAtLineStart();

    }

    /**
     * add message on printer problem
     */
    public void addPageFailureMessage(String value) {
        pageErrorMessages=pageErrorMessages+value+ '\n';
    }


}
