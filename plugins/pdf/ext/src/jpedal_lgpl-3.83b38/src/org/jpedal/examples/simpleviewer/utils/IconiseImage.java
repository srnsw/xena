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
* IconiseImage.java
* ---------------
*/
package org.jpedal.examples.simpleviewer.utils;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;

/**
 *Used in GUI example code.
 * Wrapper for putting image on display
 */
public class IconiseImage implements Icon{
	
	private BufferedImage current_image;
	private int w, h;
	
	public IconiseImage( BufferedImage current_image ) {
		this.current_image = current_image;
		w = current_image.getWidth();
		h = current_image.getHeight();
	}
	
	final public void paintIcon( Component c, Graphics g, int x, int y ){
		
		Graphics2D g2 = (Graphics2D)g;
		g2.drawImage( current_image, null, 0, 0 );
	}
	
	final public int getIconWidth(){
		return w;
	}
	
	final public int getIconHeight(){
		return h;
	}
}

