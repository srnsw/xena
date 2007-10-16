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

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXHandler;
import org.xml.sax.ContentHandler;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.view.XenaView;

/**
 *  Super-class for views which wish to manipulate the XML as a JDOM tree.
 *  Try not to use this class if you think the data is going to be large.
 *
 * @created    2 July 2002
 */
abstract public class JdomXenaView extends XenaView {
	protected Element element;
	private Document document;

	@Override
    public void updateViewFromElement() throws XenaException {
	}

	public Element getElement() {
		return element;
	}

	public void setElement(Element element) throws XenaException {
		this.element = element;
		updateViewFromElement();
	}

	SAXHandler sh;

	@Override
    public ContentHandler getContentHandler() throws XenaException {
		XmlContentHandlerSplitter splitter = new XmlContentHandlerSplitter();
		sh = new SAXHandler();
		splitter.addContentHandler(sh);
		splitter.addContentHandler(getTmpFileContentHandler());
		return splitter;
	}

	@Override
    public void parse() throws java.io.IOException, org.xml.sax.SAXException, XenaException {
		if (sh != null) {
			document = sh.getDocument();
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
