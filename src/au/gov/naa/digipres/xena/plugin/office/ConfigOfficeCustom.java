package au.gov.naa.digipres.xena.plugin.office;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JMenuItem;

import au.gov.naa.digipres.xena.gui.CustomMenuItem;
import au.gov.naa.digipres.xena.gui.MainFrame;
import au.gov.naa.digipres.xena.javatools.JarPreferences;
import au.gov.naa.digipres.xena.kernel.properties.PropertyMessageException;
import au.gov.naa.digipres.xena.plugin.office.ConfigOpenOffice.AlreadyDoneException;

/**
 * Menu item for configuring the Office plugin.
 *
 * @author Chris Bitmead
 */
public class ConfigOfficeCustom extends CustomMenuItem {

	public static final String OOO_DIR_PREF = "oooDirectory";

	public ConfigOfficeCustom() {
		setMenuItem(new JMenuItem("Office"));
		setPath(MainFrame.EDIT_PREFERENCES_PLUGINS_MENU);
	}

	public void actionPerformed(ActionEvent e) {
		// We have to create the dialog here, because if we create it in the
		// class, it doesn't get the right look and feel.
		ConfigOfficeDialog dialog = new ConfigOfficeDialog();
		dialog.setLocationRelativeTo(MainFrame.singleton());
		dialog.setModal(true);

		JarPreferences prefs = (JarPreferences)JarPreferences.userNodeForPackage(ConfigOfficeCustom.class);
		dialog.setOooDirectory(new File(prefs.get(OOO_DIR_PREF, ConfigOfficeDialog.DEFAULT_DIR)));
		dialog.setVisible(true);
		if (dialog.isOk()) {
			ConfigOpenOffice conf = new ConfigOpenOffice();
			conf.setInstallDir(dialog.getOooDirectory());
			
			try
			{
				conf.modify();
			}
			catch (IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			catch (AlreadyDoneException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			catch (PropertyMessageException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			prefs.put(OOO_DIR_PREF, dialog.getOooDirectory().toString());
		}
	}
}
