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
 * Created on 30/11/2005 justinw5
 * 
 */
package au.gov.naa.digipres.xena.viewer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

/**
 * Simple dialog to set preferences for the Xena Viewer. Currently
 * there are only two preferences that can be set - the Xena plugin
 * directory, and the xena destination directory. Each of these options
 * has an associated entry field, and a browse button which will bring
 * up a file chooser that populates the appropriate entry field with the 
 * selected directory.
 * The entry fields can be pre-populated from the calling window, and
 * thus previously saved preferences can be automatically restored.
 * created 1/12/2005
 * xena
 * Short desc of class:
 */
public class ViewerPreferencesDialog extends JDialog {
	private static final String DIALOG_TITLE = "XenaViewer Preferences";

	private String xenaDestDir;
	private JTextField xenaDestTF;

	private boolean approved = false;

	public ViewerPreferencesDialog(Frame owner) throws HeadlessException {
		super(owner);
		initGUI();
	}

	/**
	 * One-time GUI initialisation
	 */
	private void initGUI() {
		this.setModal(true);
		this.setTitle(DIALOG_TITLE);
		this.setResizable(false);

		JPanel prefsPanel = new JPanel(new BorderLayout());

		prefsPanel.setBorder(new TitledBorder(new EtchedBorder(), DIALOG_TITLE));
		prefsPanel.setLayout(new GridLayout(2, 1));

		JLabel xenaDestLabel = new JLabel("Xena destination directory:");

		xenaDestTF = new JTextField(30);
		JButton xenaDestBrowseButton = new JButton("Browse");

		JPanel xenaDestPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		xenaDestPanel.add(xenaDestLabel);
		xenaDestPanel.add(xenaDestTF);
		xenaDestPanel.add(xenaDestBrowseButton);

		prefsPanel.add(xenaDestPanel);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton okButton = new JButton("OK");
		JButton cancelButton = new JButton("Cancel");
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);

		this.add(prefsPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);

		// Action Listeners
		this.addWindowListener(new WindowAdapter() {

			@Override
            public void windowClosing(WindowEvent e) {
				doCloseDialog();
			}

		});

		okButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				xenaDestDir = xenaDestTF.getText();
				approved = true;
				doCloseDialog();
			}

		});

		cancelButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				doCloseDialog();
			}

		});

		xenaDestBrowseButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String chosenDir = getChosenDir(xenaDestDir);
				if (chosenDir != null) {
					setXenaDestDir(chosenDir);
				}
			}

		});

		this.pack();
	}

	/**
	 * Displays a file chooser, starting at the given directory.
	 * Returns the chosen directory, or null if no choice made.
	 * 
	 * @param currentDir
	 * @return
	 */
	private String getChosenDir(String currentDir) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setCurrentDirectory(new File(currentDir));
		int retVal = fileChooser.showOpenDialog(this);

		// We have returned from the file chooser
		if (retVal == JFileChooser.APPROVE_OPTION) {
			return fileChooser.getSelectedFile().getPath();
		} else {
			return null;
		}
	}

	private void doCloseDialog() {
		this.setVisible(false);
	}

	/**
	 * @return Returns the xenaDestDir.
	 */
	public String getXenaDestDir() {
		return xenaDestDir;
	}

	/**
	 * @param xenaDestDir
	 * The xenaDestDir to set.
	 */
	public void setXenaDestDir(String xenaDestDir) {
		this.xenaDestDir = xenaDestDir;
		this.xenaDestTF.setText(xenaDestDir);
	}

	/**
	 * @return Returns the approved.
	 */
	public boolean isApproved() {
		return approved;
	}

}
