/**
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.jpedal.org
 *
 * (C) Copyright 2007, IDRsolutions and Contributors.
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

  * UpdateDialog.java
  * ---------------
  * (C) Copyright 2007, by IDRsolutions and Contributors.
 */
package org.jpedal.examples.simpleviewer.gui.popups;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.jpedal.utils.BrowserLauncher;

public class UpdateDialog extends JDialog {

	private String availableVersion;
	private String currentVersion;

	public UpdateDialog(Container parent, String currentVersion, String availableVersion){
		super((JFrame)null, "Update Info", true);

		this.currentVersion = currentVersion;
		this.availableVersion = availableVersion;
		
		setSize(550, 350);

		init();

		setLocationRelativeTo(parent);
	}

	private void init() {
		getContentPane().setLayout(new GridBagLayout());

		GridBagConstraints mainPanelConstraints = new GridBagConstraints();

		mainPanelConstraints.fill = GridBagConstraints.BOTH; 
		mainPanelConstraints.weighty = 1;
		mainPanelConstraints.weightx = 1;
		mainPanelConstraints.insets = new Insets(10,10,10,10);
		
		addCenterPanel(mainPanelConstraints);

		mainPanelConstraints.weighty = 0;
		mainPanelConstraints.gridy = 1;
		
		addBottomButtons(mainPanelConstraints);
		
	}

	private void addCenterPanel(GridBagConstraints mainPanelConstraints) {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEtchedBorder());
		add(panel, mainPanelConstraints);
		
		GridBagConstraints panelConstraints = new GridBagConstraints();

		panelConstraints.gridx = 0;
		panelConstraints.gridy = 0;
		panelConstraints.fill = GridBagConstraints.HORIZONTAL; 
		panelConstraints.anchor = GridBagConstraints.PAGE_START;
		panelConstraints.weighty = 0;
		panelConstraints.weightx = 0;
		panelConstraints.insets = new Insets(10,10,10,10);

		panelConstraints.gridwidth = 2;

		SimpleAttributeSet plain = new SimpleAttributeSet();
		StyleConstants.setForeground(plain, Color.black);
		StyleConstants.setBold(plain, false);

		SimpleAttributeSet bold = new SimpleAttributeSet();
		StyleConstants.setForeground(bold, Color.black);
		StyleConstants.setBold(bold, true);

		JTextPane header = new JTextPane();
		header.setEditable(false);
		header.setOpaque(false);
		Document doc = header.getDocument();
		try {
			doc.insertString(0, "A new version of JPedal is available.", bold);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		panel.add(header, panelConstraints);

		panelConstraints.gridwidth = 1;
		panelConstraints.gridy = 1;
		panelConstraints.insets = new Insets(10,10,0,10);
		JTextPane currentVersionLabel = new JTextPane();
		currentVersionLabel.setEditable(false);
		currentVersionLabel.setOpaque(false);
		doc = currentVersionLabel.getDocument();
		try {
			doc.insertString(0, "Your current version:", plain);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		currentVersionLabel.setMinimumSize(currentVersionLabel.getPreferredSize());
		panel.add(currentVersionLabel, panelConstraints);

		panelConstraints.weightx = 1;
		panelConstraints.gridx = 1;
		JTextPane currentVersionPane = new JTextPane();
		currentVersionPane.setEditable(false);
		currentVersionPane.setOpaque(false);
		doc = currentVersionPane.getDocument();
		try {
			doc.insertString(0, currentVersion, plain);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		panel.add(currentVersionPane, panelConstraints);

		panelConstraints.insets = new Insets(0,10,10,10);
		panelConstraints.weightx = 0;
		panelConstraints.gridx = 0;
		panelConstraints.gridy = 2;
		JTextPane availableVersionLabel = new JTextPane();
		availableVersionLabel.setEditable(false);
		availableVersionLabel.setOpaque(false);
		doc = availableVersionLabel.getDocument();
		try {
			doc.insertString(0, "Available Version:", plain);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		panel.add(availableVersionLabel, panelConstraints);

		panelConstraints.gridwidth = 2;
		panelConstraints.weightx = 1;
		panelConstraints.gridx = 1;
		JTextPane availableVersionPane = new JTextPane();
		availableVersionPane.setEditable(false);
		availableVersionPane.setOpaque(false);
		doc = availableVersionPane.getDocument();
		try {
			doc.insertString(0, availableVersion, plain);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		panel.add(availableVersionPane, panelConstraints);

		panelConstraints.insets = new Insets(10,10,10,10);
		panelConstraints.weightx = 0;
		panelConstraints.gridx = 0;
		panelConstraints.gridy = 3;
		JTextPane moreInfo = new JTextPane();
		moreInfo.setEditable(false);
		moreInfo.setOpaque(false);
		doc = moreInfo.getDocument();
		try {
			doc.insertString(0, "Press ", plain);
			doc.insertString(doc.getLength(), "More info... ", bold);
			doc.insertString(doc.getLength(), "to open a web page where you can download JPedal or learn more about the new version", plain);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

		panel.add(moreInfo, panelConstraints);

		panelConstraints.gridy = 4;
		JTextPane configure = new JTextPane();
		doc = configure.getDocument();
		try {
			doc.insertString(0, "To configure automatic updates settings, see ", plain);
			doc.insertString(doc.getLength(), "View | Preferences", bold);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		configure.setEditable(false);
		configure.setOpaque(false);

		panel.add(configure, panelConstraints);

		panelConstraints.weighty = 1;
		panelConstraints.gridy = 5;
		JTextPane manual = new JTextPane();
		doc = manual.getDocument();
		try {
			doc.insertString(0, "To check for new updates manually, use ", plain);
			doc.insertString(doc.getLength(), "Help | Check for Updates", bold);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		manual.setEditable(false);
		manual.setOpaque(false);

		panel.add(manual, panelConstraints);
	}
	
	private void addBottomButtons(GridBagConstraints mainPanelConstraints) {
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));
		bottomPanel.add(Box.createHorizontalGlue());

		JButton moreInfo = new JButton("More Info");
		moreInfo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				try {
					BrowserLauncher.openURL("http://www.jpedal.org/builds.php");
				} catch (IOException e1) {
				}
			}
		});

		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				dispose();
				setVisible(false);
			}
		});
		close.setPreferredSize(moreInfo.getPreferredSize());

		bottomPanel.add(close);
		bottomPanel.add(Box.createRigidArea(new Dimension(5,0)));
		
		setFocusTraversalPolicy(new MyFocus(getFocusTraversalPolicy(), moreInfo));
		
		moreInfo.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent event) {}

			public void keyPressed(KeyEvent event) {
				if (event.getKeyCode() == 10) {
					try {
						BrowserLauncher.openURL("http://www.jpedal.org/builds.php");
					} catch (IOException e1) {
					}
				}
			}

			public void keyReleased(KeyEvent event) {}
		});
		
		bottomPanel.add(moreInfo);

		add(bottomPanel, mainPanelConstraints);
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
	
	public static void main(String[] args) {
		UpdateDialog panel = new UpdateDialog(null, "3.2", "3.3");
		panel.setVisible(true);
	}
}
