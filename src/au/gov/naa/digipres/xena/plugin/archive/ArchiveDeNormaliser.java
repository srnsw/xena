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
 * @author Dan Spasojevic
 * @author Justin Waddell
 */

/*
 * Created on 08/03/2007 justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser;

/**
 * The original archive file has been decompressed and normalised to a collection of xena files, plus an index file to link the collection together.
 * This denormaliser iterates through each xena file listed in the index file, and exports each of these xena files to a temporary file.
 * Each of these temporary files are then compressed in a zip file.
 * 
 * created 03/04/2007
 * archive
 * Short desc of class:
 */
public class ArchiveDeNormaliser extends AbstractDeNormaliser {
	private int messageCounter = 0;
	private ZipOutputStream zipOS;

	@Override
	public String getName() {
		return "Archive Denormaliser";
	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#getOutputFileExtension(au.gov.naa.digipres.xena.kernel.XenaInputSource)
	 */
	@Override
	public String getOutputFileExtension(XenaInputSource xis) throws XenaException {
		return "zip";
	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#startDocument()
	 */
	@Override
	public void startDocument() throws SAXException {
		// Initialise primary ZipOutputStream
		zipOS = new ZipOutputStream(streamResult.getOutputStream());
	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#endDocument()
	 */
	@Override
	public void endDocument() throws SAXException {
		try {
			zipOS.flush();
			zipOS.close();
		} catch (IOException e) {
			throw new SAXException("Problem closing Zip output stream", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#startElement(java.lang.String,
	 * java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		if (qName.equals(ArchiveNormaliser.ARCHIVE_PREFIX + ":" + ArchiveNormaliser.ENTRY_TAG)) {
			File entryFile = new File(sourceDirectory, atts.getValue(ArchiveNormaliser.ENTRY_OUTPUT_FILENAME));
			if (entryFile.exists() && entryFile.isFile()) {
				String messageExportFilename = ++messageCounter + "-" + outputFilename;
				File entryExportFile = null;
				try {
					// Export this entry
					normaliserManager.export(new XenaInputSource(entryFile), outputDirectory, messageExportFilename, true);

					// Create a zip entry using the entry name found in the original archive
					ZipEntry currentEntry = new ZipEntry(atts.getValue(ArchiveNormaliser.ENTRY_ORIGINAL_PATH_ATTRIBUTE));

					// Set date of entry
					String dateStr = atts.getValue(ArchiveNormaliser.ENTRY_ORIGINAL_FILE_DATE_ATTRIBUTE);
					SimpleDateFormat formatter = new SimpleDateFormat(ArchiveNormaliser.DATE_FORMAT_STRING);
					currentEntry.setTime(formatter.parse(dateStr).getTime());

					// Add new entry to zip output stream
					zipOS.putNextEntry(currentEntry);

					// Write file contents to zip output stream
					entryExportFile = new File(outputDirectory, messageExportFilename);
					FileInputStream entryExportIS = new FileInputStream(entryExportFile);
					byte[] buffer = new byte[10 * 1024];
					int bytesRead = entryExportIS.read(buffer);
					while (bytesRead > 0) {
						zipOS.write(buffer, 0, bytesRead);
						bytesRead = entryExportIS.read(buffer);
					}

					// Finalisation
					entryExportIS.close();
					zipOS.closeEntry();
				} catch (Exception ex) {
					throw new SAXException("Problem exporting archive entry " + atts.getValue(ArchiveNormaliser.ENTRY_ORIGINAL_PATH_ATTRIBUTE), ex);
				} finally {
					if (entryExportFile != null) {
						entryExportFile.delete();
					}
				}
			}

		}
	}

}
