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
 * @author Chris Bitmead
 * @author Justin Waddell
 */

/*
 * Created on 2/12/2005 justinw5
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
 * created 12/12/2005
 * xena
 * Short desc of class:
 */
public class NormalisationItemsListModel extends DefaultListModel {
	private TreeSet<File> data;

	public NormalisationItemsListModel() {
		super();
		data = new TreeSet<File>();
	}

	public ArrayList<File> getNormalisationItems() {
		return new ArrayList<File>(data);
	}

	@Override
    public int getSize() {
		// TODO Auto-generated method stub
		return data.size();
	}

	@Override
    public Object getElementAt(int index) {
		return getNormalisationItems().get(index);
	}

	@Override
    public void removeAllElements() {
		int index = data.size();
		data.clear();
		if (index > 0) {
			fireIntervalRemoved(this, 0, index);
		}
	}

	public void addElement(File file) {
		int index = data.size();
		data.add(file);
		fireIntervalAdded(this, index, index);
	}

	@Override
    public Object remove(int i) {
		Object obj = getElementAt(i);
		data.remove(obj);
		fireIntervalRemoved(this, i, i);
		return obj;

	}

}
