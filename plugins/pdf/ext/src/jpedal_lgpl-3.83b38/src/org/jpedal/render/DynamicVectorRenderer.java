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
 * DynamicVectorRenderer.java
 * ---------------
 */
package org.jpedal.render;

import org.jpedal.Display;
import org.jpedal.PdfDecoder;
import org.jpedal.color.ColorSpaces;
import org.jpedal.color.PdfColor;
import org.jpedal.color.PdfPaint;
import org.jpedal.constants.PDFImageProcessing;
import org.jpedal.exception.PdfException;
import org.jpedal.external.JPedalCustomDrawObject;
import org.jpedal.fonts.PdfFont;
import org.jpedal.fonts.StandardFonts;
import org.jpedal.fonts.glyph.*;
import org.jpedal.io.ColorSpaceConvertor;
import org.jpedal.io.JAIHelper;
import org.jpedal.io.ObjectStore;
import org.jpedal.objects.GraphicsState;
import org.jpedal.parser.FontFactory;
import org.jpedal.parser.PdfStreamDecoder;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Matrix;
import org.jpedal.utils.Messages;
import org.jpedal.utils.repositories.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.io.*;
import java.util.*;



public final class DynamicVectorRenderer {

	private int pageNumber=0;

	
	/**internal flag to control how we turn images*/
	public boolean optimisedTurnCode=true;

	/**stop screen being cleared on next repaint*/
	private boolean noRepaint=false;

	/**track items painted to reduce unnecessary calls*/
	private int lastItemPainted=-1;

	private boolean extraRot = false;

	/**tell renderer to optimise calls if possible*/
	private boolean optimsePainting=false;

	//used by type3 fonts as identifier
	private String rawKey=null;

	private static final boolean samplingDebug=false;

	/**global colours if set*/
	private PdfPaint fillCol=null,strokeCol = null;

	/**used to enusre we get message once if problem*/
	private static boolean flagException=false;

	private int pageX1=9999, pageX2=-9999, pageY1=-9999, pageY2=9999;

	//flag highlights need to be generated for page
	private boolean highlightsNeedToBeGenerated=false;

	//<start-jfr>
	private org.jpedal.external.ImageHandler customImageHandler=null;
	//<end-jfr>

	/**used to cache single image*/
	BufferedImage singleImage=null;

	int imageCount=0;

	/**default array size*/
	int defaultSize=5000;

	//used to track end of PDF page in display
	int endItem=-1;

	/**flag to enable debugging of painting*/
	public static boolean debugPaint=false;

	/**use hi res images to produce better quality display*/
	private boolean useHiResImageForDisplay=false;

	/**hint for conversion ops*/
	private static RenderingHints hints = null;

	private ObjectStore objectStoreRef;

	private boolean isPrinting;

	private static  Map cachedWidths=new HashMap();

	private static Map cachedHeights=new HashMap();

	private Map fonts=new HashMap();

	private Map fontsUsed=new HashMap();

	protected GlyphFactory factory=null;

	private PdfGlyphs glyphs;

	//<start-jfr>
	private int displayMode=Display.SINGLE_PAGE;

	//<end-jfr>

	private boolean isType3Font=false;
	//static private int glyphT3Count;
	private Map imageID=new HashMap();
	private Map imageIDtoName=new HashMap();

	//used track image as file on drive
	private String currentImageFile=null;


	/**text highlights if needed*/
	private int[] textHighlightsX,textHighlightsY,textHighlightsWidth,textHighlightsHeight;
	
	/**text line areas*/
	private Rectangle[] lineAreas;
	private int[] lineWritingMode;
	
	//currently printed page
	private int printPage = -1;

