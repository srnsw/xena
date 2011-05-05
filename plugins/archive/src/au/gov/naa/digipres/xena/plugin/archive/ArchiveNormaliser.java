/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
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
 * @author Jeff Stiff
 */

package au.gov.naa.digipres.xena.plugin.archive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.filenamer.AbstractFileNamer;
import au.gov.naa.digipres.xena.kernel.filenamer.FileNamerManager;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataWrapper;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.BinaryToXenaBinaryNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * Base class for normalising archives. An extension of this class will need to be created for each type of archive (eg Zip, GZip, 7z, RAR etc),
 * implementing the getArchiveHandler method. This method should return an instance of an ArchiveHandler implementation, which will allow
 * this class to retrieve entries in the archive one by one.
 * created 28/03/2007
 * archive
 * Short desc of class:
 */
public abstract class ArchiveNormaliser extends AbstractNormaliser {
	public final static String ARCHIVE_PREFIX = "archive";
	public final static String ARCHIVE_TAG = "archive";
	public final static String ENTRY_TAG = "entry";
	public final static String ENTRY_ORIGINAL_PATH_ATTRIBUTE = "original_path";
	public final static String ENTRY_ORIGINAL_FILE_DATE_ATTRIBUTE = "original_file_date";
	public final static String ENTRY_ORIGINAL_SIZE_ATTRIBUTE = "original_size";
	public final static String ENTRY_OUTPUT_FILENAME = "output_filename";
	public final static String ARCHIVE_URI = "http://preservation.naa.gov.au/archive/1.0";

	public final static String DATE_FORMAT_STRING = "yyyyMMdd'T'HHmmssZ";

