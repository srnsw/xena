package au.gov.naa.digipres.xena.plugin.dataset;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpringLayout;

import org.jdom.Namespace;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.javatools.SpringUtilities;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.view.XenaView;
import au.gov.naa.digipres.xena.kernel.view.XmlDivertor;
import au.gov.naa.digipres.xena.util.XmlContentHandlerSplitter;

/**
 * View for datasets that displays one record at a time. Each record is shown
 * with fields underneath each other. First, Next, Prev and Last  buttons cycle
 * through the list of records.
 *
 * @author Chris Bitmead.
 */

@SuppressWarnings("unchecked")
public class FormView extends XenaView {
	/**
     * 
     */
    private static final long serialVersionUID = 1L;

    final static String URI = "http://preservation.naa.gov.au/dataset/1.0";

	Namespace ns = Namespace.getNamespace("dataset", URI);

	BorderLayout borderLayout1 = new BorderLayout();

	JToolBar toolBar = new JToolBar();

	JButton previousPageButton = new JButton();

	JButton firstPageButton = new JButton();

	JLabel pageLabel = new JLabel();

	JTextField pageTextField = new JTextField();

	JButton nextPageButton = new JButton();

	JButton lastPageButton = new JButton();

	Component currentPage;

	int currentRecordIndex = 0;

	int numRecords;

	JLabel totalPagesLabel = new JLabel();

	JLabel ofLabel = new JLabel();

	Collection items = new ArrayList();

	JScrollPane scrollPane;

	JPanel panel = new JPanel();

	SpringLayout layout = new SpringLayout();

	public FormView() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public ContentHandler getContentHandler() throws XenaException {
		XmlContentHandlerSplitter splitter = new XmlContentHandlerSplitter();
		splitter.addContentHandler(getTmpFileContentHandler());
		panel.removeAll();
		XmlDivertor ch = new XmlDivertor(this, null) {

			int rec = 0;

			int fields = 0;

			String id = null;

			Map captions = new HashMap();

			Map names = new HashMap();

			Map types = new HashMap();

			StringBuffer tmpString;

			public void startElement(String uri, String localName,
									 String qName,
									 Attributes atts) throws SAXException {
				if (qName.equals("dataset:field")) {
					id = atts.getValue("dataset:idref");
					if (rec == currentRecordIndex) {
						setDivertNextTag();
					}
				} else if (qName.equals("dataset:field-definition")) {
					id = atts.getValue("dataset:id");
					String type = atts.getValue("dataset:type");
					if (type != null) {
						types.put(id, type);
					}
				} else if (qName.equals("dataset:field-caption")) {
					if (id != null) {
						tmpString = new StringBuffer();
					}
				} else if (qName.equals("dataset:field-name")) {
					if (id != null) {
						tmpString = new StringBuffer();
					}
				}
				super.startElement(uri, localName, qName, atts);
			}

			public JComponent getComponent(final String qName, final XenaView view) {
				fields++;
				String caption = (String)captions.get(id);
				if (caption == null) {
					caption = id;
				}
				String name = (String)names.get(id);
				String type = (String)types.get(id);
				if (name == null) {
					name = "";
				}
				if (type == null) {
					type = "";
				}
				JLabel nameLabel = new JLabel(" " + caption + " ");
				nameLabel.setToolTipText("name: " + name + ", type: " + type);
				panel.add(nameLabel);
				JPanel vpanel = new JPanel();
				vpanel.setLayout(new BorderLayout());
				panel.add(vpanel);

				JPopupMenu popup = new JPopupMenu();
                // hmmm... not sure if we need to actually decalre anything right here...
                //MouseListener mouseListener = XenaView.addPopupListener(popup, nameLabel);
                XenaView.addPopupListener(popup, nameLabel);
                final JMenuItem changeView = new JMenuItem("Change View");
				popup.add(changeView);
				changeView.addActionListener(
					new java.awt.event.ActionListener() {
					XenaView lastView;

					public void actionPerformed(ActionEvent e) {
						try {
							if (lastView == null) {
								lastView = view;
							}
							XenaView view2 = viewManager.askView(null, qName, getLevel() + 1);
                            viewManager.changeView(lastView, view2);
							lastView = view2;
						} catch (XenaException ex) {
							ex.printStackTrace();
						}
					}
				});

				return vpanel;
			}

			public void endDocument() {
				SpringUtilities.makeCompactGrid(panel, 2, fields, 5, 5, 5, 5);
				numRecords = rec;
				resetPageNumber();
			}

            public void endElement(String uri, String localName, String qName) throws SAXException {
				if (qName.equals("dataset:record")) {
					rec++;
				} else if (qName.equals("dataset:field-definition")) {
					id = null;
				} else if (qName.equals("dataset:field-caption")) {
					captions.put(id, tmpString.toString());
					tmpString = null;
				} else if (qName.equals("dataset:field-name")) {
					names.put(id, tmpString.toString());
					tmpString = null;
				} else if (qName.equals("dataset:field")) {
					id = null;
				}
				super.endElement(uri, localName, qName);
			}

			public void characters(char[] ch, int start, int length) throws SAXException {
				if (tmpString != null) {
					tmpString.append(ch, start, length);
				}
				super.characters(ch, start, length);
			}

		};

		splitter.addContentHandler(ch);
		return splitter;
	}

