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
* TxtViewer.java
* ---------------
*/
package org.jfr.examples;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.util.*;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import org.jpedal.objects.PdfPageData;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.utils.LogWriter;
import org.jpedal.io.ObjectStore;
import org.jpedal.color.PdfColor;
import org.jpedal.gui.ShowGUIMessage;
import org.jfr.parser.GenericStreamDecoder;

/**clientside display*/
public class TxtViewer extends JPanel {

	/**the actual display object*/
	ObjectStore localStore=new ObjectStore();

	DynamicVectorRenderer dvr=new DynamicVectorRenderer(1,true,1000,localStore); //

	GenericStreamDecoder painter=new GenericStreamDecoder(dvr);

	/** Stores data about the PDF file */
	private PdfPageData pageData=new PdfPageData();

	int pageWidth =600, pageHeight =800,rotation=0,insetW=5,insetH=5,rightMargin=600;

	float scaling=1.0f;

	//areas to highligh onscreen
	private Rectangle[] areas;

	private JFrame frame=new JFrame();
	private String loadedText = "";

	public TxtViewer() {

	}

	/** main method to run the software as standalone application */
	public static void main(String[] args) {
		/** 
		 * set the look and feel for the GUI components to be the
		 * default for the system it is running on
		 */
		try {
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		}catch (Exception e) { 
			LogWriter.writeLog("Exception " + e + " setting look and feel");
		}

		TxtViewer current = new TxtViewer();

		current.setupViewer();

	}

	private void setupViewer() {

		//position and sizw
		frame.addWindowListener(new WindowListener(){
			public void windowActivated(WindowEvent e) {}
			public void windowClosed(WindowEvent e) {}
			public void windowClosing(WindowEvent e) {
				System.exit(1);
			}
			public void windowDeactivated(WindowEvent e) {}
			public void windowDeiconified(WindowEvent e) {}
			public void windowIconified(WindowEvent e) {}
			public void windowOpened(WindowEvent e) {}
		});

		//build frame
		JScrollPane scrollPane=new JScrollPane();
		scrollPane.getViewport().add(this);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.getVerticalScrollBar().setUnitIncrement(80);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(80);

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(scrollPane,BorderLayout.CENTER);

		frame.setTitle("Text File Viewer "+GenericStreamDecoder.version);

		int minimumScreenWidth=300;

		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		pageWidth = d.width / 2;
		pageHeight = d.height / 2;
		if(pageWidth <minimumScreenWidth)
			pageWidth =minimumScreenWidth;

		frame.setSize(pageWidth, pageHeight);

		//centre on screen
		frame.setLocationRelativeTo(null);

		frame.setVisible(true);

		/**
		 * setup page
		 */
		pageData.setPageRotation(0,1); // this is the default
		pageData.setMediaBox(new float[]{0,0,pageWidth,pageHeight}); //page size in pixels
		pageData.checkSizeSet(1); //setup page

		rightMargin= pageWidth;
		dvr.setBackgroundColor(Color.white);
		dvr.init(pageWidth, pageHeight, rotation);

		try {
			example();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	private void example() throws Exception {
		boolean myCode = true;
		float x=0,y=0,fontSize=8;

		//list of fonts
		String[] fonts=painter.getFontList();

		/**setup page*/
		painter.setRightMargin(rightMargin);
		painter.setLeftMargin(0);

		//set location to x,y and scaling (usually 1)
		painter.setTextLocationMatrix(1,0,0,1,x,y);

		int count=fonts.length;
		if(count>17)
			count=17;

		painter.setLeftMargin(10);

		painter.setForeground(new PdfColor(0,255,0));


		if(fonts.length>1){ //trap for no fonts
			//current.setForeground(Color.BLACK);
			painter.lineDown(2); //need to move onto next line
			/**/
			painter.setForeground(new PdfColor(0,0,0));
			painter.setFont(fonts[1],16);
			loadTXTFile();
			renderLeftAlignedText(loadedText);

		}else
			ShowGUIMessage.showGUIMessage("No fonts setup","No fonts installed");

		/**
 		current.DO(imageStream);
		 /***/

		this.updateUI();
	}

	private void renderLeftAlignedText(String textString) {

		String currentWord;
		double x,wordLength,rightMargin=painter.getRightMargin(),leftMargin=painter.getLeftMargin();
		StringTokenizer words=new StringTokenizer(textString," ");

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
				//painter.setCurrentXpt(leftMargin);

			}

			/**
			 *draw text (also returns updated cursor)
			 */
			char D = 0x0D;
			char A = 0x0A;
			char tab = 0x09;
			
			String newLine = String.valueOf(D) +A;
			
			int idx = 0;
			int tabIdx = 0;
			int nlIdx = 0;
			
			while(idx<currentWord.length()){
				nlIdx = currentWord.indexOf(newLine,idx);
				tabIdx = currentWord.indexOf(tab,idx);

				if(tabIdx==-1 && nlIdx==-1){
					String nextFirst = currentWord.substring(idx,currentWord.length());
					x=painter.setText(nextFirst);
					idx=currentWord.length();
				}
				
				if(tabIdx==-1)
					tabIdx = 99999;
				if(nlIdx==-1)
					nlIdx = 99999;
				
				if(tabIdx<nlIdx){
					System.out.println("TAB");
					String nextFirst = currentWord.substring(idx,tabIdx);
					x=painter.setText(nextFirst);
					painter.tab();
					idx = tabIdx+1;
				}
				if(tabIdx>nlIdx){
					System.out.println("NEW_LINE");
					String nextFirst = currentWord.substring(idx,nlIdx);
					x=painter.setText(nextFirst);
					painter.lineDown(1);
					idx=nlIdx+2;
				}
					
			}

			//x=painter.setText(currentWord);

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

	//set to true to see red box around
	static final private boolean debugViewport=false;

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

	private void loadTXTFile(){
		JFileChooser chooser = new JFileChooser();
		chooser.showOpenDialog(frame);
		File file = chooser.getSelectedFile();

		char newLine = 0x0D;
		char carridgeReturn = 0x0A;

		try{
			FileReader input = new FileReader(file.getAbsolutePath());
			BufferedReader BR = new BufferedReader(input);
			String temp = BR.readLine();

			while(temp!=null){
				loadedText = loadedText + temp+newLine+carridgeReturn;
				temp = BR.readLine();
			}
		}catch(Exception e){
            LogWriter.writeLog("[PDF] Error loading file");

        }


	}


}
