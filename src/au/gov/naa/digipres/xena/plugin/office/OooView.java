package au.gov.naa.digipres.xena.plugin.office;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLEditorKit;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.view.XenaView;
import au.gov.naa.digipres.xena.util.XmlContentHandlerSplitter;

import com.jclark.xsl.sax.OutputMethodHandlerImpl;
import com.jclark.xsl.sax.OutputStreamDestination;
import com.jclark.xsl.sax.XSLProcessorImpl;

/**
 * View for Xena office documents. Show a quick and dirty version in the window
 * by using style sheets from openoffice.org to convert to HTML, and have a button
 * to show it properly in OpenOffice.org.
 *
 * @author Chris Bitmead
 */
public class OooView extends XenaView {
	private JButton launchButton = new JButton();

	JScrollPane scrollPane = new JScrollPane();

	HTMLEditorKit htmlKit = new HTMLEditorKit();

	JEditorPane ep = new JEditorPane();

	public static final String WRITER_EXT = "xmw";

	public static final String CALC_EXT = "xmc";

	public static final String IMPRESS_EXT = "xmi";

	String officeClass;

	public OooView() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getViewName() {
		return "Office View";
	}

	public boolean canShowTag(String tag) throws XenaException {
		return tag.equals(viewManager.getPluginManager().getTypeManager().lookupXenaFileType(XenaOooFileType.class).getTag());
	}

	public void parse() throws XenaException, IOException, SAXException {
		try {
			XSLProcessorImpl xsl = new XSLProcessorImpl();
			InputSource style = new InputSource(getClass().getClassLoader().getResource("au/gov/naa/digipres/xena/plugin/office/xsl/main_html.xsl").toExternalForm());

			InputSource xml = getTmpFile();
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			XMLReader reader = parser.getXMLReader();
			reader.setFeature("http://xml.org/sax/features/namespaces", true);
			
			org.xml.sax.helpers.XMLReaderAdapter adapter = 
				new org.xml.sax.helpers.XMLReaderAdapter(reader);
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

			}
			);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			OutputStreamDestination fd = new OutputStreamDestination(baos);
			OutputMethodHandlerImpl outputMethodHandler = new OutputMethodHandlerImpl(xsl);
			xsl.setOutputMethodHandler(outputMethodHandler);

			outputMethodHandler.setDestination(fd);

