package au.gov.naa.digipres.xena.plugin.html;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.BindException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLEditorKit;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import au.gov.naa.digipres.xena.gui.MainFrame;
import au.gov.naa.digipres.xena.helper.JdomUtil;
import au.gov.naa.digipres.xena.helper.JdomXenaView;
import au.gov.naa.digipres.xena.helper.UrlEncoder;
import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.PrintXml;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.filenamer.FileNamer;
import au.gov.naa.digipres.xena.kernel.filenamer.FileNamerManager;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.MetaDataWrapperManager;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserManager;
import au.gov.naa.digipres.xena.kernel.type.FileType;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;
import au.gov.naa.digipres.xena.plugin.html.javatools.http.AbstractHttpResponseHandler;
import au.gov.naa.digipres.xena.plugin.html.javatools.http.HttpRequestHandler;
import au.gov.naa.digipres.xena.plugin.html.javatools.http.HttpResponseHandler;
import au.gov.naa.digipres.xena.plugin.html.javatools.thread.NetworkRequestHandler;
import au.gov.naa.digipres.xena.plugin.html.javatools.thread.NetworkServer;
import au.gov.naa.digipres.xena.plugin.html.javatools.wget.Wget;

/**
 * Display an entire web site from Xena files. It does this by setting up a
 * fully fledged web server that maps the URLs from the links to
 *
 * @author Chris Bitmead
 */
public class HttpView extends JdomXenaView {
	Namespace ns = Namespace.getNamespace(HttpToXenaHttpNormaliser.PREFIX, HttpToXenaHttpNormaliser.URI);

	NetworkServer nserver = new HttpServer();

	File root;

	MapAndTopList mapAndTopList;

	static final String INDEX_NAME = "website.xena";

	GridLayout gridLayout = new GridLayout();

	JScrollPane scrollPane = new JScrollPane();

	HTMLEditorKit htmlKit = new HTMLEditorKit();

	JEditorPane editorPane = new JEditorPane();

	JPanel buttonPanel = new JPanel();

	JToolBar toolBar = new JToolBar();

	DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();

	JComboBox comboBox = new JComboBox();

	JButton back = new JButton();

	JButton forward = new JButton();

	int historyptr = -1;

	ActionListener comboBoxActionListener;

	JMenu bookmarks = new JMenu("Bookmarks");

	JLabel statusBar = new JLabel(" ");

	void addHistory(URL url) {
		comboBox.removeActionListener(comboBoxActionListener);
		for (int i = comboBoxModel.getSize() - 1; historyptr < i; i--) {
			comboBoxModel.removeElementAt(i);
		}
		if (comboBoxModel.getSize() == 0 || !comboBoxModel.getElementAt(comboBoxModel.getSize() - 1).equals(url)) {
			comboBoxModel.addElement(url);
		}
		comboBox.setSelectedItem(url);
		comboBox.addActionListener(comboBoxActionListener);
		historyptr = comboBoxModel.getSize() - 1;
		back.setEnabled(historyptr != 0);
		forward.setEnabled(false);
	}

	URL backHistory() {
		assert 0 <= historyptr;
		historyptr--;
		forward.setEnabled(true);
		back.setEnabled(0 < historyptr);
		URL rtn = (URL)comboBoxModel.getElementAt(historyptr);
		comboBox.removeActionListener(comboBoxActionListener);
		comboBox.setSelectedItem(rtn);
		comboBox.addActionListener(comboBoxActionListener);
		back.setEnabled(historyptr != 0);
		return rtn;
	}

	URL forwardHistory() {
		assert historyptr < comboBoxModel.getSize() - 1;
		historyptr++;
		forward.setEnabled(historyptr < comboBoxModel.getSize() - 1); // && history.get(historyptr + 1) != null);
		back.setEnabled(true);
		URL rtn = (URL)comboBoxModel.getElementAt(historyptr);
		comboBox.removeActionListener(comboBoxActionListener);
		comboBox.setSelectedItem(rtn);
		comboBox.addActionListener(comboBoxActionListener);
		return rtn;
	}

	public HttpView() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void urlNotFound(URL url) {
		MainFrame.singleton().showError("URL not found: " + url.toExternalForm());
	}

