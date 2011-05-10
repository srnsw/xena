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

package au.gov.naa.digipres.xena.plugin.website;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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

import au.gov.naa.digipres.xena.core.ReleaseInfo;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.filenamer.AbstractFileNamer;
import au.gov.naa.digipres.xena.kernel.filenamer.FileNamerManager;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataWrapper;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.MetaDataWrapperManager;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.BinaryToXenaBinaryNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.util.FileUtils;

/**
 * Base class for normalising web sites. This normaliser is based on the ArchiveNormaliser, as a website is considered to be a collection of web site
 * files inside a Zip archive, with the zip file given an extension of ".wsx".
 * 
 * This normaliser simply produces an individual normalised file for each file inside the zip archive, and also produces an index of each original file
 * path to the name of the corresponding normalised file. This index will be used when viewing the normalised web site.
 * 
 * created 14/7/09
 * @author Justin Waddell
 * archive
 * Short desc of class:
 */
public class WebsiteNormaliser extends AbstractNormaliser {
	public final static String WEBSITE_PREFIX = "website";
	public final static String WEBSITE_TAG = "website";
	public final static String FILE_TAG = "file";
	public final static String FILE_ORIGINAL_PATH_ATTRIBUTE = "original_path";
	public final static String FILE_OUTPUT_FILENAME = "output_filename";
	public final static String WEBSITE_URI = "http://preservation.naa.gov.au/website/1.0";

	public final static String DATE_FORMAT_STRING = "yyyyMMdd'T'HHmmssZ";