	static {
		hints =
			new RenderingHints(
					RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY);



		//hints.put(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);


		hints.put(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		//RenderingHints.VALUE_ANTIALIAS_OFF);

	}

	/**create instance and set flag to show if we draw white background*/
	public DynamicVectorRenderer(int pageNumber,boolean addBackground,int defaultSize,ObjectStore newObjectRef) {

		this.pageNumber=pageNumber;
		this.objectStoreRef = newObjectRef;
		this.addBackground=addBackground;

		setupArrays(defaultSize);

	}



	/**
	 * @param defaultSize
	 */
	private void setupArrays(int defaultSize) {
		x_coord=new float[defaultSize];
		y_coord=new float[defaultSize];
		text_color=new Vector_Object(defaultSize);
		textFillType=new Vector_Int(defaultSize);
		stroke_color=new Vector_Object(defaultSize);
		fill_color=new Vector_Object(defaultSize);
		stroke=new Vector_Object(defaultSize);
		pageObjects=new Vector_Object(defaultSize);
		javaObjects=new Vector_Object(defaultSize);
		shapeType=new Vector_Int(defaultSize);
		areas=new Vector_Rectangle(defaultSize);
		//TRvalues=new Vector_Int(defaultSize);
		af1=new Vector_Double(defaultSize);
		af2=new Vector_Double(defaultSize);
		af3=new Vector_Double(defaultSize);
		af4=new Vector_Double(defaultSize);

		fontBounds=new Vector_Rectangle(defaultSize);

		clips=new Vector_Shape(defaultSize);
		objectType=new Vector_Int(defaultSize);
		//opacity=new Vector_Float(defaultSize);

	}

	public DynamicVectorRenderer(int pageNumber,ObjectStore newObjectRef,boolean isPrinting) {

		this.pageNumber=pageNumber;
		this.objectStoreRef = newObjectRef;
		this.isPrinting=isPrinting;

		setupArrays(defaultSize);

	}

	/**
	 * set optimised painting as true or false and also reset if true
	 */
	public void setOptimsePainting(boolean optimsePainting) {
		this.optimsePainting = optimsePainting;
		lastItemPainted=-1;
	}

	//<start-jfr>
	public void setDisplayView(int displayMode){
		this.displayMode=displayMode;
	}
	//<end-jfr>

	/**real size of pdf*/
	private int w=0,h=0;

	/**background color*/
	private Color backgroundColor=Color.white;

	/**store x*/
	private float[] x_coord;

	/**store y*/
	private float[] y_coord;

	/**cache for images*/
	private Map largeImages=new WeakHashMap();

	private Vector_Object text_color;
	private Vector_Object stroke_color;
	private Vector_Object fill_color;

	private Vector_Object stroke;

	/**initial Q & D object to hold data*/
	private Vector_Object pageObjects;

	private Vector_Int shapeType;

	/**holds rectangular outline to test in redraw*/
	private Vector_Rectangle areas;

	private Vector_Rectangle fontBounds;

	private Vector_Double af1;
	private Vector_Double af2;
	private Vector_Double af3;
	private Vector_Double af4;

	/**image options*/
	private Vector_Int imageOptions;

	/**TR for text*/
	private Vector_Int TRvalues;

	/**font sizes for text*/
	private Vector_Int fs;

	/**line widths if not 0*/
	private Vector_Int lw;

	/**holds rectangular outline to test in redraw*/
	private Vector_Shape clips;

	/**holds object type*/
	private Vector_Int objectType;

	/**holds object type*/
	private Vector_Object javaObjects;

	/**holds fill type*/
	private Vector_Int textFillType;

	/**holds object type*/
	private Vector_Float opacity;


	public final static int TEXT=1;
	public final static int SHAPE=2;
	public final static int IMAGE=3;
	public final static int TRUETYPE=4;
	public final static int TYPE1C=5;
	public final static int TYPE3=6;
	public final static int CLIP=7;
	public final static int COLOR=8;
	public final static int AF=9;
	public final static int TEXTCOLOR=10;
	public final static int FILLCOLOR=11;
	public final static int STROKECOLOR=12;
	public final static int STROKE=14;
	public final static int TR=15;
	public final static int STRING=16;
	public final static int STROKEOPACITY=17;
	public final static int FILLOPACITY=18;

	public final static int STROKEDSHAPE=19;
	public final static int FILLEDSHAPE=20;

	public final static int FONTSIZE=21;
	public final static int LINEWIDTH=22;

	public final static int CUSTOM=23;

	public final static int fontBB=24;

	public final static int XFORM=25;

	public final static int MARKER=200;
	public final static boolean debugStreams=false;

	/**set flag to show if we add a background*/
	private boolean addBackground=true;

	/**current item added to queue*/
	private int currentItem=0;

	//used to track col changes
	private int lastFillTextCol,lastFillCol,lastStrokeCol;

	/**used to track strokes*/
	private Stroke lastStroke=null;

	//trakc affine transform changes
	private double[] lastAf=new double[4];

	/**used to minimise TR and font changes by ignoring duplicates*/
	private int lastTR=2,lastFS=-1,lastLW=-1;

	/**ensure colors reset if text*/
	private boolean resetTextColors=true;

	private boolean fillSet=false,strokeSet=false;

	public static boolean marksNewCode=true,newCode2=false;

    //used by abacus
	public static boolean textBasedHighlight=true;
    
	public static boolean invertHighlight=false;
	
	public static void useLegacyHighlighting(boolean legacy){
		textBasedHighlight = !legacy;
	}
	
	public final void renderHighlight(Rectangle highlight, Graphics2D g2){
		
		if(highlight!=null && !ignoreHighlight){
			
			//Backup current g2 paint and composite
			Composite comp = g2.getComposite();
			Paint p = g2.getPaint();
			
			//Set new values for highlight
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,PdfDecoder.highlightComposite));
			
			if(invertHighlight){
				g2.setColor(Color.WHITE);
				g2.setXORMode(Color.BLACK);
			}else
				g2.setPaint(PdfDecoder.highlightColor);
			
			//Draw highlight
			g2.fill(highlight);
			
			//Reset to starting values
			g2.setComposite(comp);
			g2.setPaint(p);
		}
	}
	
	public final void renderText(float x, float y, int type,Area transformedGlyph2,
			Graphics2D g2, Rectangle textHighlight,PdfPaint strokePaint,
			PdfPaint textFillCol,float strokeOpacity,float fillOpacity){
		
		Paint currentCol=g2.getPaint();

		if(!textBasedHighlight){
			//add any highlight
			if(textHighlight!=null){

				Color col=Color.BLACK;
				if((type & GraphicsState.STROKE)==GraphicsState.STROKE  && strokePaint instanceof Color)
					col=(Color)strokePaint;
				else  if((type & GraphicsState.FILL)==GraphicsState.FILL  && textFillCol instanceof Color)
					col=(Color)textFillCol;

				g2.setPaint(PdfDecoder.highlightColor);
				
				g2.fill(textHighlight);

				if(PdfDecoder.backgroundColor==null)
					g2.setPaint(new Color(255-col.getRed(),255-col.getGreen(),255-col.getBlue()));
				else
					g2.setPaint(PdfDecoder.backgroundColor);

				g2.fill(transformedGlyph2);

			}else{
				//type of draw operation to use
				Composite comp=g2.getComposite();

				if((type & GraphicsState.FILL)==GraphicsState.FILL){

					if(textFillCol!=null)
						textFillCol.setScaling(cropX,cropH,scaling,x,y);

					g2.setPaint(textFillCol);


					if(fillOpacity!=1f)
						g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,fillOpacity));

					g2.fill(transformedGlyph2);

					//reset opacity
					g2.setComposite(comp);

				}

				if((type & GraphicsState.STROKE)==GraphicsState.STROKE){

					if(strokePaint!=null)
						strokePaint.setScaling(cropX+x,cropH+y,scaling,x,y);
					g2.setPaint(strokePaint);

					if(strokeOpacity!=1f)
						g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,strokeOpacity));

					g2.draw(transformedGlyph2);

					//reset opacity
					g2.setComposite(comp);
				}
			}
		}else{
				//type of draw operation to use
				Composite comp=g2.getComposite();

				if((type & GraphicsState.FILL)==GraphicsState.FILL){

					if(textFillCol!=null)
						textFillCol.setScaling(cropX,cropH,scaling,x,y);

					g2.setPaint(textFillCol);


					if(fillOpacity!=1f)
						g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,fillOpacity));
					
					if(textHighlight!=null)
						if(invertHighlight){
							Color col = g2.getColor();
							g2.setColor(new Color(255-col.getRed(),255-col.getGreen(),255-col.getBlue()));
						}else
							if(PdfDecoder.backgroundColor!=null)
								g2.setColor(PdfDecoder.backgroundColor);

					g2.fill(transformedGlyph2);

					//reset opacity
					g2.setComposite(comp);

				}
				
				if((type & GraphicsState.STROKE)==GraphicsState.STROKE){

					if(strokePaint!=null)
						strokePaint.setScaling(cropX+x,cropH+y,scaling,x,y);
					g2.setPaint(strokePaint);

					if(strokeOpacity!=1f)
						g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,strokeOpacity));
					
					if(textHighlight!=null)
						if(invertHighlight){
							Color col = g2.getColor();
							g2.setColor(new Color(255-col.getRed(),255-col.getGreen(),255-col.getBlue()));
						}else
							if(PdfDecoder.backgroundColor!=null)
								g2.setColor(PdfDecoder.backgroundColor);
					
					g2.draw(transformedGlyph2);

					//reset opacity
					g2.setComposite(comp);
				}
		}
		
		g2.setPaint(currentCol);

		//g2.setComposite(c);

		//g2.draw(transformedGlyph2.getBounds2D());

	}

	final public void renderEmbeddedText(GraphicsState gs,int text_fill_type,Object rawglyph,int glyphType,
			Graphics2D g2,AffineTransform glyphAT,Rectangle textHighlight,
			PdfPaint strokePaint,PdfPaint fillPaint,
			float strokeOpacity,float fillOpacity,
			Rectangle currentArea,int lineWidth){
		

		//ensure stroke only shows up
		float strokeOnlyLine=0;
		if(text_fill_type==GraphicsState.STROKE && lineWidth>=1.0){
			strokeOnlyLine=scaling;//0.5f+(lineWidth*scaling);
			//System.out.println("x="+strokeOnlyLine);

            //if(1==1)
              //  throw new RuntimeException("x");

			//System.out.println(scaling+" "+lineWidth+" "+g2.getTransform().getScaleX());
			//if(strokeOnlyLine==0 && scaling>1){// && strokePaint.getRGB()!=fillPaint.getRGB()){
				//strokeOnlyLine=scaling;
			//System.out.println("scaling="+scaling+" "+strokePaint.getRGB()+" "+fillPaint.getRGB());
			//}
		}

		boolean renderDirect=(gs!=null);
        
		//get glyph to draw
		PdfGlyph glyph=FontFactory.chooseGlyph(glyphType,rawglyph);

		//track objects
		if(renderDirect){
			int fontSize=(int)gs.CTM[1][1];
			if(fontSize<0)
				fontSize=-fontSize;

			if(fontSize==0)
				fontSize=(int)gs.CTM[0][1];
			if(fontSize<0)
				fontSize=-fontSize;

			
//			if(gs.CTM[2][0]==0 && gs.CTM[2][1]==0 && glyphAT.getTranslateX()!=0 && glyphAT.getTranslateY()!=0 )
//				areas.addElement(new Rectangle((int)glyphAT.getTranslateX(),(int)glyphAT.getTranslateY(),fontSize,fontSize));
//			else
				areas.addElement(new Rectangle((int)gs.CTM[2][0],(int)gs.CTM[2][1],fontSize,fontSize));
		}

		AffineTransform at=g2.getTransform();

		//and also as flat values so we can test below
		double[] affValues=new double[6];
		at.getMatrix(affValues);

		if(glyph!=null){

            //avoid possible error if shaded
            //if(glyph.getShape()!=null && glyph.getShape().getBounds().width==0 && glyph.getShape().getBounds().height==0)
				//	return;
            
			Stroke currentStoke=g2.getStroke();

			if(lineWidth!=0)
				g2.setStroke(new BasicStroke(lineWidth));

			// set the highlight
			Color currentCol=null;

			if((strokePaint!=null) &&(strokePaint instanceof Color))
				currentCol=(Color) strokePaint;
			else if((strokePaint==null) &&(fillPaint instanceof Color))
				currentCol=(Color) fillPaint;

			Color altCol=null;

			//set transform
			g2.transform(glyphAT);

			//type of draw operation to use
			Composite comp=g2.getComposite();

			Color col=null;

			if(textBasedHighlight){
		
				/**
				 * Fill Text
				 */
				if ((text_fill_type & GraphicsState.FILL) == GraphicsState.FILL){
					
					fillPaint.setScaling(cropX,cropH,scaling, 0,0);
					g2.setPaint(fillPaint);

					if(fillOpacity!=1f)
						g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC,fillOpacity));

					if(textHighlight!=null)
						if(invertHighlight){
							Color color = g2.getColor();
							g2.setColor(new Color(255-color.getRed(),255-color.getGreen(),255-color.getBlue()));
						}else
							if(PdfDecoder.backgroundColor!=null)
								g2.setColor(PdfDecoder.backgroundColor);

                    glyph.render(GraphicsState.FILL,g2, scaling);
					
					//reset opacity
					g2.setComposite(comp);

				}

				
				/**
				 * Stroke Text (Can be fill and stroke so not in else)
				 */
                if (text_fill_type == GraphicsState.STROKE)
				glyph.setStrokedOnly(true);

				//creates shadow printing to Mac so added work around
				if(PdfDecoder.isRunningOnMac && isPrinting && text_fill_type==GraphicsState.FILLSTROKE){
				}else 
				if ((text_fill_type & GraphicsState.STROKE) == GraphicsState.STROKE){

					if(strokePaint!=null)
						strokePaint.setScaling(cropX,cropH,scaling,0,0);
					g2.setPaint(strokePaint);

					if(strokeOpacity!=1f)
						g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,strokeOpacity));

					if(textHighlight!=null)
						if(invertHighlight){
							Color color = g2.getColor();
							g2.setColor(new Color(255-color.getRed(),255-color.getGreen(),255-color.getBlue()));
						}else
							if(PdfDecoder.backgroundColor!=null)
								g2.setColor(PdfDecoder.backgroundColor);
					
					try{
						glyph.render(GraphicsState.STROKE,g2, strokeOnlyLine);
					}catch(Exception e){
						System.out.println("Exception "+e+" rendering glyph");
						e.printStackTrace();
					}
					
					//reset opacity
					g2.setComposite(comp);
				}

				//restore transform
				g2.setTransform(at);

				if(lineWidth!=0)
					g2.setStroke(currentStoke);
				
				//g2.draw(transformedGlyph2.getBounds2D());
				
			}else{
				
				if(textHighlight!=null){
					
					if ((text_fill_type & GraphicsState.FILL) == GraphicsState.FILL && fillPaint instanceof Color)
						col=(Color)fillPaint;
					else if (strokePaint instanceof Color)
						col=(Color)strokePaint;
					else//invert if nothing else available
						col=PdfDecoder.highlightColor;
					
					//g2.setPaint(highlightColor);
					//g2.setPaint(glyfPaint);

					g2.setPaint(PdfDecoder.highlightColor);

					g2.fill(textHighlight);

					if(PdfDecoder.backgroundColor==null)
						g2.setPaint(new Color(255-col.getRed(),255-col.getGreen(),255-col.getBlue()));
					else
						g2.setPaint(PdfDecoder.backgroundColor);
					//g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.5f));

				}
				
				//for a fill
				if ((text_fill_type & GraphicsState.FILL) == GraphicsState.FILL){
					fillPaint.setScaling(cropX,cropH,scaling, 0,0);

					if(textHighlight==null)
						g2.setPaint(fillPaint);


					if(fillOpacity!=1f)
						g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC,fillOpacity));


					glyph.render(GraphicsState.FILL,g2, scaling);
					//reset opacity
					g2.setComposite(comp);
					
				}//else

				//and/or do a stroke
				if ((text_fill_type & GraphicsState.STROKE) == GraphicsState.STROKE){

					if(strokePaint!=null)
						strokePaint.setScaling(cropX,cropH,scaling,0,0);
					if(textHighlight==null)
						g2.setPaint(strokePaint);

					if(strokeOpacity!=1f)
						g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,strokeOpacity));

					try{
						glyph.render(GraphicsState.STROKE,g2, strokeOnlyLine);
					}catch(Exception e){
						System.out.println("Exception "+e+" rendering glyph");
						e.printStackTrace();
					}
					//reset opacity
					g2.setComposite(comp);
				}

				//restore transform
				g2.setTransform(at);

				if(textHighlight!=null)
					g2.setPaint(col);

				if(lineWidth!=0)
					g2.setStroke(currentStoke);

			}
		}
	}

	public final void renderImage(AffineTransform imageAf, BufferedImage image,float alpha,
			GraphicsState currentGraphicsState,Graphics2D g2,float x,float y, 
			int optionsApplied){


		boolean renderDirect=(currentGraphicsState!=null);

		if(image==null)
			return;

		int w = image.getWidth();
		int h = image.getHeight();

		//track objects
		if(renderDirect){
			int iw=(int)currentGraphicsState.CTM[0][0];
			if(iw<0)
				iw=-iw;

			if(iw==0)
				iw=(int)currentGraphicsState.CTM[0][1];
			if(iw<0)
				iw=-iw;


			int ih=(int)currentGraphicsState.CTM[1][1];
			if(ih<0)
				ih=-ih;

			if(ih==0)
				ih=(int)currentGraphicsState.CTM[1][0];
			if(ih<0)
				ih=-ih;


			areas.addElement(new Rectangle((int)currentGraphicsState.CTM[2][0], (int)currentGraphicsState.CTM[2][1],iw,ih));

		}

		//plot image (needs to be flipped as well as co-ords upside down)
		//graphics less than 1 get swallowed if flipped
		AffineTransform upside_down = new AffineTransform();

		boolean applyTransform=false;

		float CTM[][]=new float[3][3];
		if(currentGraphicsState!=null)
			CTM=currentGraphicsState.CTM;

		AffineTransform before=g2.getTransform();


		boolean invertInAff=false;

		float dx=0,dy=0;

		/**
		 * setup for printing
		 */
		if(renderDirect || useHiResImageForDisplay){

			if(renderDirect){

				upside_down=null;

				//System.out.println(CTM[0][0]+" "+CTM[0][1]+" "+CTM[1][0]+" "+CTM[1][1]+" "+CTM[2][0]+" "+CTM[2][1]);

				//Turn image around if needed (ie JPEG not yet turned)
				if((optionsApplied & PDFImageProcessing.IMAGE_INVERTED) !=PDFImageProcessing.IMAGE_INVERTED){

					if(!optimisedTurnCode)
						image = invertImage(CTM, image);
                    else{


                        if((CTM[0][1]<0 && CTM[1][0]>0) && (CTM[0][0]* CTM[1][1]==0)){

                            upside_down=new AffineTransform(CTM[0][0]/w,CTM[0][1]/w,-CTM[1][0]/h,CTM[1][1]/h,CTM[2][0]+CTM[1][0],CTM[2][1]);


                        }else if((CTM[0][1]<0 || CTM[1][0]<0)){

                        	
							float[][] flip2={{1f/w,0,0},{0,-1f/h,0},{0,1f/h,1}};
							float[][] rot={{CTM[0][0],
								CTM[0][1],0},
								{CTM[1][0],CTM[1][1],0},
								{0,0,1}};

							flip2=Matrix.multiply(flip2,rot);
							upside_down=new AffineTransform(flip2[0][0], flip2[0][1], flip2[1][0],
                                    flip2[1][1], flip2[2][0], flip2[2][1]);


							dx=(float) (CTM[2][0]-image.getHeight()*flip2[1][0]);
							dy=(float) (CTM[2][1]);

							//System.out.println("x="+CTM[0][0]+" "+CTM[1][0]+" "+CTM[2][0]);
							//System.out.println("y="+CTM[0][1]+" "+CTM[1][1]+" "+CTM[2][1]);
							
							//special case
							if(CTM[0][0]<0 && CTM[1][0]<0 && CTM[0][1]>0 && CTM[1][1]<0){
							}else if(CTM[1][1]!=0)
								dy=dy+CTM[1][1];

						}else if((CTM[0][0]*CTM[0][1]==0 && CTM[1][1]*CTM[1][0]==0) && (CTM[0][1]>0 && CTM[1][0]>0)){
                            float[][] flip2={{-1f/w,0,0},{0,1f/h,0},{1f/w,0,1}};
							float[][] rot={{CTM[0][0],
								CTM[0][1],0},
								{CTM[1][0],CTM[1][1],0},
								{0,0,1}};

							flip2=Matrix.multiply(flip2,rot);
							upside_down=new AffineTransform(
									flip2[0][0],
									flip2[1][0],
									flip2[0][1],flip2[1][1],
									flip2[2][0],flip2[2][1]);


							dx=(float) (CTM[2][0]-image.getHeight()*flip2[0][1]);
							dy=(float) (CTM[2][1]);//+image.getWidth()*flip2[1][0]);



						}else if(CTM[1][1]!=0)
							invertInAff=true;
					}
				}

				if(upside_down==null)
					upside_down=new AffineTransform(CTM[0][0]/w,CTM[0][1]/w,
							CTM[1][0]/h,CTM[1][1]/h,CTM[2][0],CTM[2][1]);
			}else{
				upside_down=imageAf;

				invertInAff=((optionsApplied & PDFImageProcessing.TURN_ON_DRAW) ==PDFImageProcessing.TURN_ON_DRAW);

			}

			applyTransform=true;


		}else if (h > 1) {
			//upside_down.scale(1, -1);
			//upside_down.translate(0, -h);

			//<start-jfr>
			float dpi = PdfDecoder.dpi;
			if(dpi != 72){
				upside_down.scale(72 / dpi , 72 / dpi);
				upside_down.translate(0,h * ((dpi / 72) - 1));

				applyTransform=true;
			}
			//<end-jfr>

		}

		Composite c=g2.getComposite();

		Shape clip=g2.getClip();

		//<start-jfr>
		//bg_holiday and some Times bugs
		//if((PdfDecoder.isRunningOnMac)&&(clip!=null))
		//g2.setClip(null);
		//<end-jfr>

		if(alpha!=1.0f && PdfStreamDecoder.newForms)
		 g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha));

		/**
		 * color type3 glyphs if not black
		 */
		if(colorsLocked){

			int[] maskCol =new int[4];
			int foreground =this.fillCol.getRGB();
			maskCol[0]=((foreground>>16) & 0xFF);
			maskCol[1]=((foreground>>8) & 0xFF);
			maskCol[2]= ((foreground) & 0xFF);
			maskCol[3]=255;

			if(maskCol[0]==0 && maskCol[1]==0 && maskCol[2]==0){
				//System.out.println("black");
			}else{
				BufferedImage img=new BufferedImage(image.getWidth(),image.getHeight(),image.getType());

				Raster src=image.getRaster();
				WritableRaster dest=img.getRaster();
				for(int yy=0;yy<image.getHeight();yy++){
					for(int xx=0;xx<image.getWidth();xx++){

						int[] values=new int[4];

						//get raw color data
						src.getPixel(xx,yy,values);

						//if not transparent, fill with color
						if(values[3]>2)
							dest.setPixel(xx,yy,maskCol);

					}
				}
				image=img;
			}
		}

		if(renderDirect || useHiResImageForDisplay){

			try{

				if(optimisedTurnCode && (invertInAff && (optionsApplied & PDFImageProcessing.IMAGE_INVERTED) !=PDFImageProcessing.IMAGE_INVERTED)){

					double[] values=new double[6];
					upside_down.getMatrix(values);

					dx=(float)values[4];
					dy=(float)(values[5]+(image.getHeight()*values[3]));

					values[3]=-values[3];

					values[4]=0;
					values[5]=0;

					upside_down=new AffineTransform(values);

				}

				//<start-jfr>
				//allow user to over-ride
				boolean useCustomRenderer =customImageHandler!=null;

				if(useCustomRenderer)
					useCustomRenderer=customImageHandler.drawImageOnscreen(image,optionsApplied,upside_down,currentImageFile,g2, renderDirect, objectStoreRef, isPrinting);

				//exit if done
				if(useCustomRenderer){
					g2.setComposite(c);
					return ;
				}
				//<end-jfr>

                boolean marksTest=false;
                if(marksTest){

                    AffineTransform aff=g2.getTransform();
                    double[] g2_aff=new double[6];
                    aff.getMatrix(g2_aff);

                    double[] upside_down_aff=new double[6];
                    upside_down.getMatrix(upside_down_aff);

                    g2_aff[0]=g2_aff[0]*upside_down_aff[0];
                    g2_aff[3]=g2_aff[3]*upside_down_aff[3];

                    //g2_aff[3]=-g2_aff[3]*upside_down_aff[3];
                    ///System.out.println(g2.getTransform());
                    g2.setTransform(new AffineTransform(g2_aff));
                    //javax.imageio.ImageIO.write(image,"png", new File("/Users/markee/Desktop/xx.png"));
                    //System.out.println(upside_down);
                    //System.out.println(g2.getTransform());
                    g2.translate(dx/upside_down_aff[0], dy/upside_down_aff[3]);
                    //g2.drawImage(image,upside_down,null);

                    upside_down_aff[0]=1f;
                    upside_down_aff[3]=1f;

                    //System.out.println("final="+g2.getTransform());
                    g2.drawImage(image,0,0,null);
                    //g2.drawImage(image,new AffineTransform(new double[]{upside_down_aff[0],upside_down_aff[1],upside_down_aff[2],upside_down_aff[3],upside_down_aff[4],upside_down_aff[5]}),null);

                }else{
                    g2.translate(dx, dy);
				    g2.drawImage(image,upside_down,null);
                }
				/**/

			}catch(Exception e){
			}

		}else{


			try{

				//System.out.println(applyTransform+" "+image);

				//System.out.println(optionsApplied+" "+g2.getTransform());

				//org.jpedal.gui.ShowGUIMessage.showGUIMessage("x",image,"x");
				if(applyTransform){
					AffineTransformOp invert =new AffineTransformOp(upside_down,ColorSpaces.hints);
					image=invert.filter(image,null);
				}

				g2.translate(x,y);


				if(optimisedTurnCode && (optionsApplied & PDFImageProcessing.TURN_ON_DRAW) ==PDFImageProcessing.TURN_ON_DRAW){

					AffineTransform flip2=new AffineTransform();
					float[] flip=new float[]{1f,0f,0f,-1f,0f,(float)image.getHeight()};
					AffineTransform flip3=new AffineTransform(flip);
					if(rotation==0){

						flip2=new AffineTransform(flip);

					}else if(rotation==90){	


						flip2=new AffineTransform();	

						if(extraRot){
							flip2.rotate(Math.PI, 0, 0);
						}else{
							flip2.rotate(Math.PI/2, 0, 0);
						}
						flip2.translate(-image.getWidth(),-image.getHeight());

						flip=new float[]{-1f,0f,0f,1f,image.getWidth(),0};//(float)image.getHeight()};
						flip3=new AffineTransform(flip);

						flip2.concatenate(flip3);


					}else if(rotation==180){

						flip2=new AffineTransform();
						if(extraRot){
							flip2.rotate(Math.PI, 0, 0);
						}
						flip2.translate(-image.getWidth(), -image.getHeight());

						flip=new float[]{-1f,0f,0f,1f,image.getWidth(),0};//(float)image.getHeight()};

						flip3=new AffineTransform(flip);

						flip2.concatenate(flip3);

					}else{

						flip2=new AffineTransform();
						if(extraRot){
							flip2.rotate(Math.PI, 0, 0);
						}else{
							flip2.rotate(Math.PI/2+Math.PI, 0, 0);
						}
						flip2.translate(-image.getWidth(),-image.getHeight());

						flip=new float[]{-1f,0f,0f,1f,image.getWidth(),0};//(float)image.getHeight()};

						flip3=new AffineTransform(flip);

						flip2.concatenate(flip3);

					}

					g2.drawImage(image,flip2,null);

				}else{

					g2.drawImage(image,0,0,null);
				}

				g2.translate(-x,-y);

			}catch(Exception ee){
			}catch(Error e){
				e.printStackTrace();
				System.gc();
			}
		}

		g2.setTransform(before);

		//<start-jfr>
		if((PdfDecoder.isRunningOnMac)&&(clip!=null))
			g2.setClip(clip);
		//<end-jfr>

		g2.setComposite(c);


	}



	public final void renderShape(Shape defaultClip, int fillType,PdfPaint strokeCol,PdfPaint fillCol,
			Stroke shapeStroke,Shape currentShape,Graphics2D g2,float strokeOpacity,
			float fillOpacity,boolean renderDirect) {

        Composite comp=g2.getComposite();
        
        if(renderDirect)
        	areas.addElement(currentShape.getBounds());


		//stroke and fill (do fill first so we don't overwrite Stroke)
		if (fillType == GraphicsState.FILL || fillType == GraphicsState.FILLSTROKE) {

			fillCol.setScaling(cropX,cropH,scaling,0,0);
			g2.setPaint(fillCol);

			if(fillOpacity!=1f)
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,fillOpacity));

			g2.fill(currentShape);

//			reset opacity
			g2.setComposite(comp);

//			System.out.println(fillCol);
//			System.out.println(currentShape.getBounds());
//			if(currentShape.getBounds().getY()==673)


		}

		if ((fillType == GraphicsState.STROKE) || (fillType == GraphicsState.FILLSTROKE)) {

			//set values for drawing the shape
			Stroke currentStroke=g2.getStroke();

			//fix for using large width on point to draw line
			if(currentShape.getBounds2D().getWidth()<1.0f && ((BasicStroke)shapeStroke).getLineWidth()>10)
				g2.setStroke(new BasicStroke(1));
			else
				g2.setStroke(shapeStroke); //set stroke pattern

			strokeCol.setScaling(cropX,cropH,scaling,0,0);
			g2.setPaint(strokeCol);

			if(strokeOpacity!=1f)
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,strokeOpacity));

			Shape clip=g2.getClip();
			if(clip!=null && (clip.getBounds2D().getHeight()<1 || clip.getBounds2D().getWidth()<1))
				g2.setClip(defaultClip);  //use null or visible screen area

			g2.draw(currentShape);

			g2.setClip(clip);
			g2.setStroke(currentStroke);

