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
 * @author Dan Spasojevic
 * @author Justin Waddell
 */

/*
 * Created on 13/09/2005 andrek24
 * 
 */
package au.gov.naa.digipres.xena.kernel.plugin;

import java.io.File;
import java.util.StringTokenizer;

public class PluginLocator {

	/**
	 * Search for plugins, either in <directory-containing-xena>/plugins or
	 * <user.dir>/plugins.
	 * 
	 * @return The homePluginDir value
	 */
	public static File getHomePluginDir() {
		return new File(getHomeDir(), "plugins");
	}

	/**
	 * externalDir is a directory for storing external files that Xena needs
	 * that are architecture independant. An example might be if a plugin needs
	 * to start an external java program to do its work, the jars could be
	 * stored here.
	 * 
	 * @return File
	 */
	public static File getExternalDir() {
		return new File(getHomeDir(), "external");
	}

	/**
	 * Get the File that represents the xena.jar file this program is running
	 * from Depending on how Xena is instantiated, this may not be possible, but
	 * if it is possible, we can use it to find the plugins directory.
	 * 
	 * TODO: Remove hard-coded reference to "xena.jar"
	 * @return File
	 */
	public static File getHomeJar() {
		String s = System.getProperty("java.class.path");
		StringTokenizer st = new StringTokenizer(s, System.getProperty("path.separator"));
		while (st.hasMoreTokens()) {
			s = st.nextToken().toLowerCase();
			if (s.endsWith("xena.jar")) {
				File file = new File(s);
				return file;
			}
		}
		return null;
	}

	/**
	 * The homeDir is a directory where Xena hopes to find stuff, like plugins
	 * and external binary programs that it depends on. In the normal situation
	 * the homeDir will be the directory that xena.jar exists in. But if you
	 * start Xena with relative path names, it has to use the current working
	 * directory (user.dir) to find the home directory.
	 * 
	 * @return File
	 */
	public static File getHomeDir() {
		File homeJar = getHomeJar();
		File rtn = null;
		if (homeJar == null) {
			rtn = new File(System.getProperty("user.home"));
		} else {
			rtn = homeJar.getParentFile();
			if (rtn == null) {
				// "." directory
				rtn = new File(System.getProperty("user.dir"));
			} else if (!rtn.isAbsolute()) {
				rtn = new File(System.getProperty("user.dir"), rtn.toString());
			}
		}
		return rtn;
	}

	/**
	 * The binDir is a directory where we find external executables that are
	 * architecture dependent. Xena itself doesn't rely on external executables,
	 * but various plugins may.
	 * 
	 * @return File
	 */
	public static File getBinDir() {
		String machine = System.getProperty("os.arch").toLowerCase();
		if (4 < machine.length()) {
			machine = machine.substring(0, 4);
		}
		String os = System.getProperty("os.name").toLowerCase();
		if (3 < os.length()) {
			os = os.substring(0, 3);
		}

		return new File(getHomeDir(), os + machine);
	}

}
