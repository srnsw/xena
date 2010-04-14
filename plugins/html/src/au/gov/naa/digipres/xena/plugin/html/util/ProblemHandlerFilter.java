/**
 * This file is part of html.
 * 
 * html is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * html is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with html; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package au.gov.naa.digipres.xena.plugin.html.util;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * @author Justin Waddell
 *
 */
/**
 * This class is used to fix certain problems by filtering the HTML elements we are writing to the output XML.
 * <p>
 * There are two problems we are interested in. 
 * <p>
 * The first is a problem with the character encoding in Internet Explorer.
 * Specifically we are interested in the META elements. We want to write out the "Content-Type" meta tag and set it to UTF-8,
 * as Internet Explorer will not automatically detect the encoding being used, and uses Western encoding by default, and will
 * thus display weird characters (eg the nbsp character 160). We will write out this element whenever we have written the "head"
 * element. However we also need to make sure that we don't duplicate the Content-Type meta element if this tag already existed
 * in the original file.
 * <p>
 * The second problem is caused by a combination of the Tag Soup HTML formatter, and the Apache XML Serializer. 
 * Certain HTML elements with no content between the start and end tags are passed through directly by Tag Soup, 
 * but the Serializer interprets this as an empty tag, and writes it out as a single &lt;script /&gt; tag. 
 * Unfortunately the single tag is not valid XML, and the page is not rendered correctly.
 * This is fixed by writing out an EOL character at the end of every &lt;script&gt; tag, thus forcing an end element tag.
 * 
 * <p>  
 * created 27/04/2007
 * html
 * Short desc of class:
 */
public class ProblemHandlerFilter extends XMLFilterImpl {
	private static final String CONTENT_TYPE_ATT_VALUE = "Content-Type";
	private static final String CONTENT_TYPE_ATT_NAME = "content";
	private static final String HTTP_EQUIV_ATT_NAME = "http-equiv";

	private boolean writtenContentHeader = false;
	private boolean skipNextMetaClose = false;
	private boolean seenHTMLOpen = false;
	private boolean seenHTMLClose = false;

	private String htmlURI = "";

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.XMLFilterImpl#startElement(java.lang.String, java.lang.String, java.lang.String,
	 * org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		// We always want HTML to be the root element.
		// If the element is not HTML and we have not yet seen the HTML, write out an HTML tag.
		if (localName.equalsIgnoreCase("html")) {
			seenHTMLOpen = true;
		} else {
			if (!seenHTMLOpen) {
				AttributesImpl htmlAtts = new AttributesImpl();
				htmlURI = uri;
				startElement(htmlURI, "html", "html", htmlAtts);
				seenHTMLOpen = true;
			}
		}

		if (localName.equalsIgnoreCase("head")) {
			super.startElement(uri, localName, qName, atts);

			// Write content-encoding element
			AttributesImpl metaAtts = new AttributesImpl();
			metaAtts.addAttribute(uri, HTTP_EQUIV_ATT_NAME, HTTP_EQUIV_ATT_NAME, "CDATA", CONTENT_TYPE_ATT_VALUE);
			metaAtts.addAttribute(uri, CONTENT_TYPE_ATT_NAME, CONTENT_TYPE_ATT_NAME, "CDATA", "text/html; charset=UTF-8");
			startElement(uri, "meta", "meta", metaAtts);
			endElement(uri, "meta", "meta");
		} else if (localName.equalsIgnoreCase("meta")) {
			String metaType = atts.getValue(HTTP_EQUIV_ATT_NAME);
			if (metaType != null && metaType.equalsIgnoreCase(CONTENT_TYPE_ATT_VALUE)) {
				// This the content-encoding element.
				if (!writtenContentHeader) {
					// We haven't already written the content-encoding header
					super.startElement(uri, localName, qName, atts);
					writtenContentHeader = true;
				} else {
					// If we have already written the content-encoding header, we don't want to write it out again.
					skipNextMetaClose = true;
				}
			} else {
				// A non-content encoding meta element is written as normal
				super.startElement(uri, localName, qName, atts);
			}
		} else {
			// A non-meta element is written as normal
			super.startElement(uri, localName, qName, atts);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.XMLFilterImpl#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (localName.equalsIgnoreCase("meta")) {
			if (!skipNextMetaClose) {
				// We haven't already written the content-encoding header
				super.endElement(uri, localName, qName);
			} else {
				// Only skip closing one meta element
				skipNextMetaClose = false;
			}
		} else if (localName.equalsIgnoreCase("script") || localName.equalsIgnoreCase("a")) {
			// Output an end-of-line char. It will force the serializer to write out an end tag, 
			// but will be ignored by HTML renderers.
			char[] eolChars = {'\n'};
			characters(eolChars, 0, eolChars.length);

			super.endElement(uri, localName, qName);
		} else if (localName.equalsIgnoreCase("html")) {
			seenHTMLClose = true;
			super.endElement(uri, localName, qName);
		} else {
			super.endElement(uri, localName, qName);
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.XMLFilterImpl#endDocument()
	 */
	@Override
	public void endDocument() throws SAXException {
		if (!seenHTMLClose) {
			endElement(htmlURI, "html", "html");
		}
		super.endDocument();
	}

}
