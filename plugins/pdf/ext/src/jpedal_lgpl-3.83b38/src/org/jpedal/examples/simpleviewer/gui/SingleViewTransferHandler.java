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
* SingleViewTransferHandler.java
* ---------------
*/
package org.jpedal.examples.simpleviewer.gui;

import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.List;

import javax.swing.JComponent;

import org.jpedal.examples.simpleviewer.Commands;
import org.jpedal.examples.simpleviewer.Values;
import org.jpedal.examples.simpleviewer.gui.generic.GUIThumbnailPanel;
import org.jpedal.exception.PdfException;

public class SingleViewTransferHandler extends BaseTransferHandler{

	public SingleViewTransferHandler(Values commonValues, GUIThumbnailPanel thumbnails, SwingGUI currentGUI, Commands currentCommands) {
		super(commonValues, thumbnails, currentGUI, currentCommands);
	}

	public boolean importData(JComponent src, Transferable transferable) {
		try {
			Object dragImport = getImport(transferable);

			if (dragImport instanceof String) {
				String url = (String) dragImport;
				
				if (url.indexOf("file:/") != url.lastIndexOf("file:/")) // make sure only one url is in the String
                    currentGUI.showMessageDialog("You may only import 1 file at a time");
                else
                    openFile(url);
			} else if (dragImport instanceof List) {
				List files = (List) dragImport;
				
                //System.out.println("list = " + list);
                if (files.size() == 1) { // we can process
					File file = (File) files.get(0);
					openFile(file.getAbsolutePath());
				} else {
					currentGUI.showMessageDialog("You may only import 1 file at a time");
				}
			}
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}

	protected void openFile(String file) throws PdfException {
		String testFile = file.toLowerCase();
		
		boolean isValid = ((testFile.endsWith(".pdf"))
				|| (testFile.endsWith(".fdf")) || (testFile.endsWith(".tif"))
				|| (testFile.endsWith(".tiff")) || (testFile.endsWith(".png"))
				|| (testFile.endsWith(".jpg")) || (testFile.endsWith(".jpeg")));
	
		if (isValid) {
			currentCommands.openTransferedFile(file);
		} else {
			currentGUI.showMessageDialog("You may only import a valid PDF or image");
		}
	}	
}
