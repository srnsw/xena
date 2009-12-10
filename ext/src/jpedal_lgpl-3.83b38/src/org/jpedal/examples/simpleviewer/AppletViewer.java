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
* AppletViewer.java
* ---------------
*/
package org.jpedal.examples.simpleviewer;

/**standard Java stuff*/
import javax.swing.JApplet;
import javax.swing.JFrame;

import org.jpedal.io.ObjectStore;

/**
 * <br>Description: Demo to show JPedal being used 
 * as a GUI viewer in an applet,
 * and to demonstrate some of JPedal's capabilities
 * 
 *
 */
public class AppletViewer extends JApplet{

	private static final long serialVersionUID = 8823940529835337414L;
	
	SimpleViewer current = new SimpleViewer(Values.RUNNING_APPLET);
	
	/** main method to run the software */
	public void init()
	{
		//current.setupViewer();
		
		//hack to access frame
		if (current.currentGUI.getFrame() instanceof JFrame)
			this.getContentPane().add(((JFrame)current.currentGUI.getFrame()).getContentPane());
		else
			this.getContentPane().add(current.currentGUI.getFrame());
		
	}
	
	public void destroy(){
		//ensure cached items removed
		ObjectStore.flushPages();
	}
	
}
