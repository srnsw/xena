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
* BaseTransferHandler.java
* ---------------
*/
package org.jpedal.examples.simpleviewer.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jpedal.examples.simpleviewer.Commands;
import org.jpedal.examples.simpleviewer.Values;
import org.jpedal.examples.simpleviewer.gui.generic.GUIThumbnailPanel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class BaseTransferHandler extends TransferHandler {
	protected Commands currentCommands;
	protected SwingGUI currentGUI;
	protected GUIThumbnailPanel thumbnails;
	protected Values commonValues;

	public BaseTransferHandler(Values commonValues, GUIThumbnailPanel thumbnails, SwingGUI currentGUI, Commands currentCommands) {
		this.commonValues = commonValues;
		this.thumbnails = thumbnails;
		this.currentGUI = currentGUI;
		this.currentCommands = currentCommands;
	}

	public boolean canImport(JComponent dest, DataFlavor[] flavors) {
		return true;
	}

	protected Object getImport(Transferable transferable) throws UnsupportedFlavorException, IOException, ParserConfigurationException, SAXException {
		DataFlavor[] flavors = transferable.getTransferDataFlavors();
		DataFlavor listFlavor = null;
		int lastFlavor = flavors.length - 1;
	
		// Check the flavors and see if we find one we like.
		// If we do, save it.
		for (int f = 0; f <= lastFlavor; f++) {
			if (flavors[f].isFlavorJavaFileListType()) {
				listFlavor = flavors[f];
			}
		}
		
		// Ok, now try to display the content of the drop.
		try {
			DataFlavor bestTextFlavor = DataFlavor.selectBestTextFlavor(flavors);
			if (bestTextFlavor != null) { // this could be a file from a web page being dragged in
				Reader r = bestTextFlavor.getReaderForText(transferable);
				
				/** acquire the text data from the reader. */
				String textData = readTextDate(r);
	
//              System.out.println(textData);
				
	            /** need to remove all the 0 characters that will appear in the String when importing on Linux */
	            textData = removeChar(textData, (char) 0);
	
	            if(textData.indexOf("ftp:/") != -1) {
                	currentGUI.showMessageDialog("Files cannot be opened via FTP");
                	return null;
                }
	            
	            /** get the URL from the text data */
	            textData = getURL(textData);
	            
	            /** replace URL spaces */
	            textData = textData.replaceAll("%20", " ");
	            
				return textData;
	
	        } else if (listFlavor != null) { // this is most likely a file being dragged in
				List list = (List) transferable.getTransferData(listFlavor);
				
				return list;
			}
		} catch (Exception e) {
			return null;
		}
		
		return null;
	}
	
	private String removeChar(String s, char c) {
        String r = "";
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != c) r += s.charAt(i);
        }
        return r;
    }

    /**
	 * Returns the URL from the text data acquired from the transferable object.
	 * @param textData text data acquired from the transferable.
	 * @return the URL of the file to open
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private String getURL(String textData) throws ParserConfigurationException, SAXException, IOException {
        if (!textData.startsWith("http://") && !textData.startsWith("file://")) { // its not a url so it must be a file
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(textData.getBytes()));

            Element a = (Element) doc.getElementsByTagName("a").item(0);
            textData = getHrefAttribute(a);
        }            
		
		return textData;
	}

	/**
	 * Acquire text data from a reader. <br/><br/>
	 * Firefox this will be some html containing an "a" element with the "href" attribute linking to the to the PDF. <br/><br/>
	 * IE a simple one line String containing the URL will be returned
	 * @param r the reader to read from
	 * @return the text data from the reader
	 * @throws IOException
	 */
	private String readTextDate(Reader r) throws IOException {
		BufferedReader br = new BufferedReader(r);
		
		String textData = "";
		String line = br.readLine();
		while (line != null) {
			textData += line;
			line = br.readLine();
		}
		br.close();
		
		return textData;
	}

	/**
	 * Returns the URL held in the href attribute from an element
	 * @param element the element containing the href attribute
	 * @return the URL held in the href attribute
	 */
	private String getHrefAttribute(Element element) {
		NamedNodeMap attrs = element.getAttributes();
	
		Node nameNode = attrs.getNamedItem("href");
		if (nameNode != null) {
			return nameNode.getNodeValue();
		}
		
		return null;
	}
}