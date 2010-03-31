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
* SingleDisplay.java
* ---------------
*/
package org.jpedal;

//<start-adobe>
//<start-thin>
import org.jpedal.examples.simpleviewer.gui.generic.GUIThumbnailPanel;
//<end-thin>
//<end-adobe>

import org.jpedal.objects.PdfPageData;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;

import javax.swing.border.Border;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.HashMap;

import java.awt.image.VolatileImage;


import java.awt.*;
import java.awt.geom.AffineTransform;

import org.jpedal.parser.PdfStreamDecoder;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.repositories.Vector_Int;
import org.jpedal.external.Options;

public class SingleDisplay implements Display {

    protected int numberPagesOnSide=3;
    
	//used to flag we can decode multiple pages
	boolean isGeneratingOtherPages=false;

	Rectangle userAnnot= null;

	AffineTransform rawAf;
	Shape rawClip;

	DynamicVectorRenderer currentDisplay;

	// flag shows if running
	boolean running = false;

	protected PageOffsets currentOffset;

	/** used for creating the image size of volatileImage*/
	protected int volatileWidth, volatileHeight;

	int startViewPage=0;
	int endViewPage=0;

	Map pagesDrawn=new HashMap();

	Map cachedPageViews=new WeakHashMap();

	Map currentPageViews=new HashMap();

	boolean screenNeedsRedrawing;

	PdfDecoder pdf;

	protected Vector_Int offsets=new Vector_Int(3);
	protected Map newPages=new HashMap();
	protected int currentXOffset=0,additionalPageCount=0;


	/**any scaling factor being used to convert co-ords into correct values
	 * and to alter image size
	 */
	float oldScaling=-1,oldRotation=-1,oldVolatileWidth=-1,oldVolatileHeight=-1;

	/**centering on page*/
	int indent=0;

	/**used to draw pages offset if not in SINGLE_PAGE mode*/
	int[] xReached,yReached,pageW,pageH;

	boolean[] isRotated;

	/**optimise page redraws*/
	Map accleratedPagesAlreadyDrawn=new HashMap();

	/**flag to switch back to unaccelerate screen if no enough memory for scaling*/
	boolean overRideAcceleration=false;

	//to remind me to add feature back
	private boolean message=false;


	/**local copies*/
	int displayRotation,displayView=SINGLE_PAGE;

	int insetW,insetH;

	float scaling;

	int pageNumber;
	int pageCount=0;

	PdfPageData pageData=null;

	/**flag to optimse calculations*/
	private int lastPageChecked=-1,lastState=-1;

	Graphics2D g2;

	Rectangle[] areas;

	/**render screen using hardware acceleration*/
	boolean useAcceleration=true;

	boolean isInitialised;

	//rectangle onscreen
	int rx=0,ry=0,rw=0,rh=0;

	/**used to draw demo cross*/
	private int crx,cry,crw,crh;

	int CURRENT_BORDER_STYLE = 1;

	AffineTransform current2=null;
	Shape currentClip=null;

	protected Border myBorder;

	public SingleDisplay(int pageNumber,int pageCount,DynamicVectorRenderer currentDisplay) {

		this.pageNumber=pageNumber;
		this.pageCount=pageCount;
		this.currentDisplay=currentDisplay;

	}

	public SingleDisplay(PdfDecoder pdf) {
		this.pdf=pdf;

	}

	/**
	 * used by Storypad to display split spreads not aprt of API
	 */
	public void clearAdditionalPages() {

		offsets.clear();
		newPages.clear();
		currentXOffset=0;
		additionalPageCount=0;
	}

	/**
	 * used by Storypad to display split spreads not aprt of API
	 */
	public void addAdditionalPage(DynamicVectorRenderer dynamicRenderer, int pageWidth, int origPageWidth) {

		//store
		offsets.addElement(currentXOffset+origPageWidth);
		newPages.put(new Integer(currentXOffset+origPageWidth),dynamicRenderer);
		additionalPageCount++;

		//work out new offset
		currentXOffset=currentXOffset+pageWidth;

		//force redraw
		this.oldScaling=-oldScaling;
		this.refreshDisplay();

	}


	VolatileImage backBuffer = null;

	public void dispose(){
		
		if(backBuffer!=null)
		backBuffer.flush();
		
		currentOffset=null;
		this.areas=null;
		this.pageH=null;
		this.pageW=null;
		this.cachedPageViews=null;
		this.isRotated=null;
		
		backBuffer=null;
		
		accleratedPagesAlreadyDrawn=null;
	}
	
