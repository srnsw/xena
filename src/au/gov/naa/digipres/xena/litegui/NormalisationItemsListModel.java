/*
 * Created on 2/12/2005
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.litegui;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;

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

	public NormalisationItemsListModel()
	{
		super();
	}
	
	public ArrayList<File> getNormalisationItems()
	{
		ArrayList<File> itemList = new ArrayList<File>();
		Enumeration e = this.elements();
		while (e.hasMoreElements())
		{
			itemList.add((File)e.nextElement());
		}
		return itemList;
	}

}
