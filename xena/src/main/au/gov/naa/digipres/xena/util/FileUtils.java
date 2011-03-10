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
import java.io.IOException;

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

	/**
	 * Delete directory and its contents.
	 * @param directory
	 */
	public static void deleteDirAndContents(File directory) {
		if (directory != null && directory.exists()) {
			if (directory.isDirectory()) {
				deleteContentsOfDir(directory);
			}
			directory.delete();
		}
	}

	/**
	 * Delete the contents of a directory, without deleting the directory itself.
	 * @param directory
	 */
	public static void deleteContentsOfDir(File directory) {
		if (directory != null && directory.exists() && directory.isDirectory()) {
			File[] dirFiles = directory.listFiles();
			for (File dirFile : dirFiles) {
				if (dirFile.isDirectory()) {
					deleteDirAndContents(dirFile);
				} else {
					dirFile.delete();
				}
			}
		}
	}

	/**
	 * Return a file given a directory and a filename, but ignoring case.
	 * On Windows case is ignored anyway so this method will just check that the file exists.
	 * On other platforms case is significant so this method will attempt to find the requested file on the system while ignoring case.
	 * 
	 * If this method is called on a platform with case-sensitivity, and there are multiple files in the directory that have the same name
	 * but the case is different, then the file that is returned will be the first of these that is encountered in the directory's file list.
	 * 
	 * @param parentDir
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static File getFileIgnoreCase(File parentDir, String filename) throws IOException {
		File testFile = new File(parentDir, filename);
		File[] filesInSameDir = parentDir.listFiles();
		for (File fileInSameDir : filesInSameDir) {
			if (testFile.getName().equalsIgnoreCase(fileInSameDir.getName())) {
				testFile = fileInSameDir;
				break;
			}
		}

		// If the file doesn't exist, throw an exception
		if (!testFile.exists()) {
			throw new IOException("Filename " + filename + " does not exist in directory " + parentDir + " when ignoring case.");
		}

		return testFile;
	}

}
