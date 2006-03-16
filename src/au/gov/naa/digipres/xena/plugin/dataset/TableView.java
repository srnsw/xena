package au.gov.naa.digipres.xena.plugin.dataset;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.gui.InternalFrame;
import au.gov.naa.digipres.xena.gui.MainFrame;
import au.gov.naa.digipres.xena.gui.XenaMenu;
import au.gov.naa.digipres.xena.helper.JdomUtil;
import au.gov.naa.digipres.xena.helper.JdomXenaView;
import au.gov.naa.digipres.xena.kernel.PluginManager;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;
import au.gov.naa.digipres.xena.kernel.type.XenaFileType;
import au.gov.naa.digipres.xena.kernel.view.ViewManager;
import au.gov.naa.digipres.xena.kernel.view.XenaView;

/**
 * View for Xena dataset that displays the data in a grid fashion.
 *
 * @author Chris Bitmead
 */
public class TableView extends JdomXenaView {
	final static String URI = "http://preservation.naa.gov.au/dataset/1.0";

	Namespace ns = Namespace.getNamespace("dataset", URI);

	Element definitions;

	Element fieldDefinitions;

	java.util.List fieldDefinitionList;

	Element records;

	java.util.List recordList;

	JTable dataTable = new JTable();

	JTable rowNameTable;

	ElementTableModel myModel;

	TableSort dataSorter;

	TableSort rowSort;

	MyMenu customItems;

	MyMenu popupItems;

	MyMenu menus[];

	BorderLayout borderLayout1 = new BorderLayout();

	public TableView() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getViewName() {
		return "Table View";
	}

	public Element getRecord(int recNumber) {
		Element records = getElement().getChild("records", ns);
		java.util.List recs = records.getChildren("record", ns);
		Element recNode = (Element)recs.get(recNumber);
		return recNode;
	}

	public Element getField(int recNumber, int fieldNumber) {
		Element record = getRecord(recNumber);
		java.util.List fields = record.getChildren("field", ns);
		Element field = (Element)fields.get(fieldNumber);
		Element fieldElement = (Element)field.getChildren().iterator().next();
		return fieldElement;
	}

