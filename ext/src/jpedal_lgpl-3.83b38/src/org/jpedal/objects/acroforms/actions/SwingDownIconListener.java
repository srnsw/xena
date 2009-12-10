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
* SwingDownIconListener.java
* ---------------
*/
package org.jpedal.objects.acroforms.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.AbstractButton;

import org.jpedal.objects.acroforms.overridingImplementations.FixImageIcon;


/**
 * class that implements the action listener interface to allow the down icon to be changed on the fly
 * 
 * @author chris
 */
public class SwingDownIconListener implements ActionListener{
	/**
	 * stores the down icon images for use when more than one pressed icon needs to be used
	 */
	private BufferedImage downOffImage;
	private BufferedImage downOnImage;
	private int width,height, iconRotation;
	
	/** takes in the down off buffered image and the down on buffered image
     * each one can be null,
     */
    public SwingDownIconListener(Object indownOffImage,Object indownOnImage, int iconRotation) {
        if(!FixImageIcon.NewFixImageCode){
        	downOffImage = (BufferedImage) indownOffImage;
        	downOnImage = (BufferedImage) indownOnImage;
        }
        this.iconRotation=iconRotation;
    }
			
    public void actionPerformed(ActionEvent e) {
    	if(PDFListener.debugMouseActions)
    		System.out.println("SwingDownIconListener.actionPerformed()");
    	
        AbstractButton tmp=(AbstractButton) e.getSource();
        
        width = tmp.getWidth();
        height = tmp.getHeight();
        
        if(FixImageIcon.NewFixImageCode){
        	FixImageIcon imgI = (FixImageIcon) tmp.getPressedIcon();
            imgI.setAttributes(width,height);
            imgI.swapImage(tmp.isSelected());
        }else {
	        if(tmp.isSelected()){
	            //then setPressedIcon(Off)
	            if(downOnImage!=null){
	                tmp.setText(null);
	                FixImageIcon imgI = new FixImageIcon(downOnImage,iconRotation);
	                imgI.setAttributes(width,height);
		            tmp.setPressedIcon(imgI);
				}
	        }else{
	            //setPressedIcon(On)
	            if(downOffImage!=null){
	                tmp.setText(null);
	                FixImageIcon imgI = new FixImageIcon(downOffImage, iconRotation);
	                imgI.setAttributes(width,height);
		            tmp.setPressedIcon(imgI);
				}
	        }
        }
    }
}
