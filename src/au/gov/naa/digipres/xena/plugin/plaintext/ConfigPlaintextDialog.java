package au.gov.naa.digipres.xena.plugin.plaintext;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import au.gov.naa.digipres.xena.javatools.ListChooser;

/**
 * Dialog to configure the plaintext plugin.
 *
 * @author Chris Bitmead
 */
public class ConfigPlaintextDialog extends JDialog {
	private JPanel panel1 = new JPanel();

	private BorderLayout borderLayout1 = new BorderLayout();

	private JButton cancelButton = new JButton();

	private GridLayout gridLayout2 = new GridLayout();

	private JPanel jPanel2 = new JPanel();

	private JButton okButton = new JButton();

	private JButton defaultButton = new JButton();

	private JPanel jPanel3 = new JPanel();

	ListChooser listChooser = new ListChooser();

	boolean ok;

	private BorderLayout borderLayout2 = new BorderLayout();

	private Border border1 = BorderFactory.createEmptyBorder();

	private Border border2 = new TitledBorder(border1, "Character sets that may be guessed");

	public ConfigPlaintextDialog(Frame frame, String title, boolean modal) {
		super(frame, title, modal);
		try {
			jbInit();
			pack();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public ConfigPlaintextDialog() {
		this(null, "", false);
	}

	private void jbInit() throws Exception {
		setTitle("Configure Plaintext Plugin");
		panel1.setLayout(borderLayout1);
		panel1.setBorder(border2);
		panel1.add(listChooser, BorderLayout.CENTER);
		listChooser.setLeftTitle("Enabled");
		listChooser.setRightTitle("Disabled");
		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancelButton_actionPerformed(e);
			}
		});
		jPanel2.setDebugGraphicsOptions(0);
		jPanel2.setLayout(gridLayout2);
		okButton.setText("OK");
		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				okButton_actionPerformed(e);
			}
		});
		defaultButton.setText("Default");
		defaultButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				defaultButton_actionPerformed(e);
			}
		});
		this.setTitle("Configure PlainText Plugin");
		getContentPane().add(panel1);
		jPanel3.add(jPanel2, null);
		jPanel2.add(okButton, null);
		jPanel2.add(cancelButton, null);
		jPanel2.add(defaultButton, null);
		panel1.add(jPanel3, BorderLayout.SOUTH);
	}

	void cancelButton_actionPerformed(ActionEvent e) {
		ok = false;
		dispose();
	}

	void okButton_actionPerformed(ActionEvent e) {
		ok = true;
		dispose();
	}

	void defaultButton_actionPerformed(ActionEvent e) {
		DefaultListModel lmodel = ((DefaultListModel)listChooser.getLeftList().getModel());
		DefaultListModel rmodel = ((DefaultListModel)listChooser.getRightList().getModel());
		lmodel.removeAllElements();
		rmodel.removeAllElements();
		SortedMap charsets = java.nio.charset.Charset.availableCharsets();
		Iterator it = charsets.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			lmodel.addElement(entry.getKey());
		}
	}

	public boolean isOk() {
		return ok;
	}
}
