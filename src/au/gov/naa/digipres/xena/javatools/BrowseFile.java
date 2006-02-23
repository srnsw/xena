package au.gov.naa.digipres.xena.javatools;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.Document;

/**
 *  Title: Description: Copyright: Copyright (c) 2001 Company:
 *
 * @author
 * @created    29 August 2002
 * @version    1.0
 */

public class BrowseFile extends JPanel {
	BorderLayout borderLayout1 = new BorderLayout();

	JButton browseButton = new JButton();

	java.util.List filters = new ArrayList();

	String defaultDirectory;

	JFileChooser chooser;

	JTextField fileNameField = new JTextField();

	java.util.List actionListeners = new ArrayList();

	public BrowseFile() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		BrowseFile b = new BrowseFile();
		b.getFileChooser().setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		//		b.setText(System.getProperty("user.home"));
		b.setText("c:\\");
		JDialog d = new JDialog();
		d.getContentPane().add(b);
		d.pack();
		d.setVisible(true);
	}

	public void setDefaultDirectory(String dirName) {
		this.defaultDirectory = dirName;
	}

	public void setText(String s) {
		fileNameField.setText(s);
		if (s != null) {
			fileNameField.setColumns(s.length());
		}
	}

	public void setFile(File f) {
		fileNameField.setText(f.toString());
	}

	public void setFileChooser(JFileChooser chooser) {
		this.chooser = chooser;
	}

	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		browseButton.setEnabled(enabled);
		fileNameField.setEnabled(enabled);
	}

	public File getFile() {
		return new File(fileNameField.getText());
	}

	public JFileChooser getFileChooser() {
		if (chooser == null) {
			chooser = new JFileChooser();
			javax.swing.filechooser.FileFilter basicFilter = chooser.getChoosableFileFilters()[0];
			Iterator it = filters.iterator();
			while (it.hasNext()) {
				chooser.addChoosableFileFilter((javax.swing.filechooser.FileFilter)it.next());
			}
			chooser.setFileFilter(basicFilter);
		}
		return chooser;
	}

	public String getText() {
		return fileNameField.getText();
	}

	public Document getDocument() {
		return fileNameField.getDocument();
	}

	public void addFileFilter(javax.swing.filechooser.FileFilter filter) {
		filters.add(filter);
	}

	public void addActionListener(java.awt.event.ActionListener l) {
		actionListeners.add(l);
	}

	void activateAction() {
		Iterator it = actionListeners.iterator();
		ActionEvent e = new ActionEvent(this, 0, null);
		while (it.hasNext()) {
			ActionListener l = (ActionListener)it.next();
			l.actionPerformed(e);
		}
	}

	void jbInit() throws Exception {
		this.setLayout(borderLayout1);
		browseButton.setToolTipText("Browse for File");
		browseButton.setText("Browse");
		browseButton.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				browseButton_actionPerformed(e);
			}
		});
		fileNameField.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileNameField_actionPerformed(e);
			}
		});
		fileNameField.setText(System.getProperty("user.home"));
		this.add(fileNameField, BorderLayout.CENTER);
		this.add(browseButton, BorderLayout.EAST);
	}

	void browseButton_actionPerformed(ActionEvent e) {
		String defaultFileName = fileNameField.getText();
		File defaultFile = new File(defaultFileName);
		/*
		 *  if (defaultDirectory != null) {
		 *  chooser.setCurrentDirectory(new File(defaultDirectory));
		 *  } else if (defaultFile != null) {
		 *  if (defaultFile.isDirectory()) {
		 *  chooser.setCurrentDirectory(defaultFile);
		 *  } else {
		 *  getFileChooser().setCurrentDirectory(defaultFile.getParentFile());
		 *  }
		 *  }
		 */
		if (defaultFile != null && !defaultFile.equals("")) {
			getFileChooser().setSelectedFile(defaultFile);
		}
		int option = getFileChooser().showOpenDialog(null);
		if (option == JFileChooser.APPROVE_OPTION) {
			File file = getFileChooser().getSelectedFile();
			fileNameField.setText(file.getAbsolutePath());
		}
		activateAction();
	}

	void fileNameField_actionPerformed(ActionEvent e) {
		activateAction();
	}
}
