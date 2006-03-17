/*
 * Created on 17/03/2006
 * justinw5
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

import au.gov.naa.digipres.xena.kernel.IconFactory;

public class NormalisationCompleteDialog extends JDialog
{
	private static final String DIALOG_TITLE = "Normalisation Complete";

	public NormalisationCompleteDialog(Frame owner,
									   int totalItems, 
									   int normalisedItems, 
									   int errorItems) throws HeadlessException
	{
		super(owner, DIALOG_TITLE, true);
		initGUI(totalItems, normalisedItems, errorItems);
		this.setLocationRelativeTo(owner);
	}
	
	private void initGUI(int totalItems, 
			   			 int normalisedItems, 
			   			 int errorItems)
	{
		this.setResizable(false);
		Font labelFont = new JLabel().getFont().deriveFont(Font.BOLD, 14);
		
		// Components		
		Color darkGreen = new Color(0, 140, 0);
		JLabel totalText = new JLabel("Total Items:");
		totalText.setFont(labelFont);
		JLabel normText = new JLabel("Normalised:");
		normText.setFont(labelFont);
		JLabel errorText = new JLabel("Errors:");
		errorText.setFont(labelFont);
		JLabel totalVal = new JLabel("" + totalItems);
		totalVal.setFont(labelFont);
		JLabel normVal = new JLabel("" + normalisedItems);
		normVal.setFont(labelFont);
		normVal.setForeground(darkGreen);
		JLabel errorVal = new JLabel("" + errorItems);
		errorVal.setFont(labelFont);
		errorVal.setForeground(Color.RED);
		JLabel normIcon = new JLabel(IconFactory.getIconByName("images/icons/green_tick.png"));
		JLabel errorIcon = new JLabel(IconFactory.getIconByName("images/icons/red_cross_32.png"));
		JButton okButton = new JButton("OK");
		
		// Layout
		JPanel mainPanel = new JPanel(new GridBagLayout());
		addToGridBag(mainPanel, totalText, 0, 0, 1, 1, 1.0, 1.0, 
		             GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
		             new Insets(8, 8, 0, 0), 0, 0);
		addToGridBag(mainPanel, totalVal, 1, 0, GridBagConstraints.RELATIVE, 1, 0.0, 1.0, 
		             GridBagConstraints.EAST, GridBagConstraints.NONE,
		             new Insets(8, 30, 0, 0), 0, 0);
		addToGridBag(mainPanel, normText, 0, 1, 1, GridBagConstraints.RELATIVE, 1.0, 1.0, 
		             GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
		             new Insets(8, 8, 0, 0), 0, 0);
		addToGridBag(mainPanel, normVal, 1, 1, GridBagConstraints.RELATIVE, GridBagConstraints.RELATIVE, 0.0, 1.0, 
		             GridBagConstraints.EAST, GridBagConstraints.NONE,
		             new Insets(8, 30, 0, 0), 0, 0);
		addToGridBag(mainPanel, normIcon, 2, 1, GridBagConstraints.REMAINDER, GridBagConstraints.RELATIVE, 0.0, 1.0, 
		             GridBagConstraints.EAST, GridBagConstraints.NONE,
		             new Insets(8, 30, 0, 6), 0, 0);
		addToGridBag(mainPanel, errorText, 0, 2, 1, GridBagConstraints.REMAINDER, 1.0, 1.0, 
		             GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
		             new Insets(8, 8, 6, 0), 0, 0);
		addToGridBag(mainPanel, errorVal, 1, 2, GridBagConstraints.RELATIVE, GridBagConstraints.REMAINDER, 0.0, 1.0, 
		             GridBagConstraints.EAST, GridBagConstraints.NONE,
		             new Insets(8, 30, 6, 0), 0, 0);
		addToGridBag(mainPanel, errorIcon, 2, 2, GridBagConstraints.REMAINDER, GridBagConstraints.REMAINDER, 0.0, 1.0, 
		             GridBagConstraints.EAST, GridBagConstraints.NONE,
		             new Insets(8, 30, 6, 6), 0, 0);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(okButton);
		this.add(mainPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);
		this.pack();
		
		okButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				NormalisationCompleteDialog.this.setVisible(false);
			}
			
		});
	}
	
    private void addToGridBag(Container container, Component component,
			  int gridx, int gridy, int gridwidth, int gridheight,
			  double weightx, double weighty, int anchor, int fill,    						  
			  Insets insets, int ipadx, int ipady)
	{
		GridBagConstraints gbc = 
		new GridBagConstraints(gridx, gridy, gridwidth, gridheight,
						   weightx, weighty, anchor, fill, 
						   insets, ipadx, ipady);
		container.add(component, gbc);
	}
	


}
