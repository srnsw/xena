package au.gov.naa.digipres.xena.helper;


import org.jdom.Element;
import org.jdom.input.SAXHandler;
import org.xml.sax.ContentHandler;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.view.XenaView;

/**
 *  Super-class for views which wish to manipulate the XML as a JDOM tree.
 *  Try not to use this class if you think the data is going to be large.
 *
 * @author     Chris Bitmead
 * @created    2 July 2002
 */
abstract public class JdomXenaView extends XenaView {
	protected Element element;

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

	public ContentHandler getContentHandler() throws XenaException {
		XmlContentHandlerSplitter splitter = new XmlContentHandlerSplitter();
		sh = new SAXHandler();
		splitter.addContentHandler(sh);
		splitter.addContentHandler(getTmpFileContentHandler());
		return splitter;
	}

	public void parse() throws java.io.IOException, org.xml.sax.SAXException, XenaException {
		if (sh != null) {
			setElement(sh.getDocument().getRootElement());
		}
		super.parse();
//		updateViewFromElement();
	}
}
