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

import com.sun.star.beans.PropertyValue;
import com.sun.star.bridge.XUnoUrlResolver;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XStorable;
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

	private static XComponent loadDocument(InputStream is, String extension, boolean visible, PluginManager pluginManager) throws Exception {
		File input = File.createTempFile("input", "." + extension);
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
		}

		// Create a service manager from the initial object
		xmultiservicefactory = (XMultiServiceFactory) UnoRuntime.queryInterface(XMultiServiceFactory.class, objectInitial);

		/*
		 * A desktop environment contains tasks with one or more frames in which components can be loaded. Desktop is
		 * the environment for components which can instantiate within frames.
		 */
		XComponentLoader xcomponentloader =
		    (XComponentLoader) UnoRuntime.queryInterface(XComponentLoader.class, xmultiservicefactory.createInstance("com.sun.star.frame.Desktop"));

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
		output.deleteOnExit();

		try {
			String converter = isTextConversion ? officeType.getTextConverterName() : officeType.getOfficeConverterName();

			// Open our office document...
			boolean visible = false;
			XComponent objectDocumentToStore =
			    loadDocument(input.getByteStream(), officeType.fileExtension(), visible, normaliserManager.getPluginManager());

			// Getting an object that will offer a simple way to store a document to a URL.
			XStorable xstorable = (XStorable) UnoRuntime.queryInterface(XStorable.class, objectDocumentToStore);
			if (xstorable == null) {
				throw new SAXException("Cannot connect to OpenOffice.org - possibly something wrong with the input file");
			}

			// Preparing properties for converting the document
			PropertyValue propertyvalue[] = new PropertyValue[2];

			// Setting the flag for overwriting
			propertyvalue[0] = new PropertyValue();
			propertyvalue[0].Name = "Overwrite";
			propertyvalue[0].Value = new Boolean(true);

			// Setting the filter name
			propertyvalue[1] = new PropertyValue();
			propertyvalue[1].Name = "FilterName";
			propertyvalue[1].Value = converter;

			// Storing and converting the document
			try {
				String url = "file:///" + output.getAbsolutePath().replace('\\', '/');
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
		} catch (Exception e) {
			logger.log(Level.FINEST, "Problem normalising office document", e);
			throw new SAXException(e);
		}

		return output;
	}

}
