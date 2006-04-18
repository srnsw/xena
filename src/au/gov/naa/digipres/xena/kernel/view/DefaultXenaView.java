package au.gov.naa.digipres.xena.kernel.view;
import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.xml.transform.sax.SAXTransformerFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.javatools.SpringUtilities;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.DefaultWrapper;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;
import au.gov.naa.digipres.xena.util.XmlContentHandlerSplitter;
/**
 * Display the deafult meta-data package wrapper. In the future it might be nice
 * to display each element with its own custom view, but for now we just
 * display it all as text.
 *
 * @author aak
 */
public class DefaultXenaView extends XenaView {
	XenaView subView;

	int numMeta = 0;

	JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

	JPanel packagePanel = new JPanel();

	JPanel dataPanel = new JPanel();

	BorderLayout borderLayout2 = new BorderLayout();

	BorderLayout borderLayout3 = new BorderLayout();

	JPanel panel = new JPanel();

	public DefaultXenaView() {
		panel.setLayout(borderLayout3);
		this.add(panel);

		panel.add(split, BorderLayout.CENTER);

		dataPanel.setLayout(borderLayout2);
		split.add(dataPanel);

		JScrollPane scrollPane = new JScrollPane(packagePanel);
		split.add(scrollPane);

		split.setResizeWeight(1.0);

		SpringLayout springLayout = new SpringLayout();
		packagePanel.setLayout(springLayout);
		Border border1 = BorderFactory.createEtchedBorder(EtchedBorder.RAISED, Color.white, new Color(165, 163, 151));
		Border border2 = new TitledBorder(border1, "Xena Package");
		panel.setBorder(border2);
	}

	public String getViewName() {
		return "Xena Package View";
	}

	public boolean canShowTag(String tag) throws XenaException {
		return tag.equals(DefaultWrapper.OPENING_TAG);
	}

	public ContentHandler getContentHandler() throws XenaException {
		// Don't close the file here. Too early.
		XmlContentHandlerSplitter splitter = new XmlContentHandlerSplitter();
		SAXTransformerFactory tf = (SAXTransformerFactory)
			SAXTransformerFactory.
			newInstance();
		ContentHandler writer = getTmpFileContentHandler();
		splitter.addContentHandler(writer);
		XMLFilterImpl pkgHandler = new MyDivertor(this);
		splitter.addContentHandler(pkgHandler);
		return splitter;
	}

	public class MyDivertor extends XmlDivertor {
		String lastTag;

		boolean inMeta = false;

		StringBuffer sb;

		boolean headOnly = false;

		int metaNest = 0;

		public MyDivertor(XenaView view) throws XenaException {
			super(view, dataPanel);
		}

		public void startElement(String uri, String localName,
								 String qName,
								 Attributes atts) throws SAXException {
			if (!isDiverted()) {
				lastTag = qName;
				if (qName.equals(DefaultWrapper.META_TAG)) {
					inMeta = true;
				} else if (qName.equals(DefaultWrapper.CONTENT_TAG)) {
					this.setDivertNextTag();
				} else {
					if (headOnly) {
						JLabel labl = new JLabel(" ");
						packagePanel.add(labl);
						headOnly = false;
					}
					if (inMeta) {
						StringBuffer pad = new StringBuffer();
						for (int i = 0; i < metaNest; i++) {
							pad.append("  ");
						}
						pad.append(qName);
						JLabel labl = new JLabel(pad.toString());
						packagePanel.add(labl);
						numMeta++;
						headOnly = true;
						sb = new StringBuffer();
						metaNest++;
					}
				}
			}
			super.startElement(uri, localName, qName, atts);
		}

		public void characters(char[] ch, int start, int length) throws
			SAXException {
			if (sb != null) {
				sb.append(ch, start, length);
			}
			super.characters(ch, start, length);
		}

		public void endElement(String uri, String localName, String qName) throws
			SAXException {
			super.endElement(uri, localName, qName);
			if (!isDiverted()) {
				if (qName.equals(DefaultWrapper.META_TAG)) {
					inMeta = false;
				} else if (sb != null && headOnly) {
					JLabel lab = new JLabel(sb.toString());
					packagePanel.add(lab);
					headOnly = false;
				}
				if (inMeta) {
					metaNest--;
				}
				sb = null;
			}
		}

		public void endDocument() throws SAXException {
			SpringUtilities.makeCompactGrid(packagePanel, 2, numMeta, 5, 5, 5,5);
			super.endDocument();
		}
	}
}
