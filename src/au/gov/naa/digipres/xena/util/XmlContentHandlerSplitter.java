/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Xena; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * @author Andrew Keeling
 * @author Dan Spasojevic
 * @author Justin Waddell
 */

package au.gov.naa.digipres.xena.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * A simple splitter Content handler that splits an incoming SAX xml stream into
 * an arbitrary number of outgoing streams for multiple processing. Use
 * addContentHandler to add an outgoing stream.
 */
public class XmlContentHandlerSplitter implements ContentHandler {
	Set<ContentHandler> contentHandlers = new HashSet<ContentHandler>();

	public void clearHandlers() {
		contentHandlers.clear();
	}

	public void addContentHandler(ContentHandler handler) {
		contentHandlers.add(handler);
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		Iterator it = contentHandlers.iterator();
		while (it.hasNext()) {
			ContentHandler handler = (ContentHandler) it.next();
			handler.characters(ch, start, length);
		}
	}

	public void endDocument() throws SAXException {
		Iterator it = contentHandlers.iterator();
		while (it.hasNext()) {
			ContentHandler handler = (ContentHandler) it.next();
			handler.endDocument();
		}
	}

	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
		Iterator it = contentHandlers.iterator();
		while (it.hasNext()) {
			ContentHandler handler = (ContentHandler) it.next();
			handler.endElement(namespaceURI, localName, qName);
		}
	}

	public void endPrefixMapping(String prefix) throws SAXException {
		Iterator it = contentHandlers.iterator();
		while (it.hasNext()) {
			ContentHandler handler = (ContentHandler) it.next();
			handler.endPrefixMapping(prefix);
		}
	}

	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		Iterator it = contentHandlers.iterator();
		while (it.hasNext()) {
			ContentHandler handler = (ContentHandler) it.next();
			handler.ignorableWhitespace(ch, start, length);
		}
	}

	public void processingInstruction(String target, String data) throws SAXException {
		Iterator it = contentHandlers.iterator();
		while (it.hasNext()) {
			ContentHandler handler = (ContentHandler) it.next();
			handler.processingInstruction(target, data);
		}
	}

	public void setDocumentLocator(Locator locator) {
		Iterator it = contentHandlers.iterator();
		while (it.hasNext()) {
			ContentHandler handler = (ContentHandler) it.next();
			handler.setDocumentLocator(locator);
		}
	}

	public void skippedEntity(String name) throws SAXException {
		Iterator it = contentHandlers.iterator();
		while (it.hasNext()) {
			ContentHandler handler = (ContentHandler) it.next();
			handler.skippedEntity(name);
		}
	}

	public void startDocument() throws SAXException {
		Iterator it = contentHandlers.iterator();
		while (it.hasNext()) {
			ContentHandler handler = (ContentHandler) it.next();
			handler.startDocument();
		}
	}

	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		Iterator it = contentHandlers.iterator();
		while (it.hasNext()) {
			ContentHandler handler = (ContentHandler) it.next();
			handler.startElement(namespaceURI, localName, qName, atts);
		}
	}

	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		Iterator it = contentHandlers.iterator();
		while (it.hasNext()) {
			ContentHandler handler = (ContentHandler) it.next();
			handler.startPrefixMapping(prefix, uri);
		}
	}
}