	@Override
	public void parse(InputSource input, NormaliserResults results, boolean migrateOnly) throws SAXException, java.io.IOException {
		FileNamerManager fileNamerManager = normaliserManager.getPluginManager().getFileNamerManager();
		AbstractFileNamer fileNamer = fileNamerManager.getActiveFileNamer();

		OutputStream entryOutputStream = null;
		InputStream archiveStream = null;
		try {
			ContentHandler ch = getContentHandler();
			AttributesImpl att = new AttributesImpl();

			// Open the header element for the index XML
			ch.startElement(ARCHIVE_URI, ARCHIVE_TAG, ARCHIVE_PREFIX + ":" + ARCHIVE_TAG, att);

			// Retrieve a reference to a concrete ArchiveHandler
			archiveStream = input.getByteStream();
			ArchiveHandler archiveHandler = getArchiveHandler(archiveStream);

			// Iterate through all the entries in this archive. We have reached the end when getNextEntry returns null.
			ArchiveEntry entry = archiveHandler.getNextEntry();
			while (entry != null) {
				File tempFile = new File(entry.getFilename());
				XenaInputSource childXis = new XenaInputSource(tempFile);

				// Get the type and associated normaliser for this entry
				Type fileType = normaliserManager.getPluginManager().getGuesserManager().mostLikelyType(childXis);

				childXis.setType(fileType);
				AbstractNormaliser entryNormaliser = normaliserManager.lookup(fileType);

				File entryOutputFile;

				if (migrateOnly) {

					// Check to see if this file gets converted or passed straight through
					if (entryNormaliser.isConvertible()) {
						// File type does get converted, continue with migration routine
						// Create the Open Format file
						entryOutputFile = fileNamer.makeNewOpenFile(childXis, entryNormaliser);
					} else {
						// File type does not get converted, don't migrate, just skip to the next entry in the archive
						// Delete entry's temp file
						tempFile.delete();

						// Next entry
						entry = archiveHandler.getNextEntry();
						continue;
					}
				} else {
					// Create the Xena output file
					entryOutputFile = fileNamer.makeNewXenaFile(childXis, entryNormaliser);
				}

				childXis.setOutputFileName(entryOutputFile.getName());

				// Normalise the entry
				NormaliserResults childResults;
				try {
					entryOutputStream = new FileOutputStream(entryOutputFile);
					childResults =
					    normaliseArchiveEntry(childXis, entryNormaliser, entryOutputFile, entryOutputStream, fileNamerManager, fileType, migrateOnly);
				} catch (Exception ex) {
					System.out.println("Normalisation of archive entry failed, switching to binary.\n" + ex);

					// Remove any output
					if (entryOutputFile.exists()) {
						entryOutputFile.delete();
					}

					// Normalisation has failed - just binary normalise this entry
					entryNormaliser = normaliserManager.lookup(BinaryToXenaBinaryNormaliser.BINARY_NORMALISER_NAME);

					// Generate a filename for the xena file representing this entry
					entryOutputFile = fileNamer.makeNewXenaFile(childXis, entryNormaliser);
					childXis.setOutputFileName(entryOutputFile.getName());
					entryOutputStream = new FileOutputStream(entryOutputFile);
					childResults =
					    normaliseArchiveEntry(childXis, entryNormaliser, entryOutputFile, entryOutputStream, fileNamerManager, fileType, migrateOnly);
				} finally {
					// Always ensure we have closed the stream
					if (entryOutputStream != null) {
						entryOutputStream.close();
					}
				}
				results.addChildAIPResult(childResults);

				// Add the entry to the index XML
				String entryOutputFilename = entryOutputFile.getName();
				AttributesImpl atts = new AttributesImpl();
				atts.addAttribute(ARCHIVE_URI, ENTRY_ORIGINAL_PATH_ATTRIBUTE, ARCHIVE_PREFIX + ":" + ENTRY_ORIGINAL_PATH_ATTRIBUTE, "CDATA",
				                  entry.getName());

				// Make sure date is formatted correctly
				SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT_STRING);
				atts.addAttribute(ARCHIVE_URI, ENTRY_ORIGINAL_FILE_DATE_ATTRIBUTE, ARCHIVE_PREFIX + ":" + ENTRY_ORIGINAL_FILE_DATE_ATTRIBUTE,
				                  "CDATA", formatter.format(entry.getOriginalFileDate()));

				atts.addAttribute(ARCHIVE_URI, ENTRY_ORIGINAL_SIZE_ATTRIBUTE, ARCHIVE_PREFIX + ":" + ENTRY_ORIGINAL_SIZE_ATTRIBUTE, "CDATA",
				                  String.valueOf(entry.getOriginalSize()));
				atts.addAttribute(ARCHIVE_URI, ENTRY_OUTPUT_FILENAME, ARCHIVE_PREFIX + ":" + ENTRY_OUTPUT_FILENAME, "CDATA", entryOutputFilename);
				ch.startElement(ARCHIVE_URI, ENTRY_TAG, ARCHIVE_PREFIX + ":" + ENTRY_TAG, atts);
				ch.endElement(ARCHIVE_URI, ENTRY_TAG, ARCHIVE_PREFIX + ":" + ENTRY_TAG);

				// Delete entry's temp file
				tempFile.delete();

				// Next entry
				entry = archiveHandler.getNextEntry();

				// Check if this is an archive file within an archive
				if (normaliserManager.isArchiveFile(fileType.toString()) && migrateOnly) {
					// Delete the output file.
					entryOutputFile.delete();
				}
			}

			// Close the header element for the index XML
			ch.endElement(ARCHIVE_URI, ARCHIVE_TAG, ARCHIVE_PREFIX + ":" + ARCHIVE_TAG);
		} catch (XenaException x) {
			throw new SAXException("Problem parseing Xena file", x);
		} catch (TransformerException e) {
			throw new SAXException("Problem creating XML transformer", e);
		} finally {
			// Always ensure we have closed the streams
			if (entryOutputStream != null) {
				entryOutputStream.close();
			}

			if (archiveStream != null) {
				archiveStream.close();
			}
		}
	}

	private NormaliserResults normaliseArchiveEntry(XenaInputSource childXis, AbstractNormaliser entryNormaliser, File entryOutputFile,
	                                                OutputStream entryOutputStream, FileNamerManager fileNamerManager, Type fileType,
	                                                boolean migrateOnly) throws TransformerConfigurationException, XenaException, SAXException,
	        IOException {
		// Set up the normaliser and wrapper for this entry
		SAXTransformerFactory transformFactory = (SAXTransformerFactory) TransformerFactory.newInstance();
		TransformerHandler transformerHandler = transformFactory.newTransformerHandler();
		entryNormaliser.setProperty("http://xena/url", childXis.getSystemId());
		//AbstractMetaDataWrapper wrapper = normaliserManager.getPluginManager().getMetaDataWrapperManager().getWrapNormaliser();
		AbstractMetaDataWrapper wrapper = null;

		// Set up the wrappers defaults by the Normaliser Manager. 
		//wrapper = getNormaliserManager().wrapTheNormaliser(entryNormaliser, childXis, wrapper);

		if (migrateOnly) {
			// Create an emptyWrapper
			wrapper = normaliserManager.getPluginManager().getMetaDataWrapperManager().getEmptyWrapper().getWrapper();
			transformerHandler.getTransformer().setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		} else {
			// Get the correct wrapper
			wrapper = normaliserManager.getPluginManager().getMetaDataWrapperManager().getWrapNormaliser();
			// Set up the wrappers defaults by the Normaliser Manager. 
			wrapper = getNormaliserManager().wrapTheNormaliser(entryNormaliser, childXis, wrapper);
		}

		// Now change some of the defaults. 
		wrapper.setContentHandler(transformerHandler);
		wrapper.setLexicalHandler(transformerHandler);
		wrapper.setParent(entryNormaliser);
		entryNormaliser.setContentHandler(wrapper);
		entryNormaliser.setLexicalHandler(wrapper);
		entryNormaliser.setProperty("http://xena/file", entryOutputFile);
		entryNormaliser.setProperty("http://xena/normaliser", entryNormaliser);

		// Create the output file, and link the normaliser to it
		OutputStreamWriter osw = new OutputStreamWriter(entryOutputStream, "UTF-8");
		StreamResult streamResult = new StreamResult(osw);
		transformerHandler.setResult(streamResult);

		// Create a NormaliserResults object for this entry, which is used to link Xena files together
		NormaliserResults childResults =
		    new NormaliserResults(childXis, entryNormaliser, fileNamerManager.getDestinationDir(), fileNamerManager.getActiveFileNamer(), wrapper);
		childResults.setInputType(fileType);

		// Normalise the message
		normaliserManager.parse(entryNormaliser, childXis, wrapper, childResults, migrateOnly);

		// Populate the entry results, and link to the main results object
		childResults.setOutputFileName(entryOutputFile.getName());
		childResults.setNormalised(true);
		childResults.setId(wrapper.getSourceId(new XenaInputSource(entryOutputFile)));
		return childResults;

	}

	@Override
	public String getVersion() {
		return ReleaseInfo.getVersion() + "b" + ReleaseInfo.getBuildNumber();
	}

	protected abstract ArchiveHandler getArchiveHandler(InputStream archiveStream);
}
