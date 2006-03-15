package au.gov.naa.digipres.xena.plugin.html;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.helper.AbstractJdomNormaliser;
import au.gov.naa.digipres.xena.helper.ByteArrayURLConnection;
import au.gov.naa.digipres.xena.helper.JdomUtil;
import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.javatools.JarPreferences;
import au.gov.naa.digipres.xena.kernel.CharsetDetector;
import au.gov.naa.digipres.xena.kernel.LegacyXenaCode;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.filenamer.FileNamer;
import au.gov.naa.digipres.xena.kernel.filenamer.FileNamerManager;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserDataStore;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserManager;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.type.FileType;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;
import au.gov.naa.digipres.xena.plugin.html.javatools.wget.Wget;
import au.gov.naa.digipres.xena.plugin.html.javatools.wget.WgetErrorListener;
import au.gov.naa.digipres.xena.plugin.html.javatools.wget.WgetException;
import au.gov.naa.digipres.xena.plugin.html.javatools.wget.WgetPatternURLValidator;
import au.gov.naa.digipres.xena.plugin.html.javatools.wget.WgetProcessUrl;

/**
 * Normalise a web site via the HTTP protocol. The class is capable of spidering
 * the web site by parsing the HTML and looking for links, images and other
 * resources, and normalising them as appropriate.
 *
 * @author Chris Bitmead
 */
public class HttpToXenaHttpNormaliser extends AbstractJdomNormaliser {
	final static String URI = "http://preservation.naa.gov.au/http/1.0";

	final static String PREFIX = "http";

	protected Namespace ns = Namespace.getNamespace(PREFIX, URI);

	protected File root;

	public URL url;

	public boolean continueRun;

	protected WgetPatternURLValidator wgetPattern = new WgetPatternURLValidator();

	public String forceNormaliser;

	public boolean followLinks;

	public boolean getResources;

	public boolean forced;

	public boolean canLeaveHost;

	public String hostPattern = "";

	public String protocolPattern = "";
	
	private Logger logger = Logger.getLogger(this.getClass().getName());

	public HttpToXenaHttpNormaliser() throws XenaException {
		Type binaryType = TypeManager.singleton().lookup("Binary");
		forceNormaliser = ((Class)NormaliserManager.singleton().lookupList(binaryType).get(0)).getName();
	}

	public String getName() {
		return "HTTP";
	}

	public static File findRoot() throws XenaException {
		JarPreferences prefs = (JarPreferences)JarPreferences.userNodeForPackage(NormaliserManager.class);
		String origDir = prefs.get(NormaliserManager.DESTINATION_DIR_STRING, null);
		if (origDir == null) {
			throw new XenaException("Destination Directory are not set");
		}
		return new File(origDir);
	}

	public Element normalise(InputSource input) throws SAXException, IOException {
		try {
			root = findRoot();
			WgetNormaliser wn = new WgetNormaliser();
			MyWget wget = new MyWget(wn);
			if (!followLinks && !getResources) {
				wget.setSpiderURLs(false);
			} else if (!followLinks && getResources) {
				wget.removeFollowTag(Wget.A_HREF);
				wget.removeFollowTag(Wget.AREA_HREF);
				wget.setSpiderURLs(true);
			} else if (followLinks && !getResources) {
				wget.setSpiderURLs(true);
				wget.removeAllTags();
				wget.addFollowTag(Wget.A_HREF);
				wget.removeFollowTag(Wget.AREA_HREF);
			} else {
				wget.setSpiderURLs(true);
			}
			wget.addErrorListener(new WgetErrorHandler());
			wget.setUrlValidator(wgetPattern);

			if (canLeaveHost) {
				wgetPattern.setHost(null);
				wgetPattern.setHostPattern(hostPattern == null || hostPattern.equals("") ? null : hostPattern);
			} else {
				wgetPattern.setHost(url.getHost());
				wgetPattern.setHostPattern(null);
			}
			wgetPattern.setProtocolPattern(protocolPattern);
			wget.recursiveGet(url);
			HttpView.MapAndTopList matl = HttpView.createURLMap(root);
			return HttpView.saveURLMap(root, matl);
		} catch (WgetException ex) {
			throw new SAXException(ex);
		} catch (XenaException ex) {
			throw new SAXException(ex);
		}
	}

	public WgetPatternURLValidator getWgetPattern() {
		return wgetPattern;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
		wgetPattern.setHost(url.getHost());
	}

	public boolean isContinueRun() {
		return continueRun;
	}

	public void setContinueRun(boolean continueRun) {
		this.continueRun = continueRun;
	}

	public String getForceNormaliser() {
		return forceNormaliser;
	}

	public void setForceNormaliser(String forceNormaliser) {
		this.forceNormaliser = forceNormaliser;
	}

