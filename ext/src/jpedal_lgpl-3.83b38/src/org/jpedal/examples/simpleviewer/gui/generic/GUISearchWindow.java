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
* GUISearchWindow.java
* ---------------
*/
package org.jpedal.examples.simpleviewer.gui.generic;

import java.awt.Component;
import java.util.Map;

import org.jpedal.PdfDecoder;
import org.jpedal.examples.simpleviewer.Values;
import org.jpedal.examples.simpleviewer.gui.swing.SearchList;

/**abstract level of search window*/ 
public interface GUISearchWindow {
	
	//Varible added to allow multiple search style to be implemented
	int style = 0;

	void find(PdfDecoder decode_pdf, Values values);

	void grabFocusInInput();

	boolean isSearchVisible();

	void removeSearchWindow(boolean justHide);

	SearchList getResults();

	Map getTextRectangles();

	Component getContentPanel();

	int getStyle();

	void setStyle(int i);

}
