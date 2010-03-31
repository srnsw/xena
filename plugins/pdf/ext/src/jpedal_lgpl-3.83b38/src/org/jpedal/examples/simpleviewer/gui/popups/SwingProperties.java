package org.jpedal.examples.simpleviewer.gui.popups;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.jpedal.Display;
import org.jpedal.PdfDecoder;
import org.jpedal.examples.simpleviewer.gui.CheckNode;
import org.jpedal.examples.simpleviewer.gui.CheckRenderer;
import org.jpedal.examples.simpleviewer.gui.SwingGUI;
import org.jpedal.examples.simpleviewer.utils.PropertiesFile;
import org.jpedal.utils.Messages;
import org.jpedal.utils.SwingWorker;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.l2fprod.common.swing.JButtonBar;

public class SwingProperties extends JPanel {
	
	Map reverseMessage =new HashMap();
	
	//Array of menu tabs.
	String[] menuTabs = {"ShowMenubar","ShowButtons","ShowDisplayoptions", "ShowNavigationbar", "ShowSidetabbar"};

	String propertiesLocation = "";
	
	PropertiesFile properties = null;

	//Window Components
	JFrame jf = new JFrame("JPedal PDF Preferences");
	
	JButton confirm = new JButton("OK");

	JButton cancel = new JButton("Cancel");

	JTabbedPane tabs = new JTabbedPane();

	//Settings Fields Components

	//DPI viewer value
	JTextField dpi_Input;

	//Search window display style
	JComboBox searchStyle;

	//Show border around page
	JCheckBox border;

	//Show download window
	JCheckBox downloadWindow;

	//Use Hi Res Printing
	JCheckBox HiResPrint;

	//perform automatic update check
	JCheckBox update = new JCheckBox("Check for updates on startup");

	//max no of multiviewers
	JTextField maxMultiViewers;

	//Set autoScroll when mouse at the edge of page
	JCheckBox autoScroll;
	
	//Set if we should open the file at the last viewed page
	JCheckBox openLastDoc;

	//Set default page layout
	JComboBox pageLayout = new JComboBox(new String[]{"Single Page","Continuous","Continuous Facing", "Facing"});

	JPanel highlightBoxColor = new JPanel();
	JPanel highlightTextColor = new JPanel();
	JCheckBox invertHighlight = new JCheckBox("Highlight Inverts Page");
	JTextField highlightComposite = new JTextField(""+PdfDecoder.highlightComposite);
	
//	private SwingGUI swingGUI;

	private Component parent;

	private boolean preferencesSetup=false;

	private JButton clearHistory;

	private JLabel historyClearedLabel;

	/**
	 * showPreferenceWindow()
	 *
	 * Ensure current values are loaded then display window.
	 * @param swingGUI 
	 */
	public void showPreferenceWindow(SwingGUI swingGUI){

		if(!preferencesSetup){
			preferencesSetup=true;

			createPreferenceWindow(swingGUI);
		}
//		this.swingGUI = swingGUI;
		jf.setLocationRelativeTo(parent);
		jf.setVisible(true);
	}

	private void saveGUIPreferences(SwingGUI gui){
		Component[] components = tabs.getComponents();
		for(int i=0; i!=components.length; i++){
			if(components[i] instanceof JPanel){
				Component[] panelComponets = ((JPanel)components[i]).getComponents();
				for(int j=0; j!=panelComponets.length; j++){
					if (panelComponets[j] instanceof JScrollPane) {
						Component[] scrollComponents = ((JScrollPane)panelComponets[j]).getComponents();
						for(int k=0; k!=scrollComponents.length; k++){
							if(scrollComponents[k] instanceof JViewport){
								Component[] viewportComponents = ((JViewport)scrollComponents[k]).getComponents();
								for(int l=0; l!=viewportComponents.length; l++){
									if(viewportComponents[l] instanceof JTree){
										JTree tree = ((JTree)viewportComponents[l]);
										CheckNode root = (CheckNode)tree.getModel().getRoot();
										if(root.getChildCount()>0){
											saveMenuPreferencesChildren(root, gui);
										}
									}
								}
							}
							
						}
					}
					if(panelComponets[j] instanceof JButton){
						JButton tempButton = ((JButton)panelComponets[j]);
						String value = ((String)reverseMessage.get(tempButton.getText().substring((Messages.getMessage("PdfCustomGui.HideGuiSection")+" ").length())));
						if(tempButton.getText().startsWith(Messages.getMessage("PdfCustomGui.HideGuiSection")+" ")){
							properties.setValue(value, "true");
							gui.alterProperty(value, true);
						}else{
							properties.setValue(value, "false");
							gui.alterProperty(value, false);
						}
					}
				}
			}
		}
	}

