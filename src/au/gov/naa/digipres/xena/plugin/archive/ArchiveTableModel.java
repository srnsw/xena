/*
 * Created on 14/03/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.archive;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class ArchiveTableModel extends AbstractTableModel
{
	private static final String[] COLUMN_NAMES = 
		{"Path", "Original Size", "Original Date Created", "Output Filename"};
	private static final Class[] COLUMN_CLASSES = 
		{String.class, Long.class, Date.class, String.class};
	
	private List<ArchiveEntry> entries;

	public ArchiveTableModel()
	{
		super();
		entries = new ArrayList<ArchiveEntry>();
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.DefaultTableModel#getRowCount()
	 */
	public int getRowCount()
	{
		return entries.size();
	}	

	/* (non-Javadoc)
	 * @see javax.swing.table.DefaultTableModel#getColumnCount()
	 */
	public int getColumnCount()
	{
		return COLUMN_NAMES.length;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.DefaultTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column)
	{
		return COLUMN_NAMES[column];
	}
	
	

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		return COLUMN_CLASSES[columnIndex];
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int column)
	{
		ArchiveEntry entryInfo = entries.get(row);
		Object retVal = null;
		switch(column)
		{
		case 0:
			retVal = entryInfo.getName();
			break;
		case 1:
			retVal = entryInfo.getOriginalSize();
			break;
		case 2:
			retVal = entryInfo.getOriginalFileDate();
			break;
		case 3:
			retVal = entryInfo.getFilename();
			break;
		}
		return retVal;
	}
	
	public void addArchiveEntry(ArchiveEntry msgInfo)
	{
		entries.add(msgInfo);
		fireTableDataChanged();
	}
	
	public ArchiveEntry getArchiveEntry(int row)
	{
		return entries.get(row);
	}
	
	public String getSelectedFilename(int row)
	{
		return entries.get(row).getFilename();
	}


	
}