	public URL display(URL real, Element element) {
		try {
			// Don't search for a file if we have a
			// ref, because it must be HTML in that case.
//			System.out.println("display");
			if (element == null) {
				URL myfake = urlToFakeUrl(real);
				URL noref = new URL(myfake.getProtocol(), myfake.getHost(), myfake.getPort(), myfake.getFile());
//				System.out.println("noref: " + noref + " myfake: " + myfake.getFile());
				try {
					File file = findFile(noref);
					URL url = file.toURI().toURL();
					element = JdomUtil.loadUnwrapXml(url);
				} catch (FileNotFoundException x) {
					// External web site, perhaps.
					editorPane.setPage(real);
					return real;
				}
			}
			Element uri = element.getChild("uri", ns);
			if (real == null && uri != null) {
				// Don't overwrite the real, or we lose the ref
				real = new URL(uri.getText());
			}
			if (real == null) {
				throw new XenaException("Invalid");
			}
			Element doc = null;
			if (element.getName().equals("http")) {
				Element data = element.getChild("data", ns);
				doc = (Element)data.getChildren().get(0);
			} else {
				doc = element;
			}
			URL fake = urlToFakeUrl(real);
			if (doc.getName().equals("html")) {
				editorPane.setPage(fake);
			} else if (doc.getName().equals("png") || doc.getName().equals("jpeg")) {
				ByteArrayInputStream bais = new ByteArrayInputStream(("<html><body><img src=\"" + fake.toExternalForm() +
																	  "\"></body></html>").getBytes());
				javax.swing.text.html.HTMLDocument newdoc = new javax.swing.text.html.HTMLDocument();
				editorPane.setDocument(newdoc);
				htmlKit.read(bais, newdoc, 0);
			}
		} catch (BadLocationException x) {
			urlNotFound(real);
		} catch (IOException x) {
			urlNotFound(real);
		} catch (JDOMException x) {
			MainFrame.singleton().showError(x);
		} catch (XenaException x) {
			MainFrame.singleton().showError(x);
		}
		return real;
	}

