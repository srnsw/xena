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
 * @author Chris Bitmead
 * @author Justin Waddell
 */

package au.gov.naa.digipres.xena.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import nu.xom.converters.SAXConverter;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * General utility functions for converting between DOM and SAX xml streams.
 */
public class DOMUtil {
	/**
	 * Write an entire XML document to a SAX content handler given its root element.
	 * @param ch ContentHandler
	 * @param el JDOM Element
	 */
	public static void writeDocument(ContentHandler ch, Element el) throws SAXException {
		if (ch != null) {
			SAXConverter converter = new SAXConverter(ch);
			Nodes nodes = new Nodes(el);
			converter.convert(nodes);
		}
	}

	/**
	 * Write an XML fragment to a SAX content handler. Does not call startDocument
	 * and endDocument
	 * @param outputContentHandler ContentHandler
	 * @param el JDOM Element
	 */
	public static void writeElement(ContentHandler outputContentHandler, LexicalHandler outputLexicalHandler, Element el) throws SAXException {

		// Filter out start and end document events
		XMLFilterImpl contentHandlerProxy = new XMLFilterImpl() {
			@Override
			public void startDocument() {
				// Empty so we do not call start document on our handler
			}

			@Override
			public void endDocument() {
				// Empty so we do not call end document on our handler
			}
		};

		// Filter out everything except comment events
		LexicalHandler commentsOnlyHandler = new CommentsOnlyLexicalHandler(outputLexicalHandler);

		contentHandlerProxy.setContentHandler(outputContentHandler);
		SAXConverter converter = new SAXConverter(contentHandlerProxy);
		converter.setLexicalHandler(commentsOnlyHandler);
		Nodes nodes = new Nodes(el);
		converter.convert(nodes);
	}

	// /**
	// * Load some XML to a JDOM Element but first strip off the meta data wrapper.
	// * @param url URL
	// * @return Element
	// */
	// static public Element loadUnwrapXml(java.net.URL url) throws JDOMException, IOException, XenaException {
	// XMLFilter unwrapper = PluginManager.singleton().getMetaDataWrapperManager().getUnwrapNormaliser();
	// org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder();
	// builder.setXMLFilter(unwrapper);
	// return builder.build(url).detachRootElement();
	// }

	/**
	 * Call a normaliser to normalise some data and return the corresponding JDOM
	 * tree.
	 * @param normaliser XMLReader
	 * @param xis InputSource
	 * @return Element
	 */
	static public Element parseToElement(final XMLReader normaliser, InputSource xis) throws java.io.IOException, org.xml.sax.SAXException {
		ContentHandler oldHandler = normaliser.getContentHandler();

		Logger logger = Logger.getLogger(DOMUtil.class.getName());

		try {
			Builder documentBuilder = new Builder(normaliser);
			Document doc = documentBuilder.build(xis);
			return doc.getRootElement();
		} catch (ValidityException e) {
			logger.log(Level.FINER, "Normalisation Failed - source: " + xis.getSystemId(), e);
			throw new SAXException(xis.getSystemId(), e);
		} catch (ParsingException e) {
			logger.log(Level.FINER, "Normalisation Failed - source: " + xis.getSystemId(), e);
			throw new SAXException(xis.getSystemId(), e);
		} finally {
			normaliser.setContentHandler(oldHandler);
		}
	}

	private static class CommentsOnlyLexicalHandler implements LexicalHandler {

		LexicalHandler parent;

		public CommentsOnlyLexicalHandler(LexicalHandler parent) {
			this.parent = parent;
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ext.LexicalHandler#comment(char[], int, int)
		 */
		@Override
		public void comment(char[] ch, int start, int length) throws SAXException {
			parent.comment(ch, start, length);
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ext.LexicalHandler#endCDATA()
		 */
		@Override
		public void endCDATA() {
			// Do nothing
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ext.LexicalHandler#endDTD()
		 */
		@Override
		public void endDTD() {
			// Do nothing
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ext.LexicalHandler#endEntity(java.lang.String)
		 */
		@Override
		public void endEntity(String name) {
			// Do nothing
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ext.LexicalHandler#startCDATA()
		 */
		@Override
		public void startCDATA() {
			// Do nothing
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ext.LexicalHandler#startDTD(java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public void startDTD(String name, String publicId, String systemId) {
			// Do nothing
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ext.LexicalHandler#startEntity(java.lang.String)
		 */
		@Override
		public void startEntity(String name) {
			// Do nothing
		}

	}
}
