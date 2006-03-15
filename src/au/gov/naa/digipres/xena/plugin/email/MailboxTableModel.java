/*
 * Created on 14/03/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.email;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class MailboxTableModel extends AbstractTableModel
{
	private static final String[] COLUMN_NAMES = 
		{"From", "To", "Subject", "Date", "Output Filename"};
	private static final Class[] COLUMN_CLASSES = 
		{String.class, String.class, String.class, Date.class, String.class};
	
	private List<MessageInfo> messages;

	public MailboxTableModel()
	{
		super();
		messages = new ArrayList<MessageInfo>();
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.DefaultTableModel#getRowCount()
	 */
	public int getRowCount()
	{
		return messages.size();
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
		MessageInfo msgInfo = messages.get(row);
		Object retVal = null;
		switch(column)
		{
		case 0:
			retVal = msgInfo.getFromAddress();
			break;
		case 1:
			retVal = msgInfo.getToAddress();
			break;
		case 2:
			retVal = msgInfo.getSubject();
			break;
		case 3:
			retVal = msgInfo.getEmailDate();
			break;
		case 4:
			retVal = msgInfo.getOutputFile();
			break;
		}
		return retVal;
	}
	
	public void addMessage(MessageInfo msgInfo)
	{
		messages.add(msgInfo);
		fireTableDataChanged();
	}
	
	public MessageInfo getMessage(int row)
	{
		return messages.get(row);
	}
	
	public String getSelectedFilename(int row)
	{
		return messages.get(row).getOutputFile();
	}


	
}
