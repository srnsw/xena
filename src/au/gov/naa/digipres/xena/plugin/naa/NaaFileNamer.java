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

package au.gov.naa.digipres.xena.plugin.naa;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.CRC32;

import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.filenamer.AbstractFileNamer;
import au.gov.naa.digipres.xena.kernel.filenamer.FileNamerManager;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractSearchableNormaliser;
import au.gov.naa.digipres.xena.util.SourceURIParser;

/**
 * Generate a file name for the output file according to NAA policy.
 *
 */
public class NaaFileNamer extends AbstractFileNamer {

	private static final String TIMESTAMP_FORMAT_STRING = "yyyyMMddHHmmssSSS";
	public static String NAA_FILE_NAMER = "NAA File Namer";

	public NaaFileNamer() {
		// Nothing to do
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public String getName() {
		return NAA_FILE_NAMER;
	}

	/**
	 * Make the filename for the new XenaOutputStream.
	 * This is a fully qualified filename, based on the folders specified.
	 * 
	 * 
	 */
	@Override
	public File makeNewXenaFile(XenaInputSource xis, AbstractNormaliser normaliser, File destinationDir) {

		String id = getId(xis);
		assert id != null;

		// Get the extension - searchable normalisers will have a custom extension, otherwise just use the default
		String extension = FileNamerManager.DEFAULT_EXTENSION;
		if (normaliser instanceof AbstractSearchableNormaliser) {
			AbstractSearchableNormaliser searchableNormaliser = (AbstractSearchableNormaliser) normaliser;
			extension = searchableNormaliser.getOutputFileExtension();
		}

		File newXenaFile = new File(destinationDir, id + "." + extension);
		return newXenaFile;
	}

	/**
	 * Here is where we actually create an ID.
	 * The ID consists of a timestamp string (date and time, to millisecond accuracy), the original filename and a CRC hash of the full file path.
	 * @param urls
	 * @return
	 */
	private synchronized static String getId(XenaInputSource xis) {
		long idNumber = 0L;

		String systemID = xis.getSystemId();

		// Timestamp
		Date currentDate = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat(TIMESTAMP_FORMAT_STRING);
		String timestampString = formatter.format(currentDate);

		// Filename
		String filename = SourceURIParser.getFileNameComponent(xis);

		// Hash of system ID (full path)
		CRC32 crc32 = new CRC32();
		crc32.update(systemID.getBytes());
		idNumber = crc32.getValue();
		String crcStr = Long.toHexString(idNumber);
		while (crcStr.length() < 8) {
			crcStr = "0" + crcStr;
		}

		return timestampString + "-" + filename + "-" + crcStr;
	}

	@Override
	public FileFilter makeFileFilter() {
		return FileNamerManager.DEFAULT_FILE_FILTER;
	}

}
