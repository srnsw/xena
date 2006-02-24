package au.gov.naa.digipres.xena.plugin.dataset;

import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 *  A sorter for <code>TableModel</code>s. The sorter has a model (conforming to
 *  <code>TableModel</code>) and itself implements <code>TableModel</code>.
 *  <code>TableSorter</code> does not store or copy the data in the <code>TableModel</code>
 *  ; instead it maintains an array of integers which it keeps the same size as
 *  the number of rows in its model. When the model changes it notifies the
 *  sorter that something has changed eg. "rowsAdded" so that its internal array
 *  of integers can be reoallocated. As requests are made of the sorter (like
 *  <code>getValueAt(row, col)</code>) it redirects them to its model via the
 *  mapping array. That way the <code>TableSorter</code> appears to hold another
 *  copy of the table with the rows in a different order. The sorting algorithm
 *  used is stable which means that it does not move around rows when its
 *  comparison function returns 0 to denote that they are equivalent.
 *
 * @author     Philip Milne
 * @author     Mark Lindner
 * @created    1 December 2002
 */

public class TableSort implements TableModel {
	Model sortModel;

	TableModel model;

	List changeEvents = new ArrayList();

	private List sortingColumns = new ArrayList();

	private List sortingComparators = new ArrayList();

	private List sortingDescending = new ArrayList();

	private JTable tableView;

	public TableSort(TableModel model) {
		sortModel = new Model();
		sortModel.dependancies.add(this);
		this.model = model;
	}

	public TableSort(TableModel model, Model sortModel) {
		this.sortModel = sortModel;
		sortModel.dependancies.add(this);
		this.model = model;
	}

	public void setValueAt(Object value, int row, int col) {
		model.setValueAt(value, sortModel.indexes[row].intValue(), col);
	}

	public Model getSortModel() {
		return sortModel;
	}

	/**
	 *  Return the index of the given row in the <i>unsorted</i> model that this
	 *  model wraps. This method is useful for determining which row in the actual
	 *  model is mapped to the currently selected row in the sorted <code>TableMap</code>
	 *  .
	 *
	 * @param  row  Description of Parameter
	 * @return      The rowTranslation value
	 * @see         #getReverseRowTranslation
	 */
	public int getRowTranslation(int row) {
		if (sortModel.indexes == null) {
			return row;
		} else {
			return (sortModel.indexes[row].intValue());
		}
	}

	/**
	 *  Return the visible index of the given row in the <i>unsorted</i> model.
	 *  This method performs the reverse of <code>getRowTranslation()</code>
	 *
	 * @param  row  Description of Parameter
	 * @return      The reverseRowTranslation value
	 * @see         #getRowTranslation
	 */
	public int getReverseRowTranslation(int row) {
		for (int i = 0; i < sortModel.indexes.length; i++) {
			if (sortModel.indexes[i].intValue() == row) {
				return (i);
			}
		}

		return ( -1);
	}

	/**
	 *  Get the value at the given row and column of the unsorted table.
	 *
	 * @param  row  The row.
	 * @param  col  The column.
	 * @return      The object at the given position in the <i>unsorted</i> model.
	 */

	public Object getValueAt(int row, int col) {
		// The mapping only affects the contents of the data rows.
		// Pass all requests to these rows through the mapping array: "indexes".
		int index = (sortModel.indexes == null ? row : sortModel.indexes[row].intValue());
		return model.getValueAt(index, col);
	}

	public int getRowCount() {
		return model.getRowCount();
	}

	public int getColumnCount() {
		return model.getColumnCount();
	}

	public String getColumnName(int columnIndex) {
		return model.getColumnName(columnIndex);
	}

	public Class getColumnClass(int columnIndex) {
		return model.getColumnClass(columnIndex);
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return model.isCellEditable(getRowTranslation(rowIndex), columnIndex);
	}

	/**
	 *  Sort a column in the table in descending order.
	 *
	 * @param  column  The index of the column to sort.
	 */
	public void sortByColumn(int column) {
		sortByColumn(column, true, null);
	}

	/**
	 *  Sort a column in the table.
	 *
	 * @param  column      The index of the column to sort.
	 * @param  descending  If <code>true</code>, sorts in descending order;
	 *      otherwise, sorts in descending order.
	 */
	public void sortByColumn(int column, boolean descending) {
		sortByColumn(column, descending, null);
	}

	public void sortByColumn(int column, boolean descending, java.util.Comparator comp) {
		sortingColumns.clear();
		sortingColumns.add(new Integer(column));
		sortingComparators.clear();
		sortingComparators.add(comp);
		sortingDescending.clear();
		sortingDescending.add(new Boolean(descending));
		sort(this);
		updateDependancies();
		fireTableChanged();
	}

