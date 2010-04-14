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
* FrameCloser.java
* ---------------
*/
package org.jpedal.examples.simpleviewer.gui.swing;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.jpedal.PdfDecoder;
import org.jpedal.examples.simpleviewer.Commands;
import org.jpedal.examples.simpleviewer.Values;
import org.jpedal.examples.simpleviewer.SimpleViewer;
import org.jpedal.examples.simpleviewer.gui.generic.GUIThumbnailPanel;
import org.jpedal.examples.simpleviewer.utils.Printer;
import org.jpedal.examples.simpleviewer.utils.PropertiesFile;
import org.jpedal.gui.GUIFactory;
import org.jpedal.utils.Messages;

/**cleanly shutdown if user closes window*/
public class FrameCloser extends WindowAdapter {
	
	private Commands currentCommands;
	GUIFactory currentGUI;
	PdfDecoder decode_pdf;
	private Printer currentPrinter;
	GUIThumbnailPanel thumbnails;
	Values commonValues;
	PropertiesFile properties;
	
	public FrameCloser(Commands currentCommands,GUIFactory currentGUI,PdfDecoder decode_pdf, Printer currentPrinter,GUIThumbnailPanel thumbnails,Values commonValues,PropertiesFile properties) {
		this.currentCommands=currentCommands;
		this.currentGUI=currentGUI;
		this.decode_pdf=decode_pdf;
		this.currentPrinter=currentPrinter;
		this.thumbnails=thumbnails;
		this.commonValues=commonValues;
		this.properties = properties;
	}
	
	public void windowClosing(WindowEvent e) {
		
		properties.setValue("lastDocumentPage", ""+commonValues.getCurrentPage());
		try {
			properties.writeDoc();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
		}
		
		//remove any items stored on disk
		currentCommands.flush();


        if(currentPrinter.isPrinting())
			currentGUI.showMessageDialog(Messages.getMessage("PdfViewerBusyPrinting.message"));
        
        if(!commonValues.isProcessing()){
			
			//tell our code to exit cleanly asap
			thumbnails.terminateDrawing();
			
            
            int confirm=currentGUI.showConfirmDialog(Messages.getMessage("PdfViewerCloseing.message"),null,JOptionPane.YES_NO_OPTION);
			
			if(confirm==0){
				
				/**
				 * warn user on forms
				 */
				currentCommands.handleUnsaveForms();
				
				decode_pdf.closePdfFile();

                if(SimpleViewer.exitOnClose)
                    System.exit(0);
                else{
                    currentGUI.getFrame().setVisible(false);
                    if (currentGUI.getFrame() instanceof JFrame) {
						((JFrame)currentGUI.getFrame()).dispose();
					}
                }
            }
			
		}else{

			currentGUI.showMessageDialog(Messages.getMessage("PdfViewerDecodeWait.message"));
		}
	}
}
