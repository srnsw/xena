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
* PdfPanel.java
* ---------------
*/
package org.jpedal;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.dnd.DropTarget;

import java.awt.event.MouseEvent;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import java.util.Map;

import javax.swing.border.Border;

import org.jpedal.io.ObjectStore;

import org.jpedal.objects.PdfPageData;

//<start-jfr>

//<start-adobe>
import org.jpedal.objects.PageLines;
//<end-adobe>

import org.jpedal.objects.PdfData;

import org.jpedal.objects.PrinterOptions;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.layers.PdfLayerList;

import org.jpedal.objects.acroforms.rendering.AcroRenderer;

//<end-jfr>
import org.jpedal.render.DynamicVectorRenderer;

import org.jpedal.utils.repositories.Vector_Int;
import org.jpedal.utils.repositories.Vector_Rectangle;
import org.jpedal.utils.repositories.Vector_Shape;

import javax.swing.*;


/**
 * Do not create an instance of this class - provides GUI functionality for
 * PdfDecoder class to extend
 */
public class PdfPanel extends JPanel{

	private static final long serialVersionUID = -5480323101993399978L;
	
	/** Not part of the JPedal API - Required for Storypad */
	public JPanel[] extraButton;
	public boolean useParentButtons = false;

    protected PdfLayerList layers;    

    /** Holds the x,y,w,h of the current highlighted image, null if none */
	int[] highlightedImage = null;
	
	/** Enable / Disable Point and Click image extraction */
	private boolean ImageExtractionAllowed = true;

	//<start-jfr>
	protected Display pages;

	/** holds the extracted AcroForm data */
	//protected PdfFormData currentAcroFormData;

    //PdfArrayIterator fieldList=null;

	/**default renderer for acroforms*/
	protected AcroRenderer formRenderer;//=new DefaultAcroRenderer();

	/**hotspots for display*/
	//Hotspots displayHotspots;

	/**hotspots for printing*/
	//Hotspots printHotspots;

	//<start-adobe>
	/**holds page lines*/
	protected PageLines pageLines;
	
	
	//<end-adobe>

	//<end-jfr>

	/** 
	 * The colour of the highlighting box around the text
	 */
	public static Color highlightColor = new Color(10,100,170);
	
	/** 
	 * The colour of the text once highlighted
	 */
	public static Color backgroundColor = null;

	/** 
	 * The transparency of the highlighting box around the text stored as a float
	 */
	public static float highlightComposite = 0.35f;

    protected Rectangle[] alternateOutlines;
	String altName;

	/**tracks indent so changing to continuous does not disturb display*/
	private int lastIndent=-1;

	//<start-jfr>
	PageOffsets currentOffset;

	/**copy of flag to tell program whether to create
	 * (and possibly update) screen display
	 */
	protected boolean renderPage = false;

	/**type of printing*/
	protected boolean isPrintAutoRotateAndCenter=false;

	/**flag to show we use PDF page size*/
	protected boolean usePDFPaperSize=false;

	/**page scaling mode to use for printing*/
	protected int pageScalingMode=PrinterOptions.PAGE_SCALING_REDUCE_TO_PRINTER_MARGINS;

	//<end-jfr>

	/**display mode (continuous, facing, single)*/
	protected int displayView=Display.SINGLE_PAGE;

	/**amount we scroll screen to make visible*/
	private int scrollInterval=10;

	/** count of how many pages loaded */
	protected int pageCount = 0;

	/**
	 * if true
	 * show the crop box as a box with cross on it
	 * and remove the clip.
	 */
	private boolean showCrop = false;

	/** when true setPageParameters draws the page rotated for use with scale to window */
    boolean isNewRotationSet=false;

	/** displays the viewport border */
	protected boolean displayViewportBorder=false;

	/**flag to stop multiple attempts to decode*/
	protected boolean isDecoding=false;

	protected int alignment=Display.DISPLAY_LEFT_ALIGNED;

	/** used by setPageParameters to draw rotated pages */
	protected int displayRotation=0;

	/**current cursor location*/
	private Point current_p;

	/**allows user to create viewport on page and scale to this*/
	protected Rectangle viewableArea=null;

	/**shows merging for debugging*/
	private Vector_Int merge_level ;
	private Vector_Shape merge_outline;
	private boolean[] showDebugLevel;
	private Color[] debugColors;
	private boolean showMerging=false;

	/**used to draw demo cross*/
	AffineTransform demoAf=null;

	/**repaint manager*/
	private RepaintManager currentManager=RepaintManager.currentManager(this);

	/**current page*/
	protected int pageNumber=1;

	/**used to reduce or increase image size*/
	protected AffineTransform displayScaling;

	/**
	 * used to apply the imageable area to the displayscaling, used instead of
	 * displayScaling, as to preserve displayScaling
	 */
	protected AffineTransform viewScaling=null;

	/** holds page information used in grouping*/
	protected PdfPageData pageData = new PdfPageData();

	/**used to track highlight*/
	private Rectangle lastHighlight=null;

	/**rectangle drawn on screen by user*/
	protected Rectangle cursorBoxOnScreen = null,lastCursorBoxOnScreen=null;

	/** whether the cross-hairs are drawn */
	private boolean drawCrossHairs = false;

	/** which box the cursor is currently positioned over */
	private int boxContained = -1;

	/** color to highlight selected handle */
	private Color selectedHandleColor = Color.red;

	/** the gap around each point of reference for cursorBox */
	private int handlesGap = 5;

	/**colour of highlighted rectangle*/
	private Color outlineColor;

	/**rectangle of object currently under cursor*/
	protected Rectangle currentHighlightedObject = null;

	/**colour of a shape we highlight on the page*/
	private Color outlineHighlightColor;

	/**preferred colour to highliht page*/
	private Color[] highlightColors;

	/**gap around object to repaint*/
	static final private int strip=2;

	/**highlight around selected area*/
	private Rectangle2D[] outlineZone = null;

	private int[] processedByRegularExpression=null;

	/**allow for inset of display*/
	protected int insetW=0,insetH=0;

	/**flag to show if area selected*/
	private boolean[] highlightedZonesSelected = null;

	private boolean[] hasDrownedObjects = null;

	/**user defined viewport*/
	Rectangle userAnnot=null;

	/** default height width of bufferedimage in pixels */
	private int defaultSize = 100;

	/**height of the BufferedImage in pixels*/
    int y_size = defaultSize;

	/**unscaled page height*/
    int max_y;
	
	/**unscaled page Width*/
    int max_x;

	/**width of the BufferedImage in pixels*/
    int x_size = defaultSize;

	/**used to plot selection*/
	int[] cx=null,cy=null;

	/**any scaling factor being used to convert co-ords into correct values
	 * and to alter image size
	 */
	protected float scaling=1;

	/**mode for showing outlines*/
	private int highlightMode = 0;

	/**flag for showing all object outlines in PDFPanel*/
	public static final int SHOW_OBJECTS = 1;

	/**flag for showing all lines on page used for grouping */
	public static final int SHOW_LINES = 2;

	/**flag for showing all lines on page used for grouping */
	public static final int SHOW_BOXES = 4;

	/**size of font for selection order*/
	protected int size=20;

	/**font used to show selection order*/
	protected Font highlightFont=null;

	/**border for component*/
	protected Border myBorder=null;

	protected DropTarget dropTarget = null;

	/** the ObjectStore for this file */
	protected ObjectStore objectStoreRef = new ObjectStore();

	/**the actual display object*/
	protected DynamicVectorRenderer currentDisplay=new DynamicVectorRenderer(1,objectStoreRef,false); //

	/**flag to show if border appears on printed output*/
	protected boolean useBorder=true;

	private int[] selectionOrder;

	/**stores area of arrays in which text should be highlighted*/
	private Rectangle[] areas;
	private int[] areaDirection;

	private Object[] linkedItems,children;

	private int[] parents;

	protected boolean useAcceleration=true;

	/**all text blocks as a shape*/
	private Shape[] fragmentShapes;

	int x_size_cropped;

	int y_size_cropped;

	private AffineTransform cursorAf;

	private Rectangle actualBox;

	private boolean drawInteractively=false;

	protected int lastFormPage=-1,lastStart=-1,lastEnd=-1;

	private int pageUsedForTransform;

	protected int additionalPageCount=0,xOffset=0;

	private boolean displayForms = true;

	//private GraphicsDevice currentGraphicsDevice = null;
	public boolean extractingAsImage = false;

	private int highlightX = 0;

	private int highlightY = 0;
	
	public void setExtractingAsImage(boolean extractingAsImage) {
		this.extractingAsImage = extractingAsImage;
	}
	
	//<start-adobe>
	public void initNonPDF(PdfDecoder pdf){

		pages=new SingleDisplay(pageNumber,pageCount,currentDisplay);

		pages.setup(true,null, pdf);
	}

	/**workout combined area of shapes are in an area*/
	public  Rectangle getCombinedAreas(Rectangle targetRectangle,boolean justText){
		if(this.currentDisplay!=null)
			return currentDisplay.getCombinedAreas(targetRectangle, justText);
		return
		null;
	}

	/**
	 * put debugging info for grouping onscreen
	 * to aid in developing and debugging merging algorithms used by Storypad -
	 * (NOT PART OF API and subject to change)
	 */
	final public void addMergingDisplayForDebugging(Vector_Int merge_level,
			Vector_Shape merge_outline,int count,Color[] colors) {

		this.merge_level=merge_level;
		this.merge_outline=merge_outline;
		this.showDebugLevel=new boolean[count];
		this.debugColors=colors;

	}


	/**
	 * debug zone for Storypad - not part of API
	 */
	final public void setDebugView(int level, boolean enabled){
		if(showDebugLevel!=null)
			showDebugLevel[level]=enabled;
	}
	//<end-adobe>

	/**
	 * get pdf as Image of current page with height as required height in pixels -
	 * Page must be decoded first - used to generate thubnails for display.
	 * Use decodePageAsImage forcreating images of pages
	 */
	final public BufferedImage getPageAsThumbnail(int height,DynamicVectorRenderer currentDisplay) {

		if(currentDisplay==null){

			currentDisplay=this.currentDisplay;

			/**
			 * save to disk
			 */
			ObjectStore.cachePage(new Integer(pageNumber), currentDisplay);

		}

		BufferedImage image = getImageFromRenderer(height,currentDisplay,pageNumber);


		return image;

	}

	/**
	 */
	protected BufferedImage getImageFromRenderer(int height,DynamicVectorRenderer rend,int pageNumber) {

		//int mediaBoxW = pageData.getMediaBoxWidth(pageNumber);
		int mediaBoxH = pageData.getMediaBoxHeight(pageNumber);
		int mediaBoxX = pageData.getMediaBoxX(pageNumber);
		int mediaBoxY = pageData.getMediaBoxY(pageNumber);

		int crw=pageData.getCropBoxWidth(pageNumber);
		int crh=pageData.getCropBoxHeight(pageNumber);
		int crx=pageData.getCropBoxX(pageNumber);

		int cry=pageData.getCropBoxY(pageNumber);
		if(cry>0)
			cry=mediaBoxH-crh-cry;

		float scale=(float) height/(crh);

		int rotation=pageData.getRotation(pageNumber);

		/**allow for rotation*/
		int dr=-1;

		if((rotation==90)|(rotation==270)){
			int tmp=crw;
			crw=crh;
			crh=tmp;
			dr=1;

			tmp=crx;
			crx=cry;
			cry=tmp;
		}

		AffineTransform scaleAf = getScalingForImage(pageNumber,rotation,scale);//(int)(mediaBoxW*scale), (int)(mediaBoxH*scale),
		int cx=mediaBoxX-crx,cy=mediaBoxY-cry;

		scaleAf.translate(cx,dr*cy);
		return rend.getPageAsImage(scale,crx,cry,crw,crh,pageNumber,scaleAf,BufferedImage.TYPE_INT_RGB);

	}

