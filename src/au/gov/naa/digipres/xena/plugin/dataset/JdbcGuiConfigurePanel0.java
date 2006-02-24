package au.gov.naa.digipres.xena.plugin.dataset;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.gui.GuiConfigureSubPanel;
import au.gov.naa.digipres.xena.gui.MainFrame;
import au.gov.naa.digipres.xena.javatools.BrowseFile;
import au.gov.naa.digipres.xena.kernel.XenaException;

/**
 * Panel 0 for configuring hte JdbcNormaliser class
 *
 * @author Chris Bitmead
 */
public class JdbcGuiConfigurePanel0 extends JPanel implements GuiConfigureSubPanel {
	BorderLayout borderLayout1 = new BorderLayout();

	JPanel panel = new JPanel();

	JdbcGuiConfigure configure;

	private BorderLayout borderLayout2 = new BorderLayout();

	private JTextField passwordText = new JTextField();

	private JLabel driverLabel = new JLabel();

	private JLabel jarLabel = new JLabel();

	private BrowseFile jarText = new BrowseFile();

	private JTextField urlText = new JTextField();

	private JLabel passwordLabel = new JLabel();

	private Box urlBox;

	private JLabel userLabel = new JLabel();

	private JButton testDatabaseButton = new JButton();

	private JLabel urlLabel = new JLabel();

	private JTextField userText = new JTextField();

	private Box userBox;

	private Box connectionBox;

	private Box passwordBox;

	private JTextField driverText = new JTextField();

	private Box jarBox;

	private Box driverBox;

	private JLabel statusBar = new JLabel();

	boolean testOk = false;

	public JdbcGuiConfigurePanel0(JdbcGuiConfigure configure) {
		this.configure = configure;
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void activate() {
		configure.nextOk(testOk);
	}

	public void start() {
		JdbcNormaliser n = (JdbcNormaliser)configure.getNormaliser();
		jarText.setText(n.getJar());
		driverText.setText(n.getDriver());
		urlText.setText(n.getUrl());
		userText.setText(n.getUser());
		passwordText.setText(n.getPassword());
	}

	public void finish() throws XenaException {
		JdbcNormaliser n = (JdbcNormaliser)configure.getNormaliser();
		n.setJar(jarText.getText());
		n.setDriver(driverText.getText());
		n.setUrl(urlText.getText());
		n.setUser(userText.getText());
		n.setPassword(passwordText.getText());
	}

	void jbInit() throws Exception {
		urlBox = Box.createHorizontalBox();
		userBox = Box.createHorizontalBox();
		connectionBox = Box.createVerticalBox();
		passwordBox = Box.createHorizontalBox();
		driverBox = Box.createHorizontalBox();
		jarBox = Box.createHorizontalBox();
		urlLabel.setText("URL              ");
		testDatabaseButton.setText("Test Database Connection");
		testDatabaseButton.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				testDatabaseButton_actionPerformed(e);
			}
		});
		userLabel.setText("User Login  ");
		passwordLabel.setText("Password    ");
		driverLabel.setText("Driver Class");
		jarLabel.setText("Driver jar      ");
		this.setLayout(borderLayout1);
		panel.setLayout(borderLayout2);
		statusBar.setText(" ");
		this.add(panel, BorderLayout.NORTH);
		panel.add(connectionBox, BorderLayout.CENTER);
		driverBox.add(driverLabel, null);
		driverBox.add(driverText, null);
		jarBox.add(jarLabel, null);
		jarBox.add(jarText, null);
		connectionBox.add(jarBox, null);
		connectionBox.add(driverBox, null);
		connectionBox.add(urlBox, null);
		urlBox.add(urlLabel, null);
		urlBox.add(urlText, null);
		connectionBox.add(userBox, null);
		userBox.add(userLabel, null);
		userBox.add(userText, null);
		connectionBox.add(passwordBox, null);
		passwordBox.add(passwordLabel, null);
		passwordBox.add(passwordText, null);
		connectionBox.add(testDatabaseButton, null);
		this.add(statusBar, BorderLayout.SOUTH);
		DocumentListener myListener =
			new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				configure.nextOk(testOk = false);
			}

			public void removeUpdate(DocumentEvent e) {
				configure.nextOk(testOk = false);
			}

			public void changedUpdate(DocumentEvent e) {
				configure.nextOk(testOk = false);
			}
		};
		jarText.getDocument().addDocumentListener(myListener);
		driverText.getDocument().addDocumentListener(myListener);
		urlText.getDocument().addDocumentListener(myListener);
		userText.getDocument().addDocumentListener(myListener);
		passwordText.getDocument().addDocumentListener(myListener);
	}

	void testDatabaseButton_actionPerformed(ActionEvent ev) {
		Connection conn = null;
		try {
			finish();
			conn = ((JdbcNormaliser)configure.getNormaliser()).getConnection();
			statusBar.setText("Connection Successful");
			testOk = true;
		} catch (XenaException x) {
			statusBar.setText(x.toString());
			MainFrame.singleton().showError(x);
		} catch (SAXException x) {
			statusBar.setText(x.toString());
			MainFrame.singleton().showError(x);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					statusBar.setText("Connected ok, but failure while closing");
				}
			}
		}
		configure.nextOk(testOk);
	}
}
