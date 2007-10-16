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

package au.gov.naa.digipres.xena.kernel.view;

import java.io.IOException;

import javax.swing.JComponent;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.kernel.XenaException;

/**
 * When we view Xena XML files, the XML winds its way like a snake through
 * various Xena views and subviews. This tricky process is made convenient by
 * XmlDivertor, which can redirect the SAX XML Stream through to another view.
 *
 */
public class XmlDivertor extends XMLFilterImpl {
	protected XenaView view;

	protected JComponent component;

	protected XenaView subView;

	private int divertNextTag = 0;

	protected int npack = 0;

	protected ContentHandler ch;

	protected String divertTag;

	protected int diverted = 0;

	public XmlDivertor(XenaView view, JComponent component) throws XenaException {
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
			ch = subView.getContentHandler();
			if (ch != null) {
				ch.startDocument();
				this.setContentHandler(ch);
			}
		} catch (XenaException x) {
			throw new SAXException(x);
		}
	}

	public void undivertXml() throws SAXException {
		if (ch != null) {
			ch.endDocument();
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
		ch = null;
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

}
