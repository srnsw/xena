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

package au.gov.naa.digipres.xena.plugin.office;

import java.io.File;
import java.io.FileFilter;
import java.io.PrintStream;

import com.sun.star.beans.PropertyValue;
import com.sun.star.bridge.XUnoUrlResolver;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.UnoRuntime;

/**
 *  The class <CODE>Ms2Ooo</CODE> allows you to convert all documents in a given
 *  directory and in its subdirectories to a given type. A converted document
 *  will be created in the same directory as the origin document.
 *
 * @created    February 28, 2002
 */
public class Ms2Ooo {
	/**
	 *  Containing the loaded documents
	 */
	static XComponentLoader xcomponentloader = null;

	/**
	 *  Containing the given type to convert to
	 */
	static String stringConvertType = "";

	/**
	 *  Containing the given extension
	 */
	static String stringExtension = "";

	/**
	 *  Containing the current file or directory
	 */
	static String indent = "";

	static File outputFile;

	static PrintStream infoLog = System.out;

	/**
	 *  Connecting to the office with the component UnoUrlResolver and calling the
	 *  static method traverse
	 *
	 * @param  args  The array of the type String contains the directory, in which
	 *      all files should be converted, the favoured converting type and the
	 *      wanted extension
	 */
	public static void main(String args[]) {
		try {
			if (args.length < 2) {
				System.out.println("usage: java <inputdir> <outputdir> [logfilename]");
				System.exit(1);
			}
			outputFile = new File(args[1]);
			if (!outputFile.isDirectory()) {
				System.out.println(args[1] + " is not a directory");
				System.exit(1);
			}
			if (2 < args.length) {
				infoLog = new PrintStream(new java.io.FileOutputStream(args[2]));
			}

			/*
			 * Bootstraps a servicemanager with the jurt base components registered
			 */
			XMultiServiceFactory xmultiservicefactory = com.sun.star.comp.helper.Bootstrap.createSimpleServiceManager();

			/*
			 * Creates an instance of the component UnoUrlResolver which supports the services specified by the factory.
			 */
			Object objectUrlResolver = xmultiservicefactory.createInstance("com.sun.star.bridge.UnoUrlResolver");

			// Create a new url resolver
			XUnoUrlResolver xurlresolver = (XUnoUrlResolver) UnoRuntime.queryInterface(XUnoUrlResolver.class, objectUrlResolver);

			// Resolves an object that is specified as follow:
			// uno:<connection description>;<protocol description>;<initial object name>
			Object objectInitial = xurlresolver.resolve("uno:socket,host=localhost,port=8100;urp;StarOffice.ServiceManager");

			// Create a service manager from the initial object
			xmultiservicefactory = (XMultiServiceFactory) UnoRuntime.queryInterface(XMultiServiceFactory.class, objectInitial);

			/*
			 * A desktop environment contains tasks with one or more frames in which components can be loaded. Desktop
			 * is the environment for components which can instanciate within frames.
			 */
			xcomponentloader =
			    (XComponentLoader) UnoRuntime.queryInterface(XComponentLoader.class, xmultiservicefactory
			            .createInstance("com.sun.star.frame.Desktop"));

			// Getting the given starting directory
			File file = new File(args[0]);

			// Getting the given type to convert to
			stringConvertType = "swriter: StarOffice XML (Writer)";
			stringConvertType = "swriter: writer_Flat_XML_File";
			stringConvertType = "simpress: StarOffice XML (Impress)";
			stringConvertType = "scalc: StarOffice XML (Calc)";
			// Getting the given extension that should be appended to the origin document
			stringExtension = "xml";

			stringConvertType = "scalc: Flat XML Calc File";
			stringConvertType = "swriter: Flat XML File";
			stringConvertType = "simpress: Flat Impress XML File";
			System.out.println("T:" + stringConvertType);
			// Starting the conversion of documents in the given directory and subdirectories
			traverse(file);
			System.exit(0);
		} catch (Exception exception) {
			System.err.println(exception);
		}
	}

	/**
	 *  Traversing the given directory recursively and converting their files to
	 *  the favoured type if possible
	 *
	 * @param  fileDirectory  Containing the directory
	 */
	static void traverse(File fileDirectory) {
		// Testing, if the file is a directory, and if so, it throws an exception
		if (!fileDirectory.isDirectory()) {
			throw new IllegalArgumentException("not a directory: " + fileDirectory.getName());
		}

		System.out.println(indent + "[" + fileDirectory.getName() + "]");
		indent += "  ";

		// Getting all files and directories in the current directory
		File[] entries = fileDirectory.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return true;
				// return pathname.getName().endsWith(".doc");
			}
		});

		// Iterating for each file and directory
		for (int i = 0; i < entries.length; ++i) {
			// Testing, if the entry in the list is a directory
			if (entries[i].isDirectory()) {
				// Recursive call for the new directory
				traverse(entries[i]);
			} else {
				// Converting the document to the favoured type
				try {
					String baseName = entries[i].getName();
					baseName = baseName.substring(0, baseName.indexOf('.')) + "." + Ms2Ooo.stringExtension;
					// Composing the URL by replacing all backslashs
					File fullPath = new File(outputFile.getAbsolutePath(), baseName);
					File oldFullPath = new File(fileDirectory, entries[i].getName());
					infoLog.println(oldFullPath.getAbsolutePath() + " --> " + fullPath.getAbsolutePath());
					String newUrl = "file:///" + fullPath;
					newUrl = newUrl.replace('\\', '/');

					String stringUrl = "file:///" + entries[i].getAbsoluteFile();
					stringUrl = stringUrl.replace('\\', '/');

					// Loading the wanted document
					Object objectDocumentToStore = Ms2Ooo.xcomponentloader.loadComponentFromURL(stringUrl, "_blank", 0, new PropertyValue[0]);

					// Getting an object that will offer a simple way to store a document to a URL.
					XStorable xstorable = (XStorable) UnoRuntime.queryInterface(XStorable.class, objectDocumentToStore);

					// Preparing properties for converting the document
					PropertyValue propertyvalue[] = new PropertyValue[2];
					// Setting the flag for overwriting
					propertyvalue[0] = new PropertyValue();
					propertyvalue[0].Name = "Overwrite";
					propertyvalue[0].Value = new Boolean(true);
					// Setting the filter name
					propertyvalue[1] = new PropertyValue();
					propertyvalue[1].Name = "FilterName";
					propertyvalue[1].Value = Ms2Ooo.stringConvertType;

					// Appending the favoured extension to the origin document name

					// Storing and converting the document
					xstorable.storeAsURL(newUrl, propertyvalue);

					// Getting the method dispose() for closing the document
					XComponent xcomponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, xstorable);

					// Closing the converted document
					xcomponent.dispose();
				} catch (Exception exception) {
					exception.printStackTrace(infoLog);
				}

				System.out.println(indent + entries[i].getName());
			}
		}
		indent = indent.substring(2);
	}
}
