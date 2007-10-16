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
 * Created on 6/03/2006 justinw5
 * 
 */
package au.gov.naa.digipres.xena.kernel.properties;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Listener class to listen for selections in a Properties Menu, and display the 
 * properties dialog for the selected plugin.
 * 
 * created 10/04/2006
 * xena
 * Short desc of class:
 */
public class PropertiesMenuListener implements ActionListener {
	private PluginProperties pluginProp;
	private Window parent;

	public PropertiesMenuListener(Window parent, PluginProperties pluginProp) {
		this.pluginProp = pluginProp;
		this.parent = parent;
	}

	public void actionPerformed(ActionEvent e) {
		PropertiesDialog dialog = pluginProp.getPropertiesDialog(parent);
		dialog.pack();
		dialog.setLocation(parent.getX() + 50, parent.getY() + 50);
		dialog.setVisible(true);

		// Have finished with dialog
		dialog.dispose();
	}
}
