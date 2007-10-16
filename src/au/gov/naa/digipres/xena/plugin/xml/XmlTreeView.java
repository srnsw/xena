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
 * @author Dan Spasojevic
 * @author Justin Waddell
 */

package au.gov.naa.digipres.xena.plugin.xml;

import java.awt.BorderLayout;
import java.util.Stack;

import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.util.XmlContentHandlerSplitter;

/**
 * View to show XML as a tree.
 *
 */
public class XmlTreeView extends XmlRawView {
	JScrollPane scrollPane = new JScrollPane();

	BorderLayout borderLayout1 = new BorderLayout();

	XmlTree xt;

	@Override
    public String getViewName() {
		return "XML Tree View";
	}

	public boolean canShowTag(String tag, int level) {
		return true;
	}

	@Override
    public void initListeners() {
	}

	/*
	 * public void updateViewFromElement() { xt.setElement(getElement()); }
	 */

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

		@Override
        public void startDocument() {
		}

		@Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
			isLeaf = true;
			assert qName != null;
			stack.push(new Element(qName, atts));
			counter.checkStart();
		}

		@Override
        public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
			if (counter.checkEnd() && isLeaf) {
				xt.addNode(stack);
			}
			isLeaf = false;
			stack.pop();
		}

		@Override
        public void endDocument() {
			counter.end();
		}

		@Override
        public void characters(char[] ch, int start, int length) throws SAXException {
			if (counter.inProgress()) {
				XmlTreeView.MyContentHandler.Element el = (XmlTreeView.MyContentHandler.Element) stack.peek();
				el.data.append(ch, start, length);
			}
		}
	};

	@Override
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
