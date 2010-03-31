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
* SwingMouseHandler.java
* ---------------
*/
package org.jpedal.examples.simpleviewer.gui.swing;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import org.jpedal.PdfDecoder;
import org.jpedal.Display;
import org.jpedal.PdfHighlights;
import org.jpedal.grouping.SearchType;
import org.jpedal.io.ObjectStore;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.io.PdfReader;
import org.jpedal.examples.simpleviewer.Commands;
import org.jpedal.examples.simpleviewer.Values;
import org.jpedal.examples.simpleviewer.gui.SwingGUI;
import org.jpedal.examples.simpleviewer.gui.generic.GUIMouseHandler;
import org.jpedal.exception.PdfException;
import org.jpedal.external.Options;

import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.utils.Messages;


/**handles all mouse activity in GUI using Swing classes*/
public class SwingMouseHandler implements GUIMouseHandler{

	private PdfDecoder decode_pdf;
	private SwingGUI currentGUI;
	private Values commonValues;
	
	private Commands currentCommands;
	
	private AutoScrollThread scrollThread = new AutoScrollThread();

	//Is the mouse currently being dragged
	private boolean dragged = false;

	/**tells user if we enter a link*/
	private String message="";

	/** cursor rectangle handles */
	private Rectangle[] boxes = new Rectangle[8];

	/** the extra gap for the cursorBox handlers highlighting */
	private int handlesGap = 5;

	/** old x and y values for where drag original location was */
	private int oldX=-1,oldY=-1;

	/** flag to tell whether drag altering currentRectangle */
	private boolean dragAltering=false;

	/** which handle box is being altered */
	private int boxContained = -1;

	/** to allow new cursor box to be drawn */
	private boolean drawingCursorBox=false;

	/**used to track changes when dragging rectangle around*/
	private int old_m_x2=-1,old_m_y2=-1;

	/**current cursor position*/
	private int cx,cy;
	
	/**Flag used to avoid issue if right mouse button clicked more than once.*/
	private boolean rightClicked = false;
    /**
	 * picks up clicks so we can draw an outline on screen
	 */
	protected class mouse_clicker extends MouseAdapter {
		
		private JPopupMenu rightClick = new JPopupMenu();
		private boolean menuCreated = false;
		private int clickCount = 0;
		private long lastTime = -1;
		
		//user has pressed mouse button so we want to use this 
		//as one point of outline
		public void mousePressed(MouseEvent event) {
			if(event.getButton()==MouseEvent.BUTTON1){
				/** remove any outline and reset variables used to track change */
				
					currentGUI.setRectangle(null);
					decode_pdf.updateCursorBoxOnScreen(null, null); //remove box
					decode_pdf.removeFoundTextAreas(null); //remove highlighted text
					decode_pdf.setHighlightedImage(null);// remove image highlight

				//Remove focus from form is if anywhere on pdf panel is clicked / mouse dragged
				decode_pdf.grabFocus();

				float scaling=currentGUI.getScaling();
				int inset=currentGUI.getPDFDisplayInset();
				int rotation=currentGUI.getRotation();

				//get co-ordinates of top point of outine rectangle
				int x=(int)(((currentGUI.AdjustForAlignment(event.getX()))-inset)/scaling);
				int y=(int)((event.getY()-inset)/scaling);

				//undo any viewport scaling (no crop assumed
				if(commonValues.maxViewY!=0){ // will not be zero if viewport in play
					x=(int)(((x-(commonValues.dx*scaling))/commonValues.viewportScale));
					y=(int)((currentGUI.mediaH-((currentGUI.mediaH-(y/scaling)-commonValues.dy)/commonValues.viewportScale))*scaling);
				}
				
				if (rotation == 90) {
					commonValues.m_y1 = x+currentGUI.cropY;
					commonValues.m_x1 = y+currentGUI.cropX;
				} else if ((rotation == 180)) {
					commonValues.m_x1 = currentGUI.mediaW - (x+currentGUI.mediaW-currentGUI.cropW-currentGUI.cropX);
					commonValues.m_y1 = y+currentGUI.cropY;
				} else if ((rotation == 270)) {
					commonValues.m_y1 = currentGUI.mediaH - (x+currentGUI.mediaH-currentGUI.cropH-currentGUI.cropY);
					commonValues.m_x1 = currentGUI.mediaW - (y+currentGUI.mediaW-currentGUI.cropW-currentGUI.cropX);
				} else {
					commonValues.m_x1 = x+currentGUI.cropX;
					commonValues.m_y1 = currentGUI.mediaH - (y+currentGUI.mediaH-currentGUI.cropH-currentGUI.cropY);
				}
				
				updateCords(event.getX(), event.getY(), event.isShiftDown());
				
				if(DynamicVectorRenderer.textBasedHighlight)
					decode_pdf.setHighlightStartPoint(commonValues.m_x1, commonValues.m_y1);

			}else if(event.getButton()==MouseEvent.BUTTON3){
				rightClicked = true;
			}
		}
		public Rectangle area = null;
		public int id = -1;
		public int lastId =-1;
		
		//Right click options
		JMenuItem copy = new JMenuItem("Copy");
		
		//======================================
		
		JMenuItem selectAll = new JMenuItem("Select All");
		JMenuItem deselectall = new JMenuItem("Deselect All");
		
		//======================================
		
		JMenu extract = new JMenu("Extraction");
		JMenuItem extractText = new JMenuItem("Extract Text...");
		JMenuItem extractImage = new JMenuItem("Extract Image...");
	    ImageIcon snapshotIcon = new ImageIcon(getClass().getResource("/org/jpedal/examples/simpleviewer/res/snapshot_menu.gif"));
		JMenuItem snapShot = new JMenuItem("Snapshot...", snapshotIcon);
		
		//======================================
		
		JMenuItem find = new JMenuItem("Find Text Coords...");
		
