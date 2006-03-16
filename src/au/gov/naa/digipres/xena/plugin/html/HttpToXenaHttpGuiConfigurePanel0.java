package au.gov.naa.digipres.xena.plugin.html;
import java.awt.GridLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.xml.sax.XMLReader;

import au.gov.naa.digipres.xena.gui.GuiConfigureSubPanel;
import au.gov.naa.digipres.xena.kernel.PluginManager;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;

/**
 * Panel 0 to configure the HttpToXenaHttpNormaliser.
 *
 * @author Chris Bitmead
 */
public class HttpToXenaHttpGuiConfigurePanel0 extends JPanel implements GuiConfigureSubPanel {

	HttpToXenaHttpGuiConfigure configure;

	private JPanel urlPanel = new JPanel();

	private JPanel jPanel3 = new JPanel();

	private JPanel hostsPanel = new JPanel();

	private JLabel hostsLabel = new JLabel();

	private JTextField hostsTextField = new JTextField();

	private Border border1;

	private TitledBorder titledBorder1;

	private JPanel protocolsPanel = new JPanel();

	private JTextField protocolsTextField = new JTextField();

	private JLabel protocolsLabel = new JLabel();

	private JPanel canLeaveHostPanel = new JPanel();

	private JCheckBox canLeaveHostCheckBox = new JCheckBox();

	private JLabel canLeaveHostLabel = new JLabel();

	private GridLayout gridLayout1 = new GridLayout();

	private GridLayout gridLayout2 = new GridLayout();

	private GridLayout gridLayout3 = new GridLayout();

	private JPanel jPanel1 = new JPanel();

	private JTextField urlTextField = new JTextField();

	private JLabel urlLabel = new JLabel();

	private JPanel jPanel2 = new JPanel();

	private JCheckBox continueCheckBox = new JCheckBox();

	private JLabel continueLabel = new JLabel();

	JPanel jPanel4 = new JPanel();

	JPanel jPanel5 = new JPanel();

	JPanel normaliserPanel = new JPanel();

	Border border2;

	TitledBorder titledBorder2;

	JCheckBox normaliserCheckBox = new JCheckBox();

	JComboBox normaliserComboBox = new JComboBox();

	JLabel jLabel1 = new JLabel();

	JPanel followLinksPanel = new JPanel();

	JLabel followLinksLabel = new JLabel();

	JCheckBox followLinksCheckBox = new JCheckBox();

	JPanel getResourcesPanel = new JPanel();

	JCheckBox getResourcesCheckBox = new JCheckBox();

	JLabel getResourcesLabel = new JLabel();

	public HttpToXenaHttpGuiConfigurePanel0(HttpToXenaHttpGuiConfigure configure) {
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
		HttpToXenaHttpNormaliser normaliser = (HttpToXenaHttpNormaliser)configure.getNormaliser();
		DefaultComboBoxModel model = (DefaultComboBoxModel)normaliserComboBox.getModel();
		model.removeAllElements();
		Type binaryType = PluginManager.singleton().getTypeManager().lookup("Binary");
		Iterator it = PluginManager.singleton().getNormaliserManager().lookupList(binaryType).iterator();
		while (it.hasNext()) {
			XMLReader norm = (XMLReader)PluginManager.singleton().getNormaliserManager().lookupByClass((Class)it.next());
			model.addElement(norm);
		}
		normaliserComboBox.setSelectedItem(PluginManager.singleton().getNormaliserManager().lookupByClassName(normaliser.getForceNormaliser()));
		normaliserCheckBox.setSelected(normaliser.isForced());
		normaliserCheckBox_actionPerformed(null);
		if (normaliser.getUrl() != null) {
			urlTextField.setText(normaliser.getUrl().toString());
		}
		hostsTextField.setText(stringOrNull(normaliser.getHostPattern()));
		protocolsTextField.setText(stringOrNull(normaliser.getProtocolPattern()));
		canLeaveHostCheckBox.setSelected(normaliser.isCanLeaveHost());
		continueCheckBox.setSelected(normaliser.isContinueRun());
		canLeaveHostCheckBox_actionPerformed(null);
		followLinksCheckBox.setSelected(normaliser.isFollowLinks());
		getResourcesCheckBox.setSelected(normaliser.isGetResources());
	}

	String nullOrString(String s) {
		if (s == null || s.equals("")) {
			return null;
		} else {
			return s;
		}
	}

	String stringOrNull(String s) {
		if (s == null) {
			return "";
		} else {
			return s;
		}
	}