	protected void createBackBuffer() {
		final boolean debugBackBuffer = false;
		
		if(debugLayout)
			System.out.println("createBackBuffer");
		
		if(debugBackBuffer)
			System.out.println("Flushing back buffer");
		
		if (backBuffer != null) {
			backBuffer.flush();
			backBuffer = null;
		}

		int width=0,height=0;
		
		if(debugBackBuffer)
			System.out.println("Setting height and width");
		
		if(displayView==SINGLE_PAGE){
			if((displayRotation==90)|(displayRotation==270)){
				width=(volatileHeight+currentXOffset);
				height=volatileWidth;
			}else{
				width=volatileWidth+currentXOffset;
				height=volatileHeight;
			}
		}else if(currentOffset!=null){

			if(debugBackBuffer)
				System.out.println("Also setting offset");
			
			//height for facing pages
			int biggestFacingHeight=0;

			if((displayView==FACING)&&(pageW!=null)){
				//get 2 facing page numbers
				int p1=pageNumber;
				if((p1 & 1)==1)
					p1--;
				int p2=p1+1;

				/* if((displayRotation==90)|(displayRotation==270)){
                    biggestFacingHeight=pageH[p1];
                    if(p2<pageH.length && biggestFacingHeight<pageH[p2])
                        biggestFacingHeight=pageH[p2];
                }else{
                    biggestFacingHeight=pageH[p1];
                    if(p2<pageH.length && biggestFacingHeight<pageH[p2])
                        biggestFacingHeight=pageH[p2];
                }*/

				biggestFacingHeight=pageH[p1];
				if(p2<pageH.length && biggestFacingHeight<pageH[p2])
					biggestFacingHeight=pageH[p2];
			}

			int gaps=currentOffset.gaps;
			int doubleGaps=currentOffset.doubleGaps;


			switch(displayView){

			case FACING:

				if((displayRotation==90)|(displayRotation==270)){
					width=(int)((currentOffset.doublePageHeight+insetW)*scaling);
					height=(biggestFacingHeight);	//NOTE scaled already!
				}else{
					width=(int)((currentOffset.doublePageWidth+insetW)*scaling);
					height=(biggestFacingHeight);	//NOTE scaled already!
				}
				break;


			case CONTINUOUS:

				if((displayRotation==90)|(displayRotation==270)){
					width=(int)(currentOffset.biggestHeight*scaling);
					height=(int)((currentOffset.totalSingleWidth)*scaling)+gaps+insetH;
				}else{
					width=(int)(currentOffset.biggestWidth*scaling);
					height=(int)((currentOffset.totalSingleHeight)*scaling)+gaps+insetH;
				}
				break;


			case CONTINUOUS_FACING:

				if((displayRotation==90)|(displayRotation==270)){
					width=(int)((currentOffset.doublePageHeight)*scaling)+(insetW*2)+doubleGaps;
					height=(int)((currentOffset.totalDoubleWidth)*scaling)+doubleGaps+insetH;
				}else{
					width=(int)((currentOffset.doublePageWidth)*scaling)+(insetW*2);
					height=(int)((currentOffset.totalDoubleHeight)*scaling)+doubleGaps+insetH;
				}
				break;

			case SIDE_SCROLL:


                if(displayRotation==90 || displayRotation==270){
				    //width=(int)(((PageOffsets.SIDE_COLLAPSED_PAGE_SIZE *(PageOffsets.SIDE_SCROLL_PAGES*2))+(pageH[pageNumber]*2))*scaling);
				    width=(int)(((PageOffsets.SIDE_COLLAPSED_PAGE_SIZE *(pageCount-1))+(pageH[pageNumber]))*scaling);
                    height=(int)((currentOffset.biggestWidth*scaling));
				}else{
                    //width=(int)((((PageOffsets.SIDE_COLLAPSED_PAGE_SIZE *scaling*(PageOffsets.SIDE_SCROLL_PAGES*2))+(pageW[pageNumber]))));
                    width=(int)((((PageOffsets.SIDE_COLLAPSED_PAGE_SIZE *(pageCount-1))+(pageW[pageNumber]))*scaling));
                    height=(int)(currentOffset.biggestHeight*scaling);

                    //System.out.println("size="+width+" "+height+" "+currentOffset.totalSingleWidth);
                    
                }
				break;
			}
		}

		try{
			if(debugBackBuffer)
				System.out.println("Handle Huge pages if any");
			//avoid huge pages as VERY slow
			if(height>15000){
				volatileHeight=0;
				height=0;
				overRideAcceleration=true;
			}
			if(debugBackBuffer)
				System.out.println("Create back buffer");
			if((width>0)&&(height>0)){
				backBuffer = pdf.createVolatileImage(width,height);
				oldVolatileWidth=volatileWidth;
				oldVolatileHeight=volatileHeight;

				Graphics2D gg=(Graphics2D) backBuffer.getGraphics();
				gg.setPaint(pdf.getBackground());
				gg.fillRect(0,0,width,height);
			}

		}catch(Error e){ //switch off if not enough memory
			if(debugBackBuffer)
				System.out.println("Acceleration issue found");
			overRideAcceleration=true;
			backBuffer=null;
		}catch(Exception e){
			if(debugBackBuffer)
				System.out.println("Issue found in display mode");
			e.printStackTrace();
			LogWriter.writeLog("Exception "+e+" is display mode");
		}

		if(debugBackBuffer)
			System.out.println("Painting optimised");
        //don't redraw
        currentDisplay.setOptimsePainting(true);

    }
	
