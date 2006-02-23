/*
 * Created on 5/12/2005
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.litegui;

import java.awt.Component;
import java.io.File;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.plaf.metal.MetalIconFactory;

/**
 * Extension of DefaultListCellRenderer used to display an icon before
 * the name of the file or directory - different icons for files and 
 * directories to distinguish between them.
 * 
 * @author justinw5
 * created 12/12/2005
 * xena
 * Short desc of class:
 */
public class NormalisationItemsListRenderer extends DefaultListCellRenderer
{

	public NormalisationItemsListRenderer()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	/**
	 * Return a label as returned by DefaultListCellRenderer, but add a
	 * file or directory icon to the label.
	 */
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
	{	
		File file = (File)value;
		JLabel retLabel = (JLabel)super.getListCellRendererComponent(list,
		                                                             value,
		                                                             index,
		                                                             isSelected,
		                                                             cellHasFocus);
		if (file.isDirectory())
		{
			retLabel.setIcon(MetalIconFactory.getTreeFolderIcon());
		}
		else
		{
			retLabel.setIcon(MetalIconFactory.getTreeLeafIcon());
		}		
		
		return retLabel;
	}
	
	

}
