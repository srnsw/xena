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
		if ((destinationDir == null) || (!destinationDir.exists()) || (!destinationDir.isDirectory())) {
			throw new XenaException("Could not create new file because there was an error with the destination directory ("
			                        + destinationDir.toString() + ").");
		}
		File newXenaFile = null;
		if (destinationDir != null) {
			id = getId(input, normaliser.toString());
			String fileName = id + "." + FileNamerManager.DEFAULT_EXTENSION;
			newXenaFile = new File(destinationDir, fileName);
			int i = 1;
			DecimalFormat idFormatter = new DecimalFormat("0000");
			while (newXenaFile.exists()) {
				fileName = id + idFormatter.format(i) + "." + FileNamerManager.DEFAULT_EXTENSION;
				newXenaFile = new File(destinationDir, fileName);
				i++;
			}
		}
		return newXenaFile;
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