	@Override
	public void parse(InputSource input, NormaliserResults results, boolean migrateOnly) throws SAXException, java.io.IOException {
		FileNamerManager fileNamerManager = normaliserManager.getPluginManager().getFileNamerManager();
		AbstractFileNamer fileNamer = fileNamerManager.getActiveFileNamer();
		MetaDataWrapperManager wrapperManager = normaliserManager.getPluginManager().getMetaDataWrapperManager();

		OutputStream entryOutputStream = null;
		ZipInputStream archiveStream = null;
		File tempStagingDir = null;
		String previousBasePath = null;
		boolean setNewBasePath = false;
		try {
			ContentHandler ch = getContentHandler();
			AttributesImpl att = new AttributesImpl();

			// Open the header element for the index XML
			ch.startElement(WEBSITE_URI, WEBSITE_TAG, WEBSITE_PREFIX + ":" + WEBSITE_TAG, att);

			// Create a ZipInputStream
			archiveStream = new ZipInputStream(input.getByteStream());

			// Create a temporary directory in which to extract the files
			tempStagingDir = File.createTempFile("website-extraction", "");
			tempStagingDir.delete();
			tempStagingDir.mkdirs();

			// Temporarily set the meta data wrapper base directory to the website-extraction directory.
			// This will allow the full website path to be stored in the source tag in the wrapper.
			// Remember the previous value of the base directory so we can revert once this website has been normalised.
			previousBasePath = wrapperManager.getBasePathName();
			wrapperManager.setBasePathName(tempStagingDir.getAbsolutePath());

			// Ensure that an exception being thrown does not mean that the previous base path is overwritten with null
			setNewBasePath = true;

			// Iterate through all the entries in this archive. We have reached the end when getNextEntry returns null.
			WebsiteEntry entry = getNextEntry(archiveStream, tempStagingDir);
			while (entry != null) {
				File tempFile = new File(entry.getFilename());
				XenaInputSource childXis = new XenaInputSource(tempFile);

				// Get the type and associated normaliser for this entry
				Type fileType = normaliserManager.getPluginManager().getGuesserManager().mostLikelyType(childXis);

				childXis.setType(fileType);
				AbstractNormaliser entryNormaliser = normaliserManager.lookup(fileType);

				// Generate a filename for the xena file representing this entry
				//File entryOutputFile = fileNamer.makeNewXenaFile(childXis, entryNormaliser);
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
						//entry = archiveHandler.getNextEntry();
						entry = getNextEntry(archiveStream, tempStagingDir);
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
					    normaliseWebsiteEntry(childXis, entryNormaliser, entryOutputFile, entryOutputStream, fileNamerManager, fileType, migrateOnly);
				} catch (Exception ex) {
					System.out.println("Normalisation of website file failed, switching to binary.\n" + ex);

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
					    normaliseWebsiteEntry(childXis, entryNormaliser, entryOutputFile, entryOutputStream, fileNamerManager, fileType, migrateOnly);
				} finally {
					// Always ensure we have closed the stream
					if (entryOutputStream != null) {
						entryOutputStream.close();
					}
				}
				results.addChildAIPResult(childResults);

				// Add the entry to the index XML

				// Make sure all path separators are unix-style
				String entryPath = entry.getName().replace('\\', '/');

				String entryOutputFilename = entryOutputFile.getName();
				AttributesImpl atts = new AttributesImpl();
				atts.addAttribute(WEBSITE_URI, FILE_ORIGINAL_PATH_ATTRIBUTE, WEBSITE_PREFIX + ":" + FILE_ORIGINAL_PATH_ATTRIBUTE, "CDATA", entryPath);
				atts.addAttribute(WEBSITE_URI, FILE_OUTPUT_FILENAME, WEBSITE_PREFIX + ":" + FILE_OUTPUT_FILENAME, "CDATA", entryOutputFilename);
				ch.startElement(WEBSITE_URI, FILE_TAG, WEBSITE_PREFIX + ":" + FILE_TAG, atts);
				ch.endElement(WEBSITE_URI, FILE_TAG, WEBSITE_PREFIX + ":" + FILE_TAG);

				// Delete entry's temp file
				tempFile.delete();

				// Next entry
				entry = getNextEntry(archiveStream, tempStagingDir);
			}

			// Close the header element for the index XML
			ch.endElement(WEBSITE_URI, WEBSITE_TAG, WEBSITE_PREFIX + ":" + WEBSITE_TAG);
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

			// Delete the temporary staging directory
			FileUtils.deleteDirAndContents(tempStagingDir);

			// Set the wrapper base path back to its previous value
			if (setNewBasePath) {
				wrapperManager.setBasePathName(previousBasePath);
			}
		}
	}

	private NormaliserResults normaliseWebsiteEntry(XenaInputSource childXis, AbstractNormaliser entryNormaliser, File entryOutputFile,
	                                                OutputStream entryOutputStream, FileNamerManager fileNamerManager, Type fileType,
	                                                boolean migrateOnly) throws TransformerConfigurationException, XenaException, SAXException,
	        IOException {
		// Set up the normaliser and wrapper for this entry
		SAXTransformerFactory transformFactory = (SAXTransformerFactory) TransformerFactory.newInstance();
		TransformerHandler transformerHandler = transformFactory.newTransformerHandler();
		entryNormaliser.setProperty("http://xena/url", childXis.getSystemId());
		//AbstractMetaDataWrapper wrapper = normaliserManager.getPluginManager().getMetaDataWrapperManager().getWrapNormaliser();
		AbstractMetaDataWrapper wrapper = null;

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

		wrapper.setContentHandler(transformerHandler);
		wrapper.setLexicalHandler(transformerHandler);
		wrapper.setParent(entryNormaliser);
		wrapper.setProperty("http://xena/input", childXis);
		wrapper.setProperty("http://xena/normaliser", entryNormaliser);
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
		childResults.setOutputFileName(entryOutputFile.getName());

		// Normalise the message
		normaliserManager.parse(entryNormaliser, childXis, wrapper, childResults, migrateOnly);

		// Populate the entry results, and link to the main results object
		childResults.setNormalised(true);
		childResults.setId(wrapper.getSourceId(new XenaInputSource(entryOutputFile)));
		return childResults;

	}

	@Override
	public String getVersion() {
		return ReleaseInfo.getVersion() + "b" + ReleaseInfo.getBuildNumber();
	}

	private WebsiteEntry getNextEntry(ZipInputStream zipStream, File tempStagingDir) throws IOException {
		boolean found = false;
		ZipEntry zipEntry;
		do {
			zipEntry = zipStream.getNextEntry();
			if (zipEntry == null) {
				return null;
			}
			if (!zipEntry.isDirectory()) {
				found = true;
			}
		} while (found == false);

		File entryTempFile = new File(tempStagingDir, zipEntry.getName());
		entryTempFile.getParentFile().mkdirs();
		entryTempFile.deleteOnExit();
		FileOutputStream tempFileOS = new FileOutputStream(entryTempFile);

		// 10k buffer
		byte[] readBuff = new byte[10 * 1024];
		int bytesRead = zipStream.read(readBuff);
		while (bytesRead > 0) {
			tempFileOS.write(readBuff, 0, bytesRead);
			bytesRead = zipStream.read(readBuff);
		}

		// Create ArchiveEntry object, using full path name of the entry
		WebsiteEntry archiveEntry = new WebsiteEntry(zipEntry.getName(), entryTempFile.getAbsolutePath());
		archiveEntry.setOriginalFileDate(new Date(zipEntry.getTime()));
		archiveEntry.setOriginalSize(zipEntry.getSize());
		return archiveEntry;

	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser#getName()
	 */
	@Override
	public String getName() {
		return "Website";
	}

	@Override
	public boolean isConvertible() {
		return true;
	}

	@Override
	public String getOutputFileExtension() {
		// Don't worry about the extension, we will not be using it
		return "wsx";
	}

}
