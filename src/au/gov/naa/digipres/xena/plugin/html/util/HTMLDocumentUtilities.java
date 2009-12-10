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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.ccil.cowan.tagsoup.Parser;
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

	// Used for debugging
	private static int OUTPUT_FILE_INDEX = 0;

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
	 * @throws ParserConfigurationException 
	 * @throws ParsingException 
	 * @throws ValidityException 
	 */
	public static Element getCleanHTMLDocument(final InputSource input, NormaliserManager normaliserManager) throws IOException, SAXException,
	        TransformerConfigurationException, ParserConfigurationException, ValidityException, ParsingException {

		// Initialise tag soup
		Parser tagsoupReader = new Parser();
		tagsoupReader.setFeature(Parser.ignoreBogonsFeature, true);
		tagsoupReader.setFeature(Parser.rootBogonsFeature, false);
		SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
		TransformerHandler writer = null;
		writer = tf.newTransformerHandler();
		ByteArrayOutputStream tagSoupOutput = new ByteArrayOutputStream();
		StreamResult streamResult = new StreamResult(tagSoupOutput);
		writer.setResult(streamResult);

		// Handle lexical events from TagSoup.
		// At present this will only pass through comment events.
		LexicalHandlerFilter lexicalFilter = new LexicalHandlerFilter(writer);
		tagsoupReader.setProperty(Parser.lexicalHandlerProperty, lexicalFilter);

		// Fix problems that may occur in certain documents
		ProblemHandlerFilter filter = new ProblemHandlerFilter();
		filter.setContentHandler(writer);
		tagsoupReader.setContentHandler(filter);

		// Parse the input
		tagsoupReader.parse(input);

		// This just helps with debugging - produces a temporary copy of the file which will be parsed by JDOM
		//		byte[] bytes = tagSoupOutput.toByteArray();
		//		File outFile = new File("/home/dpuser/xena_data/clean_destination/" + "temp_html_" + ++OUTPUT_FILE_INDEX + ".html");
		//		outFile.getParentFile().mkdirs();
		//		FileOutputStream outputFileStream = new FileOutputStream(outFile);
		//		outputFileStream.write(bytes);
		//		outputFileStream.flush();
		//		outputFileStream.close();
		// End debugging

		// Create a XOM Builder, passing in an XML Reader which will use the normaliser manager as an entity resolver.
		XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
		EntityResolver entityResolver = new EntityResolverImpl(normaliserManager);
		xmlReader.setEntityResolver(entityResolver);
		Builder sax = new Builder(xmlReader, false);

		// Real version
		ByteArrayInputStream builderIn = new ByteArrayInputStream(tagSoupOutput.toByteArray());
		Reader reader = new InputStreamReader(builderIn, "UTF-8");

		// Debugging version
		//		FileInputStream builderIn = new FileInputStream(outFile);
		//		Reader reader = new InputStreamReader(builderIn);
		// End debugging

		Document htmlDocument = sax.build(reader);
		Element rootElement = htmlDocument.getRootElement();

		return rootElement;
	}
}