	private void saveMenuPreferencesChildren(CheckNode root, SwingGUI gui){
		for(int i=0; i!=root.getChildCount(); i++){
			CheckNode node = (CheckNode)root.getChildAt(i);
			String value = ((String)reverseMessage.get((String)node.getText()));
			if(node.isSelected()){
				properties.setValue(value, "true");
				gui.alterProperty(value, true);
			}else{
				properties.setValue(value, "false");
				gui.alterProperty(value, false);
			}

			if(node.getChildCount()>0){
				saveMenuPreferencesChildren(node, gui);
			}
		}
	}

	/**
	 * createPreferanceWindow(final GUI gui)
	 * Set up all settings fields then call the required methods to build the window
	 * 
	 * @param gui - Used to allow any changed settings to be saved into an external properties file.
	 * 
	 */
	private void createPreferenceWindow(final SwingGUI gui){
		
		//Get Properties file containing current preferences
		properties = gui.getProperties();
		//Get Properties file location
		propertiesLocation = gui.getPropertiesFileLocation();
		
		
		//Set up the properties window gui components
		String propValue = properties.getValue("DPI");
		if(propValue.length()>0)
			dpi_Input = new JTextField(propValue);
		else
			dpi_Input = new JTextField(PdfDecoder.dpi);
			
		propValue = properties.getValue("maxmultiviewers");
		if(propValue.length()>0)
			maxMultiViewers = new JTextField(propValue);
		else
			maxMultiViewers = new JTextField(20);
		
		searchStyle = new JComboBox(new String[]{Messages.getMessage("PageLayoutViewMenu.WindowSearch"),Messages.getMessage("PageLayoutViewMenu.TabbedSearch"),Messages.getMessage("PageLayoutViewMenu.MenuSearch")});
		pageLayout = new JComboBox(new String[]{Messages.getMessage("PageLayoutViewMenu.SinglePage"),Messages.getMessage("PageLayoutViewMenu.Continuous"),Messages.getMessage("PageLayoutViewMenu.Facing"),Messages.getMessage("PageLayoutViewMenu.ContinousFacing")});
		autoScroll = new JCheckBox(Messages.getMessage("PdfViewerViewMenuAutoscrollSet.text"));
		openLastDoc = new JCheckBox(Messages.getMessage("PdfViewerViewMenuOpenLastDoc.text"));
		border = new JCheckBox(Messages.getMessage("PageLayoutViewMenu.Borders_Show"));
		downloadWindow = new JCheckBox(Messages.getMessage("PageLayoutViewMenu.DownloadWindow_Show"));
		HiResPrint = new JCheckBox(Messages.getMessage("Printing.HiRes"));
		historyClearedLabel = new JLabel(Messages.getMessage("PageLayoutViewMenu.HistoryCleared"));
		historyClearedLabel.setForeground(Color.red);
		historyClearedLabel.setVisible(false);
		clearHistory = new JButton(Messages.getMessage("PageLayoutViewMenu.ClearHistory"));
		clearHistory.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				gui.clearRecentDocuments();

				SwingWorker searcher = new SwingWorker() {
					public Object construct() {
						for (int i = 0; i < 6; i++) {
							historyClearedLabel.setVisible(!historyClearedLabel.isVisible());
							try {
								Thread.sleep(300);
							} catch (InterruptedException e) {
							}
						}
						return null;
					}
				};

				searcher.start();
			}
		});
		JButton save = new JButton("Save As");
		JButton reset = new JButton("Reset to Default");
		
		//Create JFrame
		jf.getContentPane().setLayout(new BorderLayout());
		jf.getContentPane().add(this,BorderLayout.CENTER);
		jf.pack();
		jf.setSize(550, 450);

		/*
		 * Listeners that are reqired for each setting field
		 */
		//Set properties and close the window
		confirm.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				setPreferences(gui);
				JOptionPane.showMessageDialog(null, Messages.getMessage("PdfPreferences.restart"), "Restart Jpedal", JOptionPane.INFORMATION_MESSAGE);	
				jf.setVisible(false);
			}
		});
		//Close the window, don't save the properties
		cancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				jf.setVisible(false);
			}
		});
