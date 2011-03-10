/**
 * This file is part of DPR.
 * 
 * DPR is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * DPR is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DPR; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package au.gov.naa.digipres.xena.util;

import java.awt.Component;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;

/**
 * An extension of JFileChooser that remembers the previously selected file or directory for the
 * given class name and field name combination, and displays it by default. Java Preferences are
 * used to store the previously selected files and directories.
 * 
 * @author Justin Waddell
 *
 */
public class MemoryFileChooser extends JFileChooser {

	private static final long serialVersionUID = 1L;
	private Preferences preferences = Preferences.userNodeForPackage(this.getClass());

	public int showOpenDialog(Component parent, Class<?> callingClass, String fieldName) {
		// A (hopefully) unique combination of calling class and field name
		String preferencesNodeName = callingClass.getName() + "." + fieldName;

		// Set the current directory of the FileChooser to the previously selected file or directory.
		// The blank string is used if there was no previous choice, which will mean the FileChooser shows the default directory.
		String previouslySelectedFile = loadPreference(preferencesNodeName);
		setCurrentDirectory(new File(previouslySelectedFile));

		int returnValue = super.showOpenDialog(parent);
		if (returnValue == APPROVE_OPTION) {
			savePreference(preferencesNodeName);
		}

		return returnValue;
	}

	public int showSaveDialog(Component parent, Class<?> callingClass, String fieldName, String defaultFileName) {
		// A (hopefully) unique combination of calling class and field name
		String preferencesNodeName = callingClass.getName() + "." + fieldName;

		// Set the current directory of the FileChooser to the previously selected file or directory.
		// The blank string is used if there was no previous choice, which will mean the FileChooser shows the default directory.
		String previouslySelectedDir = loadPreference(preferencesNodeName);
		setCurrentDirectory(new File(previouslySelectedDir));

		// Use defaultFileName if it is not null or empty
		if (defaultFileName != null || !"".equals(defaultFileName)) {
			setSelectedFile(new File(defaultFileName));
		}

		int returnValue = super.showSaveDialog(parent);
		if (returnValue == APPROVE_OPTION) {
			savePreference(preferencesNodeName);
		}

		return returnValue;
	}

	public int showSaveDialog(Component parent, Class<?> callingClass, String fieldName) {
		return showSaveDialog(parent, callingClass, fieldName, null);
	}

	/**
	 * Save the selected directory in Java Preferences. Check that we have not exceeded the maximum preferences key length.
	 * @param preferencesNodeName
	 */
	private void savePreference(String preferencesNodeName) {
		String preferencesKey = preferencesNodeName;

		// If preferencesNodeName is too long, strip out characters from the start of the String to make it the right length
		if (preferencesKey.length() > Preferences.MAX_KEY_LENGTH) {
			preferencesKey = preferencesKey.substring(preferencesKey.length() - Preferences.MAX_KEY_LENGTH);
		}

		preferences.put(preferencesKey, getCurrentDirectory().getAbsolutePath());
	}

	/**
	 * Return the value of the given preferences key. Check that we have not exceeded the maximum preferences key length.
	 * @param preferencesNodeName
	 * @return
	 */
	private String loadPreference(String preferencesNodeName) {
		String preferencesKey = preferencesNodeName;

		// If preferencesNodeName is too long, strip out characters from the start of the String to make it the right length
		if (preferencesKey.length() > Preferences.MAX_KEY_LENGTH) {
			preferencesKey = preferencesKey.substring(preferencesKey.length() - Preferences.MAX_KEY_LENGTH);
		}

		return preferences.get(preferencesKey, "");
	}

}
