package au.gov.naa.digipres.xena.plugin.plaintext;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.swing.DefaultListModel;
import javax.swing.JMenuItem;

import au.gov.naa.digipres.xena.gui.CustomMenuItem;
import au.gov.naa.digipres.xena.gui.MainFrame;
import au.gov.naa.digipres.xena.javatools.JarPreferences;

/**
 * Configure the plaintext plugin.
 * @author Chris Bitmead
 */
public class ConfigPlaintextCustom extends CustomMenuItem {

	public static final String CHARSETS_PREF = "disabledCharsets";

	public ConfigPlaintextCustom() {
		setMenuItem(new JMenuItem("PlainText"));
		setPath(MainFrame.EDIT_PREFERENCES_PLUGINS_MENU);
	}

	public void actionPerformed(ActionEvent e) {
		JarPreferences prefs = (JarPreferences)JarPreferences.userNodeForPackage(ConfigPlaintextCustom.class);
		// We have to create the diolog here, because if we create it in the
		// class, it doesn't get the right look and feel.
		ConfigPlaintextDialog dialog = new ConfigPlaintextDialog();
		dialog.setLocationRelativeTo(MainFrame.singleton());
		dialog.setModal(true);
		SortedMap charsets = java.nio.charset.Charset.availableCharsets();
		DefaultListModel lmodel = ((DefaultListModel)dialog.listChooser.getLeftList().getModel());
		DefaultListModel rmodel = ((DefaultListModel)dialog.listChooser.getRightList().getModel());
		Set dcharsets = new HashSet(prefs.getList(CHARSETS_PREF, new ArrayList()));
		Iterator it = charsets.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			if (dcharsets.contains(entry.getKey())) {
				rmodel.addElement(entry.getKey());
			} else {
				lmodel.addElement(entry.getKey());
			}
		}
		MainFrame.packAndPosition(dialog);
		dialog.setVisible(true);
		if (dialog.isOk()) {
			Enumeration en = rmodel.elements();
			List saved = new ArrayList();
			while (en.hasMoreElements()) {
				saved.add((String)en.nextElement());
			}
			prefs.putList(CHARSETS_PREF, saved);
			
			// JRW - don't think this is needed as the preferences are never read
//			PlainTextGuesser.reloadDisabledCharsets();
		}
	}
}