	public String getViewName() {
		return "Form View";
	}

	public boolean canShowTag(String tag) throws XenaException {
		return tag.equals(viewManager.getPluginManager().getTypeManager().lookupXenaFileType(XenaDatasetFileType.class).getTag());
	}

	public void initListeners() throws XenaException {
		previousPageButton.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				previousPageButton_actionPerformed(e);
			}
		});
		firstPageButton.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				firstPageButton_actionPerformed(e);
			}
		});
		nextPageButton.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nextPageButton_actionPerformed(e);
			}
		});
		lastPageButton.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lastPageButton_actionPerformed(e);
			}
		});
	}

	public void resetPageNumber() {
		pageTextField.setText(Integer.toString(currentRecordIndex + 1));
		totalPagesLabel.setText(Integer.toString(numRecords));
		firstPageButton.setEnabled(currentRecordIndex != 0);
		lastPageButton.setEnabled(currentRecordIndex != numRecords - 1);
		previousPageButton.setEnabled(currentRecordIndex != 0);
		nextPageButton.setEnabled(currentRecordIndex != numRecords - 1);
	}

	void jbInit() throws Exception {
		this.setLayout(borderLayout1);
		previousPageButton.setText(" Prev ");
		firstPageButton.setText(" First ");
		pageLabel.setText(" Record: ");
		pageTextField.setText("1");
		pageTextField.setColumns(4);
		pageTextField.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pageTextField_actionPerformed(e);
			}
		});
		nextPageButton.setText(" Next ");
		lastPageButton.setText(" Last ");
		ofLabel.setText(" of ");
		toolBar.add(firstPageButton, null);
		toolBar.add(previousPageButton, null);
		toolBar.add(pageLabel, null);
		toolBar.add(pageTextField, null);
		toolBar.add(ofLabel, null);
		toolBar.add(totalPagesLabel, null);
		toolBar.add(nextPageButton, null);
		toolBar.add(lastPageButton, null);
		this.add(toolBar, BorderLayout.NORTH);
		panel.setLayout(layout);
		scrollPane = new JScrollPane(panel);
		this.add(scrollPane, BorderLayout.CENTER);
	}

	void changeToRecord(int n) {
		if (n < 0 || numRecords <= n) {
			JOptionPane.showMessageDialog(this, "Bad Number");
		}
		currentRecordIndex = n;
		try {
			rewind();
		} catch (Exception x) {
			JOptionPane.showMessageDialog(this, x.getMessage());
		}
		resetPageNumber();
		this.invalidate();
		this.validate();
	}

	void nextPageButton_actionPerformed(ActionEvent e) {
		changeToRecord(currentRecordIndex + 1);
	}

	void previousPageButton_actionPerformed(ActionEvent e) {
		changeToRecord(currentRecordIndex - 1);
	}

	void firstPageButton_actionPerformed(ActionEvent e) {
		changeToRecord(0);
	}

	void lastPageButton_actionPerformed(ActionEvent e) {
		changeToRecord(numRecords - 1);
	}

	void pageTextField_actionPerformed(ActionEvent e) {
		String value = pageTextField.getText();
		try {
			int n = Integer.parseInt(value);
			changeToRecord(n - 1);
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(null, "Bad Page Number", "Error",
										  JOptionPane.PLAIN_MESSAGE);
			resetPageNumber();
		}
	}
}
