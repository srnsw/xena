/**
 * This file is part of xena.
 * 
 * xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Justin Waddell
 * @author Jeff Stiff
 *
 */
public class FileUtils {

	private static final int READ_BUFFER_SIZE = 1024;

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

	/**
	 * Copy a given file to the specified destination directory, overwriting the file if
	 * specified.
	 * 
	 * @param inputFile
	 * 				- A file handle pointing to the original file to be copied
	 * @param destPath
	 * 				- The full path, including filename, for the destination file
	 * @param overwrite
	 * 				- If true and a file already exists at the destination the file
	 * 				  will be overwritten.
	 */
	public static void fileCopy(File inputFile, String destPath, boolean overwrite) throws IOException {
		// Create the streams
		InputStream in = new FileInputStream(inputFile);

		fileCopy(in, destPath, overwrite);

		if (in != null) {
			in.close();
		}
	}

	/**
	 * Copy the contents of the ByteArray to the specified destination directory, overwriting the file if
	 * specified.
	 * 
	 * @param byte[]
	 * 				- A ByteArray containing the migrated file to be copied
	 * @param destPath
	 * 				- The full path, including filename, for the destination file
	 * @param overwrite
	 * 				- If true and a file already exists at the destination the file
	 * 				  will be overwritten.
	 */

	public static void fileCopy(byte[] byteArrayInput, String destPath, boolean overwrite) throws IOException {
		// Create/Open the output file
		File outputFile = new File(destPath);

		// Check if the file exists
		if (outputFile.exists() && !overwrite) {
			throw new IOException("File " + outputFile.getAbsolutePath() + " exists.  Please remove before continuing");
		}

		// Create the streams
		OutputStream out = new FileOutputStream(outputFile);

		try {
			// Transfer the file contents
			out.write(byteArrayInput);
		} finally {
			try {
				if (out != null) {
					out.flush();
					out.close();
				}
			} catch (IOException x) {
				throw new IOException(x);
			}
		}
	}

	/**
	 * Copy the contents of the inputStream to the specified destination directory, overwriting the file if
	 * specified.
	 * 
	 * @param inputStream
	 * 				- An InputStream containing the original file to be copied
	 * @param destPath
	 * 				- The full path, including filename, for the destination file
	 * @param overwrite
	 * 				- If true and a file already exists at the destination the file
	 * 				  will be overwritten.
	 */
	public static void fileCopy(InputStream inputStream, String destPath, boolean overwrite) throws IOException {

		// Create/Open the output file
		File outputFile = new File(destPath);

		// Check if the file exists
		if (outputFile.exists() && !overwrite) {
			throw new IOException("File " + outputFile.getAbsolutePath() + " exists.  Please remove before continuing");
		}

		// Create the streams
		OutputStream out = new FileOutputStream(outputFile);

		try {
			// Transfer the file contents
			byte[] buf = new byte[READ_BUFFER_SIZE];
			int length;
			while ((length = inputStream.read(buf)) >= 0) {
				out.write(buf, 0, length);
			}
		} finally {
			try {
				if (out != null) {
					out.flush();
					out.close();
				}
			} catch (IOException x) {
				throw new IOException(x);
			}
		}
	}

	/**
	 * Copy directory and its contents.
	 * @param directory
	 * @param outputName - Name and location of final directory to copy as
	 */
	public static void copyDirAndContents(File directory, String outputName) throws IOException {
		if (directory != null && directory.exists()) {
			if (directory.isDirectory()) {
				// Create the new directory at the destination
				outputName = outputName + "_dir";
				File newDir = new File(outputName);
				// If the file already exists, add an incrementing numerical ID and check again
				int i = 0;
				DecimalFormat idFormatter = new DecimalFormat("0000");
				while (newDir.exists()) {
					i++;
					newDir = new File(outputName + "." + idFormatter.format(i));
				}
				if (i > 0) {
					outputName = outputName + "." + idFormatter.format(i);
				}
				newDir.mkdir();

				// Copy the contents of the directory over to the new directory
				for (File singleFile : directory.listFiles()) {
					// Copy into new directory
					fileCopy(singleFile, outputName + File.separator + singleFile.getName(), false);
				}
			}
		}
	}

