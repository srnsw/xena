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
* GUIButton.java
* ---------------
*/
package org.jpedal.examples.simpleviewer.gui.generic;

import java.awt.Font;

import javax.swing.ImageIcon;

/**abstract button object into interface*/
public interface GUIButton {
	
	void init(String path, int i, String message);
	
	void setVisible(boolean b);
	
	void setEnabled(boolean b);
	
	void setIcon(ImageIcon icon);
	
	void setFont(Font font);
	
	int getID();

	void setName(String string);
	
}
