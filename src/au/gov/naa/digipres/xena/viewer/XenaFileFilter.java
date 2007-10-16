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
 * Created on 29/11/2005 justinw5
 * 
 */
package au.gov.naa.digipres.xena.viewer;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * Simple class which ensures only .xena files will be visible
 * in a FileChooser.
 * 
 * created 29/11/2005
 * xena
 * Short desc of class: .xena file filter
 */
public class XenaFileFilter extends FileFilter {
	public static final String XENA_EXT = "XENA";

	public XenaFileFilter() {
		super();
	}

	@Override
	/**
	 * Only accepts file extensions matching XENA_EXT
	 */
	public boolean accept(File f) {
		// Show directories
		if (f.isDirectory()) {
			return true;
		} else {
			String extension = getExtension(f);
			if (extension != null && XENA_EXT.equals(extension)) {
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public String getDescription() {
		return "Xena Files (*.xena)";
	}

	/**
	 * Gets the file extension for the given file - 
	 * all characters after the final "."
	 * 
	 * @param f File of which to return the extension
	 * @return
	 */
	private String getExtension(File f) {
		if (f != null) {
			String filename = f.getName();
			int i = filename.lastIndexOf('.');
			if (i > 0 && i < filename.length() - 1) {
				return filename.substring(i + 1).toUpperCase();
			}
		}
		return null;
	}

}
