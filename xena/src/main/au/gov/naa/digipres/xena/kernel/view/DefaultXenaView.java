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

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.javatools.SpringUtilities;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.DefaultWrapper;

/**
 * Display the deafult meta-data package wrapper. In the future it might be nice
 * to display each element with its own custom view, but for now we just
 * display it all as text.
 *
 */
public class DefaultXenaView extends XenaView {
	XenaView subView;
	int numMeta = 0;
	private JPanel mainPanel = new JPanel();
	private BorderLayout mainPanelBorderLayout = new BorderLayout();
	private JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	private JPanel packagePanel = new JPanel();
	private JPanel dataPanel = new JPanel();
	private BorderLayout dataPanelBorderLayout = new BorderLayout();

	public DefaultXenaView() {
		mainPanel.setLayout(mainPanelBorderLayout);
		this.add(mainPanel);

		mainPanel.add(splitPane, BorderLayout.CENTER);

		dataPanel.setLayout(dataPanelBorderLayout);
		splitPane.add(dataPanel);

		JScrollPane scrollPane = new JScrollPane(packagePanel);
		splitPane.add(scrollPane);

		splitPane.setResizeWeight(1.0);

		SpringLayout springLayout = new SpringLayout();
		packagePanel.setLayout(springLayout);
		Border border1 = BorderFactory.createEtchedBorder(EtchedBorder.RAISED, Color.white, new Color(165, 163, 151));
		Border border2 = new TitledBorder(border1, "Xena Package");
		mainPanel.setBorder(border2);
	}

	@Override
    public String getViewName() {
		return "Xena Package View";
	}

	@Override
    public boolean canShowTag(String tag) throws XenaException {
		return DefaultWrapper.OPENING_TAG.equals(tag);
	}

	@Override
    public ContentHandler getContentHandler() throws XenaException {
		return new MyDivertor(this);
	}

	private class MyDivertor extends XmlDivertor {
		private boolean inMeta = false;
		private StringBuffer stringBuffer;
		private boolean headOnly = false;
		private int metaNest = 0;

		public MyDivertor(XenaView view) throws XenaException {
			super(view, dataPanel);
		}

		@Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
			if (!isDiverted()) {
				if (DefaultWrapper.META_TAG.equals(qName)) {
					inMeta = true;
				} else if (DefaultWrapper.CONTENT_TAG.equals(qName)) {
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
						stringBuffer = new StringBuffer();
						metaNest++;
					}
				}
			}
			super.startElement(uri, localName, qName, atts);
		}

		@Override
        public void characters(char[] ch, int start, int length) throws SAXException {
			if (stringBuffer != null) {
				stringBuffer.append(ch, start, length);
			}
			super.characters(ch, start, length);
		}

		@Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
			super.endElement(uri, localName, qName);
			if (!isDiverted()) {
				if (DefaultWrapper.META_TAG.equals(qName)) {
					inMeta = false;
				} else if (stringBuffer != null && headOnly) {
					JLabel lab = new JLabel(stringBuffer.toString());
					packagePanel.add(lab);
					headOnly = false;
				}
				if (inMeta) {
					metaNest--;
				}
				stringBuffer = null;
			}
		}

		@Override
        public void endDocument() throws SAXException {
			SpringUtilities.makeCompactGrid(packagePanel, 2, numMeta, 5, 5, 5, 5);
			super.endDocument();
		}
	}
}