	/**
	 *  Add a mouse listener to the <code>JTable</code> to trigger a table sort
	 *  when a column heading is clicked. A shift click causes the column to be
	 *  sorted in descending order, whereas a simple click causes the column to be
	 *  sorted in descending order.
	 *
	 * @param  table  The <code>JTable</code> to listen for events on.
	 */
	public void registerTableHeaderListener(JTable table) {
		final TableSort sorter = this;
		tableView = table;

		tableView.setColumnSelectionAllowed(false);
		MouseAdapter listMouseListener =
			new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				// If the table is disabled, we will ignore the mouse event,
				// effectively preventing sorting.

				if (!tableView.isEnabled() || tableView.isEditing()) {
					return;
				}

				// Save a list of highlighted rows. I don't call getSelectedRows()
				// directly since that would force me to have *two* working arrays.

				int hrows[] = new int[tableView.getSelectedRowCount()];
				int nr = tableView.getRowCount();
				for (int c = 0, i = 0; i < nr; i++) {
					if (tableView.isRowSelected(i)) {
						hrows[c++] = getRowTranslation(i);
					}
				}

				// figure out which column was selected, & sort it

				TableColumnModel columnModel = tableView.getColumnModel();
				int viewColumn = columnModel.getColumnIndexAtX(e.getX());
				int column = tableView.convertColumnIndexToModel(viewColumn);
				if (e.getClickCount() == 1 && column != -1) {
					int shiftPressed = e.getModifiers() & InputEvent.SHIFT_MASK;
					boolean descending = (shiftPressed == 0);
					sorter.sortByColumn(column, descending, null);
				}

				// now rehighlight the rows in their new positions

				tableView.clearSelection();
				for (int i = 0; i < hrows.length; i++) {
					int r = getReverseRowTranslation(hrows[i]);
					tableView.addRowSelectionInterval(r, r);
				}

			}
		};
		JTableHeader th = tableView.getTableHeader();
		th.addMouseListener(listMouseListener);
	}

	public void addTableModelListener(TableModelListener l) {
		model.addTableModelListener(l);
		changeEvents.add(l);
	}

	public void removeTableModelListener(TableModelListener l) {
		model.removeTableModelListener(l);
	}

	void fireTableChanged() {
		Iterator it = changeEvents.iterator();
		while (it.hasNext()) {
			TableModelListener l = (TableModelListener)it.next();
			l.tableChanged(new TableModelEvent(this));
		}
	}

	/**
	 *  Handle <i>table changed</i> events.
	 */
	void updateDependancies() {
		Iterator it = sortModel.dependancies.iterator();
		TableModelEvent event = new TableModelEvent(this);
		while (it.hasNext()) {
			TableSort sort = (TableSort)it.next();
			if (sort != this) {
				sort.fireTableChanged();
			}
		}
	}

	private void allocateIndexes() {
		int rowCount = model.getRowCount();

		// Set up a new array of indexes with the right number of elements
		// for the new data model.

		sortModel.indexes = new Integer[rowCount];

		// Initialise with the identity mapping.

		for (int row = 0; row < rowCount; row++) {
			sortModel.indexes[row] = new Integer(row);
		}
	}

	private void sort(Object sender) {
		if (sortModel.indexes == null) {
			allocateIndexes();
		}
		Arrays.sort(sortModel.indexes, new Comp(model, sortingComparators, sortingColumns, sortingDescending));
	}

	public class Model {
		private Integer[] indexes;

		private List dependancies = new ArrayList();
	}

	public class Comp implements java.util.Comparator {
		TableModel model;

		List comps;

		List sortingColumns;

		List sortingDescending;

		Comp(TableModel model, List comps, List sortingColumns, List sortingDescending) {
			this.model = model;
			this.comps = comps;
			this.sortingColumns = sortingColumns;
			this.sortingDescending = sortingDescending;
		}

		public int compare(Object o1, Object o2) {
			int rtn = 0;
			Iterator it = sortingColumns.iterator();
			Iterator it2 = comps.iterator();
			Iterator it3 = sortingDescending.iterator();
			while (it.hasNext()) {
				int column = ((Integer)it.next()).intValue();
				java.util.Comparator comp = (java.util.Comparator)it2.next();
				boolean descending = ((Boolean)it3.next()).booleanValue();
				if (comp == null) {
					rtn = ((java.lang.Comparable)model.getValueAt(((Integer)o1).intValue(),
						column)).compareTo(model.getValueAt(((Integer)o2).intValue(), column));
				} else {
					rtn = comp.compare(model.getValueAt(((Integer)o1).intValue(), column),
									   model.getValueAt(((Integer)o2).intValue(), column));
				}
				if (descending) {
					rtn = -rtn;
				}
				if (rtn != 0) {
					break;
				}
			}
			return rtn;
		}
	}
}
