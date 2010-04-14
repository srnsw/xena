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
* GUI.java
* ---------------
*/

package org.jpedal.examples.simpleviewer.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.jpedal.Display;
import org.jpedal.PdfDecoder;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.DPIFactory;
import org.jpedal.utils.Messages;
import org.jpedal.examples.simpleviewer.Values;
import org.jpedal.examples.simpleviewer.gui.generic.GUIButton;
import org.jpedal.examples.simpleviewer.gui.generic.GUICombo;
import org.jpedal.examples.simpleviewer.gui.generic.GUIOutline;
import org.jpedal.examples.simpleviewer.gui.generic.GUIThumbnailPanel;
import org.jpedal.examples.simpleviewer.gui.swing.SwingOutline;
import org.jpedal.examples.simpleviewer.utils.PropertiesFile;
import org.jpedal.exception.PdfException;

/**any shared GUI code - generic and AWT*/
public class GUI {
	
	/**nav buttons - global so accessible to ContentExtractor*/
	public GUIButton first,fback,back,forward,fforward,end;
	
	public GUIButton singleButton,continuousButton,continuousFacingButton,facingButton,sideScrollButton;

    /**list for types - assumes present in org/jpedal/examples/simpleviewer/annots*
	 * "OTHER" MUST BE FIRST ITEM
	 * Try adding Link to the list to see links
	 */
	private String[] annotTypes={"Other","Text","FileAttachment"};

	private Color[] annotColors={Color.RED,Color.BLUE,Color.BLUE};
	
	protected boolean hiResPrinting = false;
	
	//@annot - table of objects we wish to track
	protected Map objs;
	
	public boolean useHiResPrinting() {
		return hiResPrinting;
	}

	public void setHiResPrinting(boolean hiResPrinting) {
		this.hiResPrinting = hiResPrinting;
	}
	
	public String getPropertiesFileLocation(){
		return properties.getConfigFile();
	}
	
	public void setPropertiesFileLocation(String file){
		properties.loadProperties(file);
	}
	
	public void setProperties(String item, boolean value){
		properties.setValue(item, String.valueOf(value));
	}
		
	public void setPreferences(int dpi, int search, int border, boolean scroll, int pageMode, boolean updateDefaultValue, int maxNoOfMultiViewers, boolean showDownloadWindow, boolean useHiResPrinting){
		
		//Set border config value and repaint
		PdfDecoder.CURRENT_BORDER_STYLE = border;
		properties.setValue("borderType", String.valueOf(border));
		
		//Set autoScroll default and add to properties file
		allowScrolling = scroll;
		properties.setValue("autoScroll", String.valueOf(scroll));
		
		//Dpi is taken into effect when zoom is called
		decode_pdf.getDPIFactory().setDpi(dpi);
		properties.setValue("DPI", String.valueOf(dpi));
		
		//@kieran Ensure valid value if not recognised
		if(pageMode<Display.SINGLE_PAGE || pageMode>Display.CONTINUOUS_FACING)
			pageMode = Display.SINGLE_PAGE;
		
		//Default Page Layout
		decode_pdf.setPageMode(pageMode);
		properties.setValue("pageMode", String.valueOf(pageMode));
		
		decode_pdf.repaint();
		
		//Set the search window
		String propValue = properties.getValue("searchWindowType");
		if(propValue.length()>0 && !propValue.equals(String.valueOf(search)))
			JOptionPane.showMessageDialog(null, Messages.getMessage("PageLayoutViewMenu.ResetSearch"));
		
		properties.setValue("searchWindowType", String.valueOf(search));

		properties.setValue("automaticupdate", String.valueOf(updateDefaultValue));
		
		commonValues.setMaxMiltiViewers(maxNoOfMultiViewers);
		properties.setValue("maxmultiviewers", String.valueOf(maxNoOfMultiViewers));
		
		useDownloadWindow = showDownloadWindow;
		properties.setValue("showDownloadWindow", String.valueOf(showDownloadWindow));
		
		hiResPrinting = useHiResPrinting;
		properties.setValue("useHiResPrinting", String.valueOf(showDownloadWindow));
		
	}
	
	
	
	/**handle for internal use*/
	protected PdfDecoder decode_pdf;
	
	/** location for divider with thumbnails turned on */
	protected static final int thumbLocation=200;
	
