package au.gov.naa.digipres.xena.plugin.xml;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JOptionPane;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.ContentHandler;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.util.TextView;
import au.gov.naa.digipres.xena.util.XmlContentHandlerSplitter;

/**
 * View to show XML as plaintext in its raw form.
 *
 * @author Chris Bitmead
 */
public class XmlRawView extends TextView {
	OutputStream os;

	public String getViewName() {
		return "Raw XML View";
	}

	public boolean canShowTag(String tag) {
        //XML raw view should always be able to view XML.
		return true;
	}

	public void closeContentHandler() {
		if (os != null) {
			try {
				os.close();
			} catch (IOException x) {
				JOptionPane.showMessageDialog(this, x);
			}
		}
		super.closeContentHandler();
	}

	public ContentHandler getContentHandler() throws XenaException {
		try {
			textArea.setText("");
			XmlContentHandlerSplitter splitter = new XmlContentHandlerSplitter();
			splitter.addContentHandler(getTmpFileContentHandler());
			SAXTransformerFactory tf = (SAXTransformerFactory)SAXTransformerFactory.newInstance();
			TransformerHandler hd = tf.newTransformerHandler();
			Transformer serializer = hd.getTransformer();
			serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			final ChunkedCounter counter = new ChunkedCounter();
			counter.checkStart();
			os = new OutputStream() {

				StringBuffer buf = new StringBuffer();

				public void write(int b) throws IOException {
					if (b == '\n') {
						if (counter.checkEnd()) {
							appendLine(buf.toString());
							buf = new StringBuffer();
						}
						counter.checkStart();
					} else {
						if (counter.inProgress()) {
							buf.append((char)b);
						}
					}
				}

				public void close() {
					if (counter.checkEnd()) {
						appendLine(buf.toString());
					}
					counter.end();
					textArea.setCaretPosition(0);
				}
			};
			StreamResult streamResult = new StreamResult(os);
			hd.setResult(streamResult);
			splitter.addContentHandler(hd);
			return splitter;
		} catch (TransformerConfigurationException x) {
			throw new XenaException(x);
		}
	}
    
    
    //TODO: aak - the following was commented out. why?
	/*
	 public void doStart(String namespaceURI, String localName, String qName, Attributes atts) {
	  level++;
	  buf = new StringBuffer();
	  for (int i = 0; i < level; i++) {
	   buf.append("  ");
	  }
	  buf.append("<");
	  buf.append(qName);
	  for (int i = 0; i < atts.getLength(); i++) {
	   buf.append(" ");
	   buf.append(atts.getQName(i));
	   buf.append("=\"");
	   buf.append(atts.getValue(i));
	   buf.append("\"");
	  }
	  buf.append(">");
	  appendLine(buf.toString());
	  buf = new StringBuffer();
	 }

	 public void doEnd(String namespaceURI, String localName, String qName) {
	  if (0 < buf.length()) {
	   appendLine(buf.toString());
	   buf = new StringBuffer();
	  }
	  for (int i = 0; i < level; i++) {
	   buf.append("  ");
	  }
	  buf.append("</");
	  buf.append(qName);
	  buf.append(">");
	  appendLine(buf.toString());
	  buf = null;
	  level--;
	 }

	 public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
	  end(namespaceURI, localName, qName);
	 }

	 public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
	  found = true;
	  lineNo++;
	  start(namespaceURI, localName, qName, atts);
	 }

	   };
	   return ch;
	  } */
	/*	public void updateText() {
	  try {
	   ByteArrayOutputStream os = new ByteArrayOutputStream();
	   Format format = Format.getPrettyFormat();
	   format.setIndent("\t");
	   XMLOutputter outputter = new XMLOutputter(format);
	   outputter.output(getElement(), os);
	   ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
	   BufferedReader br = new BufferedReader(new java.io.InputStreamReader(is, PrintXml.singleton().ENCODING));
	   textArea.setText("");
	   String linetext;
	   while ((linetext = br.readLine()) != null) {
	 appendLine(linetext);
	   }
	   // Without this it seems to make a gigantic window with no scroll bars.
	   textArea.setFont(myFont);
	   XenaMenu.syncAll(menus);

	  } catch (IOException e) {
	   System.out.println(e);
	  }
	 } */
}