	public void finish() throws XenaException {
		HttpToXenaHttpNormaliser normaliser = (HttpToXenaHttpNormaliser)configure.getNormaliser();
		normaliser.setForced(normaliserCheckBox.isSelected());
		normaliser.setForceNormaliser(normaliserComboBox.getSelectedItem().getClass().getName());
		URL url = null;
		try {
			url = new URL(urlTextField.getText());
		} catch (MalformedURLException e) {
			throw new XenaException("Invalid URL", e);
		}
		normaliser.setUrl(url);
		normaliser.setCanLeaveHost(canLeaveHostCheckBox.isSelected());
		normaliser.setHostPattern(nullOrString(hostsTextField.getText()));
		normaliser.setProtocolPattern(nullOrString(protocolsTextField.getText()));
		normaliser.setContinueRun(continueCheckBox.isSelected());
		normaliser.setFollowLinks(followLinksCheckBox.isSelected());
		normaliser.setGetResources(getResourcesCheckBox.isSelected());
	}

	private void jbInit() throws Exception {
		border1 = BorderFactory.createLineBorder(SystemColor.controlText, 1);
		titledBorder1 = new TitledBorder(border1, "Follow links");
		border2 = BorderFactory.createLineBorder(SystemColor.controlText, 1);
		titledBorder2 = new TitledBorder(border2, "Force Normaliser");
		this.setLayout(gridLayout2);
		hostsLabel.setToolTipText("");
		hostsLabel.setText("Hosts      ");
		hostsTextField.setCaretPosition(0);
		hostsTextField.setText("");
		hostsTextField.setColumns(30);
		jPanel3.setBorder(titledBorder1);
		jPanel3.setInputVerifier(null);
		jPanel3.setLayout(gridLayout1);
		protocolsLabel.setText("Protocols");
		protocolsTextField.setCaretPosition(0);
		protocolsTextField.setText("");
		protocolsTextField.setColumns(30);
		canLeaveHostLabel.setText("Can Leave Host");
		canLeaveHostCheckBox.setText("");
		canLeaveHostCheckBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				canLeaveHostCheckBox_actionPerformed(e);
			}
		});
		gridLayout1.setColumns(1);
		gridLayout1.setRows(0);
		gridLayout2.setColumns(1);
		gridLayout2.setRows(0);
		urlPanel.setLayout(gridLayout3);
		gridLayout3.setColumns(1);
		gridLayout3.setHgap(0);
		gridLayout3.setRows(0);
		urlTextField.setColumns(30);
		urlLabel.setText("URL");
		continueLabel.setText("Resume Previous Run?");
		continueCheckBox.setText("");
		normaliserPanel.setBorder(titledBorder2);
		normaliserCheckBox.setText("Force Normaliser");
		normaliserCheckBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				normaliserCheckBox_actionPerformed(e);
			}
		});
		followLinksLabel.setText("Follow Links?");
		getResourcesLabel.setText("Get Images and Resources");
		getResourcesCheckBox.setText("");
		canLeaveHostPanel.add(canLeaveHostLabel, null);
		canLeaveHostPanel.add(canLeaveHostCheckBox, null);
		urlPanel.add(jPanel1, null);
		jPanel1.add(urlLabel, null);
		jPanel1.add(urlTextField, null);
		urlPanel.add(jPanel2, null);
		urlPanel.add(normaliserPanel, null);
		jPanel2.add(continueLabel, null);
		jPanel2.add(continueCheckBox, null);
		this.add(urlPanel, null);
		this.add(jPanel3, null);
		jPanel3.add(getResourcesPanel, null);
		jPanel3.add(followLinksPanel, null);
		jPanel3.add(protocolsPanel, null);
		jPanel3.add(canLeaveHostPanel, null);
		jPanel3.add(hostsPanel, null);
		protocolsPanel.add(protocolsLabel, null);
		protocolsPanel.add(protocolsTextField, null);
		hostsPanel.add(hostsLabel, null);
		hostsPanel.add(hostsTextField, null);
		normaliserPanel.add(normaliserCheckBox, null);
		normaliserPanel.add(normaliserComboBox, null);
		followLinksPanel.add(followLinksLabel, null);
		followLinksPanel.add(followLinksCheckBox, null);
		getResourcesPanel.add(getResourcesLabel, null);
		getResourcesPanel.add(getResourcesCheckBox, null);
	}

	void canLeaveHostCheckBox_actionPerformed(ActionEvent e) {
		hostsLabel.setEnabled(canLeaveHostCheckBox.isSelected());
		hostsTextField.setEnabled(canLeaveHostCheckBox.isSelected());
	}

	void normaliserCheckBox_actionPerformed(ActionEvent e) {
		HttpToXenaHttpNormaliser normaliser = (HttpToXenaHttpNormaliser)configure.getNormaliser();
		normaliserComboBox.setEnabled(normaliserCheckBox.isSelected());
	}
}
