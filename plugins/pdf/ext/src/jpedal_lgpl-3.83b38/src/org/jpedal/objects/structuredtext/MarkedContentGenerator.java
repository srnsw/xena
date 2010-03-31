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
* MarkedContentGenerator.java
* ---------------
*/
package org.jpedal.objects.structuredtext;

import org.jpedal.PdfDecoder;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.io.PdfReader;
import org.jpedal.objects.PageLookup;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.objects.raw.MCObject;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.utils.Strip;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.io.InputStream;

/**
 * extract as marked content
 */
public class MarkedContentGenerator {
	
	static final boolean XMLdebugFlag = false;
	private PdfObjectReader currentPdfFile;
	
	DocumentBuilder db=null;
	
	Document doc;
	
	Element root;
	
	PdfDecoder decode_pdf;

    Map classLookup=new HashMap(), roleLookup=new HashMap();

	Map pageStreams=new HashMap();
	private PdfObject structTreeRootObj;
	private PdfObject markInfoObj;
	
	/**
	 * main entry paint
	 */
	public Document getMarkedContentTree(PdfObjectReader currentPdfFile,PdfDecoder decode_pdf, PageLookup pageLookup) {

        return null;
        /**/

	}


	public void setRootValues(PdfObject structTreeRootObj, PdfObject markInfoObj) {
		
		this.structTreeRootObj=structTreeRootObj;
		this.markInfoObj=markInfoObj;
	
	}
}
