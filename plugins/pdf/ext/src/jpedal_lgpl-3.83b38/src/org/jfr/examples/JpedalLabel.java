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
 * Viewer.java
 * ---------------
 */
package org.jfr.examples;

import java.awt.*;
import java.awt.geom.AffineTransform;

import java.util.*;

import javax.swing.JPanel;

import org.jpedal.objects.PdfPageData;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.io.ObjectStore;
import org.jpedal.color.PdfColor;
import org.jfr.parser.GenericStreamDecoder;

/**clientside display*/
public class JpedalLabel extends JPanel {

	public static final int ALIGNMENT_LEFT = 0;
	public static final int ALIGNMENT_RIGHT = 1;
	public static final int ALIGNMENT_CENTER = 2;

	private int alignment = 0;

	private String text = "";;


	/**the actual display object*/
	ObjectStore localStore=new ObjectStore();

	DynamicVectorRenderer dvr=new DynamicVectorRenderer(1,true,1000,localStore); //

	GenericStreamDecoder painter=new GenericStreamDecoder(dvr);

	/** Stores data about the PDF file */
	private PdfPageData pageData=new PdfPageData();

	int pageWidth =600, pageHeight =800,rotation=0,insetW=5,insetH=5;

	int rightMargin=600;

	float scaling=1.0f;

	String[] fonts;

	//areas to highlight onscreen
	private Rectangle[] areas;
	
	private String font = "";

	public JpedalLabel() {
		setSystemFonts();
	}
	
	public JpedalLabel(String text) {
		setSystemFonts();
		this.text = text;
	}
	
	public JpedalLabel(String text, int alignment) {
		setSystemFonts();
		this.text = text;
		this.alignment = alignment;
	}
	
	public void paint(Graphics g) {
		
		clearLabel();
		switch(alignment){
		case 0 : renderLeftAlignedText(text); break;
		case 1 : renderRightAlignedText(text); break;
		case 2 : renderCenterAlignedText(text); break;
		default : renderLeftAlignedText(text); break;
		}
		super.paint(g);
	}
	
	public void clearLabel() {
		painter.setCurrentXpt(painter.getLeftMargin());
		dvr.flush();
	}
	
	public void renderRightAlignedText(String textString){
		System.out.println("To Be Implemented");
	}

	public void renderCenterAlignedText(String textString){
		System.out.println("To Be Implemented");
	}

	public void renderLeftAlignedText(String textString) {

		String currentWord;
		double x,wordLength,rightMargin=painter.getRightMargin(),leftMargin=painter.getLeftMargin();
		StringTokenizer words=new StringTokenizer(textString);

		while(words.hasMoreTokens()){

			//next word
			currentWord=words.nextToken();

			/**get current location*/
			x=painter.getCurrentXpt();
			wordLength=painter.getStringLength(currentWord);

			/**see if it fits on this line and move if overruns*/
			if(x+wordLength>rightMargin){

				//move down a line
				painter.lineDown(1);

				//reset back to left margin
				painter.setCurrentXpt(leftMargin);

			}

			/**
			 *draw text (also returns updated cursor)
			 */
			x=painter.setText(currentWord);

			/**
			 * draw the space which will trigger a new line if needed
			 */
			painter.setText(" ");

		}

	}

	/**
	 * get sizes of panel <BR>
	 * This is the PDF pagesize (as set in the PDF from pagesize) -
	 * It now includes any scaling factor you have set (ie a PDF size 800 * 600
	 * with a scaling factor of 2 will return 1600 *1200)
	 */
	final public Dimension getMaximumSize() {

		Dimension pageSize=null;

		int width=(int)(insetW+(pageWidth *scaling));
		int height=(int)(insetH+(pageHeight *scaling));

		if((rotation==90)|(rotation==270))
			pageSize= new Dimension(height,width);
		else
			pageSize= new Dimension(width,height);


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
	final public Dimension getPreferredSize() {
		return getMaximumSize();
	}

	/**
	 * workout Transformation to use on image
	 */
	private AffineTransform getScalingForImage(int pageNumber,int rotation,float scaling) {

		//get page sizes
		double mediaX = pageData.getMediaBoxX(pageNumber)*scaling;
		double mediaY = pageData.getMediaBoxY(pageNumber)*scaling;
		double mediaW = pageData.getMediaBoxWidth(pageNumber)*scaling;
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

		return displayScaling;
	}

	protected void paintComponent(Graphics g) {
		
		super.paintComponent(g);

		{
			Graphics2D g2 = (Graphics2D) g;

			//2 transformations for page
			AffineTransform viewScaling =null;
			AffineTransform displayScaling=getScalingForImage(0,0,this.scaling);

			//save so we can restore at end
			AffineTransform originalAff=g2.getTransform();

			//textHighlights[0]=rect;

			int x=0,y=0,w=0,h=0;


			/** 
			 * display the PDF 
			 **/
			g2.transform(displayScaling);

			dvr.paint(g2,null,null,null,false,true);

			//add viewports scaling if sets
			if(viewScaling!=null)
				g2.transform(viewScaling);

			//add highlights
			if(areas!=null){

				//note the transform for rect above now outside loop so applies to this as well
				//and disabled after this if() block

				Composite opacity=g2.getComposite();
				g2.setColor(Color.blue); //CHANGE COLOR HERE!!!!!!!
				g2.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 0.7f ) );

				//I've for allowed multiple values
				if(areas!=null){
					int count=areas.length;
					for(int i=0;i<count;i++){
						if(areas[i]!=null)
							g2.fillRect(areas[i].x,areas[i].y,areas[i].width,areas[i].height);
					}
				}
				g2.setComposite(opacity);
			}

			//restore original settings
			g2.setTransform(originalAff);

		}
	}
	
	public void setFont(String font, int fontSize){
		try {
			this.font = font;
			painter.setFont(font, fontSize);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String[] getFonts() {
		return fonts;
	}

	public void setFonts(String[] fonts) {
		this.fonts = fonts;
	}

	public void setSystemFonts() {
		float x=0,y=0;
		int fontSize=48;

		//list of fonts
		fonts=painter.getFontList();

		if(fonts==null || fonts.length<1)
			try {
				throw new Exception("No fonts configured");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			/**setup page*/
			painter.setRightMargin(rightMargin);
			painter.setLeftMargin(0);

			//set location to x,y and scaling (usually 1)
			painter.setTextLocationMatrix(1,0,0,1,x,y);

			painter.setForeground(new PdfColor(0,0,0));

			try {
				painter.setFont(fonts[0],fontSize);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//setupViewer();
	}

	public int getAlignment() {
		return alignment;
	}

	public void setAlignment(int alignment) {
		this.alignment = alignment;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
		this.repaint();
	}


}
