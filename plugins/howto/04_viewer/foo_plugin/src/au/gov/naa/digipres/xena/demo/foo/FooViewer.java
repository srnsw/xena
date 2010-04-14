/*
 * Created on 5/03/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.demo.foo;

import java.awt.Dimension;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.kernel.view.XenaView;

public class FooViewer extends XenaView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTable fooPartTable;
	private FooPartTableModel tableModel;
	private JScrollPane scrollPane;

	public FooViewer() {
		super();

		initComponents();
		initLayout();

	}

	private void initComponents() {
		tableModel = new FooPartTableModel();
		fooPartTable = new JTable(tableModel);
		scrollPane = new JScrollPane(fooPartTable);
		fooPartTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
	}

	private void initLayout() {
		add(scrollPane);
	}

	@Override
	public String getViewName() {
		return "Foo view";
	}

	@Override
	public boolean canShowTag(String tag) {
		return FooNormaliser.FOO_OPENING_ELEMENT_QUALIFIED_NAME.equals(tag);
	}

	@Override
	public ContentHandler getContentHandler() {
		return new XMLFilterImpl() {
			private StringBuffer fooContent;

			@Override
			public void startElement(String uri, String localName, String qName, Attributes atts) {
				if (qName.equals(FooNormaliser.FOO_PART_ELEMENT_QUALIFIED_NAME)) {
					fooContent = new StringBuffer();
				}
			}

			@Override
			public void characters(char[] ch, int start, int length) {
				fooContent.append(ch, start, length);
			}

			@Override
			public void endElement(String uri, String localName, String qName) {
				if (qName.equals(FooNormaliser.FOO_PART_ELEMENT_QUALIFIED_NAME)) {
					tableModel.addFooPart(new String(fooContent));
				}
			}
		};
	}

	private static class FooPartTableModel extends AbstractTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private static int PART = 0;
		private static int DATA = 1;

		private static String[] columnNames = {"Part", "Data"};
		private static Class<?>[] columnTypes = {Integer.class, String.class};
		private List<Object[]> tableEntries = new Vector<Object[]>();

		public void setEntries(List<String> fooParts) {
			tableEntries.clear();
			if (fooParts != null) {
				Iterator<String> it = fooParts.iterator();
				int i = 1;
				while (it.hasNext()) {
					Object[] result = new Object[getColumnCount()];
					result[PART] = new Integer(i++);
					result[DATA] = it.next();
					tableEntries.add(result);
				}
			}
			fireTableDataChanged();
		}

		public void addFooPart(String newPart) {
			String partNumber = (new Integer(tableEntries.size() + 1)).toString();
			Object[] newEntry = {partNumber, newPart};
			tableEntries.add(newEntry);
			fireTableDataChanged();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		public int getRowCount() {
			return tableEntries.size();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		public Object getValueAt(int row, int col) {
			Object[] dataObject = tableEntries.get(row);
			return dataObject[col];
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		public int getColumnCount() {
			return columnNames.length;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
		 */
		@Override
		public Class<?> getColumnClass(int arg0) {
			return columnTypes[arg0];
		}
	}

}
