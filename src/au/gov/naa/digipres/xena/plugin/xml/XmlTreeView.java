package au.gov.naa.digipres.xena.plugin.xml;
import java.awt.BorderLayout;
import java.util.Stack;

import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.javatools.JarPreferences;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.util.XmlContentHandlerSplitter;

/**
 * View to show XML as a tree.
 *
 * @author Chris Bitmead
 */
public class XmlTreeView extends XmlRawView {
	JScrollPane scrollPane = new JScrollPane();

	BorderLayout borderLayout1 = new BorderLayout();

	XmlTree xt;

	public String getViewName() {
		return "XML Tree View";
	}

	public boolean canShowTag(String tag, int level) {
		return true;
	}

	public void initListeners() {
	}

	/*	public void updateViewFromElement() {
	  xt.setElement(getElement());
	 } */

	/**
	 *  constructor for the XmlTreeView object
	 */
	public XmlTreeView() {
		try {
			jbInit2();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public class MyContentHandler extends XMLFilterImpl {
		class Element {
			Element(String qName, Attributes atts) {
				this.qName = qName;
				this.atts = atts;
				assert this.qName != null;
			}

			String qName;

			Attributes atts;

			StringBuffer data = new StringBuffer();

			DefaultMutableTreeNode node;
		}

		ChunkedCounter counter = new ChunkedCounter();

		Stack stack = new Stack();

		boolean isLeaf = true;

		public MyContentHandler() {
		}

		public void startDocument() {
		}

		public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
			isLeaf = true;
			assert qName != null;
			stack.push(new Element(qName, atts));
			counter.checkStart();
		}

		public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
			if (counter.checkEnd() && isLeaf) {
				xt.addNode(stack);
			}
			isLeaf = false;
			stack.pop();
		}

		public void endDocument() {
			counter.end();
		}

		public void characters(char[] ch, int start, int length) throws SAXException {
			if (counter.inProgress()) {
				XmlTreeView.MyContentHandler.Element el = (XmlTreeView.MyContentHandler.Element)stack.peek();
				el.data.append(ch, start, length);
			}
		}
	};

	public ContentHandler getContentHandler() throws XenaException {
		xt.clear();
		XmlContentHandlerSplitter splitter = new XmlContentHandlerSplitter();
		splitter.addContentHandler(getTmpFileContentHandler());
		ContentHandler ch = new MyContentHandler();
		splitter.addContentHandler(ch);
		return splitter;
	}

	protected void jbInit2() throws Exception {
		this.setLayout(borderLayout1);
		this.add(scrollPane, BorderLayout.CENTER);
		xt = new XmlTree();
		scrollPane.getViewport().add(xt);
	}
}
