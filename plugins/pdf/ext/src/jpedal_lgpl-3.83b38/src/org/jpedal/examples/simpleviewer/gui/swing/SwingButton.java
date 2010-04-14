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
* SwingButton.java
* ---------------
*/
package org.jpedal.examples.simpleviewer.gui.swing;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.jpedal.examples.simpleviewer.gui.generic.GUIButton;

/**Swing specific implementation of GUIButton interface*/
public class SwingButton extends JButton implements GUIButton{
	
	private int ID;
	
	public SwingButton(){
		super();
	}
	
	public SwingButton(String string) {
		super(string);
	}
	
	public void init(String path,int ID,String toolTip){
		
		this.ID=ID;
		
		/**bookmarks icon*/
		setToolTipText(toolTip);
		

        setBorderPainted(false);

        URL url=getClass().getResource(path);
        if(url!=null){

            ImageIcon fontIcon = new ImageIcon(url);
            setIcon(fontIcon);
            createPressedLook(this,fontIcon);
        }else{ // actually its the text
            setText(path);

            setFont(new Font("Lucida",Font.ITALIC,14));

            if(path.equals("Buy"))
                setForeground(Color.BLUE);
            else
                setForeground(Color.RED);
        }


    }
	
	/**
	 * create a pressed look of the <b>icon</b> and added it to the pressed Icon of <b>button</b>
	 */
	private static void createPressedLook(AbstractButton button, ImageIcon icon) {
		BufferedImage image = new BufferedImage(icon.getIconWidth()+2,icon.getIconHeight()+2,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D)image.getGraphics();
		g.drawImage(icon.getImage(), 1, 1, null);
		g.dispose();
		ImageIcon iconPressed = new ImageIcon(image);
		button.setPressedIcon(iconPressed);
	}
	
	public void setIcon(ImageIcon icon) {
		super.setIcon(icon);
		
	}
	
	/**command ID of button*/
	public int getID() {
		
		return ID;
	}
	
	public void setName(String s){
		super.setName(s);
	}
	
}
