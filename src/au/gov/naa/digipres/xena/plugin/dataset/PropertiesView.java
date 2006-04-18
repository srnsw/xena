package au.gov.naa.digipres.xena.plugin.dataset;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.jdom.Element;
import org.jdom.Namespace;

import au.gov.naa.digipres.xena.kernel.PluginManager;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;
import au.gov.naa.digipres.xena.util.JdomXenaView;

/**
 * View for Xena dataset that displays the dataset specific metadata.
 *
 * @author Chris Bitmead
 */
public class PropertiesView extends JdomXenaView {
	BorderLayout borderLayout1 = new BorderLayout();

	Box vbox;

	Object[][] rowData;

	Box hbox;

	JLabel sizeLabel = new JLabel();

	JLabel sizeValue = new JLabel();

	JTable table;

	JScrollPane scrollPanel;

	Namespace ns = Namespace.getNamespace("dataset", TableView.URI);

	public PropertiesView() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void updateViewFromElement() {
		Element definitions = getElement().getChild("definitions", ns);
		if (definitions != null) {
			Element fieldDefinitions = definitions.getChild("field-definitions", ns);
			java.util.List fieldDefinition = fieldDefinitions.getChildren("field-definition", ns);
			java.util.List headers = new ArrayList();
			java.util.List captions = new ArrayList();
			Iterator it = fieldDefinition.iterator();
			rowData = new Object[fieldDefinition.size()][4];
			int n = 0;
			for (; it.hasNext(); n++) {
				Element def = (Element)it.next();
				Element fieldName = def.getChild("field-name", ns);
				Element fieldCaption = def.getChild("field-caption", ns);
				int i = 0;
				rowData[n][i++] = Integer.toString(n + 1) + " ";
				if (fieldName != null) {
					rowData[n][i] = fieldName.getText();
				}
				i++;
				if (fieldCaption != null) {
					rowData[n][i] = fieldCaption.getText();
				}
				i++;
				rowData[n][i++] = def.getAttribute("type", ns).getValue();
			}
		}
		String[] headings = {
			"Column #", "Name", "Caption", "Type"};
		table.setModel(new DefaultTableModel(rowData, headings) {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		});
		DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
		dtcr.setHorizontalAlignment(JLabel.RIGHT);
		table.getColumnModel().getColumn(0).setCellRenderer(dtcr);
		Element records = getElement().getChild("records", ns);
		java.util.List recs = records.getChildren("record", ns);
		sizeValue.setText(Integer.toString(recs.size()));
	}

	public String getViewName() {
		return "Properties View";
	}

	public void initListeners() {
	}

	public boolean canShowTag(String tag) throws XenaException {
		return tag.equals(PluginManager.singleton().getTypeManager().lookupXenaFileType(XenaDatasetFileType.class).getTag());
	}

	void jbInit() throws Exception {
		sizeLabel.setText("Number of Records: ");
		hbox = Box.createHorizontalBox();
		hbox.add(sizeLabel, null);
		hbox.add(sizeValue, null);
		table = new JTable();
		scrollPanel = new JScrollPane(table);
		vbox = Box.createVerticalBox();
		vbox.add(hbox, null);
		vbox.add(scrollPanel, null);
		this.setLayout(borderLayout1);
		this.add(vbox, BorderLayout.CENTER);
	}
}