	//<start-adobe>

	/**set zones we want highlighted onscreen
	 * @deprecated
	 * please look at  setFoundTextAreas(Rectangle areas),setHighlightedAreas(Rectangle[] areas)
	 * <b>This is NOT part of the API</b> (used in Storypad)
	 */
	final public void setHighlightedZones(
			int mode,
			int[] cx,int[] cy,
			Shape[] fragmentShapes,
			Object[] linkedItems,
			int[] parents,
			Object[] childItems,
			int[] childParents,
			Rectangle2D[] outlineZone,
			boolean[] highlightedZonesSelected,boolean[] hasDrownedObjects,Color[] highlightColors,int[] selectionOrder,int[] processedByRegularExpression) {

		this.cx=cx;
		this.cy=cy;
		this.fragmentShapes=fragmentShapes;
		this.linkedItems=linkedItems;
		this.parents=parents;

		this.children=childItems;

		this.outlineZone = outlineZone;
		this.processedByRegularExpression=processedByRegularExpression;

		this.highlightedZonesSelected = highlightedZonesSelected;
		this.hasDrownedObjects = hasDrownedObjects;
		this.highlightMode = mode;
		this.highlightColors=highlightColors;
		this.selectionOrder=selectionOrder;

		//and deselect alt highlights
		this.alternateOutlines=null;


	}

	/**set merging option for Storypad (not part of API)*/
	public void setDebugDisplay(boolean isEnabled){
		this.showMerging=isEnabled;
	}

	/**
	 * set an inset display so that display will not touch edge of panel*/
	final public void setInset(int width,int height) {
		this.insetW=width;
		this.insetH=height;
	}

	/**
	 * make screen scroll to ensure point is visible
	 */
	public void ensurePointIsVisible(Point p){
		super.scrollRectToVisible(new Rectangle(p.x,y_size-p.y,scrollInterval,scrollInterval));
	}
	//<end-adobe>

	/**
	 * get sizes of panel <BR>
	 * This is the PDF pagesize (as set in the PDF from pagesize) -
	 * It now includes any scaling factor you have set (ie a PDF size 800 * 600
	 * with a scaling factor of 2 will return 1600 *1200)
	 */
	final public Dimension getMaximumSize() {

		Dimension pageSize=null;

		if(displayView!=Display.SINGLE_PAGE)
			pageSize = pages.getPageSize(displayView);

		if(pageSize==null){
			if((displayRotation==90)|(displayRotation==270))
				pageSize= new Dimension((int)(y_size_cropped+insetW+insetW+(xOffset*scaling)+(additionalPageCount*(insetW+insetW))),x_size_cropped+insetH+insetH);
			else
				pageSize= new Dimension((int)(x_size_cropped+insetW+insetW+(xOffset*scaling)+(additionalPageCount*(insetW+insetW))),y_size_cropped+insetH+insetH);

		}

        if(pageSize==null)
        pageSize=getMinimumSize();

        return pageSize;

	}

	/**
	 * get width*/
	final public Dimension getMinimumSize() {

		return new Dimension(100+insetW,100+insetH);
	}

	/**
	 * get sizes of panel <BR>
	 * This is the PDF pagesize (as set in the PDF from pagesize) -
	 * It now includes any scaling factor you have set (ie a PDF size 800 * 600
	 * with a scaling factor of 2 will return 1600 *1200)
	 */
	public Dimension getPreferredSize() {
		return getMaximumSize();
	}
	
	public Rectangle[] getHighlightedAreas(){

		if(areas==null)
			return null;
		else{
			int count=areas.length;

			Rectangle[] returnValue=new Rectangle[count];

			for(int ii=0;ii<count;ii++){
				if(areas[ii]==null)
					returnValue[ii]=null;
				else
					returnValue[ii]=new Rectangle(areas[ii].x,areas[ii].y,
							areas[ii].width,areas[ii].height);
			}
			return returnValue;
		}
	}
	
	/**
	 * Highlights a section of lines that form a paragraph
	 */
	public Rectangle setFoundParagraph(int x, int y){
		Rectangle[] lines = PdfHighlights.getLineAreas();
		if(lines!=null){
			Rectangle point = new Rectangle(x,y,1,1);
			Rectangle current = new Rectangle(0,0,0,0);
			boolean lineFound = false;
			int selectedLine = 0;

			for(int i=0; i!=lines.length; i++){
				if(lines[i].intersects(point)){
					selectedLine = i;
					lineFound = true;
					break;
				}
			}

			if(lineFound){
				double left = lines[selectedLine].x;
				double cx = lines[selectedLine].getCenterX();
				double right = lines[selectedLine].x+lines[selectedLine].width;
				double cy = lines[selectedLine].getCenterY();
				int h = lines[selectedLine].height;

				current.x=lines[selectedLine].x;
				current.y=lines[selectedLine].y;
				current.width=lines[selectedLine].width;
				current.height=lines[selectedLine].height;

				boolean foundTop = true;
				boolean foundBottom = true;
				Vector_Rectangle selected = new Vector_Rectangle(0);
				selected.addElement(lines[selectedLine]);

				while(foundTop){
					foundTop = false;
					for(int i=0; i!=lines.length; i++){
						if(lines[i].contains(left, cy+h) || lines[i].contains(cx, cy+h) || lines[i].contains(right, cy+h)){
							selected.addElement(lines[i]);
							foundTop = true;
							cy = lines[i].getCenterY();
							h = lines[i].height;

							if(current.x>lines[i].x){
								current.width = (current.x+current.width)-lines[i].x;
								current.x = lines[i].x;
							}
							if((current.x+current.width)<(lines[i].x+lines[i].width))
								current.width = (lines[i].x+lines[i].width)-current.x;
							if(current.y>lines[i].y){
								current.height = (current.y+current.height)-lines[i].y;
								current.y = lines[i].y;
							}
							if((current.y+current.height)<(lines[i].y+lines[i].height)){
								current.height = (lines[i].y+lines[i].height)-current.y;
							}

							break;
						}
					}
				}

				//Return to selected item else we have duplicate highlights
				left = lines[selectedLine].x;
				cx = lines[selectedLine].getCenterX();
				right = lines[selectedLine].x+lines[selectedLine].width;
				cy = lines[selectedLine].getCenterY();
				h = lines[selectedLine].height;

				while(foundBottom){
					foundBottom = false;
					for(int i=0; i!=lines.length; i++){
						if(lines[i].contains(left, cy-h) || lines[i].contains(cx, cy-h) || lines[i].contains(right, cy-h)){
							selected.addElement(lines[i]);
							foundBottom = true;
							cy = lines[i].getCenterY();
							h = lines[i].height;

							if(current.x>lines[i].x){
								current.width = (current.x+current.width)-lines[i].x;
								current.x = lines[i].x;
							}
							if((current.x+current.width)<(lines[i].x+lines[i].width))
								current.width = (lines[i].x+lines[i].width)-current.x;
							if(current.y>lines[i].y){
								current.height = (current.y+current.height)-lines[i].y;
								current.y = lines[i].y;
							}
							if((current.y+current.height)<(lines[i].y+lines[i].height)){
								current.height = (lines[i].y+lines[i].height)-current.y;
							}

							break;
						}
					}
				}
				selected.trim();
				areas = selected.get();
				return current;
			}
			return null;
		}
		return null;
	}
	
	//<start-adobe>
    
	/**
     * Highlight similar to adobe. Highlighting will take place in rendering order
     * from the text item at startPoint to the text item at endPoint
     */
	public void setFoundTextPoints(Point startPoint, Point endPoint) {
		
		//Ensure highlighting takes place
		boolean nothingToHighlight = false;
		
		//both null flushes areas
		if(startPoint==null && endPoint==null){
			
			areas=null;
		}else{

			if(areas==null){
				this.areas=new Rectangle[1];
				//This is the first highlight, ensure it highlights something
				nothingToHighlight = true;
			}

			Rectangle[] lines = PdfHighlights.getLineAreas();
			int[] writingMode = PdfHighlights.getLineWritingMode();

			int start = -1;
			int finish = -1;
			boolean backward = false;
			//Find the first selected line and the last selected line.
			if(lines!=null){
				for(int i=0; i!= lines.length; i++){

					if(lines[i].contains(startPoint))
						start = i;

					if(lines[i].contains(endPoint))
						finish = i;

					if(start!=-1 && finish!=-1){
						break;
					}

				}

				if(start>finish){
					int temp = start;
					start = finish;
					finish = temp;
					backward = true;
				}

				if(start!=-1 && finish!=-1){
					//Fill in all the lines between
					areas = new Rectangle[finish-start+1];

					for(int i=0; i<=(finish-start); i++){
						areas[i] = lines[start+i];
					}

					if(areas.length>0){	
						int top = 0;
						int bottom = areas.length-1;

						if(areas[top]!=null && areas[bottom]!=null){

							switch(writingMode[start]){
							case PdfData.HORIZONTAL_LEFT_TO_RIGHT : 
								// if going backwards
								if(backward){
									if((endPoint.x-15)<=areas[top].x){
										//Do nothing to areas as we want to pick up the start of a line
									}else{
										areas[top].width = areas[top].width-(endPoint.x-areas[top].x);
										areas[top].x = endPoint.x;
									}

								}else{
									if((startPoint.x-15)<=areas[top].x){
										//Do nothing to areas as we want to pick up the start of a line
									}else{
										areas[top].width = areas[top].width-(startPoint.x-areas[top].x);
										areas[top].x = startPoint.x;
									}

								}
								break;
							case PdfData.HORIZONTAL_RIGHT_TO_LEFT:
								break;
							case PdfData.VERTICAL_TOP_TO_BOTTOM:
								if(backward){
									if((endPoint.y-15)<=areas[top].y){
										//Do nothing to areas as we want to pick up the start of a line
									}else{
										areas[top].height = areas[top].height-(endPoint.y-areas[top].y);
										areas[top].y = endPoint.y;
									}

								}else{
									if((startPoint.y-15)<=areas[top].y){
										//Do nothing to areas as we want to pick up the start of a line
									}else{
										areas[top].height = areas[top].height-(startPoint.y-areas[top].y);
										areas[top].y = startPoint.y;
									}

								}
								break;
							case PdfData.VERTICAL_BOTTOM_TO_TOP : 
								if(backward){
									if((endPoint.y-15)<=areas[top].y){
										//Do nothing to areas as we want to pick up the start of a line
									}else{
										areas[top].height = areas[top].height-(endPoint.y-areas[top].y);
										areas[top].y = endPoint.y;
									}

								}else{
									if((startPoint.y-15)<=areas[top].y){
										//Do nothing to areas as we want to pick up the start of a line
									}else{
										areas[top].height = areas[top].height-(startPoint.y-areas[top].y);
										areas[top].y = startPoint.y;
									}

								}
								break;
							}


							switch(writingMode[finish]){
							case PdfData.HORIZONTAL_LEFT_TO_RIGHT : 
								// if going backwards
								if(backward){
									if((startPoint.x+15)>=areas[bottom].x+areas[bottom].width){
										//Do nothing to areas as we want to pick up the end of a line
									}else{
										areas[bottom].width = startPoint.x - areas[bottom].x;
									}

								}else{
									if((endPoint.x+15)>=areas[bottom].x+areas[bottom].width){
										//Do nothing to areas as we want to pick up the end of a line
									}else
										areas[bottom].width = endPoint.x - areas[bottom].x;
								}
								break;
							case PdfData.HORIZONTAL_RIGHT_TO_LEFT:
								break;
							case PdfData.VERTICAL_TOP_TO_BOTTOM:
								// if going backwards
								if(backward){
									if((startPoint.y+15)>=areas[bottom].y+areas[bottom].height){
										//Do nothing to areas as we want to pick up the end of a line
									}else{
										areas[bottom].height = startPoint.y - areas[bottom].y;
									}

								}else{
									if((endPoint.y+15)>=areas[bottom].y+areas[bottom].height){
										//Do nothing to areas as we want to pick up the end of a line
									}else
										areas[bottom].height = endPoint.y - areas[bottom].y;
								}
								break;
							case PdfData.VERTICAL_BOTTOM_TO_TOP : 
								// if going backwards
								if(backward){
									if((startPoint.y+15)>=areas[bottom].y+areas[bottom].height){
										//Do nothing to areas as we want to pick up the end of a line
									}else{
										areas[bottom].height = startPoint.y - areas[bottom].y;
									}

								}else{
									if((endPoint.y+15)>=areas[bottom].y+areas[bottom].height){
										//Do nothing to areas as we want to pick up the end of a line
									}else
										areas[bottom].height = endPoint.y - areas[bottom].y;
								}
								break;
							}
						}
					}
				}else {
					//This is the first highlight and nothing was selected
					if(nothingToHighlight){
						//Prevent text extraction on nothing
						areas = null;
					}
				}
			}
		}
	}
	
