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
* JPedalCustomDrawObject.java
* ---------------
*/
package org.jpedal.external;

import java.awt.*;

/**
 * custom object to draw onto screen
 */
public interface JPedalCustomDrawObject {
	
	public static Integer ALLPAGES=new Integer(1);

    /**code to execute when rendering to screen*/
    void paint(Graphics2D g2);

    /**code to execute when printing to jps*/
    void print(Graphics2D g2, int x);

    /**allow user to switch on and off*/
    void setVisible(boolean isVisible);
    
    /** sets the x media offset of the page */
    void setMedX(int x);
    
    /** sets the y media offset of the page */
    void setMedY(int y);
}
