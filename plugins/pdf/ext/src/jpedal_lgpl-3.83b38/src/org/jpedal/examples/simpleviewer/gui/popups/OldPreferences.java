/**
* ===========================================
* Java Pdf Extraction Decoding Access Library
* ===========================================
*
* Project Info:  http://www.jpedal.org
* (C) Copyright 1997-2008, IDRsolutions and Contributors.
*
* 	This file is part of JPedal
*
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA


*
* ---------------
* Preferences.java
* ---------------
*/
package org.jpedal.examples.simpleviewer.gui.popups;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jpedal.examples.simpleviewer.Commands;
import org.jpedal.examples.simpleviewer.gui.GUI;
import org.jpedal.utils.Messages;

/**
 * provides a popup preferences option in SimpleViewer
 *
 */
public class OldPreferences {
	
	//Window Components
	JFrame jf = new JFrame("JPedal PDF Preferences");
	
	JLabel title = new JLabel("Preferences");
	
	JPanel mainPane = new JPanel(new GridBagLayout());
	
	JPanel[] settings = new JPanel[4];
	
	JButton confirm = new JButton("OK");
	
	JButton cancel = new JButton("Cancel");
	
	
	//Settings Fields Components
	
	//DPI viewer value
	JTextField dpi_Input = new JTextField("96");
	String dpiDefaultValue = "96";
	
	//Search window display style
	JComboBox searchStyle = new JComboBox(new String[]{"Ext. Window","Tab Pane","Menu Bar"});
	int searchStyleDefaultValue = 1;
	
	//Show border around page
	JCheckBox border = new JCheckBox();
	int borderDefaultValue = 1;
	
	//perform automatic update check
	JCheckBox update = new JCheckBox();
	boolean updateDefaultValue = true;
	
	JTextField maxMultiViewers = new JTextField("20");
	String maxMultiViewersDefaultValue = "20";
	
	//Set autoScroll when mouse at the edge of page
	JCheckBox autoScroll = new JCheckBox();
	boolean scrollDefaultValue = false;
	
	//Set default page layout
	JComboBox pageLayout = new JComboBox(new String[]{"Single Page","Continuous","Continuous Facing", "Facing"});
	int pageLayoutDefaultValue = 1;
	
	JList settingsList = new JList(new String[]{"Display","Viewer"});
	