	/** minimum screen width to ensure menu buttons are visible */
	protected static final int minimumScreenWidth=700;
	
	/**track pages decoded once already*/
	protected HashMap pagesDecoded=new HashMap();

	/**allows user to toggle on/off text/image snapshot*/
	protected  GUIButton snapshotButton;


    /**cursorBox to draw onscreen*/
	private Rectangle currentRectangle =null;
	
	
	public int cropX;

	public int cropW;

	public int cropH;

	/**crop offset if present*/
	protected int mediaX,mediaY;

	public int mediaW;

	public int cropY;

	public int mediaH;
	
	/**Use Download Windom*/
	protected boolean useDownloadWindow = true;
	
	/**show if outlines drawn*/
	protected boolean hasOutlinesDrawn=false;
	
	/**XML structure of bookmarks*/
	protected GUIOutline tree=new SwingOutline();
	
	/**stops autoscrolling at screen edge*/
	protected boolean allowScrolling=true;
	
	/** location for the divider when bookmarks are displayed */
	protected int divLocation=170;
	
	/**flag to switch bookmarks on or off*/
	protected boolean showOutlines=true;
	
	/**scaling values as floats to save conversion*/
	protected float[] scalingFloatValues={1.0f,1.0f,1.0f,.25f,.5f,.75f,1.0f,1.25f,1.5f,2.0f,2.5f,5.0f,7.5f,10.0f};
	
	/**page scaling to use 1=100%*/
	protected float scaling = 1;
	
	/** padding so that the pdf is not right at the edge */
	protected static final int inset=25;
	
	/**store page rotation*/
	protected int rotation=0;
	
	/**scaling values as floats to save conversion*/
	protected String[] rotationValues={"0","90","180","270"};
	
	/**scaling factors on the page*/
	protected GUICombo rotationBox;

	/**allows user to set quality of images*/
	protected GUICombo qualityBox;
	
	/**scaling factors on the page*/
	protected GUICombo scalingBox;
	
	/**default scaling on the combobox scalingValues*/
	protected static int defaultSelection=0;

	/**title message on top if you want to over-ride JPedal default*/
	protected String titleMessage=null;
	
	protected Values commonValues;
	
	protected GUIThumbnailPanel thumbnails;
	
	protected PropertiesFile properties;
	
	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#allowScrolling()
	 */
	public boolean allowScrolling() {
		return allowScrolling;
	}
	
	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#getAnnotTypes()
	 */
	public String[] getAnnotTypes() {
		
		return this.annotTypes;
	}
	
	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#setNoPagesDecoded()
	 */
	public void setNoPagesDecoded() {
		pagesDecoded.clear();
		
	}
	
	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#setRectangle(java.awt.Rectangle)
	 */
	public void setRectangle(Rectangle newRect) {
		currentRectangle=newRect;
	}
	
	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#getRectangle()
	 */
	public Rectangle getRectangle() {
		return currentRectangle;
	}
	
	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#setAutoScrolling(boolean allowScrolling)
	 */
	public void setAutoScrolling(boolean allowScrolling) {
		this.allowScrolling=allowScrolling;
		
	}
	
	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#toogleAutoScrolling()
	 */
	public void  toogleAutoScrolling(){
		allowScrolling=!allowScrolling;
	}
	
	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#getRotation()
	 */
	public int getRotation() {
		return rotation;
	}
	
	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#getScaling()
	 */
	public float getScaling() {
		return scaling;
	}
	
	public void setScaling(float s){
		scaling = s;
		scalingBox.setSelectedIndex((int)scaling);
	}
	
	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#getPDFDisplayInset()
	 */
	public int getPDFDisplayInset() {
		return inset;
	}

