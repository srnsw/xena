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

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.NodeFactory;
import nu.xom.XOMHandler;

import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.view.XenaView;

/**
 *  Super-class for views which wish to manipulate the XML as a DOM tree.
 *  Try not to use this class if you think the data is going to be large.
 *
 * @created    2 July 2002
 */
abstract public class DOMXenaView extends XenaView {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected Element element;
	private Document document;
	protected XOMHandler documentBuilder;

	@Override
	public abstract void updateViewFromElement() throws XenaException;

	public Element getElement() {
		return element;
	}

	public void setElement(Element element) throws XenaException {
		this.element = element;
		updateViewFromElement();
	}

	@Override
	public ContentHandler getContentHandler() throws XenaException {
		// Create a temporary file which will contain the raw XML, and also produce a DOM tree using XOM.
		XmlContentHandlerSplitter splitter = new XmlContentHandlerSplitter();
		splitter.addContentHandler(getDocumentBuilder());
		splitter.addContentHandler(getTmpFileContentHandler());
		return splitter;
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.view.XenaView#getLexicalHandler()
	 */
	@Override
	public LexicalHandler getLexicalHandler() throws XenaException {
		XmlLexicalHandlerSplitter splitter = new XmlLexicalHandlerSplitter();
		splitter.addLexicalHandler(getDocumentBuilder());
		splitter.addLexicalHandler(getTmpFileContentHandler());
		return splitter;
	}

	/**
	 * @return the documentBuilder
	 */
	public XOMHandler getDocumentBuilder() {
		if (documentBuilder == null) {
			documentBuilder = new XOMHandler(new NodeFactory());
		}
		return documentBuilder;
	}

	@Override
	public void parse() throws java.io.IOException, org.xml.sax.SAXException, XenaException {

		// This method is called after the XIS has been parsed by the ContentHandler returned by getContentHandler.
		// This means that the document returned by our SaxHandler should be populated.

		if (documentBuilder != null) {
			document = documentBuilder.getDocument();
			setElement(document.getRootElement());
		}
		super.parse();
		// updateViewFromElement();
	}

	/**
	 * @return Returns the document.
	 */
	public Document getDocument() {
		return document;
	}
}