	/**
	 * Copy ALL files in input directory that start with the same name as passed.  
	 * If one file is found, it is copied to the destination directory directly,
	 * if there is more than one file found they are all copied to a sub-directory
	 * of the destination directory named as fileName_dir.
	 * If the single file, or the directory are already present in the destination directory
	 * the names will be appended with a numerical sequence counting up from zero.
	 * 
	 * @param baseFileName The name of the file that is used for the copy.  Any files in the 
	 * 		  inputDirectory that start with the same name will be copied.  Will also be the name
	 *        of the directory created in the destination directory if multiple files copied.
	 * @param inputDirectoryName The directory that holds the file(s) to be copied
	 * @param outputDirectoryName The destination directory of the copied files or directory
	 */
	public static void copyAllFilesLike(String baseFileName, String inputDirectoryName, String outputDirectoryName) throws IOException {
		File inputDir = new File(inputDirectoryName);

		// Determine if this is a single file or a directory copy
		Boolean isSingleFile = true;
		if (inputDir != null && inputDir.exists() && inputDir.isDirectory()) {
			int counter = 0;
			for (File singleFile : inputDir.listFiles()) {
				if (singleFile.getName().startsWith(baseFileName)) {
					counter++;
				}
				if (counter > 1) {
					// Break out, as we know this is a directory copy, we don't need to keep counting
					isSingleFile = false;
					break;
				}
			}

			// Copy the File(s)
			if (isSingleFile) {
				// Copy the file to the destination directory
				File singleFile = new File(inputDirectoryName + File.separator + baseFileName);
				fileCopy(singleFile, outputDirectoryName + File.separator + baseFileName, false);
			} else {
				// Copy the matching files to the new directory
				String newOutputDirName = outputDirectoryName + File.separator + baseFileName + "_dir";
				File newDir = new File(newOutputDirName);
				// If the directory already exists, add an incrementing numerical ID and check again
				int i = 0;
				DecimalFormat idFormatter = new DecimalFormat("0000");
				while (newDir.exists()) {
					i++;
					newDir = new File(newOutputDirName + "." + idFormatter.format(i));
				}
				if (i > 0) {
					newOutputDirName = newOutputDirName + "." + idFormatter.format(i);
				}
				newDir.mkdir();

				// Copy the contents of the directory over to the new directory
				for (File singleFile : inputDir.listFiles()) {
					if (singleFile.getName().startsWith(baseFileName)) {
						// Copy into new directory
						fileCopy(singleFile, newOutputDirName + File.separator + singleFile.getName(), false);
					}
				}
			}

		}

	}

	/**
	 * Copy ALL files in input directory that start with the same name as passed.
	 * This method differs from the copyAllFileLike in that it is written specifically
	 * for HTML output from OpenOffice where the base filename is <filename>.html and the 
	 * associated images are named <filename>_html_unique number.
	 * This function therefore copies the base file and any similarly named image files.  
	 * If one file is found, it is copied to the destination directory directly,
	 * if there is more than one file found they are all copied to a sub-directory
	 * of the destination directory named as fileName_dir.
	 * If the single file, or the directory are already present in the destination directory
	 * the names will be appended with a numerical sequence counting up from zero.
	 * This method can be used with non-HTML copies, but is written for HTML copies.
	 * 
	 * @param baseFileName The name of the file that is used for the copy.  Any files in the 
	 * 		  inputDirectory that start with the same name will be copied.  Will also be the name
	 *        of the directory created in the destination directory if multiple files copied.
	 * @param inputDirectoryName The directory that holds the file(s) to be copied
	 * @param outputDirectoryName The destination directory of the copied files or directory
	 */
	public static void copyAllFilesLikeHTML(String baseFileName, String inputDirectoryName, String outputDirectoryName) throws IOException {
		File inputDir = new File(inputDirectoryName);

		String imageNames = baseFileName;

		if (baseFileName.endsWith(".html")) {
			imageNames = baseFileName.substring(0, baseFileName.indexOf(".html")) + "_html";
		}

		// Determine if this is a single file or a directory copy
		Boolean isSingleFile = true;
		if (inputDir != null && inputDir.exists() && inputDir.isDirectory()) {
			int counter = 0;
			for (File singleFile : inputDir.listFiles()) {
				if (singleFile.getName().startsWith(baseFileName) || singleFile.getName().startsWith(imageNames)) {
					counter++;
				}
				if (counter > 1) {
					// Break out, as we know this is a directory copy, we don't need to keep counting
					isSingleFile = false;
					break;
				}
			}

			// Copy the File(s)
			if (isSingleFile) {
				// Copy the file to the destination directory
				File singleFile = new File(inputDirectoryName + File.separator + baseFileName);
				fileCopy(singleFile, outputDirectoryName + File.separator + baseFileName, true);
			} else {
				// Copy the matching files to the new directory
				String newOutputDirName = outputDirectoryName + File.separator + baseFileName + "_dir";
				File newDir = new File(newOutputDirName);
				// If the directory already exists, add an incrementing numerical ID and check again
				int i = 0;
				DecimalFormat idFormatter = new DecimalFormat("0000");
				while (newDir.exists()) {
					i++;
					newDir = new File(newOutputDirName + "." + idFormatter.format(i));
				}
				if (i > 0) {
					newOutputDirName = newOutputDirName + "." + idFormatter.format(i);
				}
				newDir.mkdir();

				// Copy the contents of the directory over to the new directory
				for (File singleFile : inputDir.listFiles()) {
					if (singleFile.getName().startsWith(baseFileName) || singleFile.getName().startsWith(imageNames)) {
						// Copy into new directory
						fileCopy(singleFile, newOutputDirName + File.separator + singleFile.getName(), false);
					}
				}
			}

		}

	}

