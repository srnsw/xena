/**
 * This file is part of office.
 * 
 * office is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * office is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with office; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package au.gov.naa.digipres.xena.plugin.office;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserManager;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.plugin.PluginManager;
import au.gov.naa.digipres.xena.kernel.properties.PropertiesManager;
import au.gov.naa.digipres.xena.util.FileUtils;

import com.sun.star.beans.PropertyValue;
import com.sun.star.bridge.XUnoUrlResolver;
import com.sun.star.connection.ConnectionSetupException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XNameAccess;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.UnoRuntime;

/**
 * This class is used by OfficeToXenaOooNormaliser and OfficeTextNormaliser to convert office documents
 * to the required format. For OfficeToXenaOooNormaliser this will be ODF, for OfficeTextNormaliser this will
 * be plain text.
 * 
 * @author Justin Waddell
 *
 */
public class OpenOfficeConverter {

	private final static String OS_X_ARCHITECTURE_NAME = "mac os x";

	private static Logger logger = Logger.getLogger(OfficeToXenaOooNormaliser.class.getName());

	private static XComponent loadDocument(InputStream is, String outputFile, boolean visible, PluginManager pluginManager) throws Exception {
		File input = new File(outputFile);
		try {
			input.deleteOnExit();
			FileOutputStream fos = new FileOutputStream(input);
			byte[] buf = new byte[4096];
			int n;
			while (0 < (n = is.read(buf))) {
				fos.write(buf, 0, n);
			}
			fos.close();
			XComponent rtn = loadDocument(input, visible, pluginManager);
			return rtn;
		} finally {
			input.delete();
		}
	}

	static XComponent loadDocument(File input, boolean visible, PluginManager pluginManager) throws Exception {		
		/*
		 * A desktop environment contains tasks with one or more frames in which components can be loaded. Desktop is
		 * the environment for components which can instantiate within frames.
		 */
		XComponentLoader xcomponentloader =
		    (XComponentLoader) UnoRuntime.queryInterface(XComponentLoader.class, getMultiServiceFactory(pluginManager).createInstance("com.sun.star.frame.Desktop"));

		PropertyValue[] loadProperties = null;
		if (visible) {
			loadProperties = new PropertyValue[0];
		} else {
			loadProperties = new PropertyValue[1];
			loadProperties[0] = new PropertyValue();
			loadProperties[0].Name = "Hidden";
			loadProperties[0].Value = new Boolean(true);
		}
		return xcomponentloader.loadComponentFromURL("file:///" + input.getAbsolutePath().replace('\\', '/'), "_blank", 0, loadProperties);
	}

	private static void startOpenOffice(PluginManager pluginManager) throws XenaException, InterruptedException {
		PropertiesManager propManager = pluginManager.getPropertiesManager();
		String fname = propManager.getPropertyValue(OfficeProperties.OFFICE_PLUGIN_NAME, OfficeProperties.OOO_DIR_PROP_NAME);
		if (fname == null || fname.equals("")) {
			throw new XenaException("OpenOffice.org is not running. OpenOffice.org location not configured.");
		}

		// NeoOffice/OpenOffice.org on OS X has a different program structure than that for Windows and Linux, so we
		// need a special case...
		File sofficeProgram;
		if (System.getProperty("os.name").toLowerCase().equals(OS_X_ARCHITECTURE_NAME)) {
			sofficeProgram = new File(new File(fname, "Contents/MacOS"), "soffice.bin");
		} else {
			if (new File(fname, "program").exists()) {
				sofficeProgram = new File(new File(fname, "program"), "soffice");
			} else {
				// In cases where the program folder doesn't exist (e.g. Arch Linux). 
				sofficeProgram = new File(fname, "soffice");
			}
		}
		List<String> commandList = new ArrayList<String>();
		commandList.add(sofficeProgram.getAbsolutePath());
		commandList.add("-nologo");
		commandList.add("-nodefault");
		commandList.add("-accept=socket,port=8100;urp;");
		String[] commandArr = commandList.toArray(new String[0]);
		try {
			logger.finest("Starting OpenOffice.org process");
			Runtime.getRuntime().exec(commandArr);
		} catch (IOException x) {
			throw new XenaException("Cannot start OpenOffice.org. Try Checking Office Properties. " + sofficeProgram.getAbsolutePath(), x);
		}

		try {
			int sleepSeconds =
			    Integer.parseInt(propManager.getPropertyValue(OfficeProperties.OFFICE_PLUGIN_NAME, OfficeProperties.OOO_SLEEP_PROP_NAME));
			Thread.sleep(1000 * sleepSeconds);
		} catch (NumberFormatException nfex) {
			throw new XenaException("Cannot start OpenOffice.org due to invalid startup sleep time. " + "Try Checking Office Properties. ", nfex);
		}
	}

