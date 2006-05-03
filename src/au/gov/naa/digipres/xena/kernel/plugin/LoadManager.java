package au.gov.naa.digipres.xena.kernel.plugin;
import au.gov.naa.digipres.xena.javatools.JarPreferences;
import au.gov.naa.digipres.xena.kernel.XenaException;

/**
 * An interface for classes which wish to load plugin classes from a jar.
 */
public interface LoadManager {
	/**
	 * Load classes from a plugin for this load Manager.
	 * @param preferences The preferences file which describes this plugin.
	 * @return Whether anything was successfully loaded.
	 * @throws XenaException
	 */
	public boolean load(JarPreferences preferences) throws XenaException;

	/**
	 * Method which is called after all the class loading is complete. This is to allow
     * the load manager to perform any housekeeping required during loading.
	 * @throws XenaException
	 */
	public void complete() throws XenaException;
}
