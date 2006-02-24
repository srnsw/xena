package au.gov.naa.digipres.xena.plugin.xml;
import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;

import au.gov.naa.digipres.xena.gui.CustomMenuItem;
import au.gov.naa.digipres.xena.gui.MainFrame;
import au.gov.naa.digipres.xena.javatools.JarPreferences;

/**
 * Configure the XML plugin menu item.
 *
 * @author Chris Bitmead
 */
public class ConfigureXmlCustom extends CustomMenuItem {

	static final String ALLOW_RAW = "allowRawXmlSubView";

	static final String ALLOW_TREE = "allowTreeXmlSubView";

	public ConfigureXmlCustom() {
		this.setMenuItem(new JMenuItem("XML"));
		this.setPath(MainFrame.EDIT_PREFERENCES_PLUGINS_MENU);
	}

	public void actionPerformed(ActionEvent e) {
		// We have to create the diolog here, because if we create it in the
		// class, it doesn't get the right look and feel.
		ConfigureXmlDialog dialog = new ConfigureXmlDialog();
		dialog.setLocationRelativeTo(MainFrame.singleton());
		dialog.setModal(true);

		JarPreferences prefs = (JarPreferences)JarPreferences.userNodeForPackage(ConfigureXmlCustom.class);
		dialog.setAllowRawXml(prefs.getBoolean(ALLOW_RAW, false));
		dialog.setAllowTreeXml(prefs.getBoolean(ALLOW_TREE, false));
		dialog.setVisible(true);
		if (dialog.isOk()) {
			prefs.putBoolean(ALLOW_RAW, dialog.getAllowRawXml());
			prefs.putBoolean(ALLOW_TREE, dialog.getAllowTreeXml());
		}
	}
}
