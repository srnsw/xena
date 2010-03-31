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
 */

package au.gov.naa.digipres.xena.demo.foo.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Vector;

import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.normalise.ExportResult;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;

/**
 * @author Justin Waddell
 *
 */
public class ExporterTester {

	/**
	 * @param args
	 * @throws XenaException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws XenaException, IOException {
		Xena xena = new Xena();

		// our foo jar will already be on the class path, so load it by name...
		Vector<String> pluginList = new Vector<String>();
		pluginList.add("au.gov.naa.digipres.xena.demo.foo.FooPlugin");
		xena.loadPlugins(pluginList);

		// set the base path to be the current working directory
		xena.setBasePath(System.getProperty("user.dir"));
		System.out.println(System.getProperty("user.dir"));

		// create the new input source
		File inputFile = new File("../../../data/example_file.foo");
		XenaInputSource xis = new XenaInputSource(inputFile);
		// guess its type
		Guess fooGuess = xena.getBestGuess(xis);

		//print the guess...
		System.out.println("Here is the best guess returned by Xena: ");
		System.out.println(fooGuess.toString());
		System.out.println("-----------------------------------------");

		// normalise the file!
		NormaliserResults results = xena.normalise(xis);
		System.out.println("Here are the results of the normalisation:");
		System.out.println(results.getResultsDetails());
		System.out.println("-----------------------------------------");

		// Export the normalised file
		File outputFile = new File(results.getDestinationDirString(), results.getOutputFileName());
		XenaInputSource normalisedXIS = new XenaInputSource(outputFile);
		ExportResult exportResult = xena.export(normalisedXIS, new File(results.getDestinationDirString()), true);
		File exportedFile = new File(exportResult.getOutputDirectoryName(), exportResult.getOutputFileName());
		System.out.println("Here are the results of the export:");
		System.out.println(exportResult.toString());
		String compareString = compareFiles(inputFile, exportedFile) ? "identical" : "different";
		System.out.println("The original file is " + compareString + " to the exported file.");
		System.out.println("-----------------------------------------");
	}

	/**
	 * Return true if and only if the files have exactly the same contents
	 * @param file1
	 * @param file2
	 * @return
	 * @throws IOException
	 */
	private static boolean compareFiles(File file1, File file2) throws IOException {
		boolean filesIdentical = true;

		if (file1.length() == file2.length()) {
			FileInputStream inputStream1 = new FileInputStream(file1);
			FileInputStream inputStream2 = new FileInputStream(file2);

			byte[] buffer1 = new byte[1024 * 10];
			byte[] buffer2 = new byte[1024 * 10];

			while (true) {
				int bytesRead1 = inputStream1.read(buffer1);
				int bytesRead2 = inputStream2.read(buffer2);

				// The files must be the same length if we have reached this point, so something has gone wrong.
				if (bytesRead1 != bytesRead2) {
					filesIdentical = false;
					break;
				}

				// We have reached the end of the files and have found no differences
				if (bytesRead1 <= 0) {
					break;
				}

				// Check that these sections of the files are exactly the same 
				if (!compareByteArrays(buffer1, bytesRead1, buffer2, bytesRead2)) {
					filesIdentical = false;
					break;
				}
			}

			inputStream1.close();
			inputStream2.close();
		} else {
			// The files are of different lengths, and thus are not exactly the same
			filesIdentical = false;
		}

		return filesIdentical;
	}

	/**
	 * Return true if and only if the byte arrays contain exactly the same contents
	 * @param buffer1
	 * @param buffer2
	 * @return
	 */
	private static boolean compareByteArrays(byte[] array1, int length1, byte[] array2, int length2) {
		if (length1 != length2) {
			return false;
		}

		for (int i = 0; i < array1.length; i++) {
			if (array1[i] != array2[i]) {
				return false;
			}
		}

		return true;

	}

}
