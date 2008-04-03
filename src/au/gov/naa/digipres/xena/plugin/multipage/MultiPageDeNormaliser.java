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

/*
 * Created on 08/03/2007 justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.multipage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser;
import au.gov.naa.digipres.xena.util.XmlDeNormaliser;

/**
 * DeNormaliser for multipage xena files. Writes out the XML for each top-level "page" element to temporary files.
 * These temporary files are then exported themselves, and an index HTML page is created which will contain links
 * to the exported page files.
 * 
 * created 08/03/2007
 * multipage
 * Short desc of class:
 */
public class MultiPageDeNormaliser extends AbstractDeNormaliser {
	private static final String XHTML_URI = "http://www.w3.org/1999/xhtml";
	private static final String TITLE_TEXT = "Xena Multipage Export Index";
	private static final String HEADER_TEXT = "The following files were exported from a Xena Multipage file:";

	private TransformerHandler pageXMLWriter;
	private TransformerHandler indexXMLWriter;
	private File tempPageFile;
	private OutputStream pageOutputStream;
	private int multipageLevel = 0;
	private int pageIndex = 1;

	@Override
	public String getName() {
		return "MultiPage DeNormaliser";
	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#getOutputFileExtension(au.gov.naa.digipres.xena.kernel.XenaInputSource)
	 */
	@Override
	public String getOutputFileExtension(XenaInputSource xis) throws XenaException {
		return "html";
	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#startDocument()
	 */
	@Override
	public void startDocument() throws SAXException {
		// Initialise the writer for the HTML index file, and write out the index file header.

		// create our transform handler
		SAXTransformerFactory transformFactory = (SAXTransformerFactory) TransformerFactory.newInstance();
		try {
			indexXMLWriter = transformFactory.newTransformerHandler();
			indexXMLWriter.setResult(streamResult);
			indexXMLWriter.startDocument();
		} catch (TransformerConfigurationException e) {
			throw new SAXException("Unable to create transformerHandler due to transformer configuration exception.");
		}

		// Index header
		Attributes atts = new AttributesImpl();

		indexXMLWriter.startElement(XHTML_URI, "html", "html", atts);
		indexXMLWriter.startElement(XHTML_URI, "head", "head", atts);
		indexXMLWriter.startElement(XHTML_URI, "title", "title", atts);
		char[] charArr = TITLE_TEXT.toCharArray();
		indexXMLWriter.characters(charArr, 0, charArr.length);
		indexXMLWriter.endElement(XHTML_URI, "title", "title");
		indexXMLWriter.endElement(XHTML_URI, "head", "head");

		indexXMLWriter.startElement(XHTML_URI, "body", "body", atts);

		indexXMLWriter.startElement(XHTML_URI, "p", "p", atts);
		charArr = HEADER_TEXT.toCharArray();
		indexXMLWriter.characters(charArr, 0, charArr.length);
		indexXMLWriter.endElement(XHTML_URI, "p", "p");

		indexXMLWriter.startElement(XHTML_URI, "p", "p", atts);
	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#endDocument()
	 */
	@Override
	public void endDocument() throws SAXException {
		// Write the index HTML footer, and end the document.
		indexXMLWriter.endElement(XHTML_URI, "p", "p");
		indexXMLWriter.endElement(XHTML_URI, "body", "body");
		indexXMLWriter.endElement(XHTML_URI, "html", "html");
		indexXMLWriter.endDocument();
	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#endElement(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
		// Check for the end of the current page
		if (qName.equalsIgnoreCase(MultiPageNormaliser.MULTIPAGE_PREFIX + ":" + MultiPageNormaliser.PAGE_TAG)) {
			// Make sure we are at the top level
			multipageLevel--;
			if (multipageLevel == 0) {
				try {
					// Close writers
					pageXMLWriter.endDocument();
					pageOutputStream.flush();
					pageOutputStream.close();
				} catch (IOException e) {
					throw new SAXException("Problem closing output stream.", e);
				}

				// Export current page, using the temporary xml file we have created
				try {
					XenaInputSource xis = new XenaInputSource(tempPageFile);

					// get the unwrapper for this package. This will most likely be empty as multipage does
					// not currently wrap the pages it contains separately, however you never know what the
					// future will hold... use the EmptyWrapper if we can't find a wrapper.
					XMLFilter unwrapper;
					try {
						unwrapper = normaliserManager.getPluginManager().getMetaDataWrapperManager().getUnwrapper(xis);
					} catch (XenaException xe) {
						unwrapper = normaliserManager.getPluginManager().getMetaDataWrapperManager().getEmptyWrapper().getUnwrapper();
					}

					// Get the appropriate denormaliser
					String tag = normaliserManager.unwrapGetTag(xis, unwrapper);
					AbstractDeNormaliser deNormaliser = normaliserManager.lookupDeNormaliser(tag);
					if (deNormaliser == null) {
						// Just use basic XML denormaliser
						deNormaliser = new XmlDeNormaliser();
					}

					// Construct output file name
					String outputFileExtension = deNormaliser.getOutputFileExtension(xis);
					String pageOutputFilename = pageIndex + "-" + getOutputFilename();

					/*
					 * This code adds the extension that the type gives us _if_ the name given to us by the meta data
					 * wrapper does not have the same extension. This could happen in a number of situations, most
					 * notably, for the plaintext, the default extension is txt, however many file extensions are valid
					 * text files. at the end of the day, this will at least reduce the instances of simple.txt ->
					 * simple.txt.txt and the like, and still give a reasonable indication of what is actually in the
					 * file.
					 * 
					 */
					if (outputFileExtension != null) {
						if (!pageOutputFilename.toLowerCase().endsWith("." + outputFileExtension.toLowerCase())) {
							pageOutputFilename = pageOutputFilename + "." + outputFileExtension;
						}
					}

					// Export file
					normaliserManager.export(xis, outputDirectory, pageOutputFilename, true);
					pageIndex++;

					// Add entry to HTML index
					AttributesImpl atts = new AttributesImpl();
					atts.addAttribute(XHTML_URI, "href", "href", "CDATA", pageOutputFilename);
					indexXMLWriter.startElement(XHTML_URI, "a", "a", atts);
					char[] charArr = pageOutputFilename.toCharArray();
					indexXMLWriter.characters(charArr, 0, charArr.length);
					indexXMLWriter.endElement(XHTML_URI, "a", "a");
					atts = new AttributesImpl();
					indexXMLWriter.startElement(XHTML_URI, "br", "br", atts);
					indexXMLWriter.endElement(XHTML_URI, "br", "br");

					// We've finished with the temp file
					tempPageFile.delete();
				} catch (Exception ex) {
					throw new SAXException("Problem exporting a multipage page.", ex);
				}
			} else {
				// Write out any embedded page tags
				pageXMLWriter.endElement(namespaceURI, localName, qName);
			}
		} else if (multipageLevel > 0) {
			// If we are processing a page, then we write out all elements.
			pageXMLWriter.endElement(namespaceURI, localName, qName);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#startElement(java.lang.String,
	 * java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		// Check for the start of a new page
		if (qName.equalsIgnoreCase(MultiPageNormaliser.MULTIPAGE_PREFIX + ":" + MultiPageNormaliser.PAGE_TAG)) {
			// It's possible that a multipage page could contain another multipage item, so we need to make sure
			// we only export the top-level multipage pages
			multipageLevel++;
			if (multipageLevel == 1) {
				// create our transform handler
				pageXMLWriter = null;
				SAXTransformerFactory transformFactory = (SAXTransformerFactory) TransformerFactory.newInstance();
				try {
					pageXMLWriter = transformFactory.newTransformerHandler();
				} catch (TransformerConfigurationException e) {
					throw new SAXException("Unable to create transformerHandler due to transformer configuration exception.");
				}

				try {
					// Create a temporary file from which we can export. I can't come up with a better way to do this
					// unfortunately...
					tempPageFile = File.createTempFile("multipage", ".tmp");
					tempPageFile.deleteOnExit();
					pageOutputStream = new FileOutputStream(tempPageFile);
					OutputStreamWriter outputStreamWriter = new OutputStreamWriter(pageOutputStream);
					StreamResult tempFileResult = new StreamResult(pageOutputStream);
					tempFileResult.setWriter(outputStreamWriter);

					pageXMLWriter.setResult(tempFileResult);
					pageXMLWriter.startDocument();
				} catch (IOException iex) {
					throw new SAXException("Problem creating temporary output.", iex);
				}
			} else {
				// Write out any embedded page tags
				pageXMLWriter.startElement(namespaceURI, localName, qName, atts);
			}
		} else if (multipageLevel > 0) {
			// If we are processing a page, then we write out all elements.
			pageXMLWriter.startElement(namespaceURI, localName, qName, atts);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#characters(char[], int, int)
	 */
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (multipageLevel > 0) {
			// If we are processing a page, then we write out all elements.
			pageXMLWriter.characters(ch, start, length);
		}
	}

}
