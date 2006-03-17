/*
 * Created on 2/12/2005
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.litegui;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeSet;

import javax.swing.DefaultListModel;

/**
 * Simple extension of DefaultListModel, which is used to return an
 * ArrayList of the selected Files (ie files and directories). 
 * More functionality may be required here in the future.
 * @author justinw5
 * created 12/12/2005
 * xena
 * Short desc of class:
 */
public class NormalisationItemsListModel extends DefaultListModel
{
	private TreeSet<File> data;
	
	public NormalisationItemsListModel()
	{
		super();
		data = new TreeSet<File>();
	}
	
	public ArrayList<File> getNormalisationItems()
	{
		return new ArrayList<File>(data);
	}

	public int getSize()
	{
		// TODO Auto-generated method stub
		return data.size();
	}

	public Object getElementAt(int index)
	{
		return getNormalisationItems().get(index);
	}

	public void removeAllElements()
	{
		int index = data.size();
		data.clear();
		if (index > 0)
		{
			fireIntervalRemoved(this, 0, index);
		}
	}

	public void addElement(File file)
	{
		int index = data.size();
		data.add(file);
		fireIntervalAdded(this, index, index);
	}

	public Object remove(int i)
	{
		Object obj = getElementAt(i);
		data.remove(obj);
		fireIntervalRemoved(this, i, i);
		return obj;
		
	}
	

}
