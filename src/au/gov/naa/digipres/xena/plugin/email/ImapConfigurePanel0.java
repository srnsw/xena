package au.gov.naa.digipres.xena.plugin.email;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.mail.MessagingException;
import javax.mail.Store;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import au.gov.naa.digipres.xena.gui.GuiConfigureSubPanel;
import au.gov.naa.digipres.xena.gui.MainFrame;
import au.gov.naa.digipres.xena.kernel.XenaException;

/**
 * Panel 0 to configure the EmailToXenaEmailNormaliser for IMAP emails.
 *
 * @author Chris Bitmead
 */
public class ImapConfigurePanel0 extends JPanel implements GuiConfigureSubPanel {
	ImapToXenaEmailGuiConfigure configure;

	JPanel portPanel = new JPanel();

	JTextField portTextField = new JTextField();

	JLabel portLabel = new JLabel();

	JPanel hostPanel = new JPanel();

	JLabel hostLabel = new JLabel();

	JTextField hostTextField = new JTextField();

	JPanel passwordPanel = new JPanel();

	JLabel passwordLabel = new JLabel();

	JTextField passwordTextField = new JTextField();

	JPanel userPanel = new JPanel();

	JLabel userLabel = new JLabel();

	JTextField userTextField = new JTextField();

	GridLayout gridLayout1 = new GridLayout();

	static final String NOT_TESTED = "Connection Not Tested";

	static final String BAD_TESTED = "Connection Failed";

	static final String OK_TESTED = "Connection OK";

	private JButton testButton = new JButton();

	private JLabel testLabel = new JLabel();

	private FlowLayout flowLayout1 = new FlowLayout();

	public ImapConfigurePanel0(ImapToXenaEmailGuiConfigure configure) {
		this.configure = configure;
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void start() throws XenaException {
	}

	public void activate() throws XenaException {
		EmailToXenaEmailNormaliser n = (EmailToXenaEmailNormaliser)configure.getNormaliser();
		hostTextField.setText(n.getHostName());
		userTextField.setText(n.getUserName());
		passwordTextField.setText(n.getPassword());
		portTextField.setText(Integer.toString(n.getPort()));
	}

	public void finish() throws XenaException {
		EmailToXenaEmailNormaliser n = (EmailToXenaEmailNormaliser)configure.getNormaliser();
		n.setHostName(hostTextField.getText());
		n.setPassword(passwordTextField.getText());
		n.setUserName(userTextField.getText());
		try {
			n.setPort(Integer.parseInt(portTextField.getText()));
		} catch (NumberFormatException x) {
			throw new XenaException(x);
		}
	}

	public ImapConfigurePanel0() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void jbInit() throws Exception {
		portTextField.setText("143");
		portTextField.setColumns(4);
		portTextField.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				portTextField_keyPressed(e);
			}
		});
		portLabel.setRequestFocusEnabled(true);
		portLabel.setVerifyInputWhenFocusTarget(true);
		portLabel.setText("Port              ");
		hostLabel.setText("Host Name");
		passwordLabel.setVerifyInputWhenFocusTarget(true);
		passwordLabel.setText("Password");
		userLabel.setText("User Name");
		userTextField.setPreferredSize(new Dimension(6, 21));
		userTextField.setText("");
		userTextField.setColumns(20);
		userTextField.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				userTextField_keyPressed(e);
			}
		});
		userTextField.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				userTextField_actionPerformed(e);
			}
		});
		passwordTextField.setText("");
		passwordTextField.setColumns(20);
		passwordTextField.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				passwordTextField_keyPressed(e);
			}
		});
		hostTextField.setCaretPosition(0);
		hostTextField.setText("");
		hostTextField.setColumns(20);
		hostTextField.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				hostTextField_keyPressed(e);
			}
		});
		this.setLayout(gridLayout1);
		gridLayout1.setColumns(1);
		gridLayout1.setRows(0);
		testButton.setText("Test Connection");
		testButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				testButton_actionPerformed(e);
			}
		});
		testLabel.setText(NOT_TESTED);
		portPanel.setLayout(flowLayout1);
		portPanel.setMinimumSize(new Dimension(400, 50));
		portPanel.setInputVerifier(null);
		flowLayout1.setAlignment(FlowLayout.LEFT);
		this.add(portPanel, null);
		portPanel.add(portLabel, null);
		portPanel.add(portTextField, null);
		this.add(hostPanel, null);
		hostPanel.add(hostLabel, null);
		hostPanel.add(hostTextField, null);
		this.add(userPanel, null);
		userPanel.add(userLabel, null);
		userPanel.add(userTextField, null);
		this.add(passwordPanel, null);
		passwordPanel.add(passwordLabel, null);
		passwordPanel.add(passwordTextField, null);
		this.add(testButton, null);
		this.add(testLabel, null);
	}

	void testButton_actionPerformed(ActionEvent e) {
		EmailToXenaEmailNormaliser n = (EmailToXenaEmailNormaliser)configure.getNormaliser();
		try {
			finish();
			configure.getInputSource().setSystemId("imap://" + hostTextField.getText() + ":" + portTextField.getText());
			n.setUserName(userTextField.getText());
			n.setPassword(passwordTextField.getText());
			Store store = n.getStore(configure.getInputSource().getType(), configure.getInputSource());
			testLabel.setText(OK_TESTED);
			configure.nextOk(true);
		} catch (MessagingException ex) {
			testLabel.setText(BAD_TESTED);
			MainFrame.singleton().showError(ex);
		} catch (XenaException x) {
			testLabel.setText(BAD_TESTED);
			MainFrame.singleton().showError(x);
		}
	}

	void notTested() {
		testLabel.setText(NOT_TESTED);
		configure.nextOk(false);
	}

	void userTextField_actionPerformed(ActionEvent e) {
	}

	void hostTextField_keyPressed(KeyEvent e) {
		notTested();
	}

	void portTextField_keyPressed(KeyEvent e) {
		notTested();
	}

	void passwordTextField_keyPressed(KeyEvent e) {
		notTested();
	}

	void userTextField_keyPressed(KeyEvent e) {
		notTested();
	}
}