	public boolean isAccelerated() {

        if(!useAcceleration || overRideAcceleration){
			return false;
		}else
			return true;
		
	}


	public void resetCachedValues() {

		//rest page views
		lastPageChecked = -1;
		lastState = -1;
	}

	public void stopGeneratingPage(){

		//request any processes die
		isGeneratingOtherPages=false;

		//wait to die
		while (running) {
			// System.out.println("Waiting to die");
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// should never be called
				e.printStackTrace();
			}
		}

	}

	public void disableScreen(){
		isInitialised=false;
	}


	boolean testAcceleratedRendering() {


		boolean canDrawAccelerated=false;

		//force redraw if page rescaled
		if((oldScaling!=scaling)||(oldRotation!=displayRotation)||
				(oldVolatileWidth!=volatileWidth)||(oldVolatileHeight!=volatileHeight)){
			backBuffer=null;
			overRideAcceleration=false;

		}

		if(DynamicVectorRenderer.debugPaint)
			System.err.println("acceleration called "+backBuffer);

		if (!overRideAcceleration &&(backBuffer == null)){//||(screenNeedsRedrawing)){

			createBackBuffer();
			accleratedPagesAlreadyDrawn.clear();

		}


		if (backBuffer != null){
			do {
				// First, we validate the back buffer
				int valCode = VolatileImage.IMAGE_INCOMPATIBLE;
				if(backBuffer!=null)
					valCode = backBuffer.validate(pdf.getGraphicsConfiguration());

				if (valCode == VolatileImage.IMAGE_RESTORED) {
					// This case is just here for illustration
					// purposes.  Since we are
					// recreating the contents of the back buffer
					// every time through this loop, we actually
					// do not need to do anything here to recreate
					// the contents.  If our VImage was an image that
					// we were going to be copying _from_, then we
					// would need to restore the contents at this point
				} else if (valCode == VolatileImage.IMAGE_INCOMPATIBLE) {
					if(!overRideAcceleration)
						createBackBuffer();

				}

				// Now we've handled validation, get on with the rendering
				if((backBuffer!=null)){

					canDrawAccelerated=true;


				}

				// Now we are done; or are we?  Check contentsLost() and loop as necessary
			} while ((backBuffer==null)||(backBuffer.contentsLost()));
		}
		return canDrawAccelerated;

	}

	public Dimension getPageSize(int displayView) {

		Dimension pageSize=null;

		//height for facing pages
		int biggestFacingHeight=0;

		if((displayView==FACING)&&(pageW!=null)){
			//get 2 facing page numbers
			int p1=pageNumber;
			if((p1 & 1)==1)
				p1--;
			int p2=p1+1;

			if((displayRotation==90)|(displayRotation==270)){
				biggestFacingHeight=pageW[p1];
				if(p2<pageW.length && biggestFacingHeight<pageW[p2])
					biggestFacingHeight=pageW[p2];
			}else{
				biggestFacingHeight=pageH[p1];
				if(p2<pageH.length && biggestFacingHeight<pageH[p2])
					biggestFacingHeight=pageH[p2];
			}
		}

		int gaps=currentOffset.gaps;
		int doubleGaps=currentOffset.doubleGaps;

		switch(displayView){

		case FACING:

			if((displayRotation==90)|(displayRotation==270))
				pageSize= new Dimension((int)((currentOffset.doublePageHeight*scaling)+insetW+insetW),(int)((biggestFacingHeight*scaling)+insetH+insetH));
			else
				pageSize= new Dimension((int)((currentOffset.doublePageWidth*scaling)+insetW+insetW),(int)((biggestFacingHeight*scaling)+insetH+insetH));

			break;


		case CONTINUOUS:

			if((displayRotation==90)|(displayRotation==270))
				pageSize= new Dimension((int)((currentOffset.biggestHeight*scaling)+insetW+insetW),(int)((currentOffset.totalSingleWidth*scaling)+gaps+insetH+insetH));
			else
				pageSize= new Dimension((int)((currentOffset.biggestWidth*scaling)+insetW+insetW),(int)((currentOffset.totalSingleHeight*scaling)+gaps+insetH+insetH));

			break;


		case CONTINUOUS_FACING:

			if((displayRotation==90)|(displayRotation==270)){
				if(pageCount == 2)
					pageSize= new Dimension((int)((currentOffset.doublePageHeight*scaling)+insetW+insetW),(int)((currentOffset.biggestWidth*scaling)+gaps+insetH+insetH));
				else
					pageSize= new Dimension((int)((currentOffset.doublePageHeight*scaling)+insetW+insetW),(int)((currentOffset.totalDoubleWidth*scaling)+doubleGaps+insetH+insetH));
			}else{
				if(pageCount == 2)
					pageSize= new Dimension((int)((currentOffset.doublePageWidth*scaling)+insetW+insetW),(int)((currentOffset.biggestHeight*scaling)+gaps+insetH+insetH));
				else
					pageSize= new Dimension((int)((currentOffset.doublePageWidth*scaling)+insetW+insetW),(int)((currentOffset.totalDoubleHeight*scaling)+doubleGaps+insetH+insetH));
			}
			break;

		case SIDE_SCROLL:
			if((displayRotation==90)||(displayRotation==270)){
				pageSize= new Dimension((int)((((PageOffsets.SIDE_COLLAPSED_PAGE_SIZE *(pageCount-1))+(pageH[pageNumber]*2))*scaling)+insetH+insetH),(int)((currentOffset.biggestWidth*scaling)+insetW+insetW));
			}else{
				pageSize= new Dimension((int)((((PageOffsets.SIDE_COLLAPSED_PAGE_SIZE *(pageCount-1))+(pageW[pageNumber]*2))*scaling)+insetW+insetW),(int)((currentOffset.biggestHeight*scaling)+insetH+insetH));

            }

//            int cropW = pageData.getCropBoxWidth(pageNumber);
//            int cropH = pageData.getCropBoxHeight(pageNumber);
//            //int cropX = pageData.getCropBoxX(pageNumber);
//            //int cropY = pageData.getCropBoxY(pageNumber);
//
//            int x_size_cropped = (int)(cropW*scaling);
//            int y_size_cropped = (int)(cropH*scaling);
//
//            if((displayRotation==90)|(displayRotation==270))
//				pageSize= new Dimension((int)(y_size_cropped+insetW+insetW)+((numberPagesOnSide+numberPagesOnSide+pageCount),x_size_cropped+insetH+insetH);
//			else
//				pageSize= new Dimension((int)(x_size_cropped+insetW+insetW),y_size_cropped+insetH+insetH);

			break;
		}

		if(debugLayout)
			System.out.println("pageSize"+pageSize);

		return pageSize;
	}

	public Rectangle getDisplayedRectangle() {

		Rectangle userAnnot=pdf.getVisibleRect();

		//get raw rectangle
		/**if((this.displayRotation==90)||(this.displayRotation==270)){
            ry =userAnnot.x;
            rx =userAnnot.y ;
            rh =userAnnot.width;
            rw =userAnnot.height;
        }else{*/
		rx =userAnnot.x;
		ry =userAnnot.y;
		rw =userAnnot.width;
		rh =userAnnot.height;


		//System.out.println("R's -> (x,y) " + rx + " " + ry);
		return userAnnot;
	}



	public void drawBorder() {

        if(PdfDecoder.CURRENT_BORDER_STYLE==PdfDecoder.BORDER_SHOW){

			if((crw >0)&&(crh >0)&&(myBorder!=null))
				myBorder.paintBorder(pdf,g2,crx-1, cry-1, crw+1, crh+1);
		}
	}

	void setDisplacementOnG2(Graphics2D gBB) { //multi pages assumes all page 0 and rotates for each
		//translate the graphic to 0,0 on volatileImage java co-ords
		float cX=crx /scaling,cY=cry /scaling;
		if(displayRotation==0 || this.displayView!=Display.SINGLE_PAGE)
			gBB.translate(-cX,cY);
		else if(displayRotation==90)
			gBB.translate(-cY,-cX);
		else if(displayRotation==180)
			gBB.translate(cX,-cY);
		else if(displayRotation==270)
			gBB.translate(cY,cX);

	}

	protected void getPageView(PdfObject Resources, PdfObject pdfObject, DynamicVectorRenderer currentDisplay, int pageNumber) {

        if(pdf.layers!=null)
        pdf.layers.setScaling(pdf.scaling);
        PdfStreamDecoder current = new PdfStreamDecoder(pdf.useHiResImageForDisplay, pdf.layers);

		current.setExternalImageRender(pdf.customImageHandler);
        
		current.setName(pdf.filename);
		current.setStore(pdf.objectStoreRef);
		//if (includeImages)
		//  current.includeImages();

		/** set hires mode or not for display */
		currentDisplay.setHiResImageForDisplayMode(pdf.useHiResImageForDisplay);

		try {
			current.init(true, true, 7, 0, pageData,pageNumber, currentDisplay,pdf.currentPdfFile);

			if (pdf.globalResources != null){
				pdf.currentPdfFile.checkResolved(pdf.globalResources);
				current.readResources(pdf.globalResources,true);

                PdfObject propObj=pdf.globalResources.getDictionary(PdfDictionary.Properties);
                if(propObj!=null)
                pdf.PropertiesObj=propObj;
            }

            /**read the resources for the page*/
			if (Resources != null){
                pdf.currentPdfFile.checkResolved(Resources);
                current.readResources(Resources,true);

                PdfObject propObj=Resources.getDictionary(PdfDictionary.Properties);
                if(propObj!=null)
                pdf.PropertiesObj=propObj;
            }

            pdf.setupPage(current, false);

            int mediaW = pageData.getMediaBoxWidth(pageNumber);
			int mediaH = pageData.getMediaBoxHeight(pageNumber);

			int rotation = pageData.getRotation(pageNumber);

			currentDisplay.init(mediaW, mediaH,rotation);

			current.decodePageContent(pdfObject, 0,0, null, null);
		} catch (Exception ex) {
		} catch (Error err) {
			pdf.decodeStatus = pdf.decodeStatus+ "Error in decoding page "+ err.toString();
			System.out.println(err);
		}

	}

	public void refreshDisplay(){

		screenNeedsRedrawing = true;
		// reset over-ride which may have been enabled

		accleratedPagesAlreadyDrawn.clear();

		overRideAcceleration = false;
	}

	public void flushPageCaches() {
		currentPageViews.clear();
		cachedPageViews.clear();
	}

	public void init(float scaling, int pageCount, int displayRotation, int pageNumber,
			DynamicVectorRenderer currentDisplay, boolean isInit, PdfPageData pageData,int insetW, int insetH){

		//if(debugLayout)
		//    System.out.println("init");

        this.currentDisplay=currentDisplay;
		this.scaling=scaling;
		this.pageCount=pageCount;
		this.displayRotation=displayRotation;
		this.pageNumber=pageNumber;
		this.pageData=pageData;

		this.insetW=insetW;
		this.insetH=insetH;

		currentDisplay.setInset(insetW,insetH);

		//reset over-ride which may have been enabled
        pageData.setScalingValue(scaling); //ensure aligned
        volatileWidth = pageData.getScaledCropBoxWidth(this.pageNumber);
		volatileHeight = pageData.getScaledCropBoxHeight(this.pageNumber);

		if(isInit){
			lastPageChecked=-1;
			setPageOffsets(pageCount,this.pageNumber);
			isInitialised=true;
		}
	}

	/**
	 * workout offsets so we  can draw pages
	 * */
	public void setPageOffsets(int pageCount,int pageNumber) {

		if(debugLayout)
			System.out.println("setPageOffsets "+pageNumber+ ' ' +pageCount+" displayView="+displayView+" scaling="+scaling);

		if(displayView==1)
			return ;

		lastPageChecked=pageNumber;
		lastState=displayView;

		xReached=new int[pageCount+1];
		yReached=new int[pageCount+1];
		pageW=new int[pageCount+1];
		pageH=new int[pageCount+1];

		int heightCorrection = 0;
		int displayRotation = 0;

		isRotated=new boolean[pageCount+1];
		int gap=currentOffset.pageGap;//set.pageGap*scaling);

		//Used to help allign first page is page 2 is cropped / rotated
		int LmaxWidth=0;
		int LmaxHeight=0;
		int RmaxWidth=0;
		int RmaxHeight=0;

        /**work out page sizes - need to do it first as we can look ahead*/
		for(int i=1;i<pageCount+1;i++){

			/**
			 * get unrotated page sizes
			 */
			pageW[i]=pageData.getScaledCropBoxWidth(i);
			pageH[i]=pageData.getScaledCropBoxHeight(i);

			displayRotation=pageData.getRotation(i)+this.displayRotation;
			if(displayRotation>=360)
				displayRotation=displayRotation-360;
			
			//swap if this page rotated and flag
			if((displayRotation==90|| displayRotation==270)){
				int tmp=pageW[i];
				pageW[i]=pageH[i];
				pageH[i]=tmp;

				isRotated[i]=true; //flag page as rotated
			}

			if((i&1)==1){
					if(pageW[i]>RmaxWidth)
						RmaxWidth = pageW[i];
					if(pageH[i]>RmaxHeight)
						RmaxHeight = pageH[i];
			}else{
					if(pageW[i]>LmaxWidth)
						LmaxWidth = pageW[i];
					if(pageH[i]>LmaxHeight)
						LmaxHeight = pageH[i];
			}
		}

		//loop through all pages and work out positions
		for(int i=1;i<pageCount+1;i++){
			heightCorrection = 0;
			if((pageCount==2)&&(displayView==FACING || displayView==CONTINUOUS_FACING)){ //special case
				//if only 2 pages display side by side
				if(i==1){
					xReached[1]=0;
					yReached[1]=0;
				}else{
					xReached[2]=xReached[1]+pageW[1]+gap;
					yReached[2]=0;
				}

			}else if(i==1){  //first page is special case
				//First page should be on the left so indent
				if(displayView==CONTINUOUS){
					xReached[1]=0;
					yReached[1]=0;
				}else if(displayView==SIDE_SCROLL){
					//Start from center of display
					xReached[1]=0;//((rw/2)-(pageW[1]/2));
					yReached[1]=0;
				}else if((displayView==FACING)||(displayView==CONTINUOUS_FACING)){
				
						xReached[1]=LmaxWidth+gap;
						yReached[1]=0;
				}
			}else{
				//Calculate position for all other pages / cases
				if((displayView==CONTINUOUS_FACING)){
					//Left Pages
					if((i & 1)==0){
						//Last Page rotated so correct height
						if(i<pageCount)
							heightCorrection = (pageH[i+1]-pageH[i])/2;
						if(heightCorrection<0)
							heightCorrection = 0;//-heightCorrection;
						if(i>3){
							int temp = (pageH[i-2]-pageH[i-1])/2;
							if(temp>0)
								heightCorrection = heightCorrection+temp;
						}
						yReached[i] = (yReached[i-1]+pageH[i-1] +gap)+heightCorrection;
					}else{ //Right Pages
						//Last Page rotated so correct height 
						heightCorrection = (pageH[i-1]-pageH[i])/2;
						yReached[i] = (yReached[i-1])+heightCorrection;
					}

					if((i & 1)==0){//Indent Left pages by diff between maxWidth and pageW (will only indent unrotated)
						xReached[i] = xReached[i] + (LmaxWidth-pageW[i]); 
					}else{//Place Right Pages with a gap (This keeps pages centered)
						xReached[i] = xReached[i-1]+pageW[i-1] +gap;
					}

				}else if(displayView==CONTINUOUS){
					//Place page below last with gap
					yReached[i] = (yReached[i-1]+pageH[i-1]+gap);
					if(isRotated[i]) //if rotated indent to left to keep centred
						xReached[i] = xReached[i]-((LmaxWidth-pageW[i])/2);
				}else if(displayView==SIDE_SCROLL){
					//Place page to right with gap
					//xReached[i] = (xReached[i-1]+pageW[i-1]/2);//+gap);
					xReached[i] = (int) (xReached[i-1]+(PageOffsets.SIDE_COLLAPSED_PAGE_SIZE*scaling));//+gap);

                    //if(isRotated[i]) //if rotated indent to left to keep centred
					//	yReached[i] = yReached[i]-((LmaxHeight-pageH[i])/2);
			}else if((displayView==FACING)){
					if((i&1)==1){ //If right page, place on right with gap 
						xReached[i] = (xReached[i-1]+pageW[i-1]+gap);
						if(pageH[i] < pageH[i-1])//Drop page down to keep pages centred
							yReached[i] = yReached[i]+((pageH[i-1]-pageH[i])/2);
					}else{ //If left page, indent by diff of max and current page
						xReached[i] = ((LmaxWidth-pageW[i])/2);
						if(i<pageCount)
							if(pageH[i] < pageH[i+1])//Drop page down to keep pages centered
								yReached[i] = yReached[i]+((pageH[i+1]-pageH[i])/2);
					}

				}
			}
		}

		//<start-adobe>    
		pdf.setMultiPageOffsets(xReached,yReached);
		//<end-adobe>

	}

	public void decodeOtherPages(int pageNumber, int pageCount) {
	}


	public void completeForm(Graphics2D g2) {
		g2.drawLine(crx, cry, crx +crw,cry +crh);
		g2.drawLine(crx, crh +cry, crw +crx,cry);
	}

	public void resetToDefaultClip() {

		if(current2!=null)
			g2.setTransform(current2);

		//reset transform and clip
		if(currentClip!=null)
			g2.setClip(currentClip);

	}


	public void initRenderer(Rectangle[] areas, Graphics2D g2,Border myBorder,int indent){

		//if(debugLayout)
		//    System.out.println("initRenderer");

        this.rawAf=g2.getTransform();
		this.rawClip=g2.getClip();

		this.areas=areas;
		this.g2=g2;

		this.myBorder=myBorder;

		this.indent=indent;
		pagesDrawn.clear();

		setPageSize(pageNumber, scaling);

	}

	void setPageSize(int pageNumber, float scaling) {

        /**
		 *handle clip - crop box values
		 */
        pageData.setScalingValue(scaling); //ensure aligned
        topW=pageData.getScaledCropBoxWidth(pageNumber);
		topH=pageData.getScaledCropBoxHeight(pageNumber);
		double mediaH=pageData.getScaledMediaBoxHeight(pageNumber);

		cropX=pageData.getScaledCropBoxX(pageNumber);
		cropY=pageData.getScaledCropBoxY(pageNumber);
		cropW=topW;
		cropH=topH;

		/**
		 * actual clip values - for flipped page
		 */
		if(displayView==Display.SINGLE_PAGE){
			crx =(int)(insetW+cropX);
			cry =(int)(insetH-cropY);
		}else{
			crx =insetW;
			cry =insetH;
		}
		//cry =(int)(insetH+(mediaH-cropH)-cropY);

		//amount needed to move cropped page into correct position
		//int offsetX=(int) (mediaW-cropW);
		int offsetY=(int) (mediaH-cropH);

		if((displayRotation==90||(displayRotation==270))){
			crw =(int)(cropH);
			crh =(int)(cropW);

			int tmp = crx;
			crx = cry;
			cry = tmp;

			crx=crx+offsetY;
		}else{
			crw =(int)(cropW);
			crh =(int)(cropH);

			cry=cry+offsetY;
		}
		/**/
		g2.translate(insetW-crx,insetH-cry);

		//save any transform and stroke
		current2 = g2.getTransform();

		//save global clip and set to our values
		currentClip =g2.getClip();

		//if(!showCrop)
		g2.clip(new Rectangle(crx,cry, (int) (crw+( (insetW*additionalPageCount)+currentXOffset*scaling)),crh));
         
        //System.out.println("Set pagesize "+pageNumber+" scaling="+scaling+" dimensions="+crx+" "+cry+" "+crw+" "+crh+" insetW="+insetW+" currentXOffset="+currentXOffset);

    }

	int topW,topH;
	double cropX,cropY,cropW,cropH;

	boolean thumbnailsRunning;

	//<start-adobe>
	//<start-thin>
	protected GUIThumbnailPanel thumbnails=null;
	//<end-thin>
	//<end-adobe>



	Rectangle calcVisibleArea(int topY, int topX) {

        Rectangle userAnnot;
		/**get area to draw*/
		int x=0,y=0,h,w,gap=0;

		//update coords
		getDisplayedRectangle();

		if((displayRotation!=270)&&(displayRotation!=180)&&(rx >insetW))
			x=(int)((rx -insetW)/scaling);

		//set defaults
		w=(int)((rw +insetW)/scaling);
		h= topY;

		if(displayRotation==0 || this.displayView!=SINGLE_PAGE){
			
			x =(int)(rx/scaling) - (int)(insetW/scaling);
			
			h =(int)((rh+insetH)/scaling);
			w =(int)((rw-insetW)/scaling) + (int)(insetW/scaling);
			
			if((x)<(insetW/scaling))
				x=(int)(insetW/scaling);
			
			if(y<(insetH/scaling))
				y=0;
           
			y=(int) (topY-((ry+rh)/scaling));
            
        }else if(displayRotation==90){

        	y =(int)((rx -insetW)/scaling);
        	h =(int)((rw +insetW)/scaling);;

			if(ry >insetW){
				x=(int)((ry -insetW)/scaling);
			}else{
				x=0;
			}
			w=(int)((rh)/scaling);
			
		}else if(displayRotation==270){
			
			w=(int)((rh + insetW)/scaling);
			x=topX-((int)((ry +rh)/scaling));
			h=(int)((rw +insetH)/scaling);
			y=topY-((int)((rx +rw)/scaling));

			if(x<insetH){
				x=0;
				w = w + insetH;
			}
				
			if(y<insetW){
				y=0;
				h = h + insetW;
			}
			
		}else if(displayRotation==180){

			h=(int)((rh +insetH)/scaling) + insetH;
			y=((int)((ry -insetH)/scaling));
			w=(int)((rw +insetW)/scaling) ;
			x=topX-((int)((rx -insetW)/scaling));
			x=x-w;

			//Check x or y is not negative
			if(x<0 || x<insetH){
				x=0;
				w = w + insetH;
			}
			if(y<insetW){
				y=0;
				h = h+ insetW;
			}
			
		}

		//do not use on hardware acceleration
		if(isAccelerated()||(scaling>=2)){
			int dx1=pageData.getScaledCropBoxX(pageNumber);
			int dy1=pageData.getScaledCropBoxY(pageNumber);

			//System.err.println("dx1 and dy1 == " + dx1 + " " + dy1);
			
			gap =2;
			
			if(dx1 != 0 || dy1 !=0){
				userAnnot=new Rectangle(x+(int)(dx1/scaling),y+(int)(dy1/scaling),w,h);
			} else {
				userAnnot=new Rectangle(x,y,w+(gap+gap),h+(gap+gap));
			}
			
		}else
			userAnnot=null;

		if((displayView!= Display.SINGLE_PAGE)&&(message)){
			message=true;
			System.out.println("SingleDisplay fast scrolling");
			userAnnot=null;
		}


		if(debugLayout)
			System.out.println("userAnnot="+userAnnot+" scaling="+scaling);
		
		
		return userAnnot;
	}

	public Rectangle drawPage(AffineTransform viewScaling, AffineTransform displayScaling, int pageUsedForTransform, PdfPanel pdfPanel) {

        Rectangle actualBox=null;

		/**add in other elements*/
		if((displayScaling!=null)&&(currentDisplay!=null)){

			//reset on scaling dropping back below 200 so retested below
			if(scaling<2 && oldScaling>2)
				useAcceleration=true;

            if(useAcceleration || areas!=null)
            screenNeedsRedrawing=true;
			
            boolean canDrawAccelerated=false;
            //use hardware acceleration - it sucks on large image so
            //we check scaling as well...
            if((useAcceleration)&&(!overRideAcceleration)&&(scaling<2))
				canDrawAccelerated= testAcceleratedRendering();
            else
                useAcceleration=false;
            
            
			/**
			 * pass in values as needed for patterns
			 */
			currentDisplay.setScalingValues(cropX,cropH+cropY,scaling);//@kieran

			g2.transform(displayScaling);

			if(DynamicVectorRenderer.debugPaint)
				System.err.println("accelerate or redraw");

			if(canDrawAccelerated){
				// rendering to the back buffer:
				Graphics2D gBB = (Graphics2D)backBuffer.getGraphics();

				if(screenNeedsRedrawing){
					
					//fill background white to prevent memory overRun from previous graphics memory
					gBB.setColor(currentDisplay.getBackgroundColor());
					gBB.fill(new Rectangle(0,0,backBuffer.getWidth(),backBuffer.getHeight()));

                    currentDisplay.setOptimsePainting(true);
                                        
					gBB.setTransform(displayScaling);
					setDisplacementOnG2(gBB);

                    actualBox =currentDisplay.paint(gBB,areas,viewScaling,userAnnot,false,true);


                    drawSpreadPages(gBB,viewScaling,userAnnot,false);

					screenNeedsRedrawing =false;

                }

				gBB.dispose();

				if(backBuffer !=null){

					//draw the buffer
					AffineTransform affBefore=g2.getTransform();

					g2.setTransform(rawAf);
					g2.drawImage(backBuffer, insetW,insetH, pdf);
					g2.setTransform(affBefore);

					/**
					 * 	draw other page outlines and any decoded pages so visible
					 */
					actualBox =currentDisplay.paint(g2,areas,viewScaling,userAnnot,true,true);

					drawSpreadPages(g2,viewScaling,userAnnot,true);
				}
			}else{
				if(DynamicVectorRenderer.debugPaint)
					System.err.println("standard paint called ");

				/**
				 * 	draw other page outlines and any decoded pages so visible
				 */
				//same rectangle works for any rotation so removed the rotation check
//				System.out.println("a");
				g2.clip(new Rectangle((int)(cropX/scaling),(int)(cropY/scaling),(int)(topW/scaling),(int)(topH/scaling)));
				
				currentDisplay.setOptimsePainting(false);
                
                actualBox =currentDisplay.paint(g2,areas,viewScaling,userAnnot,false,true);

				drawSpreadPages(g2,viewScaling,userAnnot,false);
			}

			//track scaling so we can update dependent values
			oldScaling=scaling;
			oldRotation=displayRotation;


		}
		
		return actualBox;
	}

	private void drawSpreadPages(Graphics2D g2,AffineTransform viewScaling,Rectangle userAnnot,boolean flag){

		if(currentXOffset>0){
			int[] currentOffset=offsets.get();

			int count=currentOffset.length;

			int currentValue=0;
			for(int i=0;i<count;i++){

				currentValue=currentOffset[i];

				if(currentValue>0){
					DynamicVectorRenderer renderer= (DynamicVectorRenderer) newPages.get(new Integer(currentValue));

					g2.setClip(null);
					g2.translate(currentValue,0);
					if(renderer!=null)
						renderer.paint(g2,areas,viewScaling,userAnnot,flag,true);

					g2.translate(-currentValue,0);
				}
			}
		}
	}

	public void setup(boolean useAcceleration,PageOffsets currentOffset,PdfDecoder pdf){

		this.useAcceleration=useAcceleration;
		this.currentOffset=currentOffset;
		this.pdf=pdf;

		overRideAcceleration=false;

	}

	public int getXCordForPage(int page){
		return xReached[page]+insetW;
	}

	public int getYCordForPage(int page){
		return yReached[page]+insetH;
	}

	public int getStartPage() {
		return this.startViewPage;
	}

	public int getEndPage() {
		return this.endViewPage;
	}

	public void setThumbnailsDrawing(boolean b) {
		thumbnailsRunning=b;

	}

	//<start-adobe>
	//<start-thin>
	public void setThumbnailPanel(GUIThumbnailPanel thumbnails) {
		this.thumbnails=thumbnails;

	}
	//<end-thin>
	//<end-adobe>

	public void setScaling(float scaling) {
		this.scaling=scaling;
		pageData.setScalingValue(scaling);
	}
	
	public Rectangle getCurrentPageCoords(){
		return new Rectangle(crx-1, cry-1, crw+1, crh+1);
	}

	public void setAcceleration(boolean enable) {
		useAcceleration = enable;
	}
}
