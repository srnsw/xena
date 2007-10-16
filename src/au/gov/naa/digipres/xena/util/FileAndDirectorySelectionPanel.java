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
 * Created on 23/02/2007 justinw5
 * 
 */
package au.gov.naa.digipres.xena.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileFilter;

import au.gov.naa.digipres.xena.litegui.LiteMainFrame;
import au.gov.naa.digipres.xena.litegui.NormalisationItemsListModel;
import au.gov.naa.digipres.xena.litegui.NormalisationItemsListRenderer;

public class FileAndDirectorySelectionPanel extends JPanel {
	private static final String LAST_DIR_VISITED_KEY = "dir/lastvisited";

	private NormalisationItemsListModel itemListModel;
	private JList itemList;
	private Preferences prefs;
	private FileFilter filter;

	/**
	 * This constructor will use a null FileFilter.
	 *
	 */
	public FileAndDirectorySelectionPanel() {
		this(null);
	}

	public FileAndDirectorySelectionPanel(FileFilter filter) {
		this.filter = filter;
		prefs = Preferences.userNodeForPackage(LiteMainFrame.class);
		initGUI();
	}

	private void initGUI() {
		this.setLayout(new GridBagLayout());

		itemListModel = new NormalisationItemsListModel();
		itemList = new JList(itemListModel);
		itemList.setCellRenderer(new NormalisationItemsListRenderer());
		itemList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane itemListSP = new JScrollPane(itemList);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(3, 1, 10, 10));

		Font buttonFont = new JButton().getFont().deriveFont(13.0f);
		JButton addFilesButton = new JButton("Add Files");
		addFilesButton.setFont(buttonFont);
		JButton addDirButton = new JButton("Add Directory");
		addDirButton.setFont(buttonFont);
		JButton removeButton = new JButton("Remove");
		removeButton.setFont(buttonFont);

		buttonPanel.add(addFilesButton);
		buttonPanel.add(addDirButton);
		buttonPanel.add(removeButton);

		addToGridBag(this, itemListSP, 0, 0, GridBagConstraints.RELATIVE, GridBagConstraints.REMAINDER, 1.0, 1.0, GridBagConstraints.NORTHWEST,
		             GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0);

		addToGridBag(this, buttonPanel, 1, 0, GridBagConstraints.REMAINDER, GridBagConstraints.REMAINDER, 0.0, 0.0, GridBagConstraints.NORTH,
		             GridBagConstraints.NONE, new Insets(5, 5, 5, 8), 0, 0);

		addFilesButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				doAddItems(true);
			}

		});

		addDirButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				doAddItems(false);
			}

		});

		removeButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				doRemoveItems();
			}

		});
	}

	/**
	 * Add items to the Normalisation Items List. If useFileMode is true,
	 * then the file chooser is set to FILES_ONLY, otherwise the file chooser
	 * is set to DIRECTORIES_ONLY.
	 * 
	 * @param useFileMode True if adding files, false if adding directories
	 */
	private void doAddItems(boolean useFileMode) {
		/*
		 * Initial directory is last visited directory. If this has not been set, then the Xena Source Directory is
		 * used. If this is not set, then the default (root) directory is used.
		 */
		JFileChooser fileChooser = new JFileChooser(prefs.get(LAST_DIR_VISITED_KEY, ""));

		// Set selection mode of file chooser
		if (useFileMode) {
			if (filter != null) {
				fileChooser.setFileFilter(filter);
			}
			fileChooser.setMultiSelectionEnabled(true);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		} else {
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}

		int retVal = fileChooser.showOpenDialog(this);

		// We have returned from the file chooser
		if (retVal == JFileChooser.APPROVE_OPTION) {
			if (useFileMode) {
				File[] selectedFiles = fileChooser.getSelectedFiles();
				for (File file : selectedFiles) {
					itemListModel.addElement(file);
				}
			} else {
				itemListModel.addElement(fileChooser.getSelectedFile());
			}

			prefs.put(LAST_DIR_VISITED_KEY, fileChooser.getCurrentDirectory().getPath());
		}
	}

	/**
	 * Remove an item or items from the Normalise Items List
	 *
	 */
	private void doRemoveItems() {
		int[] selectedIndices = itemList.getSelectedIndices();

		for (int i = selectedIndices.length - 1; i >= 0; i--) {
			itemListModel.remove(selectedIndices[i]);
		}
	}

	/**
	 * Convenience method for adding a component to a container
	 * which is using a GridBagLayout
	 * 
	 * @param container
	 * @param component
	 * @param gridx
	 * @param gridy
	 * @param gridwidth
	 * @param gridheight
	 * @param weightx
	 * @param weighty
	 * @param anchor
	 * @param fill
	 * @param insets
	 * @param ipadx
	 * @param ipady
	 */
	private void addToGridBag(Container container, Component component, int gridx, int gridy, int gridwidth, int gridheight, double weightx,
	                          double weighty, int anchor, int fill, Insets insets, int ipadx, int ipady) {
		GridBagConstraints gbc = new GridBagConstraints(gridx, gridy, gridwidth, gridheight, weightx, weighty, anchor, fill, insets, ipadx, ipady);
		container.add(component, gbc);
	}

	public List<File> getAllItems() {
		return itemListModel.getNormalisationItems();
	}

	public void clear() {
		itemListModel.removeAllElements();
	}

}
