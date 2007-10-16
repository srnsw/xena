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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.SAXOutputter;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.kernel.XenaException;

/**
 * General utility functions for converting between JDOM and SAX xml streams.
 */
public class JdomUtil {
	/**
	 * Write an entire XML document to a SAX content handler given its root element.
	 * @param ch ContentHandler
	 * @param el JDOM Element
	 */
	public static void writeDocument(org.xml.sax.ContentHandler ch, Element el) throws SAXException, JDOMException {
		if (ch != null) {
			SAXOutputter output = new SAXOutputter(ch);
			List<Element> lst = new ArrayList<Element>();
			lst.add(el);
			output.output(lst);
		}
	}

	/**
	 * Write an XML fragment to a SAX content handler. Does not call startDocument
	 * and endDocument
	 * @param ch ContentHandler
	 * @param el JDOM Element
	 */
	public static void writeElement(org.xml.sax.ContentHandler ch, Element el) throws SAXException, JDOMException {
		// This hack is because the silly JDOM SAXOutputter lacks an
		// output(Element) method that doesn't call startDocument.
		// I'm trying to convince those folks to repent.
		XMLFilterImpl proxy = new XMLFilterImpl() {
			@Override
            public void startDocument() throws SAXException {
			}

			@Override
            public void endDocument() throws SAXException {
			}
		};
		proxy.setContentHandler(ch);
		SAXOutputter output = new SAXOutputter(proxy);
		List<Element> lst = new ArrayList<Element>();
		lst.add(el);
		output.output(lst);
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
	 * Load some XML to a JDOM Element.
	 * @param url URL
	 * @return Element
	 */
	static public Element loadXml(java.net.URL url) throws JDOMException, IOException, XenaException {
		org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder();
		return builder.build(url).detachRootElement();
	}

	/**
	 * Call a normaliser to normalise some data and return the corresponding JDOM
	 * tree.
	 * @param normaliser XMLReader
	 * @param xis InputSource
	 * @return Element
	 */
	static public Element parseToElement(final XMLReader normaliser, InputSource xis) throws java.io.IOException, org.xml.sax.SAXException {
		ContentHandler oldHandler = normaliser.getContentHandler();

		Logger logger = Logger.getLogger(JdomUtil.class.getName());

		try {
			org.jdom.input.SAXBuilder sb = new org.jdom.input.SAXBuilder() {
				@Override
                protected XMLReader createParser() throws JDOMException {
					return normaliser;
				}
			};
			Element rtn = sb.build(xis).detachRootElement();
			// String msg = log.logMsg(XenaLog.LOG_OK, xis.getSystemId(), normaliser,
			// "Processed by " + normaliser.toString(), null);
			return rtn;
		} catch (JDOMException x) {
			logger.log(Level.FINER, "Normalisation Failed - source: " + xis.getSystemId(), x);
			throw new SAXException(xis == null ? "No URL" : xis.getSystemId(), x);
		} finally {
			normaliser.setContentHandler(oldHandler);
		}
	}
}