	/**
	 * Method to highlight multiple found areas
	 * @param newAreas :: The rectangles to highlight on the page
	 */
	public void setSearchHighlightAreas(Rectangle[] newAreas){
		if(this.displayView!=Display.SINGLE_PAGE){
			this.areas=null;
			return;
		}
        
        if(areas==null){
        	areas = new Rectangle[1];
        	areaDirection = new int[1];
        }
        
        Vector_Rectangle areasSelected = new Vector_Rectangle(0);
        Vector_Int areasDirection = new Vector_Int(0);
        if(newAreas!=null){ //add
                int count=newAreas.length;
                for(int ii=0;ii<count;ii++){
                	setSearchHighlightArea(newAreas[ii]);
                	
                	for(int i=0; i!=areas.length; i++){
                		areasSelected.addElement(areas[i]);
                	}

                	for(int i=0; i!=areaDirection.length; i++){
                		areasDirection.addElement(areaDirection[i]);
                	}
                	
                }
                
                areasSelected.trim();
                areas = areasSelected.get();
                
                areasDirection.trim();
                areaDirection = areasDirection.get();
                
        }else
            areas=null;

        pages.refreshDisplay();

	}
	
	/**
	 * Method to highlight a single found area from searching
	 * A value of null passsed in will flush the highlight areas
	 * @param rectArea :: The rectangle to highlight on the page or null to flush areas
	 */
	public void setSearchHighlightArea(Rectangle rectArea){
		
		if(rectArea==null){
			areas = new Rectangle[0];
			areaDirection = new int[0];
			return;
		}
		
		//Get line and writing direction data
		Rectangle[] lines = PdfHighlights.getLineAreas();
		int[] writingMode = PdfHighlights.getLineWritingMode();

		//Find all lines that intersect with selection area
		if(lines!=null){
			int maxIntersection = 0;
			int foundItem = 0;
			//Add new areas that intersect, if any
			for(int i=0; i!= lines.length; i++){
				if(lines[i]!=null && rectArea.intersects(lines[i])){
					Rectangle intersection = rectArea.intersection(lines[i]);
					if(maxIntersection<(intersection.height * intersection.width)){
						maxIntersection = (intersection.height * intersection.width);
						foundItem = i;
					}
				}
			}
			
			areas = new Rectangle[]{lines[foundItem]};
			areaDirection = new int[]{writingMode[foundItem]};
			
			//If highlighting just one line or searching
			//just adjust the on screen width
			switch(areaDirection[0]){
			case PdfData.HORIZONTAL_LEFT_TO_RIGHT:
					areas[0].x = rectArea.x;
					areas[0].width = rectArea.width;
				break;
			case PdfData.HORIZONTAL_RIGHT_TO_LEFT:
				break;
			case PdfData.VERTICAL_TOP_TO_BOTTOM:
				if((rectArea.y + rectArea.height)>(areas[0].y + areas[0].height)){
					areas[0].height = areas[0].height - (rectArea.y - areas[0].y);
					areas[0].y = rectArea.y;
				}else{ 
					if(rectArea.y<areas[0].y){
						areas[0].height = (rectArea.y + rectArea.height)-areas[0].y;
					}else{
						areas[0].y = rectArea.y;
						areas[0].height = rectArea.height;
					}
				}

				break;
			case PdfData.VERTICAL_BOTTOM_TO_TOP:
				if((rectArea.y + rectArea.height)>(areas[0].y + areas[0].height)){
					areas[0].height = areas[0].height - (rectArea.y - areas[0].y);
					areas[0].y = rectArea.y;
				}else{ 
					if(rectArea.y<areas[0].y){
						areas[0].height = (rectArea.y + rectArea.height)-areas[0].y;
					}else{
						areas[0].y = rectArea.y;
						areas[0].height = rectArea.height;
					}
				}break;
			}
		}
	}

	/**
	 * Deprecated. Please use either setMouseHighlightArea or setSearchTextArea if called from a search routine
	 * @param rectArea :: area of highlight on screen
	 * @deprecated
	 */
	public void setFoundTextArea(Rectangle rectArea){
		setMouseHighlightArea(rectArea);
	}
	
