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
 * Created on 29/11/2005 justinw5
 * 
 */
package au.gov.naa.digipres.xena.viewer;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.EtchedBorder;

import au.gov.naa.digipres.xena.core.NormalisedObjectViewFactory;
import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.kernel.FileExistsException;
import au.gov.naa.digipres.xena.kernel.IconFactory;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.view.ViewManager;
import au.gov.naa.digipres.xena.kernel.view.XenaView;

/**
 * Simple frame to display a Normalised Object View, a JPanel
 * which has been returned by NormalisedOjectViewFactory.
 * 
 * The frame's toolbar will contain a select box which will 
 * enable the user to change the type of view, e.g. Package, 
 * raw XML, tree XML etc.
 * 
 * created 29/11/2005
 * xena
 * Short desc of class: frame to display Normalised Object Views
 */
public class NormalisedObjectViewFrame extends JFrame {
	public static final int DEFAULT_WIDTH = 800;
	public static final int DEFAULT_HEIGHT = 600;

	public static final Cursor busyCursor = new Cursor(Cursor.WAIT_CURSOR);
	public static final Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);

	private ViewManager viewManager;
	JComboBox viewTypeCombo;
	DefaultComboBoxModel viewTypeModel = null;
	private XenaView currentDisplayView;
	private File xenaFile;
	NormalisedObjectViewFactory novFactory;
	JPanel xenaViewPanel;

	/**
	 * Create a new NormalisedObjectViewFrame
	 * 
	 * @param xenaView
	 * view to display in the frame
	 * @param xena
	 * Xena interface object
	 * @param xenaFile
	 * original xena File
	 */
	public NormalisedObjectViewFrame(XenaView xenaView, ViewManager viewManager, File xenaFile) {
		super();

		this.xenaFile = xenaFile;
		this.viewManager = viewManager;
		novFactory = new NormalisedObjectViewFactory(viewManager);
		try {
			initFrame(viewManager.isShowExportButton());
			setupTypeComboBox(xenaView);
			displayXenaView(xenaView);
			currentDisplayView = xenaView;
		} catch (XenaException e) {
			handleException(e);
		}
	}

	/**
	 * Create a new NormalisedObjectViewFrame
	 * 
	 * @param xenaView view to display in the frame
	 * @param xena Xena interface object
	 * @param xenaFile original xena File
	 */
	public NormalisedObjectViewFrame(XenaView xenaView, Xena xena, File xenaFile) {
		this(xenaView, xena.getPluginManager().getViewManager(), xenaFile);
	}

	/**
	 * One-time initialisation of frame GUI - menu, toolbar and
	 * event listeners.
	 * 
	 * @throws XenaException
	 */
	private void initFrame(boolean showExportButton) throws XenaException {
		setIconImage(IconFactory.getIconByName("images/xena-splash.png").getImage());

		// Setup toolbar
		JToolBar toolBar = new JToolBar();
		toolBar.setLayout(new FlowLayout(FlowLayout.LEFT));
		JPanel toolBarPanel = new JPanel(new BorderLayout());
		toolBarPanel.setBorder(new EtchedBorder());

		viewTypeCombo = new JComboBox();

		viewTypeCombo.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					// A new view type has been selected
					try {
						displayXenaView(currentDisplayView, (XenaView) viewTypeModel.getSelectedItem());
					} catch (XenaException e1) {
						handleException(e1);
					}
				}
			}

		});

		JButton exportButton = new JButton("Export");
		exportButton.setVisible(showExportButton);
		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportXenaFile();
			}
		});

		toolBar.add(viewTypeCombo);
		toolBar.add(exportButton);
		toolBarPanel.add(toolBar, BorderLayout.NORTH);
		getContentPane().add(toolBarPanel, BorderLayout.NORTH);

		// Panel in which the XenaView will be displayed
		xenaViewPanel = new JPanel(new BorderLayout());
		getContentPane().add(xenaViewPanel, BorderLayout.CENTER);
		this.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

		// Ensure resources are surrendered when window closes
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				doCloseWindow();
			}

		});
	}

	/**
	 * Displays the given XenaView, by adding the view to the display panel.
	 * 
	 * @param concreteView
	 * @throws XenaException
	 */
	private void displayXenaView(XenaView concreteView) throws XenaException {
		xenaViewPanel.removeAll();
		xenaViewPanel.add(concreteView, BorderLayout.CENTER);
		validate();
		setTitle("XenaViewer - " + xenaFile.getName() + " (" + viewTypeModel.getSelectedItem().toString() + ")");
		System.gc();
	}

	/**
	 * Displays the given view using the new view type. A new XenaView,
	 * using the new view type, is retrieved from the 
	 * NormalisedObjectViewFactory.
	 * 
	 * @param concreteView
	 * @param viewType
	 * @throws XenaException
	 */
	private void displayXenaView(XenaView concreteView, XenaView viewType) throws XenaException {

		// Need to clone the template view
		viewType = viewManager.lookup(viewType.getClass(), concreteView.getLevel(), concreteView.getTopTag());

		XenaView displayView = novFactory.getView(xenaFile, viewType);
		displayXenaView(displayView);
		currentDisplayView = displayView;
	}

	/**
	 * Retrieves the list of view types applicable to the given XenaView,
	 * and displays these options in the combox box.
	 * 
	 * @param xenaView
	 * @throws XenaException
	 */
	private void setupTypeComboBox(XenaView xenaView) throws XenaException {
		viewTypeModel = new DefaultComboBoxModel();

		// Get all applicable view types
		List<XenaView> viewTypes = viewManager.lookup(xenaView.getTopTag(), 0);

		// Add options to combo box model
		Iterator<XenaView> iter = viewTypes.iterator();
		while (iter.hasNext()) {
			viewTypeModel.addElement(iter.next());
		}

		viewTypeCombo.setModel(viewTypeModel);
	}

	/**
	 * Surrender this window's resources, and close
	 *
	 */
	private void doCloseWindow() {
		setVisible(false);
		dispose();
		currentDisplayView.doClose();
		currentDisplayView = null;
		System.gc();
	}

	/**
	 * Display error messages
	 * @param xex
	 */
	private void handleException(Exception ex) {
		ex.printStackTrace();
		JOptionPane.showMessageDialog(this, ex.getMessage(), "Xena Viewer", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * 
	 *
	 */
	private void exportXenaFile() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.setDialogTitle("Choose Export Directory");
		int retVal = chooser.showSaveDialog(this);
		if (retVal == JFileChooser.APPROVE_OPTION) {
			Xena xena = viewManager.getPluginManager().getXena();

			try {
				// Display busy cursor
				this.setCursor(busyCursor);

				xena.export(new XenaInputSource(xenaFile), chooser.getSelectedFile());

				// Display default cursor
				this.setCursor(defaultCursor);

				JOptionPane.showMessageDialog(this, "Xena file exported successfully.", "Export Complete", JOptionPane.INFORMATION_MESSAGE);
			} catch (FileExistsException e) {
				// Display default cursor
				this.setCursor(defaultCursor);

				retVal =
				    JOptionPane.showConfirmDialog(this, "A file with the same name already exists in this directory. Do you want to overwrite it?",
				                                  "File Already Exists", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (retVal == JOptionPane.OK_OPTION) {
					try {
						// Display busy cursor
						this.setCursor(busyCursor);

						xena.export(new XenaInputSource(xenaFile), chooser.getCurrentDirectory(), true);

						// Display default cursor
						this.setCursor(defaultCursor);
					} catch (Exception ex) {
						handleException(ex);
					}
				}
			} catch (Exception e) {
				handleException(e);
			} finally {
				// Ensure default cursor is displayed again
				this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}

	}

}
