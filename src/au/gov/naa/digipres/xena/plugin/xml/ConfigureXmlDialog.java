package au.gov.naa.digipres.xena.plugin.xml;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * Dialog to configure the XML plugin.
 *
 * @author Chris Bitmead
 */
public class ConfigureXmlDialog extends JDialog {
	private JPanel panel1 = new JPanel();

	private BorderLayout borderLayout1 = new BorderLayout();

	private JPanel jPanel1 = new JPanel();

	private JCheckBox allowRawXmlCheckBox = new JCheckBox();

	private JCheckBox allowTreeXmlCheckBox = new JCheckBox();

	private GridLayout gridLayout1 = new GridLayout();

	private JPanel jPanel3 = new JPanel();

	private JButton cancelButton = new JButton();

	private JButton okButton = new JButton();

	private JPanel jPanel2 = new JPanel();

	boolean ok;

	private JButton defaultButton = new JButton();

	private GridLayout gridLayout2 = new GridLayout();

	public ConfigureXmlDialog(Frame frame, String title, boolean modal) {
		super(frame, title, modal);
		try {
			jbInit();
			pack();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public ConfigureXmlDialog() {
		this(null, "", false);
	}

	public boolean getAllowRawXml() {
		return allowRawXmlCheckBox.isSelected();
	}

	public boolean getAllowTreeXml() {
		return allowTreeXmlCheckBox.isSelected();
	}

	public void setAllowRawXml(boolean v) {
		allowRawXmlCheckBox.setSelected(v);
	}

	public void setAllowTreeXml(boolean v) {
		allowTreeXmlCheckBox.setSelected(v);
	}

	private void jbInit() throws Exception {
		panel1.setLayout(borderLayout1);
		this.setTitle("Configure XML Plugin");
		allowRawXmlCheckBox.setText("Allow Raw Xml View for Sub-Views?");
		allowTreeXmlCheckBox.setText("Allow Tree Xml View for Sub-Views?");
		jPanel1.setLayout(gridLayout1);
		gridLayout1.setColumns(1);
		gridLayout1.setRows(0);
		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancelButton_actionPerformed(e);
			}
		});
		okButton.setText("OK");
		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				okButton_actionPerformed(e);
			}
		});
		jPanel2.setDebugGraphicsOptions(0);
		jPanel2.setLayout(gridLayout2);
		defaultButton.setText("Default");
		defaultButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				defaultButton_actionPerformed(e);
			}
		});
		getContentPane().add(panel1);
		panel1.add(jPanel1, BorderLayout.NORTH);
		jPanel1.add(allowRawXmlCheckBox, null);
		jPanel1.add(allowTreeXmlCheckBox, null);
		panel1.add(jPanel3, BorderLayout.SOUTH);
		jPanel3.add(jPanel2, null);
		jPanel2.add(okButton, null);
		jPanel2.add(cancelButton, null);
		jPanel2.add(defaultButton, null);
	}

	public boolean isOk() {
		return ok;
	}

	void okButton_actionPerformed(ActionEvent e) {
		ok = true;
		dispose();
	}

	void cancelButton_actionPerformed(ActionEvent e) {
		ok = false;
		dispose();
	}

	void defaultButton_actionPerformed(ActionEvent e) {
		allowRawXmlCheckBox.setSelected(false);
		allowTreeXmlCheckBox.setSelected(false);
	}
}
