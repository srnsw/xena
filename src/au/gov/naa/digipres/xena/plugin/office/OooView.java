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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLEditorKit;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.view.XenaView;
import au.gov.naa.digipres.xena.util.BinaryDeNormaliser;

import com.jclark.xsl.sax.OutputMethodHandlerImpl;
import com.jclark.xsl.sax.OutputStreamDestination;
import com.jclark.xsl.sax.XSLProcessorImpl;

/**
 * View for Xena office documents. Show a quick and dirty version in the window
 * by using style sheets from openoffice.org to convert to HTML, and have a button
 * to show it properly in OpenOffice.org.
 *
 */
public class OooView extends XenaView {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JButton launchButton = new JButton();

	JScrollPane scrollPane = new JScrollPane();

	HTMLEditorKit htmlKit = new HTMLEditorKit();

	JEditorPane ep = new JEditorPane();

	private File openDocumentFile;

	private static final String HTML_STYLESHEET = "au/gov/naa/digipres/xena/plugin/office/xsl/odt_to_xhtml.xsl";

	public OooView() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getViewName() {
		return "Office View";
	}

	@Override
	public boolean canShowTag(String tag) throws XenaException {
		return tag.equals(viewManager.getPluginManager().getTypeManager().lookupXenaFileType(XenaOooFileType.class).getTag());
	}

	@Override
	public void parse() throws XenaException, IOException, SAXException {
		try {
			XSLProcessorImpl xsl = new XSLProcessorImpl();
			InputSource style = new InputSource(getClass().getClassLoader().getResource(HTML_STYLESHEET).toExternalForm());

			ZipFile openDocZip = new ZipFile(openDocumentFile);
			ZipEntry contentEntry = openDocZip.getEntry("content.xml");

			if (contentEntry == null) {
				// No content - probably a template
				// Not sure what to display here? Nothing at the moment....
			} else {
				InputStream contentIS = openDocZip.getInputStream(contentEntry);

				InputSource xml = new InputSource(contentIS);

				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser parser = factory.newSAXParser();
				XMLReader reader = parser.getXMLReader();
				reader.setFeature("http://xml.org/sax/features/namespaces", true);

				org.xml.sax.helpers.XMLReaderAdapter adapter = new org.xml.sax.helpers.XMLReaderAdapter(reader);
				xsl.setParser(adapter);
				xsl.setErrorHandler(new ErrorHandler() {
					public void warning(SAXParseException e) {
						e.printStackTrace();
					}

					public void error(SAXParseException e) {
						e.printStackTrace();
					}

					public void fatalError(SAXParseException e) throws SAXException {
						throw e;
					}

				});

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				OutputStreamDestination fd = new OutputStreamDestination(baos);
				OutputMethodHandlerImpl outputMethodHandler = new OutputMethodHandlerImpl(xsl);
				xsl.setOutputMethodHandler(outputMethodHandler);

				outputMethodHandler.setDestination(fd);

				xsl.loadStylesheet(style);
				xsl.setEntityResolver(new EntityResolver() {
					public InputSource resolveEntity(String publicId, String systemId) {
						return null;
					}
				});
				// xml.setEncoding("UTF8");
				xsl.parse(xml);
				ByteArrayInputStream in = new ByteArrayInputStream(baos.toByteArray());
				int c;
				while ((c = in.read()) != '>') {
					// Nothing.
				}
				ByteArrayOutputStream baos2 = new ByteArrayOutputStream(baos.size());
				int delay = -1;
				// Hacks. HTML viewer doesn't like <p/> or characters >= 128.
				while (0 <= (c = in.read())) {
					if (delay != -1) {
						if (c != '>') {
							baos2.write(delay);
						}
						delay = -1;
					}
					if (128 <= c) {
						// Nothing
					} else if (c == '/') {
						delay = c;
					} else {
						baos2.write(c);
					}
				}
				ByteArrayInputStream in2 = new ByteArrayInputStream(baos2.toByteArray());
				htmlKit.read(in2, ep.getDocument(), 0);
				ep.setCaretPosition(0);

				contentIS.close();
				baos.close();
				baos2.close();
				in.close();
				in2.close();
			}
		}

		catch (Exception x) {
			x.printStackTrace();
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				baos.write("<html><body>Unable to show Preview</body></html>".getBytes());
				ByteArrayInputStream in = new ByteArrayInputStream(baos.toByteArray());
				htmlKit.read(in, ep.getDocument(), 0);
				in.close();
				baos.close();
			} catch (IOException x2) {
				throw new XenaException(x2);
			} catch (BadLocationException x2) {
				throw new XenaException(x2);
			}
		}
		super.parse();
	}

	void launchButton_actionPerformed() {
		try {
			OpenOfficeConverter.loadDocument(openDocumentFile, true, viewManager.getPluginManager());
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ex);
		}
	}

	@Override
	public ContentHandler getContentHandler() throws XenaException {
		FileOutputStream xenaTempOS = null;
		try {
			openDocumentFile = File.createTempFile("opendoc", ".tmp");
			openDocumentFile.deleteOnExit();
			xenaTempOS = new FileOutputStream(openDocumentFile);
		} catch (IOException e) {
			throw new XenaException("Problem creating temporary xena output file", e);
		}
		BinaryDeNormaliser base64Handler = new BinaryDeNormaliser();
		StreamResult result = new StreamResult(xenaTempOS);
		base64Handler.setResult(result);
		return base64Handler;
	}

	private void jbInit() throws Exception {
		launchButton.setToolTipText("");
		launchButton.setText("Show in OpenOffice.org");
		launchButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				launchButton_actionPerformed();
			}
		});
		this.add(launchButton, BorderLayout.NORTH);
		ep.setEditable(false);
		ep.setEditorKit(htmlKit);
		scrollPane.getViewport().add(ep);
		this.add(scrollPane, BorderLayout.CENTER);
	}
}
