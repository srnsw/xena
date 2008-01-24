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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.plugin.PluginManager;
import au.gov.naa.digipres.xena.kernel.properties.PropertiesManager;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.util.InputStreamEncoder;

import com.sun.star.beans.PropertyValue;
import com.sun.star.bridge.XUnoUrlResolver;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.UnoRuntime;

/**
 * Convert office documents to the Xena office (i.e. OpenOffice.org flat) file
 * format.
 *
 */
public class OfficeToXenaOooNormaliser extends AbstractNormaliser {

	public final static String OPEN_DOCUMENT_PREFIX = "opendocument";
	private final static String OPEN_DOCUMENT_URI = "http://preservation.naa.gov.au/odf/1.0";
	public final static String DOCUMENT_TYPE_TAG_NAME = "type";
	public final static String DOCUMENT_EXTENSION_TAG_NAME = "extension";
	public final static String PROCESS_DESCRIPTION_TAG_NAME = "description";
	private final static String OS_X_ARCHITECTURE_NAME = "mac os x";

	private static Logger logger = Logger.getLogger(OfficeToXenaOooNormaliser.class.getName());

	private final static String DESCRIPTION =
	    "The following data is a MIME-compliant (RFC 1421) PEM base64 (RFC 1421) representation of an Open Document Format "
	            + "(ISO 26300, Version 1.0) document, produced by Open Office version 2.0.";

	public OfficeToXenaOooNormaliser() {
		// Nothing to do
	}

	@Override
	public String getName() {
		return "Office";
	}

	private XComponent loadDocument(InputStream is, String extension, boolean visible, PluginManager pluginManager) throws Exception {
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
			// Could not connect to OpenOffice, so start it up and try again
			try {
				startOpenOffice(pluginManager);
				objectInitial = xurlresolver.resolve(address);
			} catch (Exception ex) {
				// If it fails again for any reason, just bail
				throw new XenaException("Could not start OpenOffice", ex);
			}
		} catch (com.sun.star.uno.RuntimeException rtex) {
			// Could not connect to OpenOffice, so start it up and try again
			try {
				startOpenOffice(pluginManager);
				objectInitial = xurlresolver.resolve(address);
			} catch (Exception ex) {
				// If it fails again for any reason, just bail
				throw new XenaException("Could not start OpenOffice", ex);
			}
		}

		// Create a service manager from the initial object
		xmultiservicefactory = (XMultiServiceFactory) UnoRuntime.queryInterface(XMultiServiceFactory.class, objectInitial);

		/*
		 * A desktop environment contains tasks with one or more frames in which components can be loaded. Desktop is
		 * the environment for components which can instanciate within frames.
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

		// NeoOffice/OpenOffice on OS X has a different program structure than that for Windows and Linux, so we
		// need a special case...
		File sofficeProgram;
		if (System.getProperty("os.name").toLowerCase().equals(OS_X_ARCHITECTURE_NAME)) {
			sofficeProgram = new File(new File(fname, "Contents/MacOS"), "soffice.bin");
		} else {
			sofficeProgram = new File(new File(fname, "program"), "soffice");
		}
		List<String> commandList = new ArrayList<String>();
		commandList.add(sofficeProgram.getAbsolutePath());
		commandList.add("-nologo");
		commandList.add("-nodefault");
		commandList.add("-accept=socket,port=8100;urp;");
		String[] commandArr = commandList.toArray(new String[0]);
		try {
			logger.finest("Starting OpenOffice process");
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

	@Override
	public void parse(InputSource input, NormaliserResults results) throws SAXException, IOException {
		File output = File.createTempFile("output", "xantmp");

		OfficeFileType officeType = null;
		String converter;

		XenaInputSource xis = (XenaInputSource) input;
		Type type = xis.getType();
		/*
		 * This is slightly broken --> if the type is null, then we have a problem. At least this way there is some way
		 * of ensure type != null If the normaliser has been specified though, we really should have the type as not
		 * null!
		 */
		if (type == null) {
			GuesserManager gm = getNormaliserManager().getPluginManager().getGuesserManager();
			Guess guess = gm.getBestGuess(xis);
			xis.setType(guess.getType());
			type = guess.getType();
		}

		try {
			// Verify that we are actually getting an office type input source.
			if (type instanceof OfficeFileType) {
				officeType = (OfficeFileType) type;
				converter = officeType.getOfficeConverterName();
			} else {
				throw new XenaException("Invalid FileType - must be an OfficeFileType. To override, the type should be set manually.");
			}

			output.deleteOnExit();
			boolean visible = false;

			// Open our office document...
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
				throw new XenaException("OpenOffice open document file is empty. Do you have OpenOffice Java integration installed?");
			}
		} catch (Exception e) {
			logger.log(Level.FINEST, "Problem normalisting office document", e);
			throw new SAXException(e);
		}
		// Check file was created successfully by opening up the zip and checking for at least one entry
		// Base64 encode the file and write out to content handler
		try {
			ContentHandler ch = getContentHandler();
			AttributesImpl att = new AttributesImpl();
			String tagURI = OPEN_DOCUMENT_URI;
			String tagPrefix = OPEN_DOCUMENT_PREFIX;
			ZipFile openDocumentZip = new ZipFile(output);
			// Not sure if this is even possible, but worth checking I guess...
			if (openDocumentZip.size() == 0) {
				throw new IOException("An empty document was created by OpenOffice");
			}
			att.addAttribute(OPEN_DOCUMENT_URI, PROCESS_DESCRIPTION_TAG_NAME, PROCESS_DESCRIPTION_TAG_NAME, "CDATA", DESCRIPTION);
			att.addAttribute(OPEN_DOCUMENT_URI, DOCUMENT_TYPE_TAG_NAME, DOCUMENT_TYPE_TAG_NAME, "CDATA", type.getName());
			att.addAttribute(OPEN_DOCUMENT_URI, DOCUMENT_EXTENSION_TAG_NAME, DOCUMENT_EXTENSION_TAG_NAME, "CDATA", officeType.fileExtension());

			InputStream is = new FileInputStream(output);
			ch.startElement(tagURI, tagPrefix, tagPrefix + ":" + tagPrefix, att);
			InputStreamEncoder.base64Encode(is, ch);
			ch.endElement(tagURI, tagPrefix, tagPrefix + ":" + tagPrefix);
		} catch (ZipException ex) {
			throw new IOException("OpenOffice could not create the open document file");
		} finally {
			output.delete();
		}
	}
}
