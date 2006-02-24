package au.gov.naa.digipres.xena.plugin.html.javatools.wget;
import java.io.*;
import java.net.*;
import org.jdom.*;
import org.jdom.input.*;
import org.xml.sax.XMLReader;
import java.util.*;
import java.util.regex.*;
import org.xml.sax.*;
import org.xml.sax.*;

public class Wget {
	public static final String HTML_TYPE = "text/html";

	Set alreadyProcessedURLs = new HashSet();

	WgetProcessUrl processUrl;

	WgetURLValidator urlValidator;

	List errorListeners = new ArrayList();

	boolean spiderURLs;

	public Map followTags = new HashMap();

	static protected Pattern metaRefresh = Pattern.compile("([0-9]*\\s*;\\s*url=\\s*)(.*)");

	public static final El A_HREF = new El("a", "href");

	public static final El APPLET_CODE = new El("applet", "code");

	public static final El AREA_HREF = new El("area", "href");

	public static final El BGSOUND_SRC = new El("bgsound", "src");

	public static final El BODY_BACKGROUND = new El("body", "background");

	public static final El EMBED_HREF = new El("embed", "href");

	public static final El EMBED_SRC = new El("embed", "src");

	public static final El FIG_SRC = new El("fig", "src");

	public static final El FRAME_SRC = new El("frame", "src");

	public static final El IFRAME_SRC = new El("iframe", "src");

	public static final El IMG_HREF = new El("img", "href");

	public static final El IMG_LOWSRC = new El("img", "lowsrc");

	public static final El IMG_SRC = new El("img", "src");

	public static final El INPUT_SRC = new El("input", "src");

	public static final El LAYER_SRC = new El("layer", "src");

	public static final El OVERLAY_SRC = new El("overlay", "src");

	public static final El SCRIPT_SRC = new El("script", "src");

	public static final El TABLE_BACKGROUND = new El("table", "background");

	public static final El TD_BACKGROUND = new El("td", "background");

	public static final El TH_BACKGROUND = new El("th", "background");

	public void addAllTags() {
		addAllTags(followTags);
	}

	public static void addAllTags(Map followTags) {
		addFollowTag(A_HREF, followTags);
		addFollowTag(APPLET_CODE, followTags);
		addFollowTag(AREA_HREF, followTags);
		addFollowTag(BGSOUND_SRC, followTags);
		addFollowTag(BODY_BACKGROUND, followTags);
		addFollowTag(EMBED_HREF, followTags);
		addFollowTag(EMBED_SRC, followTags);
		addFollowTag(FIG_SRC, followTags);
		addFollowTag(FRAME_SRC, followTags);
		addFollowTag(IFRAME_SRC, followTags);
		addFollowTag(IMG_HREF, followTags);
		addFollowTag(IMG_LOWSRC, followTags);
		addFollowTag(IMG_SRC, followTags);
		addFollowTag(INPUT_SRC, followTags);
		addFollowTag(LAYER_SRC, followTags);
		addFollowTag(OVERLAY_SRC, followTags);
		addFollowTag(SCRIPT_SRC, followTags);
		addFollowTag(TABLE_BACKGROUND, followTags);
		addFollowTag(TD_BACKGROUND, followTags);
		addFollowTag(TH_BACKGROUND, followTags);
	}

	public void removeAllTags() {
		followTags.clear();
	}

	public void removeFollowTag(El el) {
		Set set = (Set)followTags.get(el.name);
		if (set != null) {
			set.remove(el.attr);
			if (set.isEmpty()) {
				followTags.remove(set);
			}
		}
	}

	public void addFollowTag(El el) {
		addFollowTag(el, followTags);
	}

	static public void addFollowTag(El el, Map followTags) {
		Set set = (Set)followTags.get(el.name);
		if (set == null) {
			set = new HashSet();
			followTags.put(el.name, set);
		}
		set.add(el.attr);
	}

	public Wget(WgetProcessUrl processUrl) {
		this.processUrl = processUrl;
		HttpURLConnection.setFollowRedirects(false);
		addAllTags();
	}

	public void addErrorListener(WgetErrorListener listener) {
		errorListeners.add(listener);
	}

	public static void main(String[] args) throws WgetException {
		File dir = new File("c:/tmp/wget");
		WgetSaveToFile wgetSave = new WgetSaveToFile(dir);
		Wget wget = new Wget(wgetSave);
//		wget.setFollowLinks(followLinks);
		URL url = null;
		try {
//			url = new URL("http://www.google.com/");
//			url = new URL("http://www.techphoto.org/");
			url = new URL("http://java.sun.com/");
			wget.recursiveGet(url);
		} catch (MalformedURLException e) {
			wget.error(url, url.toExternalForm(), e);
		} catch (IOException e) {
			wget.error(url, url.toExternalForm(), e);
		} catch (WgetException e) {
			wget.error(url, url.toExternalForm(), e);
		}
	}

	/**
	 * given a tree of xhtml, return a list of URLs as Strings contained in
	 * the entire XML tree.
	 * @param element
	 * @return List of urls as Strings
	 */
	List getListOfLinks(Element element) {
		List rtn = new ArrayList();
		getListOfLinksHelper(element, rtn);
		return rtn;
	}