		//show the description in the text box or update screen
		public void mouseClicked(MouseEvent event) {
			long currentTime = new Date().getTime();
			
			if(lastTime+500 < currentTime)
				clickCount=0;
			
			lastTime = currentTime;
			
			if(event.getButton()==MouseEvent.BUTTON1){
				
				if(clickCount!=4)
					clickCount++;
					
				//highlight image on page if over
				id = decode_pdf.getDynamicRenderer().isInsideImage(cx,cy);

				if(lastId!=id && id!=-1){
					area = decode_pdf.getDynamicRenderer().getArea(id);


					if(area!=null){
						int h= area.height;
						int w= area.width;

						int x= area.x;
						int y= area.y;
						decode_pdf.getDynamicRenderer().needsHorizontalInvert = false;
						decode_pdf.getDynamicRenderer().needsVerticalInvert = false;
//						Check for negative values
						if(w<0){
							decode_pdf.getDynamicRenderer().needsHorizontalInvert = true;
							w =-w;
							x =x-w;
						}
						if(h<0){
							decode_pdf.getDynamicRenderer().needsVerticalInvert = true;
							h =-h;
							y =y-h;
						}

						if(decode_pdf.isImageExtractionAllowed()){
							decode_pdf.setHighlightedImage(new int[]{x,y,w,h});
						}

					}
					lastId = id;
				}else{
					if(decode_pdf.isImageExtractionAllowed()){
						decode_pdf.setHighlightedImage(null);
					}
					lastId = -1;
				}

				if(currentGUI.addUniqueIconToFileAttachment)
					checkLinks(true,decode_pdf.getIO());
				
				if(id==-1 && DynamicVectorRenderer.textBasedHighlight){
					if(clickCount>1){
						switch(clickCount){
						case 1 : //single click adds caret to page
							/**
							 * Does nothing yet. IF above prevents this case from ever happening
							 * Add Caret code here and add shift click code for selection.
							 * Also remember to comment out "if(clickCount>1)" from around this switch to activate
							 */
							break;
						case 2 : //double click selects line
							Rectangle[] lines = PdfHighlights.getLineAreas();
							Rectangle point = new Rectangle(cx,cy,1,1);
                            
                            if(lines!=null){//Null is page has no lines
                                for(int i=0; i!=lines.length; i++){
                                    if(lines[i].intersects(point)){
                                        currentGUI.setRectangle(lines[i]);
                                        decode_pdf.updateCursorBoxOnScreen(lines[i],PdfDecoder.highlightColor);
                                        decode_pdf.setMouseHighlightArea(lines[i]);
                                    }
                                }
                            }
							break;
						case 3 : //triple click selects paragraph
							Rectangle para = decode_pdf.setFoundParagraph(cx,cy);
							if(para!=null){
								currentGUI.setRectangle(para);
						        decode_pdf.updateCursorBoxOnScreen(para,PdfDecoder.highlightColor);
						        decode_pdf.setMouseHighlightArea(para);
							}
							break;
						case 4 : //quad click selects page
							currentCommands.executeCommand(Commands.SELECTALL, null);
							break;
						}
					}
				}
			}else if(event.getButton()==MouseEvent.BUTTON2){
				
			}else if(event.getButton()==MouseEvent.BUTTON3){
				
			}
		}
		
