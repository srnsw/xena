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

package au.gov.naa.digipres.xena.plugin.html;

import java.io.IOException;

import javax.xml.transform.TransformerConfigurationException;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.plugin.html.util.HTMLDocumentUtilities;
import au.gov.naa.digipres.xena.util.AbstractJdomNormaliser;

/**
 * Normaliser to convert HTML files into XHTML files. We rely on a couple of
 * external libraries configure to work together in the way that seems to have
 * proven most useful - TAGSOUP and JTIDY. TAGSOUP is a simple and well- written
 * HTML parser that does a good job of creating matching tags and so on. But it
 * falls down in not addressing the intricacies of XHTML and conversion. JTIDY
 * is a complex and kludgy tool to convert HTML into XHTML. However it tends to
 * be buggy and has lots of tricky edge conditions.
 * 
 * The fact is, converting HTML to XHMTL in a way that would allow it to
 * continue to render as it did originally is a very difficult if not impossible
 * mission. It may never be perfect, but if we spent a lot of time on it it may
 * be made a lot better than it is now.
 * 
 */
public class HtmlToXenaHtmlNormaliser extends AbstractJdomNormaliser {
	public HtmlToXenaHtmlNormaliser() {
		// Nothing to do
	}

	@Override
	public String getName() {
		return "HTML";
	}

	@Override
	public Element normalise(InputSource input) throws IOException, SAXException {
		Element rtn = null;
		try {
			rtn = HTMLDocumentUtilities.getCleanHTMLDocument(input, normaliserManager);
		} catch (TransformerConfigurationException x2) {
			throw new SAXException(x2);
		} catch (JDOMException x2) {
			throw new SAXException(x2);
		}

		return rtn;
	}

}
