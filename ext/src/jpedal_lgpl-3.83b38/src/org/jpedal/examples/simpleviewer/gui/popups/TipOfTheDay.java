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
* TipOfTheDay.java
* ---------------
*/
package org.jpedal.examples.simpleviewer.gui.popups;

import org.jpedal.examples.simpleviewer.utils.PropertiesFile;
import org.jpedal.utils.BrowserLauncher;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class TipOfTheDay extends JDialog {
	
	private List tipPaths = new ArrayList();
	
	private boolean tipLoadingFailed = false;

	private int currentTip;

	private JEditorPane tipPane = new JEditorPane();

	private PropertiesFile propertiesFile;

	private JCheckBox showTipsOnStartup = new JCheckBox("Show Tips on Startup");
	
	public TipOfTheDay(Container parent, String tipsRoot, PropertiesFile propertiesFile){
		super((JFrame)null, "Tip of the Day", true);
		
		this.propertiesFile = propertiesFile;
		
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		try {
			populateTipsList(tipsRoot, tipPaths);
		} catch (IOException e) {
			tipLoadingFailed = true;
		}
		
		Random r = new Random();
		currentTip = r.nextInt(tipPaths.size());
		
		setSize(550, 350);
		
		init();
		
		setLocationRelativeTo(parent);
	}

	private void init() {
		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints mainPanelConstraints = new GridBagConstraints();

		mainPanelConstraints.gridx = 0;
		mainPanelConstraints.gridy = 0;
		mainPanelConstraints.fill = GridBagConstraints.HORIZONTAL; 
		mainPanelConstraints.anchor = GridBagConstraints.PAGE_START;
		mainPanelConstraints.weighty = 0;
		mainPanelConstraints.weightx = 0;
		mainPanelConstraints.insets = new Insets(10,10,0,10);
		
		/**
		 * add the top panel to the Dialog, this is the image, and the title "Did you know ... ?"
		 */
		addTopPanel(mainPanelConstraints);
		
		mainPanelConstraints.fill = GridBagConstraints.BOTH; 
		mainPanelConstraints.gridy = 1;
		mainPanelConstraints.weighty = 1;
		mainPanelConstraints.weightx = 1;
		
		/**
		 * add the main JEditorPane to the Dialog which displays the html files
		 */
		addCenterTip(mainPanelConstraints);

		mainPanelConstraints.fill = GridBagConstraints.HORIZONTAL; 
		mainPanelConstraints.gridy = 2;
		mainPanelConstraints.weighty = 0;
		mainPanelConstraints.weightx = 0;
		mainPanelConstraints.insets = new Insets(0,7,0,10);
		
		/**
		 * add the JCheckBox to the Dialog which allows the user to enable/disable displaying on
		 * startup
		 */
		addDisplayOnStartup(mainPanelConstraints);
		
		mainPanelConstraints.gridy = 3;
		mainPanelConstraints.insets = new Insets(0,0,10,10);
		
		/**
		 * add the navigation buttons at the bottom of the panel which allows the user to move
		 * forwards/backwards through the tips, and also allows the Dialog to be closed.
		 */
		addBottomButtons(mainPanelConstraints);
	}

	private void addDisplayOnStartup(GridBagConstraints mainPanelConstraints) {
		String propValue = propertiesFile.getValue("displaytipsonstartup");
		if(propValue.length()>0)
			showTipsOnStartup.setSelected(propValue.equals("true"));
		showTipsOnStartup.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				propertiesFile.setValue("displaytipsonstartup", String.valueOf(showTipsOnStartup.isSelected()));
			}
		});
		getContentPane().add(showTipsOnStartup, mainPanelConstraints);
	}

	private void addBottomButtons(GridBagConstraints mainPanelConstraints) {
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));
		bottomPanel.add(Box.createHorizontalGlue());
		
		JButton previousTip = new JButton("Previous Tip");
		previousTip.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				changeTip(-1);
			}
		});
		bottomPanel.add(previousTip);
		
		bottomPanel.add(Box.createRigidArea(new Dimension(5,0)));
		
		JButton nextTip = new JButton("Next Tip");
		nextTip.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				changeTip(1);
			}
		});
		nextTip.setPreferredSize(previousTip.getPreferredSize());
		bottomPanel.add(nextTip);
		
		bottomPanel.add(Box.createRigidArea(new Dimension(5,0)));
		
		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				dispose();
				setVisible(false);
			}
		});
		close.setPreferredSize(previousTip.getPreferredSize());
		
		setFocusTraversalPolicy(new MyFocus(getFocusTraversalPolicy(), close));
		
		close.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent event) {}

			public void keyPressed(KeyEvent event) {
				if (event.getKeyCode() == 10) {
					dispose();
					setVisible(false);
				}
			}

			public void keyReleased(KeyEvent event) {}
		});
		
		bottomPanel.add(close);
		
		getContentPane().add(bottomPanel, mainPanelConstraints);
	}
	
	private void changeTip(int ammount) {
		currentTip += ammount;
		
		/** wrap the current tip if needed */
		if(currentTip == tipPaths.size())
			currentTip = 0;
		else if(currentTip == -1)
			currentTip = tipPaths.size() - 1;
		
		if(!tipLoadingFailed) {
			try {
				tipPane.setPage(getClass().getResource((String) tipPaths.get(currentTip)));
			} catch (IOException e) {
				tipLoadingFailed = true;
			}
		}
		
		if (tipLoadingFailed) {
			tipPane.setText("Error displaying tips, no tip to display");
		}
	}

	private void populateTipsList(String tipRoot, List items) throws IOException {
		try {
			URL url = getClass().getResource(tipRoot); //"/org/jpedal/examples/simpleviewer/res/tips"
			
			/**
			 * allow for it in jar
			 */
			if(url.toString().startsWith("jar")){
				JarURLConnection conn = (JarURLConnection) url.openConnection();
				JarFile jar = conn.getJarFile();
	
				for (Enumeration e = jar.entries(); e.hasMoreElements();) {
					JarEntry entry = (JarEntry) e.nextElement();
					String name=entry.getName();
					
					if ((!entry.isDirectory()) && name.indexOf("/res/tips/")!=-1
							&& (name.endsWith(".html") || name.endsWith(".html"))) { // this
						items.add("/" + name);
					}
				}
			}else{ //IDE
				BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
				
				String inputLine;
				
				while ((inputLine = in.readLine()) != null) {
					if (inputLine.indexOf('.') == -1) { // this is a directory
						populateTipsList(tipRoot + "/" + inputLine, items);
					} else if ((inputLine.endsWith(".htm")) || inputLine.endsWith(".html")) { // this is a file
						items.add(tipRoot + "/" + inputLine);
					}
				}
			
			
				in.close();
			}
		} catch (IOException e) {
			throw e;
		}
	}
	
	private void addCenterTip(GridBagConstraints mainPanelConstraints) {
		tipPane.setEditable(false);
		tipPane.setAutoscrolls(true);
		
		tipPane.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
					try {
						BrowserLauncher.openURL(e.getURL().toExternalForm());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getViewport().add(tipPane);
		scrollPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		
		getContentPane().add(scrollPane, mainPanelConstraints);
		
		changeTip(0);
	}

	private void addTopPanel(GridBagConstraints mainPanelConstraints) {
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));

		JLabel tipImage = new JLabel(new ImageIcon(getClass().getResource("/org/jpedal/examples/simpleviewer/res/tip.png")));
		topPanel.add(tipImage);
		
		JLabel label = new JLabel("Did you know ... ?");
		Font font = label.getFont().deriveFont(16.0f);
	    label.setFont(font);
	    
	    topPanel.add(Box.createRigidArea(new Dimension(10, 0)));
	    
		topPanel.add(label);
		getContentPane().add(topPanel, mainPanelConstraints);
	}
	
    static class MyFocus extends FocusTraversalPolicy {
        FocusTraversalPolicy original;
        JButton close;

        MyFocus(FocusTraversalPolicy original, JButton close){
            this.original = original;
            this.close = close;

        }
 
        public Component getComponentAfter(Container arg0, Component arg1) {
            return original.getComponentAfter(arg0, arg1);
        }
        
        public Component getComponentBefore(Container arg0, Component arg1) {
            return original.getComponentBefore(arg0, arg1);
        }
        
        public Component getFirstComponent(Container arg0) {
            return original.getFirstComponent(arg0);
        }
        
        public Component getLastComponent(Container arg0) {
            return original.getLastComponent(arg0);
        }
        
        public Component getDefaultComponent(Container arg0) {
            return close;
        }
    }
}