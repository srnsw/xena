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
 * @author Matthew Oliver
 */

/*
 * Created on 28/03/2007 justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.archive.macbinary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.plugin.archive.ReleaseInfo;

/**
 * Normaliser for .zip and .jar files
 * 
 * created 28/03/2007
 * archive
 * Short desc of class:
 */
public class MacBinaryNormaliser extends AbstractNormaliser {

	@Override
	public String getName() {
		return "MacBinary";
	}

	@Override
	public void parse(InputSource input, NormaliserResults results, boolean migrateOnly) throws IOException, SAXException {
		ContentHandler contentHandler = getContentHandler();

		// We want to normalise the file inside this archive, using its original file name. However the name of the
		// archived file cannot be determined until after we have started extracting. An error is thrown if
		// this file already exists, but we can't check for its existence before we start the extraction process. So
		// we'll use a unique subdirectory in the temp directory, and delete both this directory and file when we
		// are finished.

		// Ensure we're not overwriting an existing file
		File tempDir = new File(System.getProperty("java.io.tmpdir"), String.valueOf(System.currentTimeMillis()));
		if (!tempDir.exists()) {
			tempDir.mkdirs();
		}
		tempDir.deleteOnExit();
		String tempFilename = String.valueOf(System.currentTimeMillis());
		File tempFile = new File(tempDir, tempFilename);
		tempFile.deleteOnExit();

		// Create a MBDecoderOutputStream which will strip the data out of the macbinary file.
		MBDecoderOutputStream outputStream = new MBDecoderOutputStream(new FileOutputStream(tempFile));

		// Pass source bytes to receiver
		InputStream sourceStream = input.getByteStream();
		byte[] buffer = new byte[10 * 1024];
		int bytesRead = sourceStream.read(buffer);
		while (bytesRead > 0) {
			outputStream.write(buffer);
			bytesRead = sourceStream.read(buffer);
		}

		sourceStream.close();
		outputStream.flush();
		outputStream.close();

		// Rename the output file to the name extracted from the macbinary file
		File newFile = new File(tempDir, outputStream.getFileName().trim());
		newFile.deleteOnExit();
		tempFile.renameTo(newFile);
		tempFile.setLastModified(outputStream.getLastModifiedDate());

		XenaInputSource extractedXis = new XenaInputSource(newFile);
		try {
			// Get the type and associated normaliser for this entry
			Type fileType = normaliserManager.getPluginManager().getGuesserManager().mostLikelyType(extractedXis);

			extractedXis.setType(fileType);
			AbstractNormaliser entryNormaliser = normaliserManager.lookup(fileType);

			// Set up the normaliser and wrapper for this entry
			entryNormaliser.setProperty("http://xena/url", extractedXis.getSystemId());
			entryNormaliser.setContentHandler(contentHandler);
			entryNormaliser.setProperty("http://xena/file", getProperty("http://xena/file"));
			entryNormaliser.setProperty("http://xena/normaliser", entryNormaliser);

			// Normalise the entry
			entryNormaliser.parse(extractedXis, results, migrateOnly);
		} catch (XenaException ex) {
			throw new SAXException("Problem normalising the compressed file contained within a GZIP archive", ex);
		}
	}

	@Override
	public String getVersion() {
		return ReleaseInfo.getVersion() + "b" + ReleaseInfo.getBuildNumber();
	}

	@Override
	public String getOutputFileExtension() {
		return "bin";
	}

	@Override
	public boolean isConvertible() {
		// While the archive is not strictly convertible, the files within may be
		return false;
	}

}
