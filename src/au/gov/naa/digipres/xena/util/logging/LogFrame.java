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
 * @author Dan Spasojevic
 * @author Justin Waddell
 */

/*
 * Created on 7/12/2005 justinw5
 * 
 */
package au.gov.naa.digipres.xena.util.logging;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import au.gov.naa.digipres.xena.kernel.IconFactory;

/**
 * Simple frame which contains a TextArea to be used to display log
 * information.
 * created 12/12/2005
 * xena
 * Short desc of class:
 */
public class LogFrame extends JFrame {
	// Maximum log text to be retained in memory is 500kB
	private static final int MAX_LOG_SIZE = 500 * 1024;

	JTextArea logText;

	/**
	 * Creates and initialises a new LogFrame
	 * @param title
	 * @throws HeadlessException
	 */
	public LogFrame(String title) throws HeadlessException {
		super(title);
		initGUI(title);
	}

	/**
	 * One time GUI initialisation
	 * @param title
	 */
	private void initGUI(String title) {
		this.setSize(600, 400);
		this.setIconImage(IconFactory.getIconByName("images/xena-icon.png").getImage());

		logText = new JTextArea();
		logText.setEditable(false);
		JScrollPane logSP = new JScrollPane(logText);

		JButton closeButton = new JButton("Close");
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(closeButton);

		this.getContentPane().add(logSP, BorderLayout.CENTER);
		this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		// Action Listeners
		closeButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				LogFrame.this.setVisible(false);
			}

		});

	}

	/**
	 * Append the given text to the log display
	 * @param text
	 */
	public void addText(String text) {
		StringBuilder textBuff = new StringBuilder(text);
		if (textBuff.length() >= MAX_LOG_SIZE) {
			textBuff.setLength(MAX_LOG_SIZE);
			textBuff.append("**Truncated**");
			logText.setText(textBuff.toString());
		} else {
			String logContents = logText.getText();
			if (logContents.length() + textBuff.length() >= MAX_LOG_SIZE) {
				int eolIndex = logContents.indexOf("\n", textBuff.length());
				if (eolIndex < 0) {
					eolIndex = textBuff.length() - 1;
				}
				logText.replaceRange("", 0, eolIndex + 1);
			}
			logText.append(textBuff.toString());
		}

	}

	public int getLogSize() {
		return logText.getText().length();
	}
}
