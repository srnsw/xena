package au.gov.naa.digipres.xena.plugin.office;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import au.gov.naa.digipres.xena.javatools.BrowseFile;

/**
 * Dialog box for configuring the office plugin.
 *
 * @author Chris Bitmead
 */
public class ConfigOfficeDialog extends JDialog {
	private JPanel panel1 = new JPanel();

	private BorderLayout borderLayout1 = new BorderLayout();

	private JButton cancelButton = new JButton();

	private GridLayout gridLayout2 = new GridLayout();

	private JPanel jPanel2 = new JPanel();

	private JButton okButton = new JButton();

	private JButton defaultButton = new JButton();

	private JPanel jPanel3 = new JPanel();

	private JPanel jPanel1 = new JPanel();

	private BrowseFile browseOoo = new BrowseFile();

	private JLabel jLabel1 = new JLabel();

	public static String DEFAULT_DIR = "c:/Program Files/OpenOffice.org1.1.4";

	boolean ok;

	private BorderLayout borderLayout2 = new BorderLayout();

	public ConfigOfficeDialog(Frame frame, String title, boolean modal) {
		super(frame, title, modal);
		try {
			jbInit();
			pack();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public ConfigOfficeDialog() {
		this(null, "", false);
	}

	private void jbInit() throws Exception {
		panel1.setLayout(borderLayout1);
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
		jLabel1.setToolTipText("");
		jLabel1.setText("OpenOffice.org Directory: ");
		browseOoo.getFileChooser().setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		browseOoo.getFileChooser().setDialogType(JFileChooser.OPEN_DIALOG);
		jPanel1.setLayout(borderLayout2);
		this.setTitle("Configure Office Plugin");
		getContentPane().add(panel1);
		jPanel3.add(jPanel2, null);
		jPanel2.add(okButton, null);
		jPanel2.add(cancelButton, null);
		jPanel2.add(defaultButton, null);
		panel1.add(jPanel1, BorderLayout.CENTER);
		panel1.add(jPanel3, BorderLayout.SOUTH);
		jPanel1.add(jLabel1, BorderLayout.WEST);
		jPanel1.add(browseOoo, BorderLayout.CENTER);
	}

	void cancelButton_actionPerformed(ActionEvent e) {
		ok = false;
		dispose();
	}

	void okButton_actionPerformed(ActionEvent e) {
		File ooofile = new File(browseOoo.getFile(), ConfigOpenOffice.SETUP);
		if (!ooofile.isFile()) {
			JOptionPane.showMessageDialog(this, "Error: OpenOffice.org directory is invalid", "Error", JOptionPane.ERROR_MESSAGE);
		}
		ok = true;
		dispose();
	}

	void defaultButton_actionPerformed(ActionEvent e) {
		// Windows specific, but it doesn't matter much.
		browseOoo.setText(DEFAULT_DIR);
	}

	public File getOooDirectory() {
		return browseOoo.getFile();
	}

	public void setOooDirectory(File oooDirectory) {
		browseOoo.setFile(oooDirectory);
	}

	public boolean isOk() {
		return ok;
	}
}
