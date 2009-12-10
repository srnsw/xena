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
 * Created on 15/03/2007 justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.email;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser;

public class MailboxDeNormaliser extends AbstractDeNormaliser {
	private static final String MAILBOX_XSL_FILENAME = "mailbox.xsl";
	private static final String XSL_STYLESHEET_DATA = "type=\"text/xsl\" href=\"" + MAILBOX_XSL_FILENAME + "\"";
	private static final String MAILBOX_XSL_PATH = "au/gov/naa/digipres/xena/plugin/email/xsl/";

	private StringBuilder filenameBuilder;
	private TransformerHandler rootXMLWriter;
	private boolean inItem = false;
	private int messageCounter = 0;

	// If this mailbox is not in the root export directory, we need to make sure that the mailbox XSL file
	// end up in the same directory as the exported messages.
	// The outputDirectory field of AbstractDeNormaliser will point to the base export directory, while this field
	// will point to the directory where the mailbox is actually being exported (this might just be the base export
	// directory anyway).
	private File mailboxOutputDir;

	@Override
	public String getName() {
		return "Mailbox Denormaliser";
	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#getOutputFileExtension(au.gov.naa.digipres.xena.kernel.XenaInputSource)
	 */
	@Override
	public String getOutputFileExtension(XenaInputSource xis) {
		return "xml";
	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#characters(char[], int, int)
	 */
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (inItem) {
			filenameBuilder.append(ch, start, length);
		} else {
			rootXMLWriter.characters(ch, start, length);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#startDocument()
	 */
	@Override
	public void startDocument() throws SAXException {
		// Initialise mailbox output directory
		mailboxOutputDir = new File(outputDirectory, outputFilename).getParentFile();

		// Initialise the writer for the root XML file

		// create our transform handler
		SAXTransformerFactory transformFactory = (SAXTransformerFactory) TransformerFactory.newInstance();
		try {
			rootXMLWriter = transformFactory.newTransformerHandler();
			rootXMLWriter.setResult(streamResult);
			rootXMLWriter.startDocument();
			// Write xsl stylesheet link
			rootXMLWriter.processingInstruction("xml-stylesheet", XSL_STYLESHEET_DATA);

			// Copy stylesheet to output directory
			File xslFile = new File(mailboxOutputDir, MAILBOX_XSL_FILENAME);

			// No need to copy if it already exists...
			if (!xslFile.exists()) {
				InputStream xslInput = getClass().getClassLoader().getResourceAsStream(MAILBOX_XSL_PATH + MAILBOX_XSL_FILENAME);
				FileOutputStream xslOutput = new FileOutputStream(xslFile);

				// 10kB buffer
				byte[] buffer = new byte[10 * 1024];
				int bytesRead = xslInput.read(buffer);
				while (bytesRead > 0) {
					xslOutput.write(buffer, 0, bytesRead);
					bytesRead = xslInput.read(buffer);
				}
			}
		} catch (TransformerConfigurationException e) {
			throw new SAXException("Unable to create transformerHandler due to transformer configuration exception.");
		} catch (FileNotFoundException e) {
			throw new SAXException("Problem creating mailbox XSL stylesheet", e);
		} catch (IOException e) {
			throw new SAXException("Problem copying mailbox XSL stylesheet", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#endDocument()
	 */
	@Override
	public void endDocument() throws SAXException {
		rootXMLWriter.endDocument();
	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#startElement(java.lang.String,
	 * java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		if (qName.equals(EmailToXenaEmailNormaliser.MAILBOX_PREFIX + ":" + EmailToXenaEmailNormaliser.MAILBOX_ITEM_TAG)) {
			inItem = true;
			filenameBuilder = new StringBuilder();
		} else {
			rootXMLWriter.startElement(namespaceURI, localName, qName, atts);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#endElement(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
		if (qName.equals(EmailToXenaEmailNormaliser.MAILBOX_PREFIX + ":" + EmailToXenaEmailNormaliser.MAILBOX_ITEM_TAG)) {
			String messageFilename = filenameBuilder.toString();
			File messageFile = new File(sourceDirectory, messageFilename);
			if (messageFile.exists() && messageFile.isFile()) {
				messageCounter++;

				// outputFilename might be a path, so we just want to extract the file name itself.
				int lastSlashIndex = outputFilename.lastIndexOf('/');
				int lastBackslashIndex = outputFilename.lastIndexOf('\\');
				int lastPathSeparator = Math.max(lastSlashIndex, lastBackslashIndex);

				// If there is no path separator, this will still return the full filename.
				String messageExportFilename = messageCounter + "-" + outputFilename.substring(lastPathSeparator + 1);

				try {
					normaliserManager.export(new XenaInputSource(messageFile), mailboxOutputDir, messageExportFilename, true);

					// Write out link to exported message
					AttributesImpl atts = new AttributesImpl();
					rootXMLWriter.startElement(namespaceURI, localName, qName, atts);
					char[] exportFileChars = messageExportFilename.toCharArray();
					rootXMLWriter.characters(exportFileChars, 0, exportFileChars.length);
					rootXMLWriter.endElement(namespaceURI, localName, qName);
				} catch (Exception e) {
					throw new SAXException("Could not export normalised message - " + messageFilename);
				}
			}
		} else {
			rootXMLWriter.endElement(namespaceURI, localName, qName);
		}
	}

}
