package au.gov.naa.digipres.xena.plugin.plaintext;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import au.gov.naa.digipres.xena.gui.GuiConfigureSubPanel;
import au.gov.naa.digipres.xena.kernel.CharsetDetector;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;

/**
 * Panel 0 to configure the PlainTextToXenaPlainTextNormaliser.
 *
 * @author Chris Bitmead
 */
public class PlainTextConfigurePanel0 extends JPanel implements GuiConfigureSubPanel {
	PlainTextGuiConfigure configure;

	private Box vbox = Box.createVerticalBox();

	private TitledBorder titledBorder1;

	private JPanel jPanel1 = new JPanel();

	private JPanel jPanel2 = new JPanel();

	private Border border1;

	private Border border2;

	private JRadioButton radioButton2 = new JRadioButton();

	private JRadioButton radioButton1 = new JRadioButton();

	private JTextField tabSpaceField = new JTextField();

	private BorderLayout borderLayout2 = new BorderLayout();

	private BorderLayout borderLayout1 = new BorderLayout();

	private ButtonGroup group = new ButtonGroup();

	private FlowLayout flowLayout1 = new FlowLayout();

	private JPanel panel = new JPanel();

	JComboBox charsetComboBox = new JComboBox(java.nio.charset.Charset.availableCharsets().keySet().toArray());

	Border border3;

	Border border4;

	TitledBorder titledBorder2;

	public static final String DEFAULT_CHARSET = "US-ASCII";

	public PlainTextConfigurePanel0(PlainTextGuiConfigure configure) {
		this.configure = configure;
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void activate() {
		PlainTextToXenaPlainTextNormaliser n = (PlainTextToXenaPlainTextNormaliser)configure.getNormaliser();
		radioButton1.setSelected(n.getTabSize() == null);
		tabSpaceField.setEnabled(n.getTabSize() != null);
		radioButton2.setSelected(n.getTabSize() != null);
		String charset = null;
		if (n.getEncoding() == null) {
			try {
				if (configure.getInputSource() == null) {
					charset = DEFAULT_CHARSET;
				} else {
					charset = CharsetDetector.guessCharSet(configure.getInputSource().getByteStream(), 2 ^ 16);
					if (charset == null)
					{
						charset = DEFAULT_CHARSET;
					}
				}
			} catch (IOException ex) {
				charset = DEFAULT_CHARSET;
			}
		} else {
			charset = n.getEncoding();
		}
		charsetComboBox.setSelectedItem(charset);
		if (n.getTabSize() != null) {
			tabSpaceField.setText(n.getTabSize().toString());
		}
	}

	public void finish() throws XenaException {
		PlainTextToXenaPlainTextNormaliser n = (PlainTextToXenaPlainTextNormaliser)configure.getNormaliser();
		XenaInputSource is = (XenaInputSource)configure.getInputSource();
		n.setEncoding((String)charsetComboBox.getSelectedItem());
		if (radioButton1.isSelected()) {
			n.setTabSize(null);
		} else {
			try {
				n.setTabSize(new Integer(tabSpaceField.getText()));
			} catch (NumberFormatException e) {
				throw new XenaException("Bad Number for number of Tab Spaces: " + tabSpaceField.getText());
			}
		}
	}

	public void start() throws XenaException {
	}

	void jbInit() throws Exception {
		PlainTextToXenaPlainTextNormaliser n = (PlainTextToXenaPlainTextNormaliser)configure.getNormaliser();
		titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151)),
										 "Characters per Tab Space");
		border1 = BorderFactory.createLineBorder(SystemColor.controlText, 1);
		border2 = BorderFactory.createLineBorder(SystemColor.controlText, 1);
		border4 = BorderFactory.createLineBorder(Color.white, 1);
		titledBorder2 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(178, 178, 178)), "Character Set");
		charsetComboBox.setBorder(titledBorder2);
		this.add(vbox);
		panel.setBorder(titledBorder1);
		panel.setLayout(flowLayout1);
		jPanel1.setLayout(borderLayout1);
		jPanel2.setLayout(borderLayout2);
		radioButton2.setText("Specified");
		radioButton1.setSelected(true);
		radioButton1.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				radioButton1_actionPerformed(e);
			}
		});
		tabSpaceField.setColumns(3);
		group.add(radioButton2);
		radioButton2.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				radioButton2_actionPerformed(e);
			}
		});
		radioButton1.setText("Unspecified");
		group.add(radioButton1);
		tabSpaceField.setEnabled(false);
		tabSpaceField.setText("8");
		panel.add(jPanel1, null);
		jPanel1.add(radioButton1, BorderLayout.WEST);
		jPanel2.add(tabSpaceField, BorderLayout.CENTER);
		panel.add(jPanel2, null);
		jPanel2.add(radioButton2, BorderLayout.WEST);
		vbox.add(charsetComboBox, null);
		vbox.add(panel, null);
	}

	void radioButton2_actionPerformed(ActionEvent e) {
		tabSpaceField.setEnabled(true);
	}

	void radioButton1_actionPerformed(ActionEvent e) {
		tabSpaceField.setEnabled(false);
	}

}