	class WgetErrorHandler implements WgetErrorListener {
		public void errorEvent(URL url, String urlString, Exception ex) throws WgetException {
			String us = url == null ? urlString : url.toExternalForm();
			logger.log(Level.FINER, "Wget error with " + us, ex);
		}
	}

	public File getErrorFile(File xenaFile) throws XenaException {
		try {
			File errorFileDir = LegacyXenaCode.getBaseDirectory(NormaliserManager.ERROR_DIR_STRING);
			File destFileDir = LegacyXenaCode.getBaseDirectory(NormaliserManager.DESTINATION_DIR_STRING);
			if (errorFileDir == null) {
				return null;
			}
			return FileName.changeRelative(xenaFile, destFileDir, errorFileDir);
		} catch (IOException x) {
			throw new XenaException(x);
		}
	}

	public File changeRelative(File xenaFile, File origDir, File newDir) throws XenaException {
		if (newDir == null) {
			return null;
		} else {
			try {
				String relativePath = FileName.relativeTo(origDir, xenaFile);
				return new File(newDir, relativePath);
			} catch (IOException x) {
				throw new XenaException(x);
			}
		}
	}

	public boolean isFollowLinks() {
		return followLinks;
	}

	public void setFollowLinks(boolean followLinks) {
		this.followLinks = followLinks;
	}

	public boolean isGetResources() {
		return getResources;
	}

	public void setGetResources(boolean getResources) {
		this.getResources = getResources;
	}

	public boolean isForced() {
		return forced;
	}

	public void setForced(boolean forced) {
		this.forced = forced;
	}

	public boolean isCanLeaveHost() {
		return canLeaveHost;
	}

	public void setCanLeaveHost(boolean canLeaveHost) {
		this.canLeaveHost = canLeaveHost;
	}

	public String getHostPattern() {
		return hostPattern;
	}

	public void setHostPattern(String hostPattern) {
		this.hostPattern = hostPattern;
	}

	public String getProtocolPattern() {
		return protocolPattern;
	}

	public void setProtocolPattern(String protocolPattern) {
		this.protocolPattern = protocolPattern;
	}

	class WgetNormaliser implements WgetProcessUrl {
		public WgetNormaliser() {
		}

		public void process(URLConnection connection) throws IOException, WgetException {
			try {
				// If we are simulating the connection in continue mode
				// We don't want to re-normalise and store it.
				if (connection instanceof HttpURLConnectionSimulator) {
					logger.finer("URL reprocessed: " + 
					             connection.getURL().toExternalForm());
				} else {
					ByteArrayURLConnection cache = new ByteArrayURLConnection(connection);

					XenaInputSource xis = new XenaInputSource(cache, null);
					FileType type = null;
					type = (FileType)GuesserManager.singleton().mostLikelyType(xis);
					XMLReader normaliser;
					if (isForced()) {
						normaliser = (XMLReader)NormaliserManager.singleton().lookupByClassName(forceNormaliser);
					} else {
						normaliser = (XMLReader)NormaliserManager.singleton().lookup(type);
					}
					logger.finest("URL successfully processed: " + 
					              connection.getURL().toExternalForm());
					normaliser.setProperty("http://xena/log", getProperty("http://xena/log"));
					xis.setType(type);
					xis.setEncoding(connection.getContentEncoding());
					if (xis.getEncoding() == null) {
						xis.setEncoding(CharsetDetector.mustGuessCharSet(new ByteArrayInputStream(cache.getBytes()), 2 ^ 16));
					}
					FileNamer urln = FileNamerManager.singleton().getFileNamerFromPrefs();
					File file = null;
					XenaInputSource sourceName = new XenaInputSource(connection.getURL().toExternalForm(), type);
					try {
						file = urln.makeNewXenaFile(normaliser, sourceName, FileNamer.XENA_DEFAULT_EXTENSION);
					} catch (XenaException x) {
						throw new WgetException(x);
					}
					AbstractNormaliser dummy = new AbstractNormaliser() {
						public void parse(InputSource input, NormaliserResults results) 
						throws IOException, SAXException {}

						public String getName() {
							return "DUMMY";
						}
					};
					NormaliserDataStore ns = NormaliserManager.singleton().newOutputHandler(dummy, xis, true);
					ContentHandler wrapper = NormaliserManager.singleton().wrapTheNormaliser(dummy, xis);
					if (wrapper != null) {
						wrapper.startDocument();
					}
					ContentHandler ch = ns.getTransformerHandler();
					ns.getTransformerHandler().startDocument();
					AttributesImpl att = new AttributesImpl();
					AttributesImpl httpatt = new AttributesImpl();
					if (connection.getURL().equals(url)) {
						httpatt.addAttribute(URI, "top", PREFIX + ":top", "CDATA", "true");
					}
					ch.startElement(URI, "http", PREFIX + ":http", httpatt);
					ch.startElement(URI, "uri", PREFIX + ":uri", att);
					char[] curl = connection.getURL().toExternalForm().toCharArray();
					ch.characters(curl, 0, curl.length);
					ch.endElement(URI, "uri", PREFIX + ":uri");
					ch.startElement(URI, "headers", PREFIX + ":headers", att);
					String key;
					String value;
					// Yep, this should be "&" and not "&&".
					for (int count = 0; !((value = connection.getHeaderField(count)) == null & (key = connection.getHeaderFieldKey(count)) == null);
						 count++) {
						AttributesImpl atthead = new AttributesImpl();
						if (key != null) {
							atthead.addAttribute(URI, "name", PREFIX + ":name", "CDATA", key);
						}
						ch.startElement(URI, "header", PREFIX + ":header", atthead);
						ch.characters(value.toCharArray(), 0, value.length());
						ch.endElement(URI, "header", PREFIX + ":header");
					}
					ch.endElement(URI, "headers", PREFIX + ":headers");

					ch.startElement(URI, "data", PREFIX + ":data", att);
					Element data = null;
					try {
						data = JdomUtil.parseToElement(normaliser, xis);
					} catch (SAXException ex) {
						// If the document is empty, we don't consider it an error.
						// This can happen, especially in redirections.
						if (ex.getCause() == null || ex.getCause().toString().indexOf("Document root element is missing") < 0) {
							logger.log(Level.FINER, 
							           "No normaliser for " + xis.getSystemId(),
							           ex);
							data = getBinary(xis);
							storeErrorFile(connection.getURL(), file);
						}
					} catch (IOException ex) {
						data = getBinary(xis);
						storeErrorFile(connection.getURL(), file);
					}
					/* Even when normalisation failed, we still write the output file
					 in binary form for two reasons. Firstly, it is better than nothing.
					 Secondly, the writing of the error file allows for its correction
					 and re-simulating the http connection */
					if (data != null) {
						JdomUtil.writeElement(ch, data);
					}
					ch.endElement(URI, "data", PREFIX + ":data");
					ch.endElement(URI, "http", PREFIX + ":http");
					if (wrapper != null) {
						wrapper.endDocument();
					}
					ns.getTransformerHandler().endDocument();
				}
			} catch (SAXException x) {
				throw new WgetException(x);
			} catch (JDOMException x) {
				throw new WgetException(x);
			} catch (XenaException x) {
				throw new WgetException(x);
			} 
		}

