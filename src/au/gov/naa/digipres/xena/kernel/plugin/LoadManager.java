/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Xena; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * @author Andrew Keeling
 * @author Chris Bitmead
 * @author Justin Waddell
 */

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
