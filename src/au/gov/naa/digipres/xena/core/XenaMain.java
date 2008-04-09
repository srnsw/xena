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

package au.gov.naa.digipres.xena.core;

import au.gov.naa.digipres.xena.litegui.LiteMainFrame;
import org.apache.commons.cli.*;

import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.kernel.*;
import au.gov.naa.digipres.xena.kernel.normalise.*;

import java.io.*;

/**
 * Main Xena invocation class. This acts as a wrapper around Xena invocation to parse command-line
 * arguments for automated conversion and to display the Xena GUI if no command-line arguments
 * are provided.
 *
 * <p>
 * <em>Note:</em> Command-line functionality is currently minimal and should be expanded. Additionally,
 * this class was developed for in-house purposes and should not be considered production-ready.
 * </p>
 *
 * @author Matt Painter <matthew.painter@archives.govt.nz>
 */
public class XenaMain {

	public static void main(String[] args) throws Exception {

		// If no command-line arguments are provided, assume that the user is wishing to invoke the GUI
		if (args.length == 0) {
			LiteMainFrame liteMainFrame = new LiteMainFrame();
			liteMainFrame.setVisible(true);
			return;
		}

		// Parse command-line options
		XenaMain xenaMain = new XenaMain();
		Options options = xenaMain.constructOptions();
		BasicParser parser = new BasicParser();
		CommandLine commandLine = parser.parse(options, args);

		if (commandLine.hasOption('h') || !(commandLine.hasOption('p') && commandLine.hasOption('f') && commandLine.hasOption('o'))) {
			HelpFormatter f = new HelpFormatter();
			f.printHelp("xena", options);
			System.exit(1);
		}

		String[] files = commandLine.getOptionValues("f");
		String destinationPath = commandLine.getOptionValue("outputDirectory");
		String pluginsPath = commandLine.getOptionValue("pluginsDirectory");

		File destinationDirectory = xenaMain.getDestinationDirectory(destinationPath);
		File pluginsDirectory = xenaMain.getPluginsDirectory(pluginsPath);

		xenaMain.processNormalisation(files, destinationDirectory, pluginsDirectory);
	}


	/**
	 * Returns a file handle to the Xena plugins directory.
	 *
	 * @param  pluginsPath path to plugins directory
	 * @return file handle to plugins directory
	 */
	private File getPluginsDirectory(String pluginsPath) {

		// Validate plugins directory
		File pluginsDirectory = new File(pluginsPath);
		if (!pluginsDirectory.exists()) {
			System.err.println("Unable to find plugins directory");
			System.exit(1);
		}

		return pluginsDirectory;
	}

	/**
	 * Returns a file handle to the normalisation destination directory. If the path
	 * references a non-existent directory, the directory is created.
	 *
	 * @param destinationPath path to destination directory
	 * @return file handle to destination directory
	 */
	private File getDestinationDirectory(String destinationPath) {

		// Create the destination directory
		File destinationDirectory = new File(destinationPath);

		if (!destinationDirectory.mkdir()) {
			if (!destinationDirectory.exists() || !destinationDirectory.isDirectory()) {
				System.err.println("Unable to create destination directory. Exiting.");
				System.exit(1);
			}
		}

		return destinationDirectory;
	}

	/**
	 * Perform normalisation on a set of files
	 *
	 * @param files list of files to perform normalisation on
	 * @param destinationDirectory destination directory for normalised files
	 * @param pluginsDirectory directory of Xena plugins
	 */
	private void processNormalisation(String[] files, File destinationDirectory, File pluginsDirectory) throws XenaException, FileNotFoundException, IOException {
		Xena xena = new Xena();
		xena.loadPlugins(pluginsDirectory);

		int failureCount = 0;
		for (String file : files) {
			System.out.print(file);
			XenaInputSource xenaInputSource = new XenaInputSource(new File(file));

			// Normalise file using best guess
			NormaliserResults results = xena.normalise(xenaInputSource, destinationDirectory);
			if (!results.isNormalised()) {
				failureCount++;
				System.out.println(" FAIL");
			}
			else {
				System.out.println(" OK");
			}
		}

		System.out.println("-----------------------");

		if (failureCount > 0) {
			System.out.println("Normalisation failures: " + failureCount);
			System.exit(1);
		}
		else {
			System.out.println("Normalisation OK");
		}
	}


	/**
	 * Constructs command-line options
	 */
	private static Options constructOptions() {
		Options options = new Options();

		Option option = new Option("f", "file", true, "Input files");
		option.setArgs(Option.UNLIMITED_VALUES);
		options.addOption(option);

		options.addOption("p", "pluginsDirectory", true, "Path to plugins directory");
		options.addOption("o", "outputDirectory", true, "Output directory");
		options.addOption("h", "help", false, "Print usage information");

		return options;
	}
}
