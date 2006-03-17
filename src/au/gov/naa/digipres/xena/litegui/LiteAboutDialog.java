/*
 * Created on 16/03/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.litegui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

	public static void showAboutDialog(Frame parent, String title, String versionText)
	{
		aboutDialog = new JDialog(parent, title, true);
		
		// Components
		JLabel iconLabel = new JLabel(IconFactory.getIconByName("images/xena-splash.png"));
		JTextArea aboutText = new JTextArea(15, 30);
		aboutText.setEditable(false);
		aboutText.setBorder(new EmptyBorder(0, 0, 0, 0));
		JLabel versionLabel = new JLabel(versionText);
		versionLabel.setFont(versionLabel.getFont().deriveFont(Font.BOLD));
		versionLabel.setBackground(aboutText.getBackground());
		versionLabel.setOpaque(true);

		aboutText.append("\nProject Team:\n\nJohn Baczynski\nMichael Carden\nAndrew Keeling\nLiz McCredie\n" +
		                 "David Pearson\nCornel Platzer\nChris Strusz\nJustin Waddell\n" +
		                 "\nhttp://xena.sourceforge.net\nhttp://www.naa.gov.au\n\ndigipres@naa.gov.au\n");
		
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				aboutDialog.setVisible(false);
			}
		});
		
		JLabel ausGovLabel = new JLabel(IconFactory.getIconByName("images/aus_gov.gif"));
		JLabel naaLabel = new JLabel(IconFactory.getIconByName("images/naa.png"));
		JLabel ePermLabel = new JLabel(IconFactory.getIconByName("images/e-permanence.gif"));
		
		// Layout
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.setBackground(aboutText.getBackground());
		rightPanel.setBorder(new LineBorder(aboutText.getBackground(), 10));
		
//		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.setBackground(aboutText.getBackground());
		buttonPanel.setBorder(new LineBorder(aboutText.getBackground(), 5));
		buttonPanel.add(okButton, BorderLayout.SOUTH);
		
		FlowLayout iconLayout = new FlowLayout(FlowLayout.CENTER);
		iconLayout.setHgap(20);
		JPanel iconPanel = new JPanel(iconLayout);
		iconPanel.setBackground(aboutText.getBackground());
		iconPanel.add(ausGovLabel);
		iconPanel.add(naaLabel);
		iconPanel.add(ePermLabel);
		
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.setBackground(aboutText.getBackground());
		bottomPanel.add(iconPanel, BorderLayout.CENTER);
		bottomPanel.add(buttonPanel, BorderLayout.EAST);		
		
		rightPanel.add(versionLabel, BorderLayout.NORTH);
		rightPanel.add(aboutText, BorderLayout.CENTER);
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBackground(aboutText.getBackground());
		mainPanel.add(iconLabel, BorderLayout.CENTER);
		mainPanel.add(rightPanel, BorderLayout.EAST);
		mainPanel.add(bottomPanel, BorderLayout.SOUTH);
		mainPanel.setBorder(new LineBorder(Color.BLACK));
		aboutDialog.add(mainPanel, BorderLayout.CENTER);
		
		aboutDialog.setUndecorated(true);
		aboutDialog.pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension splashSize = aboutDialog.getSize();
		aboutDialog.setLocation((screenSize.width - splashSize.width) / 2, (screenSize.height - splashSize.height) / 2);
		aboutDialog.setVisible(true);
	}
	

}