	/**
	 * Set area on screen that the user has highlighted manually
	 * @param rectArea :: area of highlight on screen
	 */
	public void setMouseHighlightArea(Rectangle rectArea){
		//null flushes all
		if(rectArea==null){
			areas=null;
		}else{
			if(DynamicVectorRenderer.textBasedHighlight){
				//Check highlighting actually happens
				boolean nothingToHighlight = false;
					if(areas==null){
						//this is the first highlight make sure it actually highlights something
						nothingToHighlight = true;
						
						this.areas=new Rectangle[1];
						areas[0]=rectArea;
						
						areaDirection = new int[1];
						areaDirection[0] = 0;
					}

					//Get line and writing direction data
					Rectangle[] lines = PdfHighlights.getLineAreas();
					int[] writingMode = PdfHighlights.getLineWritingMode();
					
					//Find all lines that intersect with selection area
					if(lines!=null){
						Vector_Rectangle selected = new Vector_Rectangle(0);
						Vector_Int mode = new Vector_Int(0);
						
						//Ensure previous areas are added to selected area.
						//This ensure setFoundTextAreas works correctly
						for(int i=0; i!= areas.length; i++){
							for(int j=0; j!=selected.size(); j++)
								if(areas[i]!=null && selected.elementAt(j)!=null && !selected.elementAt(j).equals(areas[i])){
									selected.addElement(areas[i]);
									mode.addElement(areaDirection[i]);
								}
						}
						
						//Add new areas that intersect, if any
						for(int i=0; i!= lines.length; i++){
							if(lines[i]!=null && rectArea.intersects(lines[i])){
									selected.addElement(lines[i]);
									mode.addElement(writingMode[i]);
									//Highlights some text so continue
									nothingToHighlight = false;
							}
						}
						
						//Nothing has been highlighted reset to null and start again.
						if(nothingToHighlight){
							//Prevent text extraction on nothing
							areas=null;
							return;
						}

						//Remove all trailing nulls from vectors
						selected.trim(); 
						areas = selected.get();
						
						mode.trim();
						areaDirection = mode.get();

						//If more than one highlighted area on the page
						if(areas.length>1){	
							int top = 0;
							int bottom = 0;
							
							//Find page rotation as top and bottom are determined by
							//location on page
							int rot = displayRotation%360;
							
							//find the top and bottom lines
							switch(rot){
							case 0 : 
								for(int i=0; i!= areas.length; i++){
									if(areas[i].y>areas[top].y)
										top = i;
									if(areas[i].y<areas[bottom].y)
										bottom = i;
								}
								break;
							case 90 : 
								for(int i=0; i!= areas.length; i++){
									if(areas[i].x<areas[top].x)
										top = i;
									if(areas[i].x>areas[bottom].x)
										bottom = i;
								}
								break;
							case 180 : 
								for(int i=0; i!= areas.length; i++){
									if(areas[i].y<areas[top].y)
										top = i;
									if(areas[i].y>areas[bottom].y)
										bottom = i;
								}
								break;
							case 270 : 
								for(int i=0; i!= areas.length; i++){
									if(areas[i].x>areas[top].x)
										top = i;
									if(areas[i].x<areas[bottom].x)
										bottom = i;
								}
								break;
							}
							
							
							
							/**
							 * calculate the top partial line area
							 */
							switch(areaDirection[top]){
							case PdfData.HORIZONTAL_LEFT_TO_RIGHT:
								if(highlightY==rectArea.y){ //Selection from bottom to top
									if(areas[top].x < rectArea.x){
										if(highlightX>rectArea.x){//Left of selection point
											areas[top].width = areas[top].width - (rectArea.x - areas[top].x);
											areas[top].x = rectArea.x;
										}else{ //Right of selection point
											areas[top].width = areas[top].width - ((rectArea.x+rectArea.width) - areas[top].x);
											areas[top].x = (rectArea.x+rectArea.width);
										}
									}
								}else{ //Selection from top to bottom
									if(areas[top].x < highlightX){ 
										areas[top].width = areas[top].width - (highlightX - areas[top].x);
										areas[top].x = highlightX;
									}else{ //This if is used by searching only
										if(areas[top].x<rectArea.x)
											areas[top].x = rectArea.x;
									}
								}
								break;
							case PdfData.HORIZONTAL_RIGHT_TO_LEFT:
								break;
							case PdfData.VERTICAL_TOP_TO_BOTTOM:
								if(highlightX!=rectArea.x){ //Selection from Left to Right
									if(areas[top].y+areas[top].height > rectArea.y+rectArea.height){
										areas[top].height = (highlightY - areas[top].y);
									}
								}else{ //Selection from Right to Left
									if(areas[top].y+areas[top].height > rectArea.y+rectArea.height){
										if(highlightY>rectArea.y){//Bottom of selection point
											areas[top].height = (rectArea.y - areas[top].y);
										}else //Top of selection point
											areas[top].height = (rectArea.y + rectArea.height)-areas[top].y;
									}
								}break;
							case PdfData.VERTICAL_BOTTOM_TO_TOP:
								if(highlightX!=rectArea.x){ //Selection from Left to Right
									if(areas[top].y < rectArea.y){
										if(highlightY>rectArea.y){//Bottom of selection point
											areas[top].height = areas[top].height - (rectArea.y - areas[top].y);
											areas[top].y = rectArea.y;
										}else{ //Top of selection point
											areas[top].height = areas[top].height - ((rectArea.y+rectArea.height) - areas[top].y);
											areas[top].y = (rectArea.y+rectArea.height);
										}
									}
								}else{ //Selection from Right to Left
									if(areas[top].y < highlightY){ 
										areas[top].height = areas[top].height - (highlightY - areas[top].y);
										areas[top].y = highlightY;
									}else{
										if(areas[top].y<rectArea.y)
											areas[top].y = rectArea.y;
									}
								}break;
							}
							
							
							
							
							/**
							 * calculate the bottom partial line area
							 */
							switch(areaDirection[bottom]){
							case PdfData.HORIZONTAL_LEFT_TO_RIGHT:
								if(highlightY==rectArea.y){ //Selection from bottom to top
									if(areas[bottom].x+areas[bottom].width > rectArea.x+rectArea.width){
										areas[bottom].width = (highlightX - areas[bottom].x);
									}
								}else{ //Selection from top to bottom
									if(areas[bottom].x+areas[bottom].width > rectArea.x+rectArea.width){
										if(highlightX>rectArea.x){//Left of selection point
											areas[bottom].width = (rectArea.x - areas[bottom].x);
										}else //Right of selection point
											areas[bottom].width = (rectArea.x + rectArea.width)-areas[bottom].x;
									}
								}
								break;
							case PdfData.HORIZONTAL_RIGHT_TO_LEFT:
								break;
							case PdfData.VERTICAL_TOP_TO_BOTTOM:
								if(highlightX!=rectArea.x){ //Selection from Left to Right
									if(areas[bottom].y < rectArea.y){
										if(highlightY>rectArea.y){//Bottom of selection point
											areas[bottom].height = areas[bottom].height - (rectArea.y - areas[bottom].y);
											areas[bottom].y = rectArea.y;
										}else{ //Top of selection point
											areas[bottom].height = areas[bottom].height - ((rectArea.y+rectArea.height) - areas[bottom].y);
											areas[bottom].y = (rectArea.y+rectArea.height);
										}
									}
								}else{ //Selection from Right to Left
									if(areas[bottom].y < highlightY){ 
										areas[bottom].height = areas[bottom].height - (highlightY - areas[bottom].y);
										areas[bottom].y = highlightY;
									}else{
										if(areas[bottom].y<rectArea.y)
											areas[bottom].y = rectArea.y;
									}
								}break;
							case PdfData.VERTICAL_BOTTOM_TO_TOP:
								if(highlightX!=rectArea.x){ //Selection from Left to Right
									if(areas[bottom].y+areas[bottom].height > rectArea.y+rectArea.height){
										areas[bottom].height = (highlightY - areas[bottom].y);
									}
								}else{ //Selection from Right to Left
									if(areas[bottom].y+areas[bottom].height > rectArea.y+rectArea.height){
										if(highlightY>rectArea.y){//Bottom of selection point
											areas[bottom].height = (rectArea.y - areas[bottom].y);
										}else //Top of selection point
											areas[bottom].height = (rectArea.y + rectArea.height)-areas[bottom].y;
									}
								}
								break;
							}
							
							//Ensure that no area has a height or width as a
							//negitive value as the extractor can't handle it
							selected = new Vector_Rectangle(1);
							mode = new Vector_Int(1);
							for(int i=0; i!= areas.length; i++){
								if(areas[i].height>=0 && areas[i].width>=0){
									selected.addElement(areas[i]);
									mode.addElement(areaDirection[i]);
								}
							}
							selected.trim();
							areas = selected.get();
							
							mode.trim();
							areaDirection = mode.get();
							
						}else{
							//If highlighting just one line or partial line
							//just adjust the on screen width
							if(areaDirection.length==1){
								switch(areaDirection[0]){
								case PdfData.HORIZONTAL_LEFT_TO_RIGHT:
									if((rectArea.x + rectArea.width)>(areas[0].x + areas[0].width)){
										areas[0].width = areas[0].width - (rectArea.x - areas[0].x);
									}else{
										if(rectArea.x<areas[0].x)
											areas[0].width = (rectArea.x + rectArea.width)-areas[0].x;
										else
											areas[0].width = rectArea.width;
									}
									
									if(areas[0].x<rectArea.x)
										areas[0].x = rectArea.x;
									
									break;
								case PdfData.HORIZONTAL_RIGHT_TO_LEFT:
									break;
								case PdfData.VERTICAL_TOP_TO_BOTTOM:
									if((rectArea.y + rectArea.height)>(areas[0].y + areas[0].height)){
										areas[0].height = areas[0].height - (rectArea.y - areas[0].y);
										areas[0].y = rectArea.y;
									}else{ 
										if(rectArea.y<areas[0].y){
											areas[0].height = (rectArea.y + rectArea.height)-areas[0].y;
										}else{
											areas[0].y = rectArea.y;
											areas[0].height = rectArea.height;
										}
									}
									
									break;
								case PdfData.VERTICAL_BOTTOM_TO_TOP:
									if((rectArea.y + rectArea.height)>(areas[0].y + areas[0].height)){
										areas[0].height = areas[0].height - (rectArea.y - areas[0].y);
										areas[0].y = rectArea.y;
									}else{ 
										if(rectArea.y<areas[0].y){
											areas[0].height = (rectArea.y + rectArea.height)-areas[0].y;
										}else{
											areas[0].y = rectArea.y;
											areas[0].height = rectArea.height;
										}
									}break;
								}

							}
						}
					}
			}else{


				//if inset add in difference transparently
				if(areas!=null){
					boolean matchFound=false;

					//see if already added
					int size=areas.length;
					for(int i=0;i<size;i++){
						if(areas[i]!=null && areas[i].x ==rectArea.x && areas[i].y ==rectArea.y && areas[i].width ==rectArea.width &&
								areas[i].height ==rectArea.height){
							matchFound=true;
							i=size;
						}
					}

					if(!matchFound){
						int newSize=areas.length+1;
						Rectangle[] newAreas=new Rectangle[newSize];
						for(int i=1;i<newSize;i++){
							if(areas[i-1]!=null)
								newAreas[i]= new Rectangle(areas[i-1].x, areas[i-1].y, areas[i-1].width,  areas[i-1].height);
						}
						this.areas=newAreas;

						areas[0]=new Rectangle(rectArea.x, rectArea.y, rectArea.width, rectArea.height);
					}
				}else{
					this.areas=new Rectangle[1];
					areas[0]=rectArea;
				}

			}

			pages.refreshDisplay();
		}
	}
	
	public void setHighlightStartPoint(int x, int y){
		highlightX = x;
		highlightY = y;
	}
	
    /**
	 * remove zone on page for text areas if present
	 */
	public void removeFoundTextArea(Rectangle rectArea){


		if(rectArea==null|| areas==null)
			return ;

		int size=areas.length;
		for(int i=0;i<size;i++){
			if(areas[i]!=null && (areas[i].contains(rectArea) || (areas[i].x ==rectArea.x && areas[i].y ==rectArea.y && areas[i].width ==rectArea.width &&
					areas[i].height ==rectArea.height))){
				areas[i]=null;
				i=size;
			}
		}
		//currentManager.addDirtyRegion(this,0,0,x_size,y_size);

        pages.refreshDisplay();

    }

    /**
	 * remove highlight zones on page for text areas on single pages
     * null value will totally reset
     */
	public void removeFoundTextAreas(Rectangle[] rectArea){

		if(rectArea==null){
			areas=null;
		}else{
			int count=rectArea.length;
			for(int ii=0;ii<count;ii++){
				removeFoundTextArea(rectArea[ii]);   
			}
			boolean allNull = true;
			for(int ii=0;ii<areas.length;ii++){
				if(areas[ii]!=null){
					allNull=false;
					ii=areas.length;
				}
			}
			if(allNull)
				areas = null;
		}

        pages.refreshDisplay();

    }

	/**
     * Deprecated, please use setMouseHighlightAreas or setSearchTextAreas if called from a search routine
     * @param newAreas :: areas specified for highlight by the user
     * @deprecated
     */
	public void setFoundTextAreas(Rectangle[] newAreas){
		setMouseHighlightAreas(newAreas);
	}
	
    /**
     * Highlight multiple areas on screen
     * @param newAreas :: areas specified for highlight by the user
     */
	public void setMouseHighlightAreas(Rectangle[] newAreas){

        if(this.displayView!=Display.SINGLE_PAGE){
			this.areas=null;
			return;
		}
        
        if(areas==null){
        	areas = new Rectangle[1];
        	areaDirection = new int[1];
        }
        
        Vector_Rectangle areasSelected = new Vector_Rectangle(0);
        Vector_Int areasDirection = new Vector_Int(0);
        if(newAreas!=null){ //add
                int count=newAreas.length;
                for(int ii=0;ii<count;ii++){
                	setMouseHighlightArea(newAreas[ii]);
                	
                	for(int i=0; i!=areas.length; i++){
                		areasSelected.addElement(areas[i]);
                	}

                	for(int i=0; i!=areaDirection.length; i++){
                		areasDirection.addElement(areaDirection[i]);
                	}
                	
                }
                
                areasSelected.trim();
                areas = areasSelected.get();
                
                areasDirection.trim();
                areaDirection = areasDirection.get();
                
        }else
            areas=null;

        pages.refreshDisplay();

	}

	//<start-adobe>
	/**
	 * handle context sensitive tooltips -
	 * replaces default java routine with our own which is context sensitive  -
	 *
	final public String getToolTipText(MouseEvent e) {
		String result = null;

		Point raw_p = e.getPoint();

		//convert to our co-ords
		current_p = new Point((int) ((raw_p.getX()-insetW)/scaling), (int) (((y_size+insetH)-raw_p.getY())/scaling));

//		if((displayHotspots!=null))
//			result=displayHotspots.getTooltip(current_p,userAnnotIcons,pageNumber);

		//return default or ours
		return result;
	}/**/

