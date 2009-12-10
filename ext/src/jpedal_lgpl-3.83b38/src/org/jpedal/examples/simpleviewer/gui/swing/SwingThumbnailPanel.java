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
* SwingThumbnailPanel.java
* ---------------
*/
package org.jpedal.examples.simpleviewer.gui.swing;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.io.File;

import javax.swing.*;
import javax.imageio.ImageIO;

import org.jpedal.PdfDecoder;
import org.jpedal.ThumbnailDecoder;
import org.jpedal.examples.simpleviewer.Values;
import org.jpedal.examples.simpleviewer.gui.generic.GUIThumbnailPanel;

import org.jpedal.objects.PdfPageData;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.SwingWorker;
import org.jpedal.utils.repositories.Vector_Object;

/**
 * Used in GUI example code.
 * <br>adds thumbnail capabilities to viewer,
 * <br>shows pages as thumbnails within this panel,
 * <br>So this panel can be added to the viewer
 *
 */
public class SwingThumbnailPanel extends JScrollPane implements GUIThumbnailPanel {

    static final boolean debugThumbnails=false;

    /**Swing thread to decode in background - we have one thread we use for various tasks*/
	SwingWorker worker=null;

    JPanel panel=new JPanel();

    /**handles drawing of thumbnails if needed*/
	private ThumbPainter painter=new ThumbPainter();
	
    /**can switch on or off thumbnails*/
	private boolean showThumbnailsdefault=true;
    
    private boolean showThumbnails=showThumbnailsdefault;

	/**flag to allow interruption in orderly manner*/
	public boolean interrupt=false;

	/**flag to show drawig taking place*/
	public boolean drawing;

    /**custom decoder to create Thumbnails*/
    public ThumbnailDecoder thumbDecoder;

    /**
	 * thumbnails settings below
	 */
	/**buttons to display thumbnails*/
	private JButton[] pageButton;

	private boolean[] buttonDrawn;

	private boolean[] isLandscape;

	private int[] pageHeight;

	/**weight and height for thumbnails*/
	static final private int thumbH=100,thumbW=70;

	Values commonValues;
	final PdfDecoder decode_pdf;

    boolean isExtractor=false;
	private int lastPage=-1; //flag to ensure only changes result in processing


	public SwingThumbnailPanel(Values commonValues, final PdfDecoder decode_pdf){

		if(debugThumbnails)
				System.out.println("SwingThumbnailPanel");

		setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		this.commonValues=commonValues;
		this.decode_pdf=decode_pdf;

		thumbDecoder=new ThumbnailDecoder(decode_pdf);

		this.addComponentListener(new ComponentListener(){
			public void componentResized(ComponentEvent componentEvent) {

				if(!isExtractor){
					/** draw thumbnails in background, having checked not already drawing */
					if(drawing)
					terminateDrawing();

					drawThumbnails();
				}
			}

			public void componentMoved(ComponentEvent componentEvent) {
				//To change body of implemented methods use File | Settings | File Templates.
			}

			public void componentShown(ComponentEvent componentEvent) {
				//To change body of implemented methods use File | Settings | File Templates.
			}

			public void componentHidden(ComponentEvent componentEvent) {
				//To change body of implemented methods use File | Settings | File Templates.
			}
		});
	}

//
	/** class to paint thumbnails */
	private class ThumbPainter extends ComponentAdapter {

        boolean requestMade=false;
        /** used to track user stopping movement */
		Timer trapMultipleMoves = new Timer(250,
				new ActionListener() {

			public void actionPerformed(ActionEvent event) {

                if(!requestMade){

                    requestMade=true;

                    if(debugThumbnails)
                            System.out.println("actionPerformed");

                    if(commonValues.isProcessing()){
                    	if(debugThumbnails)
                        System.out.println("Still processing page");
                    }else{

                        if(debugThumbnails)
                        System.out.println("actionPerformed2");

                        /**create any new thumbnails revaled by scroll*/
                        /** draw thumbnails in background, having checked not already drawing */
                        if(drawing)
                        terminateDrawing();

                        if(debugThumbnails)
                        System.out.println("actionPerformed3");

                        requestMade=false;

                        drawThumbnails();
                    }
                }
            }
		});

