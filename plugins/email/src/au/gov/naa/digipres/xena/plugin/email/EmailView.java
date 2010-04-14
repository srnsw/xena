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

package au.gov.naa.digipres.xena.plugin.email;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Namespace;

import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.core.NormalisedObjectViewFactory;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.view.XenaView;
import au.gov.naa.digipres.xena.util.DOMUtil;
import au.gov.naa.digipres.xena.util.DOMXenaView;

/**
 * View to display an email. Body is displayed at the top and attachments
 * in a list at the bottom.
 *
 */
public class EmailView extends DOMXenaView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private BorderLayout borderLayout1 = new BorderLayout();

	private JSplitPane splitPane1 = new JSplitPane();

	private JSplitPane splitPane2 = new JSplitPane();

	private JPanel headerPanel = new JPanel();

	protected JScrollPane headerScroll = new JScrollPane();

	private BorderLayout borderLayout2 = new BorderLayout();

	private JPanel namePanel = new JPanel();

	private JPanel valuePanel = new JPanel();

	private GridLayout gridLayout1 = new GridLayout();

	private GridLayout gridLayout2 = new GridLayout();

	private JPanel bodyPanel = new JPanel();

	private JPanel attAreaPanel = new JPanel();

	private JPanel attListPanel = new JPanel();

	private JButton openButton = new JButton("Open");

	private BorderLayout borderLayout3 = new BorderLayout();

	private BorderLayout borderLayout4 = new BorderLayout();

	private BorderLayout borderLayout6 = new BorderLayout();

	protected JScrollPane attScrollPane = new JScrollPane();

	private JList attList = new JList();

	private JPanel attPanel = new JPanel();

	// private JList headerList = new JList();

	protected DefaultListModel attModel = new DefaultListModel();

	protected List<Element> attachments = new ArrayList<Element>();

	private BorderLayout borderLayout5 = new BorderLayout();

	public static void main(String[] args) {
		new EmailView();
	}

	public EmailView() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean canShowTag(String tag) throws XenaException {
		return tag.equals(viewManager.getPluginManager().getTypeManager().lookupXenaFileType(XenaEmailFileType.class).getTag());
	}

	@Override
	public String getViewName() {
		return "Email";
	}

	@Override
	public void updateViewFromElement() throws XenaException {
		Element email = getElement();
		final Namespace ns = new Namespace(MessageNormaliser.EMAIL_PREFIX, MessageNormaliser.EMAIL_URI, email);
		Element headers = email.getFirstChildElement("headers", ns.getValue());
		Elements headerItems = headers.getChildElements("header", ns.getValue());

		// Forced to do the following due to some odd XOM design decisions...
		List<Element> headerItemList = new ArrayList<Element>();
		for (int i = 0; i < headerItems.size(); i++) {
			headerItemList.add(headerItems.get(i));
		}

		Collections.sort(headerItemList, new Comparator<Element>() {
			public int compare(Element e1, Element e2) {
				String[] order = {"from", "to", "subject", "cc", "bcc", "date"};
				int ind1 = Integer.MAX_VALUE, ind2 = Integer.MAX_VALUE;

				// Get the attribute values, which will give the name of the header.
				// Each element only has one attribute, with a name of "name", and the value being the name of the email header.
				Attribute headerAttribute = e1.getAttribute(0);
				if (headerAttribute == null || headerAttribute.getLocalName() == null || !headerAttribute.getLocalName().toLowerCase().equals("name")) {
					return 1;
				}
				String headerName1 = headerAttribute.getValue();

				// Get the attribute value for the second element
				headerAttribute = e2.getAttribute(0);
				if (headerAttribute == null || headerAttribute.getLocalName() == null || !headerAttribute.getLocalName().toLowerCase().equals("name")) {
					return -1;
				}
				String headerName2 = headerAttribute.getValue();

				// Order by the hard-coded order in the order array.
				for (int i = 0; i < order.length; i++) {
					if (headerName1.equals(order[i])) {
						ind1 = i;
					}
					if (headerName2.equals(order[i])) {
						ind2 = i;
					}
				}
				return ind1 - ind2;
			}

			@Override
			public boolean equals(Object obj) {
				return true;
			}
		});

		for (Element header : headerItemList) {

			// Get the name of the header. 
			// Cannot just call header.getAttributeValue("name") as files normalised with old versions of xena do not have
			// qualified attribute names, and the new version returns null for those elements.
			Attribute headerAttribute = header.getAttribute(0);
			if (headerAttribute == null || headerAttribute.getLocalName() == null || !headerAttribute.getLocalName().toLowerCase().equals("name")) {
				throw new XenaException("Email header element has an invalid attribute.");
			}
			String headerName = headerAttribute.getValue();

			namePanel.add(new JLabel(headerName));
			String txt = header.getValue();
			if (headerName.equals("Date") || headerName.equals("Received-Date")) {
				if (txt.charAt(txt.length() - 1) == 'Z') {
					txt = txt.substring(0, txt.length() - 1) + "+0000";
				}
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ");
				try {
					Date date = sdf.parse(txt);
					sdf = new SimpleDateFormat("dd MMM yyyy HH':'mm':'ss Z");
					Pattern pat = Pattern.compile(".*([+-][0-9]{4})");
					Matcher mat = pat.matcher(txt);
					if (mat.matches()) {
						TimeZone tz = TimeZone.getTimeZone("GMT" + mat.group(1));
						sdf.setTimeZone(tz);
					}
					txt = sdf.format(date);
				} catch (ParseException ex) {
					// Do nothing, use plain text
				}
			}
			valuePanel.add(new JLabel(txt));
		}

		// Handle the email content
		Element parts = email.getFirstChildElement("parts", ns.getValue());
		Elements partItems = parts.getChildElements("part", ns.getValue());

		// Forced to do the following due to some odd XOM design decisions...
		List<Element> partItemList = new ArrayList<Element>();
		for (int i = 0; i < partItems.size(); i++) {
			partItemList.add(partItems.get(i));
		}

		if (1 < partItemList.size()) {
			splitPane1.add(splitPane2, JSplitPane.BOTTOM);
			splitPane2.add(bodyPanel, JSplitPane.TOP);
		} else {
			splitPane1.add(bodyPanel, JSplitPane.BOTTOM);
		}

		// The main body of the attachment/embedded email will be the the first child of the first part.
		Element body = partItemList.get(0).getChildElements().get(0);
		XenaView view = viewManager.getDefaultView(body.getQualifiedName(), XenaView.REGULAR_VIEW, getLevel() + 1);
		try {
			DOMUtil.writeDocument(view.getContentHandler(), body);
			view.parse();
		} catch (SAXException x) {
			throw new XenaException(x);
		} catch (IOException x) {
			throw new XenaException(x);
		}

		setSubView(bodyPanel, view);
		if (partItemList.size() > 1) {
			// We have at least one embedded email or attachment
			Element npart = partItemList.get(1);

			// Get the body of the part
			body = npart.getChildElements().get(0);

			// Retrieve the view that we should use for this part
			view = viewManager.getDefaultView(body.getQualifiedName(), XenaView.REGULAR_VIEW, getLevel() + 1);

			try {
				DOMUtil.writeDocument(view.getContentHandler(), body);
				view.parse();
			} catch (SAXException x) {
				throw new XenaException(x);
			} catch (IOException x) {
				throw new XenaException(x);
			}
			setSubView(attPanel, view);
		}

		// Create a list of all parts
		for (int i = 1; i < partItemList.size(); i++) {
			Element npart = partItemList.get(i);
			attachments.add(npart);

			// Have to get the attribute value like this as we need to be able to handle
			// files normalised with old versions of Xena, with non-qualified attributes.
			int numAttributes = npart.getAttributeCount();
			String name = null;
			for (int attributeIndex = 0; attributeIndex < numAttributes; attributeIndex++) {
				Attribute currAttribute = npart.getAttribute(attributeIndex);
				if ("filename".equals(currAttribute.getLocalName().toLowerCase())) {
					name = currAttribute.getValue();
				}
			}

			if (name == null) {
				name = "Att-" + Integer.toString(i);
			}
			attModel.addElement(name);
		}
		attList.setSelectedIndex(0);
	}

	void jbInit() throws Exception {
		setLayout(borderLayout1);
		valuePanel.setLayout(gridLayout1);
		namePanel.setLayout(gridLayout2);
		gridLayout2.setColumns(1);
		gridLayout2.setRows(0);
		gridLayout1.setColumns(1);
		gridLayout1.setRows(0);
		headerPanel.setLayout(borderLayout2);
		namePanel.setBorder(BorderFactory.createEtchedBorder());
		valuePanel.setBorder(BorderFactory.createEtchedBorder());
		bodyPanel.setLayout(borderLayout3);
		attAreaPanel.setLayout(borderLayout4);
		headerScroll.setMinimumSize(new Dimension(66, 66));
		attAreaPanel.setMinimumSize(new Dimension(66, 66));
		splitPane2.setResizeWeight(1.0);
		attPanel.setLayout(borderLayout5);
		attListPanel.setLayout(borderLayout6);
		openButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openButton_actionPerformed();
			}
		});
		headerScroll.getViewport().add(headerPanel);
		headerPanel.add(namePanel, BorderLayout.WEST);
		headerPanel.add(valuePanel, BorderLayout.CENTER);
		splitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		this.add(splitPane1, BorderLayout.CENTER);
		splitPane1.add(headerScroll, JSplitPane.TOP);

		attList.setModel(attModel);
		attScrollPane.getViewport().add(attList);
		attAreaPanel.add(attListPanel, BorderLayout.WEST);
		attListPanel.add(attScrollPane, BorderLayout.CENTER);
		attListPanel.add(openButton, BorderLayout.NORTH);
		attAreaPanel.add(attPanel, BorderLayout.CENTER);
		splitPane2.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane2.add(attAreaPanel, JSplitPane.BOTTOM);
		attList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					int index = attList.locationToIndex(e.getPoint());
					Element npart = attachments.get(index);
					Element attEl = npart.getChildElements().get(0);
					XenaView view = viewManager.getDefaultView(attEl.getQualifiedName(), XenaView.REGULAR_VIEW, getLevel() + 1);

					try {
						DOMUtil.writeDocument(view.getContentHandler(), attEl);
						view.parse();
					} catch (SAXException x) {
						throw new XenaException(x);
					} catch (IOException x) {
						throw new XenaException(x);
					}
					setSubView(attPanel, view);
					attAreaPanel.updateUI();
				} catch (XenaException x) {
					JOptionPane.showMessageDialog(EmailView.this, x);
				}
			}

		});

	}

	void openButton_actionPerformed() {
		try {
			int index = attList.getSelectedIndex();
			Element npart = attachments.get(index);
			Element attEl = npart.getChildElements().get(0);

			File tmpFile = getTempFile(attEl);
			NormalisedObjectViewFactory novFactory = new NormalisedObjectViewFactory(viewManager);
			XenaView attachmentView = novFactory.getView(tmpFile);

			// DPR shows the main email view in a dialog. This caused issues when opening up a new frame,
			// I think due to modal issues. So the solution is to open the attachment view in another dialog,
			// This requires a search for the parent frame or dialog, so we can set the parent of the attachment
			// dialog correctly.
			Container parent = getParent();
			while (parent != null && !(parent instanceof Dialog || parent instanceof Frame)) {
				parent = parent.getParent();
			}

			JDialog attachDialog;
			if (parent instanceof Dialog) {
				attachDialog = new JDialog((Dialog) parent);
			} else if (parent instanceof Frame) {
				attachDialog = new JDialog((Frame) parent);
			} else {
				// Fallback...
				attachDialog = new JDialog((Frame) null);
			}

			attachDialog.setLayout(new BorderLayout());
			attachDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			attachDialog.add(attachmentView, BorderLayout.CENTER);
			attachDialog.setSize(800, 600);
			attachDialog.setLocationRelativeTo(this);
			attachDialog.setVisible(true);

		} catch (Exception x) {
			JOptionPane.showMessageDialog(this, x);

		}
	}

	/**
	 * Returns a temporary file containing the XML representation of the given element
	 * @param element
	 * @throws TransformerConfigurationException 
	 * @throws IOException 
	 * @throws JDOMException 
	 * @throws SAXException 
	 */
	private File getTempFile(Element elementParam) throws TransformerConfigurationException, IOException, SAXException {
		SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
		TransformerHandler writer = null;

		writer = tf.newTransformerHandler();
		File file = File.createTempFile("emailview", ".xenatmp");
		file.deleteOnExit();
		XenaInputSource is = new XenaInputSource(file);
		is.setEncoding("UTF-8");
		OutputStream fos = new FileOutputStream(file);
		Writer fw = new OutputStreamWriter(fos, "UTF-8");
		StreamResult streamResult = new StreamResult(fw);
		writer.setResult(streamResult);
		DOMUtil.writeDocument(writer, elementParam);
		fw.close();

		return file;

	}
}