//			reset opacity
			g2.setComposite(comp);
		}

	}

	/* remove all page objects and flush queue */
	public void flush() {

		singleImage=null;

		imageCount=0;

		lastFS = -1;

		if(shapeType!=null){

			shapeType.clear();
			pageObjects.clear();
			objectType.clear();
			areas.clear();
			clips.clear();
			x_coord=new float[defaultSize];
			y_coord=new float[defaultSize];
			textFillType.clear();
			text_color.clear();
			fill_color.clear();
			stroke_color.clear();
			stroke.clear();

			if(TRvalues!=null)
				TRvalues=null;

			if(imageOptions!=null)
				imageOptions=null;

			if(fs!=null)
				fs=null;

			if(lw!=null)
				lw=null;

			af1.clear();
			af2.clear();
			af3.clear();
			af4.clear();

			fontBounds.clear();

			if(opacity!=null)
				opacity=null;

			if(isPrinting)
				largeImages.clear();


			endItem=-1;
		}

		//pointer we use to flag color change
		lastFillTextCol=0;
		lastFillCol=0;
		lastStrokeCol=0;

		lastClip=null;
		hasClips=false;

		//track strokes
		lastStroke=null;

		lastAf=new double[4];

		currentItem=0;

		fillSet=false;
		strokeSet=false;

		fonts.clear();
		fontsUsed.clear();

		imageID.clear();

		pageX1=9999;
		pageX2=-9999;
		pageY1=-9999;
		pageY2=9999;

		lastScaling=0;

	}

	/* remove all page objects and flush queue */
	public void dispose() {

		singleImage=null;

		shapeType=null;
		pageObjects=null;
		objectType=null;
		areas=null;
		clips=null;
		x_coord=null;
		y_coord=null;
		textFillType=null;
		text_color=null;
		fill_color=null;
		stroke_color=null;
		stroke=null;

		TRvalues=null;

		imageOptions=null;

		fs=null;

		lw=null;

		af1=null;
		af2=null;
		af3=null;
		af4=null;

		fontBounds=null;

		opacity=null;

		largeImages=null;

		lastClip=null;
		
		lastStroke=null;

		lastAf=null;
		
		fonts=null;
		fontsUsed=null;

		imageID=null;
	}

	/**set background colour - null is transparent*/
	public void setBackgroundColor(Color background){
		if(background==null)
			this.addBackground=false;
		else
			backgroundColor=background;
	}

	int xx=0;
	int yy=0;

	private double minX=-1;

	private double minY=-1;

	private double maxX=-1;

	private double maxY=-1;

	private AffineTransform aff=new AffineTransform();

	/**raw page rotation*/
	private int rotation=0;

	/**shows if colours over-ridden for type3 font*/
	private boolean colorsLocked;

	/**flag to show if we try and optimise painting*/
	private boolean optimiseDrawing;

	private boolean renderFailed;

	/**optional frame for user to pass in - if present, error warning will be displayed*/
	private Container frame=null;

	/**make sure user only gets 1 error message a session*/
	private static boolean userAlerted=false;

	/**used to create images (do not use for screen)*/
	public void paint(Graphics2D g2,Rectangle[] highlights,int myx,int myy){

		if(myx<0)
			this.xx=myx;
		else
			this.xx=0;
		if(myy<0)
			this.yy=myy;
		else
			this.yy=0;


		paint(g2,highlights,null,null,false,false);
	}

	public void setInset(int x,int y){
		xx=x;
		yy=y;

	}


	private int paintThreadCount=0;
	private int paintThreadID=0;

	/**
	 * For IDR internal use only
	 */
	private boolean[] drawnHighlights;
	
	/*renders all the objects onto the g2 surface*/
	public Rectangle paint(Graphics2D g2,Rectangle[] highlights,AffineTransform viewScaling,
			Rectangle userAnnot,boolean drawJustHighlights,boolean isScreen){
			
		//Vector_Rectangle highlightsToRender = new Vector_Rectangle();
		
		//take a lock
		int currentThreadID=++paintThreadID;
		if(isScreen)
			paintThreadCount++;
		
		/**
		 * Keep track of drawn highlights so we don't draw multiple times
		 */
		if(highlights!=null){
			drawnHighlights = new boolean[highlights.length];
			for(int i=0; i!=drawnHighlights.length; i++)
				drawnHighlights[i]=false;
		}
		
		//ensure all other threads dead or this one killed
		if(isScreen && paintThreadCount>1){
			try {
				Thread.sleep(50);
				// System.out.println("sleep");
			} catch (InterruptedException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}

			if(currentThreadID!=paintThreadID){
				paintThreadCount--;
				return null;
			}
		}


		final boolean debug=false;

		int paintedCount=0;

		String fontUsed="";
		float a=0,b = 0,c=0,d=0;


		Rectangle dirtyRegion=null;

		//local copies
		int[] objectTypes=objectType.get();
		int[] textFill=textFillType.get();
		//System.err.println("ObjectTypes.length=" + objectTypes.length);
		//@kieran - you altered this to objectTypes.length to fix a bug. It needs to be
		//currentItem to make the code work - can you let me know what you think
		int count=currentItem; //DO nOT CHANGE
		Area[] pageClips=clips.get();
		double[] afValues1=af1.get();
		int[] fsValues=null;
		if(fs!=null)
			fsValues=fs.get();

		Rectangle[] fontBounds=this.fontBounds.get();

		int[] lwValues=null;
		if(lw!=null)
			lwValues=lw.get();
		//int[] TRvalues=this.TRvalues.get();
		double[] afValues2=af2.get();
		double[] afValues3=af3.get();
		double[] afValues4=af4.get();
		Object[] text_color=this.text_color.get();
		Object[] fill_color=this.fill_color.get();

		Object[] stroke_color=this.stroke_color.get();
		Object[] pageObjects=this.pageObjects.get();

		Object[] javaObjects=this.javaObjects.get();
		Object[] stroke=this.stroke.get();
		int[] fillType=this.shapeType.get();

		float[] opacity = null;
		if(this.opacity!=null)
			opacity=this.opacity.get();

		int[] TRvalues = null;
		if(this.TRvalues!=null)
			TRvalues=this.TRvalues.get();

		int[] imageOptions=null;
		if(this.imageOptions!=null)
			imageOptions=this.imageOptions.get();

		Shape rawClip=g2.getClip();
		if(rawClip!=null)
			dirtyRegion=rawClip.getBounds();//g2.getClipBounds();

		//g2.setClip(dirtyRegion);

		boolean isInitialised=false;

		Shape defaultClip=g2.getClip();

		//used to optimise clipping
		Area clipToUse=null;
		boolean newClip=false;

		/**/
		if(noRepaint)
			noRepaint=false;
		else if(!drawJustHighlights && lastItemPainted==-1)
			paintBackground(g2, dirtyRegion);/**/

		/**save raw scaling and apply any viewport*/
		AffineTransform rawScaling=g2.getTransform();
		if(viewScaling!=null){
			g2.transform(viewScaling);
			defaultClip=g2.getClip(); //not valid if viewport so disable
		}

		//reset tracking of box
		minX=-1;
		minY=-1;
		maxX=-1;
		maxY=-1;

		//set crop
		//if(currentClip!=null)
		//g2.setClip((Shape)currentClip);

		Object currentObject=null;
		int type=0,textFillType=0,currentTR=GraphicsState.FILL;
		int lineWidth=0;
		float fillOpacity=1.0f;
		float strokeOpacity=1.0f;
		float x=0,y=0;
		int iCount=0,cCount=0,sCount=0,fsCount=-1,lwCount=0,afCount=-1,tCount=0,stCount=0,
		fillCount=0,strokeCount=0,trCount=0,opCount=0,
		stringCount=0;//note af is 1 behind!
		PdfPaint textStrokeCol=null,textFillCol=null,fillCol=null,strokeCol = null;
		Stroke currentStroke=null;

		if(colorsLocked){
			strokeCol=this.strokeCol;
			fillCol=this.fillCol;
		}

		//setup first time something to highlight
		if(highlightsNeedToBeGenerated && areas!=null && highlights!=null)
			generateHighlights(g2,count, objectTypes,pageObjects, a,b,c,d,afValues1,afValues2,afValues3,afValues4,fsValues,fontBounds);


		/**
		 * now draw all shapes
		 */
		for(int i=0;i<count;i++){

        //    if(i>4800)
        //break;

			boolean ignoreItem=false;

			type=objectTypes[i];

			Rectangle currentArea=null;

			//exit if later paint recall
			if(isScreen && currentThreadID!=paintThreadID){
				paintThreadCount--;

				return null;
			}


			if(type>0){

				x=x_coord[i];
				y=y_coord[i];


				currentObject=pageObjects[i];

					/**
				 * workout area occupied by glyf
				 */
				if(currentArea!=null){
					//ignore as already done
				}else if(afValues1!=null && type==IMAGE){

					BufferedImage img=((BufferedImage)pageObjects[i]);

					if(img!=null)
						currentArea=new Rectangle((int)x,(int)y,img.getWidth(),img.getHeight());

				}else if(afValues1!=null && type==SHAPE){

					currentArea=((Shape)pageObjects[i]).getBounds();

					//System.out.println(currentArea);
				}else if(type==TEXT && afCount>-1){

					int x1=((Area)currentObject).getBounds().x;
					int y1=((Area)currentObject).getBounds().y;

					if(marksNewCode){
						//Use on page coords to make sure the glyph needs highlighting
						currentArea=getAreaForGlyph(new float[][]{{(float)afValues1[afCount],(float)afValues2[afCount],0},
								{(float)afValues3[afCount],(float)afValues4[afCount],0},
								{x,y,1}});
					}else{
						currentArea=getAreaForGlyph(new float[][]{{(float)afValues1[afCount],(float)afValues2[afCount],0},
								{(float)afValues3[afCount],(float)afValues4[afCount],0},
								{x1,y1,1}});
					}

					//currentArea=new Rectangle((int)x,(int)y,fsValues[fsCount],fsValues[fsCount]);
					//currentArea=(((Area)currentObject).getBounds());
				}else if(fsCount!=-1 && afValues1!=null){// && afCount>-1){

					int realSize=fsValues[fsCount];
					if(realSize<0) //ignore sign which is used as flag elsewhere
						currentArea=new Rectangle((int)x+realSize,(int)y,-realSize,-realSize);
					else
						currentArea=new Rectangle((int)x,(int)y,realSize,realSize);

				}

				ignoreItem=false;

				//see if we need to draw
				if(currentArea!=null){
					//Rectangle glyphArea=new Rectangle((int)x,(int) y,(int)currentArea.getWidth(),(int)currentArea.getHeight());

					//was glyphArea, changed back to currentArea to fix highlighting issue in Sams files.
					if (type < 7 && (userAnnot != null) && ((!userAnnot.intersects(currentArea)))) {
						ignoreItem = true;
					}

					//
					//System.out.println("ignoreItem="+ignoreItem);

					//@simon - objects not drawn in unaccelerated mode & highlights broken if marksNewCode enabled
				}else if((optimiseDrawing)&&(rotation==0)&&(dirtyRegion!=null)&&(type!=STROKEOPACITY)&&(type!=FILLOPACITY)&&(type!=CLIP)&&(currentArea!=null)&&
						((!dirtyRegion.intersects(currentArea))))
					ignoreItem=true;
				//ignoreItem=false;

				if(ignoreItem || (lastItemPainted!=-1 && i<lastItemPainted)){
					//keep local counts in sync
					switch (type) {

					case SHAPE:
						sCount++;
						break;
					case IMAGE:
						iCount++;
						break;
					case CLIP:
						cCount++;
						break;
					case FONTSIZE:
						fsCount++;
						break;
					case LINEWIDTH:
						lwCount++;
						break;
					case TEXTCOLOR:
						tCount++;
						break;
					case FILLCOLOR:
						fillCount++;
						break;
					case STROKECOLOR:
						strokeCount++;
						break;
					case STROKE:
						stCount++;
						break;
					case TR:
						trCount++;
						break;
					}

				}else{

					if(!isInitialised){

						//set hints to produce high quality image
						g2.setRenderingHints(hints);
						isInitialised=true;
					}

					paintedCount++;

					switch (type) {

					case SHAPE:

						if(debug)
							System.out.println("Shape");


						if(!drawJustHighlights){

							if(newClip){
								renderClip(clipToUse, dirtyRegion,defaultClip,g2);
								newClip=false;
							}

                            Shape s=null;
                            if(endItem!=-1 && endItem<i){
                                s = g2.getClip();
                                g2.setClip(defaultClip);

                            }

							renderShape(defaultClip,fillType[sCount],strokeCol,fillCol,
									currentStroke,(Shape)currentObject,g2,strokeOpacity,fillOpacity, false);

                            if(endItem!=-1 && endItem<i)
							g2.setClip(s);

						}

						sCount++;

						break;
						/**/
					case TEXT:

						if(debug)
							System.out.println("Text");

						if(newClip){
							renderClip(clipToUse, dirtyRegion,defaultClip,g2);
							newClip=false;
						}


						if(!drawJustHighlights || textHighlightsX!=null){
							Rectangle highlight = setHighlightForGlyph(currentArea,objectTypes,highlights, i);

//							if(textBasedHighlight && !ignoreHighlight){
//								highlightsToRender.addElement(highlight);
//							}

                            AffineTransform def=g2.getTransform();

                            if(marksNewCode)
			                    g2.translate(x, y);

                            if(newCode2)
                            g2.scale(afValues1[afCount],-afValues4[afCount]);

                            renderText(x,y, currentTR,(Area)currentObject,g2,
									highlight, textStrokeCol,textFillCol,strokeOpacity,fillOpacity);

                            g2.setTransform(def);
						}
						break;

					case TRUETYPE:

						if(debug)
							System.out.println("Truetype");

						if(newClip){
							renderClip(clipToUse, dirtyRegion,defaultClip,g2);
							newClip=false;
						}

						aff=new AffineTransform(afValues1[afCount],afValues2[afCount],
								afValues3[afCount],afValues4[afCount],x,y);


						if(!drawJustHighlights || textHighlightsX!=null){
							Rectangle highlight = setHighlightForGlyph(currentArea,objectTypes,highlights, i);

//							if(textBasedHighlight && !ignoreHighlight){
//								highlightsToRender.addElement(highlight);
//							}

                            renderEmbeddedText(null,currentTR,currentObject,TRUETYPE,g2,aff,
									highlight,textStrokeCol,textFillCol,strokeOpacity,
									fillOpacity,currentArea,lineWidth);


						}
						break;

					case TYPE1C:

						if(debug)
							System.out.println("Type1c");

						if(newClip){
							renderClip(clipToUse, dirtyRegion,defaultClip,g2);
							newClip=false;
						}

						aff=new AffineTransform(afValues1[afCount],afValues2[afCount],afValues3[afCount],afValues4[afCount],x,y);

						if(!drawJustHighlights || textHighlightsX!=null){
							Rectangle highlight = setHighlightForGlyph(currentArea,objectTypes,highlights, i);

//							if(textBasedHighlight && !ignoreHighlight){
//								highlightsToRender.addElement(highlight);
//							}

							renderEmbeddedText(null,currentTR,currentObject,TYPE1C,g2,aff,
									highlight,textStrokeCol,textFillCol,strokeOpacity,
									fillOpacity,currentArea,lineWidth);

						}
						break;

					case TYPE3:

						if(debug)
							System.out.println("Type3");

						if(newClip){
							renderClip(clipToUse, dirtyRegion,defaultClip,g2);
							newClip=false;
						}

						aff=new AffineTransform(afValues1[afCount],afValues2[afCount],afValues3[afCount],afValues4[afCount],x,y);

						if(!drawJustHighlights || textHighlightsX!=null){
							Rectangle highlight = setHighlightForGlyph(currentArea,objectTypes,highlights, i);

//							if(textBasedHighlight && !ignoreHighlight){
//								highlightsToRender.addElement(highlight);
//							}

							renderEmbeddedText(null,currentTR,currentObject,TYPE3,g2,aff,
									highlight, textStrokeCol,textFillCol,strokeOpacity,
									fillOpacity,currentArea,lineWidth);

						}
						break;

					case IMAGE:

						int sampling=1;

						int currentImageOption=PDFImageProcessing.NOTHING;
						if(imageOptions!=null)
							currentImageOption=imageOptions[iCount];


						int w1=0,h1=0,pX=0,pY=0,defaultSampling=1,defaultX,defaultY;

						// generate unique value to every image on given page (no more overighting stuff in the hashmap)
						Integer pn = new Integer(this.pageNumber);
						Integer iC = new Integer(iCount);
						String key = pn.toString() + iC.toString();

						if(samplingDebug)
							System.out.println("image "+iCount+" scaling="+scaling+" lastScaling="+lastScaling+" objectStoreRef.isRawImageDataSaved(iCount)="+objectStoreRef.isRawImageDataSaved(key));

                        //@mariusz - why do we need useHiResImageForDisplay - I disabled for Rog to test
                        if(!isType3Font && objectStoreRef.isRawImageDataSaved(key)){// && useHiResImageForDisplay){


                            float  scalingToUse=scaling;

							//fix for rescaling on Enkelt-Scanning_-_Bank-10.10.115.166_-_12-12-2007_-_15-27-57jpg50-300.pdf
							if(useHiResImageForDisplay){
								if(scaling<1)
									scalingToUse=1f;
							}


							defaultX=((Integer) objectStoreRef.getRawImageDataParameter(key,ObjectStore.IMAGE_pX)).intValue();
							pX=(int)(defaultX*scalingToUse);

							defaultY=((Integer) objectStoreRef.getRawImageDataParameter(key,ObjectStore.IMAGE_pY)).intValue();
							pY=(int)(defaultY*scalingToUse);

							w1=((Integer) objectStoreRef.getRawImageDataParameter(key,ObjectStore.IMAGE_WIDTH)).intValue();
							h1=((Integer) objectStoreRef.getRawImageDataParameter(key,ObjectStore.IMAGE_HEIGHT)).intValue();

							if(samplingDebug)
								System.out.println(this+" scaling="+this.scaling+" last="+lastScaling+
										" scalingToUse="+scalingToUse+
										" pX="+pX+" defaultX="+defaultX+" pY="+pY+" defaultY="+defaultY+
										" w1="+w1+" h1="+h1);


							/**
							 * down-sample size if displaying
							 */
							//if(renderPage && !imageMask && (d==1 || d==8) && pX>0 && pY>0){
							if(pX>0){

								//see what we could reduce to and still be big enough for page
								int newW=w1,newH=h1;

								int smallestH=pY<<2; //double so comparison works
								int smallestW=pX<<2;

								//cannot be smaller than page
								while(newW>smallestW && newH>smallestH){
									sampling=sampling<<1;
									newW=newW>>1;
									newH=newH>>1;
								}

								int scaleX=w1/pX;
								if(scaleX<1)
									scaleX=1;

								int scaleY=h1/pY;
								if(scaleY<1)
									scaleY=1;

								//choose smaller value so at least size of page
								sampling=scaleX;
								if(sampling>scaleY)
									sampling=scaleY;



								/**
								 * work out default as well for ratio
								 */

								//see what we could reduce to and still be big enough for page
								int defnewW=w1,defnewH=h1;

								int defsmallestH=pY<<2; //double so comparison works
								int defsmallestW=pX<<2;

								//cannot be smaller than page
								while(defnewW>defsmallestW && defnewH>defsmallestH){
									defaultSampling=defaultSampling<<1;
									defnewW=defnewW>>1;
									defnewH=defnewH>>1;
								}

								int defscaleX=w1/defaultX;
								if(defscaleX<1)
									defscaleX=1;

								int defscaleY=h1/defaultY;
								if(defscaleY<1)
									defscaleY=1;

								//choose smaller value so at least size of page
								defaultSampling=defscaleX;
								if(defaultSampling>defscaleY)
									defaultSampling=defscaleY;

								//////////////////////


								//switch to 8 bit and reduce bw image size by averaging
								if((scaling>1f || lastScaling>1f)&& sampling>=1 && (lastScaling!=scaling)){

									newW=w1/sampling;
									newH=h1/sampling;

									if(samplingDebug)
										System.out.println("defaultSampling="+defaultSampling+" sampling="+
												sampling+" w1="+w1+
												" h1="+h1+" newW="+newW+" newH="+newH+" ");

									{//if(d==1){

										//save raw 1 bit data
										//code in DynamicVectorRenderer may need alignment
										byte[] data= objectStoreRef.getRawImageData(key);


										//make 1 bit indexed flat
										byte[] index=null;//decodeColorData.getIndexedMap();
										//if(index!=null)
										//index=decodeColorData.convertIndexToRGB(index);

										int size=newW*newH;
										//if(index!=null)
										//	size=size*3;

										byte[] newData=new byte[size];

										final int[] flag={1,2,4,8,16,32,64,128};

										int origLineLength= (w1+7)>>3;

										/**
										 * its now no longer turned so reset
										 */
//										if(!useHiResImageForDisplay)
//											data=ImageOps.invertImage(data, w1, h1, 1, 1, index);

										//scan all pixels and down-sample
										//if(sampling>1)
										for(int y1=0;y1<newH;y1++){

											//System.out.println(y1+"/"+newH);
											for(int x1=0;x1<newW;x1++){

												int bytes=0,count1=0;

												//allow for edges in number of pixels left
												int wCount=sampling,hCount=sampling;
												int wGapLeft=w1-x1;
												int hGapLeft=h1-y1;
												if(wCount>wGapLeft)
													wCount=wGapLeft;
												if(hCount>hGapLeft)
													hCount=hGapLeft;

												// System.out.println(x1+"//"+newW+" "+wCount+" "+hCount);

												//count pixels in sample we will make into a pixel (ie 2x2 is 4 pixels , 4x4 is 16 pixels)
												for(int yy=0;yy<hCount;yy++){
													for(int xx=0;xx<wCount;xx++){

														byte currentByte=data[((yy+(y1*sampling))*origLineLength)+(((x1*sampling)+xx)>>3)];
														int bit=currentByte & flag[7-(((x1*sampling)+xx)& 7)];

														if(bit!=0)
															bytes++;
														count1++;

													}
												}

												//set value as white or average of pixels
												if(count1>0){

													if(index==null)
														newData[x1+(newW*y1)]=(byte)((255*bytes)/count1);
													else
														newData[x1+(newW*y1)]=(byte)(((index[1] & 255)*bytes)/count1);
												}else{
													if(index==null)
														newData[x1+(newW*y1)]=(byte) 255;
													else
														newData[x1+(newW*y1)]=index[0];
												}
											}
										}

										//decodeColorData.setIndex(null, 0);

										final int[] bands = {0};

										//WritableRaster raster =Raster.createPackedRaster(new DataBufferByte(newData, newData.length), newW, newH, 1, null);
										Raster raster =Raster.createInterleavedRaster(new DataBufferByte(newData, newData.length),newW,newH,newW,1,bands,null);

										BufferedImage image =new BufferedImage(newW,newH,BufferedImage.TYPE_BYTE_GRAY);
										image.setData(raster);

										//currentObject=image;


										//reset our track if only graphics
										if(singleImage!=null)
											singleImage=image;

										pageObjects[i]=image;

										if(samplingDebug)
											System.out.println("xxx redo img size="+image.getWidth()+" "+image.getHeight());
										//ShowGUIMessage.showGUIMessage(w+" "+h,image,w+" "+h);

										currentObject=image;

									}/**else if(d==8 && (filter==null || filter.indexOf("DCT")==-1)){

                                     boolean hasIndex=decodeColorData.getIndexedMap()!=null;

                                     int x=0,y=0,xx=0,yy=0,jj=0,comp=0,origLineLength=0;
                                     try{

                                     if(hasIndex)
                                     comp=1;
                                     else
                                     comp=decodeColorData.getColorComponentCount();

                                     //black and white
                                     if(w*h==data.length)
                                     comp=1;

                                     byte[] newData=new byte[newW*newH*comp];

                                     //System.err.println(hasIndex+" "+data.length+" "+comp+" scaling="+sampling);
                                     origLineLength= w*comp;

                                     //System.err.println("size="+w*h*comp+" filter"+filter+" scaling="+sampling+" comp="+comp);
                                     //System.err.println("w="+w+" h="+h+" data="+data.length+" origLineLength="+origLineLength+" sampling="+sampling);
                                     //scan all pixels and down-sample
                                     for(y=0;y<newH;y++){
                                     for(x=0;x<newW;x++){

                                     //allow for edges in number of pixels left
                                     int wCount=sampling,hCount=sampling;
                                     int wGapLeft=w-x;
                                     int hGapLeft=h-y;
                                     if(wCount>wGapLeft)
                                     wCount=wGapLeft;
                                     if(hCount>hGapLeft)
                                     hCount=hGapLeft;


                                     for(jj=0;jj<comp;jj++){
                                     int byteTotal=0,count=0;
                                     //count pixels in sample we will make into a pixel (ie 2x2 is 4 pixels , 4x4 is 16 pixels)
                                     for(yy=0;yy<hCount;yy++){
                                     for(xx=0;xx<wCount;xx++){

                                     byteTotal=byteTotal+(data[((yy+(y*sampling))*origLineLength)+(((x*sampling*comp)+(xx*comp)+jj))] & 255);

                                     count++;
                                     }
                                     }

                                     //set value as white or average of pixels
                                     if(count>0)
                                     //if(index==null)
                                     newData[jj+(x*comp)+(newW*y*comp)]=(byte)((byteTotal)/count);
                                     //else
                                     //	newData[x+(newW*y)]=(byte)(((index[1] & 255)*byteTotal)/count);
                                     else{
                                     //if(index==null)
                                     //newData[jj+x+(newW*y*comp)]=(byte) 255;
                                     //else
                                     //	newData[x+(newW*y)]=index[0];
                                     }
                                     }
                                     }
                                     }

                                     data=newData;
                                     h=newH;
                                     w=newW;

                                     }catch(Exception e){

                                     }
                                     }  /**/
								}
							}



							/**handle any decode array*/
							/**
                                 if(decodeArray.length() == 0){
                                 }else if((filter!=null)&&((filter.indexOf("JPXDecode")!=-1)||(filter.indexOf("DCT")!=-1))){ //don't apply on jpegs
                                 }else
                                 applyDecodeArray(data, d, decodeArray,colorspaceID);
                                 /**/
							/**
                                 {

                                 //try to keep as binary if possible
                                 boolean hasObjectBehind=current.hasObjectsBehind(currentGraphicsState.CTM);

                                 //see if black and back object
                                 if(maskCol[0]==0 && maskCol[1]==0 && maskCol[2]==0 && !hasObjectBehind && !this.isType3Font){
                                 WritableRaster raster =Raster.createPackedRaster(new DataBufferByte(data, data.length), w, h, 1, null);
                                 image =new BufferedImage(w,h,BufferedImage.TYPE_BYTE_BINARY);
                                 image.setData(raster);
                                 }else{
                                 //if(hasObjectBehind){
                                 //image=ColorSpaceConvertor.convertToARGB(image);
                                 byte[] index={maskCol[0],maskCol[1],maskCol[2],(byte)255,(byte)255,(byte)255};
                                 image = convertIndexedToFlat(decodeColorData.getID(),1,w, h, data, index, index.length,true);
                                 //ShowGUIMessage.showGUIMessage("x",image, "x");
                                 //}else{
                                 //   WritableRaster raster =Raster.createPackedRaster(new DataBufferByte(data, data.length), w, h, d, null);
                                 //   image = new BufferedImage(new IndexColorModel(d, 1, maskCol, 0, false), raster, false, null);
                                 //}
                                 }

                                 }
                                 }else if (filter == null) { //handle no filters

                                 //save out image
                                 LogWriter.writeLog("Image "+ name+ ' ' + w+ "W * "+ h+ "H with No Compression at BPC "+ d+" and Colorspace="+colorspaceName);

                                 image =makeImage(decodeColorData,w,h,d,data);

                                 } else if (filter.indexOf("DCT") != -1) { //handle JPEGS

                                 LogWriter.writeLog("JPeg Image "+ name+ ' ' + w+ "W * "+ h+ 'H');

                                 // get image data,convert to BufferedImage from JPEG & save out
                                 if(colorspaceID== ColorSpaces.DeviceCMYK){
                                 if(extractRawCMYK){
                                 LogWriter.writeLog("Raw CMYK image " + name + " saved.");
                                 if(!objectStoreStreamRef.saveRawCMYKImage(data, name))
                                 addPageFailureMessage("Problem saving Raw CMYK image "+name);
                                 }
                                 }

                                 //separation, renderer
                                 try{
                                 image=decodeColorData.JPEGToRGBImage(data,w,h,decodeArray,pX,pY);

                                 removed=ColorSpaceConvertor.wasRemoved;
                                 }catch(Exception e){
                                 addPageFailureMessage("Problem converting "+name+" to JPEG");
                                 e.printStackTrace();
                                 image=null;
                                 }
                                 type = "jpg";
                                 }else if(filter.indexOf("JPXDecode")!=-1){ //needs imageio library

                                 LogWriter.writeLog("JPeg 2000 Image "+ name+ ' ' + w+ "W * "+ h+ 'H');

                                 if(JAIHelper.isJAIused()){
                                 image = decodeColorData.JPEG2000ToRGBImage(data,pX,pY);

                                 type = "jpg";
                                 }else{
                                 LogWriter.writeLog("JPeg 2000 Image needs JAI on classpath and enabled in JPedal");

                                 }

                                 } else { //handle other types
                                 LogWriter.writeLog(name+ ' ' + w+ "W * "+ h+ "H  with "+ filter+ " BPC="+d+" CS="+colorspaceName);

                                 image =makeImage(decodeColorData,w,h,d,data);

                                 //choose type on basis of size and avoid ICC as they seem to crash the Java class
                                 if ((d == 8)| (nonstrokeColorSpace.getID()== ColorSpaces.DeviceRGB)| (nonstrokeColorSpace.getID()== ColorSpaces.ICC))
                                 type = "jpg";
                                 }
                                 /**/
                        }

						if(!drawJustHighlights){

							if(newClip){

								renderClip(clipToUse, dirtyRegion,defaultClip,g2);
								newClip=false;
							}

							if(this.useHiResImageForDisplay){
							}else{


								if(samplingDebug)
									System.out.println("lowres scaling="+scaling+" sampling="+sampling+" option b image dim="+((BufferedImage)currentObject).getWidth()+" "+((BufferedImage)currentObject).getHeight());

								AffineTransform before=g2.getTransform();
								extraRot = false;

								if(pY>0){


									double[] matrix=new double[6];
									g2.getTransform().getMatrix(matrix);
									double ratio=((float)pY)/((BufferedImage)currentObject).getHeight();

									matrix[0]=ratio;
									matrix[1]=0;
									matrix[2]=0;
									matrix[3]=-ratio;

									g2.scale(1f/scaling,1f/scaling);

									g2.setTransform(new AffineTransform(matrix));

									if(samplingDebug)
										System.out.println("after="+g2.getTransform());

								}else{
									extraRot = true;
								}

								renderImage(null,(BufferedImage)currentObject,fillOpacity,null,g2,x,y,currentImageOption);
								g2.setTransform(before);
							}


						}
						iCount++;

						break;

					case CLIP:
						clipToUse=pageClips[cCount];
						newClip=true;
						cCount++;
						break;

					case AF:
						afCount++;
						break;
					case FONTSIZE:
						fsCount++;
						break;
					case LINEWIDTH:
						lineWidth=lwValues[lwCount];
						lwCount++;
						break;
					case TEXTCOLOR:

						if(debug)
							System.out.println("TextCOLOR");

						textFillType=textFill[tCount];

						if(textFillType==GraphicsState.STROKE)
							textStrokeCol=(PdfPaint) text_color[tCount];
						else
							textFillCol=(PdfPaint) text_color[tCount];

						tCount++;
						break;
					case FILLCOLOR:

						if(debug)
							System.out.println("FillCOLOR");

						if(!colorsLocked)
							fillCol=(PdfPaint) fill_color[fillCount];

						fillCount++;

						break;
					case STROKECOLOR:

						if(debug)
							System.out.println("StrokeCOL");

						if(!colorsLocked){
							strokeCol=(PdfPaint)stroke_color[strokeCount];
							if(strokeCol!=null)
								strokeCol.setScaling(cropX,cropH,scaling,0 ,0);
						}

						strokeCount++;
						break;

					case STROKE:

						currentStroke=(Stroke)stroke[stCount];

						if(debug)
							System.out.println("STROKE");

						stCount++;
						break;

					case TR:

						if(debug)
							System.out.println("TR");

						currentTR=TRvalues[trCount];
						trCount++;
						break;

					case STROKEOPACITY:

						if(debug)
							System.out.println("Stroke Opacity "+opacity[opCount]+" opCount="+opCount);

						strokeOpacity=opacity[opCount];
						opCount++;
						break;

                    case FILLOPACITY:

						if(debug)
							System.out.println("Set Fill Opacity "+opacity[opCount]+" count="+opCount);

						fillOpacity=opacity[opCount];
						opCount++;
						break;

					case STRING:

						if(!drawJustHighlights){

								Shape s = g2.getClip();
								g2.setClip(defaultClip);
								AffineTransform defaultAf=g2.getTransform();
								String displayValue=(String)currentObject;

								double[] af=new double[6];

								g2.getTransform().getMatrix(af);

								if(af[2]!=0)
									af[2]=-af[2];
								if(af[3]!=0)
									af[3]=-af[3];
								g2.setTransform(new AffineTransform(af));

								Font javaFont=(Font) javaObjects[stringCount];

								g2.setFont(javaFont);

								if((currentTR & GraphicsState.FILL)==GraphicsState.FILL){

									if(textFillCol!=null)
										textFillCol.setScaling(cropX,cropH,scaling,0 ,0);
									g2.setPaint(textFillCol);

								}

								if((currentTR & GraphicsState.STROKE)==GraphicsState.STROKE){

									if(textStrokeCol!=null)
										textStrokeCol.setScaling(cropX,cropH,scaling,0 ,0);
									g2.setPaint(textStrokeCol);

								}

								g2.drawString(displayValue,x,y);
								g2.setClip(s);
								g2.setTransform(defaultAf);

								stringCount++;

						}
						break;

                    case XFORM:

                        renderXForm(g2, (DynamicVectorRenderer)currentObject,fillOpacity);
                        break;

					case CUSTOM:

						Shape s = g2.getClip();
						g2.setClip(defaultClip);
						AffineTransform af = g2.getTransform();

						JPedalCustomDrawObject customObj=(JPedalCustomDrawObject)currentObject;
						if(isPrinting)
							customObj.print(g2, this.printPage);
						else
							customObj.paint(g2);

						g2.setTransform(af);
						g2.setClip(s);

						break;

					}
				}
			}
		}

        //needs to be before we return defualts to factor
        //in a viewport for abacus
        if(textBasedHighlight && highlights!=null){
			//highlights = highlightsToRender.get();
			for(int h=0; h!=highlights.length; h++){
				ignoreHighlight=false;
				renderHighlight(highlights[h], g2);
			}
		}
        
		//restore defaults
		g2.setClip(defaultClip);

		g2.setTransform(rawScaling);



		if(DynamicVectorRenderer.debugPaint)
			System.err.println("Painted "+paintedCount);

		//<start-jfr>
		//tell user if problem
		if((frame!=null)&&(renderFailed)&&(userAlerted==false)){

			userAlerted=true;

			if(PdfDecoder.showErrorMessages){
				String status = (Messages.getMessage("PdfViewer.ImageDisplayError")+
						Messages.getMessage("PdfViewer.ImageDisplayError1")+
						Messages.getMessage("PdfViewer.ImageDisplayError2")+
						Messages.getMessage("PdfViewer.ImageDisplayError3")+
						Messages.getMessage("PdfViewer.ImageDisplayError4")+
						Messages.getMessage("PdfViewer.ImageDisplayError5")+
						Messages.getMessage("PdfViewer.ImageDisplayError6")+
						Messages.getMessage("PdfViewer.ImageDisplayError7"));

				JOptionPane.showMessageDialog(frame,status);

				frame.invalidate();
				frame.repaint();
			}

		}
		//<end-jfr>

		//reduce count
		if(isScreen)
			paintThreadCount--;

		//track so we do not redo onto raster
		if(optimsePainting){
			lastItemPainted=count;
			//System.out.println("lastItem painted="+lastItemPainted);
		}else
			lastItemPainted=-1;

		//track
		lastScaling=scaling;
		

		
		//if we highlighted text return oversized
		if(minX==-1)
			return null;
		else
			return new Rectangle((int)minX,(int)minY,(int)(maxX-minX),(int)(maxY-minY));
	}

    public void renderXForm(Graphics2D g2, DynamicVectorRenderer dvr, float nonstrokeAlpha) {

        Rectangle area=dvr.getOccupiedArea();
        int fw=area.width;
        int fh=area.height;

        //ignore offpage
        if(fw<=0 || fh<=0)
        return;

        BufferedImage formImg=new  BufferedImage(fw,fh,BufferedImage.TYPE_INT_ARGB);

        Graphics2D formG2=formImg.createGraphics();

        dvr.paint(formG2,null,null,null,false,true);
        
        GraphicsState gs=new GraphicsState();
        gs.CTM[0][0] = (float)fw;
		gs.CTM[1][0] = (float)0.0;
		gs.CTM[2][0] = (float)0;
		gs.CTM[0][1] = (float)0.0;
		gs.CTM[1][1] = (float)fh;
		gs.CTM[2][1] = (float)0;
		gs.CTM[0][2] = (float)0.0;
		gs.CTM[1][2] = (float)0.0;
		gs.CTM[2][2] = (float)1.0;

        renderImage(new AffineTransform(),formImg, nonstrokeAlpha, gs,g2, area.x,area.y, PDFImageProcessing.IMAGE_INVERTED);

    }


    //work out size glyph occupies
	private static Rectangle getAreaForGlyph(float[][] trm){
		//workout area
		int w=(int) Math.sqrt((trm[0][0]*trm[0][0])+(trm[1][0]*trm[1][0]));
		int h=(int) Math.sqrt((trm[1][1]*trm[1][1])+(trm[0][1]*trm[0][1]));
		
		float xDiff = 0;
		float yDiff = 0;
		
		if(trm[0][0]<0)
			xDiff = trm[0][0];
		else if(trm[1][0]<0)
			xDiff = trm[1][0];
		
		if(trm[1][1]<0)
			yDiff = trm[1][1];
		else if(trm[0][1]<0)
			yDiff = trm[0][1];
		
		return (new Rectangle((int)(trm[2][0]+xDiff),(int)(trm[2][1]+yDiff),w,h));

	}

	/**
	 * allow user to set component for waring message in renderer to appear -
	 * if unset no message will appear
	 * @param frame
	 */
	public void setMessageFrame(Container frame){
		this.frame=frame;
	}

	public void paintBackground(Graphics2D g2, Rectangle dirtyRegion) {
		if((addBackground)){
			g2.setColor(backgroundColor);

			if(dirtyRegion==null){
				g2.fill(new Rectangle(xx,yy,(int)(w*scaling),(int)(h*scaling)));
			}else
				g2.fill(dirtyRegion);

		}
	}

	/**
	 * update clip
	 * @param defaultClip
	 */
	public static void renderClip(Area clip,Rectangle dirtyRegion,  Shape defaultClip,Graphics2D g2) {

		/**/

		if (clip != null){
			g2.setClip(clip);

			//can cause problems in Canoo so limit effect if Canoo running
			if(dirtyRegion!=null)// && (!isRunningOnRemoteClient || clip.intersects(dirtyRegion)))
				g2.clip(dirtyRegion);
		}else
			g2.setClip(defaultClip);

		/***/
	}
	
	//Flag to prevent drawing highlights too often.
	boolean ignoreHighlight = false;
	
	/**
	 * highlight a glyph by reversing the display. For white text, use black
	 */
	private Rectangle setHighlightForGlyph(Rectangle area,int[] objectTypes,Rectangle[] highlights, int i) {

		if(textBasedHighlight){
			if (highlights == null || textHighlightsX == null)
				return null;
			
			ignoreHighlight = false;
			for(int j=0; j!= highlights.length; j++){
				if(highlights[j]!=null && (highlights[j].intersects(area))){
					
					//Get intersection of the two areas
					Rectangle intersection = highlights[j].intersection(area);
					
					//Intersection area between highlight and text area
					float iArea = intersection.width*intersection.height;

					//25% of text area 
					float tArea = (area.width*area.height)/ 4f;
					
					//Only highlight if (x.y) is with highlight and more than 25% intersects
					//or intersect is greater than 60%
					if((highlights[j].contains(area.x, area.y) && iArea>tArea) || 
							iArea>(area.width*area.height)/ 1.667f
							){
						if(!drawnHighlights[j]){
							ignoreHighlight = false;
							drawnHighlights[j]=true;
							return highlights[j];
						}else{
							ignoreHighlight = true;
							return highlights[j];
						}
					}
				}
			}

			//old code not used
			return null;
		}else{
			boolean isHighlighted = false;

			if (highlights == null || textHighlightsX == null)
				return null;

			//Rectangle currentHighlight=textHighlights[i];

			int hcount = highlights.length;
			int objectType = objectTypes[i];

			for (int i2 = 0; i2 < hcount; i2++) {

				boolean fontNonEmbedded = objectType == TEXT;
				if (area != null && (fontNonEmbedded || objectType == TRUETYPE || objectType == TYPE1C || objectType == TYPE3)) {

//					if ((highlights[i2] != null) && 
//					(highlights[i2].getMinX() <= area.getMinX()) && 
//					(highlights[i2].getMinY() <= area.getMinY()) && 
//					(highlights[i2].intersects(area))) {

					if (highlights[i2] != null) {

						Rectangle intersection = highlights[i2].intersection(area);

						//@kieran: this is set to false to allow 2 different
						//rules below on when to select both. If you are going 
						//to alter, we need to test both.
						boolean inBoundingBox = false;

						/** the font is not embedded so we need to be a bit more flexible about the x and y locations */
						if (fontNonEmbedded && (highlights[i2].getMinX() <= (area.getMinX() + area.width / 3d)) && 
								(highlights[i2].getMinY() <= (area.getMinY() + area.height / 3d))) {
							inBoundingBox = true;

							/** the font is embedded so we can be strict about the x and y locations */
						} else if ((highlights[i2].getMinX() <= area.getMinX()) && 
								(highlights[i2].getMinY() <= area.getMinY())) {

							inBoundingBox = true;
						}

						if (inBoundingBox && (intersection.height >= area.height / 2d) && (intersection.width >= 1)) {

							i2 = hcount;
							isHighlighted = true;
							Rectangle2D bounds = area.getBounds2D();
							if (minX == -1) {
								minX = bounds.getMinX();
								minY = bounds.getMinY();
								maxX = bounds.getMaxX();
								maxY = bounds.getMaxY();
							} else {
								double tmp = bounds.getMinX();
								if (tmp < minX)
									minX = tmp;
								tmp = bounds.getMinY();
								if (tmp < minY)
									minY = tmp;
								tmp = bounds.getMaxX();
								if (tmp > maxX)
									maxX = tmp;
								tmp = bounds.getMaxY();
								if (tmp > maxY)
									maxY = tmp;
							}
						}
					}
				}
			}

			// if(isHighlighted)
			//  System.out.println(i);

			if(isHighlighted){
				if(marksNewCode)
					return new Rectangle(0,0,textHighlightsWidth[i],textHighlightsHeight[i]);
				else
					return new Rectangle(textHighlightsX[i],textHighlightsY[i],textHighlightsWidth[i],textHighlightsHeight[i]);
			}else
				return null;
		}
	}

	/* saves text object with attributes for rendering*/
	final public void drawText(float[][] Trm,String text,GraphicsState currentGraphicsState,float x,float y,Font javaFont) {

		/**
		 * set color first
		 */
		PdfPaint currentCol=null;

		if(Trm!=null){
			double[] nextAf=new double[]{Trm[0][0],Trm[0][1],Trm[1][0],Trm[1][1],Trm[2][0],Trm[2][1]};

			if((lastAf[0]==nextAf[0])&&(lastAf[1]==nextAf[1])&&
					(lastAf[2]==nextAf[2])&&(lastAf[3]==nextAf[3])){
			}else{
				this.drawAffine(nextAf);
				lastAf[0]=nextAf[0];
				lastAf[1]=nextAf[1];
				lastAf[2]=nextAf[2];
				lastAf[3]=nextAf[3];
			}
		}

		int text_fill_type = currentGraphicsState.getTextRenderType();

		//for a fill
		if ((text_fill_type & GraphicsState.FILL) == GraphicsState.FILL) {
			currentCol=currentGraphicsState.getNonstrokeColor();

			if(currentCol.isPattern()){
				drawColor(currentCol,GraphicsState.FILL);
				resetTextColors=true;
			}else{

				int newCol=(currentCol).getRGB();
				if((resetTextColors)||((lastFillTextCol!=newCol))){
					lastFillTextCol=newCol;
					drawColor(currentCol,GraphicsState.FILL);
				}
			}
		}

		//and/or do a stroke
		if ((text_fill_type & GraphicsState.STROKE) == GraphicsState.STROKE){
			currentCol=currentGraphicsState.getStrokeColor();

			if(currentCol.isPattern()){
				drawColor(currentCol,GraphicsState.STROKE);
				resetTextColors=true;
			}else{

				int newCol=currentCol.getRGB();
				if((resetTextColors)||(lastStrokeCol!=newCol)){
					lastStrokeCol=newCol;
					drawColor(currentCol,GraphicsState.STROKE);
				}
			}
		}

		pageObjects.addElement(text);
		javaObjects.addElement(javaFont);

		objectType.addElement(STRING);
		areas.addElement(null);

		x_coord=checkSize(x_coord,currentItem);
		y_coord=checkSize(y_coord,currentItem);
		x_coord[currentItem]=x;
		y_coord[currentItem]=y;

		currentItem++;

		resetTextColors=false;

		//flag as dirty
		//if(currentManager!=null)
		//currentManager.addDirtyRegion(drawPanel,transformedGlyph2.getBounds().x,transformedGlyph2.getBounds().y,transformedGlyph2.getBounds().width,transformedGlyph2.getBounds().height);
	}


	/**resize array*/
	private static float[] checkSize(float[] array, int currentItem) {

		int size=array.length;
		if(size<=currentItem){
			int newSize=size*2;
			float[] newArray=new float[newSize];
			System.arraycopy( array, 0, newArray, 0, size );

			array=newArray;
		}


		return array;

	}

	/**workout combined area of shapes in an area*/
	public  Rectangle getCombinedAreas(Rectangle targetRectangle,boolean justText){

		Rectangle combinedRectangle=null;

		if(areas!=null){

			//set defaults for new area
			Rectangle target = targetRectangle.getBounds();
			int x2=target.x;
			int y2=target.y;
			int x1=x2+target.width;
			int y1=y2+target.height;

			boolean matchFound=false;

			Rectangle[] currentAreas=areas.get();
			int count=currentAreas.length;
			//find all items enclosed by this rectangle
			for(int i=0;i<count;i++){
				if((currentAreas[i]!=null)&&(targetRectangle.contains(currentAreas[i]))){
					matchFound=true;

					int newX=currentAreas[i].x;
					if(x1>newX)
						x1=newX;
					newX=currentAreas[i].x+currentAreas[i].width;
					if(x2<newX)
						x2=newX;

					int newY=currentAreas[i].y;
					if(y1>newY)
						y1=newY;
					newY=currentAreas[i].y+currentAreas[i].height;
					if(y2<newY)
						y2=newY;
				}
			}

			//allow margin of 1 around object
			if(matchFound){
				combinedRectangle=new Rectangle(x1-1,y1+1,(x2-x1)+2,(y2-y1)+2);

			}

		}

		return combinedRectangle;
	}


	/*setup renderer*/
	final public void init(int x, int y,int rawRotation) {
		w=x;
		h=y;
		this.rotation=rawRotation;

	}

	/* get page as Image*/
	final public BufferedImage getPageAsImage(
			float scaling,
			int cropX,int cropY,
			int cropW,int cropH,
			int page,AffineTransform af_scaling,int type) {

		if(cropW<0){
			cropW=w;
			cropH=h;
		}else{
			cropW=(int) (cropW*scaling);
			cropH=(int) (cropH*scaling);
		}

		//ensure all drawn
		lastItemPainted=-1;

		BufferedImage image=new BufferedImage(cropW, cropH, type);

		Graphics2D g2 = image.createGraphics();
		if(type==1){
			g2.setColor(Color.white);
			g2.fillRect(0,0,cropW,cropH);
		}

		AffineTransform af=g2.getTransform();


		if(af_scaling!=null)
			g2.transform(af_scaling);
		paint(g2,null,cropX,cropY);

		//ShowGUIMessage.showGUIMessage("w",image,"w");

		g2.setTransform(af);

		return image;
	}

    final public static boolean isRotationreversed(float[][] CTM){

       return (CTM[0][0]>0 && CTM[0][1]<0 && CTM[1][0]>0 && CTM[1][1]>0);
   }

	final public static boolean isRotated(float[][] CTM){

        if(isRotationreversed(CTM))
            return true;
        else
		return (CTM[0][0]==0 && (CTM[1][1]==0));// &&(CTM[0][1]*CTM[1][0] <0));
	}




	//<start-jfr>
	/* save image in array to draw */
	final public void drawImage(int pageNumber,BufferedImage image,
			GraphicsState currentGraphicsState,
			boolean alreadyCached,String name, int optionsApplied) {

		this.pageNumber=pageNumber;
		float CTM[][]=currentGraphicsState.CTM;

		float x=currentGraphicsState.x;
		float y=currentGraphicsState.y;

        boolean cacheInMemory=image.getWidth()<100 && image.getHeight()<100;
        
        String key;
		if(rawKey==null)
			key=pageNumber+"_"+(currentItem+1);
		else
			key=rawKey+ '_' +(currentItem+1);

		if(imageOptions==null){
			imageOptions=new Vector_Int(defaultSize);
			imageOptions.setCheckpoint();
		}

		//Turn image around if needed
		//(avoid if has skew on as well as currently breaks image)
		if(!alreadyCached && image.getHeight()>1 && ((optionsApplied & PDFImageProcessing.IMAGE_INVERTED) !=PDFImageProcessing.IMAGE_INVERTED)){

			boolean turnLater=(optimisedTurnCode && (CTM[0][0]*CTM[0][1]==0) && (CTM[1][1]*CTM[1][0]==0) && !isRotated(CTM));
			if(!optimisedTurnCode || !turnLater)
				image = invertImage(CTM, image);

			if(turnLater)
				optionsApplied=optionsApplied + PDFImageProcessing.TURN_ON_DRAW;

		}

		imageOptions.addElement(optionsApplied);

		if(useHiResImageForDisplay){


		}

		x_coord=checkSize(x_coord,currentItem);
		y_coord=checkSize(y_coord,currentItem);
		x_coord[currentItem]=x;
		y_coord[currentItem]=y;

		objectType.addElement(IMAGE);
		float WidthModifier = 1;
		float HeightModifier = 1;

		if(useHiResImageForDisplay){
			if(!alreadyCached){
				WidthModifier = image.getWidth();
				HeightModifier = image.getHeight();
			}else{
				WidthModifier=((Integer)cachedWidths.get(key)).intValue();
				HeightModifier=((Integer)cachedHeights.get(key)).intValue();
			}
		}

		w=(int)(CTM[0][0]*WidthModifier);
		if(w==0)
			w=(int)(CTM[0][1]*WidthModifier);
		h=(int)(CTM[1][1]*HeightModifier);
		if(h==0)
			h=(int)(CTM[1][0]*HeightModifier);

        //fix for bug if sheered in low res
        if(!useHiResImageForDisplay && CTM[1][0]<0 && CTM[0][1]>0 && CTM[0][0]==0 && CTM[1][1]==0){
            int tmp=w;
            w=-h;
            h=tmp;
        }
        
        //corrected in generation
		if(h<0 && !useHiResImageForDisplay)
			h=-h;

		areas.addElement(new Rectangle((int)currentGraphicsState.x,(int)currentGraphicsState.y,w,h));

		checkWidth(new Rectangle((int)currentGraphicsState.x,(int)currentGraphicsState.y,w,h));

		if(useHiResImageForDisplay && !cacheInMemory){
			pageObjects.addElement(null);
		}else
			pageObjects.addElement(image);

		//store id so we can get as low res image

		imageID.put(name,new Integer(currentItem));

		currentItem++;

	}
	//<end-jfr>

	public static boolean isInverted(float[][] CTM){
		return ((CTM[0][0]>0)&&(CTM[1][1]>0))||((CTM[0][0]<0)&&(CTM[1][1]<0))||
		((CTM[0][0]*CTM[1][1])<0) && CTM[2][1]>=0;
	}

	private static BufferedImage invertImage(float[][] CTM, BufferedImage image) {
		
		boolean isInverted=false;
		if(CTM==null)
			isInverted=true;
		else
			isInverted=isInverted(CTM);


		if(isInverted){

			//turn upside down
			AffineTransform image_at2 =new AffineTransform();
			image_at2.scale(1,-1);
			image_at2.translate(0,-image.getHeight());

			AffineTransformOp invert3= new AffineTransformOp(image_at2,  ColorSpaces.hints);

			boolean imageProcessed=false;

			if(JAIHelper.isJAIused()){

				imageProcessed=true;

				try{
					image = (javax.media.jai.JAI.create("affine", image, image_at2, new javax.media.jai.InterpolationNearest())).getAsBufferedImage();
				}catch(Exception ee){
					imageProcessed=false;
					ee.printStackTrace();
				}catch(Error err){
					imageProcessed=false;
					err.printStackTrace();
				}

				if(!imageProcessed) {
					LogWriter.writeLog("Unable to use JAI for image inversion");
				}

			} /**/

			if(!imageProcessed){

				if(image.getType()==12){ //avoid turning into ARGB

					BufferedImage source=image;
					image =new BufferedImage(source.getWidth(),source.getHeight(),source.getType());

					invert3.filter(source,image);
				}else{

					boolean failed=false;
					//allow for odd behaviour on some files
					try{
						image = invert3.filter(image,null);
					}catch(Exception e){
						failed=true;
					}
					if(failed){
						try{
							invert3 = new AffineTransformOp(image_at2,null);
							image = invert3.filter(image,null);
						}catch(Exception e){
						}
					}
				}

			}
		}

		return image;
	}


	private static BufferedImage invertImageBeforeSave(BufferedImage image, boolean horizontal) {

		//turn upside down
		AffineTransform image_at2 =new AffineTransform();
		if(horizontal){
			image_at2.scale(-1,1);
			image_at2.translate(-image.getWidth(),0);
		}else{
			image_at2.scale(1,-1);
			image_at2.translate(0,-image.getHeight());
		}
		AffineTransformOp invert3= new AffineTransformOp(image_at2,  ColorSpaces.hints);

		boolean imageProcessed=false;

		if(JAIHelper.isJAIused()){

			imageProcessed=true;

			try{
				image = (javax.media.jai.JAI.create("affine", image, image_at2, new javax.media.jai.InterpolationNearest())).getAsBufferedImage();
			}catch(Exception ee){
				imageProcessed=false;
				ee.printStackTrace();
			}catch(Error err){
				imageProcessed=false;
				err.printStackTrace();
			}

			if(!imageProcessed) {
				LogWriter.writeLog("Unable to use JAI for image inversion");
			}

		} /**/

		if(!imageProcessed){

			if(image.getType()==12){ //avoid turning into ARGB

				BufferedImage source=image;
				image =new BufferedImage(source.getWidth(),source.getHeight(),source.getType());

				invert3.filter(source,image);
			}else
				image=invert3.filter(image,null);

		}
		return image;
	}


	//<start-jfr>
	/* save image in array to draw */
	final public void drawImage(BufferedImage image) {
		int h = image.getHeight();

		/**/
		//turn upside down
		if(h>1){
			AffineTransform flip=new AffineTransform();
			flip.translate(0, h);
			flip.scale(1, -1);
			AffineTransformOp invert =new AffineTransformOp(flip,ColorSpaces.hints);
			image=invert.filter(image,null);
		}

		if(useHiResImageForDisplay){

		}

		x_coord=checkSize(x_coord,currentItem);
		y_coord=checkSize(y_coord,currentItem);
		x_coord[currentItem]=0;
		y_coord[currentItem]=0;

		objectType.addElement(IMAGE);
		areas.addElement(new Rectangle(0,0,image.getWidth(),image.getHeight()));

		if(useHiResImageForDisplay){
			pageObjects.addElement(null);
		}else
			pageObjects.addElement(image);

		currentItem++;

	}
	//<end-jfr>

	/**
	 * track actual size of shape
	 */
	private void checkWidth(Rectangle rect) {

        int x1=rect.getBounds().x;
		int y2=rect.getBounds().y;
		int y1=y2+rect.getBounds().height;
		int x2=x1+rect.getBounds().width;

		if(x1<pageX1)
			pageX1=x1;
		if(x2>pageX2)
			pageX2=x2;

		if(y1>pageY1)
			pageY1=y1;
		if(y2<pageY2)
			pageY2=y2;

	}

	/**
	 * return which part of page drawn onto
	 * @return
	 */
	public Rectangle getOccupiedArea(){
		return new Rectangle(pageX1,pageY1,(pageX2-pageX1),(pageY1-pageY2));
	}

	/*save shape in array to draw*/
	final public void drawShape(Shape currentShape,GraphicsState currentGraphicsState) {

		int fillType=currentGraphicsState.getFillType();
		PdfPaint currentCol;

		int newCol;


		//check for 1 by 1 complex shape and replace with dot
		if( (currentShape.getBounds().getWidth()==1)&&
				(currentShape.getBounds().getHeight()==1))
			currentShape=new Rectangle(0,0,1,1);

		//stroke and fill (do fill first so we don't overwrite Stroke)
		if (fillType == GraphicsState.FILL || fillType == GraphicsState.FILLSTROKE) {

			currentCol=currentGraphicsState.getNonstrokeColor();

            //if(currentCol==null)
            //return;
            
			if(currentCol.isPattern()){

				drawFillColor(currentCol);
				fillSet=true;
			}else{
				newCol=currentCol.getRGB();
				if((!fillSet) || (lastFillCol!=newCol)){
					lastFillCol=newCol;
					drawFillColor(currentCol);
					fillSet=true;

				}
			}
		}

		if ((fillType == GraphicsState.STROKE) || (fillType == GraphicsState.FILLSTROKE)) {

			currentCol=currentGraphicsState.getStrokeColor();

			if(currentCol instanceof Color){
				newCol=(currentCol).getRGB();

				if((!strokeSet) || (lastStrokeCol!=newCol)){
					lastStrokeCol=newCol;
					drawStrokeColor(currentCol);
					strokeSet=true;
				}
			}else{
				drawStrokeColor(currentCol);
				strokeSet=true;
			}
		}

		Stroke newStroke=currentGraphicsState.getStroke();
		if((lastStroke!=null)&&(lastStroke.equals(newStroke))){

		}else{
			lastStroke=newStroke;
			drawStroke((newStroke));
		}

		pageObjects.addElement(currentShape);
		objectType.addElement(SHAPE);
		areas.addElement(currentShape.getBounds());

		checkWidth(currentShape.getBounds());


		x_coord=checkSize(x_coord,currentItem);
		y_coord=checkSize(y_coord,currentItem);
		x_coord[currentItem]=currentGraphicsState.x;
		y_coord[currentItem]=currentGraphicsState.y;

		shapeType.addElement(fillType);
		currentItem++;

		resetTextColors=true;

	}



	/*save text colour*/
	final public void drawColor(PdfPaint currentCol,int type) {

		areas.addElement(null);
		pageObjects.addElement(null);
		objectType.addElement(TEXTCOLOR);
		textFillType.addElement(type); //used to flag which has changed

		text_color.addElement(currentCol);

		x_coord=checkSize(x_coord,currentItem);
		y_coord=checkSize(y_coord,currentItem);
		x_coord[currentItem]=0;
		y_coord[currentItem]=0;

		currentItem++;

		//ensure any shapes reset color
		strokeSet=false;
		fillSet=false;

	}

    /*add XForm object*/
	final public void drawXForm(DynamicVectorRenderer dvr) {

		areas.addElement(null);
		pageObjects.addElement(dvr);
		objectType.addElement(XFORM);

        x_coord[currentItem]=0;
		y_coord[currentItem]=0;

		currentItem++;
	}


	/**reset on colorspace change to ensure cached data up to data*/
	public void resetOnColorspaceChange(){

		fillSet=false;
		strokeSet=false;

	}

	/*save shape colour*/
	final public void drawFillColor(PdfPaint currentCol) {

		pageObjects.addElement(null);
		objectType.addElement(FILLCOLOR);
		areas.addElement(null);

		//fill_color.addElement(new Color (currentCol.getRed(),currentCol.getGreen(),currentCol.getBlue()));
		fill_color.addElement(currentCol);

		x_coord=checkSize(x_coord,currentItem);
		y_coord=checkSize(y_coord,currentItem);
		x_coord[currentItem]=0;
		y_coord[currentItem]=0;

		currentItem++;

		this.lastFillCol=currentCol.getRGB();

	}

	/*save opacity settings*/
	final public void setGraphicsState(int fillType,float value) {

		if(value!=1.0f || opacity!=null){

			if(opacity==null){
				opacity=new Vector_Float(defaultSize);
				opacity.setCheckpoint();
			}

			pageObjects.addElement(null);

			if(fillType==GraphicsState.STROKE)
				objectType.addElement(STROKEOPACITY);
			else
				objectType.addElement(FILLOPACITY);

			opacity.addElement(value);

			x_coord=checkSize(x_coord,currentItem);
			y_coord=checkSize(y_coord,currentItem);
			x_coord[currentItem]=0;
			y_coord[currentItem]=0;

			currentItem++;
		}

	}

	/*Method to add Shape, Text or image to main display on page over PDF - will be flushed on redraw*/
	final public void drawAdditionalObjectsOverPage(int[] type, Color[] colors,Object[] obj) throws PdfException {

		if(obj==null){
			return ;
		}

		/**
		 * remember end of items from PDF page
		 */
		if(endItem==-1){

			endItem=currentItem;

			objectType.setCheckpoint();

			shapeType.setCheckpoint();

			pageObjects.setCheckpoint();

			areas.setCheckpoint();

			clips.setCheckpoint();

			textFillType.setCheckpoint();

			text_color.setCheckpoint();

			fill_color.setCheckpoint();

			stroke_color.setCheckpoint();

			stroke.setCheckpoint();

			if(imageOptions!=null)
				imageOptions.setCheckpoint();

			if(TRvalues!=null)
				TRvalues.setCheckpoint();

			if(fs!=null)
				fs.setCheckpoint();

			if(lw!=null)
				lw.setCheckpoint();

			af1.setCheckpoint();

			af2.setCheckpoint();

			af3.setCheckpoint();

			af4.setCheckpoint();

			fontBounds.setCheckpoint();

			if(opacity!=null)
				opacity.setCheckpoint();

		}

		/**
		 * cycle through items and add to display - throw exception if not valid
		 */
		int count=type.length;

		int currentType;

		GraphicsState gs;

		for(int i=0;i<count;i++){

			currentType=type[i];

			switch(currentType){
			case FILLOPACITY:
				setGraphicsState(GraphicsState.FILL, ((Float)obj[i]).floatValue());
				break;

			case STROKEOPACITY:
				setGraphicsState(GraphicsState.STROKE, ((Float)obj[i]).floatValue());
				break;

			case STROKEDSHAPE:
				gs=new GraphicsState();
				gs.setFillType(GraphicsState.STROKE);
				gs.setStrokeColor(new PdfColor(colors[i].getRed(),colors[i].getGreen(),colors[i].getBlue()));
				drawShape( (Shape)obj[i],gs);

				break;

			case FILLEDSHAPE:
				gs=new GraphicsState();
				gs.setFillType(GraphicsState.FILL);
				gs.setNonstrokeColor(new PdfColor(colors[i].getRed(),colors[i].getGreen(),colors[i].getBlue()));
				drawShape( (Shape)obj[i],gs);

				break;

			case CUSTOM:
				drawCustom(obj[i]);

				break;

			case IMAGE:
				ImageObject imgObj=(ImageObject)obj[i];
				gs=new GraphicsState();
				//float fontSize=textObj.font.getSize();
				//double[] afValues={fontSize,0f,0f,fontSize,0f,0f};
				//drawAffine(afValues);

				gs.CTM=new float[][]{
						{imgObj.image.getWidth(),0,1},
						{0,imgObj.image.getHeight(),1},
						{0,0,0}};
				
				gs.x=imgObj.x;
				gs.y=imgObj.y;

				//gs.setTextRenderType(GraphicsState.FILL);
				//gs.setNonstrokeColor(new PdfColor(colors[i].getRed(),colors[i].getGreen(),colors[i].getBlue()));
				drawImage(this.pageNumber,imgObj.image, gs,false,"extImg"+i, PDFImageProcessing.NOTHING);

				break;
				
			case STRING:
				TextObject textObj=(TextObject)obj[i];
				gs=new GraphicsState();
				float fontSize=textObj.font.getSize();
				double[] afValues={fontSize,0f,0f,fontSize,0f,0f};
				drawAffine(afValues);

				drawTR(GraphicsState.FILL);
				gs.setTextRenderType(GraphicsState.FILL);
				gs.setNonstrokeColor(new PdfColor(colors[i].getRed(),colors[i].getGreen(),colors[i].getBlue()));
				drawText(null,textObj.text,gs,textObj.x,-textObj.y,textObj.font); //note y is negative


				break;

			case 0:
				break;

			default:
				throw new PdfException("Unrecognised type "+currentType);
			}
		}
	}

	final public void flushAdditionalObjOnPage(){
		//reset and remove all from page

		//reset pointer
		if(endItem!=-1)
			currentItem=endItem;

		endItem=-1;

		objectType.resetToCheckpoint();

		shapeType.resetToCheckpoint();

		pageObjects.resetToCheckpoint();

		areas.resetToCheckpoint();

		clips.resetToCheckpoint();

		textFillType.resetToCheckpoint();

		text_color.resetToCheckpoint();

		fill_color.resetToCheckpoint();

		stroke_color.resetToCheckpoint();

		stroke.resetToCheckpoint();

		if(imageOptions!=null)
			imageOptions.resetToCheckpoint();

		if(TRvalues!=null)
			TRvalues.resetToCheckpoint();

		if(fs!=null)
			fs.resetToCheckpoint();

		if(lw!=null)
			lw.resetToCheckpoint();

		af1.resetToCheckpoint();

		af2.resetToCheckpoint();

		af3.resetToCheckpoint();

		af4.resetToCheckpoint();

		fontBounds.resetToCheckpoint();

		if(opacity!=null)
			opacity.resetToCheckpoint();

		//reset pointers we use to flag color change
		lastFillTextCol=0;
		lastFillCol=0;
		lastStrokeCol=0;

		lastClip=null;
		hasClips=false;

		lastStroke=null;

		lastAf=new double[4];

		fillSet=false;
		strokeSet=false;

		return ;

	}

	/*save shape colour*/
	final public void drawStrokeColor(Paint currentCol) {

		pageObjects.addElement(null);
		objectType.addElement(STROKECOLOR);
		areas.addElement(null);

		//stroke_color.addElement(new Color (currentCol.getRed(),currentCol.getGreen(),currentCol.getBlue()));
		stroke_color.addElement(currentCol);

		x_coord=checkSize(x_coord,currentItem);
		y_coord=checkSize(y_coord,currentItem);
		x_coord[currentItem]=0;
		y_coord[currentItem]=0;

		currentItem++;

		strokeSet=false;
		fillSet=false;
		resetTextColors=true;

	}

	/*save custom shape*/
	final public void drawCustom(Object value) {

		pageObjects.addElement(value);
		objectType.addElement(CUSTOM);
		areas.addElement(null);

		x_coord=checkSize(x_coord,currentItem);
		y_coord=checkSize(y_coord,currentItem);
		x_coord[currentItem]=0;
		y_coord[currentItem]=0;

		currentItem++;

	}

	/*save shape stroke*/
	final public void drawTR(int value) {

		if(value!=lastTR){ //only cache if needed

			if(TRvalues==null){
				TRvalues=new Vector_Int(defaultSize);
				TRvalues.setCheckpoint();
			}

			lastTR=value;

			pageObjects.addElement(null);
			objectType.addElement(TR);
			areas.addElement(null);

			this.TRvalues.addElement(value);

			x_coord=checkSize(x_coord,currentItem);
			y_coord=checkSize(y_coord,currentItem);
			x_coord[currentItem]=0;
			y_coord[currentItem]=0;


			currentItem++;
		}

	}


	/*save shape stroke*/
	final public void drawStroke(Stroke current) {

		pageObjects.addElement(null);
		objectType.addElement(STROKE);
		areas.addElement(null);

		this.stroke.addElement((current));

		x_coord=checkSize(x_coord,currentItem);
		y_coord=checkSize(y_coord,currentItem);
		x_coord[currentItem]=0;
		y_coord[currentItem]=0;

		currentItem++;

	}

	boolean hasClips=false;
	Area lastClip=null;

	private double cropX;

	private double cropH;

	private float scaling,lastScaling;

    /*save clip in array to draw*/
	final public void drawClip(GraphicsState currentGraphicsState) {

        Area clip=currentGraphicsState.getClippingShape();

		if(hasClips && lastClip==null&& clip==null){
		}else{

			pageObjects.addElement(null);
			objectType.addElement(CLIP);
			areas.addElement(null);

			lastClip=clip;

			if(clip==null){
				clips.addElement(null);

				//System.out.println("======null clip");
			}else{

				clips.addElement((Area) clip.clone());
				//System.out.println("======"+clip.getBounds());
            }

            x_coord=checkSize(x_coord,currentItem);
			y_coord=checkSize(y_coord,currentItem);
			x_coord[currentItem]=currentGraphicsState.x;
			y_coord[currentItem]=currentGraphicsState.y;

			currentItem++;
		}

		hasClips=true;
	}

	/**
	 * store glyph info
	 */
	public void drawEmbeddedText(float[][] Trm,int fontSize,PdfGlyph embeddedGlyph,
			Object javaGlyph, int type,GraphicsState gs, AffineTransform at) {

		/**
		 * set color first
		 */
		PdfPaint currentCol;

		int text_fill_type = gs.getTextRenderType();

		//for a fill
		if ((text_fill_type & GraphicsState.FILL) == GraphicsState.FILL) {
			currentCol= gs.getNonstrokeColor();

			if(currentCol.isPattern()){
				drawColor(currentCol,GraphicsState.FILL);
				resetTextColors=true;
			}else{

				int newCol=(currentCol).getRGB();
				if((resetTextColors)||((lastFillTextCol!=newCol))){
					lastFillTextCol=newCol;
					drawColor(currentCol,GraphicsState.FILL);
					resetTextColors=false;
				}
			}
		}

		//and/or do a stroke
		if ((text_fill_type & GraphicsState.STROKE) == GraphicsState.STROKE){
			currentCol= gs.getStrokeColor();

			if(currentCol.isPattern()){
				drawColor(currentCol,GraphicsState.STROKE);
				resetTextColors=true;
			}else{
				int newCol=currentCol.getRGB();
				if((resetTextColors)||(lastStrokeCol!=newCol)){
					resetTextColors=false;
					lastStrokeCol=newCol;
					drawColor(currentCol,GraphicsState.STROKE);
				}
			}
		}

		//allow for lines as shadows
		//if (text_fill_type == GraphicsState.STROKE && gs.getLineWidth()>=1.0f)
			setLineWidth((int)gs.getLineWidth());
		//else
		//	setLineWidth(0);

		drawFontSize(fontSize);

		if(javaGlyph !=null){


			if(Trm!=null){
				double[] nextAf=new double[]{Trm[0][0],Trm[0][1],Trm[1][0],Trm[1][1],Trm[2][0],Trm[2][1]};

				if((lastAf[0]==nextAf[0])&&(lastAf[1]==nextAf[1])&&
						(lastAf[2]==nextAf[2])&&(lastAf[3]==nextAf[3])){
				}else{

					this.drawAffine(nextAf);
					lastAf[0]=nextAf[0];
					lastAf[1]=nextAf[1];
					lastAf[2]=nextAf[2];
					lastAf[3]=nextAf[3];
				}
			}

			if(!(javaGlyph instanceof Area)) 
				type=-type;

		}else{

			double[] nextAf=new double[6];
			at.getMatrix(nextAf);
			if((lastAf[0]==nextAf[0])&&(lastAf[1]==nextAf[1])&&
					(lastAf[2]==nextAf[2])&&(lastAf[3]==nextAf[3])){
			}else{
				this.drawAffine(nextAf);
				lastAf[0]=nextAf[0];
				lastAf[1]=nextAf[1];
				lastAf[2]=nextAf[2];
				lastAf[3]=nextAf[3];
			}
		}
		
		if(embeddedGlyph==null)
			pageObjects.addElement(javaGlyph);
		else
			pageObjects.addElement(embeddedGlyph);
		
		objectType.addElement(type);
		
		if(type<0){
			areas.addElement(null);
		}else{
			if(javaGlyph!=null){
				if(newCode2){
					
					/**
					 * Using trm coords allows the new highlighting
					 * to identify the text positions and use the 
					 * text based highlight which is much neater
					 * 
					 * Shouldn't break anything, seemed to work fine.
					 */
					//areas.addElement(new Rectangle(0,0,fontSize,fontSize));
					//checkWidth(new Rectangle(0,0,fontSize,fontSize));
				
					areas.addElement(new Rectangle((int)Trm[2][0],(int)Trm[2][1],fontSize,fontSize));
					checkWidth(new Rectangle((int)Trm[2][0],(int)Trm[2][1],fontSize,fontSize));
				}else{
					areas.addElement(((Area) javaGlyph).getBounds());
					checkWidth(((Area) javaGlyph).getBounds());
				}
			}else{
				/**now text*/	
				int realSize=fontSize;
				if(realSize<0)
					realSize=-realSize;
				Rectangle area=new Rectangle((int)Trm[2][0],(int)Trm[2][1],realSize,realSize);

				areas.addElement(area);
				checkWidth(area);	
			}
		}
		
		x_coord=checkSize(x_coord,currentItem);
		y_coord=checkSize(y_coord,currentItem);
		x_coord[currentItem]=(float) Trm[2][0];
		y_coord[currentItem]=(float) Trm[2][1];
		

		currentItem++;

	}

	/**
	 * store fontBounds info
	 */
	public void drawFontBounds(Rectangle newfontBB) {

		pageObjects.addElement(null);
		//pageAT.add(null);
		objectType.addElement(fontBB);
		areas.addElement(null);

		fontBounds.addElement(newfontBB);

		x_coord=checkSize(x_coord,currentItem);
		y_coord=checkSize(y_coord,currentItem);
		x_coord[currentItem]=0;
		y_coord[currentItem]=0;

		currentItem++;

	}

    /**
	 * store af info
	 */
	public void drawAffine(double[] afValues) {

		pageObjects.addElement(null);
		//pageAT.add(null);
		objectType.addElement(AF);
		areas.addElement(null);


		af1.addElement(afValues[0]);
		af2.addElement(afValues[1]);
		af3.addElement(afValues[2]);
		af4.addElement(afValues[3]);

		x_coord=checkSize(x_coord,currentItem);
		y_coord=checkSize(y_coord,currentItem);
		x_coord[currentItem]=(float)afValues[4];
		y_coord[currentItem]=(float)afValues[5];

		currentItem++;

	}

	/**
	 * store af info
	 */
	public void drawFontSize(int fontSize) {

		int realSize=fontSize;
		if(realSize<0)
			realSize=-realSize;

		if(realSize!=lastFS){
			pageObjects.addElement(null);
			//pageAT.add(null);
			objectType.addElement(FONTSIZE);
			areas.addElement(null);

			if(fs==null){
				fs=new Vector_Int(defaultSize);
				fs.setCheckpoint();
			}

			fs.addElement(fontSize);

			x_coord=checkSize(x_coord,currentItem);
			y_coord=checkSize(y_coord,currentItem);
			x_coord[currentItem]=0;
			y_coord[currentItem]=0;

			currentItem++;

			lastFS=realSize;

		}
	}

	/**
	 * store line width info
	 */
	public void setLineWidth(int lineWidth) {

		if(lineWidth!=lastLW ){

			pageObjects.addElement(null);
			//pageAT.add(null);
			objectType.addElement(LINEWIDTH);

			if(lw==null){
				lw=new Vector_Int(defaultSize);
				lw.setCheckpoint();
			}

			lw.addElement(lineWidth);

			x_coord=checkSize(x_coord,currentItem);
			y_coord=checkSize(y_coord,currentItem);
			x_coord[currentItem]=0;
			y_coord[currentItem]=0;

			currentItem++;

			lastLW=lineWidth;

		}

	}

	/**
	 * set affine transform
	 */
	public void renderAffine(AffineTransform at) {
		aff=at;

	}

	/**
	 * @return true if background needed to be added
	 */
	public boolean addBackground() {

		return addBackground;
	}

	/**
	 * @return background color
	 */
	public Color getBackgroundColor() {

		return backgroundColor;
	}

	/**
	 * used by type 3 glyphs to set colour
	 */
	public void lockColors(PdfPaint strokePaint, PdfPaint nonstrokePaint) {

		colorsLocked=true;
		Color strokeColor=Color.white,nonstrokeColor=Color.white;

		if(!strokePaint.isPattern())
			strokeColor=(Color) strokePaint;
		strokeCol=new PdfColor (strokeColor.getRed(),strokeColor.getGreen(),strokeColor.getBlue());

		if(!nonstrokePaint.isPattern())
			nonstrokeColor=(Color) nonstrokePaint;
		fillCol=new PdfColor (nonstrokeColor.getRed(),nonstrokeColor.getGreen(),nonstrokeColor.getBlue());

	}

	/**
	 * Screen drawing using hi res images and not down-sampled images but may be slower
	 * and use more memory<br> Default setting is <b>false</b> and does nothing in
	 * OS version
	 */
	public void setHiResImageForDisplayMode(boolean useHiResImageForDisplay) {
	}

	/**
	 *
	 *
    public void dumpImagesFromMemory() {

        if(tmpFile==null){
            try{

                //trash the images - program will reload
                int count=pageObjects.size();
                for(int i=0;i<count;i++){
                    Object nextObject=pageObjects.elementAt(i);

                    if((nextObject!=null)&&(nextObject instanceof BufferedImage))
                        pageObjects.setElementAt(null,i);

                }

            }catch( Exception e ){
                LogWriter.writeLog( "Exception " + e + " trying to save remove object." );
                System.out.println(e + " trying to save remove object." );
            }
        }

    }*/

	/**
	 * @param optimiseDrawing The optimiseDrawing to set.
	 */
	public void setOptimiseDrawing(boolean optimiseDrawing) {
		this.optimiseDrawing = optimiseDrawing;
	}

	public void setScalingValues(double cropX, double cropH, float scaling) {

		this.cropX=cropX;
		this.cropH=cropH;
		this.scaling=scaling;

	}


	public boolean isImageCached(int pageNumber) {

		if(rawKey==null)
			return objectStoreRef.isImageCached(pageNumber+"_HIRES_"+(currentItem+1));
		else
			return objectStoreRef.isImageCached(pageNumber+"_HIRES_"+(currentItem+1)+ '_' +rawKey);
		//return false;
	}

	/**
	 * rebuild serialised version
	 *
	 * NOT PART OF API and subject to change (DO NOT USE)
	 * @param fonts
	 *
	 */
	public DynamicVectorRenderer(byte[] stream, Map fonts){

		// isRunningOnRemoteClient=true;
		// we use Cannoo to turn our stream back into a DynamicVectorRenderer
		try{
			this.fonts = fonts;

			ByteArrayInputStream bis=new ByteArrayInputStream(stream);

			//read version and throw error is not correct version
			int version=bis.read();
			if(version!=1)
				throw new PdfException("Unknown version in serialised object "+version);

			int isHires=bis.read(); //0=no,1=yes
			if(isHires==1)
				useHiResImageForDisplay=true;
			else
				useHiResImageForDisplay=false;

			pageNumber=bis.read();

			x_coord=(float[]) restoreFromStream(bis);
			y_coord=(float[]) restoreFromStream(bis);

			//read in arrays - opposite of serializeToByteArray();
			//we may need to throw an exception to allow for errors

			text_color = (Vector_Object) restoreFromStream(bis);

			textFillType = (Vector_Int) restoreFromStream(bis);

			//stroke_color = (Vector_Object) restoreFromStream(bis);
			stroke_color = new Vector_Object();
			stroke_color.restoreFromStream(bis);

			//fill_color=(Vector_Object) restoreFromStream(bis);
			fill_color = new Vector_Object();
			fill_color.restoreFromStream(bis);

			stroke = new Vector_Object();
			stroke.restoreFromStream(bis);

			pageObjects = new Vector_Object();
			pageObjects.restoreFromStream(bis);

			javaObjects=(Vector_Object) restoreFromStream(bis);

			shapeType = (Vector_Int) restoreFromStream(bis);

			af1 = (Vector_Double) restoreFromStream(bis);

			af2 = (Vector_Double) restoreFromStream(bis);

			af3 = (Vector_Double) restoreFromStream(bis);

			af4 = (Vector_Double) restoreFromStream(bis);

			fontBounds= new Vector_Rectangle();
			fontBounds.restoreFromStream(bis);

			clips = new Vector_Shape();
			clips.restoreFromStream(bis);

			objectType = (Vector_Int) restoreFromStream(bis);

			opacity=(Vector_Float) restoreFromStream(bis);

			imageOptions = (Vector_Int) restoreFromStream(bis);

			TRvalues = (Vector_Int) restoreFromStream(bis);

			fs = (Vector_Int) restoreFromStream(bis);
			lw = (Vector_Int) restoreFromStream(bis);

			int fontCount=((Integer) restoreFromStream(bis)).intValue();
			for(int ii=0;ii<fontCount;ii++){

				Object key=restoreFromStream(bis);
				Object glyphs=restoreFromStream(bis);
				fonts.put(key,glyphs);
			}

			int alteredFontCount=((Integer) restoreFromStream(bis)).intValue();
			for(int ii=0;ii<alteredFontCount;ii++){

				Object key=restoreFromStream(bis);

				PdfJavaGlyphs updatedFont=(PdfJavaGlyphs) fonts.get(key);

				updatedFont.setDisplayValues((Map) restoreFromStream(bis));
				updatedFont.setCharGlyphs((Map) restoreFromStream(bis));
				updatedFont.setEmbeddedEncs((Map) restoreFromStream(bis));

			}

			bis.close();

		}catch(Exception e){
			//JOptionPane.showMessageDialog(null, "exception deserializing in DVR "+e);
			e.printStackTrace();
		}

		//used in loop to draw so needs to be set
		currentItem=pageObjects.get().length;

	}

	/**stop screen bein cleared on repaint - used by Canoo code
	 * 
	 * NOT PART OF API and subject to change (DO NOT USE)
	 **/
	public void stopClearOnNextRepaint(boolean flag) {
		noRepaint=flag;
	}

	/**
	 * turn object into byte[] so we can move across
	 * this way should be much faster than the stadard Java serialise.
	 *
	 * NOT PART OF API and subject to change (DO NOT USE)
	 *
	 * @throws IOException
	 */
	public byte[] serializeToByteArray(Set fontsAlreadyOnClient) throws IOException{

		ByteArrayOutputStream bos=new ByteArrayOutputStream();

		//add a version so we can flag later changes
		bos.write(1);

		//flag hires
		//0=no,1=yes
		if(useHiResImageForDisplay)
			bos.write(1);
		else
			bos.write(0);

		//save page
		bos.write(pageNumber);

		//the WeakHashMaps are local caches - we ignore

		//we do not copy across hires images

		//we need to copy these in order

		//if we write a count for each we can read the count back and know how many objects
		//to read back

		//write these values first
		//pageNumber;
		//objectStoreRef;
		//isPrinting;

		text_color.trim();
		stroke_color.trim();
		fill_color.trim();
		stroke.trim();
		pageObjects.trim();
		javaObjects.trim();
		stroke.trim();
		pageObjects.trim();
		javaObjects.trim();
		shapeType.trim();
		af1.trim();
		af2.trim();
		af3.trim();
		af4.trim();

		fontBounds.trim();

		clips.trim();
		objectType.trim();
		if(opacity!=null)
			opacity.trim();
		if(imageOptions!=null)
			imageOptions.trim();
		if(TRvalues!=null)
			TRvalues.trim();

		if(fs!=null)
			fs.trim();

		if(lw!=null)
			lw.trim();

		writeToStream(bos,x_coord,"x_coord");
		writeToStream(bos,y_coord,"y_coord");
		writeToStream(bos,text_color,"text_color");
		writeToStream(bos,textFillType,"textFillType");
		//writeToStream(bos,stroke_color,"stroke_color");
		stroke_color.writeToStream(bos);
		//writeToStream(bos,fill_color,"fill_color");
		fill_color.writeToStream(bos);

		int start = bos.size();
		stroke.writeToStream(bos);
		int end = bos.size();

		if(debugStreams)
			System.out.println("stroke = "+((end-start)));

		start = end;
		pageObjects.writeToStream(bos);
		end = bos.size();

		if(debugStreams)
			System.out.println("pageObjects = "+((end-start)));

		writeToStream(bos,javaObjects,"javaObjects");
		writeToStream(bos,shapeType,"shapeType");

		writeToStream(bos,af1,"af1");
		writeToStream(bos,af2,"af2");
		writeToStream(bos,af3,"af3");
		writeToStream(bos,af4,"af4");

		fontBounds.writeToStream(bos);

		start = bos.size();
		clips.writeToStream(bos);
		end = bos.size();

		if(debugStreams)
			System.out.println("clips = "+((end-start)));

		writeToStream(bos,objectType,"objectType");
		writeToStream(bos,opacity,"opacity");
		writeToStream(bos,imageOptions,"imageOptions");
		writeToStream(bos,TRvalues,"TRvalues");

		writeToStream(bos,fs,"fs");
		writeToStream(bos,lw,"lw");

		int fontCount=0,updateCount=0;
		Map fontsAlreadySent=new HashMap();
		Map newFontsToSend=new HashMap();

		for (Iterator iter = fontsUsed.keySet().iterator(); iter.hasNext();) {
			Object fontUsed = iter.next();
			if(!fontsAlreadyOnClient.contains(fontUsed)){
				fontCount++;
				newFontsToSend.put(fontUsed, "x");
			}else{
				updateCount++;
				fontsAlreadySent.put(fontUsed, "x");
			}
		}

		/**
		 * new fonts
		 */
		writeToStream(bos,new Integer(fontCount),"new Integer(fontCount)");

		Iterator keys=newFontsToSend.keySet().iterator();
		while(keys.hasNext()){
			Object key=keys.next();

			if(debugStreams)
				System.out.println("sending font = "+key);

			writeToStream(bos,key,"key");
			writeToStream(bos,fonts.get(key),"font");

			fontsAlreadyOnClient.add(key);
		}

		/**
		 * new data on existing fonts
		 */
		/**
		 * new fonts
		 */
		writeToStream(bos,new Integer(updateCount),"new Integer(existingfontCount)");

		keys=fontsAlreadySent.keySet().iterator();
		while(keys.hasNext()){
			Object key=keys.next();

			if(debugStreams)
				System.out.println("sending font = "+key);

			writeToStream(bos,key,"key");
			PdfJavaGlyphs aa = (PdfJavaGlyphs) fonts.get(key);
			writeToStream(bos,aa.getDisplayValues(),"display");
			writeToStream(bos,aa.getCharGlyphs() ,"char");
			writeToStream(bos,aa.getEmbeddedEncs() ,"emb");

		}

		bos.close();

		fontsUsed.clear();

		if(debugStreams)
			System.out.println("total = "+bos.size());

		return bos.toByteArray();
	}

	/**
	 * generic method to return a serilized object from an InputStream
	 *
	 * NOT PART OF API and subject to change (DO NOT USE)
	 *
	 * @param bis - ByteArrayInputStream containing serilized object
	 * @return - deserilized object
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object restoreFromStream(ByteArrayInputStream bis) throws IOException, ClassNotFoundException{

		//turn back into object
		ObjectInput os=new ObjectInputStream(bis);

		return os.readObject();
	}

	/**
	 * generic method to serilized an object to an OutputStream
	 *
	 * NOT PART OF API and subject to change (DO NOT USE)
	 *
	 * @param bos - ByteArrayOutputStream to serilize to
	 * @param obj - object to serilize
	 * @param string2
	 * @throws IOException
	 */
	public static void writeToStream(ByteArrayOutputStream bos, Object obj, String string2) throws IOException{
		int start = bos.size();

		ObjectOutput os=new ObjectOutputStream(bos);

		os.writeObject(obj);

		int end = bos.size();

		if(debugStreams)
			System.out.println(string2+" = "+((end-start)));

		os.close();
	}

	/**
	 * for font if we are generatign glyph on first render
	 */
	public void checkFontSaved(Object glyph, String name, PdfFont currentFontData) {

		//save glyph at start
		/**now text*/
		pageObjects.addElement(glyph);
		objectType.addElement(MARKER);

		currentItem++;


		//if(currentFontData.isFontSubsetted())
		//	fontsAlreadySent.remove(name);

		if(fontsUsed.get(name)==null || currentFontData.isFontSubsetted()){
			fonts.put(name,currentFontData.getGlyphData());
			fontsUsed.put(name,"x");
		}
	}
	
	public boolean hasObjectsBehind(float[][] CTM) {

		boolean hasObject=false;

		double x=CTM[2][0];
		double y=CTM[2][1];
		double w=CTM[0][0];
		if(w==0)
			w=CTM[0][1];
		double h=CTM[1][1];
		if(h==0)
			h=CTM[1][0];

		Rectangle[] areas=this.areas.get();
		int count=areas.length;
		for(int i=0;i<count;i++){
			if(areas[i]!=null){

				//if h or w are negative, reverse values
				//as intersects and contains can't cope with it
				if(h<0){
					y = y+h;
					h = y-h;
				}

				if(w<0){
					x = x+w;
					w = x-w;
				}

				//Find largest of the compared areas
				//As we must check the larger contains the smaller
				Rectangle large = areas[i];
				Rectangle small = new Rectangle((int)x,(int)y,(int)w,(int)h);

				if(w*h>(areas[i].width*areas[i].height)){
					large = new Rectangle((int)x,(int)y,(int)w,(int)h);
					small = areas[i];
				}

				if(small.intersects(large)||large.contains(small)){
					i=count;
					hasObject=true;
				}
			}
		}

		return hasObject;
	}

	public Rectangle getArea(int i){
		return areas.elementAt(i);
	}

	/**
	 * Rectangle contains code does not handle negative values
	 * Use this instead.
	 * @param area : Rectangle to look in
	 * @param x : value on the x axis
	 * @param y : value on the y axis
	 * @return true is point is within area
	 */
	public static boolean rectangleContains(Rectangle area, int x, int y, int i){

		int lowX = area.x;
		int hiX = area.x+area.width;
		int lowY = area.y;
		int hiY = area.y+area.height;
		boolean containsPoint = false;
		
		//if negative value used swap the lowest and highest point
		if(lowX>hiX){
			int temp = lowX;
			lowX = hiX;
			hiX = temp;
		}

		if(lowY>hiY){
			int temp = lowY;
			lowY = hiY;
			hiY = temp;
		}
		
		if((lowY < y && y < hiY) && (lowX < x && x < hiX))
			containsPoint = true;

		return containsPoint;
	}

	/**
	 * return number of image in display queue
	 * or -1 if none
	 * @return
	 */
	public int isInsideImage(int x,int y){
		int outLine=-1;

		Rectangle[] areas=this.areas.get();
		Rectangle possArea = null;
		int count=areas.length;

		int[] types=objectType.get();
		for(int i=0;i<count;i++){
			if(areas[i]!=null){

				if(rectangleContains(areas[i],x, y,i) && types[i]==IMAGE){
					//Check for smallest image that contains this point
					if(possArea!=null){
						int area1 = possArea.height * possArea.width;
						int area2 = areas[i].height * areas[i].width;
						if(area2<area1)
							possArea = areas[i];
						outLine=i;
					}else{
						possArea = areas[i];
						outLine=i;
					}
//					i=count;
				}
			}
		}
		return outLine;
	}



	public void setObjectStoreRef(ObjectStore objectStoreRef) {
		this.objectStoreRef = objectStoreRef;
	}

	/**
	 * use by type3 fonts to differentiate images in local store
	 */
	public void setType3Glyph(String pKey) {
		this.rawKey=pKey;

		isType3Font=true;

	}

	/**
	 * return copy of image correct way round
	 * (not part of API - used by Storypad)
	 */
	public BufferedImage getLoresImage(String imageName) {

		lastItemPainted=-1;

		//updise down
		int idx=imageName.indexOf('-');
		if(idx!=-1)
			imageName=imageName.substring(idx+1,imageName.length());

		Object id=imageID.get(imageName);

		if(id==null)
			return null;
		else {
			BufferedImage source= (BufferedImage) pageObjects.elementAt(((Integer)id).intValue());


			//turn upside down
			AffineTransform image_at2 =new AffineTransform();
			image_at2.scale(1,-1);
			image_at2.translate(0,-source.getHeight());

			AffineTransformOp invert3= new AffineTransformOp(image_at2,  ColorSpaces.hints);

			BufferedImage image =new BufferedImage(source.getWidth(),source.getHeight(),source.getType());

			invert3.filter(source,image);

			return image;
		}

	}

	public boolean needsHorizontalInvert = false;
	public boolean needsVerticalInvert = false;

	public void saveImage(int id, String des, String type) {

		String name = (String)imageIDtoName.get(new Integer(id));
		BufferedImage image = null;
		if(useHiResImageForDisplay){
			image=objectStoreRef.loadStoredImage(name);

			//if not stored, try in memory
			if(image==null)
				image=(BufferedImage)pageObjects.elementAt(id);
		}else
			image=(BufferedImage)pageObjects.elementAt(id);

		if(image!=null){

			if(!optimisedTurnCode)
				image = invertImage(null, image);

			if(image.getType()==BufferedImage.TYPE_CUSTOM || (type.equals("jpg") && image.getType()==BufferedImage.TYPE_INT_ARGB)){
				image=ColorSpaceConvertor.convertToRGB(image);
				if(image.getType()==BufferedImage.TYPE_CUSTOM)
					JOptionPane.showMessageDialog(null, "This is a custom Image, Java's standard libraries may not be able to save the image as a jpg correctly.\n" +
					"Enabling JAI will ensure correct output. \n\nFor information on how to do this please go to http://www.jpedal.org/flags.php");
			}

			if(needsHorizontalInvert){
				image = invertImageBeforeSave(image, true);
			}

			if(needsVerticalInvert){
				image = invertImageBeforeSave(image, false);
			}

			if(JAIHelper.isJAIused() && type.toLowerCase().startsWith("tif")){
				javax.media.jai.JAI.create("filestore", image, des, type);
			}else if(type.toLowerCase().startsWith("tif"))
				JOptionPane.showMessageDialog(null,"Please setup JAI library for Tiffs");
			else{
				try {
					ImageIO.write(image,type,new File(des));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	//<start-jfr>
	public void setCustomImageHandler(org.jpedal.external.ImageHandler customImageHandler) {
		this.customImageHandler=customImageHandler;
	}
	//<end-jfr>

	/**
	 * operations to do once page done
	 */
	public void flagDecodingFinished() {

		highlightsNeedToBeGenerated=true;
	}

	private void generateHighlights(Graphics2D g2, int count, int[] objectTypes, Object[] pageObjects, float a, float b, float c, float d, double[] afValues1, double[] afValues2, double[] afValues3, double[] afValues4, int[] fsValues, Rectangle[] fontBounds) {

		//flag done for page
		highlightsNeedToBeGenerated=false;

		//array for text highlights
		int[] highlightIDs=new int[count];

		int fsCount=-1,afCount=-1,fontBBCount=0;//note af is 1 behind!

		float x,y;

		Rectangle currentHighlight = null;

		int type;

		float[] top=new float[count];
		float[] bottom=new float[count];
		float[] left=new float[count];
		float[] right=new float[count];
		boolean[] isFontEmbedded =new boolean[count];
		int[] fontSizes =new int[count];
		float[] w=new float[count];

		textHighlightsX=new int[count];
		textHighlightsY=new int[count];
		textHighlightsWidth=new int[count];
		textHighlightsHeight=new int[count];

		/**
		 * get highlights
		 */
		int fontBoundsX=0,fontBoundsY=0,fontBoundsH=1000,fontBoundsW=1000,fontSize=1,realSize=1;

		double matrix[]=new double[6];
		g2.getTransform().getMatrix(matrix);


		//see if rotated
		int pageRotation=0;
		if(matrix[1]<0 && matrix[2]<0)
			pageRotation=270;


		for(int i=0;i<count;i++){

			type=objectTypes[i];

			//Rectangle currentArea=null;


			if(type>0){

				x=x_coord[i];
				y=y_coord[i];

				//put in displacement if text moved up by inversion
				if(realSize<0)
					x=x+realSize;

				Object currentObject=null;

				currentObject=pageObjects[i];

				//System.out.println(i+" raw="+type+" "+currentObject);
				/**
				 * workout area occupied by glyf
				 */
//				if(currentArea!=null){
//				//ignore as already done
//				}else if(type==TEXT && afCount>-1){
//
//				int x1=((Area)currentObject).getBounds().x;
//				int y1=((Area)currentObject).getBounds().y;
//				currentArea=getAreaForGlyph(new float[][]{{(float)afValues1[afCount],(float)afValues2[afCount],0},
//				{(float)afValues3[afCount],(float)afValues4[afCount],0},
//				{x1,y1,1}});
//
//				//currentArea=new Rectangle((int)x,(int)y,fsValues[fsCount],fsValues[fsCount]);
//				//currentArea=(((Area)currentObject).getBounds());
//				}else if(fsCount!=-1 && afValues1!=null){// && afCount>-1){
//				currentArea=new Rectangle((int)x,(int)y,fsValues[fsCount],fsValues[fsCount]);
//				}


				if(type==fontBB){

					currentHighlight=fontBounds[fontBBCount];

					fontBoundsH=currentHighlight.height;
					fontBoundsX=currentHighlight.x;
					fontBoundsY=currentHighlight.y;
					fontBoundsW=currentHighlight.width;
					fontBBCount++;

				}else if(type==FONTSIZE){
					fsCount++;
					realSize=fsValues[fsCount];
					if(realSize<0)
						fontSize=-realSize;
					else
						fontSize=realSize;

				}else if(type==TRUETYPE || type==TYPE1C || type==TEXT){

					//this works in 2 different unit spaces for embedded and non-embedded hence flags
					float scaling=1f;

					if(type==TRUETYPE || type==TYPE1C){
						PdfGlyph raw=((PdfGlyph)currentObject);
						
						scaling=fontSize/1000f;
						
						int gx=raw.getFontBB(PdfGlyph.FontBB_X);
						int gy=fontBoundsY;
						int gw=raw.getFontBB(PdfGlyph.FontBB_WIDTH);
						int gh=(fontBoundsH);

						textHighlightsX[i]=gx;
						textHighlightsY[i]=gy;
						textHighlightsWidth[i]=gw;
						textHighlightsHeight[i]=gh;

						isFontEmbedded[i]=true;
						
//						if(i>383 && i<387)
//						System.out.println(i+" = "+textHighlights[i]+" pageRotation="+pageRotation+" x="+x+" y="+y);

						if(pageRotation==90){
							bottom[i]=-((textHighlightsY[i]*scaling))+x;
							left[i]=(textHighlightsX[i]*scaling)+y;
						}else if(pageRotation==270){
							bottom[i]=((textHighlightsY[i]*scaling))+x;
							left[i]=-((textHighlightsX[i]*scaling)+y);
						}else{ //0 and 180 work the same way
							bottom[i]=((textHighlightsY[i]*scaling))+y;
							left[i]=((textHighlightsX[i]*scaling))+x;
						}
						
						top[i]=bottom[i]+(textHighlightsHeight[i]*scaling);
						right[i]=left[i]+(textHighlightsWidth[i]*scaling);

						w[i]=10; //any non zero number
						fontSizes[i]=fontSize;

//						if(i>30 && i<32)
//						System.out.println(i+" = "+" pageRotation="+
//						pageRotation+" x="+x+" y="+y+" right[i]="+right[i]+" left[i]="+left[i]+" "+currentArea+" font bounds="+currentHighlight);//+" "+
//						((Area)currentObject).getBounds());

					}else{
						scaling=1f;

						float scale=1000f/fontSize;
						textHighlightsX[i]=(int)x;
						textHighlightsY[i]=(int)(y+(fontBoundsY/scale));
						textHighlightsWidth[i]=(int)((fontBoundsW)/scale);
						textHighlightsHeight[i]=(int)((fontBoundsH-fontBoundsY)/scale);


						if(pageRotation==90){
							bottom[i]=-textHighlightsY[i];
							left[i]=textHighlightsX[i];
						}else if(pageRotation==270){
							bottom[i]=(textHighlightsY[i]);
							left[i]=-textHighlightsX[i];
						}else{ //0 and 180 work the same way
							bottom[i]=textHighlightsY[i];
							left[i]=textHighlightsX[i];
						}

						top[i]=bottom[i]+textHighlightsHeight[i];
						right[i]=left[i]+textHighlightsWidth[i];

						w[i]=((Area)currentObject).getBounds().width;

						fontSizes[i]=fontSize;

//						if(textHighlights[i].y>515 && textHighlights[i].y<540 && textHighlights[i].x<101)
//						System.out.println(i+" "+textHighlights[i]+" "+currentHighlight+" fontSize="+fontSize);
						// if(y>750 && y<766)
						//  System.out.println(x+" "+y+" "+fontSize+" "+textHighlights[i]+" scale="+scale+" bounds="+fontBounds[0]+" "+fontBounds[1]+" "+fontBounds[2]+" "+fontBounds[3]);
					}
					highlightIDs[i]=i;

				}
			}
		}

		//sort highlights
		//highlightIDs=Sorts.quicksort(left,bottom,highlightIDs);

		int zz=-31;
		//scan each and adjust so it touches next
		//if(1==2)
		for(int aa=0;aa<count-1;aa++){

			int ptr=highlightIDs[aa];

			{//if(textHighlights[ptr]!=null){

				if(ptr==zz)
					System.out.println("*"+ptr+" = "+" left="+left[ptr]+
							" bottom="+bottom[ptr]+" right="+right[ptr]+" top="+top[ptr]);

				int gap=0;
				for(int next=aa+1;next<count;next++){
					int nextPtr=highlightIDs[next];

					//skip empty
					if(isFontEmbedded[nextPtr]!=isFontEmbedded[ptr] || w[nextPtr]<1)
						continue;

					if(ptr==zz)
						System.out.println("compare with="+nextPtr+" left="+left[nextPtr]+" right="+right[nextPtr]+" "+(left[nextPtr]>left[ptr] && left[nextPtr]<right[ptr]));

					//find glyph on right
					if((left[nextPtr]>left[ptr] && left[nextPtr]<right[ptr])||(left[nextPtr]>((left[ptr]+right[ptr])/2) && right[ptr]<right[nextPtr])){

						int currentW=textHighlightsWidth[ptr];
						int currentH=textHighlightsHeight[ptr];
						int currentX=textHighlightsX[ptr];
						int currentY=textHighlightsY[ptr];

						if(isFontEmbedded[nextPtr]){
							float diff=left[nextPtr]-right[ptr];

							///if(left[nextPtr]<0 && right[nextPtr]<0)
							//diff=-diff;

							//System.out.println("1diff="+diff);

							if(diff>0)
								diff=diff+.5f;
							else
								diff=diff+.5f;

							gap=(int)(((diff*1000f/fontSizes[ptr])));

							if(textHighlightsX[nextPtr]>0)
								gap=gap+textHighlightsX[nextPtr];

						}else
							gap=(int)(left[nextPtr]-right[ptr]);



						if(ptr==zz)
							System.out.println((left[nextPtr]-right[ptr])+" gap="+gap+" "+(((left[nextPtr]-right[ptr])*1000f/fontSizes[ptr]))+" currentX="+currentX+" scaling="+scaling+" "+fontBoundsW);

						boolean isCorrectLocation=(gap>0 ||(gap<0 && left[ptr]<left[nextPtr] && right[ptr]>left[nextPtr] && right[ptr]<right[nextPtr] && left[ptr]<right[ptr] &&
								( (-gap< fontSizes[ptr] && !isFontEmbedded[ptr])|| (-gap< fontBoundsW && isFontEmbedded[ptr]))));
						if(bottom[ptr]<top[nextPtr] && bottom[nextPtr]<top[ptr] && (gap>0 || isCorrectLocation)){
							if(isCorrectLocation &&   ((!isFontEmbedded[ptr] && gap<fontSizes[ptr] && currentW+gap<fontSizes[ptr]) || (isFontEmbedded[ptr] && gap<fontBoundsW))){



								if(ptr==zz)
									System.out.println(nextPtr+" = "+" left="+left[nextPtr]+
											" bottom="+bottom[nextPtr]+" right="+right[nextPtr]+" top="+top[nextPtr]);

								if(isFontEmbedded[ptr]){

									if(gap>0){
										textHighlightsWidth[ptr]=currentW+gap;
										//textHighlightsX[nextPtr]=textHighlightsX[nextPtr]-half-1;
									}else{
										textHighlightsWidth[ptr]=currentW-gap;
									}

								}else if(gap>0)
									textHighlightsWidth[ptr]=gap;
								else
									textHighlightsWidth[ptr]=currentW+gap;

								if(ptr==zz){
									System.out.println("new="+textHighlightsWidth[ptr]);
								}

								next=count;
							}else if(gap>fontBoundsW){ //off line so exit
								//next=count;
							}
						}
					}
				}
			}
		}

	}



	public void setPrintPage(int currentPrintPage) {
		printPage = currentPrintPage;
	}

	//used internally - please do not use
	public ObjectStore getObjectStore() {
		return objectStoreRef;
	}


	/**
	 * used internally - please do not use
	 */
	public void setOptimisedRotation(boolean value) {
		optimisedTurnCode=value;

	}
	
	 /**
	 * return number of image in display queue
	 * or -1 if none
	 * @return
	 */
	public int getObjectUnderneath(int x,int y){
		int typeFound=-1;
		Rectangle[] areas=this.areas.get();
		Rectangle possArea = null;
		int count=areas.length;

		int[] types=objectType.get();
		boolean nothing = true;
		for(int i=count-1;i>-1;i--){
			if(areas[i]!=null){
				if(rectangleContains(areas[i],x, y,i)){
					if(types[i] != DynamicVectorRenderer.SHAPE && types[i] != DynamicVectorRenderer.CLIP){
						nothing = false;
						typeFound = types[i];
						i=-1;
					}
				}
			}
		}
		
		if(nothing)
			return -1;
		
		
		return typeFound;
	}
}