	/**
	 * Convert the given input into the appropriate format (plain text if a text AIP is required, ODF otherwise).
	 * Put the output into a temporary file and return a reference to this file.
	 * @param input
	 * @param results
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 */
	public static File convertInput(InputSource input, OfficeFileType officeType, NormaliserResults results, NormaliserManager normaliserManager,
	                                boolean isTextConversion) throws SAXException, IOException {
		File output = File.createTempFile("output", "xantmp");

		// Turn the temp file into a directory to hold the correctly named temp file
		String tmpDirName = output.toString();
		output.delete();
		File tmpDir = new File(tmpDirName);
		tmpDir.mkdir();
		// Name the file correctly as the final output
		output = new File(tmpDir, results.getOutputFileName());

		output.deleteOnExit();
		tmpDir.deleteOnExit();

		try {
			// Determine which OOO_xx_OUTPUT_FORMAT we want (Word/Spreadsheet/Presentation etc.
			String converter =
			    isTextConversion ? officeType.getTextConverterName() : officeType.getOfficeConverterName(normaliserManager.getPluginManager()
			            .getPropertiesManager().getPropertyValue(OfficeProperties.OFFICE_PLUGIN_NAME, officeType.getOfficePropertiesName()));

			// Open our office document...
			boolean visible = false;
			String outputFileName = output.toString();
			if (results.isMigrateOnly()) {
				outputFileName = output.toString().substring(0, output.toString().lastIndexOf('.'));
			} else {
				// Try to use the original input files proper name
				File inOutFile = new File(results.getInputSystemId());
				outputFileName = output.getParent() + File.separator + inOutFile.getName().replaceAll("%20", " ");
			}

			XComponent objectDocumentToStore = loadDocument(input.getByteStream(), outputFileName, visible, normaliserManager.getPluginManager());

			// Change the output file is this is a Non-Migrate HTML output run
			if (!results.isMigrateOnly() && converter.equalsIgnoreCase("HTML (StarWriter)")) {
				output = new File(outputFileName + ".html");
			}

			// Getting an object that will offer a simple way to store a document to a URL.
			XStorable xstorable = (XStorable) UnoRuntime.queryInterface(XStorable.class, objectDocumentToStore);
			if (xstorable == null) {
				throw new SAXException("Cannot connect to OpenOffice.org - possibly something wrong with the input file");
			}

			// Preparing properties for converting the document
			PropertyValue propertyvalue[] = new PropertyValue[3];

			// Setting the flag for overwriting
			propertyvalue[0] = new PropertyValue();
			propertyvalue[0].Name = "Overwrite";
			propertyvalue[0].Value = new Boolean(true);

			// Setting the filter name
			propertyvalue[1] = new PropertyValue();
			propertyvalue[1].Name = "FilterName";
			propertyvalue[1].Value = converter;

			// Setting the document title name
			propertyvalue[2] = new PropertyValue();
			propertyvalue[2].Name = "DocumentTitle";
			//propertyvalue[2].Value = results.getOutputFileName();
			propertyvalue[2].Value = output.getName();

			// Storing and converting the document
			try {
				String url = output.toURI().toString();
				xstorable.storeToURL(url, propertyvalue);
			} catch (Exception e) {
				throw new XenaException(
				                        "Cannot convert to open document format. Maybe your OpenOffice.org installation does not have installed: "
				                                + converter
				                                + " or maybe the document is password protected or has some other problem. Try opening in OpenOffice.org manually.",
				                        e);
			}

			// Getting the method dispose() for closing the document
			XComponent xcomponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, xstorable);

			// Closing the converted document
			xcomponent.dispose();
			if (output.length() == 0) {
				throw new XenaException("OpenOffice.org open document file is empty. Do you have OpenOffice.org Java integration installed?");
			}

			// TODO:: IF the convert was to HTML try zipping it up in the website (wsx) extension.  But only really want to do this if it was a non-migrate.  Maybe do it up a level?
			if (!results.isMigrateOnly() && converter.equalsIgnoreCase("HTML (StarWriter)")) {
				output = FileUtils.zipAllFilesLikeHTML(output.getName(), output.getParent(), "wsx");
			}
		} catch (Exception e) {
			logger.log(Level.FINEST, "Problem normalising office document", e);
			throw new SAXException(e);
		}

