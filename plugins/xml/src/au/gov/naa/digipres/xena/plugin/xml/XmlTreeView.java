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

import java.awt.BorderLayout;
import java.util.Stack;

import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * View to show XML as a tree.
 *
 */
public class XmlTreeView extends XmlRawView {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	JScrollPane scrollPane = new JScrollPane();

	BorderLayout borderLayout1 = new BorderLayout();

	XmlTree xmlTree;

	XmlTreeHandler handler = null;

	@Override
	public String getViewName() {
		return "XML Tree View";
	}

	@Override
	public void initListeners() {
		// Do not want to add any listeners added by superclasses
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

	public class XmlTreeHandler extends XMLFilterImpl implements LexicalHandler {
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

		Stack<Element> stack = new Stack<Element>();

		boolean isLeaf = true;

		public XmlTreeHandler() {
			// Nothing to do here
		}

		@Override
		public void startDocument() {
			// Nothing to do here
		}

		@Override
		public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
			isLeaf = true;
			assert qName != null;
			stack.push(new Element(qName, atts));
			counter.checkStart();
		}

		@Override
		public void endElement(String namespaceURI, String localName, String qName) {
			if (counter.checkEnd() && isLeaf) {
				xmlTree.addNode(stack);
			}
			isLeaf = false;
			stack.pop();
		}

		@Override
		public void endDocument() {
			counter.end();
		}

		@Override
		public void characters(char[] ch, int start, int length) {
			if (counter.inProgress()) {
				Element el = stack.peek();
				el.data.append(ch, start, length);
			}
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ext.LexicalHandler#comment(char[], int, int)
		 */
		@Override
		public void comment(char[] ch, int start, int length) {
			String commentStr = new String(ch, start, length);
			stack.push(new Element("--" + commentStr + "--", null));
			xmlTree.addNode(stack);
			stack.pop();
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ext.LexicalHandler#endCDATA()
		 */
		@Override
		public void endCDATA() {
			// Do nothing
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ext.LexicalHandler#endDTD()
		 */
		@Override
		public void endDTD() {
			// Do nothing
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ext.LexicalHandler#endEntity(java.lang.String)
		 */
		@Override
		public void endEntity(String name) {
			// Do nothing
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ext.LexicalHandler#startCDATA()
		 */
		@Override
		public void startCDATA() {
			// Do nothing
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ext.LexicalHandler#startDTD(java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public void startDTD(String name, String publicId, String systemId) {
			// Do nothing
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ext.LexicalHandler#startEntity(java.lang.String)
		 */
		@Override
		public void startEntity(String name) {
			// Do nothing
		}
	}

	@Override
	public ContentHandler getContentHandler() {
		xmlTree.clear();
		return getXmlTreeHandler();
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.plugin.xml.XmlRawView#getLexicalHandler()
	 */
	@Override
	public LexicalHandler getLexicalHandler() {
		return getXmlTreeHandler();
	}

	private XmlTreeHandler getXmlTreeHandler() {
		if (handler == null) {
			handler = new XmlTreeHandler();
		}
		return handler;
	}

	protected void jbInit2() throws Exception {
		setLayout(borderLayout1);
		this.add(scrollPane, BorderLayout.CENTER);
		xmlTree = new XmlTree();
		scrollPane.getViewport().add(xmlTree);
	}

}
