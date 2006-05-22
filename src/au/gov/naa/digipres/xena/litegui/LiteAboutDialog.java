/*
 * Created on 16/03/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.litegui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import au.gov.naa.digipres.xena.kernel.IconFactory;

public class LiteAboutDialog
{
	private static JDialog aboutDialog;
	private static Frame parentFrame;

	public static void showAboutDialog(Frame parent, String title, String versionText)
	{
		parentFrame = parent;
		aboutDialog = new JDialog(parentFrame, "About " + title, true);
								
		// Components
		JTextArea aboutText = new JTextArea(15, 30);
		aboutText.setEditable(false);
		aboutText.setBorder(new EmptyBorder(0, 0, 0, 0));
		JLabel titleLabel = new JLabel(title);
		JLabel buildLabel = new JLabel(versionText);
		buildLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
		buildLabel.setForeground(new Color(150, 150, 150));
		buildLabel.setBackground(aboutText.getBackground());
		buildLabel.setOpaque(true);
		titleLabel.setForeground(new Color(0xd2, 0, 0));
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20.0f));
		titleLabel.setBackground(aboutText.getBackground());
		titleLabel.setOpaque(true);

		aboutText.append("\nDigital Preservation Project Team:\n" +
		                 "\n" + 
		                 "John Baczynski\n" + 
		                 "Michael Carden\n" +
		                 "James Doig\n" +
		                 "Andrew Keeling\n" + 
		                 "Naomi Lamb\n" +
		                 "Liz McCredie\n" +
		                 "Bill Orr\n" +
		                 "David Pearson\n" + 
		                 "Karen Piscopo\n" +
		                 "Cornel Platzer\n" + 
		                 "Chris Strusz\n" +
		                 "Justin Waddell\n" +
		                 "\n" +
		                 "http://xena.sourceforge.net\n" +
		                 "http://www.naa.gov.au\n" +
		                 "\n" + 
		                 "digipres@naa.gov.au\n");
		
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				aboutDialog.setVisible(false);
				aboutDialog.dispose();
				aboutDialog = null;
				parentFrame = null;
			}
		});
		
		JLabel iconLabel = new JLabel(IconFactory.getIconByName("images/xena-splash-small.png"));
		JPanel iconPanel = new JPanel(new BorderLayout());
		iconPanel.add(iconLabel, BorderLayout.CENTER);
		iconPanel.setBackground(aboutText.getBackground());
		iconPanel.setBorder(new LineBorder(aboutText.getBackground(), 10));
		
		JLabel naaLabel = new JLabel(IconFactory.getIconByName("images/NAA-GOVT.png"));
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBackground(aboutText.getBackground());
		topPanel.setBorder(new LineBorder(aboutText.getBackground(), 30));
		topPanel.add(naaLabel, BorderLayout.CENTER);
		
		// Layout
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.setBackground(aboutText.getBackground());
		rightPanel.setBorder(new LineBorder(aboutText.getBackground(), 10));
		
//		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.setBackground(aboutText.getBackground());
		buttonPanel.setBorder(new LineBorder(aboutText.getBackground(), 5));
		buttonPanel.add(okButton, BorderLayout.SOUTH);
		
//		FlowLayout iconLayout = new FlowLayout(FlowLayout.CENTER);
//		iconLayout.setHgap(20);
//		JPanel iconPanel = new JPanel(iconLayout);
//		iconPanel.setBackground(aboutText.getBackground());
//		iconPanel.add(naaLabel);
		
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.setBackground(aboutText.getBackground());
//		bottomPanel.add(iconPanel, BorderLayout.CENTER);
		bottomPanel.add(buttonPanel, BorderLayout.EAST);		
		
		rightPanel.add(titleLabel, BorderLayout.NORTH);
		rightPanel.add(buildLabel, BorderLayout.CENTER);
		rightPanel.add(aboutText, BorderLayout.SOUTH);
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBackground(aboutText.getBackground());
		mainPanel.add(topPanel, BorderLayout.NORTH);
		mainPanel.add(iconPanel, BorderLayout.CENTER);
		mainPanel.add(rightPanel, BorderLayout.EAST);
		mainPanel.add(bottomPanel, BorderLayout.SOUTH);
		mainPanel.setBorder(new LineBorder(Color.BLACK));
		aboutDialog.add(mainPanel, BorderLayout.CENTER);
		
        // We don't want the window to be resizable, but we also want the icon
		// to appear (using setResizable(false) makes the icon disappear)...
		// so just pack every time the window is resized
        aboutDialog.addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(ComponentEvent event)
			{
				aboutDialog.pack();
				aboutDialog.setLocationRelativeTo(parentFrame);
			}
		});

		aboutDialog.pack();
		aboutDialog.setLocationRelativeTo(parentFrame);
		aboutDialog.setVisible(true);
		

	}
	

}
