package au.gov.naa.digipres.xena.javatools;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *  Widget for selecting a Color
 *
 * @author Chris Bitmead
 * @created    29 August 2002
 */
public class BrowseColor extends JPanel {

	java.util.List filters = new ArrayList();

	String defaultDirectory;

	java.util.List actionListeners = new ArrayList();

	Component parent = this;

	private BorderLayout borderLayout1 = new BorderLayout();

	private JLabel colorSwatch = new JLabel();

	private JButton browseButton = new JButton();

	public BrowseColor() {
		this(null);
	}

	public BrowseColor(Component parent) {
		this.parent = parent;
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		JDialog d = new JDialog();
		BrowseColor b = new BrowseColor(d);
		Color col = Color.GREEN;
		b.setColor(col);
		d.getContentPane().add(b);
		d.pack();
		d.setVisible(true);
	}

	public void setDefaultDirectory(String dirName) {
		this.defaultDirectory = dirName;
	}

	public void setColor(Color col) {
		if (col == null) {
			colorSwatch.setForeground(Color.BLACK);
		} else {
			colorSwatch.setForeground(col);
		}
	}

	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		browseButton.setEnabled(enabled);
	}

	public Color getColor() {
		return colorSwatch.getForeground();
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
		colorSwatch.setText("XXXXXXXXXX");
		browseButton.setToolTipText("Browse for Color");
		browseButton.setText("Browse");
		browseButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				browseButton_actionPerformed(e);
			}
		});
		this.add(colorSwatch, BorderLayout.CENTER);
		this.add(browseButton, BorderLayout.NORTH);
	}

	void browseButton_actionPerformed(ActionEvent e) {
		Color col = JColorChooser.showDialog(parent, "Select Colour", getColor());
		if (col != null) {
			setColor(col);
			activateAction();
		}
	}

	void colorLabel_actionPerformed(ActionEvent e) {
		activateAction();
	}
}
