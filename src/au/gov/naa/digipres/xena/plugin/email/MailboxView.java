package au.gov.naa.digipres.xena.plugin.email;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.ContentHandler;

import au.gov.naa.digipres.xena.helper.TextView;
import au.gov.naa.digipres.xena.helper.XmlContentHandlerSplitter;
import au.gov.naa.digipres.xena.kernel.XenaException;

/**
 * View to display the mailbox summary Xena file type.
 *
 * @author Chris Bitmead
 */
public class MailboxView extends TextView {
	OutputStream os;

	public MailboxView() {
	}

	public String getViewName() {
		return "Mailbox View";
	}

	public boolean canShowTag(String tag) {
		return tag.equals("mailbox:mailbox");
	}

	public ContentHandler getContentHandler() throws XenaException {
		try {
			XmlContentHandlerSplitter splitter = new XmlContentHandlerSplitter();
			splitter.addContentHandler(getTmpFileContentHandler());
			SAXTransformerFactory tf = (SAXTransformerFactory)SAXTransformerFactory.newInstance();
			TransformerHandler hd = tf.newTransformerHandler();
			Transformer serializer = hd.getTransformer();
			serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			os = new OutputStream() {
				StringBuffer buf = new StringBuffer();

				int lineNo = 0;

				public void write(int b) throws IOException {
					if (b == '\n') {
						appendLine(buf.toString());
						buf = new StringBuffer();
						lineNo++;
					} else {
						buf.append((char)b);
					}
				}

				public void close() {
					if (0 < buf.length()) {
						appendLine(buf.toString());
					}
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
}
