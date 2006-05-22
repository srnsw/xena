package au.gov.naa.digipres.xena.plugin.office;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.plugin.PluginManager;
import au.gov.naa.digipres.xena.kernel.properties.PropertiesManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

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
 * @author Chris Bitmead
 */
public class OfficeToXenaOooNormaliser extends AbstractNormaliser {
	
    public final static String OPEN_DOCUMENT_PREFIX = "opendocument";
    private final static String OPEN_DOCUMENT_URI = "http://preservation.naa.gov.au/odf/1.0";
    
    /**
     * RFC suggests max of 76 characters per line
     */
    public static final int MAX_BASE64_RFC_LINE_LENGTH = 76;

    /**
     * Base64 turns 3 characters into 4...
     */
    public static final int CHUNK_SIZE = (MAX_BASE64_RFC_LINE_LENGTH * 3) / 4;

    public OfficeToXenaOooNormaliser() {
	}

	public String getName() {
		return "Office";
	}

	static XComponent loadDocument(InputStream is, File output, boolean visible, PluginManager pluginManager) throws Exception {
		File input = File.createTempFile("input", "xantmp");
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
		 *  Bootstraps a servicemanager with the jurt base components
		 *  registered
		 */
		XMultiServiceFactory xmultiservicefactory =
			com.sun.star.comp.helper.Bootstrap.createSimpleServiceManager();

		/*
		 *  Creates an instance of the component UnoUrlResolver which
		 *  supports the services specified by the factory.
		 */
		Object objectUrlResolver = xmultiservicefactory.createInstance(
			"com.sun.star.bridge.UnoUrlResolver");

		// Create a new url resolver
		XUnoUrlResolver xurlresolver = (XUnoUrlResolver)
			UnoRuntime.queryInterface(XUnoUrlResolver.class,
									  objectUrlResolver);

		// Resolves an object that is specified as follow:
		// uno:<connection description>;<protocol description>;<initial object name>
		Object objectInitial = null;
		String address = "uno:socket,host=localhost,port=8100;urp;StarOffice.ServiceManager";
		try {
			objectInitial = xurlresolver.resolve(address);
		} catch (com.sun.star.connection.NoConnectException ex) {
			try {
				PropertiesManager propManager = 
                    pluginManager.getPropertiesManager();
				String fname = 
					propManager.getPropertyValue(OfficeProperties.OFFICE_PLUGIN_NAME,
					                             OfficeProperties.OOO_DIR_PROP_NAME);
				if (fname == null || fname.equals("")) {
					throw new XenaException("OpenOffice.org is not running. OpenOffice.org location not configured.");
				}
				File quickstart = new File(new File(fname, "program"), "quickstart");
				try {
					Runtime.getRuntime().exec(quickstart.toString());
				} catch (IOException x) {
					throw new XenaException("Cannot start OpenOffice.org. Try Checking Office Properties. " + quickstart.toString(), x);
				}
				
				try
				{
					int sleepSeconds = 
						Integer.parseInt(propManager.getPropertyValue(OfficeProperties.OFFICE_PLUGIN_NAME,
						                					          OfficeProperties.OOO_SLEEP_PROP_NAME));
					Thread.sleep(1000 * sleepSeconds);
				}
				catch (NumberFormatException nfex)
				{
					throw new XenaException("Cannot start OpenOffice.org due to invalid startup sleep time. " +
					                        "Try Checking Office Properties. ", 
					                        nfex);					
				}
				
				objectInitial = xurlresolver.resolve(address);
			} catch (com.sun.star.connection.NoConnectException ex2) {
				// I think it is 1.1.3 or thereabouts that started throwing this error
				throw new XenaException("OpenOffice.org is not running", ex2);
			} catch (com.sun.star.uno.RuntimeException ex2) {
				// I think it was pre 1.1.2 or thereabouts that threw this error
				throw new XenaException("OpenOffice.org is not running", ex2);
			}
		}

		// Create a service manager from the initial object
		xmultiservicefactory = (XMultiServiceFactory)
			UnoRuntime.queryInterface(XMultiServiceFactory.class,
									  objectInitial);

		/*
		 *  A desktop environment contains tasks with one or more
		 *  frames in which components can be loaded. Desktop is the
		 *  environment for components which can instanciate within
		 *  frames.
		 */
		XComponentLoader xcomponentloader = (XComponentLoader)
			UnoRuntime.queryInterface(XComponentLoader.class,
									  xmultiservicefactory.createInstance(
										  "com.sun.star.frame.Desktop"));

		PropertyValue[] loadProperties = null;
		if (visible) {
			loadProperties = new PropertyValue[0];
		} else {
			loadProperties = new PropertyValue[1];
			loadProperties[0] = new PropertyValue();
			loadProperties[0].Name = "Hidden";
			loadProperties[0].Value = new Boolean(true);
		}

		return
			xcomponentloader.loadComponentFromURL(
				"file:///" + input.getAbsolutePath().replace('\\', '/'), "_blank", 0, loadProperties);
	}