	/**
	 * update rectangle we draw to highlight an area -
	 * See SimpleViewer example for example code showing current usage.
	 */
	final public void updateCursorBoxOnScreen(
			Rectangle newOutlineRectangle,
			Color outlineColor) {

		if(this.displayView!=Display.SINGLE_PAGE)
			return;


		//area to reapint
		int x_size=this.x_size;
		int y_size=this.y_size;

		if(newOutlineRectangle!=null){

			int x=newOutlineRectangle.x;
			int y=newOutlineRectangle.y;
			int w=newOutlineRectangle.width;
			int h=newOutlineRectangle.height;

			int cropX=pageData.getCropBoxX(pageNumber);
			int cropY=pageData.getCropBoxY(pageNumber);
			int cropW=pageData.getCropBoxWidth(pageNumber);
			int cropH=pageData.getCropBoxHeight(pageNumber);

			if(x<cropX){
				int diff=cropX-x;
				w=w-diff;
				x=cropX;
			}

			if(y<cropY){
				int diff=cropY-y;
				h=h-diff;
				y=y+diff;
			}
			if((x+w)>(cropW+cropX+xOffset))
				w=cropX+xOffset+cropW-x;
			if((y+h)>(cropY+cropH))
				h=cropY+cropH-y;

			cursorBoxOnScreen = new Rectangle(x,y,w,h);

		}else
			cursorBoxOnScreen=null;

		this.outlineColor = outlineColor;

		int strip=30;

		/**allow offset from page being centered*/
		int dx=0;
		//center if required
		if(alignment==Display.DISPLAY_CENTERED){
			int width=this.getBounds().width;
			int pdfWidth=this.getPDFWidth();

			if(displayView!=Display.SINGLE_PAGE)
				pdfWidth=(int)pages.getPageSize(displayView).getWidth();

			dx=((width-pdfWidth)/2);
		}

		if(lastCursorBoxOnScreen!=null){
			if(displayRotation==0 || displayRotation==180)
				currentManager.addDirtyRegion(this,insetW+dx,insetH,x_size+5+xOffset,y_size);
			else
				currentManager.addDirtyRegion(this,insetH+dx,insetW,y_size+5+xOffset,x_size);

//			if((displayRotation==90)|(displayRotation==270))
//			currentManager.addDirtyRegion(this,0,0,y_size+max_y,x_size);
//			else if((displayRotation==180))
//			currentManager.addDirtyRegion(this,0,0,x_size+1000,y_size+max_y);
//			else
//			currentManager.addDirtyRegion(this,
//			(int)(lastCursorBoxOnScreen.x*scaling)-strip,
//			(int)(((max_y)-lastCursorBoxOnScreen.y-lastCursorBoxOnScreen.height)*scaling)-strip,
//			(int)(lastCursorBoxOnScreen.width*scaling)+strip+strip,
//			(int)(lastCursorBoxOnScreen.height*scaling)+strip+strip);

			lastCursorBoxOnScreen=null;
		}



		if(cursorBoxOnScreen!=null){
			currentManager.addDirtyRegion(this,(int)(cursorBoxOnScreen.x*scaling)-strip,
					(int)(((max_y)-cursorBoxOnScreen.y-cursorBoxOnScreen.height)*scaling)-strip,
					(int)(cursorBoxOnScreen.width*scaling)+strip+strip,
					(int)(cursorBoxOnScreen.height*scaling)+strip+strip);
		}

		if(this.viewScaling!=null)
			currentManager.markCompletelyDirty(this);
	}

	/**requests repaint of an area*/
	public void repaintArea(Rectangle screenBox,int maxY){

		int strip=10;

		if(strip<this.insetH)
			strip=insetH;

		if(strip<this.insetW)
			strip=insetW;

		currentManager.addDirtyRegion(this,(int)(screenBox.x*scaling)-strip,
				/**
                (int)(((pageData.getCropBoxHeight(pageNumber)*scaling)-screenBox.y-screenBox.height)*scaling)-strip,
                /*/
				(int)((maxY-screenBox.y-screenBox.height)*scaling)-strip,
				//*/
				(int)((screenBox.x+screenBox.width)*scaling)+strip+strip,
				(int)((screenBox.y+screenBox.height)*scaling)+strip+strip);

	}

	/**
	 * update rectangle we draw to show area of object -
	 * See org.jpedal.examples.contentextractor.contentExtractor
	 */
	final public void removeHiglightedObject(){

		if(lastHighlight!=null){
			currentManager.addDirtyRegion(this,lastHighlight.x-strip,lastHighlight.y-strip,lastHighlight.width+strip+strip,lastHighlight.height+strip+strip);

			currentHighlightedObject=null;

		}

	}
	
	/**
	 * update rectangle we draw to show area of object for Storypad -
	 * (NOT PART OF API and subject to change)
	 */
	final public void addHiglightedObject(
			Rectangle currentShape,
			Color outlineHighlightColor) {

		//over-ride in multipage mode
		if(this.displayView!=Display.SINGLE_PAGE)
			return;

		this.currentHighlightedObject = currentShape;
		this.outlineHighlightColor = outlineHighlightColor;

		if((currentHighlightedObject!=null)&&(!currentHighlightedObject.equals(lastHighlight))){

			currentManager.addDirtyRegion(this,currentHighlightedObject.x-strip,currentHighlightedObject.y-strip,currentHighlightedObject.width+strip+strip,currentHighlightedObject.height+strip+strip);

			lastHighlight=currentHighlightedObject;
		}
	}

	//<end-adobe>


	public void paint(Graphics g){

		try{

			super.paint(g);

			if(!isDecoding){


				/**add any highlighted rectangle on screen*/
				if (cursorBoxOnScreen != null){
					Graphics2D g2=(Graphics2D) g;
					AffineTransform defaultAf=g2.getTransform();
					/**
                if(displayRotation==0 || displayRotation==180)
                    g2.setClip(insetW,insetH,(int)(pageData.getCropBoxWidth(pageNumber)*scaling),(int)(pageData.getCropBoxHeight(pageNumber)*scaling));
                else
                    g2.setClip(insetH,insetW,(int)(pageData.getCropBoxHeight(pageNumber)*scaling),(int)(pageData.getCropBoxWidth(pageNumber)*scaling));
					 */
					if(cursorAf!=null){
						g2.setTransform(cursorAf);

						Shape clip=g2.getClip();

						//remove clip for drawing outline
						if((alignment==Display.DISPLAY_CENTERED)&&(clip!=null))
							g2.setClip(null);

						//<start-adobe>
						paintRectangle(g2);
						//<end-adobe>

						g2.setClip(clip);

						g2.setTransform(defaultAf);

					}
				}
			}

		}catch(Exception e){

			pages.flushPageCaches();

		}catch(Error err){  //for tight memory

			pages.flushPageCaches();
			pages.stopGeneratingPage();


			paint(g);

		}


	}

	/**standard method to draw page and any highlights onto JPanel*/
	public void paintComponent(Graphics g) {

		//if(this.backgroundIsTransparent)
		//currentDisplay.setBackgroundColor(null);
		if(Display.debugLayout)
			System.out.println("PaintComponent called ");

		if (SwingUtilities.isEventDispatchThread())
			threadSafePaint(g);
		else {
			final Graphics g2 = g;
			final Runnable doPaintComponent = new Runnable() {
				public void run() {
					threadSafePaint(g2);
				}
			};
			SwingUtilities.invokeLater(doPaintComponent);
		}

	}

	/**
	 * update display
	 */
	private synchronized void threadSafePaint(Graphics g) {

        //System.out.println("threadSafePaint1-------------------------------------------------------- "+displayRotation);

		super.paintComponent(g);
		
		if(displayScaling==null)
			return;

		Graphics2D g2 = (Graphics2D) g;

		//remember so we can put it back
		final AffineTransform rawAf=g2.getTransform();

		if(Display.debugLayout)
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>START PAINT");

		/**
		 * lazy initialisation to add forms to screen
		 */

		if (renderPage && displayForms ) {

			//track all changes
			int start=pageNumber;
			int end=pageNumber;

			if(displayView!=Display.SINGLE_PAGE){
				start=pages.getStartPage();
				end=pages.getEndPage();
				if(start==0 || end==0 || lastEnd!=end || lastStart!=start)
					lastFormPage=-1;

				lastEnd=end;
				lastStart=start;

			}

			if(lastFormPage!=pageNumber && !isDecoding){

				if (formRenderer != null){
					formRenderer.displayComponentsOnscreen(start,end);
					
					//switch off if forms for this page found
					if(formRenderer.getCompData().getStartComponentCountForPage(pageNumber)!=-1)
					lastFormPage=pageNumber; //ensure not called too early
				}
			}
		}

		if(DynamicVectorRenderer.debugPaint)
			System.err.println("threadsafePaint called "+this.displayView);
        
        pages.init(scaling,pageCount,displayRotation,pageNumber,currentDisplay,false, pageData,insetW,insetH);
		int indent=0;

		//center if required
		if(alignment==Display.DISPLAY_CENTERED){
			int width=this.getBounds().width;

            if(displayView==Display.SIDE_SCROLL)
            width=getVisibleRect().width;

			int pdfWidth=this.getPDFWidth();

			if(displayView!=Display.SINGLE_PAGE)
				pdfWidth=(int)pages.getPageSize(displayView).getWidth();

			if(width>pdfWidth || displayView==Display.SIDE_SCROLL)  {

                if(displayView==Display.SIDE_SCROLL){

                    //center in middle
                    int widthOfPage=pageData.getScaledCropBoxWidth(pageNumber);

                    //System.out.println(widthOfPage+" "+pageData.getCropBoxWidth(pageNumber)+
                      //      " "+pageData.getScaledCropBoxWidth(pageNumber)+" scaling="+scaling);
                    
                    indent= (int) (0.5f+((width-(widthOfPage))/2)-pages.getXCordForPage(pageNumber));
                    //indent=-pages.getXCordForPage(pageNumber);
                    //System.out.println("indent="+indent);
                    
                }else
                    indent=((width-pdfWidth)/2);

				if(displayView==Display.SINGLE_PAGE)
					lastIndent=indent;
				else if((displayView==Display.CONTINUOUS  || displayView==Display.SIDE_SCROLL) && lastIndent!=-1){
					indent=lastIndent;
					lastIndent = -1;
				}else
					lastIndent=-1;

				g2.translate(indent,0);
			}

			if(formRenderer!=null)
					formRenderer.getCompData().resetScaledLocation(scaling,displayRotation,indent);

		}

        /**we need to store the Affine and put it back at
		 * the end otherwise we f**k up all the form components
		 * on certain pages
		 */
		Rectangle dirtyRegion=null;

		pages.initRenderer(areas,g2,myBorder,indent);

        if(!isDecoding||drawInteractively){

            //@itunes - draw page
            //if(this.displayView==Display.SIDE_SCROLL)
            //System.out.println(">>>>>>"+this.getVisibleRect()+" "+this.getAlignmentX()+" "+viewScaling+" "+displayScaling);

			actualBox =pages.drawPage(viewScaling,displayScaling,pageUsedForTransform,this);
		}else //just fill the background
            currentDisplay.paintBackground(g2, dirtyRegion);

		//disabnled if not in Single PAGE
		if(displayView==Display.SINGLE_PAGE){

			/**
			 * draw highlighted text boxes
			 */

			//add highlights
//			if(rectArea !=null){
//
//				AffineTransform defaultScaling=null;
//				if(viewScaling!=null){
//					defaultScaling=g2.getTransform();
//					g2.transform(viewScaling);
//				}
//
//				Composite opacity=g2.getComposite();
//				g2.setColor(highlightColor);
//				g2.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 0.7f ) );
//
//				if(rectArea !=null)
//					g2.fillRect(rectArea.x, rectArea.y, rectArea.width, rectArea.height);
//
//				g2.setComposite(opacity);
//
//				if(viewScaling!=null)
//					g2.setTransform(defaultScaling);
//			}

//			if(rectAreas !=null){
//
//				AffineTransform defaultScaling=null;
//				if(viewScaling!=null){
//					defaultScaling=g2.getTransform();
//					g2.transform(viewScaling);
//				}
//
//				Composite opacity=g2.getComposite();
//				g2.setColor(highlightColor);
//				g2.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 0.7f ) );
//
//				for(int jj=0;jj<rectAreas.length;jj++){
//
//					Rectangle rectArea=rectAreas[jj];
//
//					if(rectArea !=null)
//						g2.fillRect(rectArea.x, rectArea.y, rectArea.width, rectArea.height);
//
//				}
//
//				g2.setComposite(opacity);
//
//				if(viewScaling!=null)
//					g2.setTransform(defaultScaling);
//			}


			//reset after first clear
			if ((currentHighlightedObject==null)&&(lastHighlight!=null))
				lastHighlight=null;

			/**
			 * add any viewport
			 */
			if(viewScaling!=null)
				g2.transform(viewScaling);

			//<start-adobe>

			/**set any highlighted zones*/
			if (highlightedZonesSelected != null)
				paintHighlights(g2);

			/**draw any annotations*/
//			if(displayHotspots!=null)
//				displayHotspots.addHotspotsToDisplay(g2,userAnnotIcons,pageNumber);

			/**shows merging for debugging*/
			if((merge_level!=null)&&(showMerging))
				paintMergingInfo(g2);

			//<end-adobe>

			/**add any highlighted rectangle on screen*/
			if (cursorBoxOnScreen != null)
				this.cursorAf=g2.getTransform();
			else
				this.actualBox=null;

			pages.resetToDefaultClip();

			//draw highlight underneath
			if (currentHighlightedObject != null) {
				g2.setColor(outlineHighlightColor);
				g2.draw(currentHighlightedObject);
			}


			if (showCrop) {
				g2.setColor(Color.orange);
				pages.completeForm(g2);

			}

		}
		

		if(highlightedImage!=null){
			//All image highlight coords scaled here to allow for any scaling value
			
			//Varibles added to make the code more readable
			int x= (int)(highlightedImage[0]*scaling);
			int y= (int)(highlightedImage[1]*scaling);
			int w= (int)(highlightedImage[2]*scaling);
			int h= (int)(highlightedImage[3]*scaling);

//			//Check for negative values
			if(w<0){
				w =-w;
				x =x-w;
			}
			if(h<0){
				h =-h;
				y =y-h;
			}
			
			//Final values to use
			int finalX= x;
			int finalY= y;
			int finalW= w;
			int finalH= h;
			
			//Handle Any Rotation
			if(displayRotation==90){
				
				finalH= w;
				finalW= h;

				finalX=insetW+y;
				finalY= insetH +x;
				
			}else
			if(displayRotation==180){
				
				finalX= (int)((max_x*scaling)-(x)-w)+insetW;
				finalY= (insetH +y);
				
			}else
			if(displayRotation==270){
				
				finalH= w;
				finalW= h;
				
				finalY= (int)((max_x*scaling)-(x)-w)+insetW;
				finalX= (int)((max_y*scaling)-(y)-h)+insetH;
				
			}else{
				
				finalX= insetW +x;
				finalY= (int)((max_y*scaling)-(y)-h)+insetH;
				
			}
			
			Color oldColor = g2.getColor();
			Composite oldComposite = g2.getComposite();
			Stroke oldStroke = g2.getStroke();
			
			g2.setStroke(new BasicStroke(2));
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, PdfDecoder.highlightComposite));
			
