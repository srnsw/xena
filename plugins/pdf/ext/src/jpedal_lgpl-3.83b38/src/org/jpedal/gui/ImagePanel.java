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
* ImagePanel.java
* ---------------
*/
package org.jpedal.gui;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * JPanel used as a GUI Container for a BufferedImage.<P>
 * It provides a display for a  Bufferedimage which we used to debug software
 */
public class ImagePanel extends JPanel
{
	
	/**image to display*/
	BufferedImage image;
	public ImagePanel( BufferedImage image ) 
	{
		if(image!=null)
			this.image = image;
		this.setPreferredSize( new Dimension( image.getWidth(), image.getHeight() ) );
	}
	
	////////////////////////////////////////////////////////////////////////
	/**
	 * update screen display
	 */
	final public void paintComponent( Graphics g )
	{
		super.paintComponent( g );
		Graphics2D g2 = (Graphics2D)g;
		g2.drawImage( image, 0, 0, this );
	}

	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {
		if(image!=null){
			this.image = image;
			this.setPreferredSize( new Dimension( image.getWidth(), image.getHeight() ) );
			this.repaint();
		}
	}
}