	//<link><a name="createUniqueAnnotationIcons" />
	/** example code which sets up an individual icon for each annotation to display - only use
	 * if you require each annotation to have its own icon<p>
	 * To use this you ideally need to parse the annotations first -there is a method allowing you to
	 * extract just the annotations from the data.
	 */
	public void createUniqueAnnotationIcons() {

		int p=commonValues.getCurrentPage();
		final int size=16; //pixel size

		//@annot
		//PdfAnnots annotsData=decode_pdf.getPdfAnnotsData(null);

		//new list we can parse
		PdfArrayIterator annotListForPage = decode_pdf.getFormRenderer().getAnnotsOnPage(p);
		
		//and place to store so we can test later
		//flush list if needed
		if(objs==null)
			objs=new HashMap();
		else 
			objs.clear();
		
		
		//@annot
		//if(annotsData!=null){
		if(annotListForPage!=null && annotListForPage.getTokenCount()>0){ //can have empty lists

			//@annot - we don't need any of this!!!!!
			/**int max=annotsData.getAnnotCount();

			for(int j=0;j<annotTypes.length;j++){ //build a set of icons for each fileType you wish to supportby including in AnnotTypes

				//if(!annotsData.getAnnotSubType(j).equals("add type here")) //just 1 type please
				//next;

				//number icons needed
				int iconsForPage=0;

				//code to count number for selected type - just use max for all icons
				for (int i = 0; i < max; i++) {

					if(annotsData.getAnnotSubType(i).equals(annotTypes[j])) //count number of icons
						iconsForPage++;

				}

				//initialise to required size
				Image[] annotIcons = new Image[iconsForPage];

				//and create icons
				for (int i = 0; i < iconsForPage; i++) {

					//create a unique graphic
					annotIcons[i] = new BufferedImage(size, size,BufferedImage.TYPE_INT_ARGB);
					Graphics2D g2 = (Graphics2D) annotIcons[i].getGraphics();
					g2.setColor(annotColors[j]);
					g2.fill(new Rectangle(0, 0, size, size));
					g2.setColor(Color.BLACK);
					g2.draw(new Rectangle(0, 0, size-1, size-1));
					g2.setColor(Color.white);
					g2.drawString(((i+1) + " "),2, 12);

				}

				//add set of icons to display
				if(iconsForPage>0)
				decode_pdf.addUserIconsForAnnotations(p,annotTypes[j],annotIcons);
			}/**/

			/**
			 * new code to set Annot to have custom image
			 * 
			 *  scan and overwrite images of certain types
			 */
			int i=0;
			int count=annotListForPage.getTokenCount();

			while(annotListForPage.hasMoreTokens()){

				// Due to the way some pdf's are created it is necessery to take the offset of a page into account when addding custom objects
				// Variables mX and mY represent that offset and need to be taken in to account when placing any additional object on a page.

				int mX = decode_pdf.getPdfPageData().getMediaBoxX(p);
				int mY = decode_pdf.getPdfPageData().getMediaBoxY(p);
				int[] type=new int[count];
				Color[] colors=new Color[count];
				Object[] obj=new Object[count];

				//get ID of annot which has already been decoded and get actual object
				String annotKey=annotListForPage.getNextValueAsString(true);
                
				Object rawObj=decode_pdf.getFormRenderer().getCompData().getRawForm(annotKey);
				if(rawObj==null){
					//no match found
					System.out.println("no match on "+annotKey);
				}else{

					FormObject annotObj=(FormObject)rawObj;



                    //get the FS value
                    PdfObject FS=annotObj.getDictionary(PdfDictionary.FS);

                    //and the text
//                    if(FS!=null){
//                        System.out.println("----"+annotObj.getObjectRefAsString());
//                        System.out.println("Contents="+annotObj.getTextStreamValue(PdfDictionary.Contents));
//                        System.out.println("F="+FS.getTextStreamValue(PdfDictionary.F));
//                        System.out.println("D="+FS.getTextStreamValue(PdfDictionary.D));
//                    }
                    
					int subtype=annotObj.getParameterConstant(PdfDictionary.Subtype);

//                    if(subtype==PdfDictionary.Link){
//                        System.out.println("----"+annotObj.getObjectRefAsString());
//                        PdfObject Aobj=annotObj.getDictionary(PdfDictionary.A);
//                       System.out.println("A="+Aobj+" "+subtype);
//                        PdfObject winObj=Aobj.getDictionary(PdfDictionary.Win);
//                        System.out.println("Win="+winObj+" "+winObj.getTextStreamValue(PdfDictionary.D)+" "+winObj.getTextStreamValue(PdfDictionary.P));
//
//                    }
					//subtypes set in PdfDictionary - please use Constant as values may change
					//if not present ask me and I will add
					if(subtype==PdfDictionary.FileAttachment){ //might also be PdfDictionary.Link
						
						//@annot -save so we can lookup (kept as HashMap siply to reduce changes and in case
						//we want something else as value. Could be Set or Vector
						objs.put(annotObj,"x");
						
						Color col=Color.BLUE;

						//get origin of Form Object
						Rectangle location=annotObj.getBoundingRectangle();
						
						//SOME EXAMPLES to show other possible uses
						
						//example stroked shape
						//	                type[i]= org.jpedal.render.DynamicVectorRenderer.STROKEDSHAPE;
						//	                colors[i]=Color.RED;
						//	                obj[i]=new Rectangle(location.x,location.y,20,20); //ALSO sets location. Any shape can be used

						//example simple filled shape
						//	                type[i]= org.jpedal.render.DynamicVectorRenderer.FILLEDSHAPE;
						//	                colors[i]=Color.GREEN;
						//	                obj[i]=new Rectangle(location.x,location.y,20,20); //ALSO sets location. Any shape can be used

						//example text object
//						type[i]= org.jpedal.render.DynamicVectorRenderer.STRING;
//						org.jpedal.render.TextObject textObject=new org.jpedal.render.TextObject(); //composite object so we can pass in parameters
//						textObject.x=location.x+mX;
//						textObject.y=location.y+mY;
//						textObject.text=""+(i+1);
//						textObject.font=new Font("Serif",Font.PLAIN,12);
//						colors[i]=col;
//						obj[i]=textObject; //ALSO sets location
						
						//example image object
						/**/type[i]= org.jpedal.render.DynamicVectorRenderer.IMAGE;
						org.jpedal.render.ImageObject imgObject=new org.jpedal.render.ImageObject(); //composite object so we can pass in parameters
						imgObject.x=location.x+mX;
						imgObject.y=location.y+mY;
						imgObject.image=createUniqueImage(12, ""+(i+1), col);
						obj[i]=imgObject; //ALSO sets location

						//example custom - TOTAL FLEXIBILTY as you implement you own custom object
//						type[i]=org.jpedal.render.DynamicVectorRenderer.CUSTOM;
//
//		                JPedalCustomDrawObject exampleObj=new ExampleCustomDrawObject();
//		                exampleObj.setMedX(location.x+mX);
//		                exampleObj.setMedY(location.x+mY);	
//		                obj[i]=exampleObj;
						
						i++;
					}
				}
				
				//pass into JPEDAL after page decoded - will be removed automatically on new page/open file
				//BUT PRINTING retains values until manually removed

				/**/

				//this code will remove ALL items already drawn on page
				//try{
				//    decode_pdf.flushAdditionalObjectsOnPage(commonValues.getCurrentPage());
				//}catch(PdfException e){
				//    e.printStackTrace();
				//    //ShowGUIMessage.showGUIMessage( "", new JLabel(e.getMessage()),"Exception adding object to display");
				//}
				
			}
		}
	}
	