//		Save the properties into a new file
		save.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				//The properties file used when jpedal opened
				String lastProperties = gui.getPropertiesFileLocation();
				
				JFileChooser fileChooser = new JFileChooser();
				
				int i = fileChooser.showSaveDialog(jf);
				
				if(i == fileChooser.CANCEL_OPTION){
					//Do nothing
				}else if(i== fileChooser.ERROR_OPTION){
					//Do nothing
				}else if(i == fileChooser.APPROVE_OPTION){
					File f = fileChooser.getSelectedFile();

					if(f.exists())
						f.delete();
					
					//Setup properties in the new location
					gui.setPropertiesFileLocation(f.getAbsolutePath());
					setPreferences(gui);
				}
				//Reset to the properties file used when jpedal opened
				gui.setPropertiesFileLocation(lastProperties);
			}
		});
		//Reset the properties to JPedal defaults
		reset.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				int result = JOptionPane.showConfirmDialog(jf, Messages.getMessage("PdfPreferences.reset") , "Reset to Default", JOptionPane.YES_NO_OPTION);
				//The properties file used when jpedal opened
				if(result == JOptionPane.YES_OPTION){
					String lastProperties = gui.getPropertiesFileLocation();

					File f = new File(lastProperties);
					if(f.exists()){
						f.delete();
//						System.exit(1);
					}

					gui.getProperties().loadProperties(lastProperties);
					JOptionPane.showMessageDialog(jf, Messages.getMessage("PdfPreferences.restart"));
					jf.setVisible(false);
				}
			}
		});
		
		
		highlightComposite.addKeyListener(new KeyListener(){

			boolean consume = false;

			public void keyPressed(KeyEvent e) {
				consume = false;
				if((((JTextField)e.getSource()).getText().indexOf(".")!=-1 && e.getKeyChar()=='.') &&
						((e.getKeyChar()<'0' || e.getKeyChar()>'9') && (e.getKeyCode()!=8 || e.getKeyCode()!=127)))
					consume = true;
			}

			public void keyReleased(KeyEvent e) {}

			public void keyTyped(KeyEvent e) {
				if(consume)
					e.consume();
			}
			
		});
		
		
		//Only allow numerical input to the field
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
		
		/**
		 * Set the current properties from the properties file
		 */
		setLayout(new BorderLayout());

		JButtonBar toolbar = new JButtonBar(JButtonBar.VERTICAL);

		if(PdfDecoder.isRunningOnMac)
			toolbar.setPreferredSize(new Dimension(120,0));

		add(new ButtonBarPanel(toolbar), BorderLayout.CENTER);

		toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.gray));

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

		Dimension dimension = new Dimension(5,40);
		Box.Filler filler = new Box.Filler(dimension, dimension, dimension);

		confirm.setPreferredSize(cancel.getPreferredSize());

		
		buttonPanel.add(reset);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(confirm);
		buttonPanel.add(save);
		getRootPane().setDefaultButton(confirm);

		buttonPanel.add(filler);
		buttonPanel.add(cancel);
		buttonPanel.add(filler);

		buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.gray));

		add(buttonPanel, BorderLayout.SOUTH);
	}

	public void setPreferences(SwingGUI gui){
		int borderStyle = 0;
		int pageMode = (pageLayout.getSelectedIndex()+1);
		
		if(pageMode<Display.SINGLE_PAGE || pageMode>Display.CONTINUOUS_FACING)
			pageMode = Display.SINGLE_PAGE;
		if(border.isSelected()){
			borderStyle = 1;
		}
		
		int hBox = highlightBoxColor.getBackground().getRGB();
		int hText = highlightTextColor.getBackground().getRGB();
		boolean isInvert = invertHighlight.isSelected();
		
		/**
		 * set preferences from all but menu options
		 */
		properties.setValue("borderType", String.valueOf(borderStyle));
		properties.setValue("pageMode", String.valueOf(pageMode));
		properties.setValue("autoScroll", String.valueOf(autoScroll.isSelected()));
		properties.setValue("openLastDocument", String.valueOf(openLastDoc.isSelected()));
		properties.setValue("DPI", String.valueOf(dpi_Input.getText()));
		properties.setValue("searchWindowType", String.valueOf(searchStyle.getSelectedIndex()));
		properties.setValue("automaticupdate", String.valueOf(update.isSelected()));
		properties.setValue("maxmultiviewers", String.valueOf(maxMultiViewers.getText()));
		properties.setValue("showDownloadWindow", String.valueOf(downloadWindow.isSelected()));
		properties.setValue("useHiResPrinting", String.valueOf(HiResPrint.isSelected()));
		properties.setValue("highlightComposite", String.valueOf(highlightComposite.getText()));
		properties.setValue("highlightBoxColor", String.valueOf(hBox));
		properties.setValue("highlightTextColor", String.valueOf(hText));
		properties.setValue("invertHighlights", String.valueOf(isInvert));
		
		//Save all options found in a tree
		saveGUIPreferences(gui);
	}

	class ButtonBarPanel extends JPanel {

		private Component currentComponent;
		
//		Switch between idependent and properties dependent 
		private boolean newPreferencesCode = true;

		public ButtonBarPanel(JButtonBar toolbar) {
			setLayout(new BorderLayout());

			add(toolbar, BorderLayout.WEST);

			ButtonGroup group = new ButtonGroup();

			addButton("Display", "/org/jpedal/examples/simpleviewer/res/display.png", createDisplaySettings(), toolbar, group);

			addButton("Viewer", "/org/jpedal/examples/simpleviewer/res/viewer.png", createViewerSettings(), toolbar, group);

			addButton("Menu", "/org/jpedal/examples/simpleviewer/res/menu.png", createMenuSettings(), toolbar, group);

			addButton("Updates", "/org/jpedal/examples/simpleviewer/res/updates.png", createUpdateSettings(), toolbar, group);

			addButton("MulitViewer", "/org/jpedal/examples/simpleviewer/res/multiviewer.png", createMultiViewerSettings(), toolbar, group);
		}

		private JPanel makePanel(String title) {
			JPanel panel = new JPanel(new BorderLayout());
			JLabel topLeft = new JLabel(title);
			topLeft.setFont(topLeft.getFont().deriveFont(Font.BOLD));
			topLeft.setOpaque(true);
			topLeft.setBackground(panel.getBackground().brighter());
			
			JLabel topRight = new JLabel("( "+propertiesLocation+" )");
			topRight.setOpaque(true);
			topRight.setBackground(panel.getBackground().brighter());
			
			JPanel topbar = new JPanel(new BorderLayout());
			topbar.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
			topbar.setFont(topbar.getFont().deriveFont(Font.BOLD));
			topbar.setOpaque(true);
			topbar.setBackground(panel.getBackground().brighter());
			
			topbar.add(topLeft, BorderLayout.WEST);
			topbar.add(topRight, BorderLayout.EAST);
			
			panel.add(topbar, BorderLayout.NORTH);
			panel.setPreferredSize(new Dimension(400, 300));
			panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
			return panel;
		}

		private JPanel createDisplaySettings(){
			
			/**
			 * Set values from properties file first
			 */
			String propValue = properties.getValue("DPI");
			if(propValue.length()>0)
				dpi_Input.setText(propValue);
			
			propValue = properties.getValue("borderType");
			if(propValue.length()>0)
				if(Integer.parseInt(propValue)==1)
					border.setSelected(true);
				else
					border.setSelected(false);

			propValue = properties.getValue("showDownloadWindow");
			if(propValue.length()>0 && propValue.equals("true"))
				downloadWindow.setSelected(true);
			else
				downloadWindow.setSelected(false);

			propValue = properties.getValue("useHiResPrinting");
			if(propValue.length()>0 && propValue.equals("true"))
				HiResPrint.setSelected(true);
			else
				HiResPrint.setSelected(false);
			
			
			propValue = properties.getValue("highlightBoxColor");
			int hBoxColor = 0;
			if(propValue.length()>0){
				hBoxColor = Integer.parseInt(propValue);	
			}else{
				hBoxColor = PdfDecoder.highlightColor.getRGB();
			}
			final Color currentBox = new Color(hBoxColor);
			highlightBoxColor.setBackground(currentBox);
			
			
			
			propValue = properties.getValue("highlightTextColor");
			int hTextColor = 0;
			if(propValue.length()>0){
				hTextColor = Integer.parseInt(propValue);
			}else{
				if(PdfDecoder.backgroundColor!=null)
					hTextColor = PdfDecoder.backgroundColor.getRGB();
					
			}
			final Color currentText = new Color(hTextColor);
			highlightTextColor.setBackground(currentText);
			
			
			String hComposite = properties.getValue("highlightComposite");
			if(hComposite.length()>0)
				highlightComposite.setText(hComposite);
			
			String invertHighlights = properties.getValue("invertHighlights");
			if(invertHighlights.length()>0 && invertHighlights.toLowerCase().equals("true"))
				invertHighlight.setSelected(true);
			else
				invertHighlight.setSelected(false);
			
			final JButton hBoxButton = new JButton("Change Highlight Colour");
			hBoxButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					JColorChooser cc = new JColorChooser(currentBox);
					Color c = cc.showDialog(null, "Highlight Color", currentBox);
					highlightBoxColor.setBackground(c);
				
				}
			});
			
			final JButton hTextButton = new JButton("Change Highlighted Text Colour");
			hTextButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					JColorChooser cc = new JColorChooser(currentText);
					Color c = cc.showDialog(null, "Highlighted Text Color", currentText);
					highlightTextColor.setBackground(c);
				}
			});
			
			final JLabel hCompLabel = new JLabel("Change Highlight Transparency");
			
			/**
			 * Dependent of invert value, set highlight options to enabled / disabled
			 */
			if(invertHighlight.isSelected()){
				highlightBoxColor.setEnabled(false);
				highlightTextColor.setEnabled(false);
				highlightComposite.setEnabled(false);
				hTextButton.setEnabled(false);
				hBoxButton.setEnabled(false);
				hCompLabel.setEnabled(false);
			}else{
				highlightBoxColor.setEnabled(true);
				highlightTextColor.setEnabled(true);
				highlightComposite.setEnabled(true);
				hTextButton.setEnabled(true);
				hBoxButton.setEnabled(true);
				hCompLabel.setEnabled(true);
			}
			
			invertHighlight.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					if(((JCheckBox)e.getSource()).isSelected()){
						highlightBoxColor.setEnabled(false);
						highlightTextColor.setEnabled(false);
						highlightComposite.setEnabled(false);
						hTextButton.setEnabled(false);
						hBoxButton.setEnabled(false);
						hCompLabel.setEnabled(false);
					}else{
						highlightBoxColor.setEnabled(true);
						highlightTextColor.setEnabled(true);
						highlightComposite.setEnabled(true);
						hTextButton.setEnabled(true);
						hBoxButton.setEnabled(true);
						hCompLabel.setEnabled(true);
					}
				}
			});
			
			JPanel panel = makePanel("Display");

			JPanel pane = new JPanel();
			pane.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;

			c.insets = new Insets(10,0,0,5);
			c.weighty = 0;
			c.weightx = 0;
			c.gridx = 0;
			c.gridy = 0;
			JLabel label = new JLabel(Messages.getMessage("PdfViewerViewMenu.Dpi"));
			label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			pane.add(label, c);

			c.insets = new Insets(10,0,0,0);
			c.weightx = 1;
			c.gridx = 1;
			c.gridy = 0;
			pane.add(dpi_Input, c);
			
			c.gridwidth = 2;
			c.gridx = 0;
			c.gridy = 1;
			border.setMargin(new Insets(0,0,0,0));
			border.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			pane.add(border, c);

			c.gridwidth = 2;
			c.gridx = 0;
			c.gridy = 2;
			downloadWindow.setMargin(new Insets(0,0,0,0));
			downloadWindow.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			pane.add(downloadWindow, c);


			c.gridwidth = 2;
			c.gridx = 0;
			c.gridy = 3;
			HiResPrint.setMargin(new Insets(0,0,0,0));
			HiResPrint.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			pane.add(HiResPrint, c);
			
			
			c.gridwidth = 1;
			c.gridx = 0;
			c.gridy = 4;
			highlightBoxColor.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			pane.add(highlightBoxColor, c);
			c.gridwidth = 1;
			c.gridx = 1;
			c.gridy = 4;
			pane.add(hBoxButton, c);
			
			
			c.gridwidth = 1;
			c.gridx = 0;
			c.gridy = 5;
			highlightTextColor.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			pane.add(highlightTextColor, c);
			c.gridwidth = 1;
			c.gridx = 1;
			c.gridy = 5;
			pane.add(hTextButton, c);
			
			c.gridwidth = 1;
			c.gridx = 0;
			c.gridy = 6;
			pane.add(highlightComposite, c);
			c.gridwidth = 1;
			c.gridx = 1;
			c.gridy = 6;
			pane.add(hCompLabel, c);
			
			c.gridwidth = 1;
			c.gridx = 0;
			c.gridy = 7;
			pane.add(invertHighlight, c);
			
			c.gridwidth = 2;
			c.gridx = 0;
			c.gridy = 8;
			JPanel clearHistoryPanel = new JPanel();
			clearHistoryPanel.setLayout(new BoxLayout(clearHistoryPanel, BoxLayout.X_AXIS));
			clearHistoryPanel.add(clearHistory);
			clearHistoryPanel.add(Box.createHorizontalGlue());

			clearHistoryPanel.add(historyClearedLabel);
			clearHistoryPanel.add(Box.createHorizontalGlue());
			pane.add(clearHistoryPanel, c);

			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 9;
			pane.add(Box.createVerticalGlue(), c);
			//pane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0.3f,0.5f,1f), 1), "Display Settings"));

			panel.add(pane, BorderLayout.CENTER);

			return panel;
		}

		/*
		 * Creates a pane holding all Viewer settings (e.g Search Style, auto scrolling, etc)
		 */
		private JPanel createViewerSettings(){

			/**
			 * Set values from Properties file
			 */
			String propValue = properties.getValue("searchWindowType");
			if(propValue.length()>0)
				searchStyle.setSelectedIndex(Integer.parseInt(propValue));
			else
				searchStyle.setSelectedIndex(0);
			
			propValue = properties.getValue("pageMode");
			if(propValue.length()>0){
				int mode = Integer.parseInt(propValue);
				if(mode<Display.SINGLE_PAGE || mode>Display.CONTINUOUS_FACING)
					mode = Display.SINGLE_PAGE;

				pageLayout.setSelectedIndex(mode-1);
			}
			
			propValue = properties.getValue("autoScroll");
			if(propValue.equals("true"))
				autoScroll.setSelected(true);
			else
				autoScroll.setSelected(false);
			
			propValue = properties.getValue("openLastDocument");
			if(propValue.equals("true"))
				openLastDoc.setSelected(true);
			else
				openLastDoc.setSelected(false);
			
			JPanel panel = makePanel("Viewer");

			JPanel pane = new JPanel();
			pane.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;

			c.insets = new Insets(10,0,0,5);
			c.weighty = 0;
			c.weightx = 0;
			c.gridx = 0;
			c.gridy = 0;
			JLabel label = new JLabel(Messages.getMessage("PageLayoutViewMenu.SearchLayout"));
			label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			pane.add(label, c);

			c.insets = new Insets(10,0,0,0);
			c.weightx = 1;
			c.gridx = 1;
			c.gridy = 0;
			pane.add(searchStyle, c);

			c.insets = new Insets(10,0,0,5);
			c.weighty = 0;
			c.weightx = 0;
			c.gridx = 0;
			c.gridy = 1;
			JLabel label1 = new JLabel(Messages.getMessage("PageLayoutViewMenu.PageLayout"));
			label1.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			pane.add(label1, c);

			c.insets = new Insets(10,0,0,0);
			c.weightx = 1;
			c.gridx = 1;
			c.gridy = 1;
			pane.add(pageLayout, c);

			c.gridwidth = 2;
			c.gridx = 0;
			c.gridy = 2;
			autoScroll.setMargin(new Insets(0,0,0,0));
			autoScroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			pane.add(autoScroll, c);
			
			c.gridwidth = 2;
			c.gridx = 0;
			c.gridy = 3;
			openLastDoc.setMargin(new Insets(0,0,0,0));
			openLastDoc.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			pane.add(openLastDoc, c);

			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 4;
			pane.add(Box.createVerticalGlue(), c);
			//pane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0.3f,0.5f,1f), 1), "Display Settings"));

			panel.add(pane, BorderLayout.CENTER);

			return panel;
		}

		private JPanel createMenuSettings(){


			JPanel panel = makePanel("Menu");

			JPanel pane = new JPanel(new BorderLayout());
			
			tabs = new JTabbedPane();
			for(int t=0; t!=menuTabs.length; t++){
				//MenuBar Tab
				reverseMessage.put(Messages.getMessage("PdfCustomGui."+menuTabs[t]), menuTabs[t]);
				final CheckNode top = new CheckNode(Messages.getMessage("PdfCustomGui."+menuTabs[t]));
				top.setEnabled(true);
				top.setSelected(true);

				int i = 0;
				Vector last = new Vector();
				last.add(top);
				
				if(newPreferencesCode ){
					NodeList nodes = properties.getChildren(Messages.getMessage("PdfCustomGui."+menuTabs[t])+"Menu");
					addMenuToTree(t, nodes, top, last);
				}
//				else{
//					addChildToTree(t, i, top, last);
//				}
				
				final JTree tree = new JTree(top);
				JScrollPane scroll = new JScrollPane(tree);
				tree.setCellRenderer(new CheckRenderer());
				tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

				tree.addTreeSelectionListener(new TreeSelectionListener() {
					
					private void setChildrenValue(CheckNode checkNode, boolean status){
						for(int i=0; i!=checkNode.getChildCount(); i++){
							((CheckNode)checkNode.getChildAt(i)).setSelected(status);
							if(((CheckNode)checkNode.getChildAt(i)).getChildCount()>0){
								setChildrenValue(((CheckNode)checkNode.getChildAt(i)), status);
							}
						}
					}
					
					private void setParentValue(CheckNode checkNode, boolean status){
						checkNode.setSelected(status);
						if(((CheckNode)checkNode.getParent())!=null){
							setParentValue(((CheckNode)checkNode.getParent()), status);
						}
					}
					
					public void valueChanged(TreeSelectionEvent e) {

						final DefaultMutableTreeNode node = (DefaultMutableTreeNode)
						tree.getLastSelectedPathComponent();

						//toggle layer status when clicked
						Runnable updateAComponent = new Runnable() {

							public void run(){
								//update settings on display and in PdfDecoder
								CheckNode checkNode=(CheckNode)node;
								if(checkNode!=null){
									boolean reversedStatus=!checkNode.isSelected();
									if(reverseMessage.get(checkNode.getText()).equals("Preferences") && !reversedStatus){
										int result = JOptionPane.showConfirmDialog(jf, "Disabling this option will mean you can not acces this menu using this properties file. Do you want to continue?", "Preferences Access", JOptionPane.YES_NO_OPTION);
										if(result==JOptionPane.NO_OPTION){
											reversedStatus=!reversedStatus;
										}
									}

									if(checkNode.getChildCount()>0)
										setChildrenValue(checkNode, reversedStatus);


									if(((CheckNode)checkNode.getParent())!=null && reversedStatus==true)
										setParentValue(((CheckNode)checkNode.getParent()), reversedStatus);


									checkNode.setSelected(reversedStatus);

									tree.invalidate();
									tree.clearSelection();
									tree.repaint();

								}
							}
						};
						SwingUtilities.invokeLater(updateAComponent);
					}
				});
				JPanel display = new JPanel(new BorderLayout());
				
				
				final JButton hideGuiSection = new JButton();
				
				String propValue = properties.getValue(menuTabs[t]);
				if(propValue.toLowerCase().equals("true"))
					hideGuiSection.setText(Messages.getMessage("PdfCustomGui.HideGuiSection")+" "+  Messages.getMessage("PdfCustomGui."+menuTabs[t]));
				else{
					hideGuiSection.setText(Messages.getMessage("PdfCustomGui.ShowGuiSection")+" "+  Messages.getMessage("PdfCustomGui."+menuTabs[t]));
				}
				
				final int currentTab = t;
				hideGuiSection.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						if(hideGuiSection.getText().startsWith("Click here to show ")){
							hideGuiSection.setText(Messages.getMessage("PdfCustomGui.HideGuiSection")+" "+  Messages.getMessage("PdfCustomGui."+menuTabs[currentTab]));
							
						}else{
							hideGuiSection.setText(Messages.getMessage("PdfCustomGui.ShowGuiSection")+" "+  Messages.getMessage("PdfCustomGui."+menuTabs[currentTab]));
						}
					}
				});
				display.add(scroll, BorderLayout.CENTER);
				display.add(hideGuiSection, BorderLayout.SOUTH);
				tabs.add(display, Messages.getMessage("PdfCustomGui."+menuTabs[t]));
			}
			
			
			pane.add(tabs, BorderLayout.CENTER);
			panel.add(pane, BorderLayout.CENTER);


			return panel;
		}
		
		private void  addMenuToTree(int tab, NodeList nodes, CheckNode top, Vector previous){
			
			for(int i=0; i!=nodes.getLength(); i++){
				
				if(i<nodes.getLength()){
					String name = nodes.item(i).getNodeName();
					if(!name.startsWith("#")){
						//Node to add
						CheckNode newLeaf = new CheckNode(Messages.getMessage("PdfCustomGui."+name));
						newLeaf.setEnabled(true);
						//Set to reversedMessage for saving of preferences
						reverseMessage.put(Messages.getMessage("PdfCustomGui."+name), name);
						String propValue = properties.getValue(name);
						//Set if should be selected
						if(propValue.length()>0 && propValue.equals("true")){
							newLeaf.setSelected(true);
						}else{
							newLeaf.setSelected(false);
						}
						
						//If has child nodes
						if(nodes.item(i).hasChildNodes()){
							//Store this top value
							previous.add(top);
							//Set this node to ned top
							top.add(newLeaf);
							//Add new menu to tree
							addMenuToTree(tab, nodes.item(i).getChildNodes(), newLeaf, previous);
						}else{
							//Add to current top
							top.add(newLeaf);
						}
					}
				}
			}
		}

		/*
		 * Creates a pane holding update settings
		 */
		private JPanel createUpdateSettings() {
			
			/**
			 * Get value from the properties file
			 */
			String propValue = properties.getValue("automaticupdate");
			if(propValue.equals("true"))
				update.setSelected(true);
			else
				update.setSelected(false);
			
			JPanel panel = makePanel("Updates");

			JPanel pane = new JPanel();
			pane.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;

			c.insets = new Insets(10,0,0,0);
			c.weighty = 0;
			c.weightx = 1;
			c.gridwidth = 2;
			c.gridx = 0;
			c.gridy = 0;
			update.setMargin(new Insets(0,0,0,0));
			update.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			pane.add(update, c);

			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 2;
			pane.add(Box.createVerticalGlue(), c);
			//pane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0.3f,0.5f,1f), 1), "Display Settings"));

			panel.add(pane, BorderLayout.CENTER);

			return panel;
		}

		/*
		 * Creates a pane holding MultiViewer settings
		 */
		private JPanel createMultiViewerSettings() {

			/**
			 * Get values from the properties file
			 */
			String maxViewers = properties.getValue("maxmultiviewers");
			if(maxViewers!=null && maxViewers.length()>0)
				maxMultiViewers.setText(maxViewers);
			

			JPanel panel = makePanel("MultiViewer");

			JPanel pane = new JPanel();
			pane.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;

			c.insets = new Insets(10,0,0,5);
			c.weighty = 0;
			c.weightx = 0;
			c.gridx = 0;
			c.gridy = 0;
			JLabel label = new JLabel("Maximum number of MultiViewer Windows");
			label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			pane.add(label, c);

			c.insets = new Insets(10,0,0,0);
			c.weightx = 1;
			c.gridx = 1;
			c.gridy = 0;
			pane.add(maxMultiViewers, c);

			c.gridwidth = 2;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 1;
			pane.add(Box.createVerticalGlue(), c);
			//pane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0.3f,0.5f,1f), 1), "Display Settings"));

			panel.add(pane, BorderLayout.CENTER);

			return panel;
		}

		private void show(Component component) {
			if (currentComponent != null) {
				remove(currentComponent);
			}

			add("Center", currentComponent = component);
			revalidate();
			repaint();
		}

		private void addButton(String title, String iconUrl, final Component component, JButtonBar bar, ButtonGroup group) {
			Action action = new AbstractAction(title, new ImageIcon(getClass().getResource(iconUrl))) {
				public void actionPerformed(ActionEvent e) {
					show(component);
				}
			};

			JToggleButton button = new JToggleButton(action);

			if(PdfDecoder.isRunningOnMac)
				button.setHorizontalAlignment(AbstractButton.LEFT);

			bar.add(button);

			group.add(button);

			if (group.getSelection() == null) {
				button.setSelected(true);
				show(component);
			}
		}


	}

	public void setParent(Component parent) {
		this.parent = parent;
	}

	public void dispose() {
		
		this.removeAll();
		
		reverseMessage =null;
		
		menuTabs=null;
		propertiesLocation  =null;
		
		if(jf!=null)
		jf.removeAll();
		jf=null;
		
		confirm  =null;

		cancel  =null;

		if(tabs!=null)
		tabs.removeAll();
		tabs=null;

		dpi_Input=null;

		searchStyle=null;

		border =null;

		downloadWindow =null;

		HiResPrint =null;

		update  =null;

		maxMultiViewers =null;

		autoScroll =null;

		openLastDoc =null;
		
		pageLayout =null;

		if(highlightBoxColor!=null)
			highlightBoxColor.removeAll();
		highlightBoxColor  =null;
		
		if(highlightTextColor!=null)
			highlightTextColor.removeAll();
		highlightTextColor =null;
		
		if(invertHighlight!=null)
			invertHighlight.removeAll();
		invertHighlight =null;
		
		if(highlightComposite!=null)
			highlightComposite.removeAll();
		highlightComposite =null;
		
		if(jf!=null)
			jf.removeAll();
		parent =null;

		clearHistory =null;

		historyClearedLabel =null;
		
	}
}