		Element getBinary(InputSource xis) throws XenaException {
			try {
				Type binaryType = TypeManager.singleton().lookup("Binary");
				List l = NormaliserManager.singleton().lookupList(binaryType);
				if (1 <= l.size()) {
					Class cbn = (Class)l.get(0);
					XMLReader bn = (XMLReader)NormaliserManager.singleton().lookupByClass(cbn);
					return JdomUtil.parseToElement(bn, xis);
				}
			} catch (IOException x) {
				x.printStackTrace();
			} catch (SAXException x) {
				x.printStackTrace();
			}
			return null;
		}

		void storeErrorFile(URL url, File file) throws XenaException {
			try {
				File errorFile = getErrorFile(file);
				if (!errorFile.exists()) {
					URLConnection connection = url.openConnection();
					InputStream is = connection.getInputStream();
					OutputStream os = new FileOutputStream(errorFile);
					byte[] bytes = new byte[4096];
					int c;
					while (0 <= (c = is.read(bytes))) {
						os.write(bytes, 0, c);
					}
					os.close();
					is.close();
				}
			} catch (IOException x) {
				logger.log(Level.FINER, 
				           "Problem storing " + url.toExternalForm(),
				           x);
			}
		}
	}

	public class MyWget extends Wget {
		FileNamer urln = FileNamerManager.singleton().getFileNamerFromPrefs();

		public MyWget(WgetNormaliser wn) {
			super(wn);
		}

		public URLConnection openConnection(URL url) throws IOException {
			if (continueRun) {
				try {
					XenaInputSource nameSource = new XenaInputSource(url.toExternalForm(), null);
					File file = urln.makeNewXenaFile(HttpToXenaHttpNormaliser.this, nameSource, FileNamer.XENA_DEFAULT_EXTENSION);
					if (file.exists()) {
						HttpURLConnectionSimulator rtn = null;
						rtn = new HttpURLConnectionSimulator(url, file.toURL());
						File errorFile = getErrorFile(file);
						if (errorFile.exists()) {
							rtn.setFile(errorFile);
						}
						return rtn;
					}
				} catch (XenaException x) {
					throw new IOException(x.toString());
				} catch (JDOMException x) {
					throw new IOException(x.toString());
				} catch (IOException x) {
					throw new IOException(x.toString());
				}
			}
			return url.openConnection();
		}
	}
}
