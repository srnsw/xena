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
* GUIFactory.java
* ---------------
*/
package org.jpedal.gui;

import java.awt.Container;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.text.BadLocationException;
import org.jpedal.utils.repositories.Vector_Int;

public interface GUIFactory {

	public int BUTTONBAR = 0;
	public int NAVBAR = 1;
	public int PAGES = 2;

    /**flag used to show opening of multiple PDFs*/
    public static final Integer MULTIPAGE = new Integer(1);
    

    /**sets up layout menu (controls page views - Multiple, facing,etc)*/
	public void initLayoutMenus(JMenu pageLayout, String[] descriptions,
			int[] value);
	
	/**
	 * display a box giving user info about program
	 */
	public void getInfoBox();

	/**
	 * align rotation combo box to default for page
	 */
	public void resetRotationBox();

	/**popup box with details on current PDF file on tabbed Window*/
	public void showDocumentProperties(String selectedFile, String inputDir,
			long size, int pageCount, int currentPage);

	/**
	 * main method to initialise Swing specific code and create GUI display
	 */
	public void init(String[] scalingValues, final Object currentCommands,
			Object currentPrinter);

	/**add a visible box with updaing cursor location to toolbar*/
	public void addCursor();

	/**
	 *  add button to chosen menu 
	 */
	public void addButton(int line, String toolTip, String path, final int ID);

	/**
	 * setup menu items and add to menu
	 */
	public void addMenuItem(JMenu parentMenu, String text, String toolTip,
			final int ID);

	/**add selected combo - 3 possible values hard-coded (quality, scaling and rotation)*/
	public void addCombo(String title, String tooltip, int ID);
	
	/**
	 * set title or over-ride with message
	 */
	public void setViewerTitle(final String title);

	/**set all 3 combo boxes to isEnabled(value)*/
	public void resetComboBoxes(boolean value);

	/**
	 * do xml in popup as nicely coloured text
	 */
	public JScrollPane createPane(JTextPane text_pane, String content,
			boolean useXML) throws BadLocationException;

	/**get current value for a combobox (options QUALITY,SCALING,ROTATION)*/
	public int getSelectedComboIndex(int ID);

	/**set current index for a combobox (options QUALITY,SCALING,ROTATION)*/
	public void setSelectedComboIndex(int ID, int index);

	/**get current Item for a combobox (options QUALITY,SCALING,ROTATION)*/
	public void setSelectedComboItem(int ID, String index);

	/**get current Item for a combobox (options QUALITY,SCALING,ROTATION)*/
	public Object getSelectedComboItem(int ID);

	/**set rectangle to draw onscreen*/
	public void setRectangle(Rectangle newRect);

	/**get rectangle to draw onscreen*/
	public Rectangle getRectangle();

	/** 
	 * zoom into page 
	 */
	public void zoom(boolean Rotated);

	/**
	 *get current rotation
	 */
	public int getRotation();

	/**
	 * get current scaling
	 */
	public float getScaling();

	/**
	 * get inset between edge of JPanel and PDF page
	 */
	public int getPDFDisplayInset();

	/**read value from rotation box and apply - called by combo listener*/
	public void rotate();

	/**toggle state of autoscrolling on/off*/
	public void  toogleAutoScrolling();
	
	public void setAutoScrolling(boolean autoScroll);

	/**
	 * called by nav functions to decode next page
	 * (in GUI code as needs to manipulate large part of GUI)
	 */
	public void decodePage(final boolean resizePanel);

	/**
	 * remove outlines and flag for redraw
	 */
	//public void removeOutlinePanels();

	public void initStatus();

	public void resetStatus();

	public void initThumbnails(int itemSelectedCount, Vector_Int pageUsed);

	/**flush list of pages decoded*/
	public void setNoPagesDecoded();

	/**set text displayed in cursor co-ordinates box*/
	public void setCoordText(String string);

	/**set page number at bottom of screen*/
	public void setPageNumber();

	/**add MenuItem to main menu*/
	public void addToMainMenu(JMenu fileMenuList);

	/**return list of names of Annotations*/
	public String[] getAnnotTypes();
	

	/**allow access to root frame if required*/
	public Container getFrame();

	/**allow access to top button bar directly - used by ContentExtractor.
	 * For general use it is recommended you use addButton method
	 */
	public JToolBar getTopButtonBar();

    public void resetNavBar();

    public void showMessageDialog(Object message1);

	public void showMessageDialog(Object message, String title, int type);

	public String showInputDialog(Object message, String title, int type);

	public String showInputDialog(String message);

	public int showOptionDialog(Object displayValue, String message,
			int option, int type, Object icon, Object[] options, Object initial);

	public void showMessageDialog(JTextArea info);

	public int showConfirmDialog(String message, String message2, int option);
	
	public int showOverwriteDialog(String file,boolean yesToAllPresent);

	public void showItextPopup();
	
	public void showFirstTimePopup();
	
	public int showConfirmDialog(Object message, String title, int optionType, int messageType);

	/**show if user has set auto-scrolling on or off - if on moves at edge of panel to show more*/
	public boolean allowScrolling();

	public boolean isPDFOutlineVisible();

	public void setPDFOutlineVisible(boolean visible);


    /**
	 * remove the thumbnail display panel
	 */
	//public void removeThumbnails();

	/**set location of split pane between main PDF and outline/thumbnail panel*/
	public void setSplitDividerLocation(int size);

	/**message to show in status object*/
	public void updateStatusMessage(String message);

	public void resetStatusMessage(String message);

	/**set current status value 0 -100*/
	public void setStatusProgress(int size);


    public void setQualityBoxVisible(boolean visible);

	public void setPage(int newPage);
	
}