		/*
		 * (non-Javadoc)
		 *
		 * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
		 */
		public void componentMoved(ComponentEvent e) {

            //allow us to disable on scroll
			if (trapMultipleMoves.isRunning())
				trapMultipleMoves.stop();

			trapMultipleMoves.setRepeats(false);
			trapMultipleMoves.start();

		}
	}
	
    /**
     * create thumbnails of general images
     * @param thumbnailsStored
     */
    public void generateOtherThumbnails(String[] imageFiles, Vector_Object thumbnailsStored) {


        if(debugThumbnails)
        System.out.println("generateOtherThumbnails>>>>>>>>>>>>");

        drawing=true;

        getViewport().removeAll();
        panel.removeAll();

        /** draw thumbnails in background */
        int pages = imageFiles.length;

        //create display for thumbnails
        getViewport().add(panel);
        panel.setLayout(new GridLayout(pages,1,0,10));


        for (int i = 0; i < pages; i++) {

            //load the image to process
            BufferedImage page = null;
            try {
                // Load the source image from a file or cache
                if(imageFiles[i]!=null){

                    Object cachedThumbnail=thumbnailsStored.elementAt(i);
                    
                    //wait if still drawing
                	decode_pdf.waitForDecodingToFinish();
                	
                    if(cachedThumbnail==null){
                        //page = javax.media.JAI.create("fileload", imageFiles[i]).getAsBufferedImage();
                        page = ImageIO.read(new File(imageFiles[i]));

                        thumbnailsStored.addElement(page);
                    }else{
                        page=(BufferedImage)cachedThumbnail;
                    }

                    if(page!=null){

                        int w=page.getWidth();
                        int h=page.getHeight();

                        /**add a border*/
                        Graphics2D g2=(Graphics2D) page.getGraphics();
                        g2.setColor(Color.black);
                        g2.draw(new Rectangle(0,0,w-1,h-1));

                        /**scale and refresh button*/
                        ImageIcon pageIcon;
                        if(h>w)
                            pageIcon=new ImageIcon(page.getScaledInstance(-1,100,BufferedImage.SCALE_FAST));
                        else
                            pageIcon=new ImageIcon(page.getScaledInstance(100,-1,BufferedImage.SCALE_FAST));

                        pageButton[i].setIcon(pageIcon);
                        pageButton[i].setVisible(true);
                        buttonDrawn[i] = true;

                        panel.add(pageButton[i]);


                        if(debugThumbnails)
                        System.out.println("Added button");

                    }
                }
            } catch (Exception e) {
                LogWriter.writeLog("Exception " + e + " loading " + imageFiles[i]);
            }
        }

        drawing=false;
        if(debugThumbnails)
                System.out.println("generateOtherThumbnails<<<<<<<<<<<<");


        panel.setVisible(true);


    }
	
