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

package au.gov.naa.digipres.xena.plugin.html;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.text.html.HTMLEditorKit;
import javax.xml.transform.stream.StreamResult;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXHandler;
import org.jdom.output.Format;
import org.xml.sax.ContentHandler;

import au.gov.naa.digipres.xena.kernel.PrintXml;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.view.XenaView;
import au.gov.naa.digipres.xena.util.XmlContentHandlerSplitter;
import au.gov.naa.digipres.xena.util.XmlDeNormaliser;
import edu.stanford.ejalbert.BrowserLauncher;

/**
 * View to display HTML. We use the Java internal HTML widget to display the HTML,
 * but the fact is the Java HTML viewer is pathetic, so we provide a button
 * to open it in an external browser.
 *
 */
public class HtmlView extends XenaView {
	JScrollPane scrollPane = new JScrollPane();
	HTMLEditorKit htmlKit = new HTMLEditorKit();
	JEditorPane ep = new JEditorPane();
	private JButton externalButton = new JButton();

	private SAXHandler sh;
	protected Element element;
	private Document document;

	private File tempHTMLFile;

	public HtmlView() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
    public String getViewName() {
		return "HTML View";
	}

	@Override
    public boolean canShowTag(String tag) throws XenaException {
		return tag.equals(viewManager.getPluginManager().getTypeManager().lookupXenaFileType(XenaHtmlFileType.class).getTag());
	}

	private void jbInit() throws Exception {
		ep.setEditorKit(htmlKit);
		ep.setContentType("text/html; charset=" + PrintXml.singleton().ENCODING);
		ep.getDocument().putProperty("IgnoreCharsetDirective", new Boolean(true));
		scrollPane.getViewport().add(ep);
		this.add(scrollPane, BorderLayout.CENTER);
		externalButton.setToolTipText("");
		externalButton.setText("Show in Browser Window");
		externalButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				externalButton_actionPerformed(e);
			}
		});
		this.add(externalButton, BorderLayout.NORTH);

	}

	void externalButton_actionPerformed(ActionEvent e) {
		try {
			BrowserLauncher launcher = new BrowserLauncher();
			launcher.openURLinBrowser(tempHTMLFile.toURI().toURL().toString());
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ex);
		}
	}

	@Override
    public ContentHandler getContentHandler() throws XenaException {
		FileOutputStream xenaTempOS = null;
		try {
			tempHTMLFile = File.createTempFile("xena_view", ".html");
			tempHTMLFile.deleteOnExit();
			xenaTempOS = new FileOutputStream(tempHTMLFile);
		} catch (IOException e) {
			throw new XenaException("Problem creating temporary xena output file", e);
		}
		XmlDeNormaliser htmlHandler = new XmlDeNormaliser();
		StreamResult result = new StreamResult(xenaTempOS);
		htmlHandler.setResult(result);
		XmlContentHandlerSplitter splitter = new XmlContentHandlerSplitter();
		sh = new SAXHandler();
		splitter.addContentHandler(sh);
		splitter.addContentHandler(htmlHandler);
		return splitter;
	}

	/**
	 * This fixes a bug in Internet Explorer's rendering, specifically
	 * it allows META REFRESH to work.
	 */
	public static class HackPrintXml extends PrintXml {
		@Override
        public Format getFormatter() {
			Format format = super.getFormatter();
			format.setExpandEmptyElements(true);
			return format;
		}
	}

	@Override
    public void updateViewFromElement() throws XenaException {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			new HackPrintXml().printXml(getElement(), os);
			ByteArrayInputStream in = new ByteArrayInputStream(os.toByteArray());
			try {
				htmlKit.read(in, ep.getDocument(), 0);
			} catch (Exception x) {
				// Sometimes wierd HTML freaks it out.
				throw new XenaException(x);
			}
			ep.setCaretPosition(0);
		} catch (IOException e) {
			throw new XenaException(e);
		}
	}

	public Element getElement() {
		return element;
	}

	public void setElement(Element element) throws XenaException {
		this.element = element;
		updateViewFromElement();
	}

	@Override
    public void parse() throws java.io.IOException, org.xml.sax.SAXException, XenaException {
		if (sh != null) {
			document = sh.getDocument();
			setElement(document.getRootElement());
		}
		super.parse();
		// updateViewFromElement();
	}

	/**
	 * @return Returns the document.
	 */
	public Document getDocument() {
		return document;
	}

}
