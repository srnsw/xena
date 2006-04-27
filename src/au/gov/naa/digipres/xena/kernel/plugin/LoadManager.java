package au.gov.naa.digipres.xena.kernel.plugin;
import au.gov.naa.digipres.xena.javatools.JarPreferences;
import au.gov.naa.digipres.xena.kernel.XenaException;

/**
 * An interface for classes which wish to load plugin classes from a jar.
 */
public interface LoadManager {
	/**
	 * Load classes from a plugin.
	 * @param preferences The preferences file which describes this plugin.
	 * @return Whether anything was successfully loaded.
	 * @throws XenaException
	 */
	public boolean load(JarPreferences preferences) throws XenaException;

	/**
	 * Method which is called after all the class loading is complete. Gives the
	 * manager class time for consolidating its results.
	 * @throws XenaException
	 */
	public void complete() throws XenaException;
}