	private BufferedImage createUniqueImage(int size, String text, Color col){
		//create a unique graphic
		BufferedImage img = new BufferedImage(size, size,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D) img.getGraphics();
		g2.setColor(col);
		g2.fill(new Rectangle(0, 0, size, size));
		g2.setColor(Color.BLACK);
		g2.draw(new Rectangle(0, 0, size-1, size-1));
		g2.setColor(Color.white);
		g2.drawString(text,2, 12);
		
		return img;
	}

	public void setDpi(int dpi) {
		decode_pdf.getDPIFactory().setDpi(dpi);
	}
	
	public boolean isUseDownloadWindow() {
		return useDownloadWindow;
	}

	public void setUseDownloadWindow(boolean useDownloadWindow) {
		this.useDownloadWindow = useDownloadWindow;
	}

	public PropertiesFile getProperties() {
		return properties;
	}
	
	public void dispose(){
		
		first=null;
		fback=null;
		back=null;
		forward=null;
		fforward=null;
		end=null;
		
		singleButton=null;
		continuousButton=null;
		continuousFacingButton=null;
		facingButton=null;
		sideScrollButton=null;

	    annotTypes=null;

		annotColors=null;
		
		pagesDecoded=null;

		snapshotButton=null;


	    currentRectangle =null;
		
		tree=null;
		//protected PdfDecoder decode_pdf;
		
		pagesDecoded=null;

		snapshotButton=null;
		
		scalingFloatValues=null;
		
		rotationValues=null;
		
		rotationBox=null;

		qualityBox=null;
		
		scalingBox=null;
		
		titleMessage=null;
		
		//protected Values commonValues;
		
		//protected GUIThumbnailPanel thumbnails;
		
	}
}
