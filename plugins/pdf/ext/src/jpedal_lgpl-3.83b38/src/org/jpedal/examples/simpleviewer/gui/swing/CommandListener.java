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
* CommandListener.java
* ---------------
*/
package org.jpedal.examples.simpleviewer.gui.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.jpedal.examples.simpleviewer.Commands;
import org.jpedal.examples.simpleviewer.gui.generic.GUIButton;

/**single listener to execute all GUI commands and call Commands to execute*/
public class CommandListener implements ActionListener {
	
	Commands currentCommands;
	
	public CommandListener(Commands currentCommands) {
		this.currentCommands=currentCommands;
	}
	
	public void actionPerformed(ActionEvent e) {
		
		Object source=e.getSource();
		int ID;
		if(source instanceof GUIButton)
			ID=((GUIButton)source).getID();
		else if(source instanceof SwingMenuItem)
			ID=((SwingMenuItem)source).getID();
		else if(source instanceof SwingCombo)
			ID=((SwingCombo)source).getID();
		else
			ID=((SwingMenuItem)source).getID();
		
		currentCommands.executeCommand(ID, null);
	}
}