	Box setButtons = Box.createHorizontalBox();
	
	
	//Keep track of which settings panel to remove before adding new
	int currentListSelection = 0;
	
	
	/**
	 * createPreferanceWindow(final GUI gui)
	 * Set up all settings fields then call the required methods to build the window
	 * 
	 * @param gui - Used to allow any changed settings to be saved into an external properties file.
	 * 
	 */
	public void createPreferenceWindow(final GUI gui){
		jf = new JFrame(Messages.getMessage("PageLayoutViewMenu.PreferencesWindowTitle"));
		
		/*
		 * Ensure current values have been set into the fields
		 */
		for(int i=0; i!=settings.length;i++){
			settings[i] = new JPanel(new BorderLayout());
		}
		
		settingsList = new JList(new String[]{"Display","Viewer","Updates","MultiViewer"});
		settingsList.setSelectedIndex(currentListSelection);
		settingsList.setSize(50, 100);
		
		dpi_Input = new JTextField(dpiDefaultValue);
		dpi_Input.setPreferredSize(new  Dimension(dpi_Input.getFont().getSize()*4,dpi_Input.getFont().getSize()+10));
		
		maxMultiViewers = new JTextField(maxMultiViewersDefaultValue);
		maxMultiViewers.setPreferredSize(new  Dimension(maxMultiViewers.getFont().getSize()*4,maxMultiViewers.getFont().getSize()+10));
		
//		maxMultiViewers.setMinimumSize(new Dimension(20,20));
		
		searchStyle = new JComboBox(new String[]{Messages.getMessage("PageLayoutViewMenu.WindowSearch"),Messages.getMessage("PageLayoutViewMenu.TabbedSearch"),Messages.getMessage("PageLayoutViewMenu.MenuSearch")});
		pageLayout = new JComboBox(new String[]{Messages.getMessage("PageLayoutViewMenu.SinglePage"),Messages.getMessage("PageLayoutViewMenu.Continuous"),Messages.getMessage("PageLayoutViewMenu.Facing"),Messages.getMessage("PageLayoutViewMenu.ContinousFacing")});
		
		border = new JCheckBox();
		autoScroll = new JCheckBox();
		update = new JCheckBox();
		
		confirm = new JButton("OK");
		cancel = new JButton("Cancel");
		
		title = new JLabel(Messages.getMessage("PageLayoutViewMenu.Preferences"));
		title.setFont(new Font(null,Font.BOLD,14));
		
		setButtons.add(confirm);
		setButtons.add(Box.createHorizontalStrut(30));
		setButtons.add(cancel);
		
		
		/*
		 * Build the Settings panels
		 */
		for(int i=0; i<settingsList.getModel().getSize();i++){
			switch(i){
			case 0 : createDisplaySettings(settings[0]);break;
			case 1 : createViewerSettings(settings[1]);break;
			case 2 : createUpdateSettings(settings[2]);break;
			case 3 : createMultiViewerSettings(settings[3]);break;
			default : break;
			}
		}
		
		
		//Build the preferences window
		mainPane = buildMainPane(mainPane);
		
		jf.getContentPane().setLayout(new BorderLayout());
		jf.getContentPane().add(mainPane,BorderLayout.CENTER);
		jf.setSize(400, 300);
		jf.setResizable(false);
		
		/*
		 * Listeners that are reqired for each setting field
		 */
		confirm.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				dpiDefaultValue = dpi_Input.getText();
				int dpi = Integer.parseInt(dpi_Input.getText());
				int style = searchStyleDefaultValue = searchStyle.getSelectedIndex();
				int pageMode = pageLayoutDefaultValue = (pageLayout.getSelectedIndex()+1);
				
				int borderStyle = borderDefaultValue = 0;
				if(border.isSelected()){
					borderStyle = borderDefaultValue = 1;
				}
				
				updateDefaultValue = update.isSelected();

				boolean toggleScroll = scrollDefaultValue = autoScroll.isSelected();

				int maxNoOfMultiViewers = Integer.parseInt(maxMultiViewers.getText());
				//Quick hack to remove error, as class no longer used no need to implement fully
				gui.setPreferences(dpi, style, borderStyle, toggleScroll, pageMode, updateDefaultValue, maxNoOfMultiViewers, false, false);

				jf.setVisible(false);
			}
		});

		cancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				jf.setVisible(false);
			}
		});
		
		KeyListener numericalKeyListener = new KeyListener(){

			boolean consume = false;

			public void keyPressed(KeyEvent e) {
				consume = false;
				if((e.getKeyChar()<'0' || e.getKeyChar()>'9') && (e.getKeyCode()!=8 || e.getKeyCode()!=127))
					consume = true;
			}

			public void keyReleased(KeyEvent e) {}

			public void keyTyped(KeyEvent e) {
				if(consume)
					e.consume();
			}

		};
		dpi_Input.addKeyListener(numericalKeyListener);
		maxMultiViewers.addKeyListener(numericalKeyListener);
		
		settingsList.addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e) {
				changeDisplayedSettings();
			}
		});
		
		searchStyle.setSelectedIndex(searchStyleDefaultValue);
		dpi_Input.setText(dpiDefaultValue);
		if(borderDefaultValue==1)
			border.setSelected(true);
		else
			border.setSelected(false);
		autoScroll.setSelected(scrollDefaultValue);
		
		update.setSelected(updateDefaultValue);
	}
	
	/**
	 * buildMainPane(JPanel mainPane)
	 * 
	 * conveniance method to all for easier modification of preference window general layout
	 * 
	 * @param mainPane - The Jpanel that holds all fields for this window
	 * @return mainPane - Return the completed Jpanel to be attatched to a frame.
	 */
	private JPanel buildMainPane(JPanel mainPane){
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1;
		c.insets = new Insets(0,10,0,10);
		c.ipadx = 10;
		c.ipady = 10;
		c.fill = GridBagConstraints.BOTH;
			
		c.gridx = 0;
		c.gridy = 0;
		mainPane.add(title,c);
		
		c.gridx = 2;
		c.gridy = 1;
		c.gridheight = 5;
		c.gridwidth = 5;
		mainPane.add(settings[0],c);
		
		c.gridx = 5;
		c.gridy = 10;
		c.gridwidth = 2;
		c.gridheight = 1;
		mainPane.add(setButtons,c);
		
		c.gridx = 0;
		c.gridy = 1;
		c.gridheight = 9;
		c.gridwidth = 2;
		mainPane.add(settingsList,c);
		
		return mainPane;
	}
	
	/**
	 * changeDisplayedSettings()
	 * 
	 * Remove the previous settings options and
	 * display the new settings pane
	 */
	private void changeDisplayedSettings() {
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 2;
		c.gridy = 1;
		c.gridheight = 5;
		c.gridwidth = 5;
		c.weighty = 1.0;
		c.ipadx = 10;
		c.ipady = 10;
		c.insets = new Insets(0,10,0,10);
		
		mainPane.remove(settings[currentListSelection]);
		
		mainPane.add(settings[settingsList.getSelectedIndex()],c);
		
		mainPane.validate();
		mainPane.repaint();
		
		currentListSelection = settingsList.getSelectedIndex();
	}
	
	/**
	 * showPreferenceWindow()
	 *
	 * Ensure current values are loaded then display window.
	 */
	public void showPreferenceWindow(){
		jf.setVisible(true);
	}
	
	/*
	 * Creates a pane holding all Viewer settings (e.g Search Style, auto scrolling, etc)
	 */
	private JPanel createViewerSettings(JPanel pane){
		
		pane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		pane.setPreferredSize(new Dimension(250,100));
		pane.setMinimumSize(new Dimension(250,100));
		c.insets = new Insets(10,0,10,0);
		
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		pane.add(new JLabel(Messages.getMessage("PageLayoutViewMenu.SearchLayout")), c);
		
		c.gridx = 2;
		c.gridy = 0;
		pane.add(searchStyle, c);
		
		c.gridx = 0;
		c.gridy = 1;
		pane.add(new JLabel(Messages.getMessage("PageLayoutViewMenu.PageLayout")), c);
		
		c.gridx = 2;
		c.gridy = 1;
		pageLayout.setSelectedIndex((pageLayoutDefaultValue-1));
		pane.add(pageLayout, c);

		c.gridx = 0;
		c.gridy = 2;
		pane.add(new JLabel(Messages.getMessage("PdfViewerViewMenuAutoscrollSet.text")), c);
		
		c.gridx = 2;
		c.gridy = 2;
		pane.add(autoScroll, c);

		pane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0.3f,0.5f,1f), 1), "Viewer Settings"));
		
		return pane;
	}
	
	/*
	 * Creates a pane holding all PDF display settings (e.g Borders, Dpi, etc)
	 */
	private JPanel createDisplaySettings(JPanel pane){

        pane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		pane.setPreferredSize(new Dimension(250,100));
		pane.setMinimumSize(new Dimension(250,100));
		c.insets = new Insets(10,0,10,0);
//		c.ipadx = 10;
//		c.ipady = 10;
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		pane.add(new JLabel(Messages.getMessage("PdfViewerViewMenu.Dpi")), c);
		
		c.gridx = 2;
		c.gridy = 0;
		pane.add(dpi_Input, c);

		c.gridx = 0;
		c.gridy = 1;
		pane.add(new JLabel(Messages.getMessage("PageLayoutViewMenu.Borders_Show")), c);
		
		c.gridx = 2;
		c.gridy = 1;
		pane.add(border, c);
		
		pane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0.3f,0.5f,1f), 1), "Display Settings"));

		return pane;
	}
	
	/*
	 * Creates a pane holding MultiViewer settings
	 */
	private JPanel createMultiViewerSettings(JPanel pane) {
		
		
		pane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		pane.setPreferredSize(new Dimension(250,100));
		pane.setMinimumSize(new Dimension(250,100));
		c.insets = new Insets(10,0,10,0);
//		c.ipadx = 10;
//		c.ipady = 10;
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		pane.add(new JLabel("Maximum number of MultiViewer Windows "), c);
		
		c.gridx = 2;
		c.gridy = 0;
		pane.add(maxMultiViewers, c);

		pane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0.3f,0.5f,1f), 1), "MultiViewer Settings"));
		
		return pane;
	}
	
	/*
	 * Creates a pane holding update settings
	 */
	private JPanel createUpdateSettings(JPanel pane) {
		pane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		pane.setPreferredSize(new Dimension(250,100));
		pane.setMinimumSize(new Dimension(250,100));
		c.insets = new Insets(10,0,10,0);
//		c.ipadx = 10;
//		c.ipady = 10;
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		
		update.setText("Check for updates on startup");
		update.setHorizontalTextPosition(SwingConstants.LEFT);
		pane.add(update);
		
		pane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0.3f,0.5f,1f), 1), "Update Settings"));

		return pane;
	}

	/*
	 * Following methods used to load default values when application starts.
	 * 
	 */
	public void setAutoScrollDefaultValue(boolean autoScrollDefaultValue) {
		this.scrollDefaultValue = autoScrollDefaultValue;
	}

	public void setBorderDefaultValue(int borderDefaultValue) {
		this.borderDefaultValue = borderDefaultValue;
	}
	
	public void setUpdateDefaultValue(boolean updateDefaultValue) {
		this.updateDefaultValue = updateDefaultValue;
	}
	
	public void setMaxMultiViewersDefaultValue(String maxMultiViewersDefaultValue) {
		this.maxMultiViewersDefaultValue = maxMultiViewersDefaultValue;
	}

	public void setDpiDefaultValue(String dpiDefaultValue) {
		this.dpiDefaultValue = dpiDefaultValue;
	}

	public void setSearchStyleDefaultValue(int searchStyleDefaultValue) {
		this.searchStyleDefaultValue = searchStyleDefaultValue;
	}
	
	public void setPageLayoutDefaultValue(int pageLayoutDefaultValue) {
		if(pageLayoutDefaultValue>pageLayout.getItemCount()+1)
			pageLayoutDefaultValue = 1;
		this.pageLayoutDefaultValue = pageLayoutDefaultValue;
	}
}