	/**
	 * Zip ALL files in input directory that start with the same name as passed.
	 * This method is written specifically for HTML output from OpenOffice where the base filename 
	 * is <filename>.html and the associated images are named <filename>_html_unique number.
	 * This function therefore copies the base file and any similarly named image files into a 
	 * similarly named zip file with the given extension.  
	 * This method can be used with non-HTML zips, but is written for HTML zips.
	 * 
	 * @param baseFileName The name of the file that is used for the copy to zip.  Any files in the 
	 * 		  inputDirectory that start with the same name will be copied.  Will also be the name
	 *        of the output zip file created in the current directory.
	 * @param inputDirectoryName The directory that holds the file(s) to be zipped
	 * @param zipExtension The extension to use for the Zip file, should normally be one of ZIP or WSX, but is not checked.
	 */
	public static File zipAllFilesLikeHTML(String baseFileName, String inputDirectoryName, String zipExtension) throws IOException {
		File inputDir = new File(inputDirectoryName);
		File outputZip;

		String imageNames = baseFileName;

		if (baseFileName.endsWith(".html")) {
			imageNames = baseFileName.substring(0, baseFileName.indexOf(".html")) + "_html";
		}

		ArrayList<String> filesList = new ArrayList<String>();

		// Determine if this is a single file or a directory copy
		if (inputDir != null && inputDir.exists() && inputDir.isDirectory()) {
			int counter = 0;
			for (File singleFile : inputDir.listFiles()) {
				if (singleFile.getName().startsWith(baseFileName) || singleFile.getName().startsWith(imageNames)) {
					filesList.add(singleFile.toString());
					counter++;
				}
			}

			// Manually copy into String Array, (String[] filesListArray = (String[]) filesList.toArray();) fails
			String[] temp = {" "};
			String[] filesListArray = (String[]) filesList.toArray(temp);

			outputZip = createZipFile(inputDirectoryName + File.separator + baseFileName + "." + zipExtension, filesListArray, true);

		} else {
			throw new IOException(baseFileName + " does not exist.");
		}

		return outputZip;

	}

	/**
	 * Create a zip file named <zipFileName> of all the files passed in the string array.  If <isFlat> 
	 * is passed true the zip file internal structure will be a flat directory, otherwise will be created
	 * following the fully pathed structure of the input files.
	 * 
	 * @param zipFileName The name of the ZIP file to create, including any extension 
	 * @param filesToZip A string array containing the fully pathed list of the files to include in the zip file
	 * @param isFlat True if the resulting zip file should have a flat internal structure.
	 */
	public static File createZipFile(String zipFileName, String[] filesToZip, Boolean isFlat) throws IOException {
		// Create a buffer for reading the files
		byte[] buf = new byte[1024];

		try {
			// Create the ZIP file
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));

			// Compress the files
			for (int i = 0; i < filesToZip.length; i++) {
				FileInputStream in = new FileInputStream(filesToZip[i]);

				String zipEntryName = filesToZip[i];
				if (isFlat) {
					zipEntryName = (new File(filesToZip[i])).getName().toString();
				}

				// Add ZIP entry to output stream.
				out.putNextEntry(new ZipEntry(zipEntryName));

				// Transfer bytes from the file to the ZIP file
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}

				// Complete the entry
				out.closeEntry();
				in.close();
			}

			// Complete the ZIP file
			out.close();

			return new File(zipFileName);
		} catch (IOException e) {
			throw new IOException(e);
		}
	}
}