		private void createRightClickMenu(){
			
			rightClick.add(copy);
			copy.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					currentCommands.executeCommand(Commands.COPY, null);
				}
			});
			
			rightClick.addSeparator();
			
			
			rightClick.add(selectAll);
			selectAll.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					currentCommands.executeCommand(Commands.SELECTALL, null);
				}
			});
			
			rightClick.add(deselectall);
			deselectall.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					currentCommands.executeCommand(Commands.DESELECTALL, null);
				}
			});

			rightClick.addSeparator();
			
			rightClick.add(extract);
			
			extract.add(extractText);
			extractText.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					if(decode_pdf.getDisplayView()==1)
						currentCommands.extractSelectedText();
					else
						JOptionPane.showMessageDialog(currentGUI.getFrame(),"Text Extraction is only avalible in single page display mode");

				}
			});
			
			extract.add(extractImage);
			extractImage.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					if(decode_pdf.getHighlightImage()==null){
						JOptionPane.showMessageDialog(decode_pdf, "No image has been selected for extraction.", "No image selected", JOptionPane.ERROR_MESSAGE);
					}else{
						if(decode_pdf.getDisplayView()==1){
							JFileChooser jf = new JFileChooser();
							FileFilter ff1 = new FileFilter(){
								public boolean accept(File f){
									return f.isDirectory() || f.getName().toLowerCase().endsWith(".jpg") || f.getName().toLowerCase().endsWith(".jpeg");
								}
								public String getDescription(){
									return "JPG (*.jpg)" ;
								}
							};
							FileFilter ff2 = new FileFilter(){
								public boolean accept(File f){
									return f.isDirectory() || f.getName().toLowerCase().endsWith(".png");
								}
								public String getDescription(){
									return "PNG (*.png)" ;
								}
							};
							FileFilter ff3 = new FileFilter(){
								public boolean accept(File f){
									return f.isDirectory() || f.getName().toLowerCase().endsWith(".tif") || f.getName().toLowerCase().endsWith(".tiff");
								}
								public String getDescription(){
									return "TIF (*.tiff)" ;
								}
							};
							jf.addChoosableFileFilter(ff3);
							jf.addChoosableFileFilter(ff2);
							jf.addChoosableFileFilter(ff1);
							jf.showSaveDialog(null);

							File f = jf.getSelectedFile();
							boolean failed = false;
							if(f!=null){
								String filename = f.getAbsolutePath();
								String type = jf.getFileFilter().getDescription().substring(0,3).toLowerCase();

								//Check to see if user has entered extension if so ignore filter
								if(filename.indexOf('.')!=-1){
									String testExt = filename.substring(filename.indexOf('.')+1).toLowerCase();
									if(testExt.equals("jpg") || testExt.equals("jpeg"))
										type = "jpg";
									else
										if(testExt.equals("png"))
											type = "png";
										else //*.tiff files using JAI require *.TIFF
											if(testExt.equals("tif") || testExt.equals("tiff"))
												type = "tiff";
											else{
												//Unsupported file format
												JOptionPane.showMessageDialog(null, "Sorry, we can not currently save images to ."+testExt+" files.");
												failed = true;
											}
								}

								//JAI requires *.tiff instead of *.tif
								if(type.equals("tif"))
									type = "tiff";

								//Image saved in All files filter, default to .png
								if(type.equals("all"))
									type = "png";

								//If no extension at end of name, added one
								if(!filename.toLowerCase().endsWith('.' +type))
									filename = filename+ '.' +(type);

								//If valid extension was choosen
								if(!failed)
									decode_pdf.getDynamicRenderer().saveImage(id, filename,type);
							}
						}
					}
				}
			});
			
			extract.add(snapShot);
			snapShot.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					currentCommands.executeCommand(Commands.SNAPSHOT, null);
				}
			});
			
			rightClick.addSeparator();
			
			rightClick.add(find);
			find.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					
					/**ensure co-ords in right order*/
					Rectangle coords= decode_pdf.getCursorBoxOnScreen();
					if(coords==null){
						JOptionPane.showMessageDialog(decode_pdf, "There is no text selected.\nPlease highlight the text you wish to search.", "No Text selected", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					String textToFind=currentGUI.showInputDialog(Messages.getMessage("PdfViewerMessage.GetUserInput"));

					//if cancel return to menu.
					if(textToFind==null || textToFind.length()<1){
						return;
					}
					
					
					int t_x1=coords.x;
					int t_x2=coords.x+coords.width;
					int t_y1=coords.y;
					int t_y2=coords.y+coords.height;

					if(t_y1<t_y2){
						int temp = t_y2;
						t_y2=t_y1;
						t_y1=temp;
					}

					if(t_x1>t_x2){
						int temp = t_x2;
						t_x2=t_x1;
						t_x1=temp;
					}

					if(t_x1<currentGUI.cropX)
						t_x1 = currentGUI.cropX;
					if(t_x1>currentGUI.mediaW-currentGUI.cropX)
						t_x1 = currentGUI.mediaW-currentGUI.cropX;

					if(t_x2<currentGUI.cropX)
						t_x2 = currentGUI.cropX;
					if(t_x2>currentGUI.mediaW-currentGUI.cropX)
						t_x2 = currentGUI.mediaW-currentGUI.cropX;

					if(t_y1<currentGUI.cropY)
						t_y1 = currentGUI.cropY;
					if(t_y1>currentGUI.mediaH-currentGUI.cropY)
						t_y1 = currentGUI.mediaH-currentGUI.cropY;

					if(t_y2<currentGUI.cropY)
						t_y2 = currentGUI.cropY;
					if(t_y2>currentGUI.mediaH-currentGUI.cropY)
						t_y2 = currentGUI.mediaH-currentGUI.cropY;
					
					//<start-demo>
					/**<end-demo>
	                    JOptionPane.showMessageDialog(currentGUI.getFrame(),Messages.getMessage("PdfViewerMessage.FindDemo"));
	                    textToFind=null;
	                    /**/

					int searchType = SearchType.DEFAULT;

					int caseSensitiveOption=currentGUI.showConfirmDialog(Messages.getMessage("PdfViewercase.message"),
							null,	JOptionPane.YES_NO_OPTION);

					if(caseSensitiveOption==JOptionPane.YES_OPTION)
						searchType |= SearchType.CASE_SENSITIVE;

					int findAllOption=currentGUI.showConfirmDialog(Messages.getMessage("PdfViewerfindAll.message"),
							null,	JOptionPane.YES_NO_OPTION);

					if(findAllOption==JOptionPane.NO_OPTION)
						searchType |= SearchType.FIND_FIRST_OCCURANCE_ONLY;
					
					int hyphenOption=currentGUI.showConfirmDialog(Messages.getMessage("PdfViewerfindHyphen.message"),
							null,	JOptionPane.YES_NO_OPTION);

					if(hyphenOption==JOptionPane.YES_OPTION)
						searchType |= SearchType.MUTLI_LINE_RESULTS;

					if(textToFind!=null){
						try {
							float[] co_ords;
							
							if((searchType & SearchType.MUTLI_LINE_RESULTS)==SearchType.MUTLI_LINE_RESULTS)
								co_ords = decode_pdf.getGroupingObject().findTextInRectangleAcrossLines(t_x1,t_y1,t_x2,t_y2,commonValues.getCurrentPage(),textToFind,searchType);
							else
								co_ords = decode_pdf.getGroupingObject().findTextInRectangle(t_x1,t_y1,t_x2,t_y2,commonValues.getCurrentPage(),textToFind,searchType);
							
							if(co_ords!=null){
								if(co_ords.length<3)
									currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.Found")+ ' ' +co_ords[0]+ ',' +co_ords[1]);
								else{
									StringBuffer displayCoords = new StringBuffer();
									String coordsMessage = Messages.getMessage("PdfViewerMessage.FoundAt");
									for(int i=0;i<co_ords.length;i=i+5){
										displayCoords.append(coordsMessage).append(' ');
										displayCoords.append(co_ords[i]);
										displayCoords.append(',');
										displayCoords.append(co_ords[i+1]);
										
//										//Other two coords of text
//										displayCoords.append(',');
//										displayCoords.append(co_ords[i+2]);
//										displayCoords.append(',');
//										displayCoords.append(co_ords[i+3]);
										
										displayCoords.append('\n');
										if(co_ords[i+4]==-101){
											coordsMessage = Messages.getMessage("PdfViewerMessage.FoundAtHyphen");
										}else{
											coordsMessage = Messages.getMessage("PdfViewerMessage.FoundAt");
										}
										
									}
									currentGUI.showMessageDialog(displayCoords.toString());
								}
							}else
								currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.NotFound"));

						} catch (PdfException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

					}
				}
					
			});
			
			menuCreated = true;
			decode_pdf.add(rightClick);
		}
		
		//user has stopped clicking so we want to remove the outline rectangle
		public void mouseReleased(MouseEvent event) {

			if(event.getButton()==MouseEvent.BUTTON1){
				old_m_x2 = -1;
				old_m_y2 = -1;
				
				if(DynamicVectorRenderer.textBasedHighlight)
					decode_pdf.setHighlightStartPoint(-1, -1);
				
				updateCords(event.getX(), event.getY(), event.isShiftDown());

				decode_pdf.repaintArea(new Rectangle(commonValues.m_x1-currentGUI.cropX, commonValues.m_y2+currentGUI.cropY, commonValues.m_x2 - commonValues.m_x1+currentGUI.cropX,
						(commonValues.m_y1 - commonValues.m_y2)+currentGUI.cropY), currentGUI.mediaH);//redraw
				decode_pdf.repaint();

				dragged = false;

				if(decode_pdf.isExtractingAsImage()){

					/** remove any outline and reset variables used to track change */
						currentGUI.setRectangle(null);
						decode_pdf.updateCursorBoxOnScreen(null, null); //remove box
						decode_pdf.removeFoundTextAreas(null); //remove highlighted text
						decode_pdf.setHighlightedImage(null);// remove image highlight

					decode_pdf.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

					currentCommands.extractSelectedScreenAsImage();
					decode_pdf.setExtractingAsImage(false);

				}
			}else if(event.getButton()==MouseEvent.BUTTON3){
				if(decode_pdf.getHighlightImage()==null)
					extractImage.setEnabled(false);
				else
					extractImage.setEnabled(true);
				
				if(decode_pdf.getHighlightedAreas()==null){
					extractText.setEnabled(false);
					find.setEnabled(false);
					copy.setEnabled(false);
				}else{
					extractText.setEnabled(true);
					find.setEnabled(true);
					copy.setEnabled(true);
				}
				
				if(!menuCreated)
					createRightClickMenu();
				
				if(decode_pdf!=null && decode_pdf.isOpen() && decode_pdf.getDisplayView()==Display.SINGLE_PAGE)
					rightClick.show(decode_pdf, event.getX(), event.getY());
				
				rightClicked = false;
			}
		}
		
		//If mouse leaves viewer, stop scrolling
		public void mouseExited(MouseEvent arg0) {
			scrollThread.setAutoScroll(false, 0, 0, 0);
		}
	}
	
	/**listener used to update display*/
	protected class mouse_mover implements MouseMotionListener {
		
		boolean altIsDown = false;
		
		public mouse_mover() {}

		public void mouseDragged(MouseEvent event) {
			if(!rightClicked){
				altIsDown = event.isAltDown();
				dragged = true;
				int[] values = updateXY(event);
				commonValues.m_x2=values[0];
				commonValues.m_y2=values[1];

				scrollAndUpdateCoords(event);

				if(commonValues.isPDF())
					generateNewCursorBox();

				if(currentGUI.addUniqueIconToFileAttachment)
					checkLinks(false,decode_pdf.getIO());
			}
		}

		/**
		 * generate new  cursorBox and highlight extractable text,
		 * if hardware acceleration off and extraction on<br>
		 * and update current cursor box displayed on screen
		 */
		protected void generateNewCursorBox() {
			
			//redraw rectangle of dragged box onscreen if it has changed significantly
			if ((old_m_x2!=-1)|(old_m_y2!=-1)|(Math.abs(commonValues.m_x2-old_m_x2)>5)|(Math.abs(commonValues.m_y2-old_m_y2)>5)) {	

				//allow for user to go up
				int top_x = commonValues.m_x1;
				if (commonValues.m_x1 > commonValues.m_x2)
					top_x = commonValues.m_x2;
				int top_y = commonValues.m_y1;
				if (commonValues.m_y1 > commonValues.m_y2)
					top_y = commonValues.m_y2;
				int w = Math.abs(commonValues.m_x2 - commonValues.m_x1);
				int h = Math.abs(commonValues.m_y2 - commonValues.m_y1);

				//lose old highlight
				if(!DynamicVectorRenderer.textBasedHighlight){
					decode_pdf.removeFoundTextArea(currentGUI.getRectangle());
					
				}
				if(DynamicVectorRenderer.textBasedHighlight)
					decode_pdf.setCurrent_p(new Point(commonValues.m_x2, commonValues.m_y2));
                
				//add an outline rectangle  to the display
				Rectangle currentRectangle=new Rectangle (top_x,top_y,w,h);
				currentGUI.setRectangle(currentRectangle);
				
                //tell JPedal to highlight text in this area (you can add other areas to array)
                decode_pdf.updateCursorBoxOnScreen(currentRectangle,PdfDecoder.highlightColor);
				if(!decode_pdf.isExtractingAsImage()){
					int type = decode_pdf.getObjectUnderneath(commonValues.m_x1, commonValues.m_y1);
					
					if(!DynamicVectorRenderer.textBasedHighlight || altIsDown ||
							(type!=DynamicVectorRenderer.TEXT && type!=DynamicVectorRenderer.TRUETYPE && 
									type!=DynamicVectorRenderer.TYPE1C && type!=DynamicVectorRenderer.TYPE3)){
						
						//Highlight all within the rectangle
						decode_pdf.setMouseHighlightArea(currentRectangle);
						
					}else //Find start and end locations and highlight all object in order in between
						decode_pdf.setFoundTextPoints(new Point(commonValues.m_x1, commonValues.m_y1), new Point(commonValues.m_x2, commonValues.m_y2));
				}
				//reset tracking
				old_m_x2=commonValues.m_x2;
				old_m_y2=commonValues.m_y2;

			}
		}
		
		public void mouseMoved(MouseEvent event) {

			updateCords(event.getX(), event.getY(), event.isShiftDown());
			if(currentGUI.addUniqueIconToFileAttachment)
				checkLinks(false,decode_pdf.getIO());
			
			//Update cursor for this position
			int[] values = updateXY(event);
			int x =values[0];
			int y =values[1];
			decode_pdf.getObjectUnderneath(x, y);
		}

	}


	/**listener used to update display*/
	protected class Extractor_mouse_clicker extends mouse_clicker {

		public void mousePressed(MouseEvent event){
			Rectangle currentRectangle=currentGUI.getRectangle();
			if(currentRectangle==null){
				//draw the first cursor box on screen
				super.mousePressed(event);

				//ensure we keep drawing the new cursor box
				drawingCursorBox = true;
			}else{
				int[] values = updateXY(event);

				//store current cursor point for use when dragging
				oldX=values[0];
				oldY=values[1];
			}
		}

		public void mouseReleased(MouseEvent event) {
			//turn off drawing new cursor box
			drawingCursorBox = false;
			
			old_m_x2 = -1;
			old_m_y2 = -1;

			updateCords(event.getX(), event.getY(), event.isShiftDown());

			/* shuffle points to ensure cursorBox is setup correctly */
			int tmp;
			if(commonValues.m_x1>commonValues.m_x2){
				tmp=commonValues.m_x1;
				commonValues.m_x1=commonValues.m_x2;
				commonValues.m_x2=tmp;
			}
			if(commonValues.m_y1<commonValues.m_y2){
				tmp=commonValues.m_y1;
				commonValues.m_y1=commonValues.m_y2;
				commonValues.m_y2=tmp;
			}

            decode_pdf.repaint();//redraw

			//turn altering of current cursor box off
			dragAltering=false;
			dragged = false;
		}
	}



	/**listener used to update display*/
	protected class Extractor_mouse_mover extends mouse_mover {

		public void mouseDragged(MouseEvent event) {
			dragged = true;
			altIsDown = event.isAltDown();
			Rectangle currentRectangle=currentGUI.getRectangle();
			//if no rectangle or currently drawing a new rectangle 
			//use simpleViewer mouseDragged
			if(currentRectangle==null || drawingCursorBox){
				decode_pdf.setDrawCrossHairs(true,boxContained,Color.red);
				super.mouseDragged(event);
				boxContained=-1;
				return;
			}
			
			int[] values = updateXY(event);

			//generate handle boxes
			boxes=createNewRectangles(currentRectangle);

			//test if cursor was in cursor box handles when drag started
			//if we already have a handle selected don't look again
			if(boxContained==-1){
				for(int i=0;i<boxes.length;i++){
					if(boxes[i].contains(oldX,oldY)){
						boxContained = i;
						break;
					}
				}
			}

			//if there is a selected handle or we are altering the current cursor box 
			if(boxContained!=-1 || dragAltering){

				//turn new rectangle drawing off
				drawingCursorBox = false;


				//initialise box to be highlighted with current selected handle
				int highlightBox=boxContained;

				//get centre coords of selected box
				int boxCenterX = (int)boxes[boxContained].getCenterX();
				int boxCenterY = (int)boxes[boxContained].getCenterY();

//				boolean top=false,bottom=false,left=false,right=false;//Checking code
				/**check which line is to be altered in the x axis and change cursor box values*/
				if(currentRectangle.x==boxCenterX){//left
					commonValues.m_x1=values[0];
//					left =true;//Checking code
				}else if(currentRectangle.x+currentRectangle.width ==boxCenterX){//right
					commonValues.m_x2=values[0];
//					right =true;//Checking code
				}

				/**check which line is to be altered in the y axis and change cursor box values*/
				if(currentRectangle.y==boxCenterY){//bottom
					commonValues.m_y2=values[1];
//					bottom =true;//Checking code
				}else if(currentRectangle.y+currentRectangle.height ==boxCenterY){//top
					commonValues.m_y1=values[1];
//					top=true;//Checking code
				}

//				System.out.println("top="+top+" bottom="+bottom+" left="+left+" right="+right+" "+highlightBox);//Checking code
				/**
				 * work out whether the handle highlight should be changed
				 * and which way it should be changed
				 */
				boolean changeX=false,changeY=false;
				if(commonValues.m_x1>commonValues.m_x2){
					changeX=true;
				}
				if(commonValues.m_y2>commonValues.m_y1){
					changeY=true;
				}

				/**if a highlight should be changed, change it*/
				if(changeX || changeY){
					switch(highlightBox){
					case 0://left
						if(changeX)
							highlightBox = 3;//change to right
						//				    else if(!left)//Checking code
						//				        System.err.println("error 1");//Checking code
						break;

					case 1://bottom
						if(changeY)
							highlightBox = 2;//change to top
						//				    else if(!bottom)//Checking code
						//				        System.err.println("error 2");//Checking code
						break;

					case 2://top
						if(changeY)
							highlightBox = 1;//change to bottom
						//				    else if(!top)//Checking code
						//				        System.err.println("error 3");//Checking code
						break;

					case 3://right
						if(changeX)
							highlightBox = 0;//change to left
						//				    else if(!right)//Checking code
						//				        System.err.println("error 4");//Checking code
						break;

					case 4://bottom left
						if(changeX)
							highlightBox = 6;//change to bottom right
						else if(changeY)
							highlightBox = 5;//change to top left
						if(changeX && changeY)
							highlightBox = 7;//change to top right
						//				    if((!left) || (!bottom))//Checking code
						//				        System.err.println("error 5");//Checking code
						break;

					case 5://top left
						if(changeX)
							highlightBox = 7;//change to top right
						else if(changeY)
							highlightBox = 4;//change to bottom left
						if(changeX && changeY)
							highlightBox = 6;//change to bottom right
						//				    if((!left) || (!top))//Checking code
						//				        System.err.println("error 7");//Checking code
						break;

					case 6://bottom right
						if(changeX)
							highlightBox = 4;//change to bottom left
						else if(changeY)
							highlightBox = 7;//change to top right
						if(changeX && changeY)
							highlightBox = 5;//change to top left
						//			        if((!right) || (!bottom))//Checking code
						//				        System.err.println("error 9");//Checking code
						break;

					case 7://top right
						if(changeX)
							highlightBox = 5;//change to top left
						else if(changeY)
							highlightBox = 6;//change to bottom right
						if(changeX && changeY)
							highlightBox =4;//change to bottom left 
						//		            if((!right) || (!top))//Checking code
						//				        System.err.println("error 11");//Checking code
						break;

						//				default://Checking code
						//	                System.out.println("ERROR default");//Checking code
					}
				}

				//ensure crosshairs are drawn, and set current highlighted box to be drawn red
				decode_pdf.setDrawCrossHairs(true,highlightBox,Color.red);

				/**
				 * we have now changed the cursor coords, commonValues.m_x1 commonValues.m_y1 commonValues.m_x2 commonValues.m_y2 
				 * So now update displayed coords and cursor box on screen
				 */
				scrollAndUpdateCoords(event);
				generateNewCursorBox();

				//ensure we are altering the current cursor box and don't draw new one
				dragAltering=true;

				//store current cursor point for comparison next time
				oldX=values[0];
				oldY=values[1];

			}else{
				/**
				 * if there is no selected handle on drag, draw new cursorbox
				 */
				drawingCursorBox = true;

				//ensure highlight is not drawn
				boxContained=-1;

				decode_pdf.setDrawCrossHairs(true,boxContained,Color.red);

				//setup start point of new cursor box
				commonValues.m_x1=oldX;
				commonValues.m_y1=oldY;

				//setup current point for new cursor box
				commonValues.m_x2=values[0];
				commonValues.m_y2=values[1];

				scrollAndUpdateCoords(event);
				generateNewCursorBox();
			}
		}

		//variables used only in mouseMoved
		private boolean inRect=false;//whether cursor currently in cursor box
		private boolean handleChange=false;//whether the highlight should be changed

		public void mouseMoved(MouseEvent event) {

			super.mouseMoved(event);
			Rectangle currentRectangle=currentGUI.getRectangle();
			//generate handle boxes
			boxes=createNewRectangles(currentRectangle);

			//find which handle, if any cursor is in
			if(boxes!=null){
				int oldBox = boxContained;//save old selected value
				boxContained = -1;//reset current selected highlight

				for(int i=0;i<boxes.length;i++){
					if(boxes[i].contains(cx,cy)){
						boxContained = i;
						break;
					}
				}

				//if we find a handle and it is not already selected to highlight ensure redraw
				if(boxContained!=oldBox){
					handleChange = true;
				}
			}

			//if cursor in cursorbox or within handleGap pixels of it show crosshairs
			if(currentRectangle!=null){
				if((currentRectangle.x-handlesGap)<cx && (currentRectangle.x+currentRectangle.width+handlesGap)>cx &&
						(currentRectangle.y-handlesGap)<cy && (currentRectangle.y+currentRectangle.height+handlesGap)>cy){
					//cursor is in cursor box

					decode_pdf.setDrawCrossHairs(true,boxContained,Color.red);

					//if was not in rectangle repaint display
					if(!inRect || handleChange){
                        decode_pdf.repaint();
						handleChange=false;
						inRect=true;
					}
				}else{
					//cursor is NOT in cursor box

					decode_pdf.setDrawCrossHairs(false,boxContained,Color.red);

					//if was in rectangle repaint display
					if(inRect || handleChange){
                        decode_pdf.repaint();
						handleChange=false;
						inRect=false;
					}
				}
			}
		}

		/**
		 * creates the eight cursor box handles for the cursor box<br>
		 * returns Rectangle[] whos indexes are the same as those used to display them on screen<br>
		 */
		private Rectangle[] createNewRectangles(Rectangle currentRectangle) {
			if(currentRectangle!=null){

				int x1 = currentRectangle.x;
				int y1 = currentRectangle.y;
				int x2 = x1+currentRectangle.width;
				int y2 = y1+currentRectangle.height;

				Rectangle[] cursorBoxHandles = new Rectangle[8];
				//*draw centre of line handle boxs
				//left
				cursorBoxHandles[0] = new Rectangle(x1-handlesGap,(y1+(Math.abs(y2-y1))/2)-handlesGap,handlesGap*2,handlesGap*2);//0
				//bottom
				cursorBoxHandles[1] = new Rectangle((x1+(Math.abs(x2-x1))/2)-handlesGap,y1-handlesGap,handlesGap*2,handlesGap*2);//1
				//top
				cursorBoxHandles[2] = new Rectangle((x1+(Math.abs(x2-x1))/2)-handlesGap,y2-handlesGap,handlesGap*2,handlesGap*2);//2
				//right
				cursorBoxHandles[3] = new Rectangle(x2-handlesGap,(y1+(Math.abs(y2-y1))/2)-handlesGap,handlesGap*2,handlesGap*2);//3
				/**/

				//*draw corner handles
				//bottom left
				cursorBoxHandles[4] = new Rectangle(x1-handlesGap,y1-handlesGap,handlesGap*2,handlesGap*2);//4
				//top left
				cursorBoxHandles[5] = new Rectangle(x1-handlesGap,y2-handlesGap,handlesGap*2,handlesGap*2);//5
				//bottom right
				cursorBoxHandles[6] = new Rectangle(x2-handlesGap,y1-handlesGap,handlesGap*2,handlesGap*2);//6
				//top right
				cursorBoxHandles[7] = new Rectangle(x2-handlesGap,y2-handlesGap,handlesGap*2,handlesGap*2);//7
				/**/

				return cursorBoxHandles;
			}
			return null;
		}
	}



	public SwingMouseHandler(PdfDecoder decode_pdf, SwingGUI currentGUI,
			Values commonValues,Commands currentCommands) {

		this.decode_pdf=decode_pdf;
		this.currentGUI=currentGUI;
		this.commonValues=commonValues;
		this.currentCommands=currentCommands;

		decode_pdf.addExternalHandler(this, Options.SwingMouseHandler);
		
		
        if (SwingUtilities.isEventDispatchThread()){
            scrollThread.init();
        }else {
            final Runnable doPaintComponent = new Runnable() {
                public void run() {
                   scrollThread.init();
                }
            };
            SwingUtilities.invokeLater(doPaintComponent);
        }
	}


	/**
	 * checks the link areas on the page and allow user to save file
	 **/
	public void checkLinks(boolean mouseClicked, PdfObjectReader pdfObjectReader){


		//get 'hotspots' for the page
		Map objs=currentGUI.getHotspots();

        //look for a match
		if(objs!=null){

			//new code to check for match
			Iterator objKeys=objs.keySet().iterator();
			FormObject annotObj=null;
			while(objKeys.hasNext()){
				annotObj=(FormObject) objKeys.next();
				if(annotObj.getBoundingRectangle().contains(cx,cy)){
					break;
                }

				//reset to null so when exits no match
				annotObj=null;
			}

			/**action for moved over of clicked*/
			if(annotObj!=null){

				/**
				 * get EF object containing file data
				 */
				//annotObj is now actual object (on lazy initialisation so EF has not been read).....

				System.out.println(mouseClicked+" obj="+annotObj+" "+annotObj.getObjectRefAsString()+" "+annotObj.getBoundingRectangle());

				//@annot - in my example, ignore if not clicked
				if(!mouseClicked)
					return;
				
				//FS obj contains an EF obj which contains an F obj with the data in
				//F can be various - we are only interested in it as a Dictionary with a stream
				PdfObject EFobj=null, FSobj=annotObj.getDictionary(PdfDictionary.FS);
				if(FSobj!=null)
					EFobj=FSobj.getDictionary(PdfDictionary.EF);
                
				/**
				 * create the file chooser to select the file name
				 **/
				JFileChooser chooser = new JFileChooser(commonValues.getInputDir());
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int state = chooser.showSaveDialog(currentGUI.getFrame());

				
				/**
				 * save file and take the hit
				 */
				if(state==0){
					File fileTarget = chooser.getSelectedFile();

					//here is where we take the hit (Only needed if on lazy init - ie Unread Dictionary like EF)....			
					if(EFobj!=null)
						pdfObjectReader.checkResolved(EFobj);

					//contains the actual file data
					PdfObject Fobj=EFobj.getDictionary(PdfDictionary.F);

					//see if cached or decoded (cached if LARGE)
					//IMPORTANT NOTE!!! - if the object is in a compressed stream (a 'blob' of
					//objects which we need to read in one go, it will not be cached
					String nameOnDisk=Fobj.getCachedStreamFile(pdfObjectReader);
					
					//if you get null, make sure you have enabled caching and
					//file is bigger than cache
					System.out.println("file="+nameOnDisk);
					
					if(nameOnDisk!=null){ //just copy
						ObjectStore.copy(nameOnDisk,fileTarget.toString());
					}else{ //save out
						byte[] fileData=Fobj.getDecodedStream();

						if(fileData!=null){ //write out if in memory
							FileOutputStream fos;
							try {
								fos = new FileOutputStream(fileTarget);
								fos.write(fileData);
								fos.close();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}

	public void setupExtractor() {
		decode_pdf.addMouseMotionListener(new Extractor_mouse_mover());
		decode_pdf.addMouseListener(new Extractor_mouse_clicker());


	}

	/**
	 * scroll to visible Rectangle and update Coords box on screen
	 */
	protected void scrollAndUpdateCoords(MouseEvent event) {
        //scroll if user hits side
        int interval=decode_pdf.getScrollInterval();
		Rectangle visible_test=new Rectangle(currentGUI.AdjustForAlignment(event.getX()),event.getY(),interval,interval);
		if((currentGUI.allowScrolling())&&(!decode_pdf.getVisibleRect().contains(visible_test)))
                decode_pdf.scrollRectToVisible(visible_test);

        updateCords(event.getX(), event.getY(), event.isShiftDown());
    }

	public void updateCordsFromFormComponent(MouseEvent e) {
		JComponent component = (JComponent) e.getSource();
		
		int x = component.getX() + e.getX();
		int y = component.getY() + e.getY();
		
		updateCords(x, y, e.isShiftDown());
	}
	
	/**update current page co-ordinates on screen
	 */
	public void updateCords(/*MouseEvent event*/int x, int y, boolean isShiftDown){
		
		float scaling=currentGUI.getScaling();
		int inset=currentGUI.getPDFDisplayInset();
		int rotation=currentGUI.getRotation();

		int ex=currentGUI.AdjustForAlignment(x)-inset;
		int ey=y-inset;

		//undo any viewport scaling
		if(commonValues.maxViewY!=0){ // will not be zero if viewport in play
			ex=(int)(((ex-(commonValues.dx*scaling))/commonValues.viewportScale));
			ey=(int)((currentGUI.mediaH-((currentGUI.mediaH-(ey/scaling)-commonValues.dy)/commonValues.viewportScale))*scaling);
		}

		cx=(int)((ex)/scaling);
		cy=(int)((ey/scaling));


		if(decode_pdf.getDisplayView()!=Display.SINGLE_PAGE){
			cx=0;
			cy=0;
			//cx=decode_pdf.getMultiPageOffset(scaling,cx,commonValues.getCurrentPage(),Display.X_AXIS);
			// cy=decode_pdf.getMultiPageOffset(scaling,cy,commonValues.getCurrentPage(),Display.Y_AXIS);
		} else if(rotation==90){
			int tmp=(cx+currentGUI.cropY);
			cx = (cy+currentGUI.cropX);
			cy =tmp;	
		}else if((rotation==180)){
			cx =(currentGUI.cropW+currentGUI.cropX)-cx;
			cy =(cy+currentGUI.cropY);
		}else if((rotation==270)){
			int tmp=(currentGUI.cropH+currentGUI.cropY)-cx;
			cx =(currentGUI.cropW+currentGUI.cropX)-cy;
			cy =tmp;
		}else{
			cx = (cx+currentGUI.cropX);
			cy =(currentGUI.cropH+currentGUI.cropY)-cy;
		}


		if((commonValues.isProcessing())|(commonValues.getSelectedFile()==null))
			currentGUI.setCoordText("  X: "+ " Y: " + ' ' + ' '); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		else
			currentGUI.setCoordText("  X: " + cx + " Y: " + cy+ ' ' + ' ' +message); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		//scroll if user hits side and shift key not pressed
		if((currentGUI.allowScrolling())&&(!isShiftDown)){
			int interval=decode_pdf.getScrollInterval()*2;
			Rectangle visible_test=new Rectangle(currentGUI.AdjustForAlignment(x)-interval,y-interval,interval*2,interval*2);

			//If at edge call thread to allow for continuous scrolling
			if(!decode_pdf.getVisibleRect().contains(visible_test)){
				scrollThread.setAutoScroll(true,x,y,interval);
			}else{
				scrollThread.setAutoScroll(false,0,0,0);
			}
//				decode_pdf.scrollRectToVisible(visible_test);
		}
	}


	public void updateRectangle() {

		Rectangle currentRectangle=currentGUI.getRectangle();

		if(currentRectangle!=null){
			Rectangle newRect = decode_pdf.getCombinedAreas(currentRectangle,false);
			if(newRect!=null){
				commonValues.m_x1=newRect.x;
				commonValues.m_y2=newRect.y;
				commonValues.m_x2=newRect.x+newRect.width;
				commonValues.m_y1=newRect.y+newRect.height;

				currentRectangle=newRect;
				decode_pdf.updateCursorBoxOnScreen(currentRectangle,PdfDecoder.highlightColor);
                decode_pdf.repaint();
			}
		}

	}

	public void setupMouse() {
		/**
		 * track and display screen co-ordinates and support links
		 */
		decode_pdf.addMouseMotionListener(new mouse_mover());
		decode_pdf.addMouseListener(new mouse_clicker());
	}

	/**
	 * get raw co-ords and convert to correct scaled units
	 * @return int[] of size 2, [0]=new x value, [1] = new y value
	 */
	protected int[] updateXY(MouseEvent event) {

		float scaling=currentGUI.getScaling();
		int inset=currentGUI.getPDFDisplayInset();
		int rotation=currentGUI.getRotation();

		//get co-ordinates of top point of outine rectangle
		int x=(int)(((currentGUI.AdjustForAlignment(event.getX()))-inset)/scaling);
		int y=(int)((event.getY()-inset)/scaling);

		//undo any viewport scaling
		if(commonValues.maxViewY!=0){ // will not be zero if viewport in play
			x=(int)(((x-(commonValues.dx*scaling))/commonValues.viewportScale));
			y=(int)((currentGUI.mediaH-((currentGUI.mediaH-(y/scaling)-commonValues.dy)/commonValues.viewportScale))*scaling);
		}

		int[] ret=new int[2];
		if(rotation==90){	        
			ret[1] = x+currentGUI.cropY;
			ret[0] =y+currentGUI.cropX;
		}else if((rotation==180)){
			ret[0]=currentGUI.mediaW- (x+currentGUI.mediaW-currentGUI.cropW-currentGUI.cropX);
			ret[1] =y+currentGUI.cropY;
		}else if((rotation==270)){
			ret[1] =currentGUI.mediaH- (x+currentGUI.mediaH-currentGUI.cropH-currentGUI.cropY);
			ret[0]=currentGUI.mediaW-(y+currentGUI.mediaW-currentGUI.cropW-currentGUI.cropX);
		}else{
			ret[0] = x+currentGUI.cropX;
			ret[1] =currentGUI.mediaH-(y+currentGUI.mediaH-currentGUI.cropH-currentGUI.cropY);    
		}
		return ret;
	}


    class AutoScrollThread implements Runnable{

        Thread scroll;
        boolean autoScroll = false;
        int x = 0;
        int y = 0;
        int interval = 0;

        public AutoScrollThread(){
            scroll = new Thread(this);
        }

        public void setAutoScroll(boolean autoScroll, int x, int y, int interval){
            this.autoScroll = autoScroll;
            this.x = currentGUI.AdjustForAlignment(x);
            this.y = y;
            this.interval = interval;
        }

        public void init(){
            scroll.start();
        }

        int usedX,usedY;

        public void run() {

            while (Thread.currentThread().equals(scroll)) {

                //New autoscroll code allow for diagonal scrolling from corner of viewer

                //@kieran - you will see if you move the mouse to right or bottom of page, repaint gets repeatedly called
                //we need to add 2 test to ensure only redrawn if on page (you need to covert x and y back to PDF and
                //check fit in width and height - see code in this class
                //if(autoScroll && usedX!=x && usedY!=y && x>0 && y>0){
                if(autoScroll){
                    final Rectangle visible_test=new Rectangle(x-interval,y-interval,interval*2,interval*2);
                    final Rectangle currentScreen=decode_pdf.getVisibleRect();

                    if(!currentScreen.contains(visible_test)){

                        if (SwingUtilities.isEventDispatchThread()){
                            decode_pdf.scrollRectToVisible(visible_test);
                        }else {
                            final Runnable doPaintComponent = new Runnable() {
                                public void run() {
                                   decode_pdf.scrollRectToVisible(visible_test);
                                }
                            };
                            SwingUtilities.invokeLater(doPaintComponent);
                        }

                        //Check values modified by (interval*2) as visible rect changed by interval
                        if(x-(interval*2)<decode_pdf.getVisibleRect().x)
                            x = x-interval;
                        else if((x+(interval*2))>(decode_pdf.getVisibleRect().x+decode_pdf.getVisibleRect().width))
                            x = x+interval;

                        if(y-(interval*2)<decode_pdf.getVisibleRect().y)
                            y = y-interval;
                        else if((y+(interval*2))>(decode_pdf.getVisibleRect().y+decode_pdf.getVisibleRect().height))
                            y = y+interval;

                        //thrashes box if constantly called

                        //System.out.println("redraw on scroll");
                        //decode_pdf.repaint();
                    }

                    usedX=x;
                    usedY=y;

                }

                //Delay to check for mouse leaving scroll edge)
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }


}