	public void updateViewFromElement() throws XenaException {
		try {
			root = HttpToXenaHttpNormaliser.findRoot();
			mapAndTopList = getURLMap(root);
			Iterator it = mapAndTopList.topList.iterator();
			while (it.hasNext()) {
				final URL real = (URL)it.next();
				JMenuItem mitem = new JMenuItem(real.toExternalForm());
				bookmarks.add(mitem);
				mitem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						display(real, null);
						addHistory(real);
					}
				});
			}
		} catch (IOException ex) {
			MainFrame.singleton().showError(ex);
		}
		nserver.start();
		comboBoxActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object o = comboBox.getSelectedItem();
				URL url = null;
				if (o instanceof String) {
					try {
						url = new URL((String)o);
					} catch (MalformedURLException x) {
						MainFrame.singleton().showError(x);
					}
				} else if (o instanceof URL) {
					url = (URL)comboBox.getSelectedItem();
				} else {
					assert false;
				}
				display(url, null);
				addHistory(url);
			}
		};
		URL real = null;
		if (getElement().getQualifiedName().equals(TypeManager.singleton().lookupXenaFileType(XenaHttpFileType.class).getTag())) {
			real = display(null, getElement());
		} else if (0 < mapAndTopList.topList.size() &&
				   getElement().getQualifiedName().equals(TypeManager.singleton().lookupXenaFileType(XenaWebSiteFileType.class).getTag())) {
			real = display((URL)mapAndTopList.topList.iterator().next(), null);
		} else {
			try {
				FileNamer namer = FileNamerManager.singleton().getFileNamerFromPrefs();
                
                //FIXME: debug trying to fix html bug.
                
                URL myu = getInternalFrame().getSavedFile().toURI().toURL();
                System.out.println(myu.toString());
                XenaInputSource myxis = new XenaInputSource(getInternalFrame().getSavedFile().toURI().toURL(), null);
                System.out.println(myxis.toString());

                
                String sysid = MetaDataWrapperManager.singleton().getSourceId(myxis);
                //System.out.println(namer.getSystemId(myxis));
				//URL url = new URL(namer.getSystemId(new XenaInputSource(getInternalFrame().getSavedFile().toURI().toURL(), null)));
				//TODO: This is wrong...
                //String foo = namer.getSystemId(myxis);
                //System.out.println("namer get ss id result:" + foo);
                
                
                
                URL url = new URL(sysid);
                
                real = display(url, getElement());
			} catch (MalformedURLException x) {
				throw new XenaException(x);
			}
		}
		// Sleeping  allows the html to load and the page to pack properly.
		try {
			Thread.currentThread().sleep(500);
		} catch (InterruptedException x) {
			x.printStackTrace();
		}
		addHistory(real);
	}

	public boolean canShowTag(String tag) throws XenaException {
		return tag.equals(TypeManager.singleton().lookupXenaFileType(XenaWebSiteFileType.class).getTag())
			|| tag.equals(TypeManager.singleton().lookupXenaFileType(XenaHttpFileType.class).getTag())
			|| tag.equals(TypeManager.singleton().lookupXenaFileType(XenaHtmlFileType.class).getTag());
	}

	public String getViewName() {
		return "HTTP";
	}

	private void jbInit() throws Exception {
		buttonPanel.setLayout(new BorderLayout());
		JMenuBar menuBar = new JMenuBar();
		buttonPanel.add(menuBar, BorderLayout.NORTH);
		menuBar.add(bookmarks);
		this.add(buttonPanel, BorderLayout.NORTH);
		buttonPanel.add(toolBar, BorderLayout.WEST);

        //TODO: icon factory is brokened. fix.
        ImageIcon bicon = IconFactory.getIconByName("Back.gif");
		back.setToolTipText("Go back one page");
		back.setEnabled(false);
		back.setIcon(bicon);
		toolBar.add(back);
		back.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				URL real = backHistory();
				assert real != null;
				display(real, null);
			}
		});
		ImageIcon ficon = IconFactory.getIconByName("Forward.gif");
		forward.setToolTipText("Go forward one page");
		forward.setEnabled(false);
		forward.setIcon(ficon);
		toolBar.add(forward);
		forward.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				URL real = forwardHistory();
				assert real != null;
				display(real, null);
			}
		});
		ImageIcon eicon = IconFactory.getIconByName("Export.gif");
		JButton export = new JButton();
		export.setToolTipText("Show page in external browser");
		export.setIcon(eicon);
		toolBar.add(export);
		export.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					URL fake = urlToFakeUrl((URL)comboBox.getSelectedItem());
					BrowserLauncher.openURL(fake.toExternalForm());
				} catch (IOException x) {
					MainFrame.singleton().showError(x);
				}
			}
		});
		comboBox.setModel(comboBoxModel);
		buttonPanel.add(comboBox, BorderLayout.CENTER);
		comboBox.setEditable(true);
		editorPane.setEditorKit(htmlKit);
		editorPane.setContentType("text/html; charset=" + PrintXml.singleton().ENCODING);
		editorPane.getDocument().putProperty("IgnoreCharsetDirective", new Boolean(true));
		scrollPane.getViewport().add(editorPane);
		editorPane.setEditable(false);
		this.add(scrollPane, BorderLayout.CENTER);
		this.add(statusBar, BorderLayout.SOUTH);
		editorPane.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent evt) {
				try {
					if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
						// Xena browser
						URL real = evt.getURL();
						// external browser
						if (real.getHost().equals("localhost")) {
							real = fakeUrlToUrl(evt.getURL());
						}
						if (supportedProtocol(real)) {
							display(real, null);
							addHistory(real);
						} else {
							statusBar.setText("Protocol: " + real.getProtocol() + " not supported.");
						}
					} else if (evt.getEventType() == HyperlinkEvent.EventType.ENTERED) {
//						System.out.println(evt.getURL());
						if (evt.getURL() != null) {
							if (evt.getURL().getHost().equals("localhost")) {
								// external browser
								URL real = fakeUrlToUrl(evt.getURL());
//								System.out.println("real: " + real.toExternalForm() + " evt: " + evt.getURL());
								statusBar.setText(real.toExternalForm());
							} else {
								// Xena browser
								statusBar.setText("EXTERNAL LINK: " + evt.getURL().toExternalForm());
							}
						}
					} else if (evt.getEventType() == HyperlinkEvent.EventType.EXITED) {
						statusBar.setText("Done");
					}
				} catch (MalformedURLException x) {
					MainFrame.singleton().showError(x);
				}
			}
		});
	}

	boolean supportedProtocol(URL url) {
		String protocol = url.getProtocol();
		return protocol.equals("http") || protocol.equals("file");
	}

	public void close() {
		nserver.shutdown();
	}

	static public Element saveURLMap(File root, MapAndTopList urlMap) throws IOException {
		Namespace ns = Namespace.getNamespace("website", "http://preservation.naa.gov.au/website/1.0");
		Element websiteElement = new Element("website", ns);
		Element urlsElement = new Element("uris", ns);
		websiteElement.addContent(urlsElement);

		Iterator it = urlMap.map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			Element urlElement = new Element("uri", ns);
			URL url = (URL)entry.getKey();
			urlElement.setText(url.toExternalForm());
			urlElement.setAttribute("index", entry.getValue().toString(), ns);
			if (urlMap.topList.contains(url)) {
				urlElement.setAttribute("top", "true", ns);
			}
			urlsElement.addContent(urlElement);
		}

		File file = new File(root, INDEX_NAME);
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
		Document doc = new Document(websiteElement);
		new HtmlView.HackPrintXml().printXml(doc, bos);
		bos.close();
		return doc.detachRootElement();
	}

	public static MapAndTopList loadURLMap(File root) throws JDOMException, IOException {
		Namespace ns = Namespace.getNamespace("website", "http://preservation.naa.gov.au/website/1.0");
		Map map = new HashMap();
		Set topList = new HashSet();
		SAXBuilder sax = new SAXBuilder();
		Document doc = sax.build(new File(root, INDEX_NAME));
		Element websiteElement = doc.getRootElement();
		Element urlsElement = websiteElement.getChild("uris", ns);
		Iterator it = urlsElement.getChildren("uri", ns).iterator();
		while (it.hasNext()) {
			Element urlElement = (Element)it.next();
			URL url = new URL(urlElement.getText());
			map.put(url, urlElement.getAttribute("index", ns).getValue());
			String v = urlElement.getAttributeValue("top", ns);
			if (v != null) {
				topList.add(url);
			}
		}
		return new MapAndTopList(map, topList);
	}

	static MapAndTopList getURLMap(File root) throws IOException {
		MapAndTopList rtn = null;
		if (anyLaterThanRoot(root)) {
			rtn = createURLMap(root);
			saveURLMap(root, rtn);
		} else {
			try {
				rtn = loadURLMap(root);
			} catch (JDOMException e) {
				rtn = createURLMap(root);
				saveURLMap(root, rtn);
			}
		}
		return rtn;
	}

	static boolean anyLaterThanRoot(File root) {
		File index = new File(root, INDEX_NAME);
		return anyLaterThanRootHelper(index.lastModified(), root);
	}

	static boolean anyLaterThanRootHelper(long indexTime, File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (anyLaterThanRootHelper(indexTime, files[i])) {
					return true;
				}
			}
		} else {
			if (indexTime < file.lastModified()) {
				return true;
			}
		}
		return false;
	}

	static MapAndTopList createURLMap(File root) throws IOException {
		Map map = new HashMap();
		Set topList = new HashSet();
		createURLMapHelper(root, root, map, topList);
		return new MapAndTopList(map, topList);
	}

	static public class MapAndTopList {
		MapAndTopList(Map map, Set topList) {
			this.map = map;
			this.topList = topList;
		}

		Map map;

		Set topList;
	}

	static void createURLMapHelper(File root, File file, Map map, Set topList) throws IOException {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				createURLMapHelper(root, files[i], map, topList);
			}
		} else {
			UrlAndTop uam = getURLFromFile(file);
			// Non-http files will return null;
			if (uam != null) {
//				System.out.println("M: " + uam.url + " : " + javatools.util.FileName.relativeTo(root, file));
				map.put(uam.url, FileName.relativeTo(root, file));
				if (uam.top) {
					topList.add(uam.url);
				}
			} else {
				try {
					FileNamer namer = FileNamerManager.singleton().getFileNamerFromPrefs();
					
                    //String systemId = namer.getSystemId(new XenaInputSource(file, null));
					String systemId = MetaDataWrapperManager.singleton().getSourceId(new XenaInputSource(file));
                    
                    
                    // Unwrapped files can't have a name extracted. Must ignore them.
					if (systemId != null) {
						URL url = new URL(systemId);
//						System.out.println("P: " + url + " : " + javatools.util.FileName.relativeTo(root, file));
						map.put(url, FileName.relativeTo(root, file));
					}
				} catch (XenaException x) {
					// Nothing. We can't figure out the name of this file.
				}
			}
		}
	}

	static public class UrlAndTop {
		public UrlAndTop(URL url, boolean top) {
			this.url = url;
			this.top = top;
		}

		URL url;

		boolean top;
	}

	static UrlAndTop getURLFromFile(File file) {
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(file, new DefaultHandler() {
				boolean found = false;

				String url = "";

				boolean top;

				boolean insideHttp = false;

				public void startElement(String uri,
										 String localName,
										 String qName,
										 Attributes attributes) throws SAXException {
					if (found) {
						// Bail out early as soon as we've found what we want
						// for super efficiency.
						try {
							throw new FoundException(new UrlAndTop(new URL(url), top));
						} catch (MalformedURLException e) {
							throw new FoundException(null);
						}
					}
					if (qName.endsWith(":http")) {
						insideHttp = true;
						String v = attributes.getValue(HttpToXenaHttpNormaliser.PREFIX + ":top");
						if (v == null) {
							top = false;
						} else {
							top = Boolean.valueOf(v).booleanValue();
						}
					}
					if (insideHttp && qName.endsWith(":uri")) {
						found = true;
					}
				}

				public void endElement(String uri,
									   String localName,
									   String qName) throws SAXException {
					if (qName.endsWith(":http")) {
						insideHttp = false;
					}
				}

				public void characters(char[] ch, int start, int length) {
					if (found) {
						url = url + new String(ch, start, length);
					}
				}
			});
		} catch (FoundException e) {
			return e.url;
		} catch (SAXException e) {
			// Nothing
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	static boolean pathUrl(URL url) {
		return url.getPath().length() != 0 && url.getPath().charAt(0) == '/';
	}

	URL urlToFakeUrl(URL url) {
		try {
			// Host is "" for mailto: URLs
			if (pathUrl(url)) {
				String newPath = "/";
				if (url.getHost().equals("")) {
					newPath += "localhost";
				} else {
					newPath += url.getHost();
				}
				if (0 <= url.getPort()) {
					newPath += ":" + url.getPort();
				}
				newPath += url.getFile();
				if (url.getRef() != null) {
					newPath += "#" + url.getRef();
				}
				if (newPath.lastIndexOf('%') < 0) {
					newPath = UrlEncoder.encode(newPath);
				}
				URL rtn = new URL("http", "localhost", nserver.getPort(), newPath);
				return rtn;
			}
		} catch (MalformedURLException x) {
			x.printStackTrace();
		} catch (UnsupportedEncodingException x) {
			x.printStackTrace();
		}
		return url;
	}

	static URL fakeUrlToUrl(URL url) throws MalformedURLException {
//		try {
		if (pathUrl(url)) {
			int ind = url.getPath().indexOf('/', 1);
			if (ind < 0) {
				ind = url.getPath().length();
			}
			String firstComponent = url.getPath().substring(1, ind);
			String restPath = url.getFile().substring(ind);
			ind = firstComponent.indexOf(':');
			int port = -1;
			if (0 < ind) {
				String portS = firstComponent.substring(ind + 1);
				firstComponent = firstComponent.substring(0, ind);
				port = Integer.parseInt(portS);
			}
			if (url.getRef() != null) {
				restPath += "#" + url.getRef();
			}
			String protocol = url.getProtocol();
			if (firstComponent.equals("localhost")) {
				firstComponent = "";
				protocol = "file";
			}
			URL rtn = new URL(protocol, firstComponent, port, restPath);
			return rtn;
		} else {
			return url;
		}
	}

	static public class FoundException extends SAXException {
		UrlAndTop url;

		public FoundException(UrlAndTop url) {
			super("Found");
			this.url = url;
		}
	}

	public Element findTag(Element e, String tag) {
		if (e.getQualifiedName().equals(tag)) {
			return e;
		} else {
			Iterator it = e.getChildren().iterator();
			while (it.hasNext()) {
				Element el = (Element)it.next();
				Element check = findTag(el, tag);
				if (check != null) {
					return check;
				}
			}
		}
		return null;
	}

	URL getUrlFromJdom(Element httpE) throws MalformedURLException {
		Element urlE = httpE.getChild("uri", ns);
		URL origURL = new URL(urlE.getText());
		return origURL;
	}

	File findFile(URL fake) throws IOException {
		URL realURL = fakeUrlToUrl(fake);
//		System.out.println(fake + " : " + realURL.toExternalForm());
		Iterator it = mapAndTopList.map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			URL u = (URL)entry.getKey();
			if (u.equals(realURL)) {
				return new File(root, (String)entry.getValue());
			}
		}
		throw new FileNotFoundException(fake + " not Found");
	}

	public class XenaResponseHandler extends AbstractHttpResponseHandler {
		Element data;

		Element headers;

		File file;

		boolean exists;

		TransformerHandler deNormaliser;

		URL origURL;

		Set nonDisplayedHeaders = new HashSet();

		XenaResponseHandler(URL url, Socket socket) {
			super(url, socket);
			nonDisplayedHeaders.add("content-type");
			nonDisplayedHeaders.add("content-length");
			nonDisplayedHeaders.add("connection");
			nonDisplayedHeaders.add("etag");
			nonDisplayedHeaders.add("accept-ranges");
			nonDisplayedHeaders.add("transfer-encoding");

		}

		public boolean exists() {
			return exists;
		}

		public long lastModified() {
			return 0L;
		}

		public long getLength() {
			return -1;
		}

		public String getMimeType() {
			return "text/html";
		}

		public void processURL(PrintStream ps, boolean sendFile) throws IOException {
			try {
				try {
//					System.out.println("processURL");
					file = findFile(getUrl());
					exists = true;
					Element httpE = JdomUtil.loadUnwrapXml(file.toURL());
					if (httpE.getName().equals("http")) {
						origURL = getUrlFromJdom(httpE);
						Element dataE = httpE.getChild("data", ns);
						data = (Element)dataE.getChildren().get(0);
						headers = httpE.getChild("headers", ns);
					} else {
						data = httpE;
					}

					deNormaliser = NormaliserManager.singleton().lookupDeNormaliser(data.getQualifiedName());

				} catch (IOException e) {
					exists = false;
				}
			} catch (XenaException e) {
				e.printStackTrace();
			} catch (JDOMException e) {
				e.printStackTrace();
			}
			super.processURL(ps, sendFile);
		}

		public void send404(PrintStream ps) throws IOException {
			super.send404(ps);
			print(ps, "Xena Web Simulator");
			print(ps, EOL);
			print(ps, EOL);
		}

		public void printHeaders(PrintStream ps, boolean exists) throws IOException {
			if (!exists) {
				print(ps, "HTTP/1.0 " + HttpURLConnection.HTTP_NOT_FOUND + " not found");
				print(ps, EOL);
			} else {
				if (headers == null) {
					print(ps, "HTTP/1.0 200 OK");
					print(ps, EOL);
				} else {

					//try {
					Iterator it = headers.getChildren("header", ns).iterator();
					String contentType = null;
					while (it.hasNext()) {
						Element header = (Element)it.next();
						String name = header.getAttributeValue("name", ns);
						String value = header.getText();
						String nameLower = name == null ? null : name.toLowerCase();
						boolean doPrint = name == null || !nonDisplayedHeaders.contains(nameLower);
						if (name != null && doPrint) {
							print(ps, name);
							print(ps, ": ");
						} else {
//							value = value.replaceAll("HTTP/1.1", "HTTP/1.0");
						}
						if (doPrint) {
							try {
								// Mainly to deal with the Location: header.
								URL url = new URL(value);
								value = urlToFakeUrl(url).toExternalForm();
							} catch (MalformedURLException ex) {
								// Nothing
							}
							print(ps, value);
							print(ps, EOL);
						}
						if (nameLower != null && nameLower.equals("content-type")) {
							contentType = value;
						}
					}
					print(ps, "Content-Type: ");
					if (deNormaliser == null) {
						print(ps, contentType);
						throw new IOException("No DeNormaliser found for element: " + data.getQualifiedName());
					} else {
						FileType type = NormaliserManager.singleton().getOutputType(deNormaliser.getClass());
						// If the type is binary, use the original mime type
						if (type.getMimeType().equals("application/octet-stream")) {
							print(ps, contentType);
						} else {
							print(ps, type.getMimeType());
							// Is this a good solution or a hack? I can't imagine us
							// outputting text other than UTF.
							if (type.getMimeType().startsWith("text/")) {
								print(ps, "; charset=UTF-8");
							}
						}
					}
					print(ps, EOL);
				}
			}
			print(ps, EOL);
		}

		void transformHtml(Element data) {
/*			ByteArrayInputStream in = new ByteArrayInputStream(data.getText().getBytes());
			int c;
			ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
//			int delay = -1;
			// Hacks. HTML viewer doesn't like <p/> or characters >= 128.
			while (0 <= (c = in.read())) {
/*				if (delay != -1) {
					if (c != '>') {
						baos2.write(delay);
					}
					delay = -1;
				}
				if (128 <= c) {
					// Nothing
	//			} else if (c == '/') {
//					delay = c;
				} else {
					baos2.write(c);
				}
			}
			data.setText(baos2.toString());



	*/
			Map followTags = new HashMap();
			Wget.addAllTags(followTags);
			Set set = (Set)followTags.get(data.getName());
			if (set != null) {
				Iterator it = data.getAttributes().iterator();
				while (it.hasNext()) {
					Attribute at = (Attribute)it.next();
					if (set.contains(at.getName())) {
//						System.out.println("NM: " + at.getName());
						try {
							if (origURL == null) {
								String nw = null;
								try {
									nw = UrlEncoder.encode(data.getAttributeValue(at.getName()));
								} catch (UnsupportedEncodingException x) {
									MainFrame.singleton().showError(x);
								}
								data.setAttribute(at.getName(), nw);
							} else {
								URL url = new URL(origURL, at.getValue());
								data.setAttribute(at.getName(), urlToFakeUrl(url).toExternalForm());
								String fake = urlToFakeUrl(url).toExternalForm();
//								System.out.println("VAL: " + data.getAttribute(at.getName()) + " FAKE: " + fake);
							}
						} catch (MalformedURLException ex) {
//							System.out.println("ex:");
							// things like news: URLs should just be ignored.
						}
					}
				}
			}
			// All this fuss over meta-refresh
			if (Wget.isMetaRefresh(data)) {
				try {
					URL url = new URL(origURL, Wget.getMetaRefreshUrl(data));
					data.setAttribute("content", Wget.getMetaRefreshHeader(data) + urlToFakeUrl(url).toExternalForm());
				} catch (MalformedURLException ex) {
					// things like news: URLs should just be ignored.
				}
			}
			Iterator it = data.getChildren().iterator();
			while (it.hasNext()) {
				Element child = (Element)it.next();
				transformHtml(child);
			}
		}

		public void sendFile(java.io.PrintStream printStream) throws IOException {
			try {
				if (deNormaliser == null) {
//					throw new IOException("No denormaliser for type: " + data.getName() + " from file: " + file);
					printStream.print("<html><body>" + "No denormaliser for type: " + data.getName() + " from file: " + file + "</body></html>");
				}
				if (data.getName().equals("html")) {
					transformHtml(data);
				}
				StreamResult sr = new StreamResult(printStream);
				deNormaliser.setResult(sr);
				deNormaliser.startDocument();
				JdomUtil.writeElement(deNormaliser, data);
				deNormaliser.endDocument();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (JDOMException e) {
				e.printStackTrace();
			}
		}
	}

	public class XenaRequestHandler extends HttpRequestHandler {
		public XenaRequestHandler() {
		}

		public HttpResponseHandler getResponseHandler(URL url, Socket socket) {
			return new XenaResponseHandler(url, socket);
		}
	}

	public class HttpServer extends NetworkServer {
		public NetworkRequestHandler getRequestHandler(Socket socket) {
			return new XenaRequestHandler();
		}

		public void run() {
			boolean keepGoing = true;
			while (keepGoing) {
				try {
					go();
					keepGoing = false;
				} catch (BindException e) {
					port++;
					// Keep going
				} catch (IOException e) {
					keepGoing = false;
					e.printStackTrace();
				}
			}
		}
	}
}
