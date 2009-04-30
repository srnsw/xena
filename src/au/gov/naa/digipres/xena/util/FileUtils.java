/**
 * This file is part of xena.
 * 
 * xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with xena; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package au.gov.naa.digipres.xena.util;

import java.io.File;

/**
 * @author Justin Waddell
 *
 */
public class FileUtils {

	/**
	 * Return the common base directory for the given directory and file.
	 * This is the first directory that is common to both the given directory and file.
	 * If there is no common directory, return null.
	 * @param path1
	 * @param path2
	 * @return common base directory, null if there is no common directory.
	 */
	public static File getCommonBaseDir(File path1, File path2) {
		File path = path1;
		while (path != null) {
			if (path2.getAbsolutePath().startsWith(path.getAbsolutePath())) {
				return path;
			}
			path = path.getParentFile();
		}
		return null;
	}

}
