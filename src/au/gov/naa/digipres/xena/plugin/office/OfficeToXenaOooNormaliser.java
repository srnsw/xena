package au.gov.naa.digipres.xena.plugin.office;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.helper.AbstractJdomNormaliser;
import au.gov.naa.digipres.xena.kernel.PluginManager;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.filenamer.FileNamer;
import au.gov.naa.digipres.xena.kernel.filenamer.FileNamerManager;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.properties.PropertiesManager;
import au.gov.naa.digipres.xena.kernel.type.FileType;
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
public class OfficeToXenaOooNormaliser extends AbstractJdomNormaliser {
	/**
	 */

	public OfficeToXenaOooNormaliser() {
	}

	public String getName() {
		return "Office";
	}

	static XComponent loadDocument(InputStream is, File output, boolean visible) throws Exception {
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
			XComponent rtn = loadDocument(input, visible);
			return rtn;
		} finally {
			input.delete();
		}
	}

	public static void main(String[] args) throws Exception {
		System.out.println(System.getProperty("user.dir"));
		List<String> plg = new ArrayList<String>();
		plg.add("xena/plugin/naa");
		plg.add("xena/plugin/office");
		plg.add("xena/plugin/base");
		PluginManager.singleton().loadPlugins(plg);
		for (int i = 0; i < args.length; i++) {
			OfficeToXenaOooNormaliser normaliser = new OfficeToXenaOooNormaliser();
			SAXTransformerFactory tf = (SAXTransformerFactory)SAXTransformerFactory.newInstance();
			TransformerHandler writer = tf.newTransformerHandler();
			XenaInputSource input = new XenaInputSource(args[i], null);
			FileType type = GuesserManager.singleton().mostLikelyType(input);
			input.setType(type);
			FileNamer fn = FileNamerManager.singleton().getFileNamerFromPrefs();
			File xenaFile = fn.makeNewXenaFile(normaliser, input, FileNamer.XENA_DEFAULT_EXTENSION);
			FileOutputStream out = new FileOutputStream(xenaFile);
			OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");
			StreamResult streamResult = new StreamResult(osw);
			writer.setResult(streamResult);
			normaliser.setContentHandler(writer);
			normaliser.parse(input);
		}
	}

	static XComponent loadDocument(File input, boolean visible) throws Exception {
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
					PluginManager.singleton().getPropertiesManager();
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
				// On my machine 3000 seems enough. Sometimes needs 10000 to allow for
				// slower machines to start openoffice.
				try
				{
					int sleepMilli = 
						Integer.parseInt(propManager.getPropertyValue(OfficeProperties.OFFICE_PLUGIN_NAME,
						                					          OfficeProperties.OOO_SLEEP_PROP_NAME));
					Thread.sleep(sleepMilli);
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

	public Element normalise(InputSource input) throws SAXException, IOException {
		File output = File.createTempFile("output", "xantmp");
		try {
			try {
				Type type = ((XenaInputSource)input).getType();
				output.deleteOnExit();
				// This is a hack. For some wierd reason, Presentations
				// crash often when we make it invisible. Hopefully this can
				// be removed at some stage.
				boolean visible = (type instanceof PresentationFileType);
//				boolean visible = false;
				XComponent objectDocumentToStore = loadDocument(input.getByteStream(), output, visible);
				// Getting an object that will offer a simple way to store a document to a URL.
				XStorable xstorable =
					(XStorable)UnoRuntime.queryInterface(XStorable.class,
														 objectDocumentToStore);
				if (xstorable == null) {
					throw new SAXException("Cannot connect to OpenOffice.org: xstorable null");
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

				String convertor;
				if (type instanceof WordProcessorFileType) {
//								  convertor = "swriter: Flat XML File";
					convertor = "Flat XML File";
				} else if (type instanceof SpreadsheetFileType) {
					convertor = "Flat XML File (Calc)";
				} else if (type instanceof PresentationFileType) {
					convertor = "Flat XML File (Impress)";
				} else if (type instanceof SylkFileType) {
					convertor = "SYLK";
				} else {
					throw new XenaException("Unknown type of Office file");
				}
				propertyvalue[1].Value = convertor;

				// Storing and converting the document
				try {
					String url = "file:///" + output.getAbsolutePath().replace('\\', '/');
					/*					File file = new File(new URI(url));
					  FileWriter fw = new FileWriter(file);
					  fw.write("<office></office>");
					  fw.close(); */
					xstorable.storeAsURL(url, propertyvalue);
				} catch (Exception e) {
					throw new XenaException("Cannot convert to Flat XML. Maybe your OpenOffice.org installation does not have installed: " +
											convertor +
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
					throw new XenaException("OpenOffice XML file is empty. Do you have OpenOffice Java integration installed?");
				}
			} catch (Exception e) {
				throw new SAXException(e);
			}

			FileInputStream fis = null;
			SAXBuilder builder = new SAXBuilder();
			try {
				fis = new FileInputStream(output);
				builder.setValidation(false);

				Document doc = builder.build(fis);
				return doc.detachRootElement();
			} catch (JDOMException e) {
				throw new SAXException("Problem parsing OpenOffice XML file", e);
			} finally {
				if (fis != null) {
					fis.close();
				}
			}
		} finally {
			output.delete();
		}
	}
}
