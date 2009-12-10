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

package au.gov.naa.digipres.xena.plugin.xml;

import java.io.OutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.util.TextView;
import au.gov.naa.digipres.xena.util.XmlContentHandlerSplitter;

/**
 * View to show XML as plaintext in its raw form.
 *
 */
public class XmlRawView extends TextView {

	private TransformerHandler saxTransformerHandler = null;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	RawXMLOutputStream os;

	@Override
	public String getViewName() {
		return "Raw XML View";
	}

	@Override
	public boolean canShowTag(String tag) {
		// XML raw view should always be able to view XML.
		return true;
	}

	@Override
	public void closeContentHandler() {
		if (os != null) {
			os.close();
		}
		super.closeContentHandler();
	}

	@Override
	public ContentHandler getContentHandler() throws XenaException {
		try {
			textArea.setText("");

			ContentHandler ch = getSaxTransformerHandler();

			if (getTmpFile() != null) {
				return ch;
			}

			XmlContentHandlerSplitter splitter = new XmlContentHandlerSplitter();
			splitter.addContentHandler(ch);
			splitter.addContentHandler(getTmpFileContentHandler());
			return splitter;
		} catch (TransformerConfigurationException x) {
			throw new XenaException(x);
		}
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.view.XenaView#getLexicalHandler()
	 */
	@Override
	public LexicalHandler getLexicalHandler() throws XenaException {
		try {
			return getSaxTransformerHandler();
		} catch (TransformerConfigurationException x) {
			throw new XenaException(x);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

	/**
	 * @return
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerConfigurationException
	 */
	private TransformerHandler getSaxTransformerHandler() throws TransformerFactoryConfigurationError, TransformerConfigurationException {
		if (saxTransformerHandler == null) {
			SAXTransformerFactory factory = (SAXTransformerFactory) TransformerFactory.newInstance();
			saxTransformerHandler = factory.newTransformerHandler();
			Transformer serializer = saxTransformerHandler.getTransformer();
			serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			os = new RawXMLOutputStream();
		}

		ChunkedCounter counter = new ChunkedCounter();
		counter.checkStart();
		os.setChunkedCounter(counter);
		StreamResult streamResult = new StreamResult(os);

		saxTransformerHandler.setResult(streamResult);
		return saxTransformerHandler;
	}

	/**
	 * We want the raw view to only be displayed by default if there are no other options
	 */
	@Override
	public int getPriority() {
		return -10000;
	}

	public class RawXMLOutputStream extends OutputStream {

		StringBuffer buf = new StringBuffer();
		ChunkedCounter counter = new ChunkedCounter();

		public void setChunkedCounter(ChunkedCounter counter) {
			this.counter = counter;
		}

		@Override
		public void write(int b) {
			if (b == '\n') {
				if (counter.checkEnd()) {
					appendLine(buf.toString());
					buf = new StringBuffer();
				}
				counter.checkStart();
			} else {
				if (counter.inProgress()) {
					buf.append((char) b);
				}
			}
		}

		@Override
		public void close() {
			if (counter.checkEnd()) {
				appendLine(buf.toString());
			}
			counter.end();
			textArea.setCaretPosition(0);
		}
	}

}