	public static boolean isMetaRefresh(Element element) {
		// All this fuss for Meta Refresh.
		if (element.getName().equals("meta")) {
			String value = element.getAttributeValue("http-equiv");
			if (value != null && value.toLowerCase().equals("refresh")) {
				String content = element.getAttributeValue("content");
				if (content != null) {
					Matcher m = metaRefresh.matcher(content);
					if (m.matches()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static String getMetaRefreshHeader(Element element) {
		// All this fuss for Meta Refresh.
		String content = element.getAttributeValue("content");
		if (content != null) {
			Matcher m = metaRefresh.matcher(content);
			if (m.matches()) {
				return m.group(1);
			}
		}
		return null;
	}

	public static String getMetaRefreshUrl(Element element) {
		// All this fuss for Meta Refresh.
		String content = element.getAttributeValue("content");
		if (content != null) {
			Matcher m = metaRefresh.matcher(content);
			if (m.matches()) {
				return m.group(2);
			}
		}
		return null;
	}

	/**
	 *
	 * @param element
	 * @return URL to follow
	 */
	List followURL(Element element) {
		List rtn = new ArrayList();
		Set set = (Set)followTags.get(element.getName());
		if (set != null) {
			Iterator it = element.getAttributes().iterator();
			while (it.hasNext()) {
				Attribute attr = (Attribute)it.next();
				if (set.contains(attr.getName())) {
//					System.out.println("attr: " + attr);
					rtn.add(attr.getValue());
				}
			}
		}
		if (isMetaRefresh(element)) {
			rtn.add(getMetaRefreshUrl(element));
		}
		return rtn;
	}

	/**
	 * Recursive Helper function for getListOfLinks
	 * @param element
	 * @param rtn
	 * @see getListOfLinks
	 */
	void getListOfLinksHelper(Element element, List rtn) {
		rtn.addAll(followURL(element));
		Iterator it = element.getChildren().iterator();
		while (it.hasNext()) {
			Element subElement = (Element)it.next();
			getListOfLinksHelper(subElement, rtn); ;
		}
	}

	/*	URL stringToURL(URL parent, String urlString) throws MalformedURLException {
	  URL rtn = new URL(parent, urlString);
	  System.out.println("X: " + rtn);
	  return rtn;
	  URL rtn = null;
	  try {
	   rtn = new URL(urlString);
	  } catch (MalformedURLException ex) {
	   if (!urlString.startsWith("/")) {
//										urlString = "/" + urlString;
	 String olddir = "";
	 if (urlString.startsWith("#")) {
	  olddir = parent.getPath();
	 } else {
	  int ind2 = parent.getPath().lastIndexOf('/');
	  if (0 <= ind2) {
	   olddir = parent.getPath().substring(0, ind2) + "/";
	  }
	 }
	 urlString = olddir + urlString;
	   }
	   rtn = new URL(parent.getProtocol(), parent.getHost(),
	  parent.getPort(), urlString);
	  }
	  return rtn;
	 } */

	String connectionToMimeType(HttpURLConnection hconnect) {
		String contentType = hconnect.getContentType();
		if (contentType != null) {
			int ind = contentType.indexOf(';');
			if (0 <= ind) {
				contentType = contentType.substring(0, ind);
			}
		}
		return contentType;
	}

	public URLConnection openConnection(URL url) throws IOException {
		return url.openConnection();
	}

	boolean validProtocol(URL url) {
		if (url.getProtocol().equals("mailto")) {
			return false;
		}
		return true;
	}

	public void recursiveGet(URL url) throws WgetException, IOException {
//		try {
		String mime;
		if (validProtocol(url) && (urlValidator == null || urlValidator.isURLValid(url)) && !isAlreadyProcessed(url)) {
			URLConnection connection = openConnection(url);
			if (connection instanceof HttpURLConnection &&
				(mime = connectionToMimeType((HttpURLConnection)connection)) != null &&
				mime.equals(HTML_TYPE)) {
				int code = ((HttpURLConnection)connection).getResponseCode();
				// In the case of HTML documents, we slurp them into memory
				// for further processing
				ByteArrayOutputStream baos;
				int len = connection.getContentLength();
				if (0 < len) {
					baos = new ByteArrayOutputStream(len);
				} else {
					baos = new ByteArrayOutputStream();
				}
				byte[] buf = new byte[4096];
				InputStream is;
				try {
					is = connection.getInputStream();
				} catch (FileNotFoundException e) {
					is = ((HttpURLConnection)connection).getErrorStream();
				} while (0 <= (len = is.read(buf))) {
					baos.write(buf, 0, len);
				}
				HttpURLConnectionProxy newconn = new HttpURLConnectionProxy((HttpURLConnection)connection);
				newconn.setData(baos.toByteArray());
				// Deal with the document
				doProcessUrl(newconn);
				if (spiderURLs) {
					newconn = new HttpURLConnectionProxy((HttpURLConnection)connection);
					newconn.setData(baos.toByteArray());
					doSpiderURLs(newconn);
				}
			} else {
				// For non-HTML documents, we send the stream directly to
				// the processor to be dealt with.
//					BufferedInputStream is = new BufferedInputStream(connection.getInputStream());
				doProcessUrl(connection);
			}
		}
//		} catch (UnknownServiceException e) {
//			error(url.toExternalForm()
//		}

//		catch (FileNotFoundException e) {
//			System.out.println("URL NOT FOUND: " + url);
		// Nothing. HttpURLConnection throws this when the web resource is missing.
//		}
	}

	public void doSpiderURLs(HttpURLConnection connection) throws IOException, WgetException {
		try {
			// Suck out the links for recursive processing.
			Element element = streamToElement(connection.getInputStream());
			List subItems = getListOfLinks(element);
			Iterator it = subItems.iterator();
			while (it.hasNext()) {
				String urlString = null;
				URL subUrl = null;
				try {
					urlString = (String)it.next();
//								URL subUrl = stringToURL(connection.getURL(), urlString);
					subUrl = new URL(connection.getURL(), urlString);
					recursiveGet(subUrl);
				} catch (MalformedURLException e) {
					error(subUrl, urlString, e);
				} catch (WgetException e) {
					error(subUrl, urlString, e);
				} catch (IOException e) {
					error(subUrl, urlString, e);
				}
			}
		} catch (JDOMException ex) {
			throw new WgetException(ex);
		}
	}

	public void doProcessUrl(URLConnection connection) throws IOException, WgetException {
//		System.out.println("processing: " + connection.getURL());
		URL url = connection.getURL();
		processUrl.process(connection);
		addAlreadyProcessed(url);

		if (isRedirected(connection)) {
			url = new URL(url, ((HttpURLConnection)connection).getHeaderField("Location"));
			recursiveGet(url);
		}
	}

	void error(URL url, String urlString, Exception e) throws WgetException {
		Iterator it = errorListeners.iterator();
		while (it.hasNext()) {
			WgetErrorListener l = (WgetErrorListener)it.next();
			l.errorEvent(url, urlString, e);
		}
	}

	/**
	 * Convert an InputStream into a jdom tree of xhtml.
	 * @param is An InputStream
	 * @return a jdom tree.
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws JDOMException
	 */
	Element streamToElement(InputStream is) throws IOException, FileNotFoundException, JDOMException, WgetException {
		/*		Tidy tidy = getTidy();
		  ByteArrayOutputStream err = new ByteArrayOutputStream();
		  tidy.setErrout(new PrintWriter(err, true));
		  ByteArrayOutputStream out = new ByteArrayOutputStream();
		  tidy.parse(new BufferedInputStream(is), out);
		  if (0 < tidy.getParseErrors()) {
		   throw new WgetException(new String(err.toByteArray()));
		  } */
		SAXBuilder sb = new SAXBuilder() {
			protected XMLReader createParser() throws JDOMException {
				XMLReader r = new org.ccil.cowan.tagsoup.Parser();
				String ignoreBogonsFeature = "http://www.ccil.org/~cowan/tagsoup/features/ignore-bogons";
				try {
					r.setFeature(ignoreBogonsFeature, true);
				} catch (SAXException x) {
					throw new JDOMException("TagSoup Error", x);
				}
				return r;
			}
		};
//		return new SAXBuilder().build(new ByteArrayInputStream(out.toByteArray())).detachRootElement();
		return sb.build(is).detachRootElement();
	}

	/*	public static Tidy getTidy() {
	  Tidy tidy = new Tidy();
	  tidy.setXmlSpace(true);
	  tidy.setTidyMark(false);
	  tidy.setQuiet(false);
	  tidy.setWraplen(0);
	  tidy.setDropEmptyParas(false);
//		tidy.setXmlOut(true);
	  tidy.setXHTML(true);
	  tidy.setShowWarnings(false);
	  return tidy;
	 } */

	static public boolean isRedirected(URLConnection connection) throws IOException {
		return connection instanceof HttpURLConnection &&
			(((HttpURLConnection)connection).getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM ||
			 ((HttpURLConnection)connection).getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP);

	}

	boolean isAlreadyProcessed(URL url) {
		// Recreating the URL removes the "ref" or anchor, which we don't want.
		try {
			URL newURL = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile());
			return alreadyProcessedURLs.contains(newURL);
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
		}
		return false;
	}

	void addAlreadyProcessed(URL url) {
		// Recreating the URL removes the "ref" or anchor, which we don't want.
		try {
			URL newURL = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile());
			alreadyProcessedURLs.add(newURL);
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
		}
	}

	public WgetURLValidator getUrlValidator() {
		return urlValidator;
	}

	public void setUrlValidator(WgetURLValidator urlValidator) {
		this.urlValidator = urlValidator;
	}

	public boolean isSpiderURLs() {
		return spiderURLs;
	}

	public void setSpiderURLs(boolean v) {
		this.spiderURLs = v;
	}

	public static class El {
		public El(String name, String attr) {
			this.name = name;
			this.attr = attr;
		}

		String name;

		String attr;
	}

}