			xsl.loadStylesheet(style);
			xsl.setEntityResolver(new EntityResolver() {
				public InputSource resolveEntity(String publicId,
												 String systemId) throws SAXException, IOException {
					return null;
				}
			});
//			xml.setEncoding("UTF8");
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
		}

		catch (Exception x) {
			x.printStackTrace();
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				baos.write("<html><body>Unable to show Preview</body></html>".getBytes());
				ByteArrayInputStream in = new ByteArrayInputStream(baos.toByteArray());
				htmlKit.read(in, ep.getDocument(), 0);
			} catch (IOException x2) {
				throw new XenaException(x2);
			} catch (BadLocationException x2) {
				throw new XenaException(x2);
			}
		}
		super.parse();
	}

	void launchButton_actionPerformed(ActionEvent e) {
		File output = null;
		try {
			
			
			
			String ext;
			if (officeClass.equals("text")) {
				ext = OooView.WRITER_EXT;
			} else if (officeClass.equals("spreadsheet")) {
				ext = OooView.CALC_EXT;
			} else if (officeClass.equals("presentation")) {
				ext = OooView.IMPRESS_EXT;
			} else {
                new XenaException("Unknown OpenOffice.org type").printStackTrace();
                //MainFrame.singleton().showError("Unknown OpenOffice.org type");
				return;
			}
			
			// This whole section is a hack due to OOo not accepting empty elements <foo/>
			// delete this whole section if issue 44955 OOo bug is resolved
			FileInputStream fis = new FileInputStream(getTmpFile().getFile());
			InputStreamReader rd = new InputStreamReader(fis, "UTF-8");

			XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
			InputSource source = new InputSource(rd);
			// This is a hack within a hack.  Due to what I consider a bug
			// (reported XALANJ-2118 in the xalan bug system), xalan does not
			// pass through namespace declarations for attributes, only for
			// tags. So we have to fudge this one.
			XMLFilterImpl filter = hackContentHandler();
			reader.setContentHandler(filter);
			filter.setParent(reader);
			SAXTransformerFactory tf = (SAXTransformerFactory)
				SAXTransformerFactory.newInstance();
			File outfile = File.createTempFile("output", "." + ext);
			outfile.deleteOnExit();
			OutputStream os = new FileOutputStream(outfile);
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
			TransformerHandler twriter = tf.newTransformerHandler();
			StreamResult streamResult = new StreamResult(osw);
			twriter.setResult(streamResult);
			filter.setContentHandler(twriter);

			reader.parse(source);


			// This is a hack in a hack in a hack. We need the ExpandEmptyElements
			// feature because of an OOo bug, and SAX doesn't have this feature.
			// So go back to JDOM.
			File outfile2 = File.createTempFile("output2", "." + ext);
			outfile2.deleteOnExit();

			org.jdom.input.SAXBuilder bu = new org.jdom.input.SAXBuilder();
			 org.jdom.Document doc = bu.build(outfile);
			 org.jdom.output.Format form = org.jdom.output.Format.getRawFormat();
			 form.setEncoding("UTF-8");
			 form.setExpandEmptyElements(true);
			 org.jdom.output.XMLOutputter so = new org.jdom.output.XMLOutputter(form);
			 OutputStream os2 = new FileOutputStream(outfile2);
			 OutputStreamWriter osw2 = new OutputStreamWriter(os2, "UTF-8");
			 so.output(doc, osw2);


//			so.output(doc, w);
			osw.close();
			rd.close();
			OfficeToXenaOooNormaliser.loadDocument(outfile2, true, viewManager.getPluginManager());

//						OfficeToXenaOooNormaliser.loadDocument(this.getTmpFile().getFile(), true);
		} catch (Exception ex) {

            new XenaException("Unknown OpenOffice.org type", ex).printStackTrace();
			//MainFrame.singleton().showError(ex);
		} finally {
			if (output != null) {
				output.delete();
			}
		}
	}

	public ContentHandler getContentHandler() throws XenaException {
		XmlContentHandlerSplitter splitter = new XmlContentHandlerSplitter();
		XMLFilterImpl hack = hackContentHandler();
		ContentHandler tmpFileHandler = getTmpFileContentHandler();
		hack.setContentHandler(tmpFileHandler);
		splitter.addContentHandler(hack);
		/*		FileOutputStream wx = null;
		  OutputStreamWriter wo = null;
		  try {
		 wx = new FileOutputStream("c:/tmp/foo.x");
		 wo = new OutputStreamWriter(wx);
		  } catch (IOException ex) {
		   ex.printStackTrace();
		  }
		  final Writer w = wo; */
		XMLFilterImpl ch = new XMLFilterImpl() {
			public void startElement(String uri, String localName, String qName,
									 Attributes atts) throws SAXException {
				// Only set officeClass if it hasn't already been set.
				// This way the class of the base document, and not any embedded documents, will be used.
				
				if (officeClass == null || officeClass.equals(""))
				{
					if (qName.equals("office:document")) {
						officeClass = atts.getValue("office:class");
					}
				}
				super.startElement(uri, localName, qName, atts);
			};

			// DEBUG
			/*			public void characters(char[] ch, int start, int length) throws SAXException {
			 String s = new String(ch, start, length);
			 try {
			  w.write(s);
			  w.write("\n");
			  w.flush();
			 } catch (IOException ex) {
			  ex.printStackTrace();
			 }
			 super.characters(ch, start, length);
			   } */
		};
		splitter.addContentHandler(ch);
		return splitter;
	}

	/** Due to what I consider a bug
	   (reported XALANJ-2118 in the xalan bug system), xalan does not
	   pass through namespace declarations for attributes, only for
	   tags. So we have to fudge this one. */

	XMLFilterImpl hackContentHandler() {
		return new XMLFilterImpl() {
			public void startElement(String uri, String localName, String qName,
									 Attributes atts) throws SAXException {
				if (qName.equals("office:document")) {
					super.startPrefixMapping("fo", "http://www.w3.org/1999/XSL/Format");
					super.startPrefixMapping("draw", "http://openoffice.org/2000/drawing");
					super.startPrefixMapping("text", "http://openoffice.org/2000/text");
					super.startPrefixMapping("svg", "http://www.w3.org/2000/svg");
					super.startPrefixMapping("style","http://openoffice.org/2000/style");
					super.startPrefixMapping("xlink","http://www.w3.org/1999/xlink");
					super.startPrefixMapping("office","http://openoffice.org/2000/office");
					super.startPrefixMapping("table","http://openoffice.org/2000/table");
					super.startPrefixMapping("dc","http://purl.org/dc/elements/1.1/");
					super.startPrefixMapping("meta","http://openoffice.org/2000/meta");
					super.startPrefixMapping("number","http://openoffice.org/2000/datastyle");
					super.startPrefixMapping("chart","http://openoffice.org/2000/chart");
					super.startPrefixMapping("dr3d","http://openoffice.org/2000/dr3d");
					super.startPrefixMapping("math","http://www.w3.org/1998/Math/MathML");
					super.startPrefixMapping("form","http://openoffice.org/2000/form");
					super.startPrefixMapping("script","http://openoffice.org/2000/script");
					super.startPrefixMapping("config","http://openoffice.org/2001/config");						
				}
				super.startElement(uri, localName, qName, atts);
			}
		};
	}

	private void jbInit() throws Exception {
		launchButton.setToolTipText("");
		launchButton.setText("Show in OpenOffice.org");
		launchButton.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				launchButton_actionPerformed(e);
			}
		});
		this.add(launchButton, BorderLayout.NORTH);
		ep.setEditable(false);
		ep.setEditorKit(htmlKit);
		scrollPane.getViewport().add(ep);
		this.add(scrollPane, BorderLayout.CENTER);
	}
}