		return output;
	}

	/**
	 * @return A string identifying the product in the form <Product Name> <version number>
	 * 
	 * TODO check into possibility of making this function and others non-static and having a connection setup to OpenOffice rather than connecting twice.
	 *      If done might have to cater for losing connection
	 */
	public static String getProductId(PluginManager pluginManager) throws XenaException {
		// Get the Configuration Provider
		XMultiServiceFactory xProvider;
		try {
			xProvider = (XMultiServiceFactory) UnoRuntime.queryInterface(
					XMultiServiceFactory.class, getMultiServiceFactory(pluginManager).createInstance("com.sun.star.configuration.ConfigurationProvider"));
		} catch (Exception ex) {
			throw new XenaException(ex);
		}

		// Get the ConfigurationAccess for the org.openoffice.Setup/Product category values
		PropertyValue[] params = new PropertyValue[1];
		params[0] = new PropertyValue();
		params[0].Name = new String("nodepath");
		params[0].Value = new String("org.openoffice.Setup/Product");
		Object xAccess;
		try {
			xAccess = xProvider.createInstanceWithArguments("com.sun.star.configuration.ConfigurationAccess", params); // note that newer versions use NamedValue[] parameters instead but that PropertyValue[] use is still supported
		} catch (Exception ex) {
			throw new XenaException(ex);
		}
		XNameAccess xConfig = (XNameAccess) UnoRuntime.queryInterface(XNameAccess.class, xAccess);
		
		// Get the Product name
		String productName;
		try {
			productName = xConfig.getByName("ooName").toString();
		} catch (NoSuchElementException nsee) {
			throw new XenaException("Could not get Product Name For OpenOffice Converter (ooName)",nsee);
		} catch (WrappedTargetException wte) {
			throw new XenaException(wte);
		}
		
		// Get the Version
		String version;
		try {
			version = xConfig.getByName("ooSetupVersionAboutBox").toString();
		} catch (NoSuchElementException nsee) {
			try {
				version = xConfig.getByName("ooSetupVersion").toString();
			} catch (NoSuchElementException nsee2) {
				throw new XenaException("Could not get Product Version for OpenOffice Converter (ooSetupVersionAboutBox/ooSetupVersion)", nsee2);
			} catch (WrappedTargetException wte) {
				throw new XenaException(wte);
			}
		} catch (WrappedTargetException wte) {
			throw new XenaException(wte);
		}
		
		// return the product name followed by a space then the version
		return productName.concat(" ").concat(version);
	}
	
	private static XMultiServiceFactory getMultiServiceFactory(PluginManager pluginManager) throws XenaException {
		// Bootstraps a servicemanager with the jurt base components registered
		XMultiServiceFactory xmultiservicefactory;
		try {
			xmultiservicefactory = com.sun.star.comp.helper.Bootstrap.createSimpleServiceManager();
		} catch (Exception ex) {
			throw new XenaException(ex);
		}

		// Creates an instance of the component UnoUrlResolver which supports the services specified by the factory.
		Object objectUrlResolver;
		try {
			objectUrlResolver = xmultiservicefactory.createInstance("com.sun.star.bridge.UnoUrlResolver");
		} catch (Exception ex) {
			throw new XenaException(ex);
		}
		
		// Create a new url resolver
		XUnoUrlResolver xurlresolver = (XUnoUrlResolver) UnoRuntime.queryInterface(XUnoUrlResolver.class, objectUrlResolver);

		// Resolves an object that is specified as follow:
		// uno:<connection description>;<protocol description>;<initial object name>
		Object objectInitial = null;
		String address = "uno:socket,host=localhost,port=8100;urp;StarOffice.ServiceManager";
		try {
			objectInitial = xurlresolver.resolve(address);
		} catch (com.sun.star.connection.NoConnectException ncex) {
			// Could not connect to OpenOffice.org, so start it up and try again
			try {
				startOpenOffice(pluginManager);
				objectInitial = xurlresolver.resolve(address);
			} catch (XenaException xex) {
				// If it fails again for any reason, just bail
				throw xex;
			} catch (Exception ex) {
				// If it fails again for any reason, just bail
				throw new XenaException(ex);
			}
		} catch (com.sun.star.uno.RuntimeException rtex) {
			// Could not connect to OpenOffice.org, so start it up and try again
			try {
				startOpenOffice(pluginManager);
				objectInitial = xurlresolver.resolve(address);
			} catch (XenaException xex) {
				// If it fails again for any reason, just bail
				throw xex;
			} catch (Exception ex) {
				// If it fails again for any reason, just bail
				throw new XenaException(ex);
			}
		} catch (IllegalArgumentException ex) {
			throw new XenaException(ex);
		} catch (ConnectionSetupException ex) {
			throw new XenaException(ex);
		}

		// Create a service manager from the initial object
		xmultiservicefactory = (XMultiServiceFactory) UnoRuntime.queryInterface(XMultiServiceFactory.class, objectInitial);
		
		return xmultiservicefactory;
	}

}