	public void parse(InputSource input, NormaliserResults results) throws SAXException, IOException {
		File output = File.createTempFile("output", "xantmp");
		Type type = ((XenaInputSource)input).getType();
		try {
			try {
				output.deleteOnExit();
				// This is a hack. For some wierd reason, Presentations
				// crash often when we make it invisible. Hopefully this can
				// be removed at some stage.
//				boolean visible = (type instanceof PresentationFileType);
				boolean visible = false;
				XComponent objectDocumentToStore = loadDocument(input.getByteStream(), output, visible, normaliserManager.getPluginManager());
				// Getting an object that will offer a simple way to store a document to a URL.
				XStorable xstorable =
					(XStorable)UnoRuntime.queryInterface(XStorable.class,
														 objectDocumentToStore);
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

				String converter;
				if (type instanceof OfficeFileType) 
				{
					OfficeFileType officeType = (OfficeFileType)type;
					converter = officeType.getOfficeConverterName();
				} 
				else 
				{
					throw new XenaException("Invalid FileType - must be an OfficeFileType");
				}
				propertyvalue[1].Value = converter;

				// Storing and converting the document
				try {
					String url = "file:///" + output.getAbsolutePath().replace('\\', '/');
					/*					File file = new File(new URI(url));
					  FileWriter fw = new FileWriter(file);
					  fw.write("<office></office>");
					  fw.close(); */
					xstorable.storeToURL(url, propertyvalue);
				} catch (Exception e) {
					throw new XenaException("Cannot convert to open document format. Maybe your OpenOffice.org installation does not have installed: " +
											converter +
											" or maybe the document is password protected or has some other problem. Try opening in OpenOffice.org manually.",
											e);
				}

				// Getting the method dispose() for closing the document
				XComponent xcomponent =
					(XComponent)UnoRuntime.queryInterface(XComponent.class,
														  xstorable);

				// Closing the converted document
				xcomponent.dispose();

				if (output.length() == 0) {
					throw new XenaException("OpenOffice open document file is empty. Do you have OpenOffice Java integration installed?");
				}
			} catch (Exception e) {
				throw new SAXException(e);
			}
			
			// Check file was created successfully by opening up the zip and checking for at least one entry
			// Base64 encode the file and write out to content handler
			try
			{
		        ContentHandler ch = getContentHandler();
		        AttributesImpl att = new AttributesImpl();
		        
				String tagURI = OPEN_DOCUMENT_URI;
				String tagPrefix = OPEN_DOCUMENT_PREFIX;

				ZipFile openDocumentZip = new ZipFile(output);
				
				// Not sure if this is even possible, but worth checking I guess...
				if (openDocumentZip.size() == 0)
				{
					throw new IOException("An empty document was created by OpenOffice");
				}
					
					
		        ch.startElement(tagURI, tagPrefix, tagPrefix + ":" + tagPrefix, att);

		        sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
		        InputStream is = new FileInputStream(output);

		        // 80 characters makes nice looking output
		        byte[] buf = new byte[CHUNK_SIZE];
		        int c;
		        while (0 <= (c = is.read(buf))) {
		            byte[] tbuf = buf;
		            if (c < buf.length) {
		                tbuf = new byte[c];
		                System.arraycopy(buf, 0, tbuf, 0, c);
		            }
		            char[] chs = encoder.encode(tbuf).toCharArray();
		            ch.characters(chs, 0, chs.length);
		        }
		        ch.endElement(tagURI, tagPrefix, tagPrefix + ":" + tagPrefix);
				
			}
			catch(ZipException ex)
			{
				throw new IOException("OpenOffice could not create the open document file");
			}
			

		} finally {
			output.delete();
		}
	}
}
