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

package au.gov.naa.digipres.xena.javatools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class PluginLoader {
	JarPreferences props;

	ClassLoader cl;

	public PluginLoader(JarPreferences props) {
		this.props = props;
		this.cl = props.getClassLoader();
	}

	public PluginLoader(JarPreferences props, ClassLoader cl) {
		this.props = props;
		this.cl = cl;
	}

	/**
	 * Assumes that there is a property containing a list of things
	 * (probably, but not necessarily classes to load). This returns a
	 * StringTokenizer for parsing that list.
	 */
	public static StringTokenizer makeTokenizer(String names) {
		if (names == null) {
			names = "";
		}
		return new StringTokenizer(names, ", \t");
	}

	public List<Class> loadClasses(String propertyName) throws ClassNotFoundException {
		String classNames = props.get(propertyName, "");
		List<Class> rtn = new ArrayList<Class>();
		StringTokenizer st = makeTokenizer(classNames);
		while (st.hasMoreTokens()) {
			String className = st.nextToken();
			Class cls = Class.forName(className, true, cl);
			rtn.add(cls);
		}
		return rtn;
	}

	public List loadInstances(String propertyName) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		List classList = loadClasses(propertyName);
		Iterator it = classList.iterator();
		List<Object> rtn = new ArrayList<Object>();
		while (it.hasNext()) {
			Class cls = (Class) it.next();
			Object o = cls.newInstance();
			rtn.add(o);
		}
		return rtn;
	}
}
