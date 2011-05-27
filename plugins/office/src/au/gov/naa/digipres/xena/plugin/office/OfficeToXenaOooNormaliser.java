/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Xena; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * @author Andrew Keeling
 * @author Chris Bitmead
 * @author Justin Waddell
 * @auther Matthew Oliver
 * @author Jeff Stiff
 */

package au.gov.naa.digipres.xena.plugin.office;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipException;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.util.FileUtils;
import au.gov.naa.digipres.xena.util.InputStreamEncoder;

/*
 * Convert office documents to the Xena office (i.e. OpenOffice.org flat) file format.
 */
public class OfficeToXenaOooNormaliser extends AbstractNormaliser {

	public final static String OPEN_DOCUMENT_PREFIX = "opendocument";
	private final static String OPEN_DOCUMENT_URI = "http://preservation.naa.gov.au/odf/1.0";
	public final static String DOCUMENT_TYPE_TAG_NAME = "type";
	public final static String DOCUMENT_EXTENSION_TAG_NAME = "extension";
	public final static String PROCESS_DESCRIPTION_TAG_NAME = "description";

	private final static String DESCRIPTION =
	    "The following data is a MIME-compliant (RFC 1421) PEM base64 (RFC 1421) representation of an Open Document Format "
	            + "(ISO 26300, Version 1.0) document, produced by Open Office version 2.0.";

	public OfficeToXenaOooNormaliser() {
		// Nothing to do
	}

	@Override
	public String getName() {
		return "Office";
	}

	@Override
	public void parse(InputSource input, NormaliserResults results, boolean migrateOnly) throws SAXException, IOException {

		XenaInputSource xis = (XenaInputSource) input;
		Type type = xis.getType();

		/*
		 * This is slightly broken --> if the type is null, then we have a problem. At least this way there is some way
		 * of ensure type != null If the normaliser has been specified though, we really should have the type as not
		 * null!
		 */
		if (type == null) {
			GuesserManager gm = normaliserManager.getPluginManager().getGuesserManager();
			type = gm.mostLikelyType(xis);
			xis.setType(type);
		}

		// Verify that we are actually getting an office type input source.
		OfficeFileType officeType;
		if (type instanceof OfficeFileType) {
			officeType = (OfficeFileType) type;
		} else {
			throw new IOException("Invalid FileType - must be an OfficeFileType. To override, the type should be set manually.");
		}

		File output = OpenOfficeConverter.convertInput(input, officeType, results, normaliserManager, false);

		// Check file was created successfully by opening up the zip and checking for at least one entry
		// Base64 encode the file and write out to content handler
		try {

			// Check if this is a migrate only
			if (migrateOnly) {
				// Just copy the output file to the final destination
				// Need to use xis.getOutputFileName as results.getOutFileName is null when part of an archive (zip)
				FileUtils.fileCopy(output, results.getDestinationDirString() + File.separator + xis.getOutputFileName(), true);
			} else {

				ContentHandler ch = getContentHandler();
				AttributesImpl att = new AttributesImpl();
				String tagURI = OPEN_DOCUMENT_URI;
				String tagPrefix = OPEN_DOCUMENT_PREFIX;
				//ZipFile openDocumentZip = new ZipFile(output);
				// Not sure if this is even possible, but worth checking I guess...
				//if (openDocumentZip.size() == 0) {
				if (output.length() <= 0) {
					throw new IOException("An empty document was created by OpenOffice.org");
				}
				String outputFileType =
				    normaliserManager.getPluginManager().getPropertiesManager()
				            .getPropertyValue(OfficeProperties.OFFICE_PLUGIN_NAME, OfficeProperties.OOO_OUTPUT_FORMAT);

				// Base64 the file
				att.addAttribute(OPEN_DOCUMENT_URI, PROCESS_DESCRIPTION_TAG_NAME, tagPrefix + ":" + PROCESS_DESCRIPTION_TAG_NAME, "CDATA",
				                 DESCRIPTION);
				att.addAttribute(OPEN_DOCUMENT_URI, DOCUMENT_TYPE_TAG_NAME, tagPrefix + ":" + DOCUMENT_TYPE_TAG_NAME, "CDATA", type.getName());
				att.addAttribute(OPEN_DOCUMENT_URI, DOCUMENT_EXTENSION_TAG_NAME, tagPrefix + ":" + DOCUMENT_EXTENSION_TAG_NAME, "CDATA",
				                 officeType.getODFExtension(outputFileType));

				InputStream is = new FileInputStream(output);
				ch.startElement(tagURI, tagPrefix, tagPrefix + ":" + tagPrefix, att);
				InputStreamEncoder.base64Encode(is, ch);
				ch.endElement(tagURI, tagPrefix, tagPrefix + ":" + tagPrefix);

				// The file seems to be correct so lets generate the export checksum. 
				String checksum = generateChecksum(output);
				setExportedChecksum(checksum);
			}

		} catch (ZipException ex) {
			throw new IOException("OpenOffice.org could not create the open document file");
		} finally {
			output.delete();
		}
	}

	@Override
	public String getVersion() {
		return ReleaseInfo.getVersion() + "b" + ReleaseInfo.getBuildNumber();
	}

	@Override
	public String getOutputFileExtension() {
		// Find the file extension
		InputSource input = (InputSource) getProperty("http://xena/input");
		XenaInputSource xis = (XenaInputSource) input;
		String extension = "";

		Type type = xis.getType();

		if (type instanceof OfficeFileType) {
			OfficeFileType officeType;
			officeType = (OfficeFileType) type;

			// Get the extension from the Office Plugin
			//extension = officeType.getODFExtension();
			String outputFileType =
			    normaliserManager.getPluginManager().getPropertiesManager()
			            .getPropertyValue(OfficeProperties.OFFICE_PLUGIN_NAME, OfficeProperties.OOO_OUTPUT_FORMAT);
			extension = officeType.getODFExtension(outputFileType);
		}

		return extension;

	}

	@Override
	public boolean isConvertible() {
		return true;
	}

}