	/**
	 * setup thumbnails if needed
	 */
	public void generateOtherVisibleThumbnails(final int currentPage){

		//stop multiple calls
		if(currentPage==-1 || currentPage==lastPage || pageButton==null)
		return;

		lastPage=currentPage;

		if(debugThumbnails)
                System.out.println("generateOtherVisibleThumbnails------->"+currentPage);

        int count = decode_pdf.getPageCount();
		
		for (int i1 = 0; i1 < count; i1++) {
			if ((i1 != currentPage - 1))
				pageButton[i1].setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		}
		
		//set button and scroll to
		if ((count > 1) && (currentPage > 0))
			pageButton[currentPage - 1].setBorder(BorderFactory.createLineBorder(Color.red));
		
		//update thumbnail pane if needed
		Rectangle rect = panel.getVisibleRect();

        if (!rect.contains(pageButton[currentPage - 1].getLocation())) {
			
			try {
				if (SwingUtilities.isEventDispatchThread()){
					Rectangle vis=new Rectangle(pageButton[currentPage - 1].getLocation().x,
							pageButton[currentPage - 1].getLocation().y,
							pageButton[currentPage-1].getBounds().width,
							pageButton[currentPage-1].getBounds().height);
					panel.scrollRectToVisible(vis);
				}else{
					SwingUtilities.invokeAndWait(new Runnable() {

						public void run() {
							Rectangle vis=new Rectangle(pageButton[currentPage - 1].getLocation().x,
									pageButton[currentPage - 1].getLocation().y,
									pageButton[currentPage-1].getBounds().width,
									pageButton[currentPage-1].getBounds().height);
							panel.scrollRectToVisible(vis);
						}
					});
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		
		//commonValues.setProcessing(false);
		
		/** draw thumbnails in background, having checked not already drawing */
        if(drawing)
        terminateDrawing();

        /** draw thumbnails in background */
        drawThumbnails();
    }
	
	/**
	 * redraw thumbnails if scrolled
	 */
	public void drawThumbnails(){

        if(debugThumbnails)
        System.out.println("start drawThumbnails------->");

        //allow for re-entry
        if(drawing)
        	this.terminateDrawing();
        
        //create the thread to just do the thumbnails
		worker = new SwingWorker() {

			public Object construct() {
				
				drawing=true;
				
				try {
					Rectangle rect = panel.getVisibleRect();
					int pages = decode_pdf.getPageCount();
					
					for (int i = 0; i < pages; i++) {

						//wait if still drawing
                    	decode_pdf.waitForDecodingToFinish();
                    	
                        if (interrupt)
							i=pages;
                        else if ((buttonDrawn!=null)&&(rect!=null)&&(!buttonDrawn[i])&& (pageButton[i] != null)
								&& (rect.intersects(pageButton[i].getBounds()))) {

                        	decode_pdf.setThumbnailsDrawing(true);
                        	
                            int h = thumbH;
							if (isLandscape[i])
								h = thumbW;

							BufferedImage page = thumbDecoder.getPageAsThumbnail(i + 1, h);
                            if (!interrupt)
                            createThumbnail(page, i + 1, false);

                            decode_pdf.setThumbnailsDrawing(false);
                        }
					}
					
				} catch (Exception e) {
					//stopped thumbnails
					e.printStackTrace();
				}
				
				//always make sure turned off
				decode_pdf.setThumbnailsDrawing(false);
				
				//always reset flag so we can interupt
				interrupt=false;
				
				drawing=false;

                if(debugThumbnails)
                System.out.println("end drawThumbnails-------<");

                return null;
			}
		};
		
		worker.start();
	}
	
	/**add any new thumbnails needed to display*/
	public void addDisplayedPageAsThumbnail(int currentPage,DynamicVectorRenderer currentDisplay){

        if(debugThumbnails)
                System.out.println("addDisplayedPageAsThumbnail "+currentPage);

		Rectangle rect = panel.getVisibleRect();

		//if not drawn get page and flag
		if (buttonDrawn!=null && !buttonDrawn[currentPage - 1] && rect.intersects(pageButton[currentPage - 1].getBounds())) {

			int h = thumbH;
			if (isLandscape[currentPage - 1])
				h = thumbW;

			BufferedImage page = decode_pdf.getPageAsThumbnail(h,currentDisplay);

			createThumbnail(page, currentPage, true);
		}
		
	}
	
	/**
	 * create a blank tile with a cross to use as a thumbnail for unloaded page
	 */
	private BufferedImage createBlankThumbnail(int w, int h) {
		BufferedImage blank=new BufferedImage(w+1,h+1,BufferedImage.TYPE_INT_RGB);
		Graphics2D g2=(Graphics2D) blank.getGraphics();
		g2.setColor(Color.white);
		g2.fill(new Rectangle(0,0,w,h));
		g2.setColor(Color.black);
		g2.draw(new Rectangle(0,0,w,h));
		g2.drawLine(0,0,w,h);
		g2.drawLine(0,h,w,0);
		return blank;
	}

    /**
     * return BufferedImage for page
     * @param page
     * @return
     */
    public BufferedImage getThumbnail(int page){

        if(pageButton==null || pageButton[page]==null)
        return null;
        else
            return (BufferedImage) ((ImageIcon)pageButton[page].getIcon()).getImage();
    }
	
	
	/**
	 *setup a thumbnail button in outlines
	 */
	private void createThumbnail(BufferedImage page, int i,boolean highLightThumbnail) {
		
		i--; //convert from page to array
		
		if(page!=null){
			/**add a border*/
			Graphics2D g2=(Graphics2D) page.getGraphics();
			g2.setColor(Color.black);
			g2.draw(new Rectangle(0,0,page.getWidth()-1,page.getHeight()-1));

            /**scale and refresh button*/
			ImageIcon pageIcon=new ImageIcon(page);
			pageButton[i].setIcon(pageIcon);
			
			buttonDrawn[i] = true;
			
		}
	}
	
	/**
	 * setup thumbnails at start - use for general images
	 */
	public void setupThumbnails(int pages,int[] pageUsed,int pageCount) {

        isExtractor=true;

        if(debugThumbnails)
                System.out.println("setupThumbnails2");


		lastPage=-1;

		Font textFont=new Font("Serif",Font.PLAIN,12);
		
		//remove any added last time
		//panel.removeAll();
		

		getVerticalScrollBar().setUnitIncrement(80);
		
		//create empty thumbnails and add to display
		BufferedImage blankPortrait = createBlankThumbnail(thumbW, thumbH);
		ImageIcon portrait=new ImageIcon(blankPortrait.getScaledInstance(-1,
				100,BufferedImage.SCALE_SMOOTH));
		
		isLandscape=new boolean[pages];
		pageHeight=new int[pages];
		pageButton=new JButton[pages];
		buttonDrawn=new boolean[pages];
		
		for(int i=0;i<pages;i++){
			
			int page=i+1;
			
			if(pageCount<2)
				pageButton[i]=new JButton(String.valueOf(page),portrait); //$NON-NLS-2$
			else
				pageButton[i]=new JButton(String.valueOf(page) +" ( Page "+pageUsed[i]+" )",portrait); //$NON-NLS-2$
			isLandscape[i]=false;
			pageHeight[i]=100;
			
			pageButton[i].setVerticalTextPosition(AbstractButton.BOTTOM);
			pageButton[i].setHorizontalTextPosition(AbstractButton.CENTER);
			if((i==0)&&(pages>1))
				pageButton[0].setBorder(BorderFactory.createLineBorder(Color.red));
			else
				pageButton[i].setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
			
			pageButton[i].setFont(textFont);
			//panel.add(pageButton[i],BorderLayout.CENTER);

        }

    }
	
	/**reset the highlights*/
	public void resetHighlightedThumbnail(int item){

        if(debugThumbnails)
                System.out.println("resetHighlightedThumbnail");

        if(pageButton!=null){
			int pages=pageButton.length;
			
			for(int i=0;i<pages;i++){
				
				if((i==item))
					pageButton[i].setBorder(BorderFactory.createLineBorder(Color.red));
				else
					pageButton[i].setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
			}
		}
	}
	
	/**
	 * setup thumbnails at start - use when adding pages
	 */
	public void setupThumbnails(int pages,Font textFont,String message,PdfPageData pageData) {


        if(debugThumbnails)
                System.out.println("setupThumbnails");

		lastPage=-1;

		getViewport().removeAll();
        panel.removeAll();
        //create dispaly for thumbnails
		getViewport().add(panel);
		panel.setLayout(new GridLayout(pages,1,0,10));
		panel.scrollRectToVisible(new Rectangle(0,0,1,1));

		getVerticalScrollBar().setUnitIncrement(80);

		//create empty thumbnails and add to display
		
		//empty thumbnails for unloaded pages
		BufferedImage blankPortrait = createBlankThumbnail(thumbW, thumbH);
		BufferedImage blankLandscape = createBlankThumbnail(thumbH,thumbW);
		ImageIcon landscape=new ImageIcon(blankLandscape.getScaledInstance(-1,
				70,BufferedImage.SCALE_SMOOTH));
		ImageIcon portrait=new ImageIcon(blankPortrait.getScaledInstance(-1,
				100,BufferedImage.SCALE_SMOOTH));
		
		isLandscape=new boolean[pages];
		pageHeight=new int[pages];
		pageButton=new JButton[pages];
		buttonDrawn=new boolean[pages];
		
		for(int i=0;i<pages;i++){
			
			int page=i+1;
			
			//create blank image with correct orientation
			final int pw,ph;
			int cropWidth=pageData.getCropBoxWidth(page);
			int cropHeight=pageData.getCropBoxHeight(page);
			int rotation=pageData.getRotation(page);
			ImageIcon usedLandscape,usedPortrait;
			
			if((rotation==0)|(rotation==180)){
				ph=(pageData.getMediaBoxHeight(page));
				pw=(pageData.getMediaBoxWidth(page));//%%
				usedLandscape = landscape;
				usedPortrait = portrait;
			}else{
				ph=(pageData.getMediaBoxWidth(page));
				pw=(pageData.getMediaBoxHeight(page));//%%
				usedLandscape = portrait;
				usedPortrait = landscape;
			}
			
			if(cropWidth>cropHeight){
				pageButton[i]=new JButton(message+ ' ' +page,usedLandscape); //$NON-NLS-2$
				isLandscape[i]=true;
				pageHeight[i]=ph;//w;%%
			}else{
				pageButton[i]=new JButton(message+ ' ' +page,usedPortrait); //$NON-NLS-2$
				isLandscape[i]=false;
				pageHeight[i]=ph;
			}
			
			pageButton[i].setVerticalTextPosition(AbstractButton.BOTTOM);
			pageButton[i].setHorizontalTextPosition(AbstractButton.CENTER);
			if((i==0)&&(pages>1))
				pageButton[0].setBorder(BorderFactory.createLineBorder(Color.red));
			else
				pageButton[i].setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
			
			pageButton[i].setFont(textFont);
			panel.add(pageButton[i],BorderLayout.CENTER);
	
        }

    }
	
	/**
	 *return a button holding the image,so we can add listener
	 */
	public Object[] getButtons() {
		return pageButton;
	}
	
	public void setThumbnailsEnabled(boolean newValue) {
		showThumbnailsdefault=newValue;
		showThumbnails=newValue;
		
	}
	
	public boolean isShownOnscreen() {

		return showThumbnails;

	}
	
	public void resetToDefault() {
		showThumbnails=showThumbnailsdefault;
		
		
	}
	
	public void setIsDisplayedOnscreen(boolean b) {
		showThumbnails=b;
		
	}
	

	public void addComponentListener() {
		panel.addComponentListener(painter);
		
	}
	
	public void removeAllListeners() {
		panel.removeComponentListener(painter);

        //remove all listeners
        Object[] buttons=getButtons();
        if(buttons!=null){
            for(int i=0;i<buttons.length;i++){
                ActionListener[] l= ((JButton)buttons[i]).getActionListeners();
                for(int j=0;j<l.length;j++)
                ((JButton)buttons[i]).removeActionListener(l[j]);
            }
        }
    }

    /**stop any drawing*/
	public void terminateDrawing() {

        //tell our code to exit cleanly asap
		if(drawing){

            interrupt=true;
			while(drawing){
				
				try {
					Thread.sleep(20);
                } catch (InterruptedException e) {
					// should never be called
					e.printStackTrace();
				}
                
			}
			
			interrupt=false; //ensure synched
		}

    }
	
	public void refreshDisplay() {
		validate();
	}

	public void dispose() {
	
		this.removeAll();
		
		worker=null;

		if(panel!=null)
			panel.removeAll();
	    panel=null;

	    painter=null;
		
	    thumbDecoder=null;

	    pageButton=null;

		buttonDrawn=null;

		isLandscape=null;

		pageHeight=null;
		
	}
	
}
