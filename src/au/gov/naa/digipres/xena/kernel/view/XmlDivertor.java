package au.gov.naa.digipres.xena.kernel.view;
import java.io.IOException;

import javax.swing.JComponent;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.kernel.PluginManager;
import au.gov.naa.digipres.xena.kernel.XenaException;

/**
 * When we view Xena XML files, the XML winds its way like a snake through
 * various Xena views and subviews. This tricky process is made convenient by
 * XmlDivertor, which can redirect the SAX XML Stream through to another view.
 *
 * @author Chris Bitmead
 */
public class XmlDivertor extends XMLFilterImpl {
	XenaView view;

	JComponent component;

	XenaView subView;

	private int divertNextTag = 0;

	int npack = 0;

	ContentHandler ch;

	String divertTag;

	int diverted = 0;

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

	public void startElement(String uri, String localName,
							 String qName,
							 Attributes atts) throws SAXException {
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
            subView = PluginManager.singleton().getViewManager().getDefaultView(divertTag, XenaView.REGULAR_VIEW, view.getLevel() + 1);
			view.setSubView(getComponent(name, subView),subView);
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
		***********************************************************************
        HACK ALERT, HACK ALERT, HACK ALERT, HACK ALERT, HACK ALERT, HACK ALERT
        Due to a bug (or poor design) in Java 1.4.2 the below line of code
        has been hacked. It should read setContentHandler(null), and this works
        fine in Java 1.5.0. This should be changed when we switch completely
        to Java 5.
        HACK ALERT, HACK ALERT, HACK ALERT, HACK ALERT, HACK ALERT, HACK ALERT
		***********************************************************************
		*/
		setContentHandler(new XMLFilterImpl());
	}

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