			//draw border
			if(currentDisplay.invertHighlight){
				g2.setColor(Color.WHITE);
				g2.setXORMode(Color.BLACK);
			}else{
				g2.setColor(PdfDecoder.highlightColor);
				g2.drawRect(finalX,finalY,finalW,finalH);
			}
			
			//fill border
			
			g2.fillRect(finalX,finalY,finalW,finalH);
			
			//set back to original setup
			g2.setColor(oldColor);
			g2.setComposite(oldComposite);
			g2.setStroke(oldStroke);
		}
		
		
		
		/**
		 * draw other pages if not in SINGLE mode
		 **/
		pages.drawBorder();

		g2.setTransform(rawAf);

		if(Display.debugLayout)
			System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<END PAINT ");

	}

	/**
	 * Method to scroll the PDF to a highlighted area.  This would typically be used after some text has been highlighted - perhaps
	 * after searching - and you wish to scroll the document to make the highlight visible. <br><br>
	 * 
	 * Example usage: <br><br>
	 * 
	 * try {<br>
	 *		int page = 4;<br>
	 * 	<br>
	 *		PdfDecoder pdfDecoder = new PdfDecoder();<br>
	 *		<br>
	 *		pdfDecoder.setExtractionMode(PdfDecoder.TEXT);<br>
	 *		pdfDecoder.init(true);<br>
	 *		pdfDecoder.openPdfFile("C:/file.pdf");<br>
	 *		pdfDecoder.setPageParameters(1, page);<br>
	 *		pdfDecoder.decodePage(page); <br>
	 *<br>
	 *		PdfGroupingAlgorithms currentGrouping = pdfDecoder.getGroupingObject();<br>
	 *		PdfPageData pageSize = pdfDecoder.getPdfPageData();<br>
	 *		int x1 = pageSize.getMediaBoxX(page);<br>
	 *		int x2 = pageSize.getMediaBoxWidth(page);<br>
	 *		int y1 = pageSize.getMediaBoxY(page);<br>
	 *		int y2 = pageSize.getMediaBoxHeight(page);<br>
	 *<br>
	 *		int rotation = pdfDecoder.getPdfPageData().getRotation(page);<br>
	 *<br>
	 *		String terms[] = {"term1"};<br>
	 *		List highlights = currentGrouping.findMultipleTermsInRectangle(x1, y1, x2, y2, rotation,<br> 
	 *				page, terms, true, true, SearchType.DEFAULT, new DefaultSearchListener());<br>
	 *<br>
	 *		// highlight all found terms<br> 
	 *		for (Iterator it = highlights.iterator(); it.hasNext();) {<br>
	 *			Rectangle highlight = (Rectangle) it.next();<br>
	 *			pdfDecoder.setFoundTextArea(highlight);<br>
	 *		}<br>
	 *<br>
	 *		// scroll to first highlight<br>
	 *		pdfDecoder.scrollRectToHighlight((Rectangle) highlights.get(0));<br>
	 *<br>
	 *		pdfDecoder.invalidate();<br>
	 *		pdfDecoder.repaint();<br>
	 *	} catch (Exception e1) {<br>
	 *		e1.printStackTrace();<br>
	 *	}<br>
	 *
	 * @param highlight
	 */
	public void scrollRectToHighlight(Rectangle highlight) {
		int x = 0, y = 0, w = 0, h = 0;
		
		int cropW = pageData.getCropBoxWidth(pageNumber);
		int cropH = pageData.getCropBoxHeight(pageNumber);
		int cropX = pageData.getCropBoxX(pageNumber);
		int cropY = pageData.getCropBoxY(pageNumber);
		
		switch (displayRotation) {
		case 0:
		    x = (int) ((highlight.x - cropX) * scaling) + insetW;
			y = (int) ((cropH - (highlight.y - cropY)) * scaling) + insetH;
			w = (int) (highlight.width * scaling);
			h = (int) (highlight.height * scaling);
			
			break;
		case 90:
			x = (int) ((highlight.y - cropY) * scaling) + insetH;
			y = (int) ((highlight.x - cropX) * scaling) + insetW;
			w = (int) (highlight.height * scaling);
			h = (int) (highlight.width * scaling);
			
			break;
		case 180:
			x = (int) ((cropW - (highlight.x - cropX)) * scaling) + insetW;
			y = (int) ((highlight.y - cropY) * scaling) + insetH;
			w = (int) (highlight.width * scaling);
			h = (int) (highlight.height * scaling);
			
			break;
		case 270:
			x = (int) ((cropH - (highlight.y - cropY)) * scaling) + insetH;
			y = (int) ((cropW - (highlight.x - cropX)) * scaling) + insetW;
			w = (int) (highlight.height * scaling);
			h = (int) (highlight.width * scaling);
			
			break;
		}
		
		Rectangle scrollto = new Rectangle(x - scrollInterval, y - scrollInterval, w + scrollInterval * 2, h + scrollInterval * 2);
		
		super.scrollRectToVisible(scrollto);
	}
	

	//<start-adobe>


	/**
	 * not part of API - used by Storypad
	 */
	private void paintMergingInfo(Graphics2D g2) {
		int merge_count = merge_outline.size() - 1;
		for( int i = 0;i < merge_count;i++ ){
			Shape s = merge_outline.elementAt( i );
			int level = merge_level.elementAt( i );

			if( ( showDebugLevel[level]) & ( s != null ) ){
				g2.setColor( debugColors[level]);

				g2.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 0.1f ) );
				g2.draw( s.getBounds() );
				g2.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 0.3f ) );
				g2.fill( s );
			}
		}
	}

	/**
	 * turn crossHairs on or off -
	 * highlight <b>newBoxContained</b> handle with specified Color,<br>
	 * if <b>newBoxContained</b> is -1 no handles are highlighted -
	 * See org.jpedal.examples.contentextractor.ContentExtractor
	 * @param newBoxContained
	 */
	public void setDrawCrossHairs(boolean newDrawCrossHairs,int newBoxContained,Color newColor){
		drawCrossHairs = newDrawCrossHairs;
		boxContained = newBoxContained;
		selectedHandleColor = newColor;
	}

	/**
	 * draw cursorBox on screen with specified color,
	 */
	private void paintRectangle(Graphics2D g2){

		Stroke oldStroke = g2.getStroke();//copy before to stop page border from being dotted        
		Stroke lineStroke;

		//allow for negative
		if(scaling<0)
			lineStroke = new BasicStroke(1/-scaling);
		else
			lineStroke = new BasicStroke(1/scaling);

		g2.setStroke(lineStroke);

		g2.setColor(outlineColor);
		
		//Draw opaqu square around highlight area
		if(!DynamicVectorRenderer.textBasedHighlight || extractingAsImage)
			g2.draw(cursorBoxOnScreen);

		if(drawCrossHairs){

			int x1 = cursorBoxOnScreen.x;
			int y1 = cursorBoxOnScreen.y;
			int x2 = x1+cursorBoxOnScreen.width;
			int y2 = y1+cursorBoxOnScreen.height;

			int mediaW = pageData.getMediaBoxWidth(pageNumber);
			int mediaH = pageData.getMediaBoxHeight(pageNumber);
			int mediaX = pageData.getMediaBoxX(pageNumber);
			int mediaY = pageData.getMediaBoxY(pageNumber);

			if(scaling>0)
				g2.setStroke(new BasicStroke(3/scaling, BasicStroke.CAP_ROUND,
						BasicStroke.JOIN_ROUND, 0, new float[]{0,6/scaling,0,6/scaling}, 0));
			else
				g2.setStroke(new BasicStroke(3/-scaling, BasicStroke.CAP_ROUND,
						BasicStroke.JOIN_ROUND, 0, new float[]{0,6/-scaling,0,6/-scaling}, 0));

			//draw dotted lines to edges
			g2.drawLine(x1,y1,mediaX,y1);
			g2.drawLine(x1,y1,x1,mediaY);
			g2.drawLine(x2,y1,mediaW,y1);
			g2.drawLine(x2,y1,x2,mediaY);

			g2.drawLine(x1,y2,mediaX,y2);
			g2.drawLine(x1,y2,x1,mediaH);
			g2.drawLine(x2,y2,mediaW,y2);
			g2.drawLine(x2,y2,x2,mediaH);


			Rectangle[] cursorBoxHandles = new Rectangle[8];
			//*centre of line handles
			//left
			cursorBoxHandles[0] = new Rectangle(x1-handlesGap,(y1+(Math.abs(y2-y1))/2)-handlesGap,handlesGap*2,handlesGap*2);//0
			//bottom
			cursorBoxHandles[1] = new Rectangle((x1+(Math.abs(x2-x1))/2)-handlesGap,y1-handlesGap,handlesGap*2,handlesGap*2);//1
			//top
			cursorBoxHandles[2] = new Rectangle((x1+(Math.abs(x2-x1))/2)-handlesGap,y2-handlesGap,handlesGap*2,handlesGap*2);//2
			//right
			cursorBoxHandles[3] = new Rectangle(x2-handlesGap,(y1+(Math.abs(y2-y1))/2)-handlesGap,handlesGap*2,handlesGap*2);//3
			/**/

			//*corner handles
			//bottom left
			cursorBoxHandles[4] = new Rectangle(x1-handlesGap,y1-handlesGap,handlesGap*2,handlesGap*2);//4
			//top left
			cursorBoxHandles[5] = new Rectangle(x1-handlesGap,y2-handlesGap,handlesGap*2,handlesGap*2);//5
			//bottom right
			cursorBoxHandles[6] = new Rectangle(x2-handlesGap,y1-handlesGap,handlesGap*2,handlesGap*2);//6
			//top right
			cursorBoxHandles[7] = new Rectangle(x2-handlesGap,y2-handlesGap,handlesGap*2,handlesGap*2);//7

			/**/

			g2.setStroke(lineStroke);
			//draw handle box containing cursor
			if(boxContained!=-1 && boxContained<cursorBoxHandles.length){
				if(selectedHandleColor!=null){
					Color old = g2.getColor();
					g2.setColor(selectedHandleColor);
					g2.fill(cursorBoxHandles[boxContained]);
					g2.setColor(old);
				}else
					g2.fill(cursorBoxHandles[boxContained]);
			}

			//draw other handles
			for(int i=0;i<cursorBoxHandles.length;i++){
				if(i!=boxContained)
					g2.draw(cursorBoxHandles[i]);
			}
		}
		g2.setStroke(oldStroke);

		if(actualBox==null){
			lastCursorBoxOnScreen=cursorBoxOnScreen;

		}else{

			Rectangle b1=cursorBoxOnScreen.getBounds();
			int minX=(int)b1.getMinX();
			int minY=(int)b1.getMinY();
			int maxX=(int)b1.getMaxX();
			int maxY=(int)b1.getMaxY();

			Rectangle bounds=actualBox.getBounds();
			int tmp=(int)bounds.getMinX();
			if(tmp<minX)
				minX=tmp;
			tmp=(int) bounds.getMinY();
			if(tmp<minY)
				minY=tmp;
			tmp=(int) bounds.getMaxX();
			if(tmp>maxX)
				maxX=tmp;
			tmp=(int)bounds.getMaxY();
			if(tmp>maxY)
				maxY=tmp;

			lastCursorBoxOnScreen=new Rectangle(minX-5,minY-5,10+maxX-minX,10+(maxY-minY));
		}
	}

	/**
	 * put selected areas onto screen display as highlights
	 */
	private void paintHighlights(Graphics2D g2) {
		if(alternateOutlines!=null){
			int items=alternateOutlines.length;

			for (int i = 0; i < items; i++) {


				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float) 0.1));
				g2.setColor(Color.darkGray);

				g2.fill(alternateOutlines[i]);
				//g2.draw(alternateOutlines[i]);

				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float) 0.9));
				g2.draw(alternateOutlines[i]);

			}

			if(merge_outline!=null && (!altName.equals("Nothing") && !altName.equals("Lines"))){

				int merge_count = merge_outline.size() - 1;

				for( int i = 0;i < merge_count;i++ ){
					Shape s = merge_outline.elementAt( i );
					int level = merge_level.elementAt( i );

					if( level==2 & ( s != null ) ){
						g2.setColor( debugColors[level]);

						g2.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 0.1f ) );
						g2.draw( s.getBounds() );
						g2.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 0.3f ) );
						g2.fill( s );
					}
				}
			}

		}else{			
			int items=highlightedZonesSelected.length;

			for (int i = 0; i < items; i++) {
				
				if (highlightedZonesSelected[i]){
					
					// added check for null due to issues with contentExtractor throwing NullPointerEception 
					if(hasDrownedObjects == null){
						highlightStoryOnscreen(g2, i, false);
					} else {
						highlightStoryOnscreen(g2, i, hasDrownedObjects[i]);
					}
					
				}else if (((highlightMode & SHOW_OBJECTS) == SHOW_OBJECTS)&&(fragmentShapes[i]!=null)) {
					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float) 0.1));
					if(highlightColors[i]==null)
						g2.setColor(Color.darkGray);
					else
						g2.setColor(highlightColors[i]);

					g2.fill(fragmentShapes[i]);
					g2.draw(outlineZone[i]);

					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float) 0.9));
					g2.draw(outlineZone[i]);

					if(children!=null){
						int[] ii=(int[]) children[i];
						if(ii!=null)
							drawRelationships(false,g2, i+"-",ii);
					}

					if(linkedItems!=null){
						int[] ii=(int[]) linkedItems[i];
						if(ii!=null)
							numberItems(false,g2, i+"-",ii);
					}
				}
			}


			//show page furniture
			if((pageLines!=null)&&((highlightMode & SHOW_LINES) == SHOW_LINES))
				pageLines.drawLines(g2);

			//show page furniture
			if((pageLines!=null)&&((highlightMode & SHOW_BOXES) == SHOW_BOXES))
				pageLines.drawBoxes(g2);    		

			/**add selection order for highlights*/
			if(selectionOrder!=null){
				drawRelationships(false,g2,"",selectionOrder);
//				numberItems(false,g2, "",selectionOrder);
			}
		}
	}

	/**
	 * draw any outline around story and any linked items
	 */
	private void highlightStoryOnscreen(Graphics2D g2, int i, boolean containsDownedObjects) {

		Stroke line = g2.getStroke();

		if(containsDownedObjects){
			Stroke dashed = new BasicStroke(8, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 12, 12 }, 0);
			g2.setStroke(dashed);
		}

		if(fragmentShapes[i]!=null){
			if(highlightColors[i]==null)
				g2.setColor(highlightColor);
			else
				g2.setColor(highlightColors[i]);

			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float) 0.1));
			g2.fill(fragmentShapes[i]);
			g2.draw(outlineZone[i]);

			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float) 0.9));
			g2.draw(outlineZone[i]);

			int xs=outlineZone[i].getBounds().x;
			int ys=outlineZone[i].getBounds().y;
			g2.drawLine(xs,ys+(int)outlineZone[i].getBounds().getHeight(),
					xs+(int)outlineZone[i].getBounds().getWidth(),ys);

			if(processedByRegularExpression[i]>0)
				g2.drawLine(xs+(int)outlineZone[i].getBounds().getWidth(),ys+(int)outlineZone[i].getBounds().getHeight(),
						xs,ys);
		}

		if(containsDownedObjects){
			int xoffset = outlineZone[i].getBounds().x;
			int yoffset = outlineZone[i].getBounds().y;
			
			xoffset = xoffset+((int)outlineZone[i].getBounds().getWidth()/2)-12;
			yoffset = yoffset+((int)outlineZone[i].getBounds().getHeight()/2);
			
			//Opps unhappy face
			g2.setColor(Color.yellow);
			g2.fillOval(xoffset, yoffset, 50, 50);
			//eyes
			g2.setColor(Color.black);
			g2.fillOval(xoffset+27, yoffset+30, 8, 8);
			g2.fillOval(xoffset+15, yoffset+30, 8, 8);
			//frown
			g2.setStroke(new BasicStroke(3.0f));
			g2.drawOval(xoffset, yoffset, 50, 50);
			g2.drawArc(xoffset+12, yoffset+5, 26, 15, 170, 200);
		}

		g2.setStroke(line);
	}
	/**
	 * add numbers to selected items
	 */
	private void numberItems(boolean isSublist,Graphics2D g2, String prefix,int[] selectionOrder) {

		int itemCount=selectionOrder.length;

		if(itemCount==0)
			return;

		int order=1;

		for(int ii=0;ii<itemCount;ii++){
			int i=selectionOrder[ii];

			if(i==-1){
				ii=itemCount;
			}else{

				String value= prefix +(order);

				//see if linked items and highlight
				if(linkedItems==null){
					if(fragmentShapes[i]!=null)
						numberItem(g2, i, value);

					order++;
				}else{
					int[] currentLinks=(int[])linkedItems[i];
					if(currentLinks!=null){
						order++;
						int childCount=currentLinks.length;
						int item=0;
						for(int j=0;j<childCount;j++){
							int childID=currentLinks[j];

							item++;

							if(childID==-1)
								j=childCount;
							else if(fragmentShapes[i]!=null){
								if(item!=1 && extraButton[childID]!=null)
									extraButton[childID].setVisible(false);
								numberItem(g2, childID, value+ '.' +(item));
							}
						}
						//System.out.println("---recurse end");
					}else if(this.parents[i]==-1){
						if(fragmentShapes[i]!=null)
							numberItem(g2, i, value);

						order++;
					}
				}
			}
		}
	}
	public  JPanel getParentChildButton(int buttonNumber){
		return extraButton[buttonNumber];
	}

	public  void setParentChildButton(int buttonNumber, JPanel j){
		extraButton[buttonNumber] = j;
	}

	public  JPanel[] getParentChildButtonArray(){
		return extraButton;
	}

	public  void setParentChildButtonArray(JPanel[] newButtonSet){
		extraButton = newButtonSet;
	}

	/**
	 * Add lines between parent and children
	 */
	private void drawRelationships(boolean isSublist,Graphics2D g2, String prefix,int[] selectionOrder) {
		int itemCount=selectionOrder.length;
		if(itemCount==0)
			return;

		for(int ii=0;ii<itemCount;ii++){
			int i=selectionOrder[ii];
			if(i==-1){
				ii=itemCount;
			}else{
				//see if linked items and highlight
				if(children!=null){
					int[] currentLinks=(int[])children[i];

					if(currentLinks!=null){
						int childCount=currentLinks.length;
						int item=0;
						for(int j=0;j<childCount;j++){
							int childID=currentLinks[j];
							item++;

							if(childID==-1)
								j=childCount;
							else if(fragmentShapes[childID]!=null){
								Stroke oldStroke = g2.getStroke();
								Stroke newStroke = new BasicStroke(5);
								g2.setStroke(newStroke);
								g2.setColor(highlightColor);
								//if not the parent object, add nex child link
								if(i!=childID){
									g2.drawLine(cx[i],cy[i],cx[childID],cy[childID]);
									g2.fillOval(cx[childID]-15,cy[childID]-15, 30, 30);
									g2.setColor(Color.red);
									g2.fillOval(cx[i]-20,cy[i]-20, 40, 40);
								}
								g2.setStroke(oldStroke);
								highlightedZonesSelected[childID]=true;

								//Highlight all linked items and add numbers
								if(linkedItems[childID]!=null){
									int y = 0;
									int[] links = (int[])linkedItems[childID];
									while(y!=links.length){
										if(links[y]!=-1){
											highlightStoryOnscreen(g2, links[y],hasDrownedObjects[y]);
											highlightedZonesSelected[links[y]]=true;
											numberItems(false,g2, "",currentLinks);
											if(useParentButtons && extraButton[links[y]]!=null)
												extraButton[links[y]].setVisible(false);
										}
										y++;
									}
								}else{
									numberItems(false,g2, "",currentLinks);
									highlightStoryOnscreen(g2, childID, hasDrownedObjects[childID]);
									if(useParentButtons && extraButton[childID]!=null)
										extraButton[childID].setVisible(false);
								}
							}
						}
					}else{
						numberItems(false,g2, "",selectionOrder);
					}
				}
			}
		}
	}

	/**
	 * @param g2
	 * @param i
	 * @param value
	 */
	private void numberItem(Graphics2D g2, int i, String value){

		AffineTransform af=new AffineTransform();
		GlyphVector gv;
		gv=highlightFont.createGlyphVector(g2.getFontRenderContext(),value);

		af.scale(1,-1);
		af.translate(cx[i],-cy[i]);

		Area a=new Area(gv.getOutline());
		a.transform(af);

		g2.setColor(Color.black);
		g2.fill(a.getBounds());

		g2.setColor(Color.white);
		g2.fill(a);
	}

	//<end-adobe>

	/**
	 * get sizes of panel <BR>
	 * This is the PDF pagesize (as set in the PDF from pagesize) -
	 * It now includes any scaling factor you have set
	 */
	final public int getPDFWidth() {
		if((displayRotation==90)|(displayRotation==270))
			return y_size+insetW+insetW;
		else
			return x_size+insetW+insetW;

	}

	/**
	 * get raw width for image
	 */
	final public int getRawPDFWidth() {
		if((displayRotation==90)| (displayRotation==270))
			return y_size;
		else
			return x_size;

	}


	//<start-adobe>

	/**
	 * allow user to set component for waring message in renderer to appear -
	 * if unset no message will appear
	 * @param frame
	 */
	public void setMessageFrame(Container frame){
		currentDisplay.setMessageFrame(frame);
	}

	//<end-adobe>


	/**
	 * get sizes of panel -
	 * This is the PDF pagesize
	 */
	final public int getPDFHeight() {
		if((displayRotation==90)|(displayRotation==270))
			return x_size+insetH+insetH;
		else
			return y_size+insetH+insetH;

	}

	/**
	 * get sizes of page excluding any insets
	 */
	final public int getRawPDFHeight() {
		if((displayRotation==90)|(displayRotation==270))
			return x_size;
		else
			return y_size;

	}

	//<start-adobe>

	/**return the hotspots areas for the page (ie regions of Annotations).
	 * See org.jpedal.examples.simpleviewer.SimpleViewer for sample code
	public Rectangle[] getPageHotspots(){

		if(displayHotspots!=null)
			return displayHotspots.getAnnotationhotSpots();
		else
			return null;
	}/**/
	//<end-adobe>


	/**do not display border when screen printed*/
	public void disableBorderForPrinting(){
		useBorder=false;
	}

	/**set border for screen and print which will be displayed<br>
	 * Setting a new value will enable screen and border painting - disable
	 * with disableBorderForPrinting() */
	final public void setPDFBorder(Border newBorder){
		this.myBorder=newBorder;

		//switch on as default
		useBorder=true;
	}

	/**
	 * workout Transformation to use on image
	 */
	protected final AffineTransform getScalingForImage(int pageNumber,int rotation,float scaling) {
		/**/
		//poss new code
		double mediaX = pageData.getMediaBoxX(pageNumber)*scaling;
		double mediaY = pageData.getMediaBoxY(pageNumber)*scaling;
		//double mediaW = pageData.getMediaBoxWidth(pageNumber)*scaling;
		double mediaH = pageData.getMediaBoxHeight(pageNumber)*scaling;

		double crw = pageData.getCropBoxWidth(pageNumber)*scaling;
		double crh = pageData.getCropBoxHeight(pageNumber)*scaling;
		double crx = pageData.getCropBoxX(pageNumber)*scaling;
		double cry = pageData.getCropBoxY(pageNumber)*scaling;

		//create scaling factor to use
		AffineTransform displayScaling = new AffineTransform();

		//** new x_size y_size declaration *
		int x_size=(int) (crw+(crx-mediaX));
		int y_size=(int) (crh+(cry-mediaY));

		if (rotation == 270) {

			displayScaling.rotate(-Math.PI / 2.0, x_size/ 2, y_size / 2);

			double x_change = (displayScaling.getTranslateX());
			double y_change = (displayScaling.getTranslateY());
			displayScaling.translate((y_size - y_change), -x_change);
			displayScaling.translate(0, y_size);
			displayScaling.scale(1, -1);
			displayScaling.translate(-(crx+mediaX), -(mediaH-crh-(cry-mediaY)));

		} else if (rotation == 180) {

			displayScaling.rotate(Math.PI, x_size / 2, y_size / 2);
			displayScaling.translate(-(crx+mediaX),y_size+(cry+mediaY)-(mediaH-crh-(cry-mediaY)));
			displayScaling.scale(1, -1);

		} else if (rotation == 90) {

			displayScaling.rotate(Math.PI / 2.0);
			displayScaling.translate(0,(cry+mediaY)-(mediaH-crh-(cry-mediaY)));
			displayScaling.scale(1, -1);

		}else{
			displayScaling.translate(0, y_size);
			displayScaling.scale(1, -1);
			displayScaling.translate(0, -(mediaH-crh-(cry-mediaY)));
		}

		displayScaling.scale(scaling,scaling);

		/*/
          //old code
          //create scaling factor to use
          AffineTransform displayScaling = new AffineTransform();

          if (raw_rotation == 270) {

              displayScaling.rotate(-Math.PI / 2.0, pageWidth/ 2, pageHeight / 2);

              double x_change = (displayScaling.getTranslateX());
              double y_change = (displayScaling.getTranslateY());
              displayScaling.translate((pageHeight - y_change), -x_change);

          } else if (raw_rotation == 180) {
              displayScaling.rotate(Math.PI, pageWidth / 2, pageHeight / 2);
          } else if (raw_rotation == 90) {

              displayScaling.rotate(Math.PI / 2.0,  pageWidth / 2,  pageHeight / 2);

              double x_change =(displayScaling.getTranslateX());
              double y_change = (displayScaling.getTranslateY());
              displayScaling.translate(-y_change, pageWidth - x_change);
          }

          displayScaling.translate(pageWidth, pageHeight);
          displayScaling.scale(1, -1);

          int mediaX = pageData.getMediaBoxX(pageNumber);
          int mediaY = pageData.getMediaBoxY(pageNumber);

          displayScaling.translate(-pageWidth-(mediaX*scaling), -(mediaY*scaling));
          displayScaling.scale(scaling,scaling);
          /**/
		/**
        if(raw_rotation == 90){
            displayScaling.translate(insetH/scaling,insetW/scaling);
        }else if(raw_rotation == 270){
            displayScaling.translate(-insetH/scaling,-insetW/scaling);
        }else if(raw_rotation == 180){
            displayScaling.translate(-insetW/scaling,insetH/(scaling));
        }else
            displayScaling.translate(insetW/scaling,-insetH/scaling);
		 */
		return displayScaling;
	}

	/**
	 * initialise panel and set size to display during updates
	 * and update the AffineTransform to new values<br>
	 * @param newRotation - sets display rotation to this value
	 */
	final public void setPageRotation(int newRotation) {

		//DO NOT DO THIS!!!
		//This code is also called to alter scaling
		//if(newRotation==oldRotation)
		//return;

		displayRotation = newRotation;

		//assume unrotated for multiple views and rotate on a page basis
		if(displayView!=Display.SINGLE_PAGE)
			newRotation=0;

		/**/
		pageUsedForTransform=pageNumber;
		displayScaling = getScalingForImage(pageNumber,newRotation,scaling);//(int)(pageData.getCropBoxWidth(pageNumber)*scaling),(int)(pageData.getCropBoxHeight(pageNumber)*scaling),

		if(newRotation == 90){
			displayScaling.translate(insetH/scaling,insetW/scaling);
		}else if(newRotation == 270){
			displayScaling.translate(-insetH/scaling,-insetW/scaling);
		}else if(newRotation == 180){
			displayScaling.translate(-insetW/scaling,insetH/(scaling));
		}else{
			displayScaling.translate(insetW/scaling,-insetH/scaling);
		}


		//force redraw if screen being cached
		pages.refreshDisplay();


		/**
		 * now apply any viewport scaling
		 */
		if(this.viewableArea!=null){

			viewScaling=new AffineTransform();

			/**workout scaling and choose larger*/
			double dx=(double)viewableArea.width/(double)pageData.getCropBoxWidth(pageNumber);
			double dy=(double)viewableArea.height/(double)pageData.getCropBoxHeight(pageNumber);
			double viewScale=dx;
			if(dy<dx)
				viewScale=dy;

			/**workout any translation*/
			double x=viewableArea.x;//left align
			double y=viewableArea.y+(viewableArea.height-(pageData.getCropBoxHeight(pageNumber)*viewScale));//top align
			//double x=viewableArea.x+(viewableArea.width-(pageData.getCropBoxWidth(pageNumber)*viewScale));//right align
			//double y=viewableArea.y;//bottom align

			//if(crx>0)
			//	x=(int) (x+crx);
			viewScaling.translate(x,y);
			viewScaling.scale(viewScale,viewScale);

		}else
			viewScaling=null;


	}

	

	/**
	 * Enables/Disables hardware acceleration of screen rendering in 1.4 (default is on)
	 */
	public void setHardwareAccelerationforScreen(boolean useAcceleration) {
		this.useAcceleration = useAcceleration;
	}

	//<start-adobe>

	/**return amount to scroll window by when scrolling (default is 10)*/
	public int getScrollInterval() {
		return scrollInterval;
	}

	/**set amount to scroll window by when scrolling*/
	public void setScrollInterval(int scrollInterval) {
		this.scrollInterval = scrollInterval;
	}

	/**
	 * JPedal will now draw the screen only when fully decoded rather than on any paint
	 * - to restore previous default behaviour (if required), call this
	 * routine with true
	 */
	public void setDrawInteractively(boolean drawInteractively) {
		this.drawInteractively = drawInteractively;
	}

	/**
	 * returns view mode used in panel -
	 * SINGLE_PAGE,CONTINUOUS,FACING,CONTINUOUS_FACING
	 * (has no effect in OS versions)
	 */
	public int getDisplayView() {
		return displayView;
	}

	//<end-adobe>


	/**read current Page scaling mode used for printing*/
	public int getPrintPageScalingMode() {
		return pageScalingMode;
	}

	/**
	 * set page scaling mode to use - default setting is
	 * PAGE_SCALING_REDUCE_TO_PRINTER_MARGINS
	 * All values start PAGE_SCALING
	 */
	public void setPrintPageScalingMode(int pageScalingMode) {
		this.pageScalingMode = pageScalingMode;
	}
	public void setUsePDFPaperSize(boolean usePDFPaperSize) {
		this.usePDFPaperSize = usePDFPaperSize;
	}


	//<start-adobe>

	//<end-adobe>

	public void setHighlightedImage(int[] highlightedImage) {
			this.highlightedImage = highlightedImage;
	}
	
	public int[] getHighlightImage(){
		return highlightedImage;
	}
	
	public boolean isImageExtractionAllowed(){
		return ImageExtractionAllowed;
	}
	
	public void setImageExtractionAllowed(boolean allow){
		ImageExtractionAllowed = allow;
	}

	public float getScaling() {
		return scaling;
	}

	public int getInsetH() {
		return insetH;
	}

	public int getInsetW() {
		return insetW;
	}
	
	protected void setDisplayForms(boolean displayForms) {
        this.displayForms = displayForms;
    }

	public Rectangle getCursorBoxOnScreen() {
		return cursorBoxOnScreen;
	}

	public boolean isExtractingAsImage() {
		return extractingAsImage;
	}

	public void setCurrent_p(Point current_p) {
		this.current_p = current_p;
	}
	
	public Rectangle getCurrentPageCoords(){
		if(pages!=null){
			return pages.getCurrentPageCoords();
		}else
			return null;
	}

}
