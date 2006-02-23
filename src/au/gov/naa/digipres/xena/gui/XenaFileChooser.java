package au.gov.naa.digipres.xena.gui;
//import javatools.swing.SimpleFileFilter;
import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import au.gov.naa.digipres.xena.javatools.JarPreferences;
import au.gov.naa.digipres.xena.kernel.filenamer.FileNamer;
import au.gov.naa.digipres.xena.kernel.filenamer.FileNamerManager;

/**
 * A JFileChooser that saves the last directory accessed. There are a number of
 * "last directories" depending on the context. For example there is a last open
 * directory, a last save directory, a last export directory etc.
 * @author Chris Bitmead.
 */
public class XenaFileChooser extends JFileChooser {
	public final static String DEFAULT_OPEN_DIRECTORY = "defaultOpenDirectory";

	public final static String DEFAULT_SAVE_DIRECTORY = "defaultSaveDirectory";

	public final static String DEFAULT_CONFIG_DIRECTORY = "defaultConfigDirectory";

	public final static String DEFAULT_EXPORT_DIRECTORY = "defaultExportDirectory";

	static JarPreferences prefs = (JarPreferences)JarPreferences.userNodeForPackage(XenaFileChooser.class);

	protected javax.swing.filechooser.FileFilter xenaFilter;

	protected javax.swing.filechooser.FileFilter normaliseFilter;

	protected javax.swing.filechooser.FileFilter basicFilter = getChoosableFileFilters()[0];

	protected String defaultDirectoryPrefName;

	/**
	 * Constructor
	 * @param prefName A Java preference name in which to store the last accessed directory.
	 */
	public XenaFileChooser(String prefName) throws IOException {
		final FileNamer fn = FileNamerManager.singleton().getFileNamerFromPrefs();
		if (fn != null) {
			xenaFilter = new FileFilter() {
				java.io.FileFilter ff = fn.makeFileFilter(FileNamer.XENA_DEFAULT_EXTENSION);

				public void FileFilter() {
				}

				public boolean accept(File f) {
					return ff.accept(f);
				}

				public String getDescription() {
					return "Xena Files";
				}
			};
			normaliseFilter = new FileFilter() {
				java.io.FileFilter ff = fn.makeFileFilter(FileNamer.XENA_CONFIG_EXTENSION);

				public boolean accept(File f) {
					return ff.accept(f);
				}

				public String getDescription() {
					return "Normaliser Config";
				}
			};
		}
		addChoosableFileFilter(xenaFilter);
		addChoosableFileFilter(normaliseFilter);
		setFileFilter(basicFilter);
		setDefaultDirectoryPrefName(prefName);
	}

	public static void main(String[] args) throws IOException {
		XenaFileChooser xfc = new XenaFileChooser("foo");
		xfc.showOpenDialog(null);
	}

	public void setDefaultDirectoryPrefName(String prefName) {
		this.defaultDirectoryPrefName = prefName;
		File def = defaultOpenDirectory(prefName);
		if (def != null) {
			this.setCurrentDirectory(def);
		}
	}

	/**
	 * The default directory.
	 */
	public static File defaultOpenDirectory(String prefName) {
		String defaultOpenDirectory = prefs.get(prefName, System.getProperty("user.home"));
		if (defaultOpenDirectory != null) {
			return new File(defaultOpenDirectory);
		} else {
			return null;
		}
	}

	/**
	 * Open Dialog that saves last directory.
	 */
	public int showOpenDialog(Component parent) throws java.awt.HeadlessException {
		int rtn = super.showOpenDialog(parent);
		if (getSelectedFile() != null) {
			savePrefs(getSelectedFile());
		}
		return rtn;
	}

	void savePrefs(File file) {
		if (getSelectedFile().isDirectory()) {
			prefs.put(defaultDirectoryPrefName, getSelectedFile().toString());
		} else {
			prefs.put(defaultDirectoryPrefName, getSelectedFile().getParent());
		}
	}

	/**
	 * Save Dialog that saves last directory.
	 */
	public int showSaveDialog(Component parent) throws java.awt.HeadlessException {
		int rtn = super.showSaveDialog(parent);
		if (getSelectedFile() != null) {
			savePrefs(getSelectedFile());
		}
		return rtn;
	}

	public int showDialog(Component parent, String approveButtonText) throws HeadlessException {
		int rtn = super.showDialog(parent, approveButtonText);
		if (getSelectedFile() != null) {
			savePrefs(getSelectedFile());
		}
		return rtn;
	}
}
