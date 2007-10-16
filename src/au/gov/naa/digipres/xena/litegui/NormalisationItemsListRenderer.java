/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Xena; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * @author Andrew Keeling
 * @author Dan Spasojevic
 * @author Justin Waddell
 */

/*
 * Created on 5/12/2005 justinw5
 * 
 */
package au.gov.naa.digipres.xena.litegui;

import java.awt.Component;
import java.io.File;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import au.gov.naa.digipres.xena.kernel.IconFactory;

/**
 * Extension of DefaultListCellRenderer used to display an icon before
 * the name of the file or directory - different icons for files and 
 * directories to distinguish between them.
 * 
 * created 12/12/2005
 * xena
 * Short desc of class:
 */
public class NormalisationItemsListRenderer extends DefaultListCellRenderer {

	public NormalisationItemsListRenderer() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int,
	 * boolean, boolean)
	 */
	@Override
	/**
	 * Return a label as returned by DefaultListCellRenderer, but add a
	 * file or directory icon to the label.
	 */
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		File file = (File) value;
		JLabel retLabel = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (file.isDirectory()) {
			retLabel.setIcon(IconFactory.getIconByName("images/icons/folder_yellow.png"));
		} else {
			retLabel.setIcon(IconFactory.getIconByName("images/icons/file_white.png"));
		}

		return retLabel;
	}

}