	public void updateViewFromElement() {
		definitions = getElement().getChild("definitions", ns);
		if (definitions != null) {
			fieldDefinitions = definitions.getChild("field-definitions", ns);
			fieldDefinitionList = fieldDefinitions.getChildren("field-definition", ns);
		}
		records = getElement().getChild("records", ns);
		recordList = records.getChildren("record", ns);
		myModel = new ElementTableModel(dataTable, getElement());
		dataTable.setModel(myModel);
		dataSorter = new TableSort(myModel);
		rowNameTable = new JTable();
		// The little bit of extra height makes labels fit better.
		dataTable.setRowHeight(dataTable.getRowHeight() + 4);
		rowNameTable.setRowHeight(rowNameTable.getRowHeight() + 4);
		JScrollPane jScrollPane1 = new JScrollPane(dataTable);
		jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		JScrollPane jScrollPane2 = new JScrollPane(rowNameTable);

		Object[][] rowHeaderData = new Object[recordList.size()][1];
		String[] headers = new String[1];
		headers[0] = "No.";
		for (int k = 0; k < rowHeaderData.length; k++) {
			rowHeaderData[k][0] = new Integer(k + 1);
		}
		DefaultTableModel rowHeaderModel = new DefaultTableModel(rowHeaderData, headers) {
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return false;
			}
		};
		rowSort = new TableSort(rowHeaderModel, dataSorter.getSortModel());
		rowNameTable.setModel(rowSort);
		rowNameTable.setSize(100, dataTable.getHeight());
		rowNameTable.setMaximumSize(new Dimension(100, dataTable.getHeight()));
		rowNameTable.setPreferredScrollableViewportSize(rowNameTable.getPreferredSize());
		rowNameTable.setSelectionModel(dataTable.getSelectionModel());
		rowNameTable.getTableHeader().setReorderingAllowed(false);
		dataTable.getTableHeader().setReorderingAllowed(false);
		jScrollPane1.setRowHeaderView(rowNameTable);
		jScrollPane1.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowNameTable.getTableHeader());
		dataTable.setModel(dataSorter);
		dataTable.setColumnSelectionAllowed(true);
		this.add(jScrollPane1, BorderLayout.CENTER);
	}

	public boolean canShowTag(String tag) throws XenaException {
		return tag.equals(PluginManager.singleton().getTypeManager().lookupXenaFileType(XenaDatasetFileType.class).getTag());
	}

	public void initListeners() {
		dataTable.getTableHeader().addMouseListener(new TableHeaderPopupListener(dataTable, dataSorter));
		rowNameTable.getTableHeader().addMouseListener(new TableRowHeaderPopupListener(rowNameTable, rowSort));
		dataTable.addMouseListener(new TablePopupListener(dataTable));
		rowNameTable.addMouseListener(new RowHeadingPopupListener(rowNameTable));
	}

	public static int numFields(java.util.List records, Namespace ns) {
		int tableWidth;
		Iterator it = records.iterator();
		for (tableWidth = 0; it.hasNext(); ) {
			Element rec = (Element)it.next();
			java.util.List fields = rec.getChildren("field", ns);
			Element last = (Element)fields.get(fields.size() - 1);
			int wid = Integer.parseInt(last.getAttributeValue("idref", ns).substring(1));
			if (tableWidth < wid) {
				tableWidth = wid;
			}
		}
		return tableWidth;
	}

	Element findField(int col) {
		if (fieldDefinitionList != null) {
			Iterator it = fieldDefinitionList.iterator();
			while (it.hasNext()) {
				Element field = (Element)it.next();
				String id = field.getAttributeValue("id", ns);
				if (id.equals("f" + (col + 1))) {
					return field;
				}
			}
		}
		return null;
	}

	XenaFileType getColumnType(int col) {
		Element field = findField(col);
		if (field != null) {
			String typeName = field.getAttributeValue("type", ns);
			if (typeName != null) {
				XenaFileType type = null;
				try {
					type = (XenaFileType)PluginManager.singleton().getTypeManager().lookupXenaTag(typeName);
				} catch (XenaException x) {
					MainFrame.singleton().showError(x);
				}
				return type;
			}
		}
		return null;
	}

	private void jbInit() throws Exception {
		this.setLayout(borderLayout1);
	}

	protected java.util.List getSortList(XenaFileType type) {
		java.util.List sortList = new ArrayList();
		if (type != null) {
			sortList.addAll(type.getSortTypes());
		}
		sortList.add(new XenaFileType.SortType() {
			public String getName() {
				return "ASCII";
			}

			public int comparison(Element e1, Element e2) {
				return e1.getText().compareTo(e2.getText());
			}
		});
		return sortList;
	}

	public class ElementRenderer implements TableCellRenderer {
		ArrayList views;

		ElementRenderer(int size) {
			views = new ArrayList(size);
			for (int i = 0; i < size; i++) {
				views.add(null);
			}
		}

		public Component getTableCellRendererComponent(JTable table,
													   Object value,
													   boolean isSelected,
													   boolean hasFocus,
													   int row,
													   int column) {
			XenaView rtn = null;
			if (value instanceof Element) {
				try {
					Element el = (Element)value;
					try {
						rtn = (XenaView)views.get(column);
					} catch (IndexOutOfBoundsException ex) {
						// Nothing
					}
					if (rtn == null) {
						rtn = viewManager.getDefaultView(el.getQualifiedName(), XenaView.THUMBNAIL_VIEW, getLevel() + 1);
						views.set(column, rtn);
					}
//XXX					rtn.setElemuent(el);

					try {
						JdomUtil.writeDocument(rtn.getContentHandler(), el);
						rtn.parse();
					} catch (JDOMException x) {
						throw new XenaException(x);
					} catch (SAXException x) {
						throw new XenaException(x);
					} catch (IOException x) {
						throw new XenaException(x);
					}

				} catch (XenaException ex) {
					ex.printStackTrace();
				}
			}
			return rtn;
		}
	}

	public class ElementTableModel implements TableModel {
		Element dataset;

		Element definitions;

		Element fieldDefinitions;

		java.util.List fieldDefinitionList;

		Element records;

		java.util.List recordList;

		JTable table;

		ElementRenderer renderer;

		int columnCount;

		public ElementTableModel(JTable table, Element dataset) {
			this.table = table;
			this.dataset = dataset;
			definitions = dataset.getChild("definitions", ns);
			if (definitions != null) {
				fieldDefinitions = definitions.getChild("field-definitions", ns);
				fieldDefinitionList = fieldDefinitions.getChildren("field-definition", ns);
			}
			records = dataset.getChild("records", ns);
			recordList = records.getChildren("record", ns);
			columnCount = numFields(recordList, ns);
			ElementRenderer renderer = new ElementRenderer(getColumnCount());
			table.setDefaultRenderer(Element.class, renderer);
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

		}

		public int getRowCount() {
			return recordList.size();
		}

		public int getColumnCount() {
			return columnCount;
		}

		public String getColumnName(int columnIndex) {
			Element field = findField(columnIndex);
			if (field != null) {
				Element fname = field.getChild("field-name", ns);
				if (fname != null) {
					return fname.getText();
				}
			}
			return " ";
		}

		public Class getColumnClass(int columnIndex) {
			return Element.class;
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			Element record = (Element)recordList.get(rowIndex);
			java.util.List children = record.getChildren("field", ns);
			Iterator it = children.iterator();
			while (it.hasNext()) {
				Element field = (Element)it.next();
				String idref = field.getAttributeValue("idref", ns);
				int num = Integer.parseInt(idref.substring(1));
				if (num == (columnIndex + 1)) {
					return (Element)field.getChildren().get(0);
				}
			}
			return "";
		}

		public void addTableModelListener(TableModelListener l) {

		}

		public void removeTableModelListener(TableModelListener l) {
		}
	}

	class MyMenu extends XenaMenu {
		public JMenuItem sort;

		public JMenu booleanSort;

		public JMenu alphaSort;

		public JMenuItem alphaDescendingSort;

		public JMenuItem booleanDescendingSort;

		public Point mousePoint;

		public JMenuItem alphaAscendingSort;

		public JMenuItem booleanAscendingSort;

		public JMenuItem propertyView;

		public JMenuItem formView;

		public JMenuItem copySelection;

		TableView view;

		MyMenu(TableView view) {
			this.view = view;
			sort = new JMenuItem("Sort Table");
		}

		public void sync() {
		}

		public void makeMenu(Container component) {
			component.add(sort);
		}

		public void initListeners() {
			sort.addActionListener(
				new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					XenaMenu.syncAll(menus);
				}
			});

		}
	}

	class TableHeaderPopup extends JPopupMenu {
		TableHeaderPopup(final JTable table, final TableSort sorter, final int col) {
			JMenu sortMenu = new JMenu("Sort Table");

			add(sortMenu);
			add(new JSeparator());
			java.util.List sortList = null;
			if (table == rowNameTable) {
				sortList = new ArrayList();
				sortList.add(new XenaFileType.SortType() {
					public String getName() {
						return "Numeric";
					}

					public int compare(Object o1, Object o2) {
						return ((Integer)o1).compareTo((Integer)o2);
					}

					public int comparison(Element e1, Element e2) {
						throw new RuntimeException("Can't happen");
					}

				});
			} else if (table == dataTable) {
				XenaFileType type = getColumnType(col);
				sortList = getSortList(type);
			}
			Iterator sli = sortList.iterator();

			while (sli.hasNext()) {
				final Comparator sType = (Comparator)sli.next();
				JMenu sTypeMenu = new JMenu(sType.toString());
				sortMenu.add(sTypeMenu);
				final TableColumnModel columnModel = table.getColumnModel();
				final int viewColumn = columnModel.getColumnIndexAtX(col);
				final int column = table.convertColumnIndexToModel(viewColumn);
				sTypeMenu.add(new JMenuItem("Ascending")).addActionListener(
					new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						sorter.sortByColumn(col, false, sType);
					}
				});
				sTypeMenu.add(new JMenuItem("Descending")).addActionListener(
					new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						sorter.sortByColumn(col, true, sType);
					}
				});
			}
		}
	}

	class RowPopup extends JPopupMenu {
		RowPopup(final int row, final Element record) {
			JMenuItem sortMenuItem = new JMenuItem("Form View");
			sortMenuItem.addActionListener(
				new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					InternalFrame ifr = getInternalFrame();
					try {
						InternalFrame nifr = MainFrame.singleton().showXena(getTmpFile().getFile(), null);
						((FormView)nifr.getView()).changeToRecord(row);
					} catch (XenaException x) {
						MainFrame.singleton().showError(x);
					}
				}
			});
			add(sortMenuItem);
		}
	}

	class TablePopupListener extends MouseAdapter {
		private JTable table;

		TablePopupListener(JTable table) {
			this.table = table;
		}

		public void mouseClicked(MouseEvent evt) {
			try {
				int row = dataSorter.getRowTranslation(dataTable.rowAtPoint(evt.getPoint()));
				int col = dataTable.columnAtPoint(evt.getPoint());

				if (SwingUtilities.isLeftMouseButton(evt)) {
					try {
						Element element = getField(row, col);
						XenaView view = viewManager.getDefaultView(element.getQualifiedName(), XenaView.REGULAR_VIEW,
																			   getLevel() + 1);
						JdomUtil.writeDocument(view.getContentHandler(), element);
						view.parse();
						MainFrame.singleton().newFrame(getInternalFrame().savedFile,
																Integer.toString(row) + "x" + Integer.toString(col), view, null,
																"Datacell: " + col + ", " + row);
					} catch (XenaException x) {
						MainFrame.singleton().showError(x);
					}
				}
			} catch (SAXException x) {
				MainFrame.singleton().showError(x);
			} catch (JDOMException x) {
				MainFrame.singleton().showError(x);
			} catch (IOException x) {
				MainFrame.singleton().showError(x);
			}
		}
	}

	class RowHeadingPopupListener extends MouseAdapter {

		private JTable table;

		public RowHeadingPopupListener(JTable table) {
			this.table = table;
		}

		public void mousePressed(MouseEvent evt) {
			int row = dataSorter.getRowTranslation(dataTable.rowAtPoint(evt.getPoint()));
			int col = dataTable.columnAtPoint(evt.getPoint());

			if (SwingUtilities.isRightMouseButton(evt)) {
				RowPopup popup = new RowPopup(row, getRecord(row));
				popup.show(evt.getComponent(), evt.getX(), evt.getY());
			}
		}
	}

	class TableHeaderPopupListener extends MouseAdapter {
		TableSort sorter;

		private JTable table;

		TableHeaderPopupListener(JTable table, TableSort sorter) {
			this.table = table;
			this.sorter = sorter;
		}

		public void mouseClicked(MouseEvent evt) {
			int col = table.columnAtPoint(evt.getPoint());
			// Default Sorting Algorithm on Mouse-1
			if (SwingUtilities.isLeftMouseButton(evt)) {
				XenaFileType type = getColumnType(col);
				java.util.List sortList = getSortList(type);
				if (0 < sortList.size()) {
					Comparator comp = (Comparator)sortList.get(0);
					sorter.sortByColumn(col, false, comp);
				}
			} else if (SwingUtilities.isRightMouseButton(evt)) {
				TableHeaderPopup popup = new TableHeaderPopup(table, dataSorter, col);
				popup.show(evt.getComponent(), evt.getX(), evt.getY());
			}
		}
	}

	class TableRowHeaderPopupListener extends MouseAdapter {
		TableSort sorter;

		private JTable table;

		TableRowHeaderPopupListener(JTable table, TableSort sorter) {
			this.table = table;
			this.sorter = sorter;
		}

		public void mouseClicked(MouseEvent evt) {
			int col = table.columnAtPoint(evt.getPoint());
			// Default Sorting Algorithm on Mouse-1
			if (SwingUtilities.isLeftMouseButton(evt)) {
				sorter.sortByColumn(col, false);
			} else if (SwingUtilities.isRightMouseButton(evt)) {
				TableHeaderPopup popup = new TableHeaderPopup(table, rowSort, col);
				popup.show(evt.getComponent(), evt.getX(), evt.getY());
			}
		}
	}
}
