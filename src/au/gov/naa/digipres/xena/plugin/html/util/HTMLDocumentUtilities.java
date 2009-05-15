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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import au.gov.naa.digipres.xena.kernel.normalise.NormaliserManager;

/**
 * @author Justin Waddell
 *
 */
public class HTMLDocumentUtilities {

	/**
	 * Return a DOM representation of the HTML document in the given InputSource. If HTML document is not well-formed, 
	 * make any necessary changes in order to produce valid HTML.
	 * @param input
	 * @param normaliserManager
	 * @return
	 * @throws IOException
	 * @throws JDOMException
	 * @throws SAXException
	 * @throws TransformerConfigurationException
	 */
	public static Element getCleanHTMLDocument(final InputSource input, NormaliserManager normaliserManager) throws IOException, JDOMException,
	        SAXException, TransformerConfigurationException {
		XMLReader tagsoupReader = new org.ccil.cowan.tagsoup.Parser();
		String ignoreBogonsFeature = "http://www.ccil.org/~cowan/tagsoup/features/ignore-bogons";
		tagsoupReader.setFeature(ignoreBogonsFeature, true);
		SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
		TransformerHandler writer = null;
		writer = tf.newTransformerHandler();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		StreamResult streamResult = new StreamResult(out);
		writer.setResult(streamResult);

		// Use ContentTypeFilter to write out the content encoding meta tag, set to UTF-8
		ContentTypeFilter filter = new ContentTypeFilter();
		filter.setContentHandler(writer);

		tagsoupReader.setContentHandler(filter);
		tagsoupReader.parse(input);

		// This just helps with debugging - produces a temporary copy of the file which will be parsed by JDOM
		// byte[] bytes = out.toByteArray();
		// File outFile = new File("D:\\xena_data\\clean_destination\\" + "temp_html_" + ++OUTPUT_FILE_INDEX + ".html");
		// FileOutputStream outputFileStream = new FileOutputStream(outFile);
		// outputFileStream.write(bytes);
		// outputFileStream.flush();
		// outputFileStream.close();

		SAXBuilder sax = new SAXBuilder();
		sax.setValidation(false);

		EntityResolver entityResolver = new EntityResolverImpl(normaliserManager);
		sax.setEntityResolver(entityResolver);

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		Reader reader = new InputStreamReader(in, "UTF-8");

		// Debugging version
		// FileInputStream in = new FileInputStream(outFile);
		// Reader reader = new InputStreamReader(in, "UTF-8");

		return sax.build(reader).detachRootElement();
	}

}
