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
* FixImageIcon.java
* ---------------
*/
package org.jpedal.objects.acroforms.overridingImplementations;

import java.awt.*;

import javax.swing.*;


public class FixImageIcon extends ImageIcon implements Icon, SwingConstants {
    
    private static final long serialVersionUID = 8946195842453749725L;

	public static final boolean NewFixImageCode = true;
    
	private int width = -1;
    private int height = -1;
    
    /** -1 means only one image,<br>0 means unselected,<br>1 means selected
     * <br>if there is only one image it is stored as the selected image
     */
    private int selected = -1;

    private Image imageSelected=null;
    private Image imageUnselected=null;
    
	private int rotation = 0;

	private int pageRotation = 0;
	
	/** constructor to be used for one image */
	public FixImageIcon(Image img,int iconRotation) {
        imageSelected = img;
        rotation=iconRotation;
        selected = -1;
    }
	
	/** constructor for 2 images to be used for multipul pressed images, 
	 * sel should be 1 if its currently selected, 0 if unselected */
    public FixImageIcon(Image selImg,Image unselImg,int iconRotation,int sel) {
        imageSelected = selImg;
        imageUnselected = unselImg;
        rotation=iconRotation;
        selected = sel;
    }
    
    public void setAttributes(int newWidth,int newHeight, int pageRotation){
        width = newWidth;
        height = newHeight;
        this.pageRotation=pageRotation;
    }
    
    public void setAttributes(int newWidth,int newHeight){
        width = newWidth;
        height = newHeight;
    }
    
    public int getIconHeight() {
    	Image image;
    	switch(selected){
    	case 1:	image = imageSelected; break;
    	case 0: image = imageUnselected; break;
    	default: image = imageSelected; break;
    	}
    	
    	if(image==null)
			return height;
    	
        if(height==-1)
            return image.getHeight(null);
        else
            return height;
    }

    public int getIconWidth() {
    	Image image;
    	switch(selected){
    	case 1:	image = imageSelected; break;
    	case 0: image = imageUnselected; break;
    	default: image = imageSelected; break;
    	}
    	
		if(image==null)
			return width;
		
        if(width==-1)
            return image.getWidth(null);
        else
            return width;
    }
    
    public Image getImage(){
    	switch(selected){
    	case 1:	return imageSelected;
    	case 0: return imageUnselected;
    	default: return imageSelected;
    	}
    }

    public void setPageRotation(int pageRotation){
    	this.pageRotation  = pageRotation;
    }
  
    public void paintIcon(Component c, Graphics g, int x, int y) {
    	Image image;
    	switch(selected){
    	case 1:	image = imageSelected; break;
    	case 0: image = imageUnselected; break;
    	default: image = imageSelected; break;
    	}
    	
		if (image == null)
			return;

		if (c.isEnabled()) {
			g.setColor(c.getBackground());
		} else {
			g.setColor(Color.gray);
		}

		Graphics2D g2 = (Graphics2D) g;

//      g.translate(x, y);
		if (width > 0 && height > 0) {
//			AffineTransform transform = g2.getTransform();
//
//			System.out.println(transform.getScaleX()+" "+transform.getScaleY()
//					+" "+transform.getTranslateX()+" "+transform.getTranslateY()+" "+transform.getShearX()+" "+
//					transform.getShearY()+" "+pRotation+" "+rotation+" "+repaint);
//
//			System.out.println(pageRotation+" "+rotation);
			int rotationRequired = rotation - pageRotation;

//			System.out.println("rotating at = "+rotationRequired*Math.PI/180);

			if (rotationRequired ==90) {
				g2.rotate(-Math.PI / 2);
				g2.translate(-height, 0);
				g2.drawImage(image, 0, 0, height, width, null);
			} else if (rotationRequired == -90) {
				g2.rotate(Math.PI / 2);
				g2.translate(0, -width);
				g2.drawImage(image, 0, 0, height, width, null);
			} else if (rotationRequired == 180 || rotationRequired == -180) { //changed for cern_example 09-10-08 (ms)
				g2.rotate(Math.PI);
				g2.translate(-width, -height);
				g2.drawImage(image, 0, 0, width, height, null);
			/**} else if (rotationRequired == -180) {
				g2.rotate(Math.PI);
				g2.translate(width, height);
				g2.drawImage(image, 0, 0, width, height, null);/**/
			} else {
				g2.drawImage(image, 0, 0, width, height, null);
			}

//			int absRotation = Math.abs(rotationRequired);
//			if(absRotation == 90){
//				g2.rotate(Math.PI/2);
//				g2.translate(0, -width);
//				g2.drawImage(image,0,0,height,width,null);
//			}else{
//				g2.rotate(rotationRequired*Math.PI/180, x + width / 2, y + height / 2);
//				g2.drawImage(image,0,0,width,height,null);
//			}
//
//			if(pageRotation != rotation){
//				System.out.println("in special code");
//				g2.rotate(-Math.PI/2);
//				g2.translate(-height, 0);
//				g2.drawImage(image,0,0,height,width,null);
//			} else {
//				g2.drawImage(image,0,0,width,height,null);
//			}
		} else
			g2.drawImage(image, 0, 0, null);

		g2.translate(-x, -y);
	}

    /** if this imageicon was constructed for use with one image this will do nothing,
     * <br>otherwise it will set the selected image to be the selected image if the flag is true
     * or the unseleced image if the flag is false.
     */
	public void swapImage(boolean selectedImage) {
		if(selected==-1)
			return;
		
		if(selectedImage)
			selected = 1;
		else
			selected = 0;
	}
}
