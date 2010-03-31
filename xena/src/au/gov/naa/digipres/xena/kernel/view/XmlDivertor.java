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

package au.gov.naa.digipres.xena.kernel.view;

import java.io.IOException;

import javax.swing.JComponent;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.kernel.XenaException;

/**
 * When we view Xena XML files, the XML winds its way like a snake through
 * various Xena views and subviews. This tricky process is made convenient by
 * XmlDivertor, which can redirect the SAX XML Stream through to another view.
 *
 */
public class XmlDivertor extends XMLFilterImpl implements LexicalHandler {
	protected XenaView view;

	protected JComponent component;

	protected XenaView subView;

	private int divertNextTag = 0;

	protected int npack = 0;

	protected ContentHandler contentHandler;

	protected LexicalHandler lexicalHandler;

	protected String divertTag;

	protected int diverted = 0;

	public XmlDivertor(XenaView view, JComponent component) {
		this.view = view;
		this.component = component;
	}

	public JComponent getComponent(String tag, XenaView view) {
		return component;
	}

	/**
	 * We've found a tag in the XML stream that signifies that the next tag
	 * will be a subview.
	 */
	public void setDivertNextTag() {
		if (divertTag == null) {
			divertNextTag = 2;
		}
	}

	public boolean isDiverted() {
		return divertTag != null;
	}

	public boolean isDivertNextTag() {
		return divertNextTag != 0;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if (divertTag != null && divertTag.equals(qName)) {
			diverted++;
		}
		if (divertNextTag == 1) {
			divertXml(qName);
		}
		if (divertNextTag != 0) {
			divertNextTag--;
		}
		super.startElement(uri, localName, qName, atts);
	}

	public void divertXml(String name) throws SAXException {
		try {
			assert divertTag == null;
			divertTag = name;
			subView = view.getViewManager().getDefaultView(divertTag, XenaView.REGULAR_VIEW, view.getLevel() + 1);
			view.setSubView(getComponent(name, subView), subView);
			contentHandler = subView.getContentHandler();
			lexicalHandler = subView.getLexicalHandler();
			if (contentHandler != null) {
				contentHandler.startDocument();
				setContentHandler(contentHandler);
			}
		} catch (XenaException x) {
			throw new SAXException(x);
		}
	}

	public void undivertXml() throws SAXException {
		if (contentHandler != null) {
			contentHandler.endDocument();
			try {
				subView.parse();
			} catch (XenaException x) {
				throw new SAXException(x);
			} catch (SAXException x) {
				throw new SAXException(x);
			} catch (IOException x) {
				throw new SAXException(x);
			}
		}
		divertTag = null;
		contentHandler = null;
		lexicalHandler = null;

		/*
		 * ********************************************************************** HACK ALERT, HACK ALERT, HACK ALERT,
		 * HACK ALERT, HACK ALERT, HACK ALERT Due to a bug (or poor design) in Java 1.4.2 the below line of code has
		 * been hacked. It should read setContentHandler(null), and this works fine in Java 1.5.0. This should be
		 * changed when we switch completely to Java 5. HACK ALERT, HACK ALERT, HACK ALERT, HACK ALERT, HACK ALERT, HACK
		 * ALERT **********************************************************************
		 */
		setContentHandler(new XMLFilterImpl());
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		if (divertTag != null && qName.equals(divertTag)) {
			if (diverted == 0) {
				undivertXml();
			} else {
				diverted--;
			}
		}
	}

	/*
	 * *************************
	 * LEXICAL HANDLER METHODS
	 * *************************
	 */

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#comment(char[], int, int)
	 */
	@Override
	public void comment(char[] ch, int start, int length) throws SAXException {
		if (lexicalHandler != null) {
			lexicalHandler.comment(ch, start, length);
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#endCDATA()
	 */
	@Override
	public void endCDATA() throws SAXException {
		if (lexicalHandler != null) {
			lexicalHandler.endCDATA();
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#endDTD()
	 */
	@Override
	public void endDTD() throws SAXException {
		if (lexicalHandler != null) {
			lexicalHandler.endDTD();
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#endEntity(java.lang.String)
	 */
	@Override
	public void endEntity(String name) throws SAXException {
		if (lexicalHandler != null) {
			lexicalHandler.endEntity(name);
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#startCDATA()
	 */
	@Override
	public void startCDATA() throws SAXException {
		if (lexicalHandler != null) {
			lexicalHandler.startCDATA();
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#startDTD(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void startDTD(String name, String publicId, String systemId) throws SAXException {
		if (lexicalHandler != null) {
			lexicalHandler.startDTD(name, publicId, systemId);
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#startEntity(java.lang.String)
	 */
	@Override
	public void startEntity(String name) throws SAXException {
		if (lexicalHandler != null) {
			lexicalHandler.startEntity(name);
		}
	}

}
