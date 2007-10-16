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
 * Created on 4/10/2005 andrek24
 * 
 */
package au.gov.naa.digipres.xena.core.test;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class XenaGUITester extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public XenaGUITester() {
		super("Xena GUI Component Tester!");
		setBounds(200, 200, 300, 400);

		this.setLayout(new GridLayout(5, 1));

		// TITLE PANEL
		JLabel titleLabel = new JLabel("Demo App for Xena stuff.");
		JPanel titlePanel = new JPanel();
		titlePanel.add(titleLabel);

		// HELP PANEL
		JLabel helpLabel = new JLabel("This button will show the help.");
		JButton helpButton = new JButton("Show help.");

		JPanel helpPanel = new JPanel();
		helpPanel.add(helpLabel);
		helpPanel.add(helpButton);

		HelpSet hs = null;
		try {
			ClassLoader cl = getClass().getClassLoader();
			URL hsURL = HelpSet.findHelpSet(cl, "XenaOnlineHelp");
			System.out.println("URL:" + hsURL);
			hs = new HelpSet(null, hsURL);
		} catch (Exception e1) {
			System.out.println("Helpset not found");
			// e1.printStackTrace();
		}
		HelpBroker hb = null;
		if (hs != null) {
			hb = hs.createHelpBroker();
		}

		helpButton.addActionListener(new CSH.DisplayHelpFromSource(hb));
		helpButton.addActionListener(new HelpListener());

		// SHOW XENA FILE PANEL
		JLabel showLabel = new JLabel("Show a xena file.");
		JButton showButon = new JButton("Open file.");
		showButon.addActionListener(new OpenListener());
		JPanel showPanel = new JPanel();
		showPanel.add(showLabel);
		showPanel.add(showButon);

		// and the view panel.

		// lets show the stuff.
		Container content = this.getContentPane();
		content.add(titlePanel);
		content.add(helpPanel);
		content.add(showPanel);

	}

	public class HelpListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.out.println("Button pressed... should be showing help.");
		}
	}

	public class OpenListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.out.println("Open Button pressed... time to open a file.");

			// okay here we go.
			// so... what to do?

			JFrame viewFrame = new JFrame("Hello world!");
			viewFrame.setBounds(250, 250, 300, 400);
			viewFrame.setVisible(true);

		}
	}

	public static void main(String[] argv) {
		XenaGUITester helpTester = new XenaGUITester();
		helpTester.setVisible(true);
	}

}
