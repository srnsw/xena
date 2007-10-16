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
 * Created on 23/03/2006 justinw5
 * 
 */
package au.gov.naa.digipres.xena.litegui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.javatools.JarPreferences;
import au.gov.naa.digipres.xena.kernel.plugin.PluginManager;
import au.gov.naa.digipres.xena.util.TableSorter;

public class AboutPluginsDialog extends JDialog {
	private static JDialog pluginsDialog = null;

	public static void showPluginsDialog(Frame parent, Xena xena, String title) {

		PluginTableModel tableModel = new PluginTableModel();

		PluginManager pluginManager = xena.getPluginManager();

		List<String> pluginNames = pluginManager.getLoadedPlugins();

		// Sort plugins by name
		Set<JarPreferences> sortedSet = new TreeSet<JarPreferences>();
		for (String name : pluginNames) {
			sortedSet.add(JarPreferences.userRoot().node(name, pluginManager.getClassLoader()));
		}

		// Add plugins to table model, in sorted order
		for (JarPreferences pluginInfo : sortedSet) {
			tableModel.addPluginInfo(pluginInfo);
		}

		TableSorter sorter = new TableSorter(tableModel);
		JTable pluginTable = new JTable(sorter);
		sorter.setTableHeader(pluginTable.getTableHeader());

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pluginsDialog.setVisible(false);
			}
		});
		buttonPanel.add(okButton);

		pluginsDialog = new JDialog(parent, title, true);
		pluginsDialog.add(new JScrollPane(pluginTable), BorderLayout.CENTER);
		pluginsDialog.add(buttonPanel, BorderLayout.SOUTH);
		pluginsDialog.setSize(300, 300);
		pluginsDialog.setLocationRelativeTo(parent);
		pluginsDialog.setVisible(true);

	}

	private static class PluginTableModel extends AbstractTableModel {
		private final String[] COLUMN_NAMES = {"Plugin Name", "Version"};
		private final Class[] COLUMN_CLASSES = {String.class, String.class};

		private List<JarPreferences> pluginList = new ArrayList<JarPreferences>();

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.DefaultTableModel#getRowCount()
		 */
		public int getRowCount() {
			return pluginList.size();
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.DefaultTableModel#getColumnCount()
		 */
		public int getColumnCount() {
			return COLUMN_NAMES.length;
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.DefaultTableModel#getColumnName(int)
		 */
		@Override
		public String getColumnName(int column) {
			return COLUMN_NAMES[column];
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
		 */
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return COLUMN_CLASSES[columnIndex];
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
		 */
		public Object getValueAt(int row, int column) {
			JarPreferences pluginInfo = pluginList.get(row);
			Object retVal = null;
			switch (column) {
			case 0:
				retVal = pluginInfo.name();
				break;
			case 1:
				retVal = pluginInfo.get("version", "");
				break;
			}
			return retVal;

		}

		public void addPluginInfo(JarPreferences pluginInfo) {
			pluginList.add(pluginInfo);
		}

	}

}
