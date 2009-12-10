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

package au.gov.naa.digipres.xena.plugin.website;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.view.XenaView;
import au.gov.naa.digipres.xena.plugin.website.webserver.WebServer;
import au.gov.naa.digipres.xena.util.UrlEncoder;
import edu.stanford.ejalbert.BrowserLauncher;

/**
 * Viewer for a complete web site normalised by Xena.
 * 
 * The primary normalised file of a web site is an index of original web site file paths to the corresponding
 * normalised file. This index is parsed by the viewer, and then passed to a custom web server created especially
 * to view this web site. The web server will receive requests for pages from the original web site, and will use
 * the index to find the normalised file, export it, and return it to the requesting client.
 * 
 * After the web server has been started, this view will open the system internet browser, and point it to the 
 * root page of the web site.
 * 
 * @author Justin Waddell
 *
 */
public class WebsiteView extends XenaView {

	private static final long serialVersionUID = 1L;

	// We always want to try firefox first - if it is not installed, will drop back to whatever it can find
	private static final String DEFAULT_BROWSER_NAME = "firefox";

	private Logger logger = Logger.getLogger(this.getClass().getName());

	private JButton launchBrowserButton = new JButton();

	private Map<String, String> linkIndex;
	private String rootOriginalPath;
	private String rootXenaFilename;

	private WebServer webServer;

	public WebsiteView() {
		super();
		linkIndex = new HashMap<String, String>();
		initGUI();
	}

	/**
	 * The main view for web sites consists of a button that launches the root page
	 * of the website in the system browser.
	 */
	private void initGUI() {
		launchBrowserButton.setText("View in External Window");
		launchBrowserButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				launchBrowserButton_actionPerformed();
			}
		});
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(launchBrowserButton);
		this.add(buttonPanel, java.awt.BorderLayout.CENTER);

	}

	/**
	 * Use the BrowserLauncher library to launch the root page
	 * of the website in the system browser.
	 */
	public void launchBrowserButton_actionPerformed() {
		try {
			if (webServer == null) {
				// We cannot view the web site without a web server
				throw new XenaException("Web Server not started, cannot continue.");
			}

			// Open browser, pointing it to the index.html file (or whatever we can find as the root page!)
			BrowserLauncher launcher = new BrowserLauncher();
			URL urlToLoad = new URL("http", "localhost", webServer.getPort(), "/" + rootOriginalPath);
			launcher.openURLinBrowser(DEFAULT_BROWSER_NAME, UrlEncoder.encode(urlToLoad.toString()));
		} catch (Exception ex) {
			logger.log(Level.FINE, "Problem viewing website", ex);
			JOptionPane.showMessageDialog(this, ex);
		}
	}

	/**
	 * Initialise and start the web server that will convert the requested HTML link
	 * to the content in the appropriate Xena file.
	 */
	public void startWebServer() {
		try {
			if (linkIndex.isEmpty() || rootOriginalPath == null || rootXenaFilename == null) {
				// We have not successfully parsed the Xena file
				throw new XenaException("Could not load the website Xena file - no entries found.");
			}

			// Map an empty request to the root file in the index
			linkIndex.put("", rootXenaFilename);

			Xena xena = viewManager.getPluginManager().getXena();

			// Setup web server
			webServer = new WebServer(xena, linkIndex, getSourceDir());
			webServer.start();

			logger.fine("WebsiteView web server started.");
		} catch (Exception ex) {
			logger.log(Level.FINE, "Problem starting web server", ex);
			JOptionPane.showMessageDialog(this, ex);
		}
	}

	@Override
	public String getViewName() {
		return "Website View";
	}

	@Override
	public boolean canShowTag(String tag) {
		return tag.equals(WebsiteNormaliser.WEBSITE_PREFIX + ":" + WebsiteNormaliser.WEBSITE_TAG);
	}

	@Override
	public ContentHandler getContentHandler() {
		return new WebsiteViewHandler();
	}

	@Override
	protected void close() {
		// Shut down the web server
		if (webServer != null) {
			webServer.shutdownServer();
		}

		super.close();
	}

	/**
	 * Parses the primary Xena file for the website (the index of web pages to normalised files)
	 * to produce a Map. It also concurrently determines the root page of the website. When the
	 * parseing is complete, it starts the web server.
	 * @author Justin Waddell
	 *
	 */
	private class WebsiteViewHandler extends XMLFilterImpl {

		private String[] rootPageOptions =
		    {"index.html", "index.html", "index.jsp", "index.asp", "index.aspx", "index.shtml", "index.php", "default.asp"};
		private Set<String> rootPageOptionSet;

		public WebsiteViewHandler() {
			super();

			// Construct our set of options for the root page
			rootPageOptionSet = new HashSet<String>();
			for (String rootPageOption : rootPageOptions) {
				rootPageOptionSet.add(rootPageOption);
			}
		}

		/*
		 * (non-Javadoc)
		 * @see org.xml.sax.helpers.XMLFilterImpl#characters(char[], int, int)
		 */
		@Override
		public void characters(char[] ch, int start, int length) {
			// Do nothing
		}

		/*
		 * (non-Javadoc)
		 * @see org.xml.sax.helpers.XMLFilterImpl#endDocument()
		 */
		@Override
		public void endDocument() {
			// Start the web server - this way it will be ready when the user clicks the launch button
			startWebServer();
		}

		/*
		 * (non-Javadoc)
		 * @see org.xml.sax.helpers.XMLFilterImpl#endElement(java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public void endElement(String uri, String localName, String qName) {
			// Do nothing
		}

		/*
		 * (non-Javadoc)
		 * @see org.xml.sax.helpers.XMLFilterImpl#startElement(java.lang.String, java.lang.String, java.lang.String,
		 * org.xml.sax.Attributes)
		 */
		@Override
		public void startElement(String uri, String localName, String qName, Attributes atts) {
			if (qName.equalsIgnoreCase(WebsiteNormaliser.WEBSITE_PREFIX + ":" + WebsiteNormaliser.FILE_TAG)) {
				String xenaFilename = atts.getValue(WebsiteNormaliser.WEBSITE_PREFIX + ":" + WebsiteNormaliser.FILE_OUTPUT_FILENAME);
				String originalPath = atts.getValue(WebsiteNormaliser.WEBSITE_PREFIX + ":" + WebsiteNormaliser.FILE_ORIGINAL_PATH_ATTRIBUTE);

				// If this is the first link, set it as the root so we at least have *something* to display
				if (linkIndex.isEmpty()) {
					rootOriginalPath = originalPath;
					rootXenaFilename = xenaFilename;
				}

				// Look for "index.html" or "index.htm". The one with the shortest path will be set as the root page.
				if (pageIsPossiblyRoot(originalPath)) {
					if (!pageIsPossiblyRoot(rootOriginalPath) || originalPath.length() < rootOriginalPath.length()) {
						rootOriginalPath = originalPath;
						rootXenaFilename = xenaFilename;
					}
				}

				linkIndex.put(originalPath, xenaFilename);
			}
		}

		/**
		 * Checks to see if the given path could be the root page of this website.
		 * It gets the name component of the path, and compares it to a list of likely root page names.
		 * @param path
		 * @return
		 */
		private boolean pageIsPossiblyRoot(String path) {
			String nameComponent = path;
			int separatorIndex = path.lastIndexOf("/");
			if (separatorIndex >= 0) {
				nameComponent = path.substring(separatorIndex + 1);
			}
			return rootPageOptionSet.contains(nameComponent);
		}

	}

}
