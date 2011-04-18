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

/*
 * Created on 29/09/2005 andrek24
 * 
 */
package au.gov.naa.digipres.xena.kernel.filenamer;

import java.io.File;
import java.io.FileFilter;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractTextNormaliser;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.plugin.office.OfficeFileType;
import au.gov.naa.digipres.xena.util.SourceURIParser;

/**
 */
public class DefaultFileNamer extends AbstractFileNamer {

	public static final String DEFAULT_FILENAMER_NAME = "Default Xena file namer";

	private Logger logger = Logger.getLogger(this.getClass().getName());

	private Map<String, String> generatedIdToSystemIdList = new HashMap<String, String>();

	/**
	 * 
	 */
	public DefaultFileNamer() {
		super();
	}

	@Override
	public String getName() {
		return DEFAULT_FILENAMER_NAME;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public File makeNewXenaFile(XenaInputSource input, AbstractNormaliser normaliser, File destinationDir) throws XenaException {
		String id = "00000000";
		if (!destinationDir.exists() || !destinationDir.isDirectory()) {
			throw new XenaException("Could not create new file because there was an error with the destination directory ("
			                        + destinationDir.toString() + ").");
		}

		// Construct the xena file
		id = getId(input, normaliser.toString());

		// Get the extension - text normalisers will have a custom extension, otherwise just use the default
		String extension = FileNamerManager.DEFAULT_EXTENSION;
		if (normaliser instanceof AbstractTextNormaliser) {
			AbstractTextNormaliser textNormaliser = (AbstractTextNormaliser) normaliser;
			extension = textNormaliser.getOutputFileExtension();
		}

		String fileName = id + "." + extension;
		File newXenaFile = new File(destinationDir, fileName);

		// If the file already exists, add an incrementing numerical ID and check again
		int i = 1;
		DecimalFormat idFormatter = new DecimalFormat("0000");
		while (newXenaFile.exists()) {
			fileName = id + idFormatter.format(i) + "." + extension;
			newXenaFile = new File(destinationDir, fileName);
			i++;
		}

		return newXenaFile;
	}

	/**
	 * Make the filename for the new Open Format File.
	 * This is a fully qualified filename, based on the folders specified.
	 * 
	 * Unlike the makeNewXenaFile method, this method only appends the new open format extension
	 * to the original filename, rather than renaming the file based on timestamp etc
	 * 
	 */
	@Override
	public File makeNewOpenFile(XenaInputSource xis, AbstractNormaliser normaliser, File destinationDir) {

		String extension = "";
		String id = SourceURIParser.getFileNameComponent(xis);
		assert id != null;

		// Need to check if this is an Office Document, as they are handled differently
		Type type = xis.getType();

		if (type instanceof OfficeFileType) {
			OfficeFileType officeType;
			officeType = (OfficeFileType) type;

			// Get the extension from the Office Plugin
			extension = officeType.getODFExtension();
		} else {
			// Get the extension from the normaliser
			extension = normaliser.getOutputFileExtension();
		}

		// If Xena has replaced any spaces with %20, put the spaces back
		id = id.replaceAll("%20", " ");

		File newOpenFile = new File(destinationDir, id + "." + extension);
		return newOpenFile;
	}

	private String getId(XenaInputSource input, String normaliserName) {
		// generate the id for this file.
		String fileName = SourceURIParser.getFileNameComponent(input);
		String newName = fileName + "_" + normaliserName;

		if (generatedIdToSystemIdList.containsKey(newName)) {
			if (overwrite == false) {
				int i = 0;
				newName = String.format("%s_%s_%2d", fileName, normaliserName, i);
				while (generatedIdToSystemIdList.containsKey(newName)) {
					i++;
					newName = String.format("%s_%s_%2d", fileName, normaliserName, i);
				}
			}
		}
		return newName;
	}

	@Override
	public FileFilter makeFileFilter() {
		return FileNamerManager.DEFAULT_FILE_FILTER;
	}

}
