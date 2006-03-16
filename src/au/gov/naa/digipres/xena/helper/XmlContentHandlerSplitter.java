package au.gov.naa.digipres.xena.helper;
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
	Set contentHandlers = new HashSet();
    


	public void addContentHandler(ContentHandler handler) {
		contentHandlers.add(handler);
	}

	public void characters(char[] ch, int start, int length) throws
		SAXException {
		Iterator it = contentHandlers.iterator();
		while (it.hasNext()) {
			ContentHandler handler = (ContentHandler)it.next();
			handler.characters(ch, start, length);
		}
	}

	public void endDocument() throws SAXException {
		Iterator it = contentHandlers.iterator();
		while (it.hasNext()) {
			ContentHandler handler = (ContentHandler)it.next();
			handler.endDocument();
		}
	}

	public void endElement(String namespaceURI, String localName, String qName) throws
		SAXException {
		Iterator it = contentHandlers.iterator();
		while (it.hasNext()) {
			ContentHandler handler = (ContentHandler)it.next();
			handler.endElement(namespaceURI, localName, qName);
		}
	}

	public void endPrefixMapping(String prefix) throws SAXException {
		Iterator it = contentHandlers.iterator();
		while (it.hasNext()) {
			ContentHandler handler = (ContentHandler)it.next();
			handler.endPrefixMapping(prefix);
		}
	}

	public void ignorableWhitespace(char[] ch, int start, int length) throws
		SAXException {
		Iterator it = contentHandlers.iterator();
		while (it.hasNext()) {
			ContentHandler handler = (ContentHandler)it.next();
			handler.ignorableWhitespace(ch, start, length);
		}
	}

	public void processingInstruction(String target, String data) throws
		SAXException {
		Iterator it = contentHandlers.iterator();
		while (it.hasNext()) {
			ContentHandler handler = (ContentHandler)it.next();
			handler.processingInstruction(target, data);
		}
	}

	public void setDocumentLocator(Locator locator) {
		Iterator it = contentHandlers.iterator();
		while (it.hasNext()) {
			ContentHandler handler = (ContentHandler)it.next();
			handler.setDocumentLocator(locator);
		}
	}

	public void skippedEntity(String name) throws SAXException {
		Iterator it = contentHandlers.iterator();
		while (it.hasNext()) {
			ContentHandler handler = (ContentHandler)it.next();
			handler.skippedEntity(name);
		}
	}

	public void startDocument() throws SAXException {
		Iterator it = contentHandlers.iterator();
		while (it.hasNext()) {
			ContentHandler handler = (ContentHandler)it.next();
			handler.startDocument();
		}
	}

	public void startElement(String namespaceURI, String localName,
							 String qName, Attributes atts) throws SAXException {
		Iterator it = contentHandlers.iterator();
		while (it.hasNext()) {
			ContentHandler handler = (ContentHandler)it.next();
			handler.startElement(namespaceURI, localName, qName, atts);
		}
	}

	public void startPrefixMapping(String prefix, String uri) throws
		SAXException {
		Iterator it = contentHandlers.iterator();
		while (it.hasNext()) {
			ContentHandler handler = (ContentHandler)it.next();
			handler.startPrefixMapping(prefix, uri);
		}
	}
}
