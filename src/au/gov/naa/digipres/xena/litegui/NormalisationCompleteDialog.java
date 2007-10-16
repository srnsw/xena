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
 * Created on 17/03/2006 justinw5
 * 
 */
package au.gov.naa.digipres.xena.litegui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import au.gov.naa.digipres.xena.kernel.IconFactory;

public class NormalisationCompleteDialog extends JDialog {
	private static final String DIALOG_TITLE = "Normalisation Complete";

	public NormalisationCompleteDialog(Frame owner, int totalItems, int normalisedItems, int errorItems) throws HeadlessException {
		super(owner, DIALOG_TITLE, true);
		initGUI(totalItems, normalisedItems, errorItems);
		this.setLocationRelativeTo(owner);
	}

	private void initGUI(int totalItems, int normalisedItems, int errorItems) {
		this.setResizable(false);
		Font labelFont = new JLabel().getFont().deriveFont(Font.BOLD, 14);
		JPanel mainPanel = new JPanel(new GridBagLayout());

		// Total Items
		Color darkGreen = new Color(0, 180, 0);
		JLabel totalText = new JLabel("Total Items:");
		totalText.setFont(labelFont);
		JLabel totalVal = new JLabel("" + totalItems);
		totalVal.setFont(labelFont);
		addToGridBag(mainPanel, totalText, 0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(8, 8, 0, 0), 0, 0);
		addToGridBag(mainPanel, totalVal, 1, 0, GridBagConstraints.RELATIVE, 1, 0.0, 1.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
		             new Insets(8, 30, 0, 0), 0, 0);

		int nextRow = 1;

		// Unstarted items
		int unstartedItems = totalItems - (normalisedItems + errorItems);
		if (unstartedItems > 0) {
			JLabel unstartedText = new JLabel("Unstarted:");
			unstartedText.setFont(labelFont);
			JLabel unstartedVal = new JLabel("" + unstartedItems);
			unstartedVal.setFont(labelFont);
			unstartedVal.setForeground(new Color(160, 160, 160));
			JLabel unstartedIcon = new JLabel(IconFactory.getIconByName("images/icons/black_cross.png"));

			addToGridBag(mainPanel, unstartedText, 0, nextRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(8,
			                                                                                                                                      8,
			                                                                                                                                      0,
			                                                                                                                                      0),
			             0, 0);
			addToGridBag(mainPanel, unstartedVal, 1, nextRow, GridBagConstraints.RELATIVE, 1, 0.0, 1.0, GridBagConstraints.EAST,
			             GridBagConstraints.NONE, new Insets(8, 30, 0, 0), 0, 0);
			addToGridBag(mainPanel, unstartedIcon, 2, nextRow, GridBagConstraints.REMAINDER, 1, 0.0, 1.0, GridBagConstraints.EAST,
			             GridBagConstraints.NONE, new Insets(8, 30, 0, 6), 0, 0);
			nextRow++;

		}

		// Normalised Items
		if (normalisedItems > 0) {
			JLabel normText = new JLabel("Normalised:");
			normText.setFont(labelFont);
			JLabel normVal = new JLabel("" + normalisedItems);
			normVal.setFont(labelFont);
			normVal.setForeground(darkGreen);
			JLabel normIcon = new JLabel(IconFactory.getIconByName("images/icons/green_tick.png"));
			addToGridBag(mainPanel, normText, 0, nextRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(8, 8, 0,
			                                                                                                                                 0), 0, 0);
			addToGridBag(mainPanel, normVal, 1, nextRow, GridBagConstraints.RELATIVE, 1, 0.0, 1.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
			             new Insets(8, 30, 0, 0), 0, 0);
			addToGridBag(mainPanel, normIcon, 2, nextRow, GridBagConstraints.REMAINDER, 1, 0.0, 1.0, GridBagConstraints.EAST,
			             GridBagConstraints.NONE, new Insets(8, 30, 0, 6), 0, 0);
			nextRow++;
		}

		// Error Items
		if (errorItems > 0) {
			JLabel errorText = new JLabel("Errors:");
			errorText.setFont(labelFont);
			JLabel errorVal = new JLabel("" + errorItems);
			errorVal.setFont(labelFont);
			errorVal.setForeground(Color.RED);
			JLabel errorIcon = new JLabel(IconFactory.getIconByName("images/icons/red_cross_32.png"));
			addToGridBag(mainPanel, errorText, 0, nextRow, 1, GridBagConstraints.REMAINDER, 1.0, 1.0, GridBagConstraints.WEST,
			             GridBagConstraints.HORIZONTAL, new Insets(8, 8, 6, 0), 0, 0);
			addToGridBag(mainPanel, errorVal, 1, nextRow, GridBagConstraints.RELATIVE, GridBagConstraints.REMAINDER, 0.0, 1.0,
			             GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(8, 30, 6, 0), 0, 0);
			addToGridBag(mainPanel, errorIcon, 2, nextRow, GridBagConstraints.REMAINDER, GridBagConstraints.REMAINDER, 0.0, 1.0,
			             GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(8, 30, 6, 6), 0, 0);
			nextRow++;
		}

		JButton okButton = new JButton("OK");
		mainPanel.setBorder(new EtchedBorder());

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(okButton);
		this.add(mainPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);
		this.pack();

		okButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				NormalisationCompleteDialog.this.setVisible(false);
			}

		});
	}

	private void addToGridBag(Container container, Component component, int gridx, int gridy, int gridwidth, int gridheight, double weightx,
	                          double weighty, int anchor, int fill, Insets insets, int ipadx, int ipady) {
		GridBagConstraints gbc = new GridBagConstraints(gridx, gridy, gridwidth, gridheight, weightx, weighty, anchor, fill, insets, ipadx, ipady);
		container.add(component, gbc);
	}

}